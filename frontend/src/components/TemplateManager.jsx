import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import Modal from './Modal.jsx'
import RichText from './RichText.jsx'
import { useToast } from './Toast.jsx'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext'

const VIDE = {
  code: '', nom: '', type: '', modeSollicitation: '', categorieAcquisition: '', description: '',
  titreParDefaut: '', contexte: '', gabaritCode: '',
  clauseCodesTexte: '', annexesTexte: '',
}

// Classement par type d'acquisition (art. 7.1). Libellés rendus via t('templatemanager.cat.<code>').
const CATEGORIES_ACQUISITION = ['BIENS', 'SERVICES', 'SERVICES_PROFESSIONNELS', 'TRAVAUX_CONSTRUCTION', 'TECHNOLOGIES', 'AUTRE']

// Découpe une saisie « a, b, c » en liste nettoyée.
const liste = (s) => (s || '').split(',').map((x) => x.trim()).filter(Boolean)
const lignes = (s) => (s || '').split('\n').map((x) => x.trim()).filter(Boolean)

/**
 * Gestion dynamique des templates de documents : créer / modifier / visualiser / supprimer,
 * avec design (gabarit → police, couverture, format), titre et contexte par défaut, clauses
 * pré-sélectionnées et annexes. Composant accessible (formulaire étiqueté, modale à focus).
 */
