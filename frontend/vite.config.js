import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      // Flux SSO OIDC (Entra ID) servi par le backend.
      '/oauth2': 'http://localhost:8080',
      '/login/oauth2': 'http://localhost:8080',
      // Documentation OpenAPI / Swagger UI (menu admin).
      '/swagger-ui': 'http://localhost:8080',
      '/v3': 'http://localhost:8080'
    }
  }
})
