# Spécification fonctionnelle et maquettes UI --- Plateforme de formulaires et de processus du CHU

**Version :** 1.0\
**Statut :** Document de conception préliminaire --- à valider avec les
équipes RH, approvisionnements, TI, sécurité, architecture,
accessibilité et protection des renseignements personnels.\
**Objet :** Décrire le produit, ses menus, fonctionnalités et maquettes
textuelles afin de servir de base à la conception UX/UI, à
l'architecture et au cahier de réalisation.

------------------------------------------------------------------------

## 1. Résumé du produit

La solution est une plateforme Web d'entreprise en mode infonuagique
permettant de concevoir et publier des formulaires électroniques,
recueillir et gérer les données, automatiser les processus d'affaires,
gérer les approbations, envoyer des notifications, produire des rapports
et intégrer les systèmes du CHU par API.

Elle comprend deux espaces principaux :

1.  **Portail utilisateur** : pour remplir des formulaires, sauvegarder
    un brouillon, soumettre une demande, téléverser des documents et
    suivre l'état de ses dossiers.
2.  **Console d'administration** : pour créer et maintenir les
    formulaires et applications, configurer les règles et workflows,
    gérer les données, les accès, les rapports, les intégrations et les
    environnements.

Les domaines Ressources humaines et Approvisionnements doivent être
séparés logiquement et, selon l'architecture retenue et l'évaluation de
risque, techniquement. Les données, fichiers, formulaires, rôles et
journaux ne doivent pas être accessibles d'un domaine à l'autre sans
autorisation explicite.

> Cette proposition décrit une cible fonctionnelle. Elle ne présume pas
> que toutes les fonctions existent déjà dans un produit commercial
> donné. Les exigences doivent être vérifiées dans une grille de
> conformité fournisseur et validées par preuve ou démonstration.

## 2. Utilisateurs et rôles

  -----------------------------------------------------------------------
  Rôle                                Responsabilités principales
  ----------------------------------- -----------------------------------
  Répondant / employé                 Consulter les formulaires
                                      autorisés, enregistrer un
                                      brouillon, soumettre une demande et
                                      suivre ses dossiers

  Gestionnaire / approbateur          Examiner les demandes assignées,
                                      demander une correction, approuver
                                      ou refuser avec commentaire

  Agent RH                            Traiter les dossiers RH selon les
                                      droits accordés, communiquer avec
                                      le demandeur et produire des
                                      documents autorisés

  Agent Approvisionnements            Traiter les demandes
                                      d'approvisionnement, vérifier les
                                      pièces et suivre les étapes de
                                      validation

  Concepteur de formulaires           Créer des formulaires, champs,
                                      règles, gabarits et versions

  Concepteur de processus             Configurer les étapes, tâches,
                                      conditions, affectations et
                                      notifications

  Responsable de domaine              Approuver les mises en production
                                      dans son secteur

  Administrateur fonctionnel          Gérer les paramètres, groupes,
                                      accès fonctionnels et rapports

  Administrateur technique / sécurité Configurer l'authentification, les
                                      intégrations, les environnements,
                                      la supervision et les contrôles
                                      techniques

  Auditeur autorisé                   Consulter les journaux et rapports
                                      d'audit en lecture seule

  Compte de service / intégration     Appeler les API selon des
                                      permissions minimales, sans accès
                                      interactif inutile
  -----------------------------------------------------------------------

Le modèle d'autorisation doit combiner les rôles et les règles
contextuelles : utilisateur, groupe, domaine, formulaire, champ, fiche,
étape du workflow, environnement et sensibilité de l'information.

## 3. Architecture fonctionnelle cible

``` text
Utilisateurs et administrateurs
            |
Portail utilisateur / Console d’administration
            |
Authentification unique + MFA administratif
            |
Passerelle applicative / WAF / protection DDoS
            |
Services partagés
  ├── Catalogue d’applications et formulaires
  ├── Moteur de rendu et de validation
  ├── Moteur de règles et calculs
  ├── Moteur de workflows et tâches
  ├── Notifications et génération de documents
  ├── Recherche, rapports et exports
  ├── API et connecteurs
  └── Audit, rétention et administration
            |
Stockage des données + stockage des fichiers
            |
Sauvegardes chiffrées, supervision et reprise après sinistre
```

### Principes d'architecture

-   Hébergement des renseignements personnels au Québec, y compris les
    copies, journaux et sauvegardes contenant de tels renseignements,
    selon les engagements contractuels et l'analyse de localisation des
    données.
-   Environnements distincts de **développement (DEV)**, **acceptation
    (UAT)** et **production (PROD)**, avec accès, secrets, données et
    déploiements séparés.
-   Cloisonnement entre clients et entre domaines RH et
    Approvisionnements.
-   Aucun renseignement réel de production dans DEV/UAT sans
    autorisation et mesures appropriées; privilégier des données
    synthétiques ou dépersonnalisées.
-   Chiffrement des communications et des données stockées, gestion
    sécurisée des clés et des secrets.
-   Sauvegardes quotidiennes redondantes, locales et externes, avec
    tests réguliers de restauration.
-   Disponibilité contractuelle visée de 99,8 % et suivi mesurable des
    incidents et périodes d'indisponibilité.
-   Gestion de session, expiration, déconnexion, renouvellement contrôlé
    et protection contre les attaques de session.
-   Interface adaptative pour ordinateur, tablette et mobile.
-   Architecture observable : métriques, journaux techniques, alertes et
    suivi des erreurs sans divulgation de renseignements personnels.

## 4. Navigation générale

### 4.1 Menu du portail utilisateur

``` text
┌──────────────────────────────────────────────────────────────────────┐
│ Logo CHU       Portail de services             Aide  FR | EN  Profil│
├──────────────────────────────────────────────────────────────────────┤
│ Accueil                                                               │
│ Mes demandes                                                          │
│   ├── Brouillons                                                      │
│   ├── Soumises                                                        │
│   ├── À compléter                                                     │
│   └── Terminées                                                       │
│ Nouveau formulaire                                                    │
│ Tâches et approbations (si le rôle le permet)                         │
│ Documents disponibles                                                 │
│ Notifications                                                         │
│ Aide et soutien                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

Fonctions attendues : - afficher les formulaires accessibles selon le
profil et le contexte; - recherche et filtres sur les formulaires; -
indiquer les brouillons et les échéances; - sauvegarde automatique et
reprise ultérieure; - aperçu des réponses avant soumission; -
confirmation de soumission avec numéro de dossier; - historique des
changements de statut et communications autorisées; - affichage
accessible au clavier, lecteur d'écran et mobile.

### 4.2 Menu de la console d'administration

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ CHU | Console d’administration     Environnement : PROD    FR/EN   Profil │
├─────────────────────┬──────────────────────────────────────────────────────┤
│ Tableau de bord     │ Zone de travail                                      │
│ Applications       │                                                      │
│ Formulaires        │                                                      │
│   ├── Catalogue    │                                                      │
│   ├── Concepteur   │                                                      │
│   ├── Versions     │                                                      │
│   └── Publications │                                                      │
│ Processus          │                                                      │
│   ├── Workflows    │                                                      │
│   ├── Tâches       │                                                      │
│   └── Règles       │                                                      │
│ Données            │                                                      │
│   ├── Fiches       │                                                      │
│   ├── Recherche    │                                                      │
│   ├── Exports      │                                                      │
│   └── Conservation │                                                      │
│ Rapports           │                                                      │
│ Notifications      │                                                      │
│ Utilisateurs & rôles│                                                     │
│ Intégrations / API │                                                      │
│ Sécurité & audit   │                                                      │
│ Environnements     │                                                      │
│ Paramètres         │                                                      │
│ Soutien            │                                                      │
└─────────────────────┴──────────────────────────────────────────────────────┘
```

Les entrées de menu doivent être filtrées par autorisation. Les actions
sensibles --- publication, export de données confidentielles, changement
de permissions et suppression --- nécessitent des droits spécifiques et,
au besoin, une confirmation renforcée.

## 5. Maquettes UI détaillées

Les maquettes ci-dessous sont des wireframes fonctionnels textuels, pas
des écrans graphiques définitifs. Les libellés, champs et statuts sont
des exemples à adapter aux processus réels du CHU.

