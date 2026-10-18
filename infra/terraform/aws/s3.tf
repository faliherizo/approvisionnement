# Stockage objet : documents/générés, exports (réversibilité), artefacts Glue.
resource "aws_s3_bucket" "documents" {
  bucket = "${local.name}-documents"
}

resource "aws_s3_bucket" "exports" {
  bucket = "${local.name}-exports"
}

# Sécurisation commune : versionnage, chiffrement, blocage de l'accès public.
resource "aws_s3_bucket_versioning" "this" {
  for_each = { documents = aws_s3_bucket.documents.id, exports = aws_s3_bucket.exports.id }
  bucket   = each.value
  versioning_configuration { status = "Enabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "this" {
  for_each = { documents = aws_s3_bucket.documents.id, exports = aws_s3_bucket.exports.id }
  bucket   = each.value
  rule {
    apply_server_side_encryption_by_default { sse_algorithm = "aws:kms" }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "this" {
  for_each                = { documents = aws_s3_bucket.documents.id, exports = aws_s3_bucket.exports.id }
  bucket                  = each.value
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
