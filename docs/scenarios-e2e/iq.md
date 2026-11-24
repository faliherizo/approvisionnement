# Investissement Québec — AO 2026-6370-0005 — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## IQ-C-01 — Gérer le cycle de validation, d'approbation et de signature, orchestration comprise

- **Source** : 1.3
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-02 — Contexte multi-juridictionnel, solution conforme, modulaire, évolutive

- **Source** : 1.3
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Principe général (contexte multi-juridictionnel, modularité) : démontré par l'ensemble des scénarios et des modules activables.

## IQ-C-03 — Service de signature appelable, distinct de l'orchestration (couplage lâche)

- **Source** : 1.3
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-04 — Capacités avancées du cycle de vie des contrats

- **Source** : 1.3
- **Conformité déclarée** : ✅

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.ia.analyse`
2. Ouvrir l'écran `/clm`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-05 — Offerte en infonuagique, connectable aux SaaS

- **Source** : 1.4.3
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Mode infonuagique : hébergement Azure (infrastructure).

## IQ-C-06 — Connexion aux applications « on premise » par API

- **Source** : 1.4.3
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-07 — Protection et confidentialité des données

- **Source** : 1.4.3
- **Conformité déclarée** : ✅

### E2E-IQ-30 — Isolation des données entre organisations et modules

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.isolation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « ${trousseRef} »

## IQ-C-08 — Échange avec les systèmes de référence : préalimenter, retourner les statuts

- **Source** : 1.4.4
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-09 — Récupération automatisée des données Salesforce Grantmaking

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-10 — Génération automatisée des contrats depuis les données Salesforce

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-11 — Préremplissage des modèles approuvés

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-12 — Acheminement des documents générés vers validation, approbation, signature

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Trousse écran ${unique}`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « contrat-${unique}.pdf » au champ « Téléverser un document »
6. Voir « contrat-${unique}.pdf »
7. Joindre « annexe-${unique}.pdf » au champ « Téléverser un document »
8. Voir « annexe-${unique}.pdf »
9. Activer « Prévisualiser — contrat-${unique}.pdf » (button)
10. Télécharger par « Télécharger — contrat-${unique}.pdf » et obtenir un fichier non vide
11. Activer « Remplacer par une nouvelle version — contrat-${unique}.pdf » (button)
12. Joindre « contrat-v2-${unique}.pdf » au champ « Fichier »
13. Saisir « Motif » : `Correction E2E`
14. Activer « Remplacer »
15. Activer « Retirer avant lancement — annexe-${unique}.pdf » (button)
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Voir « Marie E2E ${unique} »
22. Saisir « Nom » : `Retiré E2E ${unique}` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550191`
23. Activer « Ajouter un signataire »
24. Activer « Retirer — Retiré E2E ${unique} » (button)
25. Activer « Continuer »
26. Activer « Ajouter une étape »
27. Saisir « Clé » : `s1` ; « Libellé » : `Signature`
28. Choisir « Type » : `SIGNATURE`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `(premier)`
32. Activer « Tester le parcours »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Activer « Preuve » (tab)
38. Activer « Vérifier l’intégrité »

### E2E-ECR-IQ-02 — Une trousse naît d'un dossier Salesforce et d'un modèle : recherche et import, valeur corrigée, contrat généré en PDF et en Word, zone de signature attribuée, lancement ; le validateur commente puis valide sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-14, IQ-C-37, IQ-C-12
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié (champs, zone de signature, clause conditionnelle, parcours de validation)
2. Ouvrir l'écran `/clm/trousses/nouvelle`
3. Cocher « Salesforce »
4. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0001`
5. Activer « Rechercher #2 »
6. Saisir « Titre » : `Trousse modèle ${unique}` ; « Référence externe » : `REF-${UNIQUE}`
7. Choisir « Modèle de contrat » : `Convention écran ${unique}`
8. Activer « Enregistrer le brouillon »
9. Activer « Importer / actualiser »
10. Activer « Corriger »
11. Saisir « Nouvelle valeur » : `Coopérative E2E ${unique}`
12. Dans la section « Corriger la valeur », activer « Enregistrer »
13. Activer « Continuer »
14. Activer « Générer depuis le modèle »
15. Activer « Générer en Word modifiable »
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
22. Activer « Attribuer la zone — Signature du bénéficiaire »
23. Activer « Continuer »
24. Activer « Continuer »
25. Activer « Lancer le parcours »
26. Arriver sur une adresse de la forme `/clm/trousses/\d+`
27. Se connecter en tant que validateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — REF-${UNIQUE} Validation juridique »
30. Saisir « Nouveau commentaire » : `Vérifié par l'e2e`
31. Dans la section « Commentaires contextualisés », activer « Ajouter un commentaire »
32. Voir « Vérifié par l'e2e »
33. Activer « Valider »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-13 — Retour vers Salesforce des informations de signature

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-14 — Traçabilité dossier source → document → preuve

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-02 — Une trousse naît d'un dossier Salesforce et d'un modèle : recherche et import, valeur corrigée, contrat généré en PDF et en Word, zone de signature attribuée, lancement ; le validateur commente puis valide sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-14, IQ-C-37, IQ-C-12
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié (champs, zone de signature, clause conditionnelle, parcours de validation)
2. Ouvrir l'écran `/clm/trousses/nouvelle`
3. Cocher « Salesforce »
4. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0001`
5. Activer « Rechercher #2 »
6. Saisir « Titre » : `Trousse modèle ${unique}` ; « Référence externe » : `REF-${UNIQUE}`
7. Choisir « Modèle de contrat » : `Convention écran ${unique}`
8. Activer « Enregistrer le brouillon »
9. Activer « Importer / actualiser »
10. Activer « Corriger »
11. Saisir « Nouvelle valeur » : `Coopérative E2E ${unique}`
12. Dans la section « Corriger la valeur », activer « Enregistrer »
13. Activer « Continuer »
14. Activer « Générer depuis le modèle »
15. Activer « Générer en Word modifiable »
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
22. Activer « Attribuer la zone — Signature du bénéficiaire »
23. Activer « Continuer »
24. Activer « Continuer »
25. Activer « Lancer le parcours »
26. Arriver sur une adresse de la forme `/clm/trousses/\d+`
27. Se connecter en tant que validateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — REF-${UNIQUE} Validation juridique »
30. Saisir « Nouveau commentaire » : `Vérifié par l'e2e`
31. Dans la section « Commentaires contextualisés », activer « Ajouter un commentaire »
32. Voir « Vérifié par l'e2e »
33. Activer « Valider »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-15 — Erreurs d'intégration, reprises, transactions incomplètes

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-16 — Interfaces standard, documentées et supportées par l'éditeur

