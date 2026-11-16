# Questions à adresser à Investissement Québec

Questions pour valider et ajuster la solution proposée à l'AO 2026-6370-0005, mises à jour le 2026-11-08 après la
validation des exigences F-01 à F-13 et les essais en bac à sable. La colonne **Hypothèse actuelle** décrit ce que fait
la solution aujourd'hui : IQ peut la confirmer d'un mot ou la corriger. Une question marquée ★ bloque une réalisation ou
une recette.

Documents liés : [validation des exigences](../02-Analyse/validation_exigences_fonctionnelles.md) ·
[écarts entre documents](../02-Analyse/ecarts_entre_documents.md) ·
[bacs à sable](../12-Bacs-a-sable/guide_bacs_a_sable.md) ·
[profils et rôles du tenant IQ](../11-Tenant-IQ/tenant_iq_profils_et_roles.md).

## Réponses reçues — addenda 1 (9 novembre 2026)

L'[addenda 1](reponses/addenda-1.md) répond en tout ou en partie aux questions ci-dessous ; impact détaillé :
[impact_addenda_1.md](../02-Analyse/impact_addenda_1.md).

| # | Réponse d'IQ | Suite |
|---|---|---|
| A1, A2 | Besoin principal : signature, génération documentaire et orchestration ; le CLM avancé est optionnel, sauf exigence expressément obligatoire (Q1, § 4.3) | ✔ Répondu : offre de base et options distinctes |
| A5 | 1 300 signataires, 20 administrateurs d'affaires, 3 administrateurs techniques principaux, 15 administrateurs techniques utilisateurs ; 70 à 100 utilisateurs CLM inclus dans ce total (Q2, Q5) | ✔ Répondu |
| C4 | Standard : lien sécurisé par courriel, compte ou SSO ; renforcée : SMS à usage unique, multifacteur ou vérification d'identité (Q4) | ✔ Répondu ; décrire nos méthodes, leur niveau d'assurance et leurs coûts |
| D1 | Microsoft Entra ID nommé parmi les intégrations (Q3) | ◐ Protocole (OIDC ou SAML) encore à confirmer |
| D6 | Saviynt ou SCIM nommé parmi les intégrations (Q3) | ◐ Voir P1 à P4 |
| G1, F (processus) | Nombre de modèles et de processus fixé en ateliers ; indiquer le nombre inclus et l'effort unitaire (Q7, Q9) ; environ 30 champs Salesforce par modèle (Q8) | ◐ Hypothèse de prix à arrêter |
| K1 | Mise en œuvre initiale détaillée, stabilisation de 30 jours ouvrables (Q6) | ◐ Échéancier encore à convenir |
| L1, L3 | CLM avancé et CLM/IA tarifés à part, aux lignes prévues (Q2) | ✔ Répondu |
| — | Aucun éditeur visé ; solutions équivalentes admises (Q11) | ✔ Pris en compte |
| — | Nouvelle exigence obligatoire TI-22 : intégration Workday (§ 4.4) | Réalisée ; questions W1 à W6 |
| — | Signature en lot depuis Salesforce et depuis la plateforme (Q10) | Réalisée ; questions V1 à V3 |

## Réponses reçues — addenda 2 (14 novembre 2026)

L'[addenda 2](reponses/addenda-2.md) répond aux questions ci-dessous ; impact et ajustements :
[impact_addenda_2.md](../02-Analyse/impact_addenda_2.md).

