# --- Posture DR permanente Azure (« pilot light ») ---
# Réplique en continu, de la région primaire (Canada Central) vers la région de reprise
# (Canada Est), les éléments nécessaires à un rétablissement sous RTO ≤ 4 h :
#   - documents / exports (réplication d'objets entre comptes de stockage) ;
#   - images de conteneur (registre ACR dédié à la région DR) ;
#   - la base PostgreSQL Flexible s'appuie sur ses SAUVEGARDES GÉO-REDONDANTES (déjà activées en
#     production) : la restauration géo (GeoRestore) est faite au failover, aucun coût de calcul ici.
# Ce module ne crée AUCUN calcul dans la région DR. Le calcul est instancié par infra/dr/azure/failover.
# Il ne modifie pas les stacks de production (ressources source référencées en lecture).

terraform {
  required_version = ">= 1.6"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.3"
    }
  }

  # backend "azurerm" {
  #   resource_group_name  = "vina-contrat-tfstate-rg"
  #   storage_account_name = "vinacontrattfstate"
  #   container_name       = "tfstate"
  #   key                  = "dr/azure/replication/terraform.tfstate"
  # }
}

provider "azurerm" {
  features {}
}

locals {
  name    = "${var.project}-${var.environment}"
  dr_name = "${var.project}-${var.environment}-dr"

  common_tags = merge({
    project     = var.project
    environment = var.environment
    managed_by  = "terraform"
    application = "contractuelle_contrat"
    cost_center = "AO-7000017905"
    purpose     = "disaster-recovery"
  }, var.tags)
}

# Ressources source (production) — référencées en lecture pour y attacher la réplication d'objets.
data "azurerm_storage_account" "src" {
  name                = replace("${local.name}sa", "-", "")
  resource_group_name = "${local.name}-rg"
}
