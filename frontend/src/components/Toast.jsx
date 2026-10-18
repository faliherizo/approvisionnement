import { createContext, useCallback, useContext, useMemo, useState } from 'react'

const ToastContext = createContext(null)

/**
 * Fournisseur global de notifications transitoires (toasts). Expose
 * useToast() → { succes, erreur, info } pour signaler le résultat d'une action.
 */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const retirer = useCallback((id) => setToasts((t) => t.filter((x) => x.id !== id)), [])

  const notifier = useCallback((message, type) => {
    const id = crypto.randomUUID()
    setToasts((t) => [...t, { id, message, type }])
    setTimeout(() => retirer(id), 3500)
  }, [retirer])

  const api = useMemo(() => ({
    succes: (m) => notifier(m, 'succes'),
    erreur: (m) => notifier(m, 'erreur'),
    info: (m) => notifier(m, 'info'),
  }), [notifier])

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toast-zone" role="status" aria-live="polite">
        {toasts.map((t) => (
          <button key={t.id} type="button" className={`toast toast-${t.type}`} onClick={() => retirer(t.id)}>
            {t.message}
          </button>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

const SANS_EFFET = { succes() {}, erreur() {}, info() {} }

/** Accès aux notifications ; sans fournisseur, no-op (ne casse pas les tests unitaires). */
export function useToast() {
  return useContext(ToastContext) ?? SANS_EFFET
}
