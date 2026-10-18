// Compose une piste audio synchronisée : place chaque clip de narration à l'horodatage réel
// enregistré pendant le test (timeline), sur un lit de silence de la durée exacte de la vidéo.
//
// Usage :  node tour/composer-audio.mjs <timeline.json> <dureeSecondes> <sortie.m4a>
//   NARRATION_LEAD_MS (env) : décalage global appliqué à tous les clips (défaut 0).

import { execFileSync } from 'node:child_process'
import { readFileSync, existsSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const ICI = dirname(fileURLToPath(import.meta.url))
const CLIPS = resolve(ICI, 'narration-audio')
const [timelineFile, dureeStr, sortie] = process.argv.slice(2)
if (!timelineFile || !dureeStr || !sortie) {
  console.error('Usage: composer-audio.mjs <timeline.json> <dureeSecondes> <sortie.m4a>')
  process.exit(1)
}
const total = Number(dureeStr)
const LEAD = Number(process.env.NARRATION_LEAD_MS || '0')
const timeline = JSON.parse(readFileSync(timelineFile, 'utf8'))
  .filter((e) => existsSync(resolve(CLIPS, `${e.key}.wav`)))

const args = ['-y', '-loglevel', 'error', '-f', 'lavfi', '-t', String(total), '-i', 'anullsrc=r=48000:cl=mono']
timeline.forEach((e) => args.push('-i', resolve(CLIPS, `${e.key}.wav`)))

let filtre = ''
timeline.forEach((e, i) => {
  const t = Math.max(0, Math.round(e.t + LEAD))
  filtre += `[${i + 1}:a]adelay=${t}:all=1[d${i + 1}];`
})
const labels = timeline.map((_, i) => `[d${i + 1}]`).join('')
filtre += `[0:a]${labels}amix=inputs=${timeline.length + 1}:normalize=0:dropout_transition=0,atrim=0:${total}[out]`

args.push('-filter_complex', filtre, '-map', '[out]', '-c:a', 'aac', '-b:a', '160k', sortie)
execFileSync('ffmpeg', args, { stdio: 'inherit' })
console.log(`✔ Audio synchronisé (${timeline.length} clips, ${total}s) → ${sortie}`)
