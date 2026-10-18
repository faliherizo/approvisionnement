import { defineConfig, devices } from '@playwright/test'

/**
 * Configuration Playwright.
 * Démarre automatiquement le backend Java (H2, profil par défaut) et le frontend
 * React (Vite). Le backend utilise H2 en mémoire — base fraîche à chaque exécution.
 */
export default defineConfig({
  // Inclut les tests e2e fonctionnels (tests/) ET les tests de vues (views/).
  testDir: '.',
  testMatch: ['tests/**/*.spec.ts', 'views/**/*.spec.ts'],
  fullyParallel: false,
  workers: 1,
  timeout: 30_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: [
    {
      // Backend Spring Boot sur H2 (schéma via Liquibase)
      command: 'mvn -q -DskipTests spring-boot:run',
      cwd: '../backend',
      url: 'http://localhost:8080/api/health',
      timeout: 180_000,
      reuseExistingServer: true,
    },
    {
      // Frontend React (Vite) — proxy /api -> :8080
      command: 'npm run dev',
      cwd: '../frontend',
      url: 'http://localhost:5173',
      timeout: 120_000,
      reuseExistingServer: true,
    },
  ],
})
