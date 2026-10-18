output "resource_group" {
  value = azurerm_resource_group.this.name
}

output "container_app_environment" {
  value = azurerm_container_app_environment.this.name
}

output "frontend_fqdn" {
  description = "FQDN public du frontend (à pointer via Front Door vers contrat.vinaitsolution.com)."
  value       = azurerm_container_app.frontend.ingress[0].fqdn
}

output "backend_internal_fqdn" {
  description = "FQDN interne du backend (appelé par le frontend)."
  value       = azurerm_container_app.backend.ingress[0].fqdn
}

output "apps_identity_client_id" {
  description = "Client ID de l'identité managée des Container Apps."
  value       = azurerm_user_assigned_identity.apps.client_id
}

output "acr_login_server" {
  value = azurerm_container_registry.this.login_server
}

output "postgres_fqdn" {
  value = azurerm_postgresql_flexible_server.this.fqdn
}

output "key_vault_uri" {
  value = azurerm_key_vault.this.vault_uri
}

output "frontdoor_endpoint_hostname" {
  value = azurerm_cdn_frontdoor_endpoint.this.host_name
}

output "app_insights_connection_string" {
  value     = azurerm_application_insights.this.connection_string
  sensitive = true
}

output "servicebus_queue" {
  value = azurerm_servicebus_queue.publication.name
}
