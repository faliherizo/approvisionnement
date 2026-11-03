# Maquettes UI — Solution de génération de contrats et signature électronique

## 1. Portée et méthode
Ce dossier transforme les exigences fonctionnelles consolidées du cahier des besoins et du DAO en parcours d’interface. Les maquettes ne constituent pas des exigences contractuelles supplémentaires. Les choix UX non explicitement prescrits sont identifiés comme propositions ou points à confirmer.

**Formats :** une fiche Markdown par page et un SVG autonome par page. Les SVG sont des wireframes statiques; ils illustrent la structure et les zones fonctionnelles, pas une charte graphique finale ni un prototype interactif.

## 2. Analyse de couverture fonctionnelle
| Domaine | Pages | Exigences |
|---|---|---|
| Pilotage et dossiers | UI-01 à UI-04, UI-18 | F-01, F-08 |
| Documents et modèles | UI-05 à UI-09 | F-01 à F-04, F-09 |
| Salesforce et données | UI-10, UI-21 | F-02, F-10 |
| Workflow, validation et approbation | UI-11 à UI-14 | F-05, F-07 |
| Signature | UI-15, UI-16 | F-04, F-06, F-09 |
| Notifications | UI-17 | F-07 |
| Preuve et audit | UI-19, UI-23 | F-09, F-11 |
| Administration et sécurité | UI-20 à UI-22 | F-11, NF-01, NF-05, NF-08, NF-09 |
| Options | UI-24 | F-13 |

## 3. Navigation proposée
- **Espace de travail** : Tableau de bord, Mes trousses, Mes tâches, Suivi centralisé.
- **Documents** : Modèles, Éditeur de modèles, Documents et versions.
- **Administration** : Utilisateurs et groupes, Workflows, Notifications, Intégrations, Paramètres, Audit.
- **Portail externe** : parcours autonome du signataire externe, accessible par invitation sécurisée.

La navigation et les intitulés doivent être testés auprès des utilisateurs; les rôles ne voient que les fonctions autorisées.

## 4. Principes UI / accessibilité
- Cible WCAG 2.2 niveau AA comme critère de conception à valider par audit.
- HTML sémantique : `header`, `nav`, `main`, `section`, `aside`, `form`, `fieldset`, `legend`, `label`, `button`, `table`, `caption`, `th`, `td`, `footer` selon leur sens. Éviter les éléments interactifs imbriqués (ex. bouton dans bouton, lien dans bouton).
- Ne pas employer un élément HTML uniquement pour son apparence. Les SVG fournis sont des dessins autonomes et ne remplacent pas le HTML sémantique de l’application.
- Clavier, focus visible, zoom, contraste, annonces accessibles, erreurs compréhensibles, alternatives textuelles.
- Les tableaux doivent devenir des cartes ou conserver une structure lisible sur petit écran; aucune action ne doit dépendre uniquement du survol.
- Les champs ont un libellé explicite, une aide et une erreur associée; les champs obligatoires sont annoncés.
- Les statuts combinent texte et icône, jamais la couleur seule.

## 5. Règles transversales
1. Contrôle d’accès serveur pour chaque lecture et commande.
2. Journalisation des actions métier sensibles : création, affectation, validation, approbation, signature, rejet, export et administration.
3. Préserver la corrélation entre dossier Salesforce, trousse, documents, versions et preuves.
4. Les documents signés et leurs preuves sont immuables; toute correction suit un nouveau cycle à confirmer.
5. Les notifications sont configurables et traçables.
6. Les options CLM avancé et CLM/IA sont identifiées comme optionnelles et ne sont pas activées par défaut.

## 6. Points à confirmer avant maquettage haute fidélité
- Charte graphique, terminologie officielle et architecture de navigation IQ.
- Matrice de rôles, délégations, périmètres organisationnels et séparation des tâches.
- Modèle de données Salesforce exact, champs, API, événements et gestion des conflits.
- Limite maximale de taille de fichier, formats additionnels et politique antivirus.
- Règles de quorum, retours, annulation, remplacement d’intervenants et modifications après signature.
- Méthodes de vérification d’identité et preuve attendue pour les signataires externes.
- Conservation, archivage, export et durée de disponibilité des preuves.
- Canaux de notification, délais, escalades et fréquence des rappels.
- Capacités réellement incluses dans le produit de base et options CLM/IA.