### Écran 1 --- Tableau de bord d'administration

**Objectif :** donner une vue d'ensemble de la santé de la plateforme et
des activités à traiter.

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Console d’administration                         PROD   FR | EN   Profil   │
├────────────────────────────────────────────────────────────────────────────┤
│ Tableau de bord                                                           │
│                                                                            │
│ [Formulaires actifs  42] [Dossiers en cours  286] [Tâches en retard  12]  │
│ [Disponibilité 99,8 % cible] [Alertes sécurité  2]                         │
│                                                                            │
│ Activité récente                       Tâches à effectuer                  │
│ ┌───────────────────────────────┐      ┌───────────────────────────────┐   │
│ │ Formulaire modifié            │      │ Approuver version 1.4         │   │
│ │ Demande d’achat soumise       │      │ Vérifier rapport de migration │   │
│ │ Export terminé                │      │ Examiner échec d’intégration  │   │
│ └───────────────────────────────┘      └───────────────────────────────┘   │
│                                                                            │
│ [Voir les rapports] [Créer un formulaire] [Consulter les journaux]         │
└────────────────────────────────────────────────────────────────────────────┘
```

**Composants** - indicateurs filtrés selon le domaine et les
permissions; - état de la plateforme et incidents en cours; - tâches et
approbations en attente; - activité récente avec liens vers les objets
concernés; - accès rapides aux fonctions autorisées.

**Règles UX** - les indicateurs ne doivent pas révéler les volumes ou
noms de dossiers confidentiels à un utilisateur non autorisé; - la
disponibilité affichée doit provenir d'une mesure réelle et préciser la
période observée; - les alertes de sécurité sont visibles uniquement aux
rôles autorisés.

### Écran 2 --- Catalogue des applications

**Objectif :** gérer les applications Web et les espaces fonctionnels.

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Applications                                      [+ Nouvelle application] │
├────────────────────────────────────────────────────────────────────────────┤
│ Rechercher [____________________] Domaine [Tous v] État [Tous v] [Filtrer]│
│                                                                            │
│ Nom                         Domaine          Environnement  État    Actions│
│ Portail RH                  Ressources humaines PROD         Actif   [...] │
│ Portail Approvisionnements  Approvisionnements PROD         Actif   [...] │
│ Inscriptions aux activités  RH                UAT           Test    [...] │
│ Gestion des demandes        Approvisionnements DEV           Brouillon [...]│
│                                                                            │
│ [Dupliquer] [Exporter la configuration] [Importer]                         │
└────────────────────────────────────────────────────────────────────────────┘
```

**Fonctionnalités** - créer, modifier, archiver et dupliquer une
application; - définir son domaine, propriétaire, équipe responsable et
environnements; - lier des formulaires, workflows, tableaux de bord et
documents; - gérer le thème graphique, logo, styles, langue et
paramètres de session; - importer/exporter la structure et ses
dépendances; - contrôler la publication selon un processus
d'approbation.

### Écran 3 --- Concepteur visuel de formulaires

**Objectif :** permettre la création de formulaires sans programmation
pour les cas courants.

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Formulaire : Demande de service RH   Brouillon v1.5  [Aperçu] [Enregistrer]│
├─────────────────┬──────────────────────────────┬───────────────────────────┤
│ CHAMPS          │ APERÇU DU FORMULAIRE         │ PROPRIÉTÉS                │
│                 │                              │                           │
│ Texte court     │ Demande de service RH        │ Libellé : [Nom complet ]  │
│ Texte long      │ Nom complet *                │ Identifiant : [nom_complet]│
│ Courriel        │ [________________________]   │ Type : Texte              │
│ Nombre          │ Courriel professionnel *     │ Obligatoire : [Oui v]     │
│ Date            │ [________________________]   │ Aide : [______________]   │
│ Liste           │ Type de demande *            │ Validation : [_________]  │
│ Cases           │ [Sélectionner          v]   │ Visibilité : [Règle...]   │
│ Boutons radio   │ Description                  │ Confidentialité : [Interne]│
│ Fichier         │ [________________________]   │                           │
│ Matrice         │ [Précédent] [Suivant]        │ [Règles] [Permissions]    │
│ Calcul          │                              │                           │
├─────────────────┴──────────────────────────────┴───────────────────────────┤
│ Pages : [1 Informations] [2 Détails] [3 Confirmation] [+ Ajouter une page]│
│ [Valider] [Historique des versions] [Soumettre pour approbation]            │
└────────────────────────────────────────────────────────────────────────────┘
```

**Bibliothèque de champs** - texte court, texte long, courriel, URL et
hyperlien; - nombre entier, décimal, montant, pourcentage; - date et
date/heure; - case à cocher, groupe de cases, boutons radio et liste
déroulante; - téléversement de fichiers; - matrice/tableau; - champ
calculé, valeur dynamique et champ masqué; - champs de référence liés à
une autre fiche ou liste autorisée.

**Propriétés communes** - libellé, identifiant technique stable,
description et aide; - obligatoire/facultatif; - valeur par défaut et
format; - validation et message d'erreur; - condition de visibilité et
condition d'activation; - classification de confidentialité; -
permissions de lecture et de modification; - export autorisé ou
interdit; - ordre, groupe, page et répétition éventuelle.

**Fonctions de création** - formulaires à une ou plusieurs pages; -
aperçu ordinateur/tablette/mobile; - aperçu des réponses avant envoi; -
sauvegarde automatique du brouillon; - duplication, importation et
exportation de structure; - versionnage, comparaison, approbation et
retour à une version précédente; - publication planifiée ou limitée par
date, nombre de répondants ou plage réseau; - protection
anti-automatisation (par exemple mécanisme anti-bot adapté à
l'accessibilité); - limites de capacité et gestion des choix
disponibles; - relation parent-enfant entre formulaires et dossiers.

### Écran 4 --- Règles d'affaires et calculs

**Objectif :** configurer les comportements conditionnels avec un
éditeur lisible et testable.

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Règles du formulaire : Demande de service RH                 [+ Nouvelle]  │
├────────────────────────────────────────────────────────────────────────────┤
│ Nom de la règle : Afficher la section « Formation »                         │
│                                                                            │
│ SI    [Type de demande] [est égal à] [Formation]                            │
│ ALORS [Afficher] [Section Formation]                                       │
│ SINON [Masquer] [Section Formation]                                        │
│                                                                            │
│ Autres actions :                                                           │
│ [Rendre obligatoire] [Calculer valeur] [Définir valeur] [Valider]           │
│ [Déclencher notification] [Affecter un processus]                           │
│                                                                            │
│ Ordre de priorité [10]     Exécuter sur : [Changement de réponse v]          │
│ [Tester avec des données fictives] [Enregistrer] [Historique]               │
└────────────────────────────────────────────────────────────────────────────┘
```

**Capacités** - conditions combinant ET/OU et groupes de conditions; -
affichage/masquage et activation/désactivation de champs; - changement
de caractère obligatoire; - validation croisée de plusieurs réponses; -
formules de calcul et valeurs dynamiques; - contrôles de format, bornes
et règles de cohérence; - déclenchement d'événements lors de la
soumission ou de la modification d'une fiche; - gestion des conflits de
règles, ordre d'exécution et prévention des boucles; - tests unitaires
des règles avec jeux de données synthétiques; - journalisation de
l'exécution et des erreurs, sans exposer les valeurs sensibles aux rôles
non autorisés.

Les formules doivent être validées côté serveur. Une validation dans le
navigateur ne suffit pas pour protéger l'intégrité des données.

### Écran 5 --- Concepteur de workflow

