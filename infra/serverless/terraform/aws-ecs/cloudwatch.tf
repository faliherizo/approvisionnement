# Observabilité et mesure de la disponibilité (SLA 99,99 %) : alertes SNS, alarmes, canari Synthetics.
resource "aws_sns_topic" "alerts" {
  name = "${local.name}-alerts"
  # Alertes chiffrées au repos par la clé de l'organisation.
  kms_master_key_id = aws_kms_key.donnees.id
}

resource "aws_sns_topic_subscription" "email" {
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

# Alarme base de données (CPU) et file de lettres mortes (échecs de publication).
resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${local.name}-rds-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 3
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 85
  alarm_actions       = [aws_sns_topic.alerts.arn]
  dimensions          = { DBInstanceIdentifier = aws_db_instance.postgres.id }
}

resource "aws_cloudwatch_metric_alarm" "dlq" {
  alarm_name          = "${local.name}-publication-dlq"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "ApproximateNumberOfMessagesVisible"
  namespace           = "AWS/SQS"
  period              = 300
  statistic           = "Maximum"
  threshold           = 0
  alarm_actions       = [aws_sns_topic.alerts.arn]
  dimensions          = { QueueName = aws_sqs_queue.publication_dlq.name }
}

# --- Canari CloudWatch Synthetics : sonde l'URL de santé toutes les minutes = mesure indépendante
#     de la disponibilité qui atteste le taux servant au calcul du SLA 99,99 %. ---
resource "aws_s3_bucket" "canary" {
  bucket        = "${local.name}-canary-artifacts"
  force_destroy = true
}

# Artefacts des sondes synthétiques (captures d'écran de l'application) : jamais publics, chiffrés au repos.
resource "aws_s3_bucket_public_access_block" "canary" {
  bucket                  = aws_s3_bucket.canary.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "canary" {
  bucket = aws_s3_bucket.canary.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.donnees.arn
    }
  }
}

resource "aws_iam_role" "canary" {
  name = "${local.name}-canary"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "lambda.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "canary" {
  name = "canary"
  role = aws_iam_role.canary.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      { Effect = "Allow", Action = ["s3:PutObject", "s3:GetBucketLocation", "s3:ListAllMyBuckets"], Resource = ["*"] },
      { Sid = "Chiffrement", Effect = "Allow", Action = ["kms:GenerateDataKey", "kms:Decrypt"], Resource = [aws_kms_key.donnees.arn] },
      { Effect = "Allow", Action = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"], Resource = ["*"] },
      { Effect = "Allow", Action = ["cloudwatch:PutMetricData"], Resource = ["*"], Condition = { StringEquals = { "cloudwatch:namespace" = "CloudWatchSynthetics" } } }
    ]
  })
}

data "archive_file" "canary" {
  type        = "zip"
  output_path = "${path.module}/.build/canary.zip"
  source {
    # AWS exige le chemin nodejs/node_modules/<nom>.js pour un canari Node.js.
    filename = "nodejs/node_modules/apercu.js"
    content  = <<-JS
      const synthetics = require('Synthetics');
      const log = require('SyntheticsLogger');
      const apercu = async function () {
        const page = await synthetics.getPage();
        const url = process.env.URL;
        const res = await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
        if (!res || res.status() > 399) { throw new Error('Statut ' + (res && res.status())); }
        log.info('Disponibilité OK: ' + url);
      };
      exports.handler = async () => { return await synthetics.executeStep('apercu', apercu); };
    JS
  }
}

resource "aws_synthetics_canary" "availability" {
  name                 = substr("${var.project}-avail", 0, 21) # ≤ 21 caractères
  artifact_s3_location = "s3://${aws_s3_bucket.canary.id}/canary"
  execution_role_arn   = aws_iam_role.canary.arn
  runtime_version      = "syn-nodejs-puppeteer-9.0"
  handler              = "apercu.handler"
  zip_file             = data.archive_file.canary.output_path
  start_canary         = true

  schedule {
    expression = "rate(1 minute)" # sonde chaque minute
  }
  run_config {
    timeout_in_seconds    = 30
    environment_variables = { URL = var.availability_canary_url }
  }
  success_retention_period = 31
  failure_retention_period = 31
}

# Alarme si la disponibilité mesurée par le canari tombe (déclenche l'intervention 24/7).
resource "aws_cloudwatch_metric_alarm" "availability" {
  alarm_name          = "${local.name}-disponibilite"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "SuccessPercent"
  namespace           = "CloudWatchSynthetics"
  period              = 300
  statistic           = "Average"
  threshold           = 100
  alarm_actions       = [aws_sns_topic.alerts.arn]
  dimensions          = { CanaryName = aws_synthetics_canary.availability.name }
}
