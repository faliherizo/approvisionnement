import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useAuth } from '../auth.jsx'
import { useModules } from '../hooks/useModules.js'

const STATUTS = ['A_FAIRE', 'EN_COURS', 'TERMINEE', 'ANNULEE']
const TYPES = ['feature', 'bug', 'admin', 'autre']
const TACHE_VIDE = { projetId: '', nom: '', description: '', typeTache: 'feature', statut: 'A_FAIRE', assigneId: '', heuresEstimees: '', echeance: '' }

/** Projets & tâches (Espace RH) : création de tâches, assignation à une personne, suivi — porté de Nexu VINA. */
export default function ProjetsTaches() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { actif, chargement } = useModules()
  const [projets, setProjets] = useState([])
  const [employes, setEmployes] = useState([])
  const [taches, setTaches] = useState([])
  const [mesTaches, setMesTaches] = useState([])
  const [filtreProjet, setFiltreProjet] = useState('')
  const [pForm, setPForm] = useState({ code: '', nom: '', gestionnaire: '', budgetHeures: '' })
  const [tForm, setTForm] = useState(TACHE_VIDE)
  const [editId, setEditId] = useState(null)
  const [err, setErr] = useState(null)
  const gere = hasRole('ADMIN') || hasRole('DIRECTION') || hasRole('APPRO')

  const charger = useCallback(() => {
    api.get('/rh/projets').then((r) => setProjets(r.data || [])).catch(() => setProjets([]))
    api.get('/rh/employes').then((r) => setEmployes(r.data || [])).catch(() => setEmployes([]))
    api.get('/rh/taches', { params: filtreProjet ? { projetId: filtreProjet } : {} }).then((r) => setTaches(r.data || [])).catch(() => setTaches([]))
    api.get('/rh/taches/mine').then((r) => setMesTaches(r.data || [])).catch(() => setMesTaches([]))
  }, [filtreProjet])
  useEffect(() => { if (!chargement && actif('rh')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('pt.chargement')}</div>
  if (!actif('rh')) return <div className="panel">{t('pt.moduleInactif')}</div>

  async function creerProjet(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/rh/projets', { ...pForm, budgetHeures: pForm.budgetHeures ? Number(pForm.budgetHeures) : null }); setPForm({ code: '', nom: '', gestionnaire: '', budgetHeures: '' }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('pt.erreur')) }
  }
  async function soumettreTache(e) {
    e.preventDefault(); setErr(null)
    const body = { ...tForm, projetId: Number(tForm.projetId), assigneId: tForm.assigneId ? Number(tForm.assigneId) : null, heuresEstimees: tForm.heuresEstimees ? Number(tForm.heuresEstimees) : null }
    try {
      if (editId) await api.put(`/rh/taches/${editId}`, body)
      else await api.post('/rh/taches', body)
      setTForm(TACHE_VIDE); setEditId(null); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('pt.erreur')) }
  }
  function editer(x) { setEditId(x.id); setTForm({ projetId: x.projetId, nom: x.nom, description: x.description || '', typeTache: x.typeTache || 'feature', statut: x.statut, assigneId: x.assigneId || '', heuresEstimees: x.heuresEstimees ?? '', echeance: x.echeance || '' }) }
  async function supprimer(id) { if (!window.confirm(t('pt.confirmSuppr'))) return; await api.delete(`/rh/taches/${id}`); charger() }
  const badge = (s) => s === 'TERMINEE' ? 's-APPROUVE' : s === 'ANNULEE' ? 's-REFUSE' : ''
  const nomEmploye = (id) => { const e = employes.find((x) => x.id === id); return e ? `${e.prenom} ${e.nom}` : '—' }

  const tableTaches = (liste, avecActions) => (
    <table className="table">
      <thead><tr>
        <th scope="col">{t('pt.colTache')}</th><th scope="col">{t('pt.colProjet')}</th><th scope="col">{t('pt.colAssigne')}</th>
        <th scope="col">{t('pt.colStatut')}</th><th scope="col">{t('pt.colEcheance')}</th>
        {avecActions && <th scope="col"><span className="sr-only">x</span></th>}
      </tr></thead>
      <tbody>
        {liste.map((x) => (
          <tr key={x.id}>
            <td><strong>{x.nom}</strong>{x.heuresEstimees ? <span className="muted"> · {x.heuresEstimees} h</span> : null}</td>
            <td>{x.projetNom}</td>
            <td>{x.assigneNom || <span className="muted">{t('pt.nonAssigne')}</span>}</td>
            <td><span className={`badge ${badge(x.statut)}`}>{t(`pt.statut.${x.statut}`)}</span></td>
            <td>{x.echeance || '—'}</td>
            {avecActions && <td className="cmp-row">
              <button className="secondary" onClick={() => editer(x)}>{t('pt.editer')}</button>
              <button className="danger" onClick={() => supprimer(x.id)}>{t('pt.supprimer')}</button>
            </td>}
          </tr>
        ))}
        {liste.length === 0 && <tr><td colSpan={avecActions ? 6 : 5} className="muted">{t('pt.aucuneTache')}</td></tr>}
      </tbody>
    </table>
  )

  return (
    <div>
      <div className="panel">
        <h1>{t('pt.titre')}</h1>
        <p className="muted">{t('pt.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      <section className="panel">
        <h2>{t('pt.mesTaches')} <span className="badge">{mesTaches.length}</span></h2>
        {tableTaches(mesTaches, false)}
      </section>

      <section className="panel">
        <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <h2>{t('pt.toutesTaches')}</h2>
          <label>{t('pt.filtreProjet')}
            <select value={filtreProjet} onChange={(e) => setFiltreProjet(e.target.value)}>
              <option value="">{t('pt.tousProjets')}</option>
              {projets.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
            </select>
          </label>
        </div>
        {tableTaches(taches, gere)}
        {gere && (
          <form onSubmit={soumettreTache} className="form" style={{ marginTop: 12 }}>
            <h3>{editId ? t('pt.modifierTache') : t('pt.nouvelleTache')}</h3>
            <div className="cmp-row">
              <select value={tForm.projetId} required onChange={(e) => setTForm({ ...tForm, projetId: e.target.value })} aria-label={t('pt.colProjet')}>
                <option value="">{t('pt.choisirProjet')}</option>
                {projets.map((p) => <option key={p.id} value={p.id}>{p.nom}</option>)}
              </select>
              <input placeholder={t('pt.nomTache')} value={tForm.nom} required onChange={(e) => setTForm({ ...tForm, nom: e.target.value })} />
              <select value={tForm.assigneId} onChange={(e) => setTForm({ ...tForm, assigneId: e.target.value })} aria-label={t('pt.colAssigne')}>
                <option value="">{t('pt.nonAssigne')}</option>
                {employes.map((emp) => <option key={emp.id} value={emp.id}>{emp.prenom} {emp.nom}</option>)}
              </select>
            </div>
            <div className="cmp-row">
              <select value={tForm.typeTache} onChange={(e) => setTForm({ ...tForm, typeTache: e.target.value })} aria-label={t('pt.type')}>
                {TYPES.map((x) => <option key={x} value={x}>{x}</option>)}
              </select>
              <select value={tForm.statut} onChange={(e) => setTForm({ ...tForm, statut: e.target.value })} aria-label={t('pt.colStatut')}>
                {STATUTS.map((x) => <option key={x} value={x}>{t(`pt.statut.${x}`)}</option>)}
              </select>
              <input type="number" min="0" step="0.5" placeholder={t('pt.heures')} value={tForm.heuresEstimees} style={{ width: 120 }} onChange={(e) => setTForm({ ...tForm, heuresEstimees: e.target.value })} />
              <input type="date" aria-label={t('pt.colEcheance')} value={tForm.echeance} onChange={(e) => setTForm({ ...tForm, echeance: e.target.value })} />
            </div>
            <input placeholder={t('pt.description')} value={tForm.description} onChange={(e) => setTForm({ ...tForm, description: e.target.value })} />
            <div className="cmp-row">
              <button type="submit">{editId ? t('pt.enregistrer') : t('pt.creerTache')}</button>
              {editId && <button type="button" className="secondary" onClick={() => { setTForm(TACHE_VIDE); setEditId(null) }}>{t('pt.annuler')}</button>}
            </div>
          </form>
        )}
      </section>

      {gere && (
        <section className="panel">
          <h2>{t('pt.projets')}</h2>
          <table className="table">
            <thead><tr><th scope="col">{t('pt.colCode')}</th><th scope="col">{t('pt.colNomProjet')}</th><th scope="col">{t('pt.colGestionnaire')}</th><th scope="col">{t('pt.colBudget')}</th></tr></thead>
            <tbody>
              {projets.map((p) => <tr key={p.id}><td><code>{p.code}</code></td><td>{p.nom}</td><td>{p.gestionnaire || '—'}</td><td>{p.budgetHeures ?? '—'}</td></tr>)}
              {projets.length === 0 && <tr><td colSpan="4" className="muted">{t('pt.aucunProjet')}</td></tr>}
            </tbody>
          </table>
          <form onSubmit={creerProjet} className="form" style={{ marginTop: 12 }}>
            <div className="cmp-row">
              <input placeholder={t('pt.colCode')} value={pForm.code} required onChange={(e) => setPForm({ ...pForm, code: e.target.value })} />
              <input placeholder={t('pt.colNomProjet')} value={pForm.nom} required onChange={(e) => setPForm({ ...pForm, nom: e.target.value })} />
              <input placeholder={t('pt.colGestionnaire')} value={pForm.gestionnaire} onChange={(e) => setPForm({ ...pForm, gestionnaire: e.target.value })} />
              <input type="number" placeholder={t('pt.colBudget')} value={pForm.budgetHeures} style={{ width: 120 }} onChange={(e) => setPForm({ ...pForm, budgetHeures: e.target.value })} />
              <button type="submit">{t('pt.creerProjet')}</button>
            </div>
          </form>
        </section>
      )}
    </div>
  )
}
