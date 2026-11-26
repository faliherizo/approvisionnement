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

# --- Profil (PROFIL=cible : 500 utilisateurs sur l'infrastructure cible, cf. README « Profil cible ») ---
# Le profil ne fixe que les valeurs non fournies : une variable d'environnement l'emporte toujours.
if [ -n "${PROFIL:-}" ]; then
  [ -f "profils/${PROFIL}.env" ] || { echo "✗ Profil inconnu : profils/${PROFIL}.env" >&2; exit 2; }
  # shellcheck source=/dev/null
  . "profils/${PROFIL}.env"
fi

# --- Paramètres de charge (surchargeables par variables d'environnement) ---
PLAN="${PLAN:-contrat-dossiers-load.jmx}"
THREADS="${THREADS:-20}"
RAMPUP="${RAMPUP:-60}"
LOOPS="${LOOPS:-5}"
# Durée totale en secondes (montée comprise) ; 0 : les fils s'arrêtent à la fin de leurs LOOPS boucles (-1 : sans fin).
DUREE="${DUREE:-0}"
# false : une connexion par utilisateur virtuel (première boucle) au lieu d'une connexion à chaque boucle.
CONNEXION_CHAQUE_BOUCLE="${CONNEXION_CHAQUE_BOUCLE:-true}"
# Le plan borne toujours sa durée (planificateur JMeter) : une durée nulle y arrêterait les fils aussitôt. Sans durée
# demandée, une borne d'un jour laisse les LOOPS boucles décider seules de la fin.
DUREE_PLAN="$DUREE"; [ "$DUREE" -gt 0 ] || DUREE_PLAN=86400
BASE_HOST="${BASE_HOST:-localhost}"
BASE_PORT="${BASE_PORT:-8080}"
BASE_PROTOCOL="${BASE_PROTOCOL:-http}"
PAUSE_MS="${PAUSE_MS:-0}"
COMPTES="${COMPTES:-data/clm-initiateurs.csv}"
TROUSSES="${TROUSSES:-data/clm-trousses.csv}"
RESULTATS="${RESULTATS:-results}"

# --- Garde-fou : aucune charge vers un hôte distant sans autorisation nominative de cet hôte ---
# Une charge de 500 utilisateurs contre un environnement partagé (production, recette d'un client) le dégrade : elle
# n'est lancée que si CIBLE_AUTORISEE reprend exactement l'hôte visé, après accord écrit de son exploitant.
case "$BASE_HOST" in
  localhost|127.0.0.1|::1) ;;
  *) [ "${CIBLE_AUTORISEE:-}" = "$BASE_HOST" ] || {
       echo "✗ Hôte distant « $BASE_HOST » : charge refusée. Fixer CIBLE_AUTORISEE=$BASE_HOST une fois l'essai autorisé." >&2
       exit 3; } ;;
esac

# --- SLO à valider (ms, %) ---
SLO_READ_P95="${SLO_READ_P95:-800}"
SLO_READ_P99="${SLO_READ_P99:-1500}"
SLO_WRITE_P95="${SLO_WRITE_P95:-1500}"
SLO_WRITE_P99="${SLO_WRITE_P99:-2500}"
SLO_ERROR_PCT="${SLO_ERROR_PCT:-0.1}"

rm -rf "$RESULTATS"
mkdir -p "$RESULTATS"

echo "▶ JMeter [${PLAN}] — ${THREADS} threads, montée ${RAMPUP}s, ${LOOPS} boucles, durée ${DUREE}s → ${BASE_PROTOCOL}://${BASE_HOST}:${BASE_PORT}"
jmeter -n -t "$PLAN" \
  -l "$RESULTATS"/results.jtl \
  -e -o "$RESULTATS"/rapport \
  -Jthreads="$THREADS" -Jrampup="$RAMPUP" -Jloops="$LOOPS" -Jduree="$DUREE_PLAN" \
  -Jconnexion.chaque.boucle="$CONNEXION_CHAQUE_BOUCLE" \
  -JBASE_HOST="$BASE_HOST" -JBASE_PORT="$BASE_PORT" -JBASE_PROTOCOL="$BASE_PROTOCOL" \
  -Jresultats="$RESULTATS" -Jpause.ms="$PAUSE_MS" -Jcomptes="$COMPTES" -Jtrousses="$TROUSSES" \
  -j "$RESULTATS"/jmeter.log