**Objectif :** représenter les étapes d'un processus, les responsables
et les transitions.

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Processus : Demande d’approvisionnement             Version 2.1 [Publier]   │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                            │
│ [Soumission] → [Vérification] → [Approbation gestionnaire]                  │
│                                      │                                     │
│                              [Montant > seuil ?]                           │
│                                /             \                             │
│                             Oui               Non                          │
│                              ↓                 ↓                           │
│                     [Approbation finance]   [Traitement]                    │
│                              \                 /                           │
│                               → [Confirmation] → [Terminé]                  │
│                                                                            │
├────────────────────────────────────────────────────────────────────────────┤
│ Étape sélectionnée : Approbation gestionnaire                              │
│ Responsable : [Groupe gestionnaires v]   Délai cible : [3 jours]            │
│ Actions : [Approuver] [Refuser] [Retourner pour correction]                 │
│ Commentaire obligatoire en cas de refus : [Oui v]                          │
│ À l’échéance : [Rappel puis escalade v]                                     │
│ [Configurer les transitions] [Tester le processus]                          │
└────────────────────────────────────────────────────────────────────────────┘
```

**Fonctionnalités** - étapes séquentielles, conditionnelles et
parallèles si requises; - tâches assignées à un utilisateur, groupe ou
rôle; - approbation, refus, demande de correction et délégation
contrôlée; - délais, rappels et escalades; - règles de routage par
domaine, montant, type ou autres données; - gestion
d'inventaire/capacité et réservations temporaires pour inscriptions; -
historique des transitions et identité de l'acteur; - gestion des
erreurs, reprises et annulations; - tests de bout en bout avant
publication.

### Écran 6 --- Mes demandes (portail utilisateur)

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Mes demandes                                              [+ Nouvelle]     │
├────────────────────────────────────────────────────────────────────────────┤
│ Rechercher [________________] Statut [Tous v] Type [Tous v] [Filtrer]      │
│                                                                            │
│ Numéro      Demande                  Soumise le     Statut           Action │
│ RH-2026-104 Attestation              12 oct.       En traitement    [Ouvrir]│
│ RH-2026-119 Formation                15 oct.       À compléter      [Reprendre]│
│ AP-2026-052 Demande d’achat          16 oct.       En approbation   [Détails]│
│                                                                            │
│ Détail d’une demande :                                                     │
│ Statut actuel · Prochaine étape · Échéance · Historique · Documents         │
│ [Modifier si autorisé] [Ajouter un document] [Télécharger confirmation]    │
└────────────────────────────────────────────────────────────────────────────┘
```

**Règles** - un utilisateur ne voit que ses demandes et celles
explicitement partagées avec lui; - le statut et la prochaine étape sont
présentés en langage clair; - les actions disponibles dépendent du
statut et des droits; - les données confidentielles ne sont pas incluses
dans les notifications courriel par défaut; - les brouillons expirés
suivent une politique de conservation explicite.

### Écran 7 --- Tâches et approbations

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Tâches qui me sont assignées                                               │
├────────────────────────────────────────────────────────────────────────────┤
│ Priorité [Toutes v] Domaine [Tous v] Échéance [Toutes v] [Filtrer]          │
│                                                                            │
│ Dossier       Étape                  Reçu le       Échéance       Action   │
│ AP-2026-052   Approbation gestionnaire 16 oct.     20 oct.        [Examiner]│
│ RH-2026-097   Validation RH            14 oct.     19 oct.        [Examiner]│
│                                                                            │
│ Panneau d’examen (après sélection)                                         │
│ Résumé autorisé · Réponses · Pièces jointes · Historique                    │
│ Commentaire [________________________________________________________]     │
│ [Demander correction] [Refuser] [Approuver]                                 │
└────────────────────────────────────────────────────────────────────────────┘
```

Les approbations doivent enregistrer l'acteur, la date, la décision, les
commentaires et la version du dossier concernée. Les décisions
irréversibles ou sensibles peuvent nécessiter une confirmation
supplémentaire.

### Écran 8 --- Registre des données / fiches

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Données > Fiches                                                           │
├────────────────────────────────────────────────────────────────────────────┤
│ Domaine [RH v] Formulaire [Tous v] Statut [Tous v] Période [____ à ____]    │
│ Critères avancés [+ Ajouter un critère] [Enregistrer la recherche] [Chercher]│
│                                                                            │
│ Résultats : 124 fiches                    [Exporter la sélection]           │
│ ID          Formulaire           Statut          Date         Actions      │
│ RH-104      Attestation          Terminé         12 oct.      [Ouvrir]     │
│ RH-119      Formation            À compléter     15 oct.      [Ouvrir]     │
│                                                                            │
│ Fiche : [Résumé] [Réponses] [Documents] [Workflow] [Historique]             │
│ [Modifier selon droits] [Générer PDF] [Historique d’audit]                  │
└────────────────────────────────────────────────────────────────────────────┘
```

**Fonctionnalités** - recherche multicritère, filtres combinés et
recherches sauvegardées; - tri, pagination et affichage configurable des
colonnes; - droits au niveau de la fiche et des champs; - historique des
modifications; - export Excel/CSV selon autorisation et
classification; - export intégral structuré et documenté pour
réversibilité; - règles de conservation, gel légal si applicable et
suppression contrôlée; - traçabilité des exports et accès aux dossiers
sensibles.

### Écran 9 --- Rapports et tableaux de bord

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Rapports                                                                   │
├────────────────────────────────────────────────────────────────────────────┤
│ Rapport [Demandes par statut v] Période [30 derniers jours v] [Actualiser] │
│                                                                            │
│ ┌───────────────────────────────┐ ┌────────────────────────────────────┐  │
│ │ Volume par statut             │ │ Délai moyen de traitement           │  │
│ │ [Graphique configurable]      │ │ [Graphique configurable]            │  │
│ └───────────────────────────────┘ └────────────────────────────────────┘  │
│                                                                            │
│ Colonnes [Configurer] Filtres [Configurer] Visibilité [Droits]             │
│ [Enregistrer le rapport] [Exporter PDF] [Exporter Excel si autorisé]       │
└────────────────────────────────────────────────────────────────────────────┘
```

-   rapports personnalisés PDF générés à partir des données autorisées;
-   graphiques et tableaux de bord configurables;
-   filtres par domaine, période, statut et attributs autorisés;
-   pages et tableaux de bord visibles selon le rôle;
-   protection contre la divulgation indirecte de données par de petits
    groupes;
-   export et partage contrôlés, avec journalisation.

### Écran 10 --- Notifications et modèles de documents

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Notifications et modèles                                                   │
├────────────────────────────────────────────────────────────────────────────┤
│ Onglets : [Courriel] [SMS] [Messagerie interne] [Documents]                │
│                                                                            │
│ Déclencheur : [Formulaire soumis v]   Langue : [FR v]                      │
│ Destinataire : [Demandeur v]          Modèle : [Confirmation RH v]         │
│ Objet : [Confirmation de réception de votre demande]                       │
│ Corps : [Éditeur de modèle avec variables autorisées]                       │
│ Pièce jointe : [Document généré selon le modèle]                            │
│ [Envoyer un test fictif] [Enregistrer] [Activer après approbation]          │
└────────────────────────────────────────────────────────────────────────────┘
```

-   notifications déclenchées par soumission, changement d'état, tâche,
    échéance ou événement;
-   courriel, SMS si le fournisseur et les règles du CHU le permettent,
    et messagerie interne;
-   confirmation au répondant et avis aux administrateurs concernés;
-   modèles bilingues et variables contrôlées;
-   génération de PDF ou autre document à partir des réponses;
-   gestion des échecs, reprises, doublons et préférences de
    communication;
-   contenu minimal dans les messages externes, avec lien sécurisé vers
    le portail.

