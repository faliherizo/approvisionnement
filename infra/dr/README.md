# Reprise après sinistre (DR) — Canada Central → Canada Est

Reprise **inter-région** de la solution, en complément de la HA multi-zone (qui, elle, soutient le
**SLA 99,99 %** au sein d'une région). Cibles : **RPO ≤ 15 min, RTO ≤ 4 h**.

## Production de l'AO IQ (SEC-11) : posture intégrée — ADR-039

La production Azure de l'AO IQ porte sa posture de relève **dans la pile principale** `infra/terraform/azure`
(`dr.tf` et variables `dr_*`), et non plus dans les piles séparées ci-dessous :

| Élément | Régime nominal | À la bascule |
|---|---|---|
| PostgreSQL | Réplique de lecture en Canada Est + sauvegardes géo-redondantes 35 jours | Promotion (permutation forcée) ; point d'écriture virtuel inchangé |
| Documents et exports | RA-GZRS, versionnage, suppression réversible, restauration à un instant donné | Bascule du compte (lecture secondaire possible avant) |
| Images | Registre Premium géo-répliqué en Canada Est | Tirées localement |
| Secrets et clés | Coffre de relève : `db-url`, `db-password`, `clm-cle-maitresse` (ADR-037) ; clés HSM par `scripts/sauvegarder-cles-azure.sh` | Lus dans le coffre de relève |
| Calcul | AKS « pilot light » (1 nœud système, pool applicatif à 0) | Plan de contrôle Standard, pool agrandi, même chart Helm |
| Entrée | Front Door : origine de relève en priorité 2, sonde `/api/health` | Bascule par la sonde ; origine primaire désactivée |

```bash
# Exercice annuel (ne touche pas la production) ; bascule réelle ; retour planifié
ORIGINE_SECOURS=ingress-ce.contrat.vinaitsolution.com scripts/bascule-azure.sh exercice
ORIGINE_SECOURS=ingress-ce.contrat.vinaitsolution.com scripts/bascule-azure.sh bascule
scripts/bascule-azure.sh retour
```

**Essai de relève mesuré** : `.github/workflows/releve.yml` (déclenché par `workflow_dispatch` ou une poussée sur
`releve/**`) monte la pile de production conteneurisée, charge un jeu de données, détruit le site primaire, reprend
sur un site neuf (scénarios `replique` et `sauvegarde`), vérifie la preuve et le déchiffrement, et mesure RTO et RPO
avec une porte bloquante. Scripts : [`essai/`](essai/). Plan de relève et résultats : document 20 de la réponse à l'AO.

Les piles `azure/replication` et `azure/failover` ci-dessous restent une variante (relève sur Container Apps) et la
référence de la reprise AWS.

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
