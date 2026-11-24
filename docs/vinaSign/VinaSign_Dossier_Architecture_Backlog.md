# VinaSign --- Analyse, dossier d'architecture et backlog MVP

**Version :** 1.0\
**Date :** 22 novembre 2026\
**Organisation :** Vina IT Solution\
**Statut :** Dossier initial de cadrage --- à valider avant réalisation

------------------------------------------------------------------------

## 1. Résumé exécutif

VinaSign est une plateforme SaaS de signature électronique destinée aux
entreprises, PME, travailleurs autonomes et organisations qui souhaitent
préparer, faire signer, suivre et conserver des documents électroniques.

La différenciation proposée repose sur une expérience d'édition et de
signature particulièrement simple sur écran tactile : téléphone Android,
tablette, stylet ou doigt, ainsi que navigateur de bureau.

La recommandation est de construire un produit propriétaire pour les
fonctions qui créent la valeur --- éditeur tactile, gestion
documentaire, orchestration des workflows, gestion multi-tenant, audit
et API --- tout en intégrant des fournisseurs spécialisés pour certains
services de confiance (identification renforcée, horodatage qualifié ou
de confiance, certificats et services cryptographiques selon le niveau
juridique recherché).

**Principe directeur :** ne pas confondre le dessin d'une signature avec
la preuve d'une signature électronique. Le dessin constitue une
manifestation visuelle; le processus doit également établir le
consentement, l'association au document, l'intégrité, la traçabilité et,
selon le risque, l'identité du signataire.

### Décisions d'orientation proposées

  -----------------------------------------------------------------------
  Sujet                               Décision initiale
  ----------------------------------- -----------------------------------
  Produit                             SaaS multi-tenant sous marque
                                      VinaSign

  Interfaces                          Application Web responsive et PWA

  Appareils                           Android, tablettes, stylet/doigt,
                                      ordinateurs

  Frontend                            Angular

  Backend                             Java 21 / Spring Boot 3

  Documents                           PDF.js pour visualiser;
                                      bibliothèque PDF pour manipuler et
                                      finaliser

  Base de données                     PostgreSQL

  Stockage                            Stockage objet privé compatible
                                      Azure Blob ou S3

  Cache / tâches                      Redis et traitement asynchrone

  Identité                            OIDC pour les comptes;
                                      authentification adaptée pour les
                                      signataires invités

  Sécurité                            Isolation tenant, chiffrement,
                                      contrôle d'accès, audit
                                      inviolable/logiquement protégé

  Signature                           Signature visuelle et preuve de
                                      processus; PAdES via composant
                                      spécialisé selon le niveau requis

  Livraison                           MVP, V1, V2 par incréments
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 2. Vision produit et objectifs

### 2.1 Vision

Permettre à une organisation de transformer un document PDF en parcours
de signature fluide, sécurisé et traçable, sans imposer au signataire de
créer un compte ni d'installer une application.

### 2.2 Objectifs métier

1.  Réduire le délai de circulation et de signature des documents.
2.  Réduire les impressions, numérisations et échanges de courriels.
3.  Simplifier la signature manuscrite tactile.
4.  Donner à l'expéditeur une visibilité sur l'état de chaque demande.
5.  Fournir une preuve exploitable du processus de signature.
6.  Offrir un service SaaS configurable par organisation.
7.  Permettre l'intégration future avec CRM, ERP, RH, gestion
    documentaire et applications métier.

### 2.3 Utilisateurs et rôles

  -----------------------------------------------------------------------
  Rôle                                Responsabilités
  ----------------------------------- -----------------------------------
  Administrateur plateforme           Gestion globale du service,
                                      tenants, plans, paramètres
                                      techniques

  Administrateur tenant               Utilisateurs, rôles, politiques,
                                      modèles, consommation

  Préparateur / expéditeur            Importer le document, définir les
                                      signataires et placer les champs

  Signataire                          Consulter, accepter/refuser et
                                      signer le document

  Observateur                         Consulter l'état ou le document
                                      selon les permissions

  Auditeur                            Consulter les preuves et événements
                                      autorisés

  Support Vina                        Assistance encadrée, accès
                                      exceptionnel et journalisé
  -----------------------------------------------------------------------

Un signataire invité doit pouvoir ouvrir un lien sécurisé et accomplir
le parcours sans compte permanent, lorsque la politique du tenant le
permet.

------------------------------------------------------------------------

## 3. Périmètre fonctionnel

### 3.1 MVP --- indispensable

-   Création d'un tenant et gestion de ses utilisateurs.
-   Authentification des utilisateurs internes.
-   Import d'un PDF et validation du fichier.
-   Visualisation PDF multipage.
-   Navigation, zoom, déplacement et ajustement à la page.
-   Éditeur tactile : ajout, déplacement, rotation si utile et
    redimensionnement des champs.
-   Champ de signature manuscrite (doigt ou stylet).
-   Champs nom, texte, date, initiales et case à cocher.
-   Définition d'un ou plusieurs signataires.
-   Envoi d'une invitation par courriel.
-   Lien de signature sécurisé, expirant et révocable.
-   Consentement explicite avant signature.
-   Suivi des statuts.
-   Génération d'un PDF final.
-   Journal d'événements horodatés.
-   Empreinte SHA-256 des versions pertinentes.
-   Téléchargement du document final et des preuves.
-   Contrôles d'accès et séparation des tenants.

### 3.2 V1 --- industrialisation commerciale

-   Plusieurs signataires avec ordre séquentiel ou parallèle.
-   Relances et échéances.
-   OTP courriel ou SMS selon le niveau de risque.
-   Refus de signer avec motif facultatif.
-   Modèles de documents et modèles de champs.
-   Certificat de réalisation détaillé.
-   Export du dossier de preuve.
-   Tableau de bord et recherche.
-   Notifications configurables.
-   Politique de conservation et suppression.
-   Webhooks et API d'intégration.
-   Quotas et facturation par organisation.
-   Gestion des logos et personnalisation de marque.

### 3.3 V2 --- confiance avancée et intégrations

-   Signature numérique PDF PAdES via fournisseur ou composant
    cryptographique maîtrisé.
