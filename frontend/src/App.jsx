import { useEffect, useState } from 'react'
import { Routes, Route, Navigate, NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import { useI18n } from './i18n/I18nContext.jsx'
import { useModules } from './hooks/useModules.js'
import { accueilSansSocle, equivalentClm, espaceClmSeul } from './espaces.js'
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
import Formulaires from './pages/Formulaires.jsx'
import FormulaireRemplir from './pages/FormulaireRemplir.jsx'
import Saisies from './pages/Saisies.jsx'
import NotificationsAdmin from './pages/NotificationsAdmin.jsx'
import SecuriteAvancee from './pages/SecuriteAvancee.jsx'
import InteropAdmin from './pages/InteropAdmin.jsx'
import MesDemandes from './pages/MesDemandes.jsx'
import Catalogue from './pages/Catalogue.jsx'
import Taches from './pages/Taches.jsx'
import Applications from './pages/Applications.jsx'
import Paie from './pages/Paie.jsx'
import Impot from './pages/Impot.jsx'
import Declarations from './pages/Declarations.jsx'
import Personnel from './pages/Personnel.jsx'
import DossierEmploye from './pages/DossierEmploye.jsx'
import Approbations from './pages/Approbations.jsx'
import FeuilleTemps from './pages/FeuilleTemps.jsx'
import ProjetsTaches from './pages/ProjetsTaches.jsx'
import ClmTableauBord from './pages/clm/ClmTableauBord.jsx'
import Trousses from './pages/clm/Trousses.jsx'
import TrousseCreer from './pages/clm/TrousseCreer.jsx'
import TrousseDetail from './pages/clm/TrousseDetail.jsx'
import Modeles from './pages/clm/Modeles.jsx'
import ModeleEditeur from './pages/clm/ModeleEditeur.jsx'
import TachesClm from './pages/clm/TachesClm.jsx'
import SuiviClm from './pages/clm/SuiviClm.jsx'
import { IntegrationsClm, NotificationsClm, ParametresClm } from './pages/clm/AdminClm.jsx'
import JournalClm from './pages/clm/JournalClm.jsx'
import OptionsClm from './pages/clm/OptionsClm.jsx'
import UtilisateursClm, { GroupesClm, SsoClm } from './pages/clm/UtilisateursClm.jsx'
import ClmShell from './pages/clm/ClmShell.jsx'
import Portail from './pages/clm/Portail.jsx'
import { usePeutInitier } from './pages/clm/commun.jsx'

function ProtectedRoute({ children }) {
  const { user, chargement } = useAuth()
  if (chargement) return <div className="panel">Chargement de la session…</div>
  return user ? children : <Navigate to="/login" replace />
}

/** Page d'un module : sans lui, l'organisation retrouve son accueil (sa navigation ne la propose pas non plus). */
function RouteModule({ code, children }) {
  const { actif, chargement } = useModules()
  if (chargement) return <div className="panel" role="status">Chargement…</div>
  return actif(code) ? children : <Navigate to="/" replace />
}

const socle = (page) => <ProtectedRoute><RouteModule code="redaction">{page}</RouteModule></ProtectedRoute>

/** Racine : l'accueil de l'organisation pour un utilisateur connecté, sinon le site vitrine public. */
function RacineRoute() {
  const { user, chargement } = useAuth()
  const { actif, chargement: chargementModules } = useModules()
  if (chargement || (user && chargementModules)) return <div className="panel">Chargement de la session…</div>
  if (!user) return <Navigate to="/accueil" replace />
  return actif('redaction') ? <Dashboard /> : <Navigate to={accueilSansSocle(actif)} replace />
}

function Layout({ children }) {
  const { user, logout, hasRole } = useAuth()
  const { t } = useI18n()
  const { modules, actif: moduleActif, chargement: chargementModules, recharger: rechargerModules } = useModules()
  const peutInitier = usePeutInitier()
  const navigate = useNavigate()
  const location = useLocation()
  const [tenantInfo, setTenantInfo] = useState(null)
  const [modulesDe, setModulesDe] = useState(null)

  useEffect(() => { chargerTheme().then(setTenantInfo) }, [])
  // Les modules actifs dépendent de l'organisation de l'utilisateur : ils se relisent à chaque connexion.
  useEffect(() => {
    if (!user) return
    rechargerModules()
    setModulesDe(user.email)
  }, [user, rechargerModules])

  if (!user) return children
  // Tant que les modules de cette organisation ne sont pas relus, aucun menu (pas même ceux de la session précédente).
  if (modulesDe !== user.email || chargementModules) {
    return <main id="contenu" tabIndex={-1}><div className="panel" role="status">Chargement de votre espace…</div></main>
  }
  // Organisation qui ne souscrit que le CLM : elle n'a pas d'autre espace que celui du module.
  if (espaceClmSeul(modules)) return <Navigate to={equivalentClm(location.pathname)} replace />
  const superadmin = hasRole('SUPERADMIN')
  const redaction = moduleActif('redaction')
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
              {moduleActif('saisie') && (
                <DropdownMenu label={t('nav.portail')} actif={dansGroupe('/mes-demandes', '/nouvelle-demande', '/taches')}>
                  <NavLink to="/mes-demandes">{t('nav.mesDemandes')}</NavLink>
                  {moduleActif('formulaire') && <NavLink to="/nouvelle-demande">{t('nav.nouvelleDemande')}</NavLink>}
                  {moduleActif('processus') && <NavLink to="/taches">{t('nav.taches')}</NavLink>}
                  {moduleActif('processus') && <NavLink to="/approbations">{t('nav.approbations')}</NavLink>}
                </DropdownMenu>
              )}
              {moduleActif('clm') && (
                <DropdownMenu label={t('clm.nav.groupe')} actif={dansGroupe('/clm')}>
                  <NavLink to="/clm" end>{t('clm.nav.tableau')}</NavLink>
                  <NavLink to="/clm/trousses" end>{t('clm.nav.trousses')}</NavLink>
                  {peutInitier && <NavLink to="/clm/trousses/nouvelle">{t('clm.nav.nouvelle')}</NavLink>}
                  <NavLink to="/clm/taches">{t('clm.nav.taches')}</NavLink>
                  <NavLink to="/clm/suivi">{t('clm.nav.suivi')}</NavLink>
                  <NavLink to="/clm/modeles">{t('clm.nav.modeles')}</NavLink>
                  {hasRole('ADMIN') && <NavLink to="/clm/admin">{t('clm.nav.admin')}</NavLink>}
                </DropdownMenu>
              )}
              {redaction && (
                <DropdownMenu label={t('nav.documents')} actif={dansGroupe('/', '/dossiers', '/assistant', '/clauses', '/recherche')}>
                  <NavLink to="/" end>{t('nav.documents')}</NavLink>
                  <NavLink to="/dossiers">Dossiers</NavLink>
                  <NavLink to="/assistant">Assistant guidé</NavLink>
                  <NavLink to="/clauses">{t('nav.library')}</NavLink>
                  <NavLink to="/recherche">{t('nav.search')}</NavLink>
                </DropdownMenu>
              )}
              {redaction && (
                <DropdownMenu label="Pilotage" actif={dansGroupe('/tableau-de-bord', '/rapports', '/chat-ia', '/simulateur')}>
                  <NavLink to="/tableau-de-bord">{t('nav.dashboard')}</NavLink>
                  <NavLink to="/rapports">Rapports de suivi</NavLink>
                  <NavLink to="/chat-ia">{t('nav.assistant')}</NavLink>
                  {(hasRole('REDACTEUR') || hasRole('APPRO') || hasRole('ADMIN')) && (
                    <NavLink to="/simulateur">Dépôts simulés</NavLink>
                  )}
                </DropdownMenu>
              )}
              {moduleActif('formulaire') && (hasRole('ADMIN') || hasRole('REDACTEUR')) && (
                <DropdownMenu label={t('nav.formulaires')} actif={dansGroupe('/formulaires')}>
                  <NavLink to="/formulaires">{t('nav.formulairesConception')}</NavLink>
                </DropdownMenu>
              )}
              {(moduleActif('paie') || moduleActif('rh')) && (hasRole('ADMIN') || hasRole('DIRECTION')) && (
                <DropdownMenu label={t('nav.espaceRh')} actif={dansGroupe('/paie', '/impot', '/declarations', '/rh')}>
                  {moduleActif('rh') && <NavLink to="/rh/personnel">{t('nav.personnel')}</NavLink>}
                  {moduleActif('rh') && <NavLink to="/rh/projets-taches">{t('nav.projetsTaches')}</NavLink>}
                  {moduleActif('rh') && <NavLink to="/rh/feuilles-temps">{t('nav.feuilleTemps')}</NavLink>}
                  {moduleActif('paie') && <NavLink to="/paie">{t('nav.paie')}</NavLink>}
                  {moduleActif('paie') && <NavLink to="/impot">{t('nav.impot')}</NavLink>}
                  {moduleActif('paie') && <NavLink to="/declarations">{t('nav.declarations')}</NavLink>}
                </DropdownMenu>
              )}
              {hasRole('ADMIN') && (
                <DropdownMenu label="Administration" actif={dansGroupe('/utilisateurs', '/gabarits', '/templates', '/parametres', '/workflows', '/audit', '/donnees', '/applications', '/notifications', '/securite-avancee', '/interop')}>
                  <NavLink to="/utilisateurs">{t('nav.users')}</NavLink>
                  {moduleActif('application') && <NavLink to="/applications">{t('nav.applications')}</NavLink>}
                  {redaction && <NavLink to="/templates">Templates</NavLink>}
                  {redaction && <NavLink to="/gabarits">Gabarits</NavLink>}
                  {redaction && <NavLink to="/workflows">Circuits d’approbation</NavLink>}
                  <NavLink to="/audit">Journal d’audit</NavLink>
                  {redaction && <NavLink to="/donnees">Données &amp; IA</NavLink>}
                  {moduleActif('notification') && <NavLink to="/notifications">{t('nav.notifications')}</NavLink>}
                  {moduleActif('securite_avancee') && <NavLink to="/securite-avancee">{t('nav.securiteFine')}</NavLink>}
                  {moduleActif('interop') && <NavLink to="/interop">{t('nav.interop')}</NavLink>}
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
  const location = useLocation()
  // Portail du signataire : parcours autonome, sans le gabarit de l'application interne.
  if (location.pathname.startsWith('/portail/')) {
    return <Routes><Route path="/portail/:jeton" element={<Portail />} /></Routes>
  }
  // Module CLM : gabarit des maquettes UI-01 à UI-24 (bandeau, recherche globale, navigation latérale).
  if (location.pathname === '/clm' || location.pathname.startsWith('/clm/')) {
    return (
      <ProtectedRoute>
        <ClmShell>
          <Routes>
            <Route path="/clm" element={<ClmTableauBord />} />
            <Route path="/clm/trousses" element={<Trousses />} />
            <Route path="/clm/trousses/nouvelle" element={<TrousseCreer />} />
            <Route path="/clm/trousses/:id" element={<TrousseDetail />} />
            <Route path="/clm/taches" element={<TachesClm />} />
            <Route path="/clm/suivi" element={<SuiviClm />} />
            <Route path="/clm/modeles" element={<Modeles />} />
            <Route path="/clm/modeles/:id" element={<ModeleEditeur />} />
            <Route path="/clm/admin" element={<Navigate to="/clm/admin/notifications" replace />} />
            <Route path="/clm/admin/notifications" element={<NotificationsClm />} />
            <Route path="/clm/admin/integrations" element={<IntegrationsClm />} />
            <Route path="/clm/admin/parametres" element={<ParametresClm />} />
            <Route path="/clm/admin/utilisateurs" element={<UtilisateursClm />} />
            <Route path="/clm/admin/groupes" element={<GroupesClm />} />
            <Route path="/clm/admin/journal" element={<JournalClm />} />
            <Route path="/clm/options" element={<OptionsClm />} />
            <Route path="/clm/profil" element={<Profil />} />
            <Route path="/clm/securite" element={<Securite />} />
            <Route path="/clm/admin/sso" element={<SsoClm />} />
            <Route path="/clm/admin/audit" element={<Audit />} />
            <Route path="*" element={<Navigate to="/clm" replace />} />
          </Routes>
        </ClmShell>
      </ProtectedRoute>
    )
  }
  return (
    <Layout>
      <Routes>
        <Route path="/accueil" element={<Vitrine />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signature/:jeton" element={<SignaturePublique />} />
        <Route path="/" element={<RacineRoute />} />
        <Route path="/clauses" element={socle(<Clauses />)} />
        <Route path="/dossiers" element={socle(<Dossiers />)} />
        <Route path="/assistant" element={socle(<Assistant />)} />
        <Route path="/workflows" element={socle(<Workflows />)} />
        <Route path="/audit" element={<ProtectedRoute><Audit /></ProtectedRoute>} />
        <Route path="/donnees" element={socle(<DonneesIA />)} />
        <Route path="/documents/:id" element={socle(<DocumentEditor />)} />
        <Route path="/recherche" element={socle(<Search />)} />
        <Route path="/tableau-de-bord" element={socle(<Stats />)} />
        <Route path="/rapports" element={socle(<Rapports />)} />
        <Route path="/chat-ia" element={socle(<ChatIA />)} />
        <Route path="/simulateur" element={socle(<SimulateurDepots />)} />
        <Route path="/utilisateurs" element={<ProtectedRoute><Users /></ProtectedRoute>} />
        <Route path="/gabarits" element={socle(<Gabarits />)} />
        <Route path="/templates" element={socle(<Templates />)} />
        <Route path="/parametres" element={<ProtectedRoute><Parametres /></ProtectedRoute>} />
        <Route path="/securite" element={<ProtectedRoute><Securite /></ProtectedRoute>} />
        <Route path="/profil" element={<ProtectedRoute><Profil /></ProtectedRoute>} />
        <Route path="/api-docs" element={<ProtectedRoute><ApiDocs /></ProtectedRoute>} />
        <Route path="/superadmin" element={<ProtectedRoute><Superadmin /></ProtectedRoute>} />
        <Route path="/formulaires" element={<ProtectedRoute><Formulaires /></ProtectedRoute>} />
        <Route path="/formulaires/:id/remplir" element={<ProtectedRoute><FormulaireRemplir /></ProtectedRoute>} />
        <Route path="/formulaires/:id/fiches" element={<ProtectedRoute><Saisies /></ProtectedRoute>} />
        <Route path="/mes-demandes" element={<ProtectedRoute><MesDemandes /></ProtectedRoute>} />
        <Route path="/nouvelle-demande" element={<ProtectedRoute><Catalogue /></ProtectedRoute>} />
        <Route path="/taches" element={<ProtectedRoute><Taches /></ProtectedRoute>} />
        <Route path="/applications" element={<ProtectedRoute><Applications /></ProtectedRoute>} />
        <Route path="/paie" element={<ProtectedRoute><Paie /></ProtectedRoute>} />
        <Route path="/impot" element={<ProtectedRoute><Impot /></ProtectedRoute>} />
        <Route path="/declarations" element={<ProtectedRoute><Declarations /></ProtectedRoute>} />
        <Route path="/rh/personnel" element={<ProtectedRoute><Personnel /></ProtectedRoute>} />
        <Route path="/rh/employes/:id" element={<ProtectedRoute><DossierEmploye /></ProtectedRoute>} />
        <Route path="/approbations" element={<ProtectedRoute><Approbations /></ProtectedRoute>} />
        <Route path="/rh/feuilles-temps" element={<ProtectedRoute><FeuilleTemps /></ProtectedRoute>} />
        <Route path="/rh/projets-taches" element={<ProtectedRoute><ProjetsTaches /></ProtectedRoute>} />
        <Route path="/notifications" element={<ProtectedRoute><NotificationsAdmin /></ProtectedRoute>} />
        <Route path="/securite-avancee" element={<ProtectedRoute><SecuriteAvancee /></ProtectedRoute>} />
        <Route path="/interop" element={<ProtectedRoute><InteropAdmin /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  )
}
