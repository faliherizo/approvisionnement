import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

export default function Securite() {
  const { t } = useI18n()
  const [me, setMe] = useState(null)
  const [setup, setSetup] = useState(null)   // {secret, uriOtpauth}
  const [code, setCode] = useState('')
  const [q1, setQ1] = useState(t('securite.q1defaut'))
  const [r1, setR1] = useState('')
  const [msg, setMsg] = useState(null)

  const charger = () => api.get('/auth/me').then((r) => setMe(r.data))
  useEffect(() => { charger() }, [])

  async function demarrerTotp() {
    setMsg(null)
    const { data } = await api.post('/auth/mfa/totp/setup')
    setSetup(data)
  }
  async function activerTotp() {
    setMsg(null)
    try {
      await api.post('/auth/mfa/totp/enable', { code })
      setSetup(null); setCode(''); setMsg(t('securite.msgTotpActive')); charger()
    } catch (e) { setMsg(e.response?.data?.error || t('securite.codeInvalide')) }
  }
  async function activerQuestion(e) {
    e.preventDefault()
    setMsg(null)
    await api.post('/auth/mfa/question/setup', { questions: [{ question: q1, reponse: r1 }] })
    setR1(''); setMsg(t('securite.msgQuestionActive')); charger()
  }
  async function desactiver() {
    await api.post('/auth/mfa/disable'); setMsg(t('securite.msgMfaDesactive')); charger()
  }

  return (
    <div className="grid2">
      <div className="panel">
        <h1>{t('securite.titre')}</h1>
        {me && (
          <p>{t('securite.secondFacteurActuel')}{' '}
            <span className={`badge ${me.mfaActive ? 's-APPROUVE' : ''}`}>
              {me.mfaActive ? me.mfaMethode : t('securite.aucun')}
            </span>
          </p>
        )}
        {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
        {me?.mfaActive && <button className="danger" onClick={desactiver}>{t('securite.desactiverMfa')}</button>}
      </div>

      <div className="panel">
        <h2>{t('securite.authTotp')}</h2>
        <p className="muted">{t('securite.compatibleAuth')}</p>
        {!setup && <button onClick={demarrerTotp}>{t('securite.configurerTotp')}</button>}
        {setup && (
          <div className="form">
            <p>{t('securite.secret')} <code>{setup.secret}</code></p>
            <p className="muted" style={{ wordBreak: 'break-all' }}>{t('securite.uri')} {setup.uriOtpauth}</p>
            <label>{t('securite.codeConfirmation')}
              <input value={code} inputMode="numeric" placeholder="123456"
                     onChange={(e) => setCode(e.target.value)} />
            </label>
            <button onClick={activerTotp}>{t('securite.activer')}</button>
          </div>
        )}
      </div>

      <div className="panel">
        <h2>{t('securite.questionSecreteDynamique')}</h2>
        <form onSubmit={activerQuestion} className="form">
          <label>{t('securite.question')}
            <input value={q1} onChange={(e) => setQ1(e.target.value)} required />
          </label>
          <label>{t('securite.reponse')}
            <input value={r1} onChange={(e) => setR1(e.target.value)} required />
          </label>
          <button type="submit">{t('securite.activerQuestion')}</button>
        </form>
      </div>
    </div>
  )
}
