# Intégration Microsoft 365 (SharePoint, OneDrive, Outlook, Teams) et SAP

> **Public visé** : équipe de développement et d'exploitation qui branche les dépôts réels.
> **Résumé** : aujourd'hui le dépôt est **déclenché manuellement** depuis l'éditeur et **simulé**
> par défaut (aucun appel externe). L'architecture (ports & adaptateurs, ADR-002) permet de passer
> au réel en **ajoutant un adaptateur**, sans modifier le code appelant. Ce document décrit comment
> configurer chaque cible.

---

## 1. Comment ça fonctionne aujourd'hui

### 1.1 Le dépôt n'est **pas** automatique pendant l'édition

Le dépôt vers une cible externe est une **action explicite** de l'utilisateur, pas un effet de bord
de l'édition. Dans l'éditeur d'un document (`/documents/{id}`), le panneau
**« Publication (Microsoft 365 / SAP) »** permet de choisir :

- une **Cible** : `SHAREPOINT`, `ONEDRIVE`, `OUTLOOK`, `TEAMS`, `SAP` ;
- un **Format** : `PDF` ou `DOCX` ;

puis le bouton **Publier** :

1. rend le document dans le format choisi (`DocumentRenderingService`) ;
2. délègue le dépôt à l'adaptateur de la cible via le port `PublicationCiblePort` ;
3. journalise le résultat dans `PublicationLog` **et** dans le journal d'audit (`PUBLIER`).

La **référence financière SAP d'un dossier** suit un flux distinct : depuis un dossier
d'approvisionnement, le bouton **« Transmettre la référence à SAP »** appelle `SapReferenceService`,
qui agrège les métadonnées (n° AO, objet, nombre de documents, montant total) et les remet au port
`ReferenceFinancierePort`.

### 1.1.1 Répertoire de destination (SharePoint / OneDrive / Teams)

Le panneau de publication comporte un champ **« Répertoire de destination »** (ex. `Contrats/2027`)
qui indique **où** déposer le fichier dans la cible. Deux façons de le déterminer :

- **Saisi** dans le champ à chaque publication ;
- **Configuré** par défaut côté serveur, appliqué quand le champ est laissé vide, via la propriété
  `app.publication.dossier-defaut` (variable d'environnement `PUBLICATION_DOSSIER_DEFAUT`, défaut
  `Contrats`).

Le chemin est **normalisé et sécurisé** par `PublicationService.cheminValide(...)` : suppression des
séparateurs superflus et **refus de la traversée de répertoire** (`..`), pour qu'aucun dépôt ne sorte
du dossier visé — quelle que soit la cible. Le répertoire est transporté jusqu'à l'adaptateur dans
`PublicationCiblePort.ContenuPublie.destination()`. L'adaptateur de simulation l'inscrit dans l'URL
retournée (`sim://sharepoint/Contrats/2027/contrat-12.pdf`), visible dans l'historique.

Un adaptateur Microsoft Graph réel dépose alors le fichier **dans ce répertoire** :
`PUT /sites/{siteId}/drive/root:/{destination}/{nomFichier}:/content` (SharePoint) ou
`PUT /drives/{driveId}/root:/{destination}/{nomFichier}:/content` (OneDrive) ; pour Teams, le fichier
est téléversé dans le drive du canal sous le même sous-dossier. En multi-tenant, `destination` se
combine avec le `rootFolder`/`driveId` propres au tenant (voir la table de correspondance décrite dans
[trois-riviere/integration-microsoft-365-sharepoint-teams.md](trois-riviere/integration-microsoft-365-sharepoint-teams.md)).

| Rôle requis | Action |
|---|---|
| `REDACTEUR`, `APPRO`, `ADMIN` | Publier un document (M365/SAP) — `PublicationController` |
| `APPRO`, `ADMIN` | Transmettre la référence financière à SAP — `SapReferenceController` |

### 1.2 Par défaut, tout est **simulé** (repli hors connexion)

- [`SimulationPublicationAdapter`](../backend/src/main/java/qc/levis/contrat/integration/SimulationPublicationAdapter.java)
  — `@Order(LOWEST_PRECEDENCE)`, `supporte(...) == true` pour **toutes** les cibles. Renvoie une URL
  `sim://...` sans aucun appel réseau.
