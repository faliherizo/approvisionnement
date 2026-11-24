import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'
import useReference from '../hooks/useReference.js'
import Modal, { ConfirmModal } from '../components/Modal.jsx'
import { RichHtml } from '../components/RichText.jsx'

const CATEGORIES = ['STANDARD', 'OPTIONNELLE', 'PERSONNALISEE']

export default function Search() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const TYPES = useReference('TYPE_CONTRAT')
  const navigate = useNavigate()
  const [q, setQ] = useState('')
  const [categorie, setCategorie] = useState('')
  const [type, setType] = useState('')
  const [thematique, setThematique] = useState('')
  const [normative, setNormative] = useState(false)
  const [thematiques, setThematiques] = useState([])
  const [res, setRes] = useState(null)
  const [consulter, setConsulter] = useState(null)   // clause consultée (lecture seule)
  const [aSupprimer, setASupprimer] = useState(null) // { type, id, libelle }

  const peutSupprimerClause = hasRole('ADMIN') || hasRole('JURIDIQUE')
  const peutSupprimerDoc = hasRole('ADMIN') || hasRole('REDACTEUR')

  useEffect(() => {
    api.get('/search/thematiques').then(({ data }) => setThematiques(data)).catch(() => setThematiques([]))
  }, [])

  async function rechercher(e) {
    e?.preventDefault()
    const params = { q }
    if (categorie) params.categorie = categorie
    if (type) params.type = type
    if (thematique) params.thematique = thematique
    if (normative) params.normative = true
    const { data } = await api.get('/search', { params })
    setRes(data)
  }

  async function supprimer() {
    const { type, id } = aSupprimer
    await api.delete(type === 'clause' ? `/clauses/${id}` : `/documents/${id}`)
    setASupprimer(null)
    rechercher()
  }

  return (
    <div className="panel">
      <h1>{t('search.titre')}</h1>
      <form onSubmit={rechercher} className="form" style={{ maxWidth: 480 }}>
        <label>{t('search.terme')}
          <input value={q} onChange={(e) => setQ(e.target.value)}
                 placeholder={t('search.termePlaceholder')} />
        </label>
        <label>{t('search.categorieFacette')}
          <select value={categorie} onChange={(e) => setCategorie(e.target.value)}>
            <option value="">{t('search.toutesCategories')}</option>
            {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
        </label>
        <label>{t('search.typeFacette')}
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">{t('search.tousTypes')}</option>
            {TYPES.map((tp) => <option key={tp.code} value={tp.code}>{tp.libelle}</option>)}
          </select>
        </label>
        <label>{t('search.thematique')}
          <select value={thematique} onChange={(e) => setThematique(e.target.value)}>
            <option value="">{t('search.toutesThematiques')}</option>
            {thematiques.map((th) => <option key={th} value={th}>{th}</option>)}
          </select>
        </label>
        <label className="checkbox-inline">
          <input type="checkbox" checked={normative} onChange={(e) => setNormative(e.target.checked)} />
          {t('search.normativesSeulement')}
        </label>
        <button type="submit">{t('search.rechercher')}</button>
      </form>

      {res && (
        <div style={{ marginTop: 16 }}>
          <p className="muted">{t('search.resultats', { n: res.total, terme: res.terme })}</p>

          {res.facettesCategorieClause && Object.keys(res.facettesCategorieClause).length > 0 && (
            <ul className="stats-inline" aria-label={t('search.facettesAria')}>
              {Object.entries(res.facettesCategorieClause).map(([cat, n]) => (
                <li key={cat}><strong>{n}</strong> {cat}</li>
              ))}
            </ul>
          )}

          {res.facettesThematique && Object.keys(res.facettesThematique).length > 0 && (
            <ul className="stats-inline" aria-label={t('search.facettesThematiqueAria')}>
              {Object.entries(res.facettesThematique).map(([th, n]) => (
                <li key={th}><strong>{n}</strong> {th}</li>
              ))}
            </ul>
          )}

          <h2>{t('search.clauses', { n: res.clauses.length })}</h2>
          <div className="clause-list">
            {res.clauses.map((c) => (
              <article key={c.id} className="clause">
                <header>
                  <span className="code">{c.code}</span>
                  <span className={`tag t-${c.categorie}`}>{c.categorie}</span>
                </header>
                <h4>{c.titre}</h4>
                <RichHtml html={c.corps} />
                <div className="row-actions">
                  <button className="secondary" onClick={() => setConsulter(c)}>{t('search.consulter')}</button>
                  <button className="secondary" onClick={() => navigate(`/clauses?edit=${c.id}`)}>{t('search.editer')}</button>
                  {peutSupprimerClause && (
                    <button className="danger"
                            onClick={() => setASupprimer({ type: 'clause', id: c.id, libelle: `${c.code} — ${c.titre}` })}>
                      {t('search.supprimer')}
                    </button>
                  )}
                </div>
              </article>
            ))}
            {res.clauses.length === 0 && <p className="muted">{t('search.aucuneClause')}</p>}
          </div>

          <h2>{t('search.documents', { n: res.documents.length })}</h2>
          <div className="table-wrap">
            <table className="table">
              <thead><tr><th scope="col">{t('search.thTitre')}</th><th scope="col">{t('search.thType')}</th><th scope="col">{t('search.thStatut')}</th><th scope="col" className="col-actions">{t('search.thActions')}</th></tr></thead>
              <tbody>
                {res.documents.map((d) => (
                  <tr key={d.id}>
                    <td>{d.titre}</td><td>{d.type}</td>
                    <td><span className={`badge s-${d.statut}`}>{d.statut}</span></td>
                    <td>
                      <div className="row-actions">
                        <button className="secondary" onClick={() => navigate(`/documents/${d.id}`)}>{t('search.ouvrir')}</button>
                        {peutSupprimerDoc && (
                          <button className="danger"
                                  onClick={() => setASupprimer({ type: 'document', id: d.id, libelle: d.titre })}>
                            {t('search.supprimer')}
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {res.documents.length === 0 && <tr><td colSpan="4" className="muted">{t('search.aucunDocument')}</td></tr>}
              </tbody>
            </table>
          </div>

          <h2>{t('search.modeles', { n: res.templates ? res.templates.length : 0 })}</h2>
          <div className="table-wrap">
            <table className="table">
              <caption className="sr-only">{t('search.modelesCaption')}</caption>
              <thead><tr><th scope="col">{t('search.thCode')}</th><th scope="col">{t('search.thNom')}</th><th scope="col">{t('search.thType')}</th><th scope="col">{t('search.thCategorieAcquisition')}</th></tr></thead>
              <tbody>
                {(res.templates || []).map((tpl) => (
                  <tr key={tpl.id}>
                    <td>{tpl.code}</td><td>{tpl.nom}</td><td>{tpl.type}</td>
                    <td>{tpl.categorieAcquisition || '—'}</td>
                  </tr>
                ))}
                {(!res.templates || res.templates.length === 0) && (
                  <tr><td colSpan="4" className="muted">{t('search.aucunModele')}</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {consulter && (
        <Modal titre={`${consulter.code} — ${consulter.titre}`} onClose={() => setConsulter(null)} taille="lg">
          <div className="meta">
            <span className={`tag t-${consulter.categorie}`}>{consulter.categorie}</span>
            {consulter.numero && <span className="muted"> · n° {consulter.numero}</span>}
            {consulter.section && <span className="muted"> · {consulter.section}</span>}
          </div>
          <RichHtml html={consulter.corps} />
          <div className="modal-actions">
            <button className="secondary" onClick={() => setConsulter(null)}>{t('search.fermer')}</button>
            <button onClick={() => navigate(`/clauses?edit=${consulter.id}`)}>{t('search.editerBibliotheque')}</button>
          </div>
        </Modal>
      )}

      {aSupprimer && (
        <ConfirmModal
          titre={aSupprimer.type === 'clause' ? t('search.supprimerClause') : t('search.supprimerDocument')}
          message={t('search.confirmerSuppression', { libelle: aSupprimer.libelle })}
          onConfirm={supprimer}
          onClose={() => setASupprimer(null)}
        />
      )}
    </div>
  )
}
