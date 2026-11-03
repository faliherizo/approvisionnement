import { useEffect, useState, useCallback, Fragment } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useAuth } from '../auth.jsx'
import { useModules } from '../hooks/useModules.js'
import DemandeAnnexes from '../components/DemandeAnnexes.jsx'

function parseDonnees(json) {
  if (!json) return {}
  try { return JSON.parse(json) } catch { return {} }
}
function formatDate(iso) { return iso ? new Date(iso).toLocaleString('fr-CA') : '—' }

/** Suivi des fiches (soumissions) d'un formulaire : liste, validation/rejet, résumé, export Excel. */
export default function Saisies() {
  const { id } = useParams()
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { actif, chargement } = useModules()
  const [fiches, setFiches] = useState([])
  const [resume, setResume] = useState(null)
  const [ouvert, setOuvert] = useState(null)
  const [err, setErr] = useState(null)

  const peutStatuer = hasRole('ADMIN') || hasRole('APPRO') || hasRole('DIRECTION') || hasRole('SUPERADMIN')

  const charger = useCallback(() => {
    api.get(`/formulaires/${id}/saisies`).then((r) => setFiches(r.data || [])).catch(() => setFiches([]))
  }, [id])
  useEffect(() => { if (!chargement && actif('saisie')) charger() }, [charger, chargement, actif])
  useEffect(() => {
    if (!chargement && actif('rapport')) {
      api.get(`/rapport/formulaires/${id}/resume`).then((r) => setResume(r.data || {})).catch(() => setResume(null))
    }
  }, [id, chargement, actif, fiches])

  if (chargement) return <div className="panel">{t('fiches.chargement')}</div>
  if (!actif('saisie')) return <div className="panel">{t('fiches.moduleInactif')}</div>

  async function statuer(sid, action) {
    setErr(null)
    try { await api.post(`/formulaires/${id}/saisies/${sid}/${action}`); charger() }
    catch (e) { setErr(e.response?.data?.message || t('fiches.erreurAction')) }
  }
  async function exporter() {
    try {
      const r = await api.get(`/rapport/formulaires/${id}/export.xlsx`, { responseType: 'blob' })
      const url = URL.createObjectURL(r.data)
      const a = document.createElement('a'); a.href = url; a.download = `fiches-${id}.xlsx`
      document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url)
    } catch { setErr(t('fiches.erreurExport')) }
  }

  const badge = (s) => s === 'VALIDE' ? 's-APPROUVE' : s === 'REJETE' ? 's-REFUSE' : ''

  return (
    <div>
      <div className="panel">
        <Link className="btn-lien" to="/formulaires">← {t('fiches.retour')}</Link>
        <h1 style={{ marginTop: 8 }}>{t('fiches.titre')}</h1>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      {resume && (
        <section className="panel">
          <h2>{t('fiches.resume')}</h2>
          <div className="cmp-row" style={{ flexWrap: 'wrap', gap: 12 }}>
            {Object.keys(resume).length === 0 && <span className="muted">{t('fiches.aucuneDonneeResume')}</span>}
            {Object.entries(resume).map(([statut, n]) => (
              <span key={statut} className={`badge ${badge(statut)}`}>{t(`statutSaisie.${statut}`)} : <strong>{n}</strong></span>
            ))}
          </div>
          {actif('rapport') && <button className="secondary" style={{ marginTop: 12 }} onClick={exporter}>⬇ {t('fiches.exportExcel')}</button>}
        </section>
      )}

      <section className="panel">
        <h2>{t('fiches.liste')}</h2>
        <table className="table">
          <thead><tr>
            <th scope="col">{t('fiches.colNumero')}</th>
            <th scope="col">{t('fiches.colStatut')}</th>
            <th scope="col">{t('fiches.colMaj')}</th>
            <th scope="col">{t('fiches.colSoumis')}</th>
            <th scope="col"><span className="sr-only">{t('fiches.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {fiches.map((f) => {
              const d = parseDonnees(f.donneesJson)
              return (
                <Fragment key={f.id}>
                  <tr>
                    <td><code>{f.numero || f.id}</code></td>
                    <td><span className={`badge ${badge(f.statut)}`}>{t(`statutSaisie.${f.statut}`)}</span></td>
                    <td>{formatDate(f.majLe)}</td>
                    <td>{formatDate(f.soumisLe)}</td>
                    <td className="cmp-row">
                      <button className="secondary" onClick={() => setOuvert(ouvert === f.id ? null : f.id)}
                              aria-expanded={ouvert === f.id}>{ouvert === f.id ? t('fiches.masquer') : t('fiches.details')}</button>
                      {peutStatuer && f.statut === 'SOUMIS' && <button onClick={() => statuer(f.id, 'valider')}>{t('fiches.valider')}</button>}
                      {peutStatuer && f.statut === 'SOUMIS' && <button className="danger" onClick={() => statuer(f.id, 'rejeter')}>{t('fiches.rejeter')}</button>}
                    </td>
                  </tr>
                  {ouvert === f.id && (
                    <tr>
                      <td colSpan="5">
                        {Object.keys(d).length === 0 ? <span className="muted">{t('fiches.aucuneReponse')}</span> : (
                          <dl className="fiche-details">
                            {Object.entries(d).map(([k, v]) => (
                              <div key={k} className="cmp-row"><dt><code>{k}</code></dt><dd>{String(v)}</dd></div>
                            ))}
                          </dl>
                        )}
                        {actif('document') && <div style={{ marginTop: 12 }}><DemandeAnnexes formulaireId={id} saisieId={f.id} /></div>}
                      </td>
                    </tr>
                  )}
                </Fragment>
              )
            })}
            {fiches.length === 0 && <tr><td colSpan="5" className="muted">{t('fiches.aucune')}</td></tr>}
          </tbody>
        </table>
      </section>
    </div>
  )
}
