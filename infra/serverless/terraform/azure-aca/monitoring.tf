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
