# Écarts fonctionnels — notre solution vs exigences IQ 2026-6370-0005

> Compare la solution **`contractuelle_contrat`** (telle qu'implémentée) aux exigences consolidées
> `analyse_besoins_exigences.md` (F-01→F-13, NF-01→NF-14) et aux 15 stories (ST-01→ST-15).
> Légende : ✅ couvert · 🟡 partiel (adaptation requise) · ❌ absent (à construire).
>
> **Validation des sources** : `Cahier_besoins_exigences.md` et `Document_appel_offres.md` (versions .md
> des deux PDF SEAO) ont été relus — contenu cohérent, non tronqué (Salesforce Grantmaking, signature
> électronique, 25 Mo, 99,5 %, résidence Québec/Canada présents) ; l'analyse existante les reflète
> fidèlement. **Aucune mise à jour requise.** Les PDF Annexe 10 (Excel) et le bordereau natif restent à
> obtenir (cf. `ecarts_entre_documents.md` §6).

## 1. Positionnement

Notre produit est un **atelier de rédaction contractuelle multi-tenant** (modèles, clauses, gabarits,
génération PDF, circuits d'approbation, signature de base, audit, publication M365/SAP, SSO OIDC/SAML).
Il **recoupe bien** le socle IQ (identité, modèles, workflow, suivi, audit) mais présente **deux écarts
majeurs structurants** : l'**intégration Salesforce Grantmaking** (système maître IQ) et la **signature
électronique à valeur probante** (fournisseur certifié + dossier de preuve), qui sont le cœur du besoin.

## 2. Écarts par exigence fonctionnelle

| Exig. | Statut | Ce que nous avons (code) | Écart à combler | Story |
|---|---|---|---|---|
| **F-01** Trousses (docs PDF/Word, 25 Mo, métadonnées, état/historique) | 🟡 | contexte `dossier` (Dossier) + `redaction` (Document, versions) + audit | notion de **trousse de signature** (multi-docs + participants + paramètres) ; **import Word (.docx)** ; **téléversement 25 Mo** (limite actuelle 10 Mo, `application.yml`) | ST-03, ST-04 |
| **F-02** Génération depuis Salesforce + préremplissage + clauses conditionnelles | 🟡→❌ | `Template`/`Gabarit`/`ModeleDocument`/clauses + génération **PDF** (chromium) | **récupération des données Salesforce** (absent) ; **préremplissage** depuis source ; **clauses conditionnelles** dans les modèles de contrat (notre `MoteurRegles` est côté formulaires, à réutiliser) | ST-05, ST-06 |
| **F-03** Modèles (réutilisables, partagés par groupes, champs positionnés, versions) | 🟡 | `TemplateManager`, `GabaritManager`, `ModeleDocumentManager`, catégories | **positionnement précis des champs dans le document** (placement visuel) ; **cycle de vie/versions des modèles** ; partage par groupes à formaliser | ST-05 |
| **F-04** Champs (types, distinction formulaire/saisie/signature, assignés à signataires, règles, saisie assistée) | 🟡 | moteur de champs du socle formulaires (16 types, règles, calcul) | **champs ancrés dans un document contractuel** et **assignés à un signataire** (zones de signature/paraphe) ; saisie assistée | ST-05, ST-09 |
| **F-05** Validation/approbation (séquentiel/parallèle/mixte, réassignation en cours, commentaires, historique) | 🟡 | `approbation` (ApprovalWorkflow) + `processus` (Workflow/Tache/Decision) + **`Approbations` multi-niveaux** porté | **parallèle + mixte** (quorum) ; **ajout/retrait/réassignation d'intervenant en cours sans redémarrage** ; commentaires/historique à unifier | ST-07, ST-08 |
| **F-06** Signature électronique (séq/parallèle/mixte, groupes, auth par risque, consentement/horodatage/preuve, certificat) | ❌→🟡 | `SignaturePad`, signature publique par jeton (`/signature/:jeton`), image de signature, audit | **fournisseur de signature à valeur probante** (ConsignO/Notarius/DocuSign…) ; **vérification d'identité par niveau de risque** ; **consentement + horodatage qualifié + certificat** | ST-09 |
| **F-07** Notifications et rappels (activer/désactiver, rappels auto, copies, messages/échéances) | 🟡 | contexte `notification` (courriel/SMS/interne, modèles, règles, journal) | **rappels automatiques sur non-réponse** + **échéances configurables** + destinataires en copie | ST-11 |
| **F-08** Suivi centralisé (dossiers/trousses/signatures, étapes/en attente/exceptions, filtres par périmètre) | 🟡 | `Stats`, `Rapports`, tableaux de bord de dossier | **vue de suivi CLM dédiée** (trousses + signatures + exceptions) filtrée par périmètre | ST-13 |
| **F-09** Gestion doc/versions/preuve (PDF+Word, versions, journalisation, export signé+certificat+audit+métadonnées, portabilité) | 🟡 | versions de document, `AuditLog`, export PDF | **dossier de preuve exportable** (document signé + certificat + journal + métadonnées) ; **Word** ; export unifié durable | ST-10 |
| **F-10** Intégration Salesforce et systèmes IQ (API/connecteurs, récup/retour, sécurité/reprises, corrélation) | ❌ | `integration` (M365 Graph, SAP) + `interop` (API publique OAuth2) — **patron ports/adaptateurs** réutilisable | **connecteur Salesforce (Grantmaking / Public Sector)** : récupération données + retour statuts/références + idempotence + reprises + corrélation id | ST-06, ST-12 |
| **F-11** Administration et contrôle d'accès (SSO, auth int/ext, renforcée, rôles/groupes/unités/domaines, séparation) | ✅🟡 | **OIDC/SAML per-tenant**, MFA TOTP/question, rôles, `securite` (MoteurAcces, classification), multi-tenant | **authentification du signataire externe par niveau de risque** (courriel/SMS/question/forte) à formaliser | ST-01, ST-02 |
| **F-12** Mobilité et expérience (web+mobile, navigation, branding, moins d'étapes) | 🟡 | responsive, **thème par tenant** (logo/couleurs), i18n FR/EN | **parcours mobile/tablette du signataire** à valider ; réduction d'étapes | — |
| **F-13** IA / automatisation (option : extraction, classification, anomalies, aide) | 🟡 | assistant **Ollama** (ChatIA), RAG (knowledge), DonneesIA | extraction de métadonnées / classification / détection d'anomalies **appliquées aux contrats** ; contrôles humains et conditions | ST-15 |

## 3. Écarts non fonctionnels

| ID | Statut | Écart |
|---|---|---|
| NF-01 hébergement Québec/Canada | 🟡 | infra Azure/AWS documentée ; **confirmer la clause prévalente** (Cahier Québec vs DAO Canada) et la région retenue |
| NF-05 disponibilité 99,5 % 24/7 | 🟡 | plans DR/RPO-RTO existants ; **grille SLA/priorités définitive** à aligner (deux grilles divergentes, cf. ecarts_entre_documents §2) |
| NF-06 volume 55–65k docs/an, ~1300 usagers | 🟡 | à valider par tests de charge (harnais JMeter présent) |
| NF-07 téléversement **25 Mo** | ❌ | limite actuelle **10 Mo** (`spring.servlet.multipart` + `DocumentService`) → porter à 25 Mo |
| NF-08 environnements DEV/UAT/PROD distincts | ✅ | `Application.environnement` (DEV/UAT/PROD) + compose ; environnements additionnels selon contrat |
| NF-04 piste d'audit intègre/horodatée | ✅🟡 | `AuditLog` (Loi 25) présent ; **lier l'audit à trousse/workflow/signature** et garantir l'intégrité (scellé) |
| NF-13 portabilité (export preuves/journaux/métadonnées) | 🟡 | exports partiels ; **export intégral structuré du dossier de preuve** à formaliser |
| NF-14 conformité (signature électronique, RP, archives) | 🟡 | dépend du fournisseur de signature retenu + attestations |

## 4. Synthèse — réutilisable vs à construire

**Fort réemploi (socle déjà en place)** : ST-01 (SSO/sessions), ST-02 (rôles/groupes/périmètres — `securite`),
ST-11 (notifications), ST-14 (admin/audit/exploitation), base de ST-05 (modèles/champs) et ST-07/ST-08
(workflow d'approbation, enrichi par le moteur `Approbations` multi-niveaux porté).

**À construire / adapter en priorité (écarts majeurs)** :
1. **ST-06 + ST-12 — connecteur Salesforce Grantmaking** (récupération données + préremplissage + retour
   statuts). *Net-new.* Dépend de l'accès sandbox Salesforce (cf. roadmap §Dépendances).
2. **ST-09 — signature électronique à valeur probante** (adaptateur fournisseur certifié + vérification
   d'identité par risque + horodatage). *Net-new* (notre pad/jeton est une base d'orchestration, pas une
   valeur probante). Dépend de la décision « moteur de signature » (ADR-ST-09).
3. **ST-10 — dossier de preuve** (certificat + journal + métadonnées, export durable) + **Word**.
4. **ST-05/ST-04 — champs positionnés dans le document + assignation aux signataires + import Word + 25 Mo.**
5. **ST-07/ST-08 — parallèle/mixte + réassignation en cours.**

**Optionnel / à clarifier** : ST-15 (CLM avancé + IA) — tarifé séparément au bordereau ; traiter comme
option (cf. `analyse_besoins_exigences §7`).

> Le détail des stories restantes à implémenter et leur séquencement sont dans
> `06-Roadmap/reste_stories_et_roadmap.md`.
