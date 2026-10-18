# Tutoriel — Tester et valider le dépôt vers SAP

> **Public visé** : équipe de développement/exploitation et décideur qui cadre l'intégration SAP.
> **En un mot** : aujourd'hui le dépôt SAP est **simulé** ; pour le valider **réellement**, il faut un
> connecteur branché sur **le SAP du client** (ou un environnement d'essai SAP), car SAP n'a pas d'API
> universelle « comme Microsoft Graph » — l'intégration dépend du paysage SAP en place.

---

## 1. État actuel dans l'application (à savoir avant tout)

- Le dépôt vers SAP est assuré par un **adaptateur de simulation** — `SimulationSapAdapter`
  (`ReferenceFinancierePort`). Il **n'appelle aucun SAP réel** : il enregistre la référence
  financière (n° AO, objet, nombre de documents, montant total) et renvoie une URL `sap://…`.
- Ces dépôts simulés sont **visualisables** dans **Pilotage → Dépôts simulés → SAP** (un fichier
  `.txt` lisible par dossier).
- **Aucun connecteur SAP réel n'est encore implémenté** (contrairement à Microsoft 365, qui dispose
  d'un vrai client Graph). Passer au réel = **ajouter un adaptateur** `ReferenceFinancierePort` avec
  une priorité supérieure (ADR-002, OCP/DIP) — voir §5. Le code appelant ne change pas.

> 🔎 Ce qui est transmis à SAP = des **métadonnées** de dossier (référence financière), pas le fichier
> lui-même. (La cible `SAP` du panneau de publication d'un document reste, elle aussi, simulée tant
> qu'aucun adaptateur réel n'est branché.)

---

## 2. Faut-il « s'abonner à SAP » ? (réponse courte : non)

- **En production**, la solution dépose la référence dans **le SAP que la Ville possède déjà**
  (Trois-Rivières / Lévis). VINA **ne s'abonne pas** à SAP : elle **se connecte** au SAP du client,
  avec les **accès et le point d'entrée** que la Ville fournit. C'est précisément l'objet de notre
  **question préalable** au client : *module et version SAP en place, protocole d'intégration
  privilégié, données à faire circuler et sens* (voir `trois-riviere/QUESTIONS-AVANT-DEPOT.md`).
- **Pour développer/tester** le mécanisme **sans** le SAP du client, on utilise un **environnement
  d'essai SAP gratuit** (§3). Aucune dépense n'est requise pour éprouver l'intégration.

> Autrement dit : un abonnement SAP payant n'est nécessaire que si **vous** vouliez héberger votre
> propre SAP — ce qui n'est pas le cas ici. La cible de production, c'est **le SAP du client**.

---

## 3. Obtenir un environnement de test SAP (gratuit)

Pour valider l'intégration sans attendre les accès au SAP de la Ville :

| Option | Ce que c'est | Idéal pour |
|--------|--------------|------------|
| **SAP Business Accelerator Hub — bac à sable** (<https://api.sap.com>) | API SAP standard (S/4HANA, etc.) avec un **environnement *sandbox*** et une clé API | Tester des appels **OData** sans rien installer |
| **SAP BTP Trial** (<https://www.sap.com/products/technology-platform/trial.html>) | Compte d'essai gratuit de la plateforme SAP BTP | OData, **Integration Suite (CPI)**, destinations |
| **SAP Gateway Demo System « ES5 »** | Système de démo OData public (inscription gratuite) | Prototyper des lectures/écritures OData |
| **SAP S/4HANA Cloud — essai** | Essai limité de S/4HANA Cloud | Tester des API métier standard |

> 💡 Le plus rapide pour un test purement **mécanique** : le **bac à sable de SAP Business
> Accelerator Hub** (clé API + URL de sandbox) — on y envoie une requête OData et on vérifie la
> réponse, sans infrastructure.

---

## 4. Choisir le mode d'intégration (dépend du paysage SAP)

Il n'existe pas **une** façon de « déposer dans SAP » : cela dépend de ce que la Ville expose.

| Mode | Quand | Techno côté application |
|------|-------|-------------------------|
| **OData / REST** (SAP Gateway, S/4HANA) | API modernes exposées (recommandé) | Client HTTP + jeton OAuth2 ou Basic |
| **RFC / BAPI** (ECC classique) | Backend SAP traditionnel | **SAP JCo** (`sapjco3`, bibliothèque native) |
| **IDoc / fichier** | Intégration par lots | Dépôt SFTP/partage surveillé par **SAP PI/PO** |
| **SAP Integration Suite (CPI)** | Intermédiaire d'intégration en place | On appelle un *flux* CPI (HTTPS) qui parle à SAP |

> ⚠️ **À confirmer avec la Ville avant d'implémenter** (question préalable) : *quel* système (S/4HANA
> Cloud/on-prem, ECC), *quel* protocole (OData/RFC/IDoc/CPI), *quel* service/objet cible (ex. une
> demande d'achat, un engagement, un poste budgétaire), et *quelles* données doivent transiter.

---

## 5. Brancher un connecteur SAP réel dans l'application

C'est **déjà implémenté** — l'adaptateur `SapODataAdapter` (`@Primary`) lit la configuration **propre
au tenant courant** et fait le `POST` OData ; sans configuration active, il **retombe sur la
simulation**. **La configuration se fait PAR TENANT dans l'espace admin** (chaque Ville a son propre
SAP), pas dans un fichier global.

### Configuration : Paramètres → « Intégration SAP » (par tenant)

Depuis **Paramètres** (rôle `ADMIN`), section **« Intégration SAP »** :

| Champ | Exemple | Rôle |
|-------|---------|------|
| **Activer** | ☑ | Bascule en dépôt **réel** (sinon simulation) |
| **URL de base du service OData** | `https://sap.ville.ca/sap/opu/odata/sap` | Racine du service |
| **Chemin du service** | `/ZREF_APPRO_SRV/References` | Entité/collection cible |
| **Authentification** | `Basic` ou `Clé API` | Mode d'auth |
| **Utilisateur / Mot de passe** | *(auth Basic)* | Compte technique SAP |
| **Clé API** | *(auth Clé API)* | En-tête `APIKey` (bac à sable SAP) |

- Endpoints : `GET/PUT /api/parametres/integration-sap` (tenant courant).
- **Secrets chiffrés au repos** (`SecretCipher`, comme le secret M365) et **jamais réaffichés** (le
  formulaire indique « •••••••• (conservé) »). Saisie *write-only* : laissez vide pour conserver.
- **Bascule par tenant** : `SapODataAdapter` n'appelle SAP réellement que si le tenant courant a une
  config **active et complète** ; sinon `SimulationSapAdapter`. **Par défaut : simulation.**

> Le corps envoyé est un JSON `{ NumeroAO, Objet, NombreDocuments, MontantTotal }`. **Adaptez-le au
> schéma exact du service OData cible** (noms de champs, entité) une fois celui-ci connu — c'est le
> seul point qui dépend du service SAP retenu.

> 🔒 Sécurité : mêmes principes que Microsoft 365 — **secrets chiffrés** (jamais versionnés/loggués),
> **moindre privilège** (un compte technique SAP limité à l'objet visé), TLS. Voir
> [`integration-m365-sap.md`](integration-m365-sap.md).

---

## 6. Tester et valider — de la maquette au réel

### 6.a — Niveau 0 : simulation (déjà disponible, aucun SAP)

Transmettez une référence depuis un **dossier d'approvisionnement** (bouton *Transmettre la référence
à SAP*) → l'enregistrement apparaît dans **Pilotage → Dépôts simulés → SAP**. Cela valide la **chaîne
applicative** (agrégation n° AO / objet / montant), pas la connexion SAP.

### 6.b — Niveau 1 : validation automatisée contre un **simulateur SAP** (en place ✅)

Comme pour Microsoft 365 (serveur `FakeMicrosoft365Server`), le **vrai** adaptateur `SapODataAdapter`
est éprouvé contre un **stub OData en mémoire** (`FakeSapServer`) : on vérifie l'URL appelée, les
en-têtes d'authentification (Basic / APIKey), le corps envoyé et le parsing de la réponse — **sans SAP
réel**. Tests : `SapODataAdapterTest`. C'est la garantie de la mécanique et de la non-régression.

> ✅ **Déjà implémenté** : `SapODataAdapter` (adaptateur OData réel, `@Primary` +
> `@ConditionalOnProperty app.sap.enabled`), `FakeSapServer` (simulateur) et `SapODataAdapterTest`.
> Tant que `app.sap.enabled=false`, l'application reste en **simulation** (aucun bean réel créé).

### 6.c — Niveau 2 : contre un **bac à sable SAP** (mécanique réelle, sans le SAP du client)

1. Récupérez l'**URL de sandbox** et la **clé API** sur SAP Business Accelerator Hub (§3) — tutoriel
   pas-à-pas dédié : [`tutoriel-sap-sandbox.md`](tutoriel-sap-sandbox.md).
2. Dans **Paramètres → Intégration SAP** : renseignez l'URL/chemin, choisissez **Clé API**, collez la
   clé, **cochez « Activer »**, enregistrez.
3. Déclenchez un dépôt (*Transmettre la référence à SAP* depuis un dossier) et **vérifiez la réponse**
   (2xx, identifiant retourné).

Exemple de test **manuel** d'un service OData (avant même de coder l'adaptateur) :

```bash
# Lecture de test (remplacez URL et clé du sandbox) :
curl -s "https://sandbox.api.sap.com/s4hanacloud/sap/opu/odata/.../ServiceCollection?$top=1" \
  -H "APIKey: VOTRE_CLE_SANDBOX" -H "Accept: application/json" | jq .
```

Un `200` avec du JSON confirme que vous savez **parler à un service OData SAP**.

### 6.d — Niveau 3 : contre le **SAP de la Ville** (validation finale)

Avec les accès fournis par la Ville (URL, compte technique, service/objet cible) :

1. **Test de connexion** : une requête OData `GET` de lecture (ou l'obtention d'un jeton OAuth2)
   réussit (`200`).
2. **Dépôt d'une référence** de test → `2xx`.
3. **Vérification côté SAP** : la référence apparaît bien dans SAP (transaction/rapport SAP, ou un
   `GET` OData sur l'objet créé). ✅ C'est **la seule preuve** d'un dépôt réellement validé.
4. Rejeu, gestion des erreurs (`4xx/5xx`, `429`), et **traçabilité** (chaque dépôt reste journalisé —
   `PublicationLog` + audit `REFERENCE_SAP`).

---

## 7. Checklist « dépôt SAP réel »

- [ ] Protocole confirmé avec la Ville (OData / RFC / IDoc / CPI) et **objet SAP cible** identifié
- [ ] Accès obtenus : **URL du service**, **compte technique** (moindre privilège), méthode d'auth
- [ ] (Dev) Testé contre un **bac à sable SAP** ou un **`FakeSapServer`** — mécanique OK
- [ ] Adaptateur `SapODataAdapter` (OData) en place — **config par tenant** (Paramètres → Intégration SAP)
- [ ] Secrets **chiffrés** en base (comme M365), jamais renvoyés/journalisés ; TLS
- [ ] Config saisie et **« Activer »** coché pour le tenant, contre le SAP de la Ville
- [ ] **Dépôt de bout en bout** vérifié **dans SAP** (pas seulement un `2xx`)
- [ ] Journalisation (`REFERENCE_SAP`) et gestion des erreurs/rejeu en place
- [ ] Repli sur la **simulation** conservé si le tenant n'a pas de config SAP active

---

## 8. En résumé

- **Non**, vous n'avez pas à « vous abonner » à SAP : la cible de production est le **SAP de la Ville**.
- **Aujourd'hui**, l'application **simule** SAP (visualisable dans *Dépôts simulés*).
- Pour **valider réellement** : confirmer le protocole avec la Ville (§4), implémenter l'adaptateur
  (§5), l'éprouver contre un **simulateur** puis un **bac à sable SAP** (§6b–c), et enfin **contre le
  SAP de la Ville** avec vérification **dans SAP** (§6d).
- Détails techniques transverses (ports/adaptateurs, chiffrement des secrets, simulateurs) :
  [`integration-m365-sap.md`](integration-m365-sap.md).
