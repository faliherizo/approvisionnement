# Réseau de reprise : VPC sur ≥ 3 AZ de la région DR (sous-réseaux publics = ALB/NAT, privés = ECS/RDS).
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.8"

  name = "${local.name}-vpc"
  cidr = var.vpc_cidr
  azs  = var.azs

  public_subnets  = [for i, az in var.azs : cidrsubnet(var.vpc_cidr, 4, i)]
  private_subnets = [for i, az in var.azs : cidrsubnet(var.vpc_cidr, 4, i + 8)]

  enable_nat_gateway     = true
  single_nat_gateway     = false
  one_nat_gateway_per_az = true
  enable_dns_hostnames   = true
}

# --- Groupes de sécurité ---
resource "aws_security_group" "alb" {
  name_prefix = "${local.name}-alb-"
  vpc_id      = module.vpc.vpc_id
  description = "ALB de reprise : trafic HTTP/HTTPS entrant."

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr] # aucune sortie hors du VPC : rien à joindre sur Internet
  }
  lifecycle { create_before_destroy = true }
}

resource "aws_security_group" "ecs_service" {
  name_prefix = "${local.name}-svc-"
  vpc_id      = module.vpc.vpc_id
  description = "Tâches ECS : entrant depuis l'ALB uniquement."

  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  lifecycle { create_before_destroy = true }
}

resource "aws_security_group" "rds" {
  name_prefix = "${local.name}-rds-"
  vpc_id      = module.vpc.vpc_id
  description = "PostgreSQL : accès depuis les tâches ECS uniquement."

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr] # aucune sortie hors du VPC : rien à joindre sur Internet
  }
  lifecycle { create_before_destroy = true }
}
