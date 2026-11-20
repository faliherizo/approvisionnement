# Bacs à sable — tester ce qui dépend d'Investissement Québec

Quatre points restent ouverts après la validation des exigences
([validation_exigences_fonctionnelles.md](../02-Analyse/validation_exigences_fonctionnelles.md), § 4) : la recette
Salesforce, le fournisseur de signature certifié, le calendrier de conservation et l'annuaire d'entreprise. Aucun ne se
termine sans IQ, mais chacun peut être éprouvé dès maintenant. Ce guide dit comment, avec ce qui a été vérifié le
2026-11-08 et ce qui reste à faire avec IQ. Les questions à poser sont dans
[questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md).

| Point | Bac à sable local (livré) | Bac à sable externe (gratuit) | Vérifié le 2026-11-08 | Reste avec IQ |
|---|---|---|---|---|
| Salesforce Grantmaking | API REST imitée, `infra/bacs-a-sable/salesforce` | Developer Edition ; essai Public Sector Solutions | Connexion, recherche, préremplissage, retour de statut, panne et relance | Bac à sable Grantmaking d'IQ, objets et champs réels |
| Signature certifiée | ICP locale (cachet, RFC 3161) et Mailpit (SMTP) ; horodatage contre une autorité publique | Comptes développeur des fournisseurs | 2026-11-20 : invitation SMTP, signature au portail, attestation CAdES vérifiée par `openssl cms` hors plateforme | Certificat et autorité qualifiés ; relais SMTP du domaine d'IQ |
| Conservation | Horloge injectable (tests à date fixe) | — | — (rien à tester sans calendrier) | Calendrier de conservation d'IQ |
| Annuaire (SSO) | Keycloak, royaume `iq`, OIDC et SAML 2.0 | Locataire Microsoft Entra ID | Connexion OIDC et SAML, création automatique d'un compte | IdP d'IQ, correspondance groupes → rôles et domaines |
| Cachet, horodatage, révocation (Q4) | ICP OpenSSL : RFC 3161, OCSP, liste de révocation | — | 2026-11-20 : PAdES-B-LTA validé par pyHanko ; cachet révoqué refusé | Certificat d'une autorité reconnue et TSA qualifiée |
| PDF/A (Q14) et modèles Word (Q22) | Gotenberg (LibreOffice), veraPDF REST | — | 2026-11-20 : contrat issu du gabarit Word et certificats conformes PDF/A-2b (144 règles, 0 échec) | Gabarits Word réels d'IQ |

Lancer les bacs à sable locaux (aucune donnée ni aucun secret réels) :

```bash
docker compose -f infra/bacs-a-sable/docker-compose.yml up -d --build --wait
# Keycloak :8180, Salesforce imité :8099, ICP :18090, pyHanko :18093, conversion (Gotenberg) :18030, veraPDF :18092,
# courriel (Mailpit) : SMTP :11025, boîte et API :18025
docker compose -f infra/bacs-a-sable/docker-compose.yml down      # arrêt
```

## 1. Salesforce Grantmaking

### 1.1 Bac à sable local : l'API REST imitée

`infra/bacs-a-sable/salesforce/serveur.mjs` imite, au niveau HTTP, ce que la plateforme appelle chez Salesforce :
jeton OAuth 2.0 *client credentials*, lecture d'une `FundingRequest` par identifiant, recherche SOQL par numéro, `PATCH`
du statut et `limits`. Le vrai adaptateur `SalesforceRest` est donc exercé, contrairement au mode `simulation`, qui
court-circuite le HTTP. Les demandes fictives sont dans `donnees.json` : `DEM-2026-0101` et `DEM-2026-0102`, avec les
champs standards de Grantmaking et des champs personnalisés `…__c`.

Brancher la plateforme dessus (backend lancé hors Docker) :

