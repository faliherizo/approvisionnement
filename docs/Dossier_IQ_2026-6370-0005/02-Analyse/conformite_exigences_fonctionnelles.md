# Conformité des exigences fonctionnelles — AO 2026-6370-0005

Analyse exhaustive des sources [`01-Sources/`](../01-Sources/) et de l'[addenda 1](../09-Questions/reponses/addenda-1.md),
au 2026-11-12. Chaque exigence fonctionnelle y est relevée avec sa source, son caractère (obligatoire ou non) et la
conformité de la solution, preuve à l'appui.

## 1. Méthode

| Source | Parties analysées |
|---|---|
| Cahier des besoins et exigences | § 1.3 à 1.8 : solution recherchée, architecture, intégration, génération, exigences fonctionnelles détaillées, efficacité, options, technologie, conformité de la signature, sécurité, environnement technologique |
| Document d'appel d'offres (DAO) | § 3.1.1 (besoin visé), § 3.3.1 à 3.3.8 (normes, accès, langue, intégrations, API), § 7.3 (scénarios **obligatoires** de démonstration) |
| Addenda 1 (9 novembre 2026) | Réponses Q1 à Q11, nouvelle exigence TI-22, précisions § 4.1 à 4.5 |

**Caractère.** Le Cahier (§ 1.2) fixe la règle : « doit », « devra », « est tenu de » et « obligatoire » expriment une
obligation, alors que « peut » et « pourra » expriment une faculté. Le § 1.4.9 est « optionnel ». L'addenda (§ 4.3) rend
optionnelles les capacités CLM avancées, et ajoute l'exigence **obligatoire** TI-22. L'Annexe 10 (Excel) et ses niveaux
de criticité ne figurent pas dans les PDF ; les identifiants qu'elle porte (AF-43, AF-47, TI-22, SIG-07…) sont repris
quand l'addenda les cite.

**Hors de cette matrice.** Les engagements du fournisseur ne sont pas des fonctions de la solution : hébergement,
attestations ISO 27001 ou SOC 2, questionnaire CAIQ, niveaux de service, équipe, formation, prix. Ils sont résumés au § 5.

Légende : ✅ conforme · ✅🆕 conforme depuis cette analyse (écart comblé le 2026-11-12) · ⚠ non réalisé.

## 2. Résultat

| | Obligatoires | Non obligatoires (optionnelles, facultés, préférences, conditionnelles) |
|---|---|---|
| Exigences fonctionnelles relevées | **129** (107 + 8 de l'addenda 2 + 6 de l'addenda 3, dont A-08 devenue obligatoire, + 8 de l'addenda 4) | **11** |
| Conformes | **129 (100 %)**, dont 13 rendues conformes par les 7 écarts comblés ci-dessous, 6 par l'addenda 2 ([impact_addenda_2.md](impact_addenda_2.md)), 6 par l'addenda 3 ([impact_addenda_3.md](impact_addenda_3.md)) et 6 par l'addenda 4 ([impact_addenda_4.md](impact_addenda_4.md)) ; A-29 et A-30 conformes avec des limites déclarées, comme ces exigences le permettent | **11**, dont A-09 (connecteur MCP, [ADR-023](../../adr/ADR-023-connecteur-mcp-clm.md)) et D-14 (PCI DSS hors périmètre, vérifiable, [ADR-022](../../adr/ADR-022-cartes-de-paiement-hors-perimetre.md)) |
| Non réalisées | 0 | 0 |
| Sans objet | — | 0 |

### Écarts trouvés et comblés

| # | Exigence (source) | Écart constaté dans le code | Réalisation |
|---|---|---|---|
| 1 | C-30, C-31, C-55, S-05 : ajout et remplacement de signataires en cours de processus | L'ajout d'un participant exigeait une trousse au brouillon : impossible d'ajouter un nouveau signataire après le lancement | Participants ajoutés ou retirés tant que la trousse n'est pas close ; remplacement à chaud sur l'étape (invitation annulée, nouvelle invitation), tracé au journal |
| 2 | S-05 : « mise à jour d'info (changement de signature ou d'une donnée) et impact sur le processus et les traces d'audit » | Aucun document ni donnée modifiable après le lancement | Document ajouté, remplacé, régénéré ou retiré en cours de parcours : **nouveau cycle** depuis la première phase ; signatures de l'ancienne version passées au statut « invalidée » ; journal `NOUVEAU_CYCLE` ; données corrigeables |
| 3 | S-09 : « retour du document à l'étape appropriée » | Le retour renvoyait toujours au brouillon de l'initiateur | Choix de l'étape antérieure visée (« Retourner à ») ; le parcours reprend depuis sa phase, sans brouillon ; journal `RETOUR_ETAPE` |
| 4 | C-19, C-33, C-67, D-15 : groupes d'utilisateurs (attribution, partage des modèles, accès par groupe ou unité) | Les groupes n'étaient administrables qu'avec la « sécurité avancée », que le tenant IQ ne souscrit pas | Groupes administrés dans le module (écran Groupes) ; un groupe portant le code d'une unité ouvre les trousses de cette unité |
| 5 | C-65, C-66, D-11 : conservation pendant la période requise et destruction « au moment opportun » (Loi sur les archives) | Aucune durée de conservation, aucune destruction | Calendrier par type de contrat (durée après clôture, sort final), échéance calculée, gel légal, versement, destruction confirmée avec bordereau au journal scellé |
| 6 | C-63 : « standards reconnus de l'industrie » en signature | Preuve scellée par un HMAC propre à la plateforme | Cachet électronique X.509 : **CAdES** (RFC 5652, attribut ESS signing-certificate-v2) sur chaque document signé, **PAdES** sur le certificat, horodatage **RFC 3161** si une autorité est configurée ; vérifiable par `openssl` ou tout lecteur PDF |
| 7 | D-27 : échange de modèles avec les systèmes d'IQ | L'API d'intégration n'exposait pas le catalogue | `GET /api/clm/integration/v1/modeles` : modèles publiés et leurs champs |

