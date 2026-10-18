import { defineConfig, devices } from '@playwright/test'

/** Suite e2e complète sur ports isolés (backend :8100, vite :5175) — évite le conflit Docker :8080. */
export default defineConfig({
  testDir: '.',
  testMatch: ['tests/**/*.spec.ts', 'views/**/*.spec.ts'],
  fullyParallel: false,
  workers: 1,
  timeout: 30_000,
  expect: { timeout: 10_000 },
  reporter: [['line']],
  use: { baseURL: 'http://localhost:5175', trace: 'on-first-retry', screenshot: 'only-on-failure' },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: [
    { command: 'SERVER_PORT=8100 mvn -q -DskipTests spring-boot:run', cwd: '../backend',
      url: 'http://localhost:8100/api/health', timeout: 180_000, reuseExistingServer: true },
    { command: 'npm run dev -- --config vite.tour.config.js', cwd: '../frontend',
      url: 'http://localhost:5175', timeout: 120_000, reuseExistingServer: true },
  ],
})
