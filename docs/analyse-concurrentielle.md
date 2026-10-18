# Analyse concurrentielle & roadmap des écarts

**Avis # 2026-50-92 — Ville de Lévis**

Comparaison de notre solution au **standard du marché** des logiciels de rédaction /
gestion du cycle de vie contractuel (CLM : *Contract Lifecycle Management*), établi à
partir des éditeurs de référence (Icertis, Ironclad, DocuSign CLM, Sirion, CobbleStone,
DiliTrust…) et des analyses sectorielles 2025-2026.

## 1. Les 7 étapes standard du cycle CLM

1. **Intake / initiation** (demande de contrat)
2. **Rédaction** (templates + bibliothèque de clauses)
3. **Négociation / redlining** (comparaison de versions, suivi des modifications)
4. **Approbation** (workflow configurable par type/valeur)
5. **Exécution / signature électronique**
6. **Post-exécution** (dépôt central, obligations, échéances, renouvellements)
7. **Analytique / reporting** (tableaux de bord)

## 2. Matrice de conformité

| # | Fonctionnalité standard CLM | Notre solution | Écart |
|---|-----------------------------|:--------------:|-------|
| 1 | Bibliothèque de clauses (standard/optionnelle/perso, versionnée) | ✅ | — |
| 2 | Assemblage / templates de documents | ✅ (assemblage) | Templates de documents nommés : partiel |
| 3 | Adaptation par règles (type, mode, valeur, contexte) | ✅ | — |
| 4 | Workflow d'approbation hiérarchisé | ✅ | — |
| 5 | Commentaires / collaboration en ligne | ✅ | — |
| 6 | **Redlining / comparaison de versions** | ⚠️ commentaires seuls | **Versionnage + diff manquants** |
| 7 | **Signature électronique** | ❌ | **À implémenter** |
| 8 | Génération PDF / DOCX | ✅ | — |
| 9 | Addendas | ✅ | — |
| 10 | Journal d'audit | ✅ | — |
| 11 | **Dépôt central + recherche plein texte** | ⚠️ liste simple | **Recherche à ajouter** |
| 12 | **Obligations / échéances / renouvellements + alertes** | ❌ | **À implémenter** |
| 13 | **Tableaux de bord / analytique** | ❌ | **À implémenter** |
| 14 | Assistance IA (rédaction, suggestions) | ✅ (Ollama local) | — |
| 15 | Contrôle d'accès par rôle (RBAC) | ✅ | — |
| 16 | Intégration signature externe (DocuSign/ConsignO) | ❌ | Roadmap |
| 17 | Publication SEAO (appels d'offres publics QC) | ❌ | Roadmap (spécifique QC) |

**Bilan :** le socle « rédaction + adaptation + approbation + rendu + addendas + audit +
IA » est **conforme au marché**. Les écarts portent sur l'aval du cycle (signature,
obligations, analytique) et sur le redlining/versionnage.

## 3. Roadmap des écarts (priorisé)

### Vague 1 — implémentée dans cette itération ✅
- **E-1 Signature électronique (module interne)** : signataires, cérémonie de
  signature par jeton, statuts, horodatage — couvre l'étape 5 du cycle.
- **E-2 Recherche plein texte** : `/api/search` sur clauses et documents (étape 6).
- **E-3 Tableau de bord analytique** : `/api/stats` (répartition par statut/type) —
  étape 7.

### Vague 2 — court terme (roadmap)
- **E-4 Versionnage de documents + redlining/diff** : instantanés versionnés et
  comparaison texte à texte (étape 3).
- **E-5 Obligations & échéances** : dates clés, renouvellements, alertes.
- **E-6 Templates de documents nommés** (au-delà de l'assemblage dynamique).

### Vague 3 — moyen terme (roadmap)
- **E-7 Intégration signature externe** conforme (ConsignO/Notarius — écosystème
  québécois — ou DocuSign) en complément du module interne.
- **E-8 Publication / synchronisation SEAO** pour les appels d'offres publics.
- **E-9 Analytique avancée** (délais de cycle, goulots d'approbation).

## 4. Sources
- [Icertis — What is CLM](https://www.icertis.com/learn/what-is-contract-lifecycle-management/)
- [Ironclad — CLM](https://ironcladapp.com/journal/contract-management/contract-lifecycle-management)
- [DocuSign CLM](https://www.docusign.com/products/clm)
- [Sirion — Review, Redlining, Version Control](https://www.sirion.ai/library/contract-management/contract-review-redlining-version-control/)
- [CobbleStone — Clause Libraries & AI Redlining](https://www.cobblestonesoftware.com/blog/clause-libraries-ai-redlining-governance)
- [Spellbook — Redline software features](https://spellbook.com/learn/contract-redline-software-features)
