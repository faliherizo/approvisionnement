import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = {
  actif: false, issuer: '', clientId: '', clientSecret: '', scopes: 'openid profile email',
  boutonLibelle: 'Connexion gouvernementale', claimCourriel: 'email', autoProvision: true,
  roleParDefaut: 'REDACTEUR', secretDefini: false,
}

const ROLES = ['REDACTEUR', 'LECTEUR', 'JURIDIQUE', 'APPRO', 'DIRECTION', 'ADMIN']

/**
 * Configuration de l'authentification OpenID Connect du tenant (espace admin). Chaque organisation
 * branche son propre annuaire gouvernemental (Microsoft Entra ID ou tout IdP OIDC) pour que ses
 * utilisateurs se connectent via leur compte. Tant que « Activer » n'est pas coché et la config
 * complète, la page de connexion n'affiche que l'authentification locale. Le secret client n'est
 * jamais réaffiché (saisie seule).
 */
export default function IntegrationOidcManager() {
  const { t } = useI18n()
  const [config, setConfig] = useState(VIDE)
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [test, setTest] = useState(null)

  useEffect(() => {
    api.get('/parametres/integration-oidc')
      .then((r) => setConfig({ ...VIDE, ...nettoyer(r.data), clientSecret: '' }))
      .catch(() => setErr(t('integrationoidcmanager.chargementImpossible')))
  }, [])

  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))
  const maj = (champ) => (e) => setConfig({ ...config, [champ]: e.target.value })

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.put('/parametres/integration-oidc', config)
      setConfig({ ...VIDE, ...nettoyer(r.data), clientSecret: '' })
      setMsg(t('integrationoidcmanager.enregistree'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('integrationoidcmanager.enregistrementRefuse'))
    }
  }

  async function testerDecouverte() {
    setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.post('/parametres/integration-oidc/test', config)
      setTest(r.data)
    } catch {
      setTest({ succes: false, message: t('integrationoidcmanager.testImpossible') })
    }
  }

  return (
    <section className="panel">
      <h2>🔐 {t('integrationoidcmanager.titre')}</h2>
      <p className="muted">
        {t('integrationoidcmanager.introA')} <strong>{t('integrationoidcmanager.annuaire')}</strong> {t('integrationoidcmanager.introB')}
      </p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <label className="check-row">
          <input type="checkbox" checked={!!config.actif}
                 onChange={(e) => setConfig({ ...config, actif: e.target.checked })} />
          {t('integrationoidcmanager.activerConnexion')}
        </label>

        <label>{t('integrationoidcmanager.emetteur')}
          <input value={config.issuer} onChange={maj('issuer')} autoComplete="off"
                 placeholder="https://login.microsoftonline.com/<tenant>/v2.0" />
        </label>

        <div className="grid2">
          <label>{t('integrationoidcmanager.idClient')}
            <input value={config.clientId} onChange={maj('clientId')} autoComplete="off" />
          </label>
          <label>{t('integrationoidcmanager.secretClient')}{config.secretDefini ? ` ${t('integrationoidcmanager.dejaDefiniConserver')}` : ''}
            <input type="password" value={config.clientSecret} onChange={maj('clientSecret')}
                   placeholder={config.secretDefini ? `•••••••• ${t('integrationoidcmanager.conserveM')}` : ''} autoComplete="new-password" />
          </label>
        </div>

        <div className="grid2">
          <label>{t('integrationoidcmanager.scopes')}
            <input value={config.scopes} onChange={maj('scopes')} autoComplete="off" />
          </label>
          <label>{t('integrationoidcmanager.claimCourriel')}
            <input value={config.claimCourriel} onChange={maj('claimCourriel')} autoComplete="off" />
          </label>
        </div>

        <label>{t('integrationoidcmanager.boutonLibelle')}
          <input value={config.boutonLibelle} onChange={maj('boutonLibelle')} autoComplete="off" />
        </label>

        <label className="check-row">
          <input type="checkbox" checked={!!config.autoProvision}
                 onChange={(e) => setConfig({ ...config, autoProvision: e.target.checked })} />
          {t('integrationoidcmanager.autoProvision')}
        </label>

        <label>{t('integrationoidcmanager.roleParDefaut')}
          <select value={config.roleParDefaut} onChange={maj('roleParDefaut')}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </label>

        <div className="vitrine-actions">
          <button type="submit">{t('integrationoidcmanager.enregistrer')}</button>
          <button type="button" className="secondary" onClick={testerDecouverte}>{t('integrationoidcmanager.testerDecouverte')}</button>
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
