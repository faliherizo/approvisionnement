import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const STATUTS = ['SOUMIS', 'EN_TRAITEMENT', 'A_COMPLETER', 'VALIDE', 'REJETE', 'CLOTURE']
const COLONNES_MAX = 8

/** Rapport PDF personnalisé (CHU 6.03) : questions montrées (8 au plus), statuts retenus et titre au choix. */
export default function RapportPdfFiches({ formulaireId }) {
  const { t } = useI18n()
  const [questions, setQuestions] = useState([])
  const [choix, setChoix] = useState({ titre: '', champs: [], statuts: [] })
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    api.get(`/formulaires/${formulaireId}/saisies/structure`).then((r) => {
      const s = r.data?.structureJson ? JSON.parse(r.data.structureJson) : { pages: [] }
      setQuestions((s.pages || []).flatMap((p) => p.champs || []).map((c) => ({ cle: c.cle ?? c.code, libelle: c.libelle ?? c.cle ?? c.code })))
    }).catch(() => setQuestions([]))
  }, [formulaireId])

  const basculer = (liste, valeur) => (choix[liste].includes(valeur) ? choix[liste].filter((x) => x !== valeur) : [...choix[liste], valeur])

  async function produire(e) {
    e.preventDefault(); setErreur(null)
    try {
      const params = new URLSearchParams()
      if (choix.titre) params.set('titre', choix.titre)
      if (choix.champs.length) params.set('champs', choix.champs.join(','))
      if (choix.statuts.length) params.set('statuts', choix.statuts.join(','))
      const r = await api.get(`/rapport/formulaires/${formulaireId}/rapport.pdf?${params}`, { responseType: 'blob' })
      const url = URL.createObjectURL(r.data)
      Object.assign(document.createElement('a'), { href: url, download: `rapport-${formulaireId}.pdf` }).click()
      URL.revokeObjectURL(url)
    } catch { setErreur(t('rapportPdf.erreur')) }
  }

  return (
    <section className="panel" aria-labelledby="rapport-pdf-titre">
      <h2 id="rapport-pdf-titre">{t('rapportPdf.titre')}</h2>
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
      <form className="form" onSubmit={produire}>
        <label>{t('rapportPdf.intitule')}<input maxLength={120} value={choix.titre} onChange={(e) => setChoix({ ...choix, titre: e.target.value })} /></label>
        <fieldset>
          <legend>{t('rapportPdf.questions', { n: COLONNES_MAX })}</legend>
          {questions.map((q) => (
            <label key={q.cle} className="checkbox-inline">
              <input type="checkbox" checked={choix.champs.includes(q.cle)}
                     disabled={!choix.champs.includes(q.cle) && choix.champs.length >= COLONNES_MAX}
                     onChange={() => setChoix({ ...choix, champs: basculer('champs', q.cle) })} /> {q.libelle}
            </label>
          ))}
        </fieldset>
        <fieldset>
          <legend>{t('rapportPdf.statuts')}</legend>
          {STATUTS.map((s) => (
            <label key={s} className="checkbox-inline">
              <input type="checkbox" checked={choix.statuts.includes(s)} onChange={() => setChoix({ ...choix, statuts: basculer('statuts', s) })} /> {t(`statutSaisie.${s}`)}
            </label>
          ))}
        </fieldset>
        <button type="submit">{t('rapportPdf.produire')}</button>
      </form>
    </section>
  )
}
