# Modélisation métier et diagrammes

## 1. MCD conceptuel
```mermaid
erDiagram
  UTILISATEUR ||--o{ PARTICIPATION : agit
  GROUPE ||--o{ GROUPE_MEMBRE : contient
  UTILISATEUR ||--o{ GROUPE_MEMBRE : appartient
  DOSSIER ||--o{ TROUSSE : regroupe
  DOSSIER ||--o{ LIEN_SOURCE : reference
  TROUSSE ||--|{ DOCUMENT : contient
  DOCUMENT ||--o{ VERSION_DOCUMENT : versionne
  MODELE ||--o{ VERSION_MODELE : versionne
  MODELE ||--o{ GENERATION : source
  GENERATION }o--|| DOSSIER : concerne
  TROUSSE ||--o{ WORKFLOW : orchestre
  WORKFLOW ||--|{ ETAPE : comprend
  ETAPE ||--o{ AFFECTATION : assigne
  UTILISATEUR ||--o{ AFFECTATION : reçoit
  DOCUMENT ||--o{ SIGNATURE : porte
  UTILISATEUR ||--o{ SIGNATURE : signe
  WORKFLOW ||--o{ EVENEMENT_AUDIT : trace
  DOCUMENT ||--o{ EVENEMENT_AUDIT : trace
  TROUSSE ||--o{ NOTIFICATION : déclenche
  DOCUMENT ||--o{ PREUVE : produit
  SIGNATURE ||--o{ PREUVE : justifie
  DOSSIER {
    uuid id PK
    string reference_metier
    string statut
    string source_system
    string source_record_id
  }
  TROUSSE {
    uuid id PK
    uuid dossier_id FK
    string titre
    string statut
    datetime created_at
  }
  DOCUMENT {
    uuid id PK
    uuid trousse_id FK
    string nom
    string type_mime
    string statut
    uuid version_courante_id FK
  }
  VERSION_DOCUMENT {
    uuid id PK
    uuid document_id FK
    int numero
    string object_key
    string sha256
    datetime created_at
  }
  MODELE {
    uuid id PK
    string nom
    string statut
    string domaine
  }
  VERSION_MODELE {
    uuid id PK
    uuid modele_id FK
    int numero
    string schema_champs
    string object_key
  }
  WORKFLOW {
    uuid id PK
    uuid trousse_id FK
    string statut
    string mode_execution
  }
  ETAPE {
    uuid id PK
    uuid workflow_id FK
    int ordre
    string type
    string politique_completion
    string statut
  }
  AFFECTATION {
    uuid id PK
    uuid etape_id FK
    uuid utilisateur_id FK
    string role
    string statut
  }
  SIGNATURE {
    uuid id PK
    uuid document_id FK
    uuid utilisateur_id FK
    string statut
    datetime signed_at
    string provider_ref
  }
  EVENEMENT_AUDIT {
    uuid id PK
    string aggregate_type
    uuid aggregate_id
    string action
    datetime occurred_at
    string actor_ref
    string payload_hash
  }
  PREUVE {
    uuid id PK
    uuid document_id FK
    uuid signature_id FK
    string type
    string object_key
    string sha256
  }
```
Ce MCD est une base conceptuelle proposée. Les cardinalités précises, règles de conservation, utilisateurs invités et modèles de données Salesforce doivent être validés.

## 2. Séquence — génération depuis Salesforce
```mermaid
sequenceDiagram
  autonumber
  participant SF as Salesforce Grantmaking
  participant API as API Integration
  participant CASE as Gestion dossiers
  participant TPL as Modèles
  participant GEN as Génération
  participant DOC as Documents
  participant AUD as Audit
  SF->>API: Demande génération + données + référence
  API->>API: Authentifier, valider, idempotence
  API->>CASE: Créer / retrouver dossier source
  CASE->>TPL: Résoudre modèle actif
  TPL-->>CASE: Modèle + version
  CASE->>GEN: Générer document prérempli
  GEN->>DOC: Stocker version brouillon
  DOC->>AUD: Événement DOCUMENT_GENERATED
  GEN-->>API: Référence document + statut
  API-->>SF: Accusé + identifiants corrélés
```

