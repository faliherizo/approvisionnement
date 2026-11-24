import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = { code: '', formulaireCode: '', correspondances: '' }

/** « ANCIEN_NOM=cle » par ligne → table de correspondance ; les lignes vides sont ignorées. */
function lireTable(texte) {
  const table = {}
  texte.split('\n').map((l) => l.trim()).filter(Boolean).forEach((l) => {
    const i = l.indexOf('=')
    if (i > 0) table[l.slice(0, i).trim()] = l.slice(i + 1).trim()
    else table[l] = ''
  })
  return table
}

/**
 * Échanges hérités (CHU-8.04, ADR-021) : un système de la plateforme précédente garde ses noms de champs ; l'historique
 * exporté se reprend après un essai qui n'écrit rien.
 */
export default function EchangesHerites() {
  const { t } = useI18n()
  const [echanges, setEchanges] = useState([])
  const [formulaires, setFormulaires] = useState([])
  const [form, setForm] = useState(VIDE)
  const [reprise, setReprise] = useState({ id: '', export: '' })
  const [bilan, setBilan] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [message, setMessage] = useState(null)

  const charger = useCallback(() => {
    api.get('/interop/echanges').then((r) => setEchanges(r.data || [])).catch(() => setEchanges([]))
  }, [])
  useEffect(() => {
    charger()
    api.get('/formulaires').then((r) => setFormulaires(r.data || [])).catch(() => setFormulaires([]))
  }, [charger])

  async function declarer(e) {
    e.preventDefault(); setErreur(null); setMessage(null)
    try {
      await api.post('/interop/echanges', { ...form, correspondances: lireTable(form.correspondances) })
      setMessage(t('echanges.declare', { code: form.code })); setForm(VIDE); charger()
    } catch (err) { setErreur(err.response?.data?.message || t('echanges.erreur')) }
  }

  async function desactiver(ech) {
    setErreur(null); setMessage(null)
    try { await api.post(`/interop/echanges/${ech.id}/desactivation`); setMessage(t('echanges.desactive', { code: ech.code })); charger() }
    catch (err) { setErreur(err.response?.data?.message || t('echanges.erreur')) }
  }

  async function reprendre(essai) {
    setErreur(null); setMessage(null); setBilan(null)
    let enregistrements
    try { enregistrements = JSON.parse(reprise.export) } catch { setErreur(t('echanges.exportIllisible')); return }
    if (!Array.isArray(enregistrements)) { setErreur(t('echanges.exportIllisible')); return }
    try { setBilan((await api.post(`/interop/echanges/${reprise.id}/reprise`, enregistrements, { params: { essai } })).data); charger() }
    catch (err) { setErreur(err.response?.data?.message || t('echanges.erreur')) }
  }

  const actifs = echanges.filter((x) => x.actif)

  return (
    <section className="panel" aria-labelledby="echanges-titre">
      <h2 id="echanges-titre">{t('echanges.titre')}</h2>
      <p className="muted">{t('echanges.intro')}</p>
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
      {message && <p role="status">{message}</p>}

      <div className="table-scroll">
        <table className="table">
          <caption className="sr-only">{t('echanges.titre')}</caption>
          <thead><tr>
            <th scope="col">{t('echanges.code')}</th><th scope="col">{t('echanges.formulaire')}</th>
            <th scope="col">{t('echanges.correspondances')}</th><th scope="col">{t('echanges.adresse')}</th>
            <th scope="col">{t('echanges.etat')}</th><th scope="col"><span className="sr-only">{t('echanges.actions')}</span></th>
          </tr></thead>
          <tbody>
            {echanges.map((x) => (
              <tr key={x.id}>
                <td><code>{x.code}</code></td>
                <td>{x.formulaireNom} <span className="muted">({x.formulaireCode})</span></td>
                <td><dl className="dl-compact">{Object.entries(x.correspondances).map(([a, c]) => (
                  <div key={a}><dt>{a}</dt><dd>{c}</dd></div>
                ))}</dl></td>
                <td><code>/api/public/v1/echanges/{x.code}/fiches</code></td>
                <td><span className={`badge ${x.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{x.actif ? t('echanges.actif') : t('echanges.inactif')}</span></td>
                <td>{x.actif && (
                  <button type="button" className="secondary" onClick={() => desactiver(x)}
                          aria-label={t('echanges.desactiverCode', { code: x.code })}>{t('echanges.desactiver')}</button>
                )}</td>
              </tr>
            ))}
            {echanges.length === 0 && <tr><td colSpan="6" className="muted">{t('echanges.aucun')}</td></tr>}
          </tbody>
        </table>
      </div>

      <h3>{t('echanges.declarer')}</h3>
      <form onSubmit={declarer} className="form">
        <label>{t('echanges.code')}
          <input required pattern="[a-z0-9][a-z0-9\-]{1,59}" value={form.code} aria-describedby="echanges-code-aide"
                 onChange={(e) => setForm({ ...form, code: e.target.value })} />
        </label>
        <p id="echanges-code-aide" className="muted">{t('echanges.codeAide')}</p>
        <label>{t('echanges.formulaire')}
          <select required value={form.formulaireCode} onChange={(e) => setForm({ ...form, formulaireCode: e.target.value })}>
            <option value="">{t('echanges.choisir')}</option>
            {formulaires.map((f) => <option key={f.id} value={f.code}>{f.nom} ({f.code})</option>)}
          </select>
        </label>
        <label>{t('echanges.correspondances')}
          <textarea required rows={4} value={form.correspondances} aria-describedby="echanges-table-aide"
                    onChange={(e) => setForm({ ...form, correspondances: e.target.value })} />
        </label>
        <p id="echanges-table-aide" className="muted">{t('echanges.correspondancesAide')}</p>
        <button type="submit">{t('echanges.declarerBouton')}</button>
      </form>

      <h3>{t('echanges.reprise')}</h3>
      <p className="muted">{t('echanges.repriseAide')}</p>
      <form onSubmit={(e) => { e.preventDefault(); reprendre(true) }} className="form">
        <label>{t('echanges.echange')}
          <select required value={reprise.id} onChange={(e) => setReprise({ ...reprise, id: e.target.value })}>
            <option value="">{t('echanges.choisir')}</option>
            {actifs.map((x) => <option key={x.id} value={x.id}>{x.code}</option>)}
          </select>
        </label>
        <label>{t('echanges.export')}
          <textarea required rows={6} value={reprise.export} placeholder='[{"NOM_EMPLOYE": "…"}]'
                    onChange={(e) => setReprise({ ...reprise, export: e.target.value })} />
        </label>
        <div className="cmp-row">
          <button type="submit">{t('echanges.essayer')}</button>
          <button type="button" className="secondary" disabled={!bilan?.essai || bilan.acceptes === 0}
                  onClick={() => reprendre(false)}>{t('echanges.reprendre')}</button>
        </div>
      </form>

      {bilan && (
        <div aria-live="polite">
          <p>{t(bilan.essai ? 'echanges.bilanEssai' : 'echanges.bilanReprise', { acceptes: bilan.acceptes, rejetes: bilan.rejetes, total: bilan.total })}</p>
          <div className="table-scroll">
            <table className="table">
              <caption className="sr-only">{t('echanges.bilan')}</caption>
              <thead><tr>
                <th scope="col">{t('echanges.rang')}</th><th scope="col">{t('echanges.statut')}</th>
                <th scope="col">{t('echanges.numero')}</th><th scope="col">{t('echanges.motif')}</th>
              </tr></thead>
              <tbody>
                {bilan.lignes.map((l) => (
                  <tr key={l.rang}>
                    <td>{l.rang}</td>
                    <td><span className={`badge ${l.statut === 'REJETEE' ? 's-REFUSE' : 's-APPROUVE'}`}>{t(`echanges.statut.${l.statut}`)}</span></td>
                    <td>{l.numero || '—'}</td>
                    <td>{l.motif || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  )
}
