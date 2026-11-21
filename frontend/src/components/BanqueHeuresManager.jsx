import { useState } from 'react'
import api from '../api'
import ReleveBanque, { CATEGORIES } from './ReleveBanque.jsx'
import { useI18n } from '../i18n/I18nContext'

const BANQUE_VIDE = { nom: '', debut: '', fin: '', heures: '' }
const MOUVEMENT_VIDE = { type: 'CONSOMMATION', categorie: 'ANALYSE', heures: '', date: '', description: '', intervenant: '' }

/** Banques d'heures tenues par le fournisseur (CHU 9.05) : ouverture et inscription des achats, consommations, corrections. */
export default function BanqueHeuresManager({ tenants }) {
  const { t } = useI18n()
  const [tenant, setTenant] = useState('')
  const [banques, setBanques] = useState([])
  const [courante, setCourante] = useState(null)
  const [nouvelle, setNouvelle] = useState(BANQUE_VIDE)
  const [mouvement, setMouvement] = useState(MOUVEMENT_VIDE)
  const [erreur, setErreur] = useState(null)

  const base = (tid) => `/superadmin/tenants/${tid}/banques-heures`
  async function choisirTenant(tid) {
    setTenant(tid); setCourante(null); setErreur(null)
    if (tid) setBanques((await api.get(base(tid))).data)
  }
  async function faire(action) {
    setErreur(null)
    try { const r = await action(); setCourante(r.data); setBanques((await api.get(base(tenant))).data); return true }
    catch (e) { setErreur(e.response?.data?.message || t('banque.erreur')); return false }
  }

  return (
    <section className="panel" aria-labelledby="banques-sa">
      <h2 id="banques-sa">{t('banque.gestion')}</h2>
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
      <label>{t('banque.organisation')}
        <select value={tenant} onChange={(e) => choisirTenant(e.target.value)}>
          <option value="">—</option>
          {tenants.map((x) => <option key={x.id} value={x.id}>{x.titre}</option>)}
        </select>
      </label>
      {tenant && (
        <>
          <ul>
            {banques.map((b) => (
              <li key={b.id}>
                <button type="button" className="btn-lien" onClick={() => faire(() => api.get(`${base(tenant)}/${b.id}`))}>
                  {b.nom} — {t('banque.resteH', { n: b.solde.restantes })}
                </button>
              </li>
            ))}
          </ul>
          <form className="form" onSubmit={async (e) => { e.preventDefault(); if (await faire(() => api.post(base(tenant), nouvelle))) setNouvelle(BANQUE_VIDE) }}>
            <h3>{t('banque.ouvrir')}</h3>
            <div className="cmp-row" style={{ flexWrap: 'wrap', gap: 8 }}>
              <label>{t('banque.nom')}<input required value={nouvelle.nom} onChange={(e) => setNouvelle({ ...nouvelle, nom: e.target.value })} /></label>
              <label>{t('banque.debut')}<input type="date" required value={nouvelle.debut} onChange={(e) => setNouvelle({ ...nouvelle, debut: e.target.value })} /></label>
              <label>{t('banque.fin')}<input type="date" required value={nouvelle.fin} onChange={(e) => setNouvelle({ ...nouvelle, fin: e.target.value })} /></label>
              <label>{t('banque.heuresAchetees')}<input type="number" min="0.25" step="0.25" required value={nouvelle.heures} onChange={(e) => setNouvelle({ ...nouvelle, heures: e.target.value })} /></label>
            </div>
            <button type="submit">{t('banque.ouvrir')}</button>
          </form>
        </>
      )}
      {courante && (
        <>
          <h3>{courante.nom}</h3>
          <ReleveBanque banque={courante} />
          <form className="form" onSubmit={async (e) => { e.preventDefault(); if (await faire(() => api.post(`${base(tenant)}/${courante.id}/mouvements`, mouvement))) setMouvement(MOUVEMENT_VIDE) }}>
            <h3>{t('banque.inscrire')}</h3>
            <div className="cmp-row" style={{ flexWrap: 'wrap', gap: 8 }}>
              <label>{t('banque.type')}
                <select value={mouvement.type} onChange={(e) => setMouvement({ ...mouvement, type: e.target.value })}>
                  {['CONSOMMATION', 'ACHAT', 'CORRECTION'].map((x) => <option key={x} value={x}>{t(`banque.type.${x}`)}</option>)}
                </select>
              </label>
              {mouvement.type !== 'ACHAT' && (
                <label>{t('banque.categorie')}
                  <select value={mouvement.categorie} onChange={(e) => setMouvement({ ...mouvement, categorie: e.target.value })}>
                    {CATEGORIES.map((c) => <option key={c} value={c}>{t(`banque.categorie.${c}`)}</option>)}
                  </select>
                </label>
              )}
              <label>{t('banque.heures')}<input type="number" min="0.25" step="0.25" required value={mouvement.heures} onChange={(e) => setMouvement({ ...mouvement, heures: e.target.value })} /></label>
              <label>{t('banque.date')}<input type="date" required value={mouvement.date} onChange={(e) => setMouvement({ ...mouvement, date: e.target.value })} /></label>
              <label>{t('banque.description')}<input required maxLength={500} value={mouvement.description} onChange={(e) => setMouvement({ ...mouvement, description: e.target.value })} /></label>
              {mouvement.type === 'CONSOMMATION' && (
                <label>{t('banque.intervenant')}<input required maxLength={150} value={mouvement.intervenant} onChange={(e) => setMouvement({ ...mouvement, intervenant: e.target.value })} /></label>
              )}
            </div>
            <button type="submit">{t('banque.inscrire')}</button>
          </form>
        </>
      )}
    </section>
  )
}