### Écran 11 --- Utilisateurs, groupes et permissions

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Sécurité > Utilisateurs et rôles                         [+ Créer un groupe]│
├────────────────────────────────────────────────────────────────────────────┤
│ Rechercher [________________] Domaine [Tous v] Rôle [Tous v] [Filtrer]     │
│                                                                            │
│ Groupes : Administrateurs | Concepteurs RH | Agents RH | Approvisionnement │
│           Gestionnaires | Auditeurs | Comptes de service                   │
│                                                                            │
│ Matrice d’accès                                                            │
│ Ressource                 Lire  Créer  Modifier  Supprimer  Exporter       │
│ Formulaires RH             ✓      ✓       ✓          —          —           │
│ Fiches RH confidentielles  ✓      —       ✓          —          —           │
│ Journaux d’audit           ✓      —       —          —          —           │
│                                                                            │
│ [Enregistrer] [Comparer avec les droits existants] [Consulter l’audit]     │
└────────────────────────────────────────────────────────────────────────────┘
```

**Contrôles** - authentification unique avec le fournisseur d'identité
du CHU; - protocoles normalisés compatibles avec l'architecture retenue,
par exemple OIDC ou SAML; - MFA obligatoire pour les accès
administratifs; - groupes, rôles et permissions au niveau application,
formulaire, champ, page, fiche, bouton et élément d'interface lorsque
nécessaire; - séparation des tâches et principe du moindre privilège; -
revue périodique des accès et retrait rapide des comptes inutiles; -
comptes de service distincts, secrets rotatifs et permissions
minimales; - règles de mots de passe seulement lorsque
l'authentification locale est nécessaire; privilégier l'identité
institutionnelle.

### Écran 12 --- Classification et protection des données

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Sécurité > Classification des données                                      │
├────────────────────────────────────────────────────────────────────────────┤
│ Niveau      Description                 Affichage     Export      Chiffrement│
│ Public      Diffusable publiquement     Autorisé      Autorisé    Au repos  │
│ Interne     Usage institutionnel        Rôles permis  Contrôlé    Au repos  │
│ Confidentiel Accès restreint             Masqué selon rôle Restreint Au repos│
│ Secret      Accès hautement restreint    Accès explicite Interdit par défaut│
│                                                                            │
│ Champ sélectionné : Numéro d’employé                                       │
│ Classification [Confidentiel v] Masquage [Activé] Export [Interdit]         │
│ Accès autorisés [Groupes...] Conservation [Politique...]                    │
│ [Enregistrer] [Vérifier les formulaires affectés]                           │
└────────────────────────────────────────────────────────────────────────────┘
```

La classification proposée est illustrative. Le CHU doit confirmer sa
nomenclature officielle et les règles applicables à chaque catégorie de
renseignements.

Fonctions : - classer les formulaires, fiches, champs, fichiers et
rapports; - masquer ou tronquer des valeurs selon le rôle; - limiter
consultation, modification, impression, téléchargement et export; -
chiffrer les données et gérer les clés de manière appropriée; - tracer
les accès et opérations sensibles; - contrôler les accès d'urgence selon
une procédure documentée; - appliquer les durées de conservation et la
suppression sécurisée; - soutenir l'évaluation des facteurs relatifs à
la vie privée et les obligations légales applicables.

### Écran 13 --- Intégrations et API

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Intégrations et API                                      [+ Nouvelle API]  │
├────────────────────────────────────────────────────────────────────────────┤
│ Intégration        État       Dernière exécution    Erreurs     Actions    │
│ Identité CHU       Connectée  Aujourd’hui           0           [Détails]  │
│ Système RH         Connectée  Aujourd’hui           1           [Journaux] │
│ Système achats     À vérifier Hier                  0           [Détails]  │
│                                                                            │
│ API : [Lire fiche] [Créer fiche] [Compléter fiche] [Consulter statut]       │
│ Authentification : [OAuth 2.0 / OIDC selon architecture v]                  │
│ Portées : [forms.read] [records.create] [records.update]                    │
│ Limites : [Débit] [Expiration du jeton] [Rotation des secrets]              │
│ [Tester] [Publier la configuration] [Voir la documentation OpenAPI]         │
└────────────────────────────────────────────────────────────────────────────┘
```

-   API versionnée pour consulter, récupérer, créer ou compléter des
    fiches;
-   authentification sécurisée par jetons ou mécanisme équivalent;
-   autorisation fine par portée et domaine;
-   validation côté serveur, limitation du débit et protection contre
    les abus;
-   journalisation des appels sans consigner les secrets ou données
    sensibles inutiles;
-   gestion des erreurs, idempotence lorsque pertinente, reprise et
    supervision;
-   documentation des schémas, limites, codes d'erreur et procédures de
    changement;
-   tests de non-régression pour préserver les échanges existants;
-   inventaire des automatisations et dépendances avant toute migration.

### Écran 14 --- Versions et publication

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Formulaire : Demande d’approvisionnement > Versions                        │
├────────────────────────────────────────────────────────────────────────────┤
│ Version   Auteur          Date        État            Commentaire          │
│ 2.1       Concepteur A    16 oct.     En approbation   Nouveau routage      │
│ 2.0       Concepteur B    10 oct.     En production    Version active       │
│ 1.9       Concepteur B    02 oct.     Archivée         Correction validation│
│                                                                            │
│ [Comparer les versions] [Prévisualiser] [Soumettre à approbation]           │
│ [Publier si autorisé] [Restaurer une version précédente]                    │
│                                                                            │
│ Checklist de publication :                                                 │
│ [ ] Validation des champs et règles                                        │
│ [ ] Tests d’accessibilité et multilingues                                   │
│ [ ] Tests du workflow et des notifications                                 │
│ [ ] Vérification des permissions et de la classification                   │
│ [ ] Approbation du propriétaire fonctionnel                                │
│ [ ] Plan de retour arrière                                                  │
└────────────────────────────────────────────────────────────────────────────┘
```

Le retour arrière doit restaurer une configuration connue sans écraser
silencieusement les données déjà soumises. Les changements de schéma
doivent être compatibles ou accompagnés d'un plan de migration.

### Écran 15 --- Environnements, exploitation et sauvegardes

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Administration > Environnements                                            │
├────────────────────────────────────────────────────────────────────────────┤
│ Environnement   Usage             Dernier déploiement   Accès               │
│ DEV             Développement     16 oct.               Équipe technique    │
│ UAT             Acceptation       15 oct.               Testeurs autorisés  │
│ PROD             Production        10 oct.               Accès restreint     │
│                                                                            │
│ Sauvegardes : Quotidiennes | Chiffrement | Copie externe | Tests restauration│
│ Disponibilité : Objectif 99,8 % | Incident actif : Aucun (exemple)          │
│ [Historique des déploiements] [Lancer une vérification] [Plan de reprise]   │
└────────────────────────────────────────────────────────────────────────────┘
```

-   promotion contrôlée DEV → UAT → PROD;
-   paramètres et secrets propres à chaque environnement;
-   approbation de déploiement en production;
-   sauvegardes quotidiennes redondantes locales et externes;
-   surveillance de la réussite des sauvegardes et tests de
    restauration;
-   plan de reprise, objectifs RPO/RTO à négocier et essais documentés;
-   suivi des mises à jour, correctifs de sécurité, migrations et
    incidents;
-   disponibilité mesurée et rapportée selon une méthode contractuelle
    définie.

### Écran 16 --- Journaux d'audit et incidents

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Sécurité > Audit                                                           │
├────────────────────────────────────────────────────────────────────────────┤
│ Période [____ à ____] Utilisateur [________] Action [Toutes v] [Rechercher] │
│                                                                            │
│ Date/heure       Acteur        Action            Ressource       Résultat   │
│ 16 oct. 09:14    Admin A       Permission modifiée Groupe RH      Succès     │
│ 16 oct. 09:32    Agent B       Fiche consultée    RH-104          Succès     │
│ 16 oct. 10:01    Service API   Lecture API        Fiche 8xx       Refusé     │
│                                                                            │
│ Détail : acteur, horodatage, ressource, action, résultat, contexte autorisé │
│ [Exporter le journal selon autorisation] [Créer une alerte]                │
└────────────────────────────────────────────────────────────────────────────┘
```

Les journaux doivent être protégés contre l'altération, accessibles
uniquement aux personnes autorisées et conservés selon une politique
approuvée. Éviter d'y enregistrer le contenu complet des formulaires,
les mots de passe, jetons ou secrets.

