import { useEffect, useId, useLayoutEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'

/**
 * Menu déroulant accessible (WCAG) : bouton déclencheur + panneau.
 * - aria-haspopup / aria-expanded, panneau étiqueté par le bouton.
 * - Ferme sur Échap (et rend le focus au bouton) et sur clic extérieur.
 * - Le contenu (liens/boutons) est fourni par l'appelant ; chaque élément
 *   interactif reste focusable au clavier.
 *
 * @param label      libellé du bouton
 * @param children   éléments du menu (reçoit une fonction `fermer` via render-prop
 *                   si children est une fonction, sinon rendu tel quel)
 * @param align      'left' | 'right' — alignement du panneau
 * @param actif      surligne le déclencheur (ex. section courante)
 */
export default function DropdownMenu({ label, children, align = 'left', actif = false, className = '' }) {
  const [ouvert, setOuvert] = useState(false)
  const conteneur = useRef(null)
  const bouton = useRef(null)
  const panneau = useRef(null)
  const [decalage, setDecalage] = useState(0)
  const menuId = useId()
  const location = useLocation()

  const fermer = () => setOuvert(false)

  // Referme le menu après une navigation (clic sur un lien du panneau).
  useEffect(() => { setOuvert(false) }, [location.pathname])

  // Le panneau ne sort jamais de l'écran : s'il dépasse un bord (téléphone, bouton près du bord), il est décalé
  // juste assez pour tenir avec une marge de 8 px. Mesuré avant affichage : aucun saut visible.
  useLayoutEffect(() => {
    if (!ouvert || !panneau.current) { setDecalage(0); return }
    const r = panneau.current.getBoundingClientRect()
    const marge = 8
    if (r.right > window.innerWidth - marge) setDecalage(window.innerWidth - marge - r.right)
    else if (r.left < marge) setDecalage(marge - r.left)
  }, [ouvert])

  useEffect(() => {
    if (!ouvert) return
    const surClicExterieur = (e) => {
      if (conteneur.current && !conteneur.current.contains(e.target)) setOuvert(false)
    }
    const surTouche = (e) => {
      if (e.key === 'Escape') { setOuvert(false); bouton.current?.focus() }
    }
    document.addEventListener('mousedown', surClicExterieur)
    document.addEventListener('keydown', surTouche)
    return () => {
      document.removeEventListener('mousedown', surClicExterieur)
      document.removeEventListener('keydown', surTouche)
    }
  }, [ouvert])

  return (
    <div className={`dropdown ${className}`} ref={conteneur}>
      <button
        ref={bouton}
        type="button"
        className={`dropdown__trigger ${actif ? 'is-actif' : ''}`}
        aria-haspopup="true"
        aria-expanded={ouvert}
        aria-controls={menuId}
        onClick={() => setOuvert((v) => !v)}
      >
        {label}
        <span className="dropdown__chevron" aria-hidden="true">▾</span>
      </button>
      {ouvert && (
        <div
          id={menuId}
          ref={panneau}
          style={decalage ? { transform: `translateX(${decalage}px)` } : undefined}
          className={`dropdown__panel dropdown__panel--${align}`}
          onClick={(e) => { if (e.target.closest('a,button')) fermer() }}
        >
          {typeof children === 'function' ? children(fermer) : children}
        </div>
      )}
    </div>
  )
}