- [`SimulationSapAdapter`](../backend/src/main/java/qc/levis/contrat/integration/SimulationSapAdapter.java)
  — même principe, URL `sap://dossier/...`.

Ces adaptateurs permettent de **démontrer et tester** le parcours de bout en bout sans dépendre de
Microsoft 365 ni de SAP. Ils restent le **repli** dès qu'un adaptateur réel est présent.

### 1.3 Le point d'extension : ports & adaptateurs (ADR-002)

Le code applicatif ne connaît jamais le protocole. Deux ports (couche `service.port`) :

- [`PublicationCiblePort`](../backend/src/main/java/qc/levis/contrat/service/port/PublicationCiblePort.java)
  — dépôt d'un **document rendu** (octets) vers SharePoint / OneDrive / Outlook / Teams.
- [`ReferenceFinancierePort`](../backend/src/main/java/qc/levis/contrat/service/port/ReferenceFinancierePort.java)
  — dépôt de **métadonnées** financières vers SAP.

`PublicationService` reçoit **la liste** des `PublicationCiblePort` (injection Spring) et retient le
**premier** qui `supporte(cible)`. Un adaptateur réel avec une **priorité supérieure**
(`@Order(HIGHEST_PRECEDENCE)`) prime donc automatiquement sur la simulation, **sans modifier** le
service (principes OCP/DIP). Aucune cible perdue : si l'adaptateur réel ne couvre pas une cible, la
simulation la sert encore.

> **Passer au réel = ajouter un `@Component` implémentant le port + fournir la configuration.**
> Rien d'autre à changer dans les contrôleurs ni les services.

---

## 1.2 Configuration par tenant depuis l'espace admin (recommandé pour les essais clients)

Chaque organisation peut brancher **son propre** environnement Microsoft 365 **sans fichier yml ni
redéploiement**, depuis **Paramètres → « Intégration Microsoft 365 »** (rôle `ADMIN`) :

> 📘 **Comment obtenir chaque valeur du formulaire** (ID de locataire, d'application, secret, ID de
> site SharePoint, drive OneDrive, équipe/canal Teams, expéditeur Outlook) : voir le tutoriel pas-à-pas
> [`tutoriel-configuration-office365.md`](tutoriel-configuration-office365.md).


- Champs saisis : ID de locataire (tenant Entra), ID d'application (client), **secret client**, ID de
  site SharePoint, ID de drive OneDrive, ID d'équipe et de canal Teams, expéditeur Outlook, répertoire
  racine. Endpoints `GET/PUT /api/parametres/integration-m365` et `POST …/test` (tenant courant).
- Le **secret client est chiffré au repos** (`SecretCipher`, AES via `spring-security-crypto` ; clé/sel
  par `app.crypto.cle`/`app.crypto.sel`, variables `APP_CRYPTO_KEY`/`APP_CRYPTO_SALT`) et **jamais
  renvoyé** au navigateur — seule sa présence (`secretDefini`) est exposée. Saisie *write-only*.
- **Bascule par tenant, pas globale** : `GraphPublicationAdapter` (`@Order(HIGHEST_PRECEDENCE)`) ne prend
  la main que si le tenant courant a une config **active et complète** (`IntegrationM365Service.actifEtConfigure`)
  et que la cible est M365 ; sinon `supporte(...)` renvoie `false` et l'on retombe sur
  `SimulationPublicationAdapter`. **Par défaut : simulation.**
- Bouton **« Tester la connexion »** : obtient un jeton *client credentials* avec les identifiants
  enregistrés (`Microsoft365Client.tester`) et rapporte le succès/échec, sans rien déposer.
- Les appels Graph réels sont dans `HttpMicrosoft365Client` (JDK `HttpClient`, aucun SDK) : jeton
  `client_credentials`, dépôt `PUT …/root:/{destination}/{nom}:/content`, Outlook via `sendMail`.

> Le mode yml/Key Vault décrit ci-dessous reste possible pour une configuration **globale** ; la config
> par tenant en base le **surclasse** et convient mieux à un SaaS multi-organisations.

### 1.2.0 Visualiser les fichiers réellement déposés (mode simulation)

En mode simulation (défaut), les dépôts ne sont pas fictifs : chaque publication **écrit réellement le
fichier** dans un magasin local, rangé par environnement et par tenant, et une **visionneuse** permet de
le consulter/télécharger.

