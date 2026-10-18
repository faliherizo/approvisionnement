# --- Posture DR permanente (« pilot light ») ---
# Réplique en continu, de la région primaire (Canada Central / ca-central-1) vers la région de
# reprise (Canada Est / ca-west-1), les éléments nécessaires à un rétablissement sous RTO ≤ 4 h :
#   - documents et exports S3 (réplication inter-région, RPO proche de zéro) ;
#   - sauvegardes automatiques RDS PostgreSQL (PITR inter-région, RPO ≤ 5 min) ;
#   - images de conteneur ECR (réplication de registre).
# Ce module ne crée AUCUN calcul dans la région DR : il n'entretient que la donnée et les images.
# Le calcul est instancié à la demande par le module infra/dr/aws/failover (voir runbook).

terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
  }

  # Décommenter après le bootstrap de l'état distant (voir docs).
  # backend "s3" {
  #   bucket         = "vina-contrat-tfstate"
  #   key            = "dr/replication/terraform.tfstate"
  #   region         = "ca-central-1"
  #   dynamodb_table = "vina-contrat-tflock"
  #   encrypt        = true
  # }
}

# Région primaire (source) — fournisseur par défaut.
provider "aws" {
  region = var.source_region
  default_tags { tags = local.common_tags }
}

# Région de reprise (destination).
provider "aws" {
  alias  = "dr"
  region = var.dr_region
  default_tags { tags = local.common_tags }
}

locals {
  name = "${var.project}-${var.environment}"

  common_tags = merge({
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
    Application = "contractuelle_contrat"
    CostCenter  = "AO-7000017905"
    Purpose     = "disaster-recovery"
  }, var.tags)
}

data "aws_caller_identity" "current" {}

# Ressources source, gérées par le stack de production (infra/serverless/terraform/aws-ecs) :
# référencées ici en lecture seule pour y attacher la réplication.
data "aws_s3_bucket" "documents_src" {
  bucket = "${local.name}-documents"
}

data "aws_s3_bucket" "exports_src" {
  bucket = "${local.name}-exports"
}

data "aws_db_instance" "src" {
  db_instance_identifier = "${local.name}-pg"
}
