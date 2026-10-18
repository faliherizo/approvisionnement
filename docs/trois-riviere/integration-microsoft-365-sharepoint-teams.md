# Intégration d'une application avec Microsoft 365 — SharePoint / Teams

## 1. Objectif

Cette architecture permet à une application web de déposer des fichiers dans Microsoft 365, principalement dans :

- SharePoint Online
- les fichiers associés à un canal Microsoft Teams
- OneDrive, si nécessaire

L'intégration repose sur :

- Microsoft Entra ID pour l'authentification et l'autorisation
- OAuth 2.0 / OpenID Connect
- Microsoft Graph API
- un backend/BFF Spring Boot ou .NET
- une interface Angular ou React

> **Point important :** les fichiers d'un canal Teams sont généralement stockés dans le site SharePoint associé à l'équipe. Pour gérer les fichiers Teams, Microsoft Graph et SharePoint sont donc étroitement liés.

---

# 2. Architecture cible

```text
┌───────────────────────────┐
│        Application Web    │
│       Angular / React     │
└─────────────┬─────────────┘
              │ HTTPS
              ▼
┌───────────────────────────┐
│        Backend / BFF       │
│      Spring Boot / .NET    │
└─────────────┬─────────────┘
              │
              │ OAuth 2.0
              ▼
┌───────────────────────────┐
│     Microsoft Entra ID     │
│      App Registration      │
└─────────────┬─────────────┘
              │ Access Token
              ▼
┌───────────────────────────┐
│     Microsoft Graph API    │
└─────────────┬─────────────┘
              │
       ┌──────┴────────┐
       ▼               ▼
┌──────────────┐ ┌──────────────┐
│  SharePoint  │ │   OneDrive   │
└──────────────┘ └──────────────┘
       ▲
       │
       │ fichiers Teams
       │
┌──────────────┐
│    Teams     │
└──────────────┘
```

---

# 3. Architecture avec BFF

Pour une application Angular/React, il est recommandé de conserver les opérations Microsoft 365 côté serveur.

```text
Browser
   │
   │ HTTPS
   │ HttpOnly Cookie
   ▼
┌─────────────────────┐
│ Angular / React     │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│ Spring Boot BFF     │
│                     │
│ DocumentService     │
│ Authentication      │
│ MicrosoftGraph      │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│ Microsoft Entra ID   │
└─────────┬───────────┘
          │
          │ Access Token
          ▼
┌─────────────────────┐
│ Microsoft Graph API  │
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│ SharePoint / Teams   │
└─────────────────────┘
```

## Pourquoi utiliser un BFF ?

Le BFF permet notamment de :

- ne pas exposer les secrets d'application au navigateur
- centraliser l'authentification
- contrôler les permissions
- appliquer les règles métier
- valider les fichiers
- journaliser les opérations
- gérer les retries
- gérer les erreurs Microsoft Graph
- éviter de coupler directement le frontend à Microsoft Graph

---

# 4. Deux modes d'intégration

## 4.1 Mode simulation

Pendant le développement, Microsoft 365 peut être simulé.

```text
Angular
   │
   ▼
Spring Boot
   │
   ▼
MockDocumentRepository
   │
   ▼
Répertoire local
```

Exemple :

```text
/mock-sharepoint
    /sites
        /site-123
            /Documents
                /Contrats
                    contrat-123.pdf
```

Cette approche permet de développer le fonctionnel sans dépendre immédiatement d'un environnement Microsoft 365.

---

# 5. Abstraction Repository

Créer une abstraction indépendante de Microsoft :

```java
public interface DocumentRepository {

    Document upload(
        String tenantId,
        String destination,
        InputStream file,
        String fileName
    );
}
```

Implémentation de simulation :

```java
public class MockDocumentRepository
        implements DocumentRepository {

    @Override
    public Document upload(
            String tenantId,
            String destination,
            InputStream file,
            String fileName) {

        // Sauvegarde locale pour simulation
        return new Document(
            fileName,
            destination,
            "UPLOADED"
        );
    }
}
```

Implémentation réelle :

```text
DocumentRepository
       │
       ├── MockDocumentRepository
       │
       └── MicrosoftGraphDocumentRepository
```

L'application métier ne dépend donc pas directement de Microsoft Graph.

---

# 6. Mode réel Microsoft 365

En production :

```text
Application
     │
     ▼
Microsoft Entra ID
     │
     │ Access Token
     ▼
Microsoft Graph
     │
     ▼
SharePoint
```

Le backend doit disposer des permissions nécessaires dans l'App Registration.

---

# 7. Microsoft Entra ID — App Registration

