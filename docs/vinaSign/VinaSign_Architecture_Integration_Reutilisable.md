# VinaSign --- Architecture d'intégration réutilisable

**Version :** 1.0\
**Date :** 22 novembre 2026\
**Statut :** Proposition d'architecture cible

## 1. Objectif

VinaSign doit être conçu comme un **moteur de signature électronique
indépendant**, réutilisable par plusieurs applications, plutôt que comme
une fonctionnalité étroitement liée à une seule application.

Il pourra être utilisé par une GED, un CRM, un ERP, une application RH,
une application métier, une application Web ou mobile tierce, ou
directement depuis le portail autonome VinaSign.

L'application cliente conserve la responsabilité de ses processus métier
et de son référentiel documentaire. VinaSign prend en charge le workflow
de signature, l'expérience de signature, la production du document signé
et les éléments de preuve associés.

## 2. Positionnement architectural recommandé

Développer VinaSign comme un **service centralisé exposé par API**,
accompagné de SDK et de composants d'interface légers.

Éviter d'intégrer tout le moteur sous forme de bibliothèque dans chaque
projet : cela créerait des versions divergentes, de la duplication de
logique de sécurité et des difficultés de maintenance.

### 2.1 Trois modes d'intégration

  -----------------------------------------------------------------------
  Mode                    Description             Cas d'utilisation
  ----------------------- ----------------------- -----------------------
  API REST                L'application cliente   GED, CRM, ERP,
                          pilote le workflow      applications Java/.NET
                          depuis son backend      

  Interface intégrée /    L'application affiche   Angular, React,
  SDK UI                  l'expérience de         applications Web
                          préparation ou de       
                          signature VinaSign      

  Application autonome    L'utilisateur travaille Organisations sans
                          directement dans        application
                          VinaSign                documentaire existante
  -----------------------------------------------------------------------

Ces modes utilisent le même moteur métier et les mêmes règles de
sécurité.

## 3. Architecture logique cible

``` mermaid
flowchart TB
    subgraph Clients["Applications clientes"]
        GED["GED / Documentation"]
        CRM["CRM / ERP / RH"]
        METIER["Applications métier"]
        TIERS["Applications tierces"]
        AUTONOME["Portail autonome VinaSign"]
    end

    subgraph Integration["Couche d'intégration VinaSign"]
        API["API REST / API Gateway"]
        SDK["SDK Java / .NET / Angular / React"]
        EMBED["UI intégrée / iframe / Web Component"]
        OAUTH["OAuth2 / OIDC"]
        WEBHOOK["Webhooks sécurisés"]
    end

    subgraph Core["Moteur VinaSign"]
        ENVELOPE["Gestion des demandes / enveloppes"]
        DOC["Gestion documentaire temporaire"]
        EDITOR["Préparation PDF et champs tactiles"]
        WORKFLOW["Orchestration du workflow"]
        SIGN["Signature et consentement"]
        EVIDENCE["Audit et dossier de preuve"]
        CRYPTO["Services cryptographiques / PAdES"]
    end

    subgraph Infra["Services techniques"]
        DB[("PostgreSQL")]
        REDIS[("Redis / sessions")]
        STORAGE[("Stockage objet privé")]
        KMS["KMS / gestion des clés"]
        NOTIFY["Courriel / SMS / OTP"]
        TIME["Horodatage / services de confiance"]
    end

    GED --> API
    CRM --> API
    METIER --> API
    TIERS --> API
    AUTONOME --> API
    GED -.-> SDK
    CRM -.-> SDK
    METIER -.-> EMBED
    TIERS -.-> EMBED
    API --> OAUTH
    API --> ENVELOPE
    SDK --> EMBED
    ENVELOPE --> DOC
    ENVELOPE --> WORKFLOW
    DOC --> EDITOR
    WORKFLOW --> SIGN
    SIGN --> EVIDENCE
    SIGN --> CRYPTO
    ENVELOPE --> DB
    WORKFLOW --> REDIS
    DOC --> STORAGE
    CRYPTO --> KMS
    SIGN --> NOTIFY
    EVIDENCE --> TIME
    WEBHOOK --> GED
    WEBHOOK --> CRM
    WEBHOOK --> METIER
```

## 4. Responsabilités des systèmes

### 4.1 Application cliente

L'application cliente demeure responsable de l'authentification et de
l'autorisation de ses utilisateurs dans son domaine, de la logique
métier qui déclenche une signature, de la sélection du document, de la
conservation du document source si cette politique est retenue, du
traitement des événements VinaSign et de l'association du résultat au
dossier métier.

