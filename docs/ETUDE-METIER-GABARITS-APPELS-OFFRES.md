# Étude métier — Génération des gabarits normalisés d'appels d'offres (Québec / SCT)

> **Contexte** : élargir la solution (aujourd'hui centrée sur le contrat municipal Ville de Lévis)
> pour produire la **famille de documents normalisés des marchés publics du Québec** telle que
> déposée en pièces jointes (Santé Québec, n° `CO2026-7060-AO` et `2026-0036-AO`).
> Le CCAG est explicitement « un document normatif fixe obligatoire » : ces documents sont des
> **gabarits paramétrables** (organisme, n° AO, titre, dates, seuils, montants) — exactement ce
> qu'un moteur d'assemblage de clauses sait faire.

## 1. Documents cibles et découpage métier

| # | Document (pièce jointe) | Nature | Structure clé |
|---|-------------------------|--------|---------------|
| 1 | **Avis d'intérêt** (#442) | RFI / demande d'information | En-tête organisme, préambule, définition du besoin, normes, informations demandées, dépôt |
| 2 | **CCAG** (Contrat de services) | Cahier normatif fixe | Remarque, table des matières, sections 1..2 (instructions, conditions générales) |
| 3 | **Régie** | Régie de l'AO | Sommaire, terminologie 0.01.xx, objet, règles de présentation, admissibilité, conformité, adjudication |
| 4 | **Devis technique** | Description des besoins | Description besoin, contexte, livrables, critères de conformité (tableau Oui/Non), délais |
| 5 | **Contrat de services** | Contrat | Préambule ENTRE/ET, 0.00 Interprétation → 16.00 Portée, annexes |
| 6 | **Bordereau de prix** | Grille de prix | Tableau (description, heures, ressources, taux, années, total) + bloc signature |
| 7 | **Formulaire de soumission** | Formulaire adjudicataire | Statut juridique, attestations 7.00, annexes (CNESST, Revenu Québec, Charte, lobbyisme) |
| 8 | **Gabarit expérience pertinente** | Fiche de référence | 1 fiche/mandat : organisme, contact, dates, description |

**Invariants de format communs (marqueurs à reproduire)** :
- Page couverture : sur-label **« APPEL D'OFFRES - SERVICES »**, type du document en gros
  (`CONTRAT` / `RÉGIE` / `DEVIS TECHNIQUE` / `FORMULAIRE DE SOUMISSION` / `BORDEREAU DE PRIX`),
  **`NO {numéro AO}`**, titre, sous-titre `(Services de nature technique)`.
- Préambule contrat : `ENTRE : … CI-APRÈS DÉNOMMÉE L'«ORGANISME PUBLIC» ; ET : … LE «PRESTATAIRE DE SERVICES»`.
- Numérotation `0.00`, `0.01.01`, `1.00`… ; table des matières.
- Pied de page : `DATE D'IMPRESSION` (gauche) · `- Page X de Y -` (centre) · **label du document** (droite).

## 2. Cartographie « existant → cible » (analyse d'écart)

La solution possède déjà une base solide (vérifiée dans le code) :

| Besoin gabarit | Déjà présent | Écart |
|----------------|--------------|-------|
| Organisme public, n° AO, prestataire | `Document.organismePublic/numeroAO/prestataire` | — |
| Type de contrat (AO public, gré à gré, contrat services, avis d'intention…) | `Enums.TypeContrat` | manque `DEVIS`, `RÉGIE`, `FORMULAIRE`, `BORDEREAU` comme **sorties** |
| Sections numérotées + articles | `ContractModel.Section/Article`, `ContractComposer` | numérotation `0.00.xx` à affiner |
| Préambule ENTRE/ET | `PdfContractRenderer.preambule()` | libellés `«ORGANISME PUBLIC»/«PRESTATAIRE DE SERVICES»` à aligner |
| Bordereau de prix | `Bordereau` + `LignePrix` + rendu intégré | **pas de gabarit autonome** aux colonnes SCT + bloc signature |
| Annexes / addendas | `DocumentAnnexe`, `Addenda` | — |
| Rendu PDF/DOCX + branding | `PdfContractRenderer`, `DocxContractRenderer`, `BrandingProvider` | **un seul gabarit codé en dur (« CONTRAT »)** |
| Page couverture, table des matières, pied de page | présents | libellés **non paramétrés par gabarit** |

**Conclusion de l'écart** : le modèle de données et le pipeline de rendu existent. Il manque
**une dimension « Gabarit »** qui paramètre (a) le sur-label / label / sous-titre de couverture,
(b) le label du pied de page, (c) le corps spécifique (ex. tableau du bordereau, tableau de
conformité Oui/Non du devis). **Aucune refonte** : ajout OCP-compatible.

## 3. Conception retenue (SOLID / OCP)

- **`render.Gabarit`** (enum) : porte les métadonnées de présentation d'un document
  (`surLabel`, `label`, `sousTitre`, `piedLabel`) + le mode de corps (`CORPS_CONTRAT`, `BORDEREAU`, …).
  → *Open/Closed* : ajouter un document = ajouter une valeur d'enum, sans toucher aux appelants.
- **`ContractModel.gabarit`** : le modèle transporte le gabarit choisi (défaut `CONTRAT_SERVICES`,
  rétro-compatible). Le **port `DocumentRenderer.rendre(model, branding)` reste inchangé**
  (pas de rupture d'interface).
- **`ContractComposer.composer(..., gabarit)`** : sélectionne le gabarit (mappé depuis le
  `TypeContrat` du document, ou forcé via l'API).
- **`PdfContractRenderer`** : couverture + pied paramétrés par `gabarit`; branche un rendu de
  corps dédié pour `BORDEREAU`.
- **`ExportController`** : `?gabarit=REGIE|CONTRAT_SERVICES|BORDEREAU_PRIX|…` (défaut = mappé
  depuis le type du document), sur les endpoints existants `/export/pdf`, `/export/pdf/apercu`, `/export/docx`.

## 4. Validation (« contenus et format identiques aux gabarits »)

La fidélité du **contenu** se valide au plus près du rendu : un **test backend** qui **extrait le
texte** du PDF généré (Apache PDFBox, portée test) et **assère les marqueurs** de chaque gabarit
(`APPEL D'OFFRES - SERVICES`, `RÉGIE`, `CONTRAT`, `BORDEREAU DE PRIX`, `l'«ORGANISME PUBLIC»`,
en-têtes de colonnes du bordereau, etc.). Le **e2e Playwright** valide le **flux** (sélection du
gabarit → téléchargement d'un PDF valide). Cette répartition évite d'embarquer un parseur PDF
dans le navigateur tout en garantissant la conformité structurelle.

## 5. Feuille de route par phases (priorisée par valeur / effort)

| Phase | Livrable | État |
|-------|----------|------|
| **P1 (ce lot)** | Dimension `Gabarit` + couverture/pied paramétrés (CONTRAT, RÉGIE, DEVIS, FORMULAIRE, AVIS) + **Bordereau de prix** autonome + test d'extraction PDF + e2e flux | **livré** — 47 tests backend (dont 3 d'extraction PDFBox) + 35 tests e2e verts |
| P2 | Corps normatif **CCAG / Régie** (clauses SCT en catalogue) + numérotation `0.00.xx` fidèle | à venir |
| P3 | **Devis technique** : tableau « Critères de conformité » (Oui/Non) | à venir |
| P4 | **Formulaire de soumission** : statut juridique + annexes (CNESST, Revenu Québec, Charte, lobbyisme) | à venir |
| P5 | **Avis d'intérêt** + **Gabarit expérience pertinente** (fiches répétables) | à venir |
| P6 | Table des matières paginée (`Page X de Y` réel) + génération DOCX par gabarit | à venir |

> **Note d'honnêteté** : reproduire à l'identique 8 documents SCT (jusqu'à ~140 pages, texte
> normatif intégral) est un chantier **multi-phase**, pas un livrable unique. P1 pose la
> fondation extensible et livre le premier gabarit distinct (bordereau) + la validation de contenu.
