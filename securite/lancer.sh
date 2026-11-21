#!/usr/bin/env bash
# Tests d'intrusion complets contre un serveur en marche (SECURITE_API_URL, défaut http://localhost:8100) :
#  1. tests d'intrusion automatisés (Playwright) sur tout l'inventaire OpenAPI ;
#  2. balayage dynamique OWASP ZAP de l'API (passif + actif), authentifié ;
#  3. analyse Trivy du dépôt : dépendances vulnérables, secrets, configuration (Dockerfile, Terraform, Kubernetes) ;
#  4. npm audit des dépendances de production du frontend ;
#  5. rapport Markdown docs/securite/rapport-tests-intrusion.md.
# Sortie non nulle si un test d'intrusion échoue ou si un outil relève une vulnérabilité HAUTE ou CRITIQUE non acceptée.
set -uo pipefail
ICI=$(cd "$(dirname "$0")" && pwd)
RACINE=$(cd "$ICI/.." && pwd)
R="$ICI/resultats"; mkdir -p "$R"
API="${SECURITE_API_URL:-http://localhost:8100}"
API_DOCKER="${API/localhost/host.docker.internal}"
statut=0

echo "== 1. Tests d'intrusion automatisés"
[[ -d "$ICI/node_modules" ]] || (cd "$ICI" && npm ci)
(cd "$ICI" && npx playwright test) || statut=1

echo "== 2. OWASP ZAP (API, authentifié)"
JETON=$(curl -s -i -X POST "$API/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"admin@intrusion-a.example","motDePasse":"Intrusion!2026x"}' | sed -n 's/^[Ss]et-[Cc]ookie: ACCESS_TOKEN=\([^;]*\).*/\1/p')
docker run --rm -v "$R:/zap/wrk:rw" zaproxy/zap-stable:2.16.1 zap-api-scan.py -t "$API_DOCKER/v3/api-docs" -f openapi \
  -J zap.json -r zap.html -I -z "-config scanner.maxScanDurationInMins=25 \
  -config globalexcludeurl.url_list.url(0).regex=.*/api/auth/logout.* -config globalexcludeurl.url_list.url(0).enabled=true \
  -config globalexcludeurl.url_list.url(0).description=deconnexion \
  -config replacer.full_list(0).description=session -config replacer.full_list(0).enabled=true \
  -config replacer.full_list(0).matchtype=REQ_HEADER -config replacer.full_list(0).matchstr=Cookie \
  -config replacer.full_list(0).regex=false -config replacer.full_list(0).replacement=ACCESS_TOKEN=$JETON" > "$R/zap.log" 2>&1 || true

echo "== 3. Trivy (dépendances, secrets, configuration)"
docker run --rm -v "$RACINE:/src:ro" -v "$R:/sortie" -v trivy-cache:/root/.cache aquasec/trivy:0.58.1 fs \
  --scanners vuln,secret,misconfig --format json -o /sortie/trivy.json \
  --skip-dirs node_modules --skip-dirs frontend/node_modules --skip-dirs bdd/node_modules --skip-dirs e2e/node_modules \
  --skip-dirs securite/node_modules --skip-dirs backend/target --skip-dirs backend/data --skip-dirs perf \
  --skip-dirs '**/.terraform' --skip-dirs infra/bacs-a-sable/icp/etat --skip-dirs infra/bacs-a-sable/resultats --skip-dirs securite/resultats /src > "$R/trivy.log" 2>&1 || true

echo "== 4. npm audit (frontend, dépendances de production)"
(cd "$RACINE/frontend" && npm audit --omit=dev --json > "$R/npm-audit.json" 2>/dev/null) || true

echo "== 5. Rapport"
(cd "$ICI" && node outils/rapport.mjs) || statut=1
exit $statut