Preuves automatisées des écarts comblés : `ConformiteExigencesIntegrationTest` (groupes, signataire ajouté et
remplacé, nouveau cycle, retour à l'étape, conservation et gel légal), `ConservationServiceTest` (destruction et
versement à l'échéance), `ConformiteDomaineTest`, `CachetX509Test` (CAdES, PAdES, RFC 3161 avec une autorité de test),
`ClmIntegrationTest` (certificat PAdES et `.p7s` dans le dossier de preuve), e2e
`Conformité — écarts comblés` (`e2e/views/clm.exigences.spec.ts`).

## 3. Matrice — Cahier des besoins et exigences

| # | § | Exigence | Caractère | Conformité | Satisfaite par (écran, mécanisme, test) |
|---|---|---|---|---|---|
| C-01 | 1.3 | Gérer le cycle de validation, d'approbation et de signature, orchestration comprise | Obligatoire | ✅ | Trousses, parcours, portail ; e2e F-05 |
| C-02 | 1.3 | Contexte multi-juridictionnel, solution conforme, modulaire, évolutive | Obligatoire | ✅ | Interface FR/EN ; normes de signature indépendantes du pays (C-63) ; modules activables |
| C-03 | 1.3 | Service de signature appelable, distinct de l'orchestration (couplage lâche) | Principe souhaité | ✅ | Port `FournisseurSignature`, séparé du moteur de parcours |
| C-04 | 1.3 | Capacités avancées du cycle de vie des contrats | Optionnelle (addenda § 4.3) | ✅ | Tâches assignées, clauses, listes administrables ; option `clm_ia` |
| C-05 | 1.4.3 | Offerte en infonuagique, connectable aux SaaS | Obligatoire | ✅ | Déploiement Azure (Terraform) ; API REST, OAuth 2.0 |
| C-06 | 1.4.3 | Connexion aux applications « on premise » par API | Obligatoire | ✅ | API d'intégration entrante et rappels signés ([guide](../04-Architecture/api_integration_entrante.md)) |
| C-07 | 1.4.3 | Protection et confidentialité des données | Obligatoire | ✅ | Refus par défaut, isolation par organisation, secrets chiffrés, journaux sans renseignement personnel |
| C-08 | 1.4.4 | Échange avec les systèmes de référence : préalimenter, retourner les statuts | Obligatoire | ✅ | Salesforce (lecture, retour `PATCH`), Workday et autres (API, rappels) |
| C-09 | 1.4.5 | Récupération automatisée des données Salesforce Grantmaking | Obligatoire | ✅ | `SalesforceRest` ; bac à sable REST ; e2e F-02 |
| C-10 | 1.4.5 | Génération automatisée des contrats depuis les données Salesforce | Obligatoire | ✅ | Modèle publié → **Générer depuis le modèle** |
| C-11 | 1.4.5 | Préremplissage des modèles approuvés | Obligatoire | ✅ | Champ du modèle → source Salesforce ; provenance et date |
| C-12 | 1.4.5 | Acheminement des documents générés vers validation, approbation, signature | Obligatoire | ✅ | Parcours du modèle copié, **Lancer le parcours** |
| C-13 | 1.4.5 | Retour vers Salesforce des informations de signature | Obligatoire | ✅ | Boîte d'envoi, champs `CLM_*__c`, idempotence |
| C-14 | 1.4.5 | Traçabilité dossier source → document → preuve | Obligatoire | ✅ | Corrélation au dossier de preuve |
| C-15 | 1.4.5 | Erreurs d'intégration, reprises, transactions incomplètes | Obligatoire | ✅ | Reprises, file d'échec, **Relancer l'événement** ; essai de panne en bac à sable |
| C-16 | 1.4.5 | Interfaces standard, documentées et supportées par l'éditeur | Obligatoire | ✅ | API REST Salesforce v60, OAuth 2.0 client credentials |
| C-17 | 1.4.6 | Flexibilité et agilité tout au long de l'utilisation | Obligatoire | ✅ | Listes, paramètres, messages, modèles, parcours administrables par l'organisation |
| C-18 | 1.4.7 | Circuits de validation et d'approbation séquentiels, parallèles, mixtes | Obligatoire | ✅ | Phases et étapes parallèles ; e2e F-05 |
| C-19 | 1.4.7 | Attribution selon rôles, groupes ou règles d'affaires | Obligatoire | ✅🆕 | Affectation utilisateur, rôle, groupe (groupes du module), règle conditionnelle |
| C-20 | 1.4.7 | Ajout, retrait, remplacement d'un validateur ou approbateur en cours | Obligatoire | ✅ | **Réassigner, ajouter ou retirer des intervenants** |
| C-21 | 1.4.7 | Réassignation d'une étape sans redémarrage | Obligatoire | ✅ | Délégation ; réassignation à chaud |
| C-22 | 1.4.7 | Approuver, rejeter, retourner pour correction | Obligatoire | ✅ | Boîte de tâches ; e2e F-05 |
| C-23 | 1.4.7 | Commentaires et historique des validations | Obligatoire | ✅ | **Ajouter un commentaire**, « Historique des validations » |
| C-24 | 1.4.7 | Notifications et rappels configurables pour validateurs et approbateurs | Obligatoire | ✅ | UI-17, rappels par trousse, préavis d'échéance |
| C-25 | 1.4.7 | Suivi de l'état des validations en cours | Obligatoire | ✅ | Onglet Parcours, suivi centralisé |
| C-26 | 1.4.7 | Signature séquentielle, parallèle, mixte | Obligatoire | ✅ | Étapes de signature par phase |
| C-27 | 1.4.7 | Groupes de signataires | Obligatoire | ✅ | Plusieurs signataires sur une étape, avec quorum |
| C-28 | 1.4.7 | Signature simultanée et consécutive configurable | Obligatoire | ✅ | Même phase ou phases successives |
| C-29 | 1.4.7 | Association automatique des étapes aux signataires | Obligatoire | ✅ | Rôle du contrat → zone de signature ; parcours par défaut d'un lot |
| C-30 | 1.4.7 | Modification et ajout de signataires en cours de processus | Obligatoire | ✅🆕 | Onglet Participants ouvert en cours de parcours ; e2e « signataire ajouté en cours » |
| C-31 | 1.4.7 | Réassignation de signataires sans reprise complète | Obligatoire | ✅🆕 | Remplacement à chaud : invitation annulée et nouvelle envoyée ; test d'intégration |
| C-32 | 1.4.7 | Modèles réutilisables | Obligatoire | ✅ | Catalogue des modèles ; e2e F-03 |
| C-33 | 1.4.7 | Partage des modèles par groupe d'usagers | Obligatoire | ✅🆕 | **Partage par groupes** + groupes du module ; test d'intégration |
| C-34 | 1.4.7 | Ancrage précis des champs | Obligatoire | ✅ | **Positionner sur le document** (page, x, y, taille) |
| C-35 | 1.4.7 | Types : texte validé, liste, date, case à cocher | Obligatoire | ✅ | e2e F-04 |
| C-36 | 1.4.7 | Distinguer champs de formulaire, de saisie et de signature | Obligatoire | ✅ | Types distincts, zones de signature à part dans l'éditeur |
| C-37 | 1.4.7 | Attribution des champs à des signataires | Obligatoire | ✅ | **Attribuer la zone — …** |
| C-38 | 1.4.7 | Règles conditionnelles d'affichage | Obligatoire | ✅ | **Définir une condition** |
| C-39 | 1.4.7 | Saisie assistée (suggestions) | Obligatoire | ✅ | Suggestions des valeurs déjà retenues ; e2e F-04 |
| C-40 | 1.4.7 | Notifications activables et désactivables | Obligatoire | ✅ | **Désactiver — / Activer —** ; e2e F-07 |
| C-41 | 1.4.7 | Rappels automatiques de non-réponse | Obligatoire | ✅ | Planificateur ; `RappelServiceTest` |
| C-42 | 1.4.7 | Destinataires en copie | Obligatoire | ✅ | **Destinataires en copie** |
| C-43 | 1.4.7 | Vue de suivi centralisée : étapes, attentes, exceptions | Obligatoire | ✅ | Suivi centralisé ; e2e F-08 |
| C-44 | 1.4.7 | Plusieurs documents par trousse | Obligatoire | ✅ | e2e F-01 |
| C-45 | 1.4.7 | Dossiers, documents, versions, intervenants | Obligatoire | ✅ | Onglets Documents, Participants, Parcours |
| C-46 | 1.4.7 | PDF et Word | Obligatoire | ✅ | e2e F-01 (PDF + .docx) |
| C-47 | 1.4.7 | Téléversement d'au moins 25 Mo | Obligatoire | ✅ | 25 Mo par défaut, réglable |
| C-48 | 1.4.7 | Historique des étapes de validation, d'approbation, de signature | Obligatoire | ✅ | Journal scellé de la trousse |
| C-49 | 1.4.7 | Journal d'audit des actions (consultation, validation, signature, rejet, annulation…) | Obligatoire | ✅ | Historique ; journal d'audit UI-23 |
| C-50 | 1.4.7 | État et historique des transactions d'un document | Obligatoire | ✅ | **Versions** → métadonnées de version |
| C-51 | 1.4.7 | Conservation et consultation du dossier de preuve | Obligatoire | ✅ | Onglet Preuve |
| C-52 | 1.4.8 | Minimiser le nombre d'étapes | Obligatoire | ✅ | Assistant en 5 étapes, préremplissage, lots |
| C-53 | 1.4.8 | Exécution fluide web et mobile | Obligatoire | ✅ | Écrans contrôlés à 390, 768, 1280, 1680 px |
| C-54 | 1.4.8 | Limiter les dépendances entre signataires | Obligatoire | ✅ | Signatures parallèles |
| C-55 | 1.4.8 | Flexibilité des workflows en cours de processus | Obligatoire | ✅🆕 | Réassignation, ajout de signataire, nouveau cycle, retour à l'étape |
| C-56 | 1.4.8 | Minimiser les interventions manuelles | Obligatoire | ✅ | Préremplissage, rappels, lots, API |
| C-57 | 1.4.8 | Expérience simple pour initiateurs, signataires internes et externes | Obligatoire | ✅ | Portail du signataire à cibles de 44 px, WCAG 2.2 AA |
| C-58 | 1.4.8 | Personnalisation : image de marque, messages, parcours | Obligatoire (à préciser) | ✅ | Nom et logo au portail, messages UI-17, parcours configurables |
| C-59 | 1.4.9 | Automatisation et IA (extraction, classification, anomalies) | **Optionnelle** | ✅ | Option `clm_ia`, révision humaine ; e2e F-13 |
| C-60 | 1.4.10 | Identification et authentification des utilisateurs (SSO) | Obligatoire | ✅ | OIDC et SAML 2.0, testés contre Keycloak |
| C-61 | 1.4.10 | Authentification et vérification d'identité selon profil, document et risque | Obligatoire | ✅ | Niveau minimal par risque et par type de signataire (UI-22) |
| C-62 | 1.4.10 | Méthodes internes et externes, authentification renforcée | Obligatoire | ✅ | Courriel, SMS, question, combinée ; SSO et second facteur |
| C-63 | 1.4.11 | Standards reconnus de signature électronique, validité juridique | Obligatoire | ✅🆕 | CAdES, PAdES, RFC 3161, X.509 (`CachetX509`) ; certificat d'une autorité reconnue en production (§ 5) |
| C-64 | 1.4.11 | Piste d'audit complète : événements, authentification, horodatages, consentements, intégrité | Obligatoire | ✅ | Journal scellé, certificat, vérification d'intégrité ; e2e F-09 |
| C-65 | 1.4.11 | Conservation, export, portabilité durables pendant la période requise | Obligatoire | ✅🆕 | Archive ZIP (documents, `.p7s`, certificat PAdES, journal, manifeste) ; calendrier de conservation |
| C-66 | 1.4.12 | Protection du cycle de vie, dont la destruction au moment opportun (Loi sur les archives) | Obligatoire | ✅🆕 | Écran **Conservation et destruction** : gel légal, versement, destruction confirmée |
| C-67 | 1.4.12 | Accès, modèles, workflows, historiques selon rôles, groupes, unités, domaines | Obligatoire | ✅🆕 | Rôles, groupes du module, unités ; domaine d'affaires porté par groupe ou unité |
| C-68 | 1.7.1 | Compatible postes Windows, mobiles, navigateurs Edge, Chrome, Safari | Obligatoire | ✅ | Interface web adaptative ; e2e Chromium à 4 largeurs |
| C-69 | 1.7.1 | S'intégrer à Microsoft 365, Salesforce, Workday : API, connecteurs, protocoles | Obligatoire | ✅ | Salesforce (connecteur), Workday (API et rappels), Entra ID (OIDC/SAML), Outlook (courriel) ; Teams et SharePoint via l'API et les rappels |
| C-70 | 1.7.1 | Compatibilité maintenue avec l'évolution de l'environnement | Obligatoire | ✅ | Versions d'API paramétrables, standards ouverts |

## 4. Matrice — Document d'appel d'offres et addenda

| # | § | Exigence | Caractère | Conformité | Satisfaite par |
|---|---|---|---|---|---|
| D-01 | 3.1.1 | Génération automatisée ou semi-automatisée depuis des modèles approuvés | Obligatoire | ✅ | C-10 |
| D-02 | 3.1.1 | Données des systèmes sources, dont Salesforce | Obligatoire | ✅ | C-09, C-08 |
| D-03 | 3.1.1 | Préparation et gestion des documents à valider, approuver, signer | Obligatoire | ✅ | C-44 à C-47 |
| D-04 | 3.1.1 | Circuits séquentiels, parallèles, mixtes | Obligatoire | ✅ | C-18, C-26 |
| D-05 | 3.1.1 | Utilisateurs, signataires, rôles, droits, **délégations** | Obligatoire | ✅ | Comptes, rôles, **Déléguer si autorisé** |
| D-06 | 3.1.1 | Modèles et clauses contractuelles | Obligatoire | ✅ | Éditeur de modèles, clauses conditionnelles |
| D-07 | 3.1.1 | Automatisation des notifications, rappels, règles d'affaires | Obligatoire | ✅ | C-24, C-41, C-38 |
| D-08 | 3.1.1 | Suivi centralisé | Obligatoire | ✅ | C-43 |
| D-09 | 3.1.1 | Versions et historique des actions | Obligatoire | ✅ | C-48 à C-50 |
| D-10 | 3.1.1 | Piste d'audit complète | Obligatoire | ✅ | C-64 |
| D-11 | 3.1.1 | Conservation et exportation : documents, preuves, pistes, métadonnées | Obligatoire | ✅🆕 | C-65 |
| D-12 | 3.1.1 | Intégration aux systèmes d'IQ | Obligatoire | ✅ | C-08, C-69 |
| D-13 | 3.3.1 A | Accessibilité des interfaces, niveau maintenu aux mises à jour | Obligatoire | ✅ | WCAG 2.2 AA, axe-core sans violation dans chaque test e2e |
| D-14 | 3.3.1 B | PCI DSS si des cartes de paiement sont traitées | Conditionnelle | ✅🆕 | Aucune carte traitée, et c'est vérifié : tout numéro de carte (préfixe de réseau, longueur, clé de Luhn) est refusé à la frontière par un 422, sans être journalisé ; la plateforme reste hors du périmètre PCI DSS ([ADR-022](../../adr/ADR-022-cartes-de-paiement-hors-perimetre.md)) |
| D-15 | 3.3.2 | Gestion des rôles et des droits d'accès | Obligatoire | ✅🆕 | Rôles, groupes du module |
| D-16 | 3.3.2 | Principe du moindre privilège | Obligatoire | ✅ | Refus par défaut ; contrôle serveur de chaque action (tests d'architecture) |
| D-17 | 3.3.2 | Gestion distincte des utilisateurs et des administrateurs | Obligatoire | ✅ | Rôle `ADMIN` et espaces d'administration séparés |
| D-18 | 3.3.2 | Révocation ou modification des accès | Obligatoire | ✅ | **Retirer l'accès**, **Attribuer le rôle**, révocation des clients d'intégration |
| D-19 | 3.3.2 | Journalisation et traçabilité des activités | Obligatoire | ✅ | Journal d'audit, changements de comptes et de rôles |
| D-20 | 3.3.2 | Protection des renseignements et documents | Obligatoire | ✅ | C-07, antivirus, destruction (C-66) |
| D-21 | 3.3.3 | Interfaces en français, obligatoirement | Obligatoire | ✅ | Interface française complète |
| D-22 | 3.3.3 | Offertes en anglais lorsque requis | Obligatoire | ✅ | Sélecteur **Langue** ; e2e F-12 |
| D-23 | 3.3.3 | Consoles d'administration en français | Obligatoire | ✅ | Toutes les consoles du module en français |
| D-24 | 3.3.4 | Mode SaaS principalement | Obligatoire | ✅ | C-05 |
| D-25 | 3.3.4 | Fonctionnalités natives privilégiées, peu de développements spécifiques | Préférence | ✅ | Configuration par l'organisation (listes, paramètres, messages, modèles) |
| D-26 | 3.3.5 | Intégrations avec l'écosystème, dont Salesforce | Obligatoire | ✅ | C-08, C-69 |
| D-27 | 3.3.5 | Échanges de données, documents, modèles, statuts, signataires, instructions, preuves | Faculté (« pourront ») | ✅🆕 | API d'intégration : trousses, lots, documents, preuve, statuts ; **catalogue des modèles** |
| D-28 | 3.3.6 | API et connecteurs sécurisés, documentés, maintenus | Obligatoire | ✅ | OAuth 2.0, HMAC, OpenAPI, guide d'intégration |
| D-29 | 3.3.8 | Interopérabilité permettant l'évolution | Obligatoire | ✅ | Ports et adaptateurs, API documentées |
| S-01 | 7.3 sc. 1 | Création : trousse, documents, participants, positionnement des champs, modèle, configuration | Obligatoire | ✅ | e2e F-01, F-02, F-03 |
| S-02 | 7.3 sc. 2 | Validation : réception, consultation, commentaires, validation, passage à l'étape suivante | Obligatoire | ✅ | e2e F-05 |
| S-03 | 7.3 sc. 3 | Approbation, rejet, retour pour correction, historique | Obligatoire | ✅ | e2e F-05, `ClmIntegrationTest` |
| S-04 | 7.3 sc. 4 | Signature externe : invitation, authentification, consultation, signature, confirmation | Obligatoire | ✅ | e2e portail (`clm.view.spec.ts`), F-06 |
| S-05 | 7.3 sc. 5 | Ajout, remplacement, délégation, traçabilité ; mise à jour d'une donnée et son impact | Obligatoire | ✅🆕 | Signataire ajouté et remplacé, nouveau cycle, journal |
| S-06 | 7.3 sc. 6 | Rappels configurés, notifications, rappels manuels, alertes d'échéance, communications personnalisées | Obligatoire | ✅ | UI-17, **Relancer l'étape**, préavis d'échéance ; e2e F-07 |
| S-07 | 7.3 sc. 7 | Journal, horodatages, actions, authentification, dossier de preuve | Obligatoire | ✅ | Onglets Historique et Preuve ; e2e F-09 |
| S-08 | 7.3 sc. 8 | Consultation et décision sur mobile, navigation mobile | Obligatoire | ✅ | Écrans à 390 px ; portail mobile |
| S-09 | 7.3 sc. 9 | Refus, commentaires justificatifs, retour à l'étape appropriée, reprise, historique | Obligatoire | ✅🆕 | **Retourner à** une étape antérieure ; e2e « Scénario 9 » |
| S-10 | 7.3 sc. 10 | Téléchargement du signé, export du certificat, du journal, des métadonnées ; formats ; portabilité | Obligatoire | ✅ | Preuve PAdES et CAdES, ZIP, CSV ; e2e F-09, F-11 |
| S-10b | 7.3 sc. 10 | Gestion de bout en bout d'un CLM | Optionnelle (addenda § 4.3) | ✅ | Modèles, trousses, tâches, conservation, IA |
| A-01 | Add. § 4.4 | **TI-22** : intégration Workday (déclenchement, statuts, document signé) | **Obligatoire** | ✅ | API d'intégration et rappels ; e2e TI-22 |
| A-02 | Add. Q10 | **AF-47** : lot de signatures depuis Salesforce | Obligatoire (« doit ») | ✅ | `POST /v1/lots` |
| A-03 | Add. Q10 | AF-47 : lot depuis la plateforme | Faculté (« peut ») | ✅ | Écran **Signature en lot** ; e2e AF-47 |
| A-04 | Add. Q4 | SIG-07/08 : méthodes d'authentification standard et renforcée décrites | Obligatoire | ✅ | C-61, C-62 ; [impact_addenda_1.md](impact_addenda_1.md) § 2.5 |
| A-05 | Add. Q1 | CLM optionnel intégrable à Salesforce lorsqu'il est proposé | Conditionnelle | ✅ | API et préremplissage Salesforce |
| A-06 | Add. Q8 | Environ 30 champs Salesforce préremplis par modèle | Obligatoire (capacité) | ✅ | Correspondance par champ, sans plafond ; champs personnalisés lus |
| A-07 | Add. Q3 | Intégrations Microsoft 365, Workday, Entra ID | Selon l'Annexe 10 | ✅ | C-69, A-01, C-60 |
| A-08 | Add. Q3, add. 3 Q51 | Provisionnement et déprovisionnement SCIM 2.0 (Saviynt) | **Obligatoire** (add. 3 : « doit prendre en charge ») | ✅ | RST-26 : point `/scim/v2` ; [impact_addenda_3.md](impact_addenda_3.md) |
| A-09 | Add. Q3 | Connecteur MCP | Non exigé | ✅🆕 | Connecteur MCP en lecture seule (`/api/clm/integration/mcp`, outils `lister_modeles`, `lire_trousse`, `lire_lot`), authentifié par les clients d'intégration ([ADR-023](../../adr/ADR-023-connecteur-mcp-clm.md)). Connecteur additionnel hors de la portée de base : à décrire et à **tarifer séparément** dans l'offre (réponse à Q3) |
| A-10 | Add. 2 Q11 | Authentification Salesforce de remplacement (JWT Bearer) décrite | Obligatoire (« doit également décrire ») | ✅ | RST-23 ; [impact_addenda_2.md](impact_addenda_2.md) |
| A-11 | Add. 2 Q13 | Exportation ou transfert des documents et métadonnées vers un référentiel désigné par IQ | Obligatoire (« doit également permettre ») | ✅ | RST-24 : exportation en lot ZIP + index |
| A-12 | Add. 2 Q14 | Retour des statuts lancée, retournée, rejetée, annulée, terminée | Obligatoire (« doit notamment permettre ») | ✅ | RST-20 (retour d'étape), RST-21 (correspondance configurable) |
| A-13 | Add. 2 Q15 | Seules les données nécessaires transférées depuis Salesforce | Obligatoire (« devront ») | ✅ | RST-22 : lecture `fields=` limitée |
| A-14 | Add. 2 Q17 | Limites d'API, surveillance, journalisation, reprise (8 tentatives, plafond 1 h) | Obligatoire (« doit ») | ✅ | Boîte d'envoi, `Relances`, alertes |
| A-15 | Add. 2 Q19 | Interfaces internes et externes en français, en anglais sur demande d'IQ | Obligatoire (« doivent obligatoirement ») | ✅ | Interfaces FR/EN, RST-25 (langue du signataire) |
| A-16 | Add. 2 Q4 | Démontrer la volumétrie (55 000 à 65 000 documents par an) et des variations raisonnables | Obligatoire (« doit démontrer ») | ✅ | Plan JMeter `perf/clm-trousses-load.jmx` |
| A-18 | Add. 3 Q26 | Génération Word modifiable avant l'approbation finale ; nouvelle version et reprise des validations ; document à signer figé | Obligatoire (« doit pouvoir ») | ✅ | RST-28 ; nouveau cycle |
| A-19 | Add. 3 Q43 | Mappage configurable des groupes et attributs Entra ID vers les rôles, moindre privilège, traçabilité | Obligatoire (« doit permettre ») | ✅ | RST-27 |
| A-20 | Add. 3 Q44 | Domaine d'affaires alimenté par un groupe ou un attribut d'annuaire, séparation logique | Obligatoire (« doit permettre ») | ✅ | RST-27, groupes du module |
| A-21 | Add. 3 Q46-Q48 | Conservation par catégorie, gel légal, trois sorts finaux, bordereau ou certificat de destruction | Obligatoire (« doit ») | ✅ | ADR-012, RST-29 |
| A-22 | Add. 3 Q39 | Mesure de l'utilisation par enveloppe envoyée, sans double comptage | Obligatoire (« doit être ») | ✅ | RST-30 |
| A-23 | Add. 4 Q1 | Niveaux d'authentification configurables selon le type de document et le processus | Obligatoire (« doit permettre ») | ✅ | RST-35 |
| A-24 | Add. 4 Q6 | Signature par un représentant autorisé ; preuve d'autorité ; qualité du signataire tracée | Obligatoire (« doit permettre ») | ✅ | RST-32 |
| A-25 | Add. 4 Q8, Q20 | Parcours conditionnels selon type, unité, rôle, montant | Obligatoire (« doit permettre ») | ✅ | RST-36, règles sur les champs |
| A-26 | Add. 4 Q10 | Séparation de l'administration d'affaires et de l'administration technique | Obligatoire (« doit permettre ») | ✅ | RST-31, ADR-017 |
| A-27 | Add. 4 Q31 | Mappage des attributs et groupes SCIM, désactivation rapide | Obligatoire (« doit permettre ») | ✅ | RST-26, RST-34 |
| A-28 | Add. 4 Q36 | Approbation adaptée au lot, traçabilité par dossier | Obligatoire (« doit permettre ») | ✅ | RST-33 |
| A-29 | Add. 4 Q14 | Export dans des formats pérennes, PDF/PDF-A **lorsque disponibles**, export massif | Obligatoire (export) ; PDF/A conditionnel | ✅ | Export en lot ; documents produits en PDF/A-2b, scellés PAdES-B-LTA (RST-37, `ConformitePdfATest`, `PadesLtaTest`) |
| A-30 | Add. 4 Q22 | Modèles Word d'IQ, structure et présentation préservées **dans la mesure permise** ; limites déclarées | Obligatoire avec réserve | ✅ | Gabarit Word d'origine fusionné, mise en page conservée ; PDF par LibreOffice (RST-38, `FusionDocxTest`, `Addenda4IntegrationTest`) |
| A-17 | Add. 2 Q7 | `FundingRequest` lu par Id ou `Name`, correspondance configurable par modèle | Obligatoire (« devra ») | ✅ | C-08, C-09 |

## 5. Engagements du fournisseur et exigences non fonctionnelles (hors matrice)

| Sujet | Source | État |
|---|---|---|
| Hébergement au Québec ou dans une juridiction équivalente, preuves de localisation | Cahier § 1.4.2 | Azure Canada prévu ; clause prévalente à confirmer (E1) |
| Trois environnements isolés (DEV, préproduction, PRD), données anonymisées | Cahier § 1.8 | Prévus (Terraform) ; à provisionner |
| Certificat d'une autorité reconnue pour le cachet, autorité d'horodatage qualifiée | Cahier § 1.4.11 | Mécanisme livré et validé le 2026-11-20 contre une ICP réelle en bac à sable (PAdES-B-LTA par pyHanko, attestations CAdES par OpenSSL, révocation détectée) ; certificat et autorité à désigner par IQ (C1, C3) : variables `CLM_CACHET_MAGASIN`, `CLM_CACHET_TSA_URL` |
| ISO 27001, SOC 2 Type II, CAIQ, audit annuel indépendant | Cahier § 1.4.11, § 1.4.12 | Attestations de l'hébergeur ; audit à planifier |
| Niveaux de service, disponibilité 99,5 %, rapports | Cahier § 5, DAO § 3.3.10 et suivants | Grille à trancher (J1) |
| Formation, transfert, stabilisation de 30 jours ouvrables | Cahier § 4, addenda Q6 | Plan de mise en œuvre |

## 6. Comment vérifier

```bash
# Backend : écarts comblés et non-régression
cd backend && mvn -o test -Dtest='ConformiteExigencesIntegrationTest,ConservationServiceTest,ConformiteDomaineTest,CachetX509Test,ClmIntegrationTest'
# Interface : un test par exigence F-01 à F-13, addenda et conformité (données : e2e/data/clm-exigences.json)
cd e2e && npx playwright test views/clm.exigences.spec.ts
# Signature CMS d'un document signé, extraite du dossier de preuve
openssl cms -verify -inform DER -binary -in <document>.p7s -content <document> -noverify
```

Voir aussi : [validation_exigences_fonctionnelles.md](validation_exigences_fonctionnelles.md) (comment tester chaque
exigence depuis l'interface), [impact_addenda_1.md](impact_addenda_1.md),
[questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md).
