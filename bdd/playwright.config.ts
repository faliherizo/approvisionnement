import { defineConfig, devices } from '@playwright/test'
import { defineBddConfig } from 'playwright-bdd'

/**
 * Tests BDD des exigences fonctionnelles de tous les appels d'offres (Gherkin français), exécutés de bout en bout
 * contre la plateforme : serveur :8100 derrière le frontend de recette :5175 (mêmes ports isolés que e2e/).
 */
const testDir = defineBddConfig({
  features: 'features/**/*.feature',
  steps: ['etapes/**/*.ts'],
  language: 'fr',
})

export default defineConfig({
  testDir,
  fullyParallel: false,
  workers: 1,
  timeout: 180_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never' }], ['json', { outputFile: 'test-results/resultats.json' }]],
  use: {
    baseURL: process.env.BDD_FRONTEND_URL ?? 'http://localhost:5175',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
