#!/usr/bin/env bash
# Chaîne d'intégration continue des bacs à sable (GitHub Actions et Azure DevOps l'appellent tels quels) :
#  1. démarre les applications externes (ICP, pyHanko, Gotenberg, veraPDF, Mailpit, ClamAV, Ollama) ;
#  2. démarre le serveur branché dessus, avec les mêmes variables qu'en production, et le frontend de recette ;
#  3. exécute les scénarios e2e BS-01 à BS-08, puis les scénarios de validation et la contre-épreuve de révocation ;
#  4. exécute les scénarios BDD de tous les appels d'offres (bdd/) et vérifie qu'aucune exigence n'est sans scénario ;
#     puis les scénarios e2e d'interface par exigence (bdd/e2e/), avec le même contrôle de couverture ;
#  5. exécute les tests d'intrusion (securite/) : chaque point d'accès de l'API, OWASP Top 10, ZAP et Trivy.
# Sortie non nulle au premier écart ; journaux et rapports dans infra/bacs-a-sable/resultats/ et e2e/playwright-report/.
set -euo pipefail
RACINE=$(cd "$(dirname "$0")/../.." && pwd)
B="$RACINE/infra/bacs-a-sable"
R="$B/resultats"; mkdir -p "$R"
COMPOSE=(docker compose -f "$B/docker-compose.yml")
arreter() {
  [[ -n "${SERVEUR:-}" ]] && kill "$SERVEUR" 2>/dev/null || true
  # Vite tourne sous npm : on arrête ce qui écoute sur le port de recette.
  [[ -n "${FRONT:-}" ]] && { kill "$FRONT" 2>/dev/null; lsof -ti tcp:5175 -sTCP:LISTEN | xargs kill 2>/dev/null; } || true
  "${COMPOSE[@]}" logs --no-color icp pyhanko > "$R/services.log" 2>&1 || true
  [[ "${GARDER_BACS:-}" == "1" ]] || "${COMPOSE[@]}" down >/dev/null 2>&1 || true
}
trap arreter EXIT
attendre() { for _ in $(seq 1 "${2:-120}"); do curl -sf "$1" >/dev/null && return 0; sleep 2; done; echo "Injoignable : $1"; return 1; }

echo "== Applications externes"
mkdir -p "$B/icp/etat"
# L'ICP s'exécute sous l'utilisateur 10001 (sans privilège) : le dossier d'état monté lui appartient. Sur un exécuteur Linux,
# il appartiendrait sinon à l'utilisateur de la CI, et l'initialisation ne pourrait pas y écrire.
docker run --rm -v "$B/icp/etat:/etat" alpine:3.20 chown -R 10001:10001 /etat
"${COMPOSE[@]}" up -d --build --wait icp
# pyHanko partage l'espace réseau de l'ICP : il est recréé pour suivre un éventuel redémarrage de celle-ci.
"${COMPOSE[@]}" up -d --force-recreate pyhanko
# ClamAV démarré tôt : le chargement de ses signatures prend une à deux minutes.
"${COMPOSE[@]}" up -d conversion verapdf courriel antivirus
for u in http://localhost:18093/sante http://localhost:18030/health http://localhost:18092/api/info http://localhost:18025/api/v1/info; do attendre "$u"; done

echo "== Serveur branché sur les bacs à sable"
export SERVER_PORT=8100
export CLM_CACHET_MAGASIN="$B/icp/etat/cachet.p12" CLM_CACHET_MOT_DE_PASSE="$(cat "$B/icp/etat/mot-de-passe.txt")"
export CLM_CACHET_TSA_URL=http://localhost:18090/tsa CLM_CONVERSION_MODE=service CLM_CONVERSION_URL=http://localhost:18030
export CLM_SIGNATURE_FOURNISSEUR=cachet NOTIFICATION_COURRIEL_MODE=smtp MAIL_HOST=localhost MAIL_PORT=11025
export ANTIVIRUS_MODE=clamav ANTIVIRUS_CLAMAV_HOTE=localhost ANTIVIRUS_CLAMAV_PORT=13310
# IA générative (Ollama du bac à sable) : petit modèle, tiré avant le démarrage du serveur.
export OLLAMA_BASE_URL=http://localhost:11435 OLLAMA_MODEL=contrat-banc OLLAMA_LONGUEUR_MAX=120
"${COMPOSE[@]}" up -d ia
for _ in $(seq 1 60); do docker exec iq-bac-ia ollama list >/dev/null 2>&1 && break; sleep 2; done
docker exec iq-bac-ia ollama pull qwen2.5:0.5b >/dev/null
# Modèle du banc : 4 fils de calcul. Laissé à son défaut (tous les processeurs), le calcul s'étouffe sous la charge des
# autres services et tombe à environ un jeton par seconde.
docker exec iq-bac-ia sh -c 'printf "FROM qwen2.5:0.5b\nPARAMETER num_thread 4\n" > /tmp/Modelfile && ollama create contrat-banc -f /tmp/Modelfile' >/dev/null
# clamd ne parle pas HTTP : on attend qu'il réponde « PONG » (signatures chargées).
clamd_pret=0
for _ in $(seq 1 150); do [[ "$( (printf 'PING\n'; sleep 1) | nc localhost 13310 2>/dev/null)" == PONG* ]] && { clamd_pret=1; break; }; sleep 2; done
[[ $clamd_pret -eq 1 ]] || { echo "ClamAV ne répond pas (port 13310)."; "${COMPOSE[@]}" logs --no-color --tail 40 antivirus; exit 1; }
# Jar lancé directement : « mvn spring-boot:run » délègue à un processus Java qu'arrêter Maven ne tue pas.
mvn -B -q -f "$RACINE/backend/pom.xml" -DskipTests package
JAR=$(ls "$RACINE"/backend/target/contrat-backend-*.jar | grep -v original | head -1)
java -jar "$JAR" > "$R/serveur.log" 2>&1 & SERVEUR=$!
attendre http://localhost:8100/api/health 180
sleep 20   # amorçage des comptes et organisations de démonstration, après le démarrage du serveur web

