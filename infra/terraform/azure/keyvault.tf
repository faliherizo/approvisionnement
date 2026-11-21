# Coffre de secrets : chaîne de connexion BD + secrets applicatifs (JWT, chiffrement, M365/SAP).
# Les pods AKS y accèdent via Workload Identity (aucun secret dans les images).
resource "azurerm_key_vault" "this" {
  name                       = replace("${local.name}-kv", "-", "")
  location                   = azurerm_resource_group.this.location
  resource_group_name        = azurerm_resource_group.this.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  purge_protection_enabled   = true
  soft_delete_retention_days = 30
  rbac_authorization_enabled = true
  tags                       = local.common_tags
  # Refus réseau par défaut : seuls les services Azure de confiance et les adresses d'administration déclarées
  # (exécutant Terraform, poste d'exploitation) atteignent le coffre.
  network_acls {
    default_action = "Deny"
    bypass         = "AzureServices"
    ip_rules       = var.ip_administration
  }
}

# L'exécutant Terraform peut écrire les secrets.
resource "azurerm_role_assignment" "kv_admin" {
  scope                = azurerm_key_vault.this.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}

resource "azurerm_key_vault_secret" "db_url" {
  name         = "db-url"
  key_vault_id = azurerm_key_vault.this.id
  value        = "jdbc:postgresql://${azurerm_postgresql_flexible_server.this.fqdn}:5432/${var.db_name}"
  depends_on   = [azurerm_role_assignment.kv_admin]
}

resource "azurerm_key_vault_secret" "db_password" {
  name         = "db-password"
  key_vault_id = azurerm_key_vault.this.id
  value        = random_password.db.result
  depends_on   = [azurerm_role_assignment.kv_admin]
}
