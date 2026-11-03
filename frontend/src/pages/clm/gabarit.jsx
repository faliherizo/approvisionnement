import { useId } from 'react'
import { useI18n } from '../../i18n/I18nContext'

/**
 * Gabarit d'écran des maquettes UI-01 à UI-24 : titre et objectif, blocs en grille de deux colonnes,
 * barre d'actions en pied. `data-maquette` et `data-zone` relient chaque écran à sa maquette.
 */
export function EcranClm({ maquette, titre, objectif, ariane, statut, actions, titreRef, children }) {
  const { t } = useI18n()
  return (
    <div className="clm-ecran" data-maquette={maquette}>
      <header className="clm-ecran__entete">
        {ariane && <nav className="clm-ecran__ariane" aria-label={t('clm.gab.ariane')}>{ariane}</nav>}
        <h1 ref={titreRef} tabIndex={titreRef ? -1 : undefined}>{titre}</h1>
        {objectif && <p className="clm-ecran__objectif">{objectif}</p>}
        {statut}
      </header>
      {children}
      {actions && <BarreActions>{actions}</BarreActions>}
    </div>
  )
}

export function BarreActions({ children, libelle }) {
  const { t } = useI18n()
  return <div className="clm-barre" role="group" aria-label={libelle || t('clm.gab.actions')}>{children}</div>
}

export function Blocs({ children }) {
  return <div className="clm-blocs">{children}</div>
}

/** Bloc numéroté d'une maquette ; `large` occupe les deux colonnes. */
export function Bloc({ zone, maquette, titre, large, niveau = 2, className = '', children }) {
  const id = useId()
  const Titre = niveau === 3 ? 'h3' : 'h2'
  return (
    <section className={`clm-bloc ${large ? 'clm-bloc--large' : ''} ${className}`} aria-labelledby={id} data-zone={zone} data-maquette={maquette}>
      <Titre id={id} className="clm-bloc__titre">{titre}</Titre>
      {children}
    </section>
  )
}
