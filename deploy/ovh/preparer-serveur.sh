#!/usr/bin/env bash
# Prépare le VPS pour deploy/ovh/deployer.sh. Rejouable : un .env.contrat existant n'est jamais réécrit.
#
#   deploy/ovh/preparer-serveur.sh [ancien .env]   # défaut : ~/contractuelle_contrat/.env.prod (installation d'origine)
#
# Écrit deploy/ovh/.env.contrat (mode 600, jamais versionné) :
#   - repris de l'ancien fichier : APP_CRYPTO_KEY et APP_CRYPTO_SALT (ils chiffrent les secrets M365 déjà en base : les
#     changer les rendrait illisibles), OLLAMA_*, APP_CORS_ALLOWED_ORIGINS, PUBLICATION_*, LOG_APP_LEVEL ;
#   - renouvelés s'ils manquent ou gardent une valeur par défaut (« CHANGE… ») : JWT_SECRET (les sessions ouvertes
#     expirent) et DB_PASSWORD (appliqué à PostgreSQL par deployer.sh, juste avant de démarrer la nouvelle version :
#     l'ancienne garde ainsi l'accès à la base jusqu'à son remplacement) ;
#   - APP_TENANTS_DEMO=true : la démonstration publique sème les organisations des appels d'offres et leurs comptes.
# Aucune valeur n'est affichée.
set -euo pipefail
ICI="$(cd "$(dirname "$0")" && pwd)"
CIBLE="$ICI/.env.contrat"
ANCIEN="${1:-$HOME/contractuelle_contrat/.env.prod}"

if [ -f "$CIBLE" ]; then echo "✓ $CIBLE existe déjà : inchangé"; exit 0; fi

lire() { [ -f "$ANCIEN" ] && grep -E "^$1=" "$ANCIEN" | head -1 | cut -d= -f2- || true; }
defaut() { [ -z "$1" ] || [[ "$1" =~ ^(CHANGE|changeme|change_me|secret|password) ]] || [ ${#1} -lt 24 ]; }
lire_ou() { local v; v="$(lire "$1")"; echo "${v:-$2}"; }
alea() { openssl rand -base64 "$1" | tr -d '/+=\n' | cut -c1-"$2"; }

JWT="$(lire JWT_SECRET)"; defaut "$JWT" && { JWT="$(alea 72 64)"; echo "↻ JWT_SECRET renouvelé"; }
BD="$(lire DB_PASSWORD)"; BD_CHANGE=0
defaut "$BD" && { BD="$(alea 48 32)"; BD_CHANGE=1; echo "↻ DB_PASSWORD renouvelé"; }
CLE="$(lire APP_CRYPTO_KEY)"; SEL="$(lire APP_CRYPTO_SALT)"
[ -n "$CLE" ] || CLE="$(alea 48 32)"
[ -n "$SEL" ] || SEL="$(openssl rand -hex 8)"

umask 077
cat > "$CIBLE" <<EOF
# Secrets de la démonstration contrat.vinaitsolution.com — mode 600, jamais versionné (deploy/ovh/env.contrat.exemple).
DB_PASSWORD=$BD
JWT_SECRET=$JWT
APP_CRYPTO_KEY=$CLE
APP_CRYPTO_SALT=$SEL
APP_CORS_ALLOWED_ORIGINS=$(lire_ou APP_CORS_ALLOWED_ORIGINS https://contrat.vinaitsolution.com)
OLLAMA_BASE_URL=$(lire_ou OLLAMA_BASE_URL http://vina_ollama:11434)
OLLAMA_MODEL=$(lire_ou OLLAMA_MODEL llama3.1)
PUBLICATION_DOSSIER_DEFAUT=$(lire_ou PUBLICATION_DOSSIER_DEFAUT Contrats)
PUBLICATION_SIMULATEUR_REPERTOIRE=/app/data/simulateur
LOG_APP_LEVEL=$(lire_ou LOG_APP_LEVEL INFO)
APP_TENANTS_DEMO=true
EOF
chmod 600 "$CIBLE"
echo "✓ $CIBLE écrit"

[ "$BD_CHANGE" = 1 ] && echo "  le mot de passe est appliqué à PostgreSQL par deployer.sh, au démarrage de la nouvelle version"
