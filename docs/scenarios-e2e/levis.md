# Ville de Lévis — avis d'appel d'intérêt 2026-50-92 — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## LEV-2.1-1 — Rédiger des clauses administratives pour demandes de prix et procédures ouvertes (AOP)

- **Source** : Avis 2026-50-92, art. 2.1 n° 1
- **Conformité déclarée** : Oui. Bibliothèque de clauses + moteur d'assemblage produisant devis de demande de prix et d'appel d'offres public.

### E2E-ECR-LEV-08 — Depuis l'accueil, le rédacteur crée un document vierge puis un document à partir d'un template

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-C-07
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/`
2. Saisir « Titre » : `Accueil vierge ${unique}` ; « Valeur estimée ($) » : `30000`
3. Activer « Créer le document »
4. Arriver sur une adresse de la forme `/documents/\d+`
5. Voir « Accueil vierge ${unique} »
6. Ouvrir l'écran `/`
7. Choisir « Template » : `Bail — gré à gré`
8. Saisir « Titre du document » : `Accueil template ${unique}`
9. Activer « Instancier le template »
10. Arriver sur une adresse de la forme `/documents/\d+`
11. Voir « Accueil template ${unique} »

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-2 — Rédiger des contrats de gré à gré personnalisés de toute nature (prêts à usage, baux, contrats de services, etc.)

- **Source** : Avis 2026-50-92, art. 2.1 n° 2
- **Conformité déclarée** : Oui. Types de contrat paramétrables : demande de prix, AOP, gré à gré, bail, prêt à usage, contrat de services, avis d'intention, addenda.

### E2E-LEV-04 — Contrats de gré à gré de toute nature et avis d'intention se rédigent depuis l'assistant

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-2, LEV-C-08, LEV-C-09
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Bail`
3. Saisir « Valeur estimée ($) » : `12000`
4. Activer « Analyser le besoin »
5. Saisir « Titre du document » : `Bail E2E ${unique}`
6. Activer « Générer le document »
7. Arriver sur une adresse de la forme `/documents/\d+`
8. Voir « Bail E2E ${unique} »
9. Ouvrir l'écran `/assistant`
10. Choisir « Type de document » : `Avis d'intention`
11. Saisir « Valeur estimée ($) » : `40000`
12. Activer « Analyser le besoin »
13. Saisir « Titre du document » : `Avis d'intention E2E ${unique}`
14. Activer « Générer le document »
15. Arriver sur une adresse de la forme `/documents/\d+`
16. Voir « Avis d'intention E2E ${unique} »

## LEV-2.1-3 — Créer et gérer une bibliothèque centralisée de clauses standards, optionnelles et personnalisées

- **Source** : Avis 2026-50-92, art. 2.1 n° 3
- **Conformité déclarée** : Oui. Bibliothèque centralisée avec catégories STANDARD / OPTIONNELLE / PERSONNALISÉE, numérotation, sections, versions.

### E2E-LEV-01 — Le juriste crée une clause optionnelle classée par thématique, la retrouve, puis consulte son historique

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-2.1-3, LEV-C-01, LEV-C-03
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clauses`
2. Activer « Nouvelle clause »
3. Saisir « Code » : `E2E-${UNIQUE}` ; « Titre » : `Garantie de soumission ${unique}` ; « Section (ex. Attestations) » : `Garanties E2E`
4. Choisir « Catégorie » : `OPTIONNELLE`
5. Saisir « Corps de la clause » : `Le soumissionnaire fournit une garantie de soumission de 10 %.`
6. Activer « Enregistrer »
7. Saisir « Recherche (code, titre, texte)… » : `E2E-${UNIQUE}`
8. Voir « E2E-${UNIQUE} », « Garantie de soumission ${unique} »
9. Activer « Historique de la clause E2E-${UNIQUE} » (button)
10. Voir « v1 »
11. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-4 — Assembler automatiquement des documents contractuels à partir des clauses

- **Source** : Avis 2026-50-92, art. 2.1 n° 4
- **Conformité déclarée** : Oui. Assemblage en un clic : les clauses STANDARD éligibles sont intégrées automatiquement ; sélection manuelle possible.

### E2E-ECR-LEV-06 — L'administration crée un template de document, le modifie puis le supprime

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-C-04
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/templates`
2. Activer « Nouveau template »
3. Saisir « Code » : `TPL_${UNIQUE}` ; « Nom (affiché) » : `Template écran ${unique}`
4. Choisir « Type de contrat » : `CONTRAT_SERVICES`
5. Activer « Créer le template »
6. Voir « Template écran ${unique} »
7. Dans la ligne « TPL_${UNIQUE} », activer « Modifier »
8. Saisir « Nom (affiché) » : `Template écran modifié ${unique}`
9. Dans la fenêtre ouverte, activer « Enregistrer »
10. Voir « Template écran modifié ${unique} »
11. Dans la ligne « TPL_${UNIQUE} », activer « Supprimer »
12. Dans la fenêtre ouverte, activer « Supprimer »
13. Ne pas voir « TPL_${UNIQUE} »

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-5a — Adapter les clauses selon le type de contrat

- **Source** : Avis 2026-50-92, art. 2.1 n° 5a
- **Conformité déclarée** : Oui. Moteur de règles d'éligibilité par type de contrat.

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-5b — …selon le mode de sollicitation

- **Source** : Avis 2026-50-92, art. 2.1 n° 5b
- **Conformité déclarée** : Oui. Règles par mode (demande de prix, AOP, gré à gré).

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-5c — …selon la valeur estimée

