# Investissement Québec — AO 2026-6370-0005 — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## IQ-C-01 — Gérer le cycle de validation, d'approbation et de signature, orchestration comprise

- **Source** : 1.3
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-04 — Capacités avancées du cycle de vie des contrats

- **Source** : 1.3
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-07 — Protection et confidentialité des données

- **Source** : 1.4.3
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-AX-IQ-25 — SEC-28 (RST-54) : les documents d'IQ sont chiffrés par une clé dédiée à l'organisation ; l'administration technique pivote la clé sans interruption puis réenveloppe les documents, qui restent lisibles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Un document déposé par l'initiateur : la première écriture crée la clé de données de l'organisation
2. Ouvrir l'écran `/clm/admin/chiffrement`
3. Voir le titre « Chiffrement au repos »
4. Voir « Clé de l’organisation », « Clé maîtresse active », « Active »
5. Activer « Pivoter la clé »
6. Voir « Rotation effectuée »
7. Voir « Retirée (lecture seule) »
8. Préparer par l'API : Après la rotation, le document chiffré par l'ancienne version reste lisible
9. Activer « Réenvelopper les documents »
10. Voir « Réenveloppement : »
11. Préparer par l'API : Après le réenveloppement, le document se lit toujours en clair par l'API
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-30 — Isolation des données entre organisations et modules

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.isolation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « ${trousseRef} »

## IQ-C-08 — Échange avec les systèmes de référence : préalimenter, retourner les statuts

- **Source** : 1.4.4
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-09 — Récupération automatisée des données Salesforce Grantmaking

- **Source** : 1.4.5
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : parcours d'interface (27 gestes)
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-14 — Traçabilité dossier source → document → preuve

- **Source** : 1.4.5
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-ECR-IQ-02 — Une trousse naît d'un dossier Salesforce et d'un modèle : recherche et import, valeur corrigée, contrat généré en PDF et en Word, zone de signature attribuée, lancement ; le validateur commente puis valide sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-14, IQ-C-37, IQ-C-12
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (27 gestes)
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-C-16 — Interfaces standard, documentées et supportées par l'éditeur

- **Source** : 1.4.5
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-23 — Interfaces standards et documentées (OpenAPI, SCIM)

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-29, IQ-C-16
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.api.documentee`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-C-17 — Flexibilité et agilité tout au long de l'utilisation

- **Source** : 1.4.6
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-23 — Flexibilité en cours d'utilisation : l'organisation ajoute un type de contrat, change un paramètre et fait évoluer un modèle, sans développement ; les trousses suivantes en tiennent compte

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-55, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_TYPE_CONTRAT`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `PILOTE_${UNIQUE}` ; « Libellé » : `Entente pilote ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Ouvrir l'écran `/clm/admin/parametres`
8. Activer « Modifier un paramètre autorisé »
9. Saisir « Rappel tous les (jours) » : `6`
10. Activer « Enregistrer »
11. Voir « Paramètres enregistrés. »
12. Se connecter en tant que initiateur IQ
13. Ouvrir l'écran `/clm/trousses/nouvelle`
14. Constater la valeur « Rappel tous les (jours) » : `6`
15. Saisir « Titre » : `Flexible CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-FLX`
16. Choisir « Type de contrat » : `Entente pilote ${unique}`
17. Activer « Enregistrer le brouillon »
18. Voir « CB${UNIQUE}-FLX »
19. Se connecter en tant que administration d'affaires IQ
20. Ouvrir l'écran `/clm/modeles`
21. Activer « Créer un modèle »
22. Saisir « Nom » : `Évolutif CB ${unique}` ; « Corps du contrat » : `Version 1 de l'entente. {{clauses}}`
23. Activer « Soumettre à publication »
24. Voir « soumis à publication »
25. Se connecter en tant que administration principale IQ
26. Ouvrir l'écran `/clm/modeles`
27. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
28. Voir « « Évolutif CB ${unique} » publié. »
29. Se connecter en tant que administration d'affaires IQ
30. Ouvrir l'écran `/clm/modeles`
31. Dans la ligne « Évolutif CB ${unique} », activer « Nouvelle version »
32. Voir « Nouvelle version de « Évolutif CB ${unique} » créée. »
33. Activer « Modifier le brouillon — Évolutif CB ${unique} » (link)
34. Saisir « Corps du contrat » : `Version 2 de l'entente, clause de résiliation ajoutée. {{clauses}}`
35. Activer « Soumettre à publication »
36. Voir « soumis à publication »
37. Se connecter en tant que administration principale IQ
38. Ouvrir l'écran `/clm/modeles`
39. Saisir « Rechercher » : `Évolutif CB ${unique}`
40. Choisir « État » : `EN_REVISION`
41. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
42. Voir « publié. »
43. Choisir « État » : `RETIRE`
44. Voir « Évolutif CB ${unique} »
45. Ouvrir l'écran `/clm/admin/parametres`
46. Activer « Modifier un paramètre autorisé »
47. Activer « Rétablir les paramètres de la plateforme »
48. Voir « Paramètres de la plateforme rétablis. »

