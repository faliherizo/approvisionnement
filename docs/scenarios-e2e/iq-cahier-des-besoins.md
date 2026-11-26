# IQ 2026-6370-0005 — Cahier des besoins : scénarios e2e par exigence

> Généré par `bdd/outils/documenter-e2e.mjs` depuis `bdd/exigences/iq.json` (exigences du cahier, IQ-C-*) et
> `bdd/e2e/donnees/*.json` (scénarios, données et constats). Détail des étapes de chaque scénario : [iq.md](iq.md).

**Parcours d'interface** : l'utilisateur du profil saisit, configure, modifie et décide à l'écran (au moins trois
gestes). **Préparé par l'API** : la situation est mise en place par l'API et l'écran ne fait que la constater ;
un tel scénario ne détecte pas un défaut de l'écran. Les scénarios `E2E-CB-IQ-*` (`cahier-iq.json`) sont écrits
besoin par besoin, sans préparation par l'API.

**Couverture** : 62 / 66 exigences fonctionnelles du cahier démontrées par un parcours d'interface ;
4 exigences à vérification documentaire.

## Section 1.3

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-01 | Gérer le cycle de validation, d'approbation et de signature, orchestration comprise | E2E-CB-IQ-04 ✅ | E2E-IQ-06 ✅ |
| IQ-C-02 | Contexte multi-juridictionnel, solution conforme, modulaire, évolutive | — vérification documentaire | — |
| IQ-C-03 | Service de signature appelable, distinct de l'orchestration (couplage lâche) | **aucun** | E2E-IQ-11 ✅, E2E-IQ-19 ✅ |
| IQ-C-04 | Capacités avancées du cycle de vie des contrats | **aucun** | E2E-IQ-32 ✅ |

## Section 1.4.3

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-05 | Offerte en infonuagique, connectable aux SaaS | — vérification documentaire | — |
| IQ-C-06 | Connexion aux applications « on premise » par API | **aucun** | E2E-IQ-19 ✅ |
| IQ-C-07 | Protection et confidentialité des données | **aucun** | E2E-AX-IQ-25 ✅, E2E-IQ-30 ✅ |

## Section 1.4.4

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-08 | Échange avec les systèmes de référence : préalimenter, retourner les statuts | E2E-CB-IQ-17 ✅ | E2E-IQ-20 ✅ |

## Section 1.4.5

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-09 | Récupération automatisée des données Salesforce Grantmaking | E2E-CB-IQ-17 ✅ | E2E-IQ-33 ✅ |
| IQ-C-10 | Génération automatisée des contrats depuis les données Salesforce | E2E-CB-IQ-17 ✅ | E2E-IQ-33 ✅ |
| IQ-C-11 | Préremplissage des modèles approuvés | E2E-CB-IQ-01 ✅, E2E-CB-IQ-17 ✅ | E2E-IQ-33 ✅ |
| IQ-C-12 | Acheminement des documents générés vers validation, approbation, signature | E2E-CB-IQ-17 ✅, E2E-ECR-IQ-01 ✅, E2E-ECR-IQ-02 ✅ | E2E-IQ-33 ✅ |
| IQ-C-13 | Retour vers Salesforce des informations de signature | E2E-CB-IQ-17 ✅ | E2E-IQ-20 ✅ |
| IQ-C-14 | Traçabilité dossier source → document → preuve | E2E-CB-IQ-17 ✅, E2E-ECR-IQ-02 ✅ | E2E-IQ-33 ✅ |
| IQ-C-15 | Erreurs d'intégration, reprises, transactions incomplètes | E2E-CB-IQ-17 ✅, E2E-ECR-IQ-05 ✅ | E2E-IQ-20 ✅ |
| IQ-C-16 | Interfaces standard, documentées et supportées par l'éditeur | E2E-ECR-IQ-05 ✅ | E2E-IQ-19 ✅, E2E-IQ-23 ✅ |

## Section 1.4.6

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-17 | Flexibilité et agilité tout au long de l'utilisation | E2E-CB-IQ-23 ✅, E2E-CB-IQ-24 ✅ | E2E-IQ-03 ✅ |

