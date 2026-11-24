# Santé Québec — CHU, avis d'intention — scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).

## CHU-1.01 — Infonuagique, environnement dédié, accès en tout temps, sans limite d'utilisateurs

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ — multi-tenant ; infra Azure/AWS (docs trois-rivière, DR)

### E2E-CHU-15 — Applications par environnement (production, développement, acceptation)

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-1.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.applications`
2. Ouvrir l'écran `/applications`
3. Voir « Applications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-1.02 — Hébergement des RP au Québec

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ — Azure Canada / AWS Montréal (docs infra)
- **Vérification** : documentaire — Hébergement au Québec : région canadaeast (option B de l'ADR-020).

## CHU-1.03 — Cloisonnement données/formulaires entre environnements et clients

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ — TenantContext (multi-tenant) + environnement (Application)

### E2E-CHU-18 — Les formulaires d'une organisation restent invisibles des autres

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire du CHU
2. Ouvrir l'écran `/formulaires`
3. Voir « Cloison ${unique} »
4. Se connecter en tant que administrateur de Lévis
5. Ouvrir l'écran `/formulaires`
6. Ne pas voir « Cloison ${unique} »

## CHU-1.04 — Environnements PROD / DEV / acceptation (UAT)

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ — Application.environnement (DEV/UAT/PROD)

### E2E-CHU-15 — Applications par environnement (production, développement, acceptation)

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-1.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.applications`
2. Ouvrir l'écran `/applications`
3. Voir « Applications »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-1.05 — Disponibilité 99,8 % en tout temps

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ (contractuel/infra) — plans de reprise RPO/RTO
- **Vérification** : documentaire — Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation).

## CHU-1.06 — Sauvegardes redondantes locales+externes quotidiennes

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ (infra) — docs DR
- **Vérification** : documentaire — Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation).

## CHU-1.07 — Hébergement, entretien, correctifs, MAJ, migrations

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ (service) — offre de service
- **Vérification** : documentaire — Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation).

## CHU-1.08 — Gestion durée des sessions + affichage ordi/tablette/mobile

- **Source** : Avis d'intention CHU § 1
- **Conformité déclarée** : ✅ Politique de session par organisation : inactivité et durée maximale, renouvellement glissant (ADR-024) ; chaque écran des scénarios vérifié à 390, 768, 1280 et 1680 px, sans débordement ni superposition

### E2E-CHU-22 — Durée de session et affichage sur ordinateur, tablette et téléphone ; accessibilité

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.08, CHU-2.04
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/mes-demandes`
2. Voir le titre « Mes demandes »
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-2.01 — Interface d'administration FR/EN

- **Source** : Avis d'intention CHU § 2
- **Conformité déclarée** : ✅ — i18n (FR/EN/MGA, 1500+ clés)

### E2E-CHU-01 — Concevoir un formulaire : création, pages et champs, enregistrement, approbation puis publication

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.01, CHU-3.02, CHU-3.09, CHU-2.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/formulaires`
2. Saisir « Code (ex. demande_acces) » : `e2e_${unique}` ; « Nom du formulaire » : `Inscription E2E ${unique}`
3. Activer « Créer »
4. Voir « Inscription E2E ${unique} »
5. Voir le titre « Inscription E2E ${unique} »
6. Activer « + Ajouter une page »
7. Saisir « Titre de la page » : `Identité`
8. Activer « + Ajouter un champ »
9. Constater la valeur « Libellé » : `Nouveau champ`
10. Activer « Enregistrer »
11. Voir « Structure enregistrée. »
12. Activer « Soumettre à l’approbation »
13. Voir « Formulaire soumis à l’approbation. »
14. Activer « Publier »
15. Voir « Formulaire publié. »
16. Activer « Versions » (tab)
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-2.02 — Formulaires multilingues (contenu)

- **Source** : Avis d'intention CHU § 2
- **Conformité déclarée** : ✅ Contenu des formulaires multilingue : libellés des champs, titres des pages, aides et options traduisibles (FR, EN, MG), affichés dans la langue de l'utilisateur ; la valeur enregistrée reste celle de la conception

### E2E-CHU-02 — Le répondant remplit en anglais : titres, libellés et options traduits

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-2.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « LANGUE »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Voir « Identité », « Nom »
4. Passer l'interface en anglais
5. Voir « Identity », « Name »
6. Voir « Activity »
7. Passer l'interface en français

## CHU-2.03 — Gabarits, images, feuilles de style institutionnels

- **Source** : Avis d'intention CHU § 2
- **Conformité déclarée** : ✅ — thème par tenant (couleurs, logo)

### E2E-CHU-19 — Thème institutionnel appliqué à l'organisation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-2.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.theme`
2. Ouvrir l'écran `/formulaires`
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-2.04 — Accessibilité sites Web gouv. QC (SGQRI 008 / WCAG 2.2 AA)

- **Source** : Avis d'intention CHU § 2
- **Conformité déclarée** : ✅ axe-core (WCAG 2.0 à 2.2, A et AA) sur chaque écran des scénarios BDD, à quatre largeurs (390, 768, 1280, 1680 px), avec cibles ≥ 24 px, sans débordement ni superposition ; suite exécutée en intégration continue (infra/bacs-a-sable/ci.sh)

### E2E-CHU-22 — Durée de session et affichage sur ordinateur, tablette et téléphone ; accessibilité

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.08, CHU-2.04
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/mes-demandes`
2. Voir le titre « Mes demandes »
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-3.01 — Formulaires à une ou plusieurs pages

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — FormulaireBuilder (pages)

### E2E-CHU-01 — Concevoir un formulaire : création, pages et champs, enregistrement, approbation puis publication

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.01, CHU-3.02, CHU-3.09, CHU-2.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/formulaires`
2. Saisir « Code (ex. demande_acces) » : `e2e_${unique}` ; « Nom du formulaire » : `Inscription E2E ${unique}`
3. Activer « Créer »
4. Voir « Inscription E2E ${unique} »
5. Voir le titre « Inscription E2E ${unique} »
6. Activer « + Ajouter une page »
7. Saisir « Titre de la page » : `Identité`
8. Activer « + Ajouter un champ »
9. Constater la valeur « Libellé » : `Nouveau champ`
10. Activer « Enregistrer »
11. Voir « Structure enregistrée. »
12. Activer « Soumettre à l’approbation »
13. Voir « Formulaire soumis à l’approbation. »
14. Activer « Publier »
15. Voir « Formulaire publié. »
16. Activer « Versions » (tab)
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-3.02 — Tous les types de champs (texte, zone, cases, radio, liste, date, hyperlien, courriel, nombre, décimale, montant, pourcentage, fichier, matrice, valeurs dynamiques, calculées)

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — 16 types en données de référence TYPE_CHAMP

