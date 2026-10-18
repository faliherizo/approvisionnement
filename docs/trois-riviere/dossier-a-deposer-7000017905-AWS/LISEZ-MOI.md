# Dossier de soumission — **variante AWS** (AO 7000017905)

Version **hébergée sur Amazon Web Services (AWS)**, région **`ca-central-1` (Montréal, Québec)**,
DR **`ca-west-1`**. Elle **ne remplace pas** la version Azure ([`../dossier-a-deposer-7000017905/`](../dossier-a-deposer-7000017905/)),
qui demeure intacte. Une seule des deux est déposée selon l'hébergeur retenu.

## Contenu

| Fichier | Rôle |
|---|---|
| `7000017905_VINA-IT-Solutions_Soumission-complete-AWS.pdf` | **Dossier complet** (un seul PDF) — variante AWS |
| `7000017905_VINA-IT-Solutions_Memoire-technique-AWS.pdf` | Mémoire technique (hébergement AWS) |
| `7000017905_VINA-IT-Solutions_Architecture-HA-SLA-Performance-AWS.pdf` | Architecture HA / SLA 99,99 % (AWS) |
| `_src/` | Sources Markdown ayant servi au rendu |

> La **démonstration vidéo** (`…_Demonstration-art26.mp4`) est **commune** aux deux variantes : voir le
> dossier Azure.

## Ce qui change par rapport à la version Azure (et **seulement** cela)

Les prix (bordereau), la formule de qualification, les attestations signées (intégrité, résolution,
engagement, OQLF, Revenu Québec) sont **identiques** — seul l'**hébergement** diffère :

| Volet | Azure | **AWS (cette variante)** |
|---|---|---|
| Région / résidence | Canada Central (Toronto) | **`ca-central-1` (Montréal, Québec)** |
| Calcul | Container Apps | **ECS Fargate** (3 zones) |
| Base de données | PostgreSQL Flexible HA zone-redondante | **RDS PostgreSQL Multi-AZ** |
| Entrée / WAF | Front Door Standard + OWASP à l'ingress | **CloudFront + ALB + AWS WAF (OWASP managé)** |
| Secrets | Key Vault | **Secrets Manager + KMS** |
| Supervision | Azure Monitor + App Insights | **CloudWatch + Synthetics** |
| DR | Canada Est | **`ca-west-1` (Calgary)** |
| Coût infra (réservé 3 ans) | ~12–14 k$/an | **~11–13 k$/an** |

Documents **régénérés à jour (68 000 $, contrat #4, procédure §30, RPO/RTO)** et **adaptés AWS** :
**bordereau, tableau de conformité, formule de qualification, mémoire, architecture**, plus l'**annexe
Plan de reprise après sinistre**. Pages **réutilisées telles quelles** (SIGNÉES) : attestation
d'intégrité, formule de résolution, engagement de confidentialité, conformité OQLF, attestation de
Revenu Québec, checklist.

## Structure du PDF complet (30 pages)

Checklist · Bordereau (68 000 $) · Tableau de conformité (AWS) · Formule de qualification (AWS) ·
Attestation d'intégrité (signée) · Résolution (signée) · Engagement (signé) · OQLF · Attestation Revenu
Québec · Mémoire (AWS) · Architecture (AWS) · **Annexe — Plan de reprise après sinistre (RPO ≤ 15 min /
RTO ≤ 4 h)**.

## Notes

1. **Hébergement 100 % AWS** dans les documents rédactionnels et les formulaires (aucune mention Azure
   d'hébergement). Le mémoire conserve une mention **Azure au titre de l'expérience de la firme** (mandats
   MSP/INSPQ) — fait historique, indépendant de l'hébergeur retenu ; le plan de reprise cite Azure dans
   son **tableau d'équivalence** de services (document volontairement bi-cloud).
2. **Licence 68 000 $/an**, identique à la variante Azure. Bordereau, qualification et attestations
   signées identiques ; seul l'hébergement diffère.

## Justification du choix AWS

Voir [`../AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md`](../AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md) et
[`../PROPOSITIONS-HEBERGEMENT-AZURE-ET-AWS.md`](../PROPOSITIONS-HEBERGEMENT-AZURE-ET-AWS.md) :
AWS `ca-central-1` (Montréal) garantit le **99,99 % multi-zone au Québec**, un **WAF OWASP managé** peu
coûteux, et une **résidence québécoise** des données. IaC prêt : [`../../../infra/terraform/aws/`](../../../infra/terraform/aws/).
