variable "project" {
  description = "Préfixe de nommage du projet (identique au stack de production)."
  type        = string
  default     = "vina-contrat"
}

variable "environment" {
  description = "Environnement (prod, staging, dev)."
  type        = string
  default     = "prod"
}

variable "source_region" {
  description = "Région AWS primaire (production) — résidence des données au Canada."
  type        = string
  default     = "ca-central-1"
}

variable "dr_region" {
  description = "Région AWS de reprise après sinistre — seconde région canadienne (résidence des données au Canada)."
  type        = string
  default     = "ca-west-1"
}

variable "backup_retention_days" {
  description = "Rétention des sauvegardes automatiques répliquées dans la région DR (jours)."
  type        = number
  default     = 14
}

variable "tags" {
  description = "Étiquettes additionnelles."
  type        = map(string)
  default     = {}
}
