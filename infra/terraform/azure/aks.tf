# Cluster AKS : pool système + pool applicatif, noeuds répartis sur les 3 zones de disponibilité
# (haute disponibilité multi-zone qui soutient le SLA 99,99 %). Identité managée + autoscaler.
resource "azurerm_kubernetes_cluster" "this" {
  name                      = "${local.name}-aks"
  location                  = azurerm_resource_group.this.location
  resource_group_name       = azurerm_resource_group.this.name
  dns_prefix                = local.name
  kubernetes_version        = var.kubernetes_version
  sku_tier                  = "Standard" # SLA du plan de contrôle (99,95 % avec zones)
  oidc_issuer_enabled       = true       # Workload Identity (pods → identités Azure)
  workload_identity_enabled = true

  default_node_pool {
    name                         = "system"
    vm_size                      = "Standard_D2s_v5"
    zones                        = ["1", "2", "3"]
    auto_scaling_enabled         = true
    min_count                    = 3
    max_count                    = 5
    vnet_subnet_id               = azurerm_subnet.aks.id
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
    log_analytics_workspace_id = azurerm_log_analytics_workspace.this.id
  }

  tags = local.common_tags
}

# Pool applicatif dédié (backend + frontend), réparti sur les 3 zones.
resource "azurerm_kubernetes_cluster_node_pool" "app" {
  name                  = "app"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.this.id
  vm_size               = var.aks_node_size
  zones                 = ["1", "2", "3"]
  auto_scaling_enabled  = true
  min_count             = var.aks_node_min
  max_count             = var.aks_node_max
  vnet_subnet_id        = azurerm_subnet.aks.id
  node_labels           = { workload = "app" }
  tags                  = local.common_tags
}

# Autorise AKS à tirer les images de l'ACR (attribution de rôle AcrPull).
resource "azurerm_role_assignment" "aks_acr_pull" {
  scope                = azurerm_container_registry.this.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_kubernetes_cluster.this.kubelet_identity[0].object_id
}