- `SimulateurDepotStore` écrit sous `{app.publication.simulateur.repertoire}/{tenantId}/{cible}/{destination}/{nomFichier}`
  (défaut `data/simulateur`, variable `PUBLICATION_SIMULATEUR_REPERTOIRE`). Isolation par tenant,
  protection anti-traversée à l'écriture et à la lecture.
- `SimulationPublicationAdapter` (SharePoint/OneDrive/Teams/Outlook) y écrit les octets rendus ;
  `SimulationSapAdapter` y écrit un enregistrement lisible de la référence financière (`{n° AO}.txt`).
- Écran **Pilotage → « Dépôts simulés »** ([SimulateurDepots.jsx](../frontend/src/pages/SimulateurDepots.jsx)) :
  liste par environnement (SharePoint, OneDrive, Teams, Outlook, SAP) avec aperçu/téléchargement.
  Endpoints `GET /api/simulateur/depots` et `GET …/contenu?chemin=…` (rôles `REDACTEUR`/`APPRO`/`ADMIN`,
  tenant courant). C'est le « répertoire local » du patron Mock décrit dans
  [trois-riviere/integration-microsoft-365-sharepoint-teams.md](trois-riviere/integration-microsoft-365-sharepoint-teams.md).

### 1.2.1 Valider sans tenant réel : le simulateur Microsoft 365

Pour éprouver l'intégration **sans** environnement Microsoft réel, un **simulateur en mémoire**
reproduit la surface Graph utile :

- `FakeMicrosoft365Server` (test, serveur HTTP du JDK, sans dépendance) répond à l'émetteur de jetons
  Entra ID (`/{tenant}/oauth2/v2.0/token`), aux dépôts SharePoint/OneDrive/Teams
  (`PUT …/root:/{chemin}:/content`) et à l'envoi Outlook (`/users/{exp}/sendMail`), capture chaque
  requête et sait simuler des échecs (jeton refusé, dépôt interdit).
- `HttpMicrosoft365ClientTest` fait passer le **vrai** `HttpMicrosoft365Client` contre ce simulateur et
  vérifie : acquisition du jeton, URL/chemin exact par cible, en-tête `Authorization: Bearer`, octets
  téléversés, parsing du `webUrl`, et les chemins d'échec. Le client réel n'est donc **plus** exclu de la
  couverture.

Les URL de base sont **configurables** (`app.m365.graph-base-url` / `app.m365.login-base-url`, variables
`M365_GRAPH_BASE_URL` / `M365_LOGIN_BASE_URL`, défaut = Microsoft réel). On peut donc aussi valider
**en conditions réelles applicatives**, sans code : lancer un stub HTTP (le simulateur, WireMock, ou
équivalent) sur un port, pointer ces variables dessus au démarrage du backend, saisir une config M365
de test dans **Paramètres**, cliquer **« Tester la connexion »** puis publier un document — le dépôt
part réellement vers le stub et l'URL retournée apparaît dans l'historique.

## 2. Configurer Microsoft 365 (Microsoft Graph)

Les quatre cibles M365 (SharePoint, OneDrive, Outlook, Teams) passent par une **seule** API :
**Microsoft Graph**, avec le flux OAuth2 *client credentials* (application, sans utilisateur).

### 2.1 Enregistrement d'application (Microsoft Entra ID / Azure AD)

1. **Entra ID → App registrations → New registration** : nommer l'app (ex. `vina-contrat-graph`).
2. Récupérer : **Directory (tenant) ID**, **Application (client) ID**.
3. **Certificates & secrets → New client secret** : noter la valeur (une seule fois).
4. **API permissions → Add a permission → Microsoft Graph → Application permissions** :

   | Cible | Permission Graph (application) |
   |---|---|
   | SharePoint | `Sites.ReadWrite.All` (ou `Sites.Selected` + octroi par site, recommandé) |
   | OneDrive | `Files.ReadWrite.All` |
   | Outlook | `Mail.Send` |
   | Teams | `ChannelMessage.Send` + `Group.ReadWrite.All` (ou `Team.ReadBasic.All`) |

