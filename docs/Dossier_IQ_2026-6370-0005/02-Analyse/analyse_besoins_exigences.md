# Analyse des besoins et exigences — IQ 2026-6370-0005

## 1. Synthèse exécutive
Investissement Québec recherche une solution commercialisée de génération de contrats et de signature électronique, hébergée en mode infonuagique, intégrable aux applications SaaS et aux applications internes par API. L'intégration explicitement nommée est Salesforce Grantmaking (Salesforce Public Sector Solutions), système maître des dossiers d'aide financière, demandes, analyses, approbations, ententes et programmes financiers.

La solution doit permettre la récupération des données sources, le préremplissage de modèles, la génération documentaire, les validations et approbations préalables, les signatures, les notifications, le suivi centralisé, la conservation des versions, des preuves et de la piste d'audit, puis le retour des statuts pertinents vers Salesforce.

## 2. Objectifs d'affaires (SRC)
- Fluidifier et fiabiliser la production des contrats et ententes.
- Réduire les saisies manuelles par préremplissage depuis les systèmes sources.
- Orchestrer validations, approbations et signatures en séquence, en parallèle ou en mode mixte.
- Réduire les délais, interventions manuelles et dépendances inutiles.
- Assurer la traçabilité, l'intégrité et la valeur probante des documents signés.
- Offrir une expérience simple aux initiateurs, signataires internes et signataires externes.
- Maintenir la flexibilité des modèles et workflows pendant la durée contractuelle.

## 3. Acteurs et rôles métier
| Acteur | Responsabilités attendues |
|---|---|
| Initiateur | Créer une trousse, ajouter documents, participants et paramètres; lancer le processus; suivre le dossier. |
| Validateur | Consulter, commenter, valider, rejeter ou retourner pour correction. |
| Approbateur | Approuver, rejeter ou demander correction avant signature. |
| Signataire interne | Authentifier son identité et signer selon l'ordre défini. |
| Signataire externe | Accéder à l'invitation, consulter, consentir et signer selon le niveau de vérification requis. |
| Administrateur d'affaires | Administrer modèles, clauses, workflows, groupes et règles fonctionnelles. |
| Administrateur technique | Administrer accès, rôles, intégrations, environnements, sécurité, API et journaux techniques. |
| Auditeur / conformité | Consulter l'historique, les preuves, certificats et événements autorisés. |
| Système source Salesforce | Fournir données et métadonnées; recevoir les statuts et références de documents. |
| Exploitant / support | Surveiller, traiter incidents, maintenir la plateforme et assurer la continuité. |

## 4. Exigences fonctionnelles consolidées
Les identifiants `F-xx` sont des identifiants de traçabilité créés pour ce dossier; ils ne sont pas des numéros d'exigence officiels du DAO.

### F-01 — Création et gestion des trousses
- Créer un dossier ou une trousse de signature.
- Ajouter un ou plusieurs documents PDF ou Word.
- Supporter le téléversement minimal de 25 Mo (SRC).
- Associer intervenants, documents, métadonnées et paramètres du processus.
- Gérer plusieurs documents au sein d'une même trousse.
- Consulter l'état et l'historique des transactions.

### F-02 — Génération automatisée de contrats
- Récupérer automatiquement données et métadonnées depuis Salesforce Grantmaking.
- Générer contrats, ententes et documents à partir de modèles approuvés.
- Préremplir les champs à partir des données source.
- Appliquer clauses et règles conditionnelles configurées.
- Produire une version préliminaire; permettre révision/correction contrôlée.
- Gérer versions et traçabilité entre dossier source, document généré et preuve signée.
- Acheminer le document généré dans les workflows de validation, approbation et signature.

### F-03 — Modèles documentaires
- Créer et réutiliser des modèles.
- Partager les modèles par groupes d'usagers.
- Positionner précisément les champs dans le document.
- Gérer les champs et paramètres autorisés par rôle.
- Gérer le cycle de vie et les versions des modèles (proposition nécessaire pour gouvernance).

### F-04 — Champs et saisie
- Types : texte avec validation, liste de valeurs, date, case à cocher.
- Distinguer visuellement champs de formulaire, saisie et signature.
- Attribuer les champs à des signataires précis.
- Appliquer des règles conditionnelles d'affichage.
- Proposer une saisie assistée / suggestions automatiques.

### F-05 — Validation et approbation
- Workflows séquentiels, parallèles et mixtes.
- Affectation selon rôles, groupes ou règles d'affaires.
- Ajouter, retirer, remplacer ou réassigner un intervenant en cours de processus sans redémarrage complet.
- Approuver, rejeter ou retourner pour correction.
- Ajouter commentaires et conserver l'historique.
- Suivre les étapes en cours et déclencher notifications/rappels configurables.

### F-06 — Signature électronique
- Signatures séquentielles, parallèles et mixtes.
- Groupes de signataires; signatures simultanées ou consécutives configurables.
- Associer automatiquement étapes et signataires.
- Modifier/ajouter/réassigner des signataires en cours de processus.
- Authentifier les signataires selon leur profil, le document et le risque.
- Recueillir consentement, horodatage et preuve de signature.
- Générer certificat/dossier de preuve et document final signé.

### F-07 — Notifications et rappels
- Activer/désactiver les notifications.
- Configurer rappels automatiques en cas de non-réponse.
- Gérer les destinataires en copie.
- Configurer messages et échéances; scénario de démonstration demande alertes d'échéances et personnalisation des communications.

