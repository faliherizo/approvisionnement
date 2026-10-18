# Tutoriel — Configurer Microsoft 365 pour l'intégration

> **Public visé** : l'administrateur du tenant qui remplit l'écran **Paramètres → Intégration
> Microsoft 365** (`/parametres`).
> **Objectif** : obtenir, une par une, les valeurs demandées par le formulaire pour que la solution
> dépose réellement les documents dans **votre** SharePoint / OneDrive / Teams et envoie par Outlook.

Chaque section ci-dessous correspond à **un champ** du formulaire. À la fin, un tableau récapitule
quelles valeurs sont nécessaires selon la ou les cibles que vous utilisez.

> ℹ️ **Vous n'êtes pas obligé de tout remplir.** Ne renseignez que ce dont vous avez besoin :
> - Dépôt **SharePoint** → *ID de site SharePoint*
> - Dépôt **OneDrive** → *ID de drive OneDrive*
> - Dépôt **Teams** → *ID d'équipe Teams*
> - Envoi **Outlook** → *Expéditeur Outlook*
>
> Les trois premiers champs (locataire, application, secret) sont **toujours requis**.

---

## Pré-requis

- Un **compte administrateur** Microsoft Entra ID (Azure AD) de votre organisation (rôle
  *Administrateur d'application* au minimum, et un **Administrateur général** pour le *consentement
  administrateur* à l'étape 4).
- Accès au **centre d'administration Microsoft Entra** : <https://entra.microsoft.com>
  (ou Azure : <https://portal.azure.com> → *Microsoft Entra ID*).
- Facultatif mais pratique pour récupérer les identifiants : **Graph Explorer**
  <https://developer.microsoft.com/graph/graph-explorer> (connectez-vous avec votre compte).

> 🔑 **Deux « identités » à ne pas confondre** (important pour la suite) :
>
> 1. **L'application** (l'*inscription d'application* de l'étape 1) — identifiée par l'**ID
>    d'application** + le **secret**. C'est **elle** qui s'authentifie (flux *client credentials*,
>    sans utilisateur). Avec des **permissions applicatives** (ex. `Sites.ReadWrite.All`), son accès
>    est déterminé **par les permissions Graph**, **pas** par une appartenance à un site ou une
>    équipe. → **Rien à « ajouter comme membre ».**
> 2. **Un compte de service** = un **compte utilisateur dédié** que vous créez pour l'intégration
>    (ex. `depot@contoso.com`), avec **sa propre boîte aux lettres / son OneDrive**. Il n'est utile
>    que pour : le **OneDrive personnel** (étape 5, cas A) et l'**expéditeur Outlook** (étape 7). Il
>    est **facultatif** si vous déposez dans un **site SharePoint** ou dans **Teams**.
>
> Autrement dit : pour SharePoint et Teams, **l'application suffit** ; le « compte de service » ne
> concerne que OneDrive personnel et l'envoi Outlook.

---

## Étape 1 — Enregistrer une application (donne le *locataire* et l'*application*)

1. Entra → **Applications** → **Inscriptions d'applications** → **Nouvelle inscription**.
2. **Nom** : `Rédaction contractuelle – dépôts M365` (libre).
3. **Types de comptes pris en charge** : *Comptes dans cet annuaire d'organisation uniquement*.
4. **Ne remplissez pas** l'URI de redirection (l'app agit en tant que service, sans utilisateur).
5. **Enregistrer**.

Sur la page **Vue d'ensemble** de l'application, relevez :

| Champ du formulaire | Où le trouver | Format |
|---|---|---|
| **ID de locataire (Directory / tenant)** | « **ID d'annuaire (locataire)** » | GUID `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| **ID d'application (client)** | « **ID d'application (client)** » | GUID `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |

> ✅ Ce sont des **identifiants**, pas des secrets : ils ne sont pas sensibles.

---

## Étape 2 — Créer le *secret client*

1. Toujours dans l'application → **Certificats et secrets** → onglet **Secrets client** →
   **Nouveau secret client**.
2. **Description** : `contrat-depots` ; **Expiration** : 12 ou 24 mois (notez la date de
   renouvellement dans votre agenda).
3. **Ajouter**, puis **copiez immédiatement la valeur** dans la colonne **Valeur** (pas l'*ID
   secret*). ⚠️ **Elle ne sera plus jamais réaffichée.**

