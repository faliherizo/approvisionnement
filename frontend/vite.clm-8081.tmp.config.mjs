// Configuration TEMPORAIRE (à supprimer) : Vite sur 5174, proxy vers un backend de test sur 8081.
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const cible = 'http://localhost:8081'
export default defineConfig({
  plugins: [react()],
  server: { port: 5174, strictPort: true, proxy: { '/api': cible, '/oauth2': cible, '/login/oauth2': cible } },
})
