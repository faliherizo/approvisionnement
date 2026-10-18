# Stockage : documents générés et exports (réversibilité). Redondance géo-zone (ZRS/GZRS).
resource "azurerm_storage_account" "this" {
  name                     = replace("${local.name}sa", "-", "")
  resource_group_name      = azurerm_resource_group.this.name
  location                 = azurerm_resource_group.this.location
  account_tier             = "Standard"
  account_replication_type = "ZRS" # redondance inter-zones
  min_tls_version          = "TLS1_2"
  tags                     = local.common_tags
}

resource "azurerm_storage_container" "documents" {
  name                  = "documents"
  storage_account_id    = azurerm_storage_account.this.id
  container_access_type = "private"
}

resource "azurerm_storage_container" "exports" {
  name                  = "exports"
  storage_account_id    = azurerm_storage_account.this.id
  container_access_type = "private"
}