### E2E-CHU-01 — Concevoir un formulaire : création, pages et champs, enregistrement, approbation puis publication

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.01, CHU-3.02, CHU-3.09, CHU-2.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/formulaires`
2. Saisir « Code (ex. demande_acces) » : `e2e_${unique}` ; « Nom du formulaire » : `Inscription E2E ${unique}`
3. Activer « Créer »
4. Voir « Inscription E2E ${unique} »
5. Voir le titre « Inscription E2E ${unique} »
6. Activer « + Ajouter une page »
7. Saisir « Titre de la page » : `Identité`
8. Activer « + Ajouter un champ »
9. Constater la valeur « Libellé » : `Nouveau champ`
10. Activer « Enregistrer »
11. Voir « Structure enregistrée. »
12. Activer « Soumettre à l’approbation »
13. Voir « Formulaire soumis à l’approbation. »
14. Activer « Publier »
15. Voir « Formulaire publié. »
16. Activer « Versions » (tab)
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-3.03 — Aperçu des réponses avant soumission

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — FormulaireRenderer / aperçu builder

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-01 — Le concepteur évalue les règles dans l'aperçu, revient à une version antérieure puis supprime un brouillon de formulaire

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.09, CHU-4.01
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié en deux versions, et un brouillon à supprimer
2. Ouvrir l'écran `/formulaires`
3. Dans la ligne « VERS ${unique} », activer « Concevoir »
4. Activer « Aperçu »
5. Saisir « Nom complet » : `Essai d'aperçu`
6. Voir « Aperçu du formulaire »
7. Activer « Versions » (tab)
8. Activer « Revenir à cette version »
9. Activer « ← Retour à la liste »
10. Dans la ligne « SUPPR ${unique} », activer « Supprimer »
11. Ne pas voir « SUPPR ${unique} »

## CHU-3.04 — Sauvegarde automatique + reprise ultérieure

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — autosave + reprise ?saisie=

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-02 — Une fiche se dépose depuis le catalogue, puis se traite : pièce jointe et téléchargement, commentaire, document PDF, transmission, validation, critères sauvegardés ; un brouillon s'annule depuis « Mes demandes »

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.04, CHU-6.01, CHU-3.04, CHU-4.07
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/nouvelle-demande`
3. Voir « FICH ${unique} »
4. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
5. Saisir « Nom » : `Fiche traitée ${unique}`
6. Activer « Soumettre »
7. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
8. Activer « Détails »
9. Joindre « piece-${unique}.pdf » au champ « Ajouter un document »
10. Voir « piece-${unique}.pdf »
11. Télécharger par « Télécharger » et obtenir un fichier non vide
12. Saisir « Votre commentaire… » : `Commentaire de traitement ${unique}`
13. Activer « Commenter »
14. Voir « Commentaire de traitement ${unique} »
15. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
16. Activer « Transmettre »
17. Saisir « Courriel du destinataire » : `destinataire.${unique}@chu.example`
18. Dans la fenêtre ouverte, activer « Envoyer le document »
19. Activer « Valider »
20. Saisir « Nom des critères » : `Critères ${unique}`
21. Activer « Sauvegarder ces critères »
22. Activer « Supprimer les critères « Critères ${unique} » » (button)
23. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
24. Ne pas voir « Critères ${unique} »
25. Ouvrir l'écran `/mes-demandes`
26. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
27. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
28. Saisir « Nom » : `Brouillon ${unique}`
29. Voir « Enregistré »
30. Ouvrir l'écran `/mes-demandes`
31. Activer « Annuler »

## CHU-3.05 — Activation/désactivation selon date, nb max de répondants, plage réseau

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ Activation selon la date, le nombre maximal de répondants et la plage réseau (IP) du répondant

### E2E-CHU-04 — Défi anti-robot exigé avant la soumission ; ouverture bornée par la date, le nombre de répondants et le réseau

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.07, CHU-3.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « DEFI »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy`
4. Voir « Je ne suis pas un robot »
5. Cocher « Je ne suis pas un robot »
6. Activer « Soumettre »
7. Voir « Votre fiche a été soumise avec succès. »
8. Préparer par l'API : cas `chu.activation`

## CHU-3.06 — Restriction de choix selon capacité maximale

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : Oui. Places par choix déclarées dans la structure (capacites) ; un choix complet est signalé et désactivé au remplissage, et refusé à la soumission (409) ; les soumissions d'un formulaire sont sérialisées par verrou pour qu'aucune place ne soit prise deux fois.

### E2E-CHU-20 — Places par choix et inventaire des inscriptions

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.06, CHU-4.08
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.places.inventaire`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « Inventaire des inscriptions », « Restantes »
4. Télécharger par « ⬇ Exporter l'inventaire (CSV) » et obtenir un fichier non vide
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-3.07 — Mécanisme anti-automatisation (formulaires publics)

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ Défi anti-robot réel par le protocole siteverify (Cloudflare Turnstile ou hCaptcha, ADR-026), choisi par configuration ; soumission refusée sans jeton valide ; simulation par défaut en recette

### E2E-CHU-04 — Défi anti-robot exigé avant la soumission ; ouverture bornée par la date, le nombre de répondants et le réseau

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.07, CHU-3.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « DEFI »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy`
4. Voir « Je ne suis pas un robot »
5. Cocher « Je ne suis pas un robot »
6. Activer « Soumettre »
7. Voir « Votre fiche a été soumise avec succès. »
8. Préparer par l'API : cas `chu.activation`

## CHU-3.08 — Formulaires liés parent-enfant

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — Formulaire.parentId, Saisie.parentSaisieId

