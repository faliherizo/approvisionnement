import { useEffect, useState } from 'react'
import { Routes, Route, Navigate, NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import { useI18n } from './i18n/I18nContext.jsx'
import { chargerTheme } from './theme.js'
import LangSwitcher from './components/LangSwitcher.jsx'
import DropdownMenu from './components/DropdownMenu.jsx'
import Login from './pages/Login.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Clauses from './pages/Clauses.jsx'
import Dossiers from './pages/Dossiers.jsx'
import Assistant from './pages/Assistant.jsx'
import Audit from './pages/Audit.jsx'
import Workflows from './pages/Workflows.jsx'
import DonneesIA from './pages/DonneesIA.jsx'
import Rapports from './pages/Rapports.jsx'
import DocumentEditor from './pages/DocumentEditor.jsx'
import Search from './pages/Search.jsx'
import Stats from './pages/Stats.jsx'
import Securite from './pages/Securite.jsx'
import ApiDocs from './pages/ApiDocs.jsx'
import Superadmin from './pages/Superadmin.jsx'
import Gabarits from './pages/Gabarits.jsx'
import Templates from './pages/Templates.jsx'
import Users from './pages/Users.jsx'
import Profil from './pages/Profil.jsx'
import ChatIA from './pages/ChatIA.jsx'
import Parametres from './pages/Parametres.jsx'
import SimulateurDepots from './pages/SimulateurDepots.jsx'
import SignaturePublique from './pages/SignaturePublique.jsx'
import Vitrine from './pages/Vitrine.jsx'

function ProtectedRoute({ children }) {
  const { user, chargement } = useAuth()
  if (chargement) return <div className="panel">Chargement de la session…</div>
  return user ? children : <Navigate to="/login" replace />
}

/** Racine : l'application pour un utilisateur connecté, sinon le site vitrine public. */
function RacineRoute() {
  const { user, chargement } = useAuth()
  if (chargement) return <div className="panel">Chargement de la session…</div>
  return user ? <Dashboard /> : <Navigate to="/accueil" replace />
}

function Layout({ children }) {
  const { user, logout, hasRole } = useAuth()
  const { t } = useI18n()
  const navigate = useNavigate()
  const location = useLocation()
  const [tenantInfo, setTenantInfo] = useState(null)

  useEffect(() => { chargerTheme().then(setTenantInfo) }, [])

  if (!user) return children
  const superadmin = hasRole('SUPERADMIN')
  const dansGroupe = (...chemins) => chemins.some((c) => location.pathname === c || location.pathname.startsWith(c + '/'))
  // Espace superadmin : branding VINA par défaut ; sinon, titre du tenant.
  const marque = superadmin ? 'VINA IT Solutions inc.' : (tenantInfo?.titre || 'Ville de Lévis')
  return (
    <div className="app">
      <a className="skip-link" href="#contenu">Aller au contenu principal</a>
      <header className="topbar">
        <div className="brand">
          <span className="crest" aria-hidden="true">⚜️</span>
          <div>
            <strong>{marque}</strong>
            <div className="sub">{superadmin ? 'Console d’administration' : t('brand.subtitle')}</div>
          </div>
        </div>
        <nav aria-label="Navigation principale">
          {superadmin ? (
            <NavLink to="/superadmin">{t('nav.superadmin')}</NavLink>
          ) : (
            <>
              <DropdownMenu label={t('nav.documents')} actif={dansGroupe('/', '/dossiers', '/assistant', '/clauses', '/recherche')}>
                <NavLink to="/" end>{t('nav.documents')}</NavLink>
                <NavLink to="/dossiers">Dossiers</NavLink>
                <NavLink to="/assistant">Assistant guidé</NavLink>
                <NavLink to="/clauses">{t('nav.library')}</NavLink>
                <NavLink to="/recherche">{t('nav.search')}</NavLink>
              </DropdownMenu>
              <DropdownMenu label="Pilotage" actif={dansGroupe('/tableau-de-bord', '/rapports', '/chat-ia', '/simulateur')}>
                <NavLink to="/tableau-de-bord">{t('nav.dashboard')}</NavLink>
                <NavLink to="/rapports">Rapports de suivi</NavLink>
                <NavLink to="/chat-ia">{t('nav.assistant')}</NavLink>
                {(hasRole('REDACTEUR') || hasRole('APPRO') || hasRole('ADMIN')) && (
                  <NavLink to="/simulateur">Dépôts simulés</NavLink>
                )}
              </DropdownMenu>
              {hasRole('ADMIN') && (
                <DropdownMenu label="Administration" actif={dansGroupe('/utilisateurs', '/gabarits', '/templates', '/parametres', '/workflows', '/audit', '/donnees')}>
                  <NavLink to="/utilisateurs">{t('nav.users')}</NavLink>
                  <NavLink to="/templates">Templates</NavLink>
                  <NavLink to="/gabarits">Gabarits</NavLink>
                  <NavLink to="/workflows">Circuits d’approbation</NavLink>
                  <NavLink to="/audit">Journal d’audit</NavLink>
                  <NavLink to="/donnees">Données &amp; IA</NavLink>
                  <NavLink to="/parametres">Paramètres</NavLink>
                </DropdownMenu>
              )}
            </>
          )}
          <NavLink to="/profil">{t('nav.profile')}</NavLink>
        </nav>
        <div className="userbox">
          <LangSwitcher compact />
          <span>{user.nom}</span>
          <span className="roles">{user.roles?.join(', ')}</span>
          <button onClick={() => { logout(); navigate('/login') }}>{t('action.logout')}</button>
        </div>
      </header>
      <main id="contenu" tabIndex={-1}>{children}</main>
      <footer className="app-footer">
        <span>{t('footer.poweredBy')} <strong>VINA IT Solutions inc.</strong></span>
        <span className="muted">© {new Date().getFullYear()} · {t('footer.tagline')}</span>
      </footer>
    </div>
  )
}

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/accueil" element={<Vitrine />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signature/:jeton" element={<SignaturePublique />} />
        <Route path="/" element={<RacineRoute />} />
        <Route path="/clauses" element={<ProtectedRoute><Clauses /></ProtectedRoute>} />
        <Route path="/dossiers" element={<ProtectedRoute><Dossiers /></ProtectedRoute>} />
        <Route path="/assistant" element={<ProtectedRoute><Assistant /></ProtectedRoute>} />
        <Route path="/workflows" element={<ProtectedRoute><Workflows /></ProtectedRoute>} />
        <Route path="/audit" element={<ProtectedRoute><Audit /></ProtectedRoute>} />
        <Route path="/donnees" element={<ProtectedRoute><DonneesIA /></ProtectedRoute>} />
        <Route path="/documents/:id" element={<ProtectedRoute><DocumentEditor /></ProtectedRoute>} />
        <Route path="/recherche" element={<ProtectedRoute><Search /></ProtectedRoute>} />
        <Route path="/tableau-de-bord" element={<ProtectedRoute><Stats /></ProtectedRoute>} />
        <Route path="/rapports" element={<ProtectedRoute><Rapports /></ProtectedRoute>} />
        <Route path="/chat-ia" element={<ProtectedRoute><ChatIA /></ProtectedRoute>} />
        <Route path="/simulateur" element={<ProtectedRoute><SimulateurDepots /></ProtectedRoute>} />
        <Route path="/utilisateurs" element={<ProtectedRoute><Users /></ProtectedRoute>} />
        <Route path="/gabarits" element={<ProtectedRoute><Gabarits /></ProtectedRoute>} />
        <Route path="/templates" element={<ProtectedRoute><Templates /></ProtectedRoute>} />
        <Route path="/parametres" element={<ProtectedRoute><Parametres /></ProtectedRoute>} />
        <Route path="/securite" element={<ProtectedRoute><Securite /></ProtectedRoute>} />
        <Route path="/profil" element={<ProtectedRoute><Profil /></ProtectedRoute>} />
        <Route path="/api-docs" element={<ProtectedRoute><ApiDocs /></ProtectedRoute>} />
        <Route path="/superadmin" element={<ProtectedRoute><Superadmin /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  )
}