### 4.2 VinaSign

VinaSign est responsable de la gestion des demandes de signature
(enveloppes), de l'identification des signataires et de l'ordre de
signature, de la préparation du PDF et du placement des champs, de la
collecte du consentement et de la signature, des contrôles
d'authentification supplémentaires, de l'orchestration des relances,
refus, expirations et annulations, de la génération du PDF final et du
dossier de preuve, ainsi que de l'audit et des notifications.

### 4.3 Source de vérité documentaire

Deux modèles sont possibles :

1.  **GED maître :** la GED conserve le document source et le document
    final. VinaSign reçoit une copie contrôlée pour exécuter le
    workflow.
2.  **VinaSign maître :** VinaSign conserve les documents et expose les
    résultats à l'application cliente.

Pour une intégration à une GED existante, le modèle GED maître est
généralement préférable. La durée de conservation des copies temporaires
chez VinaSign, les règles de suppression et les obligations de preuve
doivent être définies contractuellement et techniquement.

## 5. Exemple de parcours avec une GED

1.  Un utilisateur ouvre un contrat dans la GED.
2.  Il sélectionne **Envoyer pour signature**.
3.  Le backend de la GED appelle l'API VinaSign avec une référence
    métier et les informations des signataires.
4.  VinaSign crée une enveloppe et retourne son identifiant.
5.  Le document est transmis à VinaSign, directement ou au moyen d'une
    URL de téléversement temporaire.
6.  Les signataires reçoivent une invitation ou accèdent à l'expérience
    intégrée.
7.  Chaque signataire consulte le document, consent et signe selon le
    workflow.
8.  VinaSign finalise le document, génère les éléments de preuve et met
    à jour le statut.
9.  VinaSign envoie un webhook à la GED.
10. La GED vérifie l'événement, interroge l'API VinaSign et récupère le
    PDF final ainsi que les preuves autorisées.
11. La GED enregistre le document final dans le dossier métier.

La GED ne doit pas se fier uniquement au contenu du webhook : elle doit
vérifier la signature de l'événement et, lorsque nécessaire, relire le
statut auprès de l'API VinaSign.

## 6. Contrat d'API REST proposé

Les routes suivantes sont indicatives et devront être précisées dans un
contrat OpenAPI versionné.

  ------------------------------------------------------------------------------------------
  Méthode                 Route                                      Fonction
  ----------------------- ------------------------------------------ -----------------------
  POST                    `/api/v1/envelopes`                        Créer une demande de
                                                                     signature

  POST                    `/api/v1/envelopes/{id}/documents`         Ajouter un document

  POST                    `/api/v1/envelopes/{id}/signers`           Ajouter ou définir les
                                                                     signataires

  PUT                     `/api/v1/envelopes/{id}/fields`            Définir les champs de
                                                                     signature

  POST                    `/api/v1/envelopes/{id}/send`              Démarrer le workflow

  POST                    `/api/v1/envelopes/{id}/sessions`          Créer une session de
                                                                     signature intégrée

  GET                     `/api/v1/envelopes/{id}`                   Lire le statut et les
                                                                     métadonnées

  GET                     `/api/v1/envelopes/{id}/documents/final`   Récupérer le document
                                                                     final

  GET                     `/api/v1/envelopes/{id}/evidence`          Récupérer le dossier de
                                                                     preuve

  POST                    `/api/v1/webhooks`                         Enregistrer une
                                                                     destination webhook
  ------------------------------------------------------------------------------------------

### 6.1 Exemple de création d'enveloppe

``` json
{
  "externalReference": "CONTRAT-2026-001",
  "workflow": {
    "signingOrder": "SEQUENTIAL"
  },
  "signers": [
    {
      "name": "Jean Dupont",
      "email": "jean@example.com",
      "order": 1
    },
    {
      "name": "Marie Martin",
      "email": "marie@example.com",
      "order": 2
    }
  ],
  "options": {
    "authentication": "EMAIL_OTP",
    "language": "fr"
  }
}
```

`externalReference` permet à l'application cliente de rattacher
l'enveloppe à son dossier métier. Elle doit être unique dans le
périmètre approprié, ou accompagnée d'une clé d'idempotence pour éviter
les créations en double.

### 6.2 Téléversement des documents

Pour les documents volumineux, privilégier un téléversement direct vers
le stockage objet privé à l'aide d'une URL temporaire à durée limitée :

1.  le backend client demande une autorisation de téléversement ;
2.  VinaSign retourne une URL temporaire limitée à un objet et à une
    opération ;
