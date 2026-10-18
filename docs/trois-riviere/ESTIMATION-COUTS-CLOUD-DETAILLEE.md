---
title: Estimation détaillée des coûts cloud (Azure & AWS) — abonnement 58 000 $/an
---

# Le 58 000 $/an couvre-t-il l'infrastructure (SLA 99,99 %) + le support + 20 % de marge ?

> Calcul **service par service**, en **HA multi-zone** (SLA 99,99 %), pour **un seul client**.
> Montants en **$ CAD, avant taxes**, tarifs **paiement à l'usage (PAYG)**, régions **Canada**
> (`canadacentral` / `ca-central-1`). Ce sont des **estimations d'ordre de grandeur** (les prix cloud
> varient) ; les **instances réservées 1–3 ans** réduisent le calcul de **~40–55 %** (voir §7).

## 0. Ce que « SLA 99,99 % » exige réellement

99,99 % (≤ 52 min d'indisponibilité/an) se garantit par la **redondance multi-zones dans une seule
région** : nœuds de calcul sur **3 zones**, base de données **primaire + veille synchrone dans une
autre zone**, entrée réseau et sauvegardes redondées. **Ce n'est pas** deux environnements complets
dupliqués (cela viserait 99,999 % / reprise multi-région). Le surcoût « HA » porte donc surtout sur la
**base (×2)** et le nombre minimal de **répliques applicatives (≥ 3)** — c'est ce qui est chiffré ici.

---

## 1. Production Azure (AKS + PostgreSQL Flexible HA) — détail

| Service | Configuration (HA multi-zone) | $/mois | $/an |
|---|---|---:|---:|
| AKS — nœuds applicatifs | 3 × D4s_v5 (4 vCPU/16 Go), zones 1/2/3 | 600 | 7 200 |
| AKS — plan de contrôle | tier Standard (SLA du control plane) | 100 | 1 200 |
| PostgreSQL Flexible | **HA redondante zones** (D2ds_v4 primaire **+ veille**), 128 Go, backup géo | 500 | 6 000 |
| Front Door + WAF | Standard : TLS géré, pare-feu OWASP, anti-DDoS | 80 | 960 |
| Log Analytics + Application Insights | journaux + télémétrie + **test de disponibilité** | 130 | 1 560 |
| Container Registry (ACR) | Standard | 20 | 240 |
| Blob Storage (ZRS) | documents + exports, redondance zones | 25 | 300 |
| Key Vault | secrets | 5 | 60 |
| Service Bus (Standard) | file de publication + DLQ | 15 | 180 |
| Event Grid | événements de domaine | 5 | 60 |
| Function App (Consumption) | worker de publication | 30 | 360 |
| Bande passante / egress | sortie de données | 50 | 600 |
| Divers (IP, DNS, alertes) | | 25 | 300 |
| **Sous-total production Azure (PAYG)** | | **~1 585** | **~19 020** |

> Avec **Front Door Premium** (WAF managé avancé) et un sur-dimensionnement, on monterait vers
> **~24 000 – 28 000 $/an**. Avec **réservation 3 ans** du calcul + BD : **~12 000 – 14 000 $/an**.

---

## 2. Production AWS (EKS/ECS + RDS Multi-AZ) — détail

| Service | Configuration (HA multi-AZ) | $/mois | $/an |
|---|---|---:|---:|
| EKS — plan de contrôle | 0,10 $/h | 73 | 876 |
| Nœuds (EC2) / Fargate | 3 × m6i.xlarge (4 vCPU/16 Go), 3 AZ | 480 | 5 760 |
| RDS PostgreSQL **Multi-AZ** | db.m6g.large **primaire + veille**, 100 Go gp3, backup | 540 | 6 480 |
| Passerelles NAT (×3, une par AZ) | HA sortie Internet | 135 | 1 620 |
| ALB | équilibreur applicatif | 25 | 300 |
| CloudFront + Route 53 | diffusion + DNS | 25 | 300 |
| S3 + ECR + Secrets Manager | stockage, images, secrets | 20 | 240 |
| SQS + Lambda + EventBridge + Step Functions + Glue | chaîne asynchrone (faible volume, 1 client) | 30 | 360 |
| CloudWatch + Synthetics (canari) | supervision + **mesure de disponibilité** | 60 | 720 |
| API Gateway | edge managé | 15 | 180 |
| Bande passante / egress | sortie de données | 50 | 600 |
| **Sous-total production AWS (PAYG)** | | **~1 453** | **~17 436** |

> Ordre de grandeur **similaire à Azure** (les passerelles NAT alourdissent AWS, des nœuds moins chers
> compensent). Fourchette réaliste selon dimensionnement : **~18 000 – 24 000 $/an**. Réservé 3 ans :
> **~11 000 – 14 000 $/an**.

**On retient une production ≈ 20 000 $/an (PAYG, un seul cloud), soit ~13 000 $/an en réservé 3 ans.**

---

## 3. Environnement d'essai / développement — réévaluation (prix validés, oct. 2026)

**Ce que l'appel d'offres exige — et n'exige pas.** Trois précisions tirées du texte :

- **Statut : « Souhaitable », pas obligatoire.** Addenda 1, exigence 26 : *« Environnement d'essai /
  projet pilote — **Souhaitable** »*. (Addenda 2 ne le modifie pas.)
- **Disponible « en tout temps » ? Non.** Article 3 : l'essai est *« distinct de la production
  permettant à la Ville de tester, valider, configurer, former… **avant la mise en production** »*.
  Aucune clause ne lui impose le « 24/7 / en tout temps » — cette obligation vise la **production**
  (SLA de disponibilité), pas l'essai.
- **Identique à la production ? Non.** Le cahier le veut **« distinct de la production »** et n'exige
  **ni HA, ni 99,99 %, ni multi-zone, ni DR** pour cet environnement.
- **Données de production ? Non exigé.** La *migration d'essai* (Art. 22) valide l'intégrité des
  modèles/clauses/documents ; dès que l'essai contient des **« Données de la Ville »** (renseignements
  personnels), la **Politique de sécurité + Loi 25** s'appliquent (chiffrement, accès, résidence Canada).
  On **recommande donc l'anonymisation** (le cahier définit « Anonymiser ») : cela change la **sécurité**
  (déjà appliquée), **pas la taille ni le coût d'infra**.

