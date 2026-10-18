import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext.jsx'

export default function Assistant() {
  const { t } = useI18n()
  const TYPES = useReference('TYPE_CONTRAT')
  const navigate = useNavigate()

  const TYPES_ACQUISITION = [
    { code: 'BIENS', libelle: t('assistant.acqBiens') },
    { code: 'SERVICES', libelle: t('assistant.acqServices') },
    { code: 'SERVICES_PROFESSIONNELS', libelle: t('assistant.acqServicesPro') },
    { code: 'TRAVAUX_CONSTRUCTION', libelle: t('assistant.acqTravaux') },
    { code: 'TECHNOLOGIES', libelle: t('assistant.acqTechno') },
    { code: 'AUTRE', libelle: t('assistant.acqAutre') },
  ]
  const [ctx, setCtx] = useState({
    type: 'APPEL_OFFRES_PUBLIC', categorieAcquisition: 'SERVICES_PROFESSIONNELS',
    valeurEstimee: '', contexte: '',
  })
  const [suggestion, setSuggestion] = useState(null)
  const [dossiers, setDossiers] = useState([])
  const [gen, setGen] = useState({ titre: '', templateId: '', dossierId: '' })

  useEffect(() => { api.get('/dossiers').then((r) => setDossiers(r.data)) }, [])

  async function analyser(e) {
    e.preventDefault()
    const { data } = await api.post('/assistant/analyser', {
      ...ctx, valeurEstimee: ctx.valeurEstimee || null,
    })
    setSuggestion(data)
  }

  async function generer(e) {
    e.preventDefault()
    const payload = {
      titre: gen.titre,
      templateId: gen.templateId || null,
      type: ctx.type,
      modeSollicitation: suggestion?.modeSuggere || null,
      valeurEstimee: ctx.valeurEstimee || null,
      contexte: ctx.contexte,
      dossierId: gen.dossierId || null,
      clauseIds: gen.templateId ? null : (suggestion?.clausesEligibles || []).map((c) => c.id),
    }
    const { data } = await api.post('/assistant/generer', payload)
    navigate(`/documents/${data.id}`)
  }

  return (
    <div className="grid2">
      <section className="panel" aria-labelledby="titre-assistant">
        <h1 id="titre-assistant">{t('assistant.title')}</h1>
        <p className="muted">{t('assistant.intro')}</p>
        <form onSubmit={analyser} className="form">
          <label>{t('assistant.typeDoc')}
            <select value={ctx.type} onChange={(e) => setCtx({ ...ctx, type: e.target.value })}>
              {TYPES.map((o) => <option key={o.code} value={o.code}>{o.libelle}</option>)}
            </select>
          </label>
          <label>{t('assistant.typeAcq')}
            <select value={ctx.categorieAcquisition}
                    onChange={(e) => setCtx({ ...ctx, categorieAcquisition: e.target.value })}>
              {TYPES_ACQUISITION.map((o) => <option key={o.code} value={o.code}>{o.libelle}</option>)}
            </select>
          </label>
          <label>{t('assistant.valeurEstimee')}
            <input type="number" value={ctx.valeurEstimee}
                   onChange={(e) => setCtx({ ...ctx, valeurEstimee: e.target.value })} />
          </label>
          <label>{t('assistant.contexte')}
            <textarea value={ctx.contexte} rows={3}
                      onChange={(e) => setCtx({ ...ctx, contexte: e.target.value })} />
          </label>
          <button type="submit">{t('assistant.analyser')}</button>
        </form>
      </section>

      <section className="panel" aria-labelledby="titre-reco">
        <h2 id="titre-reco">{t('assistant.recoTitle')}</h2>
        {!suggestion && <p className="muted">{t('assistant.recoEmpty')}</p>}
        {suggestion && (
          <>
            <p>{t('assistant.modeSuggere')}{' '}
              <strong>{suggestion.modeSuggere || t('assistant.nonDetermine')}</strong></p>

            <h3>{t('assistant.modelesReco', { n: suggestion.templatesRecommandes.length })}</h3>
            <ul className="reco-list">
              {suggestion.templatesRecommandes.map((tpl) => (
                <li key={tpl.id}>
                  {tpl.nom} <span className="muted">{t('assistant.tplMeta', { code: tpl.code, n: tpl.nombreClauses })}</span>
                  {tpl.correspondanceExacte && <span className="badge s-APPROUVE"> {t('assistant.correspExacte')}</span>}
                </li>
              ))}
              {suggestion.templatesRecommandes.length === 0 && <li className="muted">{t('assistant.aucunModele')}</li>}
            </ul>

            <h3>{t('assistant.clausesEligibles', { n: suggestion.clausesEligibles.length })}</h3>
            <ul className="reco-list">
              {suggestion.clausesEligibles.map((c) => (
                <li key={c.id}>{c.code} — {c.titre} <span className="muted">({c.categorie})</span></li>
              ))}
            </ul>

            <div className="ai-card" style={{ borderStyle: 'solid' }}>
              <h3>{t('assistant.genererDoc')}</h3>
              <form onSubmit={generer} className="form">
                <label>{t('assistant.titreDoc')}
                  <input value={gen.titre} required
                         onChange={(e) => setGen({ ...gen, titre: e.target.value })} />
                </label>
                <label>{t('assistant.modeleOpt')}
                  <select value={gen.templateId} onChange={(e) => setGen({ ...gen, templateId: e.target.value })}>
                    <option value="">{t('assistant.selManuelle')}</option>
                    {suggestion.templatesRecommandes.map((tpl) => (
                      <option key={tpl.id} value={tpl.id}>{tpl.nom} ({tpl.code})</option>
                    ))}
                  </select>
                </label>
                <label>{t('assistant.rattacherDossier')}
                  <select value={gen.dossierId} onChange={(e) => setGen({ ...gen, dossierId: e.target.value })}>
                    <option value="">{t('assistant.aucunDossier')}</option>
                    {dossiers.map((d) => <option key={d.id} value={d.id}>{d.titre}</option>)}
                  </select>
                </label>
                <button type="submit">{t('assistant.genererDoc')}</button>
              </form>
            </div>
          </>
        )}
      </section>
    </div>
  )
}