### F-08 — Suivi centralisé
- Vue de suivi des dossiers, documents, trousses et signatures.
- Afficher étapes terminées, actions en attente et exceptions nécessitant intervention.
- Filtrer selon droits et périmètre organisationnel (conception proposée).

### F-09 — Gestion documentaire, versions et preuve
- Support PDF et Word.
- Conserver versions, étapes et transactions.
- Journaliser consultation, validation, approbation, signature, rejet, annulation et autres événements.
- Consulter et exporter le document signé, certificat/dossier de preuve, journal d'audit et métadonnées.
- Assurer conservation et portabilité dans un format exploitable et durable.

### F-10 — Intégration Salesforce et systèmes IQ
- API/connecteurs standard, documentés et supportés.
- Récupérer données et métadonnées; retourner statuts/références lorsque requis.
- Sécuriser échanges, erreurs, reprises et transactions incomplètes.
- Maintenir corrélation entre identifiant du dossier Salesforce, trousse, documents et preuves.
- Décrire prérequis, limites, responsabilités, efforts et coûts.

### F-11 — Administration et contrôle d'accès
- SSO pour utilisateurs internes.
- Authentification adaptée aux signataires internes et externes; authentification renforcée lorsque requise.
- Contrôle par rôles, groupes, unités administratives et domaines d'affaires.
- Séparation des responsabilités et de l'information.
- Administration des modèles, workflows, historiques et paramètres.

### F-12 — Mobilité et expérience utilisateur
- Usage web et mobile/tablette sans interruption.
- Navigation simple, cohérente et autonome.
- Personnalisation de l'image de marque, des messages et parcours.
- Réduire étapes et interventions manuelles.

### F-13 — Fonctionnalités optionnelles IA / automatisation
- Évaluer extraction de métadonnées, classification, analyse de contenu, détection d'anomalies, aide à la préparation.
- Contrôles humains, confidentialité, limites et conditions d'utilisation obligatoires à préciser.
- CLM avancé et CLM/IA sont tarifés séparément dans le bordereau selon les instructions du DAO.

## 5. Exigences non fonctionnelles consolidées
| ID | Domaine | Exigence source / interprétation |
|---|---|---|
| NF-01 | Hébergement | Infonuagique; hébergement, traitement et communication des données au Québec ou dans une juridiction admissible approuvée par IQ (Cahier); le DAO principal précise Canada, exception évaluée et autorisée par écrit. Clarifier la règle contractuelle prévalente. |
| NF-02 | Intégration | SaaS et applications on-premise par API; architecture d'intégration documentée. |
| NF-03 | Sécurité | Confidentialité, intégrité, disponibilité, authenticité, traçabilité, destruction au bon moment; sécurité dès la conception. |
| NF-04 | Auditabilité | Piste d'audit intègre, chronologique, consultable, horodatée, associée au document, trousse, workflow et signature. |
| NF-05 | Disponibilité | Cahier: vise 99,5% 24/7/365. DAO contient des obligations SLA/availability plus détaillées; voir la section d'écarts. |
| NF-06 | Performance / volume | Estimation de 55 000 à 65 000 documents/an et population potentielle d'environ 1 300 employés (DAO); les volumes ne sont pas garantis. |
| NF-07 | Taille fichier | Téléversement minimal de 25 Mo (Cahier). |
| NF-08 | Environnements | DEV, intégration, acceptation/préproduction et production distincts; environnements additionnels selon besoin/contrat. |
| NF-09 | Résilience | Sauvegardes, restauration, continuité et reprise après sinistre; localisation des copies et lieux de relève à déclarer. |
| NF-10 | Maintenabilité | Mises à jour, correctifs, compatibilité, évolution et remplacement des composants sans perte de données/preuves. |
| NF-11 | Interopérabilité | API/connecteurs sécurisés et documentés; gestion des erreurs et reprises. |
| NF-12 | Utilisabilité | Expérience fluide web/mobile, réduction des étapes, interface en français selon les obligations applicables. |
| NF-13 | Portabilité | Export des documents, preuves, certificats, journaux et métadonnées dans des formats exploitables. |
| NF-14 | Conformité | Respect lois québécoises/canadiennes applicables, protection des renseignements personnels, archives et signature électronique; preuves et attestations à fournir. |

## 6. Règles métier proposées à valider
1. Une trousse contient un ou plusieurs documents et un workflow.
2. Chaque étape possède un type (validation, approbation, signature), un ordre, des assignations et une politique de complétion.
3. Une étape parallèle est terminée lorsque sa règle de quorum est satisfaite; règle exacte à confirmer.
4. Un rejet suspend ou redirige le workflow vers une étape déterminée et conserve l'historique.
5. Toute modification après signature invalide le processus de signature en cours ou déclenche une nouvelle version et un nouveau cycle; règle à confirmer.
6. La version signée est immuable; correction ultérieure par nouvelle version/avenant.
7. Les transitions d'état sont contrôlées côté serveur et journalisées.
8. Les événements reçus d'un système externe sont idempotents et corrélés par identifiant externe.

## 7. Hors périmètre explicite / prudence
Le cahier précise que la génération de contrats n'exige pas, à elle seule, une plateforme CLM complète, sauf disposition expresse. Le scénario 10 du DAO évoque CLM bout en bout; le bordereau sépare les fonctionnalités CLM avancées et CLM/IA. Il faut donc traiter le CLM complet comme une portée optionnelle/à clarifier, et non comme une exigence de base automatiquement incluse.

## 8. Traçabilité
La matrice `02-Analyse/matrice_tracabilite.md` relie exigences, stories, tests et preuves.
