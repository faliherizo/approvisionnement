output "dr_resource_group" {
  description = "Groupe de ressources DR (à fournir au stack de failover)."
  value       = azurerm_resource_group.dr.name
}

output "dr_acr_login_server" {
  description = "Serveur de connexion de l'ACR DR (source des images au failover)."
  value       = azurerm_container_registry.dr.login_server
}

output "dr_acr_name" {
  value = azurerm_container_registry.dr.name
}

output "dr_storage_account" {
  description = "Compte de stockage DR (documents / exports répliqués)."
  value       = azurerm_storage_account.dr.name
}
