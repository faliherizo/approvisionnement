import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import api from '../../api'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import './clm.css'

/** Rôles qui créent une trousse, comme le vérifie le serveur ; l'auditeur et le lecteur consultent seulement. */
const ROLES_INITIATEURS = ['REDACTEUR', 'APPRO', 'JURIDIQUE', 'DIRECTION', 'ADMIN', 'SUPERADMIN']

export function usePeutInitier() {
  const { hasRole } = useAuth()
  return ROLES_INITIATEURS.some((r) => hasRole(r))
}

const ICONES = {
  BROUILLON: '✎', EN_TRAITEMENT: '⟳', TERMINEE: '✔', REJETEE: '✖', ANNULEE: '⊘', EN_ATTENTE: '…', ACTIVE: '▶',
  APPROUVEE: '✔', ENVOYEE: '✉', VERIFIEE: '☑', SIGNEE: '✍', REFUSEE: '✖', BLOQUEE: '⛔', EXPIREE: '⌛', ENVOYE: '➜',
  SIGNE: '🔒', REMPLACE: '↺', RETIRE: '⊘', EN_REVISION: '◐', PUBLIE: '✔', ECHEC: '⚠', A_REVISER: '◐', VALIDEE: '✔',
  PROPRE: '✔', INFECTE: '⚠', NON_REQUISE: '⤳', A_FAIRE: '○', EN_COURS: '◑',
  TERMINE: '✔', TERMINE_AVEC_ECHECS: '⚠', CREEE: '✎', LANCEE: '⟳', INVALIDEE: '⊘',
}
const TONS = { TERMINEE: 'ok', TERMINE: 'ok', TERMINE_AVEC_ECHECS: 'ko', LANCEE: 'info', CREEE: 'attente', APPROUVEE: 'ok', SIGNEE: 'ok', SIGNE: 'ok', PUBLIE: 'ok', VALIDEE: 'ok', PROPRE: 'ok', ENVOYE: 'ok',
  REJETEE: 'ko', REFUSEE: 'ko', BLOQUEE: 'ko', ECHEC: 'ko', INFECTE: 'ko', EXPIREE: 'ko',
  EN_TRAITEMENT: 'info', ACTIVE: 'info', EN_COURS: 'info', A_FAIRE: 'attente', ENVOYEE: 'info', VERIFIEE: 'info', EN_REVISION: 'attente', A_REVISER: 'attente', EN_ATTENTE: 'attente' }

/** Statut lisible : icône + texte, jamais la couleur seule (WCAG 1.4.1). */
export function Statut({ valeur }) {
  const { t } = useI18n()
  if (!valeur) return null
  return (
    <span className={`clm-statut clm-statut--${TONS[valeur] || 'neutre'}`}>
      <span aria-hidden="true">{ICONES[valeur] || '•'}</span> {t(`clm.statut.${valeur}`)}
    </span>
  )
}

/** Message d'erreur annoncé (role=alert) ou d'état (role=status). */
export function Message({ erreur, info, onReessayer }) {
  const { t } = useI18n()
  if (erreur) {
    return (
      <div className="erreur clm-message" role="alert">
        {erreur}{onReessayer && <> <button type="button" className="lien" onClick={onReessayer}>{t('clm.reessayer')}</button></>}
      </div>
    )
  }
  return <div className="clm-info" role="status" aria-live="polite">{info || ''}</div>
}

/** Onglets accessibles (motif ARIA tabs) : flèches gauche/droite, Début/Fin. */
export function Onglets({ onglets, actif, onChange, label }) {
  const refs = useRef({})
  const clavier = (e, i) => {
    const n = onglets.length
    const cible = { ArrowRight: (i + 1) % n, ArrowLeft: (i - 1 + n) % n, Home: 0, End: n - 1 }[e.key]
    if (cible === undefined) return
    e.preventDefault()
    onChange(onglets[cible].id)
    refs.current[onglets[cible].id]?.focus()
  }
  return (
    <div className="clm-onglets" role="tablist" aria-label={label}>
      {onglets.map((o, i) => (
        <button key={o.id} type="button" role="tab" id={`onglet-${o.id}`} aria-controls={`panneau-${o.id}`}
          aria-selected={actif === o.id} tabIndex={actif === o.id ? 0 : -1} ref={(el) => { refs.current[o.id] = el }}
          className={actif === o.id ? 'is-actif' : ''} onClick={() => onChange(o.id)} onKeyDown={(e) => clavier(e, i)}>
          {o.libelle}
        </button>
      ))}
    </div>
  )
}

