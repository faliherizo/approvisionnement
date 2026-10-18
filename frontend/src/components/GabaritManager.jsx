import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import Modal from './Modal.jsx'
import { useToast } from './Toast.jsx'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'

const POLICES = [
  { code: 'HELVETICA', cle: 'gabaritmanager.policeHelvetica' },
  { code: 'TIMES', cle: 'gabaritmanager.policeTimes' },
  { code: 'COURIER', cle: 'gabaritmanager.policeCourier' },
]

const CORPS = [
  { code: 'CONTRAT', cle: 'gabaritmanager.corpsContrat' },
  { code: 'BORDEREAU', cle: 'gabaritmanager.corpsBordereau' },
]

const VIDE = {
  code: '', nom: '', surLabel: "APPEL D'OFFRES - SERVICES", label: '', sousTitre: '',
  piedLabel: '', corps: 'CONTRAT', afficherCouverture: true,
  police: '', couleurTitre: '', couleurAccent: '', logoImage: '',
  margeGauche: 56, margeDroite: 56, margeHaut: 64, margeBas: 54, ordre: 0, actif: true,
}

/**
 * Gestion dynamique des gabarits de documents (catalogue global + surcharges du tenant).
 * Créer, modifier, cloner, supprimer ; configurer couverture, corps, marges et design.
 * Composant accessible : formulaire étiqueté, modale à piège de focus, aperçu en direct.
 */
