import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const DEVISES = ['CAD', 'USD', 'EUR']
const TPS = 0.05
const TVQ = 0.09975

const VIDE = {
  forfaitAnnuel: 0, devise: 'CAD', indexationPct: 0,
  nbUtilisateursInclus: '', notes: '', actif: true,
}

/**
 * Gestionnaire de la tarification d'abonnement par tenant (réservé au superadmin).
 * Un tarif par tenant : forfait annuel « tout inclus », devise, indexation des années
 * de renouvellement, utilisateurs inclus. Affiche une projection budgétaire (période
 * ferme de 3 ans + options indexées, taxes québécoises).
 */
export default function TarifTenantManager() {
  const { t } = useI18n()
  const [tarif, setTarif] = useState(VIDE)
  const [tenants, setTenants] = useState([])
  const [cible, setCible] = useState('')
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  useEffect(() => {
    api.get('/superadmin/tenants').then((r) => setTenants(r.data))
  }, [])

  useEffect(() => {
    setMsg(null); setErr(null)
    if (!cible) { setTarif(VIDE); return }
    api.get(`/superadmin/tenants/${cible}/tarif`).then((r) => setTarif({ ...VIDE, ...nettoyer(r.data) }))
  }, [cible])

  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    if (!cible) { setErr(t('tariftenantmanager.chooseTenantError')); return }
    try {
      await api.put(`/superadmin/tenants/${cible}/tarif`, {
        forfaitAnnuel: Number(tarif.forfaitAnnuel) || 0,
        devise: tarif.devise,
        indexationPct: Number(tarif.indexationPct) || 0,
        nbUtilisateursInclus: tarif.nbUtilisateursInclus === '' ? null : Number(tarif.nbUtilisateursInclus),
        notes: tarif.notes,
        actif: tarif.actif,
      })
      setMsg(t('tariftenantmanager.saved'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('tariftenantmanager.saveDenied'))
    }
  }

  const projection = calculerProjection(Number(tarif.forfaitAnnuel) || 0, Number(tarif.indexationPct) || 0)
  const argent = (n) => n.toLocaleString('fr-CA', { style: 'currency', currency: tarif.devise || 'CAD' })

  return (
    <section className="panel">
      <h2>💰 {t('tariftenantmanager.title')}</h2>
      <p className="muted">{t('tariftenantmanager.description')}</p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <label>{t('tariftenantmanager.tenant')}
          <select value={cible} required onChange={(e) => setCible(e.target.value)}>
            <option value="">{t('tariftenantmanager.chooseTenant')}</option>
            {tenants.map((tn) => <option key={tn.id} value={tn.id}>{tn.titre} ({tn.slug})</option>)}
          </select>
        </label>

        <div className="grid2">
          <label>{t('tariftenantmanager.annualFee')}
            <input type="number" min="0" step="100" value={tarif.forfaitAnnuel}
                   onChange={(e) => setTarif({ ...tarif, forfaitAnnuel: e.target.value })} />
          </label>
          <label>{t('tariftenantmanager.currency')}
            <select value={tarif.devise} onChange={(e) => setTarif({ ...tarif, devise: e.target.value })}>
              {DEVISES.map((d) => <option key={d} value={d}>{d}</option>)}
            </select>
          </label>
        </div>

        <div className="grid2">
          <label>{t('tariftenantmanager.indexation')}
            <input type="number" min="0" max="100" step="0.25" value={tarif.indexationPct}
                   onChange={(e) => setTarif({ ...tarif, indexationPct: e.target.value })} />
          </label>
          <label>{t('tariftenantmanager.usersIncluded')}
            <input type="number" min="0" step="1" value={tarif.nbUtilisateursInclus}
                   onChange={(e) => setTarif({ ...tarif, nbUtilisateursInclus: e.target.value })} />
          </label>
        </div>

        <label>{t('tariftenantmanager.notes')}
          <input value={tarif.notes} maxLength={500}
                 onChange={(e) => setTarif({ ...tarif, notes: e.target.value })} />
        </label>

        <label className="check-row">
          <input type="checkbox" checked={!!tarif.actif}
                 onChange={(e) => setTarif({ ...tarif, actif: e.target.checked })} /> {t('tariftenantmanager.active')}
        </label>

        <button type="submit" disabled={!cible}>{t('tariftenantmanager.savePricing')}</button>
      </form>

      {cible && (
        <table className="table" aria-label={t('tariftenantmanager.projectionAria')}>
          <thead>
            <tr><th scope="col">{t('tariftenantmanager.colItem')}</th><th scope="col">{t('tariftenantmanager.colAmount')}</th><th scope="col">{t('tariftenantmanager.colWithTax')}</th></tr>
          </thead>
          <tbody>
            <tr><td>{t('tariftenantmanager.firmPeriod')}</td><td>{argent(projection.ferme)}</td><td>{argent(projection.fermeTaxes)}</td></tr>
            <tr><td>{t('tariftenantmanager.optionalYears')}</td><td>{argent(projection.options)}</td><td>{argent(projection.optionsTaxes)}</td></tr>
            <tr><td><strong>{t('tariftenantmanager.total10')}</strong></td><td><strong>{argent(projection.total)}</strong></td><td><strong>{argent(projection.totalTaxes)}</strong></td></tr>
          </tbody>
        </table>
      )}
    </section>
  )
}

/** Projection : 3 ans fermes au forfait, puis 7 ans indexés composés ; taxes québécoises. */
function calculerProjection(forfait, indexPct) {
  const taux = 1 + indexPct / 100
  const ferme = forfait * 3
  let options = 0
  let annuel = forfait
  for (let i = 0; i < 7; i++) { annuel *= taux; options += annuel }
  const avecTaxes = (n) => n * (1 + TPS + TVQ)
  const total = ferme + options
  return {
    ferme, options, total,
    fermeTaxes: avecTaxes(ferme), optionsTaxes: avecTaxes(options), totalTaxes: avecTaxes(total),
  }
}
