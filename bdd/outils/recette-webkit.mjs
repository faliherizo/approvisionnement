#!/usr/bin/env node
/**
 * Rapport de recette Safari (TI-11, RST-47) généré depuis le dernier résultat WebKit (test-results-webkit/resultats.json,
 * écrit par `npm run e2e:webkit`) : réussis et échoués par appareil, cause de chaque échec. Le rapport se termine par le
 * gabarit de la recette manuelle sur appareils réels, laissé à remplir : aucun résultat manuel n'est déduit des essais
 * automatisés. Écrit docs/scenarios-e2e/recette-webkit.md.
 */
import { existsSync, readFileSync, writeFileSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'

const BDD = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const RAPPORT = join(BDD, 'test-results-webkit/resultats.json')
const SORTIE = join(BDD, '../docs/scenarios-e2e/recette-webkit.md')

const APPAREILS = {
  'webkit-iphone': 'iPhone 13 (WebKit, 390 × 664, tactile)',
  'webkit-ipad': 'iPad 7e génération (WebKit, 810 × 1080, tactile)',
  'webkit-bureau': 'Safari de bureau (WebKit, 1280 × 720)',
}

/** Tests du rapport JSON de Playwright, à plat : identifiant du scénario, appareil, statut final, première erreur. */
function lireEssais(rapport) {
  const essais = []
  const parcourir = (suite) => {
    for (const spec of suite.specs ?? []) {
      for (const t of spec.tests ?? []) {
        const dernier = t.results?.at(-1)
        const erreur = dernier?.error?.message?.replace(/\u001b\[[0-9;]*m/g, '').split('\n').find((l) => l.trim()) ?? ''
        essais.push({ id: spec.title.split(' — ')[0], titre: spec.title, projet: t.projectName, statut: t.status, erreur })
      }
    }
    for (const s of suite.suites ?? []) parcourir(s)
  }
  for (const s of rapport.suites ?? []) parcourir(s)
  return essais
}

const STATUT = { expected: 'réussi', flaky: 'réussi après reprise', unexpected: 'échoué', skipped: 'non exécuté' }

function bilanParAppareil(essais) {
  const lignes = ['| Appareil (projet Playwright) | Scénarios | Réussis | Échoués | Non exécutés |', '|---|---:|---:|---:|---:|']
  for (const [projet, libelle] of Object.entries(APPAREILS)) {
    const siens = essais.filter((e) => e.projet === projet)
    if (!siens.length) { lignes.push(`| ${libelle} (\`${projet}\`) | — | — | — | non exécuté |`); continue }
    const compte = (...s) => siens.filter((e) => s.includes(e.statut)).length
    lignes.push(`| ${libelle} (\`${projet}\`) | ${siens.length} | ${compte('expected', 'flaky')} | ${compte('unexpected')} | ${compte('skipped')} |`)
  }
  return lignes
}

function detail(essais) {
  const ids = [...new Set(essais.map((e) => e.id))].sort()
  const projets = Object.keys(APPAREILS)
  const lignes = [`| Scénario | ${projets.map((p) => `\`${p}\``).join(' | ')} |`, `|---|${projets.map(() => ':---:').join('|')}|`]
  for (const id of ids) {
    const cellule = (p) => {
      const e = essais.find((x) => x.id === id && x.projet === p)
      return !e ? '—' : e.statut === 'unexpected' ? '❌' : e.statut === 'skipped' ? '⏸' : '✅'
    }
    lignes.push(`| ${id} | ${projets.map(cellule).join(' | ')} |`)
  }
  return lignes
}

function echecs(essais) {
  const ko = essais.filter((e) => e.statut === 'unexpected')
  if (!ko.length) return ['Aucun échec.']
  return ko.map((e) => `- **${e.id}** sur \`${e.projet}\` : ${e.erreur.slice(0, 240).replace(/\|/g, '\\|')}`)
}

/** Défauts que la recette WebKit a révélés, et leur correction : ils ne se voyaient pas sous Chromium. */
const CORRIGES = [
  '## Défauts révélés par la recette WebKit',
  '',
  '| Défaut | Appareils | Correction |',
  '|---|---|---|',
  '| Listes déroulantes à 22 px de haut : Safari dessine la liste native à sa hauteur fixe et ignore marge intérieure et hauteur minimale, sous la cible de 24 px (WCAG 2.5.8) | iPhone, iPad, Safari de bureau | `frontend/src/styles.css` : listes sans apparence native (`appearance: none`), flèche redessinée ; même hauteur (44 px) que les autres champs |',
  '| « Rétablir les paramètres de la plateforme » sans effet : le formulaire donnait le focus à son premier champ, et le toucher qui retire ce focus n\'activait pas le bouton touché | iPhone, iPad | `AdminClm.jsx` (`FormulaireParametres`) : le focus va au formulaire ouvert (`tabIndex=-1`), plus à son premier champ |',
  '',
  'Adaptations du harnais (sans effet sur l\'application) : navigation repliée derrière « Menu » dépliée avant d\'atteindre un',
  'lien qu\'elle cache (téléphone) ; requêtes interrompues par une navigation du harnais, que WebKit signale comme erreurs de',
  'la page, écartées du relevé des erreurs JavaScript.',
]

