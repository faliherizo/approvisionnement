# Reste des stories à traiter + roadmap de réalisation

> Backlog d'**implémentation** dérivé de `02-Analyse/ecarts_solution_vs_exigences.md`, **relatif aux 15
> stories analysées** (ST-01→ST-15, qui portent déjà leur analyse fonctionnelle + ADR). Ici on ne
> ré-analyse pas : on énonce, pour chaque story, **le travail qui RESTE** compte tenu de ce que
> `contractuelle_contrat` fournit déjà, plus les stories **net-new** nécessaires. Effort = T-shirt
> indicatif (S/M/L/XL), à confirmer en ateliers (P0/P1). Priorités MoSCoW du DAO conservées.

## 00. État après les addenda 1 à 4 — à lire en premier (mis à jour le 2026-11-19)

Les quatre addenda d'IQ ont fait réaliser les stories RST-20 à RST-36, et quatre stories restent proposées. Le tableau
du § 0 garde l'état des stories d'origine ; les lignes ci-dessous le complètent. Dépôt de la soumission :
**4 décembre 2026, 11 h**.

| Story | Origine | État | Repères |
|---|---|---|---|
| RST-20 « Retournée » transmise lors d'un retour d'étape | Add. 2 Q14 | ✅ | `ParcoursService.retournerAEtape` |
| RST-21 Correspondance des statuts Salesforce | Add. 2 Q14 | ✅ | liste `CLM_STATUT_SALESFORCE` |
| RST-22 Lecture Salesforce limitée aux champs nécessaires | Add. 2 Q15 | ✅ | `?fields=`, ADR-016 |
| RST-23 Authentification Salesforce JWT Bearer | Add. 2 Q11 | ✅🔌 | `AssertionJwt`, ADR-016 |
| RST-24 Exportation en lot vers un référentiel | Add. 2 Q13 | ✅ | `ExportationReferentielService` |
| RST-25 Langue de communication du signataire | Add. 2 Q19 | ✅ | `Participant.langue` |
| RST-26 Provisionnement SCIM 2.0 | Add. 3 Q51 | ✅🔌 | `/scim/v2`, ADR-014 |
| RST-27 Correspondances d'annuaire → rôles et domaines | Add. 3 Q43, Q44 | ✅🔌 | `AnnuaireService`, ADR-014 |
| RST-28 Génération Word modifiable | Add. 3 Q26 | ✅ | `/generer?format=DOCX` |
| RST-29 Certificat de destruction | Add. 3 Q48 | ✅ | `CertificatDestructionService`, ADR-012 |
| RST-30 Relevé d'utilisation par enveloppe | Add. 3 Q39 | ✅ | `Enveloppes`, ADR-015 |
| — Suivi paginé, numérotation concurrente, pool de connexions | Add. 3 Q38 | ✅ | 0 erreur à 500 utilisateurs en dév ; essai cible à faire |
| RST-31 Administration d'affaires / technique séparées | Add. 4 Q10 | ✅ | `ADMIN_AFFAIRES`, `ADMIN_TECHNIQUE`, ADR-017 |
| RST-32 Signataire représentant une entreprise | Add. 4 Q6 | ✅ | qualité, entreprise, attestation d'autorité |
| RST-33 Approbation globale d'un lot | Add. 4 Q36 | ✅ | `ApprobationLotService` |
| RST-34 Attributs SCIM dans les correspondances | Add. 4 Q31 | ✅ | origine `SCIM_ATTRIBUT` |
| RST-35 Niveau d'authentification par type de document | Add. 4 Q1 | ✅ | liste `CLM_NIVEAU_PAR_TYPE` |
| RST-36 Acheminement selon type, unité, risque, origine | Add. 4 Q8, Q20 | ✅ | `trousse.*` dans les règles |
| RST-37 PDF/A-2b et PAdES-LTA | Add. 4 Q4, Q14 | ✅ 2026-11-20 | PDF/A-2b généré (police intégrée, XMP, sRGB) ; DSS + horodatage de document → B-LTA avec l'autorité retenue ([ADR-018](../../adr/ADR-018-pdfa-pades-lta-et-gabarits-word.md)) ; validation veraPDF en recette |
| RST-38 Modèles Word d'IQ avec mise en page préservée | Add. 4 Q22 | ✅ 2026-11-20 | Gabarit `.docx` par modèle, fusion POI (runs, tableaux, en-têtes), clauses au marqueur ; PDF/A par LibreOffice dans l'image serveur |
| RST-39 Comptes locaux de secours restreints | Add. 4 Q12 | ✅ 2026-11-20 | Réservation de la connexion locale par organisation, comptes de secours désignés par l'administration technique, second facteur exigé, journal |
| RST-41 Départ d'un employé : transfert de ses trousses, étapes et tâches | Question P3 | ✅ 2026-11-20 | Inventaire puis transfert en une opération par l'administration d'affaires (`/clm/admin/departs`), journalisé par trousse ; trousses closes inchangées |
| RST-40 Tableau de bord et « Mes trousses » paginés | Add. 3 Q38 | ✅ 2026-11-20 | `LecturePaginee` : page en base avec total (admin, audit), sinon tranches de 200 ; tableau de bord par décomptes |