Dans le portail Microsoft Azure :

```text
Microsoft Entra ID
    │
    └── App registrations
           │
           └── New registration
```

Créer une application, par exemple :

```text
Name:
    MonApplication-M365

Supported account types:
    Selon le besoin de l'organisation

Redirect URI:
    https://mon-application.example.com/login/oauth2/code/azure
```

Pour le développement :

```text
http://localhost:8080/login/oauth2/code/azure
```

ou selon la configuration du framework.

---

# 8. Informations à conserver

L'application aura notamment besoin de :

```text
Tenant ID
Client ID
Client Secret
```

Ne jamais mettre le `Client Secret` dans Angular/React.

Il doit rester côté backend.

Exemple :

```properties
AZURE_TENANT_ID=xxxxxxxx
AZURE_CLIENT_ID=xxxxxxxx
AZURE_CLIENT_SECRET=xxxxxxxx
```

En production, privilégier un gestionnaire de secrets tel que :

- Azure Key Vault
- secret manager du cloud utilisé
- variables sécurisées du CI/CD

---

# 9. Permissions Microsoft Graph

Les permissions dépendent du scénario.

Deux grandes approches existent.

## 9.1 Permissions déléguées

Le backend agit au nom d'un utilisateur connecté.

```text
Utilisateur
     │
     ▼
Entra ID
     │
     ▼
Access Token
     │
     ▼
Graph API
```

Utiliser cette approche lorsque les droits doivent suivre ceux de l'utilisateur.

---

## 9.2 Permissions applicatives

Le backend agit comme une application/service.

```text
Backend
   │
   ▼
Entra ID
   │
   ▼
Application Access Token
   │
   ▼
Graph API
```

Cette approche est adaptée aux traitements automatisés ou batch.

> Les permissions exactes doivent être déterminées selon le niveau d'accès requis et appliquées selon le principe du moindre privilège.

---

# 10. Flux OAuth 2.0 — utilisateur

```text
User
 │
 │ 1. Login
 ▼
Application
 │
 │ 2. Authorization Request
 ▼
Microsoft Entra ID
 │
 │ 3. Authentication
 │
 │ 4. Authorization
 ▼
Application / BFF
 │
 │ 5. Access Token
 ▼
Microsoft Graph
 │
 │ 6. API call
 ▼
SharePoint
```

---

# 11. Flux Client Credentials

Pour un traitement serveur :

```text
Backend
   │
   │ Client ID
   │ Client Secret / Credential
   ▼
Microsoft Entra ID
   │
   │ Access Token
   ▼
Microsoft Graph
   │
   ▼
SharePoint
```

Ce mode ne représente pas un utilisateur humain.

---

# 12. API du BFF

Le frontend peut utiliser une API interne simple :

```http
POST /api/documents
Content-Type: multipart/form-data
```

Paramètres :

```text
file
destination
tenantId
```

Exemple :

```bash
curl   -X POST   http://localhost:8080/api/documents   -F "file=@contrat-123.pdf"   -F "destination=/Contrats"
```

Réponse :

```json
{
  "id": "01ABCDEF...",
  "name": "contrat-123.pdf",
  "status": "UPLOADED",
  "destination": "/Contrats"
}
```

---

# 13. Service applicatif

Exemple :

```java
@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public Document upload(
            String tenantId,
            String destination,
            MultipartFile file) throws IOException {

        validate(file);

        return repository.upload(
            tenantId,
            destination,
            file.getInputStream(),
            file.getOriginalFilename()
        );
    }

    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Le fichier est obligatoire"
            );
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Fichier trop volumineux"
            );
        }
    }
}
```

---

# 14. Microsoft Graph

Le repository réel encapsule Microsoft Graph :

```text
MicrosoftGraphDocumentRepository
             │
             ├── Authentication
             │
             ├── Site resolution
             │
             ├── Drive resolution
             │
             ├── Folder resolution
             │
             └── File upload
```

Conceptuellement :

```text
Graph API
   │
   ├── Site
   │
   ├── Drive
   │
   ├── DriveItem
   │
   └── Content
```

---

# 15. SharePoint — structure

Un site SharePoint peut être représenté ainsi :

```text
SharePoint Site
│
├── Document Library
│      │
│      ├── Contrats
│      │    ├── contrat-001.pdf
│      │    └── contrat-002.pdf
│      │
│      └── Rapports
│           ├── rapport-001.pdf
│           └── rapport-002.pdf
│
└── Lists
```

Dans Microsoft Graph, une bibliothèque de documents est généralement représentée par un `drive`.

---

