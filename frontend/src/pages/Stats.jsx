import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

function Repartition({ titre, data }) {
  const { t } = useI18n()
  const entries = Object.entries(data || {})
  const total = entries.reduce((s, [, v]) => s + v, 0) || 1
  return (
    <div className="panel">
      <h2>{titre}</h2>
      {entries.length === 0 && <p className="muted">{t('stats.aucuneDonnee')}</p>}
      {entries.map(([k, v]) => (
        <div key={k} className="bar-row">
          <span className="bar-label">{k}</span>
          <span className="bar"><span className="bar-fill" style={{ width: `${(v / total) * 100}%` }} /></span>
          <span className="bar-val">{v}</span>
        </div>
      ))}
    </div>
  )
}

export default function Stats() {
  const { t } = useI18n()
  const [stats, setStats] = useState(null)
  const [obligations, setObligations] = useState([])

  useEffect(() => {
    api.get('/stats').then((r) => setStats(r.data))
    api.get('/obligations/a-venir', { params: { jours: 60 } }).then((r) => setObligations(r.data))
  }, [])

  if (!stats) return <div className="panel">{t('stats.chargement')}</div>

  return (
    <div>
      <section className="panel">
        <h1>{t('stats.titre')}</h1>
        <p className="muted">{t('stats.description')}</p>
      </section>
      <div className="grid2">
        <div className="panel kpi-row">
          <div className="kpi"><div className="kpi-num">{stats.nbDocuments}</div><div className="kpi-lib">{t('stats.documents')}</div></div>
          <div className="kpi"><div className="kpi-num">{stats.nbClauses}</div><div className="kpi-lib">{t('stats.clausesActives')}</div></div>
          <div className="kpi"><div className="kpi-num">{obligations.length}</div><div className="kpi-lib">{t('stats.echeances60j')}</div></div>
        </div>
        <Repartition titre={t('stats.docsParStatut')} data={stats.parStatut} />
      </div>
      <div className="grid2">
        <Repartition titre={t('stats.docsParType')} data={stats.parType} />
        <Repartition titre={t('stats.docsParMode')} data={stats.parMode} />
      </div>

      <div className="panel">
        <h2>{t('stats.obligationsEcheances')}</h2>
        <table className="table">
          <thead><tr><th scope="col">{t('stats.thTitre')}</th><th scope="col">{t('stats.type')}</th><th scope="col">{t('stats.echeance')}</th><th scope="col">{t('stats.statut')}</th></tr></thead>
          <tbody>
            {obligations.map((o) => (
              <tr key={o.id}><td>{o.titre}</td><td>{o.type}</td><td>{o.dateEcheance || '—'}</td>
                <td><span className={`badge s-${o.statut}`}>{o.statut}</span></td></tr>
            ))}
            {obligations.length === 0 && <tr><td colSpan="4" className="muted">{t('stats.aucuneEcheance')}</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  )
}
