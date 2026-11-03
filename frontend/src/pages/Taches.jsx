import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'
import Modal from '../components/Modal.jsx'

function formatDate(iso) { return iso ? new Date(iso).toLocaleDateString('fr-CA') : '—' }

/** Tâches et approbations (écran 7) : tâches ouvertes assignées à mes rôles + panneau de décision. */
export default function Taches() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [taches, setTaches] = useState([])
  const [examen, setExamen] = useState(null) // tâche en cours d'examen
  const [commentaire, setCommentaire] = useState('')
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/taches/mine').then((r) => setTaches(r.data || [])).catch(() => setTaches([]))
  }, [])
  useEffect(() => { if (!chargement && actif('processus')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('taches.chargement')}</div>
  if (!actif('processus')) return <div className="panel">{t('taches.moduleInactif')}</div>

  async function decider(decision) {
    setErr(null)
    try {
      await api.post(`/taches/${examen.id}/decisions`, { decision, commentaire: commentaire || null })
      setExamen(null); setCommentaire(''); charger()
    } catch (e) { setErr(e.response?.data?.message || t('taches.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('taches.titre')}</h1>
        <p className="muted">{t('taches.intro')}</p>
        {err && !examen && <div className="erreur" role="alert">{err}</div>}
        <table className="table">
          <thead><tr>
            <th scope="col">{t('taches.colDossier')}</th>
            <th scope="col">{t('taches.colEtape')}</th>
            <th scope="col">{t('taches.colDomaine')}</th>
            <th scope="col">{t('taches.colEcheance')}</th>
            <th scope="col"><span className="sr-only">{t('taches.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {taches.map((t2) => (
              <tr key={t2.id}>
                <td><code>{t2.numeroDemande || t2.saisieId}</code><br /><span className="muted">{t2.formulaireNom}</span></td>
                <td>{t2.etapeNom || t2.etapeCode}</td>
                <td>{t2.domaine ? <span className="badge">{t2.domaine}</span> : '—'}</td>
                <td>{formatDate(t2.echeanceLe)}</td>
                <td><button onClick={() => { setExamen(t2); setCommentaire(''); setErr(null) }}>{t('taches.examiner')}</button></td>
              </tr>
            ))}
            {taches.length === 0 && <tr><td colSpan="5" className="muted">{t('taches.aucune')}</td></tr>}
          </tbody>
        </table>
      </div>

      {examen && (
        <Modal titre={t('taches.examenTitre', { numero: examen.numeroDemande || examen.saisieId })} onClose={() => setExamen(null)}>
          <p><strong>{t('taches.colEtape')} :</strong> {examen.etapeNom || examen.etapeCode}</p>
          <p className="muted">{examen.formulaireNom}</p>
          <label>{t('taches.commentaire')}{examen.commentaireRefusObligatoire ? ' *' : ''}
            <textarea rows={3} value={commentaire} onChange={(e) => setCommentaire(e.target.value)} />
          </label>
          {err && <div className="erreur" role="alert">{err}</div>}
          <div className="cmp-row" style={{ marginTop: 10 }}>
            <button onClick={() => decider('APPROUVE')}>{t('taches.approuver')}</button>
            <button className="secondary" onClick={() => decider('RETOUR')}>{t('taches.retour')}</button>
            <button className="danger" onClick={() => decider('REFUSE')}>{t('taches.refuser')}</button>
          </div>
        </Modal>
      )}
    </div>
  )
}