Conclusion : c'est un environnement **petit et peu coûteux** — c'est le point rassurant pour le budget.

**Dimensionnement : une base modeste + un calcul léger (mono-zone, sans WAF premium).** Prix de détail
relevés (Azure `canadacentral`, AWS `ca-central-1`) :

### Azure (Canada Central)
| Service | Configuration | $/mois | $/an |
|---|---|---:|---:|
| PostgreSQL Flexible **Burstable B1ms** | mono-zone, sans HA (0,0283 $/h × 730 h) | 21 | 248 |
| Calcul **Container Apps (Consommation)** | min-replicas bas, usage test (souvent dans la dotation gratuite) | 0–50 | 0–600 |
| Stockage + divers | documents de test, journaux | 10 | 120 |
| **Sous-total réaliste** | | **~30–80** | **~370 – 970** |
| Variante prudente (24/7, un peu surdimensionnée) | B2s + App Service B1 continus | ~170 | **~2 000** |

### AWS (ca-central-1, Montréal)
| Service | Configuration | $/mois | $/an |
|---|---|---:|---:|
| RDS PostgreSQL **db.t4g.micro** | mono-AZ (0,018 $/h) | ~19 | ~223 |
| **Fargate** petite tâche | 0,25 vCPU / 0,5 Go, mise à l'échelle basse | ~11 | ~135 |
| Stockage + divers | S3, journaux | ~10 | ~120 |
| **Sous-total réaliste** | | **~40** | **~480 – 800** |

**On retient ~1 000 – 2 500 $/an** selon le dimensionnement (le bas si l'on utilise la consommation et
l'arrêt automatique ; le haut si l'on garde un mini-environnement allumé 24/7). C'est une **petite ligne**.

> **Scénario qui coûterait cher — et qui n'est PAS exigé.** Si l'on faisait de l'environnement d'essai un
> **miroir complet de la production** (HA 99,99 %, multi-zone, même taille), il doublerait presque le coût
> d'infrastructure (**+ ~15 000 – 20 000 $/an**). Le cahier ne le demande pas : l'essai sert à valider
> **avant** la mise en production, pas à fournir une seconde production.

### 3.1 Est-ce couvert par le budget (licence 68 000 $/an) ? — OUI

| Poste (débours réels, un client) | PAYG | Réservé 3 ans |
|---|---:|---:|
| Production (1 cloud, HA 99,99 %) | ~18 000 – 20 000 $ | ~12 000 – 14 000 $ |
| **Environnement d'essai / dev (cette section)** | **~1 000 – 2 500 $** | **~1 000 – 2 500 $** |
| Support N1→N5 (temps du fondateur) | 0 $ (rémunéré par la marge) | 0 $ |
| **Total débours** | **~19 000 – 22 500 $** | **~13 000 – 16 500 $** |
| **Licence** | **68 000 $** | **68 000 $** |
| **Reste (temps du fondateur + marge)** | **~45 500 – 49 000 $** | **~51 500 – 55 000 $** |

**Conclusion : l'environnement d'essai exigé par la Ville est une petite ligne (~1 000 – 2 500 $/an) que
le budget de 68 000 $/an absorbe très largement** — il reste ~45 000 $+ pour le temps du fondateur
(implantation amortie, support, maintenance, évolutions) et la marge. **Le budget suffit.** La seule
façon de le mettre en tension serait de transformer l'essai en **seconde production HA** (non exigée) ou
d'embaucher une **équipe de support dédiée** (non justifiée pour un seul client).

