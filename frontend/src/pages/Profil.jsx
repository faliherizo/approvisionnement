import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import RepertoireDepotManager from '../components/RepertoireDepotManager.jsx'
import { useI18n } from '../i18n/I18nContext'

export default function Profil() {
  const { t } = useI18n()
  const [me, setMe] = useState(null)
  const [form, setForm] = useState({ nom: '', direction: '', motDePasseActuel: '', nouveauMotDePasse: '' })
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  useEffect(() => {
    api.get('/profil').then((r) => {
      setMe(r.data)
      setForm((f) => ({ ...f, nom: r.data.nom || '', direction: r.data.direction || '' }))
    })
  }, [])

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.put('/profil', form)
      setForm((f) => ({ ...f, motDePasseActuel: '', nouveauMotDePasse: '' }))
      setMsg(t('profil.profilMisAJour'))
    } catch (e2) { setErr(e2.response?.data?.erreur || t('profil.erreur')) }
  }

  if (!me) return <div className="panel">{t('profil.chargement')}</div>

  return (
    <div>
    <div className="grid2">
      <section className="panel">
        <h1>{t('profil.titre')}</h1>
        <p><strong>{t('profil.courriel')}</strong> {me.email}</p>
        <p><strong>{t('profil.roles')}</strong> {me.roles?.join(', ')}</p>
        <p><strong>{t('profil.secondFacteur')}</strong>{' '}
          <span className={`badge ${me.mfaActive ? 's-APPROUVE' : ''}`}>{me.mfaActive ? me.mfaMethode : t('profil.aucun')}</span>
        </p>
        <p className="muted">{t('profil.configurerMfa')} <Link to="/securite">{t('profil.securite')}</Link></p>
      </section>

      <section className="panel">
        <h2>{t('profil.modifierInfos')}</h2>
        {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
        {err && <div className="erreur">{err}</div>}
        <form onSubmit={enregistrer} className="form">
          <label>{t('profil.nom')}<input value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} /></label>
          <label>{t('profil.direction')}<input value={form.direction} onChange={(e) => setForm({ ...form, direction: e.target.value })} /></label>
          <hr style={{ width: '100%', border: 0, borderTop: '1px solid var(--bord)' }} />
          <p className="muted">{t('profil.changerMdp')}</p>
          <label>{t('profil.mdpActuel')}
            <input type="password" value={form.motDePasseActuel} onChange={(e) => setForm({ ...form, motDePasseActuel: e.target.value })} />
          </label>
          <label>{t('profil.nouveauMdp')}
            <input type="password" value={form.nouveauMotDePasse} onChange={(e) => setForm({ ...form, nouveauMotDePasse: e.target.value })} />
          </label>
          <button type="submit">{t('profil.enregistrer')}</button>
        </form>
      </section>
    </div>
    <RepertoireDepotManager />
    </div>
  )
}
