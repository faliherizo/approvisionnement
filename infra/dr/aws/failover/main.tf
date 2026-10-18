# --- Stack de bascule (failover) ---
# Instancié À LA DEMANDE dans la région de reprise (Canada Est / ca-west-1) lorsqu'un sinistre
# rend la région primaire indisponible. Reconstruit réseau + sécurité + calcul (ECS/Fargate),
# restaure PostgreSQL par PITR depuis les sauvegardes répliquées, rattache les buckets S3
# répliqués, puis expose l'application derrière un ALB. Voir le runbook :
#   docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md
# Objectifs : RTO ≤ 4 h, RPO ≤ 15 min.

terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
  }

  # État distant SÉPARÉ du stack de production ; idéalement hébergé dans la région DR.
  # backend "s3" {
  #   bucket         = "vina-contrat-tfstate-dr"
  #   key            = "dr/failover/terraform.tfstate"
  #   region         = "ca-west-1"
  #   dynamodb_table = "vina-contrat-tflock-dr"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.dr_region
  default_tags { tags = local.common_tags }
}

locals {
  name = "${var.project}-${var.environment}-dr"

  common_tags = merge({
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
    Application = "contractuelle_contrat"
    CostCenter  = "AO-7000017905"
    Purpose     = "disaster-recovery-failover"
  }, var.tags)
}
