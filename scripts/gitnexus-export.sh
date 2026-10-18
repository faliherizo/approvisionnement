#!/usr/bin/env bash
#
# gitnexus-export.sh — Prépare une archive ZIP propre du dépôt pour l'analyser
# avec GitNexus (https://gitnexus.vercel.app), l'outil client-side qui transforme
# un dépôt Git en graphe de connaissances dans le navigateur (Graph RAG).
#
# GitNexus tourne 100 % côté navigateur : aucun envoi serveur, aucun compte,
# adapté au code sensible (esprit Loi 25 du projet Ville de Lévis).
# Trois entrées possibles : URL GitHub, dossier local, ou ce ZIP (glisser-déposer).
#
# Usage :
#   ./scripts/gitnexus-export.sh            # → gitnexus-export.zip à la racine
#   ./scripts/gitnexus-export.sh /tmp/x.zip # → chemin de sortie explicite
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${1:-$ROOT/gitnexus-export.zip}"

cd "$ROOT"
rm -f "$OUT"

# Exclusions : dépendances, builds, index, médias lourds.
zip -r -q "$OUT" . \
  -x '*/node_modules/*' -x 'node_modules/*' \
  -x '*/target/*' -x 'target/*' \
  -x '*/dist/*' -x 'dist/*' \
  -x '*/.git/*' -x '.git/*' \
  -x '*/.gitnexus/*' -x '.gitnexus/*' \
  -x '*/test-results/*' -x '*/playwright-report/*' \
  -x '*.zip' \
  -x '*.png' -x '*.jpg' -x '*.jpeg' -x '*.pdf' -x '*.docx'

SIZE="$(du -h "$OUT" | cut -f1)"
echo "✅ Archive GitNexus prête : $OUT ($SIZE)"
echo "➡️  Ouvre https://gitnexus.vercel.app puis glisse-dépose ce fichier."
