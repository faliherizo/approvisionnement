import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth.jsx'
import { espaceClmSeul } from '../../espaces.js'
import { useModules } from '../../hooks/useModules.js'
import { useI18n } from '../../i18n/I18nContext'
import { chargerTheme } from '../../theme.js'
import LangSwitcher from '../../components/LangSwitcher.jsx'
import './clm.css'
import { usePeutInitier } from './commun.jsx'

/** Entrées principales ; la quatrième valeur réserve l'entrée aux profils qui créent des trousses. */
const PRINCIPAUX = [
  ['/clm', 'clm.nav.tableauBord', true],
  ['/clm/trousses', 'clm.nav.mesTrousses'],
  ['/clm/lots', 'clm.nav.lots', false, true],
  ['/clm/taches', 'clm.nav.taches'],
  ['/clm/modeles', 'clm.nav.modeles'],
  ['/clm/suivi', 'clm.nav.suivi'],
]
/** Entrées d'administration ; le journal d'audit (UI-23) est aussi celui de l'auditeur. */
const ADMINISTRATION = [
  ['/clm/admin/notifications', 'clm.nav.notifications'],
  ['/clm/admin/integrations', 'clm.nav.integrations'],
  ['/clm/admin/parametres', 'clm.nav.parametres'],
  ['/clm/admin/listes', 'clm.nav.listes'],
  ['/clm/admin/utilisateurs', 'clm.nav.utilisateurs'],
  ['/clm/admin/journal', 'clm.nav.journal', 'AUDITEUR'],
  ['/clm/options', 'clm.nav.options'],
]

const initiales = (nom) => (nom || '?').split(/\s+/).filter(Boolean).slice(0, 2).map((m) => m[0].toUpperCase()).join('')

/** Bandeau (recherche globale, profil) et navigation latérale communs aux maquettes du module CLM. */
export default function ClmShell({ children }) {
  const { user, logout, hasRole } = useAuth()
  const { t } = useI18n()
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [tenant, setTenant] = useState(null)
  const [menuOuvert, setMenuOuvert] = useState(false)
  const [q, setQ] = useState('')
  const { modules, chargement } = useModules()
  const admin = hasRole('ADMIN') || hasRole('SUPERADMIN')
  const peutInitier = usePeutInitier()
  const administration = ADMINISTRATION.filter(([, , aussi]) => admin || (aussi && hasRole(aussi)))
  // Une organisation qui ne souscrit que le CLM n'a pas d'autre espace vers lequel revenir.
  const autreEspace = !chargement && !espaceClmSeul(modules)

  useEffect(() => { chargerTheme().then(setTenant) }, [])
  useEffect(() => { setMenuOuvert(false) }, [pathname])

  function rechercher(e) {
    e.preventDefault()
    navigate(`/clm/trousses?q=${encodeURIComponent(q.trim())}`)
  }

  return (
    <div className="clm-shell">
      <a className="skip-link" href="#contenu">{t('clm.gab.allerContenu')}</a>
      <header className="clm-shell__entete">
        <button type="button" className="clm-shell__menu" aria-expanded={menuOuvert} aria-controls="clm-navigation"
          onClick={() => setMenuOuvert(!menuOuvert)}>
          <span aria-hidden="true">☰</span>&nbsp;{t('clm.gab.menu')}
        </button>
        <Link to="/clm" className="clm-shell__marque">
          {tenant?.titre && <>{tenant.titre} <span aria-hidden="true">|</span> </>}{t('clm.gab.marque')}
        </Link>
        <form className="clm-shell__recherche" role="search" aria-label={t('clm.gab.rechercheLibelle')} onSubmit={rechercher}>
          <label htmlFor="clm-recherche-globale" className="sr-only">{t('clm.gab.rechercheLibelle')}</label>
          <input id="clm-recherche-globale" type="search" value={q} onChange={(e) => setQ(e.target.value)} placeholder={t('clm.gab.recherche')} />
          <button type="submit">{t('clm.rechercher')}</button>
        </form>
        <div className="clm-shell__profil">
          <LangSwitcher compact />
          <span className="clm-shell__avatar" aria-hidden="true">{initiales(user?.nom)}</span>
          <span className="clm-shell__nom">{user?.nom}</span>
          <Link to="/clm/profil">{t('clm.gab.profil')}</Link>
          {autreEspace && <Link to="/">{t('clm.gab.plateforme')}</Link>}
          <button type="button" onClick={() => { logout(); navigate('/login') }}>{t('action.logout')}</button>
        </div>
      </header>
      <div className="clm-shell__corps">
        <nav id="clm-navigation" className={`clm-shell__nav ${menuOuvert ? 'is-ouvert' : ''}`} aria-label={t('clm.gab.navigation')}>
          <ul>
            {PRINCIPAUX.filter(([, , , initiateurs]) => !initiateurs || peutInitier)
              .map(([chemin, cle, fin]) => <li key={chemin}><NavLink to={chemin} end={fin}>{t(cle)}</NavLink></li>)}
          </ul>
          {administration.length > 0 && (
            <>
              <p id="clm-nav-admin" className="clm-shell__groupe">{t('clm.nav.administration')}</p>
              <ul aria-labelledby="clm-nav-admin">
                {administration.map(([chemin, cle]) => <li key={chemin}><NavLink to={chemin}>{t(cle)}</NavLink></li>)}
              </ul>
            </>
          )}
        </nav>
        <main id="contenu" tabIndex={-1} className="clm-shell__main">{children}</main>
      </div>
    </div>
  )
}