### E2E-CHU-21 — Formulaires liés parent-enfant, duplication, export et réimport de structure, conservation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.08, CHU-3.10, CHU-6.07, CHU-6.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.formulaire.conception`
2. Ouvrir l'écran `/formulaires`
3. Voir « INSCR ${unique} », « Copie ${unique} », « Import ${unique} »
4. Ouvrir l'écran `/donnees`
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-3.09 — Versions, approbation des modifs, retour à une version

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — StatutFormulaire + historique + revenirA + onglet Versions

### E2E-CHU-01 — Concevoir un formulaire : création, pages et champs, enregistrement, approbation puis publication

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.01, CHU-3.02, CHU-3.09, CHU-2.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/formulaires`
2. Saisir « Code (ex. demande_acces) » : `e2e_${unique}` ; « Nom du formulaire » : `Inscription E2E ${unique}`
3. Activer « Créer »
4. Voir « Inscription E2E ${unique} »
5. Voir le titre « Inscription E2E ${unique} »
6. Activer « + Ajouter une page »
7. Saisir « Titre de la page » : `Identité`
8. Activer « + Ajouter un champ »
9. Constater la valeur « Libellé » : `Nouveau champ`
10. Activer « Enregistrer »
11. Voir « Structure enregistrée. »
12. Activer « Soumettre à l’approbation »
13. Voir « Formulaire soumis à l’approbation. »
14. Activer « Publier »
15. Voir « Formulaire publié. »
16. Activer « Versions » (tab)
17. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-01 — Le concepteur évalue les règles dans l'aperçu, revient à une version antérieure puis supprime un brouillon de formulaire

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.09, CHU-4.01
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié en deux versions, et un brouillon à supprimer
2. Ouvrir l'écran `/formulaires`
3. Dans la ligne « VERS ${unique} », activer « Concevoir »
4. Activer « Aperçu »
5. Saisir « Nom complet » : `Essai d'aperçu`
6. Voir « Aperçu du formulaire »
7. Activer « Versions » (tab)
8. Activer « Revenir à cette version »
9. Activer « ← Retour à la liste »
10. Dans la ligne « SUPPR ${unique} », activer « Supprimer »
11. Ne pas voir « SUPPR ${unique} »

## CHU-3.10 — Duplication / export / réimport de structure

- **Source** : Avis d'intention CHU § 3
- **Conformité déclarée** : ✅ — dupliquer / exporter / importer

### E2E-CHU-21 — Formulaires liés parent-enfant, duplication, export et réimport de structure, conservation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.08, CHU-3.10, CHU-6.07, CHU-6.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.formulaire.conception`
2. Ouvrir l'écran `/formulaires`
3. Voir « INSCR ${unique} », « Copie ${unique} », « Import ${unique} »
4. Ouvrir l'écran `/donnees`
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-4.01 — Afficher/masquer/modifier des champs selon les réponses

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — MoteurRegles VISIBILITE

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-01 — Le concepteur évalue les règles dans l'aperçu, revient à une version antérieure puis supprime un brouillon de formulaire

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.09, CHU-4.01
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié en deux versions, et un brouillon à supprimer
2. Ouvrir l'écran `/formulaires`
3. Dans la ligne « VERS ${unique} », activer « Concevoir »
4. Activer « Aperçu »
5. Saisir « Nom complet » : `Essai d'aperçu`
6. Voir « Aperçu du formulaire »
7. Activer « Versions » (tab)
8. Activer « Revenir à cette version »
9. Activer « ← Retour à la liste »
10. Dans la ligne « SUPPR ${unique} », activer « Supprimer »
11. Ne pas voir « SUPPR ${unique} »

## CHU-4.02 — Obligatoire/facultatif selon le contexte

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — OBLIGATION

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-4.03 — Validation croisée + format

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — VALIDATION

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-4.04 — Calcul automatique (formules)

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — CALCUL (somme/diff/produit/quotient, détection de cycle)

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-4.05 — Routage des demandes selon règles

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — ROUTAGE + workflow (routage par montant/condition)

### E2E-CHU-05 — Traitement des fiches : recherche multicritère, critères sauvegardés et réappliqués, export Excel

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.01, CHU-6.02, CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.traitement`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « ${ficheNumero} »
4. Saisir « Numéro ou réponse » : `${ficheNumero}`
5. Saisir « Nom des critères » : `Ma fiche ${unique}`
6. Activer « Sauvegarder ces critères »
7. Voir « Critères « Ma fiche ${unique} » sauvegardés. »
8. Activer « Effacer les critères »
9. Activer « Appliquer « Ma fiche ${unique} » »
10. Constater la valeur « Numéro ou réponse » : `${ficheNumero}`
11. Voir « ${ficheNumero} »
12. Télécharger par « ⬇ Exporter en Excel » et obtenir un fichier non vide
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-03 — Le concepteur donne un circuit à un formulaire ; la fiche soumise devient une tâche que l'administration examine et approuve

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/formulaires`
3. Dans la ligne « CIRC ${unique} », activer « Concevoir »
4. Activer « Workflow » (tab)
5. Activer « + Ajouter une étape »
6. Choisir « Responsable (rôle) » : `ADMIN`
7. Activer « Enregistrer »
8. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
9. Saisir « Nom » : `Demande à approuver ${unique}`
10. Activer « Soumettre »
11. Ouvrir l'écran `/taches`
12. Dans la ligne « CIRC ${unique} », activer « Examiner »
13. Saisir « Commentaire » : `Conforme`
14. Dans la fenêtre ouverte, activer « Approuver »

## CHU-4.06 — Déclenchement d'événements selon données / modif de fiche

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — FicheEvent

### E2E-CHU-07 — Notifications : gabarits, règles d'envoi par déclencheur, journal des envois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.01, CHU-5.03, CHU-4.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.notifications`
2. Ouvrir l'écran `/notifications`
3. Voir « Gabarits », « Règles d’envoi », « Journal des envois »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-4.07 — Processus structurés : tâches, étapes d'approbation, responsabilités

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : ✅ — contexte processus : Workflow/Tache/Decision, écran Tâches et approbations (/taches)