```bash
export CLM_SALESFORCE_MODE=rest
export CLM_SALESFORCE_INSTANCE_URL=http://localhost:8099
export CLM_SALESFORCE_TOKEN_URL=http://localhost:8099/services/oauth2/token
export CLM_SALESFORCE_CLIENT_ID=clm-bac-a-sable
export CLM_SALESFORCE_CLIENT_SECRET=bac-a-sable-seulement     # secret fictif du bac à sable
export CLM_SALESFORCE_HOTES_AUTORISES=localhost               # sinon seuls salesforce.com et force.com sont admis
cd backend && mvn spring-boot:run
```

Dans un conteneur, `localhost` désigne le conteneur : utiliser `http://host.docker.internal:8099` et
`CLM_SALESFORCE_HOTES_AUTORISES=host.docker.internal`.

Parcours vérifié (compte `initiateur@iq.example`, puis `admin.ti@iq.example`) :

| Étape | Depuis l'interface | Contrôle dans le bac à sable |
|---|---|---|
| Connexion | Intégrations API → **Tester la connexion** : « Connexion établie (v60.0) » | — |
| Recherche | Créer une trousse → Origine **Salesforce** → `DEM-2026-0101` → **Rechercher** | Champs personnalisés présents (`Demandeur__c`, `Programme__c`) |
| Préremplissage | Modèle dont les champs ont pour source `Demandeur__c`, `RequestedAmount`, `Programme__c` | Valeurs de la trousse : « Coopérative Agri-Nord inc. », 250000, ESSOR |
| Retour de statut | **Lancer le parcours** | `curl localhost:8099/__bac-a-sable/journal` : `PATCH … {"CLM_Statut__c":"LANCEE", "CLM_Reference__c":…, "CLM_Correlation__c":…}` avec l'en-tête `X-Correlation-Id` |
| Panne et reprise | `curl -X POST localhost:8099/__bac-a-sable/panne -d '{"statut":503}'`, puis **Annuler la trousse** | Événement « en attente », 1 tentative, « Salesforce indisponible (HTTP 503) » |
| Relance | Lever la panne (`{"statut":0}`), puis Intégrations API → **Relancer l'événement** | `CLM_Statut__c` = `ANNULEE` |

Le bac à sable refuse un `PATCH` vers un champ inexistant (`INVALID_FIELD`), comme Salesforce : une correspondance de
champs erronée se voit avant la recette chez IQ.

**Lecture limitée (addenda 2, Q15).** La plateforme ne demande que `Id`, `Name`, les champs de la liste **Champs
Salesforce** et les sources des champs du modèle (`?fields=`). Le bac à sable refuse lui aussi un champ inconnu
(`INVALID_FIELD`) : avant l'essai, remplacer dans **Administration → Listes de référence → Champs Salesforce** les noms
de démonstration (`Demandeur`, `Montant`…) par ceux de `donnees.json` (`Demandeur__c`, `RequestedAmount`…).

**JWT Bearer (addenda 2, Q11).** Générer une paire de clés de test hors du dépôt, puis démarrer le bac à sable avec la
clé publique :

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out /tmp/cle-sf.pem
openssl pkey -in /tmp/cle-sf.pem -pubout -out /tmp/cle-sf.pub.pem
SF_JWT_CERTIFICAT=/tmp/cle-sf.pub.pem node infra/bacs-a-sable/salesforce/serveur.mjs
```

Dans Intégrations API → **Configurer la connexion** : mode REST, authentification **JWT Bearer**, identifiant client
`clm-bac-a-sable`, un utilisateur d'exécution quelconque, et le contenu de `/tmp/cle-sf.pem` comme clé privée. Le bac à
sable vérifie la signature RS256, l'émetteur et l'expiration ; une assertion falsifiée reçoit `invalid_grant`.

**Correctif issu de cet essai.** La recherche par numéro lisait l'enregistrement avec `SELECT FIELDS(STANDARD)`, qui
omet les champs personnalisés : un champ `…__c` d'IQ aurait été perdu au préremplissage. L'adaptateur recherche désormais
l'identifiant, puis relit l'enregistrement complet (`SalesforceRestTest`).

### 1.2 Bac à sable Salesforce réel, sans attendre IQ

1. **Developer Edition** (gratuite, permanente) : <https://developer.salesforce.com/signup>. Elle ne contient pas les
   objets de Grantmaking. Créer un objet personnalisé qui reprend les champs utiles (ou utiliser l'objet du bac à sable
   local comme modèle), puis indiquer son nom d'API (`…__c`) dans **Intégrations API → Configurer la connexion → Objet**.
2. **Public Sector Solutions** : Salesforce propose des organisations d'essai et des *Trailhead playgrounds* propres à
   cette offre, qui contiennent `FundingRequest`. La disponibilité et la durée de l'essai sont à confirmer auprès de
   Salesforce ; c'est le meilleur substitut au bac à sable d'IQ.
3. **Application connectée** (*External Client App* ou *Connected App*) : OAuth activé, portée `api`, **flux client
   credentials** activé avec un utilisateur d'exécution (*Run As*) dont le profil lit l'objet et écrit les trois champs
   de retour `CLM_Statut__c`, `CLM_Reference__c` et `CLM_Correlation__c` (sécurité au niveau des champs).
4. **Adresse des jetons** : le flux client credentials exige l'adresse **My Domain**
   (`https://<domaine>.my.salesforce.com/services/oauth2/token`), pas `login.salesforce.com`.
