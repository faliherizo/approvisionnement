import { useAuth } from '../auth.jsx'
import TemplateManager from '../components/TemplateManager.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Gestion des templates de documents (admin/rédacteur). */
export default function Templates() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  if (!hasRole('ADMIN') && !hasRole('REDACTEUR')) {
    return <div className="panel">{t('templates.accesReserve')}</div>
  }
  return (
    <div>
      <section className="panel">
        <h1>{t('templates.titre')}</h1>
        <p className="muted">{t('templates.description')}</p>
      </section>
      <TemplateManager />
    </div>
  )
}
