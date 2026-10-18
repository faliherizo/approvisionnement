# Orchestration de la publication : rendu du document → dépôt vers chaque cible (via SQS/Lambda) →
# export/journalisation (Glue) → notification. Rejeu et gestion d'erreur intégrés.
resource "aws_iam_role" "sfn" {
  name = "${local.name}-sfn"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "states.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "sfn" {
  name = "orchestration"
  role = aws_iam_role.sfn.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      { Effect = "Allow", Action = ["sqs:SendMessage"], Resource = [aws_sqs_queue.publication.arn] },
      { Effect = "Allow", Action = ["lambda:InvokeFunction"], Resource = [aws_lambda_function.publication_worker.arn] },
      { Effect = "Allow", Action = ["glue:StartJobRun", "glue:GetJobRun"], Resource = ["*"] },
      { Effect = "Allow", Action = ["logs:*"], Resource = ["*"] }
    ]
  })
}

resource "aws_sfn_state_machine" "publication" {
  name     = "${local.name}-publication"
  role_arn = aws_iam_role.sfn.arn

  definition = jsonencode({
    Comment = "Publication d'un document approuvé vers Microsoft 365 / SAP"
    StartAt = "MettreEnFile"
    States = {
      MettreEnFile = {
        Type     = "Task"
        Resource = "arn:aws:states:::sqs:sendMessage"
        Parameters = {
          QueueUrl        = aws_sqs_queue.publication.url
          "MessageBody.$" = "$"
        }
        Next = "AttendreTraitement"
      }
      AttendreTraitement = {
        Type    = "Wait"
        Seconds = 30
        Next    = "Journaliser"
      }
      Journaliser = {
        Type       = "Task"
        Resource   = "arn:aws:states:::glue:startJobRun.sync"
        Parameters = { JobName = aws_glue_job.export.name }
        Retry      = [{ ErrorEquals = ["States.ALL"], MaxAttempts = 2, IntervalSeconds = 15 }]
        End        = true
      }
    }
  })

  logging_configuration {
    log_destination        = "${aws_cloudwatch_log_group.sfn.arn}:*"
    include_execution_data = true
    level                  = "ALL"
  }
}

resource "aws_cloudwatch_log_group" "sfn" {
  name              = "/aws/vendedlogs/${local.name}-publication-sfn"
  retention_in_days = 90
}

# Rôle permettant à EventBridge de démarrer la machine à états.
resource "aws_iam_role" "eventbridge_invoke_sfn" {
  name = "${local.name}-eb-invoke-sfn"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Principal = { Service = "events.amazonaws.com" }, Action = "sts:AssumeRole" }]
  })
}

resource "aws_iam_role_policy" "eventbridge_invoke_sfn" {
  name = "start-execution"
  role = aws_iam_role.eventbridge_invoke_sfn.id
  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [{ Effect = "Allow", Action = ["states:StartExecution"], Resource = [aws_sfn_state_machine.publication.arn] }]
  })
}
