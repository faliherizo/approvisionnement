output "alb_dns_name" {
  description = "Nom DNS de l'ALB de reprise — cible du basculement Front Door / Route 53."
  value       = aws_lb.this.dns_name
}

output "db_endpoint" {
  description = "Endpoint de la base PostgreSQL restaurée dans la région DR."
  value       = aws_db_instance.restored.endpoint
}

output "ecs_cluster" {
  description = "Nom du cluster ECS de reprise."
  value       = aws_ecs_cluster.this.name
}
