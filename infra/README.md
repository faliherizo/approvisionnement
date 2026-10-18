# Infrastructure as Code (IaC) — contractuelle_contrat

Déploiement de la solution sur **Azure** et **AWS** en Terraform, avec CI/CD et déploiement
Kubernetes multi-zone pour soutenir le **SLA 99,99 %**.

```
infra/
├── terraform/
│   ├── azure/     # AKS, PostgreSQL Flexible HA, Front Door+WAF, Key Vault, Monitor/App Insights, ACR, Service Bus, Event Grid, Functions
│   └── aws/       # EKS, RDS Multi-AZ, API Gateway, S3, IAM, CloudWatch+Synthetics, Secrets Manager, SQS, Lambda, EventBridge, Step Functions, Glue
├── cicd/                # CI/CD modulaire — voir ci-cd-integration.md
│   ├── github-actions/  # orchestrateurs (ci, cd-aws, cd-azure) + workflows réutilisables + action composite
│   ├── azure-devops/    # azure-pipelines.yml + templates de stage
│   └── ci-cd-integration.md   # tutoriel CI/CD (Azure & AWS) + glossaire
├── k8s/           # chart Helm (backend + frontend, répartition multi-zone) + values-azure.yaml / values-aws.yaml
├── dr/            # reprise après sinistre inter-région (pilot light + failover) — RPO ≤ 15 min, RTO ≤ 4 h
├── azure-terraform-integration.md   # tutoriel Azure (+ architecture)
└── aws-terraform-integration.md     # tutoriel AWS (+ architecture)
```

## Démarrage rapide

| Cible | Tutoriel |
|---|---|
| Azure — Kubernetes (AKS, IaC) | [`azure-terraform-integration.md`](azure-terraform-integration.md) |
| AWS — Kubernetes (EKS, IaC)   | [`aws-terraform-integration.md`](aws-terraform-integration.md) |
| **Sans Kubernetes** — ECS (AWS) & Container Apps (Azure) | [`serverless/serverless-containers-integration.md`](serverless/serverless-containers-integration.md) |
| CI/CD (Azure & AWS) | [`cicd/ci-cd-integration.md`](cicd/ci-cd-integration.md) |
| **Reprise après sinistre** (RPO/RTO, bascule inter-région) | [`dr/README.md`](dr/README.md) · runbook : [`../docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md`](../docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md) |

> Deux variantes de déploiement **coexistent, indépendantes** : Kubernetes (`terraform/`, EKS/AKS) et
> conteneurs sans serveur (`serverless/`, ECS/Container Apps). Choisir selon le besoin.

Chaque tutoriel contient : l'**architecture de déploiement** (diagramme), la liste des services et
leur rôle, le *bootstrap* de l'état distant, le déploiement (`terraform apply` + Helm), le CI/CD, et
la démonstration du **SLA 99,99 %** (redondance multi-zone + mesure indépendante de disponibilité).

## Principes

- **Zéro secret statique** : CI/CD par **OIDC** (fédération d'identité) ; secrets applicatifs dans **Secrets Manager** (AWS) / **Key Vault** (Azure).
- **Moindre privilège** : IRSA (AWS) / Workload Identity (Azure) par charge de travail.
- **État distant chiffré et verrouillé** : S3 + DynamoDB (AWS) / Storage Account (Azure).
- **Haute disponibilité multi-zone** : calcul, base de données, entrée réseau et traitements asynchrones répartis sur ≥ 3 zones.
- **Mesure du SLA** : canari CloudWatch Synthetics (AWS) / test de disponibilité Application Insights (Azure).

> Résidence des données au **Canada** : `ca-central-1` (AWS) / `canadacentral` (Azure).
