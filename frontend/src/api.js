import axios from 'axios'

// Authentification par cookie httpOnly : on envoie toujours les cookies.
const api = axios.create({ baseURL: '/api', withCredentials: true })

// Déconnexion automatique si la session expire (401), sauf sur la page de login.
api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response && err.response.status === 401) {
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api
