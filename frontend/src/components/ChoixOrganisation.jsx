import { useEffect, useState } from 'react'
import api from '../api.js'
import Modal from './Modal.jsx'
import { useI18n } from '../i18n/I18nContext.jsx'
import { adresseOrganisation } from '../theme.js'

/**
 * Choix de l'organisation depuis le domaine de la plateforme : liste des organisations actives (créées dans l'espace
 * superadministrateur), puis redirection vers la page de connexion de son sous-domaine. L'administration de la
 * plateforme reste sur le domaine nu.
 */
export default function ChoixOrganisation({ onClose, naviguer = (url) => window.location.assign(url) }) {
  const { t } = useI18n()
  const [organisations, setOrganisations] = useState(null)
  const [choix, setChoix] = useState('')
  const [erreur, setErreur] = useState(false)

  useEffect(() => {
    api.get('/tenants')
      .then((r) => { setOrganisations(r.data); setChoix(r.data[0]?.slug ?? '') })
      .catch(() => { setOrganisations([]); setErreur(true) })
  }, [])

  const continuer = (e) => {
    e.preventDefault()
    if (choix) naviguer(adresseOrganisation(choix))
  }

  return (
    <Modal titre={t('vitrine.choixTitre')} onClose={onClose} taille="sm">
      <form className="form" onSubmit={continuer}>
        <p>{t('vitrine.choixAide')}</p>
        {organisations === null && <p role="status">{t('vitrine.choixChargement')}</p>}
        {erreur && <p role="alert" className="erreur">{t('vitrine.choixErreur')}</p>}
        {organisations?.length > 0 && (
          <label>{t('vitrine.choixOrganisation')}
            <select value={choix} onChange={(e) => setChoix(e.target.value)} required>
              {organisations.map((o) => <option key={o.slug} value={o.slug}>{o.titre}</option>)}
            </select>
          </label>
        )}
        <div className="modal-actions">
          <button type="button" className="secondary" onClick={() => naviguer('/login')}>{t('vitrine.choixPlateforme')}</button>
          <button type="submit" disabled={!choix}>{t('vitrine.choixContinuer')}</button>
        </div>
      </form>
    </Modal>
  )
}
