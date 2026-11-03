import { useEffect, useState } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import ReferenceManager from '../components/ReferenceManager.jsx'
import ModeleDocumentManager from '../components/ModeleDocumentManager.jsx'
import TarifTenantManager from '../components/TarifTenantManager.jsx'
import GabaritManager from '../components/GabaritManager.jsx'
import ModuleManager from '../components/ModuleManager.jsx'
import Modal from '../components/Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

export default function Superadmin() {
  const { hasRole } = useAuth()
  const { t: tr } = useI18n()
  const [tenants, setTenants] = useState([])
  const [form, setForm] = useState({ slug: '', titre: '', sousTitre: '' })
  const [admin, setAdmin] = useState({ tenantId: '', email: '', nom: '', motDePasse: '' })
  const THEME_VIDE = { tenantId: '', couleurPrimaire: '#0b4a8a', couleurAccent: '#c8102e', couleurFond: '#f4f6f9', couleurTexte: '#1c2733', logoTexte: 'VINA IT Solutions inc.', logoImage: '' }
  const [theme, setTheme] = useState(THEME_VIDE)
  const [edition, setEdition] = useState(null) // tenant en cours d'édition (Modal), ou null
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  const charger = () => api.get('/superadmin/tenants').then((r) => setTenants(r.data))
  useEffect(() => { charger() }, [])

  if (!hasRole('SUPERADMIN')) return <div className="panel">{tr('superadmin.accesReserve')}</div>

  async function creerTenant(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.post('/superadmin/tenants', form)
      setForm({ slug: '', titre: '', sousTitre: '' }); setMsg(tr('superadmin.tenantCree')); charger()
    } catch (e2) { setErr(e2.response?.data?.message || e2.response?.data?.erreur || tr('superadmin.erreur')) }
  }
  async function creerAdmin(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.post(`/superadmin/tenants/${admin.tenantId}/admin`, {
        email: admin.email, nom: admin.nom, motDePasse: admin.motDePasse })
      setAdmin({ tenantId: '', email: '', nom: '', motDePasse: '' })
      setMsg(tr('superadmin.adminCree'))
    } catch (e2) { setErr(e2.response?.data?.message || tr('superadmin.erreurCourriel')) }
  }
  async function basculerActif(t) {
    await api.put(`/superadmin/tenants/${t.id}`, { actif: !t.actif }); charger()
  }
  async function enregistrerEdition(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.put(`/superadmin/tenants/${edition.id}`, {
        titre: edition.titre, sousTitre: edition.sousTitre, actif: edition.actif })
      setEdition(null); setMsg(tr('superadmin.tenantMaj')); charger()
    } catch (e2) { setErr(e2.response?.data?.message || tr('superadmin.majRefusee')) }
  }
  function choisirThemeTenant(id) {
    const t = tenants.find((x) => String(x.id) === String(id))
    setTheme({
      tenantId: id,
      couleurPrimaire: t?.couleurPrimaire || THEME_VIDE.couleurPrimaire,
      couleurAccent: t?.couleurAccent || THEME_VIDE.couleurAccent,
      couleurFond: t?.couleurFond || THEME_VIDE.couleurFond,
      couleurTexte: t?.couleurTexte || THEME_VIDE.couleurTexte,
      logoTexte: t?.logoTexte || THEME_VIDE.logoTexte,
      logoImage: t?.logoImage || '',
    })
  }
  function chargerLogo(e) {
    const fichier = e.target.files?.[0]
    if (!fichier) return
    if (fichier.size > 500 * 1024) { setErr(tr('superadmin.logoVolumineux')); return }
    const reader = new FileReader()
    reader.onload = () => setTheme((t) => ({ ...t, logoImage: reader.result }))
    reader.readAsDataURL(fichier)
  }
  async function enregistrerTheme(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    if (!theme.tenantId) { setErr(tr('superadmin.choisirTenant')); return }
    await api.put(`/superadmin/tenants/${theme.tenantId}/theme`, {
      couleurPrimaire: theme.couleurPrimaire, couleurAccent: theme.couleurAccent,
      couleurFond: theme.couleurFond, couleurTexte: theme.couleurTexte,
      logoTexte: theme.logoTexte, logoImage: theme.logoImage })
    setMsg(tr('superadmin.themeEnregistre'))
    charger()
  }

  return (
    <div>
      <div className="panel">
        <h1>{tr('superadmin.titre')}</h1>
        {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
        {err && <div className="erreur">{err}</div>}
        <table className="table">
          <thead><tr><th scope="col">{tr('superadmin.thSlug')}</th><th scope="col">{tr('superadmin.thTitreLogin')}</th><th scope="col">{tr('superadmin.thSousTitre')}</th><th scope="col">{tr('superadmin.thActif')}</th><th scope="col"><span className="sr-only">{tr('superadmin.thActions')}</span></th></tr></thead>
          <tbody>
            {tenants.map((t) => (
              <tr key={t.id}>
                <td><code>{t.slug}</code></td><td>{t.titre}</td><td>{t.sousTitre || '—'}</td>
                <td><span className={`badge ${t.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{t.actif ? tr('superadmin.oui') : tr('superadmin.non')}</span></td>
                <td className="actions-cell">
                  <button className="secondary" onClick={() => setEdition({ id: t.id, titre: t.titre, sousTitre: t.sousTitre || '', actif: t.actif })}>{tr('superadmin.editer')}</button>
                  <button className="secondary" onClick={() => basculerActif(t)}>{t.actif ? tr('superadmin.desactiver') : tr('superadmin.activer')}</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="grid2">
        <section className="panel">
          <h2>{tr('superadmin.nouveauTenant')}</h2>
          <form onSubmit={creerTenant} className="form">
            <label>{tr('superadmin.labelSlug')}
              <input value={form.slug} required onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            </label>
            <label>{tr('superadmin.labelTitreConnexion')}
              <input value={form.titre} required onChange={(e) => setForm({ ...form, titre: e.target.value })} />
            </label>
            <label>{tr('superadmin.labelSousTitre')}
              <input value={form.sousTitre} onChange={(e) => setForm({ ...form, sousTitre: e.target.value })} />
            </label>
            <button type="submit">{tr('superadmin.creerTenant')}</button>
          </form>
        </section>

        <section className="panel">
          <h2>{tr('superadmin.creerAdminTitre')}</h2>
          <form onSubmit={creerAdmin} className="form">
            <label>{tr('superadmin.labelTenant')}
              <select value={admin.tenantId} required onChange={(e) => setAdmin({ ...admin, tenantId: e.target.value })}>
                <option value="">{tr('superadmin.choisir')}</option>
                {tenants.map((t) => <option key={t.id} value={t.id}>{t.titre} ({t.slug})</option>)}
              </select>
            </label>
            <label>{tr('superadmin.labelCourriel')}<input type="email" value={admin.email} required onChange={(e) => setAdmin({ ...admin, email: e.target.value })} /></label>
            <label>{tr('superadmin.labelNom')}<input value={admin.nom} required onChange={(e) => setAdmin({ ...admin, nom: e.target.value })} /></label>
            <label>{tr('superadmin.labelMotDePasse')}<input type="password" value={admin.motDePasse} required onChange={(e) => setAdmin({ ...admin, motDePasse: e.target.value })} /></label>
            <button type="submit">{tr('superadmin.creerAdmin')}</button>
          </form>
        </section>
      </div>

      <section className="panel">
        <h2>🎨 {tr('superadmin.themeTitre')}</h2>
        <p className="muted">{tr('superadmin.themeDescription')} <strong>VINA IT Solutions inc.</strong></p>
        <form onSubmit={enregistrerTheme} className="form" style={{ maxWidth: 520 }}>
          <label>{tr('superadmin.labelTenant')}
            <select value={theme.tenantId} onChange={(e) => choisirThemeTenant(e.target.value)}>
              <option value="">{tr('superadmin.choisirTenantOption')}</option>
              {tenants.map((t) => <option key={t.id} value={t.id}>{t.titre} ({t.slug})</option>)}
            </select>
          </label>
          <div className="theme-grid">
            <label>{tr('superadmin.couleurPrimaire')}
              <input type="color" value={theme.couleurPrimaire} onChange={(e) => setTheme({ ...theme, couleurPrimaire: e.target.value })} />
            </label>
            <label>{tr('superadmin.couleurAccent')}
              <input type="color" value={theme.couleurAccent} onChange={(e) => setTheme({ ...theme, couleurAccent: e.target.value })} />
            </label>
            <label>{tr('superadmin.fondPage')}
              <input type="color" value={theme.couleurFond} onChange={(e) => setTheme({ ...theme, couleurFond: e.target.value })} />
            </label>
            <label>{tr('superadmin.couleurTexte')}
              <input type="color" value={theme.couleurTexte} onChange={(e) => setTheme({ ...theme, couleurTexte: e.target.value })} />
            </label>
          </div>
          <label>{tr('superadmin.logoTexte')}
            <input value={theme.logoTexte} onChange={(e) => setTheme({ ...theme, logoTexte: e.target.value })} />
          </label>
          <label>{tr('superadmin.logoImage')}
            <input type="file" accept="image/png,image/jpeg" onChange={chargerLogo} />
          </label>
          {theme.logoImage && (
            <div className="logo-apercu">
              <img src={theme.logoImage} alt={tr('superadmin.altLogoTenant')} />
              <button type="button" className="secondary" onClick={() => setTheme({ ...theme, logoImage: '' })}>
                {tr('superadmin.retirerLogo')}
              </button>
            </div>
          )}
          <div className="theme-preview" style={{ background: theme.couleurFond, color: theme.couleurTexte }}>
            <span className="theme-chip" style={{ background: theme.couleurPrimaire }} />
            <span className="theme-chip" style={{ background: theme.couleurAccent }} />
            {theme.logoImage
              ? <img className="theme-logo" src={theme.logoImage} alt={tr('superadmin.altLogo')} />
              : <strong>{theme.logoTexte}</strong>} — {tr('superadmin.apercu')}
          </div>
          <button type="submit" disabled={!theme.tenantId}>{tr('superadmin.enregistrerTheme')}</button>
        </form>
      </section>

      <section className="panel">
        <h2>⚙️ {tr('superadmin.valeursSelecteurs')}</h2>
        <ReferenceManager portee="superadmin" />
      </section>

      <ModuleManager />

      <TarifTenantManager />

      <ModeleDocumentManager portee="superadmin" />
      <GabaritManager />

      {edition && (
        <Modal titre={tr('superadmin.editerTenant', { titre: edition.titre })} onClose={() => setEdition(null)}>
          <form onSubmit={enregistrerEdition} className="form">
            <label>{tr('superadmin.labelTitreConnexion')}
              <input value={edition.titre} required
                     onChange={(e) => setEdition({ ...edition, titre: e.target.value })} />
            </label>
            <label>{tr('superadmin.labelSousTitre')}
              <input value={edition.sousTitre}
                     onChange={(e) => setEdition({ ...edition, sousTitre: e.target.value })} />
            </label>
            <label className="check-row">
              <input type="checkbox" checked={!!edition.actif}
                     onChange={(e) => setEdition({ ...edition, actif: e.target.checked })} /> {tr('superadmin.tenantActif')}
            </label>
            <div className="vitrine-actions">
              <button type="submit">{tr('superadmin.enregistrerModifs')}</button>
              <button type="button" className="secondary" onClick={() => setEdition(null)}>{tr('superadmin.annuler')}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
