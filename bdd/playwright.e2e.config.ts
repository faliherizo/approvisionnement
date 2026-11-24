import { defineConfig, devices } from '@playwright/test'

/**
 * Scénarios e2e d'interface par exigence (bdd/e2e), contre la même plateforme que la suite BDD : serveur :8100 derrière le
 * frontend de recette :5175, bacs à sable démarrés (bdd/README.md § 3).
 */
export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 240_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report-e2e' }], ['json', { outputFile: 'test-results-e2e/resultats.json' }]],
  outputDir: 'test-results-e2e/artefacts',
  use: {
    baseURL: process.env.BDD_FRONTEND_URL ?? 'http://localhost:5175',
    actionTimeout: 15_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
