# Groupe de ressources DR (Canada Est).
resource "azurerm_resource_group" "dr" {
  name     = "${local.dr_name}-rg"
  location = var.dr_location
  tags     = local.common_tags
}

# =============================================================================
# 1) Registre d'images dédié à la région DR (Premium, redondance par zones).
#    Le CI publie les images ici comme en production (dual-push), ou le runbook les importe
#    via `az acr import` depuis l'ACR primaire géo-répliqué. L'image est ainsi présente au failover.
# =============================================================================
resource "azurerm_container_registry" "dr" {
  name                = replace("${local.dr_name}acr", "-", "")
  resource_group_name = azurerm_resource_group.dr.name
  location            = azurerm_resource_group.dr.location
  sku                 = "Premium"
  admin_enabled       = false
  tags                = local.common_tags
}

# =============================================================================
# 2) Compte de stockage DR + réplication d'objets depuis la production.
#    La réplication d'objets exige, côté SOURCE et DESTINATION, le versionnage de blobs et le
#    « change feed ». Ils sont activés ici sur la destination ; le compte SOURCE doit les avoir
#    activés (voir runbook §prérequis — activation non intrusive sur le compte de production).
# =============================================================================
resource "azurerm_storage_account" "dr" {
  name                     = replace("${local.dr_name}sa", "-", "")
  resource_group_name      = azurerm_resource_group.dr.name
  location                 = azurerm_resource_group.dr.location
  account_tier             = "Standard"
  account_replication_type = "ZRS" # redondance inter-zones dans la région DR
  min_tls_version          = "TLS1_2"
  tags                     = local.common_tags

  blob_properties {
    versioning_enabled  = true
    change_feed_enabled = true
  }
}

resource "azurerm_storage_container" "documents" {
  name                  = "documents"
  storage_account_id    = azurerm_storage_account.dr.id
  container_access_type = "private"
}

resource "azurerm_storage_container" "exports" {
  name                  = "exports"
  storage_account_id    = azurerm_storage_account.dr.id
  container_access_type = "private"
}

# Réplication d'objets production -> DR (documents + exports).
resource "azurerm_storage_object_replication" "this" {
  source_storage_account_id      = data.azurerm_storage_account.src.id
  destination_storage_account_id = azurerm_storage_account.dr.id

  rules {
    source_container_name      = "documents"
    destination_container_name = azurerm_storage_container.documents.name
  }
  rules {
    source_container_name      = "exports"
    destination_container_name = azurerm_storage_container.exports.name
  }
}
