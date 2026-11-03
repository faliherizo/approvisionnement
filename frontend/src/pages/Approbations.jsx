import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useAuth } from '../auth.jsx'
import { useModules } from '../hooks/useModules.js'

const OPERATEURS = ['gte', 'lte', 'eq', 'neq']

/** Gestionnaire d'approbations multi-niveaux (moteur Approbations) : règles, demandes, mes étapes, décision. */
export default function Approbations() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { actif, chargement } = useModules()
  const [etapes, setEtapes] = useState([])
  const [regles, setRegles] = useState([])
  const [demandes, setDemandes] = useState([])
  const [rForm, setRForm] = useState({ documentType: 'achat', champ: 'montant', operateur: 'gte', valeur: '10000', niveau: 1, approbateurRole: 'DIRECTION', approbateurId: '' })
  const [dForm, setDForm] = useState({ documentType: 'achat', documentId: '', montant: 25000 })
  const [err, setErr] = useState(null)
  const admin = hasRole('ADMIN') || hasRole('SUPERADMIN')

  const charger = useCallback(() => {
    api.get('/approbations/mes-etapes').then((r) => setEtapes(r.data || [])).catch(() => setEtapes([]))
    api.get('/approbations/regles').then((r) => setRegles(r.data || [])).catch(() => setRegles([]))
    api.get('/approbations/demandes').then((r) => setDemandes(r.data || [])).catch(() => setDemandes([]))
  }, [])
  useEffect(() => { if (!chargement && actif('processus')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('appro.chargement')}</div>
  if (!actif('processus')) return <div className="panel">{t('appro.moduleInactif')}</div>

  async function creerRegle(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/approbations/regles', { ...rForm, niveau: Number(rForm.niveau) }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('appro.erreur')) }
  }
  async function creerDemande(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/approbations/demandes', { documentType: dForm.documentType, documentId: dForm.documentId || null, contexte: { montant: Number(dForm.montant) } }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('appro.erreur')) }
  }
  async function decider(etapeId, decision) {
    setErr(null)
    try { await api.post(`/approbations/etapes/${etapeId}/decider`, { decision, commentaire: null }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('appro.erreur')) }
  }
  const badge = (s) => s === 'APPROVED' ? 's-APPROUVE' : s === 'REJECTED' ? 's-REFUSE' : ''

  return (
    <div>
      <div className="panel">
        <h1>{t('appro.titre')}</h1>
        <p className="muted">{t('appro.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      <section className="panel">
        <h2>{t('appro.mesEtapes')} <span className="badge">{etapes.length}</span></h2>
        <table className="table">
          <thead><tr><th scope="col">{t('appro.colDemande')}</th><th scope="col">{t('appro.colNiveau')}</th><th scope="col">{t('appro.colRole')}</th><th scope="col"><span className="sr-only">{t('appro.colActions')}</span></th></tr></thead>
          <tbody>
            {etapes.map((e) => (
              <tr key={e.id}>
                <td><code>{e.documentType}{e.documentId ? ` · ${e.documentId}` : ''}</code> (#{e.demandeId})</td>
                <td>{e.niveau}</td><td>{e.approbateurRole || e.approbateurId}</td>
                <td className="cmp-row">
                  <button onClick={() => decider(e.id, 'APPROUVE')}>{t('appro.approuver')}</button>
                  <button className="danger" onClick={() => decider(e.id, 'REFUSE')}>{t('appro.refuser')}</button>
                </td>
              </tr>
            ))}
            {etapes.length === 0 && <tr><td colSpan="4" className="muted">{t('appro.aucuneEtape')}</td></tr>}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h2>{t('appro.nouvelleDemande')}</h2>
        <form onSubmit={creerDemande} className="form">
          <div className="cmp-row">
            <input value={dForm.documentType} onChange={(e) => setDForm({ ...dForm, documentType: e.target.value })} placeholder={t('appro.documentType')} />
            <input value={dForm.documentId} onChange={(e) => setDForm({ ...dForm, documentId: e.target.value })} placeholder={t('appro.documentId')} />
            <input type="number" value={dForm.montant} onChange={(e) => setDForm({ ...dForm, montant: e.target.value })} placeholder={t('appro.montant')} />
            <button type="submit">{t('appro.creerDemande')}</button>
          </div>
        </form>
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr><th scope="col">#</th><th scope="col">{t('appro.colDemande')}</th><th scope="col">{t('appro.colStatut')}</th><th scope="col">{t('appro.colEtapes')}</th></tr></thead>
          <tbody>
            {demandes.map((d) => (
              <tr key={d.id}>
                <td>{d.id}</td><td><code>{d.documentType}</code></td>
                <td><span className={`badge ${badge(d.statut)}`}>{d.statut}</span></td>
                <td>{d.etapes?.map((e) => `N${e.niveau}:${e.statut}`).join(' · ') || t('appro.directe')}</td>
              </tr>
            ))}
            {demandes.length === 0 && <tr><td colSpan="4" className="muted">{t('appro.aucuneDemande')}</td></tr>}
          </tbody>
        </table>
      </section>

      {admin && (
        <section className="panel">
          <h2>{t('appro.regles')}</h2>
          <table className="table">
            <thead><tr><th scope="col">{t('appro.documentType')}</th><th scope="col">{t('appro.condition')}</th><th scope="col">{t('appro.colNiveau')}</th><th scope="col">{t('appro.colRole')}</th></tr></thead>
            <tbody>
              {regles.map((r) => (
                <tr key={r.id}><td><code>{r.documentType}</code></td><td>{r.champ} {r.operateur} {r.valeur}</td><td>{r.niveau}</td><td>{r.approbateurRole || r.approbateurId}</td></tr>
              ))}
              {regles.length === 0 && <tr><td colSpan="4" className="muted">{t('appro.aucuneRegle')}</td></tr>}
            </tbody>
          </table>
          <form onSubmit={creerRegle} className="form" style={{ marginTop: 12 }}>
            <div className="cmp-row">
              <input value={rForm.documentType} onChange={(e) => setRForm({ ...rForm, documentType: e.target.value })} placeholder={t('appro.documentType')} />
              <input value={rForm.champ} onChange={(e) => setRForm({ ...rForm, champ: e.target.value })} placeholder={t('appro.champ')} />
              <select value={rForm.operateur} onChange={(e) => setRForm({ ...rForm, operateur: e.target.value })} aria-label={t('appro.operateur')}>
                {OPERATEURS.map((o) => <option key={o} value={o}>{o}</option>)}
              </select>
              <input value={rForm.valeur} onChange={(e) => setRForm({ ...rForm, valeur: e.target.value })} placeholder={t('appro.valeur')} style={{ width: 90 }} />
              <input type="number" min="1" value={rForm.niveau} onChange={(e) => setRForm({ ...rForm, niveau: e.target.value })} style={{ width: 70 }} aria-label={t('appro.colNiveau')} />
              <input value={rForm.approbateurRole} onChange={(e) => setRForm({ ...rForm, approbateurRole: e.target.value })} placeholder={t('appro.colRole')} />
              <button type="submit">{t('appro.ajouterRegle')}</button>
            </div>
          </form>
        </section>
      )}
    </div>
  )
}