-   Horodatage de confiance.
-   Validation de certificats et conservation des éléments nécessaires à
    la validation à long terme.
-   Vérification d'identité renforcée selon les cas d'usage.
-   SSO entreprise (OIDC/SAML selon le besoin).
-   Connecteurs CRM/ERP/GED.
-   Signature en lot et automatisation.
-   Politiques avancées de rétention et de résidence des données.
-   Rapports d'audit et exports administratifs.

### 3.4 Hors périmètre initial

-   Service de certification d'identité opéré par Vina.
-   Autorité de certification propre à Vina.
-   Signature qualifiée au sens d'un régime étranger sans analyse
    juridique et technique spécifique.
-   Édition bureautique complète de fichiers Word/Excel.
-   Application native iOS/Android distincte.
-   Gestion de transactions financières ou de paiement.
-   Garantie universelle de validité juridique pour tout document et
    toute juridiction.

------------------------------------------------------------------------

## 4. Parcours utilisateurs

### 4.1 Préparer et envoyer une demande

1.  Le préparateur se connecte à VinaSign.
2.  Il crée une demande et importe un PDF.
3.  Le système valide le type, la taille, le nombre de pages et analyse
    le fichier.
4.  Le préparateur ajoute les signataires (nom, courriel, ordre,
    langue).
5.  Il choisit les champs requis et les positionne sur les pages.
6.  Il définit l'échéance et le niveau d'authentification.
7.  Il vérifie l'aperçu et confirme l'envoi.
8.  Le système fige la version préparée et envoie les invitations.

### 4.2 Signer sur téléphone ou tablette

1.  Le signataire ouvre le lien reçu.
2.  Le système vérifie le jeton, sa validité, son usage et les
    éventuelles exigences OTP.
3.  Le signataire consulte l'avis de consentement et les informations
    requises.
4.  Il visualise le document en mode adapté à l'écran.
5.  Il complète les champs obligatoires.
6.  Il dessine sa signature avec le doigt ou un stylet.
7.  Il peut recommencer avant de confirmer.
8.  Il confirme explicitement son intention de signer.
9.  Le backend enregistre l'événement, finalise le document et retourne
    une confirmation.
10. Le signataire peut télécharger sa copie si la politique le permet.

### 4.3 Refuser ou abandonner

Le signataire peut refuser lorsque cette option est activée. Le refus
est enregistré comme événement distinct; il ne doit jamais être
interprété comme une signature. Une session abandonnée n'est pas une
signature et peut être reprise tant que le lien est valide.

------------------------------------------------------------------------

## 5. Expérience de l'éditeur tactile

### 5.1 Principes UX

L'éditeur doit être utilisable sans précision excessive et sans dépendre
d'un stylet.

-   Afficher une page PDF à une échelle lisible.
-   Prévoir un mode « Ajuster à la largeur ».
-   Permettre le zoom par pincement et le déplacement par glissement.
-   Distinguer clairement le mode préparation du mode signature.
-   Afficher les champs avec des contours et libellés visibles.
-   Fournir une barre d'outils compacte : signature, initiales, texte,
    date, case à cocher.
-   Utiliser des poignées suffisamment grandes pour le tactile.
-   Éviter les gestes ambigus : le déplacement de page et le déplacement
    d'un champ ne doivent pas se déclencher simultanément.
-   Prévoir annuler/rétablir.
-   Maintenir un aperçu fidèle du PDF final.
-   Fournir une alternative clavier et des considérations
    d'accessibilité.

### 5.2 Modèle de coordonnées

Ne pas enregistrer la position d'un champ uniquement en pixels écran.
Enregistrer des coordonnées normalisées relativement à la page PDF, par
exemple :

-   `pageNumber`
-   `x`
-   `y`
-   `width`
-   `height`
-   `rotation`
-   `coordinateSystemVersion`

Les coordonnées normalisées doivent être indépendantes du zoom, de la
résolution et de l'appareil. La conversion entre coordonnées écran et
coordonnées PDF doit tenir compte de la taille réelle de la page, de la
rotation et du facteur de zoom.

### 5.3 Signature manuscrite

Le composant de dessin doit :

-   Capturer les points du tracé avec les événements pointeur.
-   Distinguer doigt, stylet et souris lorsque le navigateur le permet.
-   Lisser le tracé sans altérer excessivement le geste.
-   Permettre effacement et recommencement.
-   Produire une représentation vectorielle ou raster de qualité
    suffisante.
-   Conserver uniquement les données nécessaires au processus et selon
    une politique de confidentialité documentée.
-   Ne pas prétendre que la biométrie du tracé identifie de façon
    certaine une personne.

La signature dessinée est une représentation visuelle. La preuve
juridique dépend aussi du consentement, du processus, de l'identité ou
du niveau d'authentification, de l'intégrité et de la traçabilité.

### 5.4 Bibliothèques candidates

  -----------------------------------------------------------------------
  Besoin                  Option                  Remarque
  ----------------------- ----------------------- -----------------------
  Rendu PDF               PDF.js                  Très adapté à la
                                                  visualisation Web

  Manipulation PDF        pdf-lib                 Utile pour certaines
                                                  opérations côté client;
                                                  évaluer ses limites

  Manipulation serveur    Apache PDFBox           Option Java pour lire,
                                                  annoter et générer des
                                                  PDF

  Tracé tactile           Pointer Events +        Contrôle fin des gestes
                          Canvas/SVG              

  Composant de signature  Signature Pad ou        Évaluer maintenance,
                          composant équivalent    licence et
                                                  compatibilité

  Signature numérique PDF Bibliothèque            Analyse PAdES,
                          spécialisée /           certificats, horodatage
                          fournisseur             et validation requise
  -----------------------------------------------------------------------

Avant de retenir une bibliothèque, vérifier sa licence, sa maintenance,
ses vulnérabilités, ses limites PDF, le support des formulaires,
polices, annotations, rotations et documents complexes.

------------------------------------------------------------------------

## 6. Architecture logique

### 6.1 Vue d'ensemble

