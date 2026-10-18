# Analyse de conformité & roadmap d'amélioration

**Réf. :** Avis 2026-50-92 — Ville de Lévis · source : [doc-fonctionnel.md](doc-fonctionnel.md)
**Objet :** confronter la solution actuelle aux exigences fonctionnelles (EF) et non
fonctionnelles (ENF) du cahier, identifier les écarts et planifier les améliorations.

## 1. Matrice de conformité

Légende : ✅ conforme · 🟡 partiel · 🔴 à implémenter

| Exigence | État | Preuve / écart |
|----------|:----:|----------------|
| **EF1** Bibliothèque : création/modif/archivage standards/optionnelles/perso | ✅ | `Clause` + catégories, CRUD |
| EF1 Recherche multicritère (thématique, portée, type) | 🟡 | Recherche plein texte `/search` ; **manque filtres multicritères** |
| EF1 Historisation par clause (versions + auteur/date) | 🟡 | `version`+`majLe` ; **manque table d'historique par version** |
| **EF2** Génération dynamique par assemblage | ✅ | `DocumentService.assembler` |
| EF2 Adaptation type/mode/valeur/contexte | ✅ | `RuleEngine` + `ClauseCondition` |
| EF2 **Dépendances & exclusions entre clauses** | 🔴→✅ | *Implémenté dans cette itération* |
| **EF3** Demandes de prix / appels d'offres publics | ✅ | Types + assemblage |
| EF3 Gré à gré (baux, prêts à usage, services) | ✅ | Types + templates |
| EF3 **Avis d'intention** | 🔴→✅ | Type `AVIS_INTENTION` *ajouté* |
| EF3 Addendas interactifs + consolidation finale | ✅ | `Addenda` + rendu PDF/DOCX intégrant les addendas |
| **EF4** Commentaires/annotations dans le devis | ✅ | `Comment` + WYSIWYG |
| EF4 **Workflow d'approbation configurable** | 🟡 | Circuit APPRO→JURIDIQUE→DIRECTION **codé en dur** |
| **ENF1** Conformité LCOP/LCV (règles en données) | ✅ | Règles = `ClauseCondition` maintenables |
| ENF1 Loi 25 — chiffrement transit | ✅ | TLS |
| ENF1 Loi 25 — chiffrement au repos / *Privacy by Design* | 🟡 | **Chiffrement au repos + droits Loi 25 (accès/effacement) à ajouter** |
| **ENF2** Auth SSO Azure AD + RBAC + MFA | ✅ | Entra ID (OIDC) + RBAC + TOTP/question |
| ENF2 Journal d'audit | ✅ | `AuditLog` |
| **ENF4** Solution clé en main, conteneurisée | ✅ | Docker (front/back/PG/Ollama) |
| ENF4 Formation FR documentée | 🟡 | Docs techniques ; **manque guide utilisateur** |
| **DAO 2.3** Intégration **SEAO** (format devis) | 🔴 | À implémenter (export métadonnées SEAO) |
| DAO 2.3 Intégration annuaire d'entreprise | ✅ | Azure Entra ID |
| **Multi-tenant / multilingue** (au-delà du cahier) | ✅ | Bonus : isolation par tenant, i18n FR/EN/MG |

**Bilan.** Le cœur (EF1-EF4) est conforme. Écarts résiduels : workflow configurable,
historisation par clause, recherche multicritère, chiffrement au repos + droits Loi 25,
SEAO, guide utilisateur.

## 2. Améliorations livrées dans cette itération
- **EF2 — Dépendances & exclusions entre clauses** : une clause peut *requérir* d'autres
  clauses (auto-inclusion) et en *exclure* (jamais assemblées ensemble). Appliqué par le
  moteur d'assemblage.
- **EF3 — Type `AVIS_INTENTION`** ajouté (avis d'intention de contracter de gré à gré).

## 3. Roadmap des écarts restants

### Sprint 1 — Gouvernance des clauses (2-3 sem.)
- **EF1 Historisation par clause** : table `clause_version` (snapshot + auteur + date),
  endpoint d'historique et de restauration.
- **EF1 Recherche multicritère** : filtres par catégorie, type de contrat applicable,
  thématique (tags), statut de validation juridique.

### Sprint 2 — Workflow configurable (2 sem.)
- **EF4** : circuits d'approbation paramétrables par tenant (niveaux, rôles, conditions
  selon type/valeur), remplaçant le circuit codé en dur.

### Sprint 3 — Conformité Loi 25 renforcée (2-3 sem.)
- **ENF1** : chiffrement au repos (champs sensibles / TDE base), politique de conservation,
  **droits Loi 25** : accès, rectification, effacement (endpoints dédiés), registre des
  consentements. *Privacy by Design*.

### Sprint 4 — Intégration SEAO & publication (3 sem.)
- **DAO 2.3** : gabarit d'export conforme SEAO (métadonnées : numéro, catégorie UNSPSC,
  dates, addendas), pré-validation avant publication.

### Sprint 5 — Adoption & formation (2 sem.)
- **ENF4** : guide utilisateur FR (captures, parcours), matériel de formation, tutoriels
  in-app.

### Jalon cible
- Système pleinement opérationnel **octobre 2026** (ENF4). Les sprints 1-5 tiennent dans
  l'horizon avec marge pour le durcissement sécurité (questionnaire ENF2) et l'UAT.

## 4. Risques & mitigations
| Risque | Mitigation |
|--------|------------|
| Évolution LCOP/LCV | Règles en données, entretien juriste |
| Loi 25 (audit sécurité) | Sprint 3 dédié + questionnaire sécurité Ville |
| Format SEAO mouvant | Gabarit paramétrable, veille réglementaire |
| Adoption utilisateurs | Guide FR + formation + ergonomie (modales, WYSIWYG, i18n) |