---

## 4. Support N1 → N5 (coût humain)

Le support est le poste **le plus sensible**. Il dépend de **qui** l'assure :

| Scénario | Description | Coût trésorerie / an |
|---|---|---:|
| **A — Fondateur (réalité actuelle)** | 1 client ; le fondateur assure N1→N5 et l'astreinte (son temps). | **0 $** de débours (rémunéré par la marge) |
| **B — Valorisé au marché** | ~0,2 ETP d'un profil bilingue technique (~95 k$ chargé). | **~19 000 $** |
| **C — Équipe dédiée multi-niveaux** | Astreinte 24/7 réelle à plusieurs personnes. | 40 000 $+ (non réaliste pour 1 client) |

> Pour **un seul client**, le scénario réaliste est **A** : c'est du temps du fondateur, pas un
> débours. Le scénario **B** (ou C) ne se justifie **qu'avec plusieurs clients**.

---

## 5. Récapitulatif et marge 20 %

### Base « trésorerie » (réalité : 1 client, support assuré par le fondateur)

| Poste | $/an |
|---|---:|
| Production cloud (1 cloud, PAYG) | ~20 000 |
| Serveur de développement Azure | ~2 500 |
| Support N1→N5 (temps du fondateur) | 0 |
| **Coût de trésorerie total** | **~22 500** |
| **Prix pour dégager +20 % de marge** | **~27 000** |
| **Abonnement soumis** | **58 000** |
| **Verdict** | ✅ **NON perdant** — marge très confortable |

À 58 000 $ − 22 500 $ = **~35 500 $/an** restants pour **rémunérer le temps du fondateur** (support,
maintenance, évolutions) **et** dégager un profit. La marge sur coûts de trésorerie est de **~61 %**,
bien au-delà des 20 % visés. En **réservé 3 ans** (production ~13 000 $), le coût tombe à ~15 500 $ et
la marge grimpe encore.

### Base « pleinement chargée » (support payé + temps valorisé au marché)

| Poste | $/an |
|---|---:|
| Production cloud | ~20 000 |
| Serveur de développement | ~2 500 |
| Support N1→N5 (0,2 ETP, scénario B) | ~19 000 |
| Maintenance + petites évolutions (temps valorisé) | ~15 000 |
| **Coût total chargé** | **~56 500** |
| **Prix pour dégager +20 % de marge** | **~67 800** |
| **Abonnement soumis** | **58 000** |
| **Verdict** | ⚠️ marge **~2,5 %** seulement — **pas 20 %** à ce niveau de valorisation |

---

## 6. Conclusion : es-tu perdant à 58 000 $/an ?

- **NON, dans ta situation actuelle (1 client, support assuré par toi).** L'abonnement couvre
  **toute l'infrastructure cloud** (Azure ou AWS, ~20 k$) **+ le serveur de dev** (~2,5 k$) et laisse
  **~35 k$/an** pour ton temps et ta marge — soit une marge bien **supérieure à 20 %**. L'**An 1
  (158 000 $)** ajoute une forte entrée de trésorerie.
- **Tu deviendrais « juste » (marge < 20 %) uniquement** si tu **payais une équipe de support
  dédiée multi-niveaux** et valorisais tout ton temps au marché (**base chargée ~56,5 k$** → il
  faudrait ~68 k$ pour 20 % de marge). **Ce scénario ne se justifie pas pour un seul client.**

### Recommandations concrètes
1. **Rester à 58 000 $/an** : rentable en trésorerie dès ce client, et compétitif pour **gagner**.
2. **Ne pas embaucher** d'équipe de support dédiée tant qu'il n'y a qu'un client (assurer N1→N5 toi-même).
3. **Réserver le calcul + la base sur 3 ans** (durée du contrat ferme) → économie ~40–55 % sur la
   production, marge nettement améliorée.
4. **Optimiser les gros postes** : Front Door **Standard** (pas Premium), Function App **Consumption**,
   arrêt automatique du serveur de dev, taille de BD ajustée à la charge réelle.
5. **Si d'autres municipalités arrivent** : la production se mutualise, la marge grimpe **sans changer
   le prix** — mais on ne mise pas dessus aujourd'hui.

## 7. Note sur les instances réservées (contrat ferme 3 ans)

Le contrat étant **ferme sur 3 ans**, réserver les ressources persistantes (nœuds AKS/EC2, base HA)
est pertinent : **−40 à −55 %** sur ces postes. Production PAYG ~20 k$ → **~12–14 k$/an en réservé**,
ce qui fait passer le coût de trésorerie total (avec dev) sous **~16 k$/an**.