### Écran 17 --- Soutien et banque d'heures

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ Soutien et services professionnels                                         │
├────────────────────────────────────────────────────────────────────────────┤
│ Demandes de soutien                                                        │
│ Numéro  Objet                   Priorité  État          Responsable         │
│ INC-104 Échec d’export          Haute     En analyse    Soutien niveau 2    │
│ SR-208  Ajouter une règle       Normale   Planifiée     Services professionnels│
│                                                                            │
│ Banque d’heures                                                       │
│ Catégorie             Heures allouées   Utilisées   Restantes               │
│ Analyse               40                12          28                     │
│ Formation             24                8           16                     │
│ Configuration         60                20          40                     │
│ Programmation         80                31          49                     │
│ Rapports              24                6           18                     │
│                                                                            │
│ [Créer une demande] [Voir les interventions] [Exporter le relevé]          │
└────────────────────────────────────────────────────────────────────────────┘
```

Le soutien de deuxième ligne doit être disponible du lundi au vendredi,
de 8 h à 17 h, selon le fuseau horaire et les jours fériés à préciser au
contrat. La banque d'heures doit permettre de catégoriser les demandes,
faire approuver les travaux, saisir les heures et produire un relevé
vérifiable.

## 6. Catalogue fonctionnel par module

### 6.1 Formulaires

-   création multipage et gabarits réutilisables;
-   champs standards, matrices, fichiers et valeurs calculées;
-   aperçu avant soumission, brouillon et sauvegarde automatique;
-   activation selon date, quota ou plage réseau;
-   contrôle anti-bot accessible;
-   formulaires parent-enfant;
-   versions, approbation, publication et restauration;
-   import/export de structures.

### 6.2 Automatisation

-   règles conditionnelles, validations croisées et calculs;
-   événements déclenchés par soumission ou modification;
-   workflows, affectations, tâches, approbations et escalades;
-   capacités, quotas et inventaires;
-   tests de règles et suivi des erreurs.

### 6.3 Communications

-   courriel, SMS et messagerie interne;
-   confirmations et notifications aux administrateurs;
-   modèles bilingues;
-   génération de documents;
-   reprise en cas d'échec et journal des envois.

### 6.4 Données et rapports

-   recherche multicritère et filtres enregistrés;
-   fiches détaillées et historique;
-   export Excel/CSV et export intégral structuré;
-   PDF, graphiques et tableaux de bord;
-   conservation, archivage et suppression sécurisée.

### 6.5 Sécurité

-   SSO, MFA administratif et contrôle des sessions;
-   rôles, groupes, droits fins et séparation des tâches;
-   classification, masquage, chiffrement et restrictions d'export;
-   journalisation des accès et changements;
-   protection DDoS, limitation des tentatives de connexion, protection
    contre les logiciels malveillants;
-   soutien aux obligations de protection des renseignements personnels.

### 6.6 Interopérabilité

-   API versionnée et documentée;
-   authentification par jetons ou mécanisme équivalent;
-   droits par portée et domaine;
-   intégrations avec identité, RH, approvisionnement et autres
    systèmes;
-   tests de non-régression et suivi des automatisations existantes.

## 7. Exigences non fonctionnelles et critères d'acceptation

  -----------------------------------------------------------------------
  Domaine                 Exigence cible          Preuve / critère
                                                  d'acceptation
  ----------------------- ----------------------- -----------------------
  Disponibilité           99,8 % en tout temps    Rapport de
                          selon définition        disponibilité, méthode
                          contractuelle           de calcul, exclusions
                                                  et crédits de service

  Hébergement             Renseignements          Engagement contractuel,
                          personnels hébergés au  architecture et
                          Québec                  inventaire des lieux de
                                                  stockage/sauvegarde

  Environnements          DEV, UAT et PROD        Démonstration des
                          séparés                 frontières d'accès,
                                                  secrets et déploiements
                                                  distincts

  Sauvegardes             Quotidiennes,           Journaux de sauvegarde
                          redondantes locales et  et preuve de tests de
                          externes                restauration

  Utilisateurs            Pas de limite imposée   Confirmation
                          au nombre               contractuelle de
                          d'utilisateurs selon le licence et test de
                          besoin                  charge convenu

  Langues                 Administration FR/EN et Parcours complet testé
                          formulaires             dans les deux langues
                          multilingues            

  Accessibilité           Respect des exigences   Audit selon le standard
                          applicables du          applicable et
                          gouvernement du Québec  correction des écarts

  Sécurité                SSO, MFA admin,         Tests fonctionnels,
                          permissions fines,      revue de configuration
                          chiffrement et audit    et preuves de sécurité

  API                     Lecture, récupération,  Tests d'intégration
                          création et mise à jour automatisés et
                          des fiches selon droits documentation

  Réversibilité           Export intégral des     Export de test, schémas
                          données dans un format  documentés et procédure
                          exploitable             de récupération

  Migration               Continuité des échanges Inventaire, tests de
                          et automatisations      non-régression et
                          existantes              approbation du CHU

  Soutien                 Deuxième ligne du lundi SLA, canaux de contact,
                          au vendredi, 8 h--17 h  escalade et rapports de
                                                  soutien

  Test d'intrusion        Test du portail RH par  Rapport, plan de
                          une firme externe       correction et
                          spécialisée             validation de fermeture
                                                  des constats
  -----------------------------------------------------------------------

### Points à contractualiser explicitement

-   définition de la disponibilité, fenêtres de maintenance et
    exclusions;
-   délais de prise en charge et de résolution selon la sévérité;
-   RPO/RTO, durée de rétention des sauvegardes et fréquence des tests;
-   localisation des données, sauvegardes, journaux et accès de soutien;
-   propriété des données, droits d'export, coûts de sortie et
    assistance à la réversibilité;
-   périmètre du test d'intrusion, indépendance du tiers et traitement
    des constats;
-   capacité, limites d'API, volumes de fichiers et performances
    attendues;
-   définition des licences couvrant les deux secteurs et quatre
    applications Web existantes.

## 8. Accessibilité, langues et design system

-   interface en français et en anglais, avec contenu de formulaire
    traduisible séparément;
-   langage clair, messages d'erreur explicites et instructions placées
    près des champs;
-   navigation complète au clavier, focus visible et ordre logique;
-   libellés accessibles, titres hiérarchisés, tableaux correctement
    structurés et contraste suffisant;
-   ne pas communiquer un état uniquement par la couleur;
-   zoom et affichage mobile sans perte d'information ni défilement
    horizontal inutile;
-   temporisation de session annoncée et mécanisme de prolongation
    lorsqu'approprié;
-   gabarits de marque configurables sans compromettre l'accessibilité;
-   tests automatisés et manuels avec technologies d'assistance.

Le référentiel d'accessibilité applicable doit être confirmé avec le CHU
et les normes gouvernementales en vigueur au moment de l'acceptation.

## 9. Migration et mise en œuvre recommandées

1.  **Découverte** : inventorier les deux secteurs, quatre applications,
    formulaires, fiches, intégrations, automatisations, rôles et
    rapports.
2.  **Analyse de risques** : classifier les données, documenter les
    obligations de confidentialité, établir les exigences de résidence
    et réaliser les analyses nécessaires.
3.  **Conception** : valider les menus, rôles, modèle de données,
    architecture, intégrations, workflows et règles de conservation.
4.  **Socle technique** : configurer l'identité, les environnements,
    sauvegardes, supervision, journaux et contrôles de sécurité.
5.  **Prototype** : réaliser un formulaire RH et un processus
    d'approvisionnement représentatifs; valider les parcours et
    l'accessibilité.
6.  **Migration** : exporter les configurations, convertir les éléments
    nécessaires et migrer les données selon un plan approuvé.
7.  **Tests** : tests fonctionnels, sécurité, permissions,
    accessibilité, intégration, charge, sauvegarde/restauration et
    non-régression.
8.  **Acceptation** : faire exécuter les scénarios UAT par les
    représentants du CHU; obtenir les approbations fonctionnelles et
    techniques.
9.  **Mise en production** : plan de déploiement, communication,
    surveillance renforcée et procédure de retour arrière.
10. **Exploitation** : soutien niveau 2, correctifs, mises à jour, tests
    périodiques et amélioration continue.

### Scénarios de test minimums

-   formulaire multipage avec champ obligatoire conditionnel;
-   sauvegarde d'un brouillon et reprise sur un autre appareil;
-   quota de capacité atteint et fermeture automatique du formulaire;
-   approbation, refus, correction, rappel et escalade;
-   utilisateur non autorisé tentant d'accéder à une fiche ou un champ
    confidentiel;
-   export bloqué pour un rôle sans permission;
-   appel API avec jeton valide, expiré et insuffisamment autorisé;
-   restauration de sauvegarde;
-   migration d'une configuration sans rupture des intégrations;
-   test mobile, clavier, lecteur d'écran et parcours FR/EN.

## 10. Indicateurs opérationnels recommandés

-   disponibilité mensuelle et durée des incidents;
-   taux de réussite des sauvegardes et résultats des restaurations;
-   volume de formulaires actifs et de dossiers par statut;
-   temps moyen de traitement et tâches en retard;
-   taux d'échec des notifications et des appels API;
-   changements publiés, retours arrière et échecs de déploiement;
-   incidents de sécurité et délai de correction;
-   demandes de soutien, temps de prise en charge et consommation de la
    banque d'heures.

Les indicateurs de sécurité et de performance doivent éviter d'exposer
des renseignements personnels et être visibles uniquement selon les
autorisations.

## 11. Décisions à obtenir du CHU avant la conception détaillée

-   fournisseur d'identité et protocoles SSO à utiliser;
-   nomenclature officielle de classification et matrice de
    conservation;
-   référentiel d'accessibilité applicable et critères de vérification;
-   systèmes RH, approvisionnement et autres systèmes à intégrer;
-   inventaire exact des quatre applications et dépendances existantes;
-   volumes actuels et attendus, tailles de fichiers et exigences de
    performance;
-   RPO/RTO, niveaux de service et définition de la disponibilité;
-   règles de rétention, archivage, suppression et gel légal;
-   politique d'envoi de SMS et de messagerie interne;
-   responsabilités d'approbation et séparation des tâches;
-   exigences de test d'intrusion et traitement des vulnérabilités;
-   modalités de licence, banque d'heures, soutien et réversibilité.

## 12. Conclusion

La solution à mettre en place est une plateforme d'entreprise de
formulaires, de gestion de données et d'automatisation des processus.
Son interface doit combiner un portail simple pour les répondants et une
console d'administration complète, organisée par applications,
formulaires, processus, données, rapports, sécurité, intégrations et
exploitation.

La réussite ne dépend pas uniquement de la présence des fonctionnalités
dans les menus. Elle dépend de leur mise en œuvre sécurisée, du
cloisonnement réel des données, de la qualité des migrations, de la
validation des processus existants, de l'accessibilité, de la
réversibilité et de critères d'acceptation mesurables.


---

# 13. Détail fonctionnel du portail Espace RH

Cette section complète le portail utilisateur avec un espace Ressources humaines détaillé. Les types de demandes ci-dessous sont des exemples de conception : le CHU devra confirmer les démarches réellement offertes, leurs règles, les documents exigés et les circuits d’approbation.

## 13.1 Accueil de l’Espace RH

**Objectif :** offrir à l’employé une vue personnelle et sécurisée de ses démarches RH, de ses tâches et de ses documents disponibles.

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ CHU de Québec–Université Laval       Espace RH        Aide  FR | EN  Profil │
├──────────────────────────────────────────────────────────────────────────────┤
│ Accueil RH | Nouvelle demande | Mes demandes | Mes documents | Mes tâches   │
├──────────────────────────────────────────────────────────────────────────────┤
│ Bonjour, [Prénom]                                                           │
│ Que souhaitez-vous faire aujourd’hui ?                                      │
│                                                                              │
│ [Créer une demande RH] [Consulter mes documents] [Suivre une demande]        │
│                                                                              │
│ ┌──────────────────────┐ ┌──────────────────────┐ ┌───────────────────────┐ │
│ │ Brouillons           │ │ En traitement        │ │ Action requise        │ │
│ │          2           │ │          4           │ │          1            │ │
│ └──────────────────────┘ └──────────────────────┘ └───────────────────────┘ │
│                                                                              │
│ Mes demandes récentes                                                       │
│ ID        Type de demande       Date       État              Prochaine action│
│ RH-104    Attestation           12 oct.    En traitement     Consulter       │
│ RH-119    Formation             15 oct.    À compléter       Reprendre       │
│                                                                              │
│ Échéances et messages                                                       │
│ • Une pièce justificative est attendue pour RH-119                           │
│ • Votre document RH-087 est disponible dans Mes documents                    │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Fonctions de l’accueil
- afficher uniquement les dossiers et documents accessibles à l’utilisateur connecté;
- proposer des raccourcis vers les démarches fréquemment utilisées;
- présenter les brouillons, demandes soumises, dossiers en cours et demandes nécessitant une action;
- afficher les échéances, demandes de complément et documents nouvellement disponibles;
- afficher les avis de maintenance et les informations RH générales sans mélanger ces contenus aux renseignements confidentiels;
- permettre le changement de langue FR/EN;
- adapter la présentation aux ordinateurs, tablettes et téléphones;
- éviter d’afficher des détails sensibles sur un écran partagé ou dans les notifications.

## 13.2 Menu détaillé de l’Espace RH

```text
ESPACE RH
├── Accueil RH
├── Nouvelle demande
│   ├── Attestation ou document RH
│   ├── Mise à jour de renseignements
│   ├── Formation et perfectionnement
│   ├── Demande administrative RH
│   ├── Demande de congé ou d’absence (si couvert par le processus)
│   └── Autres formulaires publiés par les RH
├── Mes demandes
│   ├── Brouillons
│   ├── Soumises
│   ├── En traitement
│   ├── À compléter
│   ├── Approuvées
│   ├── Refusées
│   └── Terminées / Annulées
├── Mes tâches
│   ├── Informations à fournir
│   ├── Documents à déposer
│   └── Confirmations attendues
├── Mes documents
│   ├── Documents disponibles
│   ├── Documents générés par le portail
│   └── Documents fournis par l’employé
├── Mes approbations (uniquement si rôle d’approbateur)
├── Messages et notifications
└── Aide RH et soutien
```

Le catalogue des démarches doit être piloté par configuration : les administrateurs RH peuvent publier ou retirer un type de formulaire, définir son audience, sa période de disponibilité, ses instructions et son processus de traitement sans modifier le code de l’application.

## 13.3 Catalogue « Nouvelle demande RH »

**Objectif :** aider l’employé à choisir le bon formulaire et à comprendre ce qui sera demandé avant de commencer.

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Nouvelle demande                                                │
├──────────────────────────────────────────────────────────────────────────────┤
│ Rechercher une démarche [________________________] [Rechercher]             │
│ Catégorie [Toutes v]       Pour qui [Moi v]                                  │
│                                                                              │
│ ┌─────────────────────────────┐ ┌─────────────────────────────────────────┐ │
│ │ Attestation ou document RH  │ │ Formation et perfectionnement           │ │
│ │ Demander un document selon  │ │ Soumettre une demande de formation       │ │
│ │ les options disponibles.   │ │ et joindre les pièces requises.          │ │
│ │ Délai indicatif : affiché   │ │ Approbation : selon les règles RH        │ │
│ │ [Voir les détails]          │ │ [Voir les détails]                      │ │
│ └─────────────────────────────┘ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────┐ ┌─────────────────────────────────────────┐ │
│ │ Mise à jour de renseignements│ │ Autre demande administrative            │ │
│ │ Signaler une correction     │ │ Choisir une démarche publiée par les RH  │ │
│ │ autorisée.                  │ │ [Voir les détails]                      │ │
│ │ [Voir les détails]          │ │                                         │ │
│ └─────────────────────────────┘ └─────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Fiche de présentation d’une démarche
Chaque type de demande présente :
- nom et description en langage clair;
- personnes admissibles et éventuelles conditions d’accès;
- renseignements et pièces à préparer;
- étapes prévues et rôles impliqués, dans la mesure où leur divulgation est appropriée;
- délai indicatif, échéance et période de disponibilité;
- renseignements personnels requis et raison de leur collecte, selon les règles du CHU;
- canal de soutien;
- bouton **Commencer** et, si pertinent, bouton **Enregistrer pour plus tard**.

Un délai affiché doit être qualifié d’indicatif à moins qu’un engagement de service formel soit défini.

## 13.4 Formulaire de demande RH — parcours détaillé

Le formulaire peut être multipage et doit être configuré par type de démarche. Exemple générique :

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Nouvelle demande > Demande administrative                      │
│ Étapes :  1. Demandeur  ──  2. Détails  ──  3. Documents  ──  4. Vérification│
├──────────────────────────────────────────────────────────────────────────────┤
│ Étape 1 — Vos renseignements                                                │
│                                                                              │
│ Nom complet             [Prérempli depuis une source autorisée, si possible]│
│ Identifiant employé     [Masqué / affiché selon le besoin et les droits]     │
│ Courriel institutionnel [Prérempli, lecture seule si applicable]             │
│ Direction / unité       [Valeur autorisée v]                                 │
│                                                                              │
│ Étape 2 — Détails de la demande                                             │
│ Type de demande *       [Sélectionner v]                                     │
│ Date d’effet *          [AAAA-MM-JJ]                                         │
│ Motif / précisions      [_______________________________________________]    │
│                                                                              │
│ Étape 3 — Documents justificatifs                                           │
│ Fichier                  [Choisir un fichier]                                │
│ Formats autorisés : [configurés]  Taille maximale : [configurée]             │
│                                                                              │
│ Étape 4 — Vérification                                                      │
│ Résumé des réponses et pièces jointes                                        │
│ [Modifier] [Enregistrer le brouillon] [Soumettre la demande]                 │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Comportement attendu
- préremplir seulement les données provenant d’une source fiable, autorisée et nécessaire;
- indiquer clairement les champs obligatoires et facultatifs;
- afficher ou masquer des questions selon les réponses précédentes;
- valider les formats, les dates, les valeurs et les cohérences avant la soumission;
- présenter des messages d’erreur près du champ concerné et conserver les réponses déjà saisies;
- enregistrer automatiquement le brouillon et afficher la dernière sauvegarde;
- permettre de quitter puis reprendre un brouillon tant que la politique de conservation l’autorise;
- fournir un aperçu final des réponses et fichiers;
- demander une confirmation de soumission;
- créer un identifiant de dossier et afficher un accusé de réception;
- envoyer les notifications sans inclure inutilement de données sensibles;
- enregistrer la version du formulaire utilisée pour permettre l’audit et la compréhension des réponses.

### États d’un formulaire et d’une demande
- **Brouillon** : pas encore soumis; modifiable par le demandeur.
- **Soumise** : réception confirmée; modifications directes désactivées, sauf action explicitement permise.
- **En traitement** : assignée à une équipe ou à un intervenant.
- **À compléter** : une information ou une pièce est attendue du demandeur.
- **En approbation** : attente d’une décision d’un responsable.
- **Approuvée** : décision positive enregistrée; les étapes suivantes peuvent continuer.
- **Refusée** : décision négative avec motif communiqué selon les règles.
- **Terminée** : toutes les étapes prévues sont complétées.
- **Annulée** : demande annulée selon les permissions et règles du processus.

Le catalogue peut utiliser d’autres statuts si le processus RH l’exige. Les transitions autorisées doivent être définies dans le workflow et non laissées à une modification libre du statut.

## 13.5 Écran « Mes demandes RH »

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Mes demandes                                                   │
├──────────────────────────────────────────────────────────────────────────────┤
│ Rechercher [Numéro ou titre________________]                                │
│ Statut [Tous v] Catégorie [Toutes v] Date de soumission [________] [Filtrer]│
│                                                                              │
│ ID       Type                 Soumise le   Statut            Action          │
│ RH-104   Attestation          12 oct.      En traitement     [Détails]       │
│ RH-119   Formation            15 oct.      À compléter       [Compléter]     │
│ RH-121   Mise à jour dossier  17 oct.      Brouillon         [Reprendre]     │
│                                                                              │
│ [Voir les détails] [Télécharger l’accusé]                                   │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Fonctions
- filtres par statut, catégorie, période et numéro;
- tri et pagination;
- distinction visuelle accessible des états, avec libellé textuel et non seulement couleur;
- accès à la fiche détaillée;
- reprise d’un brouillon;
- réponse à une demande de complément;
- annulation si le processus et le rôle le permettent;
- téléchargement de l’accusé de réception et des documents autorisés;
- affichage du statut, de la dernière mise à jour, de la prochaine action et d’une échéance si applicable.

## 13.6 Détail d’une demande RH et suivi du dossier

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Mes demandes > RH-119                                            │
├──────────────────────────────────────────────────────────────────────────────┤
│ Demande de formation                           Statut : À compléter          │
│ Soumise le : 15 octobre 2026                  Dernière mise à jour : 17 oct. │
│                                                                              │
│ Progression : Soumise → Vérification → Complément demandé → Approbation      │
│                                                                              │
│ Prochaine action requise                                                    │
│ Veuillez fournir le document demandé avant la date indiquée dans le dossier.│
│ [Ajouter le document] [Répondre au message]                                  │
│                                                                              │
│ Onglets : [Résumé] [Réponses] [Documents] [Messages] [Historique]            │
│                                                                              │
│ Résumé : type de demande, date de soumission, statut et prochaines étapes    │
│ Documents : fichiers déposés et documents générés, selon les droits          │
│ Messages : échanges autorisés liés au dossier                                │
│ Historique : date, étape, statut et action réalisée                           │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Onglets du dossier
- **Résumé** : numéro, type, dates, statut et prochaine action.
- **Réponses** : réponses soumises, présentées en lecture seule après soumission sauf processus de correction.
- **Documents** : pièces déposées, documents générés, dates et disponibilité.
- **Messages** : demandes de précision et réponses rattachées au dossier.
- **Historique** : événements et changements de statut visibles au demandeur.
- **Approbations** : résumé des étapes visibles, sans révéler les commentaires confidentiels ou informations internes non destinées au demandeur.

Le système doit conserver l’historique des actions pour audit, mais le portail ne montre que les informations destinées à l’employé. Les notes internes RH doivent être séparées des messages destinés au demandeur.

## 13.7 Écran « Mes documents »

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Mes documents                                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│ Rechercher [________________________] Type [Tous v] Période [________]      │
│                                                                              │
│ Document                    Associé à       Date       Disponibilité Action │
│ Attestation générée         RH-087          10 oct.    Disponible    [Ouvrir]│
│ Confirmation de demande     RH-104          12 oct.    Disponible    [PDF]  │
│ Pièce déposée               RH-119          15 oct.    En traitement [Détails]│
│                                                                              │
│ [Télécharger la sélection si autorisé]                                      │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Fonctionnalités documentaires
- distinguer les documents fournis par l’employé, générés par la plateforme et mis à disposition par les RH;
- afficher le nom, le type, la date, le dossier associé et le statut;
- ouvrir ou télécharger un fichier seulement après vérification des permissions;
- permettre le téléversement sécurisé de pièces requises;
- valider les extensions, types MIME, tailles et noms de fichiers;
- analyser les fichiers téléversés contre les logiciels malveillants;
- stocker les documents dans un dépôt protégé, sans exposition par URL publique permanente;
- utiliser des liens temporaires ou une session autorisée pour le téléchargement;
- enregistrer les opérations de consultation, génération, remplacement et téléchargement lorsque requis;
- appliquer la conservation, l’archivage et la suppression selon la politique RH;
- générer des documents à partir de modèles approuvés, avec version du modèle et date de génération;
- contrôler l’impression et l’export lorsque la classification l’exige.

Les documents relatifs à la paie, à l’emploi, à la santé ou à d’autres catégories sensibles ne doivent être proposés que si le périmètre fonctionnel et les autorisations du CHU les couvrent expressément.

## 13.8 Tâches RH et demandes de complément

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Mes tâches                                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│ Tâches ouvertes : 2                                                        │
│                                                                              │
│ Dossier   Tâche                         Échéance      Action                │
│ RH-119    Ajouter une pièce justificative 22 oct.     [Compléter]           │
│ RH-104    Confirmer les renseignements     24 oct.     [Vérifier]            │
│                                                                              │
│ Détail de la tâche                                                          │
│ Instruction : [texte précis fourni par l’équipe RH]                          │
│ Champ / pièce demandée : [description]                                       │
│ [Joindre un fichier] [Répondre] [Soumettre le complément]                    │
└──────────────────────────────────────────────────────────────────────────────┘
```

