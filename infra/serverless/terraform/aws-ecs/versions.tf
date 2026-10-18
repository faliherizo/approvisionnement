# Versions et backend d'état distant.
# L'état est stocké dans S3 (chiffré, versionné) avec verrou DynamoDB — voir docs/aws-terraform-integration.md.
terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
  }

  # Décommenter après avoir créé le bucket + la table de verrouillage (voir tutoriel §Bootstrap).
  # backend "s3" {
  #   bucket         = "vina-contrat-tfstate"
  #   key            = "aws/prod/terraform.tfstate"
  #   region         = "ca-central-1"
  #   dynamodb_table = "vina-contrat-tflock"
  #   encrypt        = true
  # }
}
