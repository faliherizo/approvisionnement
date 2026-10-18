# Secrets applicatifs et base de données (jamais en clair dans le code ni l'état exporté).
resource "aws_secretsmanager_secret" "db" {
  name        = "${local.name}/db"
  description = "Identifiants PostgreSQL (contractuelle_contrat)."
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db.result
    host     = aws_db_instance.postgres.address
    port     = 5432
    dbname   = var.db_name
    url      = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}"
  })
}

# Secrets applicatifs (JWT, chiffrement, connecteurs M365/SAP) — valeurs injectées hors Terraform.
resource "aws_secretsmanager_secret" "app" {
  name        = "${local.name}/app"
  description = "Secrets applicatifs : JWT, APP_CRYPTO_KEY, connecteurs M365/SAP, OIDC/SAML."
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id = aws_secretsmanager_secret.app.id
  secret_string = jsonencode({
    JWT_SECRET      = "à-remplacer-hors-terraform"
    APP_CRYPTO_KEY  = "à-remplacer-hors-terraform"
    APP_CRYPTO_SALT = "à-remplacer-hors-terraform"
  })
  lifecycle {
    ignore_changes = [secret_string] # géré ensuite via la console/CI, pas par Terraform
  }
}