``` mermaid
flowchart TB
    subgraph Clients
      WEB[Angular Web]
      PWA[Angular PWA tactile]
    end

    WEB --> EDGE[HTTPS / Reverse Proxy / WAF]
    PWA --> EDGE
    EDGE --> API[Spring Boot API]

    API --> IAM[OIDC Identity Provider]
    API --> DOC[Document Service]
    API --> WF[Signature Workflow Service]
    API --> AUD[Audit Service]
    API --> NOTIF[Notification Service]

    DOC --> DB[(PostgreSQL)]
    WF --> DB
    AUD --> DB
    NOTIF --> DB

    DOC --> OBJ[(Private Object Storage)]
    API --> REDIS[(Redis)]
    WF --> QUEUE[Queue / Outbox]
    QUEUE --> NOTIF

    API --> TRUST[Trust Service Provider]
    TRUST --> API
```

### 6.2 Principes d'architecture

-   Architecture modulaire en premier lieu; ne pas découper trop tôt en
    microservices.
-   API REST versionnée.
-   Contrôle d'accès côté serveur pour chaque ressource.
-   Isolation stricte des données par tenant.
-   Stockage objet privé, accès via URL signées de courte durée.
-   Traitement asynchrone pour les notifications, rendus et
    finalisations.
-   Pattern Transactional Outbox pour fiabiliser les événements.
-   Idempotence des opérations sensibles.
-   Observabilité centralisée sans journaliser les secrets ou contenus
    sensibles.
-   Infrastructure reproductible par IaC.
-   Environnements distincts et secrets gérés par un coffre.

### 6.3 Modules backend proposés

  Module                Responsabilité
  --------------------- -------------------------------------------------
  Identity & Access     Utilisateurs, rôles, sessions, politiques
  Tenant Management     Organisations, configuration, quotas
  Document Management   Import, métadonnées, versions, stockage
  PDF Processing        Prévisualisation, champs, génération finale
  Signing Workflow      Demandes, signataires, ordre, échéances, états
  Signing Session       Jetons invités, consentement, saisie des champs
  Evidence & Audit      Événements, empreintes, dossier de preuve
  Notification          Courriels, SMS, relances
  Integration API       API publique, webhooks, clés d'intégration
  Billing & Usage       Plans, quotas, compteurs et facturation

------------------------------------------------------------------------

## 7. Architecture physique et déploiement

### 7.1 Environnements

-   Développement local Docker Compose.
-   Environnement d'intégration.
-   Préproduction.
-   Production.
-   Environnement de test de sécurité isolé.

### 7.2 Déploiement de départ

Pour le MVP, une architecture simple et maîtrisée est préférable :

-   Frontend Angular servi derrière un reverse proxy.
-   API Spring Boot conteneurisée.
-   PostgreSQL managé ou administré avec sauvegardes vérifiées.
-   Stockage objet privé.
-   Redis si nécessaire pour sessions, cache ou tâches.
-   Service de courriel transactionnel.
-   Gestion centralisée des secrets.
-   Supervision, alertes et journalisation.

La plateforme peut être déployée sur Azure ou sur une infrastructure
compatible avec les exigences contractuelles. Le choix définitif doit
tenir compte de la résidence des données, des coûts, des sauvegardes, de
la disponibilité, des régions de reprise et des contrats fournisseurs.

### 7.3 Disponibilité et reprise

À définir avec les premiers clients :

-   Objectif de disponibilité (SLO).
-   RPO et RTO.
-   Fréquence des sauvegardes.
-   Durée de conservation.
-   Tests périodiques de restauration.
-   Plan de reprise après sinistre.
-   Gestion des indisponibilités du fournisseur de courriel, SMS ou
    confiance.

Ne pas annoncer un SLA commercial avant d'avoir mesuré et validé les
capacités opérationnelles.

------------------------------------------------------------------------

## 8. Modèle de données initial

### 8.1 Entités principales

  -----------------------------------------------------------------------
  Entité                              Champs clés indicatifs
  ----------------------------------- -----------------------------------
  `tenant`                            id, name, status, plan_id,
                                      created_at

  `user_account`                      id, tenant_id, email, display_name,
                                      status

  `role`                              id, tenant_id, code, name

  `user_role`                         user_id, role_id

  `document`                          id, tenant_id, title, status,
                                      created_by

  `document_version`                  id, document_id, version,
                                      object_key, sha256, created_at

  `signature_request`                 id, tenant_id, document_id, status,
                                      expires_at

  `signer`                            id, request_id, name, email,
                                      sequence, status

  `signing_token`                     id, signer_id, token_hash,
                                      expires_at, consumed_at

  `signature_field`                   id, request_id, signer_id,
                                      page_number, field_type, x, y,
                                      width, height

  `signing_session`                   id, signer_id, started_at,
                                      completed_at, auth_level

  `consent_record`                    id, session_id,
                                      consent_text_version, accepted_at

  `signature_artifact`                id, signer_id, object_key, sha256,
                                      created_at

  `audit_event`                       id, tenant_id, request_id,
                                      event_type, occurred_at,
                                      actor_type, actor_id, metadata

  `final_document`                    id, request_id, object_key, sha256,
                                      finalized_at

  `evidence_package`                  id, request_id, object_key, sha256,
                                      generated_at

  `webhook_endpoint`                  id, tenant_id, url,
                                      secret_reference, status

  `usage_record`                      id, tenant_id, usage_type,
                                      quantity, occurred_at
  -----------------------------------------------------------------------

### 8.2 Règles de modélisation

-   Chaque donnée métier tenant-scoped porte un `tenant_id` ou est
    accessible uniquement par une relation vérifiée vers un tenant.
-   Les identifiants publics doivent être non prédictibles.
-   Les jetons de signature ne sont jamais stockés en clair; conserver
    un hash adapté.
-   Les URL d'objets ne sont pas publiques.
-   Les événements d'audit sont append-only au niveau applicatif et
    protégés contre la modification.
-   Les versions du document original, préparé et final sont distinctes.
-   Une demande signée doit référencer exactement la version signée.
-   Les métadonnées libres ne doivent pas contenir de renseignements
    personnels inutiles.

### 8.3 États du workflow

États possibles d'une demande :

`DRAFT → PREPARED → SENT → IN_PROGRESS → COMPLETED`

États alternatifs :

