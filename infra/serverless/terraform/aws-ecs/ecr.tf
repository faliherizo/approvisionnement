# Registres d'images pour le backend et le frontend (scan de vulnérabilités à l'envoi).
locals {
  ecr_repos = ["backend", "frontend"]
}

resource "aws_ecr_repository" "this" {
  for_each             = toset(local.ecr_repos)
  name                 = "${var.project}/${each.key}"
  image_tag_mutability = "IMMUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
  encryption_configuration {
    encryption_type = "KMS"
  }
}

resource "aws_ecr_lifecycle_policy" "this" {
  for_each   = aws_ecr_repository.this
  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Conserver les 20 dernières images"
      selection    = { tagStatus = "any", countType = "imageCountMoreThan", countNumber = 20 }
      action       = { type = "expire" }
    }]
  })
}
