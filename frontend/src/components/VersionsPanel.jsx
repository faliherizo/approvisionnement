import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

function formatDate(iso) { return iso ? new Date(iso).toLocaleString('fr-CA') : '—' }

/** Versions et publications d'un formulaire (écran 14) : historique des versions + retour à une version. */
export default function VersionsPanel({ id, onChange }) {
  const { t } = useI18n()
  const [versions, setVersions] = useState([])
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get(`/formulaires/${id}/historique`).then((r) => setVersions(r.data || [])).catch(() => setErr(t('versions.erreur')))
  }, [id, t])
  useEffect(() => { charger() }, [charger])

  async function revenir(numero) {
    if (!window.confirm(t('versions.confirmer', { n: numero }))) return
    setErr(null); setMsg(null)
    try {
      await api.post(`/formulaires/${id}/revenir/${numero}`)
      setMsg(t('versions.restaure', { n: numero })); charger(); onChange && onChange()
    } catch (e) { setErr(e.response?.data?.message || t('versions.erreur')) }
  }

  const badge = (s) => s === 'PUBLIE' ? 's-APPROUVE' : s === 'ARCHIVE' ? 's-REFUSE' : ''

  return (
    <div className="panel">
      <h3>{t('versions.titre')}</h3>
      <p className="muted">{t('versions.intro')}</p>
      {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}
      <table className="table">
        <thead><tr>
          <th scope="col">{t('versions.colVersion')}</th>
          <th scope="col">{t('versions.colStatut')}</th>
          <th scope="col">{t('versions.colNote')}</th>
          <th scope="col">{t('versions.colPublie')}</th>
          <th scope="col"><span className="sr-only">{t('versions.colActions')}</span></th>
        </tr></thead>
        <tbody>
          {versions.map((v) => (
            <tr key={v.numero}>
              <td>v{v.numero}</td>
              <td><span className={`badge ${badge(v.statut)}`}>{t(`statut.${v.statut}`)}</span></td>
              <td>{v.note || '—'}</td>
              <td>{formatDate(v.publieLe)}</td>
              <td><button className="secondary" onClick={() => revenir(v.numero)}>{t('versions.revenir')}</button></td>
            </tr>
          ))}
          {versions.length === 0 && <tr><td colSpan="5" className="muted">{t('versions.aucune')}</td></tr>}
        </tbody>
      </table>
    </div>
  )
}
