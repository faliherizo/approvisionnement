import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'
import { useToast } from '../components/Toast.jsx'

const EXEMPLE_CSV = 'code,titre,categorie,corps\nGEN-100,Objet du contrat,STANDARD,Le présent contrat a pour objet...'

/** Import/migration, réversibilité et encadrement de l'IA (art. 20, 21, 22). Réservé aux administrateurs. */
export default function DonneesIA() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const toast = useToast()
  const [csv, setCsv] = useState(EXEMPLE_CSV)
  const [rapport, setRapport] = useState(null)
  const [fichierWord, setFichierWord] = useState(null)
  const [nomModele, setNomModele] = useState('')
  const [rapportWord, setRapportWord] = useState(null)
  const [iaActivee, setIaActivee] = useState(true)
  const [suggestions, setSuggestions] = useState([])

  const chargerIa = useCallback(() => {
    api.get('/ia/parametres').then((r) => setIaActivee(r.data.activee)).catch(() => {})
    api.get('/ia/suggestions').then((r) => setSuggestions(r.data)).catch(() => setSuggestions([]))
  }, [])
  useEffect(() => { chargerIa() }, [chargerIa])

  if (!hasRole('ADMIN')) {
    return <div className="panel">{t('donneesia.accesReserve')}</div>
  }

  async function importer(essai) {
    try {
      const { data } = await api.post(`/import/clauses?essai=${essai}`, csv, {
        headers: { 'Content-Type': 'text/csv' },
      })
      setRapport(data)
      toast.succes(essai ? t('donneesia.toastEssaiTermine') : t('donneesia.toastImportTermine', { n: data.importes }))
    } catch { toast.erreur(t('donneesia.toastImportRefuse')) }
  }

  async function importerWord(essai) {
    if (!fichierWord) { toast.erreur(t('donneesia.toastChoisirFichier')); return }
    const form = new FormData()
    form.append('fichier', fichierWord)
    const params = new URLSearchParams({ essai: String(essai), nom: nomModele || fichierWord.name.replace(/\.docx$/i, '') })
    try {
      const { data } = await api.post(`/import/word?${params.toString()}`, form)
      setRapportWord(data)
      toast.succes(essai ? t('donneesia.toastWordAnalyse')
        : t('donneesia.toastWordImporte', { n: data.clausesCreees }))
    } catch { toast.erreur(t('donneesia.toastWordRefuse')) }
  }

  async function exporterReversibilite() {
    const { data } = await api.get('/reversibilite/export', { responseType: 'blob' })
    const url = URL.createObjectURL(new Blob([data], { type: 'application/zip' }))
    const a = document.createElement('a')
    a.href = url
    a.download = 'export-tenant.zip'
    a.click()
    URL.revokeObjectURL(url)
  }

  async function basculerIa() {
    const { data } = await api.put('/ia/parametres', { activee: !iaActivee })
    setIaActivee(data.activee)
    toast.succes(data.activee ? t('donneesia.toastIaActive') : t('donneesia.toastIaDesactive'))
    chargerIa()
  }

  return (
    <div className="grid2">
      <section className="panel" aria-labelledby="titre-import">
        <h1 id="titre-import">{t('donneesia.importTitre')}</h1>
        <p className="muted">{t('donneesia.importIntro')} (<code>code,titre,categorie,corps</code>).
          {' '}{t('donneesia.importEssaiNote')}</p>
        <label>{t('donneesia.labelCsv')}
          <textarea value={csv} rows={6} onChange={(e) => setCsv(e.target.value)}
                    aria-describedby="aide-csv" />
        </label>
        <p id="aide-csv" className="muted">{t('donneesia.aideCsv')}</p>
        <div className="row-actions">
          <button type="button" className="secondary" onClick={() => importer(true)}>{t('donneesia.migrationEssai')}</button>
          <button type="button" onClick={() => importer(false)}>{t('donneesia.importer')}</button>
        </div>
        {rapport && (
          <div className="ai-card" style={{ borderStyle: 'solid', marginTop: '1rem' }} aria-live="polite">
            <p>{rapport.essai ? t('donneesia.essai') : t('donneesia.import')} — {t('donneesia.rapportCsv', { lues: rapport.lignesLues, valides: rapport.valides, importes: rapport.importes })}</p>
            {rapport.erreurs.length > 0 && (
              <ul className="reco-list">
                {rapport.erreurs.map((e) => (
                  <li key={e.ligne}>{t('donneesia.ligneErreur', { ligne: e.ligne, message: e.message })}</li>
                ))}
              </ul>
            )}
          </div>
        )}

        <h2 style={{ marginTop: '1.5rem' }}>{t('donneesia.wordTitre')}</h2>
        <p className="muted">{t('donneesia.wordIntro')}</p>
        <label>{t('donneesia.labelNomModele')}
          <input value={nomModele} onChange={(e) => setNomModele(e.target.value)}
                 placeholder={t('donneesia.placeholderNomModele')} />
        </label>
        <label>{t('donneesia.labelFichierWord')}
          <input type="file" accept=".docx" onChange={(e) => setFichierWord(e.target.files?.[0] || null)} />
        </label>
        <div className="row-actions">
          <button type="button" className="secondary" onClick={() => importerWord(true)}>{t('donneesia.analyserEssai')}</button>
          <button type="button" onClick={() => importerWord(false)}>{t('donneesia.importerModele')}</button>
        </div>
        {rapportWord && (
          <div className="ai-card" style={{ borderStyle: 'solid', marginTop: '1rem' }} aria-live="polite">
            <p>{rapportWord.essai ? t('donneesia.essai') : t('donneesia.import')} — {t('donneesia.rapportWord', { sections: rapportWord.sectionsDetectees, clauses: rapportWord.clausesCreees })}
              {rapportWord.templateNom ? t('donneesia.rapportWordModele', { nom: rapportWord.templateNom }) : ''}.</p>
            {rapportWord.erreurs.length > 0 && (
              <ul className="reco-list">
                {rapportWord.erreurs.map((e) => <li key={e.ligne}>{e.message}</li>)}
              </ul>
            )}
          </div>
        )}
      </section>

      <div>
        <section className="panel" aria-labelledby="titre-reversibilite">
          <h2 id="titre-reversibilite">{t('donneesia.reversibiliteTitre')}</h2>
          <p className="muted">{t('donneesia.reversibiliteIntro')}</p>
          <button type="button" onClick={exporterReversibilite}>{t('donneesia.exporterZip')}</button>
        </section>

        <section className="panel" aria-labelledby="titre-ia" style={{ marginTop: '1rem' }}>
          <h2 id="titre-ia">{t('donneesia.iaTitre')}</h2>
          <p className="muted">{t('donneesia.iaIntro')}</p>
          <p>{t('donneesia.etat')} <strong>{iaActivee ? t('donneesia.active') : t('donneesia.desactive')}</strong></p>
          <button type="button" className={iaActivee ? 'danger' : ''} onClick={basculerIa}>
            {iaActivee ? t('donneesia.desactiverIa') : t('donneesia.activerIa')}
          </button>

          <h3>{t('donneesia.suggestionsRecentes')}</h3>
          <table className="table">
            <caption className="sr-only">{t('donneesia.suggestionsCaption')}</caption>
            <thead>
              <tr><th scope="col">{t('donneesia.thDate')}</th><th scope="col">{t('donneesia.thType')}</th><th scope="col">{t('donneesia.thModele')}</th><th scope="col">{t('donneesia.thUtilisateur')}</th></tr>
            </thead>
            <tbody>
              {suggestions.map((s) => (
                <tr key={s.id}>
                  <td>{new Date(s.creeLe).toLocaleString('fr-CA')}</td>
                  <td>{s.typeInteraction}</td>
                  <td>{s.modele}</td>
                  <td>{s.utilisateur}</td>
                </tr>
              ))}
              {suggestions.length === 0 && <tr><td colSpan="4" className="muted">{t('donneesia.aucuneSuggestion')}</td></tr>}
            </tbody>
          </table>
        </section>
      </div>
    </div>
  )
}