| # | Réponse d'IQ | Suite |
|---|---|---|
| A1, A2 | CLM avancé et IA en options distinctes ; le scénario 10 ne rend pas le CLM avancé obligatoire (Q1, Q2) | ✔ Répondu |
| A3 | Premier périmètre : conventions d'aide financière issues de Grantmaking (Q3) | ✔ Conforme à l'hypothèse |
| A4 | 55 000 à 65 000 documents par an ; pics inconnus ; démontrer la volumétrie (Q4) | Plan de charge CLM ajouté ; pics à recalibrer |
| A5 | Environ 1 300 employés ; répartition en conception (Q5) | ✔ Répondu |
| A6 | Une organisation principale ; préciser limites et coûts des groupes et environnements (Q6) | ✔ Aucune limite ni coût pour les groupes |
| B1 | Objet `FundingRequest`, lu par Id ou `Name` ; correspondance par modèle (Q7) | ✔ Conforme |
| B2 | `CLM_Statut__c`, `CLM_Reference__c`, `CLM_Correlation__c` envisagés (Q8) | ✔ Conforme |
| B3 | API v60.0, ajustable (Q9) | ✔ Conforme |
| B4 | REST ; aucun Platform Event ni CDC imposé (Q10) | ✔ Conforme |
| B5 | Compte d'exécution possible ; décrire JWT Bearer en remplacement (Q11) | Réalisé : JWT Bearer (RST-23) |
| B6 | Salesforce maître du dossier ; la solution gère le document signé et la preuve (Q12) | ✔ Conforme |
| B7 | Conservation dans la solution ; export ou transfert vers un référentiel d'IQ (Q13) | Réalisé : exportation en lot (RST-24) |
| B8 | Lancée, retournée, rejetée, annulée, terminée ; correspondance en conception (Q14) | Réalisé : retour d'étape transmis (RST-20), correspondance configurable (RST-21) |
| B9 | Seules les données nécessaires (Q15) | Réalisé : lecture limitée aux champs déclarés (RST-22) |
| B10 | Environnements et comptes fixés au démarrage ; données fictives (Q16) | ◐ Date encore inconnue |
| B11 | 8 reprises au plus, intervalles plafonnés à 1 h (Q17) | ✔ Conforme |
| E1 | Québec ou Canada ; autre territoire sous conditions ; lister tous les lieux (Q18) | ◐ Tableau des lieux à produire ; fournisseurs courriel, SMS et horodatage à désigner |
| — | Interfaces en français obligatoires, anglais sur demande (Q19) | Réalisé : langue de communication du signataire (RST-25) |
| — | Un éditeur peut soumissionner seul (Q20) | ✔ Répondu |

## Questions prioritaires

À poser dès la première rencontre : sans réponse, une réalisation ou une recette reste bloquée.

| # | Sujet | Ce qui en dépend |
|---|---|---|
| B1, B2, B10 | Objets et champs Salesforce, bac à sable et compte technique | Recette de la récupération des données et du retour de statut |
| C1, C3 | Fournisseur de signature et autorité d'horodatage | Valeur probante qualifiée (ST-09) |
| D1, D3, D4 | Annuaire, correspondance groupes → rôles, domaines d'affaires | Authentification unique et droits par unité |
| E3, E4, E5 | Calendrier de conservation, gel légal, sort final | Conservation et destruction (F-09) |
| W1, W2 | Processus RH Workday visés et mécanisme côté Workday | Réalisation de TI-22 (obligatoire) côté Workday |
| P1 | Saviynt ou SCIM : lequel, et pour quels comptes | Provisionnement des comptes |
| E1 | Localisation : Québec ou Canada | Région d'hébergement |

## A. Périmètre et priorités

| # | Question | Hypothèse actuelle |
|---|---|---|
| A1 ★ | Le périmètre de base se limite-t-il à la génération documentaire ciblée et à la signature, ou un CLM complet est-il attendu dès la première livraison ? | Génération, parcours et signature dans le prix de base ; CLM avancé et CLM/IA en options distinctes |
| A2 ★ | Le scénario 10 de démonstration évoque un CLM de bout en bout, alors que le cahier ne l'exige pas sauf mention expresse. Quelles capacités CLM sont obligatoires, optionnelles ou hors périmètre ? | Toutes réalisées ; la ventilation base / option reste à fixer |
| A3 | Quels processus contractuels déployer en premier (aide financière, ententes, lettres, autres) ? | Conventions d'aide financière issues de Grantmaking |
| A4 | Quels volumes mensuels et quels pics, au-delà de 55 000 à 65 000 documents par an ? | Pics non connus ; tests de charge à calibrer |
| A5 | Combien d'utilisateurs internes actifs par profil (initiateurs, validateurs, approbateurs, administrateurs) ? | Environ 1 300 employés potentiels, répartition inconnue |
| A6 | Les unités ou filiales d'IQ partagent-elles une seule organisation dans la plateforme, ou faut-il des organisations séparées ? | Une seule organisation `iq` |

## B. Salesforce Grantmaking

