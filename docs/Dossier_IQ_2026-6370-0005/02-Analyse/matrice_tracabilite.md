# Matrice de traçabilité exigences → stories → tests

| Exigence | Description synthétique | Story(s) | Vérification principale |
|---|---|---|---|
| F-01 | Dossiers, trousses, documents multiples | ST-03, ST-04 | Tests API, droits, E2E |
| F-02 | Génération de contrats depuis données source | ST-05, ST-06 | Tests fusion, données, comparaison PDF |
| F-03 | Modèles réutilisables et partagés | ST-05 | Tests droits, version modèle |
| F-04 | Champs et règles conditionnelles | ST-05, ST-06 | Tests validation et rendu |
| F-05 | Validation et approbation séquentielle/parallèle/mixte | ST-07, ST-08 | Tests transitions et concurrence |
| F-06 | Signature et gestion des signataires | ST-09 | Tests intégration, identité, callbacks |
| F-07 | Notifications et rappels | ST-11 | Tests planification et envoi |
| F-08 | Suivi centralisé | ST-13 | Tests filtres, visibilité, statut |
| F-09 | Versions, audit, preuves, export | ST-04, ST-10, ST-14 | Tests hash, immutabilité, export |
| F-10 | Salesforce et API | ST-06, ST-12 | Tests contrat, idempotence, reprise |
| F-11 | SSO, rôles, groupes, administration | ST-01, ST-02, ST-14 | Tests sécurité et séparation |
| F-12 | Web/mobile, expérience utilisateur | ST-03 à ST-13 | Tests responsive et utilisabilité |
| F-13 | IA / CLM optionnel | ST-15 | POC, sécurité, contrôle humain |
| NF-01 | Hébergement / résidence des données | ST-14 / DevOps | Preuves de localisation et audit fournisseur |
| NF-03 | Sécurité | ST-01, ST-02, ST-09, ST-14 | Tests sécurité, audit indépendant |
| NF-04 | Auditabilité | ST-08 à ST-10, ST-14 | Vérification chronologie et intégrité |
| NF-05 | Disponibilité / SLA | ST-14 | Supervision et rapports SLA |
| NF-06 | 55–65k documents/an | ST-04, ST-06, ST-09 | Test de charge |
| NF-07 | 25 Mo minimum | ST-04 | Test upload 25 Mo+ |
| NF-13 | Portabilité | ST-10 | Export / import de contrôle |

## Validation de l'implémentation (2026-11-03, mise à jour le 2026-11-08)

Détail par sous-exigence, marche à suivre depuis l'interface et tests de bout en bout par exigence :
[validation_exigences_fonctionnelles.md](validation_exigences_fonctionnelles.md).

État vérifié dans le code, l'interface et les tests du module CLM (`backend/…/clm`, `frontend/src/pages/clm`).
✅ implémentée et testée · ✅🔌 implémentée, recette sur le système réel d'IQ en attente · 🟡 partielle · ⏳ décision IQ.

