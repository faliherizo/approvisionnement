import { defineConfig, devices } from '@playwright/test'

/**
 * Scénarios e2e d'interface par exigence (bdd/e2e), contre la même plateforme que la suite BDD : serveur :8100 derrière le
 * frontend de recette :5175, bacs à sable démarrés (bdd/README.md § 3).
 */
// Tournage des vidéos d'aide et de présentation (bdd/outils/videos.mjs) : 1280 × 720, enregistrement continu.
const VIDEO = !!process.env.E2E_VIDEO

export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  workers: 1,
  timeout: VIDEO ? 1_200_000 : 240_000,
  expect: { timeout: 15_000 },
  // Un tournage ne remplace pas le dernier résultat d'exécution, dont dépend la documentation.
  reporter: VIDEO ? [['list']] : [['list'], ['html', { open: 'never', outputFolder: 'playwright-report-e2e' }], ['json', { outputFile: 'test-results-e2e/resultats.json' }]],
  outputDir: VIDEO ? 'test-results-e2e/videos-brutes' : 'test-results-e2e/artefacts',
  use: {
    baseURL: process.env.BDD_FRONTEND_URL ?? 'http://localhost:5175',
    actionTimeout: 15_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'],
    ...(VIDEO ? { viewport: { width: 1280, height: 720 }, video: { mode: 'on', size: { width: 1280, height: 720 } } } : {}) } }],
})
