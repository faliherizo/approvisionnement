import { useAuth } from '../auth.jsx'
import { useModules } from '../hooks/useModules.js'
import { useI18n } from '../i18n/I18nContext'
import ReferenceManager from '../components/ReferenceManager.jsx'
import ModeleDocumentManager from '../components/ModeleDocumentManager.jsx'
import IntegrationM365Manager from '../components/IntegrationM365Manager.jsx'
import IntegrationSapManager from '../components/IntegrationSapManager.jsx'
import IntegrationOidcManager from '../components/IntegrationOidcManager.jsx'
import IntegrationSamlManager from '../components/IntegrationSamlManager.jsx'

/** Paramètres du tenant (admin) : sections des modules souscrits, puis authentification unique (transverse). */
export default function Parametres() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const { actif } = useModules()
  if (!hasRole('ADMIN')) return <div className="panel">{t('parametres.accesReserve')}</div>
  const redaction = actif('redaction')
  return (
    <div>
      <section className="panel">
        <h1>{t('parametres.titre')}</h1>
        {(redaction || actif('formulaire')) && <ReferenceManager portee="admin" modules={['redaction', 'formulaire']} />}
      </section>
      {redaction && <ModeleDocumentManager portee="admin" />}
      {redaction && <IntegrationM365Manager />}
      {redaction && <IntegrationSapManager />}
      <IntegrationOidcManager />
      <IntegrationSamlManager />
    </div>
  )
}