# 16. Dépôt d'un fichier

Flux logique :

```text
POST /api/documents
       │
       ▼
DocumentController
       │
       ▼
DocumentService
       │
       ├── Validation
       │
       ├── Antivirus / sécurité
       │
       └── Repository
               │
               ▼
    MicrosoftGraphDocumentRepository
               │
               ▼
         Microsoft Graph
               │
               ▼
           SharePoint
```

---

# 17. Fichiers Teams

Pour un fichier associé à Teams :

```text
Microsoft Teams
      │
      ▼
Team
      │
      ▼
Channel
      │
      ▼
Files
      │
      ▼
SharePoint
      │
      ▼
Document Library
```

L'application peut donc avoir une abstraction :

```java
public interface CollaborationStorage {

    Document uploadToSharePoint(
        String siteId,
        String driveId,
        String folder,
        InputStream file
    );

    Document uploadToTeamsChannel(
        String teamId,
        String channelId,
        InputStream file
    );
}
```

---

# 18. Gestion des gros fichiers

Pour les fichiers importants, ne pas forcément utiliser un upload HTTP simple.

Utiliser un mécanisme d'upload par session/chunks lorsque le scénario l'exige.

```text
File
 │
 ├── Chunk 1
 ├── Chunk 2
 ├── Chunk 3
 ├── ...
 └── Chunk N
       │
       ▼
Microsoft Graph
       │
       ▼
SharePoint
```

Avantages :

- meilleure gestion des gros fichiers
- reprise après interruption
- limitation des problèmes réseau
- meilleure robustesse

---

# 19. Sécurité

Prévoir au minimum :

### Authentification

```text
Microsoft Entra ID
```

### Autorisation

```text
RBAC
Scopes
Application permissions
Delegated permissions
```

### Sécurité fichier

Vérifier :

```text
Extension
MIME type
Taille
Nom
Contenu
Virus / malware
```

Éviter notamment :

```text
../../fichier
```

ou les chemins contrôlés directement par l'utilisateur.

---

# 20. Multi-tenant

Si l'application est SaaS :

```text
Application
    │
    ├── Tenant A
    │      └── SharePoint Site A
    │
    ├── Tenant B
    │      └── SharePoint Site B
    │
    └── Tenant C
           └── SharePoint Site C
```

Le mapping peut être conservé dans une configuration :

```text
TenantApplication
-------------------------
tenantId
microsoftTenantId
sharePointSiteId
driveId
rootFolder
```

Exemple :

```json
{
  "tenantId": "CLIENT-A",
  "microsoftTenantId": "xxxxxxxx",
  "sharePointSiteId": "xxxxxxxx",
  "driveId": "xxxxxxxx",
  "rootFolder": "/Documents/Contrats"
}
```

---

# 21. Configuration Spring Boot

