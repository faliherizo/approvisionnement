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

# --- Reprise inter-régions (ADR-039) : lues par infra/dr/scripts/bascule-azure.sh ---

output "dr_resource_group" {
  value = azurerm_resource_group.dr.name
}

output "dr_location" {
  value = azurerm_resource_group.dr.location
}

output "postgres_hote_ecriture" {
  description = "Nom d'hôte d'écriture stable (point de terminaison virtuel si réplique) : inchangé par la bascule."
  value       = local.postgres_hote_ecriture
}

output "postgres_server_id" {
  description = "Serveur primaire : source de la géo-restauration (voie de secours)."
  value       = azurerm_postgresql_flexible_server.this.id
}

output "dr_postgres_replique" {
  description = "Réplique de relève à promouvoir (null en géo-restauration)."
  value       = local.dr_replique ? azurerm_postgresql_flexible_server.replique[0].name : null
}

output "dr_postgres_point_virtuel" {
  value = local.dr_replique ? azurerm_postgresql_flexible_server_virtual_endpoint.ecriture[0].name : null
}

output "dr_key_vault_uri" {
  value = azurerm_key_vault.dr.vault_uri
}

output "dr_aks_name" {
  value = local.dr_avec_aks ? azurerm_kubernetes_cluster.dr[0].name : null
}

output "storage_redondance" {
  value = azurerm_storage_account.this.account_replication_type
}

output "storage_blob_secondaire" {
  description = "Point de lecture de la copie secondaire (RA-GZRS), disponible sans bascule du compte."
  value       = azurerm_storage_account.this.secondary_blob_endpoint
}

output "frontdoor_profile" {
  value = azurerm_cdn_frontdoor_profile.this.name
}

output "frontdoor_origin_group" {
  value = azurerm_cdn_frontdoor_origin_group.aks.name
}
