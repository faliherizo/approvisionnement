import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import SecuriteAvancee from '../SecuriteAvancee.jsx'
import Users from '../Users.jsx'
import { Message, useErreur } from './commun.jsx'
import { BarreActions, Bloc, Blocs, EcranClm } from './gabarit.jsx'

/** Rôles qui ouvrent un droit dans le module, dans l'ordre du parcours d'un contrat. */
const ROLES_CLM = ['REDACTEUR', 'APPRO', 'JURIDIQUE', 'DIRECTION', 'ADMIN']

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
  const { t } = useI18n()
  const { actif } = useModules()
  const [version, setVersion] = useState(0)
  const groupes = actif('securite_avancee')
  return (
    <div className="clm-ecran" data-maquette="UI-20">
      <div data-zone="01"><Users key={version} /></div>
      <Blocs>
        <Bloc zone="02" titre={t('clm.ut.blocRoles')} large>
          <RolesEtPermissions onChange={() => setVersion((v) => v + 1)} />
        </Bloc>
        <Bloc zone="03" titre={t('clm.ut.blocGroupes')}>
          <p>{t('clm.ut.groupesAide')}</p>
          {groupes ? <Link to="/clm/admin/groupes">{t('clm.ut.gererGroupes')}</Link> : <p className="muted" id="clm-ut-securite">{t('clm.ut.securiteInactive')}</p>}
        </Bloc>
        <Bloc zone="04" titre={t('clm.ut.blocSso')}>
          <p>{t('clm.ut.ssoAide')}</p>
          <Link to="/parametres">{t('clm.ad.lienParametresPlateforme')}</Link>
        </Bloc>
        <Bloc zone="05" titre={t('clm.ut.blocJournal')} large>
          <p>{t('clm.ut.journalAide')}</p>
        </Bloc>
      </Blocs>
      <BarreActions>
        {groupes ? <Link className="clm-bouton secondaire" to="/clm/admin/groupes">{t('clm.ut.creerGroupe')}</Link>
          : <button type="button" className="secondary" disabled aria-describedby="clm-ut-securite">{t('clm.ut.creerGroupe')}</button>}
        <Link className="clm-bouton secondaire" to="/audit">{t('clm.ut.consulterAudit')}</Link>
      </BarreActions>
    </div>
  )
}

/** Groupes et unités (UI-20, zone 03) : écran de la sécurité avancée, ou motif de son absence quand le module est inactif. */
export function GroupesClm() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  if (chargement) return <p role="status">{t('clm.chargement')}</p>
  if (actif('securite_avancee')) return <SecuriteAvancee />
  return (
    <EcranClm maquette="UI-20" titre={t('clm.ut.titreGroupes')} objectif={t('clm.ut.groupesAide')}>
      <p className="clm-info-statique" role="note">{t('clm.ut.securiteInactive')}</p>
      <Link to="/clm/admin/utilisateurs">{t('clm.retour')}</Link>
    </EcranClm>
  )
}
