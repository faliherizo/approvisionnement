#!/usr/bin/env bash
# Crée .env (non versionné) avec des secrets aléatoires propres à ce poste, lus par docker-compose.yml :
# secret JWT (512 bits), clé et sel de chiffrement des secrets des organisations, mot de passe PostgreSQL.
# Sans effet si .env existe déjà : régénérer ces secrets invaliderait les sessions et les secrets chiffrés en base.
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -f .env ]]; then echo ".env existe déjà : secrets conservés."; exit 0; fi
umask 077
{
  echo "JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')"
  echo "APP_CRYPTO_KEY=$(openssl rand -base64 32 | tr -d '\n')"
  echo "APP_CRYPTO_SALT=$(openssl rand -hex 8)"
  echo "DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n/+=')"
} > .env
echo ".env créé (droits 600). Ne pas le versionner ni le partager."

# Installation existante : le volume PostgreSQL garde le mot de passe de sa création. On l'aligne sur le nouveau.
MDP=$(sed -n 's/^DB_PASSWORD=//p' .env)
if docker ps --format '{{.Names}}' 2>/dev/null | grep -qx contrat-db; then
  docker exec contrat-db psql -U contrat -d contrat -c "ALTER USER contrat PASSWORD '$MDP';" >/dev/null \
    && echo "Mot de passe de la base existante mis à jour."
elif docker volume ls --format '{{.Name}}' 2>/dev/null | grep -q dbdata; then
  echo "Une base existante a été trouvée (volume dbdata) mais n'est pas démarrée : lancez « docker compose up -d db »,"
  echo "puis relancez ce script après avoir supprimé .env, pour aligner son mot de passe."
fi
