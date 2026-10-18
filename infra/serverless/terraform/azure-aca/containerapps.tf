# Calcul : Azure Container Apps (ACA) — conteneurs managés « sans serveur », sans Kubernetes.
# Environnement redondant par zones ; backend (ingress interne) + frontend (ingress externe).
# Remplace la variante AKS (infra/terraform/azure) sans en modifier le contenu.

# Identité managée assignée par l'utilisateur : tirage ACR + lecture des secrets Key Vault.
resource "azurerm_user_assigned_identity" "apps" {
  name                = "${local.name}-apps-id"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  tags                = local.common_tags
}

resource "azurerm_role_assignment" "apps_acr_pull" {
  scope                = azurerm_container_registry.this.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_user_assigned_identity.apps.principal_id
}

resource "azurerm_role_assignment" "apps_kv" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.apps.principal_id
}

# Environnement Container Apps : intégré au VNet, redondant par zones, journaux vers Log Analytics.
resource "azurerm_container_app_environment" "this" {
  name                       = "${local.name}-cae"
  resource_group_name        = azurerm_resource_group.this.name
  location                   = azurerm_resource_group.this.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.this.id
  infrastructure_subnet_id   = azurerm_subnet.apps.id
  zone_redundancy_enabled    = true # réparti sur les zones de disponibilité (SLA 99,99 %)
  tags                       = local.common_tags
}

# Backend Spring Boot — ingress INTERNE (appelé par le frontend / Front Door).
resource "azurerm_container_app" "backend" {
  name                         = "backend"
  container_app_environment_id = azurerm_container_app_environment.this.id
  resource_group_name          = azurerm_resource_group.this.name
  revision_mode                = "Single"
  tags                         = local.common_tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.apps.id]
  }

  registry {
    server   = azurerm_container_registry.this.login_server
    identity = azurerm_user_assigned_identity.apps.id
  }

  # Secret BD injecté depuis Key Vault via l'identité managée.
  secret {
    name                = "db-password"
    key_vault_secret_id = azurerm_key_vault_secret.db_password.id
    identity            = azurerm_user_assigned_identity.apps.id
  }

  ingress {
    external_enabled = false # interne au VNet/environnement
    target_port      = var.backend_port
    transport        = "http"
    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  template {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas
    container {
      name   = "backend"
      image  = "${azurerm_container_registry.this.login_server}/vina-contrat/backend:${var.image_tag}"
      cpu    = var.backend_cpu
      memory = var.backend_memory
      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "prod"
      }
      env {
        name        = "DB_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name  = "KEY_VAULT_URI"
        value = azurerm_key_vault.this.vault_uri
      }
    }
  }
}

# Frontend React/Nginx — ingress EXTERNE (cible de Front Door).
resource "azurerm_container_app" "frontend" {
  name                         = "frontend"
  container_app_environment_id = azurerm_container_app_environment.this.id
  resource_group_name          = azurerm_resource_group.this.name
  revision_mode                = "Single"
  tags                         = local.common_tags

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.apps.id]
  }
  registry {
    server   = azurerm_container_registry.this.login_server
    identity = azurerm_user_assigned_identity.apps.id
  }

  ingress {
    external_enabled = true
    target_port      = var.frontend_port
    transport        = "auto"
    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  template {
    min_replicas = var.min_replicas
    max_replicas = var.max_replicas
    container {
      name   = "frontend"
      image  = "${azurerm_container_registry.this.login_server}/vina-contrat/frontend:${var.image_tag}"
      cpu    = var.frontend_cpu
      memory = var.frontend_memory
      env {
        name  = "BACKEND_URL"
        value = "https://${azurerm_container_app.backend.ingress[0].fqdn}"
      }
    }
  }
}
