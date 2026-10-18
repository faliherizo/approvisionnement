import { useRef, useEffect } from 'react'
import { useI18n } from '../i18n/I18nContext'

/** Nettoie le HTML (allowlist) pour un affichage sûr (anti-XSS basique). */
export function sanitizeHtml(html) {
  if (!html) return ''
  const modele = document.createElement('div')
  modele.innerHTML = html
  const autorises = new Set(['B', 'I', 'U', 'EM', 'STRONG', 'UL', 'OL', 'LI', 'P', 'BR', 'DIV', 'SPAN', 'H4'])
  const parcourir = (node) => {
    ;[...node.childNodes].forEach((n) => {
      if (n.nodeType === 1) {
        if (!autorises.has(n.tagName)) {
          n.replaceWith(...n.childNodes)
        } else {
          ;[...n.attributes].forEach((a) => {
            if (a.name.startsWith('on') || a.name === 'style' || a.name === 'href') n.removeAttribute(a.name)
          })
          parcourir(n)
        }
      } else if (n.nodeType === 8) {
        n.remove()
      }
    })
  }
  parcourir(modele)
  return modele.innerHTML
}

/** Rendu HTML nettoyé (pour afficher le contenu WYSIWYG). */
export function RichHtml({ html, className }) {
  return <div className={className} dangerouslySetInnerHTML={{ __html: sanitizeHtml(html) }} />
}

/** Éditeur de texte enrichi (WYSIWYG) minimal, sans dépendance. */
export default function RichText({ value, onChange, rows = 4, placeholder }) {
  const { t } = useI18n()
  const ref = useRef(null)

  useEffect(() => {
    if (ref.current && ref.current.innerHTML !== (value || '')) {
      ref.current.innerHTML = value || ''
    }
  }, [value])

  const cmd = (commande, arg) => {
    document.execCommand(commande, false, arg)
    ref.current?.focus()
    onChange?.(ref.current?.innerHTML || '')
  }

  return (
    <div className="rte">
      <div className="rte-toolbar" role="toolbar" aria-label={t('richtext.miseEnForme')}>
        <button type="button" title={t('richtext.gras')} onClick={() => cmd('bold')}><b>B</b></button>
        <button type="button" title={t('richtext.italique')} onClick={() => cmd('italic')}><i>I</i></button>
        <button type="button" title={t('richtext.souligne')} onClick={() => cmd('underline')}><u>U</u></button>
        <span className="rte-sep" />
        <button type="button" title={t('richtext.listePuces')} onClick={() => cmd('insertUnorderedList')}>{t('richtext.btnListePuces')}</button>
        <button type="button" title={t('richtext.listeNumerotee')} onClick={() => cmd('insertOrderedList')}>{t('richtext.btnListeNumerotee')}</button>
        <span className="rte-sep" />
        <button type="button" title={t('richtext.effacerFormat')} onClick={() => cmd('removeFormat')}>{t('richtext.btnFormat')}</button>
      </div>
      <div
        ref={ref}
        className="rte-zone"
        contentEditable
        role="textbox"
        aria-multiline="true"
        data-placeholder={placeholder}
        style={{ minHeight: `${rows * 1.6}em` }}
        onInput={(e) => onChange?.(e.currentTarget.innerHTML)}
        suppressContentEditableWarning
      />
    </div>
  )
}
