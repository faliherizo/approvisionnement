import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const DEMANDE_VIDE = { motif: '', dureeHeures: 2 }
const date = (iso) => (iso ? new Date(iso).toLocaleString('fr-CA', { dateStyle: 'medium', timeStyle: 'short' }) : '—')

/**
 * Accès de soutien de l'exploitant aux données CLM d'une organisation (Annexe 10, SEC-17) : le superadministrateur n'y a
 * aucun droit ; il demande un accès motivé et limité, que l'organisation accorde, puis consulte un diagnostic sans contenu.
 */
export default function SoutienClmManager({ tenants }) {
  const { t } = useI18n()
  const [tenant, setTenant] = useState('')
  const [demandes, setDemandes] = useState([])
  const [demande, setDemande] = useState(DEMANDE_VIDE)
  const [reference, setReference] = useState('')
  const [diagnostic, setDiagnostic] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  const base = (tid) => `/superadmin/tenants/${tid}/soutien-clm`
  const recharger = async (tid) => setDemandes((await api.get(`${base(tid)}/demandes`)).data)
  async function choisir(tid) {
    setTenant(tid); setDiagnostic(null); setErreur(null); setInfo('')
    if (tid) await recharger(tid)
  }
  async function demander(e) {
    e.preventDefault(); setErreur(null)
    try {
      await api.post(`${base(tenant)}/demandes`, { motif: demande.motif, dureeHeures: Number(demande.dureeHeures) })
      setDemande(DEMANDE_VIDE); setInfo(t('soutien.demandee')); await recharger(tenant)
    } catch (x) { setErreur(x.response?.data?.message || t('soutien.erreur')) }
  }
  async function consulter(e) {
    e.preventDefault(); setErreur(null); setDiagnostic(null)
    try {
      setDiagnostic((await api.get(`${base(tenant)}/trousses/${encodeURIComponent(reference.trim())}`)).data); await recharger(tenant)
    } catch (x) { setErreur(x.response?.data?.message || t('soutien.erreur')) }
  }

  return (
    <section className="panel" aria-labelledby="soutien-clm-sa">
      <h2 id="soutien-clm-sa">{t('soutien.titre')}</h2>
      <p className="muted">{t('soutien.aide')}</p>
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
      <div role="status" aria-live="polite">{info}</div>
      <label>{t('soutien.organisation')}
        <select value={tenant} onChange={(e) => choisir(e.target.value)}>
          <option value="">—</option>
          {tenants.map((x) => <option key={x.id} value={x.id}>{x.titre}</option>)}
        </select>
      </label>
      {tenant && (
        <>
          <form className="form" aria-label={t('soutien.demander')} onSubmit={demander}>
            <label>{t('soutien.motif')}<input required maxLength={500} value={demande.motif} onChange={(e) => setDemande({ ...demande, motif: e.target.value })} /></label>
            <label>{t('soutien.duree')}<input type="number" min={1} max={8} required value={demande.dureeHeures}
              onChange={(e) => setDemande({ ...demande, dureeHeures: e.target.value })} /></label>
            <button type="submit">{t('soutien.demander')}</button>
          </form>
          <div className="table-scroll">
            <table className="table">
              <caption>{t('soutien.mesDemandes')}</caption>
              <thead><tr><th scope="col">{t('soutien.motif')}</th><th scope="col">{t('soutien.statut')}</th><th scope="col">{t('soutien.expire')}</th>
                <th scope="col">{t('soutien.consultations')}</th></tr></thead>
              <tbody>
                {demandes.map((d) => (
                  <tr key={d.id}><td>{d.motif}</td><td>{t(`soutien.statut.${d.statut}`)}</td><td>{date(d.expireLe)}</td><td>{d.consultations}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
          <form className="form" aria-label={t('soutien.diagnostic')} onSubmit={consulter}>
            <label>{t('soutien.reference')}<input required value={reference} onChange={(e) => setReference(e.target.value)} /></label>
            <button type="submit">{t('soutien.consulter')}</button>
          </form>
          {diagnostic && (
            <div className="table-scroll">
              <table className="table">
                <caption>{t('soutien.diagnosticDe', { ref: diagnostic.reference, statut: diagnostic.statut })}
                  {' · '}{diagnostic.journalIntegre ? t('soutien.journalIntegre') : t('soutien.journalRompu')}</caption>
                <thead><tr><th scope="col">{t('soutien.etape')}</th><th scope="col">{t('soutien.statut')}</th><th scope="col">{t('soutien.activee')}</th></tr></thead>
                <tbody>
                  {diagnostic.etapes.map((e) => <tr key={e.cle}><td>{e.libelle}</td><td>{e.statut}</td><td>{date(e.activeeLe)}</td></tr>)}
                </tbody>
              </table>
              <p>{t('soutien.evenements', { n: diagnostic.evenements.length })} {diagnostic.evenements.map((e) => e.type).join(', ')}</p>
            </div>
          )}
        </>
      )}
    </section>
  )
}
