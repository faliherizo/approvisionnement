import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/**
 * Assistant de rédaction propulsé par Ollama (modèle local).
 * onInsertion(texte) : rappel pour insérer la proposition dans un formulaire.
 */
export default function AiAssistant({ contexteParDefaut = '', texteExistant = '', onInsertion }) {
  const { t } = useI18n()
  const [instruction, setInstruction] = useState('')
  const [contexte, setContexte] = useState(contexteParDefaut)
  const [proposition, setProposition] = useState('')
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

  async function generer() {
    if (!instruction.trim()) return
    setErreur(null)
    setChargement(true)
    setProposition('')
    try {
      const { data } = await api.post('/ia/rediger', {
        instruction, contexte, texteExistant
      })
      setProposition(data.proposition)
    } catch (err) {
      setErreur(err.response?.data?.erreur || t('aiassistant.serviceIndisponible'))
    } finally {
      setChargement(false)
    }
  }

  return (
    <div className="ai-card">
      <h2>🤖 {t('aiassistant.titre')}</h2>
      <p className="muted">{t('aiassistant.description')}</p>
      {/* Pas de <form> : l'assistant s'insère aussi DANS un formulaire (édition d'une clause), où un formulaire imbriqué
          soumettait le formulaire parent au lieu de générer. Entrée dans l'instruction lance la génération. */}
      <div className="form" role="group" aria-label={t('aiassistant.titre')}>
        <label>{t('aiassistant.instruction')}
          <input value={instruction} placeholder={t('aiassistant.instructionPlaceholder')}
                 onChange={(e) => setInstruction(e.target.value)}
                 onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); generer() } }} />
        </label>
        <label>{t('aiassistant.contexte')}
          <input value={contexte} onChange={(e) => setContexte(e.target.value)} />
        </label>
        <button type="button" onClick={generer} disabled={chargement || !instruction.trim()}>
          {chargement ? t('aiassistant.generation') : t('aiassistant.proposerTexte')}
        </button>
      </div>

      {erreur && <div className="erreur">{erreur}</div>}

      {proposition && (
        <div className="ai-result">
          <textarea rows="8" value={proposition}
                    onChange={(e) => setProposition(e.target.value)} />
          {onInsertion && (
            <button type="button" className="secondary" onClick={() => onInsertion(proposition)}>
              {t('aiassistant.insererFormulaire')}
            </button>
          )}
        </div>
      )}
    </div>
  )
}