`DECLINED`, `EXPIRED`, `CANCELLED`, `ERROR`.

États d'un signataire :

`PENDING`, `INVITED`, `VIEWED`, `AUTHENTICATED`, `SIGNED`, `DECLINED`,
`EXPIRED`.

Les transitions doivent être contrôlées par le backend et documentées.
Un client ne doit jamais pouvoir modifier directement un statut par
simple appel API.

------------------------------------------------------------------------

## 9. API REST indicative

Préfixe : `/api/v1`

  -------------------------------------------------------------------------------------------
  Méthode                 Endpoint                                    Description
  ----------------------- ------------------------------------------- -----------------------
  POST                    `/documents`                                Créer un document et
                                                                      initier l'import

  POST                    `/documents/{id}/versions`                  Ajouter une version

  GET                     `/documents/{id}`                           Lire les métadonnées

  GET                     `/documents/{id}/preview`                   Obtenir un accès
                                                                      temporaire à l'aperçu

  POST                    `/signature-requests`                       Créer une demande

  GET                     `/signature-requests/{id}`                  Consulter une demande

  PUT                     `/signature-requests/{id}/fields`           Enregistrer les champs

  POST                    `/signature-requests/{id}/send`             Envoyer les invitations

  POST                    `/signing-sessions`                         Démarrer une session
                                                                      invitée

  POST                    `/signing-sessions/{id}/authenticate`       Valider le facteur
                                                                      requis

  POST                    `/signing-sessions/{id}/consent`            Enregistrer le
                                                                      consentement

  POST                    `/signing-sessions/{id}/sign`               Soumettre la signature

  POST                    `/signing-sessions/{id}/decline`            Refuser de signer

  GET                     `/signature-requests/{id}/evidence`         Télécharger les preuves

  GET                     `/signature-requests/{id}/final-document`   Télécharger le PDF
                                                                      final

  POST                    `/webhooks`                                 Configurer un webhook
  -------------------------------------------------------------------------------------------

### 9.1 Sécurité API

-   OAuth2/OIDC pour les utilisateurs authentifiés.
-   Autorisation par tenant et rôle sur chaque endpoint.
-   Jetons invités distincts des jetons d'utilisateur.
-   Durée de vie courte, révocation et usage contrôlé des liens invités.
-   Protection contre CSRF si cookies de session utilisés.
-   CORS restrictif.
-   Limitation de débit et protection anti-énumération.
-   Validation stricte des entrées et tailles.
-   Idempotency-Key pour les opérations de soumission/finalisation.
-   Webhooks signés, avec mécanisme de rejeu contrôlé.
-   Journalisation des accès administratifs.

------------------------------------------------------------------------

## 10. Sécurité, preuve et conformité

### 10.1 Modèle de menace

  -----------------------------------------------------------------------
  Menace                              Mesures principales
  ----------------------------------- -----------------------------------
  Accès inter-tenant                  Autorisation systématique, tests
                                      d'isolation, filtrage tenant

  Vol d'un lien de signature          Jeton hashé, expiration,
                                      révocation, OTP selon risque

  Modification du PDF                 Version immuable, hash, contrôle à
                                      la finalisation

  Falsification d'événements          Journal append-only, contrôle
                                      d'accès, chaîne d'empreintes ou
                                      stockage WORM

  Fuite de documents                  Stockage privé, chiffrement, URL
                                      temporaires

  Malware dans les fichiers           Analyse antivirus, validation MIME
                                      et structure

  Injection / XSS                     Encodage, CSP, validation,
                                      sanitisation

  CSRF                                SameSite, token CSRF, contrôle
                                      Origin selon architecture

  Abus d'API                          Rate limiting, quotas, détection
                                      d'anomalies

  Compromission d'un compte           MFA pour les comptes sensibles,
                                      alertes, révocation de sessions

  Exposition de secrets               Secret manager, rotation, absence
                                      dans les logs
  -----------------------------------------------------------------------

### 10.2 Dossier de preuve

Le dossier de preuve devrait contenir, selon la politique et le cas
d'usage :

-   Identifiant unique de la demande.
-   Identifiant et empreinte de la version originale.
-   Empreinte de la version préparée.
-   Empreinte du document final.
-   Identité déclarée du signataire et méthode de vérification utilisée.
-   Événement d'invitation et canal utilisé.
-   Événement d'accès et horodatage.
-   Texte et version du consentement.
-   Événement de signature et confirmation explicite.
-   Méthode d'authentification et résultat.
-   Événements de refus, annulation ou expiration, le cas échéant.
-   Informations techniques pertinentes et proportionnées.
-   Résultat de finalisation et version du moteur.
-   Référence du service d'horodatage ou de confiance lorsqu'utilisé.

Les adresses IP et données techniques peuvent être des renseignements
personnels. Leur collecte doit être justifiée, limitée, protégée et
documentée.

### 10.3 Intégrité

-   Calculer SHA-256 des fichiers aux étapes définies.
-   Conserver les empreintes avec les métadonnées de version.
-   Vérifier l'empreinte avant et après traitement.
-   Figer la version qui fait l'objet du consentement.
-   Produire le PDF final de façon déterministe autant que possible.
-   Protéger les événements d'audit contre la modification et la
    suppression.
-   Envisager une chaîne de hachage des événements et/ou un stockage
    immuable.
-   Documenter les clés, rotations, certificats et opérations
    cryptographiques.

Une empreinte SHA-256 prouve qu'un contenu correspond à une empreinte
connue; elle ne prouve pas à elle seule qui a signé ni que le
consentement est valable.

### 10.4 Cadre juridique à examiner

Pour une commercialisation au Québec et au Canada, faire valider
notamment :

-   Loi concernant le cadre juridique des technologies de l'information
    (Québec), dont les dispositions sur l'intégrité et le lien entre
    signature et document.
-   Code civil du Québec et règles applicables à l'acte concerné.
-   Loi sur la protection des renseignements personnels dans le secteur
    privé, telle que modifiée par la Loi 25.
-   Obligations contractuelles et sectorielles du client.
-   Exigences de conservation, preuve, archivage et accès.
-   Transferts de renseignements hors Québec et évaluations requises.
-   Exigences spécifiques pour les documents notariés, actes réglementés
    ou opérations à risque élevé.

