output "region" {
  value = var.region
}

output "eks_cluster_name" {
  value = module.eks.cluster_name
}

output "eks_configure_kubectl" {
  description = "Commande pour configurer kubectl."
  value       = "aws eks update-kubeconfig --region ${var.region} --name ${module.eks.cluster_name}"
}

output "backend_irsa_role_arn" {
  description = "À annoter sur le ServiceAccount 'backend' du namespace 'contrat'."
  value       = module.irsa_backend.iam_role_arn
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
