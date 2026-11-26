# Registre d'images Premium : redondance par zones dans la région primaire et géo-réplique dans la région de relève
# (ADR-039). Même nom de connexion dans les deux régions : la région de relève tire ses images localement, sans
# dépendre de la région perdue, et chaque livraison y est présente dès sa publication.
resource "azurerm_container_registry" "this" {
  name                = replace("${local.name}acr", "-", "")
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  sku                 = "Premium"
  admin_enabled       = false
  tags                = local.common_tags

  georeplications {
    location                  = var.dr_location
    zone_redundancy_enabled   = false # Canada Est n'a pas de zones de disponibilité (ADR-020)
    regional_endpoint_enabled = true
    tags                      = local.dr_tags
  }
}
