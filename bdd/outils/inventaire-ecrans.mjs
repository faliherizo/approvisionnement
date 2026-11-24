#!/usr/bin/env node
/**
 * Inventaire des fonctionnalités de chaque écran, tiré du code du frontend (et non déclaré à la main) : pour chaque route
 * de App.jsx, le composant qu'elle rend, les composants qu'il importe, et chaque opération d'API qu'ils déclenchent
 * (lister, consulter, ajouter, modifier, supprimer, actions nommées : transférer, attribuer, approuver…).
 *
 * Une opération d'API est la trace observable d'une fonctionnalité : la porte de couverture (couverture-ecrans.mjs) exige
 * que chacune soit déclenchée par un scénario e2e, depuis un écran qui la porte.
 *
 * Les appels dont l'adresse est calculée (`api.get(base)`) ne se lisent pas statiquement : ils sont déclarés dans
 * e2e/fonctionnalites/complements.json, et l'inventaire échoue sur un appel dynamique non déclaré.
 *
 *   node outils/inventaire-ecrans.mjs            # écrit e2e/fonctionnalites/inventaire.json
 */
import { readFileSync, writeFileSync, existsSync, mkdirSync } from 'fs'
import { dirname, join, relative, resolve } from 'path'
import { fileURLToPath } from 'url'

const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const SRC = resolve(RACINE, '../frontend/src')
const SORTIE = join(RACINE, 'e2e/fonctionnalites')
const COMPLEMENTS = JSON.parse(readFileSync(join(SORTIE, 'complements.json'), 'utf8'))

/* ── Lecture de fichier, découpée en déclarations de premier niveau ───────────────────────────────────────────── */

const cache = new Map()
function module_(fichier) {
  if (cache.has(fichier)) return cache.get(fichier)
  const texte = readFileSync(fichier, 'utf8')
  const lignes = texte.split('\n')
  const decl = []
  lignes.forEach((l, i) => {
    const m = l.match(/^(export\s+)?(default\s+)?(async\s+)?function\s+(\w+)/) || l.match(/^(export\s+)?(default\s+)?const\s+(\w+)\s*=/)
    if (m) decl.push({ nom: m[4] ?? m[3], debut: i, defaut: !!m[2] })
  })
  decl.forEach((d, k) => { d.fin = k + 1 < decl.length ? decl[k + 1].debut : lignes.length })
  const defaut = decl.find((d) => d.defaut)?.nom ?? texte.match(/^export\s+default\s+(\w+)/m)?.[1]
  const imports = []
  for (const m of texte.matchAll(/^import\s+(.+?)\s+from\s+['"](\.[^'"]+)['"]/gm)) {
    const cible = resoudre(dirname(fichier), m[2])
    if (!cible) continue
    const tete = m[1]
    const def = tete.match(/^(\w+)/)?.[1]
    if (def) imports.push({ local: def, fichier: cible, nom: 'default' })
    for (const n of tete.match(/\{([^}]*)\}/)?.[1].split(',') ?? []) {
      const [orig, alias] = n.trim().split(/\s+as\s+/)
      if (orig) imports.push({ local: alias ?? orig, fichier: cible, nom: orig })
    }
  }
  const mod = { fichier, texte, lignes, decl, defaut, imports }
  cache.set(fichier, mod)
  return mod
}

function resoudre(base, chemin) {
  for (const ext of ['', '.jsx', '.js']) {
    const f = resolve(base, chemin + ext)
    if (existsSync(f) && /\.(jsx?|js)$/.test(f)) return f
  }
  return null
}

const bloc = (mod, d) => mod.lignes.slice(d.debut, d.fin).join('\n')

/**
 * Le code sans ses chaînes ni ses commentaires (les ${…} des gabarits restent) : un nom de composant cité dans un texte
 * ou un commentaire n'est pas une référence.
 */
