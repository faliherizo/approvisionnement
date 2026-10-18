locals {
  name = "${var.project}-${var.environment}"

  common_tags = merge({
    project     = var.project
    environment = var.environment
    managed_by  = "terraform"
    application = "contractuelle_contrat"
    cost_center = "AO-7000017905"
  }, var.tags)
}

data "azurerm_client_config" "current" {}
