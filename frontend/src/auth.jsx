import { createContext, useContext, useEffect, useState } from 'react'
import api from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [chargement, setChargement] = useState(true)

  // Restaure la session à partir du cookie httpOnly (via /auth/me).
  useEffect(() => {
    api.get('/auth/me')
      .then((r) => setUser({ email: r.data.email, nom: r.data.nom, roles: r.data.roles }))
      .catch(() => setUser(null))
      .finally(() => setChargement(false))
  }, [])

  /**
   * Connexion multi-facteur.
   * @param second {totpCode} | {securityAnswers} | undefined
   * @returns {ok, mfaRequired?, securityMethod?, securityQuestions?, error?}
   */
  async function login(email, motDePasse, second, slug) {
    try {
      const { data } = await api.post('/auth/login', { email, motDePasse, ...(second || {}), slug: slug || null })
      if (data.ok) {
        const u = { email: data.email, nom: data.nom, roles: data.roles }
        setUser(u)
        return { ok: true }
      }
      if (data.mfaRequired) {
        return {
          ok: false,
          mfaRequired: true,
          securityMethod: data.securityMethod,
          securityQuestions: data.securityQuestions || [],
        }
      }
      return { ok: false, error: data.error || 'Échec de la connexion' }
    } catch (err) {
      return { ok: false, error: err.response?.data?.error || 'Identifiants invalides' }
    }
  }

  async function logout() {
    try { await api.post('/auth/logout') } catch { /* ignore */ }
    setUser(null)
  }

  // ADMIN cumule l'administration d'affaires et l'administration technique (addenda 4, Q10), comme côté serveur.
  const hasRole = (role) => Boolean(user?.roles?.includes(role)
    || ((role === 'ADMIN_AFFAIRES' || role === 'ADMIN_TECHNIQUE') && user?.roles?.includes('ADMIN')))

  return (
    <AuthContext.Provider value={{ user, chargement, login, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
