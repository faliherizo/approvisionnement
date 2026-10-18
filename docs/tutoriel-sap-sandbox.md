# Tutoriel — Connecter le bac à sable SAP Business Accelerator Hub (api.sap.com)

> **Public visé** : l'administrateur du tenant qui remplit **Paramètres → Intégration SAP**.
> **Objectif** : brancher **gratuitement** l'environnement *bac à sable* de SAP Business Accelerator
> Hub (`https://api.sap.com`) pour **valider la connexion et l'authentification** de l'application à un
> service OData SAP — **sans SAP réel**.

> ⚠️ **À lire d'abord — ce que le bac à sable permet (et pas).** Le *sandbox* de SAP Business
> Accelerator Hub est **en lecture seule** : il répond aux **lectures** (`GET`) mais **refuse
> généralement les écritures** (`POST`/création). On l'utilise donc pour valider **l'authentification
> et la connectivité** (bouton **« Tester la connexion »** de l'application, qui fait un `GET`). Pour un
> **dépôt réellement écrit**, il faut un service **inscriptible** : le **SAP de la Ville**, ou un petit
> service OData que vous déployez sur **SAP BTP Trial**.

---

## 1. Créer un compte SAP (gratuit) et se connecter

1. Aller sur <https://api.sap.com>.
2. **Log On** (en haut à droite) → créer un **SAP Universal ID** gratuit si vous n'en avez pas
   (courriel + mot de passe), puis se connecter.

> Le compte est gratuit ; aucune carte bancaire n'est demandée pour le bac à sable.

## 2. Récupérer votre **clé API** (API Key)

1. Une fois **connecté**, cliquez sur votre **profil / « Settings »** (ou la pastille en haut à droite)
   → **« Show API Key »**.
2. Copiez la **clé API** affichée (une longue chaîne). C'est le secret d'accès au **bac à sable** —
   il s'envoie dans l'en-tête **`APIKey`** (c'est exactement le mode `apikey` de l'application).

> 🔑 Cette clé est **personnelle** : ne la partagez pas. Dans l'application, elle est **chiffrée** et
> **jamais réaffichée**.

## 3. Choisir une **API OData** et relever l'**URL du bac à sable**

> ⚠️ **Prenez une API OData, pas SOAP.** Un service en `…BULKREPLIC` / *Bulk Replicate* est du **SOAP**
> (réplication en masse) et ne convient pas : l'application envoie de l'**OData JSON**. Choisissez
> **« Business Partner (A2X) » = `API_BUSINESS_PARTNER`** :
> <https://api.sap.com/api/API_BUSINESS_PARTNER/overview>.

**La méthode la plus fiable — « Code Snippet » — donne l'URL complète *et* la clé d'un seul coup**
(les onglets « Configure Environments » changent de nom/place selon les versions du site) :

1. Sur la page de l'API, **connecté**, ouvrez l'onglet **« API Reference »**
   (dans l'URL : `…/api/API_BUSINESS_PARTNER/resource`).
2. Dépliez un endpoint de **lecture**, ex. **`GET /A_BusinessPartner`**.
3. Cliquez **« Try Out »** (en haut à droite du panneau de l'endpoint).
4. Cliquez **« Code Snippet »** → choisissez **cURL**.
5. SAP génère la commande complète, **clé incluse** :

   ```
   curl --location 'https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap/API_BUSINESS_PARTNER/A_BusinessPartner?$top=1' \
     --header 'APIKey: VOTRE_CLE' --header 'Accept: application/json'
   ```

Vous y lisez **hôte + chemin + clé**. Découpez l'URL en **deux** pour l'application :
   - **URL de base du service OData** = la racine, ex.
     `https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap`
   - **Chemin du service** = ce qui reste, ex. `/API_BUSINESS_PARTNER/A_BusinessPartner`
     *(nom d'une entité/collection exposée par le service)*.

> 💡 Le découpage exact (racine vs chemin) n'a pas d'importance tant que **base-url + chemin** =
> l'URL complète de la collection OData que vous voulez atteindre.

> ⚠️ **Panne côté SAP possible.** Si toutes les API S/4HANA du bac à sable renvoient un **401** avec une
> page HTML **« Anmeldung fehlgeschlagen »** (échec de connexion), c'est le **backend sandbox de SAP**
> qui est indisponible — pas votre clé ni votre URL. Une **clé invalide** donne au contraire un JSON
> `{"fault":{"faultstring":"Invalid ApiKey"}}`. Dans le premier cas : réessayez plus tard, ou passez à
> **SAP BTP Trial** (§6) pour une validation qui ne dépend pas du sandbox public.

## 4. Renseigner l'écran **Paramètres → Intégration SAP**

Dans l'application (rôle **admin**), section **« Intégration SAP »** :

| Champ | Valeur (exemple bac à sable) |
|-------|------------------------------|
| **Activer** | ☑ (nécessaire pour tester) |
| **URL de base du service OData** | `https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap` |
| **Chemin du service** | `/API_BUSINESS_PARTNER/A_BusinessPartner` |
| **Authentification** | **Clé API** |
| **Clé API** | *(collez la clé de l'étape 2)* |

Cliquez **Enregistrer**, puis **« Tester la connexion »**.

- **✓ Connexion SAP réussie (200)** → l'authentification et l'URL sont **valides** : l'application sait
  parler à SAP. 🎉
- **✗ SAP a répondu 401/403** → clé API absente/incorrecte, ou API non couverte par la clé → revoyez
  l'étape 2/3.
- **✗ Échec de connexion** → URL de base/chemin erronés → revoyez l'étape 3.

## 5. Vérifier « en dur » avant l'application (facultatif)

Pour lever tout doute, testez le service directement (le même `GET` que fait « Tester la connexion ») :

```bash
curl -s "https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/sap/API_BUSINESS_PARTNER/A_BusinessPartner?\$top=1" \
  -H "APIKey: VOTRE_CLE_API" -H "Accept: application/json" | head -c 400
```

Un JSON (ou un `200`) confirme que **clé + URL** fonctionnent.

## 6. Et le **dépôt** (écriture) ?

Le bac à sable étant **en lecture seule**, un vrai *Transmettre la référence à SAP* (qui fait un
`POST`) renverra typiquement **405 / 403** : c'est **normal**, et cela **prouve quand même** que la
requête authentifiée **atteint SAP**. Pour valider une **écriture réussie** :

- **SAP de la Ville** (cible de production) — service OData **inscriptible** fourni par la Ville
  (voir nos questions préalables : module/version, protocole, objet cible) ; ou
- **SAP BTP Trial** — déployez un petit service **OData/CAP** qui accepte le `POST`, et pointez
  l'application dessus. Tutoriel pas-à-pas dédié : [`tutoriel-sap-btp-trial.md`](tutoriel-sap-btp-trial.md)
  (connexion **et** dépôt réel, indépendant de la panne du bac à sable).

> 🔧 Le **corps** envoyé par l'application est `{ NumeroAO, Objet, NombreDocuments, MontantTotal }`.
> Adaptez-le au **schéma exact** de l'entité inscriptible de votre service (noms des champs) — c'est le
> seul point qui dépend du service SAP retenu.

---

## Récapitulatif

| Étape | Résultat |
|-------|----------|
| Compte SAP + clé API (`api.sap.com`) | Clé pour l'en-tête `APIKey` |
| URL du sandbox de l'API choisie | `base-url` + `chemin` |
| Paramètres → Intégration SAP (auth = Clé API, Activer) | Config par tenant, secret chiffré |
| **Tester la connexion** | ✓ 200 = auth + URL valides |
| Dépôt réel (écriture) | Nécessite un service **inscriptible** (SAP Ville / BTP Trial) |

> Cadre général et modes d'intégration : [`tutoriel-sap.md`](tutoriel-sap.md).
> Détails techniques (adaptateur, chiffrement, simulateur) : [`integration-m365-sap.md`](integration-m365-sap.md).
