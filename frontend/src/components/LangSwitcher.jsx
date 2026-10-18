import { useI18n } from '../i18n/I18nContext.jsx'

/** Sélecteur de langue (MG / EN / FR / FR-CA…). */
export default function LangSwitcher({ compact = false }) {
  const { locale, setLocale, locales, t } = useI18n()
  return (
    <label className={`lang-switcher ${compact ? 'compact' : ''}`}>
      {!compact && <span className="muted">{t('lang.label')}</span>}
      <select aria-label={t('lang.label')} value={locale} onChange={(e) => setLocale(e.target.value)}>
        {locales.map((l) => (
          <option key={l.code} value={l.code}>{l.drapeau} {l.label}</option>
        ))}
      </select>
    </label>
  )
}
