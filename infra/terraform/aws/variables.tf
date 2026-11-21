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

variable "kubernetes_version" {
  description = "Version du plan de contrôle EKS."
  type        = string
  default     = "1.30"
}

variable "eks_node_instance_types" {
  description = "Types d'instances des noeuds applicatifs."
  type        = list(string)
  default     = ["m6i.large"]
}

variable "eks_node_min" {
  description = "Nombre minimal de noeuds (réparti sur les 3 AZ)."
  type        = number
  default     = 3
}

variable "eks_node_max" {
  description = "Nombre maximal de noeuds."
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

variable "ip_administration" {
  description = "Adresses (CIDR) d'administration seules admises sur l'API du plan de contrôle et le coffre de secrets : exécutant Terraform, poste d'exploitation."
  type        = list(string)
  validation {
    condition     = length(var.ip_administration) > 0 && !contains(var.ip_administration, "0.0.0.0/0")
    error_message = "Déclarer au moins une adresse d'administration, et jamais 0.0.0.0/0."
  }
}