- **Source** : 1.4.5
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Compte jetable qui quitte l'organisation, avec une étape de validation ouverte à son nom
2. Ouvrir l'écran `/clm/admin`
3. Ouvrir l'écran `/clm/admin/notifications`
4. Activer « Modifier »
5. Saisir « Sujet » : `Invitation E2E ${unique}`
6. Activer « Enregistrer »
7. Saisir « Destinataire de test » : `test.${unique}@iq.example`
8. Activer « Envoyer un test »
9. Ouvrir l'écran `/clm/admin/integrations`
10. Activer « Tester la connexion »
11. Activer « Relayer maintenant »
12. Activer « Configurer la connexion »
13. Dans la section « Configurer la connexion », activer « Enregistrer »
14. Activer « Configurer la connexion »
15. Activer « Rétablir la connexion de la plateforme »
16. Saisir « Nouveau client d’intégration » Nom » : `Client E2E ${unique}`
17. Activer « Créer le client »
18. Activer « J’ai copié les valeurs »
19. Activer « Révoquer — Client E2E ${unique} » (button)
20. Ouvrir l'écran `/clm/admin/parametres`
21. Activer « Modifier un paramètre autorisé »
22. Saisir « Rappel tous les (jours) » : `4`
23. Activer « Enregistrer »
24. Activer « Modifier un paramètre autorisé »
25. Activer « Rétablir les paramètres de la plateforme »
26. Ouvrir l'écran `/clm/admin/listes`
27. Ouvrir l'écran `/clm/admin/groupes`
28. Saisir « Nouveau groupe » Code » : `GRP_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Groupe écran ${unique}`
29. Activer « Créer le groupe »
30. Choisir « Ajouter un membre au groupe GRP_${UNIQUE} » Membre » : `initiateur@iq.example`
31. Dans la section « Ajouter un membre au groupe GRP_${UNIQUE} », activer « Ajouter »
32. Activer « Retirer initiateur@iq.example du groupe GRP_${UNIQUE} » (button)
33. Activer « Supprimer le groupe GRP_${UNIQUE} » (button)
34. Ouvrir l'écran `/clm/admin/departs`
35. Saisir « Courriel de la personne qui part » : `depart.${unique}@iq.example`
36. Activer « Consulter »
37. Choisir « Personne qui reprend » : `(premier)`
38. Activer « Transférer »
39. Ouvrir l'écran `/clm/admin/journal`
40. Saisir « Acteur » : `initiateur@iq.example`
41. Activer « Rechercher #2 »
42. Ouvrir l'écran `/clm/admin/audit`
43. Ouvrir l'écran `/clm/options`
44. Ouvrir l'écran `/clm/profil`
45. Ouvrir l'écran `/clm/securite`

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-23 — Interfaces standards et documentées (OpenAPI, SCIM)

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-29, IQ-C-16
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.api.documentee`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-C-17 — Flexibilité et agilité tout au long de l'utilisation

- **Source** : 1.4.6
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-18 — Circuits de validation et d'approbation séquentiels, parallèles, mixtes

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

## IQ-C-19 — Attribution selon rôles, groupes ou règles d'affaires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

## IQ-C-20 — Ajout, retrait, remplacement d'un validateur ou approbateur en cours

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

## IQ-C-21 — Réassignation d'une étape sans redémarrage

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

## IQ-C-22 — Approuver, rejeter, retourner pour correction

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-07 — Rejet par un validateur et retour à l'initiateur

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-S-09
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.rejet`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Décision : rejeté »