5. Secret client saisi dans l'écran UI-21 ou passé par variable d'environnement, **jamais dans un fichier du dépôt**.

### 1.3 Recette avec IQ

Sur le bac à sable Grantmaking d'IQ, rejouer le tableau du § 1.1 avec un dossier réel, puis une trousse signée
jusqu'au bout (statut `TERMINEE` et document final). Prérequis à obtenir : questions B1 à B11, en priorité B1, B2 et B10.

## 2. Signature à valeur probante

### 2.1 Ce que fait la plateforme aujourd'hui

Le port `FournisseurSignature` reçoit l'empreinte des documents, l'identité vérifiée et le consentement, et rend une
attestation (fournisseur, identifiant, horodatage, jeton). En production, `SignatureProbanteCachet` la signe en **CAdES**
par le cachet X.509 de l'organisation, horodatée RFC 3161 : elle se vérifie hors de la plateforme
([ADR-019](../../adr/ADR-019-attestation-cades-et-courriel-smtp.md)). Sa valeur probante dépend du certificat et de
l'autorité retenus par IQ. L'adaptateur `SignatureProbanteLocale` (HMAC) ne sert plus qu'aux postes de développement.
Un prestataire certifié s'ajouterait comme un adaptateur de plus, sans changer les cas d'utilisation.

### 2.2 Horodatage RFC 3161 : essai livré

`infra/bacs-a-sable/horodatage/tester-tsa.sh` calcule l'empreinte SHA-256 d'un document, demande un jeton à une autorité
d'horodatage (TSA) et le vérifie avec la chaîne de certification de l'autorité :

```bash
infra/bacs-a-sable/horodatage/tester-tsa.sh contrat.pdf                              # DigiCert (public)
infra/bacs-a-sable/horodatage/tester-tsa.sh contrat.pdf https://freetsa.org/tsr     # autre TSA publique
```

Vérifié : jeton « Granted », SHA-256, vérification OK ; le même jeton présenté avec un document modifié est rejeté
(« message imprint mismatch »). Ces autorités publiques servent aux essais seulement : l'autorité qualifiée sera celle
retenue par IQ ou fournie par le prestataire de signature.

### 2.3 ICP locale, conversion et conformité (addenda 4, Q4, Q14, Q22)

Quatre applications externes, que le serveur et le scénario de validation joignent **uniquement par le réseau**, aux
protocoles des services réels. Aucune n'est une imitation : ce sont les logiciels eux-mêmes.

