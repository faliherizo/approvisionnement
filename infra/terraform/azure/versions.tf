terraform {
  required_version = ">= 1.6"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.3"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # État distant dans un compte de stockage Azure (voir tutoriel §Bootstrap).
  # backend "azurerm" {
  #   resource_group_name  = "vina-contrat-tfstate-rg"
  #   storage_account_name = "vinacontrattfstate"
  #   container_name       = "tfstate"
  #   key                  = "azure/prod/terraform.tfstate"
  # }
}
