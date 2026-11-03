import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

function dollars(n) { return (n ?? 0).toLocaleString('fr-CA', { style: 'currency', currency: 'CAD' }) }
const PROFILS = ['incorporated', 'self_employed', 'employee']

/** Impôt sur le revenu (RH/comptabilité) : moteur ImpotSurLeRevenu porté (sociétés / autonome / salarié). */
export default function Impot() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [form, setForm] = useState({ profile: 'self_employed', revenue: 80000, salaries: 0, subcontracting: 0, recordedExpensesDeductible: 0 })
  const [res, setRes] = useState(null)
  const [err, setErr] = useState(null)

  if (chargement) return <div className="panel">{t('impot.chargement')}</div>
  if (!actif('paie')) return <div className="panel">{t('impot.moduleInactif')}</div>

  async function calculer(e) {
    e.preventDefault(); setErr(null)
    try {
      const body = {
        profile: form.profile,
        revenue: Number(form.revenue),
        salaries: Number(form.salaries) || 0,
        subcontracting: Number(form.subcontracting) || 0,
        recordedExpensesDeductible: Number(form.recordedExpensesDeductible) || 0,
      }
      const r = await api.post('/paie/impot/apercu', body)
      setRes(r.data)
    } catch (e2) { setErr(e2.response?.data?.message || t('impot.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('impot.titre')}</h1>
        <p className="muted">{t('impot.intro')}</p>
        <form onSubmit={calculer} className="form">
          <div className="cmp-row">
            <label>{t('impot.profil')}
              <select value={form.profile} onChange={(e) => setForm({ ...form, profile: e.target.value })}>
                {PROFILS.map((p) => <option key={p} value={p}>{t(`impot.profil.${p}`)}</option>)}
              </select>
            </label>
            <label>{t('impot.revenu')}<input type="number" min="0" value={form.revenue} onChange={(e) => setForm({ ...form, revenue: e.target.value })} /></label>
          </div>
          <div className="cmp-row">
            <label>{t('impot.salaires')}<input type="number" min="0" value={form.salaries} onChange={(e) => setForm({ ...form, salaries: e.target.value })} /></label>
            <label>{t('impot.sousTraitance')}<input type="number" min="0" value={form.subcontracting} onChange={(e) => setForm({ ...form, subcontracting: e.target.value })} /></label>
            <label>{t('impot.autresCharges')}<input type="number" min="0" value={form.recordedExpensesDeductible} onChange={(e) => setForm({ ...form, recordedExpensesDeductible: e.target.value })} /></label>
          </div>
          {err && <div className="erreur" role="alert">{err}</div>}
          <button type="submit">{t('impot.calculer')}</button>
        </form>
      </div>

      {res && (
        <section className="panel">
          <h2>{t('impot.resultat')}</h2>
          <table className="table">
            <tbody>
              <tr><td>{t('impot.revenuTotal')}</td><td style={{ textAlign: 'right' }}>{dollars(res.totalRevenue)}</td></tr>
              <tr><td>{t('impot.chargesTotal')}</td><td style={{ textAlign: 'right' }}>{dollars(res.charges?.total)}</td></tr>
              <tr><td>{t('impot.imposable')}</td><td style={{ textAlign: 'right' }}>{dollars(res.taxableIncome)}</td></tr>
              <tr><td>{t('impot.federal')}</td><td style={{ textAlign: 'right' }}>{dollars(res.federalTax)}</td></tr>
              <tr><td>{t('impot.quebec')}</td><td style={{ textAlign: 'right' }}>{dollars(res.quebecTax)}</td></tr>
              <tr><td><strong>{t('impot.totalImpot')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(res.totalTax)}</strong></td></tr>
              <tr><td><strong>{t('impot.net')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(res.netIncome)}</strong></td></tr>
            </tbody>
          </table>
        </section>
      )}
    </div>
  )
}