3.  le client téléverse le fichier ;
4.  le client confirme le téléversement à VinaSign ;
5.  VinaSign vérifie le fichier, son type, sa taille et son empreinte
    avant de l'accepter.

Ne jamais exposer directement le stockage objet ni accorder une URL
permanente.

## 7. Authentification et autorisation des intégrations

### 7.1 Backend à backend

Utiliser OAuth 2.0 Client Credentials pour les appels serveur-à-serveur.

Chaque application cliente possède un identifiant client, un secret ou
un mécanisme d'authentification plus robuste, un ou plusieurs scopes,
une association explicite à un tenant, ainsi que des quotas et limites
d'utilisation.

Exemples de scopes :

-   `envelopes:create`
-   `envelopes:read`
-   `documents:upload`
-   `documents:download`
-   `signers:manage`
-   `webhooks:manage`

Les jetons doivent être de courte durée, validés pour leur audience
(`aud`), leur émetteur (`iss`), leur expiration et leurs scopes.

### 7.2 Utilisateurs finaux

OIDC peut être utilisé pour l'authentification des utilisateurs qui
accèdent à VinaSign. L'application cliente et VinaSign doivent définir
clairement la relation de confiance, les identifiants de corrélation et
les règles de provisionnement.

Pour les flux interactifs, utiliser Authorization Code avec PKCE lorsque
cela s'applique.

### 7.3 Ne jamais exposer les secrets dans le frontend

Les secrets OAuth Client Credentials ne doivent jamais être placés dans
une application Angular ou React, du JavaScript livré au navigateur, une
application mobile ou un dépôt Git. Les appels privilégiés doivent
passer par le backend de l'application cliente.

### 7.4 Séparer les identités

Le modèle de sécurité doit distinguer au minimum :

-   `tenant_id` : organisation cliente ;
-   `client_application_id` : application autorisée à appeler VinaSign ;
-   `actor_user_id` : utilisateur de l'application cliente qui déclenche
    l'action ;
-   `signer_id` : personne invitée à signer.

Ces identités ne sont pas interchangeables. Un signataire externe n'est
pas nécessairement un utilisateur inscrit dans l'application cliente.

## 8. Interface intégrée et SDK

### 8.1 Options d'intégration UI

  -----------------------------------------------------------------------
  Option                  Avantages               Points d'attention
  ----------------------- ----------------------- -----------------------
  iframe cross-origin     Isolation et séparation Gestion du
                          des domaines            redimensionnement et
                                                  des échanges de
                                                  messages

  Web Component           Intégration visuelle    Isolation et
                          plus fluide             compatibilité à
                                                  maîtriser

  SDK Angular / React     Expérience adaptée au   Dépendance aux versions
                          framework               et maintenance du SDK

  Redirection vers        Simple et robuste       Expérience moins
  VinaSign                                        intégrée
  -----------------------------------------------------------------------

Pour le MVP, une iframe servie depuis un domaine VinaSign distinct est
une bonne option d'isolation. Les échanges avec l'application hôte
doivent être limités à des messages explicitement définis.

Si `postMessage` est utilisé, vérifier strictement l'origine exacte de
l'émetteur et du destinataire. Ne jamais accepter `*` comme origine de
confiance pour des opérations sensibles.

### 8.2 Session de signature intégrée

Le backend client demande une session temporaire liée à une enveloppe
précise, un signataire précis, un ensemble d'actions autorisées, une
expiration courte et une utilisation limitée ou un mécanisme anti-rejeu.

Le navigateur reçoit uniquement un jeton éphémère aux droits restreints.
Il ne reçoit jamais le secret client de l'application.

### 8.3 SDK à fournir

-   **Angular SDK :** composant de lancement, gestion des sessions et
    événements UI.
-   **Java SDK :** client API pour Spring Boot.
-   **.NET SDK :** client API pour les applications .NET.
-   **React SDK :** à ajouter selon les besoins des intégrateurs.
-   **Web Component :** option de compatibilité multi-framework.

Les SDK doivent rester des clients de l'API. Ils ne doivent pas
embarquer les règles de sécurité ou le moteur de signature.

## 9. Webhooks et événements

Événements possibles :

-   `envelope.created`
-   `envelope.sent`
-   `signer.viewed`
-   `signer.signed`
-   `signer.declined`
-   `envelope.completed`
-   `envelope.expired`
-   `envelope.cancelled`
-   `document.finalized`

### 9.1 Sécurisation

Chaque webhook devrait contenir un identifiant d'événement unique, un
horodatage, le type d'événement, l'identifiant de l'enveloppe et une
signature cryptographique du contenu ou un mécanisme HMAC robuste.