## Section 1.4.7

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-18 | Circuits de validation et d'approbation séquentiels, parallèles, mixtes | E2E-CB-IQ-04 ✅, E2E-CB-IQ-05 ✅ | E2E-IQ-06 ✅, E2E-IQ-08 ✅ |
| IQ-C-19 | Attribution selon rôles, groupes ou règles d'affaires | E2E-CB-IQ-08 ✅ | E2E-IQ-09 ✅ |
| IQ-C-20 | Ajout, retrait, remplacement d'un validateur ou approbateur en cours | E2E-CB-IQ-07 ✅ | E2E-IQ-08 ✅ |
| IQ-C-21 | Réassignation d'une étape sans redémarrage | E2E-CB-IQ-07 ✅, E2E-ECR-IQ-06 ✅ | E2E-IQ-08 ✅ |
| IQ-C-22 | Approuver, rejeter, retourner pour correction | E2E-CB-IQ-04 ✅, E2E-CB-IQ-05 ✅, E2E-CB-IQ-06 ✅ | E2E-IQ-06 ✅, E2E-IQ-07 ✅ |
| IQ-C-23 | Commentaires et historique des validations | E2E-CB-IQ-04 ✅, E2E-CB-IQ-05 ✅ | E2E-IQ-06 ✅ |
| IQ-C-24 | Notifications et rappels configurables pour validateurs et approbateurs | E2E-CB-IQ-12 ✅, E2E-CB-IQ-13 ✅ | E2E-IQ-15 ✅ |
| IQ-C-25 | Suivi de l'état des validations en cours | E2E-CB-IQ-04 ✅ | E2E-AX-IQ-10 ✅, E2E-IQ-06 ✅, E2E-IQ-16 ✅ |
| IQ-C-26 | Signature séquentielle, parallèle, mixte | E2E-CB-IQ-09 ✅, E2E-CB-IQ-10 ✅ | E2E-IQ-12 ✅ |
| IQ-C-27 | Groupes de signataires | E2E-ECR-IQ-03 ✅ | E2E-IQ-12 ✅ |
| IQ-C-28 | Signature simultanée et consécutive configurable | E2E-CB-IQ-10 ✅ | E2E-IQ-12 ✅ |
| IQ-C-29 | Association automatique des étapes aux signataires | E2E-CB-IQ-09 ✅, E2E-CB-IQ-17 ✅, E2E-ECR-IQ-01 ✅ | E2E-IQ-33 ✅ |
| IQ-C-30 | Modification et ajout de signataires en cours de processus | E2E-CB-IQ-11 ✅, E2E-ECR-IQ-05 ✅, E2E-ECR-IQ-06 ✅ | E2E-IQ-10 ✅ |
| IQ-C-31 | Réassignation de signataires sans reprise complète | E2E-CB-IQ-11 ✅, E2E-ECR-IQ-06 ✅ | E2E-IQ-10 ✅ |
| IQ-C-32 | Modèles réutilisables | E2E-CB-IQ-01 ✅, E2E-CB-IQ-02 ✅ | E2E-IQ-03 ✅ |
| IQ-C-33 | Partage des modèles par groupe d'usagers | E2E-CB-IQ-14 ✅ | E2E-IQ-03 ✅ |
| IQ-C-34 | Ancrage précis des champs | E2E-CB-IQ-15 ✅ | E2E-IQ-03 ✅ |
| IQ-C-35 | Types : texte validé, liste, date, case à cocher | E2E-CB-IQ-01 ✅, E2E-CB-IQ-15 ✅ | E2E-IQ-03 ✅ |
| IQ-C-36 | Distinguer champs de formulaire, de saisie et de signature | E2E-AX-IQ-13 ✅, E2E-AX-IQ-14 ✅, E2E-CB-IQ-15 ✅ | E2E-IQ-03 ✅ |
| IQ-C-37 | Attribution des champs à des signataires | E2E-CB-IQ-15 ✅, E2E-ECR-IQ-02 ✅, E2E-ECR-IQ-04 ✅ | E2E-IQ-03 ✅ |
| IQ-C-38 | Règles conditionnelles d'affichage | E2E-CB-IQ-15 ✅ | E2E-IQ-03 ✅ |
| IQ-C-39 | Saisie assistée (suggestions) | E2E-CB-IQ-22 ✅ | E2E-IQ-04 ✅ |
| IQ-C-40 | Notifications activables et désactivables | E2E-CB-IQ-12 ✅, E2E-ECR-IQ-05 ✅ | E2E-IQ-15 ✅ |
| IQ-C-41 | Rappels automatiques de non-réponse | E2E-AX-IQ-08 ✅, E2E-CB-IQ-13 ✅ | E2E-IQ-15 ✅ |
| IQ-C-42 | Destinataires en copie | E2E-CB-IQ-13 ✅ | E2E-IQ-01 ✅ |
| IQ-C-43 | Vue de suivi centralisée : étapes, attentes, exceptions | E2E-AX-IQ-05 ✅, E2E-AX-IQ-09 ✅, E2E-CB-IQ-13 ✅, E2E-ECR-IQ-06 ✅ | E2E-AX-IQ-06 ✅, E2E-IQ-16 ✅ |
| IQ-C-44 | Plusieurs documents par trousse | E2E-CB-IQ-16 ✅, E2E-ECR-IQ-01 ✅ | E2E-IQ-01 ✅ |
| IQ-C-45 | Dossiers, documents, versions, intervenants | E2E-CB-IQ-16 ✅, E2E-ECR-IQ-01 ✅, E2E-ECR-IQ-04 ✅ | E2E-IQ-01 ✅ |
| IQ-C-46 | PDF et Word | E2E-CB-IQ-16 ✅ | E2E-IQ-01 ✅ |
| IQ-C-47 | Téléversement d'au moins 25 Mo | E2E-CB-IQ-16 ✅ | E2E-IQ-02 ✅ |
| IQ-C-48 | Historique des étapes de validation, d'approbation, de signature | E2E-CB-IQ-04 ✅ | E2E-IQ-06 ✅ |
| IQ-C-49 | Journal d'audit des actions (consultation, validation, signature, rejet, annulation…) | E2E-AX-IQ-02 ✅, E2E-CB-IQ-18 ✅ | E2E-IQ-17 ✅ |
| IQ-C-50 | État et historique des transactions d'un document | E2E-CB-IQ-16 ✅, E2E-ECR-IQ-06 ✅ | E2E-IQ-01 ✅ |
| IQ-C-51 | Conservation et consultation du dossier de preuve | E2E-CB-IQ-18 ✅, E2E-ECR-IQ-01 ✅, E2E-ECR-IQ-03 ✅, E2E-ECR-IQ-07 ✅ | E2E-IQ-17 ✅ |