- l’équipe RH peut demander un complément depuis le dossier;
- la demande de complément doit préciser ce qui manque, pourquoi et, si applicable, l’échéance;
- le demandeur reçoit une notification discrète et un lien sécurisé vers le dossier;
- les réponses et nouvelles pièces sont rattachées à la version du dossier;
- l’équipe RH est avisée du dépôt;
- le dossier reprend son workflow après validation;
- le système conserve l’historique des demandes de complément et des réponses;
- les rappels et escalades suivent des règles configurées.

## 13.9 Approbations RH pour les gestionnaires

Cet écran n’est visible qu’aux utilisateurs auxquels un rôle d’approbateur a été attribué.

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│ Espace RH > Mes approbations                                               │
├──────────────────────────────────────────────────────────────────────────────┤
│ Filtre [En attente v] Type [Tous v] Échéance [Toutes v] [Filtrer]           │
│                                                                              │
│ Dossier   Type                  Date reçue  Échéance   Action                │
│ RH-132    Formation             16 oct.     23 oct.    [Examiner]            │
│ RH-138    Demande administrative17 oct.     24 oct.    [Examiner]            │
│                                                                              │
│ Panneau d’examen                                                            │
│ Données nécessaires à la décision, documents autorisés et historique        │
│ Décision : [Approuver] [Refuser] [Retourner pour correction]                 │
│ Commentaire [___________________________________________________________]   │
│ [Confirmer la décision]                                                      │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Règles d’approbation
- n’afficher que les dossiers assignés au gestionnaire ou à son groupe;
- appliquer la séparation des tâches et empêcher l’auto-approbation lorsque requise;
- présenter uniquement les données nécessaires à la décision;
- permettre approbation, refus ou retour pour correction selon le workflow;
- exiger un motif pour les décisions définies comme sensibles;
- enregistrer l’identité, la date, la décision, le commentaire et la version du dossier;
- notifier le demandeur du changement de statut avec un message adapté;
- appliquer les délais, rappels, délégations et escalades configurés;
- prévoir un mécanisme de réaffectation autorisé en cas d’absence ou de changement de poste.

