# =============================================================================
# Calcul de reprise : Azure Container Apps (backend interne + frontend externe), redondant par zones,
# images tirées de l'ACR DR.
# =============================================================================
resource "azurerm_user_assigned_identity" "apps" {
  name                = "${local.name}-apps-id"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  tags                = local.common_tags
}

resource "azurerm_role_assignment" "apps_acr_pull" {
  scope                = data.azurerm_container_registry.dr.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_user_assigned_identity.apps.principal_id
}

resource "azurerm_role_assignment" "apps_kv" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_user_assigned_identity.apps.principal_id
}

resource "azurerm_log_analytics_workspace" "this" {
  name                = "${local.name}-logs"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "PerGB2018"
  retention_in_days   = 90
  tags                = local.common_tags
}

resource "azurerm_container_app_environment" "this" {
  name                       = "${local.name}-cae"
  resource_group_name        = azurerm_resource_group.this.name
  location                   = azurerm_resource_group.this.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.this.id
  infrastructure_subnet_id   = azurerm_subnet.apps.id
  zone_redundancy_enabled    = true # réparti sur les zones (HA rétablie dans la région DR)
  tags                       = local.common_tags
}

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
    server   = data.azurerm_container_registry.dr.login_server
    identity = azurerm_user_assigned_identity.apps.id
  }
  secret {
    name                = "db-password"
    key_vault_secret_id = azurerm_key_vault_secret.db_password.id
    identity            = azurerm_user_assigned_identity.apps.id
  }

  ingress {
    external_enabled = false
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
      image  = "${data.azurerm_container_registry.dr.login_server}/vina-contrat/backend:${var.image_tag}"
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
    server   = data.azurerm_container_registry.dr.login_server
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
      image  = "${data.azurerm_container_registry.dr.login_server}/vina-contrat/frontend:${var.image_tag}"
      cpu    = var.frontend_cpu
      memory = var.frontend_memory
      env {
        name  = "BACKEND_URL"
        value = "https://${azurerm_container_app.backend.ingress[0].fqdn}"
      }
    }
  }
}
