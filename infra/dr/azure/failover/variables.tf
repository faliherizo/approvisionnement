variable "project" {
  type    = string
  default = "vina-contrat"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "dr_location" {
  description = "Région Azure de reprise (seconde région canadienne)."
  type        = string
  default     = "canadaeast"
}

# --- Entrées fournies par le stack replication (sorties) et par le CI ---
variable "source_postgres_server_id" {
  description = "ID complet du serveur PostgreSQL Flexible de PRODUCTION (source de la restauration géo)."
  type        = string
}

variable "dr_acr_name" {
  description = "Nom de l'ACR DR (sortie dr_acr_name du stack replication)."
  type        = string
  default     = "vinacontratproddracr"
}

variable "dr_acr_resource_group" {
  description = "Groupe de ressources de l'ACR DR (sortie dr_resource_group du stack replication)."
  type        = string
  default     = "vina-contrat-prod-dr-rg"
}

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "db_password" {
  description = "Mot de passe maître de la base (identique à la source après GeoRestore ; copié depuis le Key Vault primaire par le runbook)."
  type        = string
  sensitive   = true
}

variable "vnet_cidr" {
  description = "Plage CIDR du VNet de reprise (distincte de la production)."
  type        = string
  default     = "10.70.0.0/16"
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
  default = 1.0
}

variable "backend_memory" {
  type    = string
  default = "2Gi"
}

variable "frontend_cpu" {
  type    = number
  default = 0.5
}

variable "frontend_memory" {
  type    = string
  default = "1Gi"
}

variable "min_replicas" {
  type    = number
  default = 3
}

variable "max_replicas" {
  type    = number
  default = 10
}

variable "db_name" {
  type    = string
  default = "contrat"
}

variable "tags" {
  type    = map(string)
  default = {}
}
