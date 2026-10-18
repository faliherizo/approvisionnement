output "resource_group" {
  value = azurerm_resource_group.this.name
}

output "aks_name" {
  value = azurerm_kubernetes_cluster.this.name
}

output "aks_get_credentials" {
  description = "Commande pour configurer kubectl."
  value       = "az aks get-credentials --resource-group ${azurerm_resource_group.this.name} --name ${azurerm_kubernetes_cluster.this.name}"
}

output "aks_oidc_issuer_url" {
  description = "Émetteur OIDC pour Workload Identity (fédération des ServiceAccounts)."
  value       = azurerm_kubernetes_cluster.this.oidc_issuer_url
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
