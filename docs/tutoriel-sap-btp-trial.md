# Tutoriel — Service OData/CAP sur SAP BTP Trial (test de connexion **et** de dépôt réel)

> **Public visé** : l'administrateur / développeur qui veut valider **de bout en bout** l'intégration
> SAP de l'application — **connexion** *et* **dépôt écrit** — sans dépendre du bac à sable public
> `api.sap.com` (souvent en lecture seule ou en panne « Anmeldung fehlgeschlagen »).
>
> **Principe** : on déploie **notre propre** micro-service **OData V4** avec **SAP CAP** (Cloud
> Application Programming model) sur **SAP BTP Trial** (gratuit). Ce service **accepte l'écriture**
> (`POST`) et renvoie une **référence**, exactement comme l'application l'attend. On le branche ensuite
> dans **Paramètres → Intégration SAP**.

> 🎯 **Contrat que le service doit respecter** (ce que l'application envoie — voir
> `SapODataAdapter`) :
> - **Test de connexion** : `GET {base}{chemin}?$top=1` → doit répondre **2xx**.
> - **Dépôt** : `POST {base}{chemin}` avec le corps JSON
>   `{"NumeroAO":"…","Objet":"…","NombreDocuments":3,"MontantTotal":"…"}`
>   (⚠️ `MontantTotal` est envoyé **en chaîne de caractères**), et la réponse doit contenir un champ
>   **`Reference`** (ou `ReferenceId`, ou `id`).
> - **Authentification** : en-tête **`APIKey: <clé>`** (mode « Clé API » de l'application).

---

## 0. Pré-requis (poste local)

| Outil | Installation | Vérifier |
|-------|-------------|----------|
| **Node.js LTS** (≥ 20) | <https://nodejs.org> | `node -v` |
| **CAP dev kit** | `npm i -g @sap/cds-dk` | `cds -v` |
| **Cloud Foundry CLI v8** | <https://github.com/cloudfoundry/cli/releases> (ou `brew install cloudfoundry/tap/cf-cli@8`) | `cf -v` |

## 1. Créer le compte **SAP BTP Trial** (gratuit)

1. Aller sur <https://www.sap.com/products/technology-platform/trial.html> → **« Start your free trial »**.
2. Créer / utiliser un **SAP Universal ID** (courriel + mot de passe). Aucune carte bancaire.
3. Ouvrir le **cockpit BTP** → **« Enter Your Trial Account »** : SAP crée automatiquement une
   *sous-compte* d'essai (souvent nommé **`trial`**) avec **Cloud Foundry activé** et une **région**
   (ex. `US10`, `AP21`/Singapour, `EU10`). Un **espace** (`dev`) est créé automatiquement.

> Le compte d'essai est gratuit ; il « hiberne » après quelques heures d'inactivité — un simple redémarrage
> depuis le cockpit le réveille.

## 2. Relever les coordonnées **Cloud Foundry**

Dans le cockpit BTP, ouvrez le sous-compte **`trial`** → onglet **« Overview »**. Notez :

- **API Endpoint** (ex. `https://api.cf.us10-001.hana.ondemand.com`),
- **Org** (ex. `…trial`), **Space** (`dev`),
- le **domaine des applications** (ex. `cfapps.us10-001.hana.ondemand.com`) — il compose l'URL publique.

Connectez-vous :

```bash
cf login -a https://api.cf.us10-001.hana.ondemand.com   # votre API Endpoint
# SSO : cf login --sso  (le cockpit → « … » → "Get a passcode" fournit le code)
cf target   # confirme org + space
```

## 3. Générer le projet **CAP**

```bash
cds init sap-ref-service
cd sap-ref-service
npm install
npm add @sap/cds
npm add @cap-js/sqlite -D   # base en mémoire pour la démo (données volatiles — suffisant pour tester)
```

### 3.1 Modèle de données — `db/schema.cds`

```cds
namespace sap.ref;

// Une « référence financière » telle que l'application la dépose.
entity References {
  key Reference     : UUID;                 // clé auto-générée → renvoyée à la création
      NumeroAO       : String;
      Objet          : String;
      NombreDocuments: Integer;
      MontantTotal   : String;              // envoyé EN CHAÎNE par l'application (ne pas mettre Decimal)
      creeLe         : Timestamp @cds.on.insert: $now;
}
```

### 3.2 Service OData V4 — `srv/service.cds`

```cds
using sap.ref as db from '../db/schema';

@path: '/odata/v4/sap-ref'                  // → chemin du service = /odata/v4/sap-ref/References
service SapRefService {
  entity References as projection on db.References;
}
```

> Chemin OData complet : **`/odata/v4/sap-ref/References`**. `GET …?$top=1` (test) et `POST …` (dépôt)
> y fonctionnent nativement. La clé `Reference` (UUID) est **générée par CAP à la création** et
> **renvoyée** dans la réponse → l'application la lit comme identifiant de référence.

### 3.3 Contrôle de la clé API — `srv/server.js`

L'application envoie l'en-tête **`APIKey`**. On rejette toute requête sans la bonne clé (miroir exact du
mode « Clé API ») :

```js
const cds = require('@sap/cds')

// La clé attendue vient d'une variable d'environnement (jamais en dur, jamais versionnée).
const CLE_ATTENDUE = process.env.SAP_REF_APIKEY

cds.on('bootstrap', (app) => {
  app.use('/odata/v4/sap-ref', (req, res, next) => {
    if (!CLE_ATTENDUE || req.headers.apikey === CLE_ATTENDUE) return next()
    res.status(401).json({ error: { code: '401', message: 'APIKey invalide ou absente.' } })
  })
})

module.exports = cds.server
```

> ⚠️ Sans `SAP_REF_APIKEY` défini, le service reste **ouvert** (pratique en local, à proscrire déployé).

### 3.4 `package.json` — script de démarrage

Ajoutez le profil et le script (bloc `cds` + `scripts.start`) :

```jsonc
{
  "scripts": { "start": "cds-serve" },
  "cds": {
    "requires": {
      "db": { "kind": "sqlite", "credentials": { "url": ":memory:" } }
    }
  }
}
```

### 3.5 Essai **local** (avant déploiement)

```bash
export SAP_REF_APIKEY="ma-cle-demo-123"
cds watch        # démarre sur http://localhost:4004
```

Dans un autre terminal :

```bash
# Test de connexion (GET) — 200 attendu
curl -s -o /dev/null -w "%{http_code}\n" \
  'http://localhost:4004/odata/v4/sap-ref/References?$top=1' -H "APIKey: ma-cle-demo-123"

# Dépôt (POST) — 201 + la Reference générée
curl -s 'http://localhost:4004/odata/v4/sap-ref/References' \
  -H "APIKey: ma-cle-demo-123" -H "Content-Type: application/json" \
  -d '{"NumeroAO":"7000017905","Objet":"Acquisition solution","NombreDocuments":3,"MontantTotal":"150000"}'
# → {"@odata.context":"...","Reference":"<uuid>","NumeroAO":"7000017905",...}

# Sans clé → 401 (prouve le contrôle d'auth)
curl -s -o /dev/null -w "%{http_code}\n" 'http://localhost:4004/odata/v4/sap-ref/References?$top=1'
```

## 4. Déployer sur **Cloud Foundry** (BTP Trial)

### 4.1 `manifest.yml` (à la racine du projet)

```yaml
applications:
  - name: sap-ref-service
    random-route: true
    memory: 256M
    buildpacks:
      - nodejs_buildpack
    command: npx cds-serve
    env:
      SAP_REF_APIKEY: ma-cle-demo-123     # la clé que vous mettrez dans l'application
      NODE_ENV: production
```

### 4.2 Pousser

```bash
cf push
# … à la fin, CF affiche la « route » publique, ex. :
#   routes: sap-ref-service-xxxx.cfapps.us10-001.hana.ondemand.com
cf apps                       # retrouver l'URL
cf logs sap-ref-service --recent   # en cas de souci de démarrage
```

> 💡 La route est en **HTTPS**. L'**URL de base** de l'application =
> `https://sap-ref-service-xxxx.cfapps.us10-001.hana.ondemand.com`.

### 4.3 Vérifier le service déployé

```bash
BASE="https://sap-ref-service-xxxx.cfapps.us10-001.hana.ondemand.com"   # votre route
curl -s -o /dev/null -w "GET  %{http_code}\n" "$BASE/odata/v4/sap-ref/References?\$top=1" -H "APIKey: ma-cle-demo-123"
curl -s -w "\nPOST %{http_code}\n" "$BASE/odata/v4/sap-ref/References" \
  -H "APIKey: ma-cle-demo-123" -H "Content-Type: application/json" \
  -d '{"NumeroAO":"7000017905","Objet":"Test dépôt","NombreDocuments":2,"MontantTotal":"58000"}'
```

`GET 200` + `POST 201` avec une `Reference` → le service **inscriptible** est prêt.

## 5. Brancher dans l'application — **Paramètres → Intégration SAP**

Rôle **admin**, section **« Intégration SAP »** :

| Champ | Valeur |
|-------|--------|
| **Activer** | ☑ |
| **URL de base du service OData** | `https://sap-ref-service-xxxx.cfapps.us10-001.hana.ondemand.com` |
| **Chemin du service** | `/odata/v4/sap-ref/References` |
| **Authentification** | **Clé API** |
| **Clé API** | `ma-cle-demo-123` *(= `SAP_REF_APIKEY`)* |

1. **Enregistrer**.
2. **Tester la connexion** → **✓ Connexion SAP réussie (200)** (GET sur le service).
3. Depuis un **dossier**, lancer **« Transmettre la référence à SAP »** → l'application `POST` la
   référence, le service CAP la crée et renvoie la `Reference` → **dépôt réel validé**. 🎉

> Vérifier côté service que la donnée est bien arrivée :
> ```bash
> curl -s "$BASE/odata/v4/sap-ref/References?\$orderby=creeLe desc&\$top=3" -H "APIKey: ma-cle-demo-123"
> ```

## 6. Nettoyage / coûts

- **Gratuit** : le compte d'essai ne facture rien ; l'app CF « hiberne » à l'arrêt.
- Supprimer quand terminé : `cf delete sap-ref-service -r`.
- La base est **en mémoire** : les dépôts sont **volatils** (perdus au redémarrage). Pour les conserver,
  ajouter **SAP HANA Cloud** (essai) — hors périmètre de ce tutoriel de validation.

---

## Récapitulatif

| Étape | Résultat |
|-------|----------|
| Compte BTP Trial + Cloud Foundry | Org / Space / API Endpoint |
| Projet CAP (`References`, OData V4, contrôle `APIKey`) | Service inscriptible conforme au contrat de l'app |
| `cf push` | URL publique HTTPS = **URL de base** |
| Paramètres → Intégration SAP (Clé API + Activer) | Config par tenant, secret chiffré |
| **Tester la connexion** (GET) | ✓ 200 |
| **Transmettre à SAP** (POST) | ✓ 201 + `Reference` → **dépôt réel validé** |

> Bac à sable en lecture seule (auth uniquement) : [`tutoriel-sap-sandbox.md`](tutoriel-sap-sandbox.md).
> Cadre général et per-tenant : [`tutoriel-sap.md`](tutoriel-sap.md).
> Détails techniques (adaptateur, chiffrement) : [`integration-m365-sap.md`](integration-m365-sap.md).
