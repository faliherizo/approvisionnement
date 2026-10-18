---
title: "Réponse à l'appel d'offres n° 7000017905"
subtitle: "Solution d'aide à la rédaction et à la gestion des documents d'approvisionnement — Ville de Trois-Rivières"
author: "VINA IT Solutions inc."
date: "Septembre 2026"
lang: fr-CA
---

# Réponse à l'appel d'offres n° 7000017905

**Objet :** acquisition et implantation d'une solution d'aide à la rédaction et à la gestion des documents d'approvisionnement.

| | |
|---|---|
| **À** | Sarra Boulahmi, Direction de l'approvisionnement — `SBoulahmi@v3r.net` |
| **De** | VINA IT Solutions inc. |
| **Personne-ressource** | RANDRIAMIRINDRA Faliherizo — `faliherizo@vinaitsolution.com` |
| **Adresse** | 4028, rue de l'Étape, Québec (Québec) G1P 4H4 |
| **NEQ** | 1181093866 · **TPS/TVH** : 78841 1635 RT0001 |
| **Site corporatif** | <https://vinaitsolution.com/> |
| **Appel d'offres** | n° 7000017905 — dépôt via SEAO, au plus tard le 28 septembre 2026, 11 h 00 |

Madame Boulahmi,

Nous vous remercions de l'occasion de répondre à cet appel d'offres. Vous trouverez ci-dessous notre
proposition pour doter la Direction de l'approvisionnement d'une solution moderne de rédaction et de
gestion de ses documents. La particularité de notre offre tient en une phrase : **la solution existe
déjà et fonctionne**. Plutôt que de vous décrire un produit à construire, nous vous invitons à
l'essayer dès maintenant.

> **Essayez la solution en ligne.** Elle est déployée et pleinement opérationnelle, sans rien à
> installer :
>
> - **Application** : <https://contrat.vinaitsolution.com/login>
> - **Démonstration vidéo guidée** : <https://contrat.vinaitsolution.com/accueil#demonstration>
> - **Site corporatif** : <https://vinaitsolution.com/>
>
> **Comptes de démonstration** (un par rôle, pour évaluer les fonctionnalités selon les profils) :
>
> | Courriel | Rôle | Mot de passe |
> |---|---|---|
> | `admin@levis.qc.ca` | Administrateur | `Passw0rd!` |
> | `redacteur@levis.qc.ca` | Rédacteur / approbateur | `Passw0rd!` |
> | `juriste@levis.qc.ca` | Juriste | `Passw0rd!` |
> | `direction@levis.qc.ca` | Direction | `Passw0rd!` |
> | `totp@levis.qc.ca` | Démo **MFA — TOTP** (secret de test `JBSWY3DPEHPK3PXP`) | `Passw0rd!` |
> | `question@levis.qc.ca` | Démo **MFA — question secrète** (réponse : `montreal`) | `Passw0rd!` |
>
> Environnement de démonstration : nous vous conseillons de changer le mot de passe au premier accès et
> nous créerons volontiers des **comptes nominatifs** pour les membres du comité.

## 1. En quelques mots

Ce qui nous distingue, c'est la maturité de notre solution et son ancrage québécois. Tout ce que
l'article 7 exige — modèles personnalisables, génération automatisée à partir de clauses, bibliothèque
de contenus réutilisables, historique des versions, collaboration et circuits de validation — est en
place et se démontre en ligne, pas sur papier.

Trois choix de conception méritent d'être soulignés d'emblée. D'abord, l'assistant d'intelligence
artificielle fonctionne **localement** (moteur Ollama) : aucune donnée ne quitte l'environnement de la
Ville et rien ne sert à entraîner un modèle tiers, ce qui nous place d'emblée en conformité avec la
Loi 25. Ensuite, l'interface est **entièrement en français** et son accessibilité a été vérifiée au
niveau AA (SGQRI 008 3.0 / WCAG 2.2). Enfin, pour ce mandat, la solution **sera hébergée sur Amazon Web Services (AWS)**, dans la région **ca-central-1 (Montréal, Québec)**, sur une infrastructure certifiée ISO 27001 et SOC 2 Type II, avec une haute disponibilité pouvant aller jusqu'à un taux de service de 99,99 %.

