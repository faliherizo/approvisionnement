import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import useReference from '../hooks/useReference.js'
import { ConfirmModal } from '../components/Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

// Classement des dossiers d'approvisionnement (art. 7.1 / 15). Libellés FR (application FR-first).
const TYPES_ACQUISITION = [
  { code: 'BIENS', cleLibelle: 'dossiers.typeBiens' },
  { code: 'SERVICES', cleLibelle: 'dossiers.typeServices' },
  { code: 'SERVICES_PROFESSIONNELS', cleLibelle: 'dossiers.typeServicesPro' },
  { code: 'TRAVAUX_CONSTRUCTION', cleLibelle: 'dossiers.typeTravaux' },
  { code: 'TECHNOLOGIES', cleLibelle: 'dossiers.typeTechnologies' },
  { code: 'AUTRE', cleLibelle: 'dossiers.typeAutre' },
]
const STATUTS = ['OUVERT', 'EN_COURS', 'EN_APPROBATION', 'ADJUGE', 'CLOTURE', 'ANNULE']
const VIDE = {
  titre: '', numeroAO: '', objet: '',
  typeAcquisition: 'SERVICES_PROFESSIONNELS', modeSollicitation: 'APPEL_OFFRES_PUBLIC', responsable: '',
}

export default function Dossiers() {
  const { t } = useI18n()
  const MODES = useReference('MODE_SOLLICITATION')
  const [dossiers, setDossiers] = useState([])
  const [selection, setSelection] = useState(null)
  const [documentsDossier, setDocumentsDossier] = useState([])
  const [rapport, setRapport] = useState(null)
  const [tousDocuments, setTousDocuments] = useState([])
  const [aRattacher, setARattacher] = useState('')
  const [refSap, setRefSap] = useState(null)
  const [form, setForm] = useState(VIDE)
  const [editId, setEditId] = useState(null)          // null = mode création ; sinon id en édition
  const [aSupprimer, setASupprimer] = useState(null)  // dossier à supprimer (confirmation)
  const navigate = useNavigate()

  const charger = useCallback(() => api.get('/dossiers').then((r) => setDossiers(r.data)), [])
  useEffect(() => {
    charger()
    api.get('/documents').then((r) => setTousDocuments(r.data))
  }, [charger])

  const ouvrir = useCallback((d) => {
    setSelection(d)
    setRefSap(null)
    api.get(`/dossiers/${d.id}/documents`).then((r) => setDocumentsDossier(r.data))
    api.get(`/dossiers/${d.id}/rapport`).then((r) => setRapport(r.data))
  }, [])

  const rafraichirDetail = useCallback((id) => {
    api.get(`/dossiers/${id}/documents`).then((r) => setDocumentsDossier(r.data))
    api.get(`/dossiers/${id}/rapport`).then((r) => setRapport(r.data))
    charger()
  }, [charger])

  async function soumettre(e) {
    e.preventDefault()
    if (editId) {
      const { data } = await api.put(`/dossiers/${editId}`, form)
      await charger()
      setSelection(data)
      annulerEdition()
    } else {
      const { data } = await api.post('/dossiers', form)
      setForm(VIDE)
      await charger()
      ouvrir(data)
    }
  }

  function editer(d) {
    setEditId(d.id)
    setForm({
      titre: d.titre || '', numeroAO: d.numeroAO || '', objet: d.objet || '',
      typeAcquisition: d.typeAcquisition || 'SERVICES_PROFESSIONNELS',
      modeSollicitation: d.modeSollicitation || 'APPEL_OFFRES_PUBLIC', responsable: d.responsable || '',
    })
  }

  function annulerEdition() {
    setEditId(null)
    setForm(VIDE)
  }

  async function supprimerDossier() {
    const id = aSupprimer.id
    await api.delete(`/dossiers/${id}`)
    setASupprimer(null)
    if (selection?.id === id) { setSelection(null); setDocumentsDossier([]); setRapport(null) }
    if (editId === id) annulerEdition()
    charger()
  }

  async function changerStatut(statut) {
    const { data } = await api.post(`/dossiers/${selection.id}/statut`, { statut })
    setSelection(data)
    charger()
  }

  async function rattacher(e) {
    e.preventDefault()
    if (!aRattacher) return
    await api.post(`/dossiers/${selection.id}/documents/${aRattacher}`, {})
    setARattacher('')
    rafraichirDetail(selection.id)
  }

  async function detacher(documentId) {
    await api.delete(`/dossiers/${selection.id}/documents/${documentId}`)
    rafraichirDetail(selection.id)
  }

  async function transmettreSap() {
    const { data } = await api.post(`/dossiers/${selection.id}/reference-sap`, {})
    setRefSap(data)
  }

  const rattachables = tousDocuments.filter((d) => !d.dossierId)

  const libelleType = (code) => {
    const type = TYPES_ACQUISITION.find((x) => x.code === code)
    return type ? t(type.cleLibelle) : '—'
  }

  return (
    <div className="grid2">
      <section className="panel" aria-labelledby="titre-dossiers">
        <h1 id="titre-dossiers">{t('dossiers.title')}</h1>
        <table className="table">
          <caption className="sr-only">{t('dossiers.tableCaption')}</caption>
          <thead>
            <tr>
              <th scope="col">{t('dossiers.thTitle')}</th><th scope="col">{t('dossiers.thTenderNumber')}</th>
              <th scope="col">{t('dossiers.thType')}</th><th scope="col">{t('dossiers.thStatus')}</th>
            </tr>
          </thead>
          <tbody>
            {dossiers.map((d) => (
              <tr key={d.id} onClick={() => ouvrir(d)} className="clickable"
                  aria-current={selection?.id === d.id ? 'true' : undefined}>
                <td>{d.titre}</td>
                <td>{d.numeroAO || '—'}</td>
                <td>{libelleType(d.typeAcquisition)}</td>
                <td><span className={`badge s-${d.statut}`}>{d.statut}</span></td>
              </tr>
            ))}
            {dossiers.length === 0 && (
              <tr><td colSpan="4" className="muted">{t('dossiers.empty')}</td></tr>
            )}
          </tbody>
        </table>

        {selection && (
          <div className="ai-card" style={{ borderStyle: 'solid', marginTop: '1rem' }}>
            <h2>{t('dossiers.detailTitle', { titre: selection.titre })}</h2>
            {rapport && (
              <ul className="stats-inline" aria-label={t('dossiers.reportAria')}>
                <li><strong>{rapport.nombreDocuments}</strong> {t('dossiers.docCount')}</li>
                <li><strong>{rapport.documentsApprouves}</strong> {t('dossiers.approvedCount')}</li>
                <li><strong>{rapport.documentsSignes}</strong> {t('dossiers.signedCount')}</li>
              </ul>
            )}
            <label>{t('dossiers.statusLabel')}
              <select value={selection.statut} onChange={(e) => changerStatut(e.target.value)}>
                {STATUTS.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </label>

            <div className="row-actions">
              <button type="button" className="secondary" onClick={() => editer(selection)}>{t('dossiers.edit')}</button>
              <button type="button" className="danger" onClick={() => setASupprimer(selection)}>
                {t('dossiers.deleteButton')}
              </button>
            </div>

            <div className="sap-ref">
              <button type="button" className="secondary" onClick={transmettreSap}>
                {t('dossiers.transmitSap')}
              </button>
              {refSap && (
                <output className="muted">
                  {refSap.succes ? '✓ ' : '✗ '}{refSap.message} — <code>{refSap.url}</code>
                </output>
              )}
            </div>

            <h3>{t('dossiers.attachedDocs')}</h3>
            <table className="table">
              <caption className="sr-only">{t('dossiers.attachedCaption')}</caption>
              <thead>
                <tr><th scope="col">{t('dossiers.thTitle')}</th><th scope="col">{t('dossiers.thStatus')}</th><th scope="col">{t('dossiers.thAction')}</th></tr>
              </thead>
              <tbody>
                {documentsDossier.map((d) => (
                  <tr key={d.id}>
                    <td><button type="button" className="link" onClick={() => navigate(`/documents/${d.id}`)}>{d.titre}</button></td>
                    <td><span className={`badge s-${d.statut}`}>{d.statut}</span></td>
                    <td><button type="button" onClick={() => detacher(d.id)}
                                aria-label={t('dossiers.detachAria', { titre: d.titre })}>{t('dossiers.detach')}</button></td>
                  </tr>
                ))}
                {documentsDossier.length === 0 && (
                  <tr><td colSpan="3" className="muted">{t('dossiers.noAttachedDoc')}</td></tr>
                )}
              </tbody>
            </table>

            <form onSubmit={rattacher} className="form">
              <label>{t('dossiers.attachExisting')}
                <select value={aRattacher} onChange={(e) => setARattacher(e.target.value)}>
                  <option value="">{t('dossiers.chooseDoc')}</option>
                  {rattachables.map((d) => <option key={d.id} value={d.id}>{d.titre}</option>)}
                </select>
              </label>
              <button type="submit" disabled={!aRattacher}>{t('dossiers.attachButton')}</button>
            </form>
          </div>
        )}
      </section>

      <section className="panel" aria-labelledby="titre-form-dossier">
        <h2 id="titre-form-dossier">{editId ? t('dossiers.editFormTitle') : t('dossiers.newFormTitle')}</h2>
        <form onSubmit={soumettre} className="form">
          <label>{t('dossiers.thTitle')}
            <input value={form.titre} required
                   onChange={(e) => setForm({ ...form, titre: e.target.value })} />
          </label>
          <label>{t('dossiers.tenderNumberLabel')}
            <input value={form.numeroAO} onChange={(e) => setForm({ ...form, numeroAO: e.target.value })} />
          </label>
          <label>{t('dossiers.objectLabel')}
            <textarea value={form.objet} rows={3}
                      onChange={(e) => setForm({ ...form, objet: e.target.value })} />
          </label>
          <label>{t('dossiers.acquisitionTypeLabel')}
            <select value={form.typeAcquisition}
                    onChange={(e) => setForm({ ...form, typeAcquisition: e.target.value })}>
              {TYPES_ACQUISITION.map((type) => <option key={type.code} value={type.code}>{t(type.cleLibelle)}</option>)}
            </select>
          </label>
          <label>{t('dossiers.solicitationModeLabel')}
            <select value={form.modeSollicitation}
                    onChange={(e) => setForm({ ...form, modeSollicitation: e.target.value })}>
              {MODES.map((m) => <option key={m.code} value={m.code}>{m.libelle}</option>)}
            </select>
          </label>
          <label>{t('dossiers.responsibleLabel')}
            <input value={form.responsable} onChange={(e) => setForm({ ...form, responsable: e.target.value })} />
          </label>
          <div className="row-actions">
            <button type="submit">{editId ? t('dossiers.saveChanges') : t('dossiers.createDossier')}</button>
            {editId && <button type="button" className="secondary" onClick={annulerEdition}>{t('dossiers.cancel')}</button>}
          </div>
        </form>
      </section>

      {aSupprimer && (
        <ConfirmModal
          titre={t('dossiers.deleteTitle')}
          message={t('dossiers.deleteConfirm', { titre: aSupprimer.titre })}
          onConfirm={supprimerDossier}
          onClose={() => setASupprimer(null)}
        />
      )}
    </div>
  )
}
