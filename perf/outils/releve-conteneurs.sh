#!/usr/bin/env bash
#
# Relève l'UC et la mémoire des conteneurs nommés toutes les INTERVALLE secondes (docker stats), jusqu'à son arrêt.
#   releve-conteneurs.sh <fichier.csv> <conteneur>...
# Colonnes : epoch,conteneur,cpu_pct,memoire_utilisee,memoire_limite,memoire_pct
# cpu_pct est relatif à un cœur : 200 % = deux vCPU pleinement occupés.
#
set -uo pipefail
sortie="$1"; shift
: "${INTERVALLE:=5}"
echo "epoch,conteneur,cpu_pct,memoire_utilisee,memoire_limite,memoire_pct" > "$sortie"
while true; do
  t=$(date +%s)
  docker stats --no-stream --format '{{.Name}},{{.CPUPerc}},{{.MemUsage}},{{.MemPerc}}' "$@" 2>/dev/null \
    | sed -e 's/%//g' -e 's# / #,#' -e "s/^/$t,/" >> "$sortie"
  # Charge de l'hôte (JMeter compris) : moyenne sur une minute des tâches prêtes, ×100 (400 = 4 cœurs occupés).
  [ -r /proc/loadavg ] && awk -v t="$t" '{ printf "%s,hote-charge-1min,%.0f,0B,0B,0\n", t, $1 * 100 }' /proc/loadavg >> "$sortie"
  sleep "$INTERVALLE"
done