## IQ-C-23 — Commentaires et historique des validations

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-24 — Notifications et rappels configurables pour validateurs et approbateurs

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-25 — Suivi de l'état des validations en cours

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-16 — Suivi centralisé des trousses et des échéances

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-C-25, IQ-D-08
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-26 — Signature séquentielle, parallèle, mixte

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-27 — Groupes de signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-03 — Au portail, le signataire externe vérifie son identité, lit le document, consent et signe ; un autre refuse de signer en motivant

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-27
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trousse lancée, signataire externe invité (jeton lu dans le courriel)
2. Ouvrir l'écran `/portail/${jeton}`
3. Saisir « Ville du siège ? » : `Québec`
4. Activer « Vérifier mon identité »
5. Activer « Lire / télécharger »
6. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
7. Saisir « Votre nom complet » : `Marie Tremblay`
8. Activer « Signer »
9. Voir « Merci, votre signature est enregistrée »
10. Préparer par l'API : Deuxième trousse, signataire invité
11. Ouvrir l'écran `/portail/${jeton}`
12. Saisir « Ville du siège ? » : `Québec`
13. Activer « Vérifier mon identité »
14. Activer « Refuser de signer ou signaler un problème »
15. Saisir « Motif » : `Les montants ne correspondent pas à l'entente.`
16. Activer « Confirmer le refus »

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-28 — Signature simultanée et consécutive configurable

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-29 — Association automatique des étapes aux signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Trousse écran ${unique}`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « contrat-${unique}.pdf » au champ « Téléverser un document »
6. Voir « contrat-${unique}.pdf »
7. Joindre « annexe-${unique}.pdf » au champ « Téléverser un document »
8. Voir « annexe-${unique}.pdf »
9. Activer « Prévisualiser — contrat-${unique}.pdf » (button)
10. Télécharger par « Télécharger — contrat-${unique}.pdf » et obtenir un fichier non vide
11. Activer « Remplacer par une nouvelle version — contrat-${unique}.pdf » (button)
12. Joindre « contrat-v2-${unique}.pdf » au champ « Fichier »
13. Saisir « Motif » : `Correction E2E`
14. Activer « Remplacer »
15. Activer « Retirer avant lancement — annexe-${unique}.pdf » (button)
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Voir « Marie E2E ${unique} »
22. Saisir « Nom » : `Retiré E2E ${unique}` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550191`
23. Activer « Ajouter un signataire »
24. Activer « Retirer — Retiré E2E ${unique} » (button)
25. Activer « Continuer »
26. Activer « Ajouter une étape »
27. Saisir « Clé » : `s1` ; « Libellé » : `Signature`
28. Choisir « Type » : `SIGNATURE`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `(premier)`
32. Activer « Tester le parcours »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Activer « Preuve » (tab)
38. Activer « Vérifier l’intégrité »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-30 — Modification et ajout de signataires en cours de processus

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Compte jetable qui quitte l'organisation, avec une étape de validation ouverte à son nom
2. Ouvrir l'écran `/clm/admin`
3. Ouvrir l'écran `/clm/admin/notifications`
4. Activer « Modifier »
5. Saisir « Sujet » : `Invitation E2E ${unique}`
6. Activer « Enregistrer »
7. Saisir « Destinataire de test » : `test.${unique}@iq.example`
8. Activer « Envoyer un test »
9. Ouvrir l'écran `/clm/admin/integrations`
10. Activer « Tester la connexion »
11. Activer « Relayer maintenant »
12. Activer « Configurer la connexion »
13. Dans la section « Configurer la connexion », activer « Enregistrer »
14. Activer « Configurer la connexion »
15. Activer « Rétablir la connexion de la plateforme »
16. Saisir « Nouveau client d’intégration » Nom » : `Client E2E ${unique}`
17. Activer « Créer le client »
18. Activer « J’ai copié les valeurs »
19. Activer « Révoquer — Client E2E ${unique} » (button)
20. Ouvrir l'écran `/clm/admin/parametres`
21. Activer « Modifier un paramètre autorisé »
22. Saisir « Rappel tous les (jours) » : `4`
23. Activer « Enregistrer »
24. Activer « Modifier un paramètre autorisé »
25. Activer « Rétablir les paramètres de la plateforme »
26. Ouvrir l'écran `/clm/admin/listes`
27. Ouvrir l'écran `/clm/admin/groupes`
28. Saisir « Nouveau groupe » Code » : `GRP_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Groupe écran ${unique}`
29. Activer « Créer le groupe »
30. Choisir « Ajouter un membre au groupe GRP_${UNIQUE} » Membre » : `initiateur@iq.example`
31. Dans la section « Ajouter un membre au groupe GRP_${UNIQUE} », activer « Ajouter »
32. Activer « Retirer initiateur@iq.example du groupe GRP_${UNIQUE} » (button)
33. Activer « Supprimer le groupe GRP_${UNIQUE} » (button)
34. Ouvrir l'écran `/clm/admin/departs`
35. Saisir « Courriel de la personne qui part » : `depart.${unique}@iq.example`
36. Activer « Consulter »
37. Choisir « Personne qui reprend » : `(premier)`
38. Activer « Transférer »
39. Ouvrir l'écran `/clm/admin/journal`
40. Saisir « Acteur » : `initiateur@iq.example`
41. Activer « Rechercher #2 »
42. Ouvrir l'écran `/clm/admin/audit`
43. Ouvrir l'écran `/clm/options`
44. Ouvrir l'écran `/clm/profil`
45. Ouvrir l'écran `/clm/securite`

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié ; trousse en validation et trousse en signature interne, lancées
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Choisir « Étape à relancer » : `(premier)`
4. Activer « Relancer l’étape »
5. Activer « Parcours » (tab)
6. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation » (ligne ou carte cliquable)
7. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Absence du validateur`
8. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
9. Activer « Documents (1) » (tab)
10. Activer « Analyser (IA) — piece-trousse-${unique}.pdf » (button)
11. Saisir « Justification » : `Résultat conforme au document`
12. Activer « Valider le résultat »
13. Activer « Preuve » (tab)
14. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
15. Répondre « Annulation E2E » à la prochaine saisie demandée par le navigateur
16. Activer « Annuler la trousse »
17. Ouvrir l'écran `/clm/trousses`
18. Télécharger par « Exporter la liste (CSV) » et obtenir un fichier non vide
19. Ouvrir l'écran `/clm/suivi`
20. Télécharger par « Exporter (CSV) » et obtenir un fichier non vide
21. Ouvrir l'écran `/clm/taches`
22. Activer « Nouvelle tâche »
23. Saisir « Tâche » : `Tâche E2E ${unique}`
24. Choisir « Assignée à » : `initiateur@iq.example` ; « Trousse liée » : `(premier)`
25. Activer « Confier la tâche »
26. Activer « Démarrer — Tâche E2E ${unique} » (button)
27. Activer « Réassigner — Tâche E2E ${unique} » (button)
28. Choisir « Nouvelle personne assignée » : `(premier)`
29. Activer « Confirmer la réassignation »
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot E2E ${unique}` ; « Dossiers, un par ligne » : `Dossier A ${unique} ; Marie Lot ; marie.lot.${unique}@agrinord.example ; +15145550190`
32. Choisir « Modèle de contrat » : `Convention lot ${unique}`
33. Activer « Créer le lot »
34. Activer « Approuver le lot — Lot E2E ${unique} » (button)
35. Se connecter en tant que validateur IQ
36. Ouvrir l'écran `/clm/taches`
37. Activer « Traiter — ${trousseRef} Validation »
38. Saisir « Courriel du délégataire » : `approbateur@iq.example` ; « Motif » : `Vacances`
39. Activer « Confirmer la délégation »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — ${trousseSigRef} Signature »
43. Activer « Signer maintenant »

### E2E-IQ-10 — Modifier les signataires d'une trousse en cours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-31 — Réassignation de signataires sans reprise complète

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié ; trousse en validation et trousse en signature interne, lancées
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Choisir « Étape à relancer » : `(premier)`
4. Activer « Relancer l’étape »
5. Activer « Parcours » (tab)
6. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation » (ligne ou carte cliquable)
7. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Absence du validateur`
8. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
9. Activer « Documents (1) » (tab)
10. Activer « Analyser (IA) — piece-trousse-${unique}.pdf » (button)
11. Saisir « Justification » : `Résultat conforme au document`
12. Activer « Valider le résultat »
13. Activer « Preuve » (tab)
14. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
15. Répondre « Annulation E2E » à la prochaine saisie demandée par le navigateur
16. Activer « Annuler la trousse »
17. Ouvrir l'écran `/clm/trousses`
18. Télécharger par « Exporter la liste (CSV) » et obtenir un fichier non vide
19. Ouvrir l'écran `/clm/suivi`
20. Télécharger par « Exporter (CSV) » et obtenir un fichier non vide
21. Ouvrir l'écran `/clm/taches`
22. Activer « Nouvelle tâche »
23. Saisir « Tâche » : `Tâche E2E ${unique}`
24. Choisir « Assignée à » : `initiateur@iq.example` ; « Trousse liée » : `(premier)`
25. Activer « Confier la tâche »
26. Activer « Démarrer — Tâche E2E ${unique} » (button)
27. Activer « Réassigner — Tâche E2E ${unique} » (button)
28. Choisir « Nouvelle personne assignée » : `(premier)`
29. Activer « Confirmer la réassignation »
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot E2E ${unique}` ; « Dossiers, un par ligne » : `Dossier A ${unique} ; Marie Lot ; marie.lot.${unique}@agrinord.example ; +15145550190`
32. Choisir « Modèle de contrat » : `Convention lot ${unique}`
33. Activer « Créer le lot »
34. Activer « Approuver le lot — Lot E2E ${unique} » (button)
35. Se connecter en tant que validateur IQ
36. Ouvrir l'écran `/clm/taches`
37. Activer « Traiter — ${trousseRef} Validation »
38. Saisir « Courriel du délégataire » : `approbateur@iq.example` ; « Motif » : `Vacances`
39. Activer « Confirmer la délégation »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — ${trousseSigRef} Signature »
43. Activer « Signer maintenant »

