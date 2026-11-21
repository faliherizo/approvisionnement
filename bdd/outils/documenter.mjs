#!/usr/bin/env node
/**
 * Documentation et couverture des exigences, générées depuis les sources de vérité :
 *   - bdd/exigences/*.json   catalogue des exigences de chaque appel d'offres et leur mode de vérification ;
 *   - bdd/features/**.feature scénarios Gherkin, étiquetés par les identifiants d'exigences ;
 *   - bdd/donnees/*.json      cas exécutables (appels, profils, attendus) ;
 *   - bdd/test-results/resultats.json  dernier résultat d'exécution (rapport JSON de Playwright), s'il existe.
 * Écrit docs/scenarios-bdd/README.md et un document par appel d'offres. Avec --verifier, sort en erreur si une
 * exigence « bdd » n'a aucun scénario ou si un scénario cite une exigence inconnue.
 */
import { existsSync, mkdirSync, readFileSync, readdirSync, statSync, writeFileSync } from 'fs'
import { basename, dirname, join, relative, resolve } from 'path'
import { fileURLToPath } from 'url'

const BDD = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const RACINE = resolve(BDD, '..')
const SORTIE = join(RACINE, 'docs/scenarios-bdd')
const verifier = process.argv.includes('--verifier')

const lireJson = (f) => JSON.parse(readFileSync(f, 'utf8'))
const fichiers = (d, ext) => readdirSync(d).flatMap((f) => {
  const p = join(d, f)
  return statSync(p).isDirectory() ? fichiers(p, ext) : p.endsWith(ext) ? [p] : []
})

// --- Catalogues, cas, scénarios ------------------------------------------------------------------------------------
const ORDRE = ['levis', 'trois-rivieres', 'iq', 'chu']
const catalogues = readdirSync(join(BDD, 'exigences')).filter((f) => f.endsWith('.json'))
  .sort((a, b) => ORDRE.indexOf(basename(a, '.json')) - ORDRE.indexOf(basename(b, '.json')))
  .map((f) => ({ cle: basename(f, '.json'), ...lireJson(join(BDD, 'exigences', f)) }))
const cas = Object.assign({}, ...readdirSync(join(BDD, 'donnees')).filter((f) => f.endsWith('.json') && f !== 'comptes.json')
  .map((f) => Object.fromEntries(Object.entries(lireJson(join(BDD, 'donnees', f)).cas ?? {}).map(([k, v]) => [k, { ...v, fichier: f }]))))

