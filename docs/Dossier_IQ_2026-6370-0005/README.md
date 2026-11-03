# Dossier de conception — Investissement Québec 2026-6370-0005

Solution de signature électronique et de génération de contrats.

## Démarrage
1. Lire `01-Sources/` pour les transcriptions Markdown des deux PDF.
2. Lire `02-Analyse/` et `03-Fournisseurs/` pour distinguer besoins et obligations de soumission.
3. Lire `04-Architecture/` et `05-Modelisation/` pour la conception cible proposée.
4. Consulter `06-Roadmap/` puis chaque dossier de story sous `07-Stories/`.
5. Les décisions d'architecture globales sont sous `08-ADR/`; les décisions locales sont rappelées dans chaque story.
6. Les points à clarifier sont recensés dans `09-Questions/`.

## Base documentaire et prudence
Les deux sources sont :
- `01-Sources/Cahier_besoins_exigences.md` (39 pages à l'origine).
- `01-Sources/Document_appel_offres.md` (117 pages à l'origine).

Les exigences sont qualifiées `SRC` lorsqu'elles sont explicitement issues des documents. Les choix de conception sont qualifiés `PROP` et ne constituent pas des engagements déjà acceptés par IQ. Les éléments `À CONFIRMER` sont des ambiguïtés ou données absentes des PDF transmis.

**Document Excel officiel Annexe 10** : le document d'appel d'offres y fait référence, mais le fichier Excel n'a pas été fourni. Les lignes détaillées de cette matrice ne peuvent donc pas être retranscrites ni considérées comme analysées exhaustivement ici. Il faudra l'ajouter au dossier pour compléter la matrice de conformité.

## Périmètre fonctionnel
La génération documentaire et la signature électronique sont centrales. Le cahier précise que la génération de contrats n'implique pas automatiquement un CLM complet. Le document d'appel d'offres comporte toutefois un scénario de démonstration portant sur la gestion documentaire après signature et une gestion bout en bout de CLM; la portée exacte et les options CLM/IA doivent être clarifiées.

## Hypothèses techniques de conception
Architecture proposée : application web React, backend Java/Spring Boot, API REST, base relationnelle, stockage documentaire objet, moteur de workflow, adaptateurs Salesforce Grantmaking / identité / courriel, journal d'audit append-only. Cette proposition est une architecture de réalisation, pas une technologie imposée par les sources.