### E2E-IQ-10 — Modifier les signataires d'une trousse en cours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-32 — Modèles réutilisables

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-33 — Partage des modèles par groupe d'usagers

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-34 — Ancrage précis des champs

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-35 — Types : texte validé, liste, date, case à cocher

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-36 — Distinguer champs de formulaire, de saisie et de signature

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-37 — Attribution des champs à des signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-02 — Une trousse naît d'un dossier Salesforce et d'un modèle : recherche et import, valeur corrigée, contrat généré en PDF et en Word, zone de signature attribuée, lancement ; le validateur commente puis valide sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-14, IQ-C-37, IQ-C-12
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié (champs, zone de signature, clause conditionnelle, parcours de validation)
2. Ouvrir l'écran `/clm/trousses/nouvelle`
3. Cocher « Salesforce »
4. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0001`
5. Activer « Rechercher #2 »
6. Saisir « Titre » : `Trousse modèle ${unique}` ; « Référence externe » : `REF-${UNIQUE}`
7. Choisir « Modèle de contrat » : `Convention écran ${unique}`
8. Activer « Enregistrer le brouillon »
9. Activer « Importer / actualiser »
10. Activer « Corriger »
11. Saisir « Nouvelle valeur » : `Coopérative E2E ${unique}`
12. Dans la section « Corriger la valeur », activer « Enregistrer »
13. Activer « Continuer »
14. Activer « Générer depuis le modèle »
15. Activer « Générer en Word modifiable »
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
22. Activer « Attribuer la zone — Signature du bénéficiaire »
23. Activer « Continuer »
24. Activer « Continuer »
25. Activer « Lancer le parcours »
26. Arriver sur une adresse de la forme `/clm/trousses/\d+`
27. Se connecter en tant que validateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — REF-${UNIQUE} Validation juridique »
30. Saisir « Nouveau commentaire » : `Vérifié par l'e2e`
31. Dans la section « Commentaires contextualisés », activer « Ajouter un commentaire »
32. Voir « Vérifié par l'e2e »
33. Activer « Valider »

### E2E-ECR-IQ-04 — Un modèle se crée et se rédige à l'écran : champ ajouté, vérification, aperçu, gabarit Word déposé, téléchargé puis retiré, soumission ; l'administration le publie, puis une nouvelle version s'ouvre

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-45, IQ-C-37
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Arriver sur une adresse de la forme `/clm/modeles/\d+`
4. Saisir « Nom » : `Modèle écran ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{beneficiaire}}, pour {{montant}} $.
{{clauses}}`
5. Activer « Ajouter un champ »
6. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
9. Activer « Enregistrer le brouillon »
10. Activer « Vérifier »
11. Activer « Tester la règle »
12. Joindre « gabarit-${unique}.docx » au champ « Document Word (.docx) »
13. Activer « Déposer le gabarit »
14. Télécharger par « Télécharger le gabarit » et obtenir un fichier non vide
15. Activer « Retirer le gabarit »
16. Activer « Soumettre à publication »
17. Se connecter en tant que administration d'affaires IQ
18. Ouvrir l'écran `/clm/modeles`
19. Dans la ligne « Modèle écran ${unique} », activer « Publier »
20. Dans la ligne « Modèle écran ${unique} », activer « Nouvelle version »

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-38 — Règles conditionnelles d'affichage

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-39 — Saisie assistée (suggestions)

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-04 — Générer un contrat depuis un modèle, prérempli par Salesforce

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-39
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-40 — Notifications activables et désactivables

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-41 — Rappels automatiques de non-réponse

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-42 — Destinataires en copie

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-43 — Vue de suivi centralisée : étapes, attentes, exceptions

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-16 — Suivi centralisé des trousses et des échéances

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-C-25, IQ-D-08
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-44 — Plusieurs documents par trousse

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Trousse écran ${unique}`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « contrat-${unique}.pdf » au champ « Téléverser un document »
6. Voir « contrat-${unique}.pdf »
7. Joindre « annexe-${unique}.pdf » au champ « Téléverser un document »
8. Voir « annexe-${unique}.pdf »
9. Activer « Prévisualiser — contrat-${unique}.pdf » (button)
10. Télécharger par « Télécharger — contrat-${unique}.pdf » et obtenir un fichier non vide
11. Activer « Remplacer par une nouvelle version — contrat-${unique}.pdf » (button)
12. Joindre « contrat-v2-${unique}.pdf » au champ « Fichier »
13. Saisir « Motif » : `Correction E2E`
14. Activer « Remplacer »
15. Activer « Retirer avant lancement — annexe-${unique}.pdf » (button)
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Voir « Marie E2E ${unique} »
22. Saisir « Nom » : `Retiré E2E ${unique}` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550191`
23. Activer « Ajouter un signataire »
24. Activer « Retirer — Retiré E2E ${unique} » (button)
25. Activer « Continuer »
26. Activer « Ajouter une étape »
27. Saisir « Clé » : `s1` ; « Libellé » : `Signature`
28. Choisir « Type » : `SIGNATURE`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `(premier)`
32. Activer « Tester le parcours »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Activer « Preuve » (tab)
38. Activer « Vérifier l’intégrité »

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-45 — Dossiers, documents, versions, intervenants

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Trousse écran ${unique}`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « contrat-${unique}.pdf » au champ « Téléverser un document »
6. Voir « contrat-${unique}.pdf »
7. Joindre « annexe-${unique}.pdf » au champ « Téléverser un document »
8. Voir « annexe-${unique}.pdf »
9. Activer « Prévisualiser — contrat-${unique}.pdf » (button)
10. Télécharger par « Télécharger — contrat-${unique}.pdf » et obtenir un fichier non vide
11. Activer « Remplacer par une nouvelle version — contrat-${unique}.pdf » (button)
12. Joindre « contrat-v2-${unique}.pdf » au champ « Fichier »
13. Saisir « Motif » : `Correction E2E`
14. Activer « Remplacer »
15. Activer « Retirer avant lancement — annexe-${unique}.pdf » (button)
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Voir « Marie E2E ${unique} »
22. Saisir « Nom » : `Retiré E2E ${unique}` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550191`
23. Activer « Ajouter un signataire »
24. Activer « Retirer — Retiré E2E ${unique} » (button)
25. Activer « Continuer »
26. Activer « Ajouter une étape »
27. Saisir « Clé » : `s1` ; « Libellé » : `Signature`
28. Choisir « Type » : `SIGNATURE`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `(premier)`
32. Activer « Tester le parcours »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Activer « Preuve » (tab)
38. Activer « Vérifier l’intégrité »