echo ""
echo "▶ Validation des SLO (nearest-rank sur ${RESULTATS}/results.jtl)"

# 1) Éclate le JTL en latences par catégorie + compte total/erreurs. Lecteur CSV véritable : un message d'échec contient
#    des virgules et des sauts de ligne entre guillemets ; un découpage naïf sur la virgule décalait les colonnes et
#    comptait un échec comme un succès, ce qui rendait la porte aveugle aux erreurs.
python3 - "$RESULTATS" <<'PY'
import csv, re, sys
r = sys.argv[1]
total = erreurs = 0
with open(f"{r}/results.jtl", newline="", encoding="utf-8") as jtl, \
     open(f"{r}/read.txt", "w") as lecture, open(f"{r}/write.txt", "w") as ecriture:
    for ligne in csv.DictReader(jtl):
        total += 1
        if ligne["success"] == "false":
            erreurs += 1
        lbl, e = ligne["label"], ligne["elapsed"]
        # Lecture = GET, ou POST idempotent de consultation (login, analyse assistant, recherche).
        if lbl.startswith("GET ") or re.search(r"(login|analyser|search)$", lbl):
            print(e, file=lecture)
        elif re.match(r"(POST|PUT|DELETE) ", lbl):
            print(e, file=ecriture)
with open(f"{r}/counts.txt", "w") as c:
    print(total, erreurs, file=c)
PY

# 2) Percentile par rang le plus proche via tri numérique.
pct() {  # $1 = fichier de latences, $2 = percentile
  local f="$1" p="$2" n idx
  [ -s "$f" ] || { echo 0; return; }
  n=$(wc -l < "$f")
  idx=$(awk -v n="$n" -v p="$p" 'BEGIN { i = int(p/100*n + 0.9999); print (i<1?1:(i>n?n:i)) }')
  sort -n "$f" | sed -n "${idx}p"
}

read TOTAL ERRORS < "$RESULTATS"/counts.txt
R95=$(pct "$RESULTATS"/read.txt 95);  R99=$(pct "$RESULTATS"/read.txt 99)
W95=$(pct "$RESULTATS"/write.txt 95); W99=$(pct "$RESULTATS"/write.txt 99)
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

if [ "$FAIL" -eq 0 ]; then VERDICT="SLO respectés"; echo "  ✅ Tous les SLO sont respectés."; else VERDICT="SLO dépassé"; echo "  ❌ Au moins un SLO est dépassé."; fi

# Synthèse à joindre au rapport d'essai (profil cible : README « Profil cible »), avec les paramètres réellement utilisés.
cat > "$RESULTATS"/synthese.md <<FIN
# Essai de charge — ${PLAN}

| Paramètre | Valeur |
|---|---|
| Date (UTC) | $(date -u +%Y-%m-%dT%H:%M:%SZ) |
| Profil | ${PROFIL:-défaut} |
| Cible | ${BASE_PROTOCOL}://${BASE_HOST}:${BASE_PORT} |
| Utilisateurs simultanés | ${THREADS} (montée ${RAMPUP} s, ${LOOPS} boucles, durée ${DUREE} s, pause ${PAUSE_MS} ms) |
| Connexion à chaque boucle | ${CONNEXION_CHAQUE_BOUCLE} |
| Échantillons / erreurs | ${TOTAL} / ${ERRORS} (${ERRPCT} %, SLO < ${SLO_ERROR_PCT} %) |
| Lecture P95 / P99 | ${R95} ms / ${R99} ms (SLO ${SLO_READ_P95} / ${SLO_READ_P99} ms) |
| Écriture P95 / P99 | ${W95} ms / ${W99} ms (SLO ${SLO_WRITE_P95} / ${SLO_WRITE_P99} ms) |
| Verdict | ${VERDICT} |
FIN
echo "  Rapport HTML : $RESULTATS/rapport/index.html — synthèse : $RESULTATS/synthese.md"
exit "$FAIL"
