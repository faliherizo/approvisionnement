import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { adresseOrganisation } from '../theme.js'

const VIDE = { titre: '', accroche: '', fonctionnalites: [], video: '', sousTitres: '', affiche: '', publiee: false }
const taille = (o) => (o >= 1048576 ? `${(o / 1048576).toFixed(1)} Mo` : `${Math.max(1, Math.round(o / 1024))} Ko`)

/**
 * Site vitrine de chaque organisation (https://<organisation>.contrat.vinaitsolution.com/accueil), administré par le
 * superadministrateur : titre, accroche, fonctionnalités présentées, vidéo de présentation et ses sous-titres.
 */
export default function EditeurVitrine({ tenants }) {
  const { t } = useI18n()
  const [tenantId, setTenantId] = useState('')
  const [v, setV] = useState(VIDE)
  const [medias, setMedias] = useState([])
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')
  const [envoi, setEnvoi] = useState(false)
  const organisation = tenants.find((x) => String(x.id) === String(tenantId))

  const chargerMediatheque = () => api.get('/superadmin/mediatheque').then((r) => setMedias(r.data)).catch(() => setMedias([]))
  useEffect(() => { chargerMediatheque() }, [])
  useEffect(() => {
    setMsg(''); setErr('')
    if (!tenantId) { setV(VIDE); return }
    api.get(`/superadmin/tenants/${tenantId}/vitrine`).then((r) => setV({ ...VIDE, ...r.data,
      video: r.data.video || '', sousTitres: r.data.sousTitres || '', affiche: r.data.affiche || '', accroche: r.data.accroche || '' }))
      .catch(() => setErr(t('vitrineAdmin.erreurChargement')))
  }, [tenantId]) // eslint-disable-line react-hooks/exhaustive-deps

  const majF = (i, k, val) => setV({ ...v, fonctionnalites: v.fonctionnalites.map((f, j) => (j === i ? { ...f, [k]: val } : f)) })
  const deplacer = (i, d) => {
    const f = [...v.fonctionnalites]
    ;[f[i], f[i + d]] = [f[i + d], f[i]]
    setV({ ...v, fonctionnalites: f })
  }
  const videos = medias.filter((m) => /\.(mp4|webm)$/.test(m.chemin))
  const sousTitres = medias.filter((m) => m.chemin.endsWith('.vtt'))

  async function enregistrer(e) {
    e.preventDefault()
    setMsg(''); setErr('')
    try {
      const { data } = await api.put(`/superadmin/tenants/${tenantId}/vitrine`, v)
      setV({ ...VIDE, ...data, video: data.video || '', sousTitres: data.sousTitres || '', affiche: data.affiche || '', accroche: data.accroche || '' })
      setMsg(t('vitrineAdmin.enregistree', { organisation: organisation?.titre }))
    } catch (x) { setErr(x.response?.data?.message || x.response?.data?.erreur || t('vitrineAdmin.erreur')) }
  }
  async function televerser(e) {
    const fichier = e.target.files?.[0]
    if (!fichier) return
    setMsg(''); setErr(''); setEnvoi(true)
    try {
      const donnees = new FormData()
      donnees.append('fichier', fichier)
      const { data } = await api.post(`/superadmin/tenants/${tenantId}/vitrine/video`, donnees)
      await chargerMediatheque()
      setV((x) => ({ ...x, video: data.chemin }))
      setMsg(t('vitrineAdmin.videoDeposee', { chemin: data.chemin }))
    } catch (x) { setErr(x.response?.data?.message || t('vitrineAdmin.erreurVideo')) } finally { setEnvoi(false); e.target.value = '' }
  }

  return (
    <section className="panel" aria-labelledby="titre-editeur-vitrine">
      <h2 id="titre-editeur-vitrine">🌐 {t('vitrineAdmin.titre')}</h2>
      <p className="muted">{t('vitrineAdmin.aide')}</p>
      {msg && <p className="succes" role="status">{msg}</p>}
      {err && <p className="erreur" role="alert">{err}</p>}
      <label>{t('vitrineAdmin.organisation')}
        <select value={tenantId} onChange={(e) => setTenantId(e.target.value)}>
          <option value="">{t('vitrineAdmin.choisir')}</option>
          {tenants.map((x) => <option key={x.id} value={x.id}>{x.titre} ({x.slug})</option>)}
        </select>
      </label>
      {tenantId && (
        <form className="form" onSubmit={enregistrer} aria-label={t('vitrineAdmin.formulaire', { organisation: organisation?.titre })}>
          <label>{t('vitrineAdmin.titreSite')}<input value={v.titre} maxLength={160} required onChange={(e) => setV({ ...v, titre: e.target.value })} /></label>
          <label>{t('vitrineAdmin.accroche')}<textarea rows={3} maxLength={600} value={v.accroche} onChange={(e) => setV({ ...v, accroche: e.target.value })} /></label>
          <fieldset className="form-fieldset">
            <legend>{t('vitrineAdmin.fonctionnalites')}</legend>
            {v.fonctionnalites.length === 0 && <p className="muted">{t('vitrineAdmin.aucuneFonctionnalite')}</p>}
            <ol className="vitrine-admin-liste">
              {v.fonctionnalites.map((f, i) => (
                <li key={i}>
                  <div className="form-grid">
                    <label>{t('vitrineAdmin.fonctionnaliteTitre', { n: i + 1 })}<input value={f.titre} maxLength={120} required onChange={(e) => majF(i, 'titre', e.target.value)} /></label>
                    <label>{t('vitrineAdmin.fonctionnaliteTexte', { n: i + 1 })}<input value={f.texte || ''} maxLength={400} onChange={(e) => majF(i, 'texte', e.target.value)} /></label>
                  </div>
                  <div className="cmp-row">
                    <button type="button" className="secondary" disabled={i === 0} onClick={() => deplacer(i, -1)}>{t('vitrineAdmin.monter', { n: i + 1 })}</button>
                    <button type="button" className="secondary" disabled={i === v.fonctionnalites.length - 1} onClick={() => deplacer(i, 1)}>{t('vitrineAdmin.descendre', { n: i + 1 })}</button>
                    <button type="button" className="danger" onClick={() => setV({ ...v, fonctionnalites: v.fonctionnalites.filter((_, j) => j !== i) })}>{t('vitrineAdmin.retirer', { n: i + 1 })}</button>
                  </div>
                </li>
              ))}
            </ol>
            <button type="button" className="secondary" disabled={v.fonctionnalites.length >= 24}
              onClick={() => setV({ ...v, fonctionnalites: [...v.fonctionnalites, { titre: '', texte: '' }] })}>{t('vitrineAdmin.ajouter')}</button>
          </fieldset>
          <fieldset className="form-fieldset">
            <legend>{t('vitrineAdmin.video')}</legend>
            <div className="form-grid">
              <label>{t('vitrineAdmin.videoChoisie')}
                <select value={v.video} onChange={(e) => setV({ ...v, video: e.target.value })}>
                  <option value="">{t('vitrineAdmin.sansVideo')}</option>
                  {v.video && !videos.some((m) => m.chemin === v.video) && <option value={v.video}>{v.video} — {t('vitrineAdmin.absente')}</option>}
                  {videos.map((m) => <option key={m.chemin} value={m.chemin}>{m.chemin} ({taille(m.taille)})</option>)}
                </select>
              </label>
              <label>{t('vitrineAdmin.sousTitres')}
                <select value={v.sousTitres} onChange={(e) => setV({ ...v, sousTitres: e.target.value })}>
                  <option value="">{t('vitrineAdmin.sansSousTitres')}</option>
                  {v.sousTitres && !sousTitres.some((m) => m.chemin === v.sousTitres) && <option value={v.sousTitres}>{v.sousTitres} — {t('vitrineAdmin.absente')}</option>}
                  {sousTitres.map((m) => <option key={m.chemin} value={m.chemin}>{m.chemin}</option>)}
                </select>
              </label>
            </div>
            <label>{t('vitrineAdmin.televerser')} <span className="muted">{t('vitrineAdmin.televerserAide')}</span>
              <input type="file" accept="video/mp4,video/webm" onChange={televerser} disabled={envoi} aria-busy={envoi} />
            </label>
            {v.video && videos.some((m) => m.chemin === v.video) && (
              <video className="vitrine-admin-apercu" controls preload="metadata" src={`/api/media/${v.video}`} aria-label={t('vitrineAdmin.apercuVideo')}>
                {v.sousTitres && <track kind="captions" srcLang="fr" label="Français" src={`/api/media/${v.sousTitres}`} default />}
              </video>
            )}
          </fieldset>
          <label className="check-row"><input type="checkbox" checked={v.publiee} onChange={(e) => setV({ ...v, publiee: e.target.checked })} />{t('vitrineAdmin.publiee')}</label>
          <div className="cmp-row">
            <button type="submit">{t('vitrineAdmin.enregistrer')}</button>
            {organisation && <a href={adresseOrganisation(organisation.slug).replace(/\/login$/, '/accueil')} target="_blank" rel="noreferrer">{t('vitrineAdmin.voir')}</a>}
          </div>
        </form>
      )}
    </section>
  )
}
