#!/usr/bin/env node
/**
 * Vidéos d'aide et de présentation, tournées à partir des scénarios e2e (bdd/e2e/donnees/*.json) : ce que la vidéo
 * montre est ce que le scénario vérifie. Narration en français (voix macOS « Amélie »), sous-titres WebVTT.
 *
 *   node outils/videos.mjs voix [motif]            clips de narration et leurs durées (cache : medias/.voix/)
 *   node outils/videos.mjs tourner [motif] [org]   tournage (E2E_VIDEO=1) ; [org] rejoue avec les comptes d'une organisation
 *   node outils/videos.mjs monter                  voix posée sur sa chronologie, MP4 + VTT dans medias/aide/, index
 *   node outils/videos.mjs assembler <organisation>     vidéo de présentation de l'organisation (medias/vitrines/<org>/)
 *   node outils/videos.mjs miseEnRoute <organisation>   vidéo « Mise en route de l'organisation » (medias/aide/)
 *   node outils/videos.mjs publier                 envoi de medias/ vers la médiathèque du serveur (rsync)
 *
 * medias/ reproduit l'arborescence de la médiathèque servie par /api/media/ ; il n'est pas versionné.
 */
import { execFileSync, spawnSync } from 'child_process'
import { createHash } from 'crypto'
import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import { narration } from './phrases.mjs'

const BDD = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const MEDIAS = join(BDD, 'medias')
const VOIX = join(MEDIAS, '.voix')
const DUREES = join(VOIX, 'durees.json')
const TOURNAGES = join(BDD, 'test-results-e2e/videos')
const TRACES = join(BDD, 'test-results-e2e/appels')
const VOIX_MACOS = process.env.VOIX || 'Amélie'
const ORGANISATIONS = { iq: 'Investissement Québec', levis: 'Ville de Lévis', v3r: 'Ville de Trois-Rivières', chu: 'CHU' }

const lireJson = (f) => JSON.parse(readFileSync(f, 'utf8'))
const ffmpeg = (...a) => execFileSync('ffmpeg', ['-y', '-loglevel', 'error', ...a], { stdio: ['ignore', 'inherit', 'inherit'] })
const duree = (f) => Number(execFileSync('ffprobe', ['-v', 'error', '-show_entries', 'format=duration', '-of', 'csv=p=0', f]).toString().trim())
const cle = (texte) => createHash('sha1').update(texte).digest('hex').slice(0, 16)
const scenarios = () => readdirSync(join(BDD, 'e2e/donnees')).filter((f) => f.endsWith('.json'))
  .flatMap((f) => { const j = lireJson(join(BDD, 'e2e/donnees', f)); return j.scenarios.map((s) => ({ ...s, appelOffres: j.appelOffres })) })
const organisationDe = (appelOffres) => /Investissement/.test(appelOffres) ? 'iq' : /Trois-Rivi/.test(appelOffres) ? 'v3r' : /CHU|Santé/.test(appelOffres) ? 'chu' : 'levis'

/** Clip vocal d'un texte (mis en cache) ; renvoie son fichier WAV. */
function clip(texte, durees) {
  const wav = join(VOIX, `${cle(texte)}.wav`)
  if (!existsSync(wav)) {
    const aiff = wav.replace(/\.wav$/, '.aiff')
    execFileSync('say', ['-v', VOIX_MACOS, '-o', aiff, texte])
    ffmpeg('-i', aiff, '-ac', '1', '-ar', '48000', wav)
    rmSync(aiff, { force: true })
  }
  durees[texte] ??= duree(wav)
  return wav
}

function voix(motif) {
  mkdirSync(VOIX, { recursive: true })
  const durees = existsSync(DUREES) ? lireJson(DUREES) : {}
  const textes = new Set()
  for (const s of scenarios().filter((x) => !motif || new RegExp(motif).test(x.id))) {
    textes.add(s.titre)
    for (const e of s.etapes) { const n = narration(e); if (n) textes.add(n) }
  }
  for (const t of textes) clip(t, durees)
  writeFileSync(DUREES, JSON.stringify(durees, null, 1))
  console.log(`✓ ${textes.size} clip(s) de narration ; durées : ${DUREES}`)
}

