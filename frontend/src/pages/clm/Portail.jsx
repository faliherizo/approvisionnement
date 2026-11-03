import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'
import { useI18n } from '../../i18n/I18nContext'
import LangSwitcher from '../../components/LangSwitcher.jsx'
import { Statut, dateHeure, taille } from './commun.jsx'

// Client sans redirection de session : le portail est public et ne dépend d'aucun compte.
const portail = axios.create({ baseURL: '/api/clm/portail' })

/**
 * UI-16 — Portail du signataire externe (RST-17, RST-19) : invitation, vérification d'identité, lecture,
 * consentement explicite, champs attribués, signature et reçu. Conçu d'abord pour mobile (cibles de 44 px).
 */
export default function Portail() {
  const { jeton } = useParams()
  const { t } = useI18n()
  const [vue, setVue] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [reponse, setReponse] = useState('')
  const [codeEnvoye, setCodeEnvoye] = useState(false)
  const [lus, setLus] = useState({})
  const [consentement, setConsentement] = useState(false)
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
    const r = await appel(() => portail.post(`${base}/signer`, { nomComplet: nom, consentement, champs }), t('clm.po.signee'))
    if (r) setRecu(r.data)
  }
  const refuser = async (e) => {
    e.preventDefault()
    const r = await appel(() => portail.post(`${base}/refuser`, { motif }), t('clm.po.refusEnregistre'))
    if (r) { setVue(r.data); setRefus(false) }
  }

  const ferme = vue && ['REFUSEE', 'ANNULEE', 'BLOQUEE', 'EXPIREE'].includes(vue.statut)
  const tousLus = vue && vue.documents.every((d) => lus[d.id])

  return (
    <div className="clm-portail" data-maquette="UI-16">
      <a className="skip-link" href="#contenu">{t('clm.po.allerContenu')}</a>
      <header className="clm-portail__entete">
        <p className="public-brand">{t('clm.po.marque')}</p>
        <LangSwitcher compact />
      </header>
      <main id="contenu" className="clm-portail__main" tabIndex={-1}>
        <div role="alert" aria-live="assertive">{erreur && <p className="erreur">{erreur}</p>}</div>
        <div role="status" aria-live="polite" className="clm-info">{info}</div>
        {!vue && !erreur && <p role="status">{t('clm.chargement')}</p>}
        {vue && (
          <>
            <section className="panel" data-zone="01">
              <h1 ref={titre} tabIndex={-1}>{recu ? t('clm.po.merci') : vue.titre}</h1>
              <p>{t('clm.po.invitation', { nom: vue.participant, ref: vue.reference })}</p>
              <p className="clm-meta"><Statut valeur={vue.statut} /> <span>{t('clm.po.expire', { d: dateHeure(vue.expireLe) })}</span></p>
            </section>

            {recu && (
              <section className="panel" aria-labelledby="clm-recu" data-zone="06">
                <h2 id="clm-recu">{t('clm.po.recu')}</h2>
                <dl className="clm-dl">
                  <div><dt>{t('clm.col.reference')}</dt><dd>{recu.reference}</dd></div>
                  <div><dt>{t('clm.pr.signataire')}</dt><dd>{recu.signataire}</dd></div>
                  <div><dt>{t('clm.pr.signee')}</dt><dd>{dateHeure(recu.signeeLe)}</dd></div>
                  <div><dt>{t('clm.col.empreinte')}</dt><dd><code className="clm-casse">{recu.empreinteDocuments}</code></dd></div>
                  <div><dt>{t('clm.pr.attestation')}</dt><dd><code>{recu.attestation}</code></dd></div>
                </dl>
                <button type="button" className="secondary" onClick={() => window.print()}>{t('clm.po.imprimer')}</button>
              </section>
            )}

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

            {!recu && !ferme && vue.verifiee && (
              <form className="panel form" onSubmit={signer} aria-labelledby="clm-signer">
                <h2 id="clm-signer">{t('clm.po.etape2')}</h2>
                <ul className="clm-liste" data-zone="03" aria-label={t('clm.po.documents')}>
                  {vue.documents.map((d) => (
                    <li key={d.id} className="clm-portail__doc">
                      <span>{d.nom} <span className="muted">({d.type} · {taille(d.taille)})</span> {lus[d.id] && <span role="img" aria-label={t('clm.po.consulte')}>✔</span>}</span>
                      <button type="button" className="secondary" onClick={() => ouvrir(d)}>{t('clm.po.lire')}</button>
                    </li>
                  ))}
                </ul>
                <fieldset className="form-fieldset" data-zone="05">
                  <legend>{t('clm.po.champs')}</legend>
                  {vue.champs.length === 0 && <p className="muted">{t('clm.po.aucunChamp')}</p>}
                  {vue.champs.map((c) => (
                    <label key={c.cle}>{c.libelle}{c.obligatoire && <span aria-hidden="true"> *</span>}
                      {c.type === 'LISTE' ? (
                        <select value={champs[c.cle] || ''} onChange={(e) => setChamps({ ...champs, [c.cle]: e.target.value })} required={c.obligatoire}>
                          <option value="">—</option>{c.options.map((o) => <option key={o} value={o}>{o}</option>)}
                        </select>
                      ) : (
                        <input type={c.type === 'DATE' ? 'date' : 'text'} value={champs[c.cle] || ''} required={c.obligatoire} aria-required={c.obligatoire}
                          onChange={(e) => setChamps({ ...champs, [c.cle]: e.target.value })} />
                      )}
                    </label>
                  ))}
                </fieldset>
                <div className="form" data-zone="04">
                  <label>{t('clm.po.nomComplet')}<input value={nom} onChange={(e) => setNom(e.target.value)} required autoComplete="name" /></label>
                  <label className="check-row clm-consentement">
                    <input type="checkbox" checked={consentement} onChange={(e) => setConsentement(e.target.checked)} required />
                    {t('clm.po.consentement')}
                  </label>
                </div>
                {!tousLus && <p className="muted">{t('clm.po.lireAvant')}</p>}
                <button type="submit" disabled={occupe || !consentement || !tousLus}>{t('clm.po.signer')}</button>
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
