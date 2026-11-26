import { defineConfig, devices } from '@playwright/test'

/**
 * Scénarios e2e d'interface par exigence (bdd/e2e), contre la même plateforme que la suite BDD : serveur :8100 derrière le
 * frontend de recette :5175, bacs à sable démarrés (bdd/README.md § 3).
 */
// Tournage des vidéos d'aide et de présentation (bdd/outils/videos.mjs) : 1280 × 720, enregistrement continu.
const VIDEO = !!process.env.E2E_VIDEO
// Recette Safari (TI-11) : E2E_WEBKIT=1 remplace Chromium par les projets WebKit — iPhone, iPad, Safari de bureau —, tous
// exécutés à défaut de --project. Leurs résultats vont dans test-results-webkit/ : ils ne remplacent jamais la campagne de
// référence (Chromium), dont dépendent la documentation et la couverture des écrans.
const WEBKIT = !!process.env.E2E_WEBKIT
const SORTIE = WEBKIT ? 'test-results-webkit' : 'test-results-e2e'

const CHROMIUM = { name: 'chromium', use: { ...devices['Desktop Chrome'],
  ...(VIDEO ? { viewport: { width: 1280, height: 720 }, video: { mode: 'on' as const, size: { width: 1280, height: 720 } } } : {}) } }

/** Appareils de la recette Safari : téléphone (390 px), tablette (810 px) et poste de travail, tous sous le moteur WebKit. */
const PROJETS_WEBKIT = [
  { name: 'webkit-iphone', use: { ...devices['iPhone 13'] } },
  { name: 'webkit-ipad', use: { ...devices['iPad (gen 7)'] } },
  { name: 'webkit-bureau', use: { ...devices['Desktop Safari'] } },
]

export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  workers: 1,
  timeout: VIDEO ? 1_200_000 : 240_000,
  expect: { timeout: 15_000 },
  // Un tournage ne remplace pas le dernier résultat d'exécution, dont dépend la documentation.
  reporter: VIDEO ? [['list']] : [['list'], ['html', { open: 'never', outputFolder: `playwright-report-${WEBKIT ? 'webkit' : 'e2e'}` }],
    ['json', { outputFile: `${SORTIE}/resultats.json` }]],
  outputDir: VIDEO ? 'test-results-e2e/videos-brutes' : `${SORTIE}/artefacts`,
  use: {
    baseURL: process.env.BDD_FRONTEND_URL ?? 'http://localhost:5175',
    actionTimeout: 15_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: WEBKIT ? PROJETS_WEBKIT : [CHROMIUM],
})