| Champ du formulaire | Valeur à coller |
|---|---|
| **Secret client** | la **Valeur** du secret (chaîne du type `abc8Q~...`) |

> 🔒 Dans notre application, ce secret est **chiffré au repos** et n'est **jamais réaffiché**
> (le formulaire montre « •••••••• (conservé) » une fois enregistré). Pour le changer, saisissez-en
> un nouveau ; pour le conserver, laissez le champ vide.

---

## Étape 3 — Accorder les permissions Microsoft Graph

1. Application → **API autorisées** → **Ajouter une autorisation** → **Microsoft Graph** →
   **Autorisations d'application** (⚠️ *application*, **pas** *déléguées* — l'app agit sans
   utilisateur connecté).
2. Cochez, **selon les cibles que vous utilisez** :

| Cible | Autorisation d'application Microsoft Graph |
|---|---|
| SharePoint | `Sites.ReadWrite.All` (ou, au moindre privilège, `Sites.Selected` + octroi par site) |
| OneDrive | `Files.ReadWrite.All` |
| Teams | `Files.ReadWrite.All` (dépôt dans la bibliothèque de l'équipe) + `Group.Read.All` |
| Outlook | `Mail.Send` |

3. **Ajouter les autorisations**, puis **« Accorder le consentement administrateur pour
   <organisation> »** (bouton nécessitant un **Administrateur général**). Les lignes doivent passer
   au vert « Accordé ».

> 💡 **Moindre privilège (recommandé)** : préférez `Sites.Selected` et n'autorisez l'application que
> sur le(s) site(s) concerné(s), plutôt que `Sites.ReadWrite.All` sur tout le locataire.

---

## Étape 4 — *ID de site SharePoint* (variable `M365_SHAREPOINT_SITE_ID`)

Le dépôt SharePoint se fait dans la bibliothèque de documents d'un **site**. Il faut l'**ID Graph**
de ce site, au format composite **`hôte,GUID-collection,GUID-web`** (ce n'est pas le simple GUID).

### 4.a — Créer le site (si vous n'en avez pas encore)

1. Ouvrez le **centre d'administration SharePoint** : <https://admin.microsoft.com> → *Afficher tout*
   → **SharePoint**, ou directement `https://<votre-tenant>-admin.sharepoint.com`.
2. **Sites** → **Sites actifs** → **Créer** → **Site d'équipe** (recommandé) ou **Site de
   communication**.
3. **Nom du site** : `Contrats` → l'adresse devient `https://contoso.sharepoint.com/sites/Contrats`.
4. **Propriétaire du site** : indiquez un **administrateur humain** (celui qui gérera le site). ⚠️ **Ne
   cherchez pas à « ajouter l'application comme membre »** : avec la permission applicative
   `Sites.ReadWrite.All` (étape 3), l'application accède **déjà** à tous les sites — l'appartenance au
   site n'intervient pas. **Terminer.**
5. Une bibliothèque **« Documents »** est créée d'office : c'est là que les fichiers seront déposés.

> Vous pouvez aussi réutiliser un site existant : notez simplement son adresse
> (`https://contoso.sharepoint.com/sites/<NomDuSite>`).

> 🔒 **Variante « moindre privilège » (`Sites.Selected`)** — si, à l'étape 3, vous avez choisi
> `Sites.Selected` plutôt que `Sites.ReadWrite.All`, l'application n'a accès à **aucun** site tant que
> vous ne l'y autorisez pas **explicitement**. Cet octroi se fait **via Graph** (et non par le bouton
> « Ajouter un membre » du site), avec un compte administrateur — en désignant l'application par son
> **ID d'application** (étape 1) :
>
> ```
> POST https://graph.microsoft.com/v1.0/sites/{ID_de_site}/permissions
> Content-Type: application/json
>
> {
>   "roles": ["write"],
>   "grantedToIdentities": [
>     { "application": { "id": "<ID_APPLICATION>", "displayName": "Rédaction contractuelle" } }
>   ]
> }
> ```
>
> `{ID_de_site}` = l'ID composite de l'étape 4.b. C'est **l'application** qu'on autorise ici, pas un
> compte utilisateur.

### 4.b — Récupérer l'ID du site

**Méthode 1 — Graph Explorer (recommandée)**

1. Ouvrez <https://developer.microsoft.com/graph/graph-explorer> et **connectez-vous** avec un compte
   du tenant (bouton *Sign in*).
2. Dans la barre de requête, méthode **GET**, collez (en adaptant l'hôte et le nom du site) :

   ```
   GET https://graph.microsoft.com/v1.0/sites/contoso.sharepoint.com:/sites/Contrats
   ```

   - **`contoso.sharepoint.com`** = votre hôte SharePoint (le début de l'adresse de n'importe quel
     site) ;
   - **`Contrats`** = le segment après `/sites/` dans l'URL du site.
3. **Run query**. Dans la réponse JSON, copiez le champ **`id`** :

   ```json
   {
     "id": "contoso.sharepoint.com,9b1e0c2a-...-c2,7f3ad1e4-...-d1",
     "name": "Contrats",
     "webUrl": "https://contoso.sharepoint.com/sites/Contrats"
   }
   ```

> 🔎 Pour le **site racine** du tenant : `GET /sites/contoso.sharepoint.com`.
> Pour trouver un site par mot-clé : `GET /sites?search=Contrats` (nécessite `Sites.Read.All`).

**Méthode 2 — depuis le navigateur (rapide, mais donne seulement le GUID)**

Ouvrez `https://contoso.sharepoint.com/sites/Contrats/_api/site/id` : la page affiche le **GUID de la
collection de sites**. ⚠️ Ce GUID **seul ne suffit pas** à notre application, qui attend l'**ID
composite** de la Méthode 1. Utilisez-le uniquement pour vérifier/rapprocher.

**Méthode 3 — PowerShell (PnP)**

```powershell
Install-Module PnP.PowerShell -Scope CurrentUser
Connect-PnPOnline -Url "https://contoso.sharepoint.com/sites/Contrats" -Interactive
# L'ID Graph composite = "<hôte>,<SiteId>,<WebId>"
$s = Get-PnPSite ; $w = Get-PnPWeb
"contoso.sharepoint.com,$($s.Id),$($w.Id)"
```

| Champ du formulaire (`M365_SHAREPOINT_SITE_ID`) | Valeur |
|---|---|
| **ID de site SharePoint** | l'`id` **composite** (ex. `contoso.sharepoint.com,9b1e...,7f3a...`) |

---

## Étape 5 — *ID de drive OneDrive* (variable `M365_ONEDRIVE_DRIVE_ID`)

Dans Microsoft Graph, un **`drive`** = une **bibliothèque de documents** : soit le **OneDrive** d'un
utilisateur, soit une **bibliothèque d'un site** SharePoint. Choisissez selon votre besoin :

### 5.a — Cas A : OneDrive d'un compte de service

1. **Créer/choisir le compte** : dans <https://admin.microsoft.com> → **Utilisateurs** → *Utilisateur
   actif*, créez (ou réutilisez) un compte, ex. `depot@contoso.com` (il faut une **licence** incluant
   OneDrive, ex. Microsoft 365 Business/E3).
2. **Initialiser son OneDrive** : connectez-vous une fois à <https://www.office.com> avec ce compte →
   **OneDrive** (le drive est provisionné au premier accès).
3. **Récupérer l'ID** (Graph Explorer) :

   ```
   GET https://graph.microsoft.com/v1.0/users/depot@contoso.com/drive
   ```

### 5.b — Cas B : bibliothèque d'un site SharePoint (recommandé pour un dépôt partagé)

1. Sur le site (étape 4), la bibliothèque **« Documents »** existe déjà. Pour en créer une autre :
   sur le site → **+ Nouveau** → **Bibliothèque de documents** → nommez-la (ex. `Contrats`).
2. **Récupérer l'ID** — bibliothèque **par défaut** du site :

   ```
   GET https://graph.microsoft.com/v1.0/sites/{ID_de_site}/drive
   ```

   …ou **lister toutes** les bibliothèques du site et choisir la bonne par son `name` :

   ```
   GET https://graph.microsoft.com/v1.0/sites/{ID_de_site}/drives
   ```

   (`{ID_de_site}` = l'ID composite de l'étape 4.)

### 5.c — Copier l'ID

Dans la réponse JSON, copiez le champ **`id`** du drive (chaîne longue, commençant souvent par
`b!…`) :

```json
{ "id": "b!9bE0c2a...long...Xy", "name": "Documents", "driveType": "documentLibrary",
  "webUrl": "https://contoso.sharepoint.com/sites/Contrats/Documents" }
```

| Champ du formulaire (`M365_ONEDRIVE_DRIVE_ID`) | Valeur |
|---|---|
| **ID de drive OneDrive** | le champ **`id`** du drive (ex. `b!9bE...long...Xy`) |

> 💡 Pour un **dépôt partagé par l'équipe**, préférez le **Cas B** (bibliothèque de site) : pas de
> licence individuelle à gérer, et les documents restent accessibles même si un employé part.

> ⚠️ **`accessDenied` sur `/sites/{id}/drive` (ou `/drive`) dans Graph Explorer ?** Graph Explorer
> utilise un jeton **délégué** (vos droits d'utilisateur), **pas** le jeton **applicatif** de l'app.
> Les permissions **d'application** que vous avez accordées (`Files.ReadWrite.All`…) **ne s'appliquent
> pas** à Graph Explorer. Deux solutions :
> - **Lire l'id malgré tout (mode délégué)** : dans **Graph Explorer**, sous la barre de requête,
>   ouvrez l'onglet **« Modify permissions »** (ou *Modifier les autorisations*) → repérez
>   **`Sites.Read.All`** (et au besoin **`Files.Read.All`**) → cliquez **« Consent »** (*Consentir*) et
>   acceptez l'invite Microsoft → **ré-exécutez** la requête. Comme vous êtes **propriétaire du site**,
>   elle renvoie alors le **drive** avec son **`id`**.
> - **Tester le vrai accès de l'app** (recommandé) : obtenez un jeton **app-only** (client
>   credentials) et rappelez l'endpoint — voir §9.a. Si cet appel app-only réussit, l'application est
>   correctement configurée. S'il échoue encore, ajoutez la permission d'application
>   **`Sites.ReadWrite.All`** (étape 3) et **ré-accordez le consentement administrateur**.
>
> 🔎 **Rappel** : pour un dépôt **SharePoint**, seul l'**ID de site** est requis (l'app dépose via
> `/sites/{siteId}/drive/root:…`). Le **drive id** ne sert que pour la cible **OneDrive**.

---

## Étape 6 — *ID d'équipe Teams* et *ID de canal Teams*

Une équipe Teams est adossée à un **groupe Microsoft 365** ; son **ID d'équipe = ID du groupe**.

**Depuis l'application Teams** (le plus rapide) :

1. À côté du **nom de l'équipe** → **⋯** → **Obtenir le lien vers l'équipe**. L'URL contient
   `groupId=` :
   `https://teams.microsoft.com/l/team/.../conversations?groupId=`**`<ID_ÉQUIPE>`**`&tenantId=...`
2. À côté du **nom du canal** → **⋯** → **Obtenir le lien vers le canal**. L'URL contient l'**ID de
   canal** (`19:...@thread.tacv2`) :
   `https://teams.microsoft.com/l/channel/`**`19:xxxx@thread.tacv2`**`/Général?groupId=...`

**Ou via Graph Explorer** :

```
GET https://graph.microsoft.com/v1.0/me/joinedTeams          # → id de chaque équipe
GET https://graph.microsoft.com/v1.0/teams/{ID_ÉQUIPE}/channels   # → id de chaque canal
```

| Champ du formulaire | Valeur |
|---|---|
| **ID d'équipe Teams** | l'**ID du groupe/équipe** (GUID) |
| **ID de canal Teams** | l'ID du canal `19:...@thread.tacv2` — **recommandé** (voir note) |

> ✅ **Renseignez l'ID de canal** pour que le fichier apparaisse bien dans l'onglet **Fichiers du
> canal** Teams : le dépôt cible alors le **dossier du canal** (résolu via `filesFolder`). **Sans ID de
> canal**, le fichier est déposé à la **racine de la bibliothèque de l'équipe** — présent dans le
> SharePoint de l'équipe, mais **invisible dans l'onglet Fichiers du canal**. C'est la cause la plus
> fréquente de « le dépôt Teams a réussi mais je ne vois rien dans Teams ».

> 📍 **« Pourquoi mes dépôts Teams/OneDrive affichent une URL `…sharepoint.com` ? »** Ce n'est pas une
> erreur : **presque tout, dans Microsoft 365, est stocké dans SharePoint.**
> - **Teams** : chaque équipe a un **site SharePoint dédié** ; les fichiers d'un canal y sont rangés.
>   Une URL `…/sites/<Équipe>/…` est donc **normale** pour un dépôt Teams (ouvrez Teams → équipe →
>   onglet *Fichiers* pour le voir).
> - **OneDrive** : si vous avez saisi le drive d'une **bibliothèque de site** (Cas B), l'URL est de la
>   forme `…/sites/<Site>/…` — comme SharePoint. Seul un **OneDrive personnel** (Cas A,
>   `/users/{upn}/drive`) produit une URL `…-my.sharepoint.com/personal/…`.
> - **Outlook** : ce n'est pas un dépôt mais un **envoi de courriel** (`mailto:`).
>
> Pour des destinations **réellement distinctes**, pointez SharePoint et OneDrive vers des
> sites/bibliothèques différents, et/ou utilisez un OneDrive personnel (Cas A) pour la cible OneDrive.

---

## Étape 7 — *Expéditeur Outlook (courriel)* (variable `M365_OUTLOOK_SENDER`)

C'est l'**adresse de la boîte aux lettres** depuis laquelle l'application enverra les documents en
pièce jointe (via `POST /users/{expéditeur}/sendMail`). Il n'y a **pas d'ID** à récupérer : la valeur
est **l'adresse de courriel elle-même**. Il faut simplement qu'une **boîte aux lettres** existe pour
cette adresse et que l'autorisation **`Mail.Send`** (étape 3) soit accordée.

### 7.a — Option 1 : boîte aux lettres partagée dédiée (recommandé)

Une **boîte partagée** est **gratuite** (aucune licence requise) et idéale pour un envoi
« générique » (ex. `contrats@contoso.com`) :

1. Ouvrez le **centre d'administration Exchange** : <https://admin.exchange.microsoft.com>.
2. **Boîtes aux lettres partagées** → **+ Ajouter une boîte aux lettres partagée**.
3. **Nom** : `Contrats` · **Adresse** : `contrats@contoso.com` → **Créer**.
4. La valeur à saisir est cette adresse : `contrats@contoso.com`.

### 7.b — Option 2 : boîte d'un utilisateur existant

Utilisez simplement l'**adresse UPN** d'un compte disposant d'une boîte Exchange (ex.
`jean.tremblay@contoso.com`). Aucune étape supplémentaire côté Exchange.

