import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Surchargeables pour viser un autre backend ou un autre port (ex. BACKEND_URL=http://localhost:8081 VITE_PORT=5174).
const backend = process.env.BACKEND_URL || 'http://localhost:8080'
const port = Number(process.env.VITE_PORT) || 5173

export default defineConfig({
  plugins: [react()],
  server: {
    port,
    strictPort: Boolean(process.env.VITE_PORT),
    proxy: {
      '/api': backend,
      // Flux SSO OIDC (Entra ID) servi par le backend.
      '/oauth2': backend,
      '/login/oauth2': backend,
      // Documentation OpenAPI / Swagger UI (menu admin).
      '/swagger-ui': backend,
      '/v3': backend
    }
  }
})