| Service | Application | Protocole | Remplace en production |
|---|---|---|---|
| `icp` :18090 | Autorité OpenSSL : registre, numéros de série, `openssl ts`, `openssl ocsp`, `openssl ca -gencrl` | RFC 3161 (`POST /tsa`), RFC 6960 (`POST /ocsp`), RFC 5280 (`GET /crl/ac.crl`), `GET /ac.cer` | Autorité de certification reconnue et TSA qualifiée |
| `conversion` :18030 | Gotenberg 8.17.3 (LibreOffice) | `POST /forms/libreoffice/convert`, `pdfa=PDF/A-2b` | **La même image**, déployée comme service `conversion` |
| `verapdf` :18092 | veraPDF REST officiel | `POST /api/validate/2b` | Contrôle de conformité de la recette |
| `pyhanko` :18093 | pyHanko 0.25 (validation sans logique ajoutée) | `POST /valider?profil=pades-lta` | Validateur indépendant (Adobe, EU DSS) |
| `courriel` :11025 / :18025 | Mailpit | SMTP ; boîte consultable (`/api/v1`) | Relais SMTP de l'organisation (SPF, DKIM) |

Les certificats de l'ICP publient leurs points d'accès à `http://localhost:18090`, comme une autorité à son adresse
publique. Le magasin du cachet (PKCS#12) et son mot de passe, propres au bac à sable, sont créés au premier démarrage
dans `infra/bacs-a-sable/icp/etat` (ignoré par git). Serveur branché sur les bacs à sable, mêmes variables qu'en
production :

```bash
B=infra/bacs-a-sable
export CLM_CACHET_MAGASIN=$PWD/$B/icp/etat/cachet.p12 CLM_CACHET_MOT_DE_PASSE=$(cat $B/icp/etat/mot-de-passe.txt)
export CLM_CACHET_TSA_URL=http://localhost:18090/tsa CLM_CONVERSION_MODE=service CLM_CONVERSION_URL=http://localhost:18030
export CLM_SIGNATURE_FOURNISSEUR=cachet NOTIFICATION_COURRIEL_MODE=smtp MAIL_HOST=localhost MAIL_PORT=11025
(cd backend && SERVER_PORT=8100 mvn -q -DskipTests spring-boot:run)
infra/bacs-a-sable/valider.sh              # scénario complet, sortie non nulle au premier écart
infra/bacs-a-sable/valider-signature.sh    # signature réelle : SMTP, portail, attestation CAdES vérifiée par OpenSSL
infra/bacs-a-sable/valider-revocation.sh   # contre-épreuve : l'autorité révoque le cachet
```

Les **scénarios e2e** rattachés aux exigences fonctionnelles (BS-01 à BS-08 : PDF/A, gabarit Word, PAdES-B-LTA,
signature au portail par SMTP, export de la preuve, suspension du cachet, horodatage) sont décrits dans
[scenarios_e2e_bacs_a_sable.md](scenarios_e2e_bacs_a_sable.md) et implémentés dans
`e2e/views/clm.bacs-a-sable.spec.ts` (données : `e2e/data/bacs-a-sable.json`). L'ICP y offre aussi la suspension et
la levée de suspension du cachet (`POST /admin/cachet/suspendre`, `/retablir`), et le service de validation la
vérification CAdES (`POST /cms`) et RFC 3161 (`POST /horodatage`) par OpenSSL.

`valider.sh` passe par les API ordinaires de la plateforme :

1. Il dépose un gabarit Word (variable répartie sur plusieurs runs, tableau, en-tête, marqueur de clauses), publie le
   modèle, génère le contrat en Word et en PDF, puis télécharge deux certificats de signature scellés.
2. Il soumet ensuite les PDF à veraPDF, au profil PDF/A-2b, et les certificats à pyHanko, au profil PAdES-B-LTA avec
   révocation intégrée exigée. La seule ancre de confiance est la racine de l'ICP.

Résultat du 2026-11-20 :

- 3 PDF conformes PDF/A-2b, avec 144 règles passées et 0 en échec ;
- 2 certificats VALID au profil B-LTA : cachet de confiance, jeton de l'autorité d'horodatage, DSS (certificats,
  liste, OCSP) et horodatage de document.

Après révocation, pyHanko refuse le document scellé ensuite : « The signer's certificate is revoked », sur la foi de la
réponse OCSP que le serveur a lui-même intégrée.

Pour réinitialiser l'ICP après la contre-épreuve :

```bash
docker compose -f $B/docker-compose.yml rm -sf icp pyhanko && rm -rf $B/icp/etat
docker compose -f $B/docker-compose.yml up -d --wait icp pyhanko
```

Puis redémarrer le serveur, qui lit le nouveau magasin.

### 2.4 Comptes développeur des fournisseurs

| Fournisseur | Bac à sable | Intérêt pour IQ |
|---|---|---|
| Notarius — ConsignO Cloud | Environnement d'essai sur demande | Acteur québécois, signatures numériques certifiées pour les professionnels |
| OneSpan Sign | Compte *sandbox* développeur gratuit | Hébergement canadien offert |
| DocuSign eSignature | Compte développeur gratuit (environnement *demo*) | Très répandu ; hébergement au Canada à vérifier |
| Adobe Acrobat Sign | Compte développeur gratuit | Intégration Microsoft 365 |

Les conditions (gratuité, hébergement, certifications) sont à confirmer auprès de chaque fournisseur au moment du choix.

Démarche de test, une fois un compte ouvert :

1. Écrire l'adaptateur `FournisseurSignature` du fournisseur (envoi de l'empreinte ou du document, récupération de la
   preuve et du jeton d'horodatage).
2. Exécuter contre lui la même suite de tests de contrat que contre `SignatureProbanteLocale` : une attestation se
   vérifie, une attestation d'un autre document ne se vérifie pas.
3. Rejouer le test e2e `F-05, F-06, F-08, F-09` (`e2e/views/clm.exigences.spec.ts`) avec l'adaptateur actif : certificat
   et dossier de preuve doivent porter l'identifiant et le jeton du fournisseur.

## 3. Conservation et destruction

Il n'existe pas de bac à sable externe : la règle vient du calendrier de conservation d'IQ. Les organismes publics
québécois établissent ce calendrier selon la *Loi sur les archives*, et la *Loi concernant le cadre juridique des
technologies de l'information* encadre l'intégrité des documents pendant leur cycle de vie. L'assujettissement exact
d'IQ est à confirmer avec IQ.

Ce qui permet de tester sans attendre :

- **L'horloge est injectée** (`ContexteClm`, `Clock`) : une règle de conservation se teste en fixant la date, sans
  attendre des années. C'est déjà le cas des rappels et des échéances (`RappelServiceTest`).
- **Plan de test, dès réception du calendrier** : pour chaque type de contrat, une trousse terminée, conservée jusqu'à
  l'échéance, puis versée ou détruite selon le sort final ; un gel légal bloque la destruction ; la destruction laisse une
  trace au journal (qui, quand, quelle règle) sans conserver le contenu détruit ; le dossier de preuve exporté reste
  vérifiable après versement.

La réalisation (durées par type de contrat, gel légal, sort final, bordereau de destruction) attend les réponses E3 à E6.

## 4. Annuaire d'entreprise et authentification unique

### 4.1 Bac à sable local : Keycloak

Le royaume `iq` (`infra/bacs-a-sable/keycloak/realm-iq.json`) contient un compte par profil (mêmes courriels que les
comptes de démonstration, mot de passe `Passw0rd!`), un compte `nouvel.employe@iq.example` absent de la plateforme,
des groupes « domaines-affaires » (une unité par compte) et des rôles `clm-*` proposés. Deux clients : `clm-iq` (OIDC,
PKCE) et `clm-iq-saml` (SAML 2.0).

Configurer l'organisation IQ (compte `admin.ti@iq.example`, **Authentification unique (SSO)**) :

| Protocole | Réglage |
|---|---|
| OIDC | Émetteur `http://localhost:8180/realms/iq` · client `clm-iq` · secret `bac-a-sable-oidc-seulement` (fictif) · revendication courriel `email` · création automatique, rôle par défaut `LECTEUR` |
| SAML 2.0 | IdP `http://localhost:8180/realms/iq` · SSO `http://localhost:8180/realms/iq/protocol/saml` · certificat : `<ds:X509Certificate>` de `http://localhost:8180/realms/iq/protocol/saml/descriptor` · SP `clm-iq-saml` · attribut courriel `email` |

Démarrer la connexion : `/api/auth/oidc/login?slug=iq` ou `/api/auth/saml/login?slug=iq` (bouton de la page de connexion
du sous-domaine `iq`). Console Keycloak : <http://localhost:8180/admin> (`admin` / `admin-bac-a-sable`).

Vérifié (backend lancé hors Docker sur le port 8092) :

| Essai | Résultat |
|---|---|
| OIDC, `initiateur@iq.example` | Session ouverte, rôles du compte existant conservés (`REDACTEUR`) |
| OIDC, `nouvel.employe@iq.example` | Compte créé automatiquement avec le rôle par défaut (`LECTEUR`) |
| SAML, mêmes comptes | Mêmes résultats, après correctif |

**Correctif issu de cet essai.** Le retour SAML était refusé (403) par la politique CORS : l'IdP poste l'assertion depuis
sa propre origine (`Origin: null` chez Keycloak, `login.microsoftonline.com` chez Entra ID). Le retour SAML est
maintenant exclu du contrôle CORS, et lui seul : l'assertion signée fait foi (`CorsRetourSamlTest`).

**Écart constaté.** Les groupes et rôles transmis par l'annuaire (`groups`, `roles`) ne sont pas repris : un compte
créé par SSO reçoit le rôle par défaut, et son unité n'est pas renseignée. La correspondance groupes → rôles → unités
(domaines d'affaires) sera réalisée selon les réponses D3 à D6.

Avec l'application dans Docker, l'émetteur doit être le même pour le navigateur et pour le conteneur. Lancer Keycloak
avec `KC_HOSTNAME=http://keycloak:8180`, ajouter `127.0.0.1 keycloak` au fichier `hosts` du poste, et relier le
conteneur backend au réseau des bacs à sable.

### 4.2 Bac à sable proche de la cible : Microsoft Entra ID

Si IQ utilise Entra ID (à confirmer, D1), un locataire Entra de test reproduit la cible : inscription d'application
(*App registration*) avec l'adresse de retour `/api/auth/oidc/callback`, ou application d'entreprise SAML avec l'ACS
`/api/auth/saml/callback` ; groupes de sécurité pour les domaines d'affaires ; revendication `groups` dans le jeton. Le
secret client se saisit dans l'écran SSO ou passe par variable d'environnement, **jamais dans un fichier du dépôt**.

## 4bis. Provisionnement SCIM 2.0 (addenda 3, Q51)

Aucun bac à sable externe n'est nécessaire : le point `/scim/v2` de la plateforme se teste avec `curl` ou avec l'outil
de provisionnement d'une organisation Entra ID de développement (*Enterprise application* → *Provisioning* → mode
automatique, URL du locataire et jeton secret). Procédure, opérations prises en charge et cycle de vie des comptes :
[guide_annuaire_scim.md](../04-Architecture/guide_annuaire_scim.md). Pour Saviynt, une instance d'essai de l'éditeur ou
celle d'IQ est requise (question D2).

## 5. Fichiers

| Fichier | Rôle |
|---|---|
| `infra/bacs-a-sable/docker-compose.yml` | Keycloak, Salesforce imité, ICP, pyHanko, conversion (Gotenberg), veraPDF |
| `infra/bacs-a-sable/icp/` | Configuration OpenSSL, initialisation, points de publication HTTP, révocation |
| `infra/bacs-a-sable/validation/serveur.py` | Service pyHanko |
| `infra/bacs-a-sable/outils/Dockerfile` | Image de l'ICP et de pyHanko |
| `infra/bacs-a-sable/donnees/fabriquer_gabarit.py` | Gabarit Word de démonstration |
| `infra/bacs-a-sable/valider.sh`, `valider-signature.sh`, `valider-revocation.sh` | Scénarios de validation et contre-épreuve |
| `infra/bacs-a-sable/salesforce/serveur.mjs`, `donnees.json` | API REST Salesforce imitée et demandes fictives |
| `infra/bacs-a-sable/keycloak/realm-iq.json` | Royaume `iq` : comptes, groupes, clients OIDC et SAML |
| `infra/bacs-a-sable/horodatage/tester-tsa.sh` | Essai d'horodatage RFC 3161 |