| # | Question | Hypothèse actuelle |
|---|---|---|
| B1 ★ | Quels objets, champs, relations et événements lire et écrire ? L'objet maître est-il `FundingRequest` ? | Lecture de `FundingRequest` par identifiant ou par numéro (`Name`) ; correspondance des champs réglée par modèle |
| B2 ★ | IQ accepte-t-il de créer trois champs de retour sur l'objet, ou en impose-t-il d'autres ? | `CLM_Statut__c`, `CLM_Reference__c`, `CLM_Correlation__c`, renommables dans l'écran UI-21 |
| B3 | Quelle version et quelle configuration de Public Sector Solutions / Grantmaking sont en service ? Quelle version d'API ? | API `v60.0`, modifiable |
| B4 | Quels mécanismes sont autorisés : REST, Platform Events, Change Data Capture, webhook, intergiciel ? | REST : lecture à la demande et `PATCH` de statut avec reprises ; aucun événement entrant |
| B5 | Le flux OAuth *client credentials* avec un utilisateur d'exécution dédié est-il permis par la sécurité d'IQ ? | Oui ; sinon flux JWT bearer à réaliser |
| B6 | Quel système est maître du statut contractuel, du document signé et des métadonnées ? | Salesforce pour le dossier ; la plateforme pour le document signé et la preuve |
| B7 | Les fichiers signés doivent-ils être déposés dans Salesforce, dans la plateforme ou dans un référentiel tiers (SharePoint) ? | Conservés dans la plateforme ; référence et statut renvoyés à Salesforce |
| B8 | Quels statuts renvoyer à Salesforce, et à quel moment ? | Lancée, retournée, rejetée, annulée, terminée |
| B9 | Quelles données personnelles passent de Salesforce à la plateforme, et l'évaluation des facteurs relatifs à la vie privée (ÉFVP) les couvre-t-elle ? | Seuls les champs liés à un modèle sont copiés |
| B10 ★ | Quels environnements Salesforce (bac à sable, acceptation, production), comptes techniques et jeux de données anonymisés seront fournis, et à quelle date ? | Essais faits sur un bac à sable local imitant l'API |
| B11 | Quelles limites d'API et quelles fenêtres de maintenance appliquer ? | Reprises : 8 au plus, intervalles croissants, plafonnés à une heure |

## C. Signature et preuve

| # | Question | Hypothèse actuelle |
|---|---|---|
| C1 ★ | Un fournisseur de signature est-il déjà retenu ou autorisé (Notarius/ConsignO, OneSpan, DocuSign, Adobe, autre) ? Les preuves doivent-elles être produites par la plateforme ou par ce tiers ? | Port prêt pour un fournisseur certifié ; preuve locale scellée (HMAC), non qualifiée |
| C2 | Quels niveaux de signature sont acceptés selon le type de document et le montant ? | Niveau d'identité selon le risque de la trousse (faible, moyen, élevé) |
| C3 ★ | Quelle autorité d'horodatage qualifiée (RFC 3161) utiliser ? | Horodatage serveur ; essai RFC 3161 validé avec une autorité publique |
| C4 | Quelles méthodes d'authentification pour les signataires internes et externes (SSO, courriel, SMS, question, vérification d'identité forte) ? | Externes : courriel, SMS, forte selon le risque ; internes : courriel, courriel, SMS |
| C5 | Les signataires externes signent-ils sans créer de compte ? | Oui : lien à jeton valide 14 jours et vérification d'identité |
| C6 | Quels formats de certificat, de preuve et d'export sont exigés (PDF/A, PAdES à validation longue, XML) ? | Certificat PDF, dossier de preuve ZIP (documents, journal, métadonnées) |
| C7 | La signature doit-elle apparaître visuellement dans le document, à un emplacement défini ? | Zones de signature positionnées dans le modèle |
| C8 | Un mandataire ou un représentant peut-il signer pour une entreprise ? Quelle preuve de pouvoir est exigée ? | Non traité |
| C9 | Une signature manuscrite numérisée ou sur papier doit-elle rester possible en dernier recours ? | Non prévu |
| C10 | Qui, chez IQ, signe pour IQ selon les montants (délégation de signature) ? | Signataires désignés par étape, sans règle de montant |

## D. Annuaire, accès et rôles