## Section 1.4.8

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-52 | Minimiser le nombre d'étapes | E2E-CB-IQ-17 ✅ | E2E-IQ-06 ✅ |
| IQ-C-53 | Exécution fluide web et mobile | E2E-CB-IQ-17 ✅ | E2E-IQ-34 ✅ |
| IQ-C-54 | Limiter les dépendances entre signataires | E2E-CB-IQ-09 ✅, E2E-CB-IQ-10 ✅ | E2E-IQ-11 ✅, E2E-IQ-12 ✅ |
| IQ-C-55 | Flexibilité des workflows en cours de processus | E2E-CB-IQ-06 ✅, E2E-CB-IQ-07 ✅, E2E-CB-IQ-11 ✅, E2E-CB-IQ-23 ✅ | E2E-IQ-08 ✅ |
| IQ-C-56 | Minimiser les interventions manuelles | E2E-CB-IQ-17 ✅, E2E-CB-IQ-22 ✅ | E2E-IQ-33 ✅ |
| IQ-C-57 | Expérience simple pour initiateurs, signataires internes et externes | E2E-CB-IQ-17 ✅, E2E-AIDE-01 | E2E-IQ-01 ✅, E2E-IQ-11 ✅ |
| IQ-C-58 | Personnalisation : image de marque, messages, parcours | E2E-CB-IQ-12 ✅, E2E-VIT-01 | E2E-IQ-15 ✅ |

## Section 1.4.9

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-59 | Automatisation et IA (extraction, classification, anomalies) | E2E-ECR-IQ-06 ✅ | E2E-IQ-32 ✅ |

## Section 1.4.10

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-60 | Identification et authentification des utilisateurs (SSO) | E2E-ECR-IQ-07 ✅ | E2E-IQ-28 ✅, E2E-IQ-29 ✅ |
| IQ-C-61 | Authentification et vérification d'identité selon profil, document et risque | E2E-CB-IQ-20 ✅ | E2E-IQ-11 ✅, E2E-IQ-14 ✅ |
| IQ-C-62 | Méthodes internes et externes, authentification renforcée | E2E-CB-IQ-20 ✅, E2E-ECR-IQ-07 ✅ | E2E-IQ-11 ✅ |

## Section 1.4.11

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-63 | Standards reconnus de signature électronique, validité juridique | E2E-CB-IQ-18 ✅ | E2E-IQ-11 ✅ |
| IQ-C-64 | Piste d'audit complète : événements, authentification, horodatages, consentements, intégrité | E2E-CB-IQ-18 ✅ | E2E-IQ-11 ✅, E2E-IQ-17 ✅ |
| IQ-C-65 | Conservation, export, portabilité durables pendant la période requise | E2E-CB-IQ-18 ✅, E2E-ECR-IQ-07 ✅ | E2E-IQ-17 ✅ |