### E2E-ECR-IQ-04 — Un modèle se crée et se rédige à l'écran : champ ajouté, vérification, aperçu, gabarit Word déposé, téléchargé puis retiré, soumission ; l'administration le publie, puis une nouvelle version s'ouvre

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-45, IQ-C-37
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Arriver sur une adresse de la forme `/clm/modeles/\d+`
4. Saisir « Nom » : `Modèle écran ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{beneficiaire}}, pour {{montant}} $.
{{clauses}}`
5. Activer « Ajouter un champ »
6. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
9. Activer « Enregistrer le brouillon »
10. Activer « Vérifier »
11. Activer « Tester la règle »
12. Joindre « gabarit-${unique}.docx » au champ « Document Word (.docx) »
13. Activer « Déposer le gabarit »
14. Télécharger par « Télécharger le gabarit » et obtenir un fichier non vide
15. Activer « Retirer le gabarit »
16. Activer « Soumettre à publication »
17. Se connecter en tant que administration d'affaires IQ
18. Ouvrir l'écran `/clm/modeles`
19. Dans la ligne « Modèle écran ${unique} », activer « Publier »
20. Dans la ligne « Modèle écran ${unique} », activer « Nouvelle version »

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-46 — PDF et Word

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-47 — Téléversement d'au moins 25 Mo

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-02 — Dépôt de documents volumineux dans une trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-47
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.documents.taille`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-48 — Historique des étapes de validation, d'approbation, de signature

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-49 — Journal d'audit des actions (consultation, validation, signature, rejet, annulation…)

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-50 — État et historique des transactions d'un document

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Modèle publié ; trousse en validation et trousse en signature interne, lancées
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Choisir « Étape à relancer » : `(premier)`
4. Activer « Relancer l’étape »
5. Activer « Parcours » (tab)
6. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation » (ligne ou carte cliquable)
7. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Absence du validateur`
8. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
9. Activer « Documents (1) » (tab)
10. Activer « Analyser (IA) — piece-trousse-${unique}.pdf » (button)
11. Saisir « Justification » : `Résultat conforme au document`
12. Activer « Valider le résultat »
13. Activer « Preuve » (tab)
14. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
15. Répondre « Annulation E2E » à la prochaine saisie demandée par le navigateur
16. Activer « Annuler la trousse »
17. Ouvrir l'écran `/clm/trousses`
18. Télécharger par « Exporter la liste (CSV) » et obtenir un fichier non vide
19. Ouvrir l'écran `/clm/suivi`
20. Télécharger par « Exporter (CSV) » et obtenir un fichier non vide
21. Ouvrir l'écran `/clm/taches`
22. Activer « Nouvelle tâche »
23. Saisir « Tâche » : `Tâche E2E ${unique}`
24. Choisir « Assignée à » : `initiateur@iq.example` ; « Trousse liée » : `(premier)`
25. Activer « Confier la tâche »
26. Activer « Démarrer — Tâche E2E ${unique} » (button)
27. Activer « Réassigner — Tâche E2E ${unique} » (button)
28. Choisir « Nouvelle personne assignée » : `(premier)`
29. Activer « Confirmer la réassignation »
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot E2E ${unique}` ; « Dossiers, un par ligne » : `Dossier A ${unique} ; Marie Lot ; marie.lot.${unique}@agrinord.example ; +15145550190`
32. Choisir « Modèle de contrat » : `Convention lot ${unique}`
33. Activer « Créer le lot »
34. Activer « Approuver le lot — Lot E2E ${unique} » (button)
35. Se connecter en tant que validateur IQ
36. Ouvrir l'écran `/clm/taches`
37. Activer « Traiter — ${trousseRef} Validation »
38. Saisir « Courriel du délégataire » : `approbateur@iq.example` ; « Motif » : `Vacances`
39. Activer « Confirmer la délégation »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — ${trousseSigRef} Signature »
43. Activer « Signer maintenant »

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-51 — Conservation et consultation du dossier de preuve

- **Source** : 1.4.7
- **Conformité déclarée** : ✅

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Trousse écran ${unique}`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « contrat-${unique}.pdf » au champ « Téléverser un document »
6. Voir « contrat-${unique}.pdf »
7. Joindre « annexe-${unique}.pdf » au champ « Téléverser un document »
8. Voir « annexe-${unique}.pdf »
9. Activer « Prévisualiser — contrat-${unique}.pdf » (button)
10. Télécharger par « Télécharger — contrat-${unique}.pdf » et obtenir un fichier non vide
11. Activer « Remplacer par une nouvelle version — contrat-${unique}.pdf » (button)
12. Joindre « contrat-v2-${unique}.pdf » au champ « Fichier »
13. Saisir « Motif » : `Correction E2E`
14. Activer « Remplacer »
15. Activer « Retirer avant lancement — annexe-${unique}.pdf » (button)
16. Activer « Continuer »
17. Saisir « Nom » : `Marie E2E ${unique}` ; « Courriel » : `marie.${unique}@agrinord.example` ; « Téléphone » : `+15145550190`
18. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
19. Cocher « Externe »
20. Activer « Ajouter un signataire »
21. Voir « Marie E2E ${unique} »
22. Saisir « Nom » : `Retiré E2E ${unique}` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550191`
23. Activer « Ajouter un signataire »
24. Activer « Retirer — Retiré E2E ${unique} » (button)
25. Activer « Continuer »
26. Activer « Ajouter une étape »
27. Saisir « Clé » : `s1` ; « Libellé » : `Signature`
28. Choisir « Type » : `SIGNATURE`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `(premier)`
32. Activer « Tester le parcours »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Activer « Preuve » (tab)
38. Activer « Vérifier l’intégrité »

### E2E-ECR-IQ-03 — Au portail, le signataire externe vérifie son identité, lit le document, consent et signe ; un autre refuse de signer en motivant

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-27
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trousse lancée, signataire externe invité (jeton lu dans le courriel)
2. Ouvrir l'écran `/portail/${jeton}`
3. Saisir « Ville du siège ? » : `Québec`
4. Activer « Vérifier mon identité »
5. Activer « Lire / télécharger »
6. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
7. Saisir « Votre nom complet » : `Marie Tremblay`
8. Activer « Signer »
9. Voir « Merci, votre signature est enregistrée »
10. Préparer par l'API : Deuxième trousse, signataire invité
11. Ouvrir l'écran `/portail/${jeton}`
12. Saisir « Ville du siège ? » : `Québec`
13. Activer « Vérifier mon identité »
14. Activer « Refuser de signer ou signaler un problème »
15. Saisir « Motif » : `Les montants ne correspondent pas à l'entente.`
16. Activer « Confirmer le refus »

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Signataire invité avec vérification par code
2. Ouvrir l'écran `/portail/${jeton}`
3. Activer « Recevoir mon code »
4. Préparer par l'API : cas `iq.signature.externe`
5. Ouvrir l'écran `/clm/admin/journal`
6. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide
7. Ouvrir l'écran `/clm/admin/conservation`
8. Choisir « Type de contrat » : `PRET`
9. Saisir « Durée après la clôture (ans) » : `10`
10. Activer « Enregistrer la règle »
11. Activer « Retirer — Contrat de prêt » (button)
12. Répondre « Litige E2E ${unique} » à la prochaine saisie demandée par le navigateur
13. Activer « Geler — ${trousseRef} » (button)
14. Activer « Lever le gel — ${trousseRef} » (button)
15. Saisir « Clôturées du » : `${aujourdhui}` ; « au » : `${demain}`
16. Télécharger par « Télécharger le lot » et obtenir un fichier non vide
17. Ouvrir l'écran `/clm/admin/sso`
18. Saisir « Groupe (identifiant ou nom) » : `grp-e2e-${unique}`
19. Choisir « Ajouter la correspondance » Rôle » : `LECTEUR`
20. Activer « Ajouter la correspondance »
21. Activer « Retirer — grp-e2e-${unique} » (button)
22. Activer « Créer un jeton | Remplacer le jeton »
23. Préparer par l'API : Administrateur jetable (futur compte de secours)
24. Se connecter avec le compte `secours.${unique}@iq.example`
25. Ouvrir l'écran `/clm/securite`
26. Activer « Configurer le TOTP »
27. Relever le texte affiché (`code`) sous le nom `secretTotp`
28. Saisir le code TOTP courant dans « Code de confirmation »
29. Activer « Activer »
30. Ouvrir l'écran `/clm/admin/sso`
31. Activer « Compte de secours — secours.${unique}@iq.example » (checkbox)
32. Activer « Réserver aux comptes de secours »
33. Activer « Ouvrir à tous les comptes »
34. Activer « Compte de secours — secours.${unique}@iq.example » (checkbox)

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-52 — Minimiser le nombre d'étapes

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-53 — Exécution fluide web et mobile

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
3. Ouvrir l'écran `/clm/trousses`
4. Voir « Trousses »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-54 — Limiter les dépendances entre signataires

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-55 — Flexibilité des workflows en cours de processus

