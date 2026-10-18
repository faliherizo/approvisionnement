locals {
  name = "${var.project}-${var.environment}"

  common_tags = merge({
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
    Application = "contractuelle_contrat"
    CostCenter  = "AO-7000017905"
  }, var.tags)
}

data "aws_caller_identity" "current" {}
data "aws_region" "current" {}
