#!/usr/bin/env bash
# Déploie une version de la plateforme contractuelle sur le VPS — appelé par .github/workflows/deployer-contrat.yml, ou
# à la main.
#
#   deploy/ovh/deployer.sh <version>          # les images contrat/{backend,web}:<version> doivent être chargées
#   deploy/ovh/deployer.sh --retour-arriere   # revient à la version précédente, base comprise
#
# 1. sauvegarde la base (pg_dump, format personnalisé) — ~/sauvegardes/contrat, les 10 dernières ;
# 2. aligne le mot de passe de la base sur .env.contrat, puis démarre la version (Liquibase applique ses migrations) ;
# 3. attend que le serveur Java ET le relais nginx du frontend répondent /api/health ;
# 4. en cas d'échec : restaure la base sauvegardée — une migration appliquée ne se défait pas par un retour d'image —,
#    redémarre la version précédente, et sort en erreur ;
# 5. ne garde que les images courante et précédente.
set -euo pipefail
ICI="$(cd "$(dirname "$0")" && pwd)"
cd "$ICI"
SAUVEGARDES="$HOME/sauvegardes/contrat"
DELAI_SANTE_S=300
PROJET=contractuelle_contrat

[ -f .env.contrat ] || { echo "❌ deploy/ovh/.env.contrat absent — lancer deploy/ovh/preparer-serveur.sh"; exit 2; }
PRECEDENTE="$(cat .version-courante 2>/dev/null || true)"

if [ "${1:-}" = "--retour-arriere" ]; then
  VERSION="$(cat .version-precedente 2>/dev/null || true)"
  [ -n "$VERSION" ] || { echo "❌ aucune version précédente connue"; exit 2; }
else
  VERSION="${1:?version attendue (sha court)}"
fi

composer() { CONTRAT_VERSION="$1" docker compose -p "$PROJET" --env-file .env.contrat -f docker-compose.yml "${@:2}"; }

for image in backend web; do
  docker image inspect "contrat/$image:$VERSION" >/dev/null 2>&1 || { echo "❌ image contrat/$image:$VERSION absente (docker load)"; exit 2; }
done

# Le dossier des dépôts simulés appartient à l'utilisateur du serveur Java (uid 10001), sans droits de root sur l'hôte.
mkdir -p donnees/simulateur
docker run --rm -v "$ICI/donnees:/d" alpine chown -R 10001:10001 /d/simulateur

ARCHIVE=""
sauvegarder() {
  docker inspect contrat-db >/dev/null 2>&1 || return 0
  [ "$(docker inspect -f '{{.State.Running}}' contrat-db)" = "true" ] || return 0
  mkdir -p "$SAUVEGARDES"
  ARCHIVE="$SAUVEGARDES/contrat-$(date +%Y%m%d-%H%M%S)-${PRECEDENTE:-initiale}.dump"
  # pg_dump lit un instantané cohérent : le serveur n'a pas à être arrêté.
  docker exec contrat-db pg_dump -U contrat -d contrat -Fc > "$ARCHIVE"
  chmod 600 "$ARCHIVE"
  ls -1t "$SAUVEGARDES"/contrat-*.dump | tail -n +11 | xargs -r rm -f
  echo "✓ sauvegarde : $ARCHIVE ($(du -h "$ARCHIVE" | cut -f1))"
}

restaurer() {
  [ -n "$ARCHIVE" ] || return 0
  echo "↩ restauration de la base : $ARCHIVE"
  docker stop contrat-backend >/dev/null 2>&1 || true
  docker exec -i contrat-db pg_restore -U contrat -d contrat --clean --if-exists --no-owner < "$ARCHIVE"
}

# Le mot de passe de .env.contrat fait foi : POSTGRES_PASSWORD n'agit qu'à la création du volume, il est donc appliqué
# ici (sans effet s'il n'a pas changé). Il passe par l'entrée standard de psql : ni liste des processus, ni journal.
aligner_mot_de_passe_base() {
  docker inspect -f '{{.State.Running}}' contrat-db 2>/dev/null | grep -q true || return 0
  local mdp; mdp="$(grep -E '^DB_PASSWORD=' .env.contrat | cut -d= -f2-)"
  printf "ALTER USER contrat WITH PASSWORD '%s';\n" "${mdp//\'/\'\'}" | docker exec -i contrat-db psql -q -U contrat -d contrat
}

en_sante() {
  local fin=$((SECONDS + DELAI_SANTE_S))
  while [ $SECONDS -lt $fin ]; do
    if [ "$(docker inspect -f '{{.State.Health.Status}}' contrat-backend 2>/dev/null)" = "healthy" ] \
      && docker exec contrat-frontend wget -qO /dev/null http://127.0.0.1:8080/api/health 2>/dev/null; then
      return 0
    fi
    sleep 5
  done
  return 1
}

echo "▶ déploiement de $VERSION (précédente : ${PRECEDENTE:-aucune})"
sauvegarder
aligner_mot_de_passe_base
composer "$VERSION" up -d --remove-orphans

if ! en_sante; then
  echo "❌ $VERSION ne répond pas après ${DELAI_SANTE_S} s — journal du serveur :"
  docker logs --tail 80 contrat-backend 2>&1 || true
  if [ -n "$PRECEDENTE" ] && [ "$PRECEDENTE" != "$VERSION" ]; then
    restaurer
    echo "↩ retour à $PRECEDENTE"
    composer "$PRECEDENTE" up -d --remove-orphans
    en_sante && echo "✓ $PRECEDENTE rétablie" || echo "❌ $PRECEDENTE ne répond pas non plus"
  fi
  exit 1
fi

if [ "$VERSION" != "$PRECEDENTE" ]; then
  [ -n "$PRECEDENTE" ] && echo "$PRECEDENTE" > .version-precedente
  echo "$VERSION" > .version-courante
fi
GARDER="$(cat .version-courante) $(cat .version-precedente 2>/dev/null || true)"
for ref in $(docker images --format '{{.Repository}}:{{.Tag}}' | grep '^contrat/'); do
  [[ " $GARDER " == *" ${ref##*:} "* ]] || docker rmi "$ref" >/dev/null 2>&1 || true
done
echo "✓ $VERSION en service — https://contrat.vinaitsolution.com"