### 7.c — Vérifier que la boîte existe (Graph Explorer)

```
GET https://graph.microsoft.com/v1.0/users/contrats@contoso.com?$select=mail,userPrincipalName
```

Une réponse `200` avec l'adresse confirme que la boîte est joignable.

| Champ du formulaire (`M365_OUTLOOK_SENDER`) | Valeur |
|---|---|
| **Expéditeur Outlook (courriel)** | l'adresse de la boîte d'envoi (ex. `contrats@contoso.com`) |

> 🔒 **Moindre privilège (fortement recommandé)** : l'autorisation d'application `Mail.Send` permet, par
> défaut, d'envoyer depuis **n'importe quelle** boîte du tenant. Pour **restreindre** l'application à
> **cette seule** boîte, créez un **groupe de sécurité à extension messagerie** contenant l'expéditeur,
> puis appliquez une *Application Access Policy* dans Exchange Online (PowerShell) :
>
> ```powershell
> Connect-ExchangeOnline
> New-ApplicationAccessPolicy -AppId <ID_APPLICATION> `
>   -PolicyScopeGroupId grp-envoi-contrats@contoso.com `
>   -AccessRight RestrictAccess -Description "App rédaction : envoi limité"
> # Vérifier :
> Test-ApplicationAccessPolicy -Identity contrats@contoso.com -AppId <ID_APPLICATION>
> ```

