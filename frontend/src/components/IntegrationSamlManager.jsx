import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = {
  actif: false, idpEntityId: '', idpSsoUrl: '', idpCertificat: '', spEntityId: '',
  claimCourriel: 'email', boutonLibelle: 'Connexion SAML', autoProvision: true,
  roleParDefaut: 'REDACTEUR', certificatDefini: false,
}

const ROLES = ['REDACTEUR', 'LECTEUR', 'JURIDIQUE', 'APPRO', 'DIRECTION', 'ADMIN']

/**
 * Configuration de l'authentification SAML 2.0 (Service Provider) du tenant (espace admin). Chaque
 * organisation branche son propre fournisseur d'identité SAML (ADFS, Azure AD, Shibboleth…) pour que
 * ses utilisateurs se connectent via leur compte. Tant que « Activer » n'est pas coché et la config
 * complète (EntityID, URL SSO, certificat), la page de connexion n'affiche que l'authentification
 * locale. Le certificat de signature de l'IdP est public : il est réaffiché.
 */
export default function IntegrationSamlManager() {
  const { t } = useI18n()
  const [config, setConfig] = useState(VIDE)
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [test, setTest] = useState(null)

  useEffect(() => {
    api.get('/parametres/integration-saml')
      .then((r) => setConfig({ ...VIDE, ...nettoyer(r.data) }))
      .catch(() => setErr(t('integrationsamlmanager.chargementImpossible')))
  }, [])

  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))
  const maj = (champ) => (e) => setConfig({ ...config, [champ]: e.target.value })

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.put('/parametres/integration-saml', config)
      setConfig({ ...VIDE, ...nettoyer(r.data) })
      setMsg(t('integrationsamlmanager.enregistree'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('integrationsamlmanager.enregistrementRefuse'))
    }
  }

  async function testerConfig() {
    setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.post('/parametres/integration-saml/test', config)
      setTest(r.data)
    } catch {
      setTest({ succes: false, message: t('integrationsamlmanager.testImpossible') })
    }
  }

  return (
    <section className="panel">
      <h2>🔏 {t('integrationsamlmanager.titre')}</h2>
      <p className="muted">
        {t('integrationsamlmanager.introA')} <strong>{t('integrationsamlmanager.fournisseur')}</strong> {t('integrationsamlmanager.introB')}
      </p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <label className="check-row">
          <input type="checkbox" checked={!!config.actif}
                 onChange={(e) => setConfig({ ...config, actif: e.target.checked })} />
          {t('integrationsamlmanager.activerConnexion')}
        </label>

        <label>{t('integrationsamlmanager.idpEntityId')}
          <input value={config.idpEntityId} onChange={maj('idpEntityId')} autoComplete="off"
                 placeholder="https://idp.example.org/entity" />
        </label>

        <label>{t('integrationsamlmanager.idpSsoUrl')}
          <input value={config.idpSsoUrl} onChange={maj('idpSsoUrl')} autoComplete="off"
                 placeholder="https://idp.example.org/sso" />
        </label>

        <label>{t('integrationsamlmanager.idpCertificat')}{config.certificatDefini ? ` ${t('integrationsamlmanager.certificatDefini')}` : ''}
          <textarea value={config.idpCertificat} onChange={maj('idpCertificat')} rows={6}
                    autoComplete="off" placeholder="-----BEGIN CERTIFICATE-----&#10;...&#10;-----END CERTIFICATE-----" />
        </label>

        <div className="grid2">
          <label>{t('integrationsamlmanager.spEntityId')}
            <input value={config.spEntityId} onChange={maj('spEntityId')} autoComplete="off" />
          </label>
          <label>{t('integrationsamlmanager.claimCourriel')}
            <input value={config.claimCourriel} onChange={maj('claimCourriel')} autoComplete="off" />
          </label>
        </div>

        <label>{t('integrationsamlmanager.boutonLibelle')}
          <input value={config.boutonLibelle} onChange={maj('boutonLibelle')} autoComplete="off" />
        </label>

        <label className="check-row">
          <input type="checkbox" checked={!!config.autoProvision}
                 onChange={(e) => setConfig({ ...config, autoProvision: e.target.checked })} />
          {t('integrationsamlmanager.autoProvision')}
        </label>

        <label>{t('integrationsamlmanager.roleParDefaut')}
          <select value={config.roleParDefaut} onChange={maj('roleParDefaut')}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </label>

        <div className="vitrine-actions">
          <button type="submit">{t('integrationsamlmanager.enregistrer')}</button>
          <button type="button" className="secondary" onClick={testerConfig}>{t('integrationsamlmanager.tester')}</button>
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