/** Tournage ; `organisation` (ex. v3r) rejoue les parcours avec les comptes de cette organisation, sur son sous-domaine. */
function tourner(motif, organisation) {
  if (!existsSync(DUREES)) voix(motif)
  const env = { ...process.env, E2E_VIDEO: '1', E2E_VIDEO_DUREES: DUREES }
  if (organisation) Object.assign(env, { E2E_COMPTES: organisation, BDD_FRONTEND_URL: `http://${organisation}.e2e.localhost:5175` })
  const r = spawnSync('npx', ['playwright', 'test', '-c', 'playwright.e2e.config.ts', ...(motif ? ['-g', motif] : [])],
    { cwd: BDD, stdio: 'inherit', env })
  process.exitCode = r.status ?? 1
}

/** Horodatage WebVTT (hh:mm:ss.mmm). */
const hms = (ms) => new Date(Math.max(0, ms)).toISOString().slice(11, 23)
const nettoyer = (t) => String(t).replace(/`/g, '').replace(/ \((button|link|tab|checkbox|radio|menuitem)\)$/, '')

function vtt(chronologie, total, decalage = 0) {
  return chronologie.map((m, i) => {
    const fin = i + 1 < chronologie.length ? chronologie[i + 1].t : Math.min(m.t + 6000, total)
    return `${hms(m.t + decalage)} --> ${hms(Math.max(fin, m.t + 1500) + decalage)}\n${nettoyer(m.texte)}`
  })
}

/** Piste audio : chaque clip posé à son instant sur un silence de la durée de la vidéo. */
function piste(chronologie, durees, total, sortie) {
  const entrees = chronologie.flatMap((m) => ['-i', clip(m.voix, durees)])
  const filtres = chronologie.map((m, i) => `[${i}:a]adelay=${m.t}|${m.t}[a${i}]`).join(';')
  const mix = `${chronologie.map((_, i) => `[a${i}]`).join('')}amix=inputs=${chronologie.length}:normalize=0,apad=whole_dur=${total / 1000}[sortie]`
  ffmpeg(...entrees, '-filter_complex', `${filtres};${mix}`, '-map', '[sortie]', '-ac', '1', '-ar', '48000', sortie)
}

// Écrans de passage (connexion, accueil d'arrivée) : un tutoriel n'y est proposé que s'il ne montre rien d'autre.
const PASSAGE = new Set(['blank', '/login', '/', '/clm'])
const utiles = (r) => {
  const propres = r.filter((x) => x !== 'blank' && x !== '/login')
  const metier = propres.filter((x) => !PASSAGE.has(x))
  return metier.length ? metier : propres
}

function routes(id) {
  const f = join(TRACES, `${id}.json`)
  if (!existsSync(f)) return []
  return utiles([...new Set(lireJson(f).ecrans.map((r) => r.split('?')[0].replace(/\/\d+(?=\/|$)/g, '/:id')))])
}

function monter() {
  const durees = lireJson(DUREES)
  const AIDE = join(MEDIAS, 'aide')
  mkdirSync(AIDE, { recursive: true })
  const indexF = join(AIDE, 'index.json')
  const index = existsSync(indexF) ? lireJson(indexF).filter((x) => !x.assemblage).map((x) => ({ ...x, routes: utiles(x.routes) })) : []
  for (const f of readdirSync(TOURNAGES).filter((x) => x.endsWith('.json'))) {
    const t = lireJson(join(TOURNAGES, f))
    if (!t.reussi || !t.video || !existsSync(t.video) || t.chronologie.length === 0) { console.log(`✗ ${t.id} : tournage incomplet, ignoré`); continue }
    const organisation = t.organisation ?? organisationDe(t.appelOffres)
    const nom = (t.organisation ? `${t.id}-${t.organisation}` : t.id).toLowerCase()
    const total = Math.round(duree(t.video) * 1000)
    const audio = join(VOIX, `${nom}.wav`)
    piste(t.chronologie, durees, total, audio)
    ffmpeg('-i', t.video, '-i', audio, '-map', '0:v', '-map', '1:a', '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '30', '-pix_fmt', 'yuv420p',
      '-c:a', 'aac', '-b:a', '96k', '-movflags', '+faststart', join(AIDE, `${nom}.mp4`))
    writeFileSync(join(AIDE, `${nom}.vtt`), `WEBVTT\n\n${vtt(t.chronologie, total).join('\n\n')}\n`)
    const s = scenarios().find((x) => x.id === t.id)
    const r = routes(t.id)
    const id = t.organisation ? `${t.id}-${t.organisation.toUpperCase()}` : t.id
    const entree = { id, titre: t.titre, exigences: t.exigences, profil: t.profil, organisation,
      espace: r.some((x) => x.startsWith('/clm')) ? 'clm' : 'socle', routes: r, miseEnRoute: s?.miseEnRoute ?? null,
      etapes: t.chronologie.slice(1).map((m) => nettoyer(m.texte)), duree: Math.round(total / 1000),
      video: `aide/${nom}.mp4`, sousTitres: `aide/${nom}.vtt` }
    const i = index.findIndex((x) => x.id === id)
    if (i >= 0) index[i] = entree; else index.push(entree)
    console.log(`✓ ${t.id} : ${entree.duree} s`)
  }
  const assemblages = existsSync(indexF) ? lireJson(indexF).filter((x) => x.assemblage) : []
  writeFileSync(indexF, JSON.stringify([...index.sort((a, b) => a.id.localeCompare(b.id)), ...assemblages], null, 1))
  console.log(`✓ index de l'aide : ${index.length} tutoriel(s)`)
}

