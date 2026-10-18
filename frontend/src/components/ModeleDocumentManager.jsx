import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const POLICES = ['HELVETICA', 'TIMES', 'COURIER']

const VIDE = {
  police: 'HELVETICA', couleurTitre: '#0b4a8a', couleurAccent: '#0b4a8a',
  marque: '', piedDePage: '', afficherCouverture: true, logoImage: '',
}

/**
 * Gestionnaire du modèle de document (gabarit de rendu PDF/DOCX) propre à l'entreprise.
 * Utilisé côté admin (modèle du tenant) et superadmin (modèle global par défaut).
 */
export default function ModeleDocumentManager({ portee = 'admin' }) {
  const { t } = useI18n()
  const superadmin = portee === 'superadmin'
  const [modele, setModele] = useState(VIDE)
  const [tenants, setTenants] = useState([])
  const [cible, setCible] = useState('')   // '' = gabarit global ; sinon id du tenant
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  // Endpoint selon la portée : admin = son tenant ; superadmin = tenant choisi ou global.
  const baseUrl = () => (superadmin && cible) ? `/superadmin/tenants/${cible}/modele-document` : '/modele-document'

  useEffect(() => {
    if (superadmin) api.get('/superadmin/tenants').then((r) => setTenants(r.data))
  }, [superadmin])

  useEffect(() => {
    setMsg(null); setErr(null)
    api.get(baseUrl()).then((r) => setModele({ ...VIDE, ...nettoyer(r.data) }))
  }, [cible]) // eslint-disable-line react-hooks/exhaustive-deps

  // N'écrase pas les valeurs par défaut avec des null renvoyés par l'API.
  const nettoyer = (d) => Object.fromEntries(Object.entries(d || {}).filter(([, v]) => v != null))

  function chargerLogo(e) {
    const fichier = e.target.files?.[0]
    if (!fichier) return
    if (fichier.size > 500 * 1024) { setErr(t('modeledocumentmanager.logoTooLarge')); return }
    const reader = new FileReader()
    reader.onload = () => setModele((m) => ({ ...m, logoImage: reader.result }))
    reader.readAsDataURL(fichier)
  }

  async function enregistrer(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    try {
      await api.put(baseUrl(), {
        police: modele.police, couleurTitre: modele.couleurTitre, couleurAccent: modele.couleurAccent,
        marque: modele.marque, piedDePage: modele.piedDePage,
        afficherCouverture: modele.afficherCouverture, logoImage: modele.logoImage,
      })
      setMsg(t('modeledocumentmanager.saved'))
    } catch (e2) {
      setErr(e2.response?.data?.message || t('modeledocumentmanager.saveDenied'))
    }
  }

  const sousTitre = superadmin ? t('modeledocumentmanager.scopeTenant') : t('modeledocumentmanager.scopeCompany')

  return (
    <section className="panel">
      <h2>📄 {t('modeledocumentmanager.title')} {sousTitre}</h2>
      <p className="muted">{t('modeledocumentmanager.description')}</p>
      {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur">{err}</div>}

      <form onSubmit={enregistrer} className="form">
        <div className="form-grid">
          {superadmin && (
            <label className="col-2">{t('modeledocumentmanager.tenant')}
              <select value={cible} onChange={(e) => setCible(e.target.value)}>
                <option value="">{t('modeledocumentmanager.globalTemplate')}</option>
                {tenants.map((tn) => <option key={tn.id} value={tn.id}>{tn.titre} ({tn.slug})</option>)}
              </select>
            </label>
          )}
          <label>{t('modeledocumentmanager.font')}
            <select value={modele.police} onChange={(e) => setModele({ ...modele, police: e.target.value })}>
              {POLICES.map((code) => <option key={code} value={code}>{t(`modeledocumentmanager.font_${code}`)}</option>)}
            </select>
          </label>
          <label>{t('modeledocumentmanager.brand')}
            <input value={modele.marque} placeholder={t('modeledocumentmanager.brandPlaceholder')} onChange={(e) => setModele({ ...modele, marque: e.target.value })} />
          </label>
          <label>{t('modeledocumentmanager.titleColor')}
            <input type="color" value={modele.couleurTitre} onChange={(e) => setModele({ ...modele, couleurTitre: e.target.value })} />
          </label>
          <label>{t('modeledocumentmanager.accentColor')}
            <input type="color" value={modele.couleurAccent} onChange={(e) => setModele({ ...modele, couleurAccent: e.target.value })} />
          </label>
          <label className="col-2">{t('modeledocumentmanager.footer')}
            <input value={modele.piedDePage} placeholder={t('modeledocumentmanager.footerPlaceholder')} onChange={(e) => setModele({ ...modele, piedDePage: e.target.value })} />
          </label>
          <label className="col-2">{t('modeledocumentmanager.logo')}
            <input type="file" accept="image/png,image/jpeg" onChange={chargerLogo} />
          </label>
          <label className="check-row col-2">
            <input type="checkbox" checked={modele.afficherCouverture}
                   onChange={(e) => setModele({ ...modele, afficherCouverture: e.target.checked })} /> {t('modeledocumentmanager.showCover')}
          </label>
        </div>

        {modele.logoImage && (
          <div className="logo-apercu">
            <img src={modele.logoImage} alt={t('modeledocumentmanager.logoAlt')} />
            <button type="button" className="secondary" onClick={() => setModele({ ...modele, logoImage: '' })}>
              {t('modeledocumentmanager.removeLogo')}
            </button>
          </div>
        )}

        <div className="modele-apercu" style={{ borderTopColor: modele.couleurTitre }}>
          {modele.logoImage && <img className="theme-logo" src={modele.logoImage} alt={t('modeledocumentmanager.logoShortAlt')} />}
          <strong style={{ color: modele.couleurTitre, fontFamily: apercuFont(modele.police) }}>{t('modeledocumentmanager.previewLabel')}</strong>
          <span className="modele-accent" style={{ background: modele.couleurAccent }} />
          {modele.piedDePage && <em className="muted">{modele.piedDePage}</em>}
        </div>

        <button type="submit">{t('modeledocumentmanager.saveModel')}</button>
      </form>
    </section>
  )
}

function apercuFont(police) {
  if (police === 'TIMES') return 'Times New Roman, serif'
  if (police === 'COURIER') return 'Courier New, monospace'
  return 'Helvetica, Arial, sans-serif'
}