| # | Question | Hypothèse actuelle |
|---|---|---|
| D1 ★ | Quel fournisseur d'identité (Microsoft Entra ID, ADFS, autre) et quel protocole (OIDC ou SAML 2.0) ? | Les deux sont réalisés et testés contre Keycloak |
| D2 | IQ fournira-t-il une inscription d'application de test (bac à sable) et des comptes de test ? | Non obtenu |
| D3 ★ | Quels groupes de l'annuaire donnent quel rôle (initiateur, validateur, approbateur, signataire, administrateur, auditeur) ? | Un compte créé par SSO reçoit un rôle par défaut ; les groupes de l'annuaire ne sont pas encore repris |
| D4 ★ | Qu'est-ce qu'un « domaine d'affaires » chez IQ : une unité administrative, une direction, un programme ? D'où vient-il (groupe, attribut) ? | Visibilité par unité responsable de la trousse |
| D5 | Faut-il séparer l'administration d'affaires (modèles, parcours) de l'administration technique (accès, intégrations) ? | Un seul rôle `ADMIN` ; rôles distincts réalisables |
| D6 | Création des comptes : à la première connexion, par provisionnement (SCIM) ou par l'administrateur ? Départ d'un employé : désactivation automatique ? | Création à la première connexion ; désactivation manuelle |
| D7 | Le second facteur est-il assuré par l'annuaire ? Faut-il le désactiver dans la plateforme pour les comptes SSO ? | Second facteur disponible dans la plateforme |
| D8 | Des comptes locaux de secours sont-ils permis si l'annuaire est indisponible ? | Comptes locaux possibles, désactivables |
| D9 | Les gestionnaires d'unité (maquettes UI-01, UI-02, UI-18, UI-24) doivent-ils voir toutes les trousses de leur unité ? | Visibilité par groupe d'unité |

## E. Données, conservation, sécurité et hébergement

| # | Question | Hypothèse actuelle |
|---|---|---|
| E1 ★ | Le cahier admet certaines juridictions hors Québec sous conditions, le document principal exige le Canada. Quelle clause prévaut, et quelle localisation minimale ? | Azure, région Canada |
| E2 | Les accès d'administration et de soutien doivent-ils se faire exclusivement depuis le Canada ? | Oui |
| E3 ★ | Quel calendrier de conservation s'applique aux contrats, preuves et journaux ? Quelles durées par type de document ? | Aucune durée ni destruction automatique |
| E4 ★ | Comment déclarer et lever un gel légal ? Qui en décide ? | Non traité |
| E5 ★ | Quel sort final à l'échéance : destruction, versement aux archives, conservation permanente ? Quel bordereau ou preuve de destruction ? | Non traité |
| E6 | Quel format d'archivage à long terme (PDF/A) et quel dépôt (plateforme, GED d'IQ) ? | PDF et Word d'origine conservés, empreintes scellées |
| E7 | Quelle classification des données, et quelles mesures selon la classe ? | Données confidentielles ; chiffrement au repos et en transit |
| E8 | Quels RTO, RPO, fréquence et durée de conservation des sauvegardes ? | Plans de reprise rédigés, valeurs à convenir |
| E9 | Quelles normes et certifications sont obligatoires, et lesquelles sont acceptées comme équivalentes (ISO 27001, SOC 2 Type II) ? | Celles de l'hébergeur ; audit indépendant à prévoir |
| E10 | L'Annexe 10 Excel et les politiques de sécurité citées peuvent-elles être fournies ? | Non reçues |
| E11 | Qui réalise l'évaluation des facteurs relatifs à la vie privée (Loi 25), et quand ? | IQ, avec l'appui du fournisseur |

## F. Parcours, validation et règles d'affaires

