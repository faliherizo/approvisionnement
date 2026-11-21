variable "project" {
  description = "Préfixe de nommage du projet."
  type        = string
  default     = "vina-contrat"
}

variable "environment" {
  description = "Environnement (prod, staging, dev)."
  type        = string
  default     = "prod"
}

variable "region" {
  description = "Région AWS (résidence des données au Canada pour l'AO)."
  type        = string
  default     = "ca-central-1"
}

variable "azs" {
  description = "Zones de disponibilité utilisées (≥ 3 pour la HA / SLA 99,99 %)."
  type        = list(string)
  default     = ["ca-central-1a", "ca-central-1b", "ca-central-1d"]
}

variable "vpc_cidr" {
  description = "Plage CIDR du VPC."
  type        = string
  default     = "10.40.0.0/16"
}

variable "image_tag" {
  description = "Tag des images backend/frontend dans ECR (fourni par le CI, défaut latest)."
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
  description = "Unités CPU de la tâche backend (1024 = 1 vCPU)."
  type        = number
  default     = 1024
}

variable "backend_memory" {
  description = "Mémoire (Mo) de la tâche backend."
  type        = number
  default     = 2048
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
  description = "Nombre de tâches par service (réparties sur les 3 AZ)."
  type        = number
  default     = 3
}

variable "max_count" {
  description = "Nombre maximal de tâches (mise à l'échelle automatique)."
  type        = number
  default     = 9
}

variable "postgres_version" {
  description = "Version majeure PostgreSQL RDS."
  type        = string
  default     = "16"
}

variable "postgres_instance_class" {
  description = "Classe d'instance RDS."
  type        = string
  default     = "db.m6g.large"
}

variable "postgres_allocated_storage" {
  description = "Stockage RDS (Go)."
  type        = number
  default     = 100
}

variable "db_name" {
  description = "Nom de la base applicative."
  type        = string
  default     = "contrat"
}

variable "db_username" {
  description = "Utilisateur maître de la base (le mot de passe est généré et stocké dans Secrets Manager)."
  type        = string
  default     = "contrat_admin"
}

variable "availability_canary_url" {
  description = "URL publique surveillée par le canari CloudWatch Synthetics (mesure du SLA 99,99 %)."
  type        = string
  default     = "https://contrat.vinaitsolution.com/actuator/health"
}

variable "alarm_email" {
  description = "Adresse recevant les alertes CloudWatch (SNS)."
  type        = string
  default     = "faliherizo@vinaitsolution.com"
}

variable "tags" {
  description = "Étiquettes additionnelles."
  type        = map(string)
  default     = {}
}

variable "certificat_acm_arn" {
  description = "Certificat ACM (TLS) du domaine servi par le répartiteur : l'application n'est servie qu'en HTTPS."
  type        = string
}