---

## Étape 8 — *Répertoire racine par défaut*

Ce n'est **pas** une donnée Microsoft : c'est simplement le **dossier** dans lequel déposer par
défaut (ex. `Contrats` ou `Contrats/2027`). Il est **créé automatiquement** au premier dépôt s'il
n'existe pas. Laissé vide, la valeur configurée côté serveur est utilisée (`Contrats`).

| Champ du formulaire | Valeur |
|---|---|
| **Répertoire racine par défaut** | un nom de dossier, ex. `Contrats` |

---

## Étape 9 — Enregistrer et tester

1. Dans **Paramètres → Intégration Microsoft 365**, cochez **« Activer les dépôts réels via
   Microsoft 365 »**, collez les valeurs, puis **Enregistrer**.
2. Cliquez **« Tester la connexion »** : l'application obtient un **jeton** avec vos identifiants
   (locataire + application + secret). Un ✓ vert confirme que l'authentification fonctionne.
3. Publiez un document (une fois validé) vers SharePoint/OneDrive/Teams : l'URL de dépôt réel
   apparaît dans l'historique.

> Tant que la case **Activer** est décochée (ou la configuration incomplète), les dépôts restent
> **simulés** — visibles dans **Pilotage → Dépôts simulés** — sans rien envoyer chez Microsoft.

