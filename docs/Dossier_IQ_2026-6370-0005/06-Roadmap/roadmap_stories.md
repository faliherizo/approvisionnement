# Roadmap de réalisation — proposition

## Hypothèses de planification
Roadmap indicative à confirmer après ateliers et analyse du produit commercialisé retenu. Les délais ci-dessous ne sont pas des engagements extraits du DAO. Les services récurrents démarrent après acceptation finale; le contrat prévoit trois ans initiaux et deux options de 12 mois. Le déploiement initial doit intégrer une stabilisation de 30 jours ouvrables.

## Phases
| Phase | Durée indicative | Résultats / jalons |
|---|---:|---|
| P0 — Mobilisation et cadrage | 2 semaines | Gouvernance, RACI, périmètre, risques, backlog, plan projet |
| P1 — Ateliers et architecture détaillée | 3 semaines | Cartographie processus, sécurité, Salesforce, modèles, MCD, ADR |
| P2 — Fondations plateforme | 3–4 semaines | Environnements, CI/CD, IAM, observabilité, stockage, audit |
| P3 — Documents et modèles | 3–4 semaines | Dossiers, trousses, import PDF/Word, versioning, modèles, génération |
| P4 — Workflow et signature | 4–5 semaines | Validations, approbations, séquences/parallèles, signature, preuves |
| P5 — Intégration et notifications | 3–4 semaines | Salesforce, courriel, callbacks, retries, rappels |
| P6 — Administration et suivi | 2–3 semaines | Dashboard, gestion des accès, configuration, exports |
| P7 — Qualité, sécurité et acceptation | 3 semaines | Tests fonctionnels, intégration, performance, sécurité, UAT |
| P8 — Formation et mise en production | 2 semaines | Guides, formation, migration/configuration, Go/No-Go |
| P9 — Stabilisation | 30 jours ouvrables | Support renforcé, correction, transfert et clôture |

Les phases peuvent se chevaucher selon disponibilité des équipes et dépendances du fournisseur. La démonstration et la POC pré-adjudication sont des étapes d'évaluation avant contrat, pas des sprints de réalisation.

## Lots et stories
| Lot | Stories | Priorité |
|---|---|---|
| Identité et habilitations | ST-01, ST-02 | Must |
| Dossiers et documents | ST-03, ST-04 | Must |
| Modèles et génération | ST-05, ST-06 | Must |
| Workflow métier | ST-07, ST-08 | Must |
| Signature et preuve | ST-09, ST-10 | Must |
| Intégration et notifications | ST-11, ST-12 | Must |
| Pilotage et exploitation | ST-13, ST-14 | Must |
| CLM avancé / IA | ST-15 | Option / à clarifier |

## Dépendances critiques
- Accès sandbox Salesforce, schémas d'objets, API, données de test et responsable système.
- Décision sur moteur de signature, méthode de vérification d'identité et valeur probante.
- Modèles contractuels, clauses, règles et propriétaires métier.
- Politique de conservation et classification documentaire.
- Identité interne / SSO et processus de gestion des invités.
- Annexe 10 Excel, exigences de sécurité détaillées, RACI et bordereau officiel.
- Localisation hébergement, sous-traitants et approbations juridiques.
- Définition RTO/RPO, fenêtres de maintenance, SLA contractuel final.

## Definition of Ready
- Exigence liée et source identifiée.
- Règle métier et cas d'exception compris.
- Maquette / contrat API et modèle de données définis.
- Critères d'acceptation testables.
- Dépendances et données de test disponibles.
- Décision ADR requise prise ou explicitement différée.

## Definition of Done
- Code revu, tests automatisés exécutés.
- Tests sécurité et droits d'accès couverts.
- Événements audit et erreurs documentés.
- Documentation fonctionnelle/technique mise à jour.
- Observabilité et alertes ajoutées.
- Démonstration métier et acceptation IQ obtenues lorsque requis.