La validité dépend du document, du contexte, de l'identification, du
consentement et des règles applicables. Faire valider les parcours et
les niveaux de signature par un conseiller juridique québécois avant de
présenter une garantie juridique ou une qualification.

### 10.5 Niveaux de signature proposés

  -----------------------------------------------------------------------
  Niveau                  Parcours                Usage indicatif
  ----------------------- ----------------------- -----------------------
  Simple                  Lien sécurisé,          Documents à risque
                          consentement, signature faible ou modéré
                          manuscrite visuelle,    
                          audit                   

  Renforcé                Authentification        Contrats d'affaires
                          additionnelle (OTP ou   plus sensibles
                          autre), audit enrichi   

  Avancé                  Identité vérifiée,      Cas exigeant une
                          signature               assurance supérieure
                          numérique/certificat et 
                          horodatage via          
                          fournisseur             
  -----------------------------------------------------------------------

Ces niveaux sont des offres fonctionnelles internes, pas des catégories
juridiques universelles. Leur qualification doit être validée pour
chaque marché.

------------------------------------------------------------------------

## 11. Architecture multi-tenant

### 11.1 Modèle initial

Une base PostgreSQL partagée avec `tenant_id` dans les tables métier
peut convenir au MVP, à condition de mettre en œuvre :

-   Contrôles d'autorisation côté backend.
-   Contraintes et index tenant-aware.
-   Row-Level Security PostgreSQL en défense supplémentaire, si
    compatible avec le modèle de connexion.
-   Tests automatisés d'accès croisé.
-   Stockage objet préfixé par tenant avec politiques d'accès.
-   Clés et secrets séparés lorsque le risque l'exige.

### 11.2 Évolution

Pour les clients Enterprise, prévoir éventuellement :

-   Base dédiée.
-   Stockage dédié.
-   Clés de chiffrement dédiées.
-   Région d'hébergement contractuelle.
-   Politiques de conservation spécifiques.
-   SSO et intégration SIEM.

------------------------------------------------------------------------

## 12. Exigences non fonctionnelles

Les cibles ci-dessous sont des objectifs initiaux à confirmer par les
essais et les coûts.

  -----------------------------------------------------------------------
  Domaine                             Cible initiale
  ----------------------------------- -----------------------------------
  Performance UI                      Interaction tactile fluide sur
                                      appareils récents

  Chargement                          Affichage progressif des pages PDF

  API                                 P95 inférieur à 500 ms pour les
                                      opérations courantes hors
                                      traitement PDF

  Disponibilité                       Objectif MVP à définir; cible
                                      commerciale après validation
                                      opérationnelle

  Sécurité                            OWASP ASVS adapté au risque; OWASP
                                      Top 10

  Accessibilité                       WCAG 2.2 AA comme cible de
                                      conception

  Compatibilité                       Versions récentes de Chrome, Edge,
                                      Safari et Firefox

  Mobile                              Android Chrome; tests sur tailles
                                      d'écran variées

  Confidentialité                     Minimisation, rétention documentée,
                                      accès contrôlés

  Observabilité                       Logs structurés, métriques, traces
                                      et alertes

  Résilience                          Reprises contrôlées, idempotence,
                                      sauvegardes restaurables

  Maintenabilité                      Modules séparés, contrats API,
                                      tests automatisés
  -----------------------------------------------------------------------

### 12.1 Tests de compatibilité tactile

Tester au minimum :

-   Téléphone Android petit écran.
-   Téléphone Android grand écran.
-   Tablette Android portrait et paysage.
-   Stylet et doigt.
-   Souris et clavier.
-   Documents de plusieurs pages.
-   PDF tournés, pages de formats différents et documents lourds.
-   Gestes rapides, zooms successifs et perte/reprise de focus.
-   Rotation de l'appareil pendant la préparation.
-   Réseau lent ou interrompu.

------------------------------------------------------------------------

## 13. Backlog produit

### 13.1 Épics

  -----------------------------------------------------------------------
  Épic                    Objectif                Priorité
  ----------------------- ----------------------- -----------------------
  E01 --- Fondations      Dépôt, CI/CD,           P0
                          environnements,         
                          observabilité           

  E02 --- Identité et     Comptes, rôles,         P0
  tenants                 isolation               

  E03 --- Documents       Import, stockage,       P0
                          versions,               
                          prévisualisation        

  E04 --- Éditeur tactile Affichage, gestes,      P0
                          champs, signature       

  E05 --- Workflow        Signataires,            P0
                          invitations, statuts    

  E06 --- Session         Lien sécurisé,          P0
  signataire              consentement,           
                          soumission              

  E07 --- Finalisation    PDF final, empreintes,  P0
                          certificat initial      

  E08 --- Audit           Journal d'événements et P0
                          export                  

  E09 --- Notifications   Courriels et relances   P0
                          de base                 

  E10 --- Administration  Utilisateurs,           P1
                          configuration, quotas   

  E11 --- Sécurité        OTP, MFA, tests         P1
  avancée                 d'intrusion             

  E12 --- API et          API publique, webhooks  P2
  intégrations                                    

  E13 --- PAdES et        Certificats,            P2
  confiance               horodatage, validation  

  E14 --- Facturation     Plans, compteurs,       P2
                          paiement                
  -----------------------------------------------------------------------

### 13.2 User stories --- MVP

#### E01 --- Fondations

-   **US-001** En tant que développeur, je veux un dépôt structuré
    frontend/backend afin de livrer indépendamment les composants.
-   **US-002** En tant qu'équipe, je veux une pipeline CI qui compile,
    teste et analyse le code.
-   **US-003** En tant qu'équipe, je veux des environnements distincts
    et des secrets externalisés.
-   **US-004** En tant qu'opérateur, je veux des logs structurés et des
    métriques afin de diagnostiquer les incidents.

Critères d'acceptation : - Build reproductible. - Tests exécutés dans la
CI. - Aucun secret dans le dépôt. - Images conteneurisées versionnées. -
Déploiement préproduction documenté.

#### E02 --- Identité et tenants

