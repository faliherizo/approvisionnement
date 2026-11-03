import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'
import FormulaireBuilder from '../components/FormulaireBuilder.jsx'
import WorkflowBuilder from '../components/WorkflowBuilder.jsx'
import VersionsPanel from '../components/VersionsPanel.jsx'

/** Espace de conception des formulaires : liste, création, édition de structure, cycle de vie. */
export default function Formulaires() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [liste, setListe] = useState([])
  const [domaines, setDomaines] = useState([])
  const [filtreDomaine, setFiltreDomaine] = useState('')
  const [form, setForm] = useState({ code: '', nom: '', domaine: '', conservationJours: '' })
  const [choisi, setChoisi] = useState(null)
  const [onglet, setOnglet] = useState('structure')
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    const params = filtreDomaine ? { domaine: filtreDomaine } : {}
    api.get('/formulaires', { params }).then((r) => setListe(r.data || [])).catch(() => setListe([]))
  }, [filtreDomaine])
  useEffect(() => { charger() }, [charger])
  useEffect(() => { api.get('/reference/DOMAINE').then((r) => setDomaines(r.data || [])).catch(() => setDomaines([])) }, [])

  const libelleDomaine = (code) => domaines.find((d) => d.code === code)?.libelle || code || '—'

  if (chargement) return <div className="panel">{t('formulaires.chargement')}</div>
  if (!actif('formulaire')) return <div className="panel">{t('formulaires.moduleInactif')}</div>

  async function creer(e) {
    e.preventDefault(); setErr(null)
    try {
      const r = await api.post('/formulaires', { code: form.code, nom: form.nom, domaine: form.domaine || null, parentId: null, conservationJours: form.conservationJours ? Number(form.conservationJours) : null })
      setForm({ code: '', nom: '', domaine: '', conservationJours: '' }); charger(); setChoisi(r.data.id)
    } catch (e2) { setErr(e2.response?.data?.message || t('formulaires.erreurCreation')) }
  }
  async function supprimer(id) {
    if (!window.confirm(t('formulaires.confirmerSuppression'))) return
    await api.delete(`/formulaires/${id}`); if (choisi === id) setChoisi(null); charger()
  }

  if (choisi) {
    const f = liste.find((x) => x.id === choisi)
    return (
      <div>
        <div className="panel">
          <button className="secondary" onClick={() => { setChoisi(null); setOnglet('structure'); charger() }}>← {t('formulaires.retourListe')}</button>
          <h1 style={{ marginTop: 8 }}>{f ? f.nom : t('formulaires.titre')}</h1>
          {f && <p className="muted"><code>{f.code}</code></p>}
          <div className="cmp-row" role="tablist">
            <button className={onglet === 'structure' ? '' : 'secondary'} role="tab" aria-selected={onglet === 'structure'} onClick={() => setOnglet('structure')}>{t('formulaires.ongletStructure')}</button>
            {actif('processus') && <button className={onglet === 'workflow' ? '' : 'secondary'} role="tab" aria-selected={onglet === 'workflow'} onClick={() => setOnglet('workflow')}>{t('formulaires.ongletWorkflow')}</button>}
            <button className={onglet === 'versions' ? '' : 'secondary'} role="tab" aria-selected={onglet === 'versions'} onClick={() => setOnglet('versions')}>{t('formulaires.ongletVersions')}</button>
          </div>
        </div>
        {onglet === 'workflow' && actif('processus') && <WorkflowBuilder id={choisi} />}
        {onglet === 'versions' && <VersionsPanel id={choisi} onChange={charger} />}
        {onglet === 'structure' && <FormulaireBuilder id={choisi} onChange={charger} />}
      </div>
    )
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('formulaires.titre')}</h1>
        <p className="muted">{t('formulaires.intro')}</p>
        <label>{t('formulaires.filtreDomaine')}
          <select value={filtreDomaine} onChange={(e) => setFiltreDomaine(e.target.value)}>
            <option value="">{t('formulaires.tousDomaines')}</option>
            {domaines.map((d) => <option key={d.code} value={d.code}>{d.libelle}</option>)}
          </select>
        </label>
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr>
            <th scope="col">{t('formulaires.colNom')}</th>
            <th scope="col">{t('formulaires.colCode')}</th>
            <th scope="col">{t('formulaires.colDomaine')}</th>
            <th scope="col">{t('formulaires.colVersion')}</th>
            <th scope="col">{t('formulaires.colStatut')}</th>
            <th scope="col"><span className="sr-only">{t('formulaires.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {liste.map((f) => (
              <tr key={f.id}>
                <td><strong>{f.nom}</strong></td>
                <td><code>{f.code}</code></td>
                <td>{f.domaine ? <span className="badge">{libelleDomaine(f.domaine)}</span> : <span className="muted">—</span>}</td>
                <td>{f.versionCourante != null ? `v${f.versionCourante}` : '—'}</td>
                <td>{f.statutCourant ? <span className={`badge ${f.statutCourant === 'PUBLIE' ? 's-APPROUVE' : ''}`}>{t(`statut.${f.statutCourant}`)}</span> : '—'}</td>
                <td className="cmp-row">
                  <button className="secondary" onClick={() => setChoisi(f.id)}>{t('formulaires.concevoir')}</button>
                  {actif('saisie') && f.statutCourant === 'PUBLIE' && <Link className="btn-lien" to={`/formulaires/${f.id}/remplir`}>{t('formulaires.remplir')}</Link>}
                  {actif('saisie') && <Link className="btn-lien" to={`/formulaires/${f.id}/fiches`}>{t('formulaires.fiches')}</Link>}
                  <button className="danger" onClick={() => supprimer(f.id)}>{t('formulaires.supprimer')}</button>
                </td>
              </tr>
            ))}
            {liste.length === 0 && <tr><td colSpan="6" className="muted">{t('formulaires.aucun')}</td></tr>}
          </tbody>
        </table>
      </div>

      <section className="panel">
        <h2>{t('formulaires.nouveau')}</h2>
        <form onSubmit={creer} className="form">
          <div className="cmp-row">
            <input placeholder={t('formulaires.phCode')} value={form.code} required
                   onChange={(e) => setForm({ ...form, code: e.target.value })} />
            <input placeholder={t('formulaires.phNom')} value={form.nom} required
                   onChange={(e) => setForm({ ...form, nom: e.target.value })} />
            <select value={form.domaine} onChange={(e) => setForm({ ...form, domaine: e.target.value })} aria-label={t('formulaires.colDomaine')}>
              <option value="">{t('formulaires.sansDomaine')}</option>
              {domaines.map((d) => <option key={d.code} value={d.code}>{d.libelle}</option>)}
            </select>
            <input type="number" min="0" style={{ width: 150 }} placeholder={t('formulaires.conservation')} value={form.conservationJours}
                   onChange={(e) => setForm({ ...form, conservationJours: e.target.value })} aria-label={t('formulaires.conservation')} />
            <button type="submit">{t('formulaires.creer')}</button>
          </div>
          {err && <div className="erreur">{err}</div>}
        </form>
      </section>
    </div>
  )
}
