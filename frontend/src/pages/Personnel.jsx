import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

const FREQUENCES = ['weekly', 'biweekly', 'semimonthly', 'monthly']
const TYPES = ['employee', 'self_employed', 'contractor']
const VIDE = { prenom: '', nom: '', nas: '', courriel: '', telephone: '', dateEmbauche: '', poste: '', departement: '', salaireAnnuel: 60000, frequence: 'biweekly', province: 'QC', typeTravailleur: 'employee', statut: 'ACTIF' }

/** Gestion du personnel (RH) : liste, recherche, création/édition d'un dossier employé. */
export default function Personnel() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [liste, setListe] = useState([])
  const [recherche, setRecherche] = useState('')
  const [form, setForm] = useState(VIDE)
  const [editionId, setEditionId] = useState(null)
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/rh/employes').then((r) => setListe(r.data || [])).catch(() => setListe([]))
  }, [])
  useEffect(() => { if (!chargement && actif('rh')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('personnel.chargement')}</div>
  if (!actif('rh')) return <div className="panel">{t('personnel.moduleInactif')}</div>

  async function soumettre(e) {
    e.preventDefault(); setErr(null)
    try {
      if (editionId) await api.put(`/rh/employes/${editionId}`, form)
      else await api.post('/rh/employes', form)
      setForm(VIDE); setEditionId(null); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('personnel.erreur')) }
  }
  function editer(x) {
    setEditionId(x.id)
    setForm({ prenom: x.prenom, nom: x.nom, nas: '', courriel: x.courriel || '', telephone: x.telephone || '', dateEmbauche: x.dateEmbauche || '', poste: x.poste || '', departement: x.departement || '', salaireAnnuel: x.salaireAnnuel, frequence: x.frequence || 'biweekly', province: x.province || 'QC', typeTravailleur: x.typeTravailleur || 'employee', statut: x.statut || 'ACTIF' })
  }
  async function archiver(x) { await api.put(`/rh/employes/${x.id}/archiver`, null, { params: { actif: !x.actif } }); charger() }

  const filtree = liste.filter((x) => {
    const q = recherche.trim().toLowerCase()
    return !q || `${x.prenom} ${x.nom} ${x.matricule} ${x.poste || ''} ${x.departement || ''}`.toLowerCase().includes(q)
  })

  return (
    <div>
      <div className="panel">
        <h1>{t('personnel.titre')}</h1>
        <p className="muted">{t('personnel.intro')}</p>
        <label>{t('personnel.recherche')}
          <input value={recherche} onChange={(e) => setRecherche(e.target.value)} placeholder={t('personnel.recherchePh')} />
        </label>
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr>
            <th scope="col">{t('personnel.colMatricule')}</th>
            <th scope="col">{t('personnel.colNom')}</th>
            <th scope="col">{t('personnel.colPoste')}</th>
            <th scope="col">{t('personnel.colStatut')}</th>
            <th scope="col"><span className="sr-only">{t('personnel.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {filtree.map((x) => (
              <tr key={x.id} style={x.actif ? undefined : { opacity: 0.6 }}>
                <td><code>{x.matricule || x.id}</code></td>
                <td><Link to={`/rh/employes/${x.id}`}>{x.prenom} {x.nom}</Link></td>
                <td>{x.poste || '—'}{x.departement ? ` · ${x.departement}` : ''}</td>
                <td><span className={`badge ${x.statut === 'ACTIF' ? 's-APPROUVE' : 's-REFUSE'}`}>{t(`personnel.statut.${x.statut}`)}</span></td>
                <td className="cmp-row">
                  <button className="secondary" onClick={() => editer(x)}>{t('personnel.editer')}</button>
                  <button className="secondary" onClick={() => archiver(x)}>{x.actif ? t('personnel.archiver') : t('personnel.reactiver')}</button>
                </td>
              </tr>
            ))}
            {filtree.length === 0 && <tr><td colSpan="5" className="muted">{t('personnel.aucun')}</td></tr>}
          </tbody>
        </table>
      </div>

      <section className="panel">
        <h2>{editionId ? t('personnel.modifier') : t('personnel.nouveau')}</h2>
        <form onSubmit={soumettre} className="form">
          <div className="cmp-row">
            <input placeholder={t('personnel.prenom')} value={form.prenom} required onChange={(e) => setForm({ ...form, prenom: e.target.value })} />
            <input placeholder={t('personnel.nom')} value={form.nom} required onChange={(e) => setForm({ ...form, nom: e.target.value })} />
            <input placeholder={t('personnel.nas')} value={form.nas} onChange={(e) => setForm({ ...form, nas: e.target.value })} />
          </div>
          <div className="cmp-row">
            <input type="email" placeholder={t('personnel.courriel')} value={form.courriel} onChange={(e) => setForm({ ...form, courriel: e.target.value })} />
            <input placeholder={t('personnel.telephone')} value={form.telephone} onChange={(e) => setForm({ ...form, telephone: e.target.value })} />
            <input type="date" aria-label={t('personnel.dateEmbauche')} value={form.dateEmbauche} onChange={(e) => setForm({ ...form, dateEmbauche: e.target.value })} />
          </div>
          <div className="cmp-row">
            <input placeholder={t('personnel.poste')} value={form.poste} onChange={(e) => setForm({ ...form, poste: e.target.value })} />
            <input placeholder={t('personnel.departement')} value={form.departement} onChange={(e) => setForm({ ...form, departement: e.target.value })} />
            <input type="number" placeholder={t('personnel.salaire')} value={form.salaireAnnuel} onChange={(e) => setForm({ ...form, salaireAnnuel: Number(e.target.value) })} />
          </div>
          <div className="cmp-row">
            <select value={form.frequence} onChange={(e) => setForm({ ...form, frequence: e.target.value })} aria-label={t('personnel.frequence')}>
              {FREQUENCES.map((f) => <option key={f} value={f}>{f}</option>)}
            </select>
            <select value={form.typeTravailleur} onChange={(e) => setForm({ ...form, typeTravailleur: e.target.value })} aria-label={t('personnel.type')}>
              {TYPES.map((x) => <option key={x} value={x}>{x}</option>)}
            </select>
          </div>
          {err && <div className="erreur" role="alert">{err}</div>}
          <div className="cmp-row">
            <button type="submit">{editionId ? t('personnel.enregistrer') : t('personnel.creer')}</button>
            {editionId && <button type="button" className="secondary" onClick={() => { setForm(VIDE); setEditionId(null) }}>{t('personnel.annuler')}</button>}
          </div>
        </form>
      </section>
    </div>
  )
}