function codeSeul(texte) {
  let sortie = ''
  const pile = []
  for (let i = 0; i < texte.length; i++) {
    const c = texte[i], haut = pile[pile.length - 1]
    if (haut === "'" || haut === '"') { if (c === '\\') i++; else if (c === haut || c === '\n') pile.pop(); continue }
    if (haut === '`') {
      if (c === '\\') i++
      else if (c === '`') pile.pop()
      else if (c === '$' && texte[i + 1] === '{') { pile.push('$'); i++; sortie += ' ' }
      continue
    }
    if (c === '/' && texte[i + 1] === '/' && texte[i - 1] !== ':') { while (i < texte.length && texte[i] !== '\n') i++; sortie += '\n'; continue }
    if (c === '/' && texte[i + 1] === '*') { const f = texte.indexOf('*/', i + 2); i = f < 0 ? texte.length : f + 1; continue }
    if (c === "'" || c === '"' || c === '`') { pile.push(c); sortie += ' '; continue }
    if (haut === '$' && c === '}') { pile.pop(); continue }
    if (c === '{' && haut === '$') { pile.push('{'); sortie += c; continue }
    if (c === '}' && haut === '{') { pile.pop(); sortie += c; continue }
    sortie += c
  }
  return sortie
}

/** Les déclarations d'un composant : la sienne et, de proche en proche, les déclarations locales qu'il nomme. */
function declarationsDe(mod, nom) {
  const racine = mod.decl.find((d) => d.nom === (nom === 'default' ? mod.defaut : nom))
  if (!racine) return mod.decl // composant introuvable : tout le fichier, par prudence
  const retenues = new Set([racine])
  const aVoir = [racine]
  while (aVoir.length) {
    const texte = codeSeul(bloc(mod, aVoir.pop()))
    for (const d of mod.decl) if (!retenues.has(d) && new RegExp(`\\b${d.nom}\\b`).test(texte)) { retenues.add(d); aVoir.push(d) }
  }
  return [...retenues]
}

/* ── Extraction des appels d'API ──────────────────────────────────────────────────────────────────────────────── */

/** Premier argument d'un appel ouvert à `i` (la parenthèse), en respectant chaînes, gabarits et imbrications. */
function premierArgument(texte, i) {
  // Pile de contextes : '(' '[' '{' (code), '$' (code d'un ${…} de gabarit), et les chaînes ' " `.
  const pile = []
  let j = i + 1
  for (; j < texte.length; j++) {
    const c = texte[j]
    const haut = pile[pile.length - 1]
    if (haut === "'" || haut === '"') { if (c === '\\') j++; else if (c === haut) pile.pop(); continue }
    if (haut === '`') {
      if (c === '\\') j++
      else if (c === '`') pile.pop()
      else if (c === '$' && texte[j + 1] === '{') { pile.push('$'); j++ }
      continue
    }
    if (c === "'" || c === '"' || c === '`') { pile.push(c); continue }
    if ('([{'.includes(c)) { pile.push(c); continue }
    if (')]}'.includes(c)) { if (!pile.length) break; pile.pop(); continue }
    if (c === ',' && !pile.length) break
  }
  return texte.slice(i + 1, j)
}