**Avant le dépôt :**
- retransmettre à IQ les questions du § 0 de `09-Questions/questions_sans_reponse_IQ.md` ;
- refaire l'essai de 500 utilisateurs sur l'infrastructure cible ;
- remplir l'Annexe 2 et l'Annexe 10 en Excel natif ;
- reprendre `04-Architecture/solutions_proposees_reponses_IQ.md` dans la soumission.

## 0. État d'avancement — à lire avant toute nouvelle analyse (mis à jour le 2026-11-03)

Le module CLM (`backend/…/clm`, `frontend/src/pages/clm`, maquettes UI-01 à UI-24) implémente les stories ci-dessous.
**Une story marquée ✅ n'est plus à analyser** : seule la colonne « Reste » appelle encore du travail.

Légende : ✅ livrée et testée · ✅🔌 livrée et testée, recette sur le système réel d'IQ en attente · 🟡 partielle · ⏳ en attente d'une décision IQ (vague V1).

| Story | État | Livré (repères code et tests) | Reste |
|---|---|---|---|
| ST-01 SSO & sessions | ✅🔌 | Socle OIDC/SAML/MFA/JWT ; signataires externes sans compte (RST-17) ; correspondances groupes et attributs Entra ID → rôles et domaines (RST-27) ; SCIM 2.0 (RST-26) | Recette avec l'inscription d'application Entra ID d'IQ ; politique de durée de session |
| ST-02 Rôles, groupes, périmètres | ✅ | `Habilitations` (périmètre par unité/groupe, séparation initiateur/approbateur, consultation d'audit), rôle `AUDITEUR`, `@PreAuthorize` sur chaque API, garde de module par tenant (ADR-009), refus journalisés `CLM_ACCES_REFUSE`, `ArchitectureClmTest` et `ArchitectureModulesTest` ; organisation IQ et un compte par profil (`11-Tenant-IQ/`) | Charger la liste des unités administratives d'IQ (donnée d'atelier, pas de code). Rôles d'administration distincts : faits (RST-31) |
| ST-03 Dossiers & trousses | ✅ | `Trousse`, `TrousseService`, journal scellé `EvenementTrousse` ; UI-02/03/04 | — |
| ST-04 Documents & versions | ✅ | `DocumentClmService` (versions, remplacement, verrou après signature), `FormatDocument` ; UI-05/06 | — |
| ST-05 Modèles & champs | ✅ | `Modele`/`ModeleContrat` : champs positionnés, assignés à un rôle, cycle de vie, versions, partage par groupes, auto-publication refusée ; UI-07/08/09 | — |
| ST-06 Génération depuis Salesforce | ✅🔌 | `SalesforceRest` (OAuth 2.0, lecture par Id ou SOQL), `Preremplissage` (provenance, conflits), clauses conditionnelles via `MoteurRegles` ; UI-10 | Recette sur le bac à sable Salesforce d'IQ (objet et champs à confirmer) |
| ST-07 Configurer le workflow | ✅ | `Parcours` : séquentiel/parallèle/mixte, quorum, politiques de rejet, affectation utilisateur/groupe/rôle/participant ; UI-11 | — |
| ST-08 Exécuter validation/approbation | ✅ | `ParcoursService` : décision, retour pour correction, réassignation à chaud, délégation, relance, commentaires ; UI-12/13/14 | — |
| ST-09 Signature électronique | ✅ 2026-11-20 | Identité par risque, consentement, signatures séquentielles/parallèles, port `FournisseurSignature` avec attestation locale (`SignatureProbanteLocale`) ; UI-15/16 | Attestation CAdES par le cachet X.509, horodatée RFC 3161, vérifiable hors plateforme ([ADR-019](../../adr/ADR-019-attestation-cades-et-courriel-smtp.md)) ; validée en bac à sable (invitation SMTP, portail, `openssl cms`). Reste à IQ : certificat et autorité qualifiés |
| ST-10 Dossier de preuve & export | ✅ | `PreuveService` : certificat PDF/JSON, manifeste SHA-256, sceau HMAC, archive ZIP portable, vérification qui détecte une altération en base ; UI-19 | — |
| ST-11 Notifications, rappels, échéances | ✅ | `RappelService` (non-réponse, échéances), `MessagerieClm` (modèle par événement et canal, copies) ; UI-17 | — |
| ST-12 Statuts vers Salesforce | ✅🔌 | Boîte d'envoi `MessageSortant` : idempotence, reprises, file d'erreur, relance, corrélation ; UI-21 | Recette sur le bac à sable Salesforce d'IQ |
| ST-13 Suivi centralisé | ✅ | `SuiviService` : filtres en base, pagination serveur (50 lignes), requêtes groupées, exceptions, export CSV ; UI-01/18 | Tableau de bord et « Mes trousses » paginés (RST-40 ✅) |
| ST-14 Administration, audit, exploitation | ✅ | Audit lié à la trousse (journal scellé + audit du tenant), configuration UI-17/21/22, journal UI-23 et export CSV ; alertes d'exploitation (`AlertesExploitation` : altération détectée, file d'erreur Salesforce, échec du planificateur) relevées par Azure Monitor (`infra/terraform/azure/monitoring.tf`) ; runbook `04-Architecture/runbook_exploitation_clm.md` | Appliquer le Terraform sur l'abonnement retenu (RST-18) |
| ST-15 CLM avancé & IA (option) | ✅ | `AnalyseService`/`AnalyseContrat` : métadonnées, classification, anomalies, validation humaine justifiée ; module `clm_ia` activable par le superadmin ; UI-24 | — |
| RST-16 25 Mo + PDF/Word | ✅ | Taille configurable (`CLM_TAILLE_MAX_OCTETS`, 25 Mo), format vérifié sur les octets, analyse antivirus (port) | — |
| RST-17 Identité du signataire selon le risque | ✅ | `Identite` : niveau minimal par risque (courriel, SMS, question, forte à double canal), blocage après 5 essais | Brancher les passerelles courriel et SMS réelles au déploiement (simulations en place) |
| RST-18 Résidence & SLA | ⏳ | Azure retenu : Terraform validé (AKS, PostgreSQL, Key Vault, Front Door, supervision), région `canadacentral` par défaut | Choisir la région (`canadacentral` ou `canadaeast`, ou Québec exigé) et la grille SLA, puis ADR |
| RST-19 Parcours mobile du signataire | ✅ | Portail UI-16 (cibles de 44 px) ; e2e à 390 et 768 px sur 8 états, de l'invitation au reçu, avec axe-core | — |

**Qualité (au 2026-11-03)** : 130 tests backend CLM verts (unitaires, contrat, architecture, intégration) ; 23 tests e2e
(vues CLM aux 4 largeurs, fidélité aux 24 maquettes, gestion superadmin, isolation du tenant IQ ; axe-core sans
violation) ; couverture CLM de 99,36 % des lignes et de 87,88 % des branches, domaine à 100 %. Le build (`mvn verify`,
règle `jacoco:check@check-clm`) échoue si la couverture repasse sous 96 % de lignes (agrégé et chaque classe), 87 % de
branches ou 100 % sur le domaine. Reste transverse : porter les branches à 92 %, exécuter un test de mutation (PIT),
pagination serveur et test de charge CLM (NF-06), politique de conservation et de destruction (F-09, NF-03).

## 1. Reste à implémenter par story existante

> Analyse d'origine, conservée pour mémoire ; l'état à jour est au § 0.

| Story | Réemploi | Reste à faire (delta) | Effort | Prio |
|---|---|---|---|---|
| **ST-01** SSO & sessions | Élevé (OIDC/SAML, MFA, JWT) | Mapper l'IdP IQ ; gestion des **invités/externes** ; durées de session/renouvellement conformes | S | Must |
| **ST-02** Rôles, groupes, périmètres | Élevé (`securite`, MoteurAcces, multi-tenant) | Modéliser **unités administratives / domaines d'affaires** IQ ; séparation des responsabilités documentée + tests d'archi | S–M | Must |
| **ST-03** Dossiers & trousses | Moyen (`dossier`, Document) | Entité **Trousse** (multi-docs + participants + paramètres de processus + état) ; historique des transactions | M | Must |
| **ST-04** Import documents & versions | Moyen (versions PDF) | **Import Word (.docx)** ; **téléversement 25 Mo** (NF-07) ; versioning document unifié trousse↔document↔preuve | M | Must |
| **ST-05** Modèles & champs | Moyen (templates/gabarits) | **Positionnement visuel des champs** dans le document ; **champs assignés à un signataire** ; versions/cycle de vie des modèles ; partage par groupes | L | Must |
| **ST-06** Générer contrat depuis Salesforce | Faible (génération PDF ok, source absente) | **Connecteur Salesforce Grantmaking** (lecture données/métadonnées) + **préremplissage** + **clauses conditionnelles** (réutiliser `MoteurRegles`) | XL | Must |
| **ST-07** Configurer workflow | Moyen (`processus`, `Approbations`) | Éditeur de workflow **séquentiel/parallèle/mixte** + règles de quorum + affectation par rôle/groupe/règle | M–L | Must |
| **ST-08** Exécuter validation/approbation | Moyen | **Parallèle/mixte** à l'exécution ; **réassignation/ajout/retrait en cours sans redémarrage** ; commentaires + historique unifiés | M | Must |
| **ST-09** Orchestrer signature électronique | Faible (pad + jeton = base) | **Adaptateur fournisseur certifié** (valeur probante) ; **vérification d'identité par risque** ; consentement + **horodatage qualifié** ; signatures séq/parallèle/mixte + groupes | XL | Must |
| **ST-10** Dossier de preuve & export | Faible (audit présent) | **Certificat/dossier de preuve** (document signé + journal + métadonnées) ; **immuabilité/scellé** ; export durable/portable (NF-13) | L | Must |
| **ST-11** Notifications, rappels, échéances | Élevé (`notification`) | **Rappels automatiques** (non-réponse) + **échéances configurables** + copies + personnalisation des messages | S–M | Must |
| **ST-12** Synchroniser statuts vers Salesforce | Faible (patron interop/M365) | **Callbacks/retours de statut** vers Salesforce + **idempotence** + reprises + corrélation id externe | L | Must |
| **ST-13** Vue de suivi centralisée | Moyen (Stats/Rapports) | **Tableau CLM** (trousses/signatures/exceptions) filtré par périmètre ; actions en attente | M | Must |
| **ST-14** Administration, audit, exploitation | Élevé (audit, admin, observabilité) | Lier l'audit à **trousse/workflow/signature** ; scellé d'intégrité ; runbooks/alertes ; configuration | M | Must |
| **ST-15** CLM avancé & IA | Faible (Ollama/RAG base) | Extraction métadonnées / classification / détection d'anomalies **sur contrats** + contrôles humains ; **tarifé à part** | L | Option |

## 2. Stories net-new (hors des 15, issues des écarts)

| ID | Titre | Justification | Effort | Prio |
|---|---|---|---|---|
| **RST-16** | Téléversement 25 Mo + formats PDF/Word | NF-07 + F-01/F-09 ; limite actuelle 10 Mo ; import `.docx` | S | Must |
| **RST-17** | Authentification du signataire externe par niveau de risque | F-06/F-11 ; parcours invité (courriel/SMS/question/forte) selon document et risque | M | Must |
| **RST-18** | Résidence des données & grille SLA définitives | NF-01/NF-05 ; trancher Québec vs Canada + priorités support (ateliers + ADR) | S (décision) | Must |
| **RST-19** | Parcours mobile du signataire | F-12 ; signer sur mobile/tablette sans interruption + tests responsive/axe-core | M | Should |

## 3. Roadmap de réalisation (delta, alignée sur P0–P9)

La roadmap de phases `roadmap_stories.md` reste la cadence contractuelle. Ci-dessous la **séquence du
travail restant** (le socle réutilisé n'est pas rejoué) :

| Vague | Contenu (stories/delta) | Pré-requis |
|---|---|---|
| **V1 — Cadrage & décisions** (P0/P1) | RST-18 (résidence/SLA), ADR signature (ST-09) et Salesforce (ST-06), accès **sandbox Salesforce**, modèles/clauses/propriétaires | Ateliers IQ, Annexe 10, bordereau |
| **V2 — Socle & habilitations** (P2) | ST-01 (IdP IQ + invités), ST-02 (unités/domaines), environnements, observabilité, audit scellé (ST-14) | V1 |
| **V3 — Documents & modèles** (P3) | ST-03 (trousse), RST-16 (25 Mo + Word), ST-04 (versions), ST-05 (champs positionnés + assignation) | V2 |
| **V4 — Génération Salesforce** (P3/P5) | **ST-06** (connecteur + préremplissage + clauses conditionnelles) | Sandbox SF (V1) |
| **V5 — Workflow & signature** (P4) | ST-07/ST-08 (parallèle/mixte + réassignation), **ST-09** (signature probante + RST-17), ST-10 (dossier de preuve) | V3 ; décision signature (V1) |
| **V6 — Intégration & notifications** (P5) | **ST-12** (retour statuts Salesforce, idempotence/reprises), ST-11 (rappels/échéances) | V4, V5 |
| **V7 — Pilotage** (P6) | ST-13 (vue CLM), finalisation ST-14 | V5, V6 |
| **V8 — Qualité & mise en prod** (P7/P8/P9) | Tests fonctionnels/perf (55–65k docs/an)/sécurité, UAT, formation, stabilisation 30 j | tout |
| **Option** | **ST-15** (CLM avancé + IA) — chiffré séparément | Décision IQ |

## 4. Dépendances critiques (rappel, bloquantes V1)
Sandbox Salesforce + schémas d'objets + données de test ; **décision du moteur de signature** (valeur
probante, vérification d'identité) ; modèles/clauses/règles + propriétaires ; politique de conservation ;
IdP interne / gestion des invités ; **Annexe 10 Excel + bordereau** ; localisation d'hébergement ;
RTO/RPO + SLA définitif. *(Détail dans `roadmap_stories.md` et `09-Questions/questions_a_clarifier_IQ.md`.)*

## 5. Chemin critique
**ST-06 (Salesforce)** et **ST-09 (signature probante)** sont les deux écarts XL et le **chemin critique** :
tous deux dépendent de décisions/accès externes (V1). À sécuriser en priorité absolue, car tout le reste
(génération, workflow de signature, preuve, retour de statuts) en découle.
