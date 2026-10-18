# Multi-tenant par sous-domaine (`slug.contrat.vinaitsolution.com`)

Chaque organisation (tenant) accède à l'application via **son propre sous-domaine**, dérivé du
**slug** créé par le superadmin. La page de connexion **ne demande pas** le tenant : il est déduit
du sous-domaine.

```
https://levis.contrat.vinaitsolution.com        → tenant « levis »
https://trois-rivieres.contrat.vinaitsolution.com → tenant « trois-rivieres »
https://contrat.vinaitsolution.com               → domaine nu : vitrine + connexion générique (branding VINA)
```

## 1. Comment ça marche

1. **Le superadmin crée le tenant** (console superadmin) : `slug`, titre, thème (couleurs, logo).
   Le slug est unique (`existsBySlug`). Aucune action DNS n'est nécessaire par tenant : le
   **wildcard** (`*.contrat…`) couvre déjà tous les slugs présents et futurs.
2. **Accès** : l'organisation ouvre `https://<slug>.contrat.vinaitsolution.com`.
3. **Résolution du tenant (frontend)** : `slugTenant()` (`src/theme.js`) lit le premier label du
   nom d'hôte. Il charge le branding public du tenant via `GET /api/tenants/{slug}/public`
   (titre + thème) et l'applique. Les labels réservés (`www`, `app`, `contrat`, `accueil`,
   `localhost`) ⇒ pas de tenant ⇒ branding VINA par défaut.
4. **Connexion** : la page `/login` n'affiche **aucun sélecteur d'établissement**. Elle envoie le
   `slug` du sous-domaine à `POST /api/auth/login`.
5. **Vérification (backend)** : le tenant réel d'un compte est porté par son **JWT**
   (`genererToken(email, roles, tenantId)`). À la connexion, si un `slug` est fourni et que le
   compte n'est **pas** superadmin, le backend vérifie que `user.tenantId` correspond bien au
   tenant du slug ; sinon la connexion est **refusée** (« Ce compte n'appartient pas à
   l'établissement … »). Un slug absent/inconnu ⇒ aucun contrôle (dev / domaine nu).
6. **Isolation des données** : inchangée et déjà en place — `TenantContext` (alimenté par le claim
   `tenant` du JWT) filtre toutes les requêtes. Le sous-domaine ne fait qu'**aiguiller et vérifier**
   la connexion ; il ne donne jamais accès aux données d'un autre tenant.

> ✅ Aucune configuration par tenant hormis la **création du slug** par le superadmin. Le wildcard
> DNS + le certificat wildcard rendent le nouveau sous-domaine immédiatement fonctionnel.

## 2. DNS — un seul enregistrement wildcard

En plus de `contrat` (domaine nu), ajouter un enregistrement **wildcard** pointant vers la même
cible que l'application :

| Type | Sous-domaine | Cible | Rôle |
|------|-------------|-------|------|
| A (ou CNAME) | `contrat` | IP/nom du service | Domaine nu (vitrine + connexion générique) |
| A (ou CNAME) | `*.contrat` | IP/nom du service | **Tous les tenants** (`levis`, `trois-rivieres`, …) |

```bash
dig levis.contrat.vinaitsolution.com +short          # doit résoudre vers la même cible
dig trois-rivieres.contrat.vinaitsolution.com +short  # idem (grâce au wildcard)
```

## 3. Certificat TLS — wildcard obligatoire

Un certificat pour `contrat.vinaitsolution.com` **ne couvre pas** `levis.contrat…`. Il faut un
**certificat wildcard** `*.contrat.vinaitsolution.com` (couvre un seul niveau de sous-domaine).

- **Let's Encrypt** : le wildcard exige le challenge **DNS-01** (le HTTP-01/webroot ne peut pas
  valider un wildcard). Voir les commandes par plateforme dans `deploiement-azure.md` (Azure) et le
  guide OVH.
- Inclure **les deux** noms sur le même certificat : `-d contrat.vinaitsolution.com -d
  '*.contrat.vinaitsolution.com'`.

## 4. Reverse proxy (Nginx) — `server_name` wildcard

Le bloc `server { listen 443 … }` du frontend accepte le domaine nu **et** le wildcard ; le reste
de la configuration est inchangé (proxy vers `contrat-frontend`, timeout étendu sur `/api/ia/`) :

```nginx
server {
    listen 443 ssl http2;
    server_name contrat.vinaitsolution.com *.contrat.vinaitsolution.com;   # ← wildcard ajouté

    ssl_certificate     /etc/letsencrypt/live/contrat.vinaitsolution.com/fullchain.pem;  # cert wildcard
    ssl_certificate_key /etc/letsencrypt/live/contrat.vinaitsolution.com/privkey.pem;
    # … (identique : location /api/ia/ timeout 300s, location / → contrat-frontend:80)
}
```

Le bloc HTTP `:80` (redirection + ACME) doit lui aussi inclure `*.contrat.vinaitsolution.com` dans
son `server_name`.

## 5. Cookie de session et CORS

- Le cookie `ACCESS_TOKEN` (httpOnly, `Secure`, `SameSite=Lax`) est posé pour l'**hôte courant**
  (le sous-domaine du tenant) ⇒ **isolation de session par tenant** (le cookie de `levis` n'est pas
  envoyé à `trois-rivieres`).
- Frontend et `/api` sont servis par le **même sous-domaine** (Nginx) ⇒ requêtes **same-origin**,
  CORS non sollicité en production. `APP_CORS_ALLOWED_ORIGINS` ne sert qu'au dev.

## 6. Points de configuration (récapitulatif)

| Élément | Valeur |
|---------|--------|
| DNS | ajouter `*.contrat` (wildcard) en plus de `contrat` |
| TLS | certificat **wildcard** `*.contrat.vinaitsolution.com` (DNS-01) + `contrat.vinaitsolution.com` |
| Nginx | `server_name contrat.vinaitsolution.com *.contrat.vinaitsolution.com;` (blocs `:80` et `:443`) |
| Superadmin | crée le `slug` du tenant → sous-domaine immédiatement disponible |
| Backend | vérification slug↔compte à la connexion (déjà implémentée) ; aucune variable à ajouter |
| Frontend | résolution du sous-domaine (déjà implémentée : `slugTenant()`), plus de sélecteur de tenant |

> ℹ️ **Ajouter un tenant en production** = créer le slug côté superadmin. Aucune reconfiguration
> DNS, TLS ou Nginx tant qu'on reste sur `*.contrat.vinaitsolution.com`. Pour un **domaine propre**
> au client (ex. `contrat.ville-x.ca`), ajouter un enregistrement DNS, un certificat et un
> `server_name` dédiés (voir guide OVH, option « certificats séparés »).
