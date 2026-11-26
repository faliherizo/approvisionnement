# ADR-033 — Journal de sécurité lu par le SIEM de l'organisation (Microsoft Sentinel)

- **Statut** : Accepté
- **Date** : 2026-11-25
- **Portée** :
  - `ClientIntegration.Systeme.SIEM`, `ClientIntegration.lecteurJournal()` ;
  - domaine `clm.domain.EvenementSiem` (ASIM « AuditEvent » et CEF) ; port `JournalSecurite`, adaptateur
    `JournalSecuriteAudit` (par `AuditQueryService.depuis`, sans lire les tables du module d'audit) ;
  - `JournalSiemService`, `GET /api/clm/integration/v1/journal`.
- **Origine** : Annexe 10 d'IQ, TI-04 (Essentielle) ; story RST-46. Prolonge ADR-011 (API d'intégration entrante).

## Contexte

- **Existant** : le journal d'audit est lisible et exportable par l'API, mais avec une session utilisateur ; les alertes
  d'exploitation vont à Azure Monitor (hébergement), pas au SIEM d'IQ.
- **TI-04** : à défaut d'un connecteur Defender for Cloud Apps, des API permettant à IQ de récupérer les événements pour
  Microsoft Sentinel.

## Options envisagées

1. **Lecture machine par curseur, jeton d'intégration existant** : un client d'intégration de type SIEM (client
   credentials, ADR-011) lit les événements de son organisation postérieurs à un curseur, normalisés ASIM et CEF.
   Sentinel les collecte par le *Codeless Connector Platform* (sondeur REST) ou une Logic App.
2. **Poussée (syslog/CEF ou Event Hub)** vers un collecteur d'IQ : exige une ouverture réseau entrante chez IQ et un
   état de livraison par client ; à ajouter plus tard comme abonné si IQ le demande.
3. **Connecteur Defender for Cloud Apps** : catalogue Microsoft réservé aux éditeurs référencés ; hors de portée à court
   terme.

## Décision

Option 1.

- **Séparation des usages** : un client `SIEM` ne lit que le journal ; il ne crée ni ne lit aucune trousse (403). Un
  client d'affaires (Workday, Salesforce, autre) ne lit pas le journal (403). Révoquer le client coupe la collecte (401).
- **Curseur** : l'identifiant croissant de l'entrée d'audit ; `apres` = dernier identifiant reçu, `limite` ≤ 500,
  `curseurSuivant` et `suite` dans la réponse. Aucune entrée n'est sautée ni répétée.
- **Format** : chaque événement en champs ASIM (`TimeGenerated`, `EventVendor`, `EventProduct`, `EventSchema`
  = `AuditEvent`, `EventResult`, `EventSeverity`, `ActorUsername`, `Object`, `TargetResourceId`, `EventMessage`,
  `EventOriginalType`, `EventUid`) et en ligne CEF parallèle. Les refus, échecs, infections et altérations sont
  `Failure`/`Medium` ; l'administration et les accès de soutien `Low`.
- **Loi 25** : le détail d'une modification de valeur (`CLM_VALEUR_MODIFIEE`, ADR-030) n'est jamais transmis ; le SIEM
  reçoit l'événement, pas les valeurs.
- **Portée** : le journal de l'organisation tout entier (actions CLM, refus d'accès, connexions, changements de rôle,
  accès de soutien), puisqu'il est déjà borné au tenant.

## Conséquences

- **Positives** : TI-04 démontrable sans session ni développement chez IQ au-delà de la configuration du connecteur ;
  aucune ouverture réseau entrante chez IQ.
- **Négatives** :
  - collecte par sondage (latence du pas de sondage, 5 minutes conseillées) ;
  - le connecteur Sentinel reste à déployer et à éprouver dans l'espace de travail d'IQ ;
  - aucun connecteur Defender for Cloud Apps.
- **Vérification** :
  - `EvenementSiemTest` : résultat, sévérité, champs ASIM, échappement CEF, détail confidentiel ;
  - `JournalSiemServiceTest` : réservé au client SIEM, pages bornées, curseur ;
  - `AnnexeDixIntegrationTest` (RST-46) : collecte complète par curseur, aucune valeur d'affaires, séparation des
    usages, révocation ;
  - scénario e2e `E2E-AX-IQ-07` ; guide `docs/Dossier_IQ_2026-6370-0005/04-Architecture/connecteur-sentinel.md`.
