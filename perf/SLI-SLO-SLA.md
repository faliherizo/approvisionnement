# SLI / SLO / SLA — solution VINA (dossiers d'approvisionnement & API contractuelle)

> Objectifs de performance et de disponibilité, alignés sur l'appel d'offres Trois-Rivières
> n° 7000017905 (« Disponibilité du service », service hébergé 24/7, crédits de service) et sur
> l'offre SaaS Azure (cible 99,99 %). Les tests de charge [JMeter](contrat-dossiers-load.jmx)
> **valident** les SLO de latence et de taux d'erreur ; la disponibilité (SLA) se mesure en continu
> par la supervision d'exploitation.

## 1. Indicateurs de niveau de service (SLI) — ce qu'on mesure

| SLI | Définition | Source de mesure |
|-----|-----------|------------------|
| **Disponibilité** | % de temps où le service répond correctement (2xx/3xx) sur la fenêtre mensuelle | Sonde de supervision + journaux |
| **Latence (lecture)** | Temps de réponse des requêtes `GET` (liste, rapport) | JMeter (P95/P99) |
| **Latence (écriture)** | Temps de réponse des requêtes `POST/PUT` (création, statut) | JMeter (P95/P99) |
| **Taux d'erreur** | % de requêtes en échec (≥ 400 ou timeout) sur le total | JMeter + journaux |

## 2. Objectifs internes (SLO) — cibles de conception

Mesurés sous charge nominale (voir plan JMeter : 20 utilisateurs simultanés, 60 s de montée).

| SLO | Cible | Gate JMeter |
|-----|-------|-------------|
| Disponibilité mensuelle | **≥ 99,99 %** | supervision (hors JMeter) |
| Latence lecture **P95** | **≤ 800 ms** | `slo.read.p95.ms=800` |
| Latence lecture **P99** | ≤ 1500 ms | `slo.read.p99.ms=1500` |
| Latence écriture **P95** | **≤ 1500 ms** | `slo.write.p95.ms=1500` |
| Latence écriture **P99** | ≤ 2500 ms | `slo.write.p99.ms=2500` |
| Taux d'erreur | **< 0,1 %** | `slo.error.rate.pct=0.1` |

> Le SLO est un **plancher interne plus strict que le SLA contractuel** : on se garde une marge
> avant de payer un crédit de service.

## 3. Engagement contractuel (SLA) — ce qu'on promet au client

Aligné sur les paliers de l'appel d'offres (crédits de service en cas de manquement) :

| Palier de disponibilité mensuelle | Crédit de service |
|-----------------------------------|-------------------|
| ≥ 99,99 % (cible Azure HA) | — (conforme) |
| 99,90 % – 99,99 % | crédit palier 1 |
| 99,50 % – 99,90 % | crédit palier 2 |
| < 99,50 % | crédit palier 3 |

- **Fenêtre** : mensuelle, 24/7, hors maintenance planifiée communiquée à l'avance.
- **Latence contractuelle (plafond dur)** : lecture ≤ 3000 ms, écriture ≤ 5000 ms par requête —
  contrôlée par les *Duration Assertion* du plan JMeter (échec de la requête au-delà).

## 4. Comment valider

```bash
cd perf
./run.sh                                     # P1 : plan dossiers (défaut)
PLAN=contrat-assistant-load.jmx ./run.sh     # P2 : assistant guidé + recherche
PLAN=contrat-gouvernance-load.jmx ./run.sh   # P3 : journal d'audit + workflows
PLAN=contrat-publication-load.jmx ./run.sh   # P4 : publication M365/SAP
PLAN=contrat-sap-load.jmx ./run.sh           # P5 : référence financière SAP
PLAN=contrat-donnees-load.jmx ./run.sh       # P6 : réversibilité + import
BASE_PORT=8100 ./run.sh                      # contre un backend natif sur un port isolé
```

Plans disponibles : `contrat-dossiers-load.jmx` (P1), `contrat-assistant-load.jmx` (P2),
`contrat-gouvernance-load.jmx` (P3), `contrat-publication-load.jmx` (P4),
`contrat-sap-load.jmx` (P5), `contrat-donnees-load.jmx` (P6). Le runner
classe automatiquement les requêtes en lecture (`GET`, login, `analyser`, `search`) et écriture
(`POST/PUT/DELETE` mutants) pour appliquer les bons SLO.

`run.sh` lance JMeter en mode non-graphique, produit `results/rapport/` (rapport HTML) et
**calcule P95/P99/taux d'erreur** depuis `results/results.jtl`, puis compare aux SLO du §2 :
sortie non nulle si un objectif est dépassé (utilisable en CI comme porte de performance).
