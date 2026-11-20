# Tenant Investissement Québec — organisation, profils et rôles

Ce document décrit l'organisation de démonstration créée pour l'AO 2026-6370-0005 : le tenant, les modules qu'il
souscrit, un compte par profil demandé par le client, la correspondance avec les rôles de la plateforme et ce que chaque
profil voit et peut faire. La validation de l'isolation est rapportée dans
[validation_isolation_tenant_iq.md](validation_isolation_tenant_iq.md) ; la décision d'architecture dans
[ADR-009](../../adr/ADR-009-isolation-fonctionnelle-par-module.md).

## 1. Organisation

| Élément | Valeur |
|---|---|
| Identifiant (slug, sous-domaine) | `iq` |
| Titre | Investissement Québec |
| Sous-titre | Contrats et signature électronique — AO 2026-6370-0005 |
| Modules souscrits | `clm` — Contrats & signature électronique ; `clm_ia` — option CLM/IA |
| Module explicitement retiré | `redaction` — socle de rédaction contractuelle (dossiers, documents, clauses, gabarits, circuits, assistant, pilotage) |
| Autres modules | `formulaire`, `regle`, `saisie`, `processus`, `notification`, `rapport`, `interop`, `document`, `application`, `securite_avancee`, `rh`, `paie` : inactifs (valeur par défaut du catalogue) |
| Amorçage | [`data/tenants-demo.json`](../../../backend/src/main/resources/data/tenants-demo.json), lu par `TenantsDemoSeeder` après le catalogue des modules |
| Activation de l'amorçage | `app.amorcage.tenants-demo` : vrai en développement, en test et dans le `docker-compose.yml` de démonstration, faux en production (variable `APP_TENANTS_DEMO`) |

L'amorçage est idempotent : les modules ne sont appliqués qu'à la création du tenant (le superadmin a pu les changer
depuis) et un compte existant n'est jamais modifié.

## 2. Profils du client et comptes de démonstration

Source des profils : [analyse des besoins, § 3 « Acteurs et rôles métier »](../02-Analyse/analyse_besoins_exigences.md) et
pied de page « Rôles » des maquettes UI-01 à UI-24.

| Profil (client) | Responsabilités attendues (§ 3) | Compte de démonstration | Rôle plateforme |
|---|---|---|---|
| Initiateur | Créer une trousse, ajouter documents, participants et paramètres ; lancer ; suivre | `initiateur@iq.example` — Camille Tremblay, Financement des entreprises | `REDACTEUR` |
| Validateur | Consulter, commenter, valider, rejeter ou retourner pour correction | `validateur@iq.example` — Louis Gagnon, Affaires juridiques | `JURIDIQUE` |
| Approbateur | Approuver, rejeter ou demander correction avant signature | `approbateur@iq.example` — Sophie Roy, Vice-présidence Financement | `DIRECTION` |
| Signataire interne | Authentifier son identité et signer selon l'ordre défini | `signataire@iq.example` — Étienne Côté, Direction générale | `LECTEUR` |
| Administrateur d'affaires | Administrer modèles, clauses, workflows, groupes, règles, conservation ; gérer les trousses | `admin.affaires@iq.example` — Julie Bouchard, Gestion contractuelle | `ADMIN_AFFAIRES` |
| Administrateur technique | Administrer intégrations, authentification unique, annuaire et SCIM, sécurité ; aucun accès au contenu des trousses | `admin.technique@iq.example` — Nadia Ouellet, Technologies de l'information | `ADMIN_TECHNIQUE` |
| Administrateur principal | Cumule les deux administrations ; seul à attribuer les rôles (addenda 4, Q10 ; ADR-017) | `admin.ti@iq.example` — Patrick Morin, Technologies de l'information | `ADMIN` |
| Auditeur / conformité | Consulter l'historique, les preuves, certificats et événements | `auditeur@iq.example` — Nadia Lavoie, Conformité et audit interne | `AUDITEUR` (nouveau) |

Mot de passe de démonstration : `Passw0rd!` (même convention que les comptes de Lévis). Ces comptes n'existent pas en
production.

### 2.1 Se connecter au tenant IQ

