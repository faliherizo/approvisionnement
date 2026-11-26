import { useId, useState } from 'react'
import { useI18n } from '../../i18n/I18nContext'

/** Segment d'un écart : l'ajout et le retrait se lisent par leur balise et leur libellé masqué, jamais par la seule couleur. */
function Segment({ s }) {
  const { t } = useI18n()
  if (s.nature === 'AJOUT') return <ins className="clm-diff__ajout"><span className="sr-only">{t('clm.ng.ajoute')} </span>{s.texte}</ins>
  if (s.nature === 'RETRAIT') return <del className="clm-diff__retrait"><span className="sr-only">{t('clm.ng.retire')} </span>{s.texte}</del>
  return <span>{s.texte}</span>
}

/** Écarts clause par clause entre deux versions d'une négociation (RST-55). */
export function Ecarts({ ecarts }) {
  const { t } = useI18n()
  return (
    <ul className="clm-liste clm-diff" aria-label={t('clm.ng.ecarts')}>
      {ecarts.map((e) => (
        <li key={e.cle}>
          <strong>{e.titre}</strong> <span className="muted">· {t(`clm.ng.changement.${e.changement}`)}</span>
          <p className="clm-diff__texte">{e.segments.map((s, k) => <span key={k}>{k > 0 ? ' ' : ''}<Segment s={s} /></span>)}</p>
        </li>
      ))}
    </ul>
  )
}

/** Choix de deux versions et affichage de leur comparaison ; `charger(de, a)` rend la comparaison du serveur. */
export function ComparaisonVersions({ versions, charger, onErreur }) {
  const { t } = useI18n()
  const id = useId()
  // Sans choix explicite : de la première à la dernière version, qui change à chaque tour.
  const [choixDe, setDe] = useState(null)
  const [choixA, setA] = useState(null)
  const [resultat, setResultat] = useState(null)
  if (versions.length < 2) return <p className="muted">{t('clm.ng.uneSeuleVersion')}</p>
  const de = choixDe ?? versions[0]
  const a = choixA ?? versions[versions.length - 1]
  async function comparer(e) {
    e.preventDefault()
    try { setResultat(await charger(de, a)) } catch (err) { onErreur?.(err) }
  }
  return (
    <form className="form" onSubmit={comparer} aria-labelledby={id}>
      <p id={id} className="sr-only">{t('clm.ng.comparer')}</p>
      <div className="cmp-row">
        <label>{t('clm.ng.versionDepart')}
          <select value={de} onChange={(e) => setDe(Number(e.target.value))}>{versions.map((v) => <option key={v} value={v}>v{v}</option>)}</select>
        </label>
        <label>{t('clm.ng.versionArrivee')}
          <select value={a} onChange={(e) => setA(Number(e.target.value))}>{versions.map((v) => <option key={v} value={v}>v{v}</option>)}</select>
        </label>
        <button type="submit" className="secondary">{t('clm.ng.comparer')}</button>
      </div>
      {resultat && (
        <div role="status" aria-live="polite">
          <p className="muted">{t('clm.ng.comparaisonDe', { de: resultat.de, a: resultat.a })}</p>
          <Ecarts ecarts={resultat.ecarts} />
        </div>
      )}
    </form>
  )
}

/** Clauses d'une version, lisibles telles quelles (retours à la ligne conservés). */
export function ClausesVersion({ clauses }) {
  return (
    <ol className="clm-clauses">
      {clauses.map((c) => (
        <li key={c.cle}><strong>{c.titre}</strong><p className="clm-diff__texte">{c.texte}</p></li>
      ))}
    </ol>
  )
}
