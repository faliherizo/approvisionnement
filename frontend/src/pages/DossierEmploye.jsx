import { useEffect, useState, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

const PERIODES = { weekly: 52, biweekly: 26, semimonthly: 24, monthly: 12 }
function dollars(n) { return (n ?? 0).toLocaleString('fr-CA', { style: 'currency', currency: 'CAD' }) }

/** Dossier d'un employé (RH) : onglets Informations · Paie (bulletin calculé) · Historique. */
export default function DossierEmploye() {
  const { id } = useParams()
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [emp, setEmp] = useState(null)
  const [onglet, setOnglet] = useState('infos')
  const [bulletin, setBulletin] = useState(null)
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get(`/rh/employes/${id}`).then((r) => setEmp(r.data)).catch(() => setErr(t('dossier.erreur')))
  }, [id, t])
  useEffect(() => { if (!chargement && actif('rh')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('dossier.chargement')}</div>
  if (!actif('rh')) return <div className="panel">{t('dossier.moduleInactif')}</div>
  if (!emp) return <div className="panel">{err ? <span className="erreur">{err}</span> : t('dossier.chargement')}</div>

  async function calculerPaie() {
    setErr(null)
    try {
      const periods = PERIODES[emp.frequence] || 26
      const brutPeriode = emp.salaireAnnuel / periods
      const r = await api.post('/paie/apercu', { grossSalary: brutPeriode, frequency: emp.frequence, sansRetenues: emp.typeTravailleur === 'self_employed' })
      setBulletin(r.data)
    } catch (e) { setErr(e.response?.data?.message || t('dossier.erreurPaie')) }
  }

  const onglets = [['infos', t('dossier.onglet.infos')], ['paie', t('dossier.onglet.paie')], ['historique', t('dossier.onglet.historique')]]
  const ligne = (cle, val) => <tr><td>{t(cle)}</td><td>{val || '—'}</td></tr>

  return (
    <div>
      <div className="panel">
        <Link className="btn-lien" to="/rh/personnel">← {t('dossier.retour')}</Link>
        <h1 style={{ marginTop: 8 }}>{emp.prenom} {emp.nom}</h1>
        <p className="muted"><code>{emp.matricule}</code> · <span className={`badge ${emp.statut === 'ACTIF' ? 's-APPROUVE' : 's-REFUSE'}`}>{t(`personnel.statut.${emp.statut}`)}</span></p>
        <div className="cmp-row" role="tablist">
          {onglets.map(([o, lab]) => (
            <button key={o} role="tab" aria-selected={onglet === o} className={onglet === o ? '' : 'secondary'}
                    onClick={() => setOnglet(o)}>{lab}</button>
          ))}
        </div>
      </div>

      {onglet === 'infos' && (
        <section className="panel">
          <h2>{t('dossier.onglet.infos')}</h2>
          <table className="table"><tbody>
            {ligne('personnel.courriel', emp.courriel)}
            {ligne('personnel.telephone', emp.telephone)}
            {ligne('personnel.nas', emp.nasMasque)}
            {ligne('personnel.dateEmbauche', emp.dateEmbauche)}
            {ligne('personnel.poste', emp.poste)}
            {ligne('personnel.departement', emp.departement)}
            {ligne('personnel.salaire', dollars(emp.salaireAnnuel))}
            {ligne('personnel.frequence', emp.frequence)}
            {ligne('personnel.type', emp.typeTravailleur)}
            {ligne('dossier.province', emp.province)}
          </tbody></table>
          <Link className="btn-lien" to="/rh/personnel">{t('dossier.modifierDansPersonnel')}</Link>
        </section>
      )}

      {onglet === 'paie' && (
        <section className="panel">
          <h2>{t('dossier.onglet.paie')}</h2>
          {!actif('paie') ? <p className="muted">{t('dossier.paieInactive')}</p> : (
            <>
              <p className="muted">{t('dossier.paieIntro', { salaire: dollars(emp.salaireAnnuel), frequence: emp.frequence })}</p>
              <button onClick={calculerPaie}>{t('dossier.calculerBulletin')}</button>
              {err && <div className="erreur" role="alert">{err}</div>}
              {bulletin && (
                <table className="table" style={{ marginTop: 12 }}><tbody>
                  <tr><td>{t('paie.brut')}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin.gross)}</td></tr>
                  <tr><td>{t('paie.federalTax')}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin.federalTax)}</td></tr>
                  <tr><td>{t('paie.quebecTax')}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin.quebecTax)}</td></tr>
                  <tr><td>{t('paie.qpp')} / {t('paie.ei')} / {t('paie.qpip')}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin.qpp + bulletin.qpp2)} / {dollars(bulletin.ei)} / {dollars(bulletin.qpip)}</td></tr>
                  <tr><td><strong>{t('paie.net')}</strong></td><td style={{ textAlign: 'right' }}><strong>{dollars(bulletin.netPay)}</strong></td></tr>
                  <tr><td>{t('paie.coutTotal')}</td><td style={{ textAlign: 'right' }}>{dollars(bulletin.totalEmployerCost)}</td></tr>
                </tbody></table>
              )}
            </>
          )}
        </section>
      )}

      {onglet === 'historique' && (
        <section className="panel">
          <h2>{t('dossier.onglet.historique')}</h2>
          <p className="muted">{t('dossier.historiqueVide')}</p>
        </section>
      )}
    </div>
  )
}