/** Chemins littéraux d'un argument : chaque chaîne ou gabarit qui commence par « / », les ${…} devenus {}. */
function chemins(arg) {
  const resultat = []
  const normal = arg.replace(/\$\{(?:[^{}]|\{[^{}]*\})*\}/g, '{}')
  // Un gabarit collé à la fin d'un segment (`/journal${qs}`, `/apercu-image${suffixe}`) est une chaîne de requête.
  for (const m of normal.matchAll(/[`'"](\/[^`'"]*)[`'"]/g)) resultat.push(m[1].split('?')[0].replace(/([^/])\{\}$/, '$1').replace(/\/+$/, '') || '/')
  return [...new Set(resultat)]
}

const APPELS = [
  { re: /\bapi\.(get|post|put|patch|delete)\(/g, methode: (m) => m[1].toUpperCase() },
  { re: /\bportail\.(get|post|put|patch|delete)\(/g, methode: (m) => m[1].toUpperCase(), prefixe: '/clm/portail' },
  { re: /\buseRessource\(/g, methode: () => 'GET' },
  { re: /\btelecharger\(/g, methode: () => 'GET' },
]

/** Le nom de la fonction qui porte l'appel (gestionnaire, effet), pour nommer la fonctionnalité. */
function fonctionEnglobante(lignes, ligne) {
  // Un gestionnaire en ligne (onClick={() => api.delete(…)}) n'a pas de nom : la fonction précédente serait trompeuse.
  if (/\bon[A-Z]\w*=\{/.test(lignes[ligne]) && /=>/.test(lignes[ligne])) return `gestionnaire en ligne (${lignes[ligne].match(/\bon[A-Z]\w*/)[0]})`
  for (let k = ligne; k >= Math.max(0, ligne - 60); k--) {
    const m = lignes[k].match(/(?:async\s+)?function\s+(\w+)/) || lignes[k].match(/const\s+(\w+)\s*=\s*(?:useCallback\()?\s*(?:async\s*)?\(/)
      || lignes[k].match(/^\s*(\w+)\s*\([^)]*\)\s*\{/)
    if (m && !['if', 'for', 'while', 'switch', 'catch'].includes(m[1])) return m[1]
  }
  return null
}

function typeOperation(methode, chemin) {
  const dernier = chemin.split('/').filter((s) => s && s !== '{}').pop() ?? ''
  if (methode === 'DELETE') return 'supprimer'
  if (methode === 'PUT' || methode === 'PATCH') return 'modifier'
  if (methode === 'GET') return /export|telecharg|download|contenu|pdf|docx|csv|xml|archive|preuve/i.test(chemin) ? 'télécharger' : chemin.endsWith('{}') ? 'consulter' : 'lister'
  return /^(transferer|attribuer|assigner|reassigner|approuver|refuser|rejeter|valider|soumettre|publier|signer|envoyer|annuler|activer|desactiver|archiver|restaurer|cloturer|lancer|relancer|demarrer|retourner|deleguer|importer|exporter|dupliquer|deplacer|verrouiller|deverrouiller|resoudre|rouvrir|capturer|emettre|generer|assembler)/i.test(dernier)
    ? dernier : 'ajouter'
}

const dejaVu = new Set()
function operationsDe(mod, declarations, sansComplement) {
  const ops = []
  for (const d of declarations) {
    const texte = bloc(mod, d)
    const decalage = mod.lignes.slice(0, d.debut).join('\n').length + (d.debut ? 1 : 0)
    for (const forme of APPELS) {
      for (const m of texte.matchAll(forme.re)) {
        const pos = m.index + m[0].length - 1
        const ligneAbs = mod.texte.slice(0, decalage + pos).split('\n').length - 1
        const arg = premierArgument(texte, pos).trim()
        const source = `${relative(SRC, mod.fichier)}:${ligneAbs + 1}`
        let trouves = chemins(arg)
        if (!trouves.length) {
          // Relais (le paramètre d'une aide comme useRessource) ou adresse calculée : déclarée en complément.
          const cle = `${relative(SRC, mod.fichier)}|${arg}`
          const c = COMPLEMENTS.dynamiques[cle]
          if (!c) { if (!dejaVu.has(cle)) { dejaVu.add(cle); sansComplement.push(`${source} : ${m[0]}${arg})`) } continue }
          if (c.relais) continue
          trouves = c.variantes ? [c.chemins] : c.chemins
        }
        for (const ch of trouves) {
          const variantes = [ch].flat().map((x) => (forme.prefixe ?? '') + x)
          const chemin = variantes[0]
          const methode = forme.methode(m)
          ops.push({ methode, chemin, ...(variantes.length > 1 ? { variantes } : {}), type: typeOperation(methode, chemin), fonction: fonctionEnglobante(mod.lignes, ligneAbs), source })
        }
      }
    }
    for (const m of texte.matchAll(/\b(?:useListe|useReference)\(\s*['"](\w+)['"]/g)) {
      const ligneAbs = mod.texte.slice(0, decalage + m.index).split('\n').length - 1
      ops.push({ methode: 'GET', chemin: `/reference/${m[1]}`, type: 'lister', fonction: m[0].split('(')[0], source: `${relative(SRC, mod.fichier)}:${ligneAbs + 1}` })
    }
    for (const m of texte.matchAll(/href=\{?[`'"]\/api(\/[^`'"]*)[`'"]/g)) {
      const ligneAbs = mod.texte.slice(0, decalage + m.index).split('\n').length - 1
      const chemin = m[1].replace(/\$\{[^}]*\}/g, '{}').split('?')[0]
      ops.push({ methode: 'GET', chemin, type: 'télécharger', fonction: 'lien', source: `${relative(SRC, mod.fichier)}:${ligneAbs + 1}` })
    }
  }
  return ops
}

/** Un composant et tout ce qu'il importe : fichiers et opérations. */
function composant(fichier, nom, sansComplement, vus = new Set()) {
  const cle = `${fichier}#${nom}`
  if (vus.has(cle)) return { fichiers: [], ops: [] }
  vus.add(cle)
  const mod = module_(fichier)
  const decls = declarationsDe(mod, nom)
  const ops = operationsDe(mod, decls, sansComplement)
  const fichiers = [relative(SRC, fichier)]
  const textes = codeSeul(decls.map((d) => bloc(mod, d)).join('\n'))
  for (const imp of mod.imports) {
    if (!/\.jsx$/.test(imp.fichier) && !/hooks\//.test(imp.fichier)) continue
    if (!new RegExp(`\\b${imp.local}\\b`).test(textes)) continue
    const sous = composant(imp.fichier, imp.nom, sansComplement, vus)
    fichiers.push(...sous.fichiers)
    ops.push(...sous.ops)
  }
  return { fichiers: [...new Set(fichiers)], ops }
}

/* ── Routes ───────────────────────────────────────────────────────────────────────────────────────────────────── */

const APP = join(SRC, 'App.jsx')
const app = module_(APP)
const ENVELOPPES = new Set(['ProtectedRoute', 'RouteModule', 'Navigate', 'Routes', 'Route'])
const routes = []
for (const m of app.texte.matchAll(/<Route\s+path="([^"]+)"\s+element=\{(.+?)\}\s*\/>/g)) {
  const [, chemin, element] = m
  if (chemin === '*') continue
  const noms = [...element.matchAll(/<(\w+)/g)].map((x) => x[1]).filter((n) => !ENVELOPPES.has(n))
  const nom = noms.pop() ?? element.match(/socle\(<(\w+)/)?.[1]
  if (nom) routes.push({ chemin, composant: nom, espace: m.index < app.texte.indexOf('path="/accueil"') ? 'clm' : 'socle' })
}

const sansComplement = []
const ecrans = routes.map((r) => {
  const imp = app.imports.find((i) => i.local === r.composant)
  const { fichiers, ops } = imp ? composant(imp.fichier, imp.nom, sansComplement) : composant(APP, r.composant, sansComplement)
  const parCle = new Map()
  for (const o of ops) {
    const id = `${o.methode} ${o.chemin}`
    if (!parCle.has(id)) parCle.set(id, { id, ...o, sources: [] })
    parCle.get(id).sources.push(o.source)
  }
  const operations = [...parCle.values()].map(({ source, ...o }) => ({ ...o, sources: [...new Set(o.sources)] }))
    .sort((a, b) => a.id.localeCompare(b.id))
  return { route: r.chemin, espace: r.espace, composant: r.composant, fichiers, operations }
})

// La coquille (menus, modules, session) appelle l'API sur tous les écrans : elle est inventoriée à part.
const coquille = ['App.jsx', 'auth.jsx', 'pages/clm/ClmShell.jsx'].map((f) => {
  const mod = module_(join(SRC, f))
  return operationsDe(mod, mod.decl, sansComplement)
}).flat()

if (sansComplement.length) {
  console.error(`✗ appels d'API à adresse calculée, non déclarés dans e2e/fonctionnalites/complements.json :\n  ${sansComplement.join('\n  ')}`)
  process.exit(1)
}

mkdirSync(SORTIE, { recursive: true })
const inventaire = {
  genere: 'node outils/inventaire-ecrans.mjs — ne pas éditer',
  ecrans,
  coquille: [...new Map(coquille.map((o) => [`${o.methode} ${o.chemin}`, { id: `${o.methode} ${o.chemin}`, ...o }])).values()],
}
writeFileSync(join(SORTIE, 'inventaire.json'), JSON.stringify(inventaire, null, 2) + '\n')
const total = ecrans.reduce((n, e) => n + e.operations.length, 0)
console.log(`✓ inventaire : ${ecrans.length} écrans, ${total} opérations (écran × opération), coquille ${inventaire.coquille.length}`)
