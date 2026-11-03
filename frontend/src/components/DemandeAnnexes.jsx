import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

function formatTaille(o) {
  if (o == null) return '—'
  if (o < 1024) return `${o} o`
  if (o < 1024 * 1024) return `${(o / 1024).toFixed(0)} Ko`
  return `${(o / 1024 / 1024).toFixed(1)} Mo`
}
function formatDate(iso) { return iso ? new Date(iso).toLocaleString('fr-CA') : '—' }

/**
 * Pièces jointes et commentaires d'une demande (module « document »). Téléversement, liste, téléchargement
 * contrôlé, fil de commentaires (visibilité interne/demandeur).
 */
export default function DemandeAnnexes({ formulaireId, saisieId }) {
  const { t } = useI18n()
  const [documents, setDocuments] = useState([])
  const [commentaires, setCommentaires] = useState([])
  const [comm, setComm] = useState({ visibilite: 'INTERNE', corps: '' })
  const [envoi, setEnvoi] = useState(false)
  const [err, setErr] = useState(null)
  const base = `/formulaires/${formulaireId}/saisies/${saisieId}`

  const charger = useCallback(() => {
    api.get(`${base}/documents`).then((r) => setDocuments(r.data || [])).catch(() => setDocuments([]))
    api.get(`${base}/commentaires`).then((r) => setCommentaires(r.data || [])).catch(() => setCommentaires([]))
  }, [base])
  useEffect(() => { charger() }, [charger])

  async function televerser(e) {
    const fichiers = e.target.files
    if (!fichiers || fichiers.length === 0) return
    setErr(null); setEnvoi(true)
    try {
      const fd = new FormData()
      for (const f of fichiers) fd.append('fichiers', f)
      await api.post(`${base}/documents`, fd)
      charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('annexes.erreurUpload')) }
    finally { setEnvoi(false); e.target.value = '' }
  }

  async function ajouterCommentaire(e) {
    e.preventDefault(); setErr(null)
    try {
      await api.post(`${base}/commentaires`, comm)
      setComm({ visibilite: comm.visibilite, corps: '' }); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('annexes.erreurCommentaire')) }
  }

  return (
    <div className="grid2" style={{ gap: 16 }}>
      <section>
        <h3>{t('annexes.documents')}</h3>
        {err && <div className="erreur" role="alert">{err}</div>}
        <table className="table">
          <thead><tr>
            <th scope="col">{t('annexes.colNom')}</th>
            <th scope="col">{t('annexes.colTaille')}</th>
            <th scope="col">{t('annexes.colClassification')}</th>
            <th scope="col"><span className="sr-only">{t('annexes.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {documents.map((d) => (
              <tr key={d.id}>
                <td>{d.nomOriginal}</td>
                <td>{formatTaille(d.tailleOctets)}</td>
                <td><span className="badge">{d.classification}</span></td>
                <td><a className="btn-lien" href={`/api/documents/${d.id}/download`}>{t('annexes.telecharger')}</a></td>
              </tr>
            ))}
            {documents.length === 0 && <tr><td colSpan="4" className="muted">{t('annexes.aucunDocument')}</td></tr>}
          </tbody>
        </table>
        <label>{t('annexes.ajouterDocument')}
          <input type="file" multiple disabled={envoi} onChange={televerser} />
        </label>
        {envoi && <p className="muted" role="status">{t('annexes.envoiEnCours')}</p>}
      </section>

      <section>
        <h3>{t('annexes.commentaires')}</h3>
        <ul className="reco-list">
          {commentaires.map((c) => (
            <li key={c.id}>
              <span className={`badge ${c.visibilite === 'DEMANDEUR' ? 's-APPROUVE' : ''}`}>{t(`annexes.visibilite.${c.visibilite}`)}</span>{' '}
              <strong>{c.auteur}</strong> <span className="muted">· {formatDate(c.creeLe)}</span>
              <div>{c.corps}</div>
            </li>
          ))}
          {commentaires.length === 0 && <li className="muted">{t('annexes.aucunCommentaire')}</li>}
        </ul>
        <form onSubmit={ajouterCommentaire} className="form">
          <label>{t('annexes.visibiliteLabel')}
            <select value={comm.visibilite} onChange={(e) => setComm({ ...comm, visibilite: e.target.value })}>
              <option value="INTERNE">{t('annexes.visibilite.INTERNE')}</option>
              <option value="DEMANDEUR">{t('annexes.visibilite.DEMANDEUR')}</option>
            </select>
          </label>
          <textarea rows={2} value={comm.corps} required placeholder={t('annexes.commentairePlaceholder')}
                    onChange={(e) => setComm({ ...comm, corps: e.target.value })} />
          <button type="submit">{t('annexes.commenter')}</button>
        </form>
      </section>
    </div>
  )
}
