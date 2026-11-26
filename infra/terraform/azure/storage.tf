# Stockage : documents générés et exports (réversibilité).
# Redondance géo-zone avec lecture de la copie secondaire (RA-GZRS) : trois zones dans la région primaire, copie
# asynchrone dans la région jumelle (Canada Est), lisible sans attendre la bascule du compte (ADR-039).
resource "azurerm_storage_account" "this" {
  name                            = replace("${local.name}sa", "-", "")
  resource_group_name             = azurerm_resource_group.this.name
  location                        = azurerm_resource_group.this.location
  account_tier                    = "Standard"
  account_replication_type        = local.stockage_redondance
  min_tls_version                 = "TLS1_2"
  allow_nested_items_to_be_public = false
  tags                            = local.common_tags

  blob_properties {
    # Versionnage + flux de modifications : toute écriture ou suppression garde la version précédente ; préalables de la
    # restauration à un instant donné.
    versioning_enabled            = true
    change_feed_enabled           = true
    change_feed_retention_in_days = var.stockage_retention_suppression_jours

    delete_retention_policy {
      days = var.stockage_retention_suppression_jours
    }

    container_delete_retention_policy {
      days = var.stockage_retention_suppression_jours
    }

    # Restauration à un instant donné des blobs (rançongiciel, suppression massive par erreur). Incompatible avec
    # l'immuabilité au niveau des versions : retirée si celle-ci est activée.
    dynamic "restore_policy" {
      for_each = var.stockage_immutabilite_jours == 0 ? [1] : []
      content {
        days = var.stockage_retention_suppression_jours - 1
      }
    }
  }

  # Immuabilité (WORM) optionnelle, déverrouillée : désactivée par défaut, voir la variable et l'ADR-039.
  dynamic "immutability_policy" {
    for_each = var.stockage_immutabilite_jours > 0 ? [1] : []
    content {
      allow_protected_append_writes = false
      state                         = "Unlocked"
      period_since_creation_in_days = var.stockage_immutabilite_jours
    }
  }
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