## Section 1.4.12

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-66 | Protection du cycle de vie, dont la destruction au moment opportun (Loi sur les archives) | E2E-AX-IQ-21 ✅, E2E-ECR-IQ-07 ✅ | E2E-IQ-18 ✅ |
| IQ-C-67 | Accès, modèles, workflows, historiques selon rôles, groupes, unités, domaines | E2E-CB-IQ-03 ✅, E2E-CB-IQ-08 ✅, E2E-CB-IQ-14 ✅, E2E-CB-IQ-19 ✅, E2E-CB-IQ-24 ✅ | E2E-IQ-25 ✅, E2E-IQ-26 ✅ |

## Section 1.7.1

| Exigence | Besoin | Parcours d'interface | Préparés par l'API |
|---|---|---|---|
| IQ-C-68 | Compatible postes Windows, mobiles, navigateurs Edge, Chrome, Safari | — vérification documentaire | — |
| IQ-C-69 | S'intégrer à Microsoft 365, Salesforce, Workday : API, connecteurs, protocoles | E2E-AX-IQ-01 ✅ | E2E-IQ-19 ✅ |
| IQ-C-70 | Compatibilité maintenue avec l'évolution de l'environnement | — vérification documentaire | — |

## Défauts et constats relevés par ces parcours

| # | Exigences | Scénario | Constat | Suite donnée |
|---|---|---|---|---|
| D-01 | IQ-C-32, IQ-D-06 | E2E-CB-IQ-02 | Un modèle sans corps, sans champ ni gabarit Word passait la vérification : il pouvait être soumis puis publié, et rien n'en serait généré. | Corrigé — la vérification exige un corps ou un gabarit Word (« Le corps du contrat est vide. »). |
| D-02 | IQ-C-32, IQ-C-38 | E2E-CB-IQ-17 | « Vérifier », « Tester la règle » et « Soumettre à publication » jugeaient la dernière version enregistrée, pas ce qui est à l'écran : corps « vide » juste après l'avoir saisi, ou soumission sans les dernières modifications. | Corrigé — un brouillon modifié est enregistré avant ces trois actions. |
| D-03 | IQ-C-32 | E2E-CB-IQ-17 | Le second enregistrement d'un modèle dans l'éditeur échouait toujours (« Conflit de version ») : le serveur renvoyait l'ancien numéro de version. | Corrigé — la version renvoyée est celle enregistrée ; test d'intégration à deux enregistrements successifs. |
| D-04 | IQ-C-12, IQ-C-57 | E2E-CB-IQ-09 | En production, les liens de signature (invitations, « Signer maintenant ») menaient à http://localhost:5173/portail : l'adresse publique du portail n'était pas configurée. | Corrigé — CLM_PORTAIL_URL posée en production ; le déploiement refuse de démarrer sans adresse https. |
| D-05 | IQ-C-47 | E2E-CB-IQ-16 | En production, toute pièce de plus de 1 Mo était refusée (413) par les relais nginx, alors que le cahier exige au moins 25 Mo. | Corrigé — 32 Mo aux deux relais (comme Spring) ; la recette déployée vérifie qu'une pièce de 26 Mo passe. |
| C-01 | IQ-A-03 | E2E-CB-IQ-01 | La signature en lot propose un modèle dont un champ obligatoire n'a ni source Salesforce ni valeur par défaut ; chaque dossier échoue alors (« Données incomplètes »). | Amélioration proposée : avertir au choix du modèle. Le parcours utilise un champ sourcé de Salesforce. |
| C-02 | IQ-C-52, IQ-C-57 | E2E-CB-IQ-09 | Un signataire interne déjà connecté reçoit tout de même un code par courriel au portail avant de signer. | Selon la politique d'identité interne d'IQ (niveau COURRIEL) : à confirmer avec IQ (étape de plus pour l'interne). |
| C-03 | IQ-C-30, IQ-C-57 | E2E-CB-IQ-09 | La case « Externe » est cochée par défaut à l'ajout d'un signataire : un employé d'IQ devient signataire externe si on oublie de la décocher. | Constat d'ergonomie : décocher par défaut quand le courriel appartient à l'organisation (à décider). |
| C-04 | IQ-C-32, IQ-C-67 | E2E-CB-IQ-03 | Un modèle créé par l'administration d'affaires doit être publié par une autre administration (séparation des responsabilités) ; l'auteur reçoit un refus explicite. | Comportement voulu : chez IQ, admin.ti@iq.example (administration principale) publie les modèles de admin.affaires@iq.example. |
