import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

const CANAUX = ['COURRIEL', 'SMS', 'INTERNE']
const DECLENCHEURS = ['SOUMISSION', 'VALIDATION', 'REJET']
function formatDate(iso) { return iso ? new Date(iso).toLocaleString('fr-CA') : '—' }

/** Configuration des notifications : gabarits, règles (déclencheur → gabarit → canal) et journal. */
export default function NotificationsAdmin() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [modeles, setModeles] = useState([])
  const [regles, setRegles] = useState([])
  const [journal, setJournal] = useState([])
  const [mForm, setMForm] = useState({ code: '', canal: 'COURRIEL', sujet: '', corps: '' })
  const [rForm, setRForm] = useState({ formulaireId: '', declencheur: 'SOUMISSION', modeleCode: '', destinataires: '' })
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/notification/modeles').then((r) => setModeles(r.data || [])).catch(() => setModeles([]))
    api.get('/notification/regles').then((r) => setRegles(r.data || [])).catch(() => setRegles([]))
    api.get('/notification/journal').then((r) => setJournal(r.data || [])).catch(() => setJournal([]))
  }, [])
  useEffect(() => { if (!chargement && actif('notification')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('notif.chargement')}</div>
  if (!actif('notification')) return <div className="panel">{t('notif.moduleInactif')}</div>

  async function creerModele(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/notification/modeles', mForm); setMForm({ code: '', canal: 'COURRIEL', sujet: '', corps: '' }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('notif.erreur')) }
  }
  async function creerRegle(e) {
    e.preventDefault(); setErr(null)
    try {
      await api.post('/notification/regles', { ...rForm, formulaireId: Number(rForm.formulaireId) })
      setRForm({ formulaireId: '', declencheur: 'SOUMISSION', modeleCode: '', destinataires: '' }); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('notif.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('notif.titre')}</h1>
        <p className="muted">{t('notif.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      <section className="panel">
        <h2>{t('notif.gabarits')}</h2>
        <table className="table">
          <thead><tr><th scope="col">{t('notif.colCode')}</th><th scope="col">{t('notif.colCanal')}</th><th scope="col">{t('notif.colSujet')}</th></tr></thead>
          <tbody>
            {modeles.map((m) => <tr key={m.id}><td><code>{m.code}</code></td><td><span className="badge">{m.canal}</span></td><td>{m.sujet || '—'}</td></tr>)}
            {modeles.length === 0 && <tr><td colSpan="3" className="muted">{t('notif.aucunGabarit')}</td></tr>}
          </tbody>
        </table>
        <form onSubmit={creerModele} className="form" style={{ marginTop: 12 }}>
          <div className="cmp-row">
            <input placeholder={t('notif.phCode')} value={mForm.code} required onChange={(e) => setMForm({ ...mForm, code: e.target.value })} />
            <select value={mForm.canal} onChange={(e) => setMForm({ ...mForm, canal: e.target.value })} aria-label={t('notif.colCanal')}>
              {CANAUX.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
            <input placeholder={t('notif.phSujet')} value={mForm.sujet} onChange={(e) => setMForm({ ...mForm, sujet: e.target.value })} />
          </div>
          <label>{t('notif.corps')}
            <textarea rows={3} value={mForm.corps} placeholder={t('notif.corpsAide')} onChange={(e) => setMForm({ ...mForm, corps: e.target.value })} />
          </label>
          <button type="submit">{t('notif.ajouterGabarit')}</button>
        </form>
      </section>

      <section className="panel">
        <h2>{t('notif.regles')}</h2>
        <table className="table">
          <thead><tr><th scope="col">{t('notif.colFormulaire')}</th><th scope="col">{t('notif.colDeclencheur')}</th><th scope="col">{t('notif.colGabarit')}</th><th scope="col">{t('notif.colDestinataires')}</th></tr></thead>
          <tbody>
            {regles.map((r) => <tr key={r.id}><td>{r.formulaireId}</td><td><span className="badge">{t(`declencheur.${r.declencheur}`) }</span></td><td><code>{r.modeleCode}</code></td><td>{r.destinataires || '—'}</td></tr>)}
            {regles.length === 0 && <tr><td colSpan="4" className="muted">{t('notif.aucuneRegle')}</td></tr>}
          </tbody>
        </table>
        <form onSubmit={creerRegle} className="form" style={{ marginTop: 12 }}>
          <div className="cmp-row">
            <input type="number" placeholder={t('notif.phFormulaireId')} value={rForm.formulaireId} required style={{ width: 150 }} onChange={(e) => setRForm({ ...rForm, formulaireId: e.target.value })} />
            <select value={rForm.declencheur} onChange={(e) => setRForm({ ...rForm, declencheur: e.target.value })} aria-label={t('notif.colDeclencheur')}>
              {DECLENCHEURS.map((d) => <option key={d} value={d}>{t(`declencheur.${d}`)}</option>)}
            </select>
            <select value={rForm.modeleCode} required onChange={(e) => setRForm({ ...rForm, modeleCode: e.target.value })} aria-label={t('notif.colGabarit')}>
              <option value="">{t('notif.choisirGabarit')}</option>
              {modeles.map((m) => <option key={m.id} value={m.code}>{m.code} ({m.canal})</option>)}
            </select>
            <input placeholder={t('notif.phDestinataires')} value={rForm.destinataires} onChange={(e) => setRForm({ ...rForm, destinataires: e.target.value })} />
            <button type="submit">{t('notif.ajouterRegle')}</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>{t('notif.journal')}</h2>
        <table className="table">
          <thead><tr><th scope="col">{t('notif.colCanal')}</th><th scope="col">{t('notif.colDestinataire')}</th><th scope="col">{t('notif.colSujet')}</th><th scope="col">{t('notif.colStatut')}</th><th scope="col">{t('notif.colDate')}</th></tr></thead>
          <tbody>
            {journal.map((e) => <tr key={e.id}><td>{e.canal}</td><td>{e.destinataire}</td><td>{e.sujet || '—'}</td><td><span className="badge">{e.statut}</span></td><td>{formatDate(e.envoyeLe)}</td></tr>)}
            {journal.length === 0 && <tr><td colSpan="5" className="muted">{t('notif.aucunEnvoi')}</td></tr>}
          </tbody>
        </table>
      </section>
    </div>
  )
}
