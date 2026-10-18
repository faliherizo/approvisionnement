import { useEffect, useRef } from 'react'
import RichText from './RichText.jsx'
import { useI18n } from '../i18n/I18nContext'

const SELECTEUR_FOCUS = 'a[href], button:not([disabled]), input:not([disabled]), '
  + 'select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"]), [contenteditable="true"]'

/**
 * Fenêtre modale accessible : rôle dialog, fermeture par Échap / clic sur l'arrière-plan,
 * focus initial dans la modale, piège de focus (Tab) et restauration du focus à la fermeture.
 */
export default function Modal({ titre, children, onClose, taille = 'md' }) {
  const { t } = useI18n()
  const ref = useRef(null)
  const precedent = useRef(null)
  // onClose peut être une fonction recréée à chaque rendu (arrow inline) : on la lit via une
  // ref pour que l'effet de focus reste monté UNE seule fois. Sinon il se relance à chaque
  // frappe et vole le focus des champs (saisie impossible dans la modale).
  const onCloseRef = useRef(onClose)
  onCloseRef.current = onClose

  useEffect(() => {
    precedent.current = document.activeElement
    document.body.style.overflow = 'hidden'
    const focusables = () => ref.current?.querySelectorAll(SELECTEUR_FOCUS)
    ;(focusables()?.[0] || ref.current)?.focus()

    const onKey = (e) => {
      if (e.key === 'Escape') { onCloseRef.current?.(); return }
      if (e.key !== 'Tab') return
      const items = focusables()
      if (!items || items.length === 0) return
      const premier = items[0]
      const dernier = items[items.length - 1]
      if (e.shiftKey && document.activeElement === premier) { e.preventDefault(); dernier.focus() }
      else if (!e.shiftKey && document.activeElement === dernier) { e.preventDefault(); premier.focus() }
    }
    document.addEventListener('keydown', onKey)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = ''
      precedent.current?.focus?.()
    }
  }, [])

  return (
    <div className="modal-overlay" onMouseDown={onClose}>
      <div ref={ref} className={`modal modal-${taille}`} role="dialog" aria-modal="true"
           aria-label={titre} tabIndex={-1} onMouseDown={(e) => e.stopPropagation()}>
        <header className="modal-head">
          <h3>{titre}</h3>
          <button className="modal-x" aria-label={t('modal.fermer')} onClick={onClose}>×</button>
        </header>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  )
}

/** Rendu d'un champ de saisie selon son type (texte, select, textarea, richtext WYSIWYG). */
function champ(c, valeurs, setValeurs) {
  const set = (valeur) => setValeurs({ ...valeurs, [c.name]: valeur })
  if (c.type === 'select') {
    return (
      <select value={valeurs[c.name] || (c.options?.[0]?.code ?? '')}
              onChange={(e) => set(e.target.value)} required={c.required}>
        {(c.options || []).map((o) => <option key={o.code} value={o.code}>{o.libelle}</option>)}
      </select>
    )
  }
  if (c.type === 'richtext') {
    return <RichText value={valeurs[c.name] || ''} onChange={set}
                     rows={c.rows || 4} placeholder={c.placeholder} />
  }
  if (c.type === 'textarea') {
    return <textarea rows={c.rows || 3} value={valeurs[c.name] || ''}
                     onChange={(e) => set(e.target.value)} required={c.required} />
  }
  return <input type={c.type || 'text'} value={valeurs[c.name] || ''} placeholder={c.placeholder}
                onChange={(e) => set(e.target.value)} required={c.required} />
}

/** Modale de saisie simple (remplace window.prompt). */
export function PromptModal({ titre, label, champs, onValider, onClose, valeurs, setValeurs }) {
  const { t } = useI18n()
  return (
    <Modal titre={titre} onClose={onClose}>
      <form className="form" onSubmit={(e) => { e.preventDefault(); onValider() }}>
        {champs.map((c) => (
          <label key={c.name}>{c.label}
            {champ(c, valeurs, setValeurs)}
          </label>
        ))}
        <div className="modal-actions">
          <button type="button" className="secondary" onClick={onClose}>{t('modal.annuler')}</button>
          <button type="submit">{t('modal.valider')}</button>
        </div>
      </form>
    </Modal>
  )
}

/** Modale de confirmation (remplace window.confirm). */
export function ConfirmModal({ titre, message, onConfirm, onClose, danger = true }) {
  const { t } = useI18n()
  return (
    <Modal titre={titre} onClose={onClose} taille="sm">
      <p>{message}</p>
      <div className="modal-actions">
        <button className="secondary" onClick={onClose}>{t('modal.annuler')}</button>
        <button className={danger ? 'danger' : ''} onClick={() => { onConfirm(); onClose() }}>{t('modal.confirmer')}</button>
      </div>
    </Modal>
  )
}