### E2E-CHU-05 — Traitement des fiches : recherche multicritère, critères sauvegardés et réappliqués, export Excel

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.01, CHU-6.02, CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.traitement`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « ${ficheNumero} »
4. Saisir « Numéro ou réponse » : `${ficheNumero}`
5. Saisir « Nom des critères » : `Ma fiche ${unique}`
6. Activer « Sauvegarder ces critères »
7. Voir « Critères « Ma fiche ${unique} » sauvegardés. »
8. Activer « Effacer les critères »
9. Activer « Appliquer « Ma fiche ${unique} » »
10. Constater la valeur « Numéro ou réponse » : `${ficheNumero}`
11. Voir « ${ficheNumero} »
12. Télécharger par « ⬇ Exporter en Excel » et obtenir un fichier non vide
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-02 — Une fiche se dépose depuis le catalogue, puis se traite : pièce jointe et téléchargement, commentaire, document PDF, transmission, validation, critères sauvegardés ; un brouillon s'annule depuis « Mes demandes »

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.04, CHU-6.01, CHU-3.04, CHU-4.07
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/nouvelle-demande`
3. Voir « FICH ${unique} »
4. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
5. Saisir « Nom » : `Fiche traitée ${unique}`
6. Activer « Soumettre »
7. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
8. Activer « Détails »
9. Joindre « piece-${unique}.pdf » au champ « Ajouter un document »
10. Voir « piece-${unique}.pdf »
11. Télécharger par « Télécharger » et obtenir un fichier non vide
12. Saisir « Votre commentaire… » : `Commentaire de traitement ${unique}`
13. Activer « Commenter »
14. Voir « Commentaire de traitement ${unique} »
15. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
16. Activer « Transmettre »
17. Saisir « Courriel du destinataire » : `destinataire.${unique}@chu.example`
18. Dans la fenêtre ouverte, activer « Envoyer le document »
19. Activer « Valider »
20. Saisir « Nom des critères » : `Critères ${unique}`
21. Activer « Sauvegarder ces critères »
22. Activer « Supprimer les critères « Critères ${unique} » » (button)
23. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
24. Ne pas voir « Critères ${unique} »
25. Ouvrir l'écran `/mes-demandes`
26. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
27. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
28. Saisir « Nom » : `Brouillon ${unique}`
29. Voir « Enregistré »
30. Ouvrir l'écran `/mes-demandes`
31. Activer « Annuler »

### E2E-ECR-CHU-03 — Le concepteur donne un circuit à un formulaire ; la fiche soumise devient une tâche que l'administration examine et approuve

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/formulaires`
3. Dans la ligne « CIRC ${unique} », activer « Concevoir »
4. Activer « Workflow » (tab)
5. Activer « + Ajouter une étape »
6. Choisir « Responsable (rôle) » : `ADMIN`
7. Activer « Enregistrer »
8. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
9. Saisir « Nom » : `Demande à approuver ${unique}`
10. Activer « Soumettre »
11. Ouvrir l'écran `/taches`
12. Dans la ligne « CIRC ${unique} », activer « Examiner »
13. Saisir « Commentaire » : `Conforme`
14. Dans la fenêtre ouverte, activer « Approuver »

### E2E-ECR-CHU-04 — Ressources humaines : un employé est créé, modifié, consulté avec son aperçu de paie, puis archivé ; une feuille de temps est saisie, soumise et approuvée ; un projet et ses tâches se gèrent

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-9.01, CHU-4.07
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/rh/personnel`
2. Saisir « Prénom » : `Camille` ; « Nom » : `E2E ${unique}` ; « Poste » : `Analyste` ; « Salaire annuel ($) » : `65000`
3. Activer « Créer »
4. Voir « Camille E2E ${unique} »
5. Dans la ligne « E2E ${unique} », activer « Éditer »
6. Saisir « Département » : `Ressources humaines`
7. Activer « Enregistrer »
8. Activer « Camille E2E ${unique} »
9. Activer « Paie » (tab)
10. Activer « Calculer le bulletin »
11. Ouvrir l'écran `/rh/personnel`
12. Dans la ligne « E2E ${unique} », activer « Archiver »
13. Dans la ligne « E2E ${unique} », activer « Réactiver »
14. Ouvrir l'écran `/rh/feuilles-temps`
15. Activer « + Nouvelle feuille »
16. Activer « + Ajouter une ligne »
17. Saisir « Heures » : `7`
18. Activer « Enregistrer »
19. Activer « Ouvrir »
20. Saisir « Heures » : `7.5`
21. Activer « Enregistrer »
22. Activer « Ouvrir »
23. Activer « Soumettre »
24. Ouvrir l'écran `/rh/feuilles-temps`
25. Activer « Approuver »
26. Ouvrir l'écran `/rh/projets-taches`
27. Saisir « Code » : `PRJ-${UNIQUE}` ; « Nom du projet » : `Projet E2E ${unique}`
28. Activer « Créer le projet »
29. Ouvrir l'écran `/rh/projets-taches`
30. Choisir « Projet » : `Projet E2E ${unique}`
31. Saisir « Nom de la tâche » : `Tâche E2E ${unique}`
32. Activer « Créer la tâche »
33. Dans la ligne « Tâche E2E ${unique} », activer « Éditer »
34. Saisir « Nom de la tâche » : `Tâche E2E modifiée ${unique}`
35. Activer « Enregistrer »
36. Dans la ligne « Tâche E2E modifiée ${unique} », activer « Supprimer »
37. Ne pas voir « Tâche E2E modifiée ${unique} »

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-4.08 — Gestion d'inventaire / capacité (inscriptions à des activités)

- **Source** : Avis d'intention CHU § 4
- **Conformité déclarée** : Oui. Inventaire des inscriptions par choix : places, inscrits, restantes et fiches qui occupent les places ; export CSV protégé contre l'injection de formules ; réservé au personnel qui traite.

### E2E-CHU-20 — Places par choix et inventaire des inscriptions

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.06, CHU-4.08
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.places.inventaire`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « Inventaire des inscriptions », « Restantes »
4. Télécharger par « ⬇ Exporter l'inventaire (CSV) » et obtenir un fichier non vide
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-5.01 — Notifications courriel / SMS / interne selon déclencheur

- **Source** : Avis d'intention CHU § 5
- **Conformité déclarée** : ✅ — contexte notification (3 canaux, simulation ; adaptateurs réels Twilio/ACS à activer)

### E2E-CHU-07 — Notifications : gabarits, règles d'envoi par déclencheur, journal des envois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.01, CHU-5.03, CHU-4.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.notifications`
2. Ouvrir l'écran `/notifications`
3. Voir « Gabarits », « Règles d’envoi », « Journal des envois »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-5.02 — Confirmation à la soumission