- **Source** : 1.4.8
- **Conformité déclarée** : ✅🆕

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

## IQ-C-56 — Minimiser les interventions manuelles

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-57 — Expérience simple pour initiateurs, signataires internes et externes

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-58 — Personnalisation : image de marque, messages, parcours

- **Source** : 1.4.8
- **Conformité déclarée** : ✅

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-59 — Automatisation et IA (extraction, classification, anomalies)

- **Source** : 1.4.9
- **Conformité déclarée** : ✅

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.ia.analyse`
2. Ouvrir l'écran `/clm`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-60 — Identification et authentification des utilisateurs (SSO)

- **Source** : 1.4.10
- **Conformité déclarée** : ✅

### E2E-IQ-28 — Authentification unique de l'organisation

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-60
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.sso.connexion`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »

### E2E-IQ-29 — Politique de session : l'administration technique fixe l'inactivité et la durée maximale

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-60
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/sso`
2. Voir « Politique de session »
3. Saisir « Délai d'inactivité (minutes) » : `30` ; « Durée maximale de session (minutes) » : `480`
4. Activer « Enregistrer la politique »
5. Voir « Politique de session enregistrée. »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-61 — Authentification et vérification d'identité selon profil, document et risque

- **Source** : 1.4.10
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-14 — Vérification d'identité proportionnée au risque

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-A-23, IQ-C-61
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.identite.par-risque`
2. Ouvrir l'écran `/clm/admin/parametres`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-62 — Méthodes internes et externes, authentification renforcée

- **Source** : 1.4.10
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-63 — Standards reconnus de signature électronique, validité juridique

- **Source** : 1.4.11
- **Conformité déclarée** : ✅🆕

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-64 — Piste d'audit complète : événements, authentification, horodatages, consentements, intégrité

- **Source** : 1.4.11
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-65 — Conservation, export, portabilité durables pendant la période requise

- **Source** : 1.4.11
- **Conformité déclarée** : ✅🆕

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Signataire invité avec vérification par code
2. Ouvrir l'écran `/portail/${jeton}`
3. Activer « Recevoir mon code »
4. Préparer par l'API : cas `iq.signature.externe`
5. Ouvrir l'écran `/clm/admin/journal`
6. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide
7. Ouvrir l'écran `/clm/admin/conservation`
8. Choisir « Type de contrat » : `PRET`
9. Saisir « Durée après la clôture (ans) » : `10`
10. Activer « Enregistrer la règle »
11. Activer « Retirer — Contrat de prêt » (button)
12. Répondre « Litige E2E ${unique} » à la prochaine saisie demandée par le navigateur
13. Activer « Geler — ${trousseRef} » (button)
14. Activer « Lever le gel — ${trousseRef} » (button)
15. Saisir « Clôturées du » : `${aujourdhui}` ; « au » : `${demain}`
16. Télécharger par « Télécharger le lot » et obtenir un fichier non vide
17. Ouvrir l'écran `/clm/admin/sso`
18. Saisir « Groupe (identifiant ou nom) » : `grp-e2e-${unique}`
19. Choisir « Ajouter la correspondance » Rôle » : `LECTEUR`
20. Activer « Ajouter la correspondance »
21. Activer « Retirer — grp-e2e-${unique} » (button)
22. Activer « Créer un jeton | Remplacer le jeton »
23. Préparer par l'API : Administrateur jetable (futur compte de secours)
24. Se connecter avec le compte `secours.${unique}@iq.example`
25. Ouvrir l'écran `/clm/securite`
26. Activer « Configurer le TOTP »
27. Relever le texte affiché (`code`) sous le nom `secretTotp`
28. Saisir le code TOTP courant dans « Code de confirmation »
29. Activer « Activer »
30. Ouvrir l'écran `/clm/admin/sso`
31. Activer « Compte de secours — secours.${unique}@iq.example » (checkbox)
32. Activer « Réserver aux comptes de secours »
33. Activer « Ouvrir à tous les comptes »
34. Activer « Compte de secours — secours.${unique}@iq.example » (checkbox)

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-66 — Protection du cycle de vie, dont la destruction au moment opportun (Loi sur les archives)

- **Source** : 1.4.12
- **Conformité déclarée** : ✅🆕

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-67 — Accès, modèles, workflows, historiques selon rôles, groupes, unités, domaines

- **Source** : 1.4.12
- **Conformité déclarée** : ✅🆕

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.administrations`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Se connecter en tant que auditeur IQ
5. Ouvrir l'écran `/clm/admin/journal`
6. Voir « Journal »

### E2E-IQ-26 — Comptes, groupes et rôles administrés

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-05, IQ-D-18, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.comptes`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-68 — Compatible postes Windows, mobiles, navigateurs Edge, Chrome, Safari

- **Source** : 1.7.1
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Compatibilité navigateurs : Chromium éprouvé par la suite ; Edge partage le moteur ; Safari et postes mobiles à confirmer en recette.

## IQ-C-69 — S'intégrer à Microsoft 365, Salesforce, Workday : API, connecteurs, protocoles

- **Source** : 1.7.1
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-70 — Compatibilité maintenue avec l'évolution de l'environnement

- **Source** : 1.7.1
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Compatibilité maintenue avec l'évolution de l'environnement : engagement de maintenance et versions d'API paramétrables.

## IQ-D-01 — Génération automatisée ou semi-automatisée depuis des modèles approuvés

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-02 — Données des systèmes sources, dont Salesforce

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-03 — Préparation et gestion des documents à valider, approuver, signer

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-04 — Circuits séquentiels, parallèles, mixtes

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-05 — Utilisateurs, signataires, rôles, droits, délégations

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

