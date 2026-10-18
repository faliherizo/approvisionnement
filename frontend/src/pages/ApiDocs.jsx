import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Documentation & test des API (Swagger UI) — réservé aux administrateurs. */
export default function ApiDocs() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  if (!hasRole('ADMIN')) {
    return <div className="panel">{t('apidocs.accesReserve')}</div>
  }
  return (
    <div className="panel" style={{ padding: 0, overflow: 'hidden' }}>
      <div style={{ padding: '12px 20px', borderBottom: '1px solid var(--bord)' }}>
        <h1 style={{ margin: 0 }}>{t('apidocs.titre')}</h1>
        <p className="muted" style={{ margin: '4px 0 0' }}>
          {t('apidocs.description')} <a href="/v3/api-docs" target="_blank" rel="noreferrer">/v3/api-docs</a>
        </p>
      </div>
      <iframe title="Swagger UI" src="/swagger-ui/index.html"
              style={{ width: '100%', height: '78vh', border: 0 }} />
    </div>
  )
}
