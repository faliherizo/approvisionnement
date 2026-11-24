import { defineConfig, devices } from '@playwright/test'

/**
 * Recette d'un environnement déployé (E2E_BASE_URL), en lecture seule — .github/workflows/recette-e2e.yml.
 *   E2E_BASE_URL=https://contrat.vinaitsolution.com npx playwright test -c playwright.deploiement.config.ts
 */
export default defineConfig({
  testDir: 'deploiement',
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  timeout: 600_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report-deploiement' }]],
  outputDir: 'test-results-deploiement',
  use: {
    baseURL: process.env.E2E_BASE_URL,
    actionTimeout: 20_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