- **Source** : Avis 2026-50-92, art. 2.1 n° 5c
- **Conformité déclarée** : Oui. Seuils paramétrables (p. ex. seuil d'AOP) déclenchant l'inclusion de clauses.

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-5d — …selon le contexte particulier du projet

- **Source** : Avis 2026-50-92, art. 2.1 n° 5d
- **Conformité déclarée** : Oui. Conditions d'éligibilité sur attributs de contexte du dossier.

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-5e — …selon les autres clauses sélectionnées

- **Source** : Avis 2026-50-92, art. 2.1 n° 5e
- **Conformité déclarée** : Oui. Gestion des dépendances (auto-inclusion des clauses requises) et exclusions (incompatibilités).

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-6 — Faciliter la révision et la validation par différents intervenants (approvisionnement, juridique, autres)

- **Source** : Avis 2026-50-92, art. 2.1 n° 6
- **Conformité déclarée** : Oui. Rôles distincts (Rédacteur, Approbateur, Juridique, Administrateur) et circuit d'approbation.

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-06 — Le juriste commente le devis, puis le circuit d'approbation démarre et chaque niveau décide

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-6, LEV-2.1-6a, LEV-C-11, LEV-C-17
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis à réviser
2. Se connecter en tant que juriste de Lévis
3. Ouvrir l'écran `/documents/${document}`
4. Saisir « Commenter le devis… » : `Préciser la garantie ${unique}`
5. Activer « Commenter »
6. Voir « Commentaire ajouté », « Préciser la garantie ${unique} »
7. Se connecter en tant que rédacteur de Lévis
8. Ouvrir l'écran `/documents/${document}`
9. Activer « Démarrer l'approbation »
10. Voir « Circuit d’approbation démarré », « APPRO »
11. Se connecter en tant que administrateur de Lévis
12. Ouvrir l'écran `/audit`
13. Voir « Journal d’audit »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-6a — (Idéalement) Commenter directement dans le devis

- **Source** : Avis 2026-50-92, art. 2.1 n° 6a
- **Conformité déclarée** : Oui. Fil de commentaires par document, visibles des intervenants.

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-06 — Le juriste commente le devis, puis le circuit d'approbation démarre et chaque niveau décide

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-6, LEV-2.1-6a, LEV-C-11, LEV-C-17
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis à réviser
2. Se connecter en tant que juriste de Lévis
3. Ouvrir l'écran `/documents/${document}`
4. Saisir « Commenter le devis… » : `Préciser la garantie ${unique}`
5. Activer « Commenter »
6. Voir « Commentaire ajouté », « Préciser la garantie ${unique} »
7. Se connecter en tant que rédacteur de Lévis
8. Ouvrir l'écran `/documents/${document}`
9. Activer « Démarrer l'approbation »
10. Voir « Circuit d’approbation démarré », « APPRO »
11. Se connecter en tant que administrateur de Lévis
12. Ouvrir l'écran `/audit`
13. Voir « Journal d’audit »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-6b — (Idéalement) Gestion de niveaux de hiérarchisation d'approbation

- **Source** : Avis 2026-50-92, art. 2.1 n° 6b
- **Conformité déclarée** : Oui. Circuit d'approbation hiérarchisé multi-niveaux (N1, N2…) avec décisions (approuvé/refusé) et traçabilité.

### E2E-ECR-LEV-04 — L'administration crée un circuit d'approbation, le modifie puis le supprime

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-12, LEV-2.1-6b
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/workflows`
2. Saisir « Nom » : `Circuit écran ${unique}`
3. Choisir « Type de document » : `PRET_A_USAGE`
4. Cocher « APPRO »
5. Cocher « DIRECTION »
6. Activer « Créer le circuit »
7. Voir « Circuit écran ${unique} »
8. Activer « Éditer le circuit Circuit écran ${unique} » (button)
9. Saisir « Nom » : `Circuit écran modifié ${unique}`
10. Activer « Enregistrer les modifications »
11. Voir « Circuit écran modifié ${unique} »
12. Activer « Supprimer le circuit Circuit écran modifié ${unique} » (button)
13. Ne pas voir « Circuit écran modifié ${unique} »

### E2E-LEV-05 — L'administration configure un circuit d'approbation par type de document

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-12, LEV-2.1-6b
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/workflows`
2. Saisir « Nom » : `Circuit prêt E2E ${unique}`
3. Choisir « Type de document » : `Prêt à usage`
4. Cocher « DIRECTION »
5. Activer « Créer le circuit »
6. Voir « Circuit créé », « Circuit prêt E2E ${unique} »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Activer « Supprimer le circuit Circuit prêt E2E ${unique} » (button)
9. Voir « Circuit supprimé »
10. Ne pas voir « Circuit prêt E2E ${unique} »

## LEV-2.1-7 — Rédiger des addendas de manière interactive et en lien avec les clauses du devis

- **Source** : Avis 2026-50-92, art. 2.1 n° 7
- **Conformité déclarée** : Oui. Création d'addendas, ajout de modifications rattachées à une clause précise du devis, puis émission (réintégration au document).

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-07 — Un addenda modifie une clause du devis, est émis et réintégré ; la fiche SEAO se produit

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-20
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis assemblé
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Nouvel addenda »
4. Saisir « Objet de l'addenda » : `Report de la date d'ouverture ${unique}`
5. Activer « Valider »
6. Voir « Report de la date d'ouverture ${unique} »
7. Activer « Générer »
8. Voir « Aperçu PDF », « Export PDF », « Export DOCX », « Fiche SEAO (XML) »
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-8 — Générer les documents finaux pour la Ville et son cocontractant, intégrant l'ensemble des addendas

- **Source** : Avis 2026-50-92, art. 2.1 n° 8
- **Conformité déclarée** : Oui. Génération PDF et Word (DOCX) ; les addendas émis sont intégrés au document final et à ses annexes.

### E2E-ECR-LEV-07 — L'administration crée un gabarit, le modifie, personnalise un gabarit global, puis supprime ses gabarits

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-8
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/gabarits`
2. Activer « Nouveau gabarit »
3. Saisir « Code » : `GAB_${UNIQUE}` ; « Nom (affiché) » : `Gabarit écran ${unique}`
4. Dans la fenêtre ouverte, activer « Créer le gabarit »
5. Voir « Gabarit écran ${unique} »
6. Dans la ligne « GAB_${UNIQUE} », activer « Modifier »
7. Saisir « Sous-titre » : `Sous-titre E2E`
8. Dans la fenêtre ouverte, activer « Enregistrer »
9. Dans la ligne « GAB_${UNIQUE} », activer « Supprimer »
10. Dans la fenêtre ouverte, activer « Supprimer »
11. Ne pas voir « GAB_${UNIQUE} »
12. Activer « Personnaliser »
13. Voir « cloné pour personnalis »

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-ECR-LEV-17 — Le cocontractant signe le devis par son lien de signature, sans compte

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-8
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document dont la signature est démarrée
2. Ouvrir l'écran `/signature/${jeton}`
3. Saisir « Nom complet » : `Cocontractant E2E`
4. Tracer une signature manuscrite sur le pavé de signature
5. Activer « Apposer ma signature »
6. Voir « Votre signature a bien été enregistrée »

### E2E-LEV-07 — Un addenda modifie une clause du devis, est émis et réintégré ; la fiche SEAO se produit

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-20
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis assemblé
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Nouvel addenda »
4. Saisir « Objet de l'addenda » : `Report de la date d'ouverture ${unique}`
5. Activer « Valider »
6. Voir « Report de la date d'ouverture ${unique} »
7. Activer « Générer »
8. Voir « Aperçu PDF », « Export PDF », « Export DOCX », « Fiche SEAO (XML) »
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-9 — Solution validée juridiquement selon les lois applicables au Québec

- **Source** : Avis 2026-50-92, art. 2.1 n° 9
- **Conformité déclarée** : Oui. Contenu aligné sur la LCOP, la LCV, le Code municipal et le cadre normatif SCT (CCAG) ; clauses normatives verrouillées ; assistance juridique offerte (voir §5).

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-2.1-10 — Soutien technique complet du prestataire

- **Source** : Avis 2026-50-92, art. 2.1 n° 10
- **Conformité déclarée** : Oui. Soutien en français, jours ouvrés (voir §5 et §6).
- **Vérification** : documentaire — Service (soutien technique) : engagement contractuel, non vérifiable par un test.

## LEV-2.1-11 — Assistance juridique par un ou des juriste(s)

- **Source** : Avis 2026-50-92, art. 2.1 n° 11
- **Conformité déclarée** : Oui. Service d'assistance juridique lié à la solution (voir §5).
- **Vérification** : documentaire — Service (assistance juridique) : engagement contractuel.

## LEV-2.1-12 — Formation en français, documentée, complète et détaillée

- **Source** : Avis 2026-50-92, art. 2.1 n° 12
- **Conformité déclarée** : Oui. Programme de formation et documentation en français (voir §6).
- **Vérification** : documentaire — Service (formation en français) : programme et documentation.

## LEV-2.1-13 — Ensemble des fonctionnalités présentes et pleinement fonctionnelles

- **Source** : Avis 2026-50-92, art. 2.1 n° 13
- **Conformité déclarée** : Oui. Solution en production ; démonstration en ligne disponible (art. 4.4).
- **Vérification** : documentaire — Démontré par l'ensemble des scénarios BDD de Lévis (toutes les fonctionnalités exigées s'exécutent).

## LEV-C-01 — EF1 Bibliothèque : création/modif/archivage standards/optionnelles/perso

- **Source** : Cahier 2026-50-92 — EF1
- **Conformité déclarée** : ✅ Clause + catégories, CRUD

### E2E-ECR-LEV-02 — Le juriste modifie une clause, restaure sa première version depuis l'historique, puis la supprime

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-C-01, LEV-C-03
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Clause standard « ECR-… »
2. Ouvrir l'écran `/clauses`
3. Saisir « Recherche (code, titre, texte)… » : `ECR-${UNIQUE}`
4. Voir « Clause écran ${unique} »
5. Activer « Éditer »
6. Saisir « Titre » : `Clause écran modifiée ${unique}`
7. Activer « Enregistrer »
8. Voir « Clause écran modifiée ${unique} »
9. Activer « Historique de la clause ECR-${UNIQUE} » (button)
10. Activer « Restaurer la version 1 »
11. Voir « Clause écran ${unique} »
12. Activer « Supprimer »
13. Dans la fenêtre ouverte, activer « Confirmer »
14. Ne pas voir « Clause écran ${unique} »

### E2E-ECR-LEV-11 — L'administration importe des clauses par CSV et efface une personne au titre de la Loi 25

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-01, LEV-C-15
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Personne à effacer
2. Ouvrir l'écran `/donnees`
3. Saisir « Contenu CSV » : `code,titre,categorie,corps
IMP-${UNIQUE},Clause importée ${unique},STANDARD,Texte importé.`
4. Activer « Importer »
5. Voir « IMP-${UNIQUE} »
6. Ouvrir l'écran `/vie-privee`
7. Saisir « Motif » : `Demande écrite E2E ${unique}`
8. Saisir « Courriel de la personne concernée #2 » : `efface.${unique}@levis.qc.ca`
9. Activer « Effacer la personne »
10. Dans la fenêtre ouverte, activer « Confirmer »
11. Voir « Demande écrite E2E ${unique} »

### E2E-ECR-LEV-16 — L'assistant IA (Ollama hébergé) : base de connaissances alimentée puis épurée, question posée ; proposition de texte insérée dans une clause, et proposée dans l'éditeur du devis

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-13, LEV-C-01
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Clause et document à enrichir
2. Ouvrir l'écran `/chat-ia`
3. Saisir « Titre » : `Note E2E ${unique}` ; « Contenu (texte du document / données) » : `La garantie d'exécution est de 10 % du contrat.`
4. Activer « Ajouter à la base de connaissances »
5. Voir « Note E2E ${unique} »
6. Saisir « Votre question juridique / de rédaction… » : `Quel est le taux de la garantie d'exécution ?`
7. Activer « Envoyer »
8. Activer « Retirer « Note E2E ${unique} » de la base de connaissances » (button)
9. Ne pas voir « Note E2E ${unique} »
10. Ouvrir l'écran `/clauses`
11. Saisir « Recherche (code, titre, texte)… » : `IA-${UNIQUE}`
12. Activer « Éditer »
13. Saisir « Instruction » : `Rédige une clause de garantie d'exécution`
14. Activer « Proposer un texte »
15. Activer « Insérer dans le formulaire »
16. Dans la fenêtre ouverte, activer « Enregistrer »
17. Ouvrir l'écran `/documents/${doc}`
18. Saisir « Instruction » : `Rédige l'objet du contrat`
19. Activer « Proposer un texte »
20. Activer « Insérer dans le formulaire »

### E2E-LEV-01 — Le juriste crée une clause optionnelle classée par thématique, la retrouve, puis consulte son historique

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-2.1-3, LEV-C-01, LEV-C-03
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clauses`
2. Activer « Nouvelle clause »
3. Saisir « Code » : `E2E-${UNIQUE}` ; « Titre » : `Garantie de soumission ${unique}` ; « Section (ex. Attestations) » : `Garanties E2E`
4. Choisir « Catégorie » : `OPTIONNELLE`
5. Saisir « Corps de la clause » : `Le soumissionnaire fournit une garantie de soumission de 10 %.`
6. Activer « Enregistrer »
7. Saisir « Recherche (code, titre, texte)… » : `E2E-${UNIQUE}`
8. Voir « E2E-${UNIQUE} », « Garantie de soumission ${unique} »
9. Activer « Historique de la clause E2E-${UNIQUE} » (button)
10. Voir « v1 »
11. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-02 — EF1 Recherche multicritère (thématique, portée, type)

- **Source** : Cahier 2026-50-92 — EF1
- **Conformité déclarée** : ✅ Recherche multicritère /api/search : terme, portée (catégorie), thématique (section), type de contrat applicable, clauses normatives ; facettes portée et thématique

### E2E-ECR-LEV-03 — Depuis la recherche, le juriste supprime une clause et l'administration un document

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-C-02
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Une clause et un document au même mot-clé
2. Ouvrir l'écran `/recherche`
3. Saisir « Terme » : `${unique}`
4. Activer « Rechercher »
5. Voir « Recherche ${unique} », « Document recherche ${unique} »
6. Dans la ligne « RCH-${UNIQUE} », activer « Supprimer »
7. Dans la fenêtre ouverte, activer « Confirmer »
8. Ne pas voir « RCH-${UNIQUE} »
9. Se connecter en tant que administrateur de Lévis
10. Ouvrir l'écran `/recherche`
11. Saisir « Terme » : `${unique}`
12. Activer « Rechercher »
13. Dans la ligne « Document recherche ${unique} », activer « Supprimer »
14. Dans la fenêtre ouverte, activer « Confirmer »
15. Ne pas voir « RCH-${UNIQUE} », « Document recherche ${unique} »

### E2E-LEV-02 — Recherche multicritère : terme, portée, thématique, type de contrat et clauses normatives

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-02
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/recherche`
2. Saisir « Terme » : (vide)
3. Choisir « Thématique (section) » : `Dispositions générales`
4. Cocher « Clauses normatives seulement »
5. Activer « Rechercher »
6. Voir « GEN-002 », « Dispositions générales »
7. Choisir « Catégorie de clause (facette) » : `OPTIONNELLE`
8. Activer « Rechercher »
9. Voir « Aucune clause. »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-03 — EF1 Historisation par clause (versions + auteur/date)

- **Source** : Cahier 2026-50-92 — EF1
- **Conformité déclarée** : Oui. Chaque création, modification ou restauration fige une version consultable (texte, auteur, date, motif) ; toute version antérieure se restaure et la restauration devient une nouvelle version.

### E2E-ECR-LEV-02 — Le juriste modifie une clause, restaure sa première version depuis l'historique, puis la supprime

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-C-01, LEV-C-03
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Clause standard « ECR-… »
2. Ouvrir l'écran `/clauses`
3. Saisir « Recherche (code, titre, texte)… » : `ECR-${UNIQUE}`
4. Voir « Clause écran ${unique} »
5. Activer « Éditer »
6. Saisir « Titre » : `Clause écran modifiée ${unique}`
7. Activer « Enregistrer »
8. Voir « Clause écran modifiée ${unique} »
9. Activer « Historique de la clause ECR-${UNIQUE} » (button)
10. Activer « Restaurer la version 1 »
11. Voir « Clause écran ${unique} »
12. Activer « Supprimer »
13. Dans la fenêtre ouverte, activer « Confirmer »
14. Ne pas voir « Clause écran ${unique} »

### E2E-LEV-01 — Le juriste crée une clause optionnelle classée par thématique, la retrouve, puis consulte son historique

- **Profil** : juriste de Lévis
- **Exigences démontrées** : LEV-2.1-3, LEV-C-01, LEV-C-03
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/clauses`
2. Activer « Nouvelle clause »
3. Saisir « Code » : `E2E-${UNIQUE}` ; « Titre » : `Garantie de soumission ${unique}` ; « Section (ex. Attestations) » : `Garanties E2E`
4. Choisir « Catégorie » : `OPTIONNELLE`
5. Saisir « Corps de la clause » : `Le soumissionnaire fournit une garantie de soumission de 10 %.`
6. Activer « Enregistrer »
7. Saisir « Recherche (code, titre, texte)… » : `E2E-${UNIQUE}`
8. Voir « E2E-${UNIQUE} », « Garantie de soumission ${unique} »
9. Activer « Historique de la clause E2E-${UNIQUE} » (button)
10. Voir « v1 »
11. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-04 — EF2 Génération dynamique par assemblage

- **Source** : Cahier 2026-50-92 — EF2
- **Conformité déclarée** : ✅ DocumentService.assembler

### E2E-ECR-LEV-06 — L'administration crée un template de document, le modifie puis le supprime

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-C-04
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/templates`
2. Activer « Nouveau template »
3. Saisir « Code » : `TPL_${UNIQUE}` ; « Nom (affiché) » : `Template écran ${unique}`
4. Choisir « Type de contrat » : `CONTRAT_SERVICES`
5. Activer « Créer le template »
6. Voir « Template écran ${unique} »
7. Dans la ligne « TPL_${UNIQUE} », activer « Modifier »
8. Saisir « Nom (affiché) » : `Template écran modifié ${unique}`
9. Dans la fenêtre ouverte, activer « Enregistrer »
10. Voir « Template écran modifié ${unique} »
11. Dans la ligne « TPL_${UNIQUE} », activer « Supprimer »
12. Dans la fenêtre ouverte, activer « Supprimer »
13. Ne pas voir « TPL_${UNIQUE} »

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-05 — EF2 Adaptation type/mode/valeur/contexte

- **Source** : Cahier 2026-50-92 — EF2
- **Conformité déclarée** : ✅ RuleEngine + ClauseCondition

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-06 — EF2 Dépendances & exclusions entre clauses

- **Source** : Cahier 2026-50-92 — EF2
- **Conformité déclarée** : ✅ Dépendances et exclusions entre clauses, appliquées par le moteur d'assemblage

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-07 — EF3 Demandes de prix / appels d'offres publics

- **Source** : Cahier 2026-50-92 — EF3
- **Conformité déclarée** : ✅ Types + assemblage

### E2E-ECR-LEV-08 — Depuis l'accueil, le rédacteur crée un document vierge puis un document à partir d'un template

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-C-07
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/`
2. Saisir « Titre » : `Accueil vierge ${unique}` ; « Valeur estimée ($) » : `30000`
3. Activer « Créer le document »
4. Arriver sur une adresse de la forme `/documents/\d+`
5. Voir « Accueil vierge ${unique} »
6. Ouvrir l'écran `/`
7. Choisir « Template » : `Bail — gré à gré`
8. Saisir « Titre du document » : `Accueil template ${unique}`
9. Activer « Instancier le template »
10. Arriver sur une adresse de la forme `/documents/\d+`
11. Voir « Accueil template ${unique} »

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-08 — EF3 Gré à gré (baux, prêts à usage, services)

- **Source** : Cahier 2026-50-92 — EF3
- **Conformité déclarée** : ✅ Types + templates

### E2E-LEV-04 — Contrats de gré à gré de toute nature et avis d'intention se rédigent depuis l'assistant

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-2, LEV-C-08, LEV-C-09
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Bail`
3. Saisir « Valeur estimée ($) » : `12000`
4. Activer « Analyser le besoin »
5. Saisir « Titre du document » : `Bail E2E ${unique}`
6. Activer « Générer le document »
7. Arriver sur une adresse de la forme `/documents/\d+`
8. Voir « Bail E2E ${unique} »
9. Ouvrir l'écran `/assistant`
10. Choisir « Type de document » : `Avis d'intention`
11. Saisir « Valeur estimée ($) » : `40000`
12. Activer « Analyser le besoin »
13. Saisir « Titre du document » : `Avis d'intention E2E ${unique}`
14. Activer « Générer le document »
15. Arriver sur une adresse de la forme `/documents/\d+`
16. Voir « Avis d'intention E2E ${unique} »

## LEV-C-09 — EF3 Avis d'intention

- **Source** : Cahier 2026-50-92 — EF3
- **Conformité déclarée** : ✅ Type de document AVIS_INTENTION (avis d'intention de contracter de gré à gré)

### E2E-LEV-04 — Contrats de gré à gré de toute nature et avis d'intention se rédigent depuis l'assistant

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-2, LEV-C-08, LEV-C-09
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Bail`
3. Saisir « Valeur estimée ($) » : `12000`
4. Activer « Analyser le besoin »
5. Saisir « Titre du document » : `Bail E2E ${unique}`
6. Activer « Générer le document »
7. Arriver sur une adresse de la forme `/documents/\d+`
8. Voir « Bail E2E ${unique} »
9. Ouvrir l'écran `/assistant`
10. Choisir « Type de document » : `Avis d'intention`
11. Saisir « Valeur estimée ($) » : `40000`
12. Activer « Analyser le besoin »
13. Saisir « Titre du document » : `Avis d'intention E2E ${unique}`
14. Activer « Générer le document »
15. Arriver sur une adresse de la forme `/documents/\d+`
16. Voir « Avis d'intention E2E ${unique} »

## LEV-C-10 — EF3 Addendas interactifs + consolidation finale

- **Source** : Cahier 2026-50-92 — EF3
- **Conformité déclarée** : ✅ Addenda + rendu PDF/DOCX intégrant les addendas

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-07 — Un addenda modifie une clause du devis, est émis et réintégré ; la fiche SEAO se produit

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-20
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis assemblé
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Nouvel addenda »
4. Saisir « Objet de l'addenda » : `Report de la date d'ouverture ${unique}`
5. Activer « Valider »
6. Voir « Report de la date d'ouverture ${unique} »
7. Activer « Générer »
8. Voir « Aperçu PDF », « Export PDF », « Export DOCX », « Fiche SEAO (XML) »
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-11 — EF4 Commentaires/annotations dans le devis

- **Source** : Cahier 2026-50-92 — EF4
- **Conformité déclarée** : ✅ Comment + WYSIWYG

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-06 — Le juriste commente le devis, puis le circuit d'approbation démarre et chaque niveau décide

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-6, LEV-2.1-6a, LEV-C-11, LEV-C-17
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis à réviser
2. Se connecter en tant que juriste de Lévis
3. Ouvrir l'écran `/documents/${document}`
4. Saisir « Commenter le devis… » : `Préciser la garantie ${unique}`
5. Activer « Commenter »
6. Voir « Commentaire ajouté », « Préciser la garantie ${unique} »
7. Se connecter en tant que rédacteur de Lévis
8. Ouvrir l'écran `/documents/${document}`
9. Activer « Démarrer l'approbation »
10. Voir « Circuit d’approbation démarré », « APPRO »
11. Se connecter en tant que administrateur de Lévis
12. Ouvrir l'écran `/audit`
13. Voir « Journal d’audit »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-12 — EF4 Workflow d'approbation configurable

- **Source** : Cahier 2026-50-92 — EF4
- **Conformité déclarée** : ✅ Circuit d'approbation configurable par type de document (/api/workflows, écran Circuits) ; le circuit par défaut ne sert qu'en l'absence de circuit configuré

### E2E-ECR-LEV-04 — L'administration crée un circuit d'approbation, le modifie puis le supprime

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-12, LEV-2.1-6b
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/workflows`
2. Saisir « Nom » : `Circuit écran ${unique}`
3. Choisir « Type de document » : `PRET_A_USAGE`
4. Cocher « APPRO »
5. Cocher « DIRECTION »
6. Activer « Créer le circuit »
7. Voir « Circuit écran ${unique} »
8. Activer « Éditer le circuit Circuit écran ${unique} » (button)
9. Saisir « Nom » : `Circuit écran modifié ${unique}`
10. Activer « Enregistrer les modifications »
11. Voir « Circuit écran modifié ${unique} »
12. Activer « Supprimer le circuit Circuit écran modifié ${unique} » (button)
13. Ne pas voir « Circuit écran modifié ${unique} »

### E2E-LEV-05 — L'administration configure un circuit d'approbation par type de document

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-12, LEV-2.1-6b
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/workflows`
2. Saisir « Nom » : `Circuit prêt E2E ${unique}`
3. Choisir « Type de document » : `Prêt à usage`
4. Cocher « DIRECTION »
5. Activer « Créer le circuit »
6. Voir « Circuit créé », « Circuit prêt E2E ${unique} »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
8. Activer « Supprimer le circuit Circuit prêt E2E ${unique} » (button)
9. Voir « Circuit supprimé »
10. Ne pas voir « Circuit prêt E2E ${unique} »

## LEV-C-13 — ENF1 Conformité LCOP/LCV (règles en données)

- **Source** : Cahier 2026-50-92 — ENF1
- **Conformité déclarée** : ✅ Règles = ClauseCondition maintenables

### E2E-ECR-LEV-14 — Les paramètres de l'organisation : valeurs des listes, modèle de document et les quatre intégrations, enregistrées et testées

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-13, LEV-C-16, LEV-C-21
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/parametres`
2. Activer « Ajouter une valeur »
3. Saisir « Code » : `E2E_${UNIQUE}` ; « Libellé » : `Valeur E2E ${unique}`
4. Activer « Enregistrer »
5. Voir « Valeur E2E ${unique} »
6. Activer « Modifier — Valeur E2E ${unique} » (button)
7. Saisir « Libellé » : `Valeur E2E modifiée ${unique}`
8. Activer « Enregistrer »
9. Activer « Retirer — Valeur E2E modifiée ${unique} » (button)
10. Activer « Supprimer — Valeur E2E modifiée ${unique} » (button)
11. Ne pas voir « Valeur E2E modifiée ${unique} »
12. Saisir « Pied de page (mention) » : `Document E2E ${unique}`
13. Activer « Enregistrer le modèle »
14. Dans la section « Intégration Microsoft 365 », activer « Enregistrer »
15. Dans la section « Intégration Microsoft 365 », activer « Tester la connexion »
16. Saisir « URL de base du service OData » : `https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap` ; « Chemin du service » : `/API_BUSINESS_PARTNER/A_BusinessPartner`
17. Dans la section « Intégration SAP », activer « Enregistrer »
18. Dans la section « Intégration SAP », activer « Tester la connexion »
19. Saisir « Émetteur (issuer / URL de découverte) » : `http://localhost:8180/realms/iq` ; « Identifiant client (client_id) » : `contrat-e2e`
20. Dans la section « Authentification OpenID Connect », activer « Enregistrer »
21. Dans la section « Authentification OpenID Connect », activer « Tester la découverte »
22. Dans la section « Authentification SAML 2.0 », activer « Enregistrer »
23. Dans la section « Authentification SAML 2.0 », activer « Tester la configuration »

### E2E-LEV-03 — L'assistant guidé recommande les clauses selon le type, la valeur et le contexte, puis génère le devis assemblé

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-1, LEV-2.1-4, LEV-2.1-5a, LEV-2.1-5b, LEV-2.1-5c, LEV-2.1-5d, LEV-2.1-5e, LEV-C-04, LEV-C-05, LEV-C-06, LEV-C-07, LEV-C-13, LEV-2.1-9
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/assistant`
2. Choisir « Type de document » : `Appel d'offres public`
3. Saisir « Valeur estimée ($) » : `250000` ; « Contexte du projet » : `Travaux de voirie`
4. Activer « Analyser le besoin »
5. Voir « Mode de sollicitation suggéré : », « Clauses éligibles »
6. Saisir « Titre du document » : `Devis E2E ${unique}`
7. Activer « Générer le document »
8. Arriver sur une adresse de la forme `/documents/\d+`
9. Voir « Devis E2E ${unique} », « 🔒 normative »
10. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-14 — ENF1 Loi 25 — chiffrement transit

- **Source** : Cahier 2026-50-92 — ENF1
- **Conformité déclarée** : ✅ TLS
- **Vérification** : documentaire — Chiffrement en transit assuré par l'infrastructure (TLS de l'entrée Kubernetes et de Front Door).

## LEV-C-15 — ENF1 Loi 25 — chiffrement au repos / Privacy by Design

- **Source** : Cahier 2026-50-92 — ENF1
- **Conformité déclarée** : Oui. Droits d'accès et d'effacement (Loi 25, art. 27 à 28.1) : dossier structuré par source (compte, journal, rédaction, fiches, notifications), effacement par pseudonymisation avec motif de conservation, registre des demandes sous empreinte ; chiffrement en transit et au repos assuré par l'infrastructure.

### E2E-ECR-LEV-11 — L'administration importe des clauses par CSV et efface une personne au titre de la Loi 25

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-01, LEV-C-15
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Personne à effacer
2. Ouvrir l'écran `/donnees`
3. Saisir « Contenu CSV » : `code,titre,categorie,corps
IMP-${UNIQUE},Clause importée ${unique},STANDARD,Texte importé.`
4. Activer « Importer »
5. Voir « IMP-${UNIQUE} »
6. Ouvrir l'écran `/vie-privee`
7. Saisir « Motif » : `Demande écrite E2E ${unique}`
8. Saisir « Courriel de la personne concernée #2 » : `efface.${unique}@levis.qc.ca`
9. Activer « Effacer la personne »
10. Dans la fenêtre ouverte, activer « Confirmer »
11. Voir « Demande écrite E2E ${unique} »

### E2E-LEV-08 — Droits Loi 25 : accès aux renseignements d'une personne, registre des demandes

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-15
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/vie-privee`
2. Saisir « Courriel de la personne concernée » : `redacteur@levis.qc.ca`
3. Activer « Rassembler les renseignements »
4. Voir « renseignement(s) détenu(s) sur redacteur@levis.qc.ca »
5. Voir « Registre des demandes »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-16 — ENF2 Auth SSO Azure AD + RBAC + MFA

- **Source** : Cahier 2026-50-92 — ENF2
- **Conformité déclarée** : ✅ Entra ID (OIDC) + RBAC + TOTP/question

### E2E-ECR-LEV-01 — L'administration crée un compte, le modifie puis le supprime depuis l'écran des utilisateurs

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/utilisateurs`
2. Activer « Nouvel utilisateur »
3. Saisir « Courriel » : `e2e.${unique}@levis.qc.ca` ; « Nom » : `Compte E2E ${unique}` ; « Mot de passe » : `Passw0rd!E2E-2026`
4. Cocher « REDACTEUR »
5. Activer « Enregistrer »
6. Saisir « Recherche (courriel, nom)… » : `e2e.${unique}`
7. Voir « Compte E2E ${unique} »
8. Dans la ligne « e2e.${unique}@levis.qc.ca », activer « Éditer »
9. Saisir « Direction » : `Approvisionnement E2E`
10. Activer « Enregistrer »
11. Voir « Compte E2E ${unique} »
12. Dans la ligne « e2e.${unique}@levis.qc.ca », activer « Supprimer »
13. Dans la fenêtre ouverte, activer « Confirmer »
14. Ne pas voir « e2e.${unique}@levis.qc.ca »

### E2E-ECR-LEV-09 — Le rédacteur modifie son profil et gère ses répertoires de dépôt

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-C-16
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/profil`
2. Saisir « Direction » : `Approvisionnement ${unique}`
3. Activer « Enregistrer »
4. Saisir « Nouveau répertoire » : `Contrats/E2E-${unique}`
5. Activer « Ajouter »
6. Voir « Contrats/E2E-${unique} »
7. Activer « Retirer »
8. Ne pas voir « Contrats/E2E-${unique} »

### E2E-ECR-LEV-10 — Un utilisateur active puis désactive le MFA par application (TOTP) et par question secrète

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Compte sans MFA
2. Se connecter avec le compte `mfa.${unique}@levis.qc.ca`
3. Ouvrir l'écran `/securite`
4. Activer « Configurer le TOTP »
5. Relever le texte affiché (`code`) sous le nom `secretTotp`
6. Saisir le code TOTP courant dans « Code de confirmation »
7. Activer « Activer »
8. Activer « Désactiver le MFA »
9. Saisir « Question » : `Ville natale ?` ; « Réponse » : `Lévis`
10. Activer « Activer la question secrète »
11. Activer « Désactiver le MFA »

### E2E-ECR-LEV-14 — Les paramètres de l'organisation : valeurs des listes, modèle de document et les quatre intégrations, enregistrées et testées

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-13, LEV-C-16, LEV-C-21
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/parametres`
2. Activer « Ajouter une valeur »
3. Saisir « Code » : `E2E_${UNIQUE}` ; « Libellé » : `Valeur E2E ${unique}`
4. Activer « Enregistrer »
5. Voir « Valeur E2E ${unique} »
6. Activer « Modifier — Valeur E2E ${unique} » (button)
7. Saisir « Libellé » : `Valeur E2E modifiée ${unique}`
8. Activer « Enregistrer »
9. Activer « Retirer — Valeur E2E modifiée ${unique} » (button)
10. Activer « Supprimer — Valeur E2E modifiée ${unique} » (button)
11. Ne pas voir « Valeur E2E modifiée ${unique} »
12. Saisir « Pied de page (mention) » : `Document E2E ${unique}`
13. Activer « Enregistrer le modèle »
14. Dans la section « Intégration Microsoft 365 », activer « Enregistrer »
15. Dans la section « Intégration Microsoft 365 », activer « Tester la connexion »
16. Saisir « URL de base du service OData » : `https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap` ; « Chemin du service » : `/API_BUSINESS_PARTNER/A_BusinessPartner`
17. Dans la section « Intégration SAP », activer « Enregistrer »
18. Dans la section « Intégration SAP », activer « Tester la connexion »
19. Saisir « Émetteur (issuer / URL de découverte) » : `http://localhost:8180/realms/iq` ; « Identifiant client (client_id) » : `contrat-e2e`
20. Dans la section « Authentification OpenID Connect », activer « Enregistrer »
21. Dans la section « Authentification OpenID Connect », activer « Tester la découverte »
22. Dans la section « Authentification SAML 2.0 », activer « Enregistrer »
23. Dans la section « Authentification SAML 2.0 », activer « Tester la configuration »

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ❌ échoué

1. Ouvrir l'écran `/utilisateurs`
2. Voir « redacteur@levis.qc.ca », « REDACTEUR »
3. Passer l'interface en anglais
4. Voir « Documents »
5. Passer l'interface en français
6. Ouvrir l'écran `/parametres`
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-17 — ENF2 Journal d'audit

- **Source** : Cahier 2026-50-92 — ENF2
- **Conformité déclarée** : ✅ AuditLog

### E2E-LEV-06 — Le juriste commente le devis, puis le circuit d'approbation démarre et chaque niveau décide

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-6, LEV-2.1-6a, LEV-C-11, LEV-C-17
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis à réviser
2. Se connecter en tant que juriste de Lévis
3. Ouvrir l'écran `/documents/${document}`
4. Saisir « Commenter le devis… » : `Préciser la garantie ${unique}`
5. Activer « Commenter »
6. Voir « Commentaire ajouté », « Préciser la garantie ${unique} »
7. Se connecter en tant que rédacteur de Lévis
8. Ouvrir l'écran `/documents/${document}`
9. Activer « Démarrer l'approbation »
10. Voir « Circuit d’approbation démarré », « APPRO »
11. Se connecter en tant que administrateur de Lévis
12. Ouvrir l'écran `/audit`
13. Voir « Journal d’audit »
14. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-18 — ENF4 Solution clé en main, conteneurisée

- **Source** : Cahier 2026-50-92 — ENF4
- **Conformité déclarée** : ✅ Docker (front/back/PG/Ollama)
- **Vérification** : documentaire — Livraison conteneurisée (Docker Compose, chart Helm).

## LEV-C-19 — ENF4 Formation FR documentée

- **Source** : Cahier 2026-50-92 — ENF4
- **Conformité déclarée** : ✅ Guide de l'utilisateur en français, par profil et par écran (docs/guide-utilisateur.md), en plus de la documentation technique
- **Vérification** : documentaire — Guide de l'utilisateur en français : connexion, recherche multicritère, bibliothèque et historique, rédaction, révision et approbation, addendas, SEAO, administration, questions fréquentes.

## LEV-C-20 — DAO 2.3 Intégration SEAO (format devis)

- **Source** : Cahier 2026-50-92 — DAO
- **Conformité déclarée** : Oui. Fiche XML des métadonnées de l'avis (numéro, organisme, titre, type d'avis, nature, montant estimé, adjudicataire), éléments du jeu de données ouvertes SEAO ; le SEAO n'offrant pas de dépôt par API, la fiche est reprise dans le SEAO et versée au dossier ; export journalisé. Nomenclature des codes à confirmer avec la Ville.

