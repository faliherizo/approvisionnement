import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

function dollars(n) {
  return (n ?? 0).toLocaleString('fr-CA', { style: 'currency', currency: 'CAD' })
}

/**
 * Aperçu de paie (RH) : calcule les retenues Revenu Québec (RRQ/RRQ2, AE, RQAP, impôt féd.+QC) via le
 * moteur pur porté de Nexu VINA. Démontre la réutilisation RH (voir docs 07, section C).
 */
export default function Paie() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [frequences, setFrequences] = useState([])
  const [form, setForm] = useState({ grossSalary: 2000, frequency: 'biweekly', sansRetenues: false })
  const [bulletin, setBulletin] = useState(null)
  const [err, setErr] = useState(null)

  useEffect(() => { api.get('/paie/frequences').then((r) => setFrequences(r.data || [])).catch(() => setFrequences([])) }, [])

  if (chargement) return <div className="panel">{t('paie.chargement')}</div>
  if (!actif('paie')) return <div className="panel">{t('paie.moduleInactif')}</div>

  async function calculer(e) {
    e.preventDefault(); setErr(null)
    try {
      const r = await api.post('/paie/apercu', { ...form, grossSalary: Number(form.grossSalary) })
      setBulletin(r.data)
    } catch (e2) { setErr(e2.response?.data?.message || t('paie.erreur')) }
  }

  const lignesEmploye = [
    ['paie.federalTax', 'federalTax'], ['paie.quebecTax', 'quebecTax'], ['paie.qpp', 'qpp'],
    ['paie.qpp2', 'qpp2'], ['paie.ei', 'ei'], ['paie.qpip', 'qpip'],
  ]
  const lignesEmployeur = [
    ['paie.employerQpp', 'employerQpp'], ['paie.employerEi', 'employerEi'], ['paie.employerQpip', 'employerQpip'],
    ['paie.employerCnesst', 'employerCnesst'], ['paie.employerFss', 'employerFss'],
  ]

  return (
    <div>
      <div className="panel">
        <h1>{t('paie.titre')}</h1>
        <p className="muted">{t('paie.intro')}</p>
        <form onSubmit={calculer} className="form">
          <div className="cmp-row">
            <label>{t('paie.brut')}
              <input type="number" min="0" step="0.01" value={form.grossSalary}
                     onChange={(e) => setForm({ ...form, grossSalary: e.target.value })} />
            </label>
            <label>{t('paie.frequence')}
              <select value={form.frequency} onChange={(e) => setForm({ ...form, frequency: e.target.value })}>
                {frequences.map((f) => <option key={f.value} value={f.value}>{t(`paie.freq.${f.value}`)}</option>)}
              </select>
            </label>
            <label className="check-row">
              <input type="checkbox" checked={form.sansRetenues} onChange={(e) => setForm({ ...form, sansRetenues: e.target.checked })} /> {t('paie.sansRetenues')}
            </label>
            <button type="submit">{t('paie.calculer')}</button>
          </div>
          {err && <div className="erreur" role="alert">{err}</div>}
        </form>
      </div>

      {bulletin && (
        <div className="grid2">
          <section className="panel">
            <h2>{t('paie.retenuesEmploye')}</h2>
            <table className="table">
              <tbody>
                {lignesEmploye.map(([cle, champ]) => (
                  <tr key={champ}><td>{t(cle)}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin[champ])}</td></tr>
                ))}
                <tr><td><strong>{t('paie.totalRetenues')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(bulletin.totalEmployeeDeductions)}</strong></td></tr>
                <tr><td><strong>{t('paie.net')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(bulletin.netPay)}</strong></td></tr>
              </tbody>
            </table>
          </section>
          <section className="panel">
            <h2>{t('paie.chargesEmployeur')}</h2>
            <table className="table">
              <tbody>
                {lignesEmployeur.map(([cle, champ]) => (
                  <tr key={champ}><td>{t(cle)}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin[champ])}</td></tr>
                ))}
                <tr><td><strong>{t('paie.coutTotal')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(bulletin.totalEmployerCost)}</strong></td></tr>
              </tbody>
            </table>
          </section>
        </div>
      )}
    </div>
  )
}
