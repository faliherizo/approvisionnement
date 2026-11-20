import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import IntegrationOidcManager from '../../components/IntegrationOidcManager.jsx'
import IntegrationSamlManager from '../../components/IntegrationSamlManager.jsx'
import AnnuaireManager from '../../components/AnnuaireManager.jsx'
import Users from '../Users.jsx'
import { ChoixListe, GardeModule, Message, useErreur, useListe, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs, EcranClm } from './gabarit.jsx'

/** Rôles qui ouvrent un droit dans le module, dans l'ordre du parcours d'un contrat, puis la consultation d'audit. */
const ROLES_CLM = ['REDACTEUR', 'APPRO', 'JURIDIQUE', 'DIRECTION', 'ADMIN_AFFAIRES', 'ADMIN_TECHNIQUE', 'ADMIN', 'AUDITEUR']

/** UI-20, zone 02 : ce que chaque rôle permet dans le module ; attribution d'un rôle et retrait de l'accès d'un compte. */
function RolesEtPermissions({ onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [courriel, setCourriel] = useState('')
  const [role, setRole] = useState(ROLES_CLM[0])
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function modifier(transformer, message) {
    setErreur(null)
    setInfo('')
    const adresse = courriel.trim().toLowerCase()
    try {
      const { data } = await api.get('/admin/users', { params: { q: adresse, page: 0, taille: 5 } })
      const compte = data.contenu.find((u) => u.email.toLowerCase() === adresse)
      if (!compte) { setErreur(t('clm.ut.introuvable', { courriel: adresse })); return }
      await api.put(`/admin/users/${compte.id}`, { ...transformer(compte), motDePasse: '' })
      setInfo(message(compte))
      onChange()
    } catch (e) { setErreur(versErreur(e)) }
  }
  const attribuer = () => modifier((u) => ({ ...u, roles: [...new Set([...u.roles, role])] }),
    (u) => t('clm.ut.roleAttribue', { role: t(`clm.ut.role.${role}`), nom: u.nom }))
  const retirer = () => modifier((u) => ({ ...u, actif: false }), (u) => t('clm.ut.accesRetire', { nom: u.nom }))

  return (
    <>
      <div className="table-scroll">
        <table className="table clm-table">
          <thead><tr><th scope="col">{t('clm.ut.colRole')}</th><th scope="col">{t('clm.ut.colPermissions')}</th></tr></thead>
          <tbody>
            {ROLES_CLM.map((r) => (
              <tr key={r}>
                <td data-label={t('clm.ut.colRole')}>{t(`clm.ut.role.${r}`)} <code>{r}</code></td>
                <td data-label={t('clm.ut.colPermissions')}>{t(`clm.ut.permissions.${r}`)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="muted">{t('clm.ut.permissionsTous')}</p>
      <form className="form clm-sousform" onSubmit={(e) => { e.preventDefault(); attribuer() }}>
        <Message erreur={erreur} info={info} />
        <div className="form-grid">
          <label>{t('clm.ut.courriel')}<input type="email" value={courriel} onChange={(e) => setCourriel(e.target.value)} required autoComplete="off" /></label>
          <label>{t('clm.ut.colRole')}
            <select value={role} onChange={(e) => setRole(e.target.value)}>
              {ROLES_CLM.map((r) => <option key={r} value={r}>{t(`clm.ut.role.${r}`)}</option>)}
            </select>
          </label>
        </div>
        <div className="cmp-row">
          <button type="submit">{t('clm.ut.attribuerRole')}</button>
          <button type="button" className="danger" disabled={!courriel.trim()} onClick={retirer}>{t('clm.ut.retirerAcces')}</button>
        </div>
      </form>
    </>
  )
}

/**
 * UI-20 — Utilisateurs et groupes : l'administration des comptes de la plateforme est réutilisée (recherche, statut,
 * édition) ; le module y ajoute ses rôles et permissions, l'accès aux groupes et le renvoi au journal des changements.
 */
export default function UtilisateursClm() {
  return <GardeModule><Utilisateurs /></GardeModule>
}

function Utilisateurs() {
  const { t } = useI18n()
  const [version, setVersion] = useState(0)
  return (
    <div className="clm-ecran" data-maquette="UI-20">
      <div data-zone="01"><Users key={version} /></div>
      <Blocs>
        <Bloc zone="02" titre={t('clm.ut.blocRoles')} large>
          <RolesEtPermissions onChange={() => setVersion((v) => v + 1)} />
        </Bloc>
        <Bloc zone="03" titre={t('clm.ut.blocGroupes')}>
          <p>{t('clm.ut.groupesAide')}</p>
          <Link to="/clm/admin/groupes">{t('clm.ut.gererGroupes')}</Link>
        </Bloc>
        <Bloc zone="04" titre={t('clm.ut.blocSso')}>
          <p>{t('clm.ut.ssoAide')}</p>
          <Link to="/clm/admin/sso">{t('clm.ut.titreSso')}</Link>
        </Bloc>
        <Bloc zone="05" titre={t('clm.ut.blocJournal')} large>
          <p>{t('clm.ut.journalAide')}</p>
        </Bloc>
      </Blocs>
      <BarreActions>
        <Link className="clm-bouton secondaire" to="/clm/admin/groupes">{t('clm.ut.creerGroupe')}</Link>
        <Link className="clm-bouton secondaire" to="/clm/admin/audit">{t('clm.ut.consulterAudit')}</Link>
      </BarreActions>
    </div>
  )
}

/** Groupes et unités (UI-20, zone 03), administrés dans l'espace du module. */
export function GroupesClm() {
  return <GardeModule><Groupes /></GardeModule>
}

/** Source d'identité de l'organisation (UI-20, zone 04), configurée sans quitter l'espace du module. */
export function SsoClm() {
  return <GardeModule><Sso /></GardeModule>
}

function Sso() {
  const { t } = useI18n()
  return (
    <EcranClm maquette="UI-20" titre={t('clm.ut.titreSso')} objectif={t('clm.ut.ssoObjectif')}>
      <IntegrationOidcManager />
      <IntegrationSamlManager />
      <AnnuaireManager />
      <Link to="/clm/admin/utilisateurs">{t('clm.retour')}</Link>
    </EcranClm>
  )
}

/**
 * Groupes du module (Cahier § 1.4.7 et § 1.4.12) : partage des modèles, affectation d'une étape, visibilité par unité.
 * Un groupe dont le code est celui d'une unité de l'organisation ouvre à ses membres les trousses de cette unité.
 */
function Groupes() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { actif } = useModules()
  const { donnees: groupes, recharger } = useRessource('/clm/groupes')
  const { donnees: membres } = useRessource('/clm/membres')
  const unites = useListe('CLM_UNITE')
  const [f, setF] = useState({ code: '', libelle: '' })
  const [ajout, setAjout] = useState({})
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { await fn(); setInfo(ok); recharger(); return true } catch (x) { setErreur(versErreur(x)); return false }
  }
  const creer = async (e) => {
    e.preventDefault()
    if (await faire(() => api.post('/clm/admin/groupes', f), t('clm.gr.cree', { code: f.code.toUpperCase() }))) setF({ code: '', libelle: '' })
  }
  const choisirUnite = (code) => setF({ code, libelle: unites.libelle(code) })

  return (
    <EcranClm maquette="UI-20" titre={t('clm.ut.titreGroupes')} objectif={t('clm.gr.objectif')}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc titre={t('clm.gr.nouveau')} large>
          <form className="form" onSubmit={creer} aria-label={t('clm.gr.nouveau')}>
            <div className="form-grid">
              <label>{t('clm.gr.depuisUnite')} <span className="muted">{t('clm.gr.depuisUniteAide')}</span>
                <ChoixListe liste={unites} value="" onChange={choisirUnite} vide={t('clm.gr.aucuneUnite')} /></label>
              <label>{t('clm.gr.code')} <span className="muted">{t('clm.gr.codeAide')}</span>
                <input value={f.code} onChange={(e) => setF({ ...f, code: e.target.value.toUpperCase() })} maxLength={60} required /></label>
              <label className="col-2">{t('clm.gr.libelle')}<input value={f.libelle} onChange={(e) => setF({ ...f, libelle: e.target.value })} maxLength={120} required /></label>
            </div>
            <div className="cmp-row"><button type="submit">{t('clm.gr.creer')}</button></div>
          </form>
        </Bloc>
        <Bloc titre={t('clm.gr.liste')} large>
          {!groupes ? <p role="status">{t('clm.chargement')}</p> : groupes.length === 0 ? <p className="muted">{t('clm.gr.aucun')}</p> : (
            <ul className="clm-liste">
              {groupes.map((g) => (
                <li key={g.id}>
                  <p><strong>{g.libelle}</strong> <code>{g.code}</code>{g.unite && <span className="muted"> · {t('clm.gr.unite')}</span>}</p>
                  {g.membres.length === 0 ? <p className="muted">{t('clm.gr.sansMembre')}</p> : (
                    <ul aria-label={t('clm.gr.membresDe', { code: g.code })}>
                      {g.membres.map((m) => (
                        <li key={m}>{m}{' '}
                          <button type="button" className="lien danger" aria-label={t('clm.gr.retirerMembre', { courriel: m, code: g.code })}
                            onClick={() => faire(() => api.delete(`/clm/admin/groupes/${g.id}/membres/${encodeURIComponent(m)}`), t('clm.gr.membreRetire', { courriel: m }))}>
                            {t('clm.retirer')}</button></li>
                      ))}
                    </ul>
                  )}
                  <form className="cmp-row" aria-label={t('clm.gr.ajouterA', { code: g.code })} onSubmit={(e) => {
                    e.preventDefault()
                    faire(() => api.post(`/clm/admin/groupes/${g.id}/membres`, { courriel: ajout[g.id] }), t('clm.gr.membreAjoute', { courriel: ajout[g.id] }))
                      .then((ok) => ok && setAjout({ ...ajout, [g.id]: '' }))
                  }}>
                    <label>{t('clm.gr.membre')}
                      <select value={ajout[g.id] || ''} onChange={(e) => setAjout({ ...ajout, [g.id]: e.target.value })} required>
                        <option value="">{t('clm.gr.choisirMembre')}</option>
                        {(membres || []).filter((m) => !g.membres.includes(m.courriel)).map((m) => <option key={m.courriel} value={m.courriel}>{m.nom} · {m.courriel}</option>)}
                      </select>
                    </label>
                    <button type="submit" className="secondary">{t('clm.gr.ajouter')}</button>
                    <button type="button" className="danger" aria-label={t('clm.gr.supprimer', { code: g.code })} onClick={() => {
                      if (window.confirm(t('clm.gr.confirmerSuppression', { code: g.code }))) faire(() => api.delete(`/clm/admin/groupes/${g.id}`), t('clm.gr.supprime', { code: g.code }))
                    }}>{t('clm.gr.supprimerCourt')}</button>
                  </form>
                </li>
              ))}
            </ul>
          )}
        </Bloc>
      </Blocs>
      {actif('securite_avancee') && <p><Link to="/securite-avancee">{t('clm.gr.securiteAvancee')}</Link></p>}
      <Link to="/clm/admin/utilisateurs">{t('clm.retour')}</Link>
    </EcranClm>
  )
}
