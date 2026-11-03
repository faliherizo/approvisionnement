# Opportunité Santé Québec — CHU de Québec–Université Laval

**Besoin** : plateforme infonuagique de **création de formulaires électroniques, d'applications Web
configurables et de gestion centralisée des données** (portails RH et approvisionnements). Contrat
12 mois + 2 × 12 mois d'option.

Ce dossier **planifie l'intégration** de ces besoins dans notre solution `contractuelle_contrat`
(Java/Spring + React), en **réutilisant au maximum** les actifs existants et les **conceptions** du
projet `freelance-invoicing`.

## Principes directeurs (non négociables)

1. **Tout configurable, rien de figé.** Le cœur est un **moteur de formulaires piloté par métadonnées** :
   formulaires, champs, types, règles, validations, classifications sont stockés **comme données de
   référence** (schéma JSON/JSONB), **jamais** comme colonnes métier en dur. Ajouter un champ = une
   configuration, pas une migration de schéma.
2. **Modules activables/désactivables depuis l'espace superadmin.** Chaque capacité (Form Builder,
   Règles & Automatisation, Notifications, Rapports/BI, API & Interopérabilité, Sécurité avancée) est un
   **module à bascule par tenant**, via un **registre de modules avec résolution de dépendances**.
3. **Réutiliser > réécrire.** Socle Java existant d'abord ; conceptions `freelance-invoicing` portées
   ensuite ; construction neuve en dernier recours.

## État des lieux (synthèse des deux inventaires)

| Brique | `contractuelle_contrat` (Java, cible) | `freelance-invoicing` (TS, source de conception) |
|---|---|---|
| Multi-tenant + isolation | ✅ `TenantContext` | ✅ `saas/` |
| SSO SAML + OIDC, 2FA | ✅ (OIDC/SAML, TOTP) | ✅ (+ WebAuthn/passkeys) |
| Audit Loi 25 | ✅ `AuditLog` | ✅ **chaîné SHA-256** (à porter) |
| Rendu PDF/DOCX depuis données | ✅ `DocumentRenderer` | ✅ pdfkit |
| Workflows d'approbation + versioning | ✅ `ApprovalWorkflow`, `DocumentVersion` | ✅ `flux/` (chaînes datées, historique) |
| Données de référence configurables | ✅ `ReferenceValue` | ✅ `customFields.ts` (EAV) |
| **Moteur de formulaires (champs dynamiques)** | ❌ **à construire** | ✅ `flux/formulaires.ts` (**concept à porter**) |
| **Registre de modules / feature flags par tenant** | ❌ (seul `iaActivee`) | ✅ `saas/catalogueModules.ts` (**à porter**) |
| Notifications email/SMS/interne | ❌ | ✅ `notifications/` (ACS, Twilio, Telnyx) |
| RBAC fin (champ/fiche/bouton) + classification | ❌ (rôles grossiers) | ✅ `accessPolicy`/`tenantRoles`/`scope` |
| WAF / anti-brute-force / rate-limit | ❌ | ✅ `securite/pareFeu.ts` |
| Rapports/BI, export Excel, tableaux de bord | ❌ (CSV seulement) | ✅ `bi/`, `export/` |
| API publique tokenisée (OAuth/clé) | ❌ | ❌ (**à construire des deux côtés**) |

## Documents du dossier

| Fichier | Contenu |
|---|---|
| [`01-ANALYSE-BESOIN-ET-REUTILISATION.md`](01-ANALYSE-BESOIN-ET-REUTILISATION.md) | Les 9 blocs du besoin CHU → **Réutiliser / Porter / Construire**, exigence par exigence |
| [`02-ROADMAP.md`](02-ROADMAP.md) | Feuille de route phasée (6 phases), jalons, dépendances |
| [`03-BACKLOG-STORIES.md`](03-BACKLOG-STORIES.md) | Épopées → user stories avec critères d'acceptation |
| [`04-ARCHITECTURE-MODULES.md`](04-ARCHITECTURE-MODULES.md) | Nouveaux contextes/modules, registre de modules (superadmin), principe piloté par métadonnées |
| [`05-MCD-MOTEUR-FORMULAIRES.md`](05-MCD-MOTEUR-FORMULAIRES.md) | Modèle conceptuel de données (diagramme ER Mermaid) du moteur de formulaires |
| [`06-DIAGRAMMES-SEQUENCE.md`](06-DIAGRAMMES-SEQUENCE.md) | Diagrammes de séquence Mermaid des flux clés |

> **Note d'implémentation.** `freelance-invoicing` étant en **TypeScript/Node**, le code n'est pas
> copiable tel quel dans notre backend **Java/Spring** : on en porte les **modèles et algorithmes**
> (schéma de formulaire, calcul avec détection de cycle, catalogue de modules, pare-feu applicatif).
> Un portage Java partiel existe déjà là-bas (`backend-java/`) — à exploiter comme référence.