### 9.a — Test *app-only* (facultatif, pour diagnostiquer)

Pour reproduire **exactement** ce que fait l'application (jeton d'application, sans utilisateur), à
lancer **en local** (pas dans Graph Explorer, qui est délégué) :

```bash
TENANT='<ID_LOCATAIRE>'
CLIENT='<ID_APPLICATION>'
SECRET='<SECRET_CLIENT>'
SITE='<ID_DE_SITE>'   # ex. contoso.sharepoint.com,GUID,GUID

# 1) Jeton app-only (client credentials)
TOKEN=$(curl -s -X POST "https://login.microsoftonline.com/$TENANT/oauth2/v2.0/token" \
  -d "client_id=$CLIENT" -d "scope=https://graph.microsoft.com/.default" \
  -d "client_secret=$SECRET" -d "grant_type=client_credentials" | jq -r .access_token)

# 2) Accès au drive du site en tant qu'application (renvoie l'id du drive)
curl -s "https://graph.microsoft.com/v1.0/sites/$SITE/drive" \
  -H "Authorization: Bearer $TOKEN" | jq '{id, name, webUrl}'
```

- **Un `id` renvoyé** → l'application est correctement configurée (et vous avez le drive id).
- **`accessDenied` en app-only** → ajoutez la permission d'application **`Sites.ReadWrite.All`**
  (étape 3), **ré-accordez le consentement administrateur**, patientez ~1 min, relancez.