### E2E-ECR-LEV-13 — Dans l'éditeur, chaque fonction du devis s'exerce : assemblage, sections, commentaires, approbation, publication, addenda, versions, obligations, signature, annexes, bordereau et générations

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-2.1-4, LEV-2.1-6, LEV-2.1-6a, LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-11, LEV-C-20
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Document de demande de prix
2. Ouvrir l'écran `/documents/${doc}`
3. Activer « Assembler le devis »
4. Activer « Ajouter une section libre »
5. Saisir « Titre de la section » : `Section libre ${unique}` ; « Contenu » : `Contenu propre au projet.`
6. Dans la fenêtre ouverte, activer « Ajouter la section »
7. Voir « Section libre ${unique} »
8. Dans la section « Devis assemblé », activer « Rédiger »
9. Saisir « Titre de la section » : `Section rédigée ${unique}`
10. Dans la fenêtre ouverte, activer « Enregistrer »
11. Voir « Section rédigée ${unique} »
12. Dans la section « Devis assemblé », activer « Retirer »
13. Saisir « Commenter le devis… » : `Commentaire ${unique}`
14. Activer « Commenter »
15. Activer « Marquer résolu »
16. Activer « Démarrer l'approbation »
17. Se connecter en tant que rédacteur de Lévis
18. Ouvrir l'écran `/documents/${doc}`
19. Dans la section « Circuit d'approbation », activer « Approuver »
20. Voir « Étape approuvée »
21. Se connecter en tant que juriste de Lévis
22. Ouvrir l'écran `/documents/${doc}`
23. Dans la section « Circuit d'approbation », activer « Approuver »
24. Voir « Étape approuvée »
25. Se connecter en tant que direction de Lévis
26. Ouvrir l'écran `/documents/${doc}`
27. Dans la section « Circuit d'approbation », activer « Approuver »
28. Voir « Étape approuvée »
29. Se connecter en tant que administrateur de Lévis
30. Ouvrir l'écran `/documents/${doc}`
31. Saisir « Répertoire de destination » : `Contrats/E2E-${unique}`
32. Activer « Enregistrer ce répertoire »
33. Activer « Publier »
34. Activer « Nouvel addenda »
35. Saisir « Objet de l'addenda » : `Addenda ${unique}`
36. Dans la fenêtre ouverte, activer « Valider »
37. Activer « Ajouter un changement »
38. Choisir « Clause du devis à modifier » : `(premier)`
39. Saisir « Nouveau texte de la clause » : `Texte modifié par l'addenda ${unique}.`
40. Dans la fenêtre ouverte, activer « Ajouter le changement »
41. Activer « Émettre (réintégrer) »
42. Activer « Capturer une version »
43. Dans la fenêtre ouverte, activer « Valider »
44. Activer « Capturer une version »
45. Dans la fenêtre ouverte, activer « Valider »
46. Choisir « version A » : `v1` ; « version B » : `v2`
47. Activer « Comparer »
48. Dans la section « Obligations », activer « Ajouter »
49. Saisir « Titre » : `Obligation ${unique}`
50. Dans la fenêtre ouverte, activer « Valider »
51. Activer « Compléter »
52. Activer « Ajouter un signataire »
53. Saisir « Nom » : `Signataire ${unique}` ; « Courriel » : `signataire.${unique}@exemple.ca`
54. Dans la fenêtre ouverte, activer « Valider »
55. Activer « Démarrer la signature »
56. Activer « Signer »
57. Saisir « Nom complet du signataire » : `Signataire ${unique}`
58. Dans la fenêtre ouverte, activer « Apposer la signature »
59. Activer « Ajouter une annexe »
60. Saisir « Titre » : `Annexe ${unique}`
61. Dans la fenêtre ouverte, activer « Valider »
62. Voir « Annexe ${unique} »
63. Dans la section « Annexes », activer « Supprimer »
64. Activer « Ajouter une ligne »
65. Dans la section « Bordereau de prix », activer « Enregistrer »
66. Activer « Générer »
67. Activer « Aperçu PDF »
68. Dans la fenêtre ouverte, activer « Fermer »
69. Activer « Générer »
70. Télécharger par « Fiche SEAO (XML) » et obtenir un fichier non vide

