variable "project" {
  type    = string
  default = "vina-contrat"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "dr_region" {
  description = "Région AWS de reprise (seconde région canadienne)."
  type        = string
  default     = "ca-west-1"
}

variable "azs" {
  description = "Zones de disponibilité de la région DR (≥ 3 pour la HA)."
  type        = list(string)
  default     = ["ca-west-1a", "ca-west-1b", "ca-west-1c"]
}

variable "vpc_cidr" {
  description = "Plage CIDR du VPC de reprise (distincte de la production)."
  type        = string
  default     = "10.60.0.0/16"
}

# --- Entrées fournies par le stack de réplication (sorties) et par le CI ---
variable "source_automated_backups_arn" {
  description = "ARN des sauvegardes automatiques RDS répliquées (sortie rds_replicated_backups_arn du stack replication) — source du restore PITR."
  type        = string
}

variable "dr_kms_key_arn" {
  description = "ARN de la clé KMS DR (sortie dr_kms_key_arn du stack replication)."
  type        = string
}

variable "backend_image" {
  description = "URI complet de l'image backend dans l'ECR de la région DR (ex. <acct>.dkr.ecr.ca-west-1.amazonaws.com/vina-contrat-prod-backend:<tag>)."
  type        = string
}

variable "frontend_image" {
  description = "URI complet de l'image frontend dans l'ECR de la région DR."
  type        = string
}

variable "db_url" {
  description = "URL JDBC de la base restaurée (renseignée par le runbook une fois l'endpoint DR connu)."
  type        = string
  default     = ""
}

variable "db_password" {
  description = "Mot de passe maître de la base (identique à la source ; renseigné par le runbook depuis Secrets Manager)."
  type        = string
  default     = ""
  sensitive   = true
}

variable "jwt_secret" {
  description = "Secret JWT applicatif (répliqué depuis la source)."
  type        = string
  default     = ""
  sensitive   = true
}

variable "postgres_instance_class" {
  type    = string
  default = "db.m6g.large"
}

variable "backend_port" {
  type    = number
  default = 8085
}

variable "frontend_port" {
  type    = number
  default = 80
}

variable "backend_cpu" {
  type    = number
  default = 1024
}

variable "backend_memory" {
  type    = number
  default = 2048
}

variable "frontend_cpu" {
  type    = number
  default = 512
}

variable "frontend_memory" {
  type    = number
  default = 1024
}

variable "desired_count" {
  description = "Nombre de tâches par service (réparties sur les AZ de la région DR)."
  type        = number
  default     = 3
}

variable "tags" {
  type    = map(string)
  default = {}
}