const GABARIT = [
  '## Recette manuelle sur appareils réels — gabarit à remplir',
  '',
  'Les essais automatisés ci-dessus emploient le moteur WebKit de Playwright avec l\'émulation des appareils : ils ne',
  'remplacent pas un essai sur l\'appareil réel (clavier virtuel, sélecteur natif des listes, appareil photo, mémoire',
  'des mots de passe, politiques de l\'organisation). Chaque ligne est remplie par la personne qui exécute l\'essai ; une',
  'case vide signifie « non éprouvé ».',
  '',
  '| # | Parcours (scénario automatisé de référence) | iPhone — Safari iOS | iPad — Safari iPadOS | Windows 10 — Edge | Windows 10 — Chrome | Windows 11 — Edge | Windows 11 — Chrome |',
  '|---|---|---|---|---|---|---|---|',
  '| 1 | Connexion, changement de langue (FR, EN), déconnexion | | | | | | |',
  '| 2 | Modèle créé, configuré et publié (E2E-CB-IQ-01) | | | | | | |',
  '| 3 | Trousse créée, document déposé, signataires ajoutés, parcours lancé (E2E-ECR-IQ-01) | | | | | | |',
  '| 4 | Validation puis retour pour correction (E2E-CB-IQ-06) | | | | | | |',
  '| 5 | Portail du signataire externe : identité, lecture, consentement, signature tracée au doigt ou à la souris (E2E-ECR-IQ-03) | | | | | | |',
  '| 6 | Refus motivé au portail (E2E-ECR-IQ-03) | | | | | | |',
  '| 7 | Suivi des trousses, relance, listes exportées (E2E-ECR-IQ-06) | | | | | | |',
  '| 8 | Téléchargement du document signé et du dossier de preuve (E2E-CB-IQ-18) | | | | | | |',
  '| 9 | Lecteur d\'écran (VoiceOver sur iOS et iPadOS, Narrateur sur Windows) sur le portail du signataire | | | | | | |',
  '| 10 | Zoom à 200 % et orientation portrait et paysage, sans perte de contenu | | | | | | |',
  '',
  'Pour chaque appareil, consigner sous le tableau :',
  '',
  '| Appareil | Modèle | Version du système | Version du navigateur | Date | Personne | Résultat global | Anomalies (référence) |',
  '|---|---|---|---|---|---|---|---|',
  '| iPhone | | | | | | | |',
  '| iPad | | | | | | | |',
  '| Windows 10 — Edge | | | | | | | |',
  '| Windows 10 — Chrome | | | | | | | |',
  '| Windows 11 — Edge | | | | | | | |',
  '| Windows 11 — Chrome | | | | | | | |',
  '',
  'Légende : ✅ conforme · ❌ anomalie (référence du billet) · ⏸ non éprouvé.',
]

function document(essais, quand) {
  const total = essais.length
  const reussis = essais.filter((e) => ['expected', 'flaky'].includes(e.statut)).length
  return [
    '# Recette Safari et appareils mobiles (TI-11, RST-47)',
    '',
    '> Document généré par `bdd/outils/recette-webkit.mjs` depuis le dernier résultat de `npm run e2e:webkit`',
    '> (`bdd/test-results-webkit/resultats.json`). Ne pas modifier à la main, sauf le gabarit de recette manuelle, à',
    '> recopier dans le procès-verbal de recette avant de le remplir.',
    '',
    '## Essais automatisés sous WebKit (moteur de Safari)',
    '',
    `Exécution : ${quand}. Parcours : besoins du cahier d'IQ (E2E-CB-IQ-*), écarts de l'annexe 10 (E2E-AX-IQ-*),`,
    'création d\'une trousse à l\'écran (E2E-ECR-IQ-01) et portail du signataire externe (E2E-ECR-IQ-03), chacun avec son',
    'contrôle d\'écran (axe-core WCAG 2.2 AA, cibles ≥ 24 px, sans débordement ni superposition aux quatre largeurs).',
    `Chromium (Chrome, Edge) reste couvert par la campagne de référence (\`npm run e2e\`). Bilan : **${reussis} réussis sur ${total}**.`,
    '',
    ...bilanParAppareil(essais),
    '',
    '### Résultat par scénario',
    '',
    ...detail(essais),
    '',
    '### Échecs',
    '',
    ...echecs(essais),
    '',
    ...CORRIGES,
    '',
    ...GABARIT,
    '',
  ].join('\n')
}

if (!existsSync(RAPPORT)) {
  console.error(`✗ Aucun résultat WebKit (${RAPPORT}) : lancer d'abord npm run e2e:webkit.`)
  process.exit(1)
}
const rapport = JSON.parse(readFileSync(RAPPORT, 'utf8'))
const quand = (rapport.stats?.startTime ?? new Date().toISOString()).slice(0, 16).replace('T', ' ') + ' UTC'
const essais = lireEssais(rapport)
writeFileSync(SORTIE, document(essais, quand))
console.log(`Recette WebKit écrite dans docs/scenarios-e2e/recette-webkit.md : ${essais.length} essais.`)