- **Source** : Avis d'intention CHU § 5
- **Conformité déclarée** : ✅ — écran de remplissage

### E2E-CHU-03 — Remplir : règles en direct (visibilité, obligation, validation, calcul), sauvegarde automatique, confirmation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.03, CHU-3.04, CHU-4.01, CHU-4.02, CHU-4.03, CHU-4.04, CHU-5.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié « REGLES »
2. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
3. Saisir « Nom » : `Alice Roy` ; « Heures » : `70` ; « Taux horaire » : `20`
4. Voir « Enregistré », « 60 heures au plus »
5. Saisir « Heures » : `30`
6. Ne pas voir « 60 heures au plus »
7. Constater la valeur « Total » : `600`
8. Cocher « Présentiel »
9. Voir « Disponibilités »
10. Choisir « Activité » : `Formation`
11. Voir « Pièce justificative »
12. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-5.03 — Notification aux administrateurs à la création/modif d'une fiche

- **Source** : Avis d'intention CHU § 5
- **Conformité déclarée** : ✅ — règles de notification sur événements

### E2E-CHU-07 — Notifications : gabarits, règles d'envoi par déclencheur, journal des envois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.01, CHU-5.03, CHU-4.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.notifications`
2. Ouvrir l'écran `/notifications`
3. Voir « Gabarits », « Règles d’envoi », « Journal des envois »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-5.04 — Production d'un document à partir des données + transmission

