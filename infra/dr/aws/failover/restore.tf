# =============================================================================
# Restauration de PostgreSQL par PITR depuis les sauvegardes automatiques répliquées.
# use_latest_restorable_time = true → point de restauration le plus récent (RPO ≤ 15 min).
# =============================================================================
resource "aws_db_subnet_group" "this" {
  name       = "${local.name}-db"
  subnet_ids = module.vpc.private_subnets
}

resource "aws_db_instance" "restored" {
  identifier             = "${local.name}-pg"
  instance_class         = var.postgres_instance_class
  multi_az               = true # HA multi-AZ rétablie dans la région DR
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  storage_encrypted = true
  kms_key_id        = var.dr_kms_key_arn

  restore_to_point_in_time {
    source_db_instance_automated_backups_arn = var.source_automated_backups_arn
    use_latest_restorable_time               = true
  }

  backup_retention_period = 14
  deletion_protection     = false # facilite les exercices de reprise ; à activer une fois DR promue en primaire
  skip_final_snapshot     = true
  apply_immediately       = true
}

# =============================================================================
# Buckets S3 répliqués (créés par le stack replication) — référencés en lecture.
# =============================================================================
data "aws_s3_bucket" "documents_dr" {
  bucket = "${var.project}-${var.environment}-documents-dr"
}

data "aws_s3_bucket" "exports_dr" {
  bucket = "${var.project}-${var.environment}-exports-dr"
}

# =============================================================================
# Secrets applicatifs dans la région DR (valeurs renseignées par le runbook depuis la source).
# =============================================================================
resource "aws_secretsmanager_secret" "db" {
  name       = "${local.name}-db"
  kms_key_id = var.dr_kms_key_arn
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    url      = var.db_url != "" ? var.db_url : "jdbc:postgresql://${aws_db_instance.restored.endpoint}/contrat"
    password = var.db_password
  })
}

resource "aws_secretsmanager_secret" "app" {
  name       = "${local.name}-app"
  kms_key_id = var.dr_kms_key_arn
}

resource "aws_secretsmanager_secret_version" "app" {
  secret_id     = aws_secretsmanager_secret.app.id
  secret_string = jsonencode({ JWT_SECRET = var.jwt_secret })
}
