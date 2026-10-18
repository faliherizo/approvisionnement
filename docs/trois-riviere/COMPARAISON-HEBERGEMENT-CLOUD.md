---
title: Comparaison des coûts d'hébergement annuel — Azure vs AWS vs OVHcloud (AO 7000017905)
---

# Hébergement annuel de la solution : Azure vs AWS vs OVHcloud

> Comparaison **service par service**, en **haute disponibilité multi-zone (SLA 99,99 %)**, pour
> **un seul client**, **données hébergées au Canada**. Montants en **$ CAD, avant taxes**, tarifs
> **paiement à l'usage (PAYG)** sauf mention « engagé/réservé ». Ce sont des **ordres de grandeur**
> (les prix cloud varient et les prix OVH publics sont convertis USD→CAD ≈ 1,37) ; les **engagements
> 1–3 ans** réduisent les postes de calcul et de base de **~30–55 %**.

## 1. Les exigences du client (100 %) et ce qu'elles imposent à l'hébergement

Chaque exigence de l'appel d'offres ayant une incidence sur l'hébergement est traduite ici en
**composant d'infrastructure requis** (indépendant du fournisseur).

| # | Exigence (cahier des charges) | Composant d'hébergement requis |
|---|---|---|
| Art. 16 | **Disponibilité 99,99 %** | Calcul redondé **sur ≥ 3 zones**, base **primaire + veille synchrone**, entrée réseau redondée, bascule automatique |
| Art. 16 | **Sauvegarde + reprise (DR)**, RTO/RPO | Sauvegardes **géo-redondantes**, restauration testée — **RPO ≤ 15 min, RTO ≤ 4 h** |
| Art. 16 | **ISO 27001 / SOC 2 Type II** | Hébergeur **certifié** ISO 27001 + SOC 2 Type II |
| Art. 14-18 | **Chiffrement (transit + repos)**, journal d'audit | TLS géré, **chiffrement au repos (KMS)**, journaux centralisés exportables |
| Art. 16 | **Pare-feu applicatif + anti-DDoS** | **WAF (OWASP)** + protection **anti-DDoS** en entrée |
| Art. 18 | **Supervision + mesure du SLA** | Télémétrie, journaux, **tests de disponibilité synthétiques** (mesure indépendante) |
| Art. 14 | **Secrets / clés** | **Coffre de secrets** managé (aucun secret dans les images) |
| Loi 25 / Addenda 1 | **Résidence des données au Canada** | Région(s) **canadienne(s)** pour calcul, base, stockage **et** sauvegardes/DR |
| Art. 19/21 | **IA locale (aucune donnée sortante)** | Calcul dédié pour **Ollama** (CPU), dans le même périmètre |
| Art. 9 | **Intégration M365 / SAP** | Sortie réseau (egress) vers Graph/OData ; aucun composant hébergé propre |
| Art. 20/22 | **Réversibilité / export, migration** | **Stockage objet** (exports en formats ouverts) |
| Art. 8 | **Performance** (P95/P99, cf. doc HA) | Calcul à l'échelle automatique + base dimensionnée |

## 2. Correspondance des services par fournisseur

