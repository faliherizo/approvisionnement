import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Config jetable pour la captation vidéo : port isolé 5175, proxy vers backend natif :8100
// (évite le conflit avec le conteneur Docker occupant :8080).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    strictPort: true,
    proxy: {
      '/api/': 'http://localhost:8100',
      '/scim': 'http://localhost:8100',
      '/oauth2': 'http://localhost:8100',
      '/login/oauth2': 'http://localhost:8100',
      '/swagger-ui': 'http://localhost:8100',
      '/v3': 'http://localhost:8100',
    },
  },
})