| # | Question | Hypothèse actuelle |
|---|---|---|
| F1 | Règle de quorum des étapes parallèles : tous, un parmi, majorité, paramétrable ? | Paramétrable par étape |
| F2 | Qui peut modifier les validateurs ou les signataires après le lancement ? Quelles modifications imposent une nouvelle approbation ? | Initiateur et administrateur ; aucune réapprobation imposée |
| F3 | « Retour pour correction » : vers l'initiateur, vers un validateur antérieur ou vers une étape choisie ? | Vers l'initiateur : la trousse redevient un brouillon et le parcours reprend depuis le début |
| F4 | Règles d'expiration, d'annulation, de délégation et de relance ? | Invitation 14 jours ; rappels tous les 2 jours, 3 au plus ; alerte un jour avant l'échéance ; délégation par l'intervenant |
| F5 | Les commentaires sont-ils visibles de tous les participants ou restreints par rôle ? | Visibles des intervenants de la trousse |
| F6 | Un rejet arrête-t-il la trousse, ou certains rejets doivent-ils renvoyer à une étape précise ? | Le rejet arrête la trousse |
| F7 | Des règles de montant ou de programme déterminent-elles les approbateurs (ex. vice-présidence au-delà d'un seuil) ? | Étapes conditionnelles par règle sur un champ (ex. montant) |
| F8 | Séparation des responsabilités : l'initiateur peut-il signer sa propre trousse ? | Il ne peut ni valider ni approuver, mais peut signer |

## G. Modèles, champs et documents

| # | Question | Hypothèse actuelle |
|---|---|---|
| G1 | Combien de modèles et de clauses existent aujourd'hui, et sous quel format (Word, PDF, autre outil) ? | Modèles créés dans l'éditeur de la plateforme |
| G2 | Faut-il importer les modèles Word existants en conservant leur mise en page ? | Corps de texte avec variables ; mise en page Word non reprise |
| G3 | Qui rédige, approuve et publie un modèle ? Faut-il une double approbation (affaires et juridique) ? | Rédaction par un initiateur ou un juriste, publication par un administrateur |
| G4 | Le document généré doit-il être en Word modifiable, en PDF, ou les deux ? | PDF généré ; Word accepté en dépôt |
| G5 | Une trousse peut-elle contenir des documents non signés (annexes de référence) ? | Oui |
| G6 | Faut-il une taille de document supérieure à 25 Mo ? | 25 Mo, réglable jusqu'au plafond de la plateforme |
| G7 | La saisie assistée doit-elle puiser au-delà des valeurs déjà saisies par l'initiateur (listes de l'organisation, référentiels d'IQ) ? | Suggestions tirées des trousses de l'initiateur ; listes de référence administrables |

## H. Notifications

| # | Question | Hypothèse actuelle |
|---|---|---|
| H1 | Quels canaux : courriel, SMS, notification dans l'application, Teams ? Quel fournisseur de SMS ? | Courriel, SMS et notification interne |
| H2 | Quelle adresse d'expédition (domaine d'IQ, SPF et DKIM) ? | Adresse de la plateforme |
| H3 | Les messages aux externes doivent-ils être bilingues ? | Français, messages modifiables par l'organisation |
| H4 | Qui reçoit les alertes de retard et les copies ? | Initiateur, plus les copies déclarées sur la trousse |

## I. Suivi, rapports et IA

| # | Question | Hypothèse actuelle |
|---|---|---|
| I1 | Quels indicateurs suivre (délais par étape, volumes, taux de rejet) et pour qui ? | Tableau de bord, suivi centralisé, export CSV |
| I2 | Faut-il alimenter un entrepôt ou un outil de rapports d'IQ (Power BI) ? | Exports CSV |
| I3 | L'option IA peut-elle traiter les contrats sur un modèle hébergé par le fournisseur ? Un modèle externe (infonuagique) est-il interdit ? | Modèle exécuté dans l'environnement de la plateforme ; aucune donnée envoyée à un tiers |
| I4 | Quelles analyses IA sont attendues en priorité (classification, extraction, anomalies, comparaison aux clauses types) ? | Classification, extraction, anomalies, avec validation humaine obligatoire |

## J. Exploitation et niveaux de service

| # | Question | Hypothèse actuelle |
|---|---|---|
| J1 | Les grilles de niveaux de service divergent : cahier (critique / majeure / moyenne / mineure), document principal (priorités 1 à 5). Laquelle s'applique ? | À trancher ([écarts entre documents](../02-Analyse/ecarts_entre_documents.md)) |
| J2 | Quelle définition de la disponibilité (période, exclusions, maintenance planifiée, fournisseurs tiers) ? | 99,5 % visé, hors maintenance annoncée |
| J3 | Quels crédits de service, pénalités et mécanismes de mesure ? | À convenir |
| J4 | Quel soutien de premier niveau IQ garde-t-il à l'interne ? | Soutien de deuxième et troisième niveaux par le fournisseur |
| J5 | Quels environnements fournir : développement, intégration, acceptation, production ? | Quatre environnements distincts |

## K. Livraison, gouvernance et migration

| # | Question | Hypothèse actuelle |
|---|---|---|
| K1 | Échéancier cible et date de mise en production souhaitée ? | À convenir |
| K2 | Qui approuve modèles, clauses, parcours, contrôles de sécurité et critères d'acceptation ? | À convenir |
| K3 | Quels livrables par jalon, sous quels formats ? | Documentation, guides, rapports de tests |
| K4 | Faut-il migrer des documents, des modèles ou des dossiers en cours ? | Aucune migration prévue |
| K5 | Quels rôles d'IQ participent au transfert de connaissances et à l'administration autonome ? | Administrateurs d'affaires et techniques |

## L. Contrat, tarification et propriété

