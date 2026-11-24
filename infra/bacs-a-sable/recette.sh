#!/usr/bin/env bash
# Plateforme de recette locale, détachée : les bacs à sable, le serveur :8100 branché dessus (mêmes variables que ci.sh) et
# le frontend de recette :5175. Pour jouer la BDD (bdd/) et les scénarios e2e (bdd/e2e/) à la main, sans tout le ci.sh.
#
#   infra/bacs-a-sable/recette.sh            # (re)démarre serveur et frontend ; journaux dans resultats/
#   infra/bacs-a-sable/recette.sh --arreter  # arrête serveur et frontend (les bacs à sable restent)
set -euo pipefail
RACINE=$(cd "$(dirname "$0")/../.." && pwd)
B="$RACINE/infra/bacs-a-sable"
R="$B/resultats"; mkdir -p "$R"
JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || true)}
export JAVA_HOME

arreter() {
  lsof -ti tcp:8100 -sTCP:LISTEN | xargs kill 2>/dev/null || true
  lsof -ti tcp:5175 -sTCP:LISTEN | xargs kill 2>/dev/null || true
  sleep 2
}
# Lancé dans sa propre session (setsid) : ni la fin du terminal ni celle de l'appelant n'arrêtent le processus. Toujours
# appelé en arrière-plan (&) : exec remplace le sous-shell, qui ne garde ainsi aucune sortie de l'appelant ouverte.
detacher() { exec perl -MPOSIX -e 'POSIX::setsid(); exec @ARGV' "$@"; }
attendre() { for _ in $(seq 1 "${2:-120}"); do curl -s -o /dev/null "$1" && return 0; sleep 2; done; echo "Injoignable : $1"; return 1; }

arreter
[[ "${1:-}" == "--arreter" ]] && exit 0

docker compose -f "$B/docker-compose.yml" up -d >/dev/null
for _ in $(seq 1 90); do [[ "$( (printf 'PING\n'; sleep 1) | nc localhost 13310 2>/dev/null)" == PONG* ]] && break; sleep 2; done
# IA générative : le petit modèle du bac à sable, tiré une fois (le volume ia-modeles le garde).
docker exec iq-bac-ia ollama pull smollm2:135m >/dev/null
# Modèle du banc : smollm2 (135 M de paramètres) à 4 fils de calcul. Laissé à son défaut (tous les processeurs), le
# calcul s'étouffe sous la charge des autres services et tombe à environ un jeton par seconde.
docker exec iq-bac-ia sh -c 'printf "FROM smollm2:135m\nPARAMETER num_thread 4\n" > /tmp/Modelfile && ollama create contrat-banc -f /tmp/Modelfile' >/dev/null

(cd "$RACINE/backend" && mvn -q -o -DskipTests -Djacoco.skip=true package)
JAR=$(ls "$RACINE"/backend/target/contrat-backend-*.jar | grep -v original | head -1)
# Le serveur tourne sur une copie : un « mvn verify » pendant la recette réécrit target/ sans casser le serveur en marche.
cp "$JAR" "$R/serveur.jar"; JAR="$R/serveur.jar"
SERVER_PORT=8100 CLM_PORTAIL_URL=http://localhost:5175/portail \
  CLM_CACHET_MAGASIN="$B/icp/etat/cachet.p12" CLM_CACHET_MOT_DE_PASSE="$(cat "$B/icp/etat/mot-de-passe.txt")" \
  CLM_CACHET_TSA_URL=http://localhost:18090/tsa CLM_CONVERSION_MODE=service CLM_CONVERSION_URL=http://localhost:18030 \
  CLM_SIGNATURE_FOURNISSEUR=cachet NOTIFICATION_COURRIEL_MODE=smtp MAIL_HOST=localhost MAIL_PORT=11025 \
  ANTIVIRUS_MODE=clamav ANTIVIRUS_CLAMAV_HOTE=localhost ANTIVIRUS_CLAMAV_PORT=13310 \
  OLLAMA_BASE_URL=http://localhost:11435 OLLAMA_MODEL=contrat-banc OLLAMA_LONGUEUR_MAX=48 \
  detacher "$JAVA_HOME/bin/java" -jar "$JAR" < /dev/null > "$R/serveur.log" 2>&1 &
(cd "$RACINE/frontend" && detacher npx vite --config vite.tour.config.js < /dev/null > "$R/frontend.log" 2>&1 &)
attendre http://localhost:8100/api/health 180
attendre http://localhost:5175 60
echo "✓ recette prête : serveur :8100, frontend :5175 — journaux $R/{serveur,frontend}.log"
