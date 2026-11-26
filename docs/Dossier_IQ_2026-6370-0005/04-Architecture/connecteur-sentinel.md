# Connecteur Microsoft Sentinel — journal de sécurité du module de signature

Annexe 10, TI-04 · story RST-46 · décision : [ADR-033](../../adr/ADR-033-journal-de-securite-pour-le-siem.md).

Le SIEM d'IQ collecte le journal d'audit de l'organisation par l'API d'intégration, avec un jeton OAuth 2.0
(client credentials), sans session utilisateur. Aucune ouverture réseau entrante n'est requise chez IQ.

## 1. Créer le client SIEM (administration technique d'IQ)

1. *Administration CLM → Intégrations API → Nouveau client d'intégration*.
2. Système : **SIEM (Microsoft Sentinel) — lecture du journal seulement**. Aucune adresse de rappel.
3. Copier l'**identifiant client** et le **secret** : ils ne sont affichés qu'une fois. Les conserver dans le coffre de
   clés d'IQ (Azure Key Vault).

Un client SIEM ne lit que le journal : toute autre opération de l'API lui est refusée (403). Le révoquer coupe la
collecte (401) ; en créer un nouveau pour la rotation du secret.

## 2. Contrat de l'API

| Élément | Valeur |
|---|---|
| Jeton | `POST /api/clm/integration/oauth/token` (`application/x-www-form-urlencoded` : `grant_type=client_credentials`, `client_id`, `client_secret`) → `access_token`, valable une heure |
| Lecture | `GET /api/clm/integration/v1/journal?apres={curseur}&limite={1..500}` avec `Authorization: Bearer {jeton}` |
| Réponse | `{ "evenements": [ … ], "cef": [ … ], "curseurSuivant": 1234, "suite": true }` |
| Curseur | `apres=0` au premier appel, puis la valeur de `curseurSuivant` ; `suite=true` : page pleine, rappeler aussitôt |

Champs de chaque événement (schéma ASIM « AuditEvent ») : `EventUid`, `TimeGenerated`, `EventVendor` (`Vina`),
`EventProduct` (`CLM`), `EventProductVersion`, `EventSchema`, `EventSchemaVersion`, `EventType`, `EventOriginalType`
(action de la plateforme, ex. `CLM_ACCES_REFUSE`, `CLM_ACCES_SOUTIEN`, `CLM_TROUSSE_TERMINEE`), `EventResult`
(`Success`/`Failure`), `EventSeverity` (`Informational`/`Low`/`Medium`), `ActorUsername`, `Object`, `TargetResourceId`,
`EventMessage`. La liste `cef` porte la même suite d'événements en ArcSight CEF, pour un collecteur syslog.

Le détail d'une modification de valeur (`CLM_VALEUR_MODIFIEE`) n'est jamais transmis (Loi 25) : il reste au journal
d'audit de l'organisation.

## 3. Connecteur sans code (Codeless Connector Platform)

Définition du sondeur (`RestApiPoller`) à adapter à l'espace de travail d'IQ ; la table cible est une table
personnalisée `VinaClm_CL` (ou `ASimAuditEventLogs` par une règle de transformation qui recopie les champs ASIM).

```json
{
  "kind": "RestApiPoller",
  "properties": {
    "connectorDefinitionName": "VinaClmAuditCcp",
    "dataType": "VinaClm_CL",
    "auth": {
      "type": "OAuth2",
      "ClientId": "[[parameters('clientId')]",
      "ClientSecret": "[[parameters('clientSecret')]",
      "GrantType": "client_credentials",
      "TokenEndpoint": "https://<hote-de-la-plateforme>/api/clm/integration/oauth/token",
      "TokenEndpointHeaders": { "Content-Type": "application/x-www-form-urlencoded" }
    },
    "request": {
      "apiEndpoint": "https://<hote-de-la-plateforme>/api/clm/integration/v1/journal",
      "httpMethod": "GET",
      "queryWindowInMin": 5,
      "rateLimitQps": 1,
      "retryCount": 3,
      "timeoutInSeconds": 60,
      "queryParameters": { "limite": 500 }
    },
    "paging": {
      "pagingType": "NextPageToken",
      "nextPageTokenJsonPath": "$.curseurSuivant",
      "nextPageParaName": "apres",
      "hasNextFlagJsonPath": "$.suite"
    },
    "response": { "eventsJsonPaths": ["$.evenements"], "format": "json" },
    "dcrConfig": { "streamName": "Custom-VinaClm", "dataCollectionEndpoint": "[[parameters('dcrEndpoint')]", "dataCollectionRuleImmutableId": "[[parameters('dcrId')]" }
  }
}
```

Le sondeur garde le dernier curseur entre deux sondages ; une reprise après panne repart du dernier `curseurSuivant`
enregistré, sans perte ni doublon. À défaut de CCP, une Logic App planifiée (5 minutes) appelle les mêmes points et
envoie `evenements` à l'API d'ingestion des journaux (Logs Ingestion API).

## 4. Règles analytiques suggérées

- Refus d'accès répétés : `EventResult == "Failure"` et `EventOriginalType == "CLM_ACCES_REFUSE"`, plus de 10 en 15 minutes
  pour un même `ActorUsername`.
- Accès de soutien de l'exploitant : tout `EventOriginalType == "CLM_ACCES_SOUTIEN"` (SEC-17, ADR-031).
- Intégrité : `EventOriginalType == "CLM_INTEGRITE_VERIFIEE"` avec `EventResult == "Failure"`.
- Documents infectés : `EventOriginalType == "CLM_DOCUMENT_INFECTE"`.

## 5. Vérification

Éprouvé par `AnnexeDixIntegrationTest` (collecte complète par curseur, séparation des usages, révocation) et par le
scénario e2e `E2E-AX-IQ-07`. **Reste à faire** : déployer le connecteur dans l'espace de travail Sentinel d'IQ et
valider l'ingestion de bout en bout.