Prévoir la validation de signature côté client, une fenêtre de validité
de l'horodatage, une protection contre le rejeu, des tentatives de
livraison avec backoff, un journal des livraisons, la rotation des
secrets et un traitement idempotent côté application cliente.

Les destinations webhook doivent être contrôlées pour prévenir les
risques SSRF. Éviter les URL arbitraires non validées, notamment vers
des adresses IP privées, locales ou des services de métadonnées cloud.

## 10. Sécurité de la plateforme

Contrôles à intégrer dès la conception :

-   isolation stricte des tenants au niveau API, services et données ;
-   contrôle d'accès par rôle et par scope ;
-   chiffrement en transit TLS et chiffrement au repos ;
-   stockage privé des documents ;
-   gestion des clés avec KMS ou service équivalent ;
-   antivirus et validation des fichiers téléversés ;
-   limitation des tailles, types et volumes ;
-   journalisation d'audit protégée contre l'altération ;
-   limitation de débit et protection contre les abus ;
-   idempotence des opérations sensibles ;
-   expiration des sessions et jetons ;
-   rotation des secrets et révocation des applications clientes ;
-   surveillance des accès et alertes de sécurité ;
-   politique de conservation et de suppression des documents.

Pour des clients entreprise, mTLS peut être proposé en complément de
OAuth2, selon le niveau de risque et les contraintes d'exploitation.

## 11. Multi-tenant et modèle de données

Le modèle doit permettre à plusieurs organisations et plusieurs
applications clientes d'utiliser le même service, sans fuite de données
entre tenants.

  Entité                 Rôle
  ---------------------- -----------------------------------------
  `tenant`               Organisation cliente
  `client_application`   Application autorisée à utiliser l'API
  `client_credential`    Identifiants et métadonnées de sécurité
  `envelope`             Demande de signature
  `document`             Document source ou final
  `signer`               Signataire associé à une enveloppe
  `signature_field`      Champ de signature ou d'initiales
  `signing_session`      Session éphémère d'un signataire
  `workflow_event`       Historique des transitions
  `audit_event`          Événement de preuve et d'audit
  `webhook_endpoint`     Destination d'événements
  `webhook_delivery`     Tentative de livraison d'un événement

Chaque ressource doit être liée à un tenant. Les vérifications de tenant
doivent être effectuées côté serveur et ne jamais dépendre uniquement
d'un identifiant fourni par le frontend.

## 12. Déploiement et offre de service

Trois modèles de déploiement sont envisageables :

1.  **SaaS mutualisé :** modèle recommandé pour le lancement, avec
    isolation logique stricte entre tenants.
2.  **Instance dédiée :** environnement isolé pour les grands clients ou
    les exigences particulières.
3.  **Déploiement privé / sur site :** option ultérieure, plus coûteuse
    à maintenir et à faire évoluer.

Commencer par un SaaS mutualisé bien sécurisé, tout en concevant les
frontières de configuration et de données pour rendre possible une
instance dédiée.

## 13. Architecture technologique recommandée

-   Frontend : Angular, avec interface responsive et tactile.
-   Backend : Java 21 et Spring Boot 3.
-   API : REST versionnée, contrat OpenAPI.
-   Base de données : PostgreSQL.
-   Sessions et tâches temporaires : Redis.
-   Documents : stockage objet privé.
-   Identité : OAuth2 / OIDC.
-   Clés : KMS ou service de gestion de clés.
-   Notifications : fournisseur courriel et SMS selon les besoins.
-   Signature PDF : composant spécialisé capable de produire et valider
    les signatures numériques PDF/PAdES lorsque ce niveau est requis.

Une architecture modulaire dans un même déploiement Spring Boot est
suffisante au départ. Il n'est pas nécessaire de démarrer avec des
microservices. Les modules doivent toutefois être séparés clairement
afin de permettre une extraction ultérieure si la charge ou les besoins
opérationnels le justifient.

## 14. Gouvernance de l'API et des SDK

Prévoir dès le début :

-   versionnement explicite des routes (`/api/v1`) ;
-   contrat OpenAPI comme référence ;
-   règles de compatibilité ascendante ;
-   politique de dépréciation ;
-   environnements sandbox et production ;
-   clés distinctes par environnement ;
-   quotas, métriques et limites par application ;
-   journal des appels API ;
-   documentation d'intégration et exemples exécutables ;
-   tests de contrat automatisés ;
-   stratégie de publication et de versionnement des SDK.

Les modifications incompatibles doivent passer par une nouvelle version
majeure de l'API ou une période de migration clairement documentée.

