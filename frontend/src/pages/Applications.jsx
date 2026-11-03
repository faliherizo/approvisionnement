import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

const ENVIRONNEMENTS = ['DEV', 'UAT', 'PROD']
const STATUTS = ['BROUILLON', 'TEST', 'ACTIF']
const VIDE = { code: '', nom: '', domaine: '', environnement: 'DEV', statut: 'BROUILLON', proprietaire: '', description: '' }

/** Catalogue d'applications / espaces fonctionnels (écran 2) : liste, création, édition, archivage. */
export default function Applications() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [liste, setListe] = useState([])
  const [domaines, setDomaines] = useState([])
  const [filtre, setFiltre] = useState('')
  const [form, setForm] = useState(VIDE)
  const [editionId, setEditionId] = useState(null)
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    const params = filtre ? { domaine: filtre } : {}
    api.get('/applications', { params }).then((r) => setListe(r.data || [])).catch(() => setListe([]))
  }, [filtre])
  useEffect(() => { if (!chargement && actif('application')) charger() }, [charger, chargement, actif])
  useEffect(() => { api.get('/reference/DOMAINE').then((r) => setDomaines(r.data || [])).catch(() => setDomaines([])) }, [])

  if (chargement) return <div className="panel">{t('applications.chargement')}</div>
  if (!actif('application')) return <div className="panel">{t('applications.moduleInactif')}</div>

  async function soumettre(e) {
    e.preventDefault(); setErr(null)
    try {
      if (editionId) await api.put(`/applications/${editionId}`, form)
      else await api.post('/applications', form)
      setForm(VIDE); setEditionId(null); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('applications.erreur')) }
  }
  function editer(a) { setEditionId(a.id); setForm({ code: a.code, nom: a.nom, domaine: a.domaine || '', environnement: a.environnement || 'DEV', statut: a.statut || 'BROUILLON', proprietaire: a.proprietaire || '', description: a.description || '' }) }
  async function archiver(a) { await api.put(`/applications/${a.id}/archiver`, null, { params: { actif: !a.actif } }); charger() }
  const libelleDomaine = (c) => domaines.find((d) => d.code === c)?.libelle || c || '—'
  const badgeStatut = (s) => s === 'ACTIF' ? 's-APPROUVE' : s === 'TEST' ? '' : 's-REFUSE'

  return (
    <div>
      <div className="panel">
        <h1>{t('applications.titre')}</h1>
        <p className="muted">{t('applications.intro')}</p>
        <label>{t('applications.filtreDomaine')}
          <select value={filtre} onChange={(e) => setFiltre(e.target.value)}>
            <option value="">{t('applications.tousDomaines')}</option>
            {domaines.map((d) => <option key={d.code} value={d.code}>{d.libelle}</option>)}
          </select>
        </label>
        <table className="table" style={{ marginTop: 12 }}>
          <thead><tr>
            <th scope="col">{t('applications.colNom')}</th>
            <th scope="col">{t('applications.colDomaine')}</th>
            <th scope="col">{t('applications.colEnv')}</th>
            <th scope="col">{t('applications.colStatut')}</th>
            <th scope="col"><span className="sr-only">{t('applications.colActions')}</span></th>
          </tr></thead>
          <tbody>
            {liste.map((a) => (
              <tr key={a.id} style={a.actif ? undefined : { opacity: 0.6 }}>
                <td><strong>{a.nom}</strong><br /><code className="muted">{a.code}</code></td>
                <td>{a.domaine ? <span className="badge">{libelleDomaine(a.domaine)}</span> : '—'}</td>
                <td><span className="badge">{a.environnement}</span></td>
                <td><span className={`badge ${badgeStatut(a.statut)}`}>{t(`applications.statut.${a.statut}`)}</span></td>
                <td className="cmp-row">
                  <button className="secondary" onClick={() => editer(a)}>{t('applications.editer')}</button>
                  <button className="secondary" onClick={() => archiver(a)}>{a.actif ? t('applications.archiver') : t('applications.reactiver')}</button>
                </td>
              </tr>
            ))}
            {liste.length === 0 && <tr><td colSpan="5" className="muted">{t('applications.aucune')}</td></tr>}
          </tbody>
        </table>
      </div>

      <section className="panel">
        <h2>{editionId ? t('applications.modifier') : t('applications.nouvelle')}</h2>
        <form onSubmit={soumettre} className="form">
          <div className="cmp-row">
            <input placeholder={t('applications.phCode')} value={form.code} required disabled={!!editionId}
                   onChange={(e) => setForm({ ...form, code: e.target.value })} />
            <input placeholder={t('applications.phNom')} value={form.nom} required
                   onChange={(e) => setForm({ ...form, nom: e.target.value })} />
          </div>
          <div className="cmp-row">
            <select value={form.domaine} onChange={(e) => setForm({ ...form, domaine: e.target.value })} aria-label={t('applications.colDomaine')}>
              <option value="">{t('applications.sansDomaine')}</option>
              {domaines.map((d) => <option key={d.code} value={d.code}>{d.libelle}</option>)}
            </select>
            <select value={form.environnement} onChange={(e) => setForm({ ...form, environnement: e.target.value })} aria-label={t('applications.colEnv')}>
              {ENVIRONNEMENTS.map((x) => <option key={x} value={x}>{x}</option>)}
            </select>
            <select value={form.statut} onChange={(e) => setForm({ ...form, statut: e.target.value })} aria-label={t('applications.colStatut')}>
              {STATUTS.map((x) => <option key={x} value={x}>{t(`applications.statut.${x}`)}</option>)}
            </select>
          </div>
          <input placeholder={t('applications.phProprietaire')} value={form.proprietaire}
                 onChange={(e) => setForm({ ...form, proprietaire: e.target.value })} />
          <textarea rows={2} placeholder={t('applications.phDescription')} value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })} />
          {err && <div className="erreur" role="alert">{err}</div>}
          <div className="cmp-row">
            <button type="submit">{editionId ? t('applications.enregistrer') : t('applications.creer')}</button>
            {editionId && <button type="button" className="secondary" onClick={() => { setForm(VIDE); setEditionId(null) }}>{t('applications.annuler')}</button>}
          </div>
        </form>
      </section>
    </div>
  )
}
