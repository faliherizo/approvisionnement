# Documentation Fonctionnelle et Non Fonctionnelle
## Solution Logicielle Clé en Main de Rédaction Contractuelle
[cite_start]**Référence Projet :** Avis d'appel d'intérêt N° 2026-50-92   
[cite_start]**Client :** Ville de Lévis (Direction de l'approvisionnement) [cite: 1, 2]  
[cite_start]**Horizon Opérationnel :** Octobre 2026 [cite: 77]

---

## 1. Introduction et Objectifs du Système
[cite_start]Le système vise à structurer, uniformiser, automatiser et documenter rigoureusement la rédaction des clauses administratives de la Ville de Lévis[cite: 3, 34]. [cite_start]Il intervient dans l'élaboration des demandes de prix, appels d'offres publics, contrats de gré à gré, avis d'intention et addendas, tout en assurant une conformité légale stricte par rapport aux réalités du marché québécois[cite: 3, 5, 34, 35].

---

## 2. Exigences Fonctionnelles (EF)

### EF1. Gestion de la Bibliothèque de Clauses (Centralisation)
* [cite_start]**Création et Stockage :** Permettre la création, la modification et l'archivage de clauses standards, optionnelles et personnalisées dans un référentiel unique[cite: 39].
* **Indexation et Recherche :** Moteur de recherche multicritère pour retrouver rapidement une clause selon sa thématique, sa portée juridique ou son type de contrat.
* **Historisation :** Suivi des versions de chaque clause avec journalisation de l'auteur et de la date des modifications.

### EF2. Moteur d'Assemblage Automatique et Intelligent
* [cite_start]**Génération Dynamique :** Générer automatiquement un document contractuel complet par l'assemblage logique de clauses prédéfinies[cite: 41].
* [cite_start]**Adaptation Contextuelle :** Ajuster automatiquement le libellé ou l'inclusion des clauses en fonction de règles d'affaires basées sur[cite: 42]:
    * [cite_start]Le type de contrat (services, baux, prêts à usage, etc.)[cite: 38, 45].
    * [cite_start]Le mode de sollicitation (appel d'offres public, demande de prix, gré à gré)[cite: 34, 47].
    * [cite_start]La valeur estimée du contrat[cite: 49].
    * [cite_start]Le contexte particulier du projet[cite: 51].
    * [cite_start]Les interactions avec les autres clauses déjà sélectionnées (dépendances et exclusions)[cite: 52].

### EF3. Rédaction Multiformat de Documents Contractuels
* [cite_start]**Procédures Ouvertes & Demandes de Prix :** Rédaction des clauses administratives pour les appels d'offres publics et invitations[cite: 37].
* [cite_start]**Contrats de Gré à Gré :** Personnalisation complète pour les contrats de toute nature (baux, prêts à usage, contrats de services)[cite: 38].
* [cite_start]**Gestion des Addendas :** Interface interactive permettant de rédiger des addendas en lien direct avec les clauses initiales du devis[cite: 57].
* [cite_start]**Consolidation Finale :** Génération des documents contractuels finaux intégrant automatiquement l'ensemble des addendas émis au fil du processus[cite: 58].

### EF4. Flux de Travail, Collaboration et Approbation
* [cite_start]**Révision Collaborative :** Possibilité pour les intervenants d'annoter et de commenter directement à l'intérieur du devis en cours de rédaction[cite: 55].
* [cite_start]**Gestion des Approbations :** Workflow hiérarchisé configurable pour la validation des clauses et documents par les différentes directions (Approvisionnement, Services juridiques, et directions requérantes)[cite: 53, 56].

---

## 3. Exigences Non Fonctionnelles (ENF)

### ENF1. Conformité Légale et Cadre Normatif
* [cite_start]**Validation Juridique Québécoise :** La solution et ses modèles doivent être validés juridiquement selon les lois applicables au Québec[cite: 5, 35, 59], notamment :
    * [cite_start]Loi sur les contrats des organismes publics (LCOP / LCOM)[cite: 5, 35].
    * [cite_start]Loi sur les cités et villes (LCV)[cite: 5, 35].
* [cite_start]**Protection des Renseignements Personnels :** Respect strict des exigences de la **Loi 25** (Gouvernance des données et vie privée)[cite: 72].

### ENF2. Sécurité de l'Information
* [cite_start]** questionnaire de Sécurité :** Engagement obligatoire du fournisseur à se soumettre et répondre avec succès au questionnaire de sécurité de l'information de la Ville[cite: 75].
* **Contrôle d'Accès :** Authentification sécurisée (idéalement via SSO/Azure AD de la Ville) avec gestion des droits d'accès basée sur les rôles (RBAC).

### ENF3. Soutien et Accompagnement (SLA)
* [cite_start]**Soutien Technique :** Assistance technique complète fournie par le prestataire[cite: 60].
* [cite_start]**Assistance Juridique :** Support juridique lié à l'utilisation et l'interprétation de la solution, assuré par un ou des juriste(s) qualifié(s)[cite: 61].
* [cite_start]**Disponibilité :** Support accessible de **9h00 à 16h00 (Heure de l'Est)**, pendant les jours ouvrés du Québec[cite: 71].
* [cite_start]**Langue :** Maîtrise parfaite de la langue française (à l'oral comme à l'écrit) par l'ensemble du personnel de soutien[cite: 70].

### ENF4. Exploitation et Déploiement
* [cite_start]**Modèle d'Affaires :** Solution logicielle clé en main, éprouvée, robuste et entièrement fonctionnelle (aucun développement sur mesure lourd)[cite: 5, 30, 36, 63].
* [cite_start]**Localisation :** Le fournisseur doit obligatoirement disposer d'un bureau au Canada[cite: 69].
* [cite_start]**Formation :** Fourniture d'une formation complète, documentée et détaillée, dispensée obligatoirement en français[cite: 62].
* [cite_start]**Calendrier :** Système pleinement opérationnel au plus tard en **octobre 2026**[cite: 77].

---

## 4. Matrice de Traçabilité des Livrables Fournisseurs
[cite_start]Pour que la proposition soit jugée recevable par la Direction de l'approvisionnement, elle doit inclure[cite: 2, 90, 104]:

| # | Élément Requis | Description attendue | Document associé |
| :--- | :--- | :--- | :--- |
| 1 | **Présentation Écrite** | [cite_start]Démonstration que la solution répond à 100% des fonctionnalités exigées[cite: 68, 91]. | [cite_start]Proposition technique [cite: 91] |
| 2 | **Estimation Budgétaire**| [cite_start]Coût annuel de la solution pour fins de planification budgétaire sur 5 ans[cite: 28, 91]. | [cite_start]Proposition financière [cite: 91] |
| 3 | **Profil de l'Entreprise** | [cite_start]Brève présentation de l'entreprise démontrant son expérience auprès de municipalités québécoises ou d'organismes publics[cite: 67, 94]. | [cite_start]Profil corporatif [cite: 94] |
| 4 | **Références Clients** | [cite_start]Liste de clients d'envergure similaire utilisant activement la même solution d'affaires[cite: 95]. | [cite_start]Références [cite: 95] |
| 5 | **Formulaire de Coordonnées**| [cite_start]Formulaire d'identification du fournisseur dûment signé (incluant NEQ, TVQ, TPS)[cite: 103, 110]. | [cite_start]Annexe Coordonnées [cite: 103, 110] |



# Documentation d'Architecture de Solution (DAS) & Architecture Organique (DAO)
## Système Clé en Main de Rédaction Contractuelle
[cite_start]**Référence du Projet :** Avis d'appel d'intérêt N° 2026-50-92 [cite: 3, 20]  
[cite_start]**Client :** Ville de Lévis (Direction de l'approvisionnement) [cite: 1, 2]  
[cite_start]**Statut :** Architecture Cible (Cible : Octobre 2026) [cite: 77]

---

## PARTIE 1 : ARCHITECTURE DE SOLUTION (DAS)

### 1.1 Alignement Stratégique et Objectifs d'Affaires
[cite_start]La solution d’affaires doit répondre au besoin de la Ville de Lévis de structurer, documenter et uniformiser de manière rigoureuse la rédaction de ses clauses administratives[cite: 3, 34]. [cite_start]L'objectif principal est de garantir la conformité légale (LCOP/LCOM, LCV, Loi 25) [cite: 5, 35, 72] tout en optimisant le traitement opérationnel des processus d’approvisionnement suivants :
* [cite_start]Demandes de prix [cite: 3, 34]
* [cite_start]Appels d'offres publics [cite: 3, 34]
* [cite_start]Contrats de gré à gré (services, baux, prêts à usage, etc.) [cite: 3, 34, 38]
* [cite_start]Avis d'intention et addendas [cite: 3, 34]

### 1.2 Vue d'Ensemble Logique de la Solution
[cite_start]La solution est conçue comme une plateforme SaaS (Software as a Service) ou web, centralisée et robuste[cite: 30, 36]. Elle s'articule autour de quatre grands blocs logiques :

[ Interface Utilisateur Web (Français) ]
                           │
   ┌───────────────────────┼───────────────────────┐
   ▼                       ▼                       ▼
[ Moteur de Règles ]    [ Moteur d'Assemblage ] [ Workflow & Collab ]
│                       │                       │
└───────────────────────┼───────────────────────┘
▼
[ Base de Données / Référentiel Cloud ]



### 1.3 Architecture Fonctionnelle Cible
1. **Module Référentiel (Bibliothèque Centrale) :** Gestion du cycle de vie des clauses standards, optionnelles et personnalisées[cite: 39].
2. **Moteur d'Assemblage Intelligent :** Automatisation de la génération de documents selon des critères d'affaires (valeur du contrat, type, mode de sollicitation)[cite: 41, 42, 45, 47, 49].
3. **Module Interactif des Addendas :** Prise en compte dynamique des modifications en cours de processus avec fusion finale[cite: 57, 58].
4. **Module Collaboratif :** Commentaires en ligne, annotations et suivi du niveau hiérarchique d'approbation[cite: 53, 55, 56].

---

## PARTIE 2 : ARCHITECTURE ORGANIQUE (DAO)

Cette section décrit l'organisation interne (technique) de l'application logicielle clé en main requise pour assurer sa robustesse[cite: 36].

### 2.1 Schéma des Composants Organiques (Composants applicatifs)

* **Composant UI (User Interface) :** Interface moderne, responsive et entièrement localisée en français[cite: 62, 70]. Permet la saisie des variables de projet et la révision de documents textuels.
* **Moteur de Règles d'Affaires (BRE - Business Rules Engine) :** Composant chargé d'évaluer les lois du marché québécois (LCOM, LCV) et le contexte du projet [cite: 5, 35, 51] afin de sélectionner automatiquement le bon libellé d'une clause[cite: 42].
* **Composant Documentaire (Parser/Generator) :** Module responsable de la compilation des clauses sélectionnées et de l'exportation des fichiers finaux consolidés pour la Ville et ses cocontractants[cite: 41, 58].
* **Gestionnaire de Flux (Workflow Engine) :** Module gérant les états des documents (Brouillon, En Révision, Approuvé) et routant les notifications vers les Services juridiques ou l'Approvisionnement[cite: 53].

### 2.2 Modèle de Données Conceptuel (MDC)
Le schéma organique repose sur les entités clés suivantes :
* **Utilisateur / Rôle :** Identifie l'intervenant et ses droits d'approbation (Approvisionnement, Juriste, Externe)[cite: 53].
* **Clause :** Contient le texte brut, les métadonnées de versioning, le type (standard, optionnelle) et son statut de validation juridique[cite: 39, 59].
* **Règle d'Affaire :** Lie les variables de contexte (ex: Valeur > Seuil public) à l'obligation d'inclure une Clause spécifique[cite: 42, 49].
* **Projet Contractuel :** Instance d'une demande contenant les données spécifiques de la mise en concurrence et l'historique des Addendas[cite: 5, 51, 57].

### 2.3 Intégration et Interfaces Externes
Pour garantir le fonctionnement "clé en main" tout en respectant l'écosystème gouvernemental[cite: 5, 30]:
* **Interface SEAO (Optionnelle / Consultation) :** Alignement du format des devis générés avec les exigences de publication sur le Système électronique d'appel d'offres (SEAO)[cite: 6, 87].
* **Authentification :** Intégration requise avec l'annuaire d'entreprise pour respecter le contrôle d'accès sécurisé.

---

## PARTIE 3 : EXIGENCES ET CONTRAINTES DE CONCEPTION

### 3.1 Sécurité et Souveraineté des Données
* **Loi 25 (Québec) :** Le système doit appliquer les principes de confidentialité dès la conception (*Privacy by Design*), incluant la gestion du chiffrement des données au repos et en transit[cite: 72].
* **Sécurité de l'information :** Soumission obligatoire au questionnaire d’exigences de sécurité de la Ville de Lévis[cite: 75].
* **Infrastructure :** Le fournisseur doit disposer d'un bureau au Canada[cite: 69], assurant une proximité légale et opérationnelle.

### 3.2 Performance et Disponibilité
* **Disponibilité du Support :** L'infrastructure de support doit garantir une réactivité maximale de **9h00 à 16h00 (Heure de l'Est)** les jours ouvrés du Québec[cite: 71].
* **Soutien Hybride :** Garantie d’une double assistance : technique (sur l'application) et juridique (par des juristes sur l'usage de la solution)[cite: 60, 61].