- **Source** : Avis d'intention CHU § 5
- **Conformité déclarée** : Oui. Document PDF/A-2b produit depuis les données d'une fiche (questions et réponses dans l'ordre du formulaire), téléchargeable par le demandeur et le personnel ; transmission par courriel en pièce jointe (relais SMTP de l'organisation), journalisée.

### E2E-CHU-08 — Document d'une fiche produit puis transmis par courriel

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.04
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.document.fiche`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « Transmettre »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-02 — Une fiche se dépose depuis le catalogue, puis se traite : pièce jointe et téléchargement, commentaire, document PDF, transmission, validation, critères sauvegardés ; un brouillon s'annule depuis « Mes demandes »

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.04, CHU-6.01, CHU-3.04, CHU-4.07
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/nouvelle-demande`
3. Voir « FICH ${unique} »
4. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
5. Saisir « Nom » : `Fiche traitée ${unique}`
6. Activer « Soumettre »
7. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
8. Activer « Détails »
9. Joindre « piece-${unique}.pdf » au champ « Ajouter un document »
10. Voir « piece-${unique}.pdf »
11. Télécharger par « Télécharger » et obtenir un fichier non vide
12. Saisir « Votre commentaire… » : `Commentaire de traitement ${unique}`
13. Activer « Commenter »
14. Voir « Commentaire de traitement ${unique} »
15. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
16. Activer « Transmettre »
17. Saisir « Courriel du destinataire » : `destinataire.${unique}@chu.example`
18. Dans la fenêtre ouverte, activer « Envoyer le document »
19. Activer « Valider »
20. Saisir « Nom des critères » : `Critères ${unique}`
21. Activer « Sauvegarder ces critères »
22. Activer « Supprimer les critères « Critères ${unique} » » (button)
23. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
24. Ne pas voir « Critères ${unique} »
25. Ouvrir l'écran `/mes-demandes`
26. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
27. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
28. Saisir « Nom » : `Brouillon ${unique}`
29. Voir « Enregistré »
30. Ouvrir l'écran `/mes-demandes`
31. Activer « Annuler »

## CHU-5.05 — Messagerie interne sécurisée

- **Source** : Avis d'intention CHU § 5
- **Conformité déclarée** : Oui. Messagerie interne entre membres actifs de l'organisation : conversations visibles des seuls participants (404 sinon), messages chiffrés au repos et non modifiables, texte brut (aucun HTML interprété), non-lus par participant, échanges tracés au journal sans leur contenu, effaçables au titre de la Loi 25.

### E2E-CHU-09 — Messagerie interne : une conversation visible de ses seuls participants

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.messagerie`
2. Ouvrir l'écran `/messagerie`
3. Voir « Messagerie interne », « Conversations »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-07 — Un tableau de bord est créé, ouvert, modifié puis supprimé ; une conversation est ouverte, reçoit une réponse et un participant

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.04, CHU-5.05
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (source des indicateurs) ; deux membres de l'organisation
2. Ouvrir l'écran `/tableaux-de-bord`
3. Activer « Nouveau tableau de bord »
4. Saisir « Nom » : `Tableau E2E ${unique}` ; « Titre de l'indicateur » : `Fiches par activité`
5. Choisir « Formulaire » : `TDB ${unique}`
6. Activer « Enregistrer »
7. Choisir « Tableau de bord » : `Tableau E2E ${unique}`
8. Voir « Fiches par activité »
9. Activer « Modifier »
10. Saisir « Nom » : `Tableau E2E modifié ${unique}`
11. Activer « Enregistrer »
12. Activer « Supprimer le tableau de bord »
13. Dans la fenêtre ouverte, activer « Confirmer »
14. Ouvrir l'écran `/messagerie`
15. Activer « Nouvelle conversation »
16. Saisir « Sujet » : `Conversation E2E ${unique}` ; « Participants (courriels) » : `participant.${unique}@chu.example` ; « Message » : `Premier message`
17. Activer « Envoyer »
18. Activer « Conversation E2E ${unique} »
19. Saisir « Votre réponse » : `Réponse E2E`
20. Activer « Envoyer »
21. Voir « Réponse E2E »
22. Saisir « Ajouter un participant (courriel) » : `invite.${unique}@chu.example`
23. Activer « Ajouter »

## CHU-6.01 — Recherche multicritère + critères sauvegardés

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : ✅ — contexte rapport (CritereRechercheSauve)

### E2E-CHU-05 — Traitement des fiches : recherche multicritère, critères sauvegardés et réappliqués, export Excel

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.01, CHU-6.02, CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.traitement`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « ${ficheNumero} »
4. Saisir « Numéro ou réponse » : `${ficheNumero}`
5. Saisir « Nom des critères » : `Ma fiche ${unique}`
6. Activer « Sauvegarder ces critères »
7. Voir « Critères « Ma fiche ${unique} » sauvegardés. »
8. Activer « Effacer les critères »
9. Activer « Appliquer « Ma fiche ${unique} » »
10. Constater la valeur « Numéro ou réponse » : `${ficheNumero}`
11. Voir « ${ficheNumero} »
12. Télécharger par « ⬇ Exporter en Excel » et obtenir un fichier non vide
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-02 — Une fiche se dépose depuis le catalogue, puis se traite : pièce jointe et téléchargement, commentaire, document PDF, transmission, validation, critères sauvegardés ; un brouillon s'annule depuis « Mes demandes »

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-5.04, CHU-6.01, CHU-3.04, CHU-4.07
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié
2. Ouvrir l'écran `/nouvelle-demande`
3. Voir « FICH ${unique} »
4. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
5. Saisir « Nom » : `Fiche traitée ${unique}`
6. Activer « Soumettre »
7. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
8. Activer « Détails »
9. Joindre « piece-${unique}.pdf » au champ « Ajouter un document »
10. Voir « piece-${unique}.pdf »
11. Télécharger par « Télécharger » et obtenir un fichier non vide
12. Saisir « Votre commentaire… » : `Commentaire de traitement ${unique}`
13. Activer « Commenter »
14. Voir « Commentaire de traitement ${unique} »
15. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
16. Activer « Transmettre »
17. Saisir « Courriel du destinataire » : `destinataire.${unique}@chu.example`
18. Dans la fenêtre ouverte, activer « Envoyer le document »
19. Activer « Valider »
20. Saisir « Nom des critères » : `Critères ${unique}`
21. Activer « Sauvegarder ces critères »
22. Activer « Supprimer les critères « Critères ${unique} » » (button)
23. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
24. Ne pas voir « Critères ${unique} »
25. Ouvrir l'écran `/mes-demandes`
26. Télécharger par « Document PDF de la fiche » et obtenir un fichier non vide
27. Ouvrir l'écran `/formulaires/${formulaire}/remplir`
28. Saisir « Nom » : `Brouillon ${unique}`
29. Voir « Enregistré »
30. Ouvrir l'écran `/mes-demandes`
31. Activer « Annuler »

## CHU-6.02 — Export Excel (toutes/sélection)

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : ✅ — export POI

### E2E-CHU-05 — Traitement des fiches : recherche multicritère, critères sauvegardés et réappliqués, export Excel

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.01, CHU-6.02, CHU-4.07, CHU-4.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.traitement`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « ${ficheNumero} »
4. Saisir « Numéro ou réponse » : `${ficheNumero}`
5. Saisir « Nom des critères » : `Ma fiche ${unique}`
6. Activer « Sauvegarder ces critères »
7. Voir « Critères « Ma fiche ${unique} » sauvegardés. »
8. Activer « Effacer les critères »
9. Activer « Appliquer « Ma fiche ${unique} » »
10. Constater la valeur « Numéro ou réponse » : `${ficheNumero}`
11. Voir « ${ficheNumero} »
12. Télécharger par « ⬇ Exporter en Excel » et obtenir un fichier non vide
13. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-6.03 — Rapports PDF personnalisés

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : Oui. Rapport PDF/A personnalisé sur les fiches d'un formulaire : titre, questions montrées (8 au plus) et statuts retenus au choix, décompte par statut ; paramètres inconnus refusés ; réservé au personnel.

### E2E-CHU-10 — Rapport PDF personnalisé sur les fiches d'un formulaire

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.rapport.pdf`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Voir « Rapport PDF personnalisé »
4. Saisir « Titre du rapport (facultatif) » : `Rapport E2E ${unique}`
5. Télécharger par « Produire le rapport PDF » et obtenir un fichier non vide
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-6.04 — Graphiques + tableaux de bord configurables

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : Oui. Tableaux de bord configurables : compteurs, histogrammes (par statut ou par réponse) et évolutions mensuelles sur les fiches des formulaires de l'organisation ; privés ou partagés, modifiables par le seul propriétaire ; seules des valeurs agrégées sont exposées ; graphiques accessibles avec vue tableau.

### E2E-CHU-11 — Tableaux de bord configurables, partagés selon les droits

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.04, CHU-6.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.tableaux-de-bord`
2. Ouvrir l'écran `/tableaux-de-bord`
3. Voir « Tableaux de bord »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-07 — Un tableau de bord est créé, ouvert, modifié puis supprimé ; une conversation est ouverte, reçoit une réponse et un participant

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.04, CHU-5.05
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (source des indicateurs) ; deux membres de l'organisation
2. Ouvrir l'écran `/tableaux-de-bord`
3. Activer « Nouveau tableau de bord »
4. Saisir « Nom » : `Tableau E2E ${unique}` ; « Titre de l'indicateur » : `Fiches par activité`
5. Choisir « Formulaire » : `TDB ${unique}`
6. Activer « Enregistrer »
7. Choisir « Tableau de bord » : `Tableau E2E ${unique}`
8. Voir « Fiches par activité »
9. Activer « Modifier »
10. Saisir « Nom » : `Tableau E2E modifié ${unique}`
11. Activer « Enregistrer »
12. Activer « Supprimer le tableau de bord »
13. Dans la fenêtre ouverte, activer « Confirmer »
14. Ouvrir l'écran `/messagerie`
15. Activer « Nouvelle conversation »
16. Saisir « Sujet » : `Conversation E2E ${unique}` ; « Participants (courriels) » : `participant.${unique}@chu.example` ; « Message » : `Premier message`
17. Activer « Envoyer »
18. Activer « Conversation E2E ${unique} »
19. Saisir « Votre réponse » : `Réponse E2E`
20. Activer « Envoyer »
21. Voir « Réponse E2E »
22. Saisir « Ajouter un participant (courriel) » : `invite.${unique}@chu.example`
23. Activer « Ajouter »

## CHU-6.05 — Pages listes/dashboards/rapports selon les droits

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : ✅ Listes et rapports selon les droits ; tableaux de bord configurables par l'utilisateur, partageables (CHU 6.04)

### E2E-CHU-11 — Tableaux de bord configurables, partagés selon les droits

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-6.04, CHU-6.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.tableaux-de-bord`
2. Ouvrir l'écran `/tableaux-de-bord`
3. Voir « Tableaux de bord »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-6.06 — Export intégral structuré (réversibilité)

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : ✅ Export intégral unifié /api/reversibilite/export : une archive ZIP JSON + manifeste, où chaque module souscrit (rédaction, formulaires, saisie) verse ses données ; s'ajoutent l'export de structure JSON et des données Excel par formulaire

### E2E-CHU-21 — Formulaires liés parent-enfant, duplication, export et réimport de structure, conservation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.08, CHU-3.10, CHU-6.07, CHU-6.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.formulaire.conception`
2. Ouvrir l'écran `/formulaires`
3. Voir « INSCR ${unique} », « Copie ${unique} », « Import ${unique} »
4. Ouvrir l'écran `/donnees`
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-6.07 — Durée de conservation des données

- **Source** : Avis d'intention CHU § 6
- **Conformité déclarée** : ✅ Politique de rétention par formulaire (durée de conservation des fiches), appliquée à l'échéance

### E2E-CHU-21 — Formulaires liés parent-enfant, duplication, export et réimport de structure, conservation

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-3.08, CHU-3.10, CHU-6.07, CHU-6.06
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.formulaire.conception`
2. Ouvrir l'écran `/formulaires`
3. Voir « INSCR ${unique} », « Copie ${unique} », « Import ${unique} »
4. Ouvrir l'écran `/donnees`
5. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.01 — SSO identité org + méthodes normalisées

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — connecteurs OIDC/SAML per-tenant

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.02 — 2FA accès admin + politique de mots de passe

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — MFA TOTP / question

### E2E-CHU-17 — Second facteur et politique de mots de passe pour l'accès administrateur

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.02
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/securite`
2. Voir « Authentification TOTP », « Question secrète dynamique »
3. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.03 — Groupes avec droits lecture/modif/suppression

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — securite (Groupe/GroupeMembre/PermissionAcces)

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-7.04 — Contrôle d'accès par utilisateur/groupe/rôle/formulaire/champ/page/fiche/bouton

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — MoteurAcces (portée champ/fiche/bouton, refus par défaut)

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-7.05 — Classification public/interne/confidentiel/secret

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — CLASSIFICATION + Masquage

### E2E-CHU-06 — Pièces jointes d'une fiche : classification, analyse antivirus (maliciel refusé), commentaires internes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.05, CHU-7.08
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.antivirus.pieces-jointes`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Activer « Détails »
4. Voir « Documents », « sain.pdf »
5. Ne pas voir « piege.txt »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.06 — Mesures renforcées (masquage, chiffrement, restriction affichage/export)

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ Masquage et classification des champs, restriction d'affichage et d'export ; chiffrement applicatif des secrets et de la messagerie (SecretCipher) ; chiffrement au repos de la base par l'hébergement (PostgreSQL géré, AES-256, toujours actif)

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.07 — Traçabilité accès et modifications

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — journal d'audit (Loi 25)

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.08 — Protection DDoS / brute-force / maliciels

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ Antivirus ClamAV sur tout fichier reçu, fermé par défaut (ADR-027) ; limiteur de débit applicatif (PareFeuFilter) et verrouillage après échecs de connexion ; anti-DDoS de plateforme (Front Door) et pare-feu applicatif OWASP à l'ingress

### E2E-CHU-06 — Pièces jointes d'une fiche : classification, analyse antivirus (maliciel refusé), commentaires internes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.05, CHU-7.08
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.antivirus.pieces-jointes`
2. Ouvrir l'écran `/formulaires/${formulaire}/fiches`
3. Activer « Détails »
4. Voir « Documents », « sain.pdf »
5. Ne pas voir « piege.txt »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-7.09 — Config pour obligations Loi 25

- **Source** : Avis d'intention CHU § 7
- **Conformité déclarée** : ✅ — masquage NAS, courriel masqué en journal

### E2E-CHU-16 — Sécurité fine : groupes, membres, permissions par champ, fiche ou bouton ; masquage et journal

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-7.03, CHU-7.04, CHU-7.06, CHU-7.07, CHU-7.09, CHU-7.01
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.securite`
2. Ouvrir l'écran `/securite-avancee`
3. Saisir « Code du groupe » : `e2e_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
4. Activer « Ajouter le groupe »
5. Voir « Groupe E2E ${unique} »
6. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-8.01 — API consulter/récupérer/créer/compléter des fiches

- **Source** : Avis d'intention CHU § 8
- **Conformité déclarée** : ✅ — interop (/api/public/v1/formulaires/{code}/fiches)

### E2E-CHU-13 — API : un client d'intégration est créé avec ses portées, son secret montré une seule fois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-8.01, CHU-8.02, CHU-8.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/interop`
2. Saisir « Nom du client » : `Client E2E ${unique}` ; « Portées (ex. fiches:ecrire) » : `fiches:lire`
3. Activer « Créer le client »
4. Voir « Identifiants du client »
5. Activer « J’ai copié le secret »
6. Voir « Client E2E ${unique} »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-8.02 — Auth API par jeton / OAuth

- **Source** : Avis d'intention CHU § 8
- **Conformité déclarée** : ✅ — OAuth2 client_credentials (jetons SHA-256)

### E2E-CHU-13 — API : un client d'intégration est créé avec ses portées, son secret montré une seule fois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-8.01, CHU-8.02, CHU-8.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/interop`
2. Saisir « Nom du client » : `Client E2E ${unique}` ; « Portées (ex. fiches:ecrire) » : `fiches:lire`
3. Activer « Créer le client »
4. Voir « Identifiants du client »
5. Activer « J’ai copié le secret »
6. Voir « Client E2E ${unique} »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-8.03 — Échanges formulaires/données/sites/systèmes externes

- **Source** : Avis d'intention CHU § 8
- **Conformité déclarée** : ✅ API publique à jeton, API d'intégration entrante (ADR-011), échanges hérités (CHU 8.04), connecteurs M365 et SAP, connecteur MCP ; une intégration propre à un système s'ajoute comme adaptateur

### E2E-CHU-13 — API : un client d'intégration est créé avec ses portées, son secret montré une seule fois

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-8.01, CHU-8.02, CHU-8.03
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Ouvrir l'écran `/interop`
2. Saisir « Nom du client » : `Client E2E ${unique}` ; « Portées (ex. fiches:ecrire) » : `fiches:lire`
3. Activer « Créer le client »
4. Voir « Identifiants du client »
5. Activer « J’ai copié le secret »
6. Voir « Client E2E ${unique} »
7. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

## CHU-8.04 — Maintien des échanges déjà en place

- **Source** : Avis d'intention CHU § 8
- **Conformité déclarée** : ✅🆕 — correspondances d'échange (entrant, sortant, reprise de l'historique) — ADR-021

