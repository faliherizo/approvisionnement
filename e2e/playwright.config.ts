import { defineConfig, devices } from '@playwright/test'

// Surchargeables pour viser des serveurs déjà lancés ailleurs (ex. port 8080 occupé) :
// E2E_FRONTEND_URL=http://localhost:5174 E2E_BACKEND_URL=http://localhost:8081 npx playwright test …
const frontend = process.env.E2E_FRONTEND_URL || 'http://localhost:5173'
const backend = process.env.E2E_BACKEND_URL || 'http://localhost:8080'

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
    baseURL: frontend,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: [
    {
      // Backend Spring Boot sur H2 (schéma via Liquibase)
      command: `mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments=--server.port=${new URL(backend).port || 8080}`,
      cwd: '../backend',
      url: `${backend}/api/health`,
      timeout: 180_000,
      reuseExistingServer: true,
    },
    {
      // Frontend React (Vite) — proxy /api -> backend
      command: 'npm run dev',
      cwd: '../frontend',
      env: { BACKEND_URL: backend, VITE_PORT: new URL(frontend).port || '5173' },
      url: frontend,
      timeout: 120_000,
      reuseExistingServer: true,
    },
  ],
})
