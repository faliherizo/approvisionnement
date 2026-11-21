variable "project" {
  type    = string
  default = "vina-contrat"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "location" {
  description = "Région Azure (résidence des données au Canada)."
  type        = string
  default     = "canadacentral"
}

variable "image_tag" {
  description = "Tag des images backend/frontend dans l'ACR (fourni par le CI, défaut latest)."
  type        = string
  default     = "latest"
}

variable "backend_port" {
  type    = number
  default = 8085
}

variable "frontend_port" {
  type    = number
  default = 8080
}

variable "backend_cpu" {
  description = "vCPU de la révision backend (ex. 1.0)."
  type        = number
  default     = 1.0
}

variable "backend_memory" {
  description = "Mémoire de la révision backend (ex. 2Gi)."
  type        = string
  default     = "2Gi"
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
  description = "Répliques minimales par app (réparties sur les zones)."
  type        = number
  default     = 3
}

variable "max_replicas" {
  description = "Répliques maximales (mise à l'échelle automatique)."
  type        = number
  default     = 10
}

variable "postgres_version" {
  type    = string
  default = "16"
}

variable "postgres_sku" {
  description = "SKU PostgreSQL Flexible Server (General Purpose pour la HA)."
  type        = string
  default     = "GP_Standard_D4s_v3"
}

variable "postgres_storage_mb" {
  type    = number
  default = 131072 # 128 Go
}

variable "db_name" {
  type    = string
  default = "contrat"
}

variable "db_admin" {
  type    = string
  default = "contrat_admin"
}

variable "app_hostname" {
  description = "Nom d'hôte public servi par Front Door."
  type        = string
  default     = "contrat.vinaitsolution.com"
}

variable "availability_test_url" {
  description = "URL sondée par le test de disponibilité Application Insights (mesure du SLA 99,99 %)."
  type        = string
  default     = "https://contrat.vinaitsolution.com/actuator/health"
}

variable "alert_email" {
  type    = string
  default = "faliherizo@vinaitsolution.com"
}

variable "tags" {
  type    = map(string)
  default = {}
}

variable "ip_administration" {
  description = "Adresses (CIDR) d'administration admises sur le coffre de clés et l'API AKS : exécutant Terraform, poste d'exploitation."
  type        = list(string)
  default     = []
}
