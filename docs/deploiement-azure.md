# Déploiement Azure (SaaS)

Pipeline **CI/CD → Azure Container Apps** pour exploiter la solution en **SaaS
multi-tenant**. Backend Spring Boot (profil `prod` → PostgreSQL), frontend Nginx,
base **Azure Database for PostgreSQL**, registre **ACR**, secrets injectés.

## Architecture cible

```
GitHub Actions ──(OIDC)──► Azure
   build+test ─► ACR (images) ─► Container Apps Environment
                                   ├─ contrat-backend (Spring Boot, prod)
                                   └─ contrat-frontend (Nginx SPA + proxy /api)
                                        │
                                   Azure Database for PostgreSQL (flexible)
```

## Fichiers
- [`.github/workflows/azure-deploy.yml`](../.github/workflows/azure-deploy.yml) — CI/CD
  (tests + couverture, build & push images, déploiement).
- [`infra/main.bicep`](../infra/main.bicep) — Infrastructure as Code (ACR, PostgreSQL,
  Container Apps env + apps, Log Analytics).
- Frontend : `default.conf.template` (proxy `${BACKEND_URL}` substitué au démarrage).

## 1. Prérequis (une fois)

```bash
az group create -n contrat-rg -l canadacentral

# App registration + federated credential (OIDC) pour GitHub Actions
az ad app create --display-name contrat-gh
# → créer un federated credential (sujet: repo:<org>/<repo>:ref:refs/heads/main)
# et un rôle Contributor sur le resource group.
```

## 2. Provisionnement de l'infrastructure

```bash
az deployment group create -g contrat-rg -f infra/main.bicep \
  -p acrName=contratacr dbPassword='<motDePasseFort>' jwtSecret='<cle256bits>'
```
Sorties : `backendUrl`, `frontendUrl`.

## 3. Variables & secrets GitHub

| Type | Nom | Valeur |
|------|-----|--------|
| Secret | `AZURE_CLIENT_ID` / `AZURE_TENANT_ID` / `AZURE_SUBSCRIPTION_ID` | OIDC |
| Variable | `ACR_NAME` | `contratacr` |
| Variable | `AZURE_RESOURCE_GROUP` | `contrat-rg` |
| Variable | `BACKEND_CONTAINERAPP` | `contrat-backend` |
| Variable | `FRONTEND_CONTAINERAPP` | `contrat-frontend` |

Le frontend doit connaître l'URL du backend : définir sur le Container App frontend
`BACKEND_URL = https://<backendUrl>` (le template Nginx la substitue).

## 4. Déploiement

Push sur `main` → le workflow teste (couverture ≥ 90 %), publie les images taguées
par `github.sha` et met à jour les Container Apps. Rollback = redéployer un tag antérieur.

## 5. Multi-tenant par sous-domaine (`slug.contrat.vinaitsolution.com`)

Chaque tenant accède à l'application via **son sous-domaine** (slug créé par le superadmin) ; la
page de connexion ne demande pas le tenant (résolu du sous-domaine). Architecture et logique
détaillées dans **[deploiement-multitenant-sous-domaine.md](deploiement-multitenant-sous-domaine.md)**.
Côté Azure, prévoir :

- **DNS** : un enregistrement **wildcard** `*.contrat.vinaitsolution.com` (CNAME vers le FQDN du
  frontend Container App ou de l'Azure Front Door) en plus de `contrat.vinaitsolution.com`.
- **Domaine personnalisé + certificat wildcard** : deux options.
  - **Azure Front Door (recommandé)** devant le frontend : ajouter un **domaine personnalisé
    wildcard** `*.contrat.vinaitsolution.com` et laisser Front Door **gérer le certificat**
    (managed TLS) — pas de renouvellement manuel. Route par défaut → Container App frontend.
  - **Container Apps directement** : lier le domaine wildcard et téléverser un **certificat
    wildcard** (Let's Encrypt DNS-01, ou App Service Certificate). À défaut de wildcard managé,
    prévoir le renouvellement.
    ```bash
    # Exemple : certificat wildcard Let's Encrypt via DNS-01 (challenge sur la zone Azure DNS)
    #   certbot certonly --dns-azure \
    #     -d contrat.vinaitsolution.com -d '*.contrat.vinaitsolution.com'
    az containerapp hostname add   -g contrat-rg -n contrat-frontend --hostname '*.contrat.vinaitsolution.com'
    az containerapp ssl upload     -g contrat-rg -n contrat-frontend \
      --hostname '*.contrat.vinaitsolution.com' --certificate-file wildcard.pfx --certificate-password '<pwd>'
    ```
- **Ajouter un tenant** = créer le slug côté superadmin. **Aucune** reconfiguration DNS/TLS/infra
  tant qu'on reste sous `*.contrat.vinaitsolution.com`.

## 6. Sécurité / SaaS
- **Secrets** (DB, JWT) injectés via secrets Container App / Key Vault — jamais dans l'image.
- **Cookie** `Secure` + `SameSite` adaptés (HTTPS Azure) via `COOKIE_SECURE`/`COOKIE_SAMESITE` ;
  posé pour l'hôte courant ⇒ **session isolée par sous-domaine/tenant**.
- **CORS** : frontend et `/api` servis par le même sous-domaine (same-origin) ⇒ CORS non sollicité
  en prod ; `APP_CORS_ALLOWED_ORIGINS` sert surtout au dev.
- **SSO Entra ID** : profil `azure` + variables `AZURE_*` (voir
  [securite-authentification.md](securite-authentification.md)).
- **Dépôts Microsoft 365 / SAP** (SharePoint, OneDrive, Outlook, Teams, SAP) : profils `m365`/`sap`
  + secrets `M365_*`/`SAP_*` dans Key Vault. Configuration détaillée dans
  [integration-m365-sap.md](integration-m365-sap.md).
- **Multi-tenant** : isolation applicative par `TenantContext` (claim JWT `tenant`) ; à la
  connexion, vérification que le compte appartient bien au tenant du sous-domaine.
- **Observabilité** : logs → Log Analytics ; Actuator exposé côté backend.
- **Mise à l'échelle** : `minReplicas/maxReplicas` sur les Container Apps.
