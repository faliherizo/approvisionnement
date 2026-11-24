#!/usr/bin/env node
/**
 * Documentation et couverture des scénarios e2e d'interface, générées depuis les sources de vérité :
 *   - bdd/exigences/*.json          catalogue des exigences de chaque appel d'offres ;
 *   - bdd/e2e/donnees/*.json        scénarios e2e : profil, exigences démontrées, gestes d'interface, données ;
 *   - bdd/test-results-e2e/resultats.json  dernier résultat d'exécution (rapport JSON de Playwright), s'il existe.
 * Écrit docs/scenarios-e2e/README.md et un document par appel d'offres. Avec --verifier, sort en erreur si une exigence
 * fonctionnelle (vérification « bdd ») n'a aucun scénario e2e, ou si un scénario cite une exigence inconnue.
 */
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'fs'
import { basename, dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'

const BDD = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const RACINE = resolve(BDD, '..')
const SORTIE = join(RACINE, 'docs/scenarios-e2e')
const verifier = process.argv.includes('--verifier')
const lireJson = (f) => JSON.parse(readFileSync(f, 'utf8'))

const ORDRE = ['levis', 'trois-rivieres', 'iq', 'chu']
const catalogues = readdirSync(join(BDD, 'exigences')).filter((f) => f.endsWith('.json'))
  .sort((a, b) => ORDRE.indexOf(basename(a, '.json')) - ORDRE.indexOf(basename(b, '.json')))
  .map((f) => ({ cle: basename(f, '.json'), ...lireJson(join(BDD, 'exigences', f)) }))
const jeux = readdirSync(join(BDD, 'e2e/donnees')).filter((f) => f.endsWith('.json'))
  .map((f) => ({ fichier: f, ...lireJson(join(BDD, 'e2e/donnees', f)) }))

// --- Résultats : statut de chaque scénario, d'après son identifiant en tête du titre du test -----------------------
const resultats = {}
const RAPPORT = join(BDD, 'test-results-e2e/resultats.json')
if (existsSync(RAPPORT)) {
  const parcourir = (suite) => {
    for (const spec of suite.specs ?? []) {
      const id = spec.title.split(' — ')[0]
      const r = spec.tests?.[0]?.results?.at(-1)
      if (r) resultats[id] = r.status === 'passed' ? 'réussi' : r.status === 'skipped' ? 'non exécuté' : 'échoué'
    }
    for (const s of suite.suites ?? []) parcourir(s)
  }
  for (const s of lireJson(RAPPORT).suites ?? []) parcourir(s)
}
// Un scénario absent de cette exécution (exécution partielle, --grep) garde le résultat déjà inscrit dans la documentation.
const precedents = {}
if (existsSync(SORTIE)) {
  for (const f of readdirSync(SORTIE).filter((x) => x.endsWith('.md'))) {
    let id = null
    for (const ligne of readFileSync(join(SORTIE, f), 'utf8').split('\n')) {
      const titre = ligne.match(/^### (E2E-[A-Z0-9-]+) — /)
      if (titre) id = titre[1]
      const r = ligne.match(/^- \*\*Dernier résultat\*\* : (✅ réussi|❌ échoué|⏸ non exécuté)/)
      if (id && r) precedents[id] = r[1]
    }
  }
}
const etat = (id) => ({ 'réussi': '✅ réussi', 'échoué': '❌ échoué', 'non exécuté': '⏸ non exécuté' }[resultats[id]]
  ?? precedents[id] ?? '— pas encore exécuté')

// --- Étapes en langage d'analyste -----------------------------------------------------------------------------------
const liste = (v) => [v].flat().map((x) => `« ${x} »`).join(', ')
const paires = (o) => Object.entries(o).map(([k, v]) => `« ${k} » : ${v === '' ? '(vide)' : `\`${v}\``}`).join(' ; ')
function phrase(e) {
  if ('aller' in e) return `Ouvrir l'écran \`${e.aller}\``
  if ('connecter' in e) return typeof e.connecter === 'string' ? `Se connecter en tant que ${e.connecter}` : `Se connecter avec le compte \`${e.connecter.email}\``
  if ('lire' in e) return `Relever le texte affiché (\`${e.lire.selecteur}\`) sous le nom \`${e.lire.variable}\``
  if ('remplirTotp' in e) return `Saisir le code TOTP courant dans ${Object.keys(e.remplirTotp).map((k) => `« ${k} »`).join(', ')}`
  if ('largeur' in e) return `Afficher l'écran à ${e.largeur} px de large${e.largeur < 600 ? ' (téléphone)' : ''}`
  if ('langue' in e) return `Passer l'interface en ${{ fr: 'français', en: 'anglais', mg: 'malgache' }[e.langue]}`
  if ('cliquer' in e) return typeof e.cliquer === 'string' ? `Activer « ${e.cliquer} »` : `Activer « ${e.cliquer.nom} » (${e.cliquer.role})`
  if ('dansLaLigne' in e) return `Dans la ligne « ${e.dansLaLigne.texte} », activer « ${e.dansLaLigne.cliquer} »`
  if ('remplir' in e) return `Saisir ${paires(e.remplir)}`
  if ('choisir' in e) return `Choisir ${paires(e.choisir)}`
  if ('options' in e) return Object.entries(e.options).map(([k, v]) => `Constater que « ${k} »${v.contient ? ` propose ${liste(v.contient)}` : ''}${v.contient && v.exclut ? ' et' : ''}${v.exclut ? ` ne propose plus ${liste(v.exclut)}` : ''}`).join(' ; ')
  if ('cocher' in e) return `Cocher « ${e.cocher} »`
  if ('decocher' in e) return `Décocher « ${e.decocher} »`
  if ('televerser' in e) return `Joindre « ${e.televerser.fichier.nom} » au champ « ${e.televerser.champ} »`
  if ('voir' in e) return `Voir ${liste(e.voir)}`
  if ('nePasVoir' in e) return `Ne pas voir ${liste(e.nePasVoir)}`
  if ('valeur' in e) return `Constater la valeur ${paires(e.valeur)}`
  if ('voirTitre' in e) return `Voir le titre « ${e.voirTitre} »`
  if ('url' in e) return `Arriver sur une adresse de la forme \`${e.url}\``
  if ('preparer' in e) return typeof e.preparer === 'string' ? `Préparer par l'API : cas \`${e.preparer}\``
    : `Préparer par l'API : ${e.preparer.description}`
  if ('telecharger' in e) return `Télécharger par « ${e.telecharger} » et obtenir un fichier non vide`
  if ('dansLaFenetre' in e) return `Dans la fenêtre ouverte, activer « ${e.dansLaFenetre} »`
  if ('dansLaSection' in e) return `Dans la section « ${e.dansLaSection.titre} », activer « ${e.dansLaSection.cliquer} »`
  if ('cliquerTexte' in e) return `Activer l'élément « ${e.cliquerTexte} » (ligne ou carte cliquable)`
  if ('tracer' in e) return 'Tracer une signature manuscrite sur le pavé de signature'
  if ('repondre' in e) return `Répondre « ${e.repondre} » à la prochaine saisie demandée par le navigateur`
  if ('ecranConforme' in e) return "Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px"
  return JSON.stringify(e)
}

// --- Nature d'un scénario : parcours d'interface, ou préparation par l'API dont l'écran ne fait que constater ------------
// Au moins trois gestes d'utilisateur (saisir, choisir, activer, joindre…) : l'exigence est vécue à l'écran.
const GESTES = ['cliquer', 'remplir', 'choisir', 'cocher', 'decocher', 'dansLaLigne', 'dansLaFenetre', 'dansLaSection',
  'cliquerTexte', 'televerser', 'telecharger', 'tracer', 'repondre', 'remplirTotp']
const gestes = (sc) => sc.etapes.filter((e) => GESTES.some((g) => g in e)).length
const parInterface = (sc) => gestes(sc) >= 3
const nature = (sc) => parInterface(sc) ? `parcours d'interface (${gestes(sc)} gestes)` : 'préparé par l\'API, écran constaté'

// --- Couverture ------------------------------------------------------------------------------------------------------
const parExigence = {}
const inconnues = []
const connues = new Set(catalogues.flatMap((c) => c.exigences.map((e) => e.id)))
for (const j of jeux) for (const s of j.scenarios) for (const x of s.exigences) {
  if (!connues.has(x)) inconnues.push(`${s.id} cite ${x}, absente des catalogues`)
  ;(parExigence[x] ??= []).push({ ...s, fichier: j.fichier })
}
const fonctionnelles = (c) => c.exigences.filter((e) => e.verification === 'bdd')
const sansScenario = catalogues.flatMap((c) => fonctionnelles(c).filter((e) => !parExigence[e.id]).map((e) => e.id))

if (verifier) {
  for (const m of inconnues) console.error(`✗ ${m}`)
  for (const id of sansScenario) console.error(`✗ ${id} : exigence fonctionnelle sans scénario e2e`)
  if (inconnues.length || sansScenario.length) process.exit(1)
  console.log(`✓ couverture e2e : ${catalogues.reduce((n, c) => n + fonctionnelles(c).length, 0)} exigences fonctionnelles, toutes couvertes`)
  process.exit(0)
}

// --- Documents --------------------------------------------------------------------------------------------------------
mkdirSync(SORTIE, { recursive: true })
const cellule = (t) => String(t ?? '').replace(/\|/g, '\\|').replace(/\n/g, ' ')
const lignesReadme = [
  '# Scénarios e2e d\'interface, par exigence', '',
  '> Document généré par `bdd/outils/documenter-e2e.mjs` depuis `bdd/exigences/*.json` (exigences),',
  '> `bdd/e2e/donnees/*.json` (scénarios et données de test) et le dernier résultat d\'exécution. Ne pas modifier à la main.', '',
  '## Principe', '',
  '- **Ce qui est vérifié** : chaque exigence fonctionnelle est démontrée depuis l\'interface, comme un utilisateur du',
  '  profil concerné la vivrait. Il se connecte, ouvre l\'écran, saisit, choisit, active, joint un fichier, puis constate',
  '  le résultat à l\'écran.',
  '- **Préparation par l\'API** : seule la mise en place que l\'exigence ne vise pas passe par l\'API (ex. un devis à',
  '  commenter). Elle est marquée « Préparer » dans les étapes.',
  '- **Contrôle d\'écran** : chaque écran visité est contrôlé selon WCAG 2.2 AA et SGQRI 008 (axe-core), avec des cibles',
  '  ≥ 24 px, sans débordement ni superposition, aux quatre largeurs de référence.',
  '- **Données** : les données de test sont dans les fichiers JSON, jamais dans le code du moteur',
  '  (`bdd/e2e/exigences.spec.ts`). `${unique}` rend chaque exécution indépendante des précédentes.',
  '- **Complément de la suite BDD** : la suite BDD (`docs/scenarios-bdd/`) vérifie les règles par l\'API sur tous les',
  '  cas limites ; les scénarios e2e vérifient le parcours d\'interface.', '',
  '## Vocabulaire des étapes (JSON)', '',
  '| Clé | Geste | Exemple |', '|---|---|---|',
  '| `aller` | ouvrir un écran | `{ "aller": "/recherche" }` |',
  '| `connecter` | changer d\'utilisateur | `{ "connecter": "juriste de Lévis" }` |',
  '| `langue` | changer la langue de l\'interface | `{ "langue": "en" }` |',
  '| `largeur` | afficher l\'écran à une largeur donnée (téléphone : 390) | `{ "largeur": 390 }` |',
  '| `cliquer` | activer un bouton ou un lien, par son nom accessible | `{ "cliquer": "Rechercher" }` |',
  '| `dansLaLigne` | agir sur une ligne de tableau | `{ "dansLaLigne": { "texte": "Inscription", "cliquer": "Concevoir" } }` |',
  '| `remplir` | saisir des champs, par leur libellé ; « Section » Libellé » limite à une section, « Libellé #2 » vise la 2e occurrence | `{ "remplir": { "Terme": "garantie" } }` |',
  '| `choisir` | choisir dans une liste, par libellé ou par code | `{ "choisir": { "Catégorie": "OPTIONNELLE" } }` |',
  '| `options` | constater les options d\'une liste (présentes, absentes) | `{ "options": { "Unité responsable": { "contient": ["Innovation"], "exclut": ["Ancienne"] } } }` |',
  '| `cocher` / `decocher` | case à cocher, par son libellé | `{ "cocher": "Clauses normatives seulement" }` |',
  '| `televerser` | joindre un fichier | `{ "televerser": { "champ": "Pièce", "fichier": { "fichier": "pdf", "nom": "a.pdf" } } }` |',
  '| `voir` / `nePasVoir` | constater un texte à l\'écran | `{ "voir": ["Circuit créé"] }` |',
  '| `valeur` | constater la valeur d\'un champ (ex. calculé) | `{ "valeur": { "Total": "600" } }` |',
  '| `voirTitre` / `url` | constater le titre ou l\'adresse | `{ "url": "/documents/\\\\d+" }` |',
  '| `telecharger` | télécharger un fichier et vérifier qu\'il n\'est pas vide | `{ "telecharger": "Exporter en CSV" }` |',
  '| `preparer` | mise en place par l\'API (cas BDD nommé ou étapes en ligne) | `{ "preparer": "lev.clauses.recherche-multicritere" }` |',
  '| `ecranConforme` | contrôle d\'accessibilité et de mise en page | `{ "ecranConforme": true }` |',
  '| `dansLaFenetre` | activer un bouton de la fenêtre de dialogue ouverte (confirmation) | `{ "dansLaFenetre": "Supprimer" }` |',
  '| `dansLaSection` | activer un bouton d\'une section, repérée par le début de son titre | `{ "dansLaSection": { "titre": "Annexes", "cliquer": "Supprimer" } }` |',
  '| `cliquerTexte` | activer un élément cliquable par son texte (ligne de tableau, carte) | `{ "cliquerTexte": "Dossier E2E" }` |',
  '| `lire` / `remplirTotp` | relever un texte affiché (ex. secret TOTP), puis saisir le code calculé à partir de lui | `{ "lire": { "selecteur": "code", "variable": "secret" } }` |',
  '| `tracer` | tracer une signature manuscrite à la souris sur le pavé de signature | `{ "tracer": true }` |',
  '| `repondre` | réponse à une saisie native du navigateur (motif demandé par `window.prompt`) ; les confirmations sont acceptées | `{ "repondre": "Départ à la retraite" }` |', '',
  'Chaque scénario consigne aussi les écrans ouverts et les appels d\'API faits depuis chacun : la couverture des fonctionnalités écran par écran en découle ([ecrans.md](ecrans.md)).', '',
  '## Exécution', '',
  'Même plateforme que la suite BDD : serveur :8100 branché sur les bacs à sable, frontend de recette :5175 (`bdd/README.md`).', '',
  '```bash', 'cd bdd', 'npm run e2e              # exécute les scénarios, puis régénère ce dossier', 'npm run e2e:couverture   # échoue si une exigence fonctionnelle n\'a aucun scénario e2e',
  'npx playwright test -c playwright.e2e.config.ts --grep @LEV-C-02   # les scénarios d\'une exigence', '```', '',
  '## Couverture', '',
  '| Appel d\'offres | Document | Exigences fonctionnelles | Couvertes en e2e | Scénarios | Réussis |', '|---|---|---:|---:|---:|---:|']
for (const c of catalogues) {
  const f = fonctionnelles(c)
  const scenarios = new Set(f.flatMap((e) => (parExigence[e.id] ?? []).map((s) => s.id)))
  const reussis = [...scenarios].filter((id) => etat(id).startsWith('✅')).length
  lignesReadme.push(`| ${cellule(c.appelOffres)} | [${c.cle}.md](${c.cle}.md) | ${f.length} | ${f.filter((e) => parExigence[e.id]).length} | ${scenarios.size} | ${reussis} |`)
}
writeFileSync(join(SORTIE, 'README.md'), lignesReadme.join('\n') + '\n')

for (const c of catalogues) {
  const l = [`# ${c.appelOffres} — scénarios e2e par exigence`, '',
    '> Généré par `bdd/outils/documenter-e2e.mjs`. Méthode et vocabulaire : [README](README.md).', '']
  for (const e of c.exigences) {
    l.push(`## ${e.id} — ${e.libelle.replace(/\*/g, '')}`, '')
    l.push(`- **Source** : ${e.source}`, `- **Conformité déclarée** : ${e.conformiteDeclaree ?? '—'}`)
    const s = parExigence[e.id] ?? []
    if (e.verification !== 'bdd') {
      l.push(`- **Vérification** : ${e.verification}${e.justification ? ` — ${e.justification}` : ''}`, '')
      continue
    }
    if (!s.length) { l.push('- **Scénario e2e** : aucun (écart de couverture)', ''); continue }
    l.push(`- **Démontrée à l'écran** : ${s.some(parInterface) ? 'oui' : 'non — seulement préparée par l\'API'}`, '')
    for (const sc of s) {
      l.push(`### ${sc.id} — ${sc.titre}`, '',
        `- **Profil** : ${sc.profil}${sc.organisation ? ` (organisation ${sc.organisation})` : ''}`,
        `- **Exigences démontrées** : ${sc.exigences.join(', ')}`,
        `- **Données** : \`bdd/e2e/donnees/${sc.fichier}\``,
        `- **Nature** : ${nature(sc)}`,
        `- **Dernier résultat** : ${etat(sc.id)}`, '')
      sc.etapes.forEach((x, i) => l.push(`${i + 1}. ${phrase(x)}`))
      l.push('')
    }
  }
  writeFileSync(join(SORTIE, `${c.cle}.md`), l.join('\n'))
}
// --- Cahier des besoins d'IQ : chaque exigence du cahier, par section, avec ses parcours et les constats -----------------
const iq = catalogues.find((c) => c.cle === 'iq')
if (iq) {
  const constats = jeux.flatMap((j) => j.constats ?? [])
  const cahier = iq.exigences.filter((e) => e.id.startsWith('IQ-C-'))
  const sections = [...new Set(cahier.map((e) => e.source))]
  const lien = (sc) => `${sc.id}${etat(sc.id).startsWith('✅') ? ' ✅' : etat(sc.id).startsWith('❌') ? ' ❌' : ''}`
  const l = ['# IQ 2026-6370-0005 — Cahier des besoins : scénarios e2e par exigence', '',
    '> Généré par `bdd/outils/documenter-e2e.mjs` depuis `bdd/exigences/iq.json` (exigences du cahier, IQ-C-*) et',
    '> `bdd/e2e/donnees/*.json` (scénarios, données et constats). Détail des étapes de chaque scénario : [iq.md](iq.md).', '',
    '**Parcours d\'interface** : l\'utilisateur du profil saisit, configure, modifie et décide à l\'écran (au moins trois',
    'gestes). **Préparé par l\'API** : la situation est mise en place par l\'API et l\'écran ne fait que la constater ;',
    'un tel scénario ne détecte pas un défaut de l\'écran. Les scénarios `E2E-CB-IQ-*` (`cahier-iq.json`) sont écrits',
    'besoin par besoin, sans préparation par l\'API.', '']
  const demontrees = cahier.filter((e) => e.verification === 'bdd' && (parExigence[e.id] ?? []).some(parInterface))
  const fonct = cahier.filter((e) => e.verification === 'bdd')
  l.push(`**Couverture** : ${demontrees.length} / ${fonct.length} exigences fonctionnelles du cahier démontrées par un parcours d'interface ;`,
    `${cahier.length - fonct.length} exigences à vérification documentaire.`, '')
  for (const sec of sections) {
    l.push(`## Section ${sec}`, '', '| Exigence | Besoin | Parcours d\'interface | Préparés par l\'API |', '|---|---|---|---|')
    for (const e of cahier.filter((x) => x.source === sec)) {
      const s = parExigence[e.id] ?? []
      const ui = s.filter(parInterface).map(lien).join(', ')
      const api = s.filter((x) => !parInterface(x)).map(lien).join(', ')
      const besoin = cellule(e.libelle.replace(/\*/g, ''))
      if (e.verification !== 'bdd') l.push(`| ${e.id} | ${besoin} | — vérification ${e.verification} | — |`)
      else l.push(`| ${e.id} | ${besoin} | ${ui || '**aucun**'} | ${api || '—'} |`)
    }
    l.push('')
  }
  if (constats.length) {
    l.push('## Défauts et constats relevés par ces parcours', '',
      '| # | Exigences | Scénario | Constat | Suite donnée |', '|---|---|---|---|---|')
    for (const c of constats) l.push(`| ${c.id} | ${c.exigences.join(', ')} | ${c.scenario} | ${cellule(c.constat)} | ${cellule(c.suite)} |`)
    l.push('')
  }
  writeFileSync(join(SORTIE, 'iq-cahier-des-besoins.md'), l.join('\n'))
}
console.log(`docs/scenarios-e2e : ${catalogues.length} documents ; ${sansScenario.length} exigence(s) fonctionnelle(s) sans scénario e2e`)
