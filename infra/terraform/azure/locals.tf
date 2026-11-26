locals {
  name = "${var.project}-${var.environment}"

  common_tags = merge({
    project     = var.project
    environment = var.environment
    managed_by  = "terraform"
    application = "contractuelle_contrat"
    cost_center = "AO-7000017905"
  }, var.tags)
}

locals {
  # Reprise inter-régions (ADR-039).
  dr_name      = "${local.name}-dr"
  dr_replique  = var.dr_strategie_base == "replique"
  dr_avec_aks  = var.dr_calcul != "aucun"
  dr_veille    = var.dr_calcul == "veille_active"
  dr_tags      = merge(local.common_tags, { purpose = "disaster-recovery" })
  dr_zones_aks = null # Canada Est n'a pas de zones de disponibilité (ADR-020)

  # Redondance du stockage : géo-zone (zones dans la région primaire + copie dans la région jumelle) avec lecture de la
  # copie secondaire ; géo seule si la région primaire n'a pas de zones.
  stockage_redondance = length(var.zones) > 0 ? "RAGZRS" : "RAGRS"

  # Point d'écriture stable de la base : le point de terminaison virtuel suit la promotion de la réplique, la chaîne
  # de connexion des pods ne change pas à la bascule.
  postgres_hote_ecriture = local.dr_replique ? "${local.name}-pg-ve.writer.postgres.database.azure.com" : azurerm_postgresql_flexible_server.this.fqdn
}

data "azurerm_client_config" "current" {}
