# Azure Monitor + Application Insights : journaux, télémétrie et TEST DE DISPONIBILITÉ synthétique
# multi-régions = mesure indépendante qui atteste le taux servant au calcul du SLA 99,99 %.
resource "azurerm_log_analytics_workspace" "this" {
  name                = "${local.name}-law"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "PerGB2018"
  retention_in_days   = 90
  tags                = local.common_tags
}

resource "azurerm_application_insights" "this" {
  name                = "${local.name}-appi"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  workspace_id        = azurerm_log_analytics_workspace.this.id
  application_type    = "web"
  tags                = local.common_tags
}

# Test de disponibilité multi-régions (sonde toutes les 5 min depuis plusieurs régions).
resource "azurerm_application_insights_standard_web_test" "availability" {
  name                    = "${local.name}-availability"
  resource_group_name     = azurerm_resource_group.this.name
  location                = azurerm_resource_group.this.location
  application_insights_id = azurerm_application_insights.this.id
  geo_locations           = ["us-ca-sjc-azr", "us-tx-sn1-azr", "emea-nl-ams-azr"]
  frequency               = 300
  timeout                 = 30
  enabled                 = true

  request {
    url = var.availability_test_url
  }
  validation_rules {
    expected_status_code = 200
    ssl_check_enabled    = true
  }
  tags = local.common_tags
}

# Alerte si la disponibilité mesurée chute (déclenche l'intervention 24/7).
resource "azurerm_monitor_action_group" "ops" {
  name                = "${local.name}-ops"
  resource_group_name = azurerm_resource_group.this.name
  short_name          = "ops"
  email_receiver {
    name          = "equipe"
    email_address = var.alert_email
  }
}

resource "azurerm_monitor_metric_alert" "availability" {
  name                = "${local.name}-disponibilite"
  resource_group_name = azurerm_resource_group.this.name
  scopes              = [azurerm_application_insights.this.id]
  description         = "Disponibilité mesurée sous le seuil (SLA 99,99 %)."
  frequency           = "PT5M"
  window_size         = "PT5M"

  criteria {
    metric_namespace = "microsoft.insights/components"
    metric_name      = "availabilityResults/availabilityPercentage"
    aggregation      = "Average"
    operator         = "LessThan"
    threshold        = 100
  }
  action {
    action_group_id = azurerm_monitor_action_group.ops.id
  }
}

# ST-14 — Alertes d'exploitation du module Contrats & signature. L'application écrit une ligne stable
# « CLM_ALERTE code=… tenant=… ressource=… detail=… » (AdaptateursClm.AlertesJournal) ; Container Insights (AKS) ou
# Application Insights la versent au workspace, chaque règle la relève et prévient l'équipe d'exploitation.
# Procédures : docs/Dossier_IQ_2026-6370-0005/04-Architecture/runbook_exploitation_clm.md.
locals {
  alertes_clm = {
    integrite-alteree = {
      code        = "INTEGRITE_ALTEREE"
      severite    = 1
      description = "Preuve de signature altérée : document, attestation ou journal scellé ne correspond plus (runbook CLM, § 3)."
    }
    salesforce-file-erreur = {
      code        = "SALESFORCE_FILE_ERREUR"
      severite    = 2
      description = "Retour de statut vers Salesforce en file d'erreur après épuisement des reprises (runbook CLM, § 4)."
    }
    planificateur-echec = {
      code        = "PLANIFICATEUR_ECHEC"
      severite    = 2
      description = "Tâche du planificateur CLM en échec : rappels, échéances ou relais Salesforce (runbook CLM, § 5)."
    }
  }
}

resource "azurerm_monitor_scheduled_query_rules_alert_v2" "clm" {
  for_each = local.alertes_clm

  name                 = "${local.name}-clm-${each.key}"
  display_name         = "CLM — ${each.value.code}"
  description          = each.value.description
  resource_group_name  = azurerm_resource_group.this.name
  location             = azurerm_resource_group.this.location
  scopes               = [azurerm_log_analytics_workspace.this.id]
  severity             = each.value.severite
  evaluation_frequency = "PT5M"
  window_duration      = "PT5M"
  enabled              = true
  # Les tables de journaux n'existent qu'après les premières écritures : la requête est valide, sa cible pas encore.
  skip_query_validation = true

  criteria {
    # Le code est lu juste après le marqueur : une valeur reprise dans « ressource » ou « detail » ne déclenche rien.
    query                   = <<-KQL
      union isfuzzy=true
        (ContainerLogV2 | project TimeGenerated, Message = tostring(LogMessage)),
        (ContainerLog | project TimeGenerated, Message = LogEntry),
        (AppTraces | project TimeGenerated, Message)
      | where Message has "CLM_ALERTE"
      | parse Message with * "CLM_ALERTE code=" Code " tenant=" Tenant " ressource=" Ressource " detail=" Detail
      | where Code == "${each.value.code}"
    KQL
    time_aggregation_method = "Count"
    operator                = "GreaterThan"
    threshold               = 0

    failing_periods {
      minimum_failing_periods_to_trigger_alert = 1
      number_of_evaluation_periods             = 1
    }
  }

  action {
    action_groups = [azurerm_monitor_action_group.ops.id]
    custom_properties = {
      runbook = "docs/Dossier_IQ_2026-6370-0005/04-Architecture/runbook_exploitation_clm.md"
      code    = each.value.code
    }
  }

  tags = local.common_tags
}