| Environnement | Adresse | Amorçage des comptes |
|---|---|---|
| Docker (`docker compose up -d`) | <http://localhost:8082/login> | `APP_TENANTS_DEMO` vaut `true` par défaut dans [`docker-compose.yml`](../../../docker-compose.yml) |
| Développement (backend `:8085`, Vite) | <http://localhost:5173/login> | Actif par défaut (profil hors `prod`) |
| Production | `https://iq.<domaine>/login` | Aucun compte de démonstration (`APP_TENANTS_DEMO=false`) |

1. Ouvrir la page de connexion, saisir le **courriel** d'un compte du tableau ci-dessus et le mot de passe `Passw0rd!`.
2. Aucun second facteur n'est demandé aux comptes de démonstration.
3. Le compte arrive directement sur le **Tableau de bord des contrats** (`/clm`). Les écrans d'approvisionnement ne sont
   pas proposés : le tenant exploite IQ seul ([ADR-010](../../adr/ADR-010-exclusivite-produits-approvisionnement-iq.md)).

Par l'API (champ `motDePasse`, et non `password`) :

```bash
curl -c iq.jar -X POST http://localhost:8082/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin.ti@iq.example","motDePasse":"Passw0rd!"}'
```

**Dépannage — « courriel ou mot de passe invalide » sur Docker.** Le conteneur backend tourne en profil `prod`, qui
n'amorce pas les organisations de démonstration. Si les comptes manquent, vérifier que la variable est bien passée et
recréer le conteneur, sans reconstruire l'image :

```bash
docker inspect contrat-backend --format '{{range .Config.Env}}{{println .}}{{end}}' | grep APP_TENANTS_DEMO
docker compose up -d --no-build backend
docker logs contrat-backend 2>&1 | grep "démonstration"   # 1 organisation + 7 comptes créés
```

L'amorçage est idempotent : un redémarrage ne recrée ni ne modifie un compte existant. Un mot de passe changé depuis
l'application reste donc changé.

Acteurs du § 3 sans compte dans le tenant :

| Acteur | Accès |
|---|---|
| Signataire externe | Aucun compte : invitation à jeton, portail du signataire (UI-16) avec vérification d'identité |
| Système source Salesforce | Aucun compte utilisateur : client OAuth 2.0 configuré par l'administrateur technique (UI-21) |
| Exploitant / support | Hors tenant : console superadmin de VINA (`superadmin@plateforme.ca`) et alertes d'exploitation Azure (ST-14, runbook) |

Le « gestionnaire » des maquettes UI-01, UI-02, UI-18 et UI-24 n'a pas de rôle dédié : la vue de toutes les trousses
relève de l'administrateur d'affaires ; une vue limitée à une unité exige les groupes du module « Sécurité avancée »,
non souscrit.

## 3. Rôles de la plateforme et droits dans le module

Règles appliquées par le serveur ; l'interface n'offre que les actions permises.

| Action | REDACTEUR | JURIDIQUE | DIRECTION | LECTEUR | ADMIN | AUDITEUR |
|---|:-:|:-:|:-:|:-:|:-:|:-:|
| Voir une trousse (propriétaire, unité, intervenant désigné) | ✔ | ✔ | ✔ | ✔ | toutes | toutes, lecture seule |
| Créer une trousse | ✔ | ✔ | ✔ | — | ✔ | — |
| Modifier, lancer, annuler, reprendre ; documents, participants, parcours, génération | sa trousse | sa trousse | sa trousse | — | ✔ | — |
| Valider, approuver, signer une étape | si désigné | si désigné | si désigné | si désigné | si désigné | si désigné |
| Commenter une étape ; lancer ou réviser une analyse IA | intervenant | intervenant | intervenant | intervenant | ✔ | intervenant |
| Dossier de preuve d'une trousse visible : consulter, certificat, vérifier l'intégrité | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Exporter le dossier de preuve | sa trousse | sa trousse | sa trousse | — | ✔ | ✔ |
| Concevoir un modèle | ✔ | ✔ | — | — | ✔ | — |
| Publier un modèle | — | — | — | — | ✔ | — |
| Journal d'audit et événements (UI-23) | — | — | — | — | ✔ | ✔ |
| Administration du module (UI-17, UI-20, UI-21, UI-22, UI-24), authentification unique, journal de sécurité | — | — | — | — | ✔ | — |

