import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useToast } from '../components/Toast.jsx'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext.jsx'

const ROLES = ['APPRO', 'JURIDIQUE', 'DIRECTION', 'ADMIN', 'REDACTEUR']
const VIDE = { nom: '', type: 'APPEL_OFFRES_PUBLIC', etapes: ['APPRO', 'JURIDIQUE', 'DIRECTION'] }

/** Configuration des circuits d'approbation par type de document (art. 7.5, ADR-005). */
export default function Workflows() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const toast = useToast()
  const TYPES = useReference('TYPE_CONTRAT')
  const [liste, setListe] = useState([])
  const [form, setForm] = useState(VIDE)
  const [editId, setEditId] = useState(null)  // null = création ; sinon id en édition

  const charger = useCallback(() => {
    api.get('/workflows').then((r) => setListe(r.data)).catch(() => setListe([]))
  }, [])
  useEffect(() => { charger() }, [charger])

  if (!hasRole('ADMIN')) {
    return <div className="panel">{t('workflows.accesReserve')}</div>
  }

  function basculerRole(role) {
    setForm((f) => ({
      ...f,
      etapes: f.etapes.includes(role) ? f.etapes.filter((r) => r !== role) : [...f.etapes, role],
    }))
  }

  async function soumettre(e) {
    e.preventDefault()
    if (form.etapes.length === 0) { toast.erreur(t('workflows.errEtape')); return }
    try {
      if (editId) {
        await api.put(`/workflows/${editId}`, form)
        toast.succes(t('workflows.circuitModifie'))
      } else {
        await api.post('/workflows', form)
        toast.succes(t('workflows.circuitCree'))
      }
      annuler()
      charger()
    } catch { toast.erreur(editId ? t('workflows.modifRefusee') : t('workflows.creationRefusee')) }
  }

  function editer(w) {
    setEditId(w.id)
    setForm({ nom: w.nom, type: w.type, etapes: [...(w.etapes || [])] })
  }

  function annuler() {
    setEditId(null)
    setForm(VIDE)
  }

  async function supprimer(id) {
    try {
      await api.delete(`/workflows/${id}`)
      toast.succes(t('workflows.circuitSupprime'))
      if (editId === id) annuler()
      charger()
    } catch { toast.erreur(t('workflows.suppressionRefusee')) }
  }

  return (
    <div className="grid2">
      <section className="panel" aria-labelledby="titre-workflows">
        <h1 id="titre-workflows">{t('workflows.title')}</h1>
        <p className="muted">{t('workflows.intro')}</p>
        <table className="table">
          <caption className="sr-only">{t('workflows.caption')}</caption>
          <thead>
            <tr><th scope="col">{t('workflows.nom')}</th><th scope="col">{t('workflows.colType')}</th><th scope="col">{t('workflows.colEtapes')}</th><th scope="col">{t('workflows.colAction')}</th></tr>
          </thead>
          <tbody>
            {liste.map((w) => (
              <tr key={w.id}>
                <td>{w.nom}</td><td>{w.type}</td>
                <td>{(w.etapes || []).join(' → ')}</td>
                <td>
                  <div className="row-actions">
                    <button type="button" className="secondary"
                            onClick={() => editer(w)}
                            aria-label={t('workflows.editAria', { nom: w.nom })}>{t('workflows.editer')}</button>
                    <button type="button" className="danger"
                            onClick={() => supprimer(w.id)}
                            aria-label={t('workflows.deleteAria', { nom: w.nom })}>{t('workflows.supprimer')}</button>
                  </div>
                </td>
              </tr>
            ))}
            {liste.length === 0 && <tr><td colSpan="4" className="muted">{t('workflows.aucunCircuit')}</td></tr>}
          </tbody>
        </table>
      </section>

      <section className="panel" aria-labelledby="titre-form-workflow">
        <h2 id="titre-form-workflow">{editId ? t('workflows.modifierCircuit') : t('workflows.nouveauCircuit')}</h2>
        <form onSubmit={soumettre} className="form">
          <label>{t('workflows.nom')}
            <input value={form.nom} required onChange={(e) => setForm({ ...form, nom: e.target.value })} />
          </label>
          <label>{t('workflows.typeDoc')}
            <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              {TYPES.map((o) => <option key={o.code} value={o.code}>{o.libelle}</option>)}
            </select>
          </label>
          <fieldset>
            <legend>{t('workflows.legendEtapes')}</legend>
            {ROLES.map((r) => (
              <label key={r} className="checkbox-inline">
                <input type="checkbox" checked={form.etapes.includes(r)} onChange={() => basculerRole(r)} />
                {r} {form.etapes.includes(r) && <span className="muted">{t('workflows.niveau', { n: form.etapes.indexOf(r) + 1 })}</span>}
              </label>
            ))}
          </fieldset>
          <div className="row-actions">
            <button type="submit">{editId ? t('workflows.enregistrer') : t('workflows.creerCircuit')}</button>
            {editId && <button type="button" className="secondary" onClick={annuler}>{t('workflows.annuler')}</button>}
          </div>
        </form>
      </section>
    </div>
  )
}