### E2E-IQ-26 — Comptes, groupes et rôles administrés

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-05, IQ-D-18, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.comptes`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-06 — Modèles et clauses contractuelles

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-07 — Automatisation des notifications, rappels, règles d'affaires

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-08 — Suivi centralisé

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-16 — Suivi centralisé des trousses et des échéances

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-C-25, IQ-D-08
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-09 — Versions et historique des actions

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-10 — Piste d'audit complète

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-11 — Conservation et exportation : documents, preuves, pistes, métadonnées

- **Source** : 3.1.1
- **Conformité déclarée** : ✅🆕

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-12 — Intégration aux systèmes d'IQ

- **Source** : 3.1.1
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-13 — Accessibilité des interfaces, niveau maintenu aux mises à jour

- **Source** : 3.3.1 A
- **Conformité déclarée** : ✅

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
3. Ouvrir l'écran `/clm/trousses`
4. Voir « Trousses »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Voir « Notifications »
3. Passer l'interface en anglais
4. Voir « Notifications »
5. Ouvrir l'écran `/clm/trousses`
6. Voir « Packages »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français

## IQ-D-14 — PCI DSS si des cartes de paiement sont traitées

- **Source** : 3.3.1 B
- **Conformité déclarée** : ✅🆕 — hors périmètre PCI : numéro de carte refusé à la frontière — ADR-022

### E2E-IQ-31 — Aucune donnée de carte de paiement acceptée

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.cartes.pci`
2. Ouvrir l'écran `/clm/taches`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-15 — Gestion des rôles et des droits d'accès

- **Source** : 3.3.2
- **Conformité déclarée** : ✅🆕

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.administrations`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Se connecter en tant que auditeur IQ
5. Ouvrir l'écran `/clm/admin/journal`
6. Voir « Journal »

## IQ-D-16 — Principe du moindre privilège

- **Source** : 3.3.2
- **Conformité déclarée** : ✅

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.administrations`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Se connecter en tant que auditeur IQ
5. Ouvrir l'écran `/clm/admin/journal`
6. Voir « Journal »

## IQ-D-17 — Gestion distincte des utilisateurs et des administrateurs

- **Source** : 3.3.2
- **Conformité déclarée** : ✅

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.administrations`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Se connecter en tant que auditeur IQ
5. Ouvrir l'écran `/clm/admin/journal`
6. Voir « Journal »

## IQ-D-18 — Révocation ou modification des accès

- **Source** : 3.3.2
- **Conformité déclarée** : ✅

### E2E-IQ-26 — Comptes, groupes et rôles administrés

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-05, IQ-D-18, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.comptes`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-19 — Journalisation et traçabilité des activités

- **Source** : 3.3.2
- **Conformité déclarée** : ✅

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-20 — Protection des renseignements et documents

- **Source** : 3.3.2
- **Conformité déclarée** : ✅

### E2E-IQ-30 — Isolation des données entre organisations et modules

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.isolation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « ${trousseRef} »

## IQ-D-21 — Interfaces en français, obligatoirement

- **Source** : 3.3.3
- **Conformité déclarée** : ✅

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Voir « Notifications »
3. Passer l'interface en anglais
4. Voir « Notifications »
5. Ouvrir l'écran `/clm/trousses`
6. Voir « Packages »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français

## IQ-D-22 — Offertes en anglais lorsque requis

- **Source** : 3.3.3
- **Conformité déclarée** : ✅

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Voir « Notifications »
3. Passer l'interface en anglais
4. Voir « Notifications »
5. Ouvrir l'écran `/clm/trousses`
6. Voir « Packages »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français

## IQ-D-23 — Consoles d'administration en français

- **Source** : 3.3.3
- **Conformité déclarée** : ✅

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Voir « Notifications »
3. Passer l'interface en anglais
4. Voir « Notifications »
5. Ouvrir l'écran `/clm/trousses`
6. Voir « Packages »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français

## IQ-D-24 — Mode SaaS principalement

- **Source** : 3.3.4
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Mode SaaS : modèle de service.

## IQ-D-25 — Fonctionnalités natives privilégiées, peu de développements spécifiques

- **Source** : 3.3.4
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Préférence pour les fonctionnalités natives : configuration par l'organisation, démontrée par les scénarios de paramétrage.

## IQ-D-26 — Intégrations avec l'écosystème, dont Salesforce

- **Source** : 3.3.5
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-27 — Échanges de données, documents, modèles, statuts, signataires, instructions, preuves

- **Source** : 3.3.5
- **Conformité déclarée** : ✅🆕

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-28 — API et connecteurs sécurisés, documentés, maintenus

- **Source** : 3.3.6
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-29 — Interopérabilité permettant l'évolution

- **Source** : 3.3.8
- **Conformité déclarée** : ✅

### E2E-IQ-23 — Interfaces standards et documentées (OpenAPI, SCIM)

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-29, IQ-C-16
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.api.documentee`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-S-01 — Création : trousse, documents, participants, positionnement des champs, modèle, configuration

- **Source** : 7.3 sc. 1
- **Conformité déclarée** : ✅

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-02 — Validation : réception, consultation, commentaires, validation, passage à l'étape suivante

- **Source** : 7.3 sc. 2
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-03 — Approbation, rejet, retour pour correction, historique

- **Source** : 7.3 sc. 3
- **Conformité déclarée** : ✅

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-04 — Signature externe : invitation, authentification, consultation, signature, confirmation

- **Source** : 7.3 sc. 4
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-05 — Ajout, remplacement, délégation, traçabilité ; mise à jour d'une donnée et son impact

- **Source** : 7.3 sc. 5
- **Conformité déclarée** : ✅🆕

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.parallele-reassignation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Réassignation », « Délégation »
5. Activer « Parcours » (tab)
6. Voir « en parallèle »

### E2E-IQ-10 — Modifier les signataires d'une trousse en cours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-06 — Rappels configurés, notifications, rappels manuels, alertes d'échéance, communications personnalisées

- **Source** : 7.3 sc. 6
- **Conformité déclarée** : ✅

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-07 — Journal, horodatages, actions, authentification, dossier de preuve

- **Source** : 7.3 sc. 7
- **Conformité déclarée** : ✅

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ❌ échoué

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-08 — Consultation et décision sur mobile, navigation mobile

- **Source** : 7.3 sc. 8
- **Conformité déclarée** : ✅

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
3. Ouvrir l'écran `/clm/trousses`
4. Voir « Trousses »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-09 — Refus, commentaires justificatifs, retour à l'étape appropriée, reprise, historique

- **Source** : 7.3 sc. 9
- **Conformité déclarée** : ✅🆕

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.validation-approbation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Parcours » (tab)
4. Voir « Résumé lisible du parcours »
5. Activer « Historique » (tab)
6. Voir « Décision : approuvé »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-07 — Rejet par un validateur et retour à l'initiateur

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-S-09
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.rejet`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Décision : rejeté »

## IQ-S-10 — Téléchargement du signé, export du certificat, du journal, des métadonnées ; formats ; portabilité