## 3. Séquence — workflow validation / approbation / signature
```mermaid
sequenceDiagram
  autonumber
  actor Initiateur
  participant UI as React
  participant API as Spring API
  participant WF as Workflow Engine
  participant N as Notification
  actor Validateur
  actor Approbateur
  actor Signataire
  participant S as Signature Provider
  participant A as Audit / preuve
  Initiateur->>UI: Soumettre la trousse
  UI->>API: POST /trousses/{id}/soumettre
  API->>WF: Démarrer workflow
  WF->>A: WorkflowStarted
  WF->>N: Notifier validateur(s)
  N-->>Validateur: Invitation
  Validateur->>UI: Valider / commenter / rejeter
  UI->>API: Décision validation
  API->>WF: Transition validée
  WF->>A: Décision horodatée
  WF->>N: Notifier approbateur(s)
  N-->>Approbateur: Demande approbation
  Approbateur->>UI: Approuver
  UI->>API: Décision approbation
  API->>WF: Activer étapes signature
  WF->>S: Créer demandes signature
  S-->>Signataire: Invitation sécurisée
  Signataire->>S: Authentification + consentement + signature
  S-->>WF: Événement signé
  WF->>A: Enregistrer preuve / événements
  WF->>N: Notification complétion
  WF-->>API: Workflow terminé
```

## 4. Séquence — refus et reprise
```mermaid
sequenceDiagram
  actor Intervenant
  participant UI
  participant API
  participant WF
  participant AUD
  Intervenant->>UI: Refuser + commentaire
  UI->>API: POST décision=REJECTED
  API->>WF: Appliquer transition
  WF->>AUD: Journaliser refus et commentaire
  WF->>WF: Suspendre / rediriger vers étape configurée
  WF-->>API: État mis à jour
  API-->>UI: Afficher motif et prochaine action
  Note over WF: Toute reprise conserve l'historique; aucune suppression d'événement.
```

## 5. BPMN 2.0 — processus principal (notation Mermaid flowchart, équivalent de lecture; le BPMN formel importable doit être produit dans un outil BPMN)
```mermaid
flowchart TD
  A((Début)) --> B[Créer dossier / trousse]
  B --> C{Document généré depuis Salesforce ?}
  C -- Oui --> D[Récupérer données et modèle]
  D --> E[Générer document]
  C -- Non --> F[Importer PDF / Word]
  E --> G[Constituer la trousse]
  F --> G
  G --> H[Définir participants et champs]
  H --> I[Soumettre workflow]
  I --> J[Validation préalable]
  J --> K{Validé ?}
  K -- Non --> L[Retour correction / rejet]
  L --> H
  K -- Oui --> M[Approbation]
  M --> N{Approuvé ?}
  N -- Non --> L
  N -- Oui --> O[Signature selon ordre configuré]
  O --> P{Toutes signatures requises reçues ?}
  P -- Non --> Q[Notifier / rappeler / traiter exception]
  Q --> O
  P -- Oui --> R[Sceller document et dossier de preuve]
  R --> S[Exporter / archiver / synchroniser Salesforce]
  S --> T((Fin))
```

## 6. BPMN — exceptions d'intégration
```mermaid
flowchart TD
  A((Message entrant)) --> B[Authentifier et valider schéma]
  B --> C{Valide ?}
  C -- Non --> D[Rejeter + journaliser]
  C -- Oui --> E[Dédupliquer avec clé idempotence]
  E --> F{Déjà traité ?}
  F -- Oui --> G[Retourner résultat antérieur]
  F -- Non --> H[Exécuter transaction métier]
  H --> I{Succès ?}
  I -- Oui --> J[Publier événement / accusé]
  I -- Non --> K{Erreur temporaire ?}
  K -- Oui --> L[Retry avec backoff]
  L --> H
  K -- Non --> M[File d'exception + alerte]
  J --> N((Fin))
  G --> N
  D --> N
  M --> N
```

## 7. États de workflow recommandés
`DRAFT`, `READY`, `VALIDATION_PENDING`, `APPROVAL_PENDING`, `SIGNATURE_PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`, `EXPIRED`, `ON_HOLD`, `ERROR`.
Transitions permises selon rôle, configuration, version du document et événements externes. Un diagramme d'état détaillé devra être raffiné lors des ateliers.