## 15. Backlog complémentaire --- Epic E15 : plateforme d'intégration

  -----------------------------------------------------------------------
  ID                      Fonctionnalité          Priorité
  ----------------------- ----------------------- -----------------------
  E15-01                  Définir le contrat      Must
                          OpenAPI v1              

  E15-02                  Gérer les tenants et    Must
                          applications clientes   

  E15-03                  Mettre en place OAuth2  Must
                          Client Credentials      

  E15-04                  Appliquer les scopes et Must
                          l'isolation tenant      

  E15-05                  Ajouter les références  Must
                          externes et clés        
                          d'idempotence           

  E15-06                  Développer les webhooks Must
                          signés et les           
                          mécanismes de reprise   

  E15-07                  Créer les sessions de   Must
                          signature intégrées à   
                          durée limitée           

  E15-08                  Fournir une intégration Must
                          iframe sécurisée        

  E15-09                  Fournir un SDK Angular  Should

  E15-10                  Fournir un SDK Java     Should

  E15-11                  Fournir un SDK .NET     Should

  E15-12                  Fournir un              Should
                          environnement sandbox   

  E15-13                  Ajouter quotas,         Should
                          métriques et suivi par  
                          application             

  E15-14                  Préparer l'option       Could
                          d'instance dédiée       

  E15-15                  Étudier le déploiement  Could
                          privé / sur site        
  -----------------------------------------------------------------------

## 16. Tests d'intégration et critères d'acceptation

La plateforme d'intégration doit notamment démontrer que :

-   une application cliente autorisée peut créer une enveloppe ;
-   une application non autorisée ne peut ni lire ni modifier cette
    enveloppe ;
-   un tenant ne peut accéder aux ressources d'un autre tenant ;
-   un jeton expiré, mal signé ou destiné à une autre audience est
    refusé ;
-   un secret client n'est jamais exposé dans le navigateur ;
-   une session intégrée expirée ou réutilisée est refusée ;
-   un webhook dont la signature est invalide est rejeté ;
-   un webhook livré plusieurs fois ne déclenche pas plusieurs
    traitements métier ;
-   une URL webhook dangereuse ou non autorisée est bloquée ;
-   le PDF final et les preuves sont récupérables uniquement par une
    application autorisée ;
-   les erreurs et tentatives d'intégration sont traçables ;
-   les SDK respectent le contrat OpenAPI et les règles de
    compatibilité.

## 17. Points de vigilance juridiques et de confiance

Il faut distinguer clairement :

-   la représentation visuelle d'une signature manuscrite ;
-   le processus d'identification et de consentement du signataire ;
-   les éléments de preuve du processus ;
-   la signature numérique cryptographique intégrée au PDF, par exemple
    PAdES.

Un tracé manuscrit et une empreinte SHA-256 ne constituent pas, à eux
seuls, une signature numérique PAdES. Le niveau de preuve requis dépend
du contexte, du risque, du type de document et des exigences
applicables.

Les obligations relatives au cadre juridique québécois, à la protection
des renseignements personnels, à la conservation et à la valeur probante
doivent être validées avec des spécialistes juridiques et de sécurité
avant la mise en production.

## 18. Décisions d'architecture recommandées

  -----------------------------------------------------------------------
  Décision                            Recommandation
  ----------------------------------- -----------------------------------
  Nature du produit                   Service de signature indépendant

  Intégration principale              API REST

  Interface intégrée                  iframe cross-origin avec session
                                      éphémère

  SDK                                 Clients légers Angular, Java et
                                      .NET

  Authentification machine            OAuth2 Client Credentials

  Authentification utilisateur        OIDC selon le parcours

  Notifications                       Webhooks signés, avec reprise et
                                      idempotence

  Documents                           Stockage privé, téléversement
                                      temporaire

  Architecture backend initiale       Monolithe modulaire Spring Boot

  Déploiement initial                 SaaS mutualisé

  Source documentaire pour une GED    GED maître, VinaSign opérateur du
                                      workflow
  -----------------------------------------------------------------------

## 19. Conclusion

VinaSign doit être construit comme une **plateforme de signature
électronique réutilisable**, et non comme un simple écran de signature
attaché à une application.

Le cœur du produit sera un moteur centralisé accessible par API. Les
applications clientes pourront l'intégrer à l'aide de SDK, d'une
interface embarquée ou d'une redirection vers le portail VinaSign. Cette
approche permet de réutiliser le même moteur dans plusieurs produits,
notamment une GED, un CRM, une application RH ou une plateforme métier,
tout en conservant une séparation claire des responsabilités et des
frontières de sécurité.
