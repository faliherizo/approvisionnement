# Catalogue des stories

État au 2026-11-03 : ✅ livrée et testée · ✅🔌 livrée, recette sur le système réel d'IQ en attente · 🟡 partielle ·
⏳ en attente d'une décision IQ. Détail et reste à faire : [§ 0 du reste à faire](../06-Roadmap/reste_stories_et_roadmap.md).

- ⏳ [ST-01 — Authentification SSO et sessions](ST-01/ST-01_authentification_sso_et_sessions.md)
- ✅ [ST-02 — Rôles, groupes et périmètres](ST-02/ST-02_r_les_groupes_et_p_rim_tres.md)
- ✅ [ST-03 — Créer dossiers et trousses](ST-03/ST-03_cr_er_dossiers_et_trousses.md)
- ✅ [ST-04 — Importer documents et gérer versions](ST-04/ST-04_importer_documents_et_g_rer_versions.md)
- ✅ [ST-05 — Administrer modèles et champs](ST-05/ST-05_administrer_mod_les_et_champs.md)
- ✅🔌 [ST-06 — Générer contrat depuis Salesforce](ST-06/ST-06_g_n_rer_contrat_depuis_salesforce.md)
- ✅ [ST-07 — Configurer workflow de validation et approbation](ST-07/ST-07_configurer_workflow_de_validation_et_approbation.md)
- ✅ [ST-08 — Exécuter validation et approbation](ST-08/ST-08_ex_cuter_validation_et_approbation.md)
- 🟡 [ST-09 — Orchestrer signature électronique](ST-09/ST-09_orchestrer_signature_lectronique.md)
- ✅ [ST-10 — Constituer dossier de preuve et exporter](ST-10/ST-10_constituer_dossier_de_preuve_et_exporter.md)
- ✅ [ST-11 — Notifications, rappels et échéances](ST-11/ST-11_notifications_rappels_et_ch_ances.md)
- ✅🔌 [ST-12 — Synchroniser les statuts vers Salesforce](ST-12/ST-12_synchroniser_les_statuts_vers_salesforce.md)
- ✅ [ST-13 — Vue de suivi centralisée](ST-13/ST-13_vue_de_suivi_centralis_e.md)
- ✅ [ST-14 — Administration, audit et exploitation](ST-14/ST-14_administration_audit_et_exploitation.md)
- ✅ [ST-15 — Capacités CLM avancées et IA (option)](ST-15/ST-15_capacit_s_clm_avanc_es_et_ia_option.md)

Stories issues des écarts (sans fiche) : ✅ RST-16 · ✅ RST-17 · ⏳ RST-18 · ✅ RST-19.

Stories issues de l'addenda 2 (2026-11-16, détail : [impact_addenda_2.md](../02-Analyse/impact_addenda_2.md) § 2) :
✅ RST-20 « retournée » transmise lors d'un retour d'étape · ✅ RST-21 correspondance des statuts Salesforce ·
✅ RST-22 lecture Salesforce limitée aux champs nécessaires · ✅🔌 RST-23 authentification JWT Bearer ·
✅ RST-24 exportation en lot vers un référentiel · ✅ RST-25 langue de communication du signataire.

Stories issues de l'addenda 3 (2026-11-17, détail : [impact_addenda_3.md](../02-Analyse/impact_addenda_3.md) § 3) :
✅🔌 RST-26 provisionnement SCIM 2.0 · ✅🔌 RST-27 correspondances d'annuaire vers rôles et domaines d'affaires ·
✅ RST-28 génération Word modifiable · ✅ RST-29 certificat de destruction · ✅ RST-30 relevé d'utilisation par enveloppe.

Stories issues de l'addenda 4 (2026-11-19, détail : [impact_addenda_4.md](../02-Analyse/impact_addenda_4.md) § 2) :
✅ RST-31 administration d'affaires et technique séparées · ✅ RST-32 signataire représentant une entreprise ·
✅ RST-33 approbation globale d'un lot · ✅ RST-34 attributs SCIM · ✅ RST-35 niveau par type de document ·
✅ RST-36 acheminement selon type, unité, risque. Réalisées le 2026-11-20 (§ 3 de l'analyse) : ✅ RST-37 PDF/A-2b et PAdES-B-LTA ·
✅ RST-38 gabarits Word d'IQ · ✅ RST-39 comptes de secours · ✅ RST-40 tableau de bord et « Mes trousses » paginés ·
✅ RST-41 départ d'un employé (question P3). Validés dans les bacs à sable `infra/bacs-a-sable` (ICP, Gotenberg, veraPDF, pyHanko).

Stories issues de l'Annexe 10 (fichier Excel reçu le 2026-11-25, détail et critères :
[couverture-annexe-10.md](../02-Analyse/couverture-annexe-10.md) § 4). Réalisées le 2026-11-25 :
✅ RST-42 rapports personnalisables et exportables (AF-37, ADR-032) · ✅ RST-43 modification contrôlée des champs en cours
et journal avant/après (AF-34, SEC-15, ADR-030) · ✅ RST-44 accès de l'exploitant aux données d'IQ verrouillé et encadré
(SEC-17, ADR-031) · ✅🔌 RST-45 Microsoft 365 pour le module de signature : SharePoint/OneDrive et Teams (TI-01,
obligatoire, ADR-029 ; recette sur le locataire d'IQ en attente) · ✅🔌 RST-46 événements de sécurité pour le SIEM d'IQ
(TI-04, ADR-033 ; connecteur Sentinel à déployer chez IQ). Restent : 🟡 RST-47 compatibilité Safari iOS, iPadOS et Windows
éprouvée (TI-11) · 🟡 RST-48 essai de 500 utilisateurs sur l'infrastructure cible (TI-12) · 🟡 RST-49 priorisation des
trousses (AF-46) · 🟡 RST-50 participants en consultation ou en complétion seulement (AF-48) · 🟡 RST-51 comptage des
documents et des pages signés (AF-49) · 🟡 RST-52 découverte des droits par API (TI-21) · ⏳ RST-53 intégration
Microsoft Purview (SEC-27) · ⏳ RST-54 clé de chiffrement dédiée à IQ (SEC-28) · ⏳ RST-55 négociation assistée avec un
tiers (IA-05, option).
