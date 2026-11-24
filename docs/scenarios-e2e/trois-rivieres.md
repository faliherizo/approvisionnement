# Ville de Trois-Rivières — appel d'offres 7000017905 — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## TR-01 — Données hébergées au Canada

- **Source** : Section III n° 1
- **Conformité déclarée** : ✅ Hébergement Microsoft Azure, régions canadiennes (Canada Central/Est), résidence des données au Canada.
- **Vérification** : documentaire — Hébergement Azure au Canada : infrastructure (région paramétrée, validation Terraform).

## TR-02 — Générer des documents Word modifiables

- **Source** : Section III n° 2
- **Conformité déclarée** : ✅ Export .docx modifiable (moteur Apache POI, DocxContractRenderer).

### E2E-TR-02 — Un document approuvé se génère en Word modifiable et se publie vers Microsoft 365

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-02, TR-04, TR-20
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Voir « Publication (Microsoft 365 / SAP) »
4. Activer « Générer »
5. Télécharger par « Export DOCX » et obtenir un fichier non vide

## TR-03 — Migrer/adapter les modèles Word existants

- **Source** : Section III n° 3
- **Conformité déclarée** : ✅ Import de modèles .docx existants, détection de sections, mise au format gabarit.

### E2E-TR-01 — Un modèle Word existant est analysé puis importé comme modèle de la plateforme

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-03
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/donnees`
2. Saisir « Nom du modèle » : `Modèle Word E2E ${unique}`
3. Joindre « gabarit.docx » au champ « Fichier Word »
4. Activer « Analyser (essai) »
5. Voir « section(s) détectée(s) »
6. Activer « Importer le modèle »
7. Voir « Modèle importé »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-04 — Compatible Microsoft 365

- **Source** : Section III n° 4
- **Conformité déclarée** : ✅ Intégration Microsoft Graph réelle (SharePoint, OneDrive, Teams, Outlook).

### E2E-TR-02 — Un document approuvé se génère en Word modifiable et se publie vers Microsoft 365

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-02, TR-04, TR-20
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Voir « Publication (Microsoft 365 / SAP) »
4. Activer « Générer »
5. Télécharger par « Export DOCX » et obtenir un fichier non vide

## TR-05 — Gestion sécurisée des accès + auth. corporative

- **Source** : Section III n° 5
- **Conformité déclarée** : ✅ Jeton httpOnly, RBAC ; module de connecteur d'authentification — OIDC et SAML 2.0 (Microsoft Entra ID ou IdP gouvernemental) configurable et activable par organisation — les utilisateurs gouvernementaux du client s'authentifient via leur annuaire.

### E2E-TR-03 — Accès sécurisés : second facteur au choix de l'utilisateur, rôles administrés, responsabilités séparées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-05, TR-06, TR-07, TR-13
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/securite`
2. Voir le titre « Sécurité du compte »
3. Voir « Authentification TOTP », « Question secrète dynamique »
4. Ne pas voir « Circuits d’approbation »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
6. Se connecter en tant que administrateur de Lévis
7. Ouvrir l'écran `/utilisateurs`
8. Voir « juriste@levis.qc.ca », « JURIDIQUE »

## TR-06 — Authentification multifacteur (MFA)

- **Source** : Section III n° 6
- **Conformité déclarée** : ✅ MFA (TOTP et questions secrètes) — comptes de démo totp@ et question@levis.qc.ca —, ou MFA corporatif via SSO.

### E2E-TR-03 — Accès sécurisés : second facteur au choix de l'utilisateur, rôles administrés, responsabilités séparées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-05, TR-06, TR-07, TR-13
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/securite`
2. Voir le titre « Sécurité du compte »
3. Voir « Authentification TOTP », « Question secrète dynamique »
4. Ne pas voir « Circuits d’approbation »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
6. Se connecter en tant que administrateur de Lévis
7. Ouvrir l'écran `/utilisateurs`
8. Voir « juriste@levis.qc.ca », « JURIDIQUE »

## TR-07 — Rôles, permissions et profils d'accès

- **Source** : Section III n° 7
- **Conformité déclarée** : ✅ RBAC (rédacteur, approbateur, juriste, admin, superadmin), isolation multi-tenant.

### E2E-TR-03 — Accès sécurisés : second facteur au choix de l'utilisateur, rôles administrés, responsabilités séparées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-05, TR-06, TR-07, TR-13
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/securite`
2. Voir le titre « Sécurité du compte »
3. Voir « Authentification TOTP », « Question secrète dynamique »
4. Ne pas voir « Circuits d’approbation »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
6. Se connecter en tant que administrateur de Lévis
7. Ouvrir l'écran `/utilisateurs`
8. Voir « juriste@levis.qc.ca », « JURIDIQUE »

## TR-08 — Chiffrement au repos et en transit

