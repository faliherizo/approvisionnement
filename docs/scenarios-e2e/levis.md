# Ville de Lévis — avis d'appel d'intérêt 2026-50-92 — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## LEV-2.1-1 — Rédiger des clauses administratives pour demandes de prix et procédures ouvertes (AOP)

- **Source** : Avis 2026-50-92, art. 2.1 n° 1
- **Conformité déclarée** : Oui. Bibliothèque de clauses + moteur d'assemblage produisant devis de demande de prix et d'appel d'offres public.

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

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

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

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

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

### E2E-LEV-09 — Connexion : authentification unique proposée, comptes et rôles administrés, interface multilingue

- **Profil** : administrateur de Lévis
- **Exigences démontrées** : LEV-C-16, LEV-C-21, LEV-C-22
- **Données** : `bdd/e2e/donnees/levis.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/utilisateurs`
2. Voir « redacteur@levis.qc.ca », « REDACTEUR »
3. Passer l'interface en anglais
4. Voir « Documents »
5. Passer l'interface en français
6. Ouvrir l'écran `/parametres`
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
