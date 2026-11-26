#!/usr/bin/env bash
#
# Crée les comptes d'essai du profil cible (un par utilisateur virtuel) par l'API d'administration, comme le ferait un
# administrateur de l'organisation, et écrit le fichier de comptes lu par JMeter (email,motDePasse).
#
#   BASE_URL            serveur visé (localhost seulement : l'amorçage écrit dans la base)
#   ADMIN_EMAIL         administrateur de l'organisation d'essai
#   ADMIN_MOT_DE_PASSE  son mot de passe
#   MOT_DE_PASSE        mot de passe commun des comptes d'essai (généré par l'appelant, jamais versionné)
#   NOMBRE              nombre de comptes (défaut 500)
#   SORTIE              fichier de comptes (défaut data/cible/comptes.csv, ignoré par Git)
#   DOMAINE             domaine des courriels (défaut iq.example)
#
# Codes de sortie : 1 connexion de l'administrateur refusée (à réessayer pendant l'amorçage), 2 paramètre manquant,
# 3 hôte distant refusé, 4 création d'un compte refusée.
#
set -euo pipefail
cd "$(dirname "$0")/.."

: "${BASE_URL:=http://localhost:8080}"
: "${NOMBRE:=500}"
: "${SORTIE:=data/cible/comptes.csv}"
: "${DOMAINE:=iq.example}"
: "${ROLE:=REDACTEUR}"
[ -n "${ADMIN_EMAIL:-}" ] && [ -n "${ADMIN_MOT_DE_PASSE:-}" ] && [ -n "${MOT_DE_PASSE:-}" ] \
  || { echo "✗ ADMIN_EMAIL, ADMIN_MOT_DE_PASSE et MOT_DE_PASSE sont obligatoires." >&2; exit 2; }

# Même garde-fou que run.sh : l'amorçage crée des comptes, il ne vise jamais un environnement distant.
case "$BASE_URL" in
  http://localhost:*|http://127.0.0.1:*) ;;
  *) echo "✗ Amorçage refusé hors de localhost : $BASE_URL" >&2; exit 3 ;;
esac

session=$(mktemp)
trap 'rm -f "$session"' EXIT

json() { python3 -c 'import json,sys; print(json.dumps(dict(zip(sys.argv[1::2], sys.argv[2::2]))))' "$@"; }

code=$(curl -sS -o /dev/null -w '%{http_code}' -c "$session" -H 'Content-Type: application/json' \
  -d "$(json email "$ADMIN_EMAIL" motDePasse "$ADMIN_MOT_DE_PASSE")" "$BASE_URL/api/auth/login")
[ "$code" = 200 ] || { echo "✗ Connexion de l'administrateur refusée (HTTP $code)." >&2; exit 1; }

mkdir -p "$(dirname "$SORTIE")"
echo "email,motDePasse" > "$SORTIE"
chmod 600 "$SORTIE"

creer() {  # $1 = numéro du compte
  local email corps code
  email=$(printf 'essai-charge-%03d@%s' "$1" "$DOMAINE")
  corps=$(python3 -c 'import json,sys; print(json.dumps({"email": sys.argv[1], "nom": "Essai de charge " + sys.argv[2],
    "direction": "Essai de charge TI-12", "motDePasse": sys.argv[3], "roles": [sys.argv[4]], "actif": True}))' \
    "$email" "$1" "$MOT_DE_PASSE" "$ROLE")
  code=$(curl -sS -o /dev/null -w '%{http_code}' -b "$session" -H 'Content-Type: application/json' \
    -d "$corps" "$BASE_URL/api/admin/users")
  [ "$code" = 200 ] || { echo "✗ Création de $email refusée (HTTP $code)." >&2; exit 4; }
  echo "$email,$MOT_DE_PASSE" >> "$SORTIE"
}

for i in $(seq 1 "$NOMBRE"); do creer "$i"; done
echo "✓ $NOMBRE comptes d'essai créés ($ROLE) — $SORTIE"