export default function TemplateManager() {
  const toast = useToast()
  const { t } = useI18n()
  const TYPES = useReference('TYPE_CONTRAT')
  const MODES = useReference('MODE_SOLLICITATION')
  const [liste_, setListe] = useState([])
  const [gabarits, setGabarits] = useState([])
  const [edition, setEdition] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)

  const charger = useCallback(() => {
    api.get('/templates').then((r) => setListe(r.data)).catch(() => setListe([]))
  }, [])
  useEffect(() => {
    charger()
    api.get('/gabarits').then((r) => setGabarits(r.data)).catch(() => setGabarits([]))
  }, [charger])

  function ouvrirCreation() { setEdition({ ...VIDE, type: TYPES[0]?.code || '' }) }
  function ouvrirEdition(tpl) {
    setEdition({
      ...VIDE, ...Object.fromEntries(Object.entries(tpl).filter(([, v]) => v != null)),
      clauseCodesTexte: (tpl.clauseCodes || []).join(', '),
      annexesTexte: (tpl.annexes || []).join('\n'),
    })
  }

  async function confirmerSuppression() {
    try {
      await api.delete(`/templates/${aSupprimer.id}`)
      toast.succes(t('templatemanager.toastSupprime')); setASupprimer(null); charger()
    } catch { toast.erreur(t('templatemanager.toastSuppressionRefusee')) }
  }

  async function enregistrer(e) {
    e.preventDefault()
    const tpl = edition
    const corps = {
      code: tpl.code, nom: tpl.nom, type: tpl.type,
      modeSollicitation: tpl.modeSollicitation || null,
      categorieAcquisition: tpl.categorieAcquisition || null,
      description: tpl.description, titreParDefaut: tpl.titreParDefaut,
      contexte: tpl.contexte, gabaritCode: tpl.gabaritCode || null,
      clauseCodes: liste(tpl.clauseCodesTexte), annexes: lignes(tpl.annexesTexte),
    }
    try {
      if (tpl.id) await api.put(`/templates/${tpl.id}`, corps)
      else await api.post('/templates', corps)
      toast.succes(tpl.id ? t('templatemanager.toastModifie') : t('templatemanager.toastCree'))
      setEdition(null); charger()
    } catch (e2) {
      toast.erreur(e2.response?.data?.message || t('templatemanager.toastEnregistrementRefuse'))
    }
  }

  const maj = (champ) => (e) => setEdition((t) => ({ ...t, [champ]: e.target.value }))
  const nomType = (code) => TYPES.find((x) => x.code === code)?.libelle || code

  return (
    <section className="panel">
      <div className="form-legend-row">
        <div>
          <h2>🧩 {t('templatemanager.titre')}</h2>
          <p className="muted">{t('templatemanager.description')}</p>
        </div>
        <button type="button" onClick={ouvrirCreation}>{t('templatemanager.nouveau')}</button>
      </div>

      <div className="table-scroll">
        <table className="table">
          <thead>
            <tr>
              <th scope="col">{t('templatemanager.thNom')}</th>
              <th scope="col">{t('templatemanager.thCode')}</th>
              <th scope="col">{t('templatemanager.thType')}</th>
              <th scope="col">{t('templatemanager.thClauses')}</th>
              <th scope="col">{t('templatemanager.thGabarit')}</th>
              <th scope="col"><span className="sr-only">{t('templatemanager.thActions')}</span></th>
            </tr>
          </thead>
          <tbody>
            {liste_.map((tpl) => (
              <tr key={tpl.id}>
                <td>{tpl.nom}</td>
                <td><span className="code">{tpl.code}</span></td>
                <td>{nomType(tpl.type)}</td>
                <td>{tpl.clauseCodes?.length || 0}</td>
                <td>{tpl.gabaritCode || '—'}</td>
                <td className="row-actions">
                  <button type="button" className="secondary" onClick={() => ouvrirEdition(tpl)}>{t('templatemanager.modifier')}</button>
                  <button type="button" className="danger" onClick={() => setASupprimer(tpl)}>{t('templatemanager.supprimer')}</button>
                </td>
              </tr>
            ))}
            {liste_.length === 0 && <tr><td colSpan={6} className="muted">{t('templatemanager.aucun')}</td></tr>}
          </tbody>
        </table>
      </div>

      {edition && (
        <Modal titre={edition.id ? t('templatemanager.modalModifier') : t('templatemanager.modalNouveau')}
               onClose={() => setEdition(null)} taille="lg">
          <form onSubmit={enregistrer} className="form">
            <div className="form-grid">
              <label>{t('templatemanager.labelCode')}
                <input value={edition.code} onChange={maj('code')} placeholder={t('templatemanager.placeholderCode')} required />
              </label>
              <label>{t('templatemanager.labelNom')}
                <input value={edition.nom} onChange={maj('nom')} placeholder={t('templatemanager.placeholderNom')} required />
              </label>
              <label>{t('templatemanager.labelType')}
                <select value={edition.type} onChange={maj('type')} required>
                  <option value="">{t('templatemanager.optionChoisir')}</option>
                  {TYPES.map((x) => <option key={x.code} value={x.code}>{x.libelle}</option>)}
                </select>
              </label>
              <label>{t('templatemanager.labelMode')}
                <select value={edition.modeSollicitation} onChange={maj('modeSollicitation')}>
                  <option value="">{t('templatemanager.optionAucun')}</option>
                  {MODES.map((x) => <option key={x.code} value={x.code}>{x.libelle}</option>)}
                </select>
              </label>
              <label>{t('templatemanager.labelCategorie')}
                <select value={edition.categorieAcquisition} onChange={maj('categorieAcquisition')}>
                  <option value="">{t('templatemanager.optionNonClasse')}</option>
                  {CATEGORIES_ACQUISITION.map((x) => <option key={x} value={x}>{t(`templatemanager.cat.${x}`)}</option>)}
                </select>
              </label>
              <label>{t('templatemanager.labelGabarit')}
                <select value={edition.gabaritCode} onChange={maj('gabaritCode')}>
                  <option value="">{t('templatemanager.optionDeduitType')}</option>
                  {gabarits.map((g) => <option key={g.id} value={g.code}>{g.nom || g.code}</option>)}
                </select>
              </label>
              <label>{t('templatemanager.labelTitreDefaut')}
                <input value={edition.titreParDefaut} onChange={maj('titreParDefaut')} placeholder={t('templatemanager.placeholderTitreDefaut')} />
              </label>
              <label className="col-2">{t('templatemanager.labelDescription')}
                <input value={edition.description} onChange={maj('description')} placeholder={t('templatemanager.placeholderDescription')} />
              </label>
              <label className="col-2">{t('templatemanager.labelClauseCodes')}
                <input value={edition.clauseCodesTexte} onChange={maj('clauseCodesTexte')} placeholder={t('templatemanager.placeholderClauseCodes')} />
              </label>
              <label className="col-2">{t('templatemanager.labelAnnexes')}
                <textarea rows={3} value={edition.annexesTexte} onChange={maj('annexesTexte')}
                          placeholder={t('templatemanager.placeholderAnnexes')} />
              </label>
            </div>
            <label>{t('templatemanager.labelContexte')}
              <RichText value={edition.contexte} libelle={t('templatemanager.labelContexte')} onChange={(v) => setEdition({ ...edition, contexte: v })}
                        rows={6} placeholder={t('templatemanager.placeholderContexte')} />
            </label>
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setEdition(null)}>{t('templatemanager.annuler')}</button>
              <button type="submit">{edition.id ? t('templatemanager.enregistrer') : t('templatemanager.creer')}</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <Modal titre={t('templatemanager.modalSupprimer')} onClose={() => setASupprimer(null)} taille="sm">
          <p>{t('templatemanager.confirmerSuppression', { nom: aSupprimer.nom || aSupprimer.code })}</p>
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={() => setASupprimer(null)}>{t('templatemanager.annuler')}</button>
            <button type="button" className="danger" onClick={confirmerSuppression}>{t('templatemanager.supprimer')}</button>
          </div>
        </Modal>
      )}
    </section>
  )
}
