import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/**
 * Activation/désactivation des modules par tenant (superadmin). Les dépendances sont résolues côté
 * serveur : activer un module active ses prérequis, le désactiver désactive ses dépendants. Le message
 * de retour énumère les modules réellement affectés, y compris le produit concurrent retiré (approvisionnement
 * et IQ s'excluent : voir ADR-010).
 */
export default function ModuleManager() {
  const { t } = useI18n()
  const [tenants, setTenants] = useState([])
  const [tenantId, setTenantId] = useState('')
  const [etat, setEtat] = useState([])
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [occupe, setOccupe] = useState(false)

  useEffect(() => { api.get('/superadmin/tenants').then((r) => setTenants(r.data)).catch(() => setTenants([])) }, [])

  const charger = useCallback((id) => {
    if (!id) { setEtat([]); return }
    api.get(`/superadmin/tenants/${id}/modules`).then((r) => setEtat(r.data || [])).catch(() => setEtat([]))
  }, [])
  useEffect(() => { charger(tenantId) }, [tenantId, charger])

  async function basculer(mod) {
    if (!tenantId || occupe) return
    setErr(null); setMsg(null); setOccupe(true)
    const action = mod.actif ? 'desactiver' : 'activer'
    try {
      const r = await api.put(`/superadmin/tenants/${tenantId}/modules/${mod.code}/${action}`)
      setMsg(r.data?.message || t('module.bascule.ok'))
      charger(tenantId)
    } catch (e) {
      setErr(e.response?.data?.message || e.response?.data?.erreur || t('module.bascule.erreur'))
    } finally { setOccupe(false) }
  }

  const nomTenant = (id) => tenants.find((x) => String(x.id) === String(id))?.titre || ''

  return (
    <section className="panel">
      <h2>🧩 {t('module.titre')}</h2>
      <p className="muted">{t('module.description')}</p>
      <p className="muted">{t('module.exclusivite')}</p>
      {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur">{err}</div>}

      <label>{t('module.choisirTenant')}
        <select value={tenantId} onChange={(e) => { setTenantId(e.target.value); setMsg(null); setErr(null) }}>
          <option value="">{t('module.choisir')}</option>
          {tenants.map((x) => <option key={x.id} value={x.id}>{x.titre} ({x.slug})</option>)}
        </select>
      </label>

      {tenantId && (
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr>
            <th scope="col">{t('module.colModule')}</th>
            <th scope="col">{t('module.colFamille')}</th>
            <th scope="col">{t('module.colPrerequis')}</th>
            <th scope="col">{t('module.colEtat')}</th>
            <th scope="col"><span className="sr-only">{t('module.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {etat.map((m) => (
              <tr key={m.code}>
                <td><strong>{m.nom}</strong><br /><code className="muted">{m.code}</code></td>
                <td>{m.famille || '—'}</td>
                <td>{m.requiert && m.requiert.length ? m.requiert.map((c) => <code key={c} style={{ marginRight: 4 }}>{c}</code>) : <span className="muted">—</span>}</td>
                <td><span className={`badge ${m.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{m.actif ? t('module.actif') : t('module.inactif')}</span></td>
                <td>
                  <button className={m.actif ? 'danger' : ''} disabled={occupe} onClick={() => basculer(m)}>
                    {m.actif ? t('module.desactiver') : t('module.activer')}
                  </button>
                </td>
              </tr>
            ))}
            {etat.length === 0 && <tr><td colSpan="5" className="muted">{t('module.aucun', { tenant: nomTenant(tenantId) })}</td></tr>}
          </tbody>
        </table>
      )}
    </section>
  )
}
