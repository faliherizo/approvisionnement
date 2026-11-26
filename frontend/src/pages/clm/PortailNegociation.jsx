import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import axios from 'axios'
import { useI18n } from '../../i18n/I18nContext'
import LangSwitcher from '../../components/LangSwitcher.jsx'
import { Statut, dateHeure } from './commun.jsx'
import { ClausesVersion, ComparaisonVersions } from './NegociationCommun.jsx'

// Client sans redirection de session : le portail est public et ne dépend d'aucun compte.
const portail = axios.create({ baseURL: '/api/clm/portail' })

/**
 * RST-55 (IA-05) — Portail externe, volet négociation : la contrepartie lit la version courante, propose des modifications
 * clause par clause (texte et commentaire) ou accepte la version, lit les réponses de l'organisation et compare les versions.
 */
export default function PortailNegociation() {
  const { jeton } = useParams()
  const j = encodeURIComponent(jeton)
  const { t, setLocale } = useI18n()
  const [params] = useSearchParams()
  const langueLien = params.get('lang')
  useEffect(() => { if (langueLien === 'en' || langueLien === 'fr') setLocale(langueLien === 'en' ? 'en' : 'fr-CA') }, [langueLien, setLocale])
  const [vue, setVue] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [occupe, setOccupe] = useState(false)
  const titre = useRef(null)
  const message = useCallback((e) => e?.response?.data?.message || t('clm.po.erreur'), [t])

  useEffect(() => {
    portail.get(`/negociation/${j}`).then((r) => setVue(r.data)).catch((e) => setErreur(message(e)))
  }, [j, message])
  useEffect(() => { titre.current?.focus() }, [vue?.etat])

  async function envoyer(propositions) {
    setErreur(null); setOccupe(true)
    try {
      const r = await portail.post(`/negociation/${j}/propositions`, { propositions })
      setVue(r.data); setInfo(t('clm.pn.propositionsEnvoyees'))
    } catch (e) { setErreur(message(e)) } finally { setOccupe(false) }
  }
  async function accepter() {
    if (!window.confirm(t('clm.pn.confirmerAcceptation'))) return
    setErreur(null); setOccupe(true)
    try {
      const r = await portail.post(`/negociation/${j}/acceptation`)
      setVue(r.data); setInfo(t('clm.pn.versionAcceptee'))
    } catch (e) { setErreur(message(e)) } finally { setOccupe(false) }
  }
  const comparer = (de, a) => portail.get(`/negociation/${j}/comparaison`, { params: { de, a } }).then((r) => r.data)

  return (
    <div className="clm-portail" data-maquette="RST-55">
      <a className="skip-link" href="#contenu">{t('clm.po.allerContenu')}</a>
      <header className="clm-portail__entete">
        <div className="clm-portail__marque">
          {vue?.logo && <img className="clm-portail__logo" src={vue.logo} alt="" />}
          <div>
            <p className="public-brand">{vue?.organisation || t('clm.po.marque')}</p>
            <p className="clm-portail__sous-titre">{t('clm.pn.sousTitre')}</p>
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
              <h1 ref={titre} tabIndex={-1}>{vue.titre}</h1>
              <p>{t('clm.pn.invitation', { nom: vue.contrepartie, ref: vue.reference, org: vue.organisation })}</p>
              <p className="clm-meta"><Statut valeur={vue.etat} /> <span>{t('clm.po.expire', { d: dateHeure(vue.expireLe) })}</span></p>
              <p>{t(`clm.pn.etat.${vue.etat}`)}</p>
            </section>
            {vue.etat === 'ATTENTE_CONTREPARTIE'
              ? <Propositions vue={vue} occupe={occupe} onEnvoyer={envoyer} onAccepter={accepter} />
              : (
                <section className="panel" aria-labelledby="pn-version">
                  <h2 id="pn-version">{t('clm.pn.versionCourante', { v: vue.versionCourante })}</h2>
                  <ClausesVersion clauses={vue.clauses} />
                </section>
              )}
            <Reponses propositions={vue.propositions} />
            {vue.versions.length > 1 && (
              <section className="panel" aria-labelledby="pn-comparer">
                <h2 id="pn-comparer">{t('clm.ng.comparerTitre')}</h2>
                <ComparaisonVersions versions={vue.versions} charger={comparer} onErreur={(e) => setErreur(message(e))} />
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}

/** Version courante, clause par clause : la contrepartie ouvre une proposition sur chaque clause qu'elle veut modifier. */
function Propositions({ vue, occupe, onEnvoyer, onAccepter }) {
  const { t } = useI18n()
  const [saisies, setSaisies] = useState({})
  const ouvertes = Object.keys(saisies)
  const maj = (cle, k) => (e) => setSaisies({ ...saisies, [cle]: { ...saisies[cle], [k]: e.target.value } })
  const basculer = (c) => {
    const { [c.cle]: retiree, ...autres } = saisies
    setSaisies(retiree ? autres : { ...saisies, [c.cle]: { texte: c.texte, commentaire: '' } })
  }
  function envoyer(e) {
    e.preventDefault()
    onEnvoyer(ouvertes.map((cle) => ({ cle, texte: saisies[cle].texte, commentaire: saisies[cle].commentaire })))
  }
  return (
    <form className="panel form" onSubmit={envoyer} aria-labelledby="pn-proposer">
      <h2 id="pn-proposer">{t('clm.pn.versionCourante', { v: vue.versionCourante })}</h2>
      <p>{t('clm.pn.aide')}</p>
      <ol className="clm-clauses">
        {vue.clauses.map((c) => (
          <li key={c.cle}>
            <strong>{c.titre}</strong>
            <p className="clm-diff__texte">{c.texte}</p>
            <button type="button" className="secondary" aria-expanded={!!saisies[c.cle]} onClick={() => basculer(c)}>
              {saisies[c.cle] ? t('clm.pn.retirerProposition', { clause: c.titre }) : t('clm.pn.proposer', { clause: c.titre })}</button>
            {saisies[c.cle] && (
              <div className="form">
                <label>{t('clm.pn.texteSouhaite', { clause: c.titre })}
                  <textarea rows={4} value={saisies[c.cle].texte} onChange={maj(c.cle, 'texte')} required /></label>
                <label>{t('clm.pn.commentaire', { clause: c.titre })} <span className="muted">{t('clm.ng.facultatif')}</span>
                  <textarea rows={2} value={saisies[c.cle].commentaire} onChange={maj(c.cle, 'commentaire')} maxLength={2000} /></label>
              </div>
            )}
          </li>
        ))}
      </ol>
      <div className="cmp-row">
        <button type="submit" disabled={occupe || ouvertes.length === 0}>{t('clm.pn.envoyer', { n: ouvertes.length })}</button>
        <button type="button" className="secondary" disabled={occupe || ouvertes.length > 0} onClick={onAccepter}>{t('clm.pn.accepter')}</button>
      </div>
    </form>
  )
}

/** Réponses de l'organisation à chaque proposition : décision, contre-proposition et motif. */
function Reponses({ propositions }) {
  const { t } = useI18n()
  if (propositions.length === 0) return null
  return (
    <section className="panel" aria-labelledby="pn-reponses">
      <h2 id="pn-reponses">{t('clm.pn.reponses')}</h2>
      <ul className="clm-liste">
        {propositions.map((p) => (
          <li key={p.id}>
            <strong>{p.titre}</strong> · {t('clm.ng.tourN', { n: p.tour })} · <Statut valeur={p.decision} />
            <p className="clm-diff__texte">{t('clm.pn.vousProposiez', { texte: p.textePropose })}</p>
            {p.texteContreProposition && <p className="clm-diff__texte">{t('clm.pn.contreProposition', { texte: p.texteContreProposition })}</p>}
            {p.justification && <p className="muted">{t('clm.pn.motif', { motif: p.justification })}</p>}
          </li>
        ))}
      </ul>
    </section>
  )
}