5. **Grant admin consent** (consentement administrateur du locataire).
6. Noter les **identifiants de destination** : ID du site SharePoint, ID du drive OneDrive, adresse
   d'expéditeur Outlook, ID d'équipe et de canal Teams.

> **Principe du moindre privilège** : préférer `Sites.Selected` avec octroi par site plutôt que
> `Sites.ReadWrite.All` sur tout le locataire, lorsque la Ville le permet.

### 2.2 Dépendances Maven (backend)

```xml
<!-- backend/pom.xml -->
<dependency>
  <groupId>com.microsoft.graph</groupId>
  <artifactId>microsoft-graph</artifactId>
  <version>6.16.0</version>
</dependency>
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.13.3</version>
</dependency>
```

### 2.3 Adaptateur réel (esquisse)

À placer dans `backend/src/main/java/qc/levis/contrat/integration/` :

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)                 // prime sur la simulation (OCP/DIP)
@ConditionalOnProperty(prefix = "app.m365", name = "enabled", havingValue = "true")
public class GraphPublicationAdapter implements PublicationCiblePort {

    private final GraphServiceClient graph;         // construit à partir de ClientSecretCredential
    private final M365Properties props;             // @ConfigurationProperties("app.m365")

    @Override
    public boolean supporte(Enums.CiblePublication cible) {
        return cible == SHAREPOINT || cible == ONEDRIVE || cible == OUTLOOK || cible == TEAMS;
    }

    @Override
    public ResultatPublication deposer(Enums.CiblePublication cible, ContenuPublie c) {
        return switch (cible) {
            case SHAREPOINT -> televerser(props.sharepointDriveId(), c);   // PUT .../root:/{nom}:/content
            case ONEDRIVE   -> televerser(props.onedriveDriveId(), c);
            case OUTLOOK    -> envoyerCourriel(c);                         // POST /users/{exp}/sendMail
            case TEAMS      -> posterCanal(c);                            // POST /teams/{id}/channels/{id}/messages
            default -> throw new IllegalStateException("Cible non gérée: " + cible);
        };
    }
    // ... téléversement Graph, gestion d'erreurs → ResultatPublication(false, null, message)
}
```

Correspondance appels Graph :

| Cible | Appel Microsoft Graph |
|---|---|
| SharePoint | `PUT /sites/{siteId}/drive/root:/{dossier}/{nomFichier}:/content` |
| OneDrive | `PUT /drives/{driveId}/root:/{dossier}/{nomFichier}:/content` |
| Outlook | `POST /users/{expediteur}/sendMail` (pièce jointe = octets rendus) |
| Teams | `POST /teams/{teamId}/channels/{channelId}/messages` (+ lien du fichier déposé) |

### 2.4 Configuration (profil `m365`)

Créer `backend/src/main/resources/application-m365.yml` — **valeurs par variables d'environnement,
aucun secret en dur** (cohérent avec `application.yml` et `application-azure.yml`) :

```yaml
# Profil "m365" : dépôts réels vers Microsoft 365 via Microsoft Graph.
# Activation : --spring.profiles.active=prod,m365  (cumulable)
app:
  m365:
    enabled: ${M365_ENABLED:true}
    tenant-id:     ${M365_TENANT_ID}
    client-id:     ${M365_CLIENT_ID}
    client-secret: ${M365_CLIENT_SECRET}
    sharepoint-site-id: ${M365_SHAREPOINT_SITE_ID}
    onedrive-drive-id:  ${M365_ONEDRIVE_DRIVE_ID}
    outlook-expediteur: ${M365_OUTLOOK_SENDER}
    teams-team-id:      ${M365_TEAMS_TEAM_ID}
    teams-channel-id:   ${M365_TEAMS_CHANNEL_ID}
    dossier-cible:      ${M365_TARGET_FOLDER:Contrats}
