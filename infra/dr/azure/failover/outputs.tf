output "frontend_fqdn" {
  description = "FQDN public du frontend de reprise — nouvelle origine à câbler sur Front Door."
  value       = azurerm_container_app.frontend.ingress[0].fqdn
}

output "db_fqdn" {
  description = "FQDN de la base PostgreSQL restaurée (GeoRestore) dans la région DR."
  value       = azurerm_postgresql_flexible_server.restored.fqdn
}

output "resource_group" {
  value = azurerm_resource_group.this.name
}