-   **US-010** En tant qu'administrateur, je veux créer une
    organisation.
-   **US-011** En tant qu'administrateur tenant, je veux inviter et
    désactiver des utilisateurs.
-   **US-012** En tant qu'administrateur, je veux attribuer des rôles.
-   **US-013** En tant que client, je veux que mes données soient
    isolées de celles des autres organisations.

Critères d'acceptation : - Un utilisateur ne lit ni ne modifie une
ressource d'un autre tenant. - Les autorisations sont vérifiées côté
serveur. - Les accès administratifs sont journalisés. - Les tests
négatifs d'isolation sont automatisés.

#### E03 --- Documents

-   **US-020** En tant que préparateur, je veux importer un PDF.
-   **US-021** En tant que préparateur, je veux visualiser le PDF page
    par page.
-   **US-022** En tant que préparateur, je veux consulter les
    métadonnées et le statut du document.
-   **US-023** En tant que système, je veux conserver une version
    originale immuable.

Critères d'acceptation : - Contrôle du type réel, de la taille et de la
structure du fichier. - Analyse antivirus ou contrôle équivalent. -
Stockage privé. - Empreinte SHA-256 calculée. - Version originale non
écrasée.

#### E04 --- Éditeur tactile

-   **US-030** En tant que préparateur, je veux zoomer et déplacer le
    document au doigt.
-   **US-031** Je veux ajouter un champ de signature.
-   **US-032** Je veux déplacer et redimensionner le champ.
-   **US-033** Je veux ajouter texte, date, initiales et case à cocher.
-   **US-034** Je veux annuler et rétablir une modification.
-   **US-035** Je veux que les positions restent correctes sur
    téléphone, tablette et ordinateur.
-   **US-036** En tant que signataire, je veux dessiner ma signature
    avec le doigt ou le stylet.

Critères d'acceptation : - Les coordonnées sont enregistrées dans le
référentiel de la page, pas en pixels écran. - Les champs ne se
déplacent pas lors d'un changement de zoom. - Les gestes de navigation
et de manipulation sont distincts. - La signature peut être effacée et
redessinée avant confirmation. - Les essais passent sur téléphone et
tablette Android. - Le PDF final reflète précisément les champs
attendus.

#### E05 --- Workflow

-   **US-040** En tant que préparateur, je veux ajouter un ou plusieurs
    signataires.
-   **US-041** Je veux définir un ordre de signature.
-   **US-042** Je veux fixer une date d'expiration.
-   **US-043** Je veux annuler une demande envoyée.
-   **US-044** Je veux suivre les statuts de chaque signataire.

Critères d'acceptation : - Transitions d'état contrôlées par le
backend. - Un signataire ne peut pas signer au nom d'un autre. - Les
événements d'envoi et d'annulation sont tracés. - Les demandes expirées
ne peuvent plus être signées.

#### E06 --- Session signataire

-   **US-050** En tant que signataire invité, je veux ouvrir un lien
    sécurisé sans créer de compte.
-   **US-051** Je veux consulter le document avant de signer.
-   **US-052** Je veux accepter explicitement le consentement
    électronique.
-   **US-053** Je veux compléter les champs qui me sont assignés.
-   **US-054** Je veux confirmer ma signature.
-   **US-055** Je veux refuser de signer.

Critères d'acceptation : - Jeton aléatoire, stocké sous forme hashée,
expirant et révocable. - Contrôle d'usage et protection contre le
rejeu. - Consentement versionné et horodaté. - Signature liée à la
version précise du document. - Confirmation présentée après succès
serveur. - Refus et signature sont des événements distincts.

#### E07 --- Finalisation

-   **US-060** En tant que signataire, je veux recevoir le document
    final.
-   **US-061** En tant qu'expéditeur, je veux télécharger le PDF signé.
-   **US-062** En tant que système, je veux calculer l'empreinte du PDF
    final.
-   **US-063** Je veux produire un certificat de réalisation initial.

Critères d'acceptation : - Le PDF final est stocké comme nouvelle
version immuable. - Son empreinte est enregistrée. - Le statut COMPLETED
n'est appliqué qu'après finalisation réussie. - Les erreurs de
génération sont reprises sans double signature.

#### E08 --- Audit

-   **US-070** En tant qu'auditeur autorisé, je veux consulter la
    chronologie d'une demande.
-   **US-071** Je veux exporter le dossier de preuve.
-   **US-072** En tant qu'administrateur, je veux consulter les actions
    sensibles.

Critères d'acceptation : - Événements horodatés et ordonnés. - Identité
de l'acteur ou type d'acteur enregistré. - Pas de
modification/suppression via les API métier. - Accès aux preuves
contrôlé et journalisé.

#### E09 --- Notifications

-   **US-080** En tant que signataire, je veux recevoir une invitation
    par courriel.
-   **US-081** Je veux recevoir une confirmation après signature.
-   **US-082** En tant qu'expéditeur, je veux recevoir une notification
    lorsque tous les signataires ont terminé.

Critères d'acceptation : - Envoi asynchrone. - Modèles français et
anglais. - Reprise en cas d'échec temporaire. - Aucun document
confidentiel joint par défaut au courriel. - Les liens ont une durée de
validité définie.

### 13.3 Backlog V1 et V2

  ID       Fonctionnalité                      Version   Priorité
  -------- ----------------------------------- --------- ----------
  US-100   OTP par courriel                    V1        P1
  US-101   OTP SMS via fournisseur             V1        P1
  US-102   Relances automatiques               V1        P1
  US-103   Modèles de documents                V1        P1
  US-104   Tableau de bord et recherche        V1        P1
  US-105   API publique                        V1        P1
  US-106   Webhooks signés                     V1        P1
  US-107   Export détaillé des preuves         V1        P1
  US-108   Quotas par tenant                   V1        P1
  US-109   SSO entreprise                      V2        P2
  US-110   PAdES et horodatage                 V2        P2
  US-111   Validation à long terme             V2        P2
  US-112   Vérification d'identité renforcée   V2        P2
  US-113   Connecteurs CRM/ERP/GED             V2        P2
  US-114   Facturation automatisée             V2        P2

------------------------------------------------------------------------

