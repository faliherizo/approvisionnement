import { Link } from 'react-router-dom'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import Users from '../Users.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'

/**
 * UI-20 — Utilisateurs et groupes : l'écran des utilisateurs de la plateforme (recherche, rôles, statut) est réutilisé tel quel ;
 * groupes, source d'identité et journal des changements renvoient aux écrans de sécurité et d'audit existants.
 */
export default function UtilisateursClm() {
  const { t } = useI18n()
  const { actif } = useModules()
  const groupes = actif('securite_avancee')
  return (
    <div className="clm-ecran" data-maquette="UI-20">
      <Users />
      <Blocs>
        <Bloc zone="03" titre={t('clm.ut.blocGroupes')}>
          <p>{t('clm.ut.groupesAide')}</p>
          {groupes ? <Link to="/clm/admin/groupes">{t('clm.ut.gererGroupes')}</Link> : <p className="muted">{t('clm.ut.securiteInactive')}</p>}
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
        {groupes && <Link className="clm-bouton secondaire" to="/clm/admin/groupes">{t('clm.ut.creerGroupe')}</Link>}
        <Link className="clm-bouton secondaire" to="/audit">{t('clm.ut.consulterAudit')}</Link>
      </BarreActions>
    </div>
  )
}