- **Source** : 7.3 sc. 10
- **Conformité déclarée** : ✅

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-10b — Gestion de bout en bout d'un CLM

- **Source** : 7.3 sc. 10
- **Conformité déclarée** : ✅

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.ia.analyse`
2. Ouvrir l'écran `/clm`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-01 — TI-22 : intégration Workday (déclenchement, statuts, document signé)

- **Source** : Add. § 4.4
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-02 — AF-47 : lot de signatures depuis Salesforce

- **Source** : Add. Q10
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-03 — AF-47 : lot depuis la plateforme

- **Source** : Add. Q10
- **Conformité déclarée** : ✅

### E2E-IQ-22 — Signature en lot et approbation globale

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-03, IQ-A-28
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.lots`
2. Ouvrir l'écran `/clm/lots`
3. Voir « Signature en lot »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-04 — SIG-07/08 : méthodes d'authentification standard et renforcée décrites

- **Source** : Add. Q4
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Méthodes d'authentification décrites dans la soumission ; leur comportement est éprouvé par IQ-C-61 et IQ-C-62.

## IQ-A-05 — CLM optionnel intégrable à Salesforce lorsqu'il est proposé

- **Source** : Add. Q1
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-06 — Environ 30 champs Salesforce préremplis par modèle

- **Source** : Add. Q8
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-07 — Intégrations Microsoft 365, Workday, Entra ID

- **Source** : Add. Q3
- **Conformité déclarée** : ✅

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-08 — Provisionnement et déprovisionnement SCIM 2.0 (Saviynt)

- **Source** : Add. Q3, add. 3 Q51
- **Conformité déclarée** : ✅

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-09 — Connecteur MCP

- **Source** : Add. Q3
- **Conformité déclarée** : ✅🆕 — connecteur MCP en lecture seule — ADR-023

### E2E-IQ-24 — Connecteur MCP en lecture seule pour un assistant d'IA

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-09
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.mcp`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-A-10 — Authentification Salesforce de remplacement (JWT Bearer) décrite

- **Source** : Add. 2 Q11
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-11 — Exportation ou transfert des documents et métadonnées vers un référentiel désigné par IQ

- **Source** : Add. 2 Q13
- **Conformité déclarée** : ✅

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-12 — Retour des statuts lancée, retournée, rejetée, annulée, terminée

- **Source** : Add. 2 Q14
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-13 — Seules les données nécessaires transférées depuis Salesforce

- **Source** : Add. 2 Q15
- **Conformité déclarée** : ✅

### E2E-IQ-21 — Minimisation des données échangées avec Salesforce

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.minimisation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « Trousses »

## IQ-A-14 — Limites d'API, surveillance, journalisation, reprise (8 tentatives, plafond 1 h)

- **Source** : Add. 2 Q17
- **Conformité déclarée** : ✅

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-15 — Interfaces internes et externes en français, en anglais sur demande d'IQ

- **Source** : Add. 2 Q19
- **Conformité déclarée** : ✅

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Voir « Notifications »
3. Passer l'interface en anglais
4. Voir « Notifications »
5. Ouvrir l'écran `/clm/trousses`
6. Voir « Packages »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français

## IQ-A-16 — Démontrer la volumétrie (55 000 à 65 000 documents par an) et des variations raisonnables

- **Source** : Add. 2 Q4
- **Conformité déclarée** : ✅
- **Vérification** : documentaire — Volumétrie démontrée par le harnais de performance JMeter (plans, seuils SLI/SLO).

## IQ-A-18 — Génération Word modifiable avant l'approbation finale ; nouvelle version et reprise des validations ; document à signer figé

- **Source** : Add. 3 Q26
- **Conformité déclarée** : ✅

### E2E-IQ-05 — Gabarit Word d'IQ fusionné en Word et en PDF

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-30, IQ-A-18
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.gabarit.word`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « Documents »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-19 — Mappage configurable des groupes et attributs Entra ID vers les rôles, moindre privilège, traçabilité

- **Source** : Add. 3 Q43
- **Conformité déclarée** : ✅

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-20 — Domaine d'affaires alimenté par un groupe ou un attribut d'annuaire, séparation logique

- **Source** : Add. 3 Q44
- **Conformité déclarée** : ✅

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-21 — Conservation par catégorie, gel légal, trois sorts finaux, bordereau ou certificat de destruction

- **Source** : Add. 3 Q46-Q48
- **Conformité déclarée** : ✅

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-22 — Mesure de l'utilisation par enveloppe envoyée, sans double comptage

- **Source** : Add. 3 Q39
- **Conformité déclarée** : ✅

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-23 — Niveaux d'authentification configurables selon le type de document et le processus

- **Source** : Add. 4 Q1
- **Conformité déclarée** : ✅

### E2E-IQ-14 — Vérification d'identité proportionnée au risque

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-A-23, IQ-C-61
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.identite.par-risque`
2. Ouvrir l'écran `/clm/admin/parametres`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-24 — Signature par un représentant autorisé ; preuve d'autorité ; qualité du signataire tracée

- **Source** : Add. 4 Q6
- **Conformité déclarée** : ✅

### E2E-IQ-13 — Signature d'un représentant après attestation de son autorité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-24
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.representant`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »

## IQ-A-25 — Parcours conditionnels selon type, unité, rôle, montant

- **Source** : Add. 4 Q8, Q20
- **Conformité déclarée** : ✅

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

## IQ-A-26 — Séparation de l'administration d'affaires et de l'administration technique

- **Source** : Add. 4 Q10
- **Conformité déclarée** : ✅

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.administrations`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Se connecter en tant que auditeur IQ
5. Ouvrir l'écran `/clm/admin/journal`
6. Voir « Journal »

## IQ-A-27 — Mappage des attributs et groupes SCIM, désactivation rapide

- **Source** : Add. 4 Q31
- **Conformité déclarée** : ✅

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-28 — Approbation adaptée au lot, traçabilité par dossier

- **Source** : Add. 4 Q36
- **Conformité déclarée** : ✅

### E2E-IQ-22 — Signature en lot et approbation globale

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-03, IQ-A-28
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.lots`
2. Ouvrir l'écran `/clm/lots`
3. Voir « Signature en lot »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-29 — Export dans des formats pérennes, PDF/PDF-A lorsque disponibles, export massif

- **Source** : Add. 4 Q14
- **Conformité déclarée** : ✅

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Se connecter en tant que administration d'affaires IQ
6. Ouvrir l'écran `/clm/admin/journal`
7. Voir « Journal »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-30 — Modèles Word d'IQ, structure et présentation préservées dans la mesure permise ; limites déclarées

- **Source** : Add. 4 Q22
- **Conformité déclarée** : ✅

### E2E-IQ-05 — Gabarit Word d'IQ fusionné en Word et en PDF

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-30, IQ-A-18
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.gabarit.word`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « Documents »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-17 — FundingRequest lu par Id ou Name, correspondance configurable par modèle

- **Source** : Add. 2 Q7
- **Conformité déclarée** : ✅

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
