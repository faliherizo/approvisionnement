#!/usr/bin/env bash
# Montage : ajoute le segment de publication Office 365 à la fin de la vidéo démo du site vitrine.
#
# Préserve la narration audio de la démo existante ; ajoute une piste silencieuse au segment O365
# (les vidéos Playwright n'ont pas de son). Normalise les deux en 1280×800 / 30 fps / H.264 + AAC,
# concatène sans ré-encodage, puis régénère la version WebM. L'original est sauvegardé.
#
# Usage :  e2e/tour/monter-demo.sh [segment.(webm|mp4)] [base.(webm|mp4)]
#   segment : partie ajoutée à la fin (défaut : capture O365).
#   base    : vidéo de tête (défaut : la démo vitrine actuelle). Fournir le NOUVEAU tour app pour
#             reconstruire la vidéo de zéro plutôt que d'ajouter à l'ancien montage.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MEDIA="$ROOT/frontend/public/media"
DEMO="$MEDIA/demo-vina.mp4"
SEG="${1:-$ROOT/e2e/tour/o365-video/publication-o365.mp4}"
BASE="${2:-$DEMO}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

[ -f "$BASE" ] || { echo "✖ Base introuvable : $BASE"; exit 1; }
[ -f "$SEG" ]  || { echo "✖ Segment introuvable : $SEG"; exit 1; }

# Filtre vidéo commun : mise à l'échelle sans déformation + fond noir + 30 fps + SAR carré.
VF="scale=1280:800:force_original_aspect_ratio=decrease,pad=1280:800:(ow-iw)/2:(oh-ih)/2,setsar=1,fps=30"

# Normalise un fichier en MP4 uniforme. Ajoute un silence si le fichier n'a pas d'audio.
normaliser() {
  local src="$1" dst="$2"
  if ffprobe -v error -select_streams a -show_entries stream=index -of csv=p=0 "$src" | grep -q .; then
    ffmpeg -y -loglevel error -i "$src" -vf "$VF" \
      -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p \
      -c:a aac -ar 48000 -ac 2 "$dst"
  else
    ffmpeg -y -loglevel error -i "$src" -f lavfi -i anullsrc=r=48000:cl=stereo -shortest \
      -vf "$VF" -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p \
      -c:a aac -ar 48000 -ac 2 "$dst"
  fi
}

echo "▶ Normalisation de la base (narration conservée)…"; normaliser "$BASE" "$TMP/0.mp4"
echo "▶ Normalisation du segment Office 365 (silence ajouté)…"; normaliser "$SEG" "$TMP/1.mp4"

printf "file '%s'\nfile '%s'\n" "$TMP/0.mp4" "$TMP/1.mp4" > "$TMP/liste.txt"

STAMP="$(date +%Y%m%d-%H%M%S)"
cp "$DEMO" "$MEDIA/demo-vina.avant-o365-$STAMP.mp4"
echo "▶ Concaténation…"
ffmpeg -y -loglevel error -f concat -safe 0 -i "$TMP/liste.txt" -c copy "$TMP/final.mp4"
mv "$TMP/final.mp4" "$DEMO"

echo "▶ Régénération WebM (VP9 + Opus)…"
ffmpeg -y -loglevel error -i "$DEMO" -c:v libvpx-vp9 -b:v 0 -crf 33 -c:a libopus "$MEDIA/demo-vina.webm"

echo "✔ Montage terminé."
echo "   MP4  : $DEMO ($(ffprobe -v error -show_entries format=duration -of csv=p=0 "$DEMO" | cut -d. -f1) s)"
echo "   WebM : $MEDIA/demo-vina.webm"
echo "   Sauvegarde de l'original : $MEDIA/demo-vina.avant-o365-$STAMP.mp4"
