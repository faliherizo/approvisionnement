output "region" {
  value = var.region
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.this.name
}

output "alb_dns_name" {
  description = "Nom DNS de l'ALB (à pointer via Route 53 / CloudFront vers contrat.vinaitsolution.com)."
  value       = aws_lb.this.dns_name
}

output "ecs_task_role_arn" {
  description = "Rôle IAM des tâches backend (permissions applicatives)."
  value       = aws_iam_role.ecs_task.arn
}

output "ecr_backend_url" {
  value = aws_ecr_repository.this["backend"].repository_url
}

output "ecr_frontend_url" {
  value = aws_ecr_repository.this["frontend"].repository_url
}

output "rds_endpoint" {
  value = aws_db_instance.postgres.address
}

output "documents_bucket" {
  value = aws_s3_bucket.documents.id
}

output "api_gateway_endpoint" {
  value = aws_apigatewayv2_api.this.api_endpoint
}

output "db_secret_arn" {
  value = aws_secretsmanager_secret.db.arn
}

output "app_secret_arn" {
  value = aws_secretsmanager_secret.app.arn
}

output "publication_queue_url" {
  value = aws_sqs_queue.publication.url
}

output "domain_event_bus" {
  value = aws_cloudwatch_event_bus.domain.name
}
