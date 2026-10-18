# Roadmap d'intégration — écarts Trois-Rivières (n° 7000017905)

> Suivi **vivant** de l'implémentation des écarts identifiés dans
> [02-COMPARAISON-ECARTS.md](02-COMPARAISON-ECARTS.md) selon la conception
> [03-CONCEPTION-INTEGRATION.md](03-CONCEPTION-INTEGRATION.md). Mis à jour **au fur et à mesure**.
> Principe : **zéro duplication de module** — on étend un contexte existant ou on ajoute un contexte
> disjoint. DDD, clean architecture (le domaine ne dépend d'aucun framework), clean code, SOLID.
>
> **Portes de qualité (bloquantes, chaque phase)** : tests unitaires ≥ 96 % (agrégé **et** par
> classe), intégration sur H2+Liquibase, e2e Playwright, accessibilité WCAG 2.2 AA / SGQRI 008 3.0,
> aucun chevauchement/débordement (390/768/1280/1680 px), JMeter conforme aux SLI/SLO/SLA.

## Légende d'état

⬜ À faire · 🟦 En cours · ✅ Terminé & testé · 🧪 Tests en place · ⏸ Bloqué

---

## Phases

| Phase | Écart(s) | État | Tests U | Tests I | E2E | JMeter |
|-------|----------|:----:|:------:|:-------:|:---:|:------:|
| **P1** — Dossier d'approvisionnement (agrégat + rattachement documents + vue + rapports) | 1, 11 | ✅ | ✅ | ✅ | ✅ | ✅ |
| **P2** — Assistant guidé + classement par type d'acquisition + recherche enrichie | 5, 7.1, 10 | ✅ | ✅ | ✅ | ✅ | ✅ |
| **P3** — Journal d'audit consultable/exportable + résolution commentaires + workflow configurable | 8, 7.5 | ✅ | ✅ | ✅ | ✅ | ✅ |
| **P4** — Intégrations M365 (SharePoint/OneDrive/Outlook) via port + adaptateurs | 2 (M365) | ✅ | ✅ | ✅ | ✅ | ✅ |
| **P5** — Connecteur SAP + notification Teams | 2 (SAP/Teams) | ✅ | ✅ | ✅ | ✅ | ✅ |
| **P6** — Import/migration (+ migration d'essai) + Réversibilité + traçabilité/désactivation IA | 3, 4, 9 | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## P1 — Dossier d'approvisionnement

**Objectif** : regrouper plusieurs documents d'un même dossier d'acquisition (art. 15), avec vue
dossier et rapports de suivi par dossier, **sans dupliquer** le `Document`.

**Conception (ADR-001)** : nouvel agrégat `Dossier` + `Document.dossierId` (FK nullable). Le
`Document` reste l'unique agrégat de rédaction ; le `Dossier` regroupe et agrège.

### Lot de livraison
- [x] Domaine : `Dossier` (entité + invariant de cycle de vie), `Enums.StatutDossier`, `Enums.TypeAcquisition`
- [x] Migration Liquibase `022-dossier.yaml` (table `dossier` + colonne `document.dossier_id` + index)
- [x] Repo : `DossierRepository` (isolation tenant)
- [x] Application : `DossierService` (créer, modifier, lister, get, statut, rattacher/détacher, rapport)
- [x] API : `DossierController` + DTO `DossierRequest` / `DossierStatutRequest` / `DossierRapport`
- [x] Rattachement : `Document.dossierId` + création directe dans un dossier + endpoints rattacher/détacher
- [x] Frontend : page `Dossiers.jsx` (liste, création, vue dossier, rapport, statut, rattachement) — WCAG 2.2 AA
- [x] Tests unitaires `DossierServiceTest` (17) + `DossierTest` (4) — **100 % lignes** sur les 3 classes
- [x] Tests d'intégration (parcours API dossier) dans `ApiIntegrationTest` — **69/69 verts**
- [x] E2E Playwright `dossiers.spec.ts` + POM `DossiersPage` + vue responsive — verts
- [x] JMeter : plan `contrat-dossiers-load.jmx` + `data/*.csv` + `run.sh` — **tous les SLO respectés**

### Résultats de qualité (P1)
- **Backend** : 69/69 tests verts. Couverture JaCoCo — `Dossier` 100 %, `DossierService` 100 %,
  `DossierController` 100 % lignes.
- **E2E** : `tests/dossiers.spec.ts` (parcours complet) + `views/responsive.view.spec.ts` (390 px) verts.
- **Perf** : lecture P95 ≈ 130 ms (SLO 800), écriture P95 ≈ 8 ms (SLO 1500), 0 % d'erreur → conforme
  (voir [../../perf/SLI-SLO-SLA.md](../../perf/SLI-SLO-SLA.md)).

### Journal
- 2026-09-16 — Démarrage P1, conception validée par le client.
- 2026-09-16 — **P1 livrée** : backend (domaine/service/API/migration) + frontend + tests U/I/E2E +
  plan de charge JMeter conforme aux SLO. Aucun module dupliqué (ADR-001 respecté).

---

## P2 — Assistant guidé + classement + recherche enrichie

**Objectif** : parcours guidé de génération (art. 7.2), classement des modèles par type
d'acquisition (art. 7.1) et recherche à facettes sur les contenus réutilisables (art. 7.4), **en
réutilisant** `RuleEngine`/`TemplateService`/`DocumentService` (ADR-004) — aucun nouveau moteur.

### Lot de livraison
- [x] Domaine : `DocumentTemplate.categorieAcquisition` (classement art. 7.1) + migration `023`
- [x] Application : `AssistantGuideService` (analyser/générer) au-dessus des services existants
- [x] API : `AssistantController` (`/analyser`, `/generer`) + DTO assistant/recherche
- [x] Recherche enrichie : `SearchController` étendu (facettes catégorie/type + templates)
- [x] Frontend : page `Assistant.jsx`, facettes+modèles dans `Search.jsx`, catégorie dans `TemplateManager`
- [x] Tests unitaires `AssistantGuideServiceTest` (7) + `SearchControllerTest` (6)
- [x] Tests d'intégration (assistant + recherche enrichie) dans `ApiIntegrationTest` — **84/84 verts**
- [x] E2E Playwright `assistant.spec.ts` + vue responsive — verts
- [x] JMeter : plan `contrat-assistant-load.jmx` + `data/contextes.csv` — **tous les SLO respectés**

### Résultats de qualité (P2)
- **Backend** : 84/84 tests verts. Couverture — `AssistantGuideService` **100 % lignes / 100 % branches**,
  `SearchController` **100 % / 100 %**, `DocumentTemplate` **100 %**, `AssistantController` **100 % lignes**.
- **E2E** : `tests/assistant.spec.ts` + vue responsive 390 px verts.
- **Perf** : lecture P95 ≈ 102 ms (SLO 800), écriture P95 ≈ 25 ms (SLO 1500), 0 % d'erreur → conforme.

### Journal
- 2026-09-16 — **P2 livrée** : assistant guidé (réutilise RuleEngine/TemplateService, ADR-004),
  classement par type d'acquisition, recherche à facettes. Tests U 100 % lignes/branches, e2e + JMeter verts.

---

## P3 — Journal d'audit + résolution commentaires + workflow configurable

**Objectif** : rendre le journal d'audit consultable/exportable (art. 18, ADR-006), permettre la
résolution des commentaires et les fils de discussion (art. 7.5), et rendre le circuit
d'approbation **configurable par type de document** (art. 7.5, ADR-005) — en réutilisant `AuditLog`,
`Comment` et `ApprovalStep` sans les dupliquer.

### Lot de livraison
- [x] Audit : `AuditLog.tenantId` (isolation), `AuditLogRepository.rechercher` (filtres),
  `AuditQueryService` (read model + export CSV), `AuditController` (`/api/audit`, `/export`), migration `024`
- [x] Commentaires : `Comment.resoluPar`/`parentId`, `DocumentService.resoudreCommentaire`, endpoint résolution
- [x] Workflow : `ApprovalWorkflow` (+ étapes ordonnées), `ApprovalWorkflowService`/`Controller`,
  résolution du circuit dans `DocumentService.demarrerApprobation` (gabarit configuré, sinon défaut)
- [x] Frontend : pages `Audit.jsx` et `Workflows.jsx`, résolution des commentaires dans `DocumentEditor`
- [x] Tests unitaires : `AuditQueryServiceTest` (5), `ApprovalWorkflowServiceTest` (10),
  `CommentTest` (1) — **101/101 verts**
- [x] Tests d'intégration : workflow configurable, fil+résolution commentaires, audit consultable/exportable + RBAC
- [x] E2E Playwright `gouvernance.spec.ts` + vues responsive — verts
- [x] JMeter : plan `contrat-gouvernance-load.jmx` + `data/admins.csv` — **tous les SLO respectés**

### Résultats de qualité (P3)
- **Backend** : 101/101 tests verts. Couverture **100 % lignes** sur `AuditQueryService`,
  `AuditController`, `ApprovalWorkflowService`, `ApprovalWorkflowController`, `AuditLog`,
  `ApprovalWorkflow`, `Comment` (services `AuditQueryService`/`ApprovalWorkflowService` aussi 100 % branches).
- **E2E** : `tests/gouvernance.spec.ts` + vues responsive 390 px verts.
- **Perf** : lecture P95 ≈ 97 ms (SLO 800), écriture P95 ≈ 7 ms (SLO 1500), 0 % d'erreur → conforme.

### Journal
- 2026-09-16 — **P3 livrée** : journal d'audit consultable/exportable (ADR-006), résolution des
  commentaires + fils, circuit d'approbation configurable (ADR-005). Aucun module dupliqué.

---

## P4 — Intégrations Microsoft 365 / SAP (port + adaptateurs)

**Objectif** : publier un document vers SharePoint, OneDrive, Outlook, Teams ou SAP (art. 9), via
un **port** `PublicationCiblePort` et des **adaptateurs** d'infrastructure (ADR-002, DIP/OCP). Le
rendu est **réutilisé** (extraction de `DocumentRenderingService`, partagé avec l'export) — aucune
duplication du pipeline.

### Lot de livraison
- [x] Refactor sans duplication : `DocumentRenderingService` extrait, `ExportController` y délègue
- [x] Port `PublicationCiblePort` (domaine/application) + `SimulationPublicationAdapter` (repli, OCP)
- [x] `PublicationLog` (traçabilité) + `PublicationLogRepository` + migration `025`
- [x] `PublicationService` (rendu → routage adaptateur → journal → audit) + `PublicationController`
- [x] Frontend : panneau « Publication (M365/SAP) » + historique dans `DocumentEditor`
- [x] Tests unitaires `PublicationServiceTest` (4), `SimulationPublicationAdapterTest` (2),
  `PublicationLogTest` (1) — **109/109 verts**
- [x] Tests d'intégration (publication PDF/DOCX + historique + branches gabarit) dans `ApiIntegrationTest`
- [x] E2E Playwright `publication.spec.ts` — vert
- [x] JMeter : plan `contrat-publication-load.jmx` — **tous les SLO respectés**

### Résultats de qualité (P4)
- **Backend** : 109/109 verts. Couverture **100 % lignes** sur `PublicationService`,
  `PublicationController`, `SimulationPublicationAdapter`, `DocumentRenderingService`, `ExportController`,
  `PublicationLog` (`PublicationService`/`DocumentRenderingService` aussi **100 % branches**).
- **E2E** : `tests/publication.spec.ts` vert.
- **Perf** : lecture P95 ≈ 97 ms (SLO 800), écriture P95 ≈ 21 ms (SLO 1500, rendu PDF + dépôt inclus),
  0 % d'erreur → conforme.

> **Note d'intégration réelle** : la connexion vive à MS Graph / SAP se branche via le **même port**
> (un adaptateur prioritaire, config-driven avec Entra ID) sans toucher au code appelant ; elle
> requiert un tenant M365/SAP et ses secrets, hors périmètre testable localement. L'adaptateur de
> simulation assure le repli et la traçabilité en attendant.

### Journal
- 2026-09-16 — **P4 livrée** : publication externe via port/adaptateurs (ADR-002), rendu mutualisé,
  journal de publication. Adaptateur de simulation + seam pour Graph/SAP. Tests U/I/E2E + JMeter verts.

---

## P5 — Connecteur SAP + notification Teams

**Objectif** : transmettre la **référence financière** d'un dossier à SAP (n° AO, objet, nombre de
documents, montant total agrégé — art. 9) et **notifier Teams**. Réutilise le contexte dossier et
le **même port de publication** que P4 (Teams = cible existante) — aucune duplication.

### Lot de livraison
- [x] Port `ReferenceFinancierePort` + `SimulationSapAdapter` (repli config-driven, ADR-002)
- [x] `SapReferenceService` (agrège la valeur estimée des documents du dossier) + `SapReferenceController`
- [x] Teams : cible `TEAMS` du `PublicationCiblePort` de P4 (aucun code nouveau) — testée
- [x] Frontend : bouton « Transmettre la référence à SAP » sur le détail dossier (`Dossiers.jsx`)
- [x] Tests unitaires `SapReferenceServiceTest` (2), `SimulationSapAdapterTest` (3) — **115/115 verts**
- [x] Tests d'intégration (référence SAP agrégée + publication Teams) dans `ApiIntegrationTest`
- [x] E2E Playwright `sap.spec.ts` — vert
- [x] JMeter : plan `contrat-sap-load.jmx` — **tous les SLO respectés**

### Résultats de qualité (P5)
- **Backend** : 115/115 verts. Couverture **100 % lignes** sur `SapReferenceService`,
  `SapReferenceController`, `SimulationSapAdapter` (service/adaptateur **100 % branches**).
- **Perf** : lecture P95 ≈ 131 ms (SLO 800), écriture P95 ≈ 10 ms (SLO 1500), 0 % d'erreur → conforme.

> **Note** : SAP et Teams passent par les mêmes ports/adaptateurs que P4 (ADR-002). Le connecteur
> SAP réel (RFC/OData) et l'envoi Teams réel (Graph) se branchent sur ces ports avec les secrets
> du tenant, sans toucher au code appelant.

### Journal
- 2026-09-16 — **P5 livrée** : référence financière SAP au niveau dossier + notification Teams via
  le port de publication. Réutilisation totale (dossier + port P4), zéro duplication.

---

## P6 — Import/migration + Réversibilité + encadrement IA

**Objectif** : import de contenus avec **migration d'essai** (art. 22), **réversibilité** par export
complet du tenant en formats ouverts (art. 20, ADR-003), et **traçabilité + désactivation** de l'IA
(art. 21). Réutilise `ClauseService`, les dépôts existants et le port d'assistance IA.

### Lot de livraison
- [x] Import : `ImportService` (analyseur CSV RFC 4180, validation, essai vs réel) + `ImportController` (text/csv)
- [x] Réversibilité : `ExportTenantService` (ZIP JSON + manifeste, réutilise tous les dépôts) + `ReversibiliteController`
- [x] IA : `Tenant.iaActivee` + `AiSuggestion` + `AiTraceabiliteService` (activation + traçabilité),
  câblés dans `AiController` (refus si désactivée, journalisation) + endpoints paramètres/suggestions ; migration `026`
- [x] Frontend : page `DonneesIA.jsx` (import/essai, export réversibilité, bascule IA + journal des suggestions)
- [x] Tests unitaires : `ImportServiceTest` (7), `ExportTenantServiceTest` (2), `AiTraceabiliteServiceTest` (11),
  `AiSuggestionTest` (2) + `AiControllerSuccessTest` (2, port simulé) — **142/142 verts**
- [x] Tests d'intégration : import essai/réel + RBAC, export ZIP + RBAC, IA traçabilité + désactivation (409)
- [x] E2E Playwright `donnees-ia.spec.ts` — verts
- [x] JMeter : plan `contrat-donnees-load.jmx` — **tous les SLO respectés**

### Résultats de qualité (P6)
- **Backend** : 142/142 verts. Couverture **100 % lignes** sur `ImportService` (+ `CsvParser`),
  `ExportTenantService`, `AiTraceabiliteService`, `ImportController`, `ReversibiliteController`,
  `AiSuggestion` ; `AiController` 97 % lignes (branches RAG hors-Ollama).
- **Perf** : lecture P95 ≈ 105 ms (SLO 800, export ZIP inclus), écriture P95 ≈ 6 ms (SLO 1500),
  0 % d'erreur → conforme.

### Journal
- 2026-09-16 — **P6 livrée** : import + migration d'essai, réversibilité (export ZIP formats ouverts,
  ADR-003), encadrement IA (désactivation par tenant + traçabilité des suggestions). Zéro duplication.

---

## Bilan — intégration Trois-Rivières terminée (P1 → P6)

Les **6 phases** sont livrées, testées et conformes aux portes de qualité. **Aucun module dupliqué** :
chaque écart a étendu un contexte existant ou ajouté un contexte disjoint via un port (ADR-001 à 007).

| Indicateur | Résultat |
|-----------|----------|
| Tests backend (unitaires + intégration) | **142/142 verts** |
| Couverture des classes ajoutées | **100 % lignes** (sauf `AiController` 97 %, dépendance Ollama) |
| E2E Playwright ajoutés | dossiers, assistant, gouvernance, publication, sap, données-ia + vues responsive |
| Plans de charge JMeter | 6 plans, **tous les SLO respectés** (données de test en CSV) |
| Migrations Liquibase | `022` → `026` (additives) |

**Reste hors périmètre logiciel** (contractuel/exploitation) : SLA de disponibilité 99,99 %,
DR/RTO/RPO, certifications ISO/SOC, gestion des vulnérabilités (Annexe A) — traités au déploiement
Azure et en CI (Trivy). Les connecteurs **réels** M365/SAP se branchent sur les ports existants
(ADR-002) avec les secrets du tenant, sans modification du code appelant.

---

## Addenda 1 — Tableau de conformité (37 exigences)

Analyse : [04-ANALYSE-ADDENDA-1.md](04-ANALYSE-ADDENDA-1.md) · Conception + ADR :
[05-CONCEPTION-ADDENDA-1.md](05-CONCEPTION-ADDENDA-1.md) · Source :
[addenda-1.md](addenda-1.md). La grande majorité des 37 exigences est **déjà couverte** par P1→P6 ;
3 écarts concrets ont été implémentés (extensions, aucun module dupliqué).

| Phase | Écart | Exig. | État | Détail |
|-------|-------|:-----:|:----:|--------|
| **A1** | Import de modèles **Word (.docx)** | 3, 16 | ✅ | `WordTemplateParser` (POI) + `ImportService.analyserWord/importerWord` + `POST /api/import/word` + UI (Données & IA) |
| **A2** | Journal d'**accès** complet | 9 | ✅ | audit `DECONNEXION` + `CONNEXION_ECHEC` (scopés au tenant du compte) |
| **A3** | **Rapports de suivi** consolidés | 22 | ✅ | `RapportService` (read-model) + `GET /api/rapports/suivi` + écran `Rapports.jsx` |

### Résultats de qualité (Addenda 1)
- **Backend** : 157/157 tests verts. Couverture **100 % lignes** sur `RapportService`,
  `RapportController`, `ImportController`, `AuditService` ; `WordTemplateParser` 97 % lignes ;
  `ImportService` 100 % lignes.
- **E2E** : `tests/addenda.spec.ts` (import Word essai+réel, rapports de suivi) verts, plus
  couverture d'intégration (journal d'accès, RBAC des rapports).
- **ADR** : ADR-008 (import Word via parseur POI en infrastructure), ADR-009 (journal d'accès sans
  nouveau chemin d'écriture), ADR-010 (rapports en read-model).

> Recherches : Apache POI `XWPFDocument.getParagraphs()/getTables()` pour l'extraction Word ;
> composants d'un tableau de bord CLM (portefeuille + éléments d'action) pour les rapports de suivi.

### Conformité au tableau (synthèse)
- **Obligatoires (1-19)** : couverts ; 3/16 (Word) et 9 (accès) renforcés ci-dessus ; 1/8 relèvent
  du déploiement (Canada, chiffrement au repos) ; 17 (coûts) est narratif.
- **Souhaitables (20-27)** : couverts ; 22 (rapports) implémenté.
- **Informations requises (28-37)** : narratif de soumission (réponse à l'AO).
