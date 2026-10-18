resource "azurerm_container_registry" "this" {
  name                = replace("${local.name}acr", "-", "")
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  sku                 = "Premium" # redondance par zones (cohérence HA) ; géo-réplication multi-région disponible si reprise inter-région requise
  admin_enabled       = false
  tags                = local.common_tags
}
