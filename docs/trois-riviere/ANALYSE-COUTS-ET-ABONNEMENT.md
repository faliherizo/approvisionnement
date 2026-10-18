---
title: Analyse des coûts et estimation de l'abonnement — AO 7000017905
---

# Ré-évaluation des coûts et estimation de l'abonnement (SaaS multilocataire)

> Document d'aide à la décision (interne). Objet : (1) valider le **budget annuel réaliste** fourni,
> (2) en déduire le **coût par client**, (3) proposer un **abonnement correct** pour la réponse à
> l'appel d'offres de la Ville de Trois-Rivières, cohérent avec le SLA 99,99 % et le marché.

---

## 1. Le budget annuel fourni est-il réaliste ?

Budget proposé pour exploiter le service (SLA 99,99 %, support humain multi-niveaux) :

| Poste | Budget annuel | Réaliste ? | Commentaire |
|---|---:|:--:|---|
| Licence SaaS / plateforme | 50 000 $ | ✅ | Investissement produit continu + amortissement R&D et licences tierces. |
| Hébergement cloud | 25 000 $ | ✅ | Environnement HA multi-zone (calcul + BD HA + Front Door/WAF + réseau). |
| Sauvegarde + PRA | 7 500 $ | ✅ | Sauvegardes géo-redondantes + plan de reprise testé (RPO/RTO courts). |
| Monitoring / sécurité | 10 000 $ | ✅ | Azure Monitor/App Insights + outils sécurité, revues, tests d'intrusion. |
| Support N1/N2/N3 | 30 000 $ | ✅ | Équipe de soutien à trois niveaux (fractions d'ETP). |
| Maintenance corrective | 15 000 $ | ✅ | Correctifs, mises à jour de dépendances et de sécurité. |
| Petites évolutions | 15 000 $ | ✅ | Améliorations mineures incluses au forfait. |
| SLA / disponibilité / astreinte | 10 000 $ | ✅ | Rotation d'astreinte 24/7 pour tenir le 99,99 %. |
| **Total** | **162 500 $** | ✅ | **Coût pleinement chargé, réaliste — voire prudent.** |

**Conclusion** : le budget de **162 500 $/an est réaliste** comme **coût pleinement chargé** d'un
service exploité au niveau attendu (haute disponibilité + support humain). Il est cohérent avec le
marché pour une exploitation **dédiée**. La nuance essentielle suit.

---

## 2. La bonne lecture : coûts mutualisés vs coûts par client

La solution est un **SaaS multilocataire** : la même plateforme sert **plusieurs organismes** (Ville
de Lévis, Ville de Trois-Rivières, autres municipalités). La majorité des postes sont **mutualisés**
(partagés entre tous les clients) ; seuls quelques-uns sont **dédiés** (propres à un client).

| Poste | Montant | Nature | Part mutualisée | Part dédiée / client |
|---|---:|---|---:|---:|
| Licence / plateforme (R&D) | 50 000 $ | Mutualisé | 50 000 $ | — |
| Maintenance corrective | 15 000 $ | Mutualisé | 15 000 $ | — |
| Petites évolutions | 15 000 $ | Mutualisé | 15 000 $ | — |
| SLA / astreinte 24/7 | 10 000 $ | Mutualisé | 10 000 $ | — |
| Support N1/N2/N3 | 30 000 $ | Mixte | 20 000 $ (socle) | 10 000 $ |
| Monitoring / sécurité | 10 000 $ | Mixte | 6 000 $ | 4 000 $ |
| Hébergement cloud | 25 000 $ | Dédié | — | 25 000 $ |
| Sauvegarde + PRA | 7 500 $ | Dédié | — | 7 500 $ |
| **Totaux** | **162 500 $** | | **116 000 $** (fixe) | **46 500 $ / client** |

**Coût par client** = `116 000 $ ÷ N` (part fixe amortie sur *N* clients) `+ 46 500 $` (part dédiée).

| Nombre de clients *N* | Coût par client / an |
|:--:|---:|
| 1 | **162 500 $** |
| 2 | 104 500 $ |
| 3 | 85 200 $ |
| 5 | 69 700 $ |
| 8 | 61 000 $ |
| 10 | **58 100 $** |
| 15 | 54 200 $ |

> Le modèle **se réconcilie exactement** avec le budget fourni : à *N = 1*, le coût par client vaut
> bien **162 500 $**. Plus la base de clients grandit, plus le coût par client baisse (économie
> d'échelle du multilocataire).

---

## 3. Estimation correcte de l'abonnement

Un abonnement doit **couvrir le coût par client + une marge** (≈ 15–20 %) tout en restant
**concurrentiel** (repère marché « compétitif » : 55 000 – 65 000 $/an).

| Scénario | Base de clients visée | Coût/client | Abonnement pour marge ~15 % | Positionnement marché |
|---|:--:|---:|---:|---|
| **Prudent** (démarrage) | 3 | 85 000 $ | ~98 000 $ | Au-dessus du marché (non compétitif) |
| **Équilibré (recommandé)** | 5 | 69 700 $ | **~68 000 – 72 000 $** | Haut du compétitif / bas premium |
| **Concurrentiel** | 8 | 61 000 $ | ~65 000 $ | Haut du marché compétitif |
| **Agressif (pari d'échelle)** | 10 | 58 100 $ | ~58 000 – 60 000 $ | Cœur du marché compétitif |

### 3.1 Réalité actuelle : **un seul client, sans garantie d'autres**

L'amortissement sur plusieurs clients (tableau ci-dessus) est **hypothétique** : à ce jour, il n'y a
**qu'un client** (Trois-Rivières) et aucune certitude d'en ajouter. Le prix **ne doit donc pas** reposer
sur un portefeuille futur. Il faut raisonner à **N = 1**, mais avec la **bonne base de coût**.

Le coût « pleinement chargé » de 162 500 $ **surestime la sortie de trésorerie réelle** pour ce seul
client, car il valorise :
- le **R&D / plateforme (50 000 $)** — un **investissement déjà consenti** par VINA (plateforme
  construite), pas une dépense annuelle en argent ;
- la **maintenance, les évolutions, le support, l'astreinte** — c'est du **temps du fondateur**
  (entreprise sans salariés dédiés), non des débours.

**Coût de trésorerie incrémental** réellement engagé pour servir ce client (avant taxes), issu du
**calcul détaillé service par service** (§6, un seul cloud, HA multi-zone, PAYG) :

| Poste (débours réel) | Montant / an |
|---|---:|
| Infrastructure Azure complète (calcul + BD **HA primaire+veille** + Front Door/WAF + supervision + Defender/sécurité + Private Link + backup) | ~21 200 $ |
| Environnement de TEST / dev (ligne TEST §6.1) | ~5 400 $ |
| **Coût de trésorerie / an (milieu de fourchette)** | **~26 600 $** |

> Fourchette complète (ta ventilation §6.1) : **~17 300 – 35 900 $/an**. En **réservé 3 ans** (contrat
> ferme), le calcul et la base baissent de ~40–55 % → trésorerie **~18 000 – 20 000 $/an**. Détail
> poste par poste (Azure **et** AWS) à la **§6**.

Le reste (support N1→N5, maintenance, évolutions, astreinte) est le **temps du fondateur**, à rémunérer
par la marge, sans être un débours (voir §6.4).

### 3.2 Recommandation (contexte mono-client)

- **Conserver un abonnement compétitif de ~58 000 $/an** (le montant déjà soumis). À N = 1 :
  58 000 $ − ~26 600 $ (trésorerie) = **~31 400 $/an** pour rémunérer le temps de support/maintenance
  du fondateur et la marge — **positif en trésorerie dès le premier client**, sans dépendre d'autres.
- **Priorité : remporter ce client.** Comme c'est le **seul**, un prix **au cœur de la fourchette
  compétitive (55–65 k$)** maximise le **pointage Prix (30 pts)** et la probabilité de gagner — sans
  quoi il n'y a aucun revenu.
- **Cushion optionnel : 60 000 – 62 000 $/an** si l'on veut une marge un peu plus confortable tout en
  restant compétitif ; au-delà, on fragilise le pointage Prix sans nécessité.
- **Année 1 (158 000 $ avec l'implantation)** apporte de plus une **entrée de trésorerie importante**
  qui finance largement la mise en place et les débours de la première année.
- **L'upside est automatique** : si d'autres municipalités s'ajoutent plus tard, la part mutualisée se
  dilue et la marge s'améliore **sans changer le prix** — mais on ne mise pas dessus aujourd'hui.

> En clair : le **58 000 $/an déjà soumis est le bon choix** pour un contexte mono-client — compétitif
> pour gagner, et **positif en trésorerie** dès ce seul client. Il n'y a **pas lieu de relever à 65 k$**
> (ce montant ne se justifiait que par une amortization sur ~5 clients, aujourd'hui non acquise).

---

## 4. Incidence sur la réponse à l'appel d'offres

**Décision retenue : licence de base fixée à 68 000 $/an.** Ce montant reste **dans le bas de la
fourchette du marché du secteur public** (voir §8, comparaison marché réel : ~55–130 k$CA/an), pour une
offre **tout inclus** (hébergement HA SLA 99,99 %, support, maintenance, IA locale, connecteurs M365/SAP,
DR RPO/RTO). Il dégage une **marge confortable** tout en demeurant crédible face au marché — et le
**dossier déposé (mémoire, formulaires, bordereau) est mis à jour en conséquence**.

| Poste (offre déposée) | Montant |
|---|---:|
| Licence / an (tout inclus, SLA 99,99 %) | **68 000 $** |
| Implantation (incluse à l'An 1) | 100 000 $ |
| An 1 (2027) | 168 000 $ |
| An 2 / An 3 | 68 000 $ chacune |
| **Ferme 3 ans (avant taxes)** | **304 000 $** |
| Ferme 3 ans (TTC) | 349 524,00 $ |
| 10 ans (ferme + 7 options +5 %/an, av. taxes) | 885 339 $ |

- **Toujours compétitif** : total ferme 3 ans (304 k$) reste dans la fourchette du marché du secteur
  public ; le **pointage Prix (30 pts)** demeure solide, d'autant qu'**aucun comparable direct** n'existe
  sur le SEAO (faible risque de sous-cotation — §8).
- **Marge nettement renforcée à N = 1** : 68 000 $ − ~26 600 $ de débours = **~41 400 $/an** pour le temps
  du fondateur + marge ; l'An 1 (168 000 $) finance largement la mise en place.
- **Couverture pleinement chargée** : à 68 000 $, l'offre couvre même le scénario « base chargée »
  (support valorisé au marché + temps du fondateur, ~56,5–60,6 k$) avec ~+12 à +20 % de marge (voir §6.5).

---

## 5. Conclusion (contexte : un seul client, avenir incertain)

**Licence de base fixée à 68 000 $/an — le dossier déposé est mis à jour en conséquence.**

- Reste **compétitif** (bas de la fourchette du secteur public, §8) tout en étant **largement positif en
  trésorerie dès ce seul client** (~41 400 $/an au-dessus des débours, + l'apport de l'An 1 à 168 000 $).
- **Justifié par le marché** : ~68 k$ correspond au bas-milieu des plateformes d'approvisionnement/contrats
  du secteur public (OpenGov ~55–130 k$CA), et **aucun comparable direct sur le SEAO** ne vient sous-coter
  l'offre — le risque « pointage Prix » d'un tarif à 68 k$ est donc limité.
- On **ne mise pas** sur d'autres clients pour justifier le prix ; s'ils arrivent, c'est un **bonus**
  (marge améliorée sans changer le tarif).

> Le bordereau, le mémoire et le PDF de soumission sont **mis à jour à 68 000 $/an** (ferme 3 ans =
> **304 000 $** ; 10 ans = **885 339 $** avant taxes).

---

## 6. Détail des calculs d'infrastructure (par service, HA multi-zone)

> Estimations en **$ CAD, avant taxes**, PAYG, régions **Canada**. La version autonome et exhaustive
> est dans [`ESTIMATION-COUTS-CLOUD-DETAILLEE.md`](ESTIMATION-COUTS-CLOUD-DETAILLEE.md).
> Rappel : **99,99 % = multi-zones dans une région** (base primaire+veille + ≥ 3 répliques), **pas**
> deux environnements complets dupliqués.

### 6.1 Production Azure (Container Apps) — ventilation détaillée

Estimation retenue (variante **Container Apps**, incluant Defender/Sécurité, Private Link et un
**environnement de TEST/dev**), en **fourchette** — c'est la lecture la plus prudente :

| Service | $/mois | $/an (milieu) |
|---|---:|---:|
| Container Apps (backend + frontend, multi-zone) | 250 – 500 | ~4 500 |
| PostgreSQL **HA** (primaire + veille) | 450 – 750 | ~7 200 |
| Blob Storage | 30 – 100 | ~780 |
| **Container Registry (ACR Premium)** ¹ | 50 – 70 | ~720 |
| Front Door + WAF ² | 100 – 200 (Standard) / ~450 (Premium) | ~1 800 – 5 400 |
| Service Bus | 20 – 60 | ~480 |
| Functions (Consumption) / Event Grid ³ | 20 – 70 | ~540 |
| Azure Monitor + Application Insights | 80 – 180 | ~1 560 |
| Key Vault | 10 – 30 | ~240 |
| Defender / Sécurité | 30 – 100 | ~780 |
| Backup | 50 – 150 | ~1 200 |
| Réseau / Private Link | 100 – 250 | ~2 100 |
| **Environnement de TEST / dev** | 300 – 600 | ~5 400 |
| **Total Azure** | **1 440 – 2 990 $/mois** | **~17 300 – 35 900 $/an (milieu ~26 600 $)** |

> Cette ventilation **inclut déjà l'environnement de test/dev** (ligne TEST) et la sécurité (Defender,
> Private Link) — d'où un total plus élevé que la variante Kubernetes ci-dessous, mais **plus complet**.

¹ **ACR ajouté** : il manquait à la ventilation initiale. **Maintenu en Premium** (~50–70 $/mois) pour
la **redondance par zones** (cohérence avec l'archi multi-zones ; le Standard n'a pas de redondance de
zones). Précision : l'ACR n'est **pas dans le chemin de service en direct** (l'appli tourne sans lui),
il n'intervient qu'aux déploiements/montées en charge — il ne **détermine donc pas** le 99,99 %, mais on
le garde résilient. La **géo-réplication multi-région** (aussi Premium) n'est requise que pour une
**reprise inter-région** (99,999 %), non pour ce 99,99 % mono-région.
² **Front Door** : le Terraform est en **Premium** (jeu de règles WAF **OWASP managé**, revendiqué dans
le mémoire) ≈ 450 $/mois. En **Standard** (règles WAF personnalisées seulement) ≈ 100–200 $/mois. À
choisir selon l'exigence de sécurité — voir §7.
³ **Functions** : le Terraform a été ramené de **EP1 (Elastic Premium ≈ 220 $/mois)** à **Consumption
(Y1)**, conforme à ta ventilation (20–70 $/mois).

### 6.2 Production AWS (EKS/ECS + RDS Multi-AZ)

| Service | Configuration | $/an |
|---|---|---:|
| EKS — plan de contrôle | 0,10 $/h | 876 |
| Nœuds / Fargate | 3 × m6i.xlarge, 3 AZ | 5 760 |
| RDS PostgreSQL **Multi-AZ** | db.m6g.large primaire + veille, 100 Go, backup | 6 480 |
| Passerelles NAT (×3) | une par AZ (HA) | 1 620 |
| ALB + CloudFront + Route 53 | entrée + diffusion + DNS | 600 |
| S3 + ECR + Secrets Manager | stockage, images, secrets | 240 |
| SQS + Lambda + EventBridge + Step Functions + Glue | chaîne asynchrone | 360 |
| CloudWatch + Synthetics | supervision + canari (disponibilité) | 720 |
| API Gateway | edge managé | 180 |
| Egress | sortie de données | 600 |
| **Total production AWS (PAYG)** | | **~17 436** |

**Production retenue ≈ 20 000 $/an (un seul cloud, PAYG) ; ~13 000 $/an en réservé 3 ans.**

### 6.3 Serveur de développement / TEST (Azure)

L'environnement de test/dev est **déjà compris** dans la ventilation §6.1 (**ligne TEST : 300–600 $/mois
≈ 5 400 $/an**) — il n'est donc **pas** compté une seconde fois ici. Il couvre : une instance de calcul
de dev, une base de dev mono-zone et le stockage associé.

### 6.4 Support N1 → N5 (coût humain)

| Scénario | Coût trésorerie / an |
|---|---:|
| **A — Fondateur assure N1→N5 (réalité, 1 client)** | **0 $** (temps propre, rémunéré par la marge) |
| B — Valorisé au marché (~0,2 ETP) | ~19 000 $ |
| C — Équipe dédiée 24/7 multi-niveaux | 40 000 $+ (injustifié pour 1 client) |

### 6.5 Récapitulatif + marge 20 %

Sur la base de **ta ventilation Azure §6.1** (qui inclut déjà le TEST/dev, Defender et Private Link) :

| | Trésorerie (fondateur = support) | Pleinement chargé (support payé) |
|---|---:|---:|
| Production cloud **+ TEST/dev** (milieu §6.1) | 26 600 | 26 600 |
| Support N1→N5 | 0 | 19 000 |
| Maintenance + évolutions (temps valorisé) | 0 | 15 000 |
| **Coût total** | **~26 600** | **~60 600** |
| Prix pour **+20 % de marge** | ~31 900 | ~72 700 |
| **Abonnement soumis** | **58 000** | **58 000** |
| **Marge réelle à 58 000 $** | **~54 %** ✅ | **~ −4 %** ⚠️ |

Test de robustesse sur **toute ta fourchette** (support par le fondateur) :

| Hypothèse Azure §6.1 | Coût trésorerie/an | Reste à 58 000 $ | Prix pour +20 % | Verdict |
|---|---:|---:|---:|---|
| Bas (1 440 $/mois) | ~17 300 | ~40 700 | ~20 800 | ✅ très large |
| Milieu (2 215 $/mois) | ~26 600 | ~31 400 | ~31 900 | ✅ confortable |
| **Haut (2 990 $/mois)** | **~35 900** | **~22 100** | **~43 100** | ✅ **couvert même au pire** |

**Verdict : même au HAUT de ta propre fourchette (35 900 $/an), 58 000 $ couvre toute l'infrastructure
(TEST inclus) ET dégage plus de 20 % de marge (prix requis ~43 100 $ < 58 000 $). Non perdant.**
Le seul cas « juste » (marge < 20 %) reste une **équipe de support dédiée payée** (base chargée
~60 600 $ → il faudrait ~72 700 $), non justifiée pour un client unique — le fondateur assure N1→N5.

---

## 7. Validation : chaque service Terraform est-il dans l'estimation ?

Confrontation des **ressources réellement provisionnées** par le Terraform (`infra/`) aux lignes de coût.

### 7.1 Azure (stacks AKS `infra/terraform/azure` et Container Apps `infra/serverless/terraform/azure-aca`)

| Ressource Terraform | Facturable ? | Ligne d'estimation | Couvert ? |
|---|:--:|---|:--:|
| `azurerm_container_app` (×2) + `container_app_environment` | Oui | Container Apps | ✅ |
| `azurerm_kubernetes_cluster` (+ node pool) *(variante AKS)* | Oui | Container Apps (équivalent calcul) ⚠️ + plan de contrôle AKS ~73 $/mois si AKS | ✅/⚠️ |
| `azurerm_postgresql_flexible_server` (HA) + `_database` | Oui / non | PostgreSQL HA | ✅ |
| `azurerm_container_registry` | Oui | **ACR (ajouté §6.1)** | ✅ *(corrigé)* |
| `azurerm_storage_account` + `storage_container` (×2) | Oui | Blob Storage | ✅ |
| `azurerm_cdn_frontdoor_*` (profil, endpoint, origin, WAF) | Oui | Front Door + WAF | ✅ *(palier à trancher)* |
| `azurerm_servicebus_namespace` + `queue` | Oui | Service Bus | ✅ |
| `azurerm_eventgrid_topic` | Oui | Functions/Event Grid | ✅ |
| `azurerm_linux_function_app` + `service_plan` | Oui | Functions (Consumption) | ✅ *(EP1→Y1 corrigé)* |
| `azurerm_log_analytics_workspace` + `application_insights` + `standard_web_test` | Oui | Azure Monitor | ✅ |
| `azurerm_monitor_action_group` + `metric_alert` | Négligeable | Azure Monitor | ✅ |
| `azurerm_key_vault` + `key_vault_secret` (×2) | Oui | Key Vault | ✅ |
| `azurerm_private_dns_zone` (+ lien) | ~0,50 $/mois | Réseau/Private Link | ✅ |
| `azurerm_virtual_network` + `subnet` (×2) | Gratuit (transfert facturé) | Réseau/Private Link | ✅ |
| `azurerm_user_assigned_identity`, `role_assignment` (×3-4), `resource_group` | Gratuit | — | ✅ |
| **Microsoft Defender** *(non provisionné en TF)* | Oui | Defender/Sécurité | ➕ prévu dans l'estimation (à activer) |
| **Backup géo-redondant** (option de la BD) | Oui | Backup | ✅ |

**Rien d'oublié après correction.** Trois écarts détectés et traités :
1. **ACR manquait** → **ajouté** (§6.1, ~50–70 $/mois) et **maintenu en Premium** (redondance par zones,
   cohérence HA ; il n'est toutefois pas dans le chemin de service en direct — voir note ¹).
2. **Functions EP1** (~220 $/mois) → **ramené à Consumption** dans le TF (conforme à l'estimation).
3. **Front Door Premium** (~450 $/mois) vs **Standard** (~150 $/mois) : **décision à prendre** — Premium
   requis pour le **WAF OWASP managé** (revendiqué au mémoire). Deux totaux ci-dessous.

### 7.2 AWS (stacks EKS `infra/terraform/aws` et ECS `infra/serverless/terraform/aws-ecs`)

| Ressource Terraform | Ligne d'estimation | Couvert ? |
|---|---|:--:|
| `module.eks` / `aws_ecs_cluster` + tâches | EKS control plane + nœuds/Fargate | ✅ |
| `module.vpc` (NAT ×3, EIP) | Passerelles NAT ×3 | ✅ |
| `aws_db_instance` (Multi-AZ) | RDS Multi-AZ | ✅ |
| `aws_lb` + target groups + listener *(ECS)* | ALB | ✅ |
| `aws_ecr_repository` | S3/ECR/Secrets | ✅ |
| `aws_s3_bucket` (×3) | S3 | ✅ |
| `aws_secretsmanager_secret` (×2) | Secrets Manager | ✅ |
| `aws_sqs_queue` (×2) | chaîne asynchrone | ✅ |
| `aws_lambda_function` | Lambda | ✅ |
| `aws_cloudwatch_event_bus` + rules | EventBridge | ✅ |
| `aws_sfn_state_machine` | Step Functions | ✅ |
| `aws_glue_job` + catalog | Glue | ✅ |
| `aws_apigatewayv2_*` (+ VPC Link : gratuit) | API Gateway | ✅ |
| `aws_cloudwatch_log_group` (×5) + `metric_alarm` (×3) + `sns_topic` | CloudWatch | ✅ |
| `aws_synthetics_canary` (+ bucket) | Synthetics | ✅ |
| Chiffrement KMS (S3/ECR) | *(à isoler)* | ⚠️ ~1-3 $/mois — négligeable |
| **Transfert inter-AZ** (réplication RDS Multi-AZ, trafic inter-zones) | *(non isolé)* | ⚠️ ~20-60 $/mois — à prévoir |
| IAM, security groups, subnets, route tables | Gratuit | ✅ |

**AWS : estimation quasi complète.** À ajouter par prudence : **transfert inter-AZ ~30 $/mois** et
**KMS ~2 $/mois** (repli dans « Egress + divers »). Ordre de grandeur inchangé.

### 7.3 Total Azure corrigé et robustesse à 58 000 $

| Configuration Azure | Trésorerie / an | Reste à 58 000 $ | Prix pour +20 % | Verdict |
|---|---:|---:|---:|---|
| Front Door **Standard** + **ACR Premium** | ~27 400 | ~30 600 | ~32 900 | ✅ confortable |
| Front Door **Premium** (WAF OWASP managé) + **ACR Premium** | ~31 000 | ~27 000 | ~37 200 | ✅ couvert |

**Conclusion de la validation : aucun service facturable n'est oublié** (ACR ajouté et **maintenu
Premium** pour la redondance de zones, Functions ramené à Consumption, inter-AZ/KMS notés). **Même au
pire cas** (Front Door Premium + ACR Premium, ~31 000 $/an de trésorerie), 58 000 $ **couvre tout** avec
plus de 20 % de marge (prix requis ~37 200 $ < 58 000 $). **Non perdant, estimation validée.**

> Rappel de ce qui **détermine réellement** le 99,99 % (mono-région, multi-zones) : **calcul
> multi-zones** (Container Apps/AKS), **PostgreSQL HA redondante par zones** et **Front Door** (SLA
> 99,99 % en Standard **comme** en Premium — le choix Premium ne porte que sur le **WAF OWASP managé**,
> pas sur la disponibilité). L'ACR et la géo-réplication n'en font pas partie.

---

## 8. Comparaison au marché réel (recherche, sept. 2026)

Aucun logiciel **directement** comparable (rédaction + génération contractuelle municipale, en
français, hébergé au Québec, SLA 99,99 %) n'est offert sur le SEAO actuellement. Le repère de prix
vient donc du **marché plus large** des plateformes d'approvisionnement/gestion de contrats du secteur
public et des CLM (*Contract Lifecycle Management*). Chiffres publiés (⚠️ plusieurs en **$ US**) :

| Solution / catégorie | Type | Prix annuel publié | Source |
|---|---|---|---|
| **OpenGov** Procurement & Contract Mgmt | Secteur public (municipal) | **~40 000–95 000 $US/an** (plage large 20 000–175 000 $US) ≈ **55 000–130 000 $CA** | Civic IQ, Capterra |
| Ex. petite ville (Sweet Home, OR) | Municipal (petit) | ~8 986 $US/an + 15 725 $US implantation | dossier municipal |
| **Euna / Bonfire** (ex-Bonfire) | Secteur public | Joliet **26 500 $US/an** ; eCivis (subventions) 102 449 $US | G2, SelectHub |
| CLM **PME** | Privé | **5 000–25 000 $US/an** | Sirion, Hyperstart |
| CLM **milieu de gamme** | Privé | **15 000–50 000 $US/an** (jusqu'à 80–120 k$ pour gros) | Hyperstart, Clause & Current |
| CLM par utilisateur | Privé | **7–700 $US/utilisateur/mois** (milieu 50–300) | Ironclad, Concord |
| **Edilex / Xpertdoc / Experlogix** (Québec) | Doc. juridique/contrats | **Sur devis** (Experlogix : dès ~12 $US/utilisateur/mois) | GetApp/Capterra Canada |

**Points clés de la comparaison :**
- Le comparable **le plus proche** (plateforme d'approvisionnement/contrats **du secteur public**,
  type **OpenGov**) se situe à **~55 000–130 000 $CA/an** (milieu ~90 000 $CA). **68 000 $CA/an est
  donc dans la moitié BASSE de cette fourchette**, pas au-dessus.
- Notre prix est **tout inclus** (hébergement HA **SLA 99,99 %**, support N1→N5, maintenance, mises à
  jour, **IA locale**, **connecteurs M365/SAP**, implantation en An 1). Les CLM « milieu de gamme »
  (15–50 k$US) **facturent en sus** l'implantation (1–25 k$US) et **chaque intégration (15–45 k$US)**,
  ce qui rapproche leur coût réel du nôtre.
- Les éditeurs québécois (Edilex, Xpertdoc) sont **sur devis** — aucun prix public inférieur opposable.
- **Absence de comparable direct sur le SEAO** = faible risque d'être **sous-coté** par un concurrent
  équivalent, donc le risque « pointage Prix » d'un tarif à 68 k$ est **limité**.

### Verdict marché
**68 000 $CA/an n'est PAS trop élevé.** C'est **dans le bas de la fourchette du secteur public**
(OpenGov ~55–130 k$CA), pour une offre **tout inclus** et **différenciée** (français, résidence des
données au Québec, SLA 99,99 %, IA locale). Positionnement des trois options :

| Abonnement | Positionnement marché réel | Lecture |
|---|---|---|
| 58 000 $CA/an | Bas de la fourchette secteur public | Agressif — maximise le pointage Prix |
| 65 000 $CA/an | Bas-milieu secteur public | Équilibré |
| **68 000 $CA/an** | **Bas-milieu secteur public, sous le milieu (~90 k$CA)** | **Défendable — marge saine, reste crédible** |

> Conclusion : **68 k$ est justifiable au regard du marché** (surtout sans comparable direct sur le
> SEAO). Le choix entre 58 k$ (priorité *gagner sur le prix*) et 68 k$ (priorité *marge*, en s'appuyant
> sur la qualité et la différenciation) reste **stratégique**, mais **aucun des deux n'est hors marché**.
