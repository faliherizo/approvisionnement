import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import axios from 'axios'
import { useI18n } from '../../i18n/I18nContext'
import LangSwitcher from '../../components/LangSwitcher.jsx'
import { Statut, dateHeure } from './commun.jsx'
import { ChampsPortail, DocumentsPortail, FormulaireCompletion, FormulaireConsultation, RecuPortail } from './PortailParticipation.jsx'

// Client sans redirection de session : le portail est public et ne dépend d'aucun compte.
const portail = axios.create({ baseURL: '/api/clm/portail' })

/**
 * UI-16 — Portail du signataire externe (RST-17, RST-19) : invitation, vérification d'identité, lecture,
 * consentement explicite, champs attribués, signature et reçu. Conçu d'abord pour mobile (cibles de 44 px).
 * Selon sa nature (AF-48, RST-50), le participant signe, atteste sa consultation ou soumet ses champs.
 */
export default function Portail() {
  const { jeton } = useParams()
  const { t, setLocale } = useI18n()
  const [params] = useSearchParams()
  // Addenda 2, Q19 : le lien d'invitation porte la langue du signataire ; il garde le sélecteur pour en changer.
  const langueInvitation = params.get('lang')
  useEffect(() => { if (langueInvitation === 'en' || langueInvitation === 'fr') setLocale(langueInvitation === 'en' ? 'en' : 'fr-CA') }, [langueInvitation, setLocale])
  const [vue, setVue] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [reponse, setReponse] = useState('')
  const [codeEnvoye, setCodeEnvoye] = useState(false)
  const [lus, setLus] = useState({})
  const [consentement, setConsentement] = useState(false)
  const [autorite, setAutorite] = useState(false)
  const [referenceAutorite, setReferenceAutorite] = useState('')
  const [nom, setNom] = useState('')
  const [champs, setChamps] = useState({})
  const [motif, setMotif] = useState('')
  const [refus, setRefus] = useState(false)
  const [recu, setRecu] = useState(null)
  const [occupe, setOccupe] = useState(false)
  const titre = useRef(null)
  const base = `/${encodeURIComponent(jeton)}`

  const message = useCallback((e) => e?.response?.data?.message || t('clm.po.erreur'), [t])
  const charger = useCallback(() => {
    portail.get(base).then((r) => { setVue(r.data); if (r.data.recu) setRecu(r.data.recu) }).catch((e) => setErreur(message(e)))
  }, [base, message])
  useEffect(() => { charger() }, [charger])
  useEffect(() => { titre.current?.focus() }, [vue?.verifiee, recu])

  async function appel(fn, ok) {
    setErreur(null); setOccupe(true)
    try { const r = await fn(); if (ok) setInfo(ok); return r } catch (e) { setErreur(message(e)); return null } finally { setOccupe(false) }
  }
  const demarrer = async () => {
    const r = await appel(() => portail.post(`${base}/verification`), vue.niveau === 'QUESTION' ? '' : t('clm.po.codeEnvoye'))
    if (r) { setVue(r.data); setCodeEnvoye(true) }
  }
  const verifier = async (e) => {
    e.preventDefault()
    const r = await appel(() => portail.post(`${base}/verification/reponse`, { reponse }), t('clm.po.identiteVerifiee'))
    if (r) { setVue(r.data); setReponse('') }
  }
  const ouvrir = async (doc) => {
    const r = await appel(() => portail.get(`${base}/documents/${doc.id}`, { responseType: 'blob' }))
    if (!r) return
    const url = URL.createObjectURL(r.data)
    const a = document.createElement('a')
    a.href = url; a.download = doc.nom; document.body.appendChild(a); a.click(); a.remove()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
    setLus({ ...lus, [doc.id]: true })
  }
  const signer = async (e) => {
    e.preventDefault()
    const r = await appel(() => portail.post(`${base}/signer`, { nomComplet: nom, consentement, champs, autoriteAttestee: autorite, referenceAutorite }), t('clm.po.signee'))
    if (r) setRecu(r.data)
  }
  const attester = async () => {
    const r = await appel(() => portail.post(`${base}/consultation`, { attestation: true }), t('clm.po.consultationAttestee'))
    if (r) setRecu(r.data)
  }
  const completer = async (valeurs) => {
    const r = await appel(() => portail.post(`${base}/completion`, { champs: valeurs }), t('clm.po.champsSoumis'))
    if (r) setRecu(r.data)
  }
  const refuser = async (e) => {
    e.preventDefault()
    const r = await appel(() => portail.post(`${base}/refuser`, { motif }), t('clm.po.refusEnregistre'))
    if (r) { setVue(r.data); setRefus(false) }
  }

  const ferme = vue && ['REFUSEE', 'ANNULEE', 'BLOQUEE', 'EXPIREE'].includes(vue.statut)
  const tousLus = vue && vue.documents.every((d) => lus[d.id])
  const nature = vue?.nature || 'SIGNER'
  const actif = vue && !recu && !ferme && vue.verifiee

  return (
    <div className="clm-portail" data-maquette="UI-16">
      <a className="skip-link" href="#contenu">{t('clm.po.allerContenu')}</a>
      <header className="clm-portail__entete">
        <div className="clm-portail__marque">
          {vue?.logo && <img className="clm-portail__logo" src={vue.logo} alt="" />}
          <div>
            <p className="public-brand">{vue?.organisation || t('clm.po.marque')}</p>
            {vue?.organisation && <p className="clm-portail__sous-titre">{t('clm.po.marque')}</p>}
          </div>
        </div>
        <LangSwitcher compact />
      </header>
      <main id="contenu" className="clm-portail__main" tabIndex={-1}>
        <div role="alert" aria-live="assertive">{erreur && <p className="erreur">{erreur}</p>}</div>
        <div role="status" aria-live="polite" className="clm-info">{info}</div>
        {!vue && !erreur && <p role="status">{t('clm.chargement')}</p>}
        {vue && (
          <>
            <section className="panel" data-zone="01">
              <h1 ref={titre} tabIndex={-1}>{recu ? t(`clm.po.merciAction.${recu.action || 'SIGNATURE'}`) : vue.titre}</h1>
              <p>{t(`clm.po.invitationNature.${nature}`, { nom: vue.participant, ref: vue.reference })}</p>
              <p className="clm-meta"><Statut valeur={vue.statut} /> <span>{t('clm.po.expire', { d: dateHeure(vue.expireLe) })}</span></p>
            </section>

            {recu && <RecuPortail recu={recu} />}

            {!recu && ferme && <section className="panel"><p>{t(`clm.po.ferme.${vue.statut}`)}</p></section>}

            {!recu && !ferme && !vue.verifiee && (
              <section className="panel" aria-labelledby="clm-verif" data-zone="02">
                <h2 id="clm-verif">{t('clm.po.etape1')}</h2>
                <p>{t(`clm.po.niveau.${vue.niveau}`)}</p>
                {vue.niveau === 'QUESTION' || codeEnvoye ? (
                  <form className="form" onSubmit={verifier}>
                    <label>{vue.niveau === 'QUESTION' ? vue.question || t('clm.po.question') : t('clm.po.code')}
                      <input value={reponse} onChange={(e) => setReponse(e.target.value)} required autoComplete={vue.niveau === 'QUESTION' ? 'off' : 'one-time-code'}
                        inputMode={vue.niveau === 'QUESTION' ? 'text' : 'numeric'} />
                    </label>
                    <button type="submit" disabled={occupe}>{t('clm.po.verifier')}</button>
                    {vue.niveau !== 'QUESTION' && <button type="button" className="secondary" onClick={demarrer} disabled={occupe}>{t('clm.po.renvoyer')}</button>}
                  </form>
                ) : <button type="button" onClick={demarrer} disabled={occupe}>{t('clm.po.recevoirCode')}</button>}
              </section>
            )}

            {actif && nature === 'CONSULTER' && <FormulaireConsultation vue={vue} lus={lus} onOuvrir={ouvrir} onAttester={attester} occupe={occupe} />}
            {actif && nature === 'COMPLETER' && <FormulaireCompletion vue={vue} lus={lus} onOuvrir={ouvrir} onSoumettre={completer} occupe={occupe} />}

            {actif && nature === 'SIGNER' && (
              <form className="panel form" onSubmit={signer} aria-labelledby="clm-signer">
                <h2 id="clm-signer">{t('clm.po.etape2')}</h2>
                <DocumentsPortail documents={vue.documents} lus={lus} onOuvrir={ouvrir} />
                <ChampsPortail champs={vue.champs} valeurs={champs} onChange={setChamps} legende={t('clm.po.champs')} aucun={t('clm.po.aucunChamp')} />
                <div className="form" data-zone="04">
                  {vue.entreprise && <p>{t('clm.po.representation', { entreprise: vue.entreprise, qualite: vue.qualite || '—' })}</p>}
                  {vue.autoriteRequise && (
                    <>
                      <label className="check-row clm-consentement">
                        <input type="checkbox" checked={autorite} onChange={(e) => setAutorite(e.target.checked)} required />
                        {t('clm.po.autorite', { entreprise: vue.entreprise })}
                      </label>
                      <label>{t('clm.po.referenceAutorite')} <span className="muted">{t('clm.po.referenceAutoriteAide')}</span>
                        <input value={referenceAutorite} onChange={(e) => setReferenceAutorite(e.target.value)} maxLength={200} /></label>
                    </>
                  )}
                  <label>{t('clm.po.nomComplet')}<input value={nom} onChange={(e) => setNom(e.target.value)} required autoComplete="name" /></label>
                  <label className="check-row clm-consentement">
                    <input type="checkbox" checked={consentement} onChange={(e) => setConsentement(e.target.checked)} required />
                    {t('clm.po.consentement')}
                  </label>
                </div>
                {!tousLus && <p className="muted">{t('clm.po.lireAvant')}</p>}
                <button type="submit" disabled={occupe || !consentement || !tousLus || (vue.autoriteRequise && !autorite)}>{t('clm.po.signer')}</button>
              </form>
            )}

            {!recu && !ferme && (
              <section className="panel">
                {refus ? (
                  <form className="form" onSubmit={refuser}>
                    <label>{t('clm.po.motifRefus')}<textarea rows={3} value={motif} onChange={(e) => setMotif(e.target.value)} required /></label>
                    <div className="cmp-row"><button type="button" className="secondary" onClick={() => setRefus(false)}>{t('clm.annuler')}</button>
                      <button type="submit" className="danger" disabled={occupe}>{t('clm.po.confirmerRefus')}</button></div>
                  </form>
                ) : <button type="button" className="lien" onClick={() => setRefus(true)}>{t('clm.po.refuser')}</button>}
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}
