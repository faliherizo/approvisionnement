# PostgreSQL Flexible Server en HA redondante par zones : primaire + veille synchrone dans une
# autre zone, bascule automatique (soutient le SLA 99,99 %). Intégré au VNet privé, chiffré.
resource "random_password" "db" {
  length  = 32
  special = false
}

resource "azurerm_postgresql_flexible_server" "this" {
  name                = "${local.name}-pg"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  version             = var.postgres_version
  sku_name            = var.postgres_sku
  storage_mb          = var.postgres_storage_mb

  administrator_login    = var.db_admin
  administrator_password = random_password.db.result

  delegated_subnet_id = azurerm_subnet.postgres.id
  private_dns_zone_id = azurerm_private_dns_zone.postgres.id

  # Avec zones : veille synchrone dans une autre zone. Sans zones (canadaeast) : veille synchrone dans la même zone,
  # la reprise régionale restant assurée par infra/dr (ADR-020).
  zone = length(var.zones) > 0 ? var.zones[0] : null
  high_availability {
    mode                      = length(var.zones) > 1 ? "ZoneRedundant" : "SameZone"
    standby_availability_zone = length(var.zones) > 1 ? var.zones[1] : null
  }

  backup_retention_days        = 14
  geo_redundant_backup_enabled = true
  auto_grow_enabled            = true

  tags = local.common_tags

  depends_on = [azurerm_private_dns_zone_virtual_network_link.postgres]
}

resource "azurerm_postgresql_flexible_server_database" "app" {
  name      = var.db_name
  server_id = azurerm_postgresql_flexible_server.this.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}
