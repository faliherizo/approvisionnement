#!/usr/bin/env bash
# Captation « tout auto » de la publication Office 365, via CDP + capture d'écran.
#
# 1. Copie votre profil Chrome vers un dossier dédié (non-défaut) — nécessaire car Chrome interdit le
#    débogage distant sur le profil par défaut. La session O365 est préservée (cookies déchiffrés par
#    le vrai Trousseau, car ce Chrome n'est pas lancé par Playwright).
# 2. Lance ce Chrome dédié avec --remote-debugging-port.
# 3. Démarre l'enregistrement de l'écran (ffmpeg avfoundation).
# 4. Pilote le parcours via CDP (publication réelle + navigation O365).
# 5. Arrête l'enregistrement et le Chrome dédié.
#
# PRÉ-REQUIS : Chrome complètement fermé ; permission « Enregistrement de l'écran » accordée au
# terminal (Réglages Système → Confidentialité → Enregistrement de l'écran).
#
# Usage :  e2e/tour/capturer-cdp.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SRC_PROFILE="${SRC_PROFILE:-$HOME/Library/Application Support/Google/Chrome/Default}"
DEST="${DEST:-/tmp/chrome-o365-profile}"
PORT="${CDP_PORT:-9222}"
SCREEN_INDEX="${SCREEN_INDEX:-1}"        # « Capture screen 0 » (voir ffmpeg -list_devices)
OUT_DIR="$ROOT/e2e/tour/o365-video"
OUT_VIDEO="$OUT_DIR/publication-o365.mp4"
CHROME_BIN="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
mkdir -p "$OUT_DIR"

# Le Chrome dédié utilise un profil COPIÉ (data-dir distinct) : il peut coexister avec le Chrome normal.
pgrep -x "Google Chrome" >/dev/null && echo "ℹ Chrome est ouvert — la capture d'écran filmera tout l'écran ; évitez d'interagir pendant l'enregistrement."
[ -d "$SRC_PROFILE" ] || { echo "✖ Profil source introuvable : $SRC_PROFILE"; exit 1; }
[ -x "$CHROME_BIN" ]  || { echo "✖ Google Chrome introuvable."; exit 1; }

echo "▶ Copie du profil (session O365 préservée, caches exclus)…"
mkdir -p "$DEST/Default"
rsync -a --delete \
  --exclude 'Cache' --exclude 'Code Cache' --exclude 'GPUCache' --exclude 'DawnCache' \
  --exclude 'GrShaderCache' --exclude 'Service Worker' --exclude 'Application Cache' \
  --exclude 'component_crx_cache' --exclude 'extensions_crx_cache' \
  "$SRC_PROFILE/" "$DEST/Default/" 2>/dev/null || true

echo "▶ Lancement du Chrome dédié (port $PORT)…"
"$CHROME_BIN" \
  --user-data-dir="$DEST" --remote-debugging-port="$PORT" \
  --no-first-run --no-default-browser-check --disable-sync --no-service-autorun \
  --start-maximized --window-position=0,0 about:blank >/dev/null 2>&1 &
CHROME_PID=$!
trap 'kill "$CHROME_PID" 2>/dev/null || true; [ -n "${FF_PID:-}" ] && kill -INT "$FF_PID" 2>/dev/null || true' EXIT

# Attente du point de terminaison CDP.
for i in $(seq 1 30); do
  curl -s "http://localhost:$PORT/json/version" >/dev/null 2>&1 && break
  sleep 1
  [ "$i" = "30" ] && { echo "✖ CDP indisponible sur :$PORT"; exit 1; }
done
echo "✔ CDP prêt."

RAW_VIDEO="$OUT_DIR/o365-raw.mp4"
TIMELINE="$OUT_DIR/timeline-o365.json"
echo "▶ Démarrage de l'enregistrement écran → $RAW_VIDEO"
ffmpeg -hide_banner -loglevel error -y \
  -f avfoundation -pixel_format uyvy422 -framerate 25 -capture_cursor 1 -i "${SCREEN_INDEX}:none" \
  -pix_fmt yuv420p -c:v libx264 -preset ultrafast "$RAW_VIDEO" </dev/null &
FF_PID=$!
# Horodatage du début d'enregistrement (ms epoch), pour caler la narration sur la vidéo.
REC_START_MS="$(python3 -c 'import time;print(int(time.time()*1000))')"
sleep 2   # laisse l'enregistrement s'amorcer

echo "▶ Pilotage du parcours via CDP (avec narration)…"
set +e
CDP_URL="http://localhost:$PORT" REC_START_MS="$REC_START_MS" TIMELINE_OUT="$TIMELINE" \
  node "$ROOT/e2e/tour/o365-capture-cdp.mjs"
RC=$?
set -e

echo "▶ Arrêt de l'enregistrement…"
kill -INT "$FF_PID" 2>/dev/null || true
wait "$FF_PID" 2>/dev/null || true
FF_PID=""
kill "$CHROME_PID" 2>/dev/null || true
CHROME_PID=""
trap - EXIT

if [ ! -f "$RAW_VIDEO" ]; then echo "✖ Aucune vidéo produite (permission écran ? voir README)."; exit 1; fi

# Composition de la narration synchronisée puis mux sur la vidéo.
DUR="$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$RAW_VIDEO")"
if [ -f "$TIMELINE" ]; then
  echo "▶ Composition de la narration synchronisée…"
  node "$ROOT/e2e/tour/composer-audio.mjs" "$TIMELINE" "$DUR" "$OUT_DIR/o365-audio.m4a"
  ffmpeg -hide_banner -loglevel error -y -i "$RAW_VIDEO" -i "$OUT_DIR/o365-audio.m4a" \
    -map 0:v:0 -map 1:a:0 -c:v copy -c:a aac -shortest -movflags +faststart "$OUT_VIDEO"
else
  cp "$RAW_VIDEO" "$OUT_VIDEO"
fi
rm -f "$RAW_VIDEO"
echo "🎬 Segment O365 (narré) : $OUT_VIDEO ($(ffprobe -v error -show_entries format=duration -of csv=p=0 "$OUT_VIDEO" 2>/dev/null | cut -d. -f1)s)"
exit "$RC"