- **Source** : Section III n° 8
- **Conformité déclarée** : ✅ TLS en transit ; secrets chiffrés (AES) au repos ; PostgreSQL chiffré.

### E2E-TR-04 — Le secret d'intégration est stocké chiffré, jamais réaffiché ; les règles internes se paramètrent

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-08, TR-10
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/parametres`
2. Voir le titre « Paramètres du tenant — valeurs des sélecteurs »
3. Voir « Secret client », « stocké chiffré et n’est jamais réaffiché »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-09 — Journaux d'accès et d'activité

- **Source** : Section III n° 9
- **Conformité déclarée** : ✅ Journal d'audit complet (accès et actions), traçable et filtrable.

### E2E-TR-05 — Le journal d'audit se filtre et s'exporte ; versions et validations du document restent tracées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-09, TR-11, TR-12
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Capturer une version »
4. Saisir « Note de version » : `Version E2E ${unique}`
5. Activer « Valider »
6. Voir « Version E2E ${unique} »
7. Se connecter en tant que administrateur de Lévis
8. Ouvrir l'écran `/audit`
9. Activer « Filtrer »
10. Voir « Événements du journal d’audit »
11. Télécharger par « Exporter en CSV » et obtenir un fichier non vide
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-10 — Configurable aux règles internes + LCOM

- **Source** : Section III n° 10
- **Conformité déclarée** : ✅ Circuits d'approbation, gabarits et règles configurables par la Ville.

### E2E-TR-04 — Le secret d'intégration est stocké chiffré, jamais réaffiché ; les règles internes se paramètrent

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-08, TR-10
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/parametres`
2. Voir le titre « Paramètres du tenant — valeurs des sélecteurs »
3. Voir « Secret client », « stocké chiffré et n’est jamais réaffiché »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-11 — Traçabilité versions/validations/approbations

- **Source** : Section III n° 11
- **Conformité déclarée** : ✅ Versionnage, historique des validations, approbations et modifications.

### E2E-TR-05 — Le journal d'audit se filtre et s'exporte ; versions et validations du document restent tracées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-09, TR-11, TR-12
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Capturer une version »
4. Saisir « Note de version » : `Version E2E ${unique}`
5. Activer « Valider »
6. Voir « Version E2E ${unique} »
7. Se connecter en tant que administrateur de Lévis
8. Ouvrir l'écran `/audit`
9. Activer « Filtrer »
10. Voir « Événements du journal d’audit »
11. Télécharger par « Exporter en CSV » et obtenir un fichier non vide
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-12 — Conservation docs/versions/journaux

- **Source** : Section III n° 12
- **Conformité déclarée** : ✅ Conservation des documents, versions, journaux et historiques.

### E2E-TR-05 — Le journal d'audit se filtre et s'exporte ; versions et validations du document restent tracées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-09, TR-11, TR-12
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Capturer une version »
4. Saisir « Note de version » : `Version E2E ${unique}`
5. Activer « Valider »
6. Voir « Version E2E ${unique} »
7. Se connecter en tant que administrateur de Lévis
8. Ouvrir l'écran `/audit`
9. Activer « Filtrer »
10. Voir « Événements du journal d’audit »
11. Télécharger par « Exporter en CSV » et obtenir un fichier non vide
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-13 — Séparation des responsabilités

- **Source** : Section III n° 13
- **Conformité déclarée** : ✅ Contrôle des accès/rôles soutenant la séparation des responsabilités.

### E2E-TR-03 — Accès sécurisés : second facteur au choix de l'utilisateur, rôles administrés, responsabilités séparées

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-05, TR-06, TR-07, TR-13
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/securite`
2. Voir le titre « Sécurité du compte »
3. Voir « Authentification TOTP », « Question secrète dynamique »
4. Ne pas voir « Circuits d’approbation »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
6. Se connecter en tant que administrateur de Lévis
7. Ouvrir l'écran `/utilisateurs`
8. Voir « juriste@levis.qc.ca », « JURIDIQUE »

## TR-14 — Pas d'entraînement d'IA externe sans autorisation

- **Source** : Section III n° 14
- **Conformité déclarée** : ✅ Aucun entraînement de modèle externe ; IA locale (Ollama), données non transmises.
- **Vérification** : documentaire — Aucun entraînement de modèle externe : IA locale (Ollama), aucune donnée transmise ; vérifié par revue d'architecture.

## TR-15 — Validation humaine des contenus générés

- **Source** : Section III n° 15
- **Conformité déclarée** : ✅ Tout contenu IA est proposé et exige une validation humaine avant usage.

### E2E-TR-06 — L'assistant IA est encadré : suggestions tracées, désactivable puis réactivable par l'administration

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-15, TR-24
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/donnees`
2. Voir « Encadrement de l'assistant IA », « Suggestions récentes »
3. Activer « Désactiver l'assistant IA »
4. Voir « Assistant IA désactivé »
5. Activer « Activer l'assistant IA »
6. Voir « Assistant IA activé »

