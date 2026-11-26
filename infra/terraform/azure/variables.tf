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
  default     = "https://contrat.vinaitsolution.com/api/health"
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
  description = "Adresses (CIDR) d'administration seules admises sur l'API du plan de contrôle et le coffre de secrets : exécutant Terraform, poste d'exploitation."
  type        = list(string)
  validation {
    condition     = length(var.ip_administration) > 0 && !contains(var.ip_administration, "0.0.0.0/0")
    error_message = "Déclarer au moins une adresse d'administration, et jamais 0.0.0.0/0."
  }
}

# --- Reprise après sinistre inter-régions (SEC-11, ADR-039) ---

variable "dr_location" {
  description = "Région de relève, jumelle de la région primaire (Canada Est pour Canada Central) — ADR-020, ADR-039."
  type        = string
  default     = "canadaeast"

  validation {
    condition     = contains(["canadacentral", "canadaeast"], var.dr_location)
    error_message = "Résidence des données au Canada : la région de relève est canadacentral ou canadaeast."
  }
}

variable "dr_strategie_base" {
  description = "Reprise de la base : « replique » (réplique de lecture inter-régions promue, RPO de quelques secondes) ou « geo_restauration » (sauvegardes géo-redondantes seules, RPO jusqu'à 1 h) — ADR-039."
  type        = string
  default     = "replique"

  validation {
    condition     = contains(["replique", "geo_restauration"], var.dr_strategie_base)
    error_message = "dr_strategie_base : replique ou geo_restauration."
  }
}

variable "dr_calcul" {
  description = "Calcul de la région de relève : « pilot_light » (AKS minimal, pool applicatif à zéro, agrandi à la bascule), « veille_active » (AKS dimensionné, prêt à servir) ou « aucun » (créé à la bascule) — ADR-039."
  type        = string
  default     = "pilot_light"

  validation {
    condition     = contains(["pilot_light", "veille_active", "aucun"], var.dr_calcul)
    error_message = "dr_calcul : pilot_light, veille_active ou aucun."
  }
}

variable "dr_postgres_sku" {
  description = "SKU de la réplique de relève. Même famille que le primaire : une réplique plus petite prend du retard sous charge et allonge le RPO."
  type        = string
  default     = "GP_Standard_D4s_v3"
}

variable "postgres_backup_retention_days" {
  description = "Rétention des sauvegardes PostgreSQL (fenêtre de restauration à un instant donné), copiées dans la région jumelle. 35 jours : maximum du service, couvre le cycle de restauration trimestrielle."
  type        = number
  default     = 35

  validation {
    condition     = var.postgres_backup_retention_days >= 7 && var.postgres_backup_retention_days <= 35
    error_message = "Rétention PostgreSQL Flexible Server : de 7 à 35 jours."
  }
}

variable "stockage_retention_suppression_jours" {
  description = "Suppression réversible des documents et conteneurs du stockage (jours) ; la restauration à un instant donné des blobs couvre un jour de moins."
  type        = number
  default     = 30

  validation {
    condition     = var.stockage_retention_suppression_jours >= 7 && var.stockage_retention_suppression_jours <= 365
    error_message = "Suppression réversible : de 7 à 365 jours."
  }
}

variable "stockage_immutabilite_jours" {
  description = "Immuabilité (WORM) au niveau des versions de blobs, en jours ; 0 la désactive. Désactivée par défaut : le calendrier de conservation d'IQ impose la destruction au terme (ADR-012, Loi 25), que l'immuabilité empêcherait ; l'inaltérabilité est prouvée par le journal scellé et les empreintes (ADR-039)."
  type        = number
  default     = 0

  validation {
    condition     = var.stockage_immutabilite_jours >= 0
    error_message = "stockage_immutabilite_jours : 0 (désactivée) ou un nombre de jours."
  }
}

variable "dr_aks_node_max" {
  description = "Plafond du pool applicatif de relève (agrandi à la bascule jusqu'à la capacité de production)."
  type        = number
  default     = 9
}

variable "origine_primaire_hote" {
  description = "Nom d'hôte de l'entrée (ingress) AKS de la région primaire, origine de priorité 1 de Front Door."
  type        = string
  default     = "ingress-cc.contrat.vinaitsolution.com"
}

variable "origine_secours_hote" {
  description = "Nom d'hôte de l'entrée AKS de la région de relève, origine de priorité 2 de Front Door ; null si la région de relève ne porte pas de calcul."
  type        = string
  default     = "ingress-ce.contrat.vinaitsolution.com"
  nullable    = true
}

variable "clm_cle_maitresse_id" {
  description = "Identifiant de la clé maîtresse active (CLM_CLE_MAITRESSE_ID, ADR-037). Une rotation ajoute une nouvelle clé sous un nouvel identifiant ; l'ancienne reste déclarée le temps du réenveloppement."
  type        = string
  default     = "azure-1"
}

variable "dr_retard_replication_alerte_s" {
  description = "Retard de réplication (secondes) au-delà duquel l'exploitation est alertée : 300 s laisse une marge sous l'objectif de RPO de 15 minutes."
  type        = number
  default     = 300
}