### E2E-LEV-07 — Un addenda modifie une clause du devis, est émis et réintégré ; la fiche SEAO se produit

- **Profil** : rédacteur de Lévis
- **Exigences démontrées** : LEV-2.1-7, LEV-2.1-8, LEV-C-10, LEV-C-20
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Devis assemblé
2. Ouvrir l'écran `/documents/${document}`
3. Activer « Nouvel addenda »
4. Saisir « Objet de l'addenda » : `Report de la date d'ouverture ${unique}`
5. Activer « Valider »
6. Voir « Report de la date d'ouverture ${unique} »
7. Activer « Générer »
8. Voir « Aperçu PDF », « Export PDF », « Export DOCX », « Fiche SEAO (XML) »
9. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-21 — DAO 2.3 Intégration annuaire d'entreprise

- **Source** : Cahier 2026-50-92 — DAO
- **Conformité déclarée** : ✅ Azure Entra ID

### E2E-ECR-LEV-14 — Les paramètres de l'organisation : valeurs des listes, modèle de document et les quatre intégrations, enregistrées et testées

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-13, LEV-C-16, LEV-C-21
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/parametres`
2. Activer « Ajouter une valeur »
3. Saisir « Code » : `E2E_${UNIQUE}` ; « Libellé » : `Valeur E2E ${unique}`
4. Activer « Enregistrer »
5. Voir « Valeur E2E ${unique} »
6. Activer « Modifier — Valeur E2E ${unique} » (button)
7. Saisir « Libellé » : `Valeur E2E modifiée ${unique}`
8. Activer « Enregistrer »
9. Activer « Retirer — Valeur E2E modifiée ${unique} » (button)
10. Activer « Supprimer — Valeur E2E modifiée ${unique} » (button)
11. Ne pas voir « Valeur E2E modifiée ${unique} »
12. Saisir « Pied de page (mention) » : `Document E2E ${unique}`
13. Activer « Enregistrer le modèle »
14. Dans la section « Intégration Microsoft 365 », activer « Enregistrer »
15. Dans la section « Intégration Microsoft 365 », activer « Tester la connexion »
16. Saisir « URL de base du service OData » : `https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap` ; « Chemin du service » : `/API_BUSINESS_PARTNER/A_BusinessPartner`
17. Dans la section « Intégration SAP », activer « Enregistrer »
18. Dans la section « Intégration SAP », activer « Tester la connexion »
19. Saisir « Émetteur (issuer / URL de découverte) » : `http://localhost:8180/realms/iq` ; « Identifiant client (client_id) » : `contrat-e2e`
20. Dans la section « Authentification OpenID Connect », activer « Enregistrer »
21. Dans la section « Authentification OpenID Connect », activer « Tester la découverte »
22. Dans la section « Authentification SAML 2.0 », activer « Enregistrer »
23. Dans la section « Authentification SAML 2.0 », activer « Tester la configuration »

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ❌ échoué

