import { defineConfig } from '@playwright/test'

/**
 * Tests d'intrusion : contre le serveur réel (SECURITE_API_URL, défaut http://localhost:8100) et, pour les en-têtes du
 * frontal, contre le frontend (SECURITE_FRONTEND_URL). Un seul ouvrier : les essais de force brute et les organisations
 * d'essai partagent l'état du serveur.
 */
export default defineConfig({
  testDir: './tests',
  timeout: 600_000,
  workers: 1,
  reporter: [['list'], ['json', { outputFile: 'resultats/playwright.json' }], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.SECURITE_API_URL ?? 'http://localhost:8100',
    extraHTTPHeaders: { Accept: 'application/json' },
  },
})
