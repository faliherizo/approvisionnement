# Architecture technique — DDD, Clean Architecture & SOLID

**Avis # 2026-50-92 — Ville de Lévis**

Ce document décrit l'organisation technique du backend Java (Spring Boot) selon les
principes du **Domain-Driven Design (DDD)**, de la **Clean / Hexagonal Architecture**
et **SOLID**, et documente l'API **OpenAPI 3.0**.

---

## 1. Bounded contexts (DDD)

La solution se découpe en contextes délimités cohérents avec le processus BPMN :

| Bounded context | Agrégats / entités | Responsabilité |
|-----------------|--------------------|----------------|
| **Rédaction** | `Clause`, `ClauseCondition` | Bibliothèque et adaptation des clauses |
| **Document** | `Document`, `DocumentClause` | Assemblage et cycle de vie du devis |
| **Approbation** | `ApprovalStep` | Circuit hiérarchisé |
| **Collaboration** | `Comment` | Commentaires en ligne |
| **Addenda** | `Addenda`, `AddendaChange` | Modifications post-publication |
| **Signature** | `Signataire` | Cérémonie de signature électronique |
| **Identité & accès** | `User`, `SecurityQuestion` | Auth, MFA, RBAC |
| **Audit** | `AuditLog` | Journalisation transversale |

Chaque contexte possède son **agrégat racine** et son **dépôt** (repository), évitant
le couplage direct entre contextes (communication via services applicatifs).

---

## 2. Couches (Clean / Hexagonal Architecture)

```
┌──────────────────────────────────────────────────────────────┐
│  ADAPTATEURS ENTRANTS  (api/)                                  │
│  Contrôleurs REST + DTOs  →  traduisent HTTP ⇄ cas d'usage     │
├──────────────────────────────────────────────────────────────┤
│  APPLICATION  (service/)                                        │
│  AuthService, DocumentService, AddendaService, SignatureService│
│  RuleEngine, RenderService, AuditService  →  orchestration      │
│  PORTS (service/port/)  →  interfaces (AssistantRedactionPort) │
├──────────────────────────────────────────────────────────────┤
│  DOMAINE  (domain/)                                            │
│  Entités, agrégats, invariants, énumérations métier            │
├──────────────────────────────────────────────────────────────┤
│  ADAPTATEURS SORTANTS  (repo/, security/, config/)             │
│  Spring Data JPA, JWT/TOTP, Ollama, Liquibase, OpenAPI         │
└──────────────────────────────────────────────────────────────┘
```

- **Règle de dépendance** : les couches externes dépendent des couches internes,
  jamais l'inverse. Le domaine ne dépend d'aucun framework métier.
- **Ports & adaptateurs** : l'assistance IA est un **port** (`AssistantRedactionPort`)
  implémenté par l'**adaptateur** `OllamaService`. Le contrôleur dépend du port, ce
  qui permet de substituer Ollama (Azure OpenAI, Claude) sans modification.

> Choix pragmatique assumé : les entités du domaine portent les annotations JPA
> (couplage ORM) pour éviter une couche de mapping supplémentaire. Le port IA
> illustre l'inversion de dépendance là où la substituabilité a le plus de valeur.

---

## 3. Cartographie SOLID

| Principe | Application concrète |
|----------|----------------------|
| **S — Responsabilité unique** | `RuleEngine` (règles), `RenderService` (rendu), `AuthService` (auth), `AuditService` (journal) : une raison de changer chacun |
| **O — Ouvert/fermé** | Nouvelle IA = nouvel adaptateur du port ; nouvelle règle = nouvelle `ClauseCondition` (donnée), sans toucher au moteur |
| **L — Substitution de Liskov** | Tout `AssistantRedactionPort` est interchangeable ; les dépôts respectent le contrat `JpaRepository` |
| **I — Ségrégation des interfaces** | Ports fins et ciblés (`AssistantRedactionPort` n'expose que `rediger`) ; dépôts par agrégat |
| **D — Inversion des dépendances** | `AiController` dépend de l'abstraction `AssistantRedactionPort`, pas de `OllamaService` |

Autres leviers : injection par constructeur (testabilité), DTOs immuables (`record`),
règles métier externalisées en **données** (maintenables par les juristes).

---

## 4. OpenAPI 3.0

- Spécification générée par **springdoc-openapi** : `GET /v3/api-docs` (JSON).
- **Swagger UI** interactif : `GET /swagger-ui.html`.
- Métadonnées configurées dans `config/OpenApiConfig`.
- ~29 opérations documentées automatiquement à partir des contrôleurs et DTOs.

---

## 5. Arborescence des paquets

```
qc.levis.contrat
├── api/            adaptateurs entrants : contrôleurs REST + Dtos
├── service/        cas d'usage (application)
│   └── port/       ports (interfaces) — architecture hexagonale
├── domain/         entités, agrégats, énumérations (DDD)
├── repo/           dépôts Spring Data (adaptateurs sortants)
├── security/       JWT, filtre cookie, TOTP
└── config/         sécurité, CORS, OpenAPI, Liquibase seed
```
