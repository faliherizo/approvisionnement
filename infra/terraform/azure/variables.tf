variable "project" {
  type    = string
  default = "vina-contrat"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "location" {
  description = "Région Azure : canadacentral (Toronto, zones de disponibilité) ou canadaeast (Québec) — ADR-020."
  type        = string
  default     = "canadacentral"

  validation {
    condition     = contains(["canadacentral", "canadaeast"], var.location)
    error_message = "Résidence des données au Canada : canadacentral ou canadaeast seulement (Cahier § 1.4.2)."
  }
}

variable "zones" {
  description = "Zones de disponibilité utilisées ; liste vide pour une région sans zones (ex. canadaeast) — ADR-020."
  type        = list(string)
  default     = ["1", "2", "3"]
}

variable "kubernetes_version" {
  type    = string
  default = "1.30"
}

variable "aks_node_size" {
  type    = string
  default = "Standard_D4s_v5"
}

variable "aks_node_min" {
  description = "Minimum de noeuds (répartis sur les zones 1,2,3)."
  type        = number
  default     = 3
}

variable "aks_node_max" {
  type    = number
  default = 9
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
