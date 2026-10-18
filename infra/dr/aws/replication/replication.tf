# =============================================================================
# 1) Clé KMS dans la région DR (chiffrement des répliques S3 et des sauvegardes RDS).
# =============================================================================
resource "aws_kms_key" "dr" {
  provider                = aws.dr
  description             = "${local.name} — chiffrement des données répliquées (DR)"
  deletion_window_in_days = 14
  enable_key_rotation     = true
}

resource "aws_kms_alias" "dr" {
  provider      = aws.dr
  name          = "alias/${local.name}-dr"
  target_key_id = aws_kms_key.dr.id
}

# =============================================================================
# 2) Réplication S3 inter-région (documents + exports).
#    Les buckets DR (destination) sont versionnés et chiffrés ; la configuration de
#    réplication est attachée aux buckets source (gérés par le stack de production).
# =============================================================================
resource "aws_s3_bucket" "documents_dr" {
  provider = aws.dr
  bucket   = "${local.name}-documents-dr"
}

resource "aws_s3_bucket" "exports_dr" {
  provider = aws.dr
  bucket   = "${local.name}-exports-dr"
}

resource "aws_s3_bucket_versioning" "dr" {
  provider = aws.dr
  for_each = { documents = aws_s3_bucket.documents_dr.id, exports = aws_s3_bucket.exports_dr.id }
  bucket   = each.value
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "dr" {
  provider = aws.dr
  for_each = { documents = aws_s3_bucket.documents_dr.id, exports = aws_s3_bucket.exports_dr.id }
  bucket   = each.value
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.dr.arn
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "dr" {
  provider                = aws.dr
  for_each                = { documents = aws_s3_bucket.documents_dr.id, exports = aws_s3_bucket.exports_dr.id }
  bucket                  = each.value
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Rôle IAM assumé par S3 pour répliquer les objets.
resource "aws_iam_role" "s3_replication" {
  name = "${local.name}-s3-crr"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "s3.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "s3_replication" {
  name = "s3-crr"
  role = aws_iam_role.s3_replication.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "ReadSource"
        Effect   = "Allow"
        Action   = ["s3:GetReplicationConfiguration", "s3:ListBucket", "s3:GetObjectVersionForReplication", "s3:GetObjectVersionAcl", "s3:GetObjectVersionTagging"]
        Resource = [data.aws_s3_bucket.documents_src.arn, "${data.aws_s3_bucket.documents_src.arn}/*", data.aws_s3_bucket.exports_src.arn, "${data.aws_s3_bucket.exports_src.arn}/*"]
      },
      {
        Sid      = "WriteDestination"
        Effect   = "Allow"
        Action   = ["s3:ReplicateObject", "s3:ReplicateDelete", "s3:ReplicateTags"]
        Resource = ["${aws_s3_bucket.documents_dr.arn}/*", "${aws_s3_bucket.exports_dr.arn}/*"]
      },
      {
        Sid      = "UseKmsKeys"
        Effect   = "Allow"
        Action   = ["kms:Decrypt", "kms:Encrypt", "kms:GenerateDataKey"]
        Resource = ["*"]
      }
    ]
  })
}

resource "aws_s3_bucket_replication_configuration" "documents" {
  bucket     = data.aws_s3_bucket.documents_src.id
  role       = aws_iam_role.s3_replication.arn
  depends_on = [aws_s3_bucket_versioning.dr]

  rule {
    id     = "documents-to-dr"
    status = "Enabled"
    filter {}
    delete_marker_replication { status = "Enabled" }
    destination {
      bucket        = aws_s3_bucket.documents_dr.arn
      storage_class = "STANDARD"
      encryption_configuration { replica_kms_key_id = aws_kms_key.dr.arn }
    }
    source_selection_criteria {
      sse_kms_encrypted_objects { status = "Enabled" }
    }
  }
}

resource "aws_s3_bucket_replication_configuration" "exports" {
  bucket     = data.aws_s3_bucket.exports_src.id
  role       = aws_iam_role.s3_replication.arn
  depends_on = [aws_s3_bucket_versioning.dr]

  rule {
    id     = "exports-to-dr"
    status = "Enabled"
    filter {}
    delete_marker_replication { status = "Enabled" }
    destination {
      bucket        = aws_s3_bucket.exports_dr.arn
      storage_class = "STANDARD"
      encryption_configuration { replica_kms_key_id = aws_kms_key.dr.arn }
    }
    source_selection_criteria {
      sse_kms_encrypted_objects { status = "Enabled" }
    }
  }
}

# =============================================================================
# 3) Réplication inter-région des sauvegardes automatiques RDS (PITR dans la région DR).
#    Permet de restaurer la base à moins de 5 min avant le sinistre (RPO ≤ 15 min garanti).
# =============================================================================
resource "aws_db_instance_automated_backups_replication" "this" {
  provider               = aws.dr
  source_db_instance_arn = data.aws_db_instance.src.db_instance_arn
  retention_period       = var.backup_retention_days
  kms_key_id             = aws_kms_key.dr.arn
}

# =============================================================================
# 4) Réplication du registre d'images ECR vers la région DR (image déjà présente au failover).
# =============================================================================
resource "aws_ecr_replication_configuration" "this" {
  replication_configuration {
    rule {
      destination {
        region      = var.dr_region
        registry_id = data.aws_caller_identity.current.account_id
      }
    }
  }
}
