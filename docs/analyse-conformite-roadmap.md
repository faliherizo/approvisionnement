# Analyse de conformité & roadmap d'amélioration

**Réf. :** Avis 2026-50-92 — Ville de Lévis · source : [doc-fonctionnel.md](doc-fonctionnel.md)
**Objet :** confronter la solution actuelle aux exigences fonctionnelles (EF) et non
fonctionnelles (ENF) du cahier, identifier les écarts et planifier les améliorations.

## 1. Matrice de conformité

Légende : ✅ conforme · 🟡 partiel · 🔴 à implémenter

| Exigence | État | Preuve / écart |
|----------|:----:|----------------|
| **EF1** Bibliothèque : création/modif/archivage standards/optionnelles/perso | ✅ | `Clause` + catégories, CRUD |
| EF1 Recherche multicritère (thématique, portée, type) | 🟡→✅ | `/api/search` : terme, portée, thématique (section), type de contrat applicable, normatives ; facettes (`RechercheMulticritere`) |
| EF1 Historisation par clause (versions + auteur/date) | 🟡→✅ | Table `clause_version` (version, auteur, date, motif) + restauration (LEV-C-03) |
| **EF2** Génération dynamique par assemblage | ✅ | `DocumentService.assembler` |
| EF2 Adaptation type/mode/valeur/contexte | ✅ | `RuleEngine` + `ClauseCondition` |
| EF2 **Dépendances & exclusions entre clauses** | 🔴→✅ | *Implémenté dans cette itération* |
| **EF3** Demandes de prix / appels d'offres publics | ✅ | Types + assemblage |
| EF3 Gré à gré (baux, prêts à usage, services) | ✅ | Types + templates |
| EF3 **Avis d'intention** | 🔴→✅ | Type `AVIS_INTENTION` *ajouté* |
| EF3 Addendas interactifs + consolidation finale | ✅ | `Addenda` + rendu PDF/DOCX intégrant les addendas |
| **EF4** Commentaires/annotations dans le devis | ✅ | `Comment` + WYSIWYG |
| EF4 **Workflow d'approbation configurable** | 🟡→✅ | Circuits par type de document (`/api/workflows`, écran Circuits) ; le circuit APPRO→JURIDIQUE→DIRECTION ne sert que par défaut |
| **ENF1** Conformité LCOP/LCV (règles en données) | ✅ | Règles = `ClauseCondition` maintenables |
| ENF1 Loi 25 — chiffrement transit | ✅ | TLS |
| ENF1 Loi 25 — chiffrement au repos / *Privacy by Design* | 🟡 | Droits Loi 25 livrés (accès, effacement par pseudonymisation, registre — LEV-C-15) ; chiffrement au repos assuré par l'hébergement (chiffrement du stockage géré), hors application |
| **ENF2** Auth SSO Azure AD + RBAC + MFA | ✅ | Entra ID (OIDC) + RBAC + TOTP/question |
| ENF2 Journal d'audit | ✅ | `AuditLog` |
| **ENF4** Solution clé en main, conteneurisée | ✅ | Docker (front/back/PG/Ollama) |
| ENF4 Formation FR documentée | 🟡→✅ | [Guide de l'utilisateur](guide-utilisateur.md), par profil, en français |
| **DAO 2.3** Intégration **SEAO** (format devis) | 🔴→✅ | Fiche des métadonnées SEAO d'un devis en XML (LEV-C-20) |
| DAO 2.3 Intégration annuaire d'entreprise | ✅ | Azure Entra ID |
| **Multi-tenant / multilingue** (au-delà du cahier) | ✅ | Bonus : isolation par tenant, i18n FR/EN/MG |

**Bilan.** Le cœur (EF1-EF4) est conforme, et les écarts relevés à l'origine sont comblés (recherche multicritère,
historisation par clause, circuit configurable, droits Loi 25, SEAO, guide de l'utilisateur). Seul le chiffrement au
repos relève de l'hébergement et non de l'application.

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
