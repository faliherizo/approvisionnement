import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import api from '../api'
import SignaturePad from '../components/SignaturePad.jsx'
import { useI18n } from '../i18n/I18nContext'

/**
 * Page publique de signature (accès par jeton, sans authentification).
 * Un signataire externe consulte sa demande et appose sa signature (tracé ou image).
 */
export default function SignaturePublique() {
  const { t } = useI18n()
  const { jeton } = useParams()
  const [etat, setEtat] = useState(null)      // { nom, role, statut, documentId }
  const [nom, setNom] = useState('')
  const [image, setImage] = useState('')
  const [erreur, setErreur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  useEffect(() => {
    api.get(`/signature/${jeton}`)
      .then((r) => { setEtat(r.data); setNom(r.data.nom || '') })
      .catch(() => setErreur(t('signaturepublique.lienInvalide')))
  }, [jeton])

  async function signer(e) {
    e.preventDefault()
    setErreur(null)
    if (!image) { setErreur(t('signaturepublique.veuillezApposer')); return }
    setEnvoi(true)
    try {
      const { data } = await api.post(`/signature/${jeton}/signer`, { nomComplet: nom, signatureImage: image })
      setEtat((s) => ({ ...s, statut: data.statut }))
    } catch {
      setErreur(t('signaturepublique.erreurEnregistrement'))
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <div className="public-page">
      <div className="panel public-card">
        <div className="public-brand">VINA IT Solutions inc.</div>
        <h1>{t('signaturepublique.titre')}</h1>

        {erreur && !etat && <div className="erreur">{erreur}</div>}

        {etat && etat.statut === 'SIGNE' && (
          <div className="public-ok">
            <p>✔ {t('signaturepublique.merci', { nom: etat.nom })}</p>
            <p className="muted">{t('signaturepublique.fermerPage')}</p>
          </div>
        )}

        {etat && etat.statut !== 'SIGNE' && (
          <form className="form" onSubmit={signer}>
            <p className="muted">
              {etat.role
                ? t('signaturepublique.invitationRole', { role: etat.role })
                : t('signaturepublique.invitation')}
            </p>
            <label>{t('signaturepublique.nomComplet')}
              <input value={nom} required onChange={(e) => setNom(e.target.value)} />
            </label>
            <div className="form-legend">{t('signaturepublique.signatureManuscrite')}</div>
            <SignaturePad onChange={setImage} />
            {erreur && <div className="erreur">{erreur}</div>}
            <button type="submit" disabled={envoi}>
              {envoi ? t('signaturepublique.enregistrement') : t('signaturepublique.apposerSignature')}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
