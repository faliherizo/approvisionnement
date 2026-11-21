import { useState } from 'react'
import api from '../api'
import Modal from './Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Document PDF d'une fiche (CHU 5.04) : ouvert dans un nouvel onglet ; le lien porte le numéro de la fiche pour être explicite. */
export function BoutonDocumentFiche({ formulaireId, fiche, onErreur }) {
  const { t } = useI18n()
  async function ouvrir() {
    try {
      const r = await api.get(`/formulaires/${formulaireId}/saisies/${fiche.id}/document.pdf`, { responseType: 'blob' })
      const url = URL.createObjectURL(r.data)
      Object.assign(document.createElement('a'), { href: url, download: `${fiche.numero || fiche.id}.pdf` }).click()
      URL.revokeObjectURL(url)
    } catch { onErreur?.(t('docfiche.erreur')) }
  }
  return (
    <button type="button" className="secondary" onClick={ouvrir} aria-label={t('docfiche.telechargerLabel', { numero: fiche.numero || fiche.id })}>
      {t('docfiche.telecharger')}
    </button>
  )
}

/** Transmission du document d'une fiche par courriel, en pièce jointe. */
export function TransmettreFiche({ formulaireId, fiche, onFait, onClose }) {
  const { t } = useI18n()
  const [f, setF] = useState({ destinataire: '', message: '' })
  const [erreur, setErreur] = useState(null)

  async function envoyer(e) {
    e.preventDefault(); setErreur(null)
    try {
      const r = await api.post(`/formulaires/${formulaireId}/saisies/${fiche.id}/document/transmettre`, f)
      onFait(t(`docfiche.statut.${r.data.statut}`)); onClose()
    } catch (err) { setErreur(err.response?.data?.message || t('docfiche.erreurTransmission')) }
  }

  return (
    <Modal titre={t('docfiche.transmettreTitre', { numero: fiche.numero || fiche.id })} onClose={onClose}>
      <form className="form" onSubmit={envoyer}>
        {erreur && <div className="erreur" role="alert">{erreur}</div>}
        <label>{t('docfiche.destinataire')}
          <input type="email" required value={f.destinataire} onChange={(e) => setF({ ...f, destinataire: e.target.value })} />
        </label>
        <label>{t('docfiche.message')}
          <textarea rows={4} maxLength={2000} value={f.message} onChange={(e) => setF({ ...f, message: e.target.value })} />
        </label>
        <div className="modal-actions">
          <button type="button" className="secondary" onClick={onClose}>{t('modal.annuler')}</button>
          <button type="submit">{t('docfiche.envoyer')}</button>
        </div>
      </form>
    </Modal>
  )
}
