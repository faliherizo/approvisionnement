# Diagrammes de séquence — flux clés

Diagrammes Mermaid des principaux parcours. Tous passent par le **garde de module** (refus par défaut si
le module n'est pas activé pour le tenant) et le **contexte tenant**.

## 1. Composer, versionner et publier un formulaire

```mermaid
sequenceDiagram
  actor Admin
  participant UI as SPA (Form Builder)
  participant API as formulaire.api
  participant MOD as commun.module (garde)
  participant SVC as formulaire.application
  participant WF as approbation
  participant DB as BD (métadonnées JSON)

  Admin->>UI: Compose pages/champs (types, options, règles)
  UI->>API: POST /formulaires (définition JSON)
  API->>MOD: module "formulaire" actif pour ce tenant ?
  MOD-->>API: OK
  API->>SVC: valider structure (types/règles/réf.)
  SVC->>DB: créer FORMULAIRE_VERSION (brouillon, snapshot)
  Admin->>UI: Soumettre pour approbation
  UI->>API: POST /formulaires/{id}/versions/{v}/approbation
  API->>WF: ouvrir circuit (rôles ordonnés)
  WF-->>API: approuvé
  API->>SVC: publier version
  SVC->>DB: statut = PUBLIÉ (retour arrière possible)
  SVC-->>Admin: formulaire en ligne
```

## 2. Remplir un formulaire (logique conditionnelle, autosave, captcha, soumission)

```mermaid
sequenceDiagram
  actor Usager
  participant UI as SPA (rendu formulaire)
  participant API as saisie.api
  participant REG as regle.application
  participant SVC as saisie.application
  participant AV as antivirus
  participant DB as BD

  Usager->>UI: Ouvre le formulaire public
  UI->>API: GET /formulaires/{code}/rendu
  API->>SVC: fenêtre d'activation ouverte ? (date/nb max/IP)
  SVC-->>UI: structure + règles de visibilité
  loop Saisie
    Usager->>UI: répond à un champ
    UI->>REG: évaluer visibilité/obligation/calcul (client)
    UI->>API: PATCH /saisies/{id} (autosave brouillon)
    API->>DB: maj donnees_json + maj_le
  end
  Usager->>UI: Téléverse un fichier
  UI->>API: POST /saisies/{id}/fichiers
  API->>AV: analyser
  AV-->>API: sain
  Usager->>UI: Soumettre (captcha)
  UI->>API: POST /saisies/{id}/soumettre (jeton captcha)
  API->>REG: revalider côté serveur (règles + format + capacité)
  REG-->>API: valide
  API->>SVC: statut = SOUMIS + index EAV
  SVC-->>Usager: accusé + confirmation déclenchée
```

## 3. Routage d'approbation + notification multicanal

```mermaid
sequenceDiagram
  participant SVC as saisie.application
  participant RE as regle (routage)
  participant WF as approbation
  participant NO as notification
  participant MAIL as Courriel (ACS/SMTP)
  participant SMS as SMS (Twilio/Telnyx)

  SVC->>RE: évaluer règles de routage (champ/opérateur/valeur)
  RE-->>SVC: étape + rôle approbateur
  SVC->>WF: créer décision attendue (historisée)
  SVC->>NO: déclencheur "en attente d'approbation"
  NO->>MAIL: courriel à l'approbateur (modèle i18n)
  NO->>SMS: SMS optionnel
  WF-->>SVC: approuvé / rejeté (conserve l'historique au retour arrière)
  SVC->>NO: déclencheur "décision"
  NO->>MAIL: confirmation au répondant + PDF généré
```

## 4. Génération d'un document PDF depuis les données

```mermaid
sequenceDiagram
  participant SVC as saisie.application
  participant RND as redaction.render (réutilisé)
  participant NO as notification
  SVC->>RND: composer document (gabarit + donnees_json)
  RND-->>SVC: PDF/DOCX
  SVC->>NO: transmettre au répondant/administrateur
```

## 5. Accès par API publique tokenisée (système externe CHU)

```mermaid
sequenceDiagram
  participant Ext as Système externe CHU
  participant OAU as interop (OAuth2)
  participant MOD as commun.module (garde)
  participant API as interop.api
  participant SEC as securite (RBAC + classification)
  participant DB as BD

  Ext->>OAU: client_credentials (client_id/secret ou clé d'API)
  OAU-->>Ext: jeton d'accès (scopes, expiration)
  Ext->>API: POST /api/v1/formulaires/{code}/fiches (jeton)
  API->>MOD: module "interop" actif ? quota ?
  MOD-->>API: OK
  API->>SEC: autorisation + masquage par classification
  SEC-->>API: champs autorisés
  API->>DB: créer/compléter la fiche
  API-->>Ext: 201 + identifiant de fiche (journalisé)
```

## 6. Activer/désactiver un module depuis le superadmin

```mermaid
sequenceDiagram
  actor Superadmin
  participant UI as SPA superadmin
  participant API as commun.module.api
  participant SVC as module.application
  participant DB as BD

  Superadmin->>UI: Active "Notifications" pour le tenant CHU
  UI->>API: PUT /tenants/{id}/modules/notification {actif:true}
  API->>SVC: résoudre dépendances (prérequis)
  SVC->>DB: TENANT_MODULE (notification + dépendances) = actif
  SVC-->>UI: modules activés + vues démasquées
  Note over API,SVC: Toute requête ultérieure sur un module inactif → 403 (refus par défaut)
```