- **Jeton refusé (étape 1)** → secret expiré/incorrect, ou consentement admin non accordé.

---

## Récapitulatif — quelles valeurs selon la cible

| Champ | SharePoint | OneDrive | Teams | Outlook |
|---|:--:|:--:|:--:|:--:|
| ID de locataire | ✅ | ✅ | ✅ | ✅ |
| ID d'application (client) | ✅ | ✅ | ✅ | ✅ |
| Secret client | ✅ | ✅ | ✅ | ✅ |
| ID de site SharePoint | ✅ | — | — | — |
| ID de drive OneDrive | — | ✅ | — | — |
| ID d'équipe Teams | — | — | ✅ | — |
| ID de canal Teams | — | — | facultatif | — |
| Expéditeur Outlook | — | — | — | ✅ |
| Répertoire racine par défaut | facultatif | facultatif | facultatif | — |

## Permissions Microsoft Graph — récapitulatif

| Autorisation (application) | Requise pour |
|---|---|
| `Sites.ReadWrite.All` *(ou `Sites.Selected`)* | SharePoint |
| `Files.ReadWrite.All` | OneDrive, Teams |
| `Group.Read.All` | Teams (résolution de l'équipe) |
| `Mail.Send` | Outlook |

> Après toute modification des autorisations : **ré-accorder le consentement administrateur**.

---

## Dépannage

| Symptôme | Cause probable | Correctif |
|---|---|---|
| `accessDenied` sur `/sites/{id}/drive` **dans Graph Explorer** | Graph Explorer utilise un jeton **délégué** ; les permissions **d'application** ne s'y appliquent pas | Consentir `Sites.Read.All` **délégué** (onglet *Modify permissions*), **ou** tester en **app-only** (§9.a) |
| « Test de connexion » échoue | Secret expiré/incorrect, ou consentement admin non accordé | Recréer le secret (étape 2) ; accorder le consentement (étape 3) |
| Dépôt refusé `403` | Autorisation manquante ou `Sites.Selected` sans octroi sur le site | Vérifier les autorisations (étape 3) et l'octroi par site |
| `404` sur SharePoint/Teams | `ID de site` / `ID d'équipe` erroné | Re-vérifier l'`id` via Graph (étapes 4/6) |
| Le fichier n'apparaît pas | Publication en mode **simulé** (case Activer décochée) | Cocher « Activer », re-tester |

> 📎 Détails techniques de l'intégration (adaptateurs, chiffrement du secret, simulateur) :
> voir [`integration-m365-sap.md`](integration-m365-sap.md).