## 13.10 Messages et notifications de l’Espace RH

### Centre de notifications
- liste des notifications non lues et lues;
- filtres par date et catégorie;
- lien sécurisé vers le dossier concerné;
- préférences de communication compatibles avec les obligations du CHU;
- indication claire des notifications système et des messages d’un agent RH.

### Événements pouvant déclencher une notification
- brouillon soumis avec succès;
- demande reçue et numéro de dossier attribué;
- changement de statut;
- complément requis;
- échéance prochaine ou dépassée;
- décision d’approbation ou de refus;
- document prêt à être consulté;
- tâche attribuée à un gestionnaire;
- échec de traitement nécessitant une intervention.

Les notifications externes doivent limiter les renseignements personnels. Elles peuvent indiquer qu’une action est requise et fournir un lien sécurisé, sans inclure le détail du dossier ou la pièce jointe sensible par défaut.

## 13.11 Aide et soutien RH

L’écran d’aide doit fournir :
- foire aux questions sur les démarches offertes;
- instructions propres à chaque formulaire;
- explication des statuts et des étapes;
- procédure en cas de problème de connexion, de téléversement ou de soumission;
- coordonnées ou mécanisme de contact officiel validé par le CHU;
- numéro de référence à communiquer au soutien;
- indication de l’état du service en cas d’incident, si cette information est publiée.