## 2. À propos de VINA IT Solutions

VINA IT Solutions inc. (NEQ 1181093866) est une entreprise québécoise qui conçoit, héberge et maintient
des solutions logicielles infonuagiques pour des organisations publiques et privées, notamment en
gestion documentaire et en conformité.

Parmi nos réalisations, nous avons contribué à des mandats majeurs du secteur public québécois :
le **site institutionnel `sante.quebec`** de **Santé Québec** (via Ateko / Bell inc., 2025-2026) ; la
**modernisation des interfaces utilisateur et l'intégration de l'authentification gouvernementale
(SAG)** au **ministère de la Sécurité publique** (via COFOMO, 2025-2026) ; le **système provincial de
gestion des patients et des vaccins (SI-PMI / PANORAMA)** de l'**INSPQ** (via Randstad, 2021-2025) ; et
le **Registre des événements et le Registre des entreprises (projet Agro) de Revenu Québec** (via COFOMO,
2018-2020) ; ainsi que les **microservices
AccèsD et la protection des renseignements personnels** chez **Desjardins** (grande institution
financière coopérative, 2020-2021). Ces mandats — architecture de
solution, sécurité applicative (OAuth 2.0 / OpenID Connect, SAML) et DevSecOps sur Microsoft Azure —
sont détaillés dans la Formule de qualification. Nous exploitons aussi une plateforme de **transport scolaire**
(<https://transport.vinaitsolution.com/accueil>) et servons des clients en ligne comme
**instituteliewiesel.com**, **sherlock-home.com** et **peopleinfinance.fr**. La présente solution de
rédaction contractuelle fait par ailleurs l'objet d'un déploiement de référence pour la **Ville de
Lévis**. Nos réalisations plus anciennes incluent cotransportage.fr, famousbike.fr et
velo-pliable-electrique.com, aujourd'hui hors ligne.

## 3. Réponse aux exigences fonctionnelles (article 7)

**Gestion des modèles (7.1).** La solution permet de créer, modifier, visualiser et supprimer des
modèles de documents, avec leur mise en forme (police, page couverture, format). Chaque modèle peut
être **classé par type d'acquisition** — biens, services, services professionnels, travaux de
construction, technologies de l'information — ce qui facilite le repérage et la standardisation.

**Génération automatisée et assistant guidé (7.2).** À partir d'un modèle et de quelques paramètres
(type de contrat, mode de sollicitation, valeur estimée, contexte), un moteur de règles sélectionne les
clauses appropriées, en tenant compte des dépendances et des exclusions entre elles. Pour accompagner
les rédacteurs, un **assistant guidé** propose le mode de sollicitation pertinent, recommande les
modèles adaptés et prévisualise les clauses éligibles avant de produire le document. Celui-ci s'exporte
aussi bien en **Word modifiable** qu'en **PDF**.

**Contenus réutilisables (7.3).** Une bibliothèque centralise les clauses (standard, optionnelles,
personnalisées). Les droits d'ajout, de modification et d'utilisation sont gérés par rôle, et la mise à
jour d'une clause bénéficie à l'ensemble des rédacteurs.

**Gestion documentaire et recherche (7.4).** Chaque document conserve l'historique de ses versions
(auteur, date, nature du changement) avec comparaison visuelle des écarts. La recherche s'effectue par
titre, mots-clés, catégorie, type et contenu, et s'appuie sur des facettes pour affiner rapidement les
résultats.

**Collaboration et validation (7.5).** Les intervenants commentent les documents dans des fils de
discussion, avec un statut de résolution qui permet de suivre les points réglés. Les circuits
d'approbation ne sont pas figés : au-delà du parcours usuel (Approvisionnement, Juridique, Direction),
la Ville peut **configurer ses propres circuits** selon le type de document.

## 4. Environnement technique et intégrations (articles 8-9)

L'interface est conviviale et en français, et la gestion des utilisateurs, des rôles et des accès se
fait directement dans l'application.

Côté écosystème Microsoft 365, la solution exporte nativement vers Word et sait **déposer un document
dans SharePoint ou OneDrive, l'envoyer par Outlook ou notifier une équipe Teams**. Ces intégrations
reposent sur une architecture à connecteurs : chaque destination est desservie par un adaptateur
distinct, si bien qu'ajouter ou activer une intégration se fait par configuration, sans toucher au cœur
du logiciel. L'activation en environnement réel se fait avec les autorisations Microsoft 365 de la Ville
(Microsoft Entra ID). Le même principe s'applique à **SAP** : nous transmettons la référence financière
d'un dossier (numéro d'appel d'offres, objet, montant agrégé) vers le système financier par un
connecteur dédié.

L'**authentification corporative** repose sur un **module de connecteur d'authentification configurable et
activable par organisation**, prenant en charge **OpenID Connect (OIDC)** et **SAML 2.0** : la Ville branche
son annuaire gouvernemental (**Microsoft Entra ID** ou tout autre fournisseur d'identité OIDC / SAML)
directement depuis l'espace d'administration, de sorte que les utilisateurs gouvernementaux se connectent
avec leurs identifiants habituels. Elle est complétée par l'**authentification à deux facteurs**
(application TOTP ou questions de sécurité) et par une gestion fine des permissions (RBAC).

Toutes les fonctions sont exposées par une **API REST documentée au format OpenAPI / Swagger** — interface
Swagger : <https://contrat.vinaitsolution.com/swagger-ui/index.html>, spécification OpenAPI :
<https://contrat.vinaitsolution.com/v3/api-docs> —, ce qui facilite l'intégration avec les systèmes de la
Ville.

Nous restons transparents sur un point : les limitations et les coûts propres à chaque environnement
(version de SAP, configuration du tenant Microsoft 365, GED en place) dépendent d'informations que nous
souhaitons confirmer avec la Ville. Nos questions à ce sujet figurent dans le document joint.

## 5. Sécurité, données, intelligence artificielle et réversibilité (articles 14-22)

La sécurité repose sur le chiffrement des échanges et des données au repos, un contrôle d'accès en refus
par défaut, l'authentification à deux facteurs et un journal d'audit. Ce **journal est consultable,
filtrable et exportable en CSV** : la Ville garde ainsi une trace vérifiable des connexions, des
modifications, des validations, des exportations et des suppressions (article 18).

Le regroupement par **dossier d'approvisionnement** répond directement à l'article 15 : tous les
documents, versions et validations d'un même processus sont réunis, avec des rapports de suivi par
dossier et une séparation nette des responsabilités par rôle.

En matière d'intelligence artificielle (articles 19 et 21), notre parti pris est clair. L'assistant
fonctionne localement, aucune donnée n'est envoyée à l'extérieur ni utilisée pour entraîner un modèle
tiers. Chaque suggestion est **journalisée** (invite, réponse, modèle, utilisateur) et la Ville peut
**désactiver l'assistant** à sa discrétion. La validation humaine demeure la règle.

La **réversibilité** (article 20) est traitée sérieusement : à la demande, la solution produit une
archive complète des données du locataire — clauses, modèles, gabarits, documents, dossiers, circuits —
en formats ouverts (JSON), accompagnée d'un manifeste. Les données appartiennent à la Ville et lui
restent restituables sans dépendance à notre outil.

Enfin, la **migration des contenus** (article 22) s'accompagne d'une **migration d'essai** : nous
validons chaque enregistrement, ligne par ligne, et présentons un rapport avant tout import réel, ce qui
évite les mauvaises surprises en production.

### 5.1 Au sujet des certifications ISO 27001 / SOC 2 Type II

Ce point mérite une réponse franche. Pour ce mandat, la solution sera hébergée sur **Amazon Web Services (AWS)**, dont l'infrastructure et les services sont **certifiés ISO 27001 et SOC 2 Type II**. La Ville hérite donc des
contrôles physiques, d'infrastructure et d'exploitation couverts par ces certifications, selon le modèle
de responsabilité partagée. Nous pouvons fournir les attestations d'AWS, la matrice de responsabilité
partagée et nos propres politiques de sécurité applicative (accès, chiffrement, journalisation, gestion
des vulnérabilités).

La question de savoir si cela suffit dépend de l'intention de l'article 16. Si l'exigence vise la
**certification de l'hébergement**, l'hébergement AWS y répond directement, preuves à l'appui. Si elle
vise une **certification propre au fournisseur** (VINA lui-même), il faut être précis : la certification
de l'hébergeur et celle du fournisseur sont deux choses distinctes, et l'une ne remplace pas l'autre. À
ce jour, **VINA ne détient pas encore de certification ISO 27001 ou SOC 2 Type II en propre** ; nous
appliquons des pratiques alignées sur ces référentiels et sommes prêts, si la Ville l'exige, à nous
engager contractuellement sur une feuille de route de certification.

C'est pourquoi nous demandons à la Ville de préciser deux choses : la certification est-elle attendue du
**fournisseur** ou de l'**hébergement**, et constitue-t-elle une **condition d'admissibilité au dépôt**
ou une **exigence à satisfaire avant la mise en production** ? Ces questions figurent en tête du document
joint.

### 5.2 Notification, traitement et suivi des incidents de sécurité (article 30)

Nous appliquons une procédure formelle de gestion des incidents, alignée sur la **Directive de gestion
des vulnérabilités (annexe A)** de la Ville et sur la **Loi 25**. Elle comporte cinq étapes.

1. **Détection et qualification.** La supervision continue (télémétrie applicative, journaux
   centralisés, tests de disponibilité synthétiques, alertes de sécurité Microsoft Defender for Cloud)
   déclenche une alerte. L'incident est **qualifié en gravité** — *majeur*, *critique*, *mineur* — selon
   son impact sur la confidentialité, l'intégrité et la disponibilité, et selon qu'il touche ou non des
   **renseignements personnels**.

2. **Notification à la Ville — sans délai, au plus tard 24 h.** Dès la découverte, nous informons le
   **point de contact désigné** par la Ville, par le canal convenu au contrat (courriel de sécurité
   dédié et numéro d'urgence), **dans les 24 heures au plus tard** — idéalement immédiatement après la
   détection pour un incident majeur ou critique. La notification initiale décrit ce qui est connu :
   nature de l'incident, systèmes et données potentiellement touchés, impact estimé, premières mesures.

3. **Confinement, traitement et remédiation.** Nous isolons la cause (révocation d'accès, blocage de
   flux, application d'un correctif, bascule sur ressource saine), puis corrigeons durablement. Les
   actions sont horodatées et journalisées pour la traçabilité.

4. **Suivi à fréquence proportionnelle à la gravité.** Nous tenons la Ville informée de l'évolution :
   **mises à jour horaires pour un incident majeur**, **rapports quotidiens pour une vulnérabilité
   critique** en cours de correction, jusqu'au rétablissement complet.

5. **Rapport post-incident.** Une fois l'incident résolu, nous remettons, sur demande, un **rapport
   écrit** : **analyse de la cause racine**, chronologie, impacts constatés, actions correctives et
   **mesures préventives** pour éviter la récurrence. Ce retour d'expérience alimente l'amélioration
   continue de la posture de sécurité.

**Volet Loi 25.** Lorsqu'un incident implique des renseignements personnels, nous coopérons avec la
Ville pour **évaluer le risque de préjudice sérieux**, **tenir le registre des incidents de
confidentialité**, aviser au besoin la **Commission d'accès à l'information du Québec** selon les
processus de la Ville, et mettre en œuvre sans délai les mesures de mitigation. Les points de contact et
d'escalade sont convenus au **démarrage du contrat**, et nous fournissons sur demande un **plan de
correction** à la suite de tout balayage de conformité de la Ville.

### 5.3 Sauvegarde, reprise après sinistre et cibles RPO/RTO — faisabilité (article 16)

Nous nous engageons sur des cibles de reprise **fermes et réalistes**, éprouvées par des exercices de
restauration : **RPO ≤ 15 minutes** (perte de données maximale) et **RTO ≤ 4 heures** (délai de
rétablissement maximal) en cas de sinistre régional.

Ces cibles sont **atteignables avec les services infonuagiques standards** effectivement déployés
(Terraform IaC) :

- **RPO ≤ 15 min** — la base RDS PostgreSQL en **haute disponibilité Multi-AZ** réplique en continu vers une instance de veille (perte quasi nulle en bascule intra-région) ; les **sauvegardes
  automatiques géo-redondantes** et le *point-in-time restore* bornent la perte à ≤ 15 min même en
  sinistre de région ; les documents sont sur stockage **S3 (répliqué inter-région)**.
- **RTO ≤ 4 h** — l'**infrastructure est reproductible en une commande** (`terraform apply`) dans la
  **région canadienne secondaire (`ca-west-1`)** ; le conteneur applicatif est **déjà publié** au
  registre ; la restauration de la base et du stockage, puis la redirection du trafic (Route 53 / CloudFront),
  s'exécutent selon un **runbook documenté et testé**. La procédure détaillée, étape par étape, est
  fournie dans le document *Plan de reprise après sinistre (RPO/RTO)* joint au dépôt.

En **fonctionnement nominal**, la redondance **multi-zone dans la région `ca-central-1` (Montréal, Québec, 3 zones de disponibilité)** — calcul ECS Fargate sur 3 zones, base RDS **Multi-AZ** (primaire + veille), entrée réseau redondée — soutient le **SLA 99,99 %** sans recourir à la bascule inter-région. La région **`ca-west-1` (Calgary)** est réservée à la **reprise après sinistre (DR)** ; les données demeurent **au Canada** (au **Québec** pour la production). La bascule inter-région n'intervient qu'en cas de
**sinistre régional complet**, événement pour
lequel les cibles RPO/RTO ci-dessus s'appliquent.

## 6. Scénario de démonstration (article 26)

Toutes les étapes du scénario demandé se réalisent dès aujourd'hui dans l'application, et nous
fournirons la captation vidéo exigée. Concrètement : la création et l'adaptation d'un modèle se font
dans la gestion des templates ; l'ajout de clauses réutilisables, dans la bibliothèque ; les règles
conditionnelles, via le moteur de règles et l'assistant guidé ; la génération d'un document, depuis
l'assistant ou un dossier ; la modification d'une version, dans l'éditeur avec comparaison ; la
collaboration, par les commentaires et leur résolution ; la validation, par le circuit d'approbation ;
l'exportation, en Word et PDF ou par publication vers Microsoft 365 ; et la recherche d'un contenu, par
la recherche à facettes. Conformément à l'article 26, tout ce que nous présenterons est **fonctionnel
aujourd'hui**, et non projeté.

## 7. Ce que nous apportons en plus (article 27)

Au-delà des exigences, plusieurs éléments ajoutent de la valeur : l'assistant guidé qui transforme un
besoin en document, l'intelligence artificielle encadrée et locale spécialisée en rédaction
contractuelle, les règles conditionnelles avancées (dépendances et exclusions), les connecteurs
Microsoft 365 et SAP extensibles sans refonte, un environnement d'essai pour la migration, un journal
d'audit exportable et des rapports par dossier. Nous partagerons par ailleurs une feuille de route
d'évolution avec la Ville.

## 8. Implantation, formation, soutien et gouvernance (articles 10-12, 23-24)

Notre démarche d'implantation suit les étapes prévues à l'article 10 : analyse, paramétrage, migration
(avec migration d'essai), formation, puis mise en production. La formation s'adresse tant aux
utilisateurs principaux qu'aux administrateurs et s'accompagne d'une documentation en français et d'un
accompagnement au démarrage. Le soutien et la maintenance couvrent la prise en charge par priorité, les
mises à jour et la maintenance planifiée communiquée à l'avance ; les plages de couverture et les délais
précis seront arrêtés au contrat, et nous soumettons quelques questions à ce sujet. Un chargé de projet
— votre personne-ressource unique — assure la gouvernance, le calendrier, les livrables et le suivi des
risques et des décisions, dans la perspective d'une acceptation par livrables conditionnels.

## 9. Accessibilité et langue (article 25)

L'accessibilité a été vérifiée au niveau AA du SGQRI 008 3.0 et de la WCAG 2.2 : navigation complète au
clavier, contrastes suffisants, cibles tactiles adéquates et absence de superposition d'éléments, sur les
navigateurs courants. L'interface, les documents produits et les communications sont en français,
conformément à la Charte de la langue française et aux attentes de l'OQLF.

## 10. Prix, bordereau et estimation budgétaire

Le modèle est un **abonnement infonuagique annuel, tout inclus** : licences pour l'ensemble des
utilisateurs, implantation et configuration, hébergement sur Amazon Web Services (AWS) (région ca-central-1 Montréal, haute
disponibilité jusqu'à 99,99 %), mises à jour, soutien et maintenance. Aucun coût caché : le forfait annuel
couvre l'intégralité des services décrits au devis. Le prix est **ferme** pour les trois premières années,
puis **indexé de 5 % par année** pour les sept années optionnelles de renouvellement, afin de suivre la
hausse des coûts tout en restant prévisible pour la Ville.

**Licence de base : 68 000 $/an (avant taxes)** — récurrent tout inclus (licences, hébergement AWS **HA SLA 99,99 %**, mises à jour, soutien, maintenance). Conformément au devis (services inclus dans les
prix, sans frais supplémentaires), les **frais d'implantation (100 000 $)** — services d'onboarding
spécifiques à la Ville : analyse/cadrage 12 000 $, configuration 20 000 $, modèles et clauses 22 000 $,
migration des données 12 000 $, M365/intégrations (dont SSO Entra) 14 000 $, formation 10 000 $, mise en
production 5 000 $, gestion de projet 5 000 $ — sont **intégrés au forfait de l'année 1**. Taxes : TPS
5 %, TVQ 9,975 %.

### 10.1 Période ferme (3 ans) — implantation incluse en année 1

| Art. | Année | Description | Prix unitaire | Montant |
|-----:|-------|-------------|--------------:|--------:|
| 1 | 2027 | Forfait annuel — récurrent 68 000 $ **+ implantation 100 000 $** | 168 000,00 $ | 168 000,00 $ |
| 2 | 2028 | Forfait annuel tout inclus (récurrent) | 68 000,00 $ | 68 000,00 $ |
| 3 | 2029 | Forfait annuel tout inclus (récurrent) | 68 000,00 $ | 68 000,00 $ |
| | | **Somme partielle — période ferme (3 ans)** | | **304 000,00 $** |
| | | TPS (5 %) | | 15 200,00 $ |
| | | TVQ (9,975 %) | | 30 324,00 $ |
| | | **SOMME GLOBALE DE LA PÉRIODE FERME** | | **349 524,00 $** |

### 10.2 Années optionnelles de renouvellement (7 ans, indexation +5 %/an)

| Art. | Année | Description | Prix unitaire | Montant |
|-----:|-------|-------------|--------------:|--------:|
| 4 | 2030 | Forfait annuel tout inclus (option 1) | 71 400,00 $ | 71 400,00 $ |
| 5 | 2031 | Forfait annuel tout inclus (option 2) | 74 970,00 $ | 74 970,00 $ |
| 6 | 2032 | Forfait annuel tout inclus (option 3) | 78 718,50 $ | 78 718,50 $ |
| 7 | 2033 | Forfait annuel tout inclus (option 4) | 82 654,43 $ | 82 654,43 $ |
| 8 | 2034 | Forfait annuel tout inclus (option 5) | 86 787,15 $ | 86 787,15 $ |
| 9 | 2035 | Forfait annuel tout inclus (option 6) | 91 126,50 $ | 91 126,50 $ |
| 10 | 2036 | Forfait annuel tout inclus (option 7) | 95 682,83 $ | 95 682,83 $ |
| | | **Somme partielle — 7 options** | | **581 339,41 $** |
| | | TPS (5 %) | | 29 066,97 $ |
| | | TVQ (9,975 %) | | 57 988,61 $ |
| | | **SOMME GLOBALE DES 7 OPTIONS** | | **668 394,99 $** |

### 10.3 Sommaire

| Poste | Avant taxes | Toutes taxes comprises |
|-------|------------:|-----------------------:|
| Période ferme (3 ans, implantation incluse en An 1) | 304 000,00 $ | 349 524,00 $ |
| Options de renouvellement (7 ans) | 581 339,41 $ | 668 394,99 $ |
| **Total sur 10 ans** | **885 339,41 $** | **1 017 918,99 $** |

### 10.4 Estimation budgétaire (planification de la Ville)

Pour la planification budgétaire : **année 1 (2027) = 168 000 $ avant taxes** (récurrent 68 000 $ +
implantation 100 000 $), puis **68 000 $ avant taxes (78 183,00 $ TTC) par année récurrente** (2028-2029).
Les années optionnelles suivent l'indexation de 5 % ci-dessus, de 71 400 $ (2030) à 95 682,83 $ (2036)
avant taxes. Le coût total de possession s'établit à **349 524,00 $ TTC sur la période ferme** et à
**1 017 918,99 $ TTC sur l'horizon complet de dix ans**, si toutes les options sont levées. Conformément au
devis, **l'ensemble des services décrits est inclus dans les prix soumis, sans frais supplémentaires**.

**Demandes hors devis (coûts additionnels).** Toute **nouvelle demande non prévue au devis** (ajout de
fonctionnalité, intégration ou développement spécifique demandé en cours de contrat) fait l'objet d'une
**analyse**, puis, **après accord écrit de la Ville**, est réalisée au **taux de 120 $/h (avant taxes)** —
sans aucune incidence sur les prix soumis ni sur les services décrits au devis, qui demeurent tout inclus.

Les mêmes montants sont reportés, sans modification, sur le **bordereau de soumission officiel** déposé
via le SEAO ; en cas de divergence, le bordereau officiel signé fait foi.

## 11. Pièces déposées

Nous joignons au dépôt, dûment remplis et signés, les formulaires prévus : le bordereau de soumission
(années 1 à 3 fermes et sept options), la formule de qualification, l'attestation d'intégrité du
soumissionnaire (LCOP c. C-65.1), la formule de résolution autorisant le signataire, l'engagement de
confidentialité et l'attestation de conformité à l'OQLF. S'y ajoutent le présent mémoire technique et
commercial, la captation vidéo du scénario de l'article 26, le **Plan de reprise après sinistre
(RPO ≤ 15 min, RTO ≤ 4 h)**, ainsi que nos preuves de sécurité (attestations AWS ISO 27001 / SOC 2 Type II, matrice de responsabilité partagée et politiques de sécurité).

## 12. Réserves

La présente réponse s'entend sous réserve des clarifications que nous demandons à la Ville dans le
document de questions joint, en particulier sur l'exigence de certification ISO 27001 / SOC 2 Type II,
les modalités de dépôt et de captation vidéo via SEAO, les accès aux environnements Microsoft 365 et SAP
pour l'activation des connecteurs, ainsi que le volume et les formats de la migration.

Nous demeurons à votre disposition pour toute précision et vous prions d'agréer, Madame Boulahmi,
l'expression de nos salutations distinguées.

**RANDRIAMIRINDRA Faliherizo**
VINA IT Solutions inc. — <https://contrat.vinaitsolution.com/login>