```

Sans le profil `m365` (ou `M365_ENABLED=false`), l'adaptateur n'est pas chargé et la **simulation
reste active** : le comportement de démonstration est préservé.

---

## 3. Configurer SAP

> 📘 **Tester et valider réellement un dépôt SAP** (faut-il « s'abonner » ? comment obtenir un
> environnement d'essai ? comment brancher et vérifier ?) : voir [`tutoriel-sap.md`](tutoriel-sap.md).


Deux points d'entrée :

- **Référence financière du dossier** (principal) → `ReferenceFinancierePort`.
- **Dépôt de document** vers la cible `SAP` (optionnel) → `PublicationCiblePort` (mêmes principes que
  M365 §2.3, avec `supporte(cible) == cible == SAP`).

Selon le paysage SAP de la Ville, choisir un canal :

| Canal | Quand | Mise en œuvre |
|---|---|---|
| **OData** (S/4HANA, Gateway) | API REST modernes exposées | `RestClient`/`WebClient` + jeton OAuth2 ou clef |
| **RFC/BAPI** (ECC) | Backend classique | SAP JCo (`sapjco3`) — bibliothèque native à installer |
| **Fichier IDoc/CSV déposé** | Intégration par lots | dépôt sur SFTP/partage surveillé par SAP PI/PO |

Esquisse d'adaptateur OData :

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "app.sap", name = "enabled", havingValue = "true")
public class SapODataAdapter implements ReferenceFinancierePort {
    @Override
    public ResultatPublication deposer(ReferenceFinanciere r) {
        // POST {baseUrl}/API_PURCHASEREQ/... { numeroAO, objet, montantTotal, nbDocs }
        // → ResultatPublication(true, "sap://.../{id}", "…") ou (false, null, message)
    }
}
```

Configuration `application-sap.yml` (profil `sap`) :

```yaml
app:
  sap:
    enabled:  ${SAP_ENABLED:true}
    base-url: ${SAP_BASE_URL}          # ex. https://sap.ville.qc.ca/sap/opu/odata/...
    client:   ${SAP_CLIENT}            # mandant
    username: ${SAP_USER}
    password: ${SAP_PASSWORD}
    # ou OAuth2 : token-url / client-id / client-secret
```

---

## 4. Récapitulatif des variables d'environnement

| Variable | Cible | Exemple |
|---|---|---|
| `M365_ENABLED` | active l'adaptateur Graph | `true` |
| `M365_TENANT_ID` | Entra ID | `00000000-0000-...` |
| `M365_CLIENT_ID` | Entra ID | `11111111-1111-...` |
| `M365_CLIENT_SECRET` | Entra ID (**secret**) | *(coffre-fort)* |
| `M365_SHAREPOINT_SITE_ID` | SharePoint | `ville.sharepoint.com,guid,guid` |
| `M365_ONEDRIVE_DRIVE_ID` | OneDrive | `b!...` |
| `M365_OUTLOOK_SENDER` | Outlook | `contrats@ville.qc.ca` |
| `M365_TEAMS_TEAM_ID` / `M365_TEAMS_CHANNEL_ID` | Teams | `19:...@thread.tacv2` |
| `SAP_ENABLED` / `SAP_BASE_URL` / `SAP_CLIENT` / `SAP_USER` / `SAP_PASSWORD` | SAP | *(coffre-fort)* |

---

## 5. Déploiement : où placer ces secrets

Ne **jamais** committer de secret. Les injecter au démarrage selon la cible d'hébergement :

- **Azure** (voir [`deploiement-azure.md`](deploiement-azure.md)) : stocker `M365_*`/`SAP_*` dans
  **Azure Key Vault**, référencés par l'App Service / le conteneur ; activer le profil
  `--spring.profiles.active=prod,m365,sap`. L'app Graph réutilise le **même locataire Entra** que le
  SSO déjà décrit dans `application-azure.yml`.
- **OVH / VPS Docker** (voir [`../../vina solution/docs/DEPLOYMENT-OVH-VPS.md`](../../vina%20solution/docs/DEPLOYMENT-OVH-VPS.md)) :
  fichier `.env` hors dépôt (droits `600`) monté par `docker-compose`, ou secrets Docker Swarm.
- **Multitenant** (voir [`deploiement-multitenant-sous-domaine.md`](deploiement-multitenant-sous-domaine.md)) :
  si chaque Ville a **son** locataire M365/SAP, prévoir des identifiants **par tenant** (table de
  configuration chiffrée, ou secrets nommés par slug) plutôt que des variables globales.

---

## 6. Activer / vérifier

```bash
# Activer les profils au lancement du backend
--spring.profiles.active=prod,m365,sap

# Vérifier qu'un adaptateur réel est bien chargé (le simulé ne doit plus servir les cibles réelles)
# → au démarrage, logguer les PublicationCiblePort injectés ; tester un dépôt vers SharePoint
#   puis contrôler l'historique (URL réelle "https://..." et non "sim://...").
```