/** Analyse minimale du Gherkin français : fonctionnalité, contexte, scénarios, étiquettes, étapes, commentaires. */
function analyser(chemin) {
  const lignes = readFileSync(chemin, 'utf8').split('\n')
  const f = { chemin: relative(RACINE, chemin), titre: '', etiquettes: [], contexte: [], scenarios: [] }
  let etiquettes = [], courant = null, dansContexte = false
  for (const brute of lignes) {
    const l = brute.trim()
    if (!l || l.startsWith('# language')) continue
    if (l.startsWith('@')) { etiquettes.push(...l.split(/\s+/)); continue }
    if (l.startsWith('Fonctionnalité:')) { f.titre = l.slice(15).trim(); f.etiquettes = etiquettes; etiquettes = []; continue }
    if (l.startsWith('Contexte:')) { dansContexte = true; courant = null; continue }
    if (/^Scénario( Outline)?:|^Plan du scénario:/.test(l)) {
      dansContexte = false
      courant = { titre: l.replace(/^[^:]+:/, '').trim(), etiquettes: [...f.etiquettes, ...etiquettes], etapes: [], notes: [] }
      etiquettes = []
      f.scenarios.push(courant)
      continue
    }
    if (l.startsWith('#')) { (courant ? courant.notes : []).push(l.replace(/^#\s*/, '')); continue }
    if (/^(Étant donné|Quand|Alors|Et|Mais|Soit)\b/.test(l)) {
      if (dansContexte) f.contexte.push(l)
      else if (courant) courant.etapes.push(l)
      continue
    }
    if (!courant && !dansContexte && f.titre) f.description = [f.description, l].filter(Boolean).join(' ')
  }
  return f
}
const features = existsSync(join(BDD, 'features')) ? fichiers(join(BDD, 'features'), '.feature').sort().map(analyser) : []
const scenarios = features.flatMap((f) => f.scenarios.map((s) => ({ ...s, feature: f })))

// --- Derniers résultats (rapport JSON de Playwright) ---------------------------------------------------------------
const resultats = new Map()
const rapport = join(BDD, 'test-results/resultats.json')
if (existsSync(rapport)) {
  const parcourir = (suite) => {
    for (const s of suite.specs ?? []) {
      const r = s.tests?.[0]?.results?.at(-1)
      const statut = s.tests?.[0]?.status === 'skipped' ? 'skipped' : r?.status ?? 'inconnu'
      resultats.set(s.title, statut)
    }
    for (const sous of suite.suites ?? []) parcourir(sous)
  }
  for (const suite of lireJson(rapport).suites ?? []) parcourir(suite)
}
const symbole = (s) => s.etiquettes.includes('@ecart') ? '⏸ écart déclaré'
  : ({ passed: '✅ réussi', failed: '❌ échec', timedOut: '❌ délai dépassé', skipped: '⏸ en attente', interrupted: '❌ interrompu' })[resultats.get(s.titre)] ?? '— non exécuté'

// --- Couverture -----------------------------------------------------------------------------------------------------
const connues = new Set(catalogues.flatMap((c) => c.exigences.map((e) => e.id)))
const parExigence = new Map()
const inconnues = []
for (const s of scenarios) {
  for (const t of s.etiquettes.filter((x) => /^@(LEV|TR|IQ|CHU)-/.test(x)).map((x) => x.slice(1))) {
    if (!connues.has(t)) inconnues.push(`${t} (« ${s.titre} », ${s.feature.chemin})`)
    parExigence.set(t, [...(parExigence.get(t) ?? []), s])
  }
}
const manquantes = catalogues.flatMap((c) => c.exigences
  .filter((e) => (e.verification === 'bdd' || e.verification === 'ecart') && !parExigence.has(e.id)).map((e) => `${e.id} — ${e.libelle}`))

// --- Rendu Markdown --------------------------------------------------------------------------------------------------
const MODES = { bdd: 'Scénario BDD exécuté', documentaire: 'Documentaire', ecart: 'Écart déclaré (scénario en attente)', 'sans-objet': 'Sans objet' }
const echapper = (t) => String(t ?? '').replace(/\|/g, '\\|').replace(/\n/g, ' ')

function rendreCas(nom) {
  const c = cas[nom]
  if (!c) return `> Cas « ${nom} » introuvable.\n`
  const lignes = [`**Cas \`${nom}\`** — ${c.description} *(données : \`bdd/donnees/${c.fichier}\`${c.profil ? ` ; profil : ${c.profil}` : ''})*`, '',
    '| # | Appel | Profil | Statut attendu | Vérifications et captures |', '|---|---|---|---|---|']
  c.etapes.forEach((e, i) => {
    const verifs = [
      ...(e.verifier ?? []).map((v) => 'texte' in v ? `contient « ${v.texte} »` : 'texteAbsent' in v ? `ne contient pas « ${v.texteAbsent} »`
        : `\`${v.chemin}\` ${Object.entries(v).filter(([k]) => k !== 'chemin').map(([k, x]) => `${k} ${JSON.stringify(x)}`).join(', ')}`),
      ...Object.entries(e.capturer ?? {}).map(([k, v]) => `capture \`${k}\` ← \`${v}\``),
      ...(e.multipart ? ['envoi multipart (fichier)'] : []),
    ]
    lignes.push(`| ${i + 1} | \`${echapper(e.appel)}\` | ${e.anonyme ? 'anonyme' : echapper(e.profil ?? '')} | ${echapper([e.statut ?? '2xx'].flat().join(', '))} | ${echapper(verifs.join(' ; ')) || '—'} |`)
  })
  return lignes.join('\n') + '\n'
}

function rendreScenario(s) {
  const etapes = [...s.feature.contexte.map((e) => `${e}   ⟵ contexte`), ...s.etapes]
  const casUtilises = [...new Set([...s.feature.contexte, ...s.etapes].map((e) => e.match(/j'exécute le cas "([^"]+)"/)?.[1]).filter(Boolean))]
  return [
    `#### ${s.titre}`, '',
    `- **Résultat de la dernière exécution :** ${symbole(s)}`,
    `- **Fonctionnalité :** ${s.feature.titre} (\`${s.feature.chemin}\`)`,
    `- **Exigences éprouvées :** ${s.etiquettes.filter((x) => /^@(LEV|TR|IQ|CHU)-/.test(x)).map((x) => `\`${x.slice(1)}\``).join(', ')}`,
    ...s.notes.map((n) => `- **Note :** ${n}`),
    '', '```gherkin', ...etapes, '```', '',
    ...casUtilises.map(rendreCas),
  ].join('\n')
}

function rendreCatalogue(c) {
  const ex = c.exigences
  const compte = (m) => ex.filter((e) => e.verification === m).length
  const reussis = ex.filter((e) => e.verification === 'bdd' && (parExigence.get(e.id) ?? []).some((s) => resultats.get(s.titre) === 'passed')).length
  const out = [`# Scénarios BDD — ${c.appelOffres}`, '', `*${c.titre}.* Source des exigences : \`${c.source}\`.`, '',
    '> Document généré par `bdd/outils/documenter.mjs` depuis le catalogue, les fonctionnalités Gherkin et les cas JSON ;',
    '> ne pas le modifier à la main. Régénérer : `cd bdd && npm run documenter`.', '',
    '## Synthèse', '', '| Exigences | Éprouvées par scénario | Dont réussies à la dernière exécution | Documentaires | Écarts déclarés | Sans objet |',
    '|---|---|---|---|---|---|',
    `| ${ex.length} | ${compte('bdd')} | ${reussis} | ${compte('documentaire')} | ${compte('ecart')} | ${compte('sans-objet')} |`, '',
    '## Matrice des exigences', '', '| Exigence | Libellé | Caractère | Vérification | Scénarios |', '|---|---|---|---|---|']
  for (const e of ex) {
    const sc = parExigence.get(e.id) ?? []
    out.push(`| \`${e.id}\` | ${echapper(e.libelle)} | ${echapper(e.caractere)} | ${MODES[e.verification]} | ${sc.length ? sc.map((s) => `${symbole(s).split(' ')[0]} [${echapper(s.titre)}](#${ancre(s.titre)})`).join('<br>') : echapper(e.justification ?? '⚠ aucun scénario')} |`)
  }
  out.push('', '## Exigences non éprouvées par un scénario', '')
  for (const e of ex.filter((x) => x.verification === 'documentaire' || x.verification === 'sans-objet')) {
    out.push(`- \`${e.id}\` — ${e.libelle} : **${MODES[e.verification]}.** ${e.justification ?? ''}${e.reference ? ` Référence : \`${e.reference}\`.` : ''}`)
  }
  const ecarts = ex.filter((x) => x.verification === 'ecart')
  if (ecarts.length) {
    out.push('', '## Écarts déclarés', '', 'Chaque écart a son scénario écrit, marqué `@ecart @fixme` : il décrit le comportement attendu et passera au vert une fois l\'écart comblé.', '')
    for (const e of ecarts) out.push(`- \`${e.id}\` — ${e.libelle} : ${e.justification}`)
  }
  out.push('', '## Scénarios détaillés', '')
  const dejaRendus = new Set()
  for (const f of features.filter((f) => f.scenarios.some((s) => s.etiquettes.some((t) => ex.some((e) => `@${e.id}` === t))))) {
    out.push(`### ${f.titre}`, '', f.description ? `${f.description}\n` : '')
    for (const s of scenarios.filter((x) => x.feature === f)) if (!dejaRendus.has(s.titre + f.chemin)) { dejaRendus.add(s.titre + f.chemin); out.push(rendreScenario(s)) }
  }
  return out.join('\n') + '\n'
}
function ancre(t) {
  return t.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^\w\s-]/g, '').trim().replace(/\s+/g, '-')
}

function rendreIndex() {
  const lignes = ['# Scénarios BDD des exigences fonctionnelles — tous les appels d\'offres', '',
    '> Document généré par `bdd/outils/documenter.mjs` ; ne pas le modifier à la main.', '',
    'Chaque exigence de chaque appel d\'offres est rattachée à un ou plusieurs **scénarios Gherkin** (`bdd/features/`), exécutés de',
    'bout en bout contre la plateforme réelle par Playwright. Les jeux de données et les **cas exécutables** (suites d\'appels,',
    'profils, statuts et valeurs attendus) sont en JSON (`bdd/donnees/`). Une exigence qui ne relève pas d\'un test (service,',
    'prix, référence, infrastructure) est déclarée **documentaire** avec sa justification ; un écart connu est **déclaré** et',
    'garde son scénario en attente. La démarche est décrite dans [`bdd/README.md`](../../bdd/README.md).', '',
    '| Appel d\'offres | Document | Exigences | Par scénario | Réussies | Documentaires | Écarts | Sans objet |', '|---|---|---|---|---|---|---|---|']
  for (const c of catalogues) {
    const ex = c.exigences
    const n = (m) => ex.filter((e) => e.verification === m).length
    const ok = ex.filter((e) => e.verification === 'bdd' && (parExigence.get(e.id) ?? []).some((s) => resultats.get(s.titre) === 'passed')).length
    lignes.push(`| ${c.appelOffres} | [${c.cle}.md](${c.cle}.md) | ${ex.length} | ${n('bdd')} | ${ok} | ${n('documentaire')} | ${n('ecart')} | ${n('sans-objet')} |`)
  }
  lignes.push('', `Scénarios : ${scenarios.length} dans ${features.length} fonctionnalités ; cas exécutables : ${Object.keys(cas).length}.`,
    resultats.size ? `Résultats : dernière exécution de \`bdd/test-results/resultats.json\`.` : 'Résultats : aucune exécution enregistrée.')
  if (manquantes.length || inconnues.length) {
    lignes.push('', '## ⚠ Couverture incomplète', '', ...manquantes.map((m) => `- Sans scénario : ${m}`), ...inconnues.map((m) => `- Étiquette inconnue : ${m}`))
  }
  return lignes.join('\n') + '\n'
}

mkdirSync(SORTIE, { recursive: true })
writeFileSync(join(SORTIE, 'README.md'), rendreIndex())
for (const c of catalogues) writeFileSync(join(SORTIE, `${c.cle}.md`), rendreCatalogue(c))
console.log(`Documentation écrite dans ${relative(RACINE, SORTIE)}/ : ${catalogues.length} appels d'offres, ${scenarios.length} scénarios, ${Object.keys(cas).length} cas.`)
for (const c of catalogues) {
  const sans = c.exigences.filter((e) => (e.verification === 'bdd' || e.verification === 'ecart') && !parExigence.has(e.id)).length
  console.log(`  ${c.cle.padEnd(16)} ${c.exigences.length} exigences, ${sans} sans scénario`)
}
if (verifier && (manquantes.length || inconnues.length)) {
  console.error(`\nCouverture incomplète : ${manquantes.length} exigence(s) sans scénario, ${inconnues.length} étiquette(s) inconnue(s).`)
  inconnues.forEach((m) => console.error(`  inconnue : ${m}`))
  process.exit(1)
}
