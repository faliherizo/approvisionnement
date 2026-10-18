# VPC réparti sur 3 zones de disponibilité : sous-réseaux publics (ALB/NAT) et privés (EKS, RDS).
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.8"

  name = "${local.name}-vpc"
  cidr = var.vpc_cidr
  azs  = var.azs

  # Un sous-réseau public et un privé par AZ (HA).
  public_subnets  = [for i, az in var.azs : cidrsubnet(var.vpc_cidr, 4, i)]
  private_subnets = [for i, az in var.azs : cidrsubnet(var.vpc_cidr, 4, i + 8)]

  enable_nat_gateway     = true
  single_nat_gateway     = false # une passerelle NAT par AZ = pas de point de défaillance unique
  one_nat_gateway_per_az = true
  enable_dns_hostnames   = true

  # Étiquettes requises par EKS pour la découverte des sous-réseaux par le load balancer controller.
  public_subnet_tags = {
    "kubernetes.io/role/elb"                  = "1"
    "kubernetes.io/cluster/${local.name}-eks" = "shared"
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"         = "1"
    "kubernetes.io/cluster/${local.name}-eks" = "shared"
  }
}
