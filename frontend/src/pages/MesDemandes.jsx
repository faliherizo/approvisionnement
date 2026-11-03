import { useEffect, useState, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

function formatDate(iso) { return iso ? new Date(iso).toLocaleDateString('fr-CA') : '—' }

// Regroupement des statuts en 4 sections du portail (maquettes §4.1).
const SECTIONS = [
  { cle: 'brouillons', statuts: ['BROUILLON'] },
  { cle: 'soumises', statuts: ['SOUMIS', 'EN_TRAITEMENT'] },
  { cle: 'aCompleter', statuts: ['A_COMPLETER'] },
  { cle: 'terminees', statuts: ['VALIDE', 'REJETE', 'CLOTURE', 'ANNULE'] },
]

/** Portail « Mes demandes » : les demandes de l'utilisateur, regroupées par état, avec actions contextuelles. */
export default function MesDemandes() {
  const { t } = useI18n()
  const navigate = useNavigate()
  const { actif, chargement } = useModules()
  const [demandes, setDemandes] = useState([])
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/mes-demandes').then((r) => setDemandes(r.data || [])).catch(() => setDemandes([]))
  }, [])
  useEffect(() => { if (!chargement && actif('saisie')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('demandes.chargement')}</div>
  if (!actif('saisie')) return <div className="panel">{t('demandes.moduleInactif')}</div>

  async function annuler(d) {
    if (!window.confirm(t('demandes.confirmerAnnulation'))) return
    setErr(null)
    try { await api.post(`/formulaires/${d.formulaireId}/saisies/${d.id}/annuler`); charger() }
    catch (e) { setErr(e.response?.data?.message || t('demandes.erreur')) }
  }

  const badge = (s) => s === 'VALIDE' || s === 'CLOTURE' ? 's-APPROUVE' : (s === 'REJETE' || s === 'ANNULE') ? 's-REFUSE' : ''

  function actions(d) {
    if (d.statut === 'BROUILLON') return (
      <>
        <button className="secondary" onClick={() => navigate(`/formulaires/${d.formulaireId}/remplir?saisie=${d.id}`)}>{t('demandes.reprendre')}</button>
        <button className="danger" onClick={() => annuler(d)}>{t('demandes.annuler')}</button>
      </>
    )
    if (d.statut === 'A_COMPLETER') return (
      <button onClick={() => navigate(`/formulaires/${d.formulaireId}/remplir?saisie=${d.id}`)}>{t('demandes.completer')}</button>
    )
    return <Link className="btn-lien" to={`/formulaires/${d.formulaireId}/fiches`}>{t('demandes.details')}</Link>
  }

  return (
    <div>
      <div className="panel">
        <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
          <h1>{t('demandes.titre')}</h1>
          <Link className="btn-lien" to="/nouvelle-demande">+ {t('demandes.nouvelle')}</Link>
        </div>
        <p className="muted">{t('demandes.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      {SECTIONS.map((sec) => {
        const items = demandes.filter((d) => sec.statuts.includes(d.statut))
        return (
          <section className="panel" key={sec.cle}>
            <h2>{t(`demandes.section.${sec.cle}`)} <span className="badge">{items.length}</span></h2>
            <table className="table">
              <thead><tr>
                <th scope="col">{t('demandes.colNumero')}</th>
                <th scope="col">{t('demandes.colFormulaire')}</th>
                <th scope="col">{t('demandes.colDomaine')}</th>
                <th scope="col">{t('demandes.colStatut')}</th>
                <th scope="col">{t('demandes.colMaj')}</th>
                <th scope="col"><span className="sr-only">{t('demandes.colActions')}</span></th>
              </tr></thead>
              <tbody>
                {items.map((d) => (
                  <tr key={d.id}>
                    <td><code>{d.numero || d.id}</code></td>
                    <td>{d.formulaireNom || '—'}</td>
                    <td>{d.domaine ? <span className="badge">{d.domaine}</span> : <span className="muted">—</span>}</td>
                    <td><span className={`badge ${badge(d.statut)}`}>{t(`statutSaisie.${d.statut}`)}</span></td>
                    <td>{formatDate(d.majLe)}</td>
                    <td className="cmp-row">{actions(d)}</td>
                  </tr>
                ))}
                {items.length === 0 && <tr><td colSpan="6" className="muted">{t('demandes.aucune')}</td></tr>}
              </tbody>
            </table>
          </section>
        )
      })}
    </div>
  )
}
