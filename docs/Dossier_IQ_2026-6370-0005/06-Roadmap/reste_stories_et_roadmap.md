# Reste des stories à traiter + roadmap de réalisation

> Backlog d'**implémentation** dérivé de `02-Analyse/ecarts_solution_vs_exigences.md`, **relatif aux 15
> stories analysées** (ST-01→ST-15, qui portent déjà leur analyse fonctionnelle + ADR). Ici on ne
> ré-analyse pas : on énonce, pour chaque story, **le travail qui RESTE** compte tenu de ce que
> `contractuelle_contrat` fournit déjà, plus les stories **net-new** nécessaires. Effort = T-shirt
> indicatif (S/M/L/XL), à confirmer en ateliers (P0/P1). Priorités MoSCoW du DAO conservées.

## 1. Reste à implémenter par story existante

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
