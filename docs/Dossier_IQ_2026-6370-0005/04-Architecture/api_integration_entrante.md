# API d'intégration entrante — Workday, Salesforce et autres systèmes

Permet à un système d'IQ de déclencher des signatures sans passer par l'écran : Workday pour les documents RH
(exigence TI-22, ajoutée par l'addenda 1), Salesforce Grantmaking pour les signatures en lot (AF-47, réponse Q10), ou
tout autre système métier. Décision d'architecture : [ADR-011](../../adr/ADR-011-api-integration-entrante.md).

## 1. Parcours type

```text
Système d'IQ                          Plateforme (module Contrats et signature)
    │ 1. POST /oauth/token (client credentials)  ─────────►  jeton d'une heure
    │ 2. POST /v1/trousses ou /v1/lots           ─────────►  trousse(s) créée(s), parcours lancé(s)
    │                                                         … validation, approbation, signature …
    │ 3. ◄──── POST <adresse de rappel>  (statut signé HMAC-SHA256, avec reprises)
    │ 4. GET /v1/trousses/{id}/documents/{doc}/contenu  ──►  document signé
    │    GET /v1/trousses/{id}/preuve                   ──►  dossier de preuve (ZIP)
```

Base des adresses : `https://<organisation>.<domaine>/api/clm/integration`.

## 2. Créer un client d'intégration

Écran **Intégrations API** (administrateur), bloc « Clients d'intégration entrants » : nom, système (Workday, Salesforce,
autre), adresse de rappel en https (facultative). La plateforme remet **une seule fois** :

| Valeur | Usage | Conservation par la plateforme |
|---|---|---|
| Identifiant client (`clm-…`) | Demande de jeton | En clair |
| Secret client | Demande de jeton | Empreinte à clé (HMAC du serveur) seulement |
| Clé de signature des rappels | Vérifier `X-CLM-Signature` | Chiffrée |

Le secret et la clé se rangent dans le coffre de secrets du système appelant, jamais dans un fichier versionné. La
révocation est immédiate : tout jeton du client est refusé, et les rappels en attente ne partent plus.

## 3. Obtenir un jeton

```bash
curl -s -X POST https://iq.<domaine>/api/clm/integration/oauth/token \
  -d grant_type=client_credentials -d client_id="$CLM_CLIENT_ID" -d client_secret="$CLM_CLIENT_SECRET"
# {"access_token":"…","token_type":"Bearer","expires_in":3600}
```

Un secret ou un identifiant erroné, comme un client révoqué, répond `401 {"error":"invalid_client"}`, sans préciser
lequel.

## 4. Créer une trousse

`POST /v1/trousses` avec l'en-tête `Authorization: Bearer <jeton>`.

```json
{
  "titre": "Lettre d'offre — poste 4471",
  "referenceExterne": "WD-BP-2026-4471",
  "initiateur": "conseillere.rh@iq.example",
  "modeleId": null,
  "sfDossierId": null,
  "risque": "MOYEN",
  "echeance": "2026-12-15",
  "valeurs": {},
  "documents": [{ "nom": "offre.pdf", "contenuBase64": "JVBERi0xLjQK…" }],
  "signataires": [{ "nom": "Alex Candidat", "courriel": "alex@exemple.ca", "telephone": "+15145550144",
                    "roleContrat": "SIGNATAIRE_1", "externe": true }],
  "lancer": true
}
```