| Besoin | Microsoft Azure | AWS | OVHcloud |
|---|---|---|---|
| Calcul conteneurs (multi-zone) | Container Apps / AKS | ECS Fargate / EKS | **Managed Kubernetes** (plan de contrôle **gratuit**) |
| Base PostgreSQL HA | Flexible Server **HA zone-redondante** | RDS **Multi-AZ** | **Public Cloud Databases PostgreSQL** (Business/Enterprise, **jusqu'à 99,99 %**) |
| Entrée réseau + WAF + anti-DDoS | **Front Door Standard** + anti-DDoS ; **WAF OWASP à l'ingress** (ModSecurity/Coraza) | **ALB + AWS WAF** (par requête) | **Load Balancer + anti-DDoS inclus** ; WAF via Ingress ModSecurity ou edge tiers ⚠️ |
| Stockage objet | Blob (ZRS/GZRS) | S3 (+ CRR) | **Object Storage** (S3-compatible), **egress gratuit** |
| Secrets / clés | Key Vault | Secrets Manager + KMS | **KMS** OVH ou Vault auto-géré ⚠️ |
| Supervision + SLA synthétique | Azure Monitor + **App Insights** (sondes) | CloudWatch + **Synthetics** | Logs Data Platform + **sonde externe** (Grafana Cloud/UptimeRobot) ⚠️ |
| Sauvegarde + DR | Backups géo-redondants + Canada Est | Backups répliqués + ca-west-1 | Backups (Object Storage) ; **2ᵉ région canadienne limitée** ⚠️ |
| Résidence Canada | **Canada Central / Canada Est** | **ca-central-1 / ca-west-1** | **Beauharnois (BHS), Québec** |
| Certifications | ISO 27001, SOC 2 Type II | ISO 27001, SOC 2 Type II | **ISO 27001/27017/27018/27701, SOC 2 Type II** |

> ⚠️ = **écart à compenser** côté OVH (moins de services managés « clés en main » ; voir §6).

## 3. Estimation annuelle — Microsoft Azure (Canada Central)

| Service | Configuration (HA multi-zone) | $/mois | $/an |
|---|---|---:|---:|
| Calcul (Container Apps/AKS, 3 zones) | 3 × ~4 vCPU/16 Go + plan de contrôle | 700 | 8 400 |
| PostgreSQL Flexible HA | primaire + **veille** zone-redondante, 128 Go, backup géo | 500 | 6 000 |
| Front Door **Standard** + WAF OWASP **à l'ingress** | TLS géré, anti-DDoS, routage ; OWASP (CRS) sur l'ingress (pas de WAF Premium à 5 489 $/an) | 80 | 960 |
| Log Analytics + App Insights | journaux + **tests de disponibilité** | 130 | 1 560 |
| Blob (ZRS) + Key Vault | documents/exports + secrets | 30 | 360 |
| Service Bus + Event Grid + Functions (Consumption) | chaîne asynchrone | 50 | 600 |
| Réseau / egress / divers | sortie de données, IP, DNS, alertes | 75 | 900 |
| **Sous-total production (PAYG)** | | **~1 585–1 705** | **~19 000–20 200** |
| Serveur de développement (arrêt auto) | VM + BD dev | 200 | ~2 500 |
| **Total Azure PAYG** | | | **~21 500 – 22 700** |
| **Total Azure réservé 3 ans** (calcul+BD −40–55 %) | | | **~14 000 – 16 000** |

## 4. Estimation annuelle — AWS (ca-central-1)

| Service | Configuration (HA multi-AZ) | $/mois | $/an |
|---|---|---:|---:|
| EKS/ECS Fargate (3 AZ) + plan de contrôle | 3 × ~4 vCPU/16 Go | 560 | 6 720 |
| RDS PostgreSQL **Multi-AZ** | primaire + veille, 100 Go gp3, backup | 540 | 6 480 |
| **Passerelles NAT ×3** (une/AZ) | HA sortie Internet | 135 | 1 620 |
| ALB + AWS WAF | équilibreur + pare-feu OWASP | 45 | 540 |
| CloudFront + Route 53 | diffusion + DNS | 25 | 300 |
| S3 + Secrets Manager + KMS | stockage, secrets, clés | 25 | 300 |
| SQS/Lambda/EventBridge/Step Functions | chaîne asynchrone | 30 | 360 |
| CloudWatch + **Synthetics** | supervision + mesure disponibilité | 60 | 720 |
| Egress + divers | sortie de données | 55 | 660 |
| **Sous-total production (PAYG)** | | **~1 475** | **~17 700** |
| Serveur de développement | | 200 | ~2 500 |
| **Total AWS PAYG** | | | **~20 200** |
| **Total AWS réservé 3 ans** (Savings Plans −40–55 %) | | | **~13 000 – 15 000** |

## 5. Estimation annuelle — OVHcloud (Beauharnois, QC)

| Service | Configuration (HA) | $/mois (CAD) | $/an |
|---|---|---:|---:|
| Managed Kubernetes — plan de contrôle | **gratuit** | 0 | 0 |
| Nœuds applicatifs (3 × B3-16, 4 vCPU/16 Go) | répartis, mise à l'échelle | ~350 | ~4 200 |
| **PostgreSQL managé** (Business, HA 2 nœuds, ~4 vCPU/15 Go, 100 Go) | primaire + réplica, **jusqu'à 99,99 %** | ~450 | ~5 400 |
| Load Balancer + **anti-DDoS inclus** | entrée réseau (500 Mbps) | ~25 | ~300 |
| Object Storage (documents/exports) | S3-compatible, **egress gratuit** | ~20 | ~240 |
| Block Storage (100 Go haute vitesse) | volumes | ~15 | ~180 |
| KMS + supervision externe (Grafana Cloud/UptimeRobot) | secrets + sonde SLA | ~50 | ~600 |
| WAF (Ingress ModSecurity managé K8s, ou edge) | pare-feu OWASP | 0–200 | 0–2 400 |
| Egress / sortie de données | **gratuit (la plupart des régions, dont Canada)** | 0 | 0 |
| **Sous-total production (PAYG)** | | **~910–1 110** | **~10 900 – 13 300** |
| Serveur de développement (instance + BD dev) | | ~55 | ~700 |
| **Total OVH PAYG** | | | **~11 600 – 14 000** |
| **Total OVH engagé (mensuel/annuel, instances −~30 %)** | | | **~9 000 – 11 000** |

## 6. Écarts et compensations — ce qu'il faut savoir avant de choisir OVH

OVH satisfait la **résidence Canada (BHS)**, les **certifications (ISO 27001/27017/27018/27701, SOC 2
Type II)** et une **base PostgreSQL managée jusqu'à 99,99 %**. Mais, par rapport à Azure/AWS, plusieurs
briques « clés en main » manquent et doivent être **compensées** (surcoût d'ingénierie/ops, non de licence) :

1. **WAF managé OWASP** : pas d'équivalent Front Door/AWS WAF. Compenser par ModSecurity sur l'Ingress
   Kubernetes (auto-géré) ou un edge tiers (Cloudflare). L'**anti-DDoS est inclus** chez OVH.
2. **Tests de disponibilité synthétiques (mesure indépendante du SLA)** : pas d'App Insights/Synthetics.
   Compenser par une sonde externe (Grafana Cloud, UptimeRobot, self-hosted).
3. **Coffre de secrets** : KMS OVH disponible ; sinon Vault auto-géré (charge ops).
4. **DR inter-région DANS le Canada** : l'empreinte canadienne d'OVH est **essentiellement Beauharnois**.
   Une bascule inter-région en **restant au Canada** (Loi 25) est **plus contrainte** qu'avec Azure
   (Canada Central ↔ Canada Est) ou AWS (ca-central-1 ↔ ca-west-1). **À valider** : disponibilité
   multi-AZ/2ᵉ zone canadienne pour tenir 99,99 % **et** le DR sans sortir du Canada.
5. **Maturité multi-zone** : le SLA 99,99 % managé (3-AZ) d'OVH est offert **par région** ; confirmer
   qu'il l'est **à BHS** et pas seulement en Europe.
6. **Écosystème managé plus mince** (files, événements, fonctions serverless) : à porter sur Kubernetes.

## 7. Synthèse comparative

| Critère | Azure | AWS | OVHcloud |
|---|---|---|---|
| **Coût annuel PAYG** | ~21 500 – 22 700 $ | ~20 200 $ | **~11 600 – 14 000 $** |
| **Coût annuel engagé 3 ans** | ~14 000 – 16 000 $ | ~13 000 – 15 000 $ | **~9 000 – 11 000 $** |
| Résidence Canada | ✅ Central + Est | ✅ central-1 + west-1 | ✅ Beauharnois (⚠️ 1 site) |
| ISO 27001 / SOC 2 Type II | ✅ | ✅ | ✅ (+ 27017/27018/27701) |
| 99,99 % multi-zone **clés en main** | ✅ | ✅ | ⚠️ à valider à BHS |
| WAF OWASP managé | ✅ Front Door | ✅ AWS WAF | ⚠️ à assembler |
| Mesure SLA synthétique managée | ✅ App Insights | ✅ Synthetics | ⚠️ externe |
| DR inter-région **au Canada** | ✅ | ✅ | ⚠️ contraint |
| Egress | payant (~600–900 $/an) | payant (~660 $/an) | **gratuit** |
| Effort d'exploitation | faible | faible | **plus élevé** |

## 8. Recommandation

- **Pour la soumission Trois-Rivières (engagement 99,99 % + DR au Canada + conformité clés en main) :
  Microsoft Azure** (Canada Central ↔ Canada Est) — c'est l'architecture déjà proposée, elle couvre
  **100 % des exigences sans assemblage**. **AWS** est équivalent (ca-central-1 ↔ ca-west-1) à coût
  très proche. Le forfait annuel de **68 000 $** couvre largement l'un ou l'autre (cf.
  [`ESTIMATION-COUTS-CLOUD-DETAILLEE.md`](ESTIMATION-COUTS-CLOUD-DETAILLEE.md) et
  [`ANALYSE-COUTS-ET-ABONNEMENT.md`](ANALYSE-COUTS-ET-ABONNEMENT.md)).
- **OVHcloud est le moins cher (~40–50 % d'économie)** et **conforme (résidence Canada + certifications)** :
  pertinent pour la production actuelle et pour maximiser la marge. À retenir **à condition de lever les
  écarts du §6** — surtout **multi-AZ + DR intra-Canada** pour garantir contractuellement le 99,99 % —,
  ce qui ajoute de l'**effort d'exploitation** plutôt que du coût de licence.
- **Décision** : soumettre l'AO sur **Azure** (risque le plus faible, exigences 100 % couvertes) ;
  **évaluer OVH** comme socle de production à moindre coût une fois les §6.4–6.5 validés.

---

### Sources

- [OVHcloud — Liste de prix Public Cloud (Canada)](https://www.ovhcloud.com/en-ca/public-cloud/prices/)
- [OVHcloud — Public Cloud Databases (pricing)](https://docs.ovhcloud.com/en/guides/public-cloud/databases/pricing)
- [OVHcloud — PostgreSQL managé (jusqu'à 99,99 %, 3-AZ)](https://www.ovhcloud.com/en/public-cloud/postgresql/)
- [OVHcloud — Conformité ISO 27001/27017/27018/27701 & SOC 2](https://www.ovhcloud.com/en/compliance/)
- [OVHcloud — Sécurité des Public Cloud Databases](https://docs.ovhcloud.com/en/guides/public-cloud/databases/concepts-security-overview)
- [Repère de prix OVH (instances, DB, stockage)](https://getdeploying.com/ovh)
- Prix Azure/AWS : reportés de [`ESTIMATION-COUTS-CLOUD-DETAILLEE.md`](ESTIMATION-COUTS-CLOUD-DETAILLEE.md) (régions Canada, PAYG et réservé 3 ans).
