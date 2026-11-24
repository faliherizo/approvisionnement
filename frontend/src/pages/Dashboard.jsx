import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import RichText from '../components/RichText.jsx'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext'

export default function Dashboard() {
  const { t } = useI18n()
  const TYPES = useReference('TYPE_CONTRAT')
  const MODES = useReference('MODE_SOLLICITATION')
  const [documents, setDocuments] = useState([])
  const [templates, setTemplates] = useState([])
  const [tpl, setTpl] = useState({ id: '', titre: '', valeurEstimee: '', contexte: '' })
  const [form, setForm] = useState({
    titre: '', type: 'APPEL_OFFRES_PUBLIC',
    modeSollicitation: 'APPEL_OFFRES_PUBLIC', valeurEstimee: '', contexte: '',
    numeroAO: '', organismePublic: '', prestataire: ''
  })
  const navigate = useNavigate()

  const charger = () => api.get('/documents').then((r) => setDocuments(r.data))
  useEffect(() => {
    charger()
    api.get('/templates').then((r) => setTemplates(r.data))
  }, [])

  async function creer(e) {
    e.preventDefault()
    const payload = { ...form, valeurEstimee: form.valeurEstimee || null }
    const { data } = await api.post('/documents', payload)
    navigate(`/documents/${data.id}`)
  }

  async function instancier(e) {
    e.preventDefault()
    if (!tpl.id) return
    const { data } = await api.post(`/templates/${tpl.id}/instancier`, {
      titre: tpl.titre, valeurEstimee: tpl.valeurEstimee || null, contexte: tpl.contexte
    })
    navigate(`/documents/${data.id}`)
  }

  return (
    <div className="grid2">
      <section className="panel">
        <h1>{t('dashboard.contractDocuments')}</h1>
        <table className="table">
          <thead>
            <tr><th scope="col">{t('dashboard.colTitle')}</th><th scope="col">{t('dashboard.colType')}</th><th scope="col">{t('dashboard.colMode')}</th><th scope="col">{t('dashboard.colStatus')}</th></tr>
          </thead>
          <tbody>
            {documents.map((d) => (
              <tr key={d.id} onClick={() => navigate(`/documents/${d.id}`)} className="clickable">
                <td>{d.titre}</td>
                <td>{d.type}</td>
                <td>{d.modeSollicitation || '—'}</td>
                <td><span className={`badge s-${d.statut}`}>{d.statut}</span></td>
              </tr>
            ))}
            {documents.length === 0 && (
              <tr><td colSpan="4" className="muted">{t('dashboard.emptyDocuments')}</td></tr>
            )}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h2>{t('dashboard.newDocument')}</h2>
        <form onSubmit={creer} className="form">
          <label>{t('dashboard.title')}
            <input value={form.titre} required
                   onChange={(e) => setForm({ ...form, titre: e.target.value })} />
          </label>
          <label>{t('dashboard.contractType')}
            <select value={form.type} required onChange={(e) => setForm({ ...form, type: e.target.value })}>
              {TYPES.map((ty) => <option key={ty.code} value={ty.code}>{ty.libelle}</option>)}
            </select>
          </label>
          <label>{t('dashboard.solicitationMode')}
            <select value={form.modeSollicitation} required
                    onChange={(e) => setForm({ ...form, modeSollicitation: e.target.value })}>
              {MODES.map((m) => <option key={m.code} value={m.code}>{m.libelle}</option>)}
            </select>
          </label>
          <label>{t('dashboard.estimatedValue')}
            <input type="number" value={form.valeurEstimee}
                   onChange={(e) => setForm({ ...form, valeurEstimee: e.target.value })} />
          </label>
          <label>{t('dashboard.projectContext')}
            <RichText value={form.contexte} rows={3} libelle={t('dashboard.projectContext')}
                      onChange={(contexte) => setForm((f) => ({ ...f, contexte }))} />
          </label>
          <label>{t('dashboard.tenderNumber')}
            <input value={form.numeroAO} onChange={(e) => setForm({ ...form, numeroAO: e.target.value })} />
          </label>
          <label>{t('dashboard.publicBody')}
            <input value={form.organismePublic} onChange={(e) => setForm({ ...form, organismePublic: e.target.value })} />
          </label>
          <label>{t('dashboard.contractor')}
            <input value={form.prestataire} onChange={(e) => setForm({ ...form, prestataire: e.target.value })} />
          </label>
          <button type="submit">{t('dashboard.createDocument')}</button>
        </form>

        <div className="ai-card" style={{ borderStyle: 'solid' }}>
          <h2>📄 {t('dashboard.createFromTemplate')}</h2>
          <form onSubmit={instancier} className="form">
            <label>{t('dashboard.template')}
              <select value={tpl.id} onChange={(e) => setTpl({ ...tpl, id: e.target.value })}>
                <option value="">{t('dashboard.chooseTemplate')}</option>
                {templates.map((tp) => <option key={tp.id} value={tp.id}>{tp.nom} ({tp.code})</option>)}
              </select>
            </label>
            <label>{t('dashboard.docTitle')}
              <input value={tpl.titre} required
                     onChange={(e) => setTpl({ ...tpl, titre: e.target.value })} />
            </label>
            <label>{t('dashboard.estimatedValue')}
              <input type="number" value={tpl.valeurEstimee}
                     onChange={(e) => setTpl({ ...tpl, valeurEstimee: e.target.value })} />
            </label>
            <button type="submit" disabled={!tpl.id}>{t('dashboard.instantiateTemplate')}</button>
          </form>
        </div>
      </section>
    </div>
  )
}
