import { useEffect, useRef, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/**
 * Défi anti-robot d'un formulaire (CHU 3.07). Le fournisseur et la clé de site viennent du serveur : Cloudflare
 * Turnstile ou hCaptcha en exploitation, case à cocher en simulation. Rend le jeton à joindre à la soumission
 * (vide tant que le défi n'est pas relevé ou après son expiration).
 */
const SCRIPTS = {
  TURNSTILE: { src: 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit', global: 'turnstile' },
  HCAPTCHA: { src: 'https://js.hcaptcha.com/1/api.js?render=explicit', global: 'hcaptcha' },
}

function chargerScript(src) {
  const existant = document.querySelector(`script[src="${src}"]`)
  if (existant?.dataset.charge) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const s = existant || Object.assign(document.createElement('script'), { src, async: true })
    s.addEventListener('load', () => { s.dataset.charge = '1'; resolve() })
    s.addEventListener('error', reject)
    if (!existant) document.head.appendChild(s)
  })
}

export default function DefiAntiRobot({ onJeton }) {
  const { t, locale } = useI18n()
  const [config, setConfig] = useState(null)
  const [coche, setCoche] = useState(false)
  const [erreur, setErreur] = useState(false)
  const zone = useRef(null)

  useEffect(() => {
    api.get('/saisie/captcha').then(({ data }) => setConfig(data)).catch(() => setErreur(true))
  }, [])

  useEffect(() => {
    const script = config && SCRIPTS[config.fournisseur]
    if (!script) return undefined
    let actif = true
    let widget
    chargerScript(script.src).then(() => {
      if (!actif || !zone.current) return
      widget = window[script.global].render(zone.current, {
        sitekey: config.cleSite,
        language: locale, hl: locale,
        callback: (jeton) => onJeton(jeton),
        'expired-callback': () => onJeton(''),
        'error-callback': () => { onJeton(''); setErreur(true) },
      })
    }).catch(() => setErreur(true))
    return () => {
      actif = false
      if (widget !== undefined) window[script.global]?.remove?.(widget)
    }
  }, [config, locale, onJeton])

  if (erreur) return <p className="erreur" role="alert">{t('remplir.captchaIndisponible')}</p>
  if (!config) return null
  if (config.fournisseur === 'SIMULATION') {
    return (
      <label className="check-row">
        <input type="checkbox" checked={coche}
               onChange={(e) => { setCoche(e.target.checked); onJeton(e.target.checked ? 'captcha-simule-ok' : '') }} />
        {' '}{t('remplir.captcha')}
      </label>
    )
  }
  return <div ref={zone} role="group" aria-label={t('remplir.captchaAria')} />
}