const html = (t) => t.replace(/&/g, '&amp;').replace(/</g, '&lt;')

/**
 * Image d'un carton de titre, rendue par Chromium (l'ffmpeg de macOS n'a pas toujours le filtre drawtext) : le texte
 * long passe à la ligne au lieu de déborder.
 */
async function image(navigateur, titre, sousTitre, sortie) {
  const page = await navigateur.newPage({ viewport: { width: 1280, height: 720 } })
  await page.setContent(`<body style="margin:0;width:1280px;height:720px;display:flex;flex-direction:column;justify-content:center;
    align-items:center;background:#0b4a8a;color:#fff;font-family:Arial,Helvetica,sans-serif;text-align:center">
    <h1 style="font-size:46px;max-width:1100px;margin:0 0 24px;line-height:1.25">${html(titre)}</h1>
    ${sousTitre ? `<p style="font-size:28px;color:#d7e3f2;margin:0">${html(sousTitre)}</p>` : ''}</body>`)
  await page.screenshot({ path: sortie })
  await page.close()
}

/** Carton de titre (3,5 s au moins) avec sa narration, au format des vidéos tournées. */
async function carton(navigateur, titre, sousTitre, durees, sortie) {
  const voixF = clip(titre, durees)
  const d = Math.max(3.5, durees[titre] + 0.8)
  const png = sortie.replace(/\.mp4$/, '.png')
  await image(navigateur, titre, sousTitre, png)
  ffmpeg('-loop', '1', '-framerate', '25', '-i', png, '-i', voixF, '-filter_complex', `[1:a]apad=whole_dur=${d}[a]`,
    '-map', '0:v', '-map', '[a]', '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '28', '-pix_fmt', 'yuv420p', '-r', '25',
    '-c:a', 'aac', '-b:a', '96k', '-ar', '48000', '-ac', '1', '-t', String(d), sortie)
  return d
}

