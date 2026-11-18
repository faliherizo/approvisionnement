# Guide d'exploitation — annuaire d'entreprise, rôles et provisionnement SCIM 2.0

Pour les administrateurs techniques d'IQ et du fournisseur. Décision d'architecture : [ADR-014](../../adr/ADR-014-annuaire-entreprise-et-scim.md).
Exigences : addenda 3, Q42, Q43, Q44, Q51.

## 1. Principe

Trois sources alimentent les droits d'un compte, dans cet ordre de priorité :

1. **SCIM 2.0** (Saviynt ou Entra ID) crée, modifie et désactive les comptes, et pousse les groupes et leurs membres.
2. **La connexion unique** (OIDC ou SAML) rapporte, à chaque connexion, les groupes et les attributs du jeton.
3. **Les correspondances** de l'organisation traduisent groupes et attributs en **rôles** et en **domaines d'affaires**.

Un compte sans correspondance reçoit le rôle par défaut (`LECTEUR` pour un compte SCIM ; celui de la configuration SSO
sinon). Tant que l'organisation n'a défini aucune correspondance de rôle, les rôles attribués à la main sont conservés.

## 2. Définir les correspondances

Administration → **Authentification unique** → bloc **Annuaire : rôles et domaines d'affaires**.

| Source | Valeur | Rôle | Domaine d'affaires | Effet |
|---|---|---|---|---|
| Groupe | `3f2a…` (identifiant d'objet Entra ID) | `JURIDIQUE` | `AFFAIRES_JURIDIQUES` | Les juristes valident et voient les trousses de leur unité |
| Groupe | `GRP-CLM-ADMIN` | `ADMIN` | — | Administration du module |
| Attribut `department` | `Financement des entreprises` | — | `FINANCEMENT_ENTREPRISES` | Domaine tiré de l'attribut d'annuaire |

- Les valeurs se comparent sans égard à la casse.
- Un domaine d'affaires est un **code de groupe du module** (majuscules, chiffres, `_`). S'il n'existe pas, il est créé
  au premier compte concerné. Pour ouvrir les trousses d'une unité, utiliser le code de l'unité (liste « Unités
  administratives »).
- Le rôle `SUPERADMIN` n'est jamais attribuable.
- Ajouts et retraits de correspondances, et chaque changement de rôle qui en découle, sont au **journal d'audit**
  (`ANNUAIRE_CORRESPONDANCE_AJOUTEE`, `ANNUAIRE_CORRESPONDANCE_RETIREE`, `ROLES_ANNUAIRE` avec l'avant et l'après).

## 3. Microsoft Entra ID (SSO)

- **OIDC** : dans l'inscription d'application, *Token configuration* → *Add groups claim* (groupes de sécurité ;
  identifiant d'objet ou nom synchronisé, voir question N6), et au besoin les *app roles* (claim `roles`). Les attributs
  utiles (ex. `department`) s'ajoutent comme *optional claims*.
- **SAML** : ajouter la revendication de groupes
  (`http://schemas.microsoft.com/ws/2008/06/identity/claims/groups`) et les attributs voulus.
- Seuls les attributs cités par une correspondance sont conservés par la plateforme.
- Un compte **désactivé** (départ, déprovisionnement) est refusé à la connexion, même si Entra ID l'authentifie.

## 4. Provisionnement SCIM 2.0 (Saviynt, Entra ID)

### 4.1 Mise en service

1. Administration → Authentification unique → bloc **Provisionnement SCIM 2.0** → **Créer un jeton**.
2. Copier le jeton **immédiatement** : il n'est plus jamais affiché (seule son empreinte SHA-256 est conservée).
3. Dans Saviynt (ou Entra ID → *Provisioning*) : URL du locataire = adresse du point affichée
   (`https://<domaine>/scim/v2`), jeton secret = le jeton copié.
4. Tester la connexion depuis l'outil, puis lancer un cycle sur un groupe pilote.

**Rotation** : **Remplacer le jeton** révoque aussitôt l'ancien ; saisir le nouveau dans Saviynt dans la foulée.

### 4.2 Ressources et opérations prises en charge

| Ressource | Opérations | Remarques |
|---|---|---|
| `/Users` | `GET` (liste, `filter=userName eq "…"`, `startIndex`, `count`), `POST`, `GET /{id}`, `PUT`, `PATCH`, `DELETE` | `userName` = courriel ; `active` accepté en booléen ou en texte (« True », « False ») |
| `/Groups` | `GET` (liste, `filter=displayName eq "…"`), `POST`, `GET /{id}`, `PUT`, `PATCH` (`add`/`remove` `members`, `members[value eq "…"]`, `displayName`), `DELETE` | Le nom **et** l'`externalId` du groupe servent aux correspondances |
| `/ServiceProviderConfig`, `/ResourceTypes` | `GET` | Découverte |

Non pris en charge : opérations en lot (*bulk*), tri, ETag, changement de mot de passe (comptes fédérés sans mot de
passe local).

### 4.3 Cycle de vie d'un compte

| Événement Saviynt | Effet dans la plateforme |
|---|---|
| Création | Compte actif, rôle `LECTEUR`, aucun mot de passe local exploitable ; journal `PROVISION_SCIM` |
| Ajout à un groupe | Rôles et domaines recalculés selon les correspondances ; journal `ROLES_ANNUAIRE` si les rôles changent |
| Retrait d'un groupe | Idem ; retour au rôle par défaut si plus aucune correspondance ne s'applique |
| `active = false` ou `DELETE` | Compte **désactivé, jamais effacé** (preuves et piste d'audit conservées) ; connexion refusée ; journal `DEPROVISION_SCIM` |
| `active = true` | Compte réactivé ; journal `REACTIVATION_SCIM` |

Les trousses et tâches d'un compte désactivé restent en place : l'administrateur les réaffecte (question P3).

### 4.4 Erreurs

Format SCIM (`urn:ietf:params:scim:api:messages:2.0:Error`) : `401` jeton invalide ou révoqué, `404` ressource
inconnue, `409 uniqueness` courriel ou nom de groupe déjà pris, `400 invalidValue` donnée ou filtre non pris en charge.

### 4.5 Essai sans Saviynt

```bash
JETON=scim_...   # créé dans l'administration
curl -s -H "Authorization: Bearer $JETON" https://<domaine>/scim/v2/ServiceProviderConfig
curl -s -X POST -H "Authorization: Bearer $JETON" -H "Content-Type: application/scim+json" \
  -d '{"userName":"essai@iq.example","name":{"givenName":"Essai","familyName":"SCIM"},"active":true}' \
  https://<domaine>/scim/v2/Users
```

Essais automatisés : `Addenda3IntegrationTest` (cycle complet : création, groupe, rôle, domaine, retrait,
désactivation, révocation du jeton) et e2e « Addenda 3 — Q43 / Q51 ».

## 5. Exploitation

- **Surveillance** : journal d'audit (filtres `PROVISION_SCIM`, `DEPROVISION_SCIM`, `ROLES_ANNUAIRE`) ; réponses `401`
  répétées = jeton à remplacer dans Saviynt.
- **Réseau** : `/scim/` est relayé par nginx vers le backend ; le point peut être restreint aux adresses de Saviynt au
  pare-feu applicatif.
- **Migration 058** : tables `annuaire_correspondance`, `annuaire_appartenance`, `annuaire_groupe_scim`,
  `annuaire_membre_scim`, `annuaire_jeton_scim`.
