import { useRef, useEffect, useState } from 'react'
import { useI18n } from '../i18n/I18nContext'

/**
 * Pavé de signature : dessin à la main (souris / tactile) sur un canvas,
 * OU téléversement d'une image de signature. Produit une data URL (PNG base64)
 * transmise via onChange.
 */
export default function SignaturePad({ onChange, width = 460, height = 160 }) {
  const { t } = useI18n()
  const canvasRef = useRef(null)
  const dessine = useRef(false)
  const [vide, setVide] = useState(true)

  useEffect(() => {
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    ctx.lineWidth = 2.2
    ctx.lineCap = 'round'
    ctx.strokeStyle = '#0b2540'
  }, [])

  const position = (e) => {
    const r = canvasRef.current.getBoundingClientRect()
    const p = e.touches ? e.touches[0] : e
    return { x: (p.clientX - r.left) * (canvasRef.current.width / r.width),
             y: (p.clientY - r.top) * (canvasRef.current.height / r.height) }
  }

  const commencer = (e) => {
    e.preventDefault()
    dessine.current = true
    const ctx = canvasRef.current.getContext('2d')
    const { x, y } = position(e)
    ctx.beginPath()
    ctx.moveTo(x, y)
  }
  const tracer = (e) => {
    if (!dessine.current) return
    e.preventDefault()
    const ctx = canvasRef.current.getContext('2d')
    const { x, y } = position(e)
    ctx.lineTo(x, y)
    ctx.stroke()
  }
  const finir = () => {
    if (!dessine.current) return
    dessine.current = false
    setVide(false)
    onChange?.(canvasRef.current.toDataURL('image/png'))
  }

  function effacer() {
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    ctx.fillStyle = '#fff'
    ctx.fillRect(0, 0, canvas.width, canvas.height)
    setVide(true)
    onChange?.('')
  }

  function televerser(e) {
    const fichier = e.target.files?.[0]
    if (!fichier) return
    const reader = new FileReader()
    reader.onload = () => {
      const img = new Image()
      img.onload = () => {
        const canvas = canvasRef.current
        const ctx = canvas.getContext('2d')
        ctx.fillStyle = '#fff'
        ctx.fillRect(0, 0, canvas.width, canvas.height)
        // Ajuste l'image dans le canvas en conservant les proportions.
        const ratio = Math.min(canvas.width / img.width, canvas.height / img.height)
        const w = img.width * ratio, h = img.height * ratio
        ctx.drawImage(img, (canvas.width - w) / 2, (canvas.height - h) / 2, w, h)
        setVide(false)
        onChange?.(canvas.toDataURL('image/png'))
      }
      img.src = reader.result
    }
    reader.readAsDataURL(fichier)
  }

  return (
    <div className="sign-pad">
      <canvas
        ref={canvasRef}
        width={width}
        height={height}
        className="sign-canvas"
        role="img" aria-label={t('signaturepad.zone')}
        onMouseDown={commencer} onMouseMove={tracer} onMouseUp={finir} onMouseLeave={finir}
        onTouchStart={commencer} onTouchMove={tracer} onTouchEnd={finir}
      />
      <div className="sign-actions">
        <button type="button" className="secondary" onClick={effacer}>{t('signaturepad.effacer')}</button>
        <label className="secondary sign-upload">
          {t('signaturepad.televerser')}
          <input type="file" accept="image/*" onChange={televerser} hidden />
        </label>
        <span className="muted">{vide ? t('signaturepad.invite') : t('signaturepad.prete')}</span>
      </div>
    </div>
  )
}