| Exigence | Serveur (repères) | Interface | Tests | État | Reste |
|---|---|---|---|---|---|
| F-01 | `TrousseService`, `DocumentClmService` | UI-02 à UI-05 | `ClmIntegrationTest`, e2e `clm.view` | ✅ | — |
| F-02 | `SalesforceRest`, `Preremplissage`, `RenduDocuments` | UI-03, UI-10 | `ClmIntegrationTest`, `SalesforceRestTest` | ✅🔌 | Recette sur le bac à sable Salesforce d'IQ |
| F-03 | `ModeleService`, `Modele` (versions, partage, publication) | UI-07 à UI-09 | `ClmIntegrationTest`, `DomaineClmTest` | ✅ | — |
| F-04 | `Modele.Champ`, `ReglesClm`, saisie des valeurs, étapes conditionnelles, saisie assistée (`Suggestions`) | UI-09 à UI-11 | `ClmIntegrationTest`, `CasLimitesDomaineClmTest`, `SuggestionsSaisieIntegrationTest`, e2e `clm.exigences` | ✅ | — |
| F-05 | `Parcours`, `ParcoursService` (séquentiel, parallèle, mixte, quorum, délégation) | UI-11 à UI-14 | `ClmIntegrationTest`, `DomaineClmTest` | ✅ | — |
| F-06 | `PortailService`, `Identite`, port `FournisseurSignature` | UI-15, UI-16 | `ClmIntegrationTest`, e2e portail | 🟡 | Fournisseur certifié et horodatage qualifié (ST-09) |
| F-07 | `RappelService` (rappels, alerte avant échéance, retard), `MessagerieClm` | UI-17, UI-22 | `RappelServiceTest`, `ClmIntegrationTest`, e2e `clm.exigences` | ✅ | — |
| F-08 | `SuiviService` (périmètre, exceptions, export) | UI-01, UI-18 | `ClmIntegrationTest` | ✅ | — |
| F-09 | Versions, `ChaineScellee`, `PreuveService` | UI-06, UI-19, UI-23 | `ClmIntegrationTest`, `DomaineClmTest` | 🟡 | Conservation et destruction selon la politique d'IQ |
| F-10 | `SalesforceService` (boîte d'envoi, idempotence, reprises), API REST `/api/clm` | UI-21 | `ClmIntegrationTest`, `SalesforceServiceAlerteTest` | ✅🔌 | Recette Salesforce d'IQ |
| F-11 | OIDC/SAML/MFA, `Habilitations`, rôle `AUDITEUR`, garde de module (ADR-009) | UI-20, UI-22, UI-23, `/clm/admin/sso` | `ArchitectureClmTest`, `ArchitectureModulesTest`, `IsolationTenantIqTest` | 🟡 | IdP d'IQ (ST-01), domaines d'affaires, rôles d'administration distincts (journal des changements de comptes et de rôles : fait le 2026-11-08) |
| F-12 | Identité de l'organisation sur le portail (`IdentiteOrganisation`) | UI-01 à UI-24, portail à 44 px, français et anglais | e2e aux 4 largeurs, axe-core | ✅ | — |
| F-13 | `AnalyseService`, module `clm_ia` | UI-24 | `ClmIntegrationTest`, `DomaineClmTest` | ✅ | — |
| NF-01 | Terraform Azure (`infra/terraform/azure`, région Canada) | — | `terraform validate` | ⏳ | Québec ou Canada (RST-18) |
| NF-02 | API REST documentée (OpenAPI), ports et adaptateurs | — | Tests de contrat des ports | ✅ | — |
| NF-03 | Refus par défaut (`@PreAuthorize`, garde de module), session httpOnly, MFA, scellé HMAC, analyse antivirus (port) | — | Tests d'architecture et d'intégration | 🟡 | Destruction à échéance ; audit indépendant |
| NF-04 | Journal scellé par trousse et audit du tenant | UI-19, UI-23 | `ClmIntegrationTest` | ✅ | — |
| NF-05 | AKS, alertes d'exploitation (ST-14), runbook | — | `AdaptateursClmTest`, `terraform validate` | 🟡 | Mesure du SLA (RST-18) |
| NF-06 | Requêtes bornées au tenant | Pagination côté client | — | 🟡 | Pagination serveur et test de charge CLM (les plans JMeter de `perf/` couvrent le socle) |
| NF-07 | `CLM_TAILLE_MAX_OCTETS` = 25 Mo, format vérifié sur les octets | UI-05 | `ClmIntegrationTest` | ✅ | — |
| NF-08 | `app.clm.environnement` (DEV à PROD), paramètres en lecture seule | UI-22 | `ClmIntegrationTest` | ✅ | — |
| NF-09 | Plans de reprise (`infra/dr`) | — | — | 🟡 | RTO/RPO à convenir et exercer |
| NF-10 | Monolithe modulaire (ADR-006 à ADR-009), règles d'architecture et couverture vérifiées à la construction | — | `ArchitectureClmTest`, `ArchitectureModulesTest`, porte JaCoCo | ✅ | — |
| NF-11 | Idempotence, reprises, file d'erreur, corrélation | UI-21 | `ClmIntegrationTest` | ✅ | — |
| NF-12 | Interface en français, WCAG 2.2 AA | UI-01 à UI-24 | e2e axe-core aux 4 largeurs | ✅ | — |
| NF-13 | Archive de preuve ZIP, exports CSV du suivi et du journal | UI-18, UI-19, UI-23 | `ClmIntegrationTest` | ✅ | — |
| NF-14 | Consentement, preuve, courriels masqués dans les journaux (Loi 25) | UI-16 | `ClmIntegrationTest` | 🟡 | Attestations du fournisseur de signature et de l'hébergeur |

Isolation du tenant d'IQ (aucune fonctionnalité d'un autre module) : voir
[`11-Tenant-IQ/validation_isolation_tenant_iq.md`](../11-Tenant-IQ/validation_isolation_tenant_iq.md).

## Sources documentaires
- Cahier des besoins et exigences, sections 1.4.2 à 1.4.12, 2 à 6.
- Document d'appel d'offres, sections 3.1 à 3.10, 4, annexes 2, 9 et 10.
- Les identifiants F/NF et ST sont construits dans le présent dossier; ils ne remplacent pas la matrice officielle Excel Annexe 10.
