#!/usr/bin/env node
/**
 * Couverture des fonctionnalités par écran : croise l'inventaire tiré du code (inventaire-ecrans.mjs) avec les traces de
 * la dernière campagne e2e (test-results-e2e/appels/*.json), et écrit docs/scenarios-e2e/ecrans.md.
 *
 * Une fonctionnalité (opération d'API d'un composant) est couverte quand un scénario e2e l'a déclenchée **depuis
 * l'interface**, sur un écran qui rend ce composant, et qu'elle a réussi (statut < 400). Un composant partagé par
 * plusieurs écrans (ReferenceManager…) est couvert une fois, depuis l'un d'eux : il est le même code.
 * Un écran est ouvert quand un scénario l'a affiché.
 *
 *   node outils/couverture-ecrans.mjs              # écrit le document, résume
 *   node outils/couverture-ecrans.mjs --verifier   # échoue s'il reste un écran non ouvert ou une fonctionnalité non couverte
 */
import { readFileSync, readdirSync, writeFileSync, existsSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'

const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const DOC = resolve(RACINE, '../docs/scenarios-e2e/ecrans.md')
const inventaire = JSON.parse(readFileSync(join(RACINE, 'e2e/fonctionnalites/inventaire.json'), 'utf8'))
const { exclusions } = JSON.parse(readFileSync(join(RACINE, 'e2e/fonctionnalites/complements.json'), 'utf8'))
const DOSSIER_TRACES = join(RACINE, 'test-results-e2e/appels')
const traces = existsSync(DOSSIER_TRACES)
  ? readdirSync(DOSSIER_TRACES).filter((f) => f.endsWith('.json')).map((f) => JSON.parse(readFileSync(join(DOSSIER_TRACES, f), 'utf8')))
  : []
// Un scénario en échec ne couvre rien : sa trace ne sert qu'au diagnostic.
traces.splice(0, traces.length, ...traces.filter((t) => t.reussi !== false))

const motif = (gabarit) => new RegExp(`^${gabarit.replace(/[.*+?^$()|[\]\\]/g, '\\$&').replace(/\\?\{\\?\}|\{\}/g, '[^/]+').replace(/:\w+/g, '[^/]+')}/?$`)

// Une adresse d'écran correspond à la route la plus précise : /clm/trousses/nouvelle avant /clm/trousses/:id.
const routes = inventaire.ecrans.map((e) => ({ ...e, re: motif(e.route), precision: e.route.split('/').filter((s) => s && !s.startsWith(':')).length }))
  .sort((a, b) => b.precision - a.precision || b.route.length - a.route.length)
const routeDe = (chemin) => routes.find((r) => r.re.test(chemin))

// Une opération vient d'une ou plusieurs lignes de code (sources). Couvrir une source depuis un écran qui la rend couvre
// cette source partout : un composant partagé est le même code, quel que soit l'écran hôte.
const unitesDe = (op) => op.sources.map((src) => `${op.id}|${src}`)
const ouverts = new Map() // route → scénarios
const couvertes = new Map() // unité → scénarios

for (const t of traces) {
  for (const e of t.ecrans) {
    const r = routeDe(e)
    if (r) (ouverts.get(r.route) ?? ouverts.set(r.route, new Set()).get(r.route)).add(t.scenario)
  }
  for (const a of t.appels) {
    if (!(a.statut < 400)) continue
    const r = routeDe(a.ecran)
    if (!r) continue
    for (const op of r.operations) {
      if (op.methode !== a.methode) continue
      if (!(op.variantes ?? [op.chemin]).some((c) => motif(c).test(a.chemin))) continue
      for (const u of unitesDe(op)) (couvertes.get(u) ?? couvertes.set(u, new Set()).get(u)).add(t.scenario)
    }
  }
}

const exclue = (route, op) => exclusions[`${route} ${op.id}`] ?? exclusions[`* ${op.id}`]
const lignes = []
let nbOps = 0, nbCouvertes = 0, nbExclues = 0
const manquants = []
const ecransFermes = []

for (const e of inventaire.ecrans) {
  const scenOuv = [...(ouverts.get(e.route) ?? [])].sort()
  if (!scenOuv.length && !exclusions[`${e.route} *`]) ecransFermes.push(e.route)
  lignes.push(`\n## \`${e.route}\` — ${e.composant}\n`)
  lignes.push(`- **Ouvert par** : ${scenOuv.length ? scenOuv.slice(0, 8).join(', ') + (scenOuv.length > 8 ? ` (+${scenOuv.length - 8})` : '') : exclusions[`${e.route} *`] ? `sans objet — ${exclusions[`${e.route} *`]}` : '❌ aucun scénario'}`)
  lignes.push(`- **Fichiers** : ${e.fichiers.map((f) => `\`${f}\``).join(', ')}`)
  if (!e.operations.length) { lignes.push('- Aucune opération d\'API : écran d\'affichage seul.'); continue }
  lignes.push('\n| Fonctionnalité | Type | Opération | Couverte par |\n|---|---|---|---|')
  for (const op of e.operations) {
    nbOps++
    const sc = [...new Set(unitesDe(op).flatMap((u) => [...(couvertes.get(u) ?? [])]))].sort()
    const ex = exclue(e.route, op)
    let etat
    if (sc.length) { nbCouvertes++; etat = `✅ ${sc.slice(0, 4).join(', ')}${sc.length > 4 ? ` (+${sc.length - 4})` : ''}` }
    else if (ex) { nbExclues++; etat = `➖ ${ex}` }
    else { etat = '❌'; manquants.push(`${e.route}  ${op.id}  (${op.fonction ?? '?'} — ${op.sources[0]})`) }
    lignes.push(`| \`${op.fonction ?? '—'}\` | ${op.type} | \`${op.methode} ${op.variantes ? op.variantes.join(' ou ') : op.chemin}\` | ${etat} |`)
  }
}

