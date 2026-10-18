---
title: Architecture HA (SLA 99,99 %) et performances — AO 7000017905
---

# Architecture de déploiement haute disponibilité (SLA 99,99 %) et résultats des tests de performance

> **VINA IT Solutions inc.** — Appel d'offres n° 7000017905, Ville de Trois-Rivières.
> Ce document explique **comment la solution garantit un SLA de disponibilité de 99,99 %** et présente
> les **résultats mesurés des tests de charge** (latence, débit, taux d'erreur) validant les objectifs
> de performance.

## 1. Ce que « 99,99 % » signifie concrètement

| Disponibilité | Indisponibilité tolérée / mois | / an |
|---|---|---|
| 99,9 % | 43,8 min | 8 h 46 |
| **99,99 % (notre engagement)** | **4,38 min** | **52,6 min** |

Atteindre 99,99 % exige d'**éliminer les points de défaillance uniques** à chaque étage
(calcul, données, entrée réseau) et de basculer automatiquement en cas d'incident.

## 2. Architecture de déploiement en haute disponibilité (Amazon Web Services, ca-central-1 Montréal)

_(Le schéma détaillé des étages figure dans le tableau ci-dessous.)_

| Étage | Mécanisme de haute disponibilité |
|---|---|
| **Entrée réseau** | **Amazon CloudFront + Application Load Balancer (ALB)** : point d'entrée redondé, TLS géré (ACM), routage et **protection anti-DDoS (AWS Shield)**. Le **pare-feu applicatif OWASP** est fourni par **AWS WAF** (règles managées Core Rule Set), facturé à la requête. |
| **Calcul (app)** | **Amazon ECS sur AWS Fargate** : plusieurs tâches du backend et du frontend, **réparties sur des zones de disponibilité** distinctes, **mise à l'échelle automatique** ; une tâche ou une zone peut tomber sans interruption de service. |
| **Données** | **Amazon RDS PostgreSQL en Multi-AZ** : nœud primaire + nœud de veille **synchrone** dans une autre zone, **bascule automatique** (failover) sans perte de données validées. |
| **Sauvegarde / reprise** | Sauvegardes **automatiques géo-redondantes** + **plan de reprise (DR)** : RPO et RTO courts, restauration testée. |
| **Secrets** | **AWS Secrets Manager + KMS** (aucun secret dans les images) ; TLS de bout en bout. |
| **Supervision** | **Amazon CloudWatch + CloudWatch Synthetics** : télémétrie applicative, journaux centralisés et **canaris de disponibilité** (sondes URL toutes les **5 minutes**) qui **mesurent la disponibilité de façon indépendante** de l'application ; sondes de santé (Actuator) et **alertes** en continu. C'est la mesure Synthetics qui **atteste le taux de disponibilité** servant au calcul du SLA. |

> Résultat : aucune défaillance d'un composant unique (une réplique, une zone, le nœud de base de
> données) n'entraîne d'indisponibilité perceptible — c'est ce qui soutient l'engagement de 99,99 %.

> **Régions.** La **haute disponibilité multi-zone (99,99 %) s'exécute dans la région AWS `ca-central-1`
> (Montréal, Québec)**, qui offre **trois zones de disponibilité**. La région **`ca-west-1` (Calgary)**
> sert à la **reprise après sinistre inter-région (DR)**. Les données restent **au Canada** — **au Québec
> pour la production** (Montréal) —, conformément à l'exigence Addenda 1.

## 3. Engagement de service (SLA) et objectifs internes (SLO)

Nous nous imposons des **objectifs internes (SLO) plus stricts que l'engagement contractuel (SLA)**,
afin de conserver une marge avant tout crédit de service.

### 3.1 Objectifs internes (SLO) — cibles de conception

| Indicateur | Cible (SLO) |
|---|---|
| Disponibilité mensuelle | **≥ 99,99 %** |
| Latence lecture P95 | ≤ 800 ms |
| Latence lecture P99 | ≤ 1500 ms |
| Latence écriture P95 | ≤ 1500 ms |
| Latence écriture P99 | ≤ 2500 ms |
| Taux d'erreur | < 0,1 % |

### 3.2 Engagement contractuel (SLA) — crédits de service

| Palier de disponibilité mensuelle | Crédit de service |
|---|---|
| **≥ 99,99 %** (cible AWS Multi-AZ) | conforme |
| 99,90 % – 99,99 % | crédit palier 1 |
| 99,50 % – 99,90 % | crédit palier 2 |
| < 99,50 % | crédit palier 3 |

Fenêtre mensuelle, 24/7, hors maintenance planifiée annoncée à l'avance. Plafonds durs de latence
par requête : lecture ≤ 3000 ms, écriture ≤ 5000 ms.

### 3.3 Notre responsabilité — garantir le 99,99 % (et si ce n'est pas atteint)

Le 99,99 % est **notre engagement, pas celui de la Ville** : c'est à VINA de le tenir, de le mesurer et
d'en assumer les conséquences en cas de manquement.

