# --- Stack de bascule (failover) Azure ---
# Instancié À LA DEMANDE dans la région de reprise (Canada Est) lorsqu'un sinistre rend Canada Central
# indisponible. Reconstruit réseau + Container Apps (calcul), restaure PostgreSQL par GeoRestore
# (sauvegardes géo-redondantes), rattache le stockage répliqué, puis expose l'application.
# Objectifs : RTO ≤ 4 h, RPO ≤ 15 min. Runbook : docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md

terraform {
  required_version = ">= 1.6"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.3"
    }
  }

  # backend "azurerm" {
  #   resource_group_name  = "vina-contrat-tfstate-dr-rg"
  #   storage_account_name = "vinacontrattfstatedr"
  #   container_name       = "tfstate"
  #   key                  = "dr/azure/failover/terraform.tfstate"
  # }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = false
      recover_soft_deleted_key_vaults = true
    }
  }
}

locals {
  name = "${var.project}-${var.environment}-dr"

  common_tags = merge({
    project     = var.project
    environment = var.environment
    managed_by  = "terraform"
    application = "contractuelle_contrat"
    cost_center = "AO-7000017905"
    purpose     = "disaster-recovery-failover"
  }, var.tags)
}

data "azurerm_client_config" "current" {}

# ACR DR (créé par le stack replication) — source des images.
data "azurerm_container_registry" "dr" {
  name                = var.dr_acr_name
  resource_group_name = var.dr_acr_resource_group
}