Exemple conceptuel :

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          azure:
            client-id: ${AZURE_CLIENT_ID}
            client-secret: ${AZURE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
        provider:
          azure:
            issuer-uri:
              https://login.microsoftonline.com/${AZURE_TENANT_ID}/v2.0
```

Pour l'accès Graph, utiliser une bibliothèque Microsoft Graph compatible avec la version de l'API et du SDK retenue.

---

# 22. Configuration par environnement

## DEV

```text
Angular
   │
   ▼
Spring Boot
   │
   ▼
MockDocumentRepository
```

## INTÉGRATION

```text
Angular
   │
   ▼
Spring Boot
   │
   ▼
Microsoft Entra ID
   │
   ▼
Microsoft Graph
   │
   ▼
SharePoint TEST
```

## PRODUCTION

```text
Angular
   │
   ▼
BFF
   │
   ▼
Entra ID
   │
   ▼
Microsoft Graph
   │
   ▼
SharePoint PRODUCTION
```

---

# 23. Stratégie de migration simulation → réel

La transition peut être réalisée sans modifier l'application métier.

```text
Phase 1

DocumentService
       │
       ▼
MockDocumentRepository
```

Puis :

```text
Phase 2

DocumentService
       │
       ▼
DocumentRepository
       │
       ├── Mock
       └── MicrosoftGraph
```

Configuration :

```yaml
document:
  provider: mock
```

Puis :

```yaml
document:
  provider: microsoft-graph
```

Ainsi, le changement de fournisseur est essentiellement une question de configuration et d'infrastructure.

---

# 24. Tests

## Tests unitaires

Tester :

```text
DocumentService
├── fichier null
├── fichier vide
├── fichier trop volumineux
├── extension interdite
├── nom invalide
├── upload réussi
└── erreur repository
```

## Tests d'intégration

Tester :

```text
BFF
 │
 ▼
Entra ID
 │
 ▼
Graph
 │
 ▼
SharePoint TEST
```

## Tests end-to-end

```text
Browser
   │
   ▼
Login
   │
   ▼
Upload
   │
   ▼
BFF
   │
   ▼
SharePoint
   │
   ▼
Validation du fichier
```

---

# 25. Gestion des erreurs

Prévoir notamment :

```text
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
413 Payload Too Large
429 Too Many Requests
5xx Microsoft Graph
```

Pour `429`, respecter les mécanismes de throttling et de retry recommandés par Microsoft Graph.

Architecture :

```text
GraphClient
    │
    ▼
RetryPolicy
    │
    ├── Retry
    ├── Backoff
    └── Max attempts
```

---

# 26. Observabilité

Journaliser :

```text
Correlation ID
Tenant ID
User ID / Application ID
Operation
SharePoint Site
Drive
File name
File size
Duration
Result
Error code
```

Ne jamais journaliser :

```text
Client Secret
Access Token
Refresh Token
```

---

# 27. Architecture finale recommandée

```text
                         ┌──────────────────────┐
                         │    Microsoft Entra   │
                         │          ID          │
                         └──────────┬───────────┘
                                    │
                              OAuth 2.0/OIDC
                                    │
                                    ▼
┌───────────────┐          ┌──────────────────────┐
│ Angular/React │ ─HTTPS─> │     Spring Boot      │
│               │          │         BFF          │
└───────────────┘          └──────────┬───────────┘
                                      │
                                      ▼
                              ┌───────────────┐
                              │ Document      │
                              │ Service       │
                              └───────┬───────┘
                                      │
                                      ▼
                              ┌───────────────┐
                              │ Repository    │
                              │ abstraction   │
                              └───────┬───────┘
                                      │
                         ┌────────────┴────────────┐
                         ▼                         ▼
                ┌─────────────────┐       ┌────────────────────┐
                │ Mock Repository │       │ Microsoft Graph    │
                │                 │       │ Repository         │
                └─────────────────┘       └─────────┬──────────┘
                                                     │
                                                     ▼
                                           ┌──────────────────┐
                                           │ Microsoft Graph  │
                                           └────────┬─────────┘
                                                    │
                                  ┌─────────────────┼─────────────────┐
                                  ▼                 ▼                 ▼
                             SharePoint           Teams           OneDrive
```

---

# 28. Recommandation de conception

Pour une application professionnelle, séparer clairement :

```text
Presentation
     │
     ▼
BFF / API
     │
     ▼
Application Service
     │
     ▼
Domain / Business Rules
     │
     ▼
Port / Interface
     │
     ▼
Microsoft Graph Adapter
     │
     ▼
Microsoft 365
```

Exemple :

```text
src/main/java
│
├── presentation
│   └── DocumentController.java
│
├── application
│   └── DocumentService.java
│
├── domain
│   ├── Document.java
│   └── DocumentRepository.java
│
├── infrastructure
│   ├── mock
│   │   └── MockDocumentRepository.java
│   │
│   └── microsoft
│       ├── MicrosoftGraphClient.java
│       └── MicrosoftGraphDocumentRepository.java
│
└── security
    └── EntraSecurityConfiguration.java
```

Cette organisation permet de remplacer ultérieurement SharePoint par un autre stockage sans modifier le cœur métier.

---

# 29. Résumé

La stratégie recommandée est :

```text
1. Développer l'application avec MockDocumentRepository
                    ↓
2. Définir DocumentRepository
                    ↓
3. Créer l'App Registration Entra ID
                    ↓
4. Configurer OAuth 2.0 / OIDC
                    ↓
5. Configurer les permissions Graph
                    ↓
6. Implémenter MicrosoftGraphDocumentRepository
                    ↓
7. Tester sur un SharePoint de développement
                    ↓
8. Ajouter Teams si nécessaire
                    ↓
9. Ajouter sécurité / retry / monitoring
                    ↓
10. Passer en production
```

## Références Microsoft à consulter

- Microsoft Graph : https://learn.microsoft.com/graph/
- Microsoft Entra ID : https://learn.microsoft.com/entra/
- Microsoft Graph — SharePoint : https://learn.microsoft.com/graph/api/resources/sharepoint
- Microsoft Graph — Drive / fichiers : https://learn.microsoft.com/graph/api/resources/drive
- Microsoft Graph — Teams : https://learn.microsoft.com/graph/api/resources/teams-api-overview
- Microsoft identity platform : https://learn.microsoft.com/entra/identity-platform/
