import axios from 'axios'

// Authentification par cookie httpOnly : on envoie toujours les cookies.
const api = axios.create({ baseURL: '/api', withCredentials: true })

// Une session était-elle ouverte ? Seule son expiration renvoie à la connexion : un visiteur anonyme des pages
// publiques (accueil, signature, portail) reçoit des 401 sans être redirigé.
let sessionOuverte = false
export function marquerSession(ouverte) { sessionOuverte = ouverte }

// Déconnexion automatique si la session expire (401), sauf sur la page de login.
api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response && err.response.status === 401 && sessionOuverte && !err.config?.url?.endsWith('/auth/me')) {
      sessionOuverte = false
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api