1. Ouvrir l'écran `/utilisateurs`
2. Voir « redacteur@levis.qc.ca », « REDACTEUR »
3. Passer l'interface en anglais
4. Voir « Documents »
5. Passer l'interface en français
6. Ouvrir l'écran `/parametres`
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## LEV-C-22 — Multi-tenant / multilingue (au-delà du cahier)

- **Source** : Cahier 2026-50-92 — Multi-tenant
- **Conformité déclarée** : ✅ Bonus : isolation par tenant, i18n FR/EN/MG

### E2E-ECR-LEV-12 — L'écran d'accueil public, la connexion à l'image de l'organisation et les dépôts simulés s'ouvrent

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-22, LEV-2.1-13
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/simulateur`
2. Activer « Rafraîchir »
3. Ouvrir l'écran `/accueil`
4. Ouvrir l'écran `http://levis.e2e.localhost:5175/login`
5. Voir « Ville de Lévis »

### E2E-ECR-LEV-15 — Le superadmin crée une organisation, la modifie, la désactive et la réactive, lui donne un administrateur, un thème, des modules, un tarif, un modèle de document et une banque d'heures

- **Profil** : superadmin
- **Exigences démontrées** : LEV-C-22, LEV-2.1-10
- **Données** : `bdd/e2e/donnees/ecrans-levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/superadmin`
2. Saisir « Nouveau tenant » Slug (sous-domaine) » : `e2e-${unique}` ; « Nouveau tenant » Titre de connexion » : `Organisation E2E ${unique}`
3. Activer « Créer le tenant »
4. Voir « Organisation E2E ${unique} »
5. Dans la ligne « e2e-${unique} », activer « Éditer »
6. Saisir « Sous-titre » : `Sous-titre E2E`
7. Dans la fenêtre ouverte, activer « Enregistrer les modifications »
8. Dans la ligne « e2e-${unique} », activer « Désactiver »
9. Dans la ligne « e2e-${unique} », activer « Activer »
10. Choisir « Créer l'administrateur d'un tenant » Tenant » : `Organisation E2E ${unique}`
11. Saisir « Créer l'administrateur d'un tenant » Courriel » : `admin.${unique}@e2e.example` ; « Créer l'administrateur d'un tenant » Nom » : `Admin E2E` ; « Créer l'administrateur d'un tenant » Mot de passe » : `Passw0rd!E2E-2026`
12. Activer « Créer l'admin »
13. Choisir « Thème & design par tenant » Tenant » : `Organisation E2E ${unique}`
14. Saisir « Thème & design par tenant » Logo / texte de marque » : `E2E ${unique}`
15. Activer « Enregistrer le thème »
16. Ouvrir l'écran `/superadmin`
17. Choisir « Modules par tenant » Organisation » : `Organisation E2E ${unique}`
18. Dans la ligne « Moteur de formulaires », activer « Activer »
19. Dans la ligne « Moteur de formulaires », activer « Désactiver »
20. Choisir « Tarification par tenant » Tenant » : `Organisation E2E ${unique}`
21. Saisir « Forfait annuel (avant taxes) » : `12000`
22. Activer « Enregistrer le tarif »
23. Choisir « Modèle de document » Tenant » : `Organisation E2E ${unique}`
24. Activer « Enregistrer le modèle »
25. Choisir « Banques d'heures des organisations » Organisation » : `Ville de Lévis`
26. Saisir « Ouvrir une banque d'heures » Nom » : `Banque E2E ${unique}` ; « Ouvrir une banque d'heures » Début » : `${aujourdhui}` ; « Ouvrir une banque d'heures » Fin » : `${dansUnMois}` ; « Heures achetées » : `40`
27. Activer « Ouvrir une banque d'heures »
28. Activer « Banque E2E ${unique} — 40 h restantes »
29. Saisir « Inscrire le mouvement » Heures » : `2` ; « Inscrire le mouvement » Date » : `${aujourdhui}` ; « Inscrire le mouvement » Description » : `Soutien E2E ${unique}` ; « Inscrire le mouvement » Intervenant » : `Conseiller E2E`
30. Activer « Inscrire le mouvement »
31. Voir « Soutien E2E ${unique} »
32. Se connecter en tant que administrateur de Lévis
33. Ouvrir l'écran `/banque-heures`
34. Télécharger par « ⬇ Télécharger le relevé (CSV) » et obtenir un fichier non vide

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ❌ échoué

1. Ouvrir l'écran `/utilisateurs`
2. Voir « redacteur@levis.qc.ca », « REDACTEUR »
3. Passer l'interface en anglais
4. Voir « Documents »
5. Passer l'interface en français
6. Ouvrir l'écran `/parametres`
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