Séparation des responsabilités : l'initiateur ne valide ni n'approuve son propre dossier (il peut le signer). « Si
désigné » : l'étape l'affecte par utilisateur, rôle, groupe ou participant ; « intervenant » : il voit la trousse comme
propriétaire, membre de l'unité ou intervenant désigné. Le rôle `AUDITEUR` n'ouvre que la consultation : un auditeur
n'intervient que si une étape le désigne expressément, comme tout compte.

## 4. Menus et écrans par profil

Tout compte IQ ouvre directement l'espace Contrats & signature (`/clm`). Bandeau commun : recherche globale des trousses,
langue, **Mon profil** (profil et sécurité du compte), **Se déconnecter**. Aucun lien « Retour à la plateforme » :
l'organisation n'a pas d'autre espace.

| Profil | Navigation principale | Administration | Action « Créer une trousse » |
|---|---|---|---|
| Initiateur, Validateur, Approbateur | Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé | — | ✔ |
| Signataire interne | Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé | — | — |
| Administrateur d'affaires, Administrateur technique | Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé | Notifications et rappels, Intégrations API, Paramètres et environnements, Utilisateurs et groupes, Journal d'audit, Options CLM et IA | ✔ |
| Auditeur / conformité | Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé | Journal d'audit | — |

Pages transverses servies dans l'espace du module :

| Besoin | Page de l'espace CLM | Profils |
|---|---|---|
| Profil personnel, mot de passe | `/clm/profil` | tous |
| Second facteur (TOTP, question) | `/clm/securite` | tous |
| Comptes, rôles et permissions (UI-20) | `/clm/admin/utilisateurs` | administrateurs |
| Authentification unique OIDC / SAML 2.0 (UI-20, zone 04) | `/clm/admin/sso` | administrateurs |
| Journal de sécurité (connexions, échecs, actions) | `/clm/admin/audit` | administrateurs |

## 5. Administration courante

- **Superadmin VINA** (`/superadmin`, « Modules par tenant », organisation « Investissement Québec (iq) ») : activer ou
  désactiver `clm`, `clm_ia` et `redaction` ; activer `clm_ia` réactive `clm`, désactiver `clm` retire `clm_ia`.
  Activer `clm` désactive les modules d'approvisionnement de l'organisation, et inversement
  ([ADR-010](../../adr/ADR-010-exclusivite-produits-approvisionnement-iq.md)).
- **Administrateur IQ** (`/clm/admin/utilisateurs`) : créer un compte, attribuer un rôle (dont `AUDITEUR`), retirer
  l'accès ; brancher l'annuaire d'IQ (`/clm/admin/sso`).

## 6. Mise en production

1. Laisser `APP_TENANTS_DEMO` à faux : aucun compte de démonstration.
2. Superadmin : créer l'organisation `iq`, son administrateur, puis activer `clm_ia` (tire `clm`) et désactiver
   `redaction`.
3. Publier le sous-domaine `iq.<domaine>` : la page de connexion prend le titre et le thème du tenant et refuse les
   comptes d'une autre organisation.
4. Brancher l'annuaire d'IQ (ST-01) dans `/clm/admin/sso`, puis désactiver les comptes locaux devenus inutiles.

## 7. Écarts et points à confirmer avec IQ

| Point | État | Proposition |
|---|---|---|
| Administrateur d'affaires et administrateur technique partagent le rôle `ADMIN` | Séparation fine non portée par le RBAC | Rôles `ADMIN_AFFAIRES` et `ADMIN_TI` si IQ l'exige |
| Gestionnaire d'unité (maquettes UI-01, UI-02, UI-18, UI-24) | Pas de rôle dédié | Groupes d'unité (module Sécurité avancée) ou rôle de consultation par unité |
| Journal des changements de comptes et de rôles (UI-20, zone 05) | Fait le 2026-11-08 : création, rôles (avant → après), désactivation, réactivation, mot de passe réinitialisé et suppression au journal d'audit | — |
| Signataire interne | `LECTEUR` : consulte aussi le catalogue des modèles publiés | Acceptable (lecture seule) ; rôle `SIGNATAIRE` dédié si IQ le demande |
