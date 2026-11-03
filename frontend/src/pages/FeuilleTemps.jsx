import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useAuth } from '../auth.jsx'
import { useModules } from '../hooks/useModules.js'

const CATEGORIES = ['travail', 'reunion', 'formation', 'conge', 'maladie', 'autre']
function ligneVide(date) { return { date: date || '', categorie: 'travail', heures: 0 } }
function parseLignes(json) { try { return JSON.parse(json || '[]') } catch { return [] } }

/** Feuille de temps (Espace RH) : saisie des heures par jour/catégorie, soumission, approbation. */
export default function FeuilleTemps() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { actif, chargement } = useModules()
  const [mine, setMine] = useState([])
  const [aApprouver, setAApprouver] = useState([])
  const [edition, setEdition] = useState(null) // {id?, periodeDebut, periodeFin, lignes:[]}
  const [err, setErr] = useState(null)
  const gestionnaire = hasRole('ADMIN') || hasRole('DIRECTION') || hasRole('APPRO')

  const charger = useCallback(() => {
    api.get('/rh/feuilles-temps/mine').then((r) => setMine(r.data || [])).catch(() => setMine([]))
    if (gestionnaire) api.get('/rh/feuilles-temps/a-approuver').then((r) => setAApprouver(r.data || [])).catch(() => setAApprouver([]))
  }, [gestionnaire])
  useEffect(() => { if (!chargement && actif('rh')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('temps.chargement')}</div>
  if (!actif('rh')) return <div className="panel">{t('temps.moduleInactif')}</div>

  const total = (lignes) => lignes.reduce((s, l) => s + (Number(l.heures) || 0), 0)

  function nouvelle() { setEdition({ periodeDebut: '', periodeFin: '', lignes: [ligneVide()] }) }
  function editer(f) { setEdition({ id: f.id, periodeDebut: f.periodeDebut || '', periodeFin: f.periodeFin || '', lignes: parseLignes(f.lignesJson), statut: f.statut }) }
  const majLigne = (i, k, v) => setEdition({ ...edition, lignes: edition.lignes.map((l, j) => j === i ? { ...l, [k]: v } : l) })

  async function enregistrer() {
    setErr(null)
    const body = { periodeDebut: edition.periodeDebut, periodeFin: edition.periodeFin, lignes: edition.lignes.map((l) => ({ ...l, heures: Number(l.heures) || 0 })) }
    try {
      if (edition.id) await api.put(`/rh/feuilles-temps/${edition.id}`, body)
      else await api.post('/rh/feuilles-temps', body)
      setEdition(null); charger()
    } catch (e) { setErr(e.response?.data?.message || t('temps.erreur')) }
  }
  async function action(id, verbe, params) {
    setErr(null)
    try { await api.post(`/rh/feuilles-temps/${id}/${verbe}`, null, params ? { params } : undefined); setEdition(null); charger() }
    catch (e) { setErr(e.response?.data?.message || t('temps.erreur')) }
  }
  const badge = (s) => s === 'APPROUVEE' ? 's-APPROUVE' : s === 'REFUSEE' ? 's-REFUSE' : ''

  if (edition) {
    const modifiable = !edition.statut || edition.statut === 'BROUILLON' || edition.statut === 'REFUSEE'
    return (
      <div className="panel">
        <button className="secondary" onClick={() => setEdition(null)}>← {t('temps.retour')}</button>
        <h1 style={{ marginTop: 8 }}>{edition.id ? t('temps.modifier') : t('temps.nouvelle')}</h1>
        <div className="cmp-row">
          <label>{t('temps.debut')}<input type="date" value={edition.periodeDebut} disabled={!modifiable} onChange={(e) => setEdition({ ...edition, periodeDebut: e.target.value })} /></label>
          <label>{t('temps.fin')}<input type="date" value={edition.periodeFin} disabled={!modifiable} onChange={(e) => setEdition({ ...edition, periodeFin: e.target.value })} /></label>
        </div>
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr><th scope="col">{t('temps.colDate')}</th><th scope="col">{t('temps.colCategorie')}</th><th scope="col">{t('temps.colHeures')}</th><th scope="col"><span className="sr-only">x</span></th></tr></thead>
          <tbody>
            {edition.lignes.map((l, i) => (
              <tr key={i}>
                <td><input type="date" value={l.date} disabled={!modifiable} onChange={(e) => majLigne(i, 'date', e.target.value)} /></td>
                <td>
                  <select value={l.categorie} disabled={!modifiable} onChange={(e) => majLigne(i, 'categorie', e.target.value)} aria-label={t('temps.colCategorie')}>
                    {CATEGORIES.map((c) => <option key={c} value={c}>{t(`temps.cat.${c}`)}</option>)}
                  </select>
                </td>
                <td><input type="number" min="0" step="0.25" value={l.heures} disabled={!modifiable} style={{ width: 90 }} onChange={(e) => majLigne(i, 'heures', e.target.value)} /></td>
                <td>{modifiable && <button className="danger" onClick={() => setEdition({ ...edition, lignes: edition.lignes.filter((_, j) => j !== i) })}>×</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <p><strong>{t('temps.total')} : {total(edition.lignes)} h</strong></p>
        {err && <div className="erreur" role="alert">{err}</div>}
        {modifiable && (
          <div className="cmp-row">
            <button className="secondary" onClick={() => setEdition({ ...edition, lignes: [...edition.lignes, ligneVide(edition.periodeDebut)] })}>+ {t('temps.ajouterLigne')}</button>
            <button onClick={enregistrer}>{t('temps.enregistrer')}</button>
            {edition.id && <button onClick={() => action(edition.id, 'soumettre')}>{t('temps.soumettre')}</button>}
          </div>
        )}
      </div>
    )
  }

  return (
    <div>
      <div className="panel">
        <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>{t('temps.titre')}</h1>
          <button onClick={nouvelle}>+ {t('temps.nouvelle')}</button>
        </div>
        <p className="muted">{t('temps.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
        <table className="table">
          <thead><tr><th scope="col">{t('temps.colPeriode')}</th><th scope="col">{t('temps.colTotal')}</th><th scope="col">{t('temps.colStatut')}</th><th scope="col"><span className="sr-only">x</span></th></tr></thead>
          <tbody>
            {mine.map((f) => (
              <tr key={f.id}>
                <td>{f.periodeDebut || '—'} → {f.periodeFin || '—'}</td>
                <td>{f.totalHeures} h</td>
                <td><span className={`badge ${badge(f.statut)}`}>{t(`temps.statut.${f.statut}`)}</span></td>
                <td><button className="secondary" onClick={() => editer(f)}>{t('temps.ouvrir')}</button></td>
              </tr>
            ))}
            {mine.length === 0 && <tr><td colSpan="4" className="muted">{t('temps.aucune')}</td></tr>}
          </tbody>
        </table>
      </div>

      {gestionnaire && (
        <section className="panel">
          <h2>{t('temps.aApprouver')} <span className="badge">{aApprouver.length}</span></h2>
          <table className="table">
            <thead><tr><th scope="col">{t('temps.colProprietaire')}</th><th scope="col">{t('temps.colPeriode')}</th><th scope="col">{t('temps.colTotal')}</th><th scope="col"><span className="sr-only">x</span></th></tr></thead>
            <tbody>
              {aApprouver.map((f) => (
                <tr key={f.id}>
                  <td>{f.proprietaire}</td><td>{f.periodeDebut} → {f.periodeFin}</td><td>{f.totalHeures} h</td>
                  <td className="cmp-row">
                    <button onClick={() => action(f.id, 'approuver')}>{t('temps.approuver')}</button>
                    <button className="danger" onClick={() => action(f.id, 'refuser', { motif: t('temps.motifRefus') })}>{t('temps.refuser')}</button>
                  </td>
                </tr>
              ))}
              {aApprouver.length === 0 && <tr><td colSpan="4" className="muted">{t('temps.rienAApprouver')}</td></tr>}
            </tbody>
          </table>
        </section>
      )}
    </div>
  )
}
