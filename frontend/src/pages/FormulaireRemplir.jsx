import { useEffect, useState, useRef, useCallback } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'
import FormulaireRenderer from '../components/FormulaireRenderer.jsx'

/** Remplissage d'une fiche par un répondant : rendu dynamique, règles live, autosave, soumission. */
export default function FormulaireRemplir() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const saisieExistante = params.get('saisie')
  const navigate = useNavigate()
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [structure, setStructure] = useState(null)
  const [saisieId, setSaisieId] = useState(null)
  const [valeurs, setValeurs] = useState({})
  const [etat, setEtat] = useState({})
  const [captcha, setCaptcha] = useState(false)
  const [sauvegarde, setSauvegarde] = useState(null)
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const [envoye, setEnvoye] = useState(false)
  const timers = useRef({})

  useEffect(() => {
    if (chargement || !actif('saisie')) return
    let annule = false
    ;(async () => {
      try {
        const s = await api.get(`/formulaires/${id}/saisies/structure`)
        const parsed = s.data?.structureJson ? JSON.parse(s.data.structureJson) : { pages: [] }
        let saisie
        if (saisieExistante) {
          const ex = await api.get(`/formulaires/${id}/saisies/${saisieExistante}`)
          saisie = ex.data
          try { if (saisie.donneesJson) setValeurs(JSON.parse(saisie.donneesJson)) } catch { /* données illisibles : repart vide */ }
        } else {
          const c = await api.post(`/formulaires/${id}/saisies`)
          saisie = c.data
        }
        if (annule) return
        setStructure(parsed); setSaisieId(saisie.id)
      } catch (e) { if (!annule) setErr(e.response?.data?.message || t('remplir.erreurOuverture')) }
    })()
    return () => { annule = true }
  }, [id, actif, chargement, t, saisieExistante])

  const captchaRequis = !!structure?.activation?.captchaRequis

  const planifier = useCallback((cle, fn, delai) => {
    clearTimeout(timers.current[cle]); timers.current[cle] = setTimeout(fn, delai)
  }, [])

  function changer(code, valeur) {
    setValeurs((v) => {
      const next = { ...v, [code]: valeur }
      if (saisieId) {
        planifier('save', () => {
          setSauvegarde(t('remplir.sauvegardeEnCours'))
          api.patch(`/formulaires/${id}/saisies/${saisieId}`, JSON.stringify(next), { headers: { 'Content-Type': 'application/json' } })
            .then(() => setSauvegarde(t('remplir.sauvegarde')))
            .catch(() => setSauvegarde(t('remplir.sauvegardeEchec')))
        }, 600)
      }
      if (actif('regle')) {
        planifier('eval', () => {
          api.post(`/formulaires/${id}/regles/evaluer`, next).then((r) => setEtat(r.data || {})).catch(() => {})
        }, 300)
      }
      return next
    })
  }

  async function soumettre(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.post(`/formulaires/${id}/saisies/${saisieId}/soumettre`, {
        donneesJson: JSON.stringify(valeurs),
        captchaToken: captchaRequis ? (captcha ? 'captcha-simule-ok' : '') : null,
      })
      setEnvoye(true); setMsg(t('remplir.merci'))
    } catch (e2) { setErr(e2.response?.data?.message || t('remplir.erreurSoumission')) }
  }

  if (chargement) return <div className="panel">{t('remplir.chargement')}</div>
  if (!actif('saisie')) return <div className="panel">{t('remplir.moduleInactif')}</div>

  if (envoye) {
    return (
      <div className="panel">
        <h1>{t('remplir.confirmationTitre')}</h1>
        <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>
        <button className="secondary" onClick={() => navigate(`/formulaires/${id}/fiches`)}>{t('remplir.voirFiches')}</button>
      </div>
    )
  }

  return (
    <form onSubmit={soumettre}>
      <div className="panel">
        <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>{t('remplir.titre')}</h1>
          {sauvegarde && <span className="muted" role="status">{sauvegarde}</span>}
        </div>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      {structure && <FormulaireRenderer structure={structure} valeurs={valeurs} etat={etat} onChange={changer} />}

      <div className="panel">
        {captchaRequis && (
          <label className="check-row">
            <input type="checkbox" checked={captcha} onChange={(e) => setCaptcha(e.target.checked)} /> {t('remplir.captcha')}
          </label>
        )}
        <button type="submit" disabled={!saisieId || (etat.erreurs && etat.erreurs.length > 0) || (captchaRequis && !captcha)}>
          {t('remplir.soumettre')}
        </button>
      </div>
    </form>
  )
}