## 14. Plan de livraison indicatif

Estimation de cadrage, à affiner après atelier de conception, validation
des bibliothèques PDF, maquettes et choix du fournisseur de confiance.

  ------------------------------------------------------------------------
  Phase                             Durée indicative Livrables
  --------------------- ---------------------------- ---------------------
  Cadrage et validation                1--2 semaines Personas, exigences,
                                                     risques, architecture

  UX et prototype                      2--3 semaines Maquettes et
  tactile                                            prototype sur
                                                     appareils réels

  Fondations techniques                2--3 semaines CI/CD, sécurité,
                                                     tenants, base

  Documents et éditeur                 3--5 semaines Import,
                                                     visualisation,
                                                     champs, signature

  Workflow et                          2--4 semaines Signataires,
  invitations                                        sessions,
                                                     notifications

  Finalisation et audit                2--3 semaines PDF final, preuves,
                                                     empreintes

  Stabilisation et                     2--3 semaines Tests, pentest,
  pilote                                             corrections, pilote
  ------------------------------------------------------------------------

Les phases peuvent se chevaucher selon les ressources. Une cible
indicative de MVP est de l'ordre de **12 à 18 semaines** avec une équipe
expérimentée et un périmètre strict. Ce délai n'est pas un engagement
contractuel.

### 14.1 Équipe indicative

-   1 architecte / responsable technique à temps partiel.
-   1 développeur frontend Angular.
-   1 développeur backend Spring Boot.
-   1 QA / automatisation à temps partiel.
-   1 UX/UI à temps partiel.
-   1 spécialiste sécurité / juridique / confiance à la demande.

### 14.2 Estimation budgétaire de cadrage

Le coût dépend fortement du taux de réalisation, de l'équipe, de la
réutilisation des composants, du niveau de sécurité et de la profondeur
du dossier de preuve.

  Lot                                               Effort indicatif
  ---------------------------------- -------------------------------
  Cadrage, architecture et UX                3--5 semaines-personnes
  Fondations et multi-tenant                 4--6 semaines-personnes
  Documents et éditeur tactile              8--12 semaines-personnes
  Workflow et sessions signataires           5--8 semaines-personnes
  Finalisation et audit                      4--6 semaines-personnes
  QA, sécurité et pilote                     4--7 semaines-personnes
  **Total indicatif**                  **28--44 semaines-personnes**

À titre de calcul, pour un coût moyen de réalisation de 900 à 1 200 CAD
par jour-personne et environ 140 à 220 jours-personnes, l'enveloppe
indicative serait de **126 000 à 264 000 CAD**, hors taxes, frais
récurrents des fournisseurs, hébergement, services de confiance et
exploitation. Cette fourchette est une hypothèse de planification et
doit être recalculée avec les taux et ressources réels de Vina.

------------------------------------------------------------------------

## 15. Stratégie de tests et critères de sortie

### 15.1 Tests fonctionnels

-   Import PDF valide et invalide.
-   Documents multipages.
-   Champs obligatoires et facultatifs.
-   Plusieurs signataires et ordres.
-   Invitation, expiration, révocation et refus.
-   Signature tactile et confirmation.
-   Finalisation et téléchargement.
-   Notifications et reprise sur erreur.

### 15.2 Tests éditeur

-   Gestes sur téléphone et tablette.
-   Rotation portrait/paysage.
-   Zoom et déplacement.
-   Champs proches des bords.
-   Pages de dimensions différentes.
-   PDF tournés.
-   Reprise après perte de réseau.
-   Précision des positions dans le PDF final.
-   Accessibilité clavier et lecteur d'écran pour les fonctions
    pertinentes.

### 15.3 Tests sécurité

-   Contrôle d'accès inter-tenant.
-   IDOR/BOLA sur toutes les ressources.
-   XSS, injection, CSRF et CORS.
-   Réutilisation et vol de jetons.
-   Brute force OTP et limitation de débit.
-   Téléversement de fichiers malveillants.
-   Fuite d'URL de stockage.
-   Modification d'un document après consentement.
-   Rejeu d'une demande de signature.
-   Falsification d'événements et accès aux preuves.
-   Gestion des secrets, clés et certificats.
-   Dépendances vulnérables et images Docker.

### 15.4 Critères d'acceptation MVP

Le MVP est prêt pour un pilote lorsque :

1.  Un tenant peut inviter ses utilisateurs.
2.  Un préparateur peut importer un PDF et positionner les champs.
3.  Le même document est manipulable correctement sur téléphone,
    tablette et bureau.
4.  Un signataire invité peut signer au doigt ou au stylet.
5.  Le consentement est enregistré avant la finalisation.
6.  Le document final est généré et son empreinte vérifiée.
7.  Le journal de preuve permet de reconstruire la chronologie.
8.  Les tests d'isolation multi-tenant passent.
9.  Les vulnérabilités critiques et élevées identifiées sont corrigées
    ou font l'objet d'une décision de risque formelle.
10. Les sauvegardes et restaurations sont testées.
11. Les conditions d'utilisation, politique de confidentialité et
    conservation sont validées.
12. Un pilote réel est réalisé avec des documents et signataires
    représentatifs.

------------------------------------------------------------------------

## 16. ADR --- décisions d'architecture initiales

### ADR-001 --- PWA responsive plutôt que deux applications natives

**Décision :** Angular responsive/PWA pour le MVP.

**Motifs :** réduire les coûts, livrer une seule interface et couvrir
téléphone, tablette et bureau.

**Conséquences :** dépendance aux capacités du navigateur; tester
précisément les gestes, téléchargements, stockage et compatibilité
Android. Une application native pourra être envisagée si des limites
concrètes apparaissent.

### ADR-002 --- Architecture modulaire plutôt que microservices

**Décision :** backend Spring Boot modulaire au démarrage.

**Motifs :** simplicité opérationnelle, transactions cohérentes et coût
d'exploitation réduit.

**Conséquences :** frontières de modules explicites; extraction
ultérieure possible pour les notifications, PDF ou intégrations si la
charge et l'organisation le justifient.

### ADR-003 --- Stockage objet privé pour les documents

**Décision :** documents dans un stockage objet privé, métadonnées dans
PostgreSQL.

