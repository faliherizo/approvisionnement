import { createContext, useContext, useState, useMemo, useCallback } from 'react'
import { translations, LOCALES } from './translations'

const DEFAUT = 'fr-CA'
const I18nContext = createContext(null)

export function I18nProvider({ children }) {
  const [locale, setLocaleState] = useState(() => localStorage.getItem('locale') || DEFAUT)

  const setLocale = useCallback((l) => {
    localStorage.setItem('locale', l)
    document.documentElement.lang = l
    setLocaleState(l)
  }, [])

  // t('clé') ou t('clé', { n: 3 }) : « {n} » dans le texte est remplacé par la valeur fournie.
  const t = useCallback((cle, params) => {
    const dict = translations[locale] || translations[DEFAUT]
    let texte = dict[cle] ?? translations[DEFAUT][cle] ?? cle
    if (params) {
      for (const [k, v] of Object.entries(params)) texte = texte.split(`{${k}}`).join(String(v))
    }
    return texte
  }, [locale])

  const valeur = useMemo(() => ({ locale, setLocale, t, locales: LOCALES }), [locale, setLocale, t])
  return <I18nContext.Provider value={valeur}>{children}</I18nContext.Provider>
}

export const useI18n = () => useContext(I18nContext)
