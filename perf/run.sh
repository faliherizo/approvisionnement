#!/usr/bin/env bash
#
# Lance le plan de charge JMeter en mode non-graphique, produit un rapport HTML,
# puis VALIDE les SLO de latence (P95/P99) et de taux d'erreur (cf. SLI-SLO-SLA.md).
# Sortie non nulle si un objectif est dépassé — utilisable comme porte de performance en CI.
#
set -euo pipefail
cd "$(dirname "$0")"

: "${JAVA_HOME:=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# --- Paramètres de charge (surchargeables par variables d'environnement) ---
PLAN="${PLAN:-contrat-dossiers-load.jmx}"
THREADS="${THREADS:-20}"
RAMPUP="${RAMPUP:-60}"
LOOPS="${LOOPS:-5}"
BASE_HOST="${BASE_HOST:-localhost}"
BASE_PORT="${BASE_PORT:-8080}"
BASE_PROTOCOL="${BASE_PROTOCOL:-http}"

# --- SLO à valider (ms, %) ---
SLO_READ_P95="${SLO_READ_P95:-800}"
SLO_READ_P99="${SLO_READ_P99:-1500}"
SLO_WRITE_P95="${SLO_WRITE_P95:-1500}"
SLO_WRITE_P99="${SLO_WRITE_P99:-2500}"
SLO_ERROR_PCT="${SLO_ERROR_PCT:-0.1}"

rm -rf results
mkdir -p results

echo "▶ JMeter [${PLAN}] — ${THREADS} threads, montée ${RAMPUP}s, ${LOOPS} boucles → ${BASE_PROTOCOL}://${BASE_HOST}:${BASE_PORT}"
jmeter -n -t "$PLAN" \
  -l results/results.jtl \
  -e -o results/rapport \
  -Jthreads="$THREADS" -Jrampup="$RAMPUP" -Jloops="$LOOPS" \
  -JBASE_HOST="$BASE_HOST" -JBASE_PORT="$BASE_PORT" -JBASE_PROTOCOL="$BASE_PROTOCOL" \
  -j results/jmeter.log

echo ""
echo "▶ Validation des SLO (nearest-rank sur results/results.jtl)"

# 1) Éclate le JTL en latences par catégorie + compte total/erreurs (awk portable : sans asort).
awk -F',' '
  BEGIN { total = 0; errors = 0 }
  NR == 1 { for (i = 1; i <= NF; i++) col[$i] = i; next }
  {
    total++
    if ($col["success"] == "false") errors++
    e = $col["elapsed"] + 0; lbl = $col["label"]
    # Lecture = GET, ou POST idempotent de consultation (login, analyse assistant, recherche).
    if (lbl ~ /^GET /) print e > "results/read.txt"
    else if (lbl ~ /(login|analyser|search)$/) print e > "results/read.txt"
    else if (lbl ~ /^(POST|PUT|DELETE) /) print e > "results/write.txt"
  }
  END { print total, errors > "results/counts.txt" }
' results/results.jtl

# 2) Percentile par rang le plus proche via tri numérique.
pct() {  # $1 = fichier de latences, $2 = percentile
  local f="$1" p="$2" n idx
  [ -s "$f" ] || { echo 0; return; }
  n=$(wc -l < "$f")
  idx=$(awk -v n="$n" -v p="$p" 'BEGIN { i = int(p/100*n + 0.9999); print (i<1?1:(i>n?n:i)) }')
  sort -n "$f" | sed -n "${idx}p"
}

read TOTAL ERRORS < results/counts.txt
R95=$(pct results/read.txt 95);  R99=$(pct results/read.txt 99)
W95=$(pct results/write.txt 95); W99=$(pct results/write.txt 99)
ERRPCT=$(awk -v e="$ERRORS" -v t="$TOTAL" 'BEGIN { printf "%.3f", (t>0 ? e/t*100 : 0) }')

echo "  Échantillons: ${TOTAL} — erreurs: ${ERRORS} (${ERRPCT}%)"
echo "  Lecture  P95=${R95} ms (SLO ${SLO_READ_P95})  P99=${R99} ms (SLO ${SLO_READ_P99})"
echo "  Écriture P95=${W95} ms (SLO ${SLO_WRITE_P95})  P99=${W99} ms (SLO ${SLO_WRITE_P99})"

FAIL=0
gt() { awk -v a="$1" -v b="$2" 'BEGIN { exit !(a > b) }'; }
gt "$R95" "$SLO_READ_P95"  && { echo "  ✗ SLO lecture P95 dépassé";  FAIL=1; }
gt "$R99" "$SLO_READ_P99"  && { echo "  ✗ SLO lecture P99 dépassé";  FAIL=1; }
gt "$W95" "$SLO_WRITE_P95" && { echo "  ✗ SLO écriture P95 dépassé"; FAIL=1; }
gt "$W99" "$SLO_WRITE_P99" && { echo "  ✗ SLO écriture P99 dépassé"; FAIL=1; }
gt "$ERRPCT" "$SLO_ERROR_PCT" && { echo "  ✗ SLO taux d'erreur dépassé"; FAIL=1; }

if [ "$FAIL" -eq 0 ]; then echo "  ✅ Tous les SLO sont respectés."; else echo "  ❌ Au moins un SLO est dépassé."; fi
echo "  Rapport HTML : results/rapport/index.html"
exit "$FAIL"
