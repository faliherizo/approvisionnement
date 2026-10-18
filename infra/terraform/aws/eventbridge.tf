# Bus d'événements métier : le backend publie « document.approuve », qui déclenche l'orchestration
# de publication (Step Functions). Découple l'application des traitements asynchrones.
resource "aws_cloudwatch_event_bus" "domain" {
  name = "${local.name}-domain"
}

resource "aws_cloudwatch_event_rule" "document_approuve" {
  name           = "${local.name}-document-approuve"
  event_bus_name = aws_cloudwatch_event_bus.domain.name
  description    = "Déclenche l'orchestration de publication quand un document est approuvé."

  event_pattern = jsonencode({
    "detail-type" = ["document.approuve"]
    source        = ["contractuelle_contrat"]
  })
}

resource "aws_cloudwatch_event_target" "to_stepfunctions" {
  rule           = aws_cloudwatch_event_rule.document_approuve.name
  event_bus_name = aws_cloudwatch_event_bus.domain.name
  arn            = aws_sfn_state_machine.publication.arn
  role_arn       = aws_iam_role.eventbridge_invoke_sfn.arn
}

# Planification (règle cron) : rapport de disponibilité mensuel (SLA) via EventBridge Scheduler-like rule.
resource "aws_cloudwatch_event_rule" "rapport_mensuel" {
  name                = "${local.name}-rapport-sla-mensuel"
  schedule_expression = "cron(0 12 1 * ? *)" # 1er du mois, 12h UTC
  description         = "Génère le rapport de disponibilité mensuel (SLA 99,99 %)."
}

resource "aws_cloudwatch_event_target" "rapport_glue" {
  rule  = aws_cloudwatch_event_rule.rapport_mensuel.name
  arn   = aws_lambda_function.publication_worker.arn # réutilisé pour déclencher le job Glue
  input = jsonencode({ action = "declencher-rapport-sla" })
}
