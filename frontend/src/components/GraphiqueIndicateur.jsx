import { useState } from 'react'
import { useI18n } from '../i18n/I18nContext'

/**
 * Graphique d'un indicateur (CHU 6.04). Une seule série, donc une seule teinte et pas de légende : le titre nomme
 * l'indicateur. Compteur → nombre mis en avant ; barres → barres horizontales avec la valeur au bout ; évolution →
 * colonnes par mois. Info-bulle au survol, et un tableau équivalent pour la lecture non visuelle.
 */
export default function GraphiqueIndicateur({ indicateur, points }) {
  const { t } = useI18n()
  const [tableau, setTableau] = useState(false)
  const max = Math.max(1, ...points.map((p) => p.valeur))
  const resume = points.map((p) => `${p.etiquette} : ${p.valeur}`).join(', ')

  if (indicateur.type === 'COMPTEUR') {
    return (
      <figure className="indicateur">
        <figcaption>{indicateur.titre}</figcaption>
        <p className="indicateur-nombre">{points[0]?.valeur ?? 0}</p>
      </figure>
    )
  }
  return (
    <figure className="indicateur">
      <figcaption>{indicateur.titre}</figcaption>
      {points.length === 0 && <p className="muted">{t('tdb.aucuneDonnee')}</p>}
      {points.length > 0 && !tableau && (indicateur.type === 'BARRES'
        ? <Barres points={points} max={max} resume={resume} />
        : <Colonnes points={points} max={max} resume={resume} />)}
      {points.length > 0 && tableau && (
        <table className="table">
          <thead><tr><th scope="col">{t('tdb.etiquette')}</th><th scope="col">{t('tdb.valeur')}</th></tr></thead>
          <tbody>{points.map((p) => <tr key={p.etiquette}><td>{p.etiquette}</td><td>{p.valeur}</td></tr>)}</tbody>
        </table>
      )}
      {points.length > 0 && (
        <button type="button" className="secondary" aria-pressed={tableau} onClick={() => setTableau(!tableau)}>
          {tableau ? t('tdb.voirGraphique') : t('tdb.voirTableau')}
        </button>
      )}
    </figure>
  )
}

const LIGNE = 28
const ETIQUETTES = 120

function Barres({ points, max, resume }) {
  const hauteur = points.length * LIGNE
  return (
    <svg className="graphique" viewBox={`0 0 400 ${hauteur}`} role="img" aria-label={resume} preserveAspectRatio="xMinYMin meet">
      {points.map((p, i) => {
        const largeur = Math.max(2, ((400 - ETIQUETTES - 40) * p.valeur) / max)
        return (
          <g key={p.etiquette} transform={`translate(0 ${i * LIGNE})`}>
            <title>{`${p.etiquette} : ${p.valeur}`}</title>
            <rect x="0" y="0" width="400" height={LIGNE} fill="transparent" />
            <text x={ETIQUETTES - 6} y={LIGNE / 2 + 4} textAnchor="end" className="graphique-texte">{p.etiquette.slice(0, 18)}</text>
            <rect x={ETIQUETTES} y="6" width={largeur} height={LIGNE - 12} rx="4" className="graphique-barre" />
            <text x={ETIQUETTES + largeur + 6} y={LIGNE / 2 + 4} className="graphique-texte">{p.valeur}</text>
          </g>
        )
      })}
    </svg>
  )
}

function Colonnes({ points, max, resume }) {
  const largeur = Math.max(320, points.length * 56)
  const haut = 160
  const pas = largeur / points.length
  return (
    <svg className="graphique" viewBox={`0 0 ${largeur} ${haut + 24}`} role="img" aria-label={resume} preserveAspectRatio="xMinYMin meet">
      <line x1="0" x2={largeur} y1={haut} y2={haut} className="graphique-axe" />
      {points.map((p, i) => {
        const h = Math.max(2, ((haut - 20) * p.valeur) / max)
        const x = i * pas + pas * 0.2
        return (
          <g key={p.etiquette}>
            <title>{`${p.etiquette} : ${p.valeur}`}</title>
            <rect x={i * pas} y="0" width={pas} height={haut + 24} fill="transparent" />
            <rect x={x} y={haut - h} width={pas * 0.6} height={h} rx="4" className="graphique-barre" />
            <text x={x + pas * 0.3} y={haut - h - 4} textAnchor="middle" className="graphique-texte">{p.valeur}</text>
            <text x={x + pas * 0.3} y={haut + 16} textAnchor="middle" className="graphique-texte">{p.etiquette}</text>
          </g>
        )
      })}
    </svg>
  )
}