- **Test d'intégration** : mocker le `GraphServiceClient` (contrat), et un test end-to-end sur un
  site/canal de recette. Conserver les tests existants de la simulation (repli).
- **Traçabilité** : chaque dépôt reste journalisé (`PublicationLog` + audit `PUBLIER` /
  `REFERENCE_SAP`), réel comme simulé.

---

## 7. Option : dépôt automatique

Le dépôt est manuel par défaut (contrôle de l'utilisateur). Pour l'**automatiser**, brancher un
écouteur d'événement métier — par exemple, à l'**approbation finale** du document, appeler
`PublicationService.publier(...)` vers la ou les cibles configurées du tenant. À décider avec la
Ville (un ADR sera rédigé) : cible(s) par défaut, format, gestion des échecs et rejeu.

---

## 8. Module Contrats et signature : dépôt automatique des trousses terminées (Annexe 10 d'IQ, TI-01)

Décision : [ADR-029](adr/ADR-029-microsoft-365-pour-le-module-de-signature.md). Le module de signature réutilise la
**même connexion Microsoft 365** et les **mêmes adaptateurs** que la publication ci-dessus (Graph prioritaire, simulation
de repli) : aucun second client Graph.

- **Connexion** : `GET/PUT /api/parametres/integration-m365` est ouvert aux organisations qui souscrivent le module
  `redaction` **ou** `clm` (administration technique). Tant qu'elle n'est pas activée, tout est simulé.
- **Réglage du dépôt** (administration technique, écran *Administration CLM → Microsoft 365*,
  `GET/PUT /api/clm/admin/integrations/m365`) : activation, bibliothèque (`SHAREPOINT` ou `ONEDRIVE`), dossier, dépôt
  du dossier de preuve, avis Teams.
- **Déclenchement** : à l'achèvement de la trousse (`SIGNATURE_TERMINEE`), un dépôt est inscrit par document signé, puis
  pour le dossier de preuve (ZIP) et l'avis Teams, dans la transaction qui termine la trousse. Classement :
  `{dossier}/{référence de la trousse}/`.
- **Avis Teams** : court fichier texte (référence, titre, emplacements) déposé dans le dossier du canal Teams configuré.
- **Reprise** : le planificateur du module relaie les dépôts dus (recul exponentiel, abandon après 8 essais) ; suivi et
  relance : `GET /api/clm/admin/integrations/m365/depots`, `POST …/depots/{id}/relancer`, `POST …/relayer`.
- **Traçabilité** : chaque dépôt réussi est consigné au journal scellé de la trousse (`DEPOT_M365`).

## 9. Module Contrats et signature : étiquettes Microsoft Purview (Annexe 10 d'IQ, SEC-27)

Décision : `docs/adr/ADR-036-etiquettes-microsoft-purview.md`.

- **Écran** : Contrats et signature → Administration → Étiquettes Purview (`/clm/admin/purview`), administration
  d'affaires ou technique. On choisit, dans le catalogue du locataire, l'étiquette de sensibilité et l'étiquette de
  rétention par défaut, et au besoin par type de contrat.
- **Effet** : chaque document déposé ou produit reçoit les étiquettes de la politique applicable et les garde (onglet
  Documents → métadonnées) ; la version signée reprend celles de sa source ; après le dépôt SharePoint/OneDrive (§ 8),
  les étiquettes sont apposées au fichier déposé (colonne « Emplacement » du suivi des dépôts, journal `ETIQUETTE_PURVIEW`).
  La destruction d'une trousse n'intervient jamais avant la fin de la rétention de l'étiquette.
- **Simulé par défaut** : catalogue représentatif et apposition consignée, sans appel externe.
- **Réel** : profil `purview` (cumulable : `--spring.profiles.active=prod,m365,purview`). Il emploie la connexion
  Microsoft 365 de l'organisation (même application Entra ID), avec les permissions d'application
  `InformationProtectionPolicy.Read.All`, `RecordsManagement.Read.All`, `Files.ReadWrite.All` et `Sites.ReadWrite.All`
  (consentement administrateur). `assignSensitivityLabel` est une API Graph facturée à l'usage.
