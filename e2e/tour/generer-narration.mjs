// Génère les clips de narration (voix macOS « Amélie », fr_CA) et leurs durées.
// Sortie : e2e/tour/narration-audio/<clé>.wav  +  e2e/tour/narration-durations.json
//
// Usage :  node tour/generer-narration.mjs

import { TOUS } from './narration.mjs'
import { execFileSync } from 'node:child_process'
import { mkdirSync, writeFileSync, rmSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const ICI = dirname(fileURLToPath(import.meta.url))
const OUT = resolve(ICI, 'narration-audio')
const VOIX = process.env.VOIX || 'Amélie'
mkdirSync(OUT, { recursive: true })

const durations = {}
for (const [cle, texte] of Object.entries(TOUS)) {
  const aiff = resolve(OUT, `${cle}.aiff`)
  const wav = resolve(OUT, `${cle}.wav`)
  execFileSync('say', ['-v', VOIX, '-o', aiff, texte])
  // Normalise en WAV mono 48 kHz (format uniforme pour la composition).
  execFileSync('ffmpeg', ['-y', '-loglevel', 'error', '-i', aiff, '-ac', '1', '-ar', '48000', wav])
  const d = Number(execFileSync('ffprobe',
    ['-v', 'error', '-show_entries', 'format=duration', '-of', 'csv=p=0', wav]).toString().trim())
  durations[cle] = d
  rmSync(aiff, { force: true })
  console.log(`♪ ${cle}  (${d.toFixed(2)}s)`)
}

writeFileSync(resolve(ICI, 'narration-durations.json'), JSON.stringify(durations, null, 2))
const total = Object.values(durations).reduce((a, b) => a + b, 0)
console.log(`✔ ${Object.keys(durations).length} clips, narration totale ${total.toFixed(1)}s → narration-durations.json`)
