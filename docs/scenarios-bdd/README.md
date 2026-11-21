# Scénarios BDD des exigences fonctionnelles — tous les appels d'offres

> Document généré par `bdd/outils/documenter.mjs` ; ne pas le modifier à la main.

Chaque exigence de chaque appel d'offres est rattachée à un ou plusieurs **scénarios Gherkin** (`bdd/features/`), exécutés de
bout en bout contre la plateforme réelle par Playwright. Les jeux de données et les **cas exécutables** (suites d'appels,
profils, statuts et valeurs attendus) sont en JSON (`bdd/donnees/`). Une exigence qui ne relève pas d'un test (service,
prix, référence, infrastructure) est déclarée **documentaire** avec sa justification ; un écart connu est **déclaré** et
garde son scénario en attente. La démarche est décrite dans [`bdd/README.md`](../../bdd/README.md).

| Appel d'offres | Document | Exigences | Par scénario | Réussies | Documentaires | Écarts | Sans objet |
|---|---|---|---|---|---|---|---|
| Ville de Lévis — avis d'appel d'intérêt 2026-50-92 | [levis.md](levis.md) | 41 | 34 | 34 | 7 | 0 | 0 |
| Ville de Trois-Rivières — appel d'offres 7000017905 | [trois-rivieres.md](trois-rivieres.md) | 37 | 20 | 20 | 17 | 0 | 0 |
| Investissement Québec — AO 2026-6370-0005 | [iq.md](iq.md) | 140 | 130 | 130 | 8 | 0 | 2 |
| Santé Québec — CHU, avis d'intention | [chu.md](chu.md) | 60 | 50 | 50 | 9 | 0 | 1 |

Scénarios : 83 dans 19 fonctionnalités ; cas exécutables : 79.
Résultats : dernière exécution de `bdd/test-results/resultats.json`.
