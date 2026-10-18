# Glue : ETL d'export (réversibilité — export complet des données en fin de contrat, exigence 19/36)
# et alimentation des rapports (disponibilité, portefeuille documentaire).
resource "aws_iam_role" "glue" {
  name = "${local.name}-glue"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "glue.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy_attachment" "glue_service" {
  role       = aws_iam_role.glue.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSGlueServiceRole"
}

resource "aws_iam_role_policy" "glue_s3" {
  name = "s3-access"
  role = aws_iam_role.glue.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Resource = [aws_s3_bucket.exports.arn, "${aws_s3_bucket.exports.arn}/*"]
    }]
  })
}

resource "aws_glue_catalog_database" "this" {
  name = replace("${local.name}_catalog", "-", "_")
}

resource "aws_glue_job" "export" {
  name         = "${local.name}-export"
  role_arn     = aws_iam_role.glue.arn
  glue_version = "4.0"

  command {
    name            = "glueetl"
    script_location = "s3://${aws_s3_bucket.exports.id}/scripts/export.py"
    python_version  = "3"
  }

  default_arguments = {
    "--job-language"     = "python"
    "--TempDir"          = "s3://${aws_s3_bucket.exports.id}/tmp/"
    "--enable-metrics"   = "true"
    "--source_db_secret" = aws_secretsmanager_secret.db.arn
    "--target_bucket"    = aws_s3_bucket.exports.id
  }

  number_of_workers = 2
  worker_type       = "G.1X"
  timeout           = 60
}
