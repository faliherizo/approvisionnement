# Worker de publication : consomme la file SQS et dépose vers M365/SAP.
data "archive_file" "publication_worker" {
  type        = "zip"
  source_file = "${path.module}/lambda-src/publication_worker.py"
  output_path = "${path.module}/.build/publication_worker.zip"
}

resource "aws_iam_role" "lambda_worker" {
  name = "${local.name}-lambda-worker"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_role_policy" "lambda_worker" {
  name = "access"
  role = aws_iam_role.lambda_worker.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"]
        Resource = "arn:aws:logs:*:*:*"
      },
      {
        Effect   = "Allow"
        Action   = ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource = [aws_sqs_queue.publication.arn]
      },
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = [aws_secretsmanager_secret.app.arn]
      },
      {
        Effect   = "Allow"
        Action   = ["events:PutEvents"]
        Resource = [aws_cloudwatch_event_bus.domain.arn]
      },
      {
        Effect   = "Allow"
        Action   = ["glue:StartJobRun"]
        Resource = [aws_glue_job.export.arn]
      }
    ]
  })
}

resource "aws_lambda_function" "publication_worker" {
  function_name    = "${local.name}-publication-worker"
  runtime          = "python3.12"
  handler          = "publication_worker.handler"
  role             = aws_iam_role.lambda_worker.arn
  filename         = data.archive_file.publication_worker.output_path
  source_code_hash = data.archive_file.publication_worker.output_base64sha256
  timeout          = 120
  memory_size      = 512

  environment {
    variables = {
      APP_SECRET_ARN = aws_secretsmanager_secret.app.arn
      EVENT_BUS      = aws_cloudwatch_event_bus.domain.name
      GLUE_JOB       = aws_glue_job.export.name
    }
  }
}

# Branche la file SQS sur la Lambda (traitement asynchrone des publications).
resource "aws_lambda_event_source_mapping" "sqs" {
  event_source_arn = aws_sqs_queue.publication.arn
  function_name    = aws_lambda_function.publication_worker.arn
  batch_size       = 5
}

# Autorise EventBridge (règle mensuelle) à invoquer la Lambda.
resource "aws_lambda_permission" "eventbridge" {
  statement_id  = "AllowEventBridge"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.publication_worker.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.rapport_mensuel.arn
}
