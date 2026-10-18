# Analyse de l'Addenda n° 1 — conformité de la solution VINA

> Confronte les **37 exigences** du Tableau de conformité (Section III de
> [addenda-1.md](addenda-1.md)) à la solution VINA telle que livrée (P1→P6, voir
> [ROADMAP-INTEGRATION.md](ROADMAP-INTEGRATION.md)). Légende : ✅ **Couvert** ·
> 🟡 **À compléter (implémentation)** · ⚙️ **Config/déploiement** · 📝 **Narratif de soumission**.

## 1. Exigences obligatoires (1-19)

| N° | Sujet | Statut | Où / action |
|----|-------|:------:|-------------|
| 1 | Hébergement Canada | ⚙️ | Azure régions canadiennes / OVH — [deploiement-azure.md](../deploiement-azure.md) |
| 2 | Word modifiable | ✅ | `DocxContractRenderer` (POI) — export `.docx` |
| 3 | Modèles Word existants (adapter/**migrer**) | 🟡 | Import CSV existe (P6) ; **manque l'import d'un `.docx` Word** → **GAP A** |
| 4 | Microsoft 365 | ✅ | Publication SharePoint/OneDrive/Outlook/Teams (P4) |
| 5 | Authentification sécurisée + corporative | ✅ | JWT httpOnly + **SSO Entra ID** |
| 6 | MFA | ✅ | TOTP + questions |
| 7 | Rôles et permissions | ✅ | RBAC (REDACTEUR/APPRO/JURIDIQUE/DIRECTION/ADMIN/SUPERADMIN) |
| 8 | Chiffrement repos + transit | ⚙️ | TLS (transit) + chiffrement au repos Azure/OVH |
| 9 | Journalisation accès + activité | 🟡 | Activité + `LOGIN` audités ; **manque LOGOUT + échecs de connexion** → **GAP B** |
| 10 | Conformité municipale (LCOM) configurable | ✅ | `RuleEngine` + valeurs de référence |
| 11 | Traçabilité contractuelle | ✅ | Versions + approbation + journal d'audit |
| 12 | Conservation documentaire | ✅ | Persistance docs/versions/journaux |
| 13 | Séparation des responsabilités | ✅ | RBAC + circuits d'approbation |
| 14 | Pas d'entraînement IA externe | ✅ | IA **locale** (Ollama), rien ne sort |
| 15 | Validation humaine des contenus IA | ✅ | Suggestions tracées, validation humaine |
| 16 | Méthode de migration détaillée | 🟡 | Migration d'essai CSV (P6) ; **compléter par import Word** → **GAP A** + doc méthode |
| 17 | Coûts détaillés | 📝 | Bordereau de soumission |
| 18 | Langue française | ✅ | Interface + doc FR |
| 19 | Fin de contrat : export complet | ✅ | Réversibilité (export ZIP formats ouverts, P6) |

## 2. Exigences souhaitables (20-27)

| N° | Sujet | Statut | Où / action |
|----|-------|:------:|-------------|
| 20 | Intégrations bureautiques M365 | ✅ | P4 |
| 21 | Dossier d'approvisionnement | ✅ | P1 |
| 22 | Rapports de suivi (docs, validations, users, dossiers) | 🟡 | Stats globales + rapport par dossier ; **manque un tableau de bord de suivi consolidé** → **GAP C** |
| 23 | Étapes de validation selon type | ✅ | Circuits configurables (P3) |
| 24 | Désactivation de l'IA | ✅ | P6 (interrupteur par tenant) |
| 25 | Références | 📝 | Réponse à l'AO (Lévis, INSPQ, Revenu Québec…) |
| 26 | Environnement d'essai / pilote | ✅ / 📝 | Démo en ligne + migration d'essai |
| 27 | Évolutions prévues | 📝 | Feuille de route |

## 3. Informations requises (28-37) — narratif de soumission

Éléments **à décrire** (pas des fonctionnalités logicielles), déjà connus de notre solution :

- **28 Type** : infonuagique (SaaS) · **29 Connecteurs/API** : REST + OpenAPI, ports/adaptateurs M365/SAP ·
  **30 Incidents** : procédure de notification/suivi (ops) · **31 Sous-traitants** : Microsoft Azure (hébergeur) ·
  **32 IA** : Ollama local (RAG, rédaction) · **33 Volume inclus** : gabarits + base de clauses semés ·
  **34 Soutien** : délais par priorité (contrat) · **35 Formation** : utilisateurs + administrateurs ·
  **36 Frais de sortie** : néant (réversibilité incluse) · **37 Limites** : IA CPU (latence), etc.

Ces points alimentent la **réponse à l'appel d'offres** ([REPONSE-APPEL-OFFRES.md](REPONSE-APPEL-OFFRES.md)),
pas le code.

## 4. Écarts logiciels à implémenter (issus de l'addenda)

Trois écarts concrets, tous **extensions** de contextes existants (aucun module dupliqué) :

- **GAP A — Import/migration de modèles Word (`.docx`)** (exig. 3, 16). Extraire les paragraphes/sections
  d'un document Word déposé pour créer un **modèle** (template) et ses **clauses**. Réutilise Apache POI
  (déjà présent, `XWPFDocument`) et le `TemplateService`/`ClauseService`.
- **GAP B — Journalisation complète des accès** (exig. 9). Ajouter l'audit de **LOGOUT** et des
  **échecs de connexion** (`CONNEXION_ECHEC`) au journal existant (`AuditService`).
- **GAP C — Tableau de bord de suivi (rapports consolidés)** (exig. 22). Endpoint + écran agrégeant :
  documents par statut/type, approbations en attente, dossiers actifs, obligations à venir, activité
  par utilisateur (source : audit). Inspiré des tableaux de bord CLM (portfolio + action items).

La conception détaillée et les ADR sont dans
[05-CONCEPTION-ADDENDA-1.md](05-CONCEPTION-ADDENDA-1.md).

## 5. Couverture globale

- **Obligatoires** : 15/19 déjà couverts, 2 à compléter (3, 16 → GAP A ; 9 → GAP B), 2 config/déploiement (1, 8), 1 narratif (17).
- **Souhaitables** : 6/8 couverts, 1 à compléter (22 → GAP C), le reste narratif.
- **Informations requises** : narratif de soumission.

> Aucun écart n'impose de refonte : la solution répond déjà à la grande majorité du tableau de
> conformité ; les 3 GAP ci-dessus renforcent la conformité (migration Word, journal d'accès complet,
> rapports de suivi).
