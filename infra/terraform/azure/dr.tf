# Reprise après sinistre inter-régions (SEC-11, ADR-039) : région de relève jumelle (Canada Est pour Canada Central).
#
# Régime nominal (« pilot light ») : la région de relève porte en continu la donnée et ce qu'il faut pour servir vite —
#   - réplique PostgreSQL en flux continu (postgres.tf) et sauvegardes géo-redondantes (voie de secours) ;
#   - copie RA-GZRS du stockage (storage.tf) et géo-réplique du registre (registry.tf) ;
#   - coffre de secrets de relève, dont la clé maîtresse du chiffrement par organisation (ADR-037) ;
#   - grappe AKS minimale, pool applicatif à zéro, et origine de priorité 2 de Front Door (frontdoor.tf).
# Bascule : infra/dr/scripts/bascule-azure.sh (promotion, agrandissement, déploiement, vérification). Procédure, rôles
# et résultats d'essai : document 20 de la réponse à l'AO ; essai mesuré : .github/workflows/releve.yml.
#
# Le groupe de ressources de relève est dans la région de relève : ses métadonnées restent administrables si la région
# primaire est perdue.
resource "azurerm_resource_group" "dr" {
  name     = "${local.dr_name}-rg"
  location = var.dr_location
  tags     = local.dr_tags

  lifecycle {
    precondition {
      condition     = var.dr_location != var.location
      error_message = "La région de relève doit différer de la région primaire (ADR-039)."
    }
  }
}

# --- Réseau de relève, appairé au réseau primaire ---

resource "azurerm_virtual_network" "dr" {
  name                = "${local.dr_name}-vnet"
  location            = azurerm_resource_group.dr.location
  resource_group_name = azurerm_resource_group.dr.name
  address_space       = ["10.60.0.0/16"] # disjoint de 10.50.0.0/16 : appairage possible
  tags                = local.dr_tags
}

resource "azurerm_subnet" "dr_aks" {
  name                 = "snet-aks"
  resource_group_name  = azurerm_resource_group.dr.name
  virtual_network_name = azurerm_virtual_network.dr.name
  address_prefixes     = ["10.60.0.0/20"]
}

resource "azurerm_subnet" "dr_postgres" {
  name                 = "snet-postgres"
  resource_group_name  = azurerm_resource_group.dr.name
  virtual_network_name = azurerm_virtual_network.dr.name
  address_prefixes     = ["10.60.16.0/24"]
  service_endpoints    = ["Microsoft.Storage"]
  delegation {
    name = "fs"
    service_delegation {
      name    = "Microsoft.DBforPostgreSQL/flexibleServers"
      actions = ["Microsoft.Network/virtualNetworks/subnets/join/action"]
    }
  }
}

# Appairage global : la réplique inter-régions en accès privé exige que les deux réseaux se joignent.
resource "azurerm_virtual_network_peering" "primaire_vers_dr" {
  name                      = "primaire-vers-dr"
  resource_group_name       = azurerm_resource_group.this.name
  virtual_network_name      = azurerm_virtual_network.this.name
  remote_virtual_network_id = azurerm_virtual_network.dr.id
  allow_forwarded_traffic   = false
}

resource "azurerm_virtual_network_peering" "dr_vers_primaire" {
  name                      = "dr-vers-primaire"
  resource_group_name       = azurerm_resource_group.dr.name
  virtual_network_name      = azurerm_virtual_network.dr.name
  remote_virtual_network_id = azurerm_virtual_network.this.id
  allow_forwarded_traffic   = false
}

# La zone DNS privée de PostgreSQL est globale : liée aussi au réseau de relève, elle résout la réplique et le point de
# terminaison virtuel depuis les deux régions.
resource "azurerm_private_dns_zone_virtual_network_link" "dr_postgres" {
  name                  = "postgres-link-dr"
  resource_group_name   = azurerm_resource_group.this.name
  private_dns_zone_name = azurerm_private_dns_zone.postgres.name
  virtual_network_id    = azurerm_virtual_network.dr.id
}

# --- Coffre de secrets de relève ---
# Key Vault réplique lui-même son contenu dans la région jumelle, mais en lecture seule pendant une panne et sans
# engagement de délai : un coffre de relève propre garantit que la clé maîtresse ADR-037 et les secrets de connexion
# sont disponibles à la bascule, en lecture comme en écriture. Les clés non exportables (RSA-HSM du profil azure-kms)
# y sont recopiées par sauvegarde et restauration (infra/dr/scripts/sauvegarder-cles-azure.sh).
resource "azurerm_key_vault" "dr" {
  name                       = replace("${local.dr_name}-kv", "-", "")
  location                   = azurerm_resource_group.dr.location
  resource_group_name        = azurerm_resource_group.dr.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  purge_protection_enabled   = true
  soft_delete_retention_days = 90
  rbac_authorization_enabled = true
  tags                       = local.dr_tags
  network_acls {
    default_action = "Deny"
    bypass         = "AzureServices"
    ip_rules       = var.ip_administration
  }
}

resource "azurerm_role_assignment" "kv_dr_admin" {
  scope                = azurerm_key_vault.dr.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

resource "azurerm_role_assignment" "kv_dr_cles" {
  scope                = azurerm_key_vault.dr.id
  role_definition_name = "Key Vault Crypto Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

locals {
  # Mêmes valeurs que le coffre primaire : la bascule ne change aucun secret de l'application.
  secrets_releve = {
    "db-url"               = azurerm_key_vault_secret.db_url.value
    "db-password"          = random_password.db.result
    "clm-cle-maitresse"    = random_bytes.clm_cle_maitresse.base64
    "clm-cle-maitresse-id" = var.clm_cle_maitresse_id
  }
}

resource "azurerm_key_vault_secret" "releve" {
  for_each     = toset(["db-url", "db-password", "clm-cle-maitresse", "clm-cle-maitresse-id"])
  name         = each.key
  key_vault_id = azurerm_key_vault.dr.id
  value        = local.secrets_releve[each.key]
  content_type = "text/plain"
  depends_on   = [azurerm_role_assignment.kv_dr_admin]
}

# --- Calcul de relève (pilot light par défaut) ---

resource "azurerm_log_analytics_workspace" "dr" {
  count               = local.dr_avec_aks ? 1 : 0
  name                = "${local.dr_name}-law"
  location            = azurerm_resource_group.dr.location
  resource_group_name = azurerm_resource_group.dr.name
  sku                 = "PerGB2018"
  retention_in_days   = 90
  tags                = local.dr_tags
}

# Grappe de relève : même version, même réseau, même identité de charge de travail que la production. En pilot light,
# plan de contrôle gratuit et un seul nœud système ; la bascule passe le plan de contrôle en Standard et agrandit le pool
# applicatif (bascule-azure.sh). En veille active, elle est dimensionnée comme la production.
resource "azurerm_kubernetes_cluster" "dr" {
  count                             = local.dr_avec_aks ? 1 : 0
  name                              = "${local.dr_name}-aks"
  location                          = azurerm_resource_group.dr.location
  resource_group_name               = azurerm_resource_group.dr.name
  dns_prefix                        = local.dr_name
  kubernetes_version                = var.kubernetes_version
  sku_tier                          = local.dr_veille ? "Standard" : "Free"
  oidc_issuer_enabled               = true
  workload_identity_enabled         = true
  role_based_access_control_enabled = true
  api_server_access_profile {
    authorized_ip_ranges = var.ip_administration
  }

  default_node_pool {
    name                         = "system"
    vm_size                      = "Standard_D2s_v5"
    zones                        = local.dr_zones_aks
    auto_scaling_enabled         = true
    min_count                    = local.dr_veille ? 2 : 1
    max_count                    = 3
    vnet_subnet_id               = azurerm_subnet.dr_aks.id
    only_critical_addons_enabled = true
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    network_policy    = "cilium"
    load_balancer_sku = "standard"
  }

  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.dr[0].id
  }

  tags = local.dr_tags
}

resource "azurerm_kubernetes_cluster_node_pool" "dr_app" {
  count                 = local.dr_avec_aks ? 1 : 0
  name                  = "app"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.dr[0].id
  vm_size               = var.aks_node_size
  zones                 = local.dr_zones_aks
  auto_scaling_enabled  = true
  min_count             = local.dr_veille ? var.aks_node_min : 0
  max_count             = var.dr_aks_node_max
  vnet_subnet_id        = azurerm_subnet.dr_aks.id
  node_labels           = { workload = "app" }
  tags                  = local.dr_tags
}

# La grappe de relève tire les images de la géo-réplique locale du registre.
resource "azurerm_role_assignment" "dr_aks_acr_pull" {
  count                = local.dr_avec_aks ? 1 : 0
  scope                = azurerm_container_registry.this.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_kubernetes_cluster.dr[0].kubelet_identity[0].object_id
}

# --- Surveillance de la posture de relève ---

# Retard de la réplique : un retard qui grandit consomme la marge de RPO ; l'exploitation est alertée bien avant
# l'objectif de 15 minutes.
resource "azurerm_monitor_metric_alert" "retard_replication" {
  count               = local.dr_replique ? 1 : 0
  name                = "${local.name}-retard-replication"
  resource_group_name = azurerm_resource_group.dr.name
  scopes              = [azurerm_postgresql_flexible_server.replique[0].id]
  description         = "Retard de la réplique de relève au-delà du seuil (marge de RPO, ADR-039)."
  severity            = 2
  frequency           = "PT1M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "Microsoft.DBforPostgreSQL/flexibleServers"
    metric_name      = "physical_replication_delay_in_seconds"
    aggregation      = "Maximum"
    operator         = "GreaterThan"
    threshold        = var.dr_retard_replication_alerte_s
  }
  action {
    action_group_id = azurerm_monitor_action_group.ops.id
  }
}
