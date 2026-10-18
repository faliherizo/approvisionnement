---
title: Deux propositions d'hébergement pour la soumission — Azure et AWS (AO 7000017905)
subtitle: Prix d'infrastructure validés aux sources officielles (28 septembre 2026), surcoût WAF retiré
---

# Deux propositions prêtes à soumettre : Azure ou AWS

> Même **solution fonctionnelle**, même **licence 68 000 $/an**, mêmes engagements (**SLA 99,99 %**,
> **RPO ≤ 15 min / RTO ≤ 4 h**, données **au Canada**, ISO 27001 / SOC 2 Type II). Seul l'**hébergeur**
> diffère. Le **surcoût WAF a été retiré** des deux (Azure : Front Door **Standard** + OWASP à l'ingress).
> Les deux tiennent **très largement dans le budget 68 k$**.

## Proposition A — Microsoft Azure (région Canada Central)

| Volet | Choix |
|---|---|
| **Région / résidence** | `Canada Central` (Toronto) — **3 zones de disponibilité**. Données **au Canada**. |
| **HA 99,99 %** | Container Apps sur **3 zones** + PostgreSQL Flexible **HA zone-redondante** (primaire + veille synchrone) + Front Door redondé. |
| **Entrée / sécurité** | **Front Door Standard** (TLS, anti-DDoS, routage) + **WAF OWASP (Core Rule Set) à l'ingress** (ModSecurity/Coraza) — **sans le WAF Premium à 5 489 $/an**. |
| **Supervision / SLA** | Azure Monitor + **Application Insights** (tests de disponibilité synthétiques managés). |
| **DR** | Reprise inter-région vers **`Canada Est`** (ville de Québec) — **DR uniquement** (pas de zones). |
| **Secrets** | Azure **Key Vault**. |
| **Intégration M365 (art. 9)** | **Native** : Entra ID (SSO), authentification **Graph** dans le **même écosystème** que la Ville. **← avantage clé.** |
| **Coût infra annuel (validé)** | **~18 500 – 20 500 $ PAYG** ; **~12 000 – 14 000 $ réservé 3 ans**. |
| **Avantages** | Intégration M365 native ; **expertise du fournisseur sur Azure** ; **dossier déjà rédigé** autour d'Azure ; supervision SLA managée. |
| **Vigilance** | HA obligatoirement en **Canada Central (Ontario)** — `Canada Est` (Québec) n'a pas de zones ; WAF OWASP à exploiter à l'ingress (charge ops modérée). |

## Proposition B — Amazon Web Services (région ca-central-1, Montréal)

| Volet | Choix |
|---|---|
| **Région / résidence** | `ca-central-1` (**Montréal, Québec**) — **3 zones de disponibilité**. Données **au Québec** (résidence plus forte que « Canada »). **← avantage pour une ville québécoise.** |
| **HA 99,99 %** | ECS Fargate sur **3 zones** + RDS PostgreSQL **Multi-AZ** (primaire + veille) + ALB redondé. |
| **Entrée / sécurité** | **ALB + AWS WAF** (règles managées **OWASP**, facturé **par requête ≈ 212 $/an**) + anti-DDoS (Shield). |
| **Supervision / SLA** | CloudWatch + **Synthetics** (canari de disponibilité). |
| **DR** | Reprise inter-région vers **`ca-west-1`** (Calgary) — données maintenues **au Canada**. |
| **Secrets** | AWS **Secrets Manager** + KMS. |
| **Intégration M365 (art. 9)** | Fonctionne via **ponts d'identité** (fédération Entra ↔ IAM) — quelques pièces de plus qu'Azure. |
| **Coût infra annuel (validé)** | **~16 500 – 18 000 $ PAYG** ; **~11 000 – 13 000 $ réservé 3 ans**. |
| **Avantages** | **Résidence québécoise** (Montréal) ; **WAF OWASP managé ~25× moins cher** ; base HA moins chère ; Terraform `infra/terraform/aws/` **prêt et validé**. |
| **Vigilance** | Intégration M365 par fédération (à câbler) ; **passerelles NAT ×3** alourdissent un peu la facture ; **réécrire les sections Azure du dossier** (mémoire, architecture, démo). |

## Comparaison directe

| Critère | Proposition A — Azure | Proposition B — AWS Montréal |
|---|---|---|
| Résidence | Canada (Toronto) | **Québec (Montréal)** |
| SLA 99,99 % multi-zone | ✅ Canada Central | ✅ ca-central-1 |
| WAF OWASP | à l'ingress (ModSecurity) | **managé (~212 $/an)** |
| Intégration M365 | **native (Entra/Graph)** | par fédération |
| Supervision SLA managée | ✅ App Insights | ✅ Synthetics |
| DR au Canada | ✅ Canada Est | ✅ ca-west-1 |
| **Coût infra réservé 3 ans** | ~12 000 – 14 000 $ | **~11 000 – 13 000 $** |
| Effort de bascule du dossier | **nul (déjà écrit)** | réécriture des sections hébergement |
| Marge à 68 k$/an (1 client) | très confortable | très confortable (léger + ) |

> **Une fois le surcoût WAF retiré, l'écart de coût entre les deux est faible (~1 000–2 000 $/an).**
> Le choix se joue donc sur : **intégration M365 + dossier prêt (Azure)** contre **résidence québécoise +
> WAF managé + coût légèrement moindre (AWS)**.

## Recommandation

- **Soumettre la Proposition A (Azure, Canada Central)** : **risque d'exécution minimal** — intégration
  M365 native (essentielle pour la publication SharePoint/Teams/Outlook), expertise du fondateur,
  **dossier déjà rédigé**, et coût désormais aligné sur AWS après retrait du WAF Premium. Marge
  confortable à 68 k$.
- **Garder la Proposition B (AWS Montréal) comme variante** — pertinente si la Ville valorise une
  **résidence strictement québécoise** (Montréal) : elle est **moins chère** et le **Terraform est prêt**.
  Argument de repli solide en négociation.
- Dans **les deux cas** : licence **68 000 $/an** inchangée ; SLA, RPO/RTO et conformité identiques.

## Ce qui a déjà été corrigé au dossier

- **Front Door Standard + WAF OWASP à l'ingress** (retrait du surcoût Premium ~5 489 $/an) — mémoire,
  document HA, Terraform des deux stacks Azure (`terraform validate` ✅).
- **HA en Canada Central (3 zones)** ; **Canada Est réservé au DR** (aucune zone) — mémoire §5.3 et
  document architecture §2.

---

### Sources de prix

- [Azure — API prix de détail (Canada Central)](https://prices.azure.com/api/retail/prices) (Front Door Standard ~80 $/mois ; PostgreSQL Flexible GP 2 vCore 0,2744 $/h ; stockage 0,1913 $/Go).
- [Azure — régions et zones](https://learn.microsoft.com/en-us/azure/reliability/regions-list) (Canada East : aucune zone).
- [AWS — prix `ca-central-1`](https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/index.json) (RDS `db.t4g.large` Multi-AZ 0,282 $/h ; AWS WAF WebACL 5 $/mois ; Fargate ARM 0,03565 $/vCPU-h).
- Détail complet : [`AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md`](AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md).
