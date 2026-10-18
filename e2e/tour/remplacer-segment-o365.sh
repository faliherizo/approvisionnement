#!/usr/bin/env bash
# Remplace UNIQUEMENT le segment « publication Office 365 » à la fin d'une vidéo de démonstration,
# en conservant intacte la tête (présentation + parcours applicatif) déjà montée.
#
# La vidéo finale = tête [0 → FRONTIERE] (présentation + app) ++ nouveau segment O365.
# La tête est réencodée depuis la vidéo existante (coupe à la frontière), le nouveau segment est
# normalisé au même gabarit, puis les deux sont concaténés sans nouvelle perte de synchronisation.
#
# Usage :  remplacer-segment-o365.sh <video-finale.mp4> <frontiere-sec> [nouveau-segment.mp4]
#   video-finale   : vidéo à mettre à jour (une sauvegarde horodatée est créée).
#   frontiere-sec  : instant (s) où l'ancien segment O365 commence (fin de la partie app).
#   nouveau-segment: défaut = e2e/tour/o365-video/publication-o365.mp4
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
FINAL="${1:?video-finale requise}"
FRONTIERE="${2:?frontiere en secondes requise}"
SEG="${3:-$ROOT/e2e/tour/o365-video/publication-o365.mp4}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

[ -f "$FINAL" ] || { echo "✖ Vidéo finale introuvable : $FINAL"; exit 1; }
[ -f "$SEG" ]   || { echo "✖ Segment introuvable : $SEG"; exit 1; }

# Gabarit commun : mise à l'échelle sans déformation + fond noir + 30 fps + SAR carré + audio stéréo 48k.
VF="scale=1280:800:force_original_aspect_ratio=decrease,pad=1280:800:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30"

normaliser() { # <src> <dst> — ajoute un silence si la source n'a pas d'audio.
  local src="$1" dst="$2"
  if ffprobe -v error -select_streams a -show_entries stream=index -of csv=p=0 "$src" | grep -q .; then
    ffmpeg -y -loglevel error -i "$src" -vf "$VF" \
      -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p -c:a aac -ar 48000 -ac 2 "$dst"
  else
    ffmpeg -y -loglevel error -i "$src" -f lavfi -i anullsrc=r=48000:cl=stereo -shortest \
      -vf "$VF" -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p -c:a aac -ar 48000 -ac 2 "$dst"
  fi
}

echo "▶ Extraction de la tête (présentation + app) : [0 → ${FRONTIERE}s]…"
ffmpeg -y -loglevel error -i "$FINAL" -t "$FRONTIERE" -vf "$VF" \
  -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p -c:a aac -ar 48000 -ac 2 "$TMP/tete.mp4"

echo "▶ Normalisation du nouveau segment Office 365…"
normaliser "$SEG" "$TMP/o365.mp4"

printf "file '%s'\nfile '%s'\n" "$TMP/tete.mp4" "$TMP/o365.mp4" > "$TMP/liste.txt"

STAMP="$(date +%Y%m%d-%H%M%S)"
cp "$FINAL" "${FINAL%.mp4}.avant-o365-$STAMP.mp4"
echo "▶ Concaténation tête + nouveau segment…"
ffmpeg -y -loglevel error -f concat -safe 0 -i "$TMP/liste.txt" -c copy "$TMP/final.mp4"
mv "$TMP/final.mp4" "$FINAL"

DUR="$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$FINAL" | cut -d. -f1)"
echo "✔ Remplacement terminé."
echo "   Vidéo      : $FINAL (${DUR}s)"
echo "   Sauvegarde : ${FINAL%.mp4}.avant-o365-$STAMP.mp4"