export default function GabaritManager() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const superadmin = hasRole('SUPERADMIN')
  const toast = useToast()
  const [liste, setListe] = useState([])
  const [edition, setEdition] = useState(null)   // gabarit en cours d'édition, ou null
  const [surchargeDesign, setSurchargeDesign] = useState(false)
  const [aSupprimer, setASupprimer] = useState(null)

  const charger = useCallback(() => {
    api.get('/gabarits').then((r) => setListe(r.data)).catch(() => setListe([]))
  }, [])
  useEffect(() => { charger() }, [charger])

  // Un gabarit est modifiable dans le périmètre courant : global pour le superadmin,
  // propre au tenant pour un admin. Sinon on propose de le cloner pour le personnaliser.
  const modifiable = (g) => superadmin ? g.tenantId == null : g.tenantId != null

  function ouvrirCreation() {
    setEdition({ ...VIDE })
    setSurchargeDesign(false)
  }
  function ouvrirEdition(g) {
    setEdition({ ...VIDE, ...Object.fromEntries(Object.entries(g).filter(([, v]) => v != null)) })
    setSurchargeDesign(!!(g.police || g.couleurTitre || g.couleurAccent || g.logoImage))
  }

  async function cloner(g) {
    try {
      await api.post(`/gabarits/${g.id}/cloner`)
      toast.succes(t('gabaritmanager.toastClone', { nom: g.nom || g.code }))
      charger()
    } catch { toast.erreur(t('gabaritmanager.cloneRefuse')) }
  }

  async function confirmerSuppression() {
    try {
      await api.delete(`/gabarits/${aSupprimer.id}`)
      toast.succes(t('gabaritmanager.toastSupprime'))
      setASupprimer(null); charger()
    } catch { toast.erreur(t('gabaritmanager.supprRefusee')) }
  }

  function chargerLogo(e) {
    const fichier = e.target.files?.[0]
    if (!fichier) return
    if (fichier.size > 500 * 1024) { toast.erreur(t('gabaritmanager.logoVolumineux')); return }
    const reader = new FileReader()
    reader.onload = () => setEdition((g) => ({ ...g, logoImage: reader.result }))
    reader.readAsDataURL(fichier)
  }

  async function enregistrer(e) {
    e.preventDefault()
    const g = edition
    const corps = { ...g }
    if (!surchargeDesign) { corps.police = null; corps.couleurTitre = null; corps.couleurAccent = null; corps.logoImage = null }
    corps.margeGauche = Number(g.margeGauche) || 56
    corps.margeDroite = Number(g.margeDroite) || 56
    corps.margeHaut = Number(g.margeHaut) || 64
    corps.margeBas = Number(g.margeBas) || 54
    corps.ordre = Number(g.ordre) || 0
    try {
      if (g.id) await api.put(`/gabarits/${g.id}`, corps)
      else await api.post('/gabarits', corps)
      toast.succes(g.id ? t('gabaritmanager.toastModifie') : t('gabaritmanager.toastCree'))
      setEdition(null); charger()
    } catch (e2) {
      toast.erreur(e2.response?.data?.message || t('gabaritmanager.enrRefuse'))
    }
  }

  const maj = (champ) => (e) => setEdition((g) => ({ ...g, [champ]: e.target.value }))

  return (
    <section className="panel">
      <div className="form-legend-row">
        <div>
          <h2>🗂️ {t('gabaritmanager.titre')}</h2>
          <p className="muted">{superadmin ? t('gabaritmanager.descGlobal') : t('gabaritmanager.descEntreprise')}</p>
        </div>
        <button type="button" onClick={ouvrirCreation}>{t('gabaritmanager.nouveau')}</button>
      </div>

      <div className="table-scroll">
        <table className="table">
          <thead>
            <tr>
              <th scope="col">{t('gabaritmanager.thNom')}</th>
              <th scope="col">{t('gabaritmanager.thCode')}</th>
              <th scope="col">{t('gabaritmanager.thCorps')}</th>
              <th scope="col">{t('gabaritmanager.thPortee')}</th>
              <th scope="col">{t('gabaritmanager.thActif')}</th>
              <th scope="col"><span className="sr-only">{t('gabaritmanager.thActions')}</span></th>
            </tr>
          </thead>
          <tbody>
            {liste.map((g) => (
              <tr key={g.id}>
                <td>{g.nom || '—'}</td>
                <td><span className="code">{g.code}</span></td>
                <td>{g.corps === 'BORDEREAU' ? t('gabaritmanager.corpsBordereauCourt') : t('gabaritmanager.corpsContratCourt')}</td>
                <td><span className={`tag ${g.tenantId == null ? 't-STANDARD' : 't-OPTIONNELLE'}`}>{g.tenantId == null ? t('gabaritmanager.porteeGlobal') : t('gabaritmanager.porteeEntreprise')}</span></td>
                <td>{g.actif ? t('gabaritmanager.oui') : t('gabaritmanager.non')}</td>
                <td className="row-actions">
                  {modifiable(g) ? (
                    <>
                      <button type="button" className="secondary" onClick={() => ouvrirEdition(g)}>{t('gabaritmanager.modifier')}</button>
                      <button type="button" className="danger" onClick={() => setASupprimer(g)}>{t('gabaritmanager.supprimer')}</button>
                    </>
                  ) : (
                    <button type="button" className="secondary" onClick={() => cloner(g)}>{t('gabaritmanager.personnaliser')}</button>
                  )}
                </td>
              </tr>
            ))}
            {liste.length === 0 && <tr><td colSpan={6} className="muted">{t('gabaritmanager.aucun')}</td></tr>}
          </tbody>
        </table>
      </div>

      {edition && (
        <Modal titre={edition.id ? t('gabaritmanager.modalTitreModifier') : t('gabaritmanager.modalTitreNouveau')} onClose={() => setEdition(null)} taille="lg">
          <form onSubmit={enregistrer} className="form">
            <div className="form-grid">
              <label>{t('gabaritmanager.labelCode')}
                <input value={edition.code} onChange={maj('code')} placeholder={t('gabaritmanager.phCode')} required />
              </label>
              <label>{t('gabaritmanager.labelNom')}
                <input value={edition.nom} onChange={maj('nom')} placeholder={t('gabaritmanager.phNom')} />
              </label>
              <label>{t('gabaritmanager.labelSurLabel')}
                <input value={edition.surLabel} onChange={maj('surLabel')} placeholder={t('gabaritmanager.phSurLabel')} />
              </label>
              <label>{t('gabaritmanager.labelPrincipal')}
                <input value={edition.label} onChange={maj('label')} placeholder={t('gabaritmanager.phContrat')} />
              </label>
              <label>{t('gabaritmanager.labelSousTitre')}
                <input value={edition.sousTitre} onChange={maj('sousTitre')} placeholder={t('gabaritmanager.phSousTitre')} />
              </label>
              <label>{t('gabaritmanager.labelPied')}
                <input value={edition.piedLabel} onChange={maj('piedLabel')} placeholder={t('gabaritmanager.phContrat')} />
              </label>
              <label>{t('gabaritmanager.labelCorps')}
                <select value={edition.corps} onChange={maj('corps')}>
                  {CORPS.map((c) => <option key={c.code} value={c.code}>{t(c.cle)}</option>)}
                </select>
              </label>
              <label>{t('gabaritmanager.labelOrdre')}
                <input type="number" value={edition.ordre} onChange={maj('ordre')} />
              </label>
              <label className="check-row col-2">
                <input type="checkbox" checked={edition.afficherCouverture}
                       onChange={(e) => setEdition({ ...edition, afficherCouverture: e.target.checked })} />
                {t('gabaritmanager.afficherCouverture')}
              </label>
            </div>

            <fieldset className="form-fieldset">
              <legend>{t('gabaritmanager.legendMarges')}</legend>
              <div className="form-grid form-grid--4">
                <label>{t('gabaritmanager.margeGauche')}<input type="number" value={edition.margeGauche} onChange={maj('margeGauche')} /></label>
                <label>{t('gabaritmanager.margeDroite')}<input type="number" value={edition.margeDroite} onChange={maj('margeDroite')} /></label>
                <label>{t('gabaritmanager.margeHaut')}<input type="number" value={edition.margeHaut} onChange={maj('margeHaut')} /></label>
                <label>{t('gabaritmanager.margeBas')}<input type="number" value={edition.margeBas} onChange={maj('margeBas')} /></label>
              </div>
            </fieldset>

            <label className="check-row">
              <input type="checkbox" checked={surchargeDesign} onChange={(e) => setSurchargeDesign(e.target.checked)} />
              {t('gabaritmanager.surchargeDesign')}
            </label>
            {surchargeDesign && (
              <div className="form-grid">
                <label>{t('gabaritmanager.labelPolice')}
                  <select value={edition.police || 'HELVETICA'} onChange={maj('police')}>
                    {POLICES.map((p) => <option key={p.code} value={p.code}>{t(p.cle)}</option>)}
                  </select>
                </label>
                <label>{t('gabaritmanager.labelLogo')}
                  <input type="file" accept="image/png,image/jpeg" onChange={chargerLogo} />
                </label>
                <label>{t('gabaritmanager.couleurTitres')}
                  <input type="color" value={edition.couleurTitre || '#0b4a8a'} onChange={maj('couleurTitre')} />
                </label>
                <label>{t('gabaritmanager.couleurAccent')}
                  <input type="color" value={edition.couleurAccent || '#0b4a8a'} onChange={maj('couleurAccent')} />
                </label>
              </div>
            )}

            <div className="gabarit-apercu" aria-label={t('gabaritmanager.apercuCouverture')}
                 style={{ borderTopColor: surchargeDesign ? (edition.couleurTitre || '#0b4a8a') : '#0b4a8a' }}>
              <span className="muted">{edition.surLabel}</span>
              <strong style={{ color: surchargeDesign ? (edition.couleurTitre || '#0b4a8a') : '#0b4a8a' }}>{edition.label || t('gabaritmanager.libelleFallback')}</strong>
              {edition.sousTitre && <em className="muted">({edition.sousTitre})</em>}
            </div>

            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setEdition(null)}>{t('gabaritmanager.annuler')}</button>
              <button type="submit">{edition.id ? t('gabaritmanager.enregistrer') : t('gabaritmanager.creer')}</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre={t('gabaritmanager.modalSupprTitre')} onClose={() => setASupprimer(null)} taille="sm">
          <p>{t('gabaritmanager.confirmerSuppression')} « <strong>{aSupprimer.nom || aSupprimer.code}</strong> » ?</p>
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={() => setASupprimer(null)}>{t('gabaritmanager.annuler')}</button>
            <button type="button" className="danger" onClick={confirmerSuppression}>{t('gabaritmanager.supprimer')}</button>
          </div>
        </Modal>
      )}
    </section>
  )
}
