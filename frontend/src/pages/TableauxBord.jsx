import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import GraphiqueIndicateur from '../components/GraphiqueIndicateur.jsx'
import { ConfirmModal } from '../components/Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

const TYPES = { COMPTEUR: 'AUCUN', BARRES: 'STATUT', EVOLUTION: 'MOIS' }
const STATUTS = ['SOUMIS', 'EN_TRAITEMENT', 'A_COMPLETER', 'VALIDE', 'REJETE', 'CLOTURE']
const nouvelIndicateur = (formulaireId) => ({ titre: '', type: 'COMPTEUR', formulaireId, regroupement: 'AUCUN', champ: '', statuts: [] })

/**
 * Tableaux de bord configurables (CHU 6.04) : chaque personne compose les siens (compteurs, histogrammes, évolutions sur
 * les fiches des formulaires publiés) et peut les partager avec l'organisation ; seul le propriétaire les modifie.
 */
export default function TableauxBord() {
  const { t } = useI18n()
  const [liste, setListe] = useState([])
  const [courant, setCourant] = useState(null)
  const [edition, setEdition] = useState(null)
  const [formulaires, setFormulaires] = useState([])
  const [aSupprimer, setASupprimer] = useState(false)
  const [erreur, setErreur] = useState(null)

  const charger = useCallback(() => api.get('/rapport/tableaux-de-bord').then((r) => setListe(r.data)).catch(() => setListe([])), [])
  useEffect(() => {
    charger()
    api.get('/catalogue').then((r) => setFormulaires(r.data || [])).catch(() => setFormulaires([]))
  }, [charger])

  async function ouvrir(id) {
    setErreur(null); setEdition(null)
    try { setCourant((await api.get(`/rapport/tableaux-de-bord/${id}`)).data) } catch { setErreur(t('tdb.erreur')) }
  }
  async function enregistrer(e) {
    e.preventDefault(); setErreur(null)
    const corps = { ...edition, indicateurs: edition.indicateurs.map((i) => ({ ...i, champ: i.champ || null })) }
    try {
      const r = edition.id ? await api.put(`/rapport/tableaux-de-bord/${edition.id}`, corps) : await api.post('/rapport/tableaux-de-bord', corps)
      setCourant(r.data); setEdition(null); charger()
    } catch (err) { setErreur(err.response?.data?.message || t('tdb.erreur')) }
  }
  async function supprimer() {
    await api.delete(`/rapport/tableaux-de-bord/${courant.id}`); setCourant(null); charger()
  }
  const majIndicateur = (n, champ) => setEdition({ ...edition, indicateurs: edition.indicateurs.map((x, i) => (i === n ? { ...x, ...champ } : x)) })

  return (
    <div>
      <div className="panel">
        <h1>{t('tdb.titre')}</h1>
        <p className="muted">{t('tdb.intro')}</p>
        {erreur && <div className="erreur" role="alert">{erreur}</div>}
        <div className="cmp-row" style={{ flexWrap: 'wrap', gap: 8 }}>
          <label>{t('tdb.choisir')}
            <select value={courant?.id ?? ''} onChange={(e) => e.target.value && ouvrir(e.target.value)}>
              <option value="">—</option>
              {liste.map((x) => <option key={x.id} value={x.id}>{x.nom}{x.partage ? ` (${t('tdb.partage')})` : ''}</option>)}
            </select>
          </label>
          <button type="button" onClick={() => setEdition({ nom: '', partage: false, indicateurs: [nouvelIndicateur(formulaires[0]?.id)] })}>{t('tdb.nouveau')}</button>
        </div>
      </div>

      {edition && (
        <form className="panel form" onSubmit={enregistrer}>
          <h2>{edition.id ? t('tdb.modifier') : t('tdb.nouveau')}</h2>
          <label>{t('tdb.nom')}<input required maxLength={120} value={edition.nom} onChange={(e) => setEdition({ ...edition, nom: e.target.value })} /></label>
          <label className="checkbox-inline"><input type="checkbox" checked={edition.partage} onChange={(e) => setEdition({ ...edition, partage: e.target.checked })} /> {t('tdb.partager')}</label>
          {edition.indicateurs.map((ind, n) => (
            <fieldset key={n} className="panel">
              <legend>{t('tdb.indicateurN', { n: n + 1 })}</legend>
              <label>{t('tdb.titreIndicateur')}<input required maxLength={100} value={ind.titre} onChange={(e) => majIndicateur(n, { titre: e.target.value })} /></label>
              <label>{t('tdb.formulaire')}
                <select required value={ind.formulaireId ?? ''} onChange={(e) => majIndicateur(n, { formulaireId: Number(e.target.value) })}>
                  <option value="">—</option>
                  {formulaires.map((f) => <option key={f.id} value={f.id}>{f.nom}</option>)}
                </select>
              </label>
              <label>{t('tdb.type')}
                <select value={ind.type} onChange={(e) => majIndicateur(n, { type: e.target.value, regroupement: TYPES[e.target.value] })}>
                  {Object.keys(TYPES).map((x) => <option key={x} value={x}>{t(`tdb.type.${x}`)}</option>)}
                </select>
              </label>
              {ind.type === 'BARRES' && (
                <>
                  <label>{t('tdb.regroupement')}
                    <select value={ind.regroupement} onChange={(e) => majIndicateur(n, { regroupement: e.target.value })}>
                      <option value="STATUT">{t('tdb.parStatut')}</option>
                      <option value="CHAMP">{t('tdb.parReponse')}</option>
                    </select>
                  </label>
                  {ind.regroupement === 'CHAMP' && (
                    <label>{t('tdb.champ')}<input required value={ind.champ} placeholder={t('tdb.champAide')} onChange={(e) => majIndicateur(n, { champ: e.target.value })} /></label>
                  )}
                </>
              )}
              <fieldset>
                <legend>{t('tdb.statuts')}</legend>
                {STATUTS.map((s) => (
                  <label key={s} className="checkbox-inline">
                    <input type="checkbox" checked={ind.statuts.includes(s)}
                           onChange={(e) => majIndicateur(n, { statuts: e.target.checked ? [...ind.statuts, s] : ind.statuts.filter((x) => x !== s) })} />
                    {' '}{t(`statutSaisie.${s}`)}
                  </label>
                ))}
              </fieldset>
              <button type="button" className="danger" onClick={() => setEdition({ ...edition, indicateurs: edition.indicateurs.filter((_, i) => i !== n) })}>
                {t('tdb.retirerIndicateur', { n: n + 1 })}
              </button>
            </fieldset>
          ))}
          <button type="button" className="secondary" disabled={edition.indicateurs.length >= 12}
                  onClick={() => setEdition({ ...edition, indicateurs: [...edition.indicateurs, nouvelIndicateur(formulaires[0]?.id)] })}>{t('tdb.ajouterIndicateur')}</button>
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={() => setEdition(null)}>{t('modal.annuler')}</button>
            <button type="submit">{t('tdb.enregistrer')}</button>
          </div>
        </form>
      )}

      {courant && !edition && (
        <section className="panel" aria-labelledby="tdb-courant">
          <h2 id="tdb-courant">{courant.nom}</h2>
          <p className="muted">{t('tdb.proprietaire', { courriel: courant.proprietaire })}</p>
          {courant.modifiable && (
            <div className="cmp-row" style={{ gap: 8, marginBottom: 12 }}>
              <button type="button" className="secondary" onClick={() => setEdition({ id: courant.id, nom: courant.nom, partage: courant.partage,
                indicateurs: courant.indicateurs.map((x) => ({ ...x.indicateur, champ: x.indicateur.champ || '' })) })}>{t('tdb.modifier')}</button>
              <button type="button" className="danger" onClick={() => setASupprimer(true)}>{t('tdb.supprimer')}</button>
            </div>
          )}
          <div className="tableaux-bord-grille">
            {courant.indicateurs.map((x, i) => <GraphiqueIndicateur key={i} indicateur={x.indicateur} points={x.points} />)}
          </div>
        </section>
      )}
      {aSupprimer && <ConfirmModal titre={t('tdb.supprimer')} message={t('tdb.confirmerSuppression', { nom: courant.nom })} onConfirm={supprimer} onClose={() => setASupprimer(false)} />}
    </div>
  )
}
