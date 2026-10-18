variable "project" {
  type    = string
  default = "vina-contrat"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "primary_location" {
  description = "Région Azure primaire (production)."
  type        = string
  default     = "canadacentral"
}

variable "dr_location" {
  description = "Région Azure de reprise — données maintenues au Canada."
  type        = string
  default     = "canadaeast"
}

variable "tags" {
  type    = map(string)
  default = {}
}