| Champ | Règle |
|---|---|
| `initiateur` | Obligatoire : courriel d'un compte actif de l'organisation. La trousse lui appartient et suit les mêmes droits qu'à l'écran. |
| `titre` | Sinon « Dossier <sfDossierId> », sinon `referenceExterne` ; l'un des trois est requis. |
| `referenceExterne` | Identifiant de l'objet d'origine (événement Workday, demande Salesforce), renvoyé dans chaque rappel. 120 caractères au plus. |
| `modeleId` | Modèle publié : il génère le contrat si aucun document n'est joint, et fournit son parcours. |
| `sfDossierId` | Préremplit les valeurs depuis Salesforce (même mécanisme que l'écran). |
| `documents` | PDF ou Word en base64, analysés par l'antivirus, 25 Mo par défaut. |
| `signataires` | Sans parcours dans le modèle : une étape de signature, tous les signataires en parallèle, tous requis. Le téléphone est requis quand le risque impose une vérification par SMS. |
| `lancer` | `true` lance le parcours ; sinon la trousse reste en brouillon pour l'initiateur. |

Réponse `201` : `id`, `reference`, `statut`, `source` (`WORKDAY`, `SALESFORCE` ou `INTEGRATION`), `referenceExterne`,
`documents`. Erreurs : `400` (donnée invalide, message explicite), `403` (initiateur hors organisation, module non
souscrit).

## 5. Signature en lot

`POST /v1/lots` : `libelle`, `modeleId`, `initiateur` et `lancer` valent pour chaque élément qui ne les précise pas ;
`elements` est une liste de demandes du § 4 (500 au plus).

```json
{ "libelle": "Conventions octobre", "modeleId": 42, "initiateur": "analyste@iq.example", "lancer": true,
  "elements": [ { "sfDossierId": "DEM-2026-0101", "signataires": [ … ] },
                { "sfDossierId": "DEM-2026-0102", "signataires": [ … ] } ] }
```

Chaque élément est créé dans sa propre transaction : un échec n'empêche pas les autres. La réponse donne le statut du
lot (`TERMINE`, `TERMINE_AVEC_ECHECS`) et, par élément, la trousse créée ou le motif de l'échec. `GET /v1/lots/{id}`
relit un lot. Le même lot se lance depuis l'écran **Signature en lot** (un dossier par ligne).

## 6. Rappels de statut

À chaque transition d'une trousse qu'il a créée, le client reçoit un `POST` à son adresse de rappel :

```http
POST /clm/rappels HTTP/1.1
Content-Type: application/json; charset=utf-8
X-CLM-Signature: sha256=5f0c…
Idempotency-Key: 7:1532:SIGNATURE_TERMINEE:31

{"evenement":"SIGNATURE_TERMINEE","cleIdempotence":"7:1532:SIGNATURE_TERMINEE:31","trousseId":1532,
 "reference":"TR-2026-01532","referenceExterne":"WD-BP-2026-4471","statut":"TERMINEE",
 "horodatage":"2026-11-12T15:04:05Z","donnees":{"signatures":1,"termineeLe":"…"}}
```

| Événement | Moment |
|---|---|
| `LANCEE` | Parcours lancé |
| `RETOURNEE` | Retour pour correction |
| `REJETEE` | Rejet |
| `ANNULEE` | Annulation |
| `SIGNATURE_TERMINEE` | Toutes les signatures recueillies : le document signé et la preuve sont disponibles |

Vérification côté destinataire : calculer HMAC-SHA256 du **corps brut reçu** avec la clé de signature, comparer à
`X-CLM-Signature` à temps constant, puis dédoublonner par `Idempotency-Key`. Répondre `2xx` dès l'enregistrement ; toute
autre réponse déclenche une reprise (1, 2, 4… minutes, plafonnées à une heure, 8 tentatives), puis l'administrateur peut
relancer depuis le bloc « Rappels aux systèmes externes ».

## 7. Récupérer le document signé et la preuve

- `GET /v1/trousses/{id}` : statut et documents (identifiant, version, empreinte, statut `SIGNE`).
- `GET /v1/trousses/{id}/documents/{documentId}/contenu` : fichier.
- `GET /v1/trousses/{id}/preuve` : dossier de preuve ZIP (documents, journal scellé, certificat, métadonnées).

Un client ne lit que les trousses qu'il a créées (`404` sinon) ; chaque téléchargement est inscrit au journal de la
trousse.

## 8. Mise en œuvre côté Workday (proposition, à valider en atelier)

| Étape | Mécanisme Workday envisagé |
|---|---|
| Déclenchement | Étape d'intégration d'un processus métier (ex. offre d'emploi) ou intégration Workday Studio / Orchestrate qui appelle `POST /v1/trousses` |
| Document | Document produit par Workday transmis en base64, ou contrat généré par un modèle de la plateforme |
| Retour de statut | Point de réception des rappels (Workday Orchestrate ou intergiciel d'IQ), qui met à jour le processus métier |
| Document signé | Récupéré par `GET …/contenu` puis rattaché au dossier de l'employé (ex. service Web de dépôt de document du travailleur) |

Le choix exact (Studio, Orchestrate, intergiciel), les processus métier visés et les droits du compte d'intégration
Workday sont à confirmer avec IQ (questions W1 à W6).

## 9. Essai sans système réel

Le test `IntegrationEntranteIntegrationTest` (backend) et le test e2e `TI-22 — client Workday…`
(`e2e/views/clm.exigences.spec.ts`) rejouent ce parcours : client créé, jeton, trousse lancée, rappel signé reçu par un
serveur local et vérifié, document et preuve récupérés, révocation.
