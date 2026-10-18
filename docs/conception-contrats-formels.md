# Conception & roadmap — production de contrats formels

**Objet :** adapter la solution pour **rédiger/générer automatiquement** des documents
conformes aux exemples de [docs/dossiers_contrat_exs/](dossiers_contrat_exs/).

## 1. Analyse des exemples

| Fichier | Type | Structure clé |
|---------|------|---------------|
| `CO2026-7060-AO - CONTRAT.pdf` | **Contrat de services** | Numérotation décimale multi-niveaux (`0.00`, `0.01.01`), **table des matières paginée**, préambule (parties), définitions, articles, **liste des annexes** (numérotées par clause), en-tête/pied de page « Page X de N » |
| `CCAG_TI_20250527.pdf` | **CCAG** (clauses générales) | Cahier **normatif fixe** : Instructions aux soumissionnaires + Conditions générales, TOC |
| `26051-S_AO_CCDE_VF.pdf` | **CCDE** (consultation) | Cahier **variable** ; sections `1.`, `1.1`, `1.2`… ; référence le CCAG ; annexes |
| `Avis d'intérêt #442.pdf` | **Avis d'intérêt / DI** | Sections numérotées, identification de l'organisme, « Page X sur N » |
| `…BORDEREAU DE PRIX.pdf` | **Bordereau de prix** | Tableau de prix (lignes, taux, totaux), mention de rejet si non signé |
| `…FORMULAIRE DE SOUMISSION`, `…RÉGIE`, `GABARIT EXPÉRIENCE` | **Annexes/formulaires** | Formulaires structurés |

### Constantes à supporter
1. **Numérotation hiérarchique** décimale, multi-niveaux, auto-calculée.
2. **Page couverture** (bandeau titre, n° d'AO, objet, mention du type).
3. **Préambule** avec **parties** (organisme public + prestataire : dénomination,
   adresse, représentants) et clause « ENTRE … ET … ».
4. **Section de définitions** (termes en majuscule).
5. **Table des matières** auto-générée, avec numéros de page.
6. **Annexes** numérotées par clause + **liste des annexes**.
7. **En-tête / pied de page** répétés (titre courant, « Page X de N », date).
8. Renvoi entre cahiers (**CCAG** normatif + **CCDE** variable).
9. **Tableaux** (bordereau de prix) et **formulaires** (soumission, régie).

## 2. Écart avec la solution actuelle

| Besoin | Actuel | Écart |
|--------|--------|-------|
| Clause hiérarchique numérotée | Clause **plate** (code) | 🔴 numéro/section à ajouter |
| Structure de document (sections/annexes) | Liste de clauses | 🔴 modèle structuré |
| Parties / préambule | ➖ (contexte libre) | 🔴 métadonnées parties |
| Page couverture + TOC + pied de page | Rendu simple | 🔴 rendu formel |
| Annexes | Addendas seulement | 🟡 notion d'annexe |
| Bordereau / formulaires | ➖ | 🔴 gabarits tableaux |
| CCAG normatif fixe | Clauses tenant | 🟡 clauses « normatives » globales |

## 3. Conception logicielle (SOLID / Clean Architecture)

Introduire un **pipeline de composition + rendu** découplé (ports & adaptateurs) :

```
Document (entité) ──► ContractComposer ──► ContractModel ──► DocumentRenderer (port)
                        (SRP: assemble)      (modèle riche)      ├─ PdfContractRenderer
                                                                 └─ DocxContractRenderer
```

- **`ContractModel`** (record immuable) : `titre`, `numeroAO`, `type`, `parties`
  (organisme, prestataire), `definitions`, `sections[]` (chaque section : `numero`,
  `titre`, `clauses[]` avec numéro décimal), `annexes[]`.
- **`ContractComposer`** (service, SRP) : construit le `ContractModel` à partir du
  `Document` (clauses assemblées + numérotation hiérarchique + annexes intégrées).
- **`DocumentRenderer`** (port, ISP/DIP) : `byte[] rendre(ContractModel)`. Deux
  adaptateurs : **PDF** (page couverture, **TOC paginée**, titres numérotés,
  **pied de page « Page X de N »**, annexes) et **DOCX**.
- **`ExportController`** dépend du **port**, choisit l'adaptateur par format (OCP).

### Modèle de données (incréments)
- `Clause` : ajouter `numero` (ex. `10.06.01`), `section` (regroupement), `normative`
  (bool, pour le CCAG global).
- `Document` : ajouter métadonnées **parties** (organisme public, prestataire).
- Nouveau `DocumentAnnexe` (titre, référence de clause, contenu) — vague 2.
- `Bordereau` / `LignePrix` (tableau de prix) — vague 3.

## 4. Roadmap

### Vague 1 — Rendu formel & numérotation (implémentée dans cette itération)
- `numero`/`section` sur les clauses (Liquibase) + numérotation à l'assemblage.
- Métadonnées **parties** sur le document.
- Pipeline **ContractComposer + DocumentRenderer (port) + PdfContractRenderer/DocxContractRenderer** :
  **page couverture**, **table des matières**, **clauses numérotées hiérarchiquement**,
  **pied de page « Page X de N »**, préambule des parties, annexes/addendas.

### Vague 2 — Structure & annexes (2-3 sem.)
- Modèle de **sections** ordonnées + **annexes** nommées et numérotées par clause.
- Section **Définitions** dédiée ; **liste des annexes**.
- Gabarits de document par type (CONTRAT, CCDE, AVIS_INTENTION).

### Vague 3 — Cahiers, bordereaux et formulaires (3-4 sem.)
- **CCAG normatif** (clauses globales fixes) + composition CCDE référant le CCAG.
- **Bordereau de prix** (tableau paramétrable) et **formulaires** (soumission, régie,
  gabarit d'expérience) comme types d'annexes structurées.

### Vague 4 — Fidélité éditoriale & conformité (2-3 sem.)
- En-têtes de page (titre courant, n° d'AO), styles typographiques, signatures.
- Export conforme **SEAO** ; validation juridique des gabarits.

## 5. Décisions de cette itération
- Pipeline **composer + renderer (port)** en place, respectant SRP/DIP/OCP.
- Numérotation hiérarchique + page  couverture + TOC + pied de page dans le rendu PDF/DOCX.
- Les évolutions structurelles lourdes (sections/annexes/bordereaux) suivent en vagues 2-3.
