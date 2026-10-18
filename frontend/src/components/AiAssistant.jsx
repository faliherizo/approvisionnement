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

  async function generer(e) {
    e.preventDefault()
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
      <form onSubmit={generer} className="form">
        <label>{t('aiassistant.instruction')}
          <input value={instruction} required placeholder={t('aiassistant.instructionPlaceholder')}
                 onChange={(e) => setInstruction(e.target.value)} />
        </label>
        <label>{t('aiassistant.contexte')}
          <input value={contexte} onChange={(e) => setContexte(e.target.value)} />
        </label>
        <button type="submit" disabled={chargement}>
          {chargement ? t('aiassistant.generation') : t('aiassistant.proposerTexte')}
        </button>
      </form>

      {erreur && <div className="erreur">{erreur}</div>}

      {proposition && (
        <div className="ai-result">
          <textarea rows="8" value={proposition}
                    onChange={(e) => setProposition(e.target.value)} />
          {onInsertion && (
            <button className="secondary" onClick={() => onInsertion(proposition)}>
              {t('aiassistant.insererFormulaire')}
            </button>
          )}
        </div>
      )}
    </div>
  )
}