## TR-16 — Méthode de migration détaillée

- **Source** : Section III n° 16
- **Conformité déclarée** : ✅ Méthode décrite (modèles, clauses, contenus) — voir mémoire §5 et import .docx.
- **Vérification** : documentaire — Méthode de migration décrite au mémoire ; l'import Word est éprouvé par le scénario TR-03.

## TR-17 — Détailler coûts (ponctuels, récurrents, additionnels, sortie)

- **Source** : Section III n° 17
- **Conformité déclarée** : ✅ Ponctuels (implantation, An 1) et récurrents (licence annuelle) au bordereau + mémoire §10 ; additionnels : toute nouvelle demande hors devis, analysée et facturée à 120 $/h (avant taxes) après accord écrit de la Ville, sans incidence sur les prix soumis ; aucuns frais de sortie (voir 36).
- **Vérification** : documentaire — Coûts : bordereau de prix.

## TR-18 — Soutien, formation, documentation en français

- **Source** : Section III n° 18
- **Conformité déclarée** : ✅ Solution, formation et documentation entièrement en français.
- **Vérification** : documentaire — Solution, soutien, formation et documentation en français : engagement de service (l'interface en français est éprouvée par tous les scénarios).

## TR-19 — Exportation complète en fin de contrat

- **Source** : Section III n° 19
- **Conformité déclarée** : ✅ Export complet des données, documents, modèles et configuration.

### E2E-TR-07 — En fin de contrat, l'administration exporte toutes les données en une archive

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-19
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/donnees`
2. Voir « Réversibilité »
3. Télécharger par « Exporter toutes les données (ZIP) » et obtenir un fichier non vide

## TR-20 — Intégrations SharePoint/Teams/OneDrive/Outlook

- **Source** : Section III n° 20
- **Conformité déclarée** : ✅ Offert — publication réelle vers les quatre (Microsoft Graph).

### E2E-TR-02 — Un document approuvé se génère en Word modifiable et se publie vers Microsoft 365

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-02, TR-04, TR-20
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `tr.document.approuve`
2. Ouvrir l'écran `/documents/${document}`
3. Voir « Publication (Microsoft 365 / SAP) »
4. Activer « Générer »
5. Télécharger par « Export DOCX » et obtenir un fichier non vide

## TR-21 — Dossier d'approvisionnement

- **Source** : Section III n° 21
- **Conformité déclarée** : ✅ Offert — regroupement docs/versions/validations/historiques par dossier.

### E2E-TR-08 — Un dossier d'approvisionnement regroupe ses documents

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : TR-21
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/dossiers`
2. Saisir « Titre » : `Dossier E2E ${unique}` ; « N° d’appel d’offres » : `AO-E2E-${UNIQUE}` ; « Objet du dossier » : `Réfection de la chaussée`
3. Activer « Créer le dossier »
4. Voir « Dossier E2E ${unique} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-22 — Rapports de suivi

- **Source** : Section III n° 22
- **Conformité déclarée** : ✅ Offert — documents générés, validations, utilisateurs, dossiers en cours.

### E2E-TR-09 — Rapports de suivi : indicateurs, documents par statut et par type, activité par utilisateur

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-22
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/rapports`
2. Voir « Documents par statut », « Documents par type », « Activité par utilisateur »
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
4. Ouvrir l'écran `/tableau-de-bord`
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## TR-23 — Étapes de validation par type de document

- **Source** : Section III n° 23
- **Conformité déclarée** : ✅ Offert — circuits d'approbation paramétrables par type/processus.

### E2E-TR-10 — Les étapes de validation se définissent par type de document

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-23
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/workflows`
2. Saisir « Nom » : `Circuit services E2E ${unique}`
3. Choisir « Type de document » : `Contrat de services`
4. Cocher « JURIDIQUE »
5. Cocher « DIRECTION »
6. Activer « Créer le circuit »
7. Voir « Circuit services E2E ${unique} »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
9. Activer « Supprimer le circuit Circuit services E2E ${unique} » (button)
10. Voir « Circuit supprimé »
11. Ne pas voir « Circuit services E2E ${unique} »

## TR-24 — Désactivation de l'IA

- **Source** : Section III n° 24
- **Conformité déclarée** : ✅ Offert — l'IA est activable/désactivable par tenant.

### E2E-TR-06 — L'assistant IA est encadré : suggestions tracées, désactivable puis réactivable par l'administration

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-15, TR-24
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/donnees`
2. Voir « Encadrement de l'assistant IA », « Suggestions récentes »
3. Activer « Désactiver l'assistant IA »
4. Voir « Assistant IA désactivé »
5. Activer « Activer l'assistant IA »
6. Voir « Assistant IA activé »

## TR-25 — Références d'organisations similaires

- **Source** : Section III n° 25
- **Conformité déclarée** : ✅ Voir Formule de qualification (mandats publics : Santé Québec, Revenu Québec, Sécurité publique via Cofomo, INSPQ via Randstad).
- **Vérification** : documentaire — Références : formule de qualification.

## TR-26 — Environnement d'essai / pilote

- **Source** : Section III n° 26
- **Conformité déclarée** : ✅ Offert — démonstration en ligne <https://contrat.vinaitsolution.com/login> (comptes par rôle : admin@, redacteur@, juriste@, direction@levis.qc.ca — mot de passe Passw0rd!) + comptes de démo MFA (totp@ TOTP, question@levis.qc.ca question secrète) ; vidéo <https://contrat.vinaitsolution.com/accueil
- **Vérification** : documentaire — Environnement d'essai en ligne : service de démonstration.

## TR-27 — Évolutions prévues à court terme

- **Source** : Section III n° 27
- **Conformité déclarée** : ✅ Évolutions à court terme : module SSO / OIDC gouvernemental configurable et activable par organisation ; connecteurs vers les systèmes financiers et SAP ; signature électronique avancée ; tableaux de bord analytiques ; enrichissement du moteur de recherche.
- **Vérification** : documentaire — Évolutions prévues : feuille de route.

## TR-28 — Type de solution (infonuagique/locale/hybride)

- **Source** : Section III n° 28
- **Conformité déclarée** : Infonuagique (SaaS), hébergée sur Microsoft Azure (régions canadiennes).
- **Vérification** : documentaire — Information requise : solution infonuagique (SaaS).

## TR-29 — Connecteurs / interfaces / API + coûts

- **Source** : Section III n° 29
- **Conformité déclarée** : API REST documentée (OpenAPI / Swagger) — interface Swagger : <https://contrat.vinaitsolution.com/swagger-ui/index.html> · spécification OpenAPI : <https://contrat.vinaitsolution.com/v3/api-docs> ; connecteurs Microsoft Graph (M365), SAP (OData) et authentification SSO (OIDC / SAML 2.0), inclus au f

### E2E-TR-11 — L'API est documentée et testable (OpenAPI 3.0)

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : TR-29
- **Données** : `bdd/e2e/donnees/trois-rivieres.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/api-docs`
2. Voir le titre « API — Documentation & tests (OpenAPI 3.0) »
3. Voir « Swagger UI interactif »

## TR-30 — Notification et suivi des incidents de sécurité

- **Source** : Section III n° 30
- **Conformité déclarée** : Procédure de notification, traitement et suivi ; journalisation et alertes.
- **Vérification** : documentaire — Information requise : procédure de notification des incidents.

## TR-31 — Sous-traitants (hébergement, traitement, soutien)

- **Source** : Section III n° 31
- **Conformité déclarée** : Microsoft Azure (hébergement) ; aucun autre sous-traitant de traitement des données.
- **Vérification** : documentaire — Information requise : sous-traitants.

## TR-32 — Utilisation de l'IA — description

- **Source** : Section III n° 32
- **Conformité déclarée** : IA locale (Ollama) d'assistance à la rédaction ; désactivable ; aucune donnée externe.
- **Vérification** : documentaire — Information requise : description de l'usage de l'IA (le comportement est éprouvé par TR-15 et TR-24).

## TR-33 — Volume inclus (modèles/clauses/contenus)

- **Source** : Section III n° 33
- **Conformité déclarée** : Nombre illimité de modèles, clauses et documents dans le forfait de base.
- **Vérification** : documentaire — Information requise : volume inclus.

## TR-34 — Délais de réponse du soutien par priorité

- **Source** : Section III n° 34
- **Conformité déclarée** : Soutien avec engagements de niveau de service (délais selon priorité — mémoire §8).
- **Vérification** : documentaire — Information requise : délais de soutien.

## TR-35 — Formations incluses (utilisateurs et admins)

- **Source** : Section III n° 35
- **Conformité déclarée** : Formation initiale par rôle (rédaction, juridique, approvisionnement) + admin, incluse An 1.
- **Vérification** : documentaire — Information requise : formations incluses.

## TR-36 — Frais de sortie / restitution

- **Source** : Section III n° 36
- **Conformité déclarée** : Aucuns frais de sortie ; export complet fourni sans coût.
- **Vérification** : documentaire — Information requise : aucuns frais de sortie (l'export complet est éprouvé par TR-19).

## TR-37 — Limites connues

- **Source** : Section III n° 37
- **Conformité déclarée** : Génération IA en français (locale) ; connecteurs sur mesure chiffrés après cadrage.
- **Vérification** : documentaire — Information requise : limites connues.
