import { useAuth } from '../auth.jsx'
import GabaritManager from '../components/GabaritManager.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Gestion des gabarits de documents pour l'entreprise (admin). */
export default function Gabarits() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  if (!hasRole('ADMIN')) return <div className="panel">{t('gabarits.accesReserve')}</div>
  return (
    <div>
      <section className="panel">
        <h1>{t('gabarits.titre')}</h1>
        <p className="muted">{t('gabarits.description')}</p>
      </section>
      <GabaritManager />
    </div>
  )
}
