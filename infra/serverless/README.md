# Déploiement « conteneurs sans serveur » (ECS & Azure Container Apps)

Variante de déploiement **sans Kubernetes** — dossier **séparé** de la variante EKS/AKS
(`infra/terraform/`), qu'elle **ne modifie pas**.

```
infra/serverless/
├── terraform/
│   ├── aws-ecs/     # ECS Fargate + ALB + RDS Multi-AZ + (SQS/Lambda/EventBridge/StepFunctions/Glue/S3/Secrets/CloudWatch/API GW)
│   └── azure-aca/   # Azure Container Apps + PostgreSQL HA + Front Door + Key Vault + Monitor + (Service Bus/Event Grid/Functions)
└── serverless-containers-integration.md   # tutoriel + architectures + glossaire (définitions, acronymes)
```

- Tutoriel complet, diagrammes et glossaire : [`serverless-containers-integration.md`](serverless-containers-integration.md).
- Le **socle** (réseau, base de données, chaîne asynchrone, supervision) est identique à la variante
  Kubernetes ; **seule la brique de calcul change** (ECS Fargate / Container Apps au lieu de EKS/AKS).
- Résidence des données au **Canada** : `ca-central-1` (AWS) / `canadacentral` (Azure).

```bash
# AWS
cd infra/serverless/terraform/aws-ecs && terraform init && terraform apply
# Azure
cd infra/serverless/terraform/azure-aca && az login && terraform init && terraform apply
```