Ne pas afficher de coordonnées, délais ou engagements de service fictifs : ils doivent être configurés à partir des informations officielles du CHU.

## 13.12 Permissions de l’Espace RH

| Fonction | Employé demandeur | Gestionnaire approbateur | Agent RH | Administrateur RH |
|---|---|---|---|---|
| Voir le catalogue RH | Oui, selon admissibilité | Oui, selon admissibilité | Oui | Oui |
| Créer un brouillon | Oui | Oui, si autorisé | Si le processus le prévoit | Selon rôle |
| Voir ses propres demandes | Oui | Oui | Oui | Selon rôle |
| Voir les demandes d’autrui | Non, sauf partage explicite | Dossiers assignés seulement | Dossiers autorisés | Selon rôle et besoin |
| Ajouter un complément | Dossiers qui lui appartiennent | Selon workflow | Au nom du dossier si permis | Selon rôle |
| Approuver une demande | Non, sauf autre rôle séparé | Dossiers assignés | Selon délégation | Ne pas présumer de ce droit |
| Voir / télécharger un document | Documents autorisés | Documents nécessaires à la décision | Documents du dossier autorisé | Selon rôle |
| Modifier un formulaire publié | Non | Non | Non, sauf rôle concepteur | Concepteurs autorisés |
| Exporter des données | Non par défaut | Non par défaut | Selon besoin et permission | Permission explicite |
| Voir les journaux d’audit | Non | Non | Selon délégation | Rôle sécurité/audit autorisé |

Cette matrice est indicative. Les permissions effectives doivent être validées par le CHU et appliquées côté serveur, y compris pour les API et les téléchargements de fichiers. Masquer un bouton dans l’interface n’est pas un contrôle d’accès suffisant.

## 13.13 Exigences de sécurité et de confidentialité propres au portail RH

- authentification institutionnelle et gestion de session sécurisée;
- MFA pour les accès administratifs et les autres accès à risque selon la politique du CHU;
- protection contre l’accès direct à un dossier en modifiant son identifiant dans l’URL;
- vérification des permissions sur chaque lecture, modification, téléchargement et appel API;
- masquage des identifiants et renseignements confidentiels dans les listes et tableaux de bord;
- chiffrement en transit et au repos;
- protection des pièces jointes contre les fichiers malveillants;
- journalisation des accès et actions sensibles;
- limitation des tentatives de connexion, protection anti-bot et protection DDoS;
- expiration des liens temporaires et absence d’URL publique permanente pour les documents confidentiels;
- contrôle de l’exportation, de l’impression et du partage;
- règles explicites de conservation, d’archivage et de suppression;
- séparation des notes internes RH et des messages destinés à l’employé;
- tests de sécurité et test d’intrusion externe du portail RH, avec correction et vérification des constats.

## 13.14 Critères d’acceptation du portail Espace RH

1. Un employé authentifié ne peut consulter que ses demandes et les documents autorisés.
2. L’employé peut démarrer un formulaire, enregistrer un brouillon, quitter et reprendre sans perdre les réponses sauvegardées.
3. Les champs conditionnels, validations croisées et messages d’erreur fonctionnent côté interface et côté serveur.
4. Avant la soumission, un écran de vérification présente les réponses et les pièces jointes.
5. Après la soumission, un numéro de dossier et une confirmation sont générés.
6. Le demandeur peut suivre le statut, l’historique visible et la prochaine action requise.
7. L’agent RH peut demander un complément et recevoir une notification lorsque le demandeur répond.
8. Les gestionnaires ne voient que les approbations qui leur sont assignées et ne peuvent approuver en dehors des règles configurées.
9. Un document confidentiel ne peut être téléchargé par un utilisateur non autorisé, même avec un lien direct.
10. Les documents générés utilisent un modèle approuvé et sont rattachés au dossier concerné.
11. Les notifications ne divulguent pas le contenu sensible des demandes.
12. Les parcours clés sont testés en français et en anglais, sur ordinateur et appareil mobile.
13. Les écrans sont vérifiés au clavier et avec les technologies d’assistance pertinentes.
14. Les accès, décisions, téléchargements et modifications sensibles sont consignés selon la politique d’audit.
15. Les données et documents suivent les règles de conservation et peuvent être exportés ou supprimés conformément aux politiques approuvées.
16. Les tests de non-régression démontrent que les workflows et intégrations RH existants fonctionnent après migration ou mise à jour.

## 13.15 Parcours utilisateur de référence

**Scénario : l’employé soumet une demande RH avec une pièce justificative.**

1. L’employé se connecte au portail institutionnel.
2. Il ouvre l’Espace RH et choisit une démarche dans le catalogue.
3. Il lit les conditions, les documents requis et les étapes prévues.
4. Il remplit le formulaire; les champs pertinents s’affichent selon ses réponses.
5. Le système sauvegarde le brouillon automatiquement.
6. L’employé joint les pièces; le système vérifie le type, la taille et la sécurité des fichiers.
7. Il consulte l’aperçu et corrige les erreurs signalées.
8. Il soumet la demande et reçoit un numéro de dossier.
9. Le moteur de processus affecte le dossier à l’équipe ou au gestionnaire approprié.
10. Si un complément est requis, l’employé reçoit un avis et répond dans son dossier sécurisé.
11. Le gestionnaire ou l’agent RH traite le dossier et enregistre sa décision.
12. L’employé consulte le statut final et, si un document est généré, le télécharge depuis **Mes documents**.
13. Les événements requis sont inscrits au journal d’audit et la demande suit la politique de conservation.

