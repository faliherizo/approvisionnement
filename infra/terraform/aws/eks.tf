# Cluster EKS avec un groupe de noeuds géré réparti sur les 3 AZ (haute disponibilité multi-zone).
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.24"

  cluster_name    = "${local.name}-eks"
  cluster_version = var.kubernetes_version

  # API du plan de contrôle : accès privé depuis le VPC, public restreint aux seules adresses d'administration.
  cluster_endpoint_public_access       = true
  cluster_endpoint_private_access      = true
  cluster_endpoint_public_access_cidrs = var.ip_administration

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets # noeuds dans les sous-réseaux privés

  # Modules d'extension gérés par AWS.
  cluster_addons = {
    coredns            = { most_recent = true }
    kube-proxy         = { most_recent = true }
    vpc-cni            = { most_recent = true }
    aws-ebs-csi-driver = { most_recent = true } # volumes persistants multi-AZ
  }

  # IRSA : les pods obtiennent des rôles IAM via OIDC (pas de clés statiques).
  enable_irsa = true

  eks_managed_node_groups = {
    app = {
      instance_types = var.eks_node_instance_types
      min_size       = var.eks_node_min
      max_size       = var.eks_node_max
      desired_size   = var.eks_node_min
      subnet_ids     = module.vpc.private_subnets # étalé sur les 3 AZ
      labels         = { workload = "app" }
    }
  }

  # Accès administrateur via les Access Entries EKS (remplace aws-auth).
  enable_cluster_creator_admin_permissions = true

  tags = local.common_tags
}

# Rôle IRSA pour les pods du backend : accès S3 (documents), SQS (files de publication), Secrets Manager.
module "irsa_backend" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.44"

  role_name = "${local.name}-backend-irsa"

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["contrat:backend"]
    }
  }
}

# Politique attachée au rôle IRSA du backend (moindre privilège).
resource "aws_iam_role_policy" "backend_irsa" {
  name = "${local.name}-backend-access"
  role = module.irsa_backend.iam_role_name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid      = "Documents"
        Effect   = "Allow"
        Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket", "s3:DeleteObject"]
        Resource = [aws_s3_bucket.documents.arn, "${aws_s3_bucket.documents.arn}/*"]
      },
      { Sid = "Chiffrement", Effect = "Allow", Action = ["kms:GenerateDataKey", "kms:Decrypt"], Resource = [aws_kms_key.donnees.arn] },
      {
        Sid      = "PublicationQueue"
        Effect   = "Allow"
        Action   = ["sqs:SendMessage", "sqs:GetQueueAttributes"]
        Resource = [aws_sqs_queue.publication.arn]
      },
      {
        Sid      = "Secrets"
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = [aws_secretsmanager_secret.app.arn, aws_secretsmanager_secret.db.arn]
      },
      {
        Sid      = "EmitEvents"
        Effect   = "Allow"
        Action   = ["events:PutEvents"]
        Resource = [aws_cloudwatch_event_bus.domain.arn]
      }
    ]
  })
}
