import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Consultation et export du journal d'audit (art. 18) — réservé aux administrateurs. */
export default function Audit() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const [filtres, setFiltres] = useState({ utilisateur: '', action: '', entite: '' })
  const [entrees, setEntrees] = useState([])

  const charger = useCallback(() => {
    const params = {}
    if (filtres.utilisateur) params.utilisateur = filtres.utilisateur
    if (filtres.action) params.action = filtres.action
    if (filtres.entite) params.entite = filtres.entite
    api.get('/audit', { params }).then((r) => setEntrees(r.data)).catch(() => setEntrees([]))
  }, [filtres])

  useEffect(() => { charger() }, [charger])

  if (!hasRole('ADMIN')) {
    return <div className="panel">{t('audit.accesReserve')}</div>
  }

  async function exporter() {
    const params = new URLSearchParams()
    Object.entries(filtres).forEach(([k, v]) => { if (v) params.append(k, v) })
    const { data } = await api.get(`/audit/export?${params.toString()}`, { responseType: 'blob' })
    const url = URL.createObjectURL(new Blob([data], { type: 'text/csv' }))
    const a = document.createElement('a')
    a.href = url
    a.download = 'journal-audit.csv'
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="panel" aria-labelledby="titre-audit">
      <h1 id="titre-audit">{t('audit.titre')}</h1>
      <p className="muted">{t('audit.description')}</p>

      <form className="form" onSubmit={(e) => { e.preventDefault(); charger() }}
            style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
        <label>{t('audit.utilisateur')}
          <input value={filtres.utilisateur} onChange={(e) => setFiltres({ ...filtres, utilisateur: e.target.value })} />
        </label>
        <label>{t('audit.action')}
          <input value={filtres.action} onChange={(e) => setFiltres({ ...filtres, action: e.target.value })}
                 placeholder={t('audit.actionPlaceholder')} />
        </label>
        <label>{t('audit.entite')}
          <input value={filtres.entite} onChange={(e) => setFiltres({ ...filtres, entite: e.target.value })}
                 placeholder={t('audit.entitePlaceholder')} />
        </label>
        <button type="submit">{t('audit.filtrer')}</button>
        <button type="button" className="secondary" onClick={exporter}>{t('audit.exporter')}</button>
      </form>

      <div className="table-wrap" style={{ marginTop: '1rem' }}>
        <table className="table">
          <caption className="sr-only">{t('audit.caption')}</caption>
          <thead>
            <tr>
              <th scope="col">{t('audit.horodatage')}</th><th scope="col">{t('audit.utilisateur')}</th>
              <th scope="col">{t('audit.action')}</th><th scope="col">{t('audit.entite')}</th>
              <th scope="col">{t('audit.ref')}</th><th scope="col">{t('audit.details')}</th>
            </tr>
          </thead>
          <tbody>
            {entrees.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.horodatage).toLocaleString('fr-CA')}</td>
                <td>{a.utilisateur}</td>
                <td><span className="badge">{a.action}</span></td>
                <td>{a.entite}</td>
                <td>{a.entiteId ?? '—'}</td>
                <td>{a.details || '—'}</td>
              </tr>
            ))}
            {entrees.length === 0 && (
              <tr><td colSpan="6" className="muted">{t('audit.aucunEvenement')}</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