**Motifs :** séparation des responsabilités, capacité d'évolution et
gestion des versions.

**Conséquences :** contrôle d'accès, chiffrement, URL temporaires,
sauvegarde et politique de rétention à concevoir.

### ADR-004 --- Coordonnées normalisées pour les champs PDF

**Décision :** enregistrer les positions relativement à la page PDF.

**Motifs :** conserver la position entre les appareils et les niveaux de
zoom.

**Conséquences :** définir et versionner le système de coordonnées;
tester les rotations et formats de page.

### ADR-005 --- Fournisseur spécialisé pour les services de confiance avancés

**Décision :** intégrer un fournisseur pour les fonctions avancées
plutôt que construire une autorité de certification ou un service
d'horodatage propriétaire au MVP.

**Motifs :** réduire les risques cryptographiques, réglementaires et
opérationnels.

**Conséquences :** vérifier les certifications, contrats, résidence,
sous-traitants, API, coûts, réversibilité et disponibilité du
fournisseur.

### ADR-006 --- Séparer signature visuelle et signature numérique

**Décision :** modéliser distinctement le tracé manuscrit, le
consentement, l'authentification, la preuve et la signature
cryptographique PDF.

**Motifs :** éviter toute ambiguïté sur la portée technique et juridique
du produit.

**Conséquences :** parcours, stockage et certificats doivent indiquer
précisément le niveau de preuve fourni.

------------------------------------------------------------------------

## 17. Risques et mesures de mitigation

  -----------------------------------------------------------------------
  Risque                  Impact                  Mesure
  ----------------------- ----------------------- -----------------------
  Mauvais alignement des  Élevé                   Coordonnées
  champs PDF                                      normalisées, tests
                                                  multi-appareils

  Complexité des PDF      Élevé                   Corpus de tests,
  réels                                           limites documentées,
                                                  traitement serveur

  Confusion signature     Élevé                   Terminologie produit
  dessinée / signature                            claire, validation
  cryptographique                                 juridique

  Preuve insuffisante     Élevé                   Dossier de preuve,
                                                  audit, consentement,
                                                  horodatage adapté

  Fuite inter-tenant      Critique                Défense en profondeur,
                                                  tests automatisés,
                                                  revue sécurité

  Dépendance fournisseur  Moyen/élevé             Abstraction, export,
                                                  clauses contractuelles,
                                                  plan de sortie

  Coûts SMS et identité   Moyen                   Quotas, facturation à
                                                  l'usage, alternatives

  Fichiers malveillants   Élevé                   Analyse, sandbox,
                                                  limites et validation

  Indisponibilité de      Moyen/élevé             Files, reprises,
  stockage ou courriel                            alertes, procédures
                                                  manuelles

  Évolution légale ou     Moyen/élevé             Veille et revue
  sectorielle                                     juridique périodique
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 18. Questions à trancher avant le démarrage

1.  Le produit vise-t-il d'abord les PME québécoises ou des
    organisations de plus grande taille?
2.  Le MVP doit-il être exclusivement en français ou bilingue
    français/anglais?
3.  Taille maximale des PDF et nombre maximal de pages?
4.  Combien de signataires par document et quels scénarios
    séquentiels/parallèles?
5.  Le signataire doit-il pouvoir signer sans compte dans tous les cas?
6.  OTP par courriel suffisant pour le pilote ou OTP SMS requis?
7.  Quel fournisseur de courriel et de SMS?
8.  Hébergement Azure Canada Central/Canada East, OVH ou autre?
9.  Quelles exigences de résidence et de conservation des données?
10. Quels documents et secteurs sont prioritaires?
11. Le produit doit-il inclure PAdES dès la V1 ou seulement en V2?
12. Quel budget, quelle équipe et quelle date cible pour le pilote?

------------------------------------------------------------------------

## 19. Références techniques et réglementaires à consulter

Les références ci-dessous constituent des points de départ; vérifier les
versions en vigueur et les exigences applicables au moment de la
conception et de la mise en production.

-   **Québec --- Loi concernant le cadre juridique des technologies de
    l'information :**
    https://www.legisquebec.gouv.qc.ca/fr/document/lc/C-1.1
-   **Québec --- Commission d'accès à l'information :**
    https://www.cai.gouv.qc.ca/
-   **OWASP Application Security Verification Standard :**
    https://owasp.org/www-project-application-security-verification-standard/
-   **OWASP Top 10 :** https://owasp.org/www-project-top-ten/
-   **W3C Pointer Events :** https://www.w3.org/TR/pointerevents/
-   **Mozilla PDF.js :** https://mozilla.github.io/pdf.js/
-   **Apache PDFBox :** https://pdfbox.apache.org/
-   **ETSI --- normes de signature électronique et PAdES :**
    https://www.etsi.org/
-   **IETF --- RFC 3161, Time-Stamp Protocol :**
    https://www.rfc-editor.org/rfc/rfc3161
-   **IETF --- RFC 7519, JSON Web Token :**
    https://www.rfc-editor.org/rfc/rfc7519
-   **WCAG 2.2 :** https://www.w3.org/TR/WCAG22/

------------------------------------------------------------------------

## 20. Conclusion et recommandation

VinaSign doit être conçu comme une plateforme de workflow documentaire
avec un éditeur tactile de première qualité, et non comme une simple
fonction qui colle une image de signature dans un PDF.

La priorité du MVP est de démontrer trois capacités :

1.  **Une expérience tactile remarquable** sur téléphone et tablette.
2.  **Un workflow fiable** de préparation, invitation, signature et
    finalisation.
3.  **Une preuve cohérente et vérifiable** qui relie le signataire, son
    consentement et la version exacte du document.

La meilleure stratégie est de conserver la maîtrise du produit et de
l'expérience utilisateur chez Vina IT Solution, tout en s'appuyant sur
des services spécialisés pour les niveaux avancés d'identité,
d'horodatage et de signature cryptographique.

**Prochaine décision recommandée :** valider le périmètre MVP, réaliser
un prototype tactile sur appareils Android réels, sélectionner la chaîne
de traitement PDF et lancer une revue juridique et sécurité avant le
pilote client.
