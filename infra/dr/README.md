# Reprise après sinistre (DR) — Canada Central → Canada Est

Reprise **inter-région** de la solution, en complément de la HA multi-zone (qui, elle, soutient le
**SLA 99,99 %** au sein d'une région). Cibles : **RPO ≤ 15 min, RTO ≤ 4 h**.

> Procédure, définitions et budget de temps : [`docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md`](../../docs/trois-riviere/PLAN-REPRISE-SINISTRE-RPO-RTO.md).

```
infra/dr/
├── azure/                   # PRODUCTION DE L'AO — Canada Central → Canada Est
│   ├── replication/         # « pilot light » : ACR DR + réplication d'objets Storage + sauvegardes géo PostgreSQL
│   └── failover/            # à la bascule : VNet + Container Apps + GeoRestore PostgreSQL + Key Vault
├── aws/                     # reprise équivalente — ca-central-1 → ca-west-1
│   ├── replication/         # « pilot light » : CRR S3 + PITR RDS + réplication ECR + KMS
│   └── failover/            # à la bascule : VPC + ECS/Fargate + restore RDS (PITR) + ALB
└── scripts/
    ├── dr-failover-azure.sh # orchestration Azure (10 étapes) ; option --drill pour les exercices
    └── dr-failover.sh       # orchestration AWS (10 étapes) ; option --drill pour les exercices
```

## Principe

- **En régime nominal**, seul `replication/` est appliqué : la région de reprise ne porte **aucun
  calcul** (coût minimal), uniquement la donnée (S3, sauvegardes RDS) et les images (ECR) répliquées.
- **Au sinistre**, `dr-failover.sh` lit les sorties de `replication/`, applique `failover/`
  (réseau + sécurité + ECS/Fargate + restauration PostgreSQL par PITR + S3 + image), vérifie la santé,
  puis redirige le trafic (Route 53).
- **Séparation stricte** : ce dossier **ne modifie pas** les stacks de production
  (`infra/terraform/`, `infra/serverless/`). La réplication S3/RDS est attachée aux ressources de
  production **en lecture** (data sources) via des ressources autonomes.

## Mise en place — Azure (production de l'AO)

```bash
# 1) Posture DR permanente (à garder appliquée)
cd infra/dr/azure/replication && terraform init && terraform apply

# 2) Exercice de bascule (ne touche pas la production)
cd infra/dr/scripts && ./dr-failover-azure.sh --drill

# 3) Bascule réelle (sinistre) — demande confirmation « OUI »
AFD_PROFILE=<profil-front-door> AFD_ORIGIN_GROUP=<groupe> ./dr-failover-azure.sh
```

## Mise en place — AWS

```bash
cd infra/dr/aws/replication && terraform init && terraform apply
cd infra/dr/scripts && ./dr-failover.sh --drill
HOSTED_ZONE_ID=<zone> ./dr-failover.sh
```

Résidence des données **au Canada** dans les deux cas : **Canada Central ↔ Canada Est** (Azure) /
`ca-central-1` ↔ `ca-west-1` (AWS). Détail, diagrammes Mermaid et budget de temps : voir le runbook.
