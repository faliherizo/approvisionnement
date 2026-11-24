import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'
import EchangesHerites from '../components/EchangesHerites.jsx'

/** Clients d'API (intégrations) : création d'identifiants OAuth2 client_credentials. Module « interop ». */
export default function InteropAdmin() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [clients, setClients] = useState([])
  const [form, setForm] = useState({ nom: '', scopes: '' })
  const [secret, setSecret] = useState(null) // identifiants affichés UNE SEULE FOIS
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/interop/clients').then((r) => setClients(r.data || [])).catch(() => setClients([]))
  }, [])
  useEffect(() => { if (!chargement && actif('interop')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('interop.chargement')}</div>
  if (!actif('interop')) return <div className="panel">{t('interop.moduleInactif')}</div>

  async function creer(e) {
    e.preventDefault(); setErr(null); setSecret(null)
    try {
      const r = await api.post('/interop/clients', null, { params: { nom: form.nom, scopes: form.scopes || undefined } })
      setSecret(r.data); setForm({ nom: '', scopes: '' }); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('interop.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('interop.titre')}</h1>
        <p className="muted">{t('interop.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      {secret && (
        <section className="panel" style={{ borderLeft: '4px solid var(--bleu)' }}>
          <h2>{t('interop.identifiantsTitre')}</h2>
          <p className="erreur" style={{ background: '#fff4ce', color: '#6b5900' }}>{t('interop.secretUneFois')}</p>
          <dl className="fiche-details">
            <div className="cmp-row"><dt>client_id</dt><dd><code>{secret.clientId}</code></dd></div>
            <div className="cmp-row"><dt>secret</dt><dd><code>{secret.secret}</code></dd></div>
          </dl>
          <button className="secondary" onClick={() => setSecret(null)}>{t('interop.compris')}</button>
        </section>
      )}

      <section className="panel">
        <h2>{t('interop.clients')}</h2>
        <table className="table">
          <thead><tr><th scope="col">{t('interop.colNom')}</th><th scope="col">client_id</th><th scope="col">{t('interop.colScopes')}</th><th scope="col">{t('interop.colActif')}</th></tr></thead>
          <tbody>
            {clients.map((c) => (
              <tr key={c.id}>
                <td>{c.nom}</td><td><code>{c.clientId}</code></td><td>{c.scopes || '—'}</td>
                <td><span className={`badge ${c.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{c.actif ? t('interop.oui') : t('interop.non')}</span></td>
              </tr>
            ))}
            {clients.length === 0 && <tr><td colSpan="4" className="muted">{t('interop.aucun')}</td></tr>}
          </tbody>
        </table>
        <form onSubmit={creer} className="form" style={{ marginTop: 12 }}>
          <div className="cmp-row">
            <input placeholder={t('interop.phNom')} value={form.nom} required onChange={(e) => setForm({ ...form, nom: e.target.value })} />
            <input placeholder={t('interop.phScopes')} value={form.scopes} onChange={(e) => setForm({ ...form, scopes: e.target.value })} />
            <button type="submit">{t('interop.creer')}</button>
          </div>
        </form>
      </section>

      <EchangesHerites />
    </div>
  )
}
