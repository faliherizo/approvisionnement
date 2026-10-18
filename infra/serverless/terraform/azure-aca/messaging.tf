# Traitement asynchrone de la publication (équivalent Azure de la chaîne SQS/EventBridge/Lambda) :
#  Service Bus (file + lettres mortes), Event Grid (événements de domaine), Function App (worker).
resource "azurerm_servicebus_namespace" "this" {
  name                = "${local.name}-sb"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  sku                 = "Standard"
  tags                = local.common_tags
}

resource "azurerm_servicebus_queue" "publication" {
  name                                 = "publication"
  namespace_id                         = azurerm_servicebus_namespace.this.id
  max_delivery_count                   = 5
  dead_lettering_on_message_expiration = true
  lock_duration                        = "PT5M"
}

# Bus d'événements de domaine (document approuvé, publication effectuée…).
resource "azurerm_eventgrid_topic" "domain" {
  name                = "${local.name}-events"
  location            = azurerm_resource_group.this.location
  resource_group_name = azurerm_resource_group.this.name
  tags                = local.common_tags
}

# Plan et Function App (worker de publication + tâches planifiées / rapport SLA mensuel).
resource "azurerm_service_plan" "func" {
  name                = "${local.name}-func-plan"
  resource_group_name = azurerm_resource_group.this.name
  location            = azurerm_resource_group.this.location
  os_type             = "Linux"
  sku_name            = "Y1" # Consumption (paiement à l'usage) — adapté à un worker faible volume
  tags                = local.common_tags
}

resource "azurerm_linux_function_app" "worker" {
  name                       = "${local.name}-worker"
  resource_group_name        = azurerm_resource_group.this.name
  location                   = azurerm_resource_group.this.location
  service_plan_id            = azurerm_service_plan.func.id
  storage_account_name       = azurerm_storage_account.this.name
  storage_account_access_key = azurerm_storage_account.this.primary_access_key

  identity { type = "SystemAssigned" }

  site_config {
    application_insights_connection_string = azurerm_application_insights.this.connection_string
  }

  app_settings = {
    FUNCTIONS_WORKER_RUNTIME = "java"
    SERVICEBUS_QUEUE         = azurerm_servicebus_queue.publication.name
    SERVICEBUS_NAMESPACE     = azurerm_servicebus_namespace.this.name
    EVENTGRID_TOPIC_ENDPOINT = azurerm_eventgrid_topic.domain.endpoint
    KEY_VAULT_URI            = azurerm_key_vault.this.vault_uri
  }
  tags = local.common_tags
}

# La Function App lit ses secrets dans Key Vault.
resource "azurerm_role_assignment" "func_kv" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_linux_function_app.worker.identity[0].principal_id
}
