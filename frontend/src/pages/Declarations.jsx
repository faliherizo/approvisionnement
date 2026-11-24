import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

function dollars(n) { return (n ?? 0).toLocaleString('fr-CA', { style: 'currency', currency: 'CAD' }) }

/** Déclarations Revenu Québec / ARC : FPZ-500 (TPS/TVQ), retenues à la source, Relevé 1 + T4. */
export default function Declarations() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [onglet, setOnglet] = useState('taxes')
  const [periode, setPeriode] = useState({ start: '2026-01-01', end: '2026-12-31' })
  const [taxesIn, setTaxesIn] = useState({ gstCollected: 0, qstCollected: 0 })
  const [depenses, setDepenses] = useState([{ date: '2026-03-01', category: 'office', amountHT: 100 }])
  const [bulletins, setBulletins] = useState([{ employeeId: 'e1', payDate: '2026-06-15', grossSalary: 2000, frequency: 'biweekly' }])
  const [annee, setAnnee] = useState({ employeeId: 'e1', annee: 2026 })
  const [res, setRes] = useState(null)
  const [err, setErr] = useState(null)

  if (chargement) return <div className="panel">{t('decl.chargement')}</div>
  if (!actif('paie')) return <div className="panel">{t('decl.moduleInactif')}</div>

  const num = (v) => Number(v) || 0
  async function appeler(url, body) {
    setErr(null); setRes(null)
    try { const r = await api.post(url, body); setRes({ type: onglet, data: r.data }) }
    catch (e) { setErr(e.response?.data?.message || t('decl.erreur')) }
  }
  const soumettreTaxes = (e) => { e.preventDefault(); appeler('/paie/declarations/taxes', { ...periode, gstCollected: num(taxesIn.gstCollected), qstCollected: num(taxesIn.qstCollected), depenses: depenses.map((d) => ({ ...d, amountHT: num(d.amountHT) })) }) }
  const soumettreRetenues = (e) => { e.preventDefault(); appeler('/paie/declarations/retenues', { ...periode, bulletins: bulletins.map((b) => ({ ...b, grossSalary: num(b.grossSalary) })) }) }
  const soumettreAnnee = (e) => { e.preventDefault(); appeler('/paie/declarations/fin-annee', { employeeId: annee.employeeId, annee: num(annee.annee), bulletins: bulletins.map((b) => ({ ...b, grossSalary: num(b.grossSalary) })) }) }

  const majDepense = (i, k, v) => setDepenses(depenses.map((d, j) => j === i ? { ...d, [k]: v } : d))
  const majBulletin = (i, k, v) => setBulletins(bulletins.map((b, j) => j === i ? { ...b, [k]: v } : b))

  const Onglets = () => (
    <div className="cmp-row" role="tablist">
      {['taxes', 'retenues', 'annee'].map((o) => (
        <button key={o} role="tab" aria-selected={onglet === o} className={onglet === o ? '' : 'secondary'}
                onClick={() => { setOnglet(o); setRes(null); setErr(null) }}>{t(`decl.onglet.${o}`)}</button>
      ))}
    </div>
  )

  return (
    <div>
      <div className="panel">
        <h1>{t('decl.titre')}</h1>
        <p className="muted">{t('decl.intro')}</p>
        <Onglets />
      </div>

      {onglet === 'taxes' && (
        <section className="panel">
          <h2>{t('decl.fpz500')}</h2>
          <form onSubmit={soumettreTaxes} className="form">
            <div className="cmp-row">
              <label>{t('decl.debut')}<input type="date" value={periode.start} onChange={(e) => setPeriode({ ...periode, start: e.target.value })} /></label>
              <label>{t('decl.fin')}<input type="date" value={periode.end} onChange={(e) => setPeriode({ ...periode, end: e.target.value })} /></label>
              <label>{t('decl.tpsPercue')}<input type="number" value={taxesIn.gstCollected} onChange={(e) => setTaxesIn({ ...taxesIn, gstCollected: e.target.value })} /></label>
              <label>{t('decl.tvqPercue')}<input type="number" value={taxesIn.qstCollected} onChange={(e) => setTaxesIn({ ...taxesIn, qstCollected: e.target.value })} /></label>
            </div>
            <strong>{t('decl.depenses')}</strong>
            {depenses.map((d, i) => (
              <div className="cmp-row" key={i}>
                <input type="date" value={d.date} onChange={(e) => majDepense(i, 'date', e.target.value)} />
                <input value={d.category} onChange={(e) => majDepense(i, 'category', e.target.value)} placeholder={t('decl.categorie')} />
                <input type="number" value={d.amountHT} onChange={(e) => majDepense(i, 'amountHT', e.target.value)} placeholder="HT" />
                <button type="button" className="danger" onClick={() => setDepenses(depenses.filter((_, j) => j !== i))} aria-label={t('decl.retirerLigne', { n: i + 1 })}>×</button>
              </div>
            ))}
            <button type="button" className="secondary" onClick={() => setDepenses([...depenses, { date: periode.start, category: 'office', amountHT: 0 }])}>+ {t('decl.ajouterDepense')}</button>
            <button type="submit">{t('decl.calculer')}</button>
          </form>
          {res?.type === 'taxes' && <p><strong>{t('decl.tpsNette')} :</strong> {dollars(res.data.gstNet)} · <strong>{t('decl.tvqNette')} :</strong> {dollars(res.data.qstNet)}</p>}
        </section>
      )}

      {(onglet === 'retenues' || onglet === 'annee') && (
        <section className="panel">
          <h2>{onglet === 'retenues' ? t('decl.retenues') : t('decl.releveT4')}</h2>
          <form onSubmit={onglet === 'retenues' ? soumettreRetenues : soumettreAnnee} className="form">
            {onglet === 'retenues' ? (
              <div className="cmp-row">
                <label>{t('decl.debut')}<input type="date" value={periode.start} onChange={(e) => setPeriode({ ...periode, start: e.target.value })} /></label>
                <label>{t('decl.fin')}<input type="date" value={periode.end} onChange={(e) => setPeriode({ ...periode, end: e.target.value })} /></label>
              </div>
            ) : (
              <div className="cmp-row">
                <label>{t('decl.employe')}<input value={annee.employeeId} onChange={(e) => setAnnee({ ...annee, employeeId: e.target.value })} /></label>
                <label>{t('decl.annee')}<input type="number" value={annee.annee} onChange={(e) => setAnnee({ ...annee, annee: e.target.value })} /></label>
              </div>
            )}
            <strong>{t('decl.bulletins')}</strong>
            {bulletins.map((b, i) => (
              <div className="cmp-row" key={i}>
                <input value={b.employeeId} onChange={(e) => majBulletin(i, 'employeeId', e.target.value)} placeholder={t('decl.employe')} />
                <input type="date" value={b.payDate} onChange={(e) => majBulletin(i, 'payDate', e.target.value)} />
                <input type="number" value={b.grossSalary} onChange={(e) => majBulletin(i, 'grossSalary', e.target.value)} placeholder={t('decl.brut')} />
                <select value={b.frequency} onChange={(e) => majBulletin(i, 'frequency', e.target.value)}>
                  {['weekly', 'biweekly', 'semimonthly', 'monthly'].map((f) => <option key={f} value={f}>{f}</option>)}
                </select>
                <button type="button" className="danger" onClick={() => setBulletins(bulletins.filter((_, j) => j !== i))} aria-label={t('decl.retirerLigne', { n: i + 1 })}>×</button>
              </div>
            ))}
            <button type="button" className="secondary" onClick={() => setBulletins([...bulletins, { employeeId: 'e1', payDate: periode.start, grossSalary: 2000, frequency: 'biweekly' }])}>+ {t('decl.ajouterBulletin')}</button>
            <button type="submit">{t('decl.calculer')}</button>
          </form>
          {err && <div className="erreur" role="alert">{err}</div>}
          {res?.type === 'retenues' && (
            <p><strong>{t('decl.versRQ')} :</strong> {dollars(res.data.totalToRevenuQuebec)} · <strong>{t('decl.versARC')} :</strong> {dollars(res.data.totalToCRA)} ({res.data.payRunCount} {t('decl.paies')})</p>
          )}
          {res?.type === 'annee' && (
            <div className="grid2">
              <div><h3>Relevé 1</h3><p>Case A : {dollars(res.data.rl1?.caseA_employmentIncome)}<br />Case B (RRQ) : {dollars(res.data.rl1?.caseB_qpp)}<br />Case E (impôt QC) : {dollars(res.data.rl1?.caseE_quebecTax)}</p></div>
              <div><h3>T4</h3><p>Case 14 : {dollars(res.data.t4?.box14_employmentIncome)}<br />Case 16 (RRQ) : {dollars(res.data.t4?.box16_qpp)}<br />Case 22 (impôt féd) : {dollars(res.data.t4?.box22_federalTax)}</p></div>
            </div>
          )}
        </section>
      )}
      {err && onglet === 'taxes' && <div className="panel erreur" role="alert">{err}</div>}
    </div>
  )
}