const taux = nbOps ? ((nbCouvertes + nbExclues) / nbOps * 100).toFixed(1) : '0'
const ecransOuverts = inventaire.ecrans.length - ecransFermes.length
const entete = `# Couverture e2e des fonctionnalités, écran par écran

> Généré par \`node outils/couverture-ecrans.mjs\` à partir de l'inventaire tiré du code du frontend
> (\`bdd/e2e/fonctionnalites/inventaire.json\`) et des traces de la dernière campagne e2e. Ne pas éditer.

**Ce que « couvert » veut dire.** Chaque écran (route de \`App.jsx\`) porte des fonctionnalités : lister, consulter,
ajouter, modifier, supprimer, télécharger, et les actions nommées (transférer, attribuer, approuver, signer, déléguer…).
Chacune se traduit par une opération d'API, relevée **dans le code** de l'écran et des composants qu'il rend — et non
déclarée à la main. Une fonctionnalité est couverte quand un scénario e2e l'a déclenchée **depuis l'interface**, sur un
écran qui la porte, avec une réponse en succès. Les préparations de données par l'API ne comptent pas.

| Mesure | Valeur |
|---|---|
| Écrans | ${inventaire.ecrans.length}, dont **${ecransOuverts} ouverts** par au moins un scénario |
| Fonctionnalités (écran × opération) | ${nbOps} |
| Couvertes par un scénario | **${nbCouvertes}** |
| Sans objet (exclusion nominative et motivée) | ${nbExclues} |
| **Taux** | **${taux} %** |

Les scénarios, leurs étapes et leurs données : [README.md](README.md) et \`bdd/e2e/donnees/*.json\`. Les exclusions et
les adresses calculées : \`bdd/e2e/fonctionnalites/complements.json\`.
`
writeFileSync(DOC, entete + lignes.join('\n') + '\n')
console.log(`couverture des écrans : ${ecransOuverts}/${inventaire.ecrans.length} ouverts ; fonctionnalités ${nbCouvertes} couvertes + ${nbExclues} sans objet / ${nbOps} (${taux} %) → ${DOC}`)

if (process.argv.includes('--verifier')) {
  if (ecransFermes.length) console.error(`✗ écrans jamais ouverts (${ecransFermes.length}) :\n  ${ecransFermes.join('\n  ')}`)
  if (manquants.length) console.error(`✗ fonctionnalités non couvertes (${manquants.length}) :\n  ${manquants.join('\n  ')}`)
  if (ecransFermes.length || manquants.length) process.exit(1)
  console.log('✓ tous les écrans ouverts, toutes les fonctionnalités couvertes')
}
