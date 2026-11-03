import { defineConfig, devices } from '@playwright/test'

/**
 * Configuration e2e Approvisionnement visant une pile DÉJÀ démarrée (docker compose : nginx :8082 →
 * backend PostgreSQL). N'amorce aucun serveur (pas de contention de ports :5173/:8080, cf. mémoire e2e).
 * Lancer : `npx playwright test --config playwright.appro.config.ts`.
 */
export default defineConfig({
  testDir: '.',
  testMatch: ['tests/approvisionnement.spec.ts'],
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  expect: { timeout: 15_000 },
  reporter: [['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:8082',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
