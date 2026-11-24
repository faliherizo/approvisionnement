import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/** Critères vides : toutes les fiches. */
export const CRITERES_VIDES = { statut: '', texte: '' }

/** Une fiche répond aux critères si son statut correspond et si le texte figure dans son numéro ou ses réponses. */
export function retenue(fiche, criteres) {
  if (criteres.statut && fiche.statut !== criteres.statut) return false
  const texte = (criteres.texte || '').trim().toLowerCase()
  if (!texte) return true
  return String(fiche.numero ?? fiche.id).toLowerCase().includes(texte) || (fiche.donneesJson || '').toLowerCase().includes(texte)
}

function lire(json) {
  try { return { ...CRITERES_VIDES, ...JSON.parse(json || '{}') } } catch { return CRITERES_VIDES }
}

/**
 * Recherche multicritère des fiches d'un formulaire, avec critères sauvegardés par utilisateur (CHU 6.01) : statut et
 * texte (numéro ou réponses). Les statuts proposés sont ceux des fiches présentes ; les critères se nomment, se
 * réappliquent et se suppriment.
 */
export default function RechercheFiches({ formulaireId, fiches, criteres, onChange }) {
  const { t } = useI18n()
  const [sauves, setSauves] = useState([])
  const [nom, setNom] = useState('')
  const [msg, setMsg] = useState(null)
  const statuts = [...new Set([...fiches.map((f) => f.statut), criteres.statut].filter(Boolean))].sort()

  const charger = useCallback(() => {
    api.get('/rapport/criteres')
      .then((r) => setSauves((r.data || []).filter((c) => String(c.formulaireId ?? '') === String(formulaireId))))
      .catch(() => setSauves([]))
  }, [formulaireId])
  useEffect(() => { charger() }, [charger])

  async function sauvegarder(e) {
    e.preventDefault()
    if (!nom.trim()) return
    await api.post('/rapport/criteres', JSON.stringify(criteres), {
      params: { formulaireId, nom: nom.trim() }, headers: { 'Content-Type': 'application/json' },
    })
    setMsg(t('rechfiches.sauvegarde', { nom: nom.trim() })); setNom(''); charger()
  }

  async function supprimer(c) {
    await api.delete(`/rapport/criteres/${c.id}`)
    charger()
  }

  return (
    <section className="panel" aria-labelledby="titre-recherche-fiches">
      <h2 id="titre-recherche-fiches">{t('rechfiches.titre')}</h2>
      <div className="form-grid">
        <label>{t('rechfiches.statut')}
          <select value={criteres.statut} onChange={(e) => onChange({ ...criteres, statut: e.target.value })}>
            <option value="">{t('rechfiches.tousStatuts')}</option>
            {statuts.map((s) => <option key={s} value={s}>{t(`statutSaisie.${s}`)}</option>)}
          </select>
        </label>
        <label>{t('rechfiches.texte')}
          <input value={criteres.texte} onChange={(e) => onChange({ ...criteres, texte: e.target.value })}
                 placeholder={t('rechfiches.textePlaceholder')} />
        </label>
      </div>
      <form className="cmp-row" onSubmit={sauvegarder}>
        <label>{t('rechfiches.nom')}
          <input value={nom} onChange={(e) => setNom(e.target.value)} />
        </label>
        <button type="submit" className="secondary" disabled={!nom.trim()}>{t('rechfiches.sauvegarder')}</button>
        <button type="button" className="secondary" onClick={() => onChange(CRITERES_VIDES)}>{t('rechfiches.effacer')}</button>
      </form>
      {msg && <p className="muted" role="status">{msg}</p>}
      {sauves.length > 0 && (
        <ul className="liste-criteres" aria-label={t('rechfiches.sauves')}>
          {sauves.map((c) => (
            <li key={c.id} className="cmp-row">
              <button type="button" className="secondary" onClick={() => onChange(lire(c.criteresJson))}>
                {t('rechfiches.appliquer', { nom: c.nom })}
              </button>
              <button type="button" className="danger" onClick={() => supprimer(c)}
                      aria-label={t('rechfiches.supprimerAria', { nom: c.nom })}>{t('rechfiches.supprimer')}</button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
