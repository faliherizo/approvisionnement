import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = {
  actif: false, msTenantId: '', clientId: '', clientSecret: '', sharepointSiteId: '',
  driveId: '', teamsTeamId: '', teamsChannelId: '', outlookExpediteur: '', dossierRacine: 'Contrats',
  secretDefini: false,
}

/**
 * Configuration Microsoft 365 du tenant (espace admin). Chaque organisation saisit ici ses propres
 * identifiants (SharePoint, OneDrive, Teams, Outlook) pour tester la solution avec son environnement
 * réel. Par défaut la publication est simulée ; elle passe au réel lorsque « Activer » est coché et
 * que les identifiants sont complets. Le secret client n'est jamais réaffiché (saisie seule).
 */
export default function IntegrationM365Manager() {
  const { t } = useI18n()
  const [config, setConfig] = useState(VIDE)
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [test, setTest] = useState(null)

  useEffect(() => {
    api.get('/parametres/integration-m365')
      .then((r) => setConfig({ ...VIDE, ...nettoyer(r.data), clientSecret: '' }))
      .catch(() => setErr(t('integrationm365manager.chargementImpossible')))
  }, [])

  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))
  const maj = (champ) => (e) => setConfig({ ...config, [champ]: e.target.value })

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.put('/parametres/integration-m365', config)
      setConfig({ ...VIDE, ...nettoyer(r.data), clientSecret: '' })
      setMsg(t('integrationm365manager.enregistree'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('integrationm365manager.enregistrementRefuse'))
    }
  }

  async function testerConnexion() {
    setErr(null); setMsg(null); setTest(null)
    try {
      const r = await api.post('/parametres/integration-m365/test', {})
      setTest(r.data)
    } catch {
      setTest({ succes: false, message: t('integrationm365manager.testImpossible') })
    }
  }

  return (
    <section className="panel">
      <h2>☁️ {t('integrationm365manager.titre')}</h2>
      <p className="muted">
        {t('integrationm365manager.introA')} <strong>{t('integrationm365manager.votre')}</strong> {t('integrationm365manager.introB')} <strong>{t('integrationm365manager.simules')}</strong>{t('integrationm365manager.introC')}
      </p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <label className="check-row">
          <input type="checkbox" checked={!!config.actif}
                 onChange={(e) => setConfig({ ...config, actif: e.target.checked })} />
          {t('integrationm365manager.activerReels')}
        </label>

        <div className="grid2">
          <label>{t('integrationm365manager.idLocataire')}
            <input value={config.msTenantId} onChange={maj('msTenantId')} autoComplete="off" />
          </label>
          <label>{t('integrationm365manager.idApplication')}
            <input value={config.clientId} onChange={maj('clientId')} autoComplete="off" />
          </label>
        </div>

        <label>{t('integrationm365manager.secretClient')}{config.secretDefini ? ` ${t('integrationm365manager.dejaDefiniConserver')}` : ''}
          <input type="password" value={config.clientSecret} onChange={maj('clientSecret')}
                 placeholder={config.secretDefini ? `•••••••• ${t('integrationm365manager.conserveM')}` : ''} autoComplete="new-password" />
        </label>

        <div className="grid2">
          <label>{t('integrationm365manager.idSiteSharepoint')}
            <input value={config.sharepointSiteId} onChange={maj('sharepointSiteId')} autoComplete="off" />
          </label>
          <label>{t('integrationm365manager.idDriveOnedrive')}
            <input value={config.driveId} onChange={maj('driveId')} autoComplete="off" />
          </label>
        </div>

        <div className="grid2">
          <label>{t('integrationm365manager.idEquipeTeams')}
            <input value={config.teamsTeamId} onChange={maj('teamsTeamId')} autoComplete="off" />
          </label>
          <label>{t('integrationm365manager.idCanalTeams')}
            <input value={config.teamsChannelId} onChange={maj('teamsChannelId')} autoComplete="off" />
          </label>
        </div>

        <div className="grid2">
          <label>{t('integrationm365manager.expediteurOutlook')}
            <input type="email" value={config.outlookExpediteur} onChange={maj('outlookExpediteur')} autoComplete="off" />
          </label>
          <label>{t('integrationm365manager.repertoireRacine')}
            <input value={config.dossierRacine} onChange={maj('dossierRacine')} placeholder={t('integrationm365manager.placeholderContrats')} />
          </label>
        </div>

        <div className="vitrine-actions">
          <button type="submit">{t('integrationm365manager.enregistrer')}</button>
          <button type="button" className="secondary" onClick={testerConnexion}>{t('integrationm365manager.testerConnexion')}</button>
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