export function Panneau({ id, actif, children }) {
  if (actif !== id) return null
  return <section role="tabpanel" id={`panneau-${id}`} aria-labelledby={`onglet-${id}`} tabIndex={0}>{children}</section>
}

export const OPERATEURS = ['EGAL', 'DIFFERENT', 'SUP', 'INF', 'SUPEGAL', 'INFEGAL', 'CONTIENT', 'VIDE', 'NON_VIDE']

/**
 * Condition simple « champ opérateur valeur », évaluée par le moteur de règles partagé : affichage d'un champ ou d'une
 * clause dans un modèle, règle d'affaires d'une étape de parcours. `champs` : [{ cle, libelle }].
 */
export function EditeurCondition({ conditions, onChange, champs, idChamp, legende, aucune }) {
  const { t } = useI18n()
  const c = conditions[0] || { champ: '', operateur: 'EGAL', valeur: '' }
  const maj = (k, v) => onChange([{ ...c, [k]: v }])
  return (
    <fieldset className="form-fieldset">
      <legend>{legende || t('clm.mo.condition')}</legend>
      <div className="form-grid">
        <label>{t('clm.mo.condChamp')}
          <select id={idChamp} value={c.champ} onChange={(e) => (e.target.value ? maj('champ', e.target.value) : onChange([]))}>
            <option value="">{aucune || t('clm.mo.toujours')}</option>
            {champs.map((x) => <option key={x.cle} value={x.cle}>{x.libelle || x.cle}</option>)}
          </select>
        </label>
        <label>{t('clm.mo.condOperateur')}
          <select value={c.operateur} onChange={(e) => maj('operateur', e.target.value)} disabled={!c.champ}>{OPERATEURS.map((o) => <option key={o} value={o}>{t(`clm.op.${o}`)}</option>)}</select>
        </label>
        <label className="col-2">{t('clm.col.valeur')}<input value={c.valeur || ''} onChange={(e) => maj('valeur', e.target.value)} disabled={!c.champ} /></label>
      </div>
    </fieldset>
  )
}

export const dateHeure = (iso) => (iso ? new Date(iso).toLocaleString('fr-CA', { dateStyle: 'medium', timeStyle: 'short' }) : '—')
export const date = (iso) => (iso ? new Date(iso).toLocaleDateString('fr-CA', { dateStyle: 'medium' }) : '—')
export const taille = (o) => (o >= 1_048_576 ? `${(o / 1_048_576).toFixed(1)} Mo` : `${Math.max(1, Math.round(o / 1024))} Ko`)

/** Message serveur en français s'il existe, sinon le message générique traduit. */
export function useErreur() {
  const { t } = useI18n()
  return useCallback((e) => e?.response?.data?.message || t('clm.erreur'), [t])
}

/** Chargement d'une ressource avec états chargement / erreur / données, rechargeable. */
export function useRessource(url, actif = true) {
  const [donnees, setDonnees] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(true)
  const versErreur = useErreur()
  const charger = useCallback(() => {
    if (!url || !actif) return
    setChargement(true)
    setErreur(null)
    api.get(url).then((r) => setDonnees(r.data)).catch((e) => setErreur(versErreur(e))).finally(() => setChargement(false))
  }, [url, actif, versErreur])
  useEffect(() => { charger() }, [charger])
  return { donnees, erreur, chargement, recharger: charger }
}

