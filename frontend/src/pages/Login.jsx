import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'
import { appliquerTheme, slugTenant } from '../theme.js'
import LangSwitcher from '../components/LangSwitcher.jsx'

export default function Login() {
  const { login, user } = useAuth()
  const { t } = useI18n()
  const navigate = useNavigate()

  const slug = slugTenant() // tenant déduit du sous-domaine (null sur domaine nu)
  const [titreTenant, setTitreTenant] = useState(null)
  const [email, setEmail] = useState('redacteur@levis.qc.ca')
  const [motDePasse, setMotDePasse] = useState('Passw0rd!')
  const [mfaMethod, setMfaMethod] = useState(null) // 'totp' | 'question'
  const [questions, setQuestions] = useState([])
  const [totpCode, setTotpCode] = useState('')
  const [answers, setAnswers] = useState([])
  const [erreur, setErreur] = useState(null)
  const [busy, setBusy] = useState(false)
  const [ssoAzure, setSsoAzure] = useState(false) // SSO Microsoft affiché seulement si actif côté backend
  const [oidc, setOidc] = useState({ actif: false, libelle: '' }) // connexion OIDC par tenant
  const [saml, setSaml] = useState({ actif: false, libelle: '' }) // connexion SAML par tenant

  // Thème VINA par défaut (portail de connexion de l'éditeur).
  const THEME_VINA = { couleurPrimaire: '#0b4a8a', couleurAccent: '#c8102e', couleurFond: '#f4f6f9', couleurTexte: '#1c2733' }

  useEffect(() => {
    api.get('/auth/config', { params: slug ? { slug } : {} })
      .then((r) => {
        setSsoAzure(!!r.data?.ssoAzure)
        setOidc({ actif: !!r.data?.oidcActif, libelle: r.data?.oidcLibelle || t('login.oidcDefaut') })
        setSaml({ actif: !!r.data?.samlActif, libelle: r.data?.samlLibelle || t('login.samlDefaut') })
      })
      .catch(() => { setSsoAzure(false); setOidc({ actif: false, libelle: '' }); setSaml({ actif: false, libelle: '' }) })
    // Le tenant vient du sous-domaine : on applique son thème et on affiche son nom (branding).
    if (slug) {
      api.get(`/tenants/${slug}/public`)
        .then((r) => { appliquerTheme(r.data.theme || THEME_VINA); setTitreTenant(r.data.titre || null) })
        .catch(() => appliquerTheme(THEME_VINA))
    } else {
      appliquerTheme(THEME_VINA)
    }
  }, [slug])

  if (user) { navigate('/'); return null }

  function second() {
    if (mfaMethod === 'totp') return { totpCode }
    if (mfaMethod === 'question') return { securityAnswers: answers }
    return undefined
  }

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)
    setBusy(true)
    try {
      const r = await login(email, motDePasse, second(), slug)
      if (r.ok) { navigate('/'); return }
      if (r.mfaRequired) {
        setMfaMethod(r.securityMethod)
        setQuestions(r.securityQuestions || [])
        setAnswers(new Array((r.securityQuestions || []).length).fill(''))
        setErreur(null)
        return
      }
      setErreur(r.error || t('login.loginFailed'))
    } finally {
      setBusy(false)
    }
  }

  function ssoMicrosoft() {
    // Redirection vers le flux OIDC Entra ID (actif sous le profil backend "azure").
    window.location.href = '/oauth2/authorization/azure'
  }

  function connexionOidc() {
    // Flux OIDC configuré par tenant (Paramètres → Intégration OIDC). Le slug cible le bon annuaire.
    window.location.href = '/api/auth/oidc/login' + (slug ? `?slug=${encodeURIComponent(slug)}` : '')
  }

  function connexionSaml() {
    // Flux SAML 2.0 configuré par tenant (Paramètres → Intégration SAML). Le slug cible le bon IdP.
    window.location.href = '/api/auth/saml/login' + (slug ? `?slug=${encodeURIComponent(slug)}` : '')
  }

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={soumettre}>
        <div className="login-head">
          <svg className="vina-logo" viewBox="0 0 120 120" role="img" aria-label="Logo VINA IT Solutions inc.">
            <rect width="120" height="120" rx="26" fill="#0b4a8a" />
            <circle cx="60" cy="27" r="8" fill="#c8102e" />
            <path d="M30 37 L60 89 L90 37" fill="none" stroke="#ffffff" strokeWidth="13"
                  strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          <h1>VINA IT Solutions inc.</h1>
          <p>{titreTenant || t('footer.tagline')}</p>
        </div>

        <label>{t('login.email')}
          <input type="email" value={email} autoComplete="username" disabled={!!mfaMethod}
                 onChange={(e) => setEmail(e.target.value)} required />
        </label>

        <label>{t('login.password')}
          <input type="password" value={motDePasse} autoComplete="current-password" disabled={!!mfaMethod}
                 onChange={(e) => setMotDePasse(e.target.value)} required />
        </label>

        {mfaMethod === 'totp' && (
          <label>{t('login.totp')}
            <input value={totpCode} inputMode="numeric" pattern="[0-9]{6}" autoFocus
                   placeholder="123456" onChange={(e) => setTotpCode(e.target.value)} required />
          </label>
        )}

        {mfaMethod === 'question' && questions.map((q, i) => (
          <label key={q + i}>{q || t('login.securityQuestion', { n: i + 1 })}
            <input value={answers[i] || ''} autoFocus={i === 0}
                   onChange={(e) => {
                     const n = [...answers]; n[i] = e.target.value; setAnswers(n)
                   }} required />
          </label>
        ))}

        {erreur && <div className="erreur">{erreur}</div>}

        <button type="submit" disabled={busy}>
          {busy ? t('login.loading') : mfaMethod ? t('login.mfaSubmit') : t('login.submit')}
        </button>

        {!mfaMethod && ssoAzure && (
          <>
            <div className="sep"><span>{t('login.or')}</span></div>
            <button type="button" className="secondary" onClick={ssoMicrosoft} disabled={busy}>
              <span aria-hidden="true">🪟</span> {t('login.sso')}
            </button>
          </>
        )}

        {!mfaMethod && oidc.actif && (
          <>
            {!ssoAzure && <div className="sep"><span>{t('login.or')}</span></div>}
            <button type="button" className="secondary" onClick={connexionOidc} disabled={busy}>
              <span aria-hidden="true">🔐</span> {oidc.libelle}
            </button>
          </>
        )}

        {!mfaMethod && saml.actif && (
          <>
            {!ssoAzure && !oidc.actif && <div className="sep"><span>{t('login.or')}</span></div>}
            <button type="button" className="secondary" onClick={connexionSaml} disabled={busy}>
              <span aria-hidden="true">🔏</span> {saml.libelle}
            </button>
          </>
        )}

        <div className="hint">
          {t('login.hint')}<code>Passw0rd!</code>
        </div>
        <div className="login-lang"><LangSwitcher /></div>
        <div className="login-accueil"><Link to="/accueil">{t('login.backHome')}</Link></div>
        <div className="powered-by">{t('footer.poweredBy')} <strong>VINA IT Solutions inc.</strong></div>
      </form>
    </div>
  )
}
