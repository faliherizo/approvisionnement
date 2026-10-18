import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = {
  actif: false, baseUrl: '', servicePath: '', auth: 'basic',
  username: '', password: '', apiKey: '', passwordDefini: false, apiKeyDefini: false,
}

/**
 * Configuration SAP du tenant (espace admin). Chaque organisation branche son propre service OData
 * SAP pour transmettre la référence financière des dossiers. Par défaut la transmission est simulée ;
 * elle passe au réel lorsque « Activer » est coché et la configuration complète. Les secrets
 * (mot de passe, clé API) ne sont jamais réaffichés (saisie seule).
 */
export default function IntegrationSapManager() {
  const { t } = useI18n()
  const [config, setConfig] = useState(VIDE)
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [test, setTest] = useState(null)

  useEffect(() => {
    api.get('/parametres/integration-sap')
      .then((r) => setConfig({ ...VIDE, ...nettoyer(r.data), password: '', apiKey: '' }))
      .catch(() => setErr(t('integrationsapmanager.chargementImpossible')))
  }, [])

  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))
  const maj = (champ) => (e) => setConfig({ ...config, [champ]: e.target.value })

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.put('/parametres/integration-sap', config)
      setConfig({ ...VIDE, ...nettoyer(r.data), password: '', apiKey: '' })
      setMsg(t('integrationsapmanager.enregistree'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('integrationsapmanager.enregistrementRefuse'))
    }
  }

  async function testerConnexion() {
    setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.post('/parametres/integration-sap/test', config)
      setTest(r.data)
    } catch {
      setTest({ succes: false, message: t('integrationsapmanager.testImpossible') })
    }
  }

  const estApiKey = config.auth === 'apikey'

  return (
    <section className="panel">
      <h2>🧾 {t('integrationsapmanager.titre')}</h2>
      <p className="muted">
        {t('integrationsapmanager.introA')} <strong>{t('integrationsapmanager.serviceOData')}</strong> {t('integrationsapmanager.introB')} <strong>{t('integrationsapmanager.votre')}</strong> {t('integrationsapmanager.introC')} <strong>{t('integrationsapmanager.simulee')}</strong>{t('integrationsapmanager.introD')}
      </p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <label className="check-row">
          <input type="checkbox" checked={!!config.actif}
                 onChange={(e) => setConfig({ ...config, actif: e.target.checked })} />
          {t('integrationsapmanager.activerReelle')}
        </label>

        <div className="grid2">
          <label>{t('integrationsapmanager.urlBase')}
            <input value={config.baseUrl} onChange={maj('baseUrl')} autoComplete="off"
                   placeholder="https://sap.ville.ca/sap/opu/odata/sap" />
          </label>
          <label>{t('integrationsapmanager.cheminService')}
            <input value={config.servicePath} onChange={maj('servicePath')} autoComplete="off"
                   placeholder="/ZREF_APPRO_SRV/References" />
          </label>
        </div>

        <label>{t('integrationsapmanager.authentification')}
          <select value={config.auth} onChange={maj('auth')}>
            <option value="basic">{t('integrationsapmanager.optionBasic')}</option>
            <option value="apikey">{t('integrationsapmanager.optionApikey')}</option>
          </select>
        </label>

        {estApiKey ? (
          <label>{t('integrationsapmanager.cleApi')}{config.apiKeyDefini ? ` ${t('integrationsapmanager.dejaDefinieConserver')}` : ''}
            <input type="password" value={config.apiKey} onChange={maj('apiKey')}
                   placeholder={config.apiKeyDefini ? `•••••••• ${t('integrationsapmanager.conserveF')}` : ''} autoComplete="new-password" />
          </label>
        ) : (
          <div className="grid2">
            <label>{t('integrationsapmanager.utilisateurSap')}
              <input value={config.username} onChange={maj('username')} autoComplete="off" />
            </label>
            <label>{t('integrationsapmanager.motDePasse')}{config.passwordDefini ? ` ${t('integrationsapmanager.dejaDefiniConserver')}` : ''}
              <input type="password" value={config.password} onChange={maj('password')}
                     placeholder={config.passwordDefini ? `•••••••• ${t('integrationsapmanager.conserveM')}` : ''} autoComplete="new-password" />
            </label>
          </div>
        )}

        <div className="vitrine-actions">
          <button type="submit">{t('integrationsapmanager.enregistrer')}</button>
          <button type="button" className="secondary" onClick={testerConnexion}>{t('integrationsapmanager.testerConnexion')}</button>
        </div>
      </form>

      {test && (
        <div className="erreur" role="status"
             style={test.succes ? { background: '#d9f5e3', color: '#1a7a44' } : {}}>
          {test.succes ? '✓ ' : '✗ '}{test.message}
        </div>
      )}
    </section>
  )
}
