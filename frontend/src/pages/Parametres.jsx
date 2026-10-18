import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'
import ReferenceManager from '../components/ReferenceManager.jsx'
import ModeleDocumentManager from '../components/ModeleDocumentManager.jsx'
import IntegrationM365Manager from '../components/IntegrationM365Manager.jsx'
import IntegrationSapManager from '../components/IntegrationSapManager.jsx'
import IntegrationOidcManager from '../components/IntegrationOidcManager.jsx'
import IntegrationSamlManager from '../components/IntegrationSamlManager.jsx'

/** Paramètres du tenant (admin) : sélecteurs dynamiques, modèle de document, intégration M365. */
export default function Parametres() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  if (!hasRole('ADMIN')) return <div className="panel">{t('parametres.accesReserve')}</div>
  return (
    <div>
      <section className="panel">
        <h1>{t('parametres.titre')}</h1>
        <ReferenceManager portee="admin" />
      </section>
      <ModeleDocumentManager portee="admin" />
      <IntegrationM365Manager />
      <IntegrationSapManager />
      <IntegrationOidcManager />
      <IntegrationSamlManager />
    </div>
  )
}