/** Garde de module : rend `children` seulement si le module CLM (ou l'option) est actif. */
export function GardeModule({ code = 'clm', children }) {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  if (chargement) return <div className="panel" role="status">{t('clm.chargement')}</div>
  if (!actif(code)) return <div className="panel" role="status">{t(code === 'clm' ? 'clm.moduleInactif' : 'clm.ia.nonSouscrite')}</div>
  return children
}

/**
 * Liste de référence de l'organisation (types de contrat, unités, rôles, champs Salesforce…), gérée dans son
 * administration : valeurs actives et libellé d'un code enregistré.
 */
export function useListe(code, actif = true) {
  const { donnees } = useRessource(actif ? `/reference/${code}` : null, actif)
  return useMemo(() => {
    const valeurs = donnees || []
    return { valeurs, charge: donnees !== null, libelle: (v) => valeurs.find((x) => x.code === v)?.libelle || v || '' }
  }, [donnees])
}

/**
 * Choix dans une liste de l'organisation. Sans liste, la saisie reste libre : l'organisation n'a rien imposé. Une valeur
 * enregistrée puis retirée de la liste reste affichée, pour ne pas être perdue à la prochaine modification.
 */
export function ChoixListe({ liste, value, onChange, vide, ...attributs }) {
  const { t } = useI18n()
  if (!liste.charge) return <select {...attributs} value="" disabled><option value="">{t('clm.chargement')}</option></select>
  if (liste.valeurs.length === 0) return <input {...attributs} value={value || ''} onChange={(e) => onChange(e.target.value)} />
  const connue = !value || liste.valeurs.some((v) => v.code === value)
  return (
    <select {...attributs} value={value || ''} onChange={(e) => onChange(e.target.value)}>
      <option value="">{vide || t('clm.liste.aucune')}</option>
      {!connue && <option value={value}>{value}</option>}
      {liste.valeurs.map((v) => <option key={v.code} value={v.code}>{v.libelle}</option>)}
    </select>
  )
}

/** Visionneuse (UI-05, UI-13) : PDF affiché par le navigateur, Word en texte extrait côté serveur. */
export function Visionneuse({ trousseId, document: doc, onFermer }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [url, setUrl] = useState(null)
  const [texte, setTexte] = useState(null)
  const [erreur, setErreur] = useState(null)
  useEffect(() => {
    let lien = null
    let actif = true
    setUrl(null)
    setTexte(null)
    setErreur(null)
    const base = `/clm/trousses/${trousseId}/documents/${doc.id}`
    const requete = doc.type === 'PDF'
      ? api.get(`${base}/contenu`, { responseType: 'blob' }).then((r) => {
        lien = URL.createObjectURL(new Blob([r.data], { type: 'application/pdf' }))
        if (actif) setUrl(lien)
      })
      : api.get(`${base}/texte`, { responseType: 'text' }).then((r) => { if (actif) setTexte(r.data) })
    requete.catch((e) => { if (actif) setErreur(versErreur(e)) })
    return () => {
      actif = false
      if (lien) URL.revokeObjectURL(lien)
    }
  }, [trousseId, doc.id, doc.type, versErreur])
  const titre = t('clm.doc.apercuDe', { nom: doc.nom })
  return (
    <div className="clm-visionneuse">
      <div className="clm-entete">
        <h3>{titre} <span className="muted">v{doc.version}</span></h3>
        {onFermer && <button type="button" className="secondary" onClick={onFermer}>{t('clm.fermer')}</button>}
      </div>
      <Message erreur={erreur} />
      {url && <iframe src={url} title={titre} />}
      {texte != null && <pre tabIndex={0} aria-label={titre}>{texte}</pre>}
      {!url && texte == null && !erreur && <p role="status">{t('clm.chargement')}</p>}
    </div>
  )
}

/** Téléchargement authentifié (cookie) d'un fichier binaire. */
export async function telecharger(url, nom) {
  const r = await api.get(url, { responseType: 'blob' })
  const lien = document.createElement('a')
  lien.href = URL.createObjectURL(r.data)
  lien.download = nom
  document.body.appendChild(lien)
  lien.click()
  lien.remove()
  setTimeout(() => URL.revokeObjectURL(lien.href), 1000)
}
