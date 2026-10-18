# File de publication asynchrone (dépôts Microsoft 365 / SAP) avec file de lettres mortes (DLQ).
resource "aws_sqs_queue" "publication_dlq" {
  name                      = "${local.name}-publication-dlq"
  message_retention_seconds = 1209600 # 14 jours
  sqs_managed_sse_enabled   = true
}

resource "aws_sqs_queue" "publication" {
  name                       = "${local.name}-publication"
  visibility_timeout_seconds = 300
  message_retention_seconds  = 345600
  sqs_managed_sse_enabled    = true

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.publication_dlq.arn
    maxReceiveCount     = 5
  })
}