### E2E-CHU-14 — Échanges hérités : un échange déjà en place est déclaré et son historique repris

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-8.04
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.echanges.herites`
2. Ouvrir l'écran `/interop`
3. Voir « Échanges hérités », « Reprise de l'historique »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px

### E2E-ECR-CHU-06 — Administration de la plateforme : applications, gabarits et règles de notification, approbations multi-niveaux, échanges hérités, permissions et groupes

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-1.04, CHU-5.01, CHU-4.07, CHU-8.04, CHU-7.03, CHU-7.04
- **Données** : `bdd/e2e/donnees/ecrans-chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : Formulaire publié (cible des notifications et des échanges)
2. Ouvrir l'écran `/applications`
3. Saisir « Code (ex. portail_appro) » : `app_${unique}` ; « Nom de l’application » : `Application E2E ${unique}`
4. Activer « Créer »
5. Dans la ligne « Application E2E ${unique} », activer « Éditer »
6. Saisir « Description » : `Décrite par l'e2e`
7. Activer « Enregistrer »
8. Dans la ligne « Application E2E ${unique} », activer « Archiver »
9. Ouvrir l'écran `/notifications`
10. Saisir « Code (ex. accuse_reception) » : `avis_${unique}` ; « Sujet » : `Avis E2E` ; « Corps du message » : `Bonjour {{nom}}`
11. Activer « Ajouter le gabarit »
12. Saisir « Id du formulaire » : `${formulaire}`
13. Choisir « Gabarit » : `avis_${unique}`
14. Saisir « Destinataires (courriels, rôles…) » : `notif.${unique}@chu.example`
15. Activer « Ajouter la règle »
16. Ouvrir l'écran `/approbations`
17. Saisir « Règles d’approbation » Type de document » : `BON_${UNIQUE}` ; « Champ » : `montant` ; « Valeur » : `1000` ; « Approbateur » : `ADMIN`
18. Activer « Ajouter la règle »
19. Saisir « Nouvelle demande d’approbation » Type de document » : `BON_${UNIQUE}` ; « Montant » : `5000`
20. Activer « Créer la demande »
21. Dans la section « Mes approbations en attente », activer « Approuver »
22. Ouvrir l'écran `/interop`
23. Saisir « Code d'échange » : `ech-${unique}` ; « Correspondances » : `NOM_EMPLOYE=nom`
24. Choisir « Formulaire cible » : `(premier)`
25. Activer « Déclarer l'échange »
26. Choisir « Échange » : `ech-${unique}`
27. Saisir « Export JSON » : `[{"NOM_EMPLOYE": "Alice"}]`
28. Activer « Essayer (n'écrit rien) »
29. Activer « Reprendre les lignes valides »
30. Activer « Désactiver l'échange ech-${unique} » (button)
31. Ouvrir l'écran `/securite-avancee`
32. Saisir « Rôle / groupe / utilisateur » : `LECTEUR` ; « Code du champ / de la fiche / du bouton » : `cible_${unique}`
33. Activer « Ajouter la permission »
34. Dans la ligne « cible_${unique} », activer « Supprimer »
35. Saisir « Code du groupe » : `grp_${unique}` ; « Nom du groupe » : `Groupe E2E ${unique}`
36. Activer « Ajouter le groupe »
37. Saisir « Identifiant / courriel — grp_${unique} » : `membre.${unique}@chu.example`
38. Dans la ligne « grp_${unique} », activer « Ajouter »

## CHU-9.01 — Licences 2 secteurs (RH + Appro) + 4 apps Web

- **Source** : Avis d'intention CHU § 9
- **Conformité déclarée** : ✅ — domaines RH/APPROVISIONNEMENT + catalogue d'Application
- **Vérification** : documentaire — Licences : modèle commercial (domaines RH et approvisionnement, applications).

## CHU-9.02 — Soutien 2e ligne L-V 8 h–17 h

- **Source** : Avis d'intention CHU § 9
- **Conformité déclarée** : n/a (service) — offre de service
- **Vérification** : documentaire — n/a (service) — offre de service

## CHU-9.03 — Tests applicatifs lors migrations/MAJ

- **Source** : Avis d'intention CHU § 9
- **Conformité déclarée** : ✅ Suite BDD par exigence (bdd/) et e2e Playwright (e2e/), rejouées à chaque demande de fusion et avant chaque mise à jour
- **Vérification** : documentaire — Démontré par cette suite BDD et les e2e Playwright, rejoués à chaque demande de fusion.

## CHU-9.04 — Test d'intrusion du portail RH (firme externe)

- **Source** : Avis d'intention CHU § 9
- **Conformité déclarée** : n/a (service) — à planifier
- **Vérification** : documentaire — n/a (service) — à planifier

## CHU-9.05 — Banque d'heures (analyse/formation/config/prog/rapports)

- **Source** : Avis d'intention CHU § 9
- **Conformité déclarée** : Oui. Banque d'heures de services professionnels tenue par le fournisseur : achats, consommations par catégorie (analyse, formation, configuration, programmation, rapports) au quart d'heure, corrections tracées ; solde contrôlé (dépassement refusé) ; l'organisation consulte solde, répartition et relevé CSV.

### E2E-CHU-12 — Banque d'heures de services professionnels tenue et suivie

- **Profil** : administrateur CHU (organisation CHU)
- **Exigences démontrées** : CHU-9.05
- **Données** : `bdd/e2e/donnees/chu.json`
- **Dernier résultat** : ✅ réussi

1. Préparer par l'API : cas `chu.banque-heures`
2. Ouvrir l'écran `/banque-heures`
3. Voir « Banque d'heures de services professionnels »
4. Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px
