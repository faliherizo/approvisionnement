import { defineConfig, devices } from '@playwright/test'

/** Captation vidéo du parcours — ports isolés (backend :8100, vite :5175) pour éviter le conflit :8080. */
export default defineConfig({
  testDir: './tour',
  testMatch: ['**/*.tour.ts'],
  fullyParallel: false,
  workers: 1,
  timeout: 360_000,
  expect: { timeout: 15_000 },
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:5175',
    actionTimeout: 20_000, // échoue vite sur un sélecteur absent (évite un blocage jusqu'au timeout du test)
    viewport: { width: 1280, height: 800 },
    video: { mode: 'on', size: { width: 1280, height: 800 } },
    trace: 'off',
    screenshot: 'off',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    {
      command: 'SERVER_PORT=8100 mvn -q -DskipTests spring-boot:run',
      cwd: '../backend',
      url: 'http://localhost:8100/api/health',
      timeout: 180_000,
      reuseExistingServer: true,
    },
    {
      command: 'npm run dev -- --config vite.tour.config.js',
      cwd: '../frontend',
      url: 'http://localhost:5175',
      timeout: 120_000,
      reuseExistingServer: true,
    },
  ],
})
