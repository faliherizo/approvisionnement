# =============================================================================
# Restauration de PostgreSQL par GEO-RESTORE depuis les sauvegardes géo-redondantes de production.
# GeoRestore utilise la dernière sauvegarde géo-répliquée disponible dans la région DR (RPO ≤ 15 min).
# La HA zone-redondante est rétablie dans la région de reprise.
# =============================================================================
resource "azurerm_postgresql_flexible_server" "restored" {
  name                = "${local.name}-pg"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location

  create_mode      = "GeoRestore"
  source_server_id = var.source_postgres_server_id

  delegated_subnet_id = azurerm_subnet.postgres.id
  private_dns_zone_id = azurerm_private_dns_zone.postgres.id

  zone = "1"
  high_availability {
    mode                      = "ZoneRedundant"
    standby_availability_zone = "2"
  }

  geo_redundant_backup_enabled = true

  tags = local.common_tags

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]
}

# =============================================================================
# Coffre de secrets DR : chaîne de connexion vers la base restaurée + mot de passe (copié de la source).
# =============================================================================
resource "azurerm_key_vault" "this" {
  name                       = replace("${local.name}-kv", "-", "")
  location                   = azurerm_resource_group.this.location
  resource_group_name        = azurerm_resource_group.this.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  purge_protection_enabled   = true
  soft_delete_retention_days = 30
  rbac_authorization_enabled = true
  tags                       = local.common_tags
}

resource "azurerm_role_assignment" "kv_admin" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

resource "azurerm_key_vault_secret" "db_url" {
  name         = "db-url"
  key_vault_id = azurerm_key_vault.this.id
  value        = "jdbc:postgresql://${azurerm_postgresql_flexible_server.restored.fqdn}:5432/${var.db_name}"
  depends_on   = [azurerm_role_assignment.kv_admin]
}

resource "azurerm_key_vault_secret" "db_password" {
  name         = "db-password"
  key_vault_id = azurerm_key_vault.this.id
  value        = var.db_password
  depends_on   = [azurerm_role_assignment.kv_admin]
}