## 7. Inventaire des pages
- [UI-01 — Tableau de bord](pages/UI-01_tableau-de-bord.md) — [Maquette SVG](svg/UI-01.svg)
- [UI-02 — Liste des trousses](pages/UI-02_liste-des-trousses.md) — [Maquette SVG](svg/UI-02.svg)
- [UI-03 — Créer une trousse](pages/UI-03_créer-une-trousse.md) — [Maquette SVG](svg/UI-03.svg)
- [UI-04 — Détail d’une trousse](pages/UI-04_détail-dune-trousse.md) — [Maquette SVG](svg/UI-04.svg)
- [UI-05 — Documents – ajout et gestion](pages/UI-05_documents-ajout-et-gestion.md) — [Maquette SVG](svg/UI-05.svg)
- [UI-06 — Historique et versions documentaires](pages/UI-06_historique-et-versions-documentaires.md) — [Maquette SVG](svg/UI-06.svg)
- [UI-07 — Catalogue des modèles](pages/UI-07_catalogue-des-modèles.md) — [Maquette SVG](svg/UI-07.svg)
- [UI-08 — Éditeur de modèle et clauses](pages/UI-08_éditeur-de-modèle-et-clauses.md) — [Maquette SVG](svg/UI-08.svg)
- [UI-09 — Configuration des champs](pages/UI-09_configuration-des-champs.md) — [Maquette SVG](svg/UI-09.svg)
- [UI-10 — Sélection du dossier Salesforce et préremplissage](pages/UI-10_sélection-du-dossier-salesforce-et-préremplissage.md) — [Maquette SVG](svg/UI-10.svg)
- [UI-11 — Concepteur de workflow](pages/UI-11_concepteur-de-workflow.md) — [Maquette SVG](svg/UI-11.svg)
- [UI-12 — Boîte de tâches](pages/UI-12_boîte-de-tâches.md) — [Maquette SVG](svg/UI-12.svg)
- [UI-13 — Validation documentaire](pages/UI-13_validation-documentaire.md) — [Maquette SVG](svg/UI-13.svg)
- [UI-14 — Approbation](pages/UI-14_approbation.md) — [Maquette SVG](svg/UI-14.svg)
- [UI-15 — Préparation de la signature](pages/UI-15_préparation-de-la-signature.md) — [Maquette SVG](svg/UI-15.svg)
- [UI-16 — Portail du signataire externe](pages/UI-16_portail-du-signataire-externe.md) — [Maquette SVG](svg/UI-16.svg)
- [UI-17 — Notifications et rappels](pages/UI-17_notifications-et-rappels.md) — [Maquette SVG](svg/UI-17.svg)
- [UI-18 — Suivi centralisé et recherche](pages/UI-18_suivi-centralisé-et-recherche.md) — [Maquette SVG](svg/UI-18.svg)
- [UI-19 — Dossier de preuve et audit](pages/UI-19_dossier-de-preuve-et-audit.md) — [Maquette SVG](svg/UI-19.svg)
- [UI-20 — Administration des utilisateurs et groupes](pages/UI-20_administration-des-utilisateurs-et-groupes.md) — [Maquette SVG](svg/UI-20.svg)
- [UI-21 — Administration des intégrations API](pages/UI-21_administration-des-intégrations-api.md) — [Maquette SVG](svg/UI-21.svg)
- [UI-22 — Paramètres et environnements](pages/UI-22_paramètres-et-environnements.md) — [Maquette SVG](svg/UI-22.svg)
- [UI-23 — Journal d’audit et événements](pages/UI-23_journal-daudit-et-événements.md) — [Maquette SVG](svg/UI-23.svg)
- [UI-24 — Options CLM et IA](pages/UI-24_options-clm-et-ia.md) — [Maquette SVG](svg/UI-24.svg)