| # | Question | Hypothèse actuelle |
|---|---|---|
| L1 | Quelle unité de facturation comparer : document, envoi, trousse, transaction, signataire ? | Abonnement annuel par organisation |
| L2 | Comment sont facturés les volumes excédentaires, les utilisateurs externes et les environnements supplémentaires ? | À convenir |
| L3 | Confirmer que le CLM avancé et le CLM/IA sont des options distinctes, hors prix obligatoire. | Oui |
| L4 | Y a-t-il des coûts de sortie, de restitution, d'export massif ou d'assistance à la transition ? | Export complet inclus |
| L5 | Les frais du fournisseur de signature et de l'autorité d'horodatage sont-ils à la charge du soumissionnaire ou d'IQ ? | À convenir |
| L6 | Durée du contrat, options de renouvellement et règle d'indexation des prix ? | À convenir |
| L7 | Qui détient les modèles, clauses et paramètres créés par IQ dans la plateforme ? | IQ |
| L8 | Les sous-traitants (hébergeur, fournisseur de signature, envoi de SMS) doivent-ils être déclarés et approuvés ? | Déclarés |

## M. Accessibilité, langue et expérience

| # | Question | Hypothèse actuelle |
|---|---|---|
| M1 | Quel niveau d'accessibilité est exigé, et quels navigateurs et appareils doivent être pris en charge ? | SGQRI 008 3.0 / WCAG 2.2 AA ; navigateurs récents, téléphone, tablette, poste |
| M2 | L'anglais est-il requis, pour les employés ou seulement pour les signataires externes ? | Interface en français et en anglais |
| M3 | Quelle image de marque : logo, couleurs, sous-domaine `iq.<domaine>` ? | Nom et logo de l'organisation sur le portail du signataire |

## W. Workday (exigence TI-22, addenda 1)

| # | Question | Hypothèse actuelle |
|---|---|---|
| W1 ★ | Quels processus métier RH déclenchent une signature (offre d'emploi, contrat, avenant, politique à attester) et avec quel volume ? | Tout processus capable d'appeler l'API d'intégration |
| W2 ★ | Côté Workday, quel mécanisme IQ privilégie-t-il : Workday Studio, Orchestrate, Workday Extend ou un intergiciel existant ? | API REST de la plateforme appelée par le mécanisme choisi par IQ |
| W3 | Le document à signer est-il produit par Workday, ou généré par un modèle de la plateforme à partir de données Workday ? | Les deux possibles |
| W4 | Le document signé doit-il revenir dans le dossier de l'employé (pièce jointe) ou seulement sa référence et son statut ? | Rappel de statut ; document et preuve récupérables par l'API |
| W5 | Le candidat ou l'employé signe-t-il comme externe (lien) ou interne (SSO) ? | Externe pour un candidat, interne pour un employé |
| W6 | IQ fournira-t-il un locataire Workday de test et un compte d'intégration ? | Non obtenu |

## P. Provisionnement des comptes (Saviynt / SCIM)

| # | Question | Hypothèse actuelle |
|---|---|---|
| P1 ★ | Saviynt pousse-t-il les comptes par SCIM 2.0, ou par un autre connecteur ? | Point SCIM 2.0 à réaliser |
| P2 | Quels attributs et quels groupes sont transmis (rôle, unité, direction, statut) ? | Courriel, nom, actif, groupes |
| P3 | Un départ doit-il désactiver le compte immédiatement, et ses trousses être réaffectées ? | Désactivation ; réaffectation manuelle |
| P4 | Les comptes créés par SCIM doivent-ils aussi se connecter par SSO uniquement ? | Oui |

## O. Microsoft 365

| # | Question | Hypothèse actuelle |
|---|---|---|
| O1 | Quels usages de Microsoft 365 sont attendus : dépôt du document signé dans SharePoint, avis dans Teams, ouverture depuis Outlook ? | Dépôt SharePoint et avis Teams, à réaliser pour le module |
| O2 | Quelle bibliothèque SharePoint et quelle nomenclature de dossiers pour les documents signés ? | Non connue |

## V. Signature en lot (AF-47)

| # | Question | Hypothèse actuelle |
|---|---|---|
| V1 | Quelle taille de lot maximale et quelle fréquence ? | 500 dossiers par lot, traitement synchrone |
| V2 | Dans un lot, le document est-il le même pour tous (entente type) ou propre à chaque dossier ? | Propre à chaque dossier, généré par le même modèle |
| V3 | Un lot doit-il pouvoir être approuvé globalement avant l'envoi en signature ? | Chaque trousse suit son parcours |