- **Surveillance 24/7 à notre charge** : la supervision (**CloudWatch + Synthetics** — canaris de disponibilité — et alertes) détecte les incidents **avant** la
  Ville ; nous n'attendons pas un signalement pour intervenir.
- **Rétablissement automatique** : la conception (répliques multi-zones, bascule automatique de la base)
  fait qu'une panne d'un composant ou d'une zone se **récupère seule**, sans intervention et sans
  interruption perceptible.
- **Engagements de rétablissement (incidents résiduels)** :

  | Sévérité | Prise en charge | Objectif de rétablissement |
  |---|---|---|
  | Majeure (service indisponible) | ≤ 30 min | ≤ 4 h |
  | Élevée (fonction clé dégradée) | ≤ 2 h | ≤ 1 jour ouvrable |
  | Normale | ≤ 1 jour ouvrable | selon planification |

- **Reprise après sinistre (DR)** : sauvegardes géo-redondantes, **RPO ≤ 15 min, RTO ≤ 4 h**, procédure
  de restauration testée.
- **Crédits de service automatiques** : si la disponibilité mensuelle passe **sous 99,99 %**, les
  **crédits du §3.2 s'appliquent** — c'est notre responsabilité financière, pas une négociation.
- **Rapport de disponibilité mensuel** remis à la Ville (mesure indépendante, transparence), et
  **analyse post-incident (post-mortem)** après tout incident majeur, avec actions correctives.
- **Maintenance planifiée** : annoncée à l'avance, en dehors des heures ouvrables, **exclue du calcul**
  du SLA.

**Partage des responsabilités** : VINA répond de la plateforme, de l'hébergement HA, des correctifs et du
rétablissement ; Amazon Web Services fournit l'infrastructure sous-jacente (SLA AWS) ; la Ville conserve la
maîtrise de ses accès et de ses données. En clair : **si le serveur n'atteint pas 99,99 %, c'est à nous
d'agir, d'expliquer et de créditer** — la Ville n'a rien à démontrer.

## 4. Résultats des tests de performance (mesurés)

**Méthode** : tests de charge automatisés **Apache JMeter**, 6 plans couvrant les parcours critiques
(dossiers, assistant/recherche, gouvernance/audit, publication M365/SAP, référence SAP,
réversibilité/import), **20 utilisateurs simultanés** (montée sur 60 s). Le harnais **classe** les
requêtes en lecture/écriture, calcule P95/P99 et le taux d'erreur, et **échoue automatiquement** si un
SLO est dépassé (utilisable comme porte de qualité en intégration continue).

### 4.1 Résultats sous charge nominale

| Indicateur mesuré | Résultat | Objectif (SLO) | Verdict |
|---|---|---|---|
| Latence lecture — médiane | 86 ms | — | — |
| Latence lecture — **P95** | **105 ms** | ≤ 800 ms | ✅ |
| Latence lecture — **P99** | **385 ms** | ≤ 1500 ms | ✅ |
| Latence écriture — médiane | 4 ms | — | — |
| Latence écriture — **P95** | **6 ms** | ≤ 1500 ms | ✅ |
| Latence écriture — **P99** | **9 ms** | ≤ 2500 ms | ✅ |
| **Taux d'erreur** | **0,000 %** (0 sur 240 requêtes) | < 0,1 % | ✅ |

> **Tous les objectifs sont respectés, avec une marge importante** : la latence de lecture P95 mesurée
> (105 ms) est **~8× sous la cible** (800 ms), et aucune requête n'a échoué.

### 4.2 Reproductibilité

Les tests sont **rejouables** par la Ville et exécutés en continu (porte de performance) :

```bash
cd perf && ./run.sh          # plan P1 (dossiers), gate SLO
PLAN=contrat-publication-load.jmx ./run.sh   # publication M365/SAP, etc.
```

Le rapport HTML détaillé et le fichier de résultats brut (`results/`) sont fournis sur demande.

## 5. Distinction latence / disponibilité

- La **latence et le taux d'erreur** (§4) sont **validés par les tests de charge** ci-dessus.
- La **disponibilité (SLA 99,99 %)** est garantie par l'**architecture HA** (§2) et **mesurée en
  continu** par la supervision (**CloudWatch / Synthetics**, canaris de disponibilité
  synthétiques) : c'est un engagement opérationnel, assorti de crédits de service (§3.2), et non une
  valeur de test ponctuel.

## 6. Synthèse

| Engagement | Moyen | Preuve |
|---|---|---|
| Disponibilité **99,99 %** | CloudFront + ALB + AWS WAF OWASP · ECS Fargate multi-zones (ca-central-1 Montréal, 3 zones) · RDS Multi-AZ · DR inter-région (ca-west-1) | Architecture §2 + supervision continue |
| **Performance** (latence, erreurs) | Optimisation applicative + autoscale | **Tests JMeter mesurés §4 — SLO respectés** |
| **Transparence** | SLI/SLO/SLA documentés, tests rejouables | Porte de performance en intégration continue |