echo "== Frontend de recette"
[[ -d "$RACINE/frontend/node_modules" ]] || npm --prefix "$RACINE/frontend" ci
(cd "$RACINE/frontend" && npm run dev -- --config vite.tour.config.js > "$R/frontend.log" 2>&1) & FRONT=$!
attendre http://localhost:5175 60

echo "== Scénarios e2e BS-01 à BS-08"
[[ -d "$RACINE/e2e/node_modules" ]] || { npm --prefix "$RACINE/e2e" ci; (cd "$RACINE/e2e" && npx playwright install --with-deps chromium); }
(cd "$RACINE/e2e" && E2E_FRONTEND_URL=http://localhost:5175 E2E_BACKEND_URL=http://localhost:8100 \
  npx playwright test -c playwright.iso.config.ts views/clm.bacs-a-sable.spec.ts)

echo "== Scénarios de validation"
"$B/valider.sh"
"$B/valider-signature.sh"
"$B/valider-revocation.sh"

echo "== Scénarios BDD des exigences (Lévis, Trois-Rivières, IQ, CHU)"
[[ -d "$RACINE/bdd/node_modules" ]] || { npm --prefix "$RACINE/bdd" ci; (cd "$RACINE/bdd" && npx playwright install --with-deps chromium); }
export BDD_FRONTEND_URL=http://localhost:5175 BDD_COURRIEL_URL=http://localhost:18025/api/v1
(cd "$RACINE/bdd" && npm run couverture && npx bddgen)
# Les résultats sont documentés (docs/scenarios-bdd/) même quand un scénario échoue, avant de rendre le verdict.
if (cd "$RACINE/bdd" && npx playwright test); then bdd=0; else bdd=$?; fi
(cd "$RACINE/bdd" && node outils/documenter.mjs)
[[ $bdd -eq 0 ]] || { echo "Scénarios BDD : écart relevé (bdd/playwright-report/)."; exit "$bdd"; }

echo "== Scénarios e2e d'interface par exigence (bdd/e2e/)"
(cd "$RACINE/bdd" && npm run e2e:couverture && node outils/inventaire-ecrans.mjs)
if (cd "$RACINE/bdd" && npx playwright test -c playwright.e2e.config.ts); then e2e=0; else e2e=$?; fi
(cd "$RACINE/bdd" && node outils/documenter-e2e.mjs)
[[ $e2e -eq 0 ]] || { echo "Scénarios e2e : écart relevé (bdd/playwright-report-e2e/)."; exit "$e2e"; }
# Chaque écran ouvert et chaque fonctionnalité d'écran (opération d'API tirée du code) déclenchée depuis l'interface.
(cd "$RACINE/bdd" && npm run e2e:ecrans)

echo "== Tests d'intrusion (securite/)"
# Le rapport docs/securite/rapport-tests-intrusion.md est régénéré même en cas d'écart ; le verdict suit.
if SECURITE_API_URL=http://localhost:8100 "$RACINE/securite/lancer.sh"; then securite=0; else securite=$?; fi
[[ $securite -eq 0 ]] || { echo "Tests d'intrusion : écart relevé (docs/securite/rapport-tests-intrusion.md)."; exit "$securite"; }
echo "Bacs à sable, scénarios BDD et e2e, tests d'intrusion : tous les contrôles sont conformes."