/** Assemble cartons et tutoriels en une vidéo, avec ses sous-titres décalés d'autant. */
async function assembler(organisation, choix, titre, sortieRel, assemblage) {
  const durees = lireJson(DUREES)
  const index = lireJson(join(MEDIAS, 'aide/index.json'))
  const pieces = choix(index.filter((x) => !x.assemblage))
  if (pieces.length === 0) throw new Error(`Aucun tutoriel pour ${organisation}`)
  const travail = join(VOIX, `assemblage-${organisation}-${assemblage}`)
  mkdirSync(travail, { recursive: true })
  const liste = []
  const cues = []
  let t = 0
  const { chromium } = await import('@playwright/test')
  const navigateur = await chromium.launch()
  const ajouterCarton = async (titreC, sousC) => {
    const f = join(travail, `carton-${liste.length}.mp4`)
    const d = await carton(navigateur, titreC, sousC, durees, f)
    cues.push(`${hms(t * 1000)} --> ${hms((t + d) * 1000)}\n${titreC}`)
    liste.push(f); t += d
  }
  await ajouterCarton(titre, ORGANISATIONS[organisation])
  for (const p of pieces) {
    await ajouterCarton(p.titre, null)
    const f = join(MEDIAS, p.video)
    const brut = readFileSync(join(MEDIAS, p.sousTitres), 'utf8').split('\n\n').slice(1).filter(Boolean)
    for (const c of brut) {
      const [temps, ...texte] = c.split('\n')
      const [a, b] = temps.split(' --> ').map((x) => { const [h, m, s] = x.split(':'); return (Number(h) * 3600 + Number(m) * 60 + Number(s)) * 1000 })
      cues.push(`${hms(a + t * 1000)} --> ${hms(b + t * 1000)}\n${texte.join('\n')}`)
    }
    liste.push(f); t += duree(f)
  }
  await navigateur.close()
  const sortie = join(MEDIAS, sortieRel)
  mkdirSync(dirname(sortie), { recursive: true })
  // Réencodage commun (cartons et tutoriels n'ont pas tous la même cadence) : concaténation par filtre.
  const entrees = liste.flatMap((f) => ['-i', f])
  const filtre = liste.map((_, i) => `[${i}:v]fps=25,scale=1280:720,setsar=1[v${i}];[${i}:a]aresample=48000,aformat=channel_layouts=mono[a${i}]`).join(';')
    + ';' + liste.map((_, i) => `[v${i}][a${i}]`).join('') + `concat=n=${liste.length}:v=1:a=1[v][a]`
  ffmpeg(...entrees, '-filter_complex', filtre, '-map', '[v]', '-map', '[a]', '-c:v', 'libx264', '-preset', 'veryfast', '-crf', '30',
    '-pix_fmt', 'yuv420p', '-c:a', 'aac', '-b:a', '96k', '-movflags', '+faststart', sortie)
  writeFileSync(sortie.replace(/\.mp4$/, '.vtt'), `WEBVTT\n\n${cues.join('\n\n')}\n`)
  rmSync(travail, { recursive: true, force: true })
  console.log(`✓ ${sortieRel} : ${Math.round(t)} s, ${pieces.length} tutoriel(s)`)
  return { pieces, duree: Math.round(t) }
}

async function presentation(organisation) {
  await assembler(organisation, (index) => index.filter((x) => x.organisation === organisation),
    `${ORGANISATIONS[organisation]} : présentation des fonctionnalités`, `vitrines/${organisation}/presentation.mp4`, 'presentation')
}

async function miseEnRoute(organisation) {
  const espace = organisation === 'iq' ? 'clm' : 'socle'
  const nom = `aide/mise-en-route-${organisation}.mp4`
  const id = `MISE-EN-ROUTE-${organisation.toUpperCase()}`
  const { pieces, duree: d } = await assembler(organisation, (index) => index.filter((x) => x.miseEnRoute && x.espace === espace && x.organisation === organisation)
    .sort((a, b) => a.miseEnRoute - b.miseEnRoute), 'Mise en route de l’organisation', nom, 'mise-en-route')
  const indexF = join(MEDIAS, 'aide/index.json')
  const index = lireJson(indexF).filter((x) => x.id !== id)
  index.push({ id, titre: 'Mise en route de l’organisation : paramétrer l’application avec le compte administrateur',
    exigences: [...new Set(pieces.flatMap((p) => p.exigences))], profil: 'administration', organisation, espace, routes: [], miseEnRoute: 0,
    etapes: pieces.map((p, i) => `${i + 1}. ${p.titre}`), duree: d, video: nom, sousTitres: nom.replace(/\.mp4$/, '.vtt'), assemblage: true })
  writeFileSync(indexF, JSON.stringify(index, null, 1))
}

function publier() {
  const hote = process.env.MEDIA_HOTE || 'deploy@51.222.86.37'
  const cible = process.env.MEDIA_CIBLE || '~/projets/VINA-DOC/approvisionnement/deploy/ovh/donnees/media/'
  execFileSync('rsync', ['-av', '--exclude', '.voix', `${MEDIAS}/`, `${hote}:${cible}`], { stdio: 'inherit' })
  // Le serveur Java (utilisateur 10001) écrit dans la médiathèque (vidéos téléversées) : elle lui revient.
  execFileSync('ssh', [hote, `docker run --rm -v ${cible.replace(/^~/, '$HOME')}:/d alpine chown -R 10001:10001 /d`], { stdio: 'inherit' })
}

const [commande, arg, arg2] = process.argv.slice(2)
const commandes = { voix: () => voix(arg), tourner: () => tourner(arg, arg2), monter, assembler: () => presentation(arg),
  miseEnRoute: () => miseEnRoute(arg), publier }
if (!commandes[commande]) { console.error(readFileSync(fileURLToPath(import.meta.url), 'utf8').split('*/')[0]); process.exit(2) }
await commandes[commande]()