### E2E-CB-IQ-24 — Listes de référence configurées par l'administration de l'organisation : chaque valeur ajoutée, renommée ou retirée se répercute aussitôt dans les listes déroulantes des écrans d'affaires

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (36 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_UNITE`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `INNOV_${UNIQUE}` ; « Libellé » : `Innovation ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Voir « Innovation ${unique} »
8. Choisir « Liste » : `CLM_UNITE`
9. Activer « Ajouter une valeur »
10. Saisir « Code » : `ANCIENNE_${UNIQUE}` ; « Libellé » : `Unité dissoute ${unique}`
11. Activer « Enregistrer »
12. Voir « Valeur enregistrée. »
13. Voir « Unité dissoute ${unique} »
14. Activer « Retirer — Unité dissoute ${unique} » (button)
15. Voir « Valeur retirée des sélecteurs. »
16. Choisir « Liste » : `CLM_ROLE_CONTRAT`
17. Activer « Ajouter une valeur »
18. Saisir « Code » : `MANDATAIRE_${UNIQUE}` ; « Libellé » : `Mandataire ${unique}`
19. Activer « Enregistrer »
20. Voir « Valeur enregistrée. »
21. Voir « Mandataire ${unique} »
22. Choisir « Liste » : `CLM_CHAMP_SALESFORCE`
23. Activer « Ajouter une valeur »
24. Saisir « Code » : `Region_${UNIQUE}` ; « Libellé » : `Région administrative ${unique}`
25. Activer « Enregistrer »
26. Voir « Valeur enregistrée. »
27. Voir « Région administrative ${unique} »
28. Choisir « Liste » : `CLM_PRIORITE_TACHE`
29. Activer « Ajouter une valeur »
30. Saisir « Code » : `CRITIQUE_${UNIQUE}` ; « Libellé » : `Critique ${unique}`
31. Activer « Enregistrer »
32. Voir « Valeur enregistrée. »
33. Voir « Critique ${unique} »
34. Choisir « Liste » : `CLM_TYPE_CONTRAT`
35. Activer « Ajouter une valeur »
36. Saisir « Code » : `ACCORD_${UNIQUE}` ; « Libellé » : `Accord cadre ${unique}`
37. Activer « Enregistrer »
38. Voir « Valeur enregistrée. »
39. Voir « Accord cadre ${unique} »
40. Activer « Modifier — Accord cadre ${unique} » (button)
41. Saisir « Libellé » : `Accord-cadre de partenariat ${unique}`
42. Activer « Enregistrer »
43. Voir « Accord-cadre de partenariat ${unique} »
44. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
45. Se connecter en tant que initiateur IQ
46. Ouvrir l'écran `/clm/trousses/nouvelle`
47. Constater que « Unité responsable » propose « Innovation ${unique} » et ne propose plus « Unité dissoute ${unique} » ; Constater que « Type de contrat » propose « Accord-cadre de partenariat ${unique} » et ne propose plus « Accord cadre ${unique} »
48. Saisir « Titre » : `Références CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REF`
49. Choisir « Unité responsable » : `Innovation ${unique}` ; « Type de contrat » : `Accord-cadre de partenariat ${unique}`
50. Activer « Enregistrer le brouillon »
51. Activer « Continuer »
52. Activer « Continuer »
53. Constater que « Rôle dans le contrat » propose « Mandataire ${unique} »
54. Ouvrir l'écran `/clm/taches`
55. Activer « Nouvelle tâche »
56. Constater que « Priorité » propose « Critique ${unique} »
57. Se connecter en tant que administration d'affaires IQ
58. Ouvrir l'écran `/clm/modeles`
59. Activer « Créer un modèle »
60. Activer « Ajouter un champ »
61. Constater que « Source Salesforce » propose « Région administrative ${unique} »

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-18 — Circuits de validation et d'approbation séquentiels, parallèles, mixtes

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-05 — Validations en parallèle : un validateur rejette en motivant, la trousse est rejetée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-22, IQ-C-23, IQ-D-04, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (26 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PAR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `v2` ; « Libellé » : `Validation financière`
16. Choisir « Type » : `VALIDATION`
17. Saisir « Phase » : `1`
18. Activer « Ajouter un responsable »
19. Saisir « Responsable 1 » : `approbateur@iq.example`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que validateur IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PAR Validation juridique »
29. Activer « Rejeter »
30. Voir « Un motif est obligatoire »
31. Saisir « Commentaire » : `Montant hors programme`
32. Activer « Rejeter »
33. Voir « Décision enregistrée : rejeté. »
34. Se connecter en tant que initiateur IQ
35. Ouvrir l'écran `/clm/trousses`
36. Activer « Ouvrir le détail de CB${UNIQUE}-PAR » (link)
37. Voir « Rejetée »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-08 — Attribution par groupe : l'administration ouvre « Groupes » depuis son menu, crée le groupe, y place le validateur et le renomme ; le validateur reçoit l'étape

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-19, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Groupes » (link)
3. Arriver sur une adresse de la forme `/clm/admin/groupes`
4. Saisir « Nouveau groupe » Code » : `JUR_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Juridique ${unique}`
5. Activer « Créer le groupe »
6. Choisir « Ajouter un membre au groupe JUR_${UNIQUE} » Membre » : `validateur@iq.example`
7. Dans la section « Ajouter un membre au groupe JUR_${UNIQUE} », activer « Ajouter »
8. Voir « validateur@iq.example »
9. Saisir « Renommer le groupe JUR_${UNIQUE} » Libellé du groupe » : `Affaires juridiques ${unique}`
10. Dans la section « Renommer le groupe JUR_${UNIQUE} », activer « Renommer »
11. Voir « Groupe JUR_${UNIQUE} renommé. », « Affaires juridiques ${unique} »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
13. Se connecter en tant que initiateur IQ
14. Ouvrir l'écran `/clm/trousses/nouvelle`
15. Saisir « Titre » : `Groupe CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-GRP`
16. Activer « Enregistrer le brouillon »
17. Activer « Continuer »
18. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
19. Voir « entente-${unique}.pdf »
20. Activer « Continuer »
21. Activer « Continuer »
22. Activer « Ajouter une étape »
23. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
24. Choisir « Type » : `VALIDATION`
25. Activer « Affecter un groupe »
26. Saisir « Responsable 1 » : `JUR_${UNIQUE}`
27. Activer « Tester le parcours »
28. Voir « Parcours valide. »
29. Activer « Enregistrer »
30. Activer « Continuer »
31. Activer « Lancer le parcours »
32. Arriver sur une adresse de la forme `/clm/trousses/\d+`
33. Se connecter en tant que validateur IQ
34. Ouvrir l'écran `/clm/taches`
35. Activer « Traiter — CB${UNIQUE}-GRP Validation juridique »
36. Activer « Valider »
37. Voir « Décision enregistrée »

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

## IQ-C-20 — Ajout, retrait, remplacement d'un validateur ou approbateur en cours

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-07 — Réassignation en cours de processus : l'étape passe à un autre intervenant sans redémarrer le parcours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (21 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Réassignation CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Activer « Parcours » (tab)
21. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation juridique » (ligne ou carte cliquable)
22. Saisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `approbateur@iq.example` ; « Assignation par utilisateur, groupe ou règle » Motif » : `Validateur en congé`
23. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
24. Se connecter en tant que validateur IQ
25. Ouvrir l'écran `/clm/taches`
26. Ne pas voir « CB${UNIQUE}-REA »
27. Se connecter en tant que approbateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — CB${UNIQUE}-REA Validation juridique »
30. Activer « Valider »
31. Voir « Décision enregistrée »

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-07 — Réassignation en cours de processus : l'étape passe à un autre intervenant sans redémarrer le parcours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (21 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Réassignation CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Activer « Parcours » (tab)
21. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation juridique » (ligne ou carte cliquable)
22. Saisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `approbateur@iq.example` ; « Assignation par utilisateur, groupe ou règle » Motif » : `Validateur en congé`
23. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
24. Se connecter en tant que validateur IQ
25. Ouvrir l'écran `/clm/taches`
26. Ne pas voir « CB${UNIQUE}-REA »
27. Se connecter en tant que approbateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — CB${UNIQUE}-REA Validation juridique »
30. Activer « Valider »
31. Voir « Décision enregistrée »

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-05 — Validations en parallèle : un validateur rejette en motivant, la trousse est rejetée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-22, IQ-C-23, IQ-D-04, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (26 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PAR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `v2` ; « Libellé » : `Validation financière`
16. Choisir « Type » : `VALIDATION`
17. Saisir « Phase » : `1`
18. Activer « Ajouter un responsable »
19. Saisir « Responsable 1 » : `approbateur@iq.example`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que validateur IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PAR Validation juridique »
29. Activer « Rejeter »
30. Voir « Un motif est obligatoire »
31. Saisir « Commentaire » : `Montant hors programme`
32. Activer « Rejeter »
33. Voir « Décision enregistrée : rejeté. »
34. Se connecter en tant que initiateur IQ
35. Ouvrir l'écran `/clm/trousses`
36. Activer « Ouvrir le détail de CB${UNIQUE}-PAR » (link)
37. Voir « Rejetée »

### E2E-CB-IQ-06 — Retour pour correction : le validateur renvoie à l'initiateur avec un motif

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-C-55, IQ-S-03, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (20 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Correction CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-COR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Se connecter en tant que validateur IQ
21. Ouvrir l'écran `/clm/taches`
22. Activer « Traiter — CB${UNIQUE}-COR Validation juridique »
23. Saisir « Commentaire » : `Annexe B manquante`
24. Activer « Retourner pour correction »
25. Voir « Trousse retournée pour correction. »
26. Se connecter en tant que initiateur IQ
27. Ouvrir l'écran `/clm/trousses`
28. Activer « Ouvrir le détail de CB${UNIQUE}-COR » (link)
29. Activer « Historique » (tab)
30. Voir « Annexe B manquante »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.rejet`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Décision : rejeté »

## IQ-C-23 — Commentaires et historique des validations

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-05 — Validations en parallèle : un validateur rejette en motivant, la trousse est rejetée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-22, IQ-C-23, IQ-D-04, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (26 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PAR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `v2` ; « Libellé » : `Validation financière`
16. Choisir « Type » : `VALIDATION`
17. Saisir « Phase » : `1`
18. Activer « Ajouter un responsable »
19. Saisir « Responsable 1 » : `approbateur@iq.example`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que validateur IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PAR Validation juridique »
29. Activer « Rejeter »
30. Voir « Un motif est obligatoire »
31. Saisir « Commentaire » : `Montant hors programme`
32. Activer « Rejeter »
33. Voir « Décision enregistrée : rejeté. »
34. Se connecter en tant que initiateur IQ
35. Ouvrir l'écran `/clm/trousses`
36. Activer « Ouvrir le détail de CB${UNIQUE}-PAR » (link)
37. Voir « Rejetée »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-12 — Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-40, IQ-C-24, IQ-C-58, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Désactiver — Invitation à signer Courriel » (button)
3. Voir « Message désactivé. »
4. Activer « Activer — Invitation à signer Courriel » (button)
5. Voir « Message activé. »
6. Activer « Désactiver — Rappel de non-réponse Courriel » (button)
7. Voir « Message désactivé. »
8. Activer « Activer — Rappel de non-réponse Courriel » (button)
9. Voir « Message activé. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-25 — Suivi de l'état des validations en cours

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-10 — AF-46 (RST-49) : dans « Mes tâches », le validateur voit d'abord la tâche de la trousse critique et filtre ses tâches par priorité

- **Profil** : validateur IQ
- **Exigences démontrées** : IQ-C-25
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Deux trousses lancées vers le validateur, la basse d'abord, puis la critique
2. Ouvrir l'écran `/clm/taches`
3. Voir « Les tâches des trousses les plus prioritaires viennent en premier »
4. Relever le texte affiché (`table.clm-table tbody tr:first-child:has-text("Critique")`) sous le nom `premiere`
5. Choisir « Priorité de la trousse » : `Critique`
6. Voir « Tâche critique ${unique} »
7. Ne pas voir « Tâche basse ${unique} »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
9. Choisir « Priorité de la trousse » : `Basse`
10. Voir « Tâche basse ${unique} »
11. Ne pas voir « Tâche critique ${unique} »

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-26 — Signature séquentielle, parallèle, mixte

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-09 — Signature séquentielle de deux signataires internes : le second n'est sollicité qu'après le premier

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-29, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Séquentielle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SSQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Activer « Ajouter un responsable »
29. Choisir « Type du responsable 1 » : `PARTICIPANT`
30. Choisir « Responsable 1 » : `Sophie Roy`
31. Activer « Tester le parcours »
32. Voir « Parcours valide. »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Se connecter en tant que approbateur IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SSQ »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SSQ Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Étienne Côté`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »
55. Se connecter en tant que approbateur IQ
56. Ouvrir l'écran `/clm/taches`
57. Activer « Traiter — CB${UNIQUE}-SSQ Signature cosignataire »
58. Activer « Signer maintenant »
59. Arriver sur une adresse de la forme `/portail/`
60. Activer « Recevoir mon code »
61. Voir « Code envoyé. »
62. Préparer par l'API : Code reçu par courriel
63. Saisir « Code de vérification » : `${code}`
64. Activer « Vérifier mon identité »
65. Activer « Lire / télécharger »
66. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
67. Saisir « Votre nom complet » : `Sophie Roy`
68. Activer « Signer »
69. Voir « Merci, votre signature est enregistrée »
70. Se connecter en tant que initiateur IQ
71. Ouvrir l'écran `/clm/trousses`
72. Activer « Ouvrir le détail de CB${UNIQUE}-SSQ » (link)
73. Voir « Terminée »
74. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-10 — Signature parallèle : les deux signataires sont sollicités en même temps

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-28, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle sig CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SPA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Saisir « Phase » : `1`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `Sophie Roy`
32. Activer « Tester le parcours »
33. Voir « Parcours valide. »
34. Activer « Enregistrer »
35. Activer « Continuer »
36. Activer « Lancer le parcours »
37. Arriver sur une adresse de la forme `/clm/trousses/\d+`
38. Se connecter en tant que approbateur IQ
39. Ouvrir l'écran `/clm/taches`
40. Activer « Traiter — CB${UNIQUE}-SPA Signature cosignataire »
41. Activer « Signer maintenant »
42. Arriver sur une adresse de la forme `/portail/`
43. Activer « Recevoir mon code »
44. Voir « Code envoyé. »
45. Préparer par l'API : Code reçu par courriel
46. Saisir « Code de vérification » : `${code}`
47. Activer « Vérifier mon identité »
48. Activer « Lire / télécharger »
49. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
50. Saisir « Votre nom complet » : `Sophie Roy`
51. Activer « Signer »
52. Voir « Merci, votre signature est enregistrée »
53. Se connecter en tant que signataire interne IQ
54. Ouvrir l'écran `/clm/taches`
55. Activer « Traiter — CB${UNIQUE}-SPA Signature IQ »
56. Activer « Signer maintenant »
57. Arriver sur une adresse de la forme `/portail/`
58. Activer « Recevoir mon code »
59. Voir « Code envoyé. »
60. Préparer par l'API : Code reçu par courriel
61. Saisir « Code de vérification » : `${code}`
62. Activer « Vérifier mon identité »
63. Activer « Lire / télécharger »
64. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
65. Saisir « Votre nom complet » : `Étienne Côté`
66. Activer « Signer »
67. Voir « Merci, votre signature est enregistrée »

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-27 — Groupes de signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-03 — Au portail, le signataire externe vérifie son identité, lit le document, consent et signe ; un autre refuse de signer en motivant

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-27
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (11 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-28 — Signature simultanée et consécutive configurable

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-10 — Signature parallèle : les deux signataires sont sollicités en même temps

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-28, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle sig CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SPA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Saisir « Phase » : `1`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `Sophie Roy`
32. Activer « Tester le parcours »
33. Voir « Parcours valide. »
34. Activer « Enregistrer »
35. Activer « Continuer »
36. Activer « Lancer le parcours »
37. Arriver sur une adresse de la forme `/clm/trousses/\d+`
38. Se connecter en tant que approbateur IQ
39. Ouvrir l'écran `/clm/taches`
40. Activer « Traiter — CB${UNIQUE}-SPA Signature cosignataire »
41. Activer « Signer maintenant »
42. Arriver sur une adresse de la forme `/portail/`
43. Activer « Recevoir mon code »
44. Voir « Code envoyé. »
45. Préparer par l'API : Code reçu par courriel
46. Saisir « Code de vérification » : `${code}`
47. Activer « Vérifier mon identité »
48. Activer « Lire / télécharger »
49. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
50. Saisir « Votre nom complet » : `Sophie Roy`
51. Activer « Signer »
52. Voir « Merci, votre signature est enregistrée »
53. Se connecter en tant que signataire interne IQ
54. Ouvrir l'écran `/clm/taches`
55. Activer « Traiter — CB${UNIQUE}-SPA Signature IQ »
56. Activer « Signer maintenant »
57. Arriver sur une adresse de la forme `/portail/`
58. Activer « Recevoir mon code »
59. Voir « Code envoyé. »
60. Préparer par l'API : Code reçu par courriel
61. Saisir « Code de vérification » : `${code}`
62. Activer « Vérifier mon identité »
63. Activer « Lire / télécharger »
64. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
65. Saisir « Votre nom complet » : `Étienne Côté`
66. Activer « Signer »
67. Voir « Merci, votre signature est enregistrée »

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-29 — Association automatique des étapes aux signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-09 — Signature séquentielle de deux signataires internes : le second n'est sollicité qu'après le premier

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-29, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Séquentielle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SSQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Activer « Ajouter un responsable »
29. Choisir « Type du responsable 1 » : `PARTICIPANT`
30. Choisir « Responsable 1 » : `Sophie Roy`
31. Activer « Tester le parcours »
32. Voir « Parcours valide. »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Se connecter en tant que approbateur IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SSQ »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SSQ Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Étienne Côté`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »
55. Se connecter en tant que approbateur IQ
56. Ouvrir l'écran `/clm/taches`
57. Activer « Traiter — CB${UNIQUE}-SSQ Signature cosignataire »
58. Activer « Signer maintenant »
59. Arriver sur une adresse de la forme `/portail/`
60. Activer « Recevoir mon code »
61. Voir « Code envoyé. »
62. Préparer par l'API : Code reçu par courriel
63. Saisir « Code de vérification » : `${code}`
64. Activer « Vérifier mon identité »
65. Activer « Lire / télécharger »
66. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
67. Saisir « Votre nom complet » : `Sophie Roy`
68. Activer « Signer »
69. Voir « Merci, votre signature est enregistrée »
70. Se connecter en tant que initiateur IQ
71. Ouvrir l'écran `/clm/trousses`
72. Activer « Ouvrir le détail de CB${UNIQUE}-SSQ » (link)
73. Voir « Terminée »
74. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-11 — Signataire ajouté puis substitué en cours de processus, sans relancer la trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-C-55, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (39 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Substitution CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SUB`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Activer « Participants (1) » (tab)
27. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
28. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
29. Décocher « Externe »
30. Activer « Ajouter un signataire »
31. Voir « Sophie Roy »
32. Activer « Parcours » (tab)
33. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Signature IQ » (ligne ou carte cliquable)
34. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `Sophie Roy`
35. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Signataire remplacé par délégation`
36. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
37. Se connecter en tant que signataire interne IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SUB »
40. Se connecter en tant que approbateur IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SUB Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Sophie Roy`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-31 — Réassignation de signataires sans reprise complète

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-11 — Signataire ajouté puis substitué en cours de processus, sans relancer la trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-C-55, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (39 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Substitution CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SUB`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Activer « Participants (1) » (tab)
27. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
28. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
29. Décocher « Externe »
30. Activer « Ajouter un signataire »
31. Voir « Sophie Roy »
32. Activer « Parcours » (tab)
33. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Signature IQ » (ligne ou carte cliquable)
34. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `Sophie Roy`
35. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Signataire remplacé par délégation`
36. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
37. Se connecter en tant que signataire interne IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SUB »
40. Se connecter en tant que approbateur IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SUB Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Sophie Roy`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-32 — Modèles réutilisables

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-02 — Un modèle vide ne peut être soumis à publication

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Activer « Vérifier »
5. Voir « Le corps du contrat est vide. »
6. Activer « Soumettre à publication »
7. Voir « Le corps du contrat est vide. »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-33 — Partage des modèles par groupe d'usagers

- **Source** : 1.4.7
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-14 — Modèle partagé avec un seul groupe : ses membres le voient, les autres non

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-33, IQ-C-67
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/groupes`
2. Saisir « Nouveau groupe » Code » : `PRT_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Partage ${unique}`
3. Activer « Créer le groupe »
4. Choisir « Ajouter un membre au groupe PRT_${UNIQUE} » Membre » : `validateur@iq.example`
5. Dans la section « Ajouter un membre au groupe PRT_${UNIQUE} », activer « Ajouter »
6. Se connecter en tant que administration d'affaires IQ
7. Ouvrir l'écran `/clm/modeles`
8. Activer « Créer un modèle »
9. Saisir « Nom » : `Partagé CB ${unique}` ; « Corps du contrat » : `Convention réservée au groupe. {{clauses}}` ; « Partage par groupes » : `PRT_${UNIQUE}`
10. Activer « Enregistrer le brouillon »
11. Voir « Brouillon enregistré. »
12. Activer « Soumettre à publication »
13. Voir « soumis à publication »
14. Se connecter en tant que administration principale IQ
15. Ouvrir l'écran `/clm/modeles`
16. Dans la ligne « Partagé CB ${unique} », activer « Publier »
17. Voir « Partagé avec : PRT_${UNIQUE} »
18. Se connecter en tant que validateur IQ
19. Ouvrir l'écran `/clm/modeles`
20. Voir « Partagé CB ${unique} »
21. Se connecter en tant que initiateur IQ
22. Ouvrir l'écran `/clm/modeles`
23. Ne pas voir « Partagé CB ${unique} »

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-34 — Ancrage précis des champs

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-35 — Types : texte validé, liste, date, case à cocher

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-36 — Distinguer champs de formulaire, de saisie et de signature

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-37 — Attribution des champs à des signataires

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-02 — Une trousse naît d'un dossier Salesforce et d'un modèle : recherche et import, valeur corrigée, contrat généré en PDF et en Word, zone de signature attribuée, lancement ; le validateur commente puis valide sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-14, IQ-C-37, IQ-C-12
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (27 gestes)
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
- **Nature** : parcours d'interface (16 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-38 — Règles conditionnelles d'affichage

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-39 — Saisie assistée (suggestions)

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-22 — Saisie assistée : une valeur déjà retenue dans une trousse est proposée à la saisie du même champ dans la trousse suivante

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-39, IQ-C-56
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (18 gestes)
- **Dernier résultat** : ✅ réussi

1. Se connecter en tant que administration d'affaires IQ
2. Ouvrir l'écran `/clm/modeles`
3. Activer « Créer un modèle »
4. Saisir « Nom » : `Suggestions CB ${unique}` ; « Corps du contrat » : `Convention au titre du programme {{programme}}. {{clauses}}`
5. Activer « Ajouter un champ »
6. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
7. Activer « Soumettre à publication »
8. Voir « soumis à publication »
9. Se connecter en tant que administration principale IQ
10. Ouvrir l'écran `/clm/modeles`
11. Dans la ligne « Suggestions CB ${unique} », activer « Publier »
12. Voir « « Suggestions CB ${unique} » publié. »
13. Se connecter en tant que initiateur IQ
14. Ouvrir l'écran `/clm/trousses/nouvelle`
15. Saisir « Titre » : `Suggestion A ${unique}` ; « Référence externe » : `CB${UNIQUE}-SGA`
16. Choisir « Modèle de contrat » : `Suggestions CB ${unique}`
17. Activer « Enregistrer le brouillon »
18. Activer « Corriger la valeur — Programme »
19. Saisir « Nouvelle valeur — Programme » : `Essor régional ${unique}`
20. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
21. Voir « Valeur de « Programme » enregistrée. »
22. Ouvrir l'écran `/clm/trousses/nouvelle`
23. Saisir « Titre » : `Suggestion B ${unique}` ; « Référence externe » : `CB${UNIQUE}-SGB`
24. Choisir « Modèle de contrat » : `Suggestions CB ${unique}`
25. Activer « Enregistrer le brouillon »
26. Activer « Corriger la valeur — Programme »
27. Voir « valeur(s) déjà utilisée(s) proposée(s) : flèche bas pour les parcourir. »
28. Saisir « Nouvelle valeur — Programme » : `Essor régional ${unique}`
29. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
30. Voir « Valeur de « Programme » enregistrée. »
31. Voir « Essor régional ${unique} »
32. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-04 — Générer un contrat depuis un modèle, prérempli par Salesforce

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-39
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-12 — Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-40, IQ-C-24, IQ-C-58, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Désactiver — Invitation à signer Courriel » (button)
3. Voir « Message désactivé. »
4. Activer « Activer — Invitation à signer Courriel » (button)
5. Voir « Message activé. »
6. Activer « Désactiver — Rappel de non-réponse Courriel » (button)
7. Voir « Message désactivé. »
8. Activer « Activer — Rappel de non-réponse Courriel » (button)
9. Voir « Message activé. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-41 — Rappels automatiques de non-réponse

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-08 — AF-46 (RST-49) : l'initiateur choisit la priorité critique d'une trousse à la création ; sans choix, la priorité se déduit du niveau de risque ; le détail l'affiche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-07, IQ-C-41
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Prio E2E ${unique}`
3. Choisir « Niveau de risque » : `Faible`
4. Constater que « Priorité » propose « Selon le niveau de risque (automatique) », « Basse », « Normale », « Haute », « Critique »
5. Choisir « Priorité » : `Critique`
6. Voir « Une trousse haute est relancée deux fois plus souvent, une critique quatre fois. »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Activer « Enregistrer le brouillon »
9. Voir « enregistré. »
10. Préparer par l'API : La trousse enregistrée à l'écran porte la priorité choisie ; une seconde, de risque élevé et sans priorité choisie, prend la priorité haute
11. Ouvrir l'écran `/clm/trousses/${choisie}`
12. Voir « Critique », « (choisie) »
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
14. Ouvrir l'écran `/clm/trousses/${deduite}`
15. Voir « Haute », « (selon le risque) »

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-42 — Destinataires en copie

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-43 — Vue de suivi centralisée : étapes, attentes, exceptions

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-05 — AF-37 (RST-42) : l'administration d'affaires crée un rapport de délais moyens, le modifie, puis l'exporte en CSV et en Excel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-A-22
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (13 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trousse d'un document validée par le validateur : lancée puis terminée, son délai de traitement est connu
2. Ouvrir l'écran `/clm/admin/rapports`
3. Voir le titre « Rapports personnalisables »
4. Activer « Nouveau rapport »
5. Saisir « Nom du rapport » : `Délais E2E ${unique}` ; « Période (jours glissants) » : `30`
6. Choisir « Type de rapport » : `Délais de traitement (moyenne)`
7. Cocher « Référence »
8. Cocher « Lancée le »
9. Cocher « Délai (h) »
10. Activer « Enregistrer le rapport »
11. Voir « Rapport « Délais E2E ${unique} » enregistré. »
12. Voir « ${reference} », « Moyenne ( »
13. Activer « Modifier — Délais E2E ${unique} »
14. Cocher « Terminée le »
15. Activer « Enregistrer le rapport »
16. Voir « Rapport « Délais E2E ${unique} » enregistré. »
17. Voir « Terminée le »
18. Télécharger par « Exporter en CSV — Délais E2E ${unique} » et obtenir un fichier non vide
19. Télécharger par « Exporter en Excel — Délais E2E ${unique} » et obtenir un fichier non vide
20. Voir « exporté (XLSX). »
21. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
22. Activer « Supprimer — Délais E2E ${unique} »
23. Voir « supprimé »
24. Ne pas voir « Modifier — Délais E2E ${unique} »

### E2E-AX-IQ-06 — AF-37 (RST-42) : le relevé d'utilisation s'exporte en CSV et en Excel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/journal`
2. Voir « Exporter le relevé (CSV) »
3. Télécharger par « Exporter le relevé (CSV) » et obtenir un fichier non vide
4. Télécharger par « Exporter le relevé (Excel) » et obtenir un fichier non vide
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-AX-IQ-09 — AF-46 (RST-49) : l'administration d'affaires trie le suivi par priorité, la plus haute d'abord, le filtre sur une priorité et l'exporte avec la priorité

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-D-08
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trois trousses : risque faible (priorité basse), priorité critique choisie, risque élevé (priorité haute), créées dans cet ordre
2. Ouvrir l'écran `/clm/suivi`
3. Saisir « Filtres état » Rechercher » : `${unique}`
4. Choisir « Trier par » : `Priorité, la plus haute d’abord`
5. Dans la section « Filtres état », activer « Rechercher »
6. Relever le texte affiché (`table.clm-table tbody tr:first-child:has-text("Critique ${unique}")`) sous le nom `premiere`
7. Relever le texte affiché (`table.clm-table tbody tr:nth-child(2):has-text("Haute ${unique}")`) sous le nom `deuxieme`
8. Relever le texte affiché (`table.clm-table tbody tr:nth-child(3):has-text("Basse ${unique}")`) sous le nom `troisieme`
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
10. Choisir « Priorité » : `Haute`
11. Dans la section « Filtres état », activer « Rechercher »
12. Voir « Haute ${unique} »
13. Ne pas voir « Critique ${unique} », « Basse ${unique} »
14. Télécharger par « Exporter (CSV) » et obtenir un fichier non vide
15. Préparer par l'API : L'export du suivi porte la colonne de priorité, dans l'ordre demandé

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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

### E2E-IQ-16 — Suivi centralisé des trousses et des échéances

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-C-25, IQ-D-08
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-44 — Plusieurs documents par trousse

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-45 — Dossiers, documents, versions, intervenants

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : parcours d'interface (16 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-46 — PDF et Word

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-47 — Téléversement d'au moins 25 Mo

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-02 — Dépôt de documents volumineux dans une trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-47
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.documents.taille`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-48 — Historique des étapes de validation, d'approbation, de signature

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-02 — AF-34 et SEC-15 (RST-43) : le modèle déclare les champs modifiables en cours ; en traitement, un champ figé est refusé, une modification permise relance le cycle et le journal nomme le champ, l'ancienne et la nouvelle valeur

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-05, IQ-A-18, IQ-C-49
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (19 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `AF34 E2E ${unique}` ; « Corps du contrat » : `Convention {{programme}} de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Activer « Ajouter un champ »
7. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
8. Voir « Modification en cours de traitement »
9. Cocher « Modifiable en cours de traitement »
10. Choisir « Effet sur les approbations obtenues » : `Nouveau cycle : toutes les approbations sont invalidées`
11. Activer « Enregistrer le brouillon »
12. Voir « Brouillon enregistré. »
13. Activer « Soumettre à publication »
14. Voir « soumis à publication »
15. Se connecter en tant que administration principale IQ
16. Ouvrir l'écran `/clm/modeles`
17. Dans la ligne « AF34 E2E ${unique} », activer « Publier »
18. Voir « « AF34 E2E ${unique} » publié. »
19. Se connecter en tant que initiateur IQ
20. Préparer par l'API : Trousse issue du modèle, valeurs saisies en brouillon, document joint et parcours lancé (validation en cours)
21. Ouvrir l'écran `/clm/trousses/${trousse}`
22. Activer « Corriger la valeur — Programme »
23. Saisir « Nouvelle valeur — Programme » : `Autre programme`
24. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
25. Voir « « Programme » n'est pas modifiable en cours de traitement. »
26. Activer « Annuler »
27. Activer « Corriger la valeur — Montant »
28. Saisir « Nouvelle valeur — Montant » : `60000`
29. Dans la section « Corriger la valeur — Montant », activer « Enregistrer »
30. Voir « Valeur de « Montant » enregistrée. »
31. Activer « Historique » (tab)
32. Voir « Valeur modifiée (avant → après) », « « Montant » (montant) : « 50000 » → « 60000 » », « Nouveau cycle (modification en cours de parcours) »
33. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-51 — Conservation et consultation du dossier de preuve

- **Source** : 1.4.7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-ECR-IQ-01 — L'initiateur crée une trousse à l'écran : documents déposés, remplacés et retirés, signataires ajoutés et retirés, parcours testé puis enregistré, lancement ; la preuve se vérifie

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-29, IQ-C-12, IQ-C-51
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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
- **Nature** : parcours d'interface (11 gestes)
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
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
3. Ouvrir l'écran `/clm/trousses`
4. Voir « Trousses »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-54 — Limiter les dépendances entre signataires

- **Source** : 1.4.8
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-09 — Signature séquentielle de deux signataires internes : le second n'est sollicité qu'après le premier

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-29, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Séquentielle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SSQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Activer « Ajouter un responsable »
29. Choisir « Type du responsable 1 » : `PARTICIPANT`
30. Choisir « Responsable 1 » : `Sophie Roy`
31. Activer « Tester le parcours »
32. Voir « Parcours valide. »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Se connecter en tant que approbateur IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SSQ »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SSQ Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Étienne Côté`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »
55. Se connecter en tant que approbateur IQ
56. Ouvrir l'écran `/clm/taches`
57. Activer « Traiter — CB${UNIQUE}-SSQ Signature cosignataire »
58. Activer « Signer maintenant »
59. Arriver sur une adresse de la forme `/portail/`
60. Activer « Recevoir mon code »
61. Voir « Code envoyé. »
62. Préparer par l'API : Code reçu par courriel
63. Saisir « Code de vérification » : `${code}`
64. Activer « Vérifier mon identité »
65. Activer « Lire / télécharger »
66. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
67. Saisir « Votre nom complet » : `Sophie Roy`
68. Activer « Signer »
69. Voir « Merci, votre signature est enregistrée »
70. Se connecter en tant que initiateur IQ
71. Ouvrir l'écran `/clm/trousses`
72. Activer « Ouvrir le détail de CB${UNIQUE}-SSQ » (link)
73. Voir « Terminée »
74. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-10 — Signature parallèle : les deux signataires sont sollicités en même temps

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-28, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle sig CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SPA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Saisir « Phase » : `1`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `Sophie Roy`
32. Activer « Tester le parcours »
33. Voir « Parcours valide. »
34. Activer « Enregistrer »
35. Activer « Continuer »
36. Activer « Lancer le parcours »
37. Arriver sur une adresse de la forme `/clm/trousses/\d+`
38. Se connecter en tant que approbateur IQ
39. Ouvrir l'écran `/clm/taches`
40. Activer « Traiter — CB${UNIQUE}-SPA Signature cosignataire »
41. Activer « Signer maintenant »
42. Arriver sur une adresse de la forme `/portail/`
43. Activer « Recevoir mon code »
44. Voir « Code envoyé. »
45. Préparer par l'API : Code reçu par courriel
46. Saisir « Code de vérification » : `${code}`
47. Activer « Vérifier mon identité »
48. Activer « Lire / télécharger »
49. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
50. Saisir « Votre nom complet » : `Sophie Roy`
51. Activer « Signer »
52. Voir « Merci, votre signature est enregistrée »
53. Se connecter en tant que signataire interne IQ
54. Ouvrir l'écran `/clm/taches`
55. Activer « Traiter — CB${UNIQUE}-SPA Signature IQ »
56. Activer « Signer maintenant »
57. Arriver sur une adresse de la forme `/portail/`
58. Activer « Recevoir mon code »
59. Voir « Code envoyé. »
60. Préparer par l'API : Code reçu par courriel
61. Saisir « Code de vérification » : `${code}`
62. Activer « Vérifier mon identité »
63. Activer « Lire / télécharger »
64. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
65. Saisir « Votre nom complet » : `Étienne Côté`
66. Activer « Signer »
67. Voir « Merci, votre signature est enregistrée »

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-12 — Signature séquentielle puis conjointe d'un groupe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-27, IQ-C-28, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.sequentielle-parallele`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Signature apposée », « Signatures conjointes »

## IQ-C-55 — Flexibilité des workflows en cours de processus

- **Source** : 1.4.8
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-06 — Retour pour correction : le validateur renvoie à l'initiateur avec un motif

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-C-55, IQ-S-03, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (20 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Correction CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-COR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Se connecter en tant que validateur IQ
21. Ouvrir l'écran `/clm/taches`
22. Activer « Traiter — CB${UNIQUE}-COR Validation juridique »
23. Saisir « Commentaire » : `Annexe B manquante`
24. Activer « Retourner pour correction »
25. Voir « Trousse retournée pour correction. »
26. Se connecter en tant que initiateur IQ
27. Ouvrir l'écran `/clm/trousses`
28. Activer « Ouvrir le détail de CB${UNIQUE}-COR » (link)
29. Activer « Historique » (tab)
30. Voir « Annexe B manquante »

### E2E-CB-IQ-07 — Réassignation en cours de processus : l'étape passe à un autre intervenant sans redémarrer le parcours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (21 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Réassignation CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Activer « Parcours » (tab)
21. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation juridique » (ligne ou carte cliquable)
22. Saisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `approbateur@iq.example` ; « Assignation par utilisateur, groupe ou règle » Motif » : `Validateur en congé`
23. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
24. Se connecter en tant que validateur IQ
25. Ouvrir l'écran `/clm/taches`
26. Ne pas voir « CB${UNIQUE}-REA »
27. Se connecter en tant que approbateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — CB${UNIQUE}-REA Validation juridique »
30. Activer « Valider »
31. Voir « Décision enregistrée »

### E2E-CB-IQ-11 — Signataire ajouté puis substitué en cours de processus, sans relancer la trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-C-55, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (39 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Substitution CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SUB`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Activer « Participants (1) » (tab)
27. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
28. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
29. Décocher « Externe »
30. Activer « Ajouter un signataire »
31. Voir « Sophie Roy »
32. Activer « Parcours » (tab)
33. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Signature IQ » (ligne ou carte cliquable)
34. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `Sophie Roy`
35. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Signataire remplacé par délégation`
36. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
37. Se connecter en tant que signataire interne IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SUB »
40. Se connecter en tant que approbateur IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SUB Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Sophie Roy`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »

### E2E-CB-IQ-23 — Flexibilité en cours d'utilisation : l'organisation ajoute un type de contrat, change un paramètre et fait évoluer un modèle, sans développement ; les trousses suivantes en tiennent compte

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-55, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_TYPE_CONTRAT`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `PILOTE_${UNIQUE}` ; « Libellé » : `Entente pilote ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Ouvrir l'écran `/clm/admin/parametres`
8. Activer « Modifier un paramètre autorisé »
9. Saisir « Rappel tous les (jours) » : `6`
10. Activer « Enregistrer »
11. Voir « Paramètres enregistrés. »
12. Se connecter en tant que initiateur IQ
13. Ouvrir l'écran `/clm/trousses/nouvelle`
14. Constater la valeur « Rappel tous les (jours) » : `6`
15. Saisir « Titre » : `Flexible CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-FLX`
16. Choisir « Type de contrat » : `Entente pilote ${unique}`
17. Activer « Enregistrer le brouillon »
18. Voir « CB${UNIQUE}-FLX »
19. Se connecter en tant que administration d'affaires IQ
20. Ouvrir l'écran `/clm/modeles`
21. Activer « Créer un modèle »
22. Saisir « Nom » : `Évolutif CB ${unique}` ; « Corps du contrat » : `Version 1 de l'entente. {{clauses}}`
23. Activer « Soumettre à publication »
24. Voir « soumis à publication »
25. Se connecter en tant que administration principale IQ
26. Ouvrir l'écran `/clm/modeles`
27. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
28. Voir « « Évolutif CB ${unique} » publié. »
29. Se connecter en tant que administration d'affaires IQ
30. Ouvrir l'écran `/clm/modeles`
31. Dans la ligne « Évolutif CB ${unique} », activer « Nouvelle version »
32. Voir « Nouvelle version de « Évolutif CB ${unique} » créée. »
33. Activer « Modifier le brouillon — Évolutif CB ${unique} » (link)
34. Saisir « Corps du contrat » : `Version 2 de l'entente, clause de résiliation ajoutée. {{clauses}}`
35. Activer « Soumettre à publication »
36. Voir « soumis à publication »
37. Se connecter en tant que administration principale IQ
38. Ouvrir l'écran `/clm/modeles`
39. Saisir « Rechercher » : `Évolutif CB ${unique}`
40. Choisir « État » : `EN_REVISION`
41. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
42. Voir « publié. »
43. Choisir « État » : `RETIRE`
44. Voir « Évolutif CB ${unique} »
45. Ouvrir l'écran `/clm/admin/parametres`
46. Activer « Modifier un paramètre autorisé »
47. Activer « Rétablir les paramètres de la plateforme »
48. Voir « Paramètres de la plateforme rétablis. »

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-CB-IQ-22 — Saisie assistée : une valeur déjà retenue dans une trousse est proposée à la saisie du même champ dans la trousse suivante

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-39, IQ-C-56
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (18 gestes)
- **Dernier résultat** : ✅ réussi

1. Se connecter en tant que administration d'affaires IQ
2. Ouvrir l'écran `/clm/modeles`
3. Activer « Créer un modèle »
4. Saisir « Nom » : `Suggestions CB ${unique}` ; « Corps du contrat » : `Convention au titre du programme {{programme}}. {{clauses}}`
5. Activer « Ajouter un champ »
6. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
7. Activer « Soumettre à publication »
8. Voir « soumis à publication »
9. Se connecter en tant que administration principale IQ
10. Ouvrir l'écran `/clm/modeles`
11. Dans la ligne « Suggestions CB ${unique} », activer « Publier »
12. Voir « « Suggestions CB ${unique} » publié. »
13. Se connecter en tant que initiateur IQ
14. Ouvrir l'écran `/clm/trousses/nouvelle`
15. Saisir « Titre » : `Suggestion A ${unique}` ; « Référence externe » : `CB${UNIQUE}-SGA`
16. Choisir « Modèle de contrat » : `Suggestions CB ${unique}`
17. Activer « Enregistrer le brouillon »
18. Activer « Corriger la valeur — Programme »
19. Saisir « Nouvelle valeur — Programme » : `Essor régional ${unique}`
20. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
21. Voir « Valeur de « Programme » enregistrée. »
22. Ouvrir l'écran `/clm/trousses/nouvelle`
23. Saisir « Titre » : `Suggestion B ${unique}` ; « Référence externe » : `CB${UNIQUE}-SGB`
24. Choisir « Modèle de contrat » : `Suggestions CB ${unique}`
25. Activer « Enregistrer le brouillon »
26. Activer « Corriger la valeur — Programme »
27. Voir « valeur(s) déjà utilisée(s) proposée(s) : flèche bas pour les parcourir. »
28. Saisir « Nouvelle valeur — Programme » : `Essor régional ${unique}`
29. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
30. Voir « Valeur de « Programme » enregistrée. »
31. Voir « Essor régional ${unique} »
32. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-AIDE-01 — L'administration d'IQ ouvre l'aide : la mise en route, le tutoriel de l'écran courant avec sa vidéo et ses étapes, puis la recherche

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-57, LEV-2.1-10
- **Données** : `bdd/e2e/donnees/vitrines-aide.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : — pas encore exécuté

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Aide »
3. Voir « Aide et tutoriels », « Mise en route de l’organisation », « Pour cet écran »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
5. Activer « Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel »
6. Voir « Étapes »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Activer « ← Retour à la liste »
9. Saisir « Rechercher un tutoriel » : `Notifications`
10. Voir « Tous les tutoriels »

## IQ-C-58 — Personnalisation : image de marque, messages, parcours

- **Source** : 1.4.8
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-12 — Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-40, IQ-C-24, IQ-C-58, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Désactiver — Invitation à signer Courriel » (button)
3. Voir « Message désactivé. »
4. Activer « Activer — Invitation à signer Courriel » (button)
5. Voir « Message activé. »
6. Activer « Désactiver — Rappel de non-réponse Courriel » (button)
7. Voir « Message désactivé. »
8. Activer « Activer — Rappel de non-réponse Courriel » (button)
9. Voir « Message activé. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-VIT-01 — Le superadministrateur modifie la vitrine d'Investissement Québec, y dépose une vidéo, la publie puis la retire ; le site de l'organisation suit

- **Profil** : superadmin
- **Exigences démontrées** : LEV-C-22, IQ-C-58
- **Données** : `bdd/e2e/donnees/vitrines-aide.json`
- **Nature** : parcours d'interface (13 gestes)
- **Dernier résultat** : — pas encore exécuté

1. Ouvrir l'écran `/superadmin`
2. Choisir « Site vitrine des organisations » Organisation » : `Investissement Québec`
3. Saisir « Titre du site » : `IQ — vitrine ${unique}` ; « Accroche » : `Contrats et signature, de bout en bout (${unique}).`
4. Activer « Ajouter une fonctionnalité »
5. Saisir « Titre de la fonctionnalité 11 » : `Fonctionnalité ${unique}` ; « Description de la fonctionnalité 11 » : `Présentée par l'e2e.`
6. Activer « Monter la fonctionnalité 11 »
7. Joindre « presentation-${unique}.mp4 » au champ « Téléverser une vidéo »
8. Voir « Vidéo déposée : vitrines/iq/televersee- »
9. Activer « Enregistrer la vitrine »
10. Voir « Vitrine de Investissement Québec enregistrée. »
11. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
12. Ouvrir l'écran `http://iq.e2e.localhost:5175/accueil`
13. Voir « IQ — vitrine ${unique} », « Fonctionnalité ${unique} », « Présentation des fonctionnalités »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/superadmin`
16. Choisir « Site vitrine des organisations » Organisation » : `Investissement Québec`
17. Décocher « Publiée (visible sur le site de l'organisation) »
18. Activer « Enregistrer la vitrine »
19. Voir « Vitrine de Investissement Québec enregistrée. »
20. Ouvrir l'écran `http://iq.e2e.localhost:5175/accueil`
21. Ne pas voir « IQ — vitrine ${unique} »
22. Ouvrir l'écran `/superadmin`
23. Choisir « Site vitrine des organisations » Organisation » : `Investissement Québec`
24. Cocher « Publiée (visible sur le site de l'organisation) »
25. Activer « Enregistrer la vitrine »
26. Voir « Vitrine de Investissement Québec enregistrée. »

## IQ-C-59 — Automatisation et IA (extraction, classification, anomalies)

- **Source** : 1.4.9
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.ia.analyse`
2. Ouvrir l'écran `/clm`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-60 — Identification et authentification des utilisateurs (SSO)

- **Source** : 1.4.10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-28 — Authentification unique de l'organisation

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-60
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.sso.connexion`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »

### E2E-IQ-29 — Politique de session : l'administration technique fixe l'inactivité et la durée maximale

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-60
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-20 — Vérification d'identité proportionnée : une trousse à risque élevé impose un niveau minimal au signataire externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-61, IQ-C-62, IQ-A-23
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Risque CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RSQ`
3. Choisir « Niveau de risque » : `ELEVE`
4. Activer « Enregistrer le brouillon »
5. Activer « Continuer »
6. Activer « Continuer »
7. Voir « Niveau minimal imposé par le risque »
8. Saisir « Nom » : `Paul Externe` ; « Courriel » : `paul.${unique}@exemple.ca` ; « Téléphone » : `+15145550177`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Cocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Paul Externe »
13. Voir « ajouté (vérification »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-14 — Vérification d'identité proportionnée au risque

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-A-23, IQ-C-61
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.identite.par-risque`
2. Ouvrir l'écran `/clm/admin/parametres`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-62 — Méthodes internes et externes, authentification renforcée

- **Source** : 1.4.10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-20 — Vérification d'identité proportionnée : une trousse à risque élevé impose un niveau minimal au signataire externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-61, IQ-C-62, IQ-A-23
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Risque CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RSQ`
3. Choisir « Niveau de risque » : `ELEVE`
4. Activer « Enregistrer le brouillon »
5. Activer « Continuer »
6. Activer « Continuer »
7. Voir « Niveau minimal imposé par le risque »
8. Saisir « Nom » : `Paul Externe` ; « Courriel » : `paul.${unique}@exemple.ca` ; « Téléphone » : `+15145550177`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Cocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Paul Externe »
13. Voir « ajouté (vérification »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-63 — Standards reconnus de signature électronique, validité juridique

- **Source** : 1.4.11
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-64 — Piste d'audit complète : événements, authentification, horodatages, consentements, intégrité

- **Source** : 1.4.11
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-21 — SEC-27 (RST-53) : l'administration d'affaires choisit, dans le catalogue Purview du locataire, l'étiquette de sensibilité et de rétention d'un type de contrat ; le document déposé la porte et la conserve, puis la politique est retirée

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-D-20
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (5 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/purview`
2. Voir le titre « Étiquettes Microsoft Purview »
3. Choisir « Type de contrat » : `Convention d'aide financière`
4. Choisir « Étiquette de sensibilité » : `Confidentiel`
5. Choisir « Étiquette de rétention » : `Contrats - 7 ans (2557 jours)`
6. Activer « Enregistrer la politique »
7. Voir « Politique « Convention d'aide financière » enregistrée. »
8. Voir « Confidentiel », « Contrats - 7 ans (2557 jours) »
9. Préparer par l'API : L'initiateur dépose un document dans une trousse de ce type : il reçoit les étiquettes de la politique
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Activer « Retirer — Convention d'aide financière »
12. Voir « Politique « Convention d'aide financière » retirée. »

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-C-67 — Accès, modèles, workflows, historiques selon rôles, groupes, unités, domaines

- **Source** : 1.4.12
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-03 — Séparation des responsabilités : l'auteur ne publie pas son propre modèle ; un administrateur le renvoie en brouillon

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Auteur CB ${unique}` ; « Corps du contrat » : `Lettre d'entente. {{clauses}}`
4. Activer « Enregistrer le brouillon »
5. Voir « Brouillon enregistré. »
6. Activer « Soumettre à publication »
7. Voir « soumis à publication »
8. Ouvrir l'écran `/clm/modeles`
9. Dans la ligne « Auteur CB ${unique} », activer « Publier »
10. Voir « l'auteur ne publie pas son propre modèle »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/modeles`
13. Dans la ligne « Auteur CB ${unique} », activer « Renvoyer en brouillon »
14. Voir « « Auteur CB ${unique} » renvoyé en brouillon. »

### E2E-CB-IQ-08 — Attribution par groupe : l'administration ouvre « Groupes » depuis son menu, crée le groupe, y place le validateur et le renomme ; le validateur reçoit l'étape

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-19, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Groupes » (link)
3. Arriver sur une adresse de la forme `/clm/admin/groupes`
4. Saisir « Nouveau groupe » Code » : `JUR_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Juridique ${unique}`
5. Activer « Créer le groupe »
6. Choisir « Ajouter un membre au groupe JUR_${UNIQUE} » Membre » : `validateur@iq.example`
7. Dans la section « Ajouter un membre au groupe JUR_${UNIQUE} », activer « Ajouter »
8. Voir « validateur@iq.example »
9. Saisir « Renommer le groupe JUR_${UNIQUE} » Libellé du groupe » : `Affaires juridiques ${unique}`
10. Dans la section « Renommer le groupe JUR_${UNIQUE} », activer « Renommer »
11. Voir « Groupe JUR_${UNIQUE} renommé. », « Affaires juridiques ${unique} »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
13. Se connecter en tant que initiateur IQ
14. Ouvrir l'écran `/clm/trousses/nouvelle`
15. Saisir « Titre » : `Groupe CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-GRP`
16. Activer « Enregistrer le brouillon »
17. Activer « Continuer »
18. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
19. Voir « entente-${unique}.pdf »
20. Activer « Continuer »
21. Activer « Continuer »
22. Activer « Ajouter une étape »
23. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
24. Choisir « Type » : `VALIDATION`
25. Activer « Affecter un groupe »
26. Saisir « Responsable 1 » : `JUR_${UNIQUE}`
27. Activer « Tester le parcours »
28. Voir « Parcours valide. »
29. Activer « Enregistrer »
30. Activer « Continuer »
31. Activer « Lancer le parcours »
32. Arriver sur une adresse de la forme `/clm/trousses/\d+`
33. Se connecter en tant que validateur IQ
34. Ouvrir l'écran `/clm/taches`
35. Activer « Traiter — CB${UNIQUE}-GRP Validation juridique »
36. Activer « Valider »
37. Voir « Décision enregistrée »

### E2E-CB-IQ-14 — Modèle partagé avec un seul groupe : ses membres le voient, les autres non

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-33, IQ-C-67
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/groupes`
2. Saisir « Nouveau groupe » Code » : `PRT_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Partage ${unique}`
3. Activer « Créer le groupe »
4. Choisir « Ajouter un membre au groupe PRT_${UNIQUE} » Membre » : `validateur@iq.example`
5. Dans la section « Ajouter un membre au groupe PRT_${UNIQUE} », activer « Ajouter »
6. Se connecter en tant que administration d'affaires IQ
7. Ouvrir l'écran `/clm/modeles`
8. Activer « Créer un modèle »
9. Saisir « Nom » : `Partagé CB ${unique}` ; « Corps du contrat » : `Convention réservée au groupe. {{clauses}}` ; « Partage par groupes » : `PRT_${UNIQUE}`
10. Activer « Enregistrer le brouillon »
11. Voir « Brouillon enregistré. »
12. Activer « Soumettre à publication »
13. Voir « soumis à publication »
14. Se connecter en tant que administration principale IQ
15. Ouvrir l'écran `/clm/modeles`
16. Dans la ligne « Partagé CB ${unique} », activer « Publier »
17. Voir « Partagé avec : PRT_${UNIQUE} »
18. Se connecter en tant que validateur IQ
19. Ouvrir l'écran `/clm/modeles`
20. Voir « Partagé CB ${unique} »
21. Se connecter en tant que initiateur IQ
22. Ouvrir l'écran `/clm/modeles`
23. Ne pas voir « Partagé CB ${unique} »

### E2E-CB-IQ-19 — Séparation des rôles : l'auditeur consulte une trousse sans pouvoir agir, l'administration technique n'accède pas aux trousses

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rôles CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-ROL`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-ROL »
5. Se connecter en tant que auditeur IQ
6. Ouvrir l'écran `/clm/trousses`
7. Activer « Ouvrir le détail de CB${UNIQUE}-ROL » (link)
8. Voir « Rôles CB ${unique} »
9. Ne pas voir « Lancer le parcours », « Annuler la trousse »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Se connecter en tant que administration technique IQ
12. Ouvrir l'écran `/clm/trousses`
13. Ne pas voir « CB${UNIQUE}-ROL »

### E2E-CB-IQ-24 — Listes de référence configurées par l'administration de l'organisation : chaque valeur ajoutée, renommée ou retirée se répercute aussitôt dans les listes déroulantes des écrans d'affaires

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (36 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_UNITE`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `INNOV_${UNIQUE}` ; « Libellé » : `Innovation ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Voir « Innovation ${unique} »
8. Choisir « Liste » : `CLM_UNITE`
9. Activer « Ajouter une valeur »
10. Saisir « Code » : `ANCIENNE_${UNIQUE}` ; « Libellé » : `Unité dissoute ${unique}`
11. Activer « Enregistrer »
12. Voir « Valeur enregistrée. »
13. Voir « Unité dissoute ${unique} »
14. Activer « Retirer — Unité dissoute ${unique} » (button)
15. Voir « Valeur retirée des sélecteurs. »
16. Choisir « Liste » : `CLM_ROLE_CONTRAT`
17. Activer « Ajouter une valeur »
18. Saisir « Code » : `MANDATAIRE_${UNIQUE}` ; « Libellé » : `Mandataire ${unique}`
19. Activer « Enregistrer »
20. Voir « Valeur enregistrée. »
21. Voir « Mandataire ${unique} »
22. Choisir « Liste » : `CLM_CHAMP_SALESFORCE`
23. Activer « Ajouter une valeur »
24. Saisir « Code » : `Region_${UNIQUE}` ; « Libellé » : `Région administrative ${unique}`
25. Activer « Enregistrer »
26. Voir « Valeur enregistrée. »
27. Voir « Région administrative ${unique} »
28. Choisir « Liste » : `CLM_PRIORITE_TACHE`
29. Activer « Ajouter une valeur »
30. Saisir « Code » : `CRITIQUE_${UNIQUE}` ; « Libellé » : `Critique ${unique}`
31. Activer « Enregistrer »
32. Voir « Valeur enregistrée. »
33. Voir « Critique ${unique} »
34. Choisir « Liste » : `CLM_TYPE_CONTRAT`
35. Activer « Ajouter une valeur »
36. Saisir « Code » : `ACCORD_${UNIQUE}` ; « Libellé » : `Accord cadre ${unique}`
37. Activer « Enregistrer »
38. Voir « Valeur enregistrée. »
39. Voir « Accord cadre ${unique} »
40. Activer « Modifier — Accord cadre ${unique} » (button)
41. Saisir « Libellé » : `Accord-cadre de partenariat ${unique}`
42. Activer « Enregistrer »
43. Voir « Accord-cadre de partenariat ${unique} »
44. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
45. Se connecter en tant que initiateur IQ
46. Ouvrir l'écran `/clm/trousses/nouvelle`
47. Constater que « Unité responsable » propose « Innovation ${unique} » et ne propose plus « Unité dissoute ${unique} » ; Constater que « Type de contrat » propose « Accord-cadre de partenariat ${unique} » et ne propose plus « Accord cadre ${unique} »
48. Saisir « Titre » : `Références CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REF`
49. Choisir « Unité responsable » : `Innovation ${unique}` ; « Type de contrat » : `Accord-cadre de partenariat ${unique}`
50. Activer « Enregistrer le brouillon »
51. Activer « Continuer »
52. Activer « Continuer »
53. Constater que « Rôle dans le contrat » propose « Mandataire ${unique} »
54. Ouvrir l'écran `/clm/taches`
55. Activer « Nouvelle tâche »
56. Constater que « Priorité » propose « Critique ${unique} »
57. Se connecter en tant que administration d'affaires IQ
58. Ouvrir l'écran `/clm/modeles`
59. Activer « Créer un modèle »
60. Activer « Ajouter un champ »
61. Constater que « Source Salesforce » propose « Région administrative ${unique} »

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-01 — TI-01 (RST-45) : l'administration technique règle le dépôt Microsoft 365 ; une trousse terminée est déposée dans SharePoint avec sa preuve, puis annoncée dans Teams

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-69, IQ-A-07
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/microsoft-365`
2. Voir le titre « Microsoft 365 : SharePoint, OneDrive et Teams »
3. Voir « Connexion Microsoft 365 »
4. Cocher « Déposer les trousses terminées dans Microsoft 365 »
5. Choisir « Bibliothèque de dépôt » : `Site SharePoint de l’organisation`
6. Saisir « Dossier de dépôt » : `Contrats signés E2E`
7. Cocher « Déposer aussi le dossier de preuve (ZIP) »
8. Cocher « Annoncer l’achèvement dans le canal Teams »
9. Activer « Enregistrer le dépôt »
10. Voir « Dépôt Microsoft 365 enregistré. »
11. Préparer par l'API : Trousse d'un document validée par le validateur : terminée, son document est verrouillé en version signée
12. Ouvrir l'écran `/clm/admin/microsoft-365`
13. Activer « Déposer maintenant »
14. Voir « dépôt(s) effectué(s). »
15. Voir « ${reference} », « Document signé », « Dossier de preuve », « Avis Teams »
16. Voir « sim://sharepoint/Contrats signés E2E/ »
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
18. Décocher « Déposer les trousses terminées dans Microsoft 365 »
19. Activer « Enregistrer le dépôt »
20. Voir « Dépôt Microsoft 365 enregistré. »

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-04 — Circuits séquentiels, parallèles, mixtes

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-05 — Validations en parallèle : un validateur rejette en motivant, la trousse est rejetée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-22, IQ-C-23, IQ-D-04, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (26 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PAR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `v2` ; « Libellé » : `Validation financière`
16. Choisir « Type » : `VALIDATION`
17. Saisir « Phase » : `1`
18. Activer « Ajouter un responsable »
19. Saisir « Responsable 1 » : `approbateur@iq.example`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que validateur IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PAR Validation juridique »
29. Activer « Rejeter »
30. Voir « Un motif est obligatoire »
31. Saisir « Commentaire » : `Montant hors programme`
32. Activer « Rejeter »
33. Voir « Décision enregistrée : rejeté. »
34. Se connecter en tant que initiateur IQ
35. Ouvrir l'écran `/clm/trousses`
36. Activer « Ouvrir le détail de CB${UNIQUE}-PAR » (link)
37. Voir « Rejetée »

### E2E-CB-IQ-09 — Signature séquentielle de deux signataires internes : le second n'est sollicité qu'après le premier

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-29, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Séquentielle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SSQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Activer « Ajouter un responsable »
29. Choisir « Type du responsable 1 » : `PARTICIPANT`
30. Choisir « Responsable 1 » : `Sophie Roy`
31. Activer « Tester le parcours »
32. Voir « Parcours valide. »
33. Activer « Enregistrer »
34. Activer « Continuer »
35. Activer « Lancer le parcours »
36. Arriver sur une adresse de la forme `/clm/trousses/\d+`
37. Se connecter en tant que approbateur IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SSQ »
40. Se connecter en tant que signataire interne IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SSQ Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Étienne Côté`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »
55. Se connecter en tant que approbateur IQ
56. Ouvrir l'écran `/clm/taches`
57. Activer « Traiter — CB${UNIQUE}-SSQ Signature cosignataire »
58. Activer « Signer maintenant »
59. Arriver sur une adresse de la forme `/portail/`
60. Activer « Recevoir mon code »
61. Voir « Code envoyé. »
62. Préparer par l'API : Code reçu par courriel
63. Saisir « Code de vérification » : `${code}`
64. Activer « Vérifier mon identité »
65. Activer « Lire / télécharger »
66. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
67. Saisir « Votre nom complet » : `Sophie Roy`
68. Activer « Signer »
69. Voir « Merci, votre signature est enregistrée »
70. Se connecter en tant que initiateur IQ
71. Ouvrir l'écran `/clm/trousses`
72. Activer « Ouvrir le détail de CB${UNIQUE}-SSQ » (link)
73. Voir « Terminée »
74. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-10 — Signature parallèle : les deux signataires sont sollicités en même temps

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-26, IQ-C-28, IQ-C-54, IQ-D-04
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (49 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle sig CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SPA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
14. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
15. Décocher « Externe »
16. Activer « Ajouter un signataire »
17. Voir « Sophie Roy »
18. Activer « Continuer »
19. Activer « Ajouter une étape »
20. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
21. Choisir « Type » : `SIGNATURE`
22. Activer « Ajouter un responsable »
23. Choisir « Type du responsable 1 » : `PARTICIPANT`
24. Choisir « Responsable 1 » : `Étienne Côté`
25. Activer « Ajouter une étape »
26. Saisir « Clé » : `s2` ; « Libellé » : `Signature cosignataire`
27. Choisir « Type » : `SIGNATURE`
28. Saisir « Phase » : `1`
29. Activer « Ajouter un responsable »
30. Choisir « Type du responsable 1 » : `PARTICIPANT`
31. Choisir « Responsable 1 » : `Sophie Roy`
32. Activer « Tester le parcours »
33. Voir « Parcours valide. »
34. Activer « Enregistrer »
35. Activer « Continuer »
36. Activer « Lancer le parcours »
37. Arriver sur une adresse de la forme `/clm/trousses/\d+`
38. Se connecter en tant que approbateur IQ
39. Ouvrir l'écran `/clm/taches`
40. Activer « Traiter — CB${UNIQUE}-SPA Signature cosignataire »
41. Activer « Signer maintenant »
42. Arriver sur une adresse de la forme `/portail/`
43. Activer « Recevoir mon code »
44. Voir « Code envoyé. »
45. Préparer par l'API : Code reçu par courriel
46. Saisir « Code de vérification » : `${code}`
47. Activer « Vérifier mon identité »
48. Activer « Lire / télécharger »
49. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
50. Saisir « Votre nom complet » : `Sophie Roy`
51. Activer « Signer »
52. Voir « Merci, votre signature est enregistrée »
53. Se connecter en tant que signataire interne IQ
54. Ouvrir l'écran `/clm/taches`
55. Activer « Traiter — CB${UNIQUE}-SPA Signature IQ »
56. Activer « Signer maintenant »
57. Arriver sur une adresse de la forme `/portail/`
58. Activer « Recevoir mon code »
59. Voir « Code envoyé. »
60. Préparer par l'API : Code reçu par courriel
61. Saisir « Code de vérification » : `${code}`
62. Activer « Vérifier mon identité »
63. Activer « Lire / télécharger »
64. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
65. Saisir « Votre nom complet » : `Étienne Côté`
66. Activer « Signer »
67. Voir « Merci, votre signature est enregistrée »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-07 — Réassignation en cours de processus : l'étape passe à un autre intervenant sans redémarrer le parcours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (21 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Réassignation CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Activer « Parcours » (tab)
21. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation juridique » (ligne ou carte cliquable)
22. Saisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `approbateur@iq.example` ; « Assignation par utilisateur, groupe ou règle » Motif » : `Validateur en congé`
23. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
24. Se connecter en tant que validateur IQ
25. Ouvrir l'écran `/clm/taches`
26. Ne pas voir « CB${UNIQUE}-REA »
27. Se connecter en tant que approbateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — CB${UNIQUE}-REA Validation juridique »
30. Activer « Valider »
31. Voir « Décision enregistrée »

### E2E-CB-IQ-08 — Attribution par groupe : l'administration ouvre « Groupes » depuis son menu, crée le groupe, y place le validateur et le renomme ; le validateur reçoit l'étape

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-19, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Groupes » (link)
3. Arriver sur une adresse de la forme `/clm/admin/groupes`
4. Saisir « Nouveau groupe » Code » : `JUR_${UNIQUE}` ; « Nouveau groupe » Libellé » : `Juridique ${unique}`
5. Activer « Créer le groupe »
6. Choisir « Ajouter un membre au groupe JUR_${UNIQUE} » Membre » : `validateur@iq.example`
7. Dans la section « Ajouter un membre au groupe JUR_${UNIQUE} », activer « Ajouter »
8. Voir « validateur@iq.example »
9. Saisir « Renommer le groupe JUR_${UNIQUE} » Libellé du groupe » : `Affaires juridiques ${unique}`
10. Dans la section « Renommer le groupe JUR_${UNIQUE} », activer « Renommer »
11. Voir « Groupe JUR_${UNIQUE} renommé. », « Affaires juridiques ${unique} »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
13. Se connecter en tant que initiateur IQ
14. Ouvrir l'écran `/clm/trousses/nouvelle`
15. Saisir « Titre » : `Groupe CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-GRP`
16. Activer « Enregistrer le brouillon »
17. Activer « Continuer »
18. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
19. Voir « entente-${unique}.pdf »
20. Activer « Continuer »
21. Activer « Continuer »
22. Activer « Ajouter une étape »
23. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
24. Choisir « Type » : `VALIDATION`
25. Activer « Affecter un groupe »
26. Saisir « Responsable 1 » : `JUR_${UNIQUE}`
27. Activer « Tester le parcours »
28. Voir « Parcours valide. »
29. Activer « Enregistrer »
30. Activer « Continuer »
31. Activer « Lancer le parcours »
32. Arriver sur une adresse de la forme `/clm/trousses/\d+`
33. Se connecter en tant que validateur IQ
34. Ouvrir l'écran `/clm/taches`
35. Activer « Traiter — CB${UNIQUE}-GRP Validation juridique »
36. Activer « Valider »
37. Voir « Décision enregistrée »

### E2E-CB-IQ-24 — Listes de référence configurées par l'administration de l'organisation : chaque valeur ajoutée, renommée ou retirée se répercute aussitôt dans les listes déroulantes des écrans d'affaires

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-67, IQ-D-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (36 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_UNITE`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `INNOV_${UNIQUE}` ; « Libellé » : `Innovation ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Voir « Innovation ${unique} »
8. Choisir « Liste » : `CLM_UNITE`
9. Activer « Ajouter une valeur »
10. Saisir « Code » : `ANCIENNE_${UNIQUE}` ; « Libellé » : `Unité dissoute ${unique}`
11. Activer « Enregistrer »
12. Voir « Valeur enregistrée. »
13. Voir « Unité dissoute ${unique} »
14. Activer « Retirer — Unité dissoute ${unique} » (button)
15. Voir « Valeur retirée des sélecteurs. »
16. Choisir « Liste » : `CLM_ROLE_CONTRAT`
17. Activer « Ajouter une valeur »
18. Saisir « Code » : `MANDATAIRE_${UNIQUE}` ; « Libellé » : `Mandataire ${unique}`
19. Activer « Enregistrer »
20. Voir « Valeur enregistrée. »
21. Voir « Mandataire ${unique} »
22. Choisir « Liste » : `CLM_CHAMP_SALESFORCE`
23. Activer « Ajouter une valeur »
24. Saisir « Code » : `Region_${UNIQUE}` ; « Libellé » : `Région administrative ${unique}`
25. Activer « Enregistrer »
26. Voir « Valeur enregistrée. »
27. Voir « Région administrative ${unique} »
28. Choisir « Liste » : `CLM_PRIORITE_TACHE`
29. Activer « Ajouter une valeur »
30. Saisir « Code » : `CRITIQUE_${UNIQUE}` ; « Libellé » : `Critique ${unique}`
31. Activer « Enregistrer »
32. Voir « Valeur enregistrée. »
33. Voir « Critique ${unique} »
34. Choisir « Liste » : `CLM_TYPE_CONTRAT`
35. Activer « Ajouter une valeur »
36. Saisir « Code » : `ACCORD_${UNIQUE}` ; « Libellé » : `Accord cadre ${unique}`
37. Activer « Enregistrer »
38. Voir « Valeur enregistrée. »
39. Voir « Accord cadre ${unique} »
40. Activer « Modifier — Accord cadre ${unique} » (button)
41. Saisir « Libellé » : `Accord-cadre de partenariat ${unique}`
42. Activer « Enregistrer »
43. Voir « Accord-cadre de partenariat ${unique} »
44. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
45. Se connecter en tant que initiateur IQ
46. Ouvrir l'écran `/clm/trousses/nouvelle`
47. Constater que « Unité responsable » propose « Innovation ${unique} » et ne propose plus « Unité dissoute ${unique} » ; Constater que « Type de contrat » propose « Accord-cadre de partenariat ${unique} » et ne propose plus « Accord cadre ${unique} »
48. Saisir « Titre » : `Références CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REF`
49. Choisir « Unité responsable » : `Innovation ${unique}` ; « Type de contrat » : `Accord-cadre de partenariat ${unique}`
50. Activer « Enregistrer le brouillon »
51. Activer « Continuer »
52. Activer « Continuer »
53. Constater que « Rôle dans le contrat » propose « Mandataire ${unique} »
54. Ouvrir l'écran `/clm/taches`
55. Activer « Nouvelle tâche »
56. Constater que « Priorité » propose « Critique ${unique} »
57. Se connecter en tant que administration d'affaires IQ
58. Ouvrir l'écran `/clm/modeles`
59. Activer « Créer un modèle »
60. Activer « Ajouter un champ »
61. Constater que « Source Salesforce » propose « Région administrative ${unique} »

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.comptes`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-06 — Modèles et clauses contractuelles

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-02 — Un modèle vide ne peut être soumis à publication

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Activer « Vérifier »
5. Voir « Le corps du contrat est vide. »
6. Activer « Soumettre à publication »
7. Voir « Le corps du contrat est vide. »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-03 — Séparation des responsabilités : l'auteur ne publie pas son propre modèle ; un administrateur le renvoie en brouillon

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Auteur CB ${unique}` ; « Corps du contrat » : `Lettre d'entente. {{clauses}}`
4. Activer « Enregistrer le brouillon »
5. Voir « Brouillon enregistré. »
6. Activer « Soumettre à publication »
7. Voir « soumis à publication »
8. Ouvrir l'écran `/clm/modeles`
9. Dans la ligne « Auteur CB ${unique} », activer « Publier »
10. Voir « l'auteur ne publie pas son propre modèle »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/modeles`
13. Dans la ligne « Auteur CB ${unique} », activer « Renvoyer en brouillon »
14. Voir « « Auteur CB ${unique} » renvoyé en brouillon. »

### E2E-CB-IQ-23 — Flexibilité en cours d'utilisation : l'organisation ajoute un type de contrat, change un paramètre et fait évoluer un modèle, sans développement ; les trousses suivantes en tiennent compte

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-17, IQ-C-55, IQ-D-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (24 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/listes`
2. Choisir « Liste » : `CLM_TYPE_CONTRAT`
3. Activer « Ajouter une valeur »
4. Saisir « Code » : `PILOTE_${UNIQUE}` ; « Libellé » : `Entente pilote ${unique}`
5. Activer « Enregistrer »
6. Voir « Valeur enregistrée. »
7. Ouvrir l'écran `/clm/admin/parametres`
8. Activer « Modifier un paramètre autorisé »
9. Saisir « Rappel tous les (jours) » : `6`
10. Activer « Enregistrer »
11. Voir « Paramètres enregistrés. »
12. Se connecter en tant que initiateur IQ
13. Ouvrir l'écran `/clm/trousses/nouvelle`
14. Constater la valeur « Rappel tous les (jours) » : `6`
15. Saisir « Titre » : `Flexible CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-FLX`
16. Choisir « Type de contrat » : `Entente pilote ${unique}`
17. Activer « Enregistrer le brouillon »
18. Voir « CB${UNIQUE}-FLX »
19. Se connecter en tant que administration d'affaires IQ
20. Ouvrir l'écran `/clm/modeles`
21. Activer « Créer un modèle »
22. Saisir « Nom » : `Évolutif CB ${unique}` ; « Corps du contrat » : `Version 1 de l'entente. {{clauses}}`
23. Activer « Soumettre à publication »
24. Voir « soumis à publication »
25. Se connecter en tant que administration principale IQ
26. Ouvrir l'écran `/clm/modeles`
27. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
28. Voir « « Évolutif CB ${unique} » publié. »
29. Se connecter en tant que administration d'affaires IQ
30. Ouvrir l'écran `/clm/modeles`
31. Dans la ligne « Évolutif CB ${unique} », activer « Nouvelle version »
32. Voir « Nouvelle version de « Évolutif CB ${unique} » créée. »
33. Activer « Modifier le brouillon — Évolutif CB ${unique} » (link)
34. Saisir « Corps du contrat » : `Version 2 de l'entente, clause de résiliation ajoutée. {{clauses}}`
35. Activer « Soumettre à publication »
36. Voir « soumis à publication »
37. Se connecter en tant que administration principale IQ
38. Ouvrir l'écran `/clm/modeles`
39. Saisir « Rechercher » : `Évolutif CB ${unique}`
40. Choisir « État » : `EN_REVISION`
41. Dans la ligne « Évolutif CB ${unique} », activer « Publier »
42. Voir « publié. »
43. Choisir « État » : `RETIRE`
44. Voir « Évolutif CB ${unique} »
45. Ouvrir l'écran `/clm/admin/parametres`
46. Activer « Modifier un paramètre autorisé »
47. Activer « Rétablir les paramètres de la plateforme »
48. Voir « Paramètres de la plateforme rétablis. »

### E2E-IQ-03 — Cycle de vie d'un modèle : rédaction, soumission, publication, nouvelle version

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-33, IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-06, IQ-C-17
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.modele.cycle`
2. Ouvrir l'écran `/clm/modeles`
3. Voir « Modèles »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-07 — Automatisation des notifications, rappels, règles d'affaires

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-08 — AF-46 (RST-49) : l'initiateur choisit la priorité critique d'une trousse à la création ; sans choix, la priorité se déduit du niveau de risque ; le détail l'affiche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-07, IQ-C-41
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Prio E2E ${unique}`
3. Choisir « Niveau de risque » : `Faible`
4. Constater que « Priorité » propose « Selon le niveau de risque (automatique) », « Basse », « Normale », « Haute », « Critique »
5. Choisir « Priorité » : `Critique`
6. Voir « Une trousse haute est relancée deux fois plus souvent, une critique quatre fois. »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Activer « Enregistrer le brouillon »
9. Voir « enregistré. »
10. Préparer par l'API : La trousse enregistrée à l'écran porte la priorité choisie ; une seconde, de risque élevé et sans priorité choisie, prend la priorité haute
11. Ouvrir l'écran `/clm/trousses/${choisie}`
12. Voir « Critique », « (choisie) »
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
14. Ouvrir l'écran `/clm/trousses/${deduite}`
15. Voir « Haute », « (selon le risque) »

### E2E-CB-IQ-12 — Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-40, IQ-C-24, IQ-C-58, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Désactiver — Invitation à signer Courriel » (button)
3. Voir « Message désactivé. »
4. Activer « Activer — Invitation à signer Courriel » (button)
5. Voir « Message activé. »
6. Activer « Désactiver — Rappel de non-réponse Courriel » (button)
7. Voir « Message désactivé. »
8. Activer « Activer — Rappel de non-réponse Courriel » (button)
9. Voir « Message activé. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-08 — Suivi centralisé

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-09 — AF-46 (RST-49) : l'administration d'affaires trie le suivi par priorité, la plus haute d'abord, le filtre sur une priorité et l'exporte avec la priorité

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-D-08
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trois trousses : risque faible (priorité basse), priorité critique choisie, risque élevé (priorité haute), créées dans cet ordre
2. Ouvrir l'écran `/clm/suivi`
3. Saisir « Filtres état » Rechercher » : `${unique}`
4. Choisir « Trier par » : `Priorité, la plus haute d’abord`
5. Dans la section « Filtres état », activer « Rechercher »
6. Relever le texte affiché (`table.clm-table tbody tr:first-child:has-text("Critique ${unique}")`) sous le nom `premiere`
7. Relever le texte affiché (`table.clm-table tbody tr:nth-child(2):has-text("Haute ${unique}")`) sous le nom `deuxieme`
8. Relever le texte affiché (`table.clm-table tbody tr:nth-child(3):has-text("Basse ${unique}")`) sous le nom `troisieme`
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
10. Choisir « Priorité » : `Haute`
11. Dans la section « Filtres état », activer « Rechercher »
12. Voir « Haute ${unique} »
13. Ne pas voir « Critique ${unique} », « Basse ${unique} »
14. Télécharger par « Exporter (CSV) » et obtenir un fichier non vide
15. Préparer par l'API : L'export du suivi porte la colonne de priorité, dans l'ordre demandé

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-IQ-16 — Suivi centralisé des trousses et des échéances

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-C-25, IQ-D-08
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.suivi`
2. Ouvrir l'écran `/clm/suivi`
3. Voir « Suivi »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-09 — Versions et historique des actions

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-16 — Documents d'une trousse : PDF de 25 Mo et document Word déposés, version remplacée avec motif, historique du document consulté ; le Word s'affiche dans la page, mise en page conservée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-47, IQ-C-50, IQ-D-03, IQ-D-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (15 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Documents CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-DOC`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « convention-25Mo-${unique}.pdf » au champ « Téléverser un document »
6. Voir « convention-25Mo-${unique}.pdf »
7. Joindre « annexe-${unique}.docx » au champ « Téléverser un document »
8. Voir « annexe-${unique}.docx »
9. Activer « Remplacer par une nouvelle version — annexe-${unique}.docx » (button)
10. Joindre « annexe-v2-${unique}.docx » au champ « Fichier »
11. Saisir « Motif » : `Montant corrigé`
12. Activer « Remplacer »
13. Voir « annexe-v2-${unique}.docx »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
15. Ouvrir l'écran `/clm/trousses`
16. Activer « Ouvrir le détail de CB${UNIQUE}-DOC » (link)
17. Activer « Historique » (tab)
18. Voir « Nouvelle version »
19. Activer « Documents (2) » (tab)
20. Activer « Versions — annexe-v2-${unique}.docx » (button)
21. Activer « Consulter les métadonnées — version 2 » (button)
22. Voir « Montant corrigé », « v2 »
23. Activer « Prévisualiser — annexe-v2-${unique}.docx » (button)
24. Voir l'aperçu « Aperçu : annexe-v2-${unique}.docx » affiché dans la page
25. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-10 — Piste d'audit complète

- **Source** : 3.1.1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-13 — Accessibilité des interfaces, niveau maintenu aux mises à jour

- **Source** : 3.3.1 A
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-31 — Aucune donnée de carte de paiement acceptée

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.cartes.pci`
2. Ouvrir l'écran `/clm/taches`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-15 — Gestion des rôles et des droits d'accès

- **Source** : 3.3.2
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-19 — Séparation des rôles : l'auditeur consulte une trousse sans pouvoir agir, l'administration technique n'accède pas aux trousses

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rôles CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-ROL`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-ROL »
5. Se connecter en tant que auditeur IQ
6. Ouvrir l'écran `/clm/trousses`
7. Activer « Ouvrir le détail de CB${UNIQUE}-ROL » (link)
8. Voir « Rôles CB ${unique} »
9. Ne pas voir « Lancer le parcours », « Annuler la trousse »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Se connecter en tant que administration technique IQ
12. Ouvrir l'écran `/clm/trousses`
13. Ne pas voir « CB${UNIQUE}-ROL »

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-03 — SEC-17 (RST-44) : l'administration d'affaires d'IQ accorde un accès de soutien à l'exploitant, voit sa consultation au journal de la trousse, puis le révoque ; sans accès, l'exploitant ne lit rien

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-16, IQ-A-26
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Une trousse d'IQ, et une demande d'accès de soutien de l'exploitant, refusée tant qu'elle n'est pas accordée
2. Ouvrir l'écran `/clm/admin/soutien`
3. Voir le titre « Accès de soutien de l’exploitant »
4. Voir « Billet E2E ${unique} »
5. Activer « Accorder l’accès à superadmin@plateforme.ca »
6. Voir « Accès accordé à superadmin@plateforme.ca. »
7. Se connecter en tant que superadmin
8. Ouvrir l'écran `/superadmin`
9. Choisir « Accès de soutien aux données CLM d'une organisation » Organisation » : `Investissement Québec`
10. Saisir « Référence de la trousse » : `${reference}`
11. Activer « Consulter le diagnostic »
12. Voir « ${reference} »
13. Ne pas voir « Soutien ${unique} »
14. Se connecter en tant que administration d'affaires IQ
15. Préparer par l'API : Pendant l'accès accordé, l'exploitant consulte le diagnostic de la trousse, sans son contenu
16. Ouvrir l'écran `/clm/trousses/${trousse}`
17. Activer « Historique » (tab)
18. Voir « Consultation de soutien de l’exploitant », « Billet E2E ${unique} »
19. Ouvrir l'écran `/clm/admin/soutien`
20. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
21. Activer « Révoquer l’accès de superadmin@plateforme.ca »
22. Voir « Accès de superadmin@plateforme.ca révoqué. »
23. Préparer par l'API : Révoqué : l'exploitant ne lit plus rien

### E2E-AX-IQ-04 — SEC-17 (RST-44) : depuis son espace, l'exploitant demande un accès de soutien motivé et limité ; sans accord de l'organisation, le diagnostic lui est refusé

- **Profil** : superadmin
- **Exigences démontrées** : IQ-D-16
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (5 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/superadmin`
2. Voir « Accès de soutien aux données CLM d'une organisation »
3. Choisir « Accès de soutien aux données CLM d'une organisation » Organisation » : `Investissement Québec`
4. Saisir « Motif (billet, symptôme) » : `Billet UI ${unique}` ; « Durée demandée (heures) » : `1`
5. Activer « Demander l'accès de soutien »
6. Voir « Demande transmise à l'organisation. », « Billet UI ${unique} », « En attente de l'organisation »
7. Saisir « Référence de la trousse » : `TR-INCONNUE`
8. Activer « Consulter le diagnostic »
9. Voir « Aucun accès de soutien accordé par l'organisation, ou accès expiré. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-19 — Séparation des rôles : l'auditeur consulte une trousse sans pouvoir agir, l'administration technique n'accède pas aux trousses

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rôles CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-ROL`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-ROL »
5. Se connecter en tant que auditeur IQ
6. Ouvrir l'écran `/clm/trousses`
7. Activer « Ouvrir le détail de CB${UNIQUE}-ROL » (link)
8. Voir « Rôles CB ${unique} »
9. Ne pas voir « Lancer le parcours », « Annuler la trousse »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Se connecter en tant que administration technique IQ
12. Ouvrir l'écran `/clm/trousses`
13. Ne pas voir « CB${UNIQUE}-ROL »

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-19 — Séparation des rôles : l'auditeur consulte une trousse sans pouvoir agir, l'administration technique n'accède pas aux trousses

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rôles CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-ROL`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-ROL »
5. Se connecter en tant que auditeur IQ
6. Ouvrir l'écran `/clm/trousses`
7. Activer « Ouvrir le détail de CB${UNIQUE}-ROL » (link)
8. Voir « Rôles CB ${unique} »
9. Ne pas voir « Lancer le parcours », « Annuler la trousse »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Se connecter en tant que administration technique IQ
12. Ouvrir l'écran `/clm/trousses`
13. Ne pas voir « CB${UNIQUE}-ROL »

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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

### E2E-IQ-26 — Comptes, groupes et rôles administrés

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-05, IQ-D-18, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.acces.comptes`
2. Ouvrir l'écran `/clm/admin/utilisateurs`
3. Voir « Utilisateurs »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-19 — Journalisation et traçabilité des activités

- **Source** : 3.3.2
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-07 — TI-04 (RST-46) : l'administration technique crée un client SIEM ; Microsoft Sentinel lit le journal d'audit par curseur (ASIM, CEF) avec son jeton, sans session, et ne peut rien d'autre

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-19
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/integrations`
2. Saisir « Nouveau client d’intégration » Nom » : `Sentinel E2E ${unique}`
3. Choisir « Nouveau client d’intégration » Système » : `SIEM (Microsoft Sentinel) — lecture du journal seulement`
4. Voir « ne déclenche aucune signature »
5. Activer « Créer le client »
6. Voir « Client « Sentinel E2E ${unique} » créé. »
7. Relever le texte affiché (`.clm-info-statique .clm-dl div:nth-child(1) code`) sous le nom `clientId`
8. Relever le texte affiché (`.clm-info-statique .clm-dl div:nth-child(2) code`) sous le nom `secret`
9. Préparer par l'API : Le SIEM obtient un jeton (client credentials) puis lit le journal page par page ; il ne crée aucune trousse
10. Activer « J’ai copié les valeurs »
11. Voir « Sentinel E2E ${unique} »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-21 — SEC-27 (RST-53) : l'administration d'affaires choisit, dans le catalogue Purview du locataire, l'étiquette de sensibilité et de rétention d'un type de contrat ; le document déposé la porte et la conserve, puis la politique est retirée

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-D-20
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (5 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/purview`
2. Voir le titre « Étiquettes Microsoft Purview »
3. Choisir « Type de contrat » : `Convention d'aide financière`
4. Choisir « Étiquette de sensibilité » : `Confidentiel`
5. Choisir « Étiquette de rétention » : `Contrats - 7 ans (2557 jours)`
6. Activer « Enregistrer la politique »
7. Voir « Politique « Convention d'aide financière » enregistrée. »
8. Voir « Confidentiel », « Contrats - 7 ans (2557 jours) »
9. Préparer par l'API : L'initiateur dépose un document dans une trousse de ce type : il reçoit les étiquettes de la politique
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Activer « Retirer — Convention d'aide financière »
12. Voir « Politique « Convention d'aide financière » retirée. »

### E2E-AX-IQ-25 — SEC-28 (RST-54) : les documents d'IQ sont chiffrés par une clé dédiée à l'organisation ; l'administration technique pivote la clé sans interruption puis réenveloppe les documents, qui restent lisibles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Un document déposé par l'initiateur : la première écriture crée la clé de données de l'organisation
2. Ouvrir l'écran `/clm/admin/chiffrement`
3. Voir le titre « Chiffrement au repos »
4. Voir « Clé de l’organisation », « Clé maîtresse active », « Active »
5. Activer « Pivoter la clé »
6. Voir « Rotation effectuée »
7. Voir « Retirée (lecture seule) »
8. Préparer par l'API : Après la rotation, le document chiffré par l'ancienne version reste lisible
9. Activer « Réenvelopper les documents »
10. Voir « Réenveloppement : »
11. Préparer par l'API : Après le réenveloppement, le document se lit toujours en clair par l'API
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-30 — Isolation des données entre organisations et modules

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-07, IQ-D-20
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.isolation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « ${trousseRef} »

## IQ-D-21 — Interfaces en français, obligatoirement

- **Source** : 3.3.3
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-CB-IQ-21 — Interfaces en français, offertes en anglais : l'utilisateur change de langue, la console d'administration reste en français

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Voir « Boîte de tâches »
3. Passer l'interface en anglais
4. Ouvrir l'écran `/clm/taches`
5. Voir « Task inbox »
6. Activer « My packages » (link)
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français
9. Ouvrir l'écran `/clm/taches`
10. Voir « Boîte de tâches »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/admin/integrations`
13. Voir « Intégrations API », « Journal des retours de statut »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-CB-IQ-21 — Interfaces en français, offertes en anglais : l'utilisateur change de langue, la console d'administration reste en français

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Voir « Boîte de tâches »
3. Passer l'interface en anglais
4. Ouvrir l'écran `/clm/taches`
5. Voir « Task inbox »
6. Activer « My packages » (link)
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français
9. Ouvrir l'écran `/clm/taches`
10. Voir « Boîte de tâches »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/admin/integrations`
13. Voir « Intégrations API », « Journal des retours de statut »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-CB-IQ-21 — Interfaces en français, offertes en anglais : l'utilisateur change de langue, la console d'administration reste en français

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Voir « Boîte de tâches »
3. Passer l'interface en anglais
4. Ouvrir l'écran `/clm/taches`
5. Voir « Task inbox »
6. Activer « My packages » (link)
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français
9. Ouvrir l'écran `/clm/taches`
10. Voir « Boîte de tâches »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/admin/integrations`
13. Voir « Intégrations API », « Journal des retours de statut »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-27 — Échanges de données, documents, modèles, statuts, signataires, instructions, preuves

- **Source** : 3.3.5
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-28 — API et connecteurs sécurisés, documentés, maintenus

- **Source** : 3.3.6
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-D-29 — Interopérabilité permettant l'évolution

- **Source** : 3.3.8
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-23 — Interfaces standards et documentées (OpenAPI, SCIM)

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-D-29, IQ-C-16
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.api.documentee`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-S-01 — Création : trousse, documents, participants, positionnement des champs, modèle, configuration

- **Source** : 7.3 sc. 1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-15 — Champs d'un modèle : liste, date, case à cocher, zone de signature positionnée pour un rôle, clause conditionnelle vérifiée par l'essai

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-34, IQ-C-35, IQ-C-36, IQ-C-37, IQ-C-38, IQ-D-01, IQ-S-01
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Champs CB ${unique}` ; « Corps du contrat » : `Programme {{programme}} à compter du {{debut}}. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Choisir « Type » : `LISTE`
7. Saisir « Options (séparées par des virgules) » : `ESSOR, IMPULSION`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `debut` ; « Libellé » : `Date de début`
10. Choisir « Type » : `DATE`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `garantie` ; « Libellé » : `Garantie exigée`
13. Choisir « Type » : `CASE`
14. Activer « Ajouter un champ »
15. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
16. Choisir « Type » : `SIGNATURE`
17. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
18. Activer « Positionner sur le document »
19. Saisir « Haut (%) » : `75`
20. Voir « Zone de signature », « Premier signataire »
21. Activer « Ajouter une clause »
22. Saisir « Titre » : `Garantie` ; « Texte » : `Le bénéficiaire fournit une garantie de prêt.`
23. Choisir « Garantie » Si le champ » : `garantie`
24. Choisir « Garantie » Opérateur » : `EGAL`
25. Saisir « Garantie » Valeur » : `true`
26. Activer « Enregistrer le brouillon »
27. Voir « Brouillon enregistré. »
28. Activer « Vérifier »
29. Voir « Modèle valide. »
30. Saisir « Programme » : `ESSOR` ; « Date de début » : `2026-12-01` ; « Garantie exigée » : `true`
31. Activer « Tester la règle »
32. Voir « Clauses retenues : clause1 », « Le bénéficiaire fournit une garantie de prêt. »
33. Saisir « Garantie exigée » : `false`
34. Activer « Tester la règle »
35. Voir « Clauses retenues : — »
36. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-01 — Créer une trousse, déposer documents et annexes, inviter un participant externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-44, IQ-C-45, IQ-C-46, IQ-C-50, IQ-D-03, IQ-D-09, IQ-S-01, IQ-C-42, IQ-C-57
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.trousse.creation`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-02 — Validation : réception, consultation, commentaires, validation, passage à l'étape suivante

- **Source** : 7.3 sc. 2
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-04 — Circuit séquentiel saisi à l'écran : validation puis approbation, décisions commentées et historisées, suivi de l'état

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-D-08, IQ-D-09, IQ-S-02, IQ-S-03
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (29 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Circuit CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SEQ`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `a1` ; « Libellé » : `Approbation`
16. Choisir « Type » : `APPROBATION`
17. Activer « Ajouter un responsable »
18. Saisir « Responsable 1 » : `approbateur@iq.example`
19. Activer « Tester le parcours »
20. Voir « Parcours valide. »
21. Activer « Enregistrer »
22. Activer « Continuer »
23. Activer « Lancer le parcours »
24. Arriver sur une adresse de la forme `/clm/trousses/\d+`
25. Activer « Parcours » (tab)
26. Voir « Validation juridique »
27. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
28. Se connecter en tant que validateur IQ
29. Ouvrir l'écran `/clm/taches`
30. Activer « Traiter — CB${UNIQUE}-SEQ Validation juridique »
31. Saisir « Commentaire » : `Clauses conformes au programme`
32. Activer « Valider »
33. Voir « Décision enregistrée »
34. Se connecter en tant que approbateur IQ
35. Ouvrir l'écran `/clm/taches`
36. Activer « Traiter — CB${UNIQUE}-SEQ Approbation »
37. Saisir « Commentaire » : `Approuvé selon la délégation`
38. Activer « Approuver »
39. Voir « Décision enregistrée : approuvé. »
40. Se connecter en tant que initiateur IQ
41. Ouvrir l'écran `/clm/suivi`
42. Voir « CB${UNIQUE}-SEQ »
43. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
44. Ouvrir l'écran `/clm/trousses`
45. Voir « Terminée »
46. Activer « Ouvrir le détail de CB${UNIQUE}-SEQ » (link)
47. Activer « Historique » (tab)
48. Voir « Clauses conformes au programme », « Approuvé selon la délégation »

### E2E-CB-IQ-06 — Retour pour correction : le validateur renvoie à l'initiateur avec un motif

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-C-55, IQ-S-03, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (20 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Correction CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-COR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Se connecter en tant que validateur IQ
21. Ouvrir l'écran `/clm/taches`
22. Activer « Traiter — CB${UNIQUE}-COR Validation juridique »
23. Saisir « Commentaire » : `Annexe B manquante`
24. Activer « Retourner pour correction »
25. Voir « Trousse retournée pour correction. »
26. Se connecter en tant que initiateur IQ
27. Ouvrir l'écran `/clm/trousses`
28. Activer « Ouvrir le détail de CB${UNIQUE}-COR » (link)
29. Activer « Historique » (tab)
30. Voir « Annexe B manquante »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-05 — Ajout, remplacement, délégation, traçabilité ; mise à jour d'une donnée et son impact

- **Source** : 7.3 sc. 5
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-02 — AF-34 et SEC-15 (RST-43) : le modèle déclare les champs modifiables en cours ; en traitement, un champ figé est refusé, une modification permise relance le cycle et le journal nomme le champ, l'ancienne et la nouvelle valeur

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-05, IQ-A-18, IQ-C-49
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (19 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `AF34 E2E ${unique}` ; « Corps du contrat » : `Convention {{programme}} de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Activer « Ajouter un champ »
7. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
8. Voir « Modification en cours de traitement »
9. Cocher « Modifiable en cours de traitement »
10. Choisir « Effet sur les approbations obtenues » : `Nouveau cycle : toutes les approbations sont invalidées`
11. Activer « Enregistrer le brouillon »
12. Voir « Brouillon enregistré. »
13. Activer « Soumettre à publication »
14. Voir « soumis à publication »
15. Se connecter en tant que administration principale IQ
16. Ouvrir l'écran `/clm/modeles`
17. Dans la ligne « AF34 E2E ${unique} », activer « Publier »
18. Voir « « AF34 E2E ${unique} » publié. »
19. Se connecter en tant que initiateur IQ
20. Préparer par l'API : Trousse issue du modèle, valeurs saisies en brouillon, document joint et parcours lancé (validation en cours)
21. Ouvrir l'écran `/clm/trousses/${trousse}`
22. Activer « Corriger la valeur — Programme »
23. Saisir « Nouvelle valeur — Programme » : `Autre programme`
24. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
25. Voir « « Programme » n'est pas modifiable en cours de traitement. »
26. Activer « Annuler »
27. Activer « Corriger la valeur — Montant »
28. Saisir « Nouvelle valeur — Montant » : `60000`
29. Dans la section « Corriger la valeur — Montant », activer « Enregistrer »
30. Voir « Valeur de « Montant » enregistrée. »
31. Activer « Historique » (tab)
32. Voir « Valeur modifiée (avant → après) », « « Montant » (montant) : « 50000 » → « 60000 » », « Nouveau cycle (modification en cours de parcours) »
33. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-07 — Réassignation en cours de processus : l'étape passe à un autre intervenant sans redémarrer le parcours

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (21 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Réassignation CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-REA`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Activer « Parcours » (tab)
21. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Validation juridique » (ligne ou carte cliquable)
22. Saisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `approbateur@iq.example` ; « Assignation par utilisateur, groupe ou règle » Motif » : `Validateur en congé`
23. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
24. Se connecter en tant que validateur IQ
25. Ouvrir l'écran `/clm/taches`
26. Ne pas voir « CB${UNIQUE}-REA »
27. Se connecter en tant que approbateur IQ
28. Ouvrir l'écran `/clm/taches`
29. Activer « Traiter — CB${UNIQUE}-REA Validation juridique »
30. Activer « Valider »
31. Voir « Décision enregistrée »

### E2E-CB-IQ-11 — Signataire ajouté puis substitué en cours de processus, sans relancer la trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-30, IQ-C-31, IQ-C-55, IQ-S-05
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (39 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Substitution CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SUB`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Activer « Participants (1) » (tab)
27. Saisir « Nom » : `Sophie Roy` ; « Courriel » : `approbateur@iq.example` ; « Téléphone » : `+15145550190`
28. Choisir « Rôle dans le contrat » : `SIGNATAIRE_2`
29. Décocher « Externe »
30. Activer « Ajouter un signataire »
31. Voir « Sophie Roy »
32. Activer « Parcours » (tab)
33. Activer l'élément « Réassigner, ajouter ou retirer des intervenants — Signature IQ » (ligne ou carte cliquable)
34. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `Sophie Roy`
35. Saisir « Assignation par utilisateur, groupe ou règle » Motif » : `Signataire remplacé par délégation`
36. Dans la section « Assignation par utilisateur, groupe ou règle », activer « Enregistrer »
37. Se connecter en tant que signataire interne IQ
38. Ouvrir l'écran `/clm/taches`
39. Ne pas voir « CB${UNIQUE}-SUB »
40. Se connecter en tant que approbateur IQ
41. Ouvrir l'écran `/clm/taches`
42. Activer « Traiter — CB${UNIQUE}-SUB Signature IQ »
43. Activer « Signer maintenant »
44. Arriver sur une adresse de la forme `/portail/`
45. Activer « Recevoir mon code »
46. Voir « Code envoyé. »
47. Préparer par l'API : Code reçu par courriel
48. Saisir « Code de vérification » : `${code}`
49. Activer « Vérifier mon identité »
50. Activer « Lire / télécharger »
51. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
52. Saisir « Votre nom complet » : `Sophie Roy`
53. Activer « Signer »
54. Voir « Merci, votre signature est enregistrée »

### E2E-IQ-08 — Étapes parallèles et réassignation d'une tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-20, IQ-C-21, IQ-C-55, IQ-D-05, IQ-S-05
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signataires.en-cours`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Ouvrir l'écran `/clm/trousses/${trousse}?onglet=participants`
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-06 — Rappels configurés, notifications, rappels manuels, alertes d'échéance, communications personnalisées

- **Source** : 7.3 sc. 6
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-12 — Notifications configurables : l'administration désactive puis réactive l'invitation à signer par courriel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-40, IQ-C-24, IQ-C-58, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (4 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/notifications`
2. Activer « Désactiver — Invitation à signer Courriel » (button)
3. Voir « Message désactivé. »
4. Activer « Activer — Invitation à signer Courriel » (button)
5. Voir « Message activé. »
6. Activer « Désactiver — Rappel de non-réponse Courriel » (button)
7. Voir « Message désactivé. »
8. Activer « Activer — Rappel de non-réponse Courriel » (button)
9. Voir « Message activé. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-CB-IQ-13 — Rappels, copies et échéance réglés à la création d'une trousse, puis retrouvés au résumé

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-41, IQ-C-42, IQ-C-24, IQ-C-43, IQ-D-07, IQ-S-06
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rappels CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RAP` ; « Échéance » : `${dansUnMois}` ; « Destinataires en copie » : `direction.${unique}@iq.example, conformite.${unique}@iq.example` ; « Rappel tous les (jours) » : `3` ; « Nombre maximal de rappels » : `4`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-RAP »
5. Ouvrir l'écran `/clm/trousses`
6. Activer « Ouvrir le détail de CB${UNIQUE}-RAP » (link)
7. Voir « direction.${unique}@iq.example », « conformite.${unique}@iq.example »
8. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-15 — Messages désactivés puis réactivés, invitation et rappel reçus

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-24, IQ-C-40, IQ-C-41, IQ-S-06, IQ-D-07, IQ-C-58
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.notifications`
2. Ouvrir l'écran `/clm/admin/notifications`
3. Voir « Notifications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-07 — Journal, horodatages, actions, authentification, dossier de preuve

- **Source** : 7.3 sc. 7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-11 — Signature externe : invitation, identité, consentement, signature, preuve

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-04, IQ-C-61, IQ-C-62, IQ-C-63, IQ-C-64, IQ-C-03, IQ-C-57, IQ-S-07, IQ-A-04, IQ-C-54
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-34 — Consulter ses tâches et ses trousses sur téléphone, sans débordement ni violation d'accessibilité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-53, IQ-S-08, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
3. Ouvrir l'écran `/clm/trousses`
4. Voir « Trousses »
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-S-09 — Refus, commentaires justificatifs, retour à l'étape appropriée, reprise, historique

- **Source** : 7.3 sc. 9
- **Conformité déclarée** : ✅🆕
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-05 — Validations en parallèle : un validateur rejette en motivant, la trousse est rejetée

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-18, IQ-C-22, IQ-C-23, IQ-D-04, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (26 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Parallèle CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PAR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `v2` ; « Libellé » : `Validation financière`
16. Choisir « Type » : `VALIDATION`
17. Saisir « Phase » : `1`
18. Activer « Ajouter un responsable »
19. Saisir « Responsable 1 » : `approbateur@iq.example`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que validateur IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PAR Validation juridique »
29. Activer « Rejeter »
30. Voir « Un motif est obligatoire »
31. Saisir « Commentaire » : `Montant hors programme`
32. Activer « Rejeter »
33. Voir « Décision enregistrée : rejeté. »
34. Se connecter en tant que initiateur IQ
35. Ouvrir l'écran `/clm/trousses`
36. Activer « Ouvrir le détail de CB${UNIQUE}-PAR » (link)
37. Voir « Rejetée »

### E2E-CB-IQ-06 — Retour pour correction : le validateur renvoie à l'initiateur avec un motif

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-22, IQ-C-55, IQ-S-03, IQ-S-09
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (20 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Correction CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-COR`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Activer « Continuer »
9. Activer « Ajouter une étape »
10. Saisir « Clé » : `v1` ; « Libellé » : `Validation juridique`
11. Choisir « Type » : `VALIDATION`
12. Activer « Ajouter un responsable »
13. Saisir « Responsable 1 » : `validateur@iq.example`
14. Activer « Tester le parcours »
15. Voir « Parcours valide. »
16. Activer « Enregistrer »
17. Activer « Continuer »
18. Activer « Lancer le parcours »
19. Arriver sur une adresse de la forme `/clm/trousses/\d+`
20. Se connecter en tant que validateur IQ
21. Ouvrir l'écran `/clm/taches`
22. Activer « Traiter — CB${UNIQUE}-COR Validation juridique »
23. Saisir « Commentaire » : `Annexe B manquante`
24. Activer « Retourner pour correction »
25. Voir « Trousse retournée pour correction. »
26. Se connecter en tant que initiateur IQ
27. Ouvrir l'écran `/clm/trousses`
28. Activer « Ouvrir le détail de CB${UNIQUE}-COR » (link)
29. Activer « Historique » (tab)
30. Voir « Annexe B manquante »

### E2E-IQ-06 — Parcours de validation puis d'approbation, décisions tracées

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-01, IQ-C-18, IQ-C-22, IQ-C-23, IQ-C-25, IQ-C-48, IQ-D-04, IQ-S-02, IQ-S-03, IQ-S-09, IQ-C-52
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.rejet`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Décision : rejeté »

## IQ-S-10 — Téléchargement du signé, export du certificat, du journal, des métadonnées ; formats ; portabilité

- **Source** : 7.3 sc. 10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-18 — Dossier de preuve d'une trousse signée : intégrité vérifiée, signatures et consentements consultés, certificat et dossier complet exportés ; journal d'audit filtré par trousse

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-63, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (37 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Preuve CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-PRV`
3. Activer « Enregistrer le brouillon »
4. Activer « Continuer »
5. Joindre « entente-${unique}.pdf » au champ « Téléverser un document »
6. Voir « entente-${unique}.pdf »
7. Activer « Continuer »
8. Saisir « Nom » : `Étienne Côté` ; « Courriel » : `signataire@iq.example` ; « Téléphone » : `+15145550190`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Décocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Étienne Côté »
13. Activer « Continuer »
14. Activer « Ajouter une étape »
15. Saisir « Clé » : `s1` ; « Libellé » : `Signature IQ`
16. Choisir « Type » : `SIGNATURE`
17. Activer « Ajouter un responsable »
18. Choisir « Type du responsable 1 » : `PARTICIPANT`
19. Choisir « Responsable 1 » : `Étienne Côté`
20. Activer « Tester le parcours »
21. Voir « Parcours valide. »
22. Activer « Enregistrer »
23. Activer « Continuer »
24. Activer « Lancer le parcours »
25. Arriver sur une adresse de la forme `/clm/trousses/\d+`
26. Se connecter en tant que signataire interne IQ
27. Ouvrir l'écran `/clm/taches`
28. Activer « Traiter — CB${UNIQUE}-PRV Signature IQ »
29. Activer « Signer maintenant »
30. Arriver sur une adresse de la forme `/portail/`
31. Activer « Recevoir mon code »
32. Voir « Code envoyé. »
33. Préparer par l'API : Code reçu par courriel
34. Saisir « Code de vérification » : `${code}`
35. Activer « Vérifier mon identité »
36. Activer « Lire / télécharger »
37. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
38. Saisir « Votre nom complet » : `Étienne Côté`
39. Activer « Signer »
40. Voir « Merci, votre signature est enregistrée »
41. Se connecter en tant que initiateur IQ
42. Ouvrir l'écran `/clm/trousses`
43. Activer « Ouvrir le détail de CB${UNIQUE}-PRV » (link)
44. Activer « Preuve » (tab)
45. Activer « Vérifier l’intégrité »
46. Voir « Intégrité confirmée : journal, documents et attestations intacts. »
47. Voir « Identités, consentements et signatures », « Étienne Côté »
48. Télécharger par « Télécharger le certificat (PDF) » et obtenir un fichier non vide
49. Télécharger par « Exporter le dossier complet (ZIP) » et obtenir un fichier non vide
50. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
51. Se connecter en tant que administration principale IQ
52. Ouvrir l'écran `/clm/admin/journal`
53. Saisir « Trousse (référence) » : `CB${UNIQUE}-PRV`
54. Activer « Rechercher #2 »
55. Voir « Trousse créée », « Document ajouté »
56. Télécharger par « Exporter le journal (CSV) » et obtenir un fichier non vide

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-32 — Analyse d'un document par l'IA encadrée, depuis l'accueil

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-59, IQ-C-04, IQ-S-10b
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.ia.analyse`
2. Ouvrir l'écran `/clm`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-01 — TI-22 : intégration Workday (déclenchement, statuts, document signé)

- **Source** : Add. § 4.4
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-02 — AF-47 : lot de signatures depuis Salesforce

- **Source** : Add. Q10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-03 — AF-47 : lot depuis la plateforme

- **Source** : Add. Q10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-01 — Modèle créé et configuré à l'écran, soumis, publié par une autre administration, puis utilisé en signature en lot (un dossier sans téléphone échoue seul)

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-32, IQ-C-35, IQ-C-11, IQ-A-03, IQ-D-06, IQ-A-02, IQ-D-01, IQ-D-02
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (22 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Voir « Nouveau modèle »
4. Saisir « Nom » : `Entente CB ${unique}`
5. Choisir « Catégorie » : `(premier)`
6. Saisir « Corps du contrat » : `Entente avec {{beneficiaire}} pour un montant de {{montant}} $. {{clauses}}`
7. Activer « Ajouter un champ »
8. Saisir « Nom technique » : `beneficiaire` ; « Libellé » : `Bénéficiaire`
9. Cocher « Obligatoire »
10. Choisir « Source Salesforce » : `Demandeur`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant` ; « Format (expression régulière) » : `^[0-9]+$`
13. Activer « Ajouter une clause »
14. Saisir « Titre » : `Versement` ; « Texte » : `Le montant est versé en un seul paiement.`
15. Activer « Enregistrer le brouillon »
16. Voir « Brouillon enregistré. »
17. Saisir « Bénéficiaire » : `Entreprise Essai inc.` ; « Montant » : `25000`
18. Activer « Tester la règle »
19. Voir « Entente avec Entreprise Essai inc. pour un montant de 25000 $ »
20. Activer « Vérifier »
21. Voir « Modèle valide. »
22. Activer « Soumettre à publication »
23. Voir « soumis à publication »
24. Se connecter en tant que administration principale IQ
25. Ouvrir l'écran `/clm/modeles`
26. Dans la ligne « Entente CB ${unique} », activer « Publier »
27. Voir « « Entente CB ${unique} » publié. »
28. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
29. Se connecter en tant que initiateur IQ
30. Ouvrir l'écran `/clm/lots`
31. Saisir « Libellé du lot » : `Lot CB ${unique}`
32. Choisir « Modèle de contrat » : `Entente CB ${unique}` ; « Première colonne » : `Numéro de dossier Salesforce`
33. Saisir « Dossiers, un par ligne » : `DEM-2026-0001;Jeanne Tremblay;jeanne.tremblay@exemple.ca;+15145550101
DEM-2026-0002;Marc Gagnon;marc.gagnon@exemple.ca;`
34. Voir « 2 dossier(s) dans le lot. »
35. Activer « Créer le lot »
36. Voir « Lot « Lot CB ${unique} » : 1 trousse(s) créée(s), 1 échec(s). »
37. Voir « DEM-2026-0001 »
38. Voir « téléphone requis pour la vérification par SMS »
39. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-22 — Signature en lot et approbation globale

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-03, IQ-A-28
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-06 — Environ 30 champs Salesforce préremplis par modèle

- **Source** : Add. Q8
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-01 — TI-01 (RST-45) : l'administration technique règle le dépôt Microsoft 365 ; une trousse terminée est déposée dans SharePoint avec sa preuve, puis annoncée dans Teams

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-C-69, IQ-A-07
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/microsoft-365`
2. Voir le titre « Microsoft 365 : SharePoint, OneDrive et Teams »
3. Voir « Connexion Microsoft 365 »
4. Cocher « Déposer les trousses terminées dans Microsoft 365 »
5. Choisir « Bibliothèque de dépôt » : `Site SharePoint de l’organisation`
6. Saisir « Dossier de dépôt » : `Contrats signés E2E`
7. Cocher « Déposer aussi le dossier de preuve (ZIP) »
8. Cocher « Annoncer l’achèvement dans le canal Teams »
9. Activer « Enregistrer le dépôt »
10. Voir « Dépôt Microsoft 365 enregistré. »
11. Préparer par l'API : Trousse d'un document validée par le validateur : terminée, son document est verrouillé en version signée
12. Ouvrir l'écran `/clm/admin/microsoft-365`
13. Activer « Déposer maintenant »
14. Voir « dépôt(s) effectué(s). »
15. Voir « ${reference} », « Document signé », « Dossier de preuve », « Avis Teams »
16. Voir « sim://sharepoint/Contrats signés E2E/ »
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
18. Décocher « Déposer les trousses terminées dans Microsoft 365 »
19. Activer « Enregistrer le dépôt »
20. Voir « Dépôt Microsoft 365 enregistré. »

### E2E-IQ-19 — API d'intégration : client, jeton, création de trousse par un système

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-01, IQ-A-02, IQ-C-06, IQ-C-16, IQ-C-69, IQ-D-12, IQ-D-26, IQ-D-27, IQ-D-28, IQ-A-07, IQ-C-03
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.integration.api`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-08 — Provisionnement et déprovisionnement SCIM 2.0 (Saviynt)

- **Source** : Add. Q3, add. 3 Q51
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-20 — TI-21 (RST-52) : l'administration technique crée le jeton SCIM ; Saviynt provisionne un compte, puis découvre par SCIM ses rôles effectifs (roles) et ses groupes du module (entitlements), en lecture seule

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/admin/sso`
2. Voir « Provisionnement SCIM 2.0 », « Découverte des droits »
3. Activer « Créer un jeton | Remplacer le jeton »
4. Voir « Jeton créé. »
5. Relever le texte affiché (`.clm-info-statique code`) sous le nom `jetonScim`
6. Préparer par l'API : Saviynt provisionne un compte ; l'administration le place dans un groupe du module ; SCIM expose ses droits effectifs
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-09 — Connecteur MCP

- **Source** : Add. Q3
- **Conformité déclarée** : ✅🆕 — connecteur MCP en lecture seule — ADR-023
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-24 — Connecteur MCP en lecture seule pour un assistant d'IA

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-09
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.mcp`
2. Ouvrir l'écran `/clm/admin/integrations`
3. Voir « Intégrations »

## IQ-A-10 — Authentification Salesforce de remplacement (JWT Bearer) décrite

- **Source** : Add. 2 Q11
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-11 — Exportation ou transfert des documents et métadonnées vers un référentiel désigné par IQ

- **Source** : Add. 2 Q13
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-12 — Retour des statuts lancée, retournée, rejetée, annulée, terminée

- **Source** : Add. 2 Q14
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-13 — Seules les données nécessaires transférées depuis Salesforce

- **Source** : Add. 2 Q15
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-21 — Minimisation des données échangées avec Salesforce

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.minimisation`
2. Ouvrir l'écran `/clm/trousses`
3. Voir « Trousses »

## IQ-A-14 — Limites d'API, surveillance, journalisation, reprise (8 tentatives, plafond 1 h)

- **Source** : Add. 2 Q17
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-20 — Statuts renvoyés à Salesforce à chaque jalon

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-08, IQ-C-13, IQ-C-15, IQ-A-05, IQ-A-10, IQ-A-12, IQ-A-14
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.salesforce.statuts`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)

## IQ-A-15 — Interfaces internes et externes en français, en anglais sur demande d'IQ

- **Source** : Add. 2 Q19
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-CB-IQ-21 — Interfaces en français, offertes en anglais : l'utilisateur change de langue, la console d'administration reste en français

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/taches`
2. Voir « Boîte de tâches »
3. Passer l'interface en anglais
4. Ouvrir l'écran `/clm/taches`
5. Voir « Task inbox »
6. Activer « My packages » (link)
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Passer l'interface en français
9. Ouvrir l'écran `/clm/taches`
10. Voir « Boîte de tâches »
11. Se connecter en tant que administration principale IQ
12. Ouvrir l'écran `/clm/admin/integrations`
13. Voir « Intégrations API », « Journal des retours de statut »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-35 — Interfaces et console d'administration en français, offertes en anglais

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-21, IQ-D-22, IQ-D-23, IQ-A-15, IQ-D-13
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-02 — AF-34 et SEC-15 (RST-43) : le modèle déclare les champs modifiables en cours ; en traitement, un champ figé est refusé, une modification permise relance le cycle et le journal nomme le champ, l'ancienne et la nouvelle valeur

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-S-05, IQ-A-18, IQ-C-49
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (19 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `AF34 E2E ${unique}` ; « Corps du contrat » : `Convention {{programme}} de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `programme` ; « Libellé » : `Programme`
6. Activer « Ajouter un champ »
7. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
8. Voir « Modification en cours de traitement »
9. Cocher « Modifiable en cours de traitement »
10. Choisir « Effet sur les approbations obtenues » : `Nouveau cycle : toutes les approbations sont invalidées`
11. Activer « Enregistrer le brouillon »
12. Voir « Brouillon enregistré. »
13. Activer « Soumettre à publication »
14. Voir « soumis à publication »
15. Se connecter en tant que administration principale IQ
16. Ouvrir l'écran `/clm/modeles`
17. Dans la ligne « AF34 E2E ${unique} », activer « Publier »
18. Voir « « AF34 E2E ${unique} » publié. »
19. Se connecter en tant que initiateur IQ
20. Préparer par l'API : Trousse issue du modèle, valeurs saisies en brouillon, document joint et parcours lancé (validation en cours)
21. Ouvrir l'écran `/clm/trousses/${trousse}`
22. Activer « Corriger la valeur — Programme »
23. Saisir « Nouvelle valeur — Programme » : `Autre programme`
24. Dans la section « Corriger la valeur — Programme », activer « Enregistrer »
25. Voir « « Programme » n'est pas modifiable en cours de traitement. »
26. Activer « Annuler »
27. Activer « Corriger la valeur — Montant »
28. Saisir « Nouvelle valeur — Montant » : `60000`
29. Dans la section « Corriger la valeur — Montant », activer « Enregistrer »
30. Voir « Valeur de « Montant » enregistrée. »
31. Activer « Historique » (tab)
32. Voir « Valeur modifiée (avant → après) », « « Montant » (montant) : « 50000 » → « 60000 » », « Nouveau cycle (modification en cours de parcours) »
33. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-05 — Gabarit Word d'IQ fusionné en Word et en PDF

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-30, IQ-A-18
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.gabarit.word`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « Documents »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-20 — Domaine d'affaires alimenté par un groupe ou un attribut d'annuaire, séparation logique

- **Source** : Add. 3 Q44
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-21 — Conservation par catégorie, gel légal, trois sorts finaux, bordereau ou certificat de destruction

- **Source** : Add. 3 Q46-Q48
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-22 — Mesure de l'utilisation par enveloppe envoyée, sans double comptage

- **Source** : Add. 3 Q39
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-05 — AF-37 (RST-42) : l'administration d'affaires crée un rapport de délais moyens, le modifie, puis l'exporte en CSV et en Excel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-43, IQ-A-22
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (13 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trousse d'un document validée par le validateur : lancée puis terminée, son délai de traitement est connu
2. Ouvrir l'écran `/clm/admin/rapports`
3. Voir le titre « Rapports personnalisables »
4. Activer « Nouveau rapport »
5. Saisir « Nom du rapport » : `Délais E2E ${unique}` ; « Période (jours glissants) » : `30`
6. Choisir « Type de rapport » : `Délais de traitement (moyenne)`
7. Cocher « Référence »
8. Cocher « Lancée le »
9. Cocher « Délai (h) »
10. Activer « Enregistrer le rapport »
11. Voir « Rapport « Délais E2E ${unique} » enregistré. »
12. Voir « ${reference} », « Moyenne ( »
13. Activer « Modifier — Délais E2E ${unique} »
14. Cocher « Terminée le »
15. Activer « Enregistrer le rapport »
16. Voir « Rapport « Délais E2E ${unique} » enregistré. »
17. Voir « Terminée le »
18. Télécharger par « Exporter en CSV — Délais E2E ${unique} » et obtenir un fichier non vide
19. Télécharger par « Exporter en Excel — Délais E2E ${unique} » et obtenir un fichier non vide
20. Voir « exporté (XLSX). »
21. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
22. Activer « Supprimer — Délais E2E ${unique} »
23. Voir « supprimé »
24. Ne pas voir « Modifier — Délais E2E ${unique} »

### E2E-AX-IQ-11 — AF-49 (RST-51) : l'administration d'affaires consulte les documents et les pages signés de la période, au total, par unité et par mois, puis exporte le relevé en CSV et en Excel

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-A-22
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Trousse de l'unité Financement des entreprises, un PDF d'une page, validée puis terminée : sa version signée est comptée
2. Ouvrir l'écran `/clm/admin/journal`
3. Voir « Documents et pages signés », « Documents signés », « Pages signées », « Documents non paginés »
4. Voir « Documents et pages signés par unité », « Documents et pages signés par mois »
5. Relever le texte affiché (`table.clm-table tbody tr:has-text("Financement des entreprises")`) sous le nom `ligneUnite`
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
7. Télécharger par « Exporter le relevé (CSV) » et obtenir un fichier non vide
8. Télécharger par « Exporter le relevé (Excel) » et obtenir un fichier non vide

### E2E-IQ-18 — Conservation par type, gel légal, exportation en lot, relevé d'utilisation

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-22
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.externe`
2. Ouvrir l'écran `/clm/admin/conservation`
3. Voir « Conservation et destruction »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-23 — Niveaux d'authentification configurables selon le type de document et le processus

- **Source** : Add. 4 Q1
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-20 — Vérification d'identité proportionnée : une trousse à risque élevé impose un niveau minimal au signataire externe

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-61, IQ-C-62, IQ-A-23
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (9 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Risque CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-RSQ`
3. Choisir « Niveau de risque » : `ELEVE`
4. Activer « Enregistrer le brouillon »
5. Activer « Continuer »
6. Activer « Continuer »
7. Voir « Niveau minimal imposé par le risque »
8. Saisir « Nom » : `Paul Externe` ; « Courriel » : `paul.${unique}@exemple.ca` ; « Téléphone » : `+15145550177`
9. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
10. Cocher « Externe »
11. Activer « Ajouter un signataire »
12. Voir « Paul Externe »
13. Voir « ajouté (vérification »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-IQ-14 — Vérification d'identité proportionnée au risque

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-A-23, IQ-C-61
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.identite.par-risque`
2. Ouvrir l'écran `/clm/admin/parametres`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-24 — Signature par un représentant autorisé ; preuve d'autorité ; qualité du signataire tracée

- **Source** : Add. 4 Q6
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-13 — Signature d'un représentant après attestation de son autorité

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-24
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.signature.representant`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Preuve » (tab)
4. Voir « ${trousseRef} »

## IQ-A-25 — Parcours conditionnels selon type, unité, rôle, montant

- **Source** : Add. 4 Q8, Q20
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-09 — Règles d'affaires : étape non requise selon les valeurs

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-19, IQ-A-25, IQ-D-07
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.parcours.regles`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Activer « Historique » (tab)
4. Voir « Étape non requise (règle d’affaires) »

## IQ-A-26 — Séparation de l'administration d'affaires et de l'administration technique

- **Source** : Add. 4 Q10
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-AX-IQ-03 — SEC-17 (RST-44) : l'administration d'affaires d'IQ accorde un accès de soutien à l'exploitant, voit sa consultation au journal de la trousse, puis le révoque ; sans accès, l'exploitant ne lit rien

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-D-16, IQ-A-26
- **Données** : `bdd/e2e/donnees/annexe-10-iq.json`
- **Nature** : parcours d'interface (6 gestes)
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Une trousse d'IQ, et une demande d'accès de soutien de l'exploitant, refusée tant qu'elle n'est pas accordée
2. Ouvrir l'écran `/clm/admin/soutien`
3. Voir le titre « Accès de soutien de l’exploitant »
4. Voir « Billet E2E ${unique} »
5. Activer « Accorder l’accès à superadmin@plateforme.ca »
6. Voir « Accès accordé à superadmin@plateforme.ca. »
7. Se connecter en tant que superadmin
8. Ouvrir l'écran `/superadmin`
9. Choisir « Accès de soutien aux données CLM d'une organisation » Organisation » : `Investissement Québec`
10. Saisir « Référence de la trousse » : `${reference}`
11. Activer « Consulter le diagnostic »
12. Voir « ${reference} »
13. Ne pas voir « Soutien ${unique} »
14. Se connecter en tant que administration d'affaires IQ
15. Préparer par l'API : Pendant l'accès accordé, l'exploitant consulte le diagnostic de la trousse, sans son contenu
16. Ouvrir l'écran `/clm/trousses/${trousse}`
17. Activer « Historique » (tab)
18. Voir « Consultation de soutien de l’exploitant », « Billet E2E ${unique} »
19. Ouvrir l'écran `/clm/admin/soutien`
20. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
21. Activer « Révoquer l’accès de superadmin@plateforme.ca »
22. Voir « Accès de superadmin@plateforme.ca révoqué. »
23. Préparer par l'API : Révoqué : l'exploitant ne lit plus rien

### E2E-CB-IQ-19 — Séparation des rôles : l'auditeur consulte une trousse sans pouvoir agir, l'administration technique n'accède pas aux trousses

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-67, IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (3 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/trousses/nouvelle`
2. Saisir « Titre » : `Rôles CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-ROL`
3. Activer « Enregistrer le brouillon »
4. Voir « CB${UNIQUE}-ROL »
5. Se connecter en tant que auditeur IQ
6. Ouvrir l'écran `/clm/trousses`
7. Activer « Ouvrir le détail de CB${UNIQUE}-ROL » (link)
8. Voir « Rôles CB ${unique} »
9. Ne pas voir « Lancer le parcours », « Annuler la trousse »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
11. Se connecter en tant que administration technique IQ
12. Ouvrir l'écran `/clm/trousses`
13. Ne pas voir « CB${UNIQUE}-ROL »

### E2E-ECR-IQ-05 — L'administration du CLM : messages modifiés et testés, connexion Salesforce testée, relayée, configurée puis rétablie, client d'intégration créé puis révoqué, paramètres modifiés puis rétablis, groupe créé et garni puis supprimé, départ d'un employé transféré, journal filtré et exporté

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-16, IQ-C-30, IQ-C-15, IQ-C-40, IQ-D-18, IQ-A-26
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (32 gestes)
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

### E2E-IQ-25 — Moindre privilège : administrations séparées, auditeur en lecture seule

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-D-15, IQ-D-16, IQ-D-17, IQ-A-26, IQ-C-67
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-07 — Le signataire demande son code au portail ; l'administration exporte le journal, gère la conservation (règle définie puis retirée, gel puis levée, lot exporté), l'annuaire (correspondance ajoutée puis retirée, jeton SCIM) et la connexion locale (réservée puis rouverte, un administrateur muni d'un second facteur désigné compte de secours puis retiré)

- **Profil** : administration principale IQ
- **Exigences démontrées** : IQ-C-51, IQ-C-65, IQ-C-66, IQ-A-21, IQ-A-11, IQ-A-19, IQ-A-08, IQ-A-27, IQ-C-60, IQ-C-62
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (23 gestes)
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

### E2E-IQ-27 — Annuaire SCIM 2.0 et correspondances de rôles

- **Profil** : administration technique IQ
- **Exigences démontrées** : IQ-A-08, IQ-A-19, IQ-A-20, IQ-A-27
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.annuaire.scim`
2. Ouvrir l'écran `/clm/admin/sso`
3. Voir « Authentification unique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-28 — Approbation adaptée au lot, traçabilité par dossier

- **Source** : Add. 4 Q36
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-ECR-IQ-06 — L'exploitation des trousses : étape relancée et réassignée, document analysé par l'IA et révisé, certificat téléchargé, trousse annulée, listes exportées, tâche confiée, démarrée et réassignée, lot créé et approuvé ; le validateur délègue, le signataire interne signe depuis sa tâche

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-31, IQ-C-50, IQ-C-30, IQ-C-59, IQ-A-28, IQ-C-43, IQ-C-21
- **Données** : `bdd/e2e/donnees/ecrans-iq.json`
- **Nature** : parcours d'interface (33 gestes)
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

### E2E-IQ-22 — Signature en lot et approbation globale

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-03, IQ-A-28
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.lots`
2. Ouvrir l'écran `/clm/lots`
3. Voir « Signature en lot »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-29 — Export dans des formats pérennes, PDF/PDF-A lorsque disponibles, export massif

- **Source** : Add. 4 Q14
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-17 — Dossier de preuve consulté, vérifié, exporté ; journal consulté et exporté

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-49, IQ-C-51, IQ-C-64, IQ-C-65, IQ-D-10, IQ-D-11, IQ-D-19, IQ-S-07, IQ-S-10, IQ-A-29
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
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
- **Démontrée à l'écran** : non — seulement préparée par l'API

### E2E-IQ-05 — Gabarit Word d'IQ fusionné en Word et en PDF

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-A-30, IQ-A-18
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.gabarit.word`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « Documents »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## IQ-A-17 — FundingRequest lu par Id ou Name, correspondance configurable par modèle

- **Source** : Add. 2 Q7
- **Conformité déclarée** : ✅
- **Démontrée à l'écran** : oui

### E2E-CB-IQ-17 — Génération depuis Salesforce Grantmaking : modèle aux champs sourcés et à l'étape de signature par rôle, dossier importé, contrat généré, signataire associé automatiquement, trousse lancée et traçable jusqu'au dossier source ; la signataire externe signe sur téléphone depuis son invitation ; les statuts sont relayés vers Salesforce par l'administration

- **Profil** : administration d'affaires IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-C-56, IQ-D-01, IQ-D-02, IQ-A-17, IQ-S-01, IQ-C-52, IQ-C-53, IQ-C-57, IQ-S-04, IQ-S-08, IQ-C-08, IQ-C-13, IQ-C-15
- **Données** : `bdd/e2e/donnees/cahier-iq.json`
- **Nature** : parcours d'interface (53 gestes)
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clm/modeles`
2. Activer « Créer un modèle »
3. Saisir « Nom » : `Convention SF CB ${unique}` ; « Corps du contrat » : `Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $. {{clauses}}`
4. Activer « Ajouter un champ »
5. Saisir « Nom technique » : `demandeur` ; « Libellé » : `Demandeur`
6. Cocher « Obligatoire »
7. Choisir « Source Salesforce » : `Demandeur`
8. Activer « Ajouter un champ »
9. Saisir « Nom technique » : `montant` ; « Libellé » : `Montant`
10. Choisir « Source Salesforce » : `Montant`
11. Activer « Ajouter un champ »
12. Saisir « Nom technique » : `signature1` ; « Libellé » : `Signature du bénéficiaire`
13. Choisir « Type » : `SIGNATURE`
14. Choisir « Rôle autorisé à compléter » : `SIGNATAIRE_1`
15. Activer « Positionner sur le document »
16. Activer « Ajouter une étape »
17. Saisir « Types : validation, approbation, signature » Clé » : `s1` ; « Types : validation, approbation, signature » Libellé » : `Signature du bénéficiaire`
18. Choisir « Types : validation, approbation, signature » Type » : `SIGNATURE`
19. Activer « Ajouter un responsable »
20. Choisir « Assignation par utilisateur, groupe ou règle » Type du responsable 1 » : `PARTICIPANT`
21. Choisir « Assignation par utilisateur, groupe ou règle » Responsable 1 » : `SIGNATAIRE_1`
22. Activer « Vérifier »
23. Voir « Modèle valide. »
24. Activer « Enregistrer le brouillon »
25. Voir « Brouillon enregistré. »
26. Activer « Soumettre à publication »
27. Voir « soumis à publication »
28. Se connecter en tant que administration principale IQ
29. Ouvrir l'écran `/clm/modeles`
30. Dans la ligne « Convention SF CB ${unique} », activer « Publier »
31. Se connecter en tant que initiateur IQ
32. Ouvrir l'écran `/clm/trousses/nouvelle`
33. Cocher « Salesforce »
34. Saisir « Identifiant de demande ou numéro de dossier Salesforce » : `DEM-2026-0002`
35. Activer « Rechercher #2 »
36. Voir « Techno Mauricie »
37. Saisir « Titre » : `Convention SF CB ${unique}` ; « Référence externe » : `CB${UNIQUE}-SF`
38. Choisir « Modèle de contrat » : `Convention SF CB ${unique}`
39. Activer « Enregistrer le brouillon »
40. Activer « Importer / actualiser »
41. Voir « Techno Mauricie », « 1200000 »
42. Activer « Continuer »
43. Activer « Générer depuis le modèle »
44. Voir « CB${UNIQUE}-SF.pdf »
45. Activer « Continuer »
46. Saisir « Nom » : `Julie Mauricie` ; « Courriel » : `julie.${unique}@technomauricie.example` ; « Téléphone » : `+18195550142`
47. Choisir « Rôle dans le contrat » : `SIGNATAIRE_1`
48. Choisir « Vérification d’identité » : `QUESTION`
49. Saisir « Question de vérification » : `Ville du siège ?` ; « Réponse attendue » : `Trois-Rivières`
50. Cocher « Externe »
51. Activer « Ajouter un signataire »
52. Voir « Julie Mauricie »
53. Choisir « Signataire de la zone « Signature du bénéficiaire » » : `(premier)`
54. Activer « Attribuer la zone — Signature du bénéficiaire »
55. Voir « Zone « Signature du bénéficiaire » attribuée. »
56. Activer « Continuer »
57. Activer « Continuer »
58. Activer « Lancer le parcours »
59. Arriver sur une adresse de la forme `/clm/trousses/\d+`
60. Activer « Historique » (tab)
61. Voir « Préremplissage Salesforce », « DEM-2026-0002 »
62. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
63. Préparer par l'API : Invitation reçue par courriel par la signataire externe
64. Afficher l'écran à 390 px de large (téléphone)
65. Ouvrir l'écran `/portail/${jeton}`
66. Saisir « Ville du siège ? » : `Trois-Rivières`
67. Activer « Vérifier mon identité »
68. Activer « Lire / télécharger »
69. Cocher « J’ai lu les documents et je consens à les signer électroniquement ; ma signature a la même valeur qu’une signature manuscrite. »
70. Saisir « Votre nom complet » : `Julie Mauricie`
71. Activer « Signer »
72. Voir « Merci, votre signature est enregistrée »
73. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
74. Afficher l'écran à 1280 px de large
75. Ouvrir l'écran `/clm/suivi`
76. Voir « CB${UNIQUE}-SF »
77. Se connecter en tant que administration principale IQ
78. Ouvrir l'écran `/clm/admin/integrations`
79. Activer « Relayer maintenant »
80. Voir « Transmis »
81. Voir « Journal des retours de statut », « LANCEE ·  », « SIGNATURE_TERMINEE ·  », « a0B5e00000GRT002 »

### E2E-IQ-33 — Générer le contrat d'un modèle publié, valeurs préremplies depuis Salesforce et corrigeables

- **Profil** : initiateur IQ
- **Exigences démontrées** : IQ-C-09, IQ-C-10, IQ-C-11, IQ-C-12, IQ-C-14, IQ-C-29, IQ-D-01, IQ-D-02, IQ-A-06, IQ-A-17, IQ-A-18, IQ-C-56
- **Données** : `bdd/e2e/donnees/iq.json`
- **Nature** : préparé par l'API, écran constaté
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `iq.generation.salesforce`
2. Ouvrir l'écran `/clm/trousses/${trousse}`
3. Voir « ${trousseRef} »
4. Activer « Historique » (tab)
5. Voir « Préremplissage Salesforce »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
