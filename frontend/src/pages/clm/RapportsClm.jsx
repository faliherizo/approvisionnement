import { useState } from 'react'
import api from '../../api'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, GardeModule, Message, dateHeure, telecharger, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const VIDE = { id: null, nom: '', type: 'DELAIS', periodeJours: 30, unite: '', typeContrat: '', statut: '', colonnes: [] }
const STATUTS = ['BROUILLON', 'EN_TRAITEMENT', 'TERMINEE', 'REJETEE', 'ANNULEE']

/** Définition d'un rapport : type, période glissante, filtres, colonnes retenues dans l'ordre du type. */
function FormulaireRapport({ types, rapport, onEnregistrer, onAnnuler }) {
  const { t } = useI18n()
  const [f, setF] = useState(rapport)
  const unites = useListe('CLM_UNITE')
  const typesContrat = useListe('CLM_TYPE_CONTRAT')
  const colonnes = types.find((x) => x.type === f.type)?.colonnes || []
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const basculer = (c) => setF({ ...f, colonnes: f.colonnes.includes(c) ? f.colonnes.filter((x) => x !== c) : colonnes.filter((x) => x === c || f.colonnes.includes(x)) })
  return (
    <form className="form" aria-label={t('clm.rap.definition')} onSubmit={(e) => { e.preventDefault(); onEnregistrer(f) }}>
      <div className="form-grid">
        <label>{t('clm.rap.nom')}<input value={f.nom} onChange={maj('nom')} required maxLength={120} /></label>
        <label>{t('clm.rap.type')}
          <select value={f.type} onChange={(e) => setF({ ...f, type: e.target.value, colonnes: [] })}>
            {types.map((x) => <option key={x.type} value={x.type}>{t(`clm.rap.type.${x.type}`)}</option>)}
          </select></label>
        <label>{t('clm.rap.periode')}<input type="number" min={1} max={366} value={f.periodeJours} onChange={maj('periodeJours')} required /></label>
        <label>{t('clm.rap.statut')}
          <select value={f.statut || ''} onChange={maj('statut')}>
            <option value="">{t('clm.rap.tous')}</option>
            {STATUTS.map((s) => <option key={s} value={s}>{t(`clm.statut.${s}`)}</option>)}
          </select></label>
        <label>{t('clm.rap.unite')}<ChoixListe liste={unites} value={f.unite || ''} vide={t('clm.rap.toutes')} onChange={(v) => setF({ ...f, unite: v })} /></label>
        <label>{t('clm.rap.typeContrat')}<ChoixListe liste={typesContrat} value={f.typeContrat || ''} vide={t('clm.rap.tous')} onChange={(v) => setF({ ...f, typeContrat: v })} /></label>
      </div>
      <fieldset className="form-fieldset">
        <legend>{t('clm.rap.colonnes')}</legend>
        <p className="muted">{t('clm.rap.colonnesAide')}</p>
        {colonnes.map((c) => (
          <label key={c} className="check-row"><input type="checkbox" checked={f.colonnes.includes(c)} onChange={() => basculer(c)} />{t(`clm.rap.col.${c}`)}</label>
        ))}
      </fieldset>
      <div className="cmp-row">
        <button type="submit">{t('clm.rap.enregistrer')}</button>
        <button type="button" className="secondary" onClick={onAnnuler}>{t('clm.annuler')}</button>
      </div>
    </form>
  )
}

/**
 * Rapports personnalisables (Annexe 10, AF-37) : signatures en attente, délais moyens, processus complétés, rejets,
 * expirations, utilisation, volumétrie et incidents ; chaque rapport s'enregistre, se modifie et s'exporte (CSV, Excel).
 */
function Rapports() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const gestion = hasRole('ADMIN_AFFAIRES')
  const versErreur = useErreur()
  const { donnees: types } = useRessource('/clm/admin/rapports/types')
  const { donnees: rapports, recharger } = useRessource('/clm/admin/rapports')
  const [edition, setEdition] = useState(null)
  const [choisi, setChoisi] = useState(null)
  const [resultat, setResultat] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(ok); recharger(); return r } catch (x) { setErreur(versErreur(x)); return null }
  }
  async function enregistrer(f) {
    const corps = { ...f, periodeJours: Number(f.periodeJours), unite: f.unite || null, typeContrat: f.typeContrat || null, statut: f.statut || null }
    const r = await faire(() => (f.id ? api.put(`/clm/admin/rapports/${f.id}`, corps) : api.post('/clm/admin/rapports', corps)), t('clm.rap.enregistre', { nom: f.nom }))
    if (r) { setEdition(null); executer(r.data) }
  }
  async function executer(r) {
    setChoisi(r); setResultat(null)
    try { setResultat((await api.get(`/clm/admin/rapports/${r.id}/resultat`)).data) } catch (x) { setErreur(versErreur(x)) }
  }
  async function supprimer(r) {
    if (!window.confirm(t('clm.rap.confirmerSuppression', { nom: r.nom }))) return
    await faire(() => api.delete(`/clm/admin/rapports/${r.id}`), t('clm.rap.supprime', { nom: r.nom }))
    if (choisi?.id === r.id) { setChoisi(null); setResultat(null) }
  }
  const exporter = (r, format) => telecharger(`/clm/admin/rapports/${r.id}/export?format=${format}`, `rapport-clm-${r.id}.${format}`)
    .then(() => setInfo(t('clm.rap.exporte', { nom: r.nom, format: format.toUpperCase() }))).catch((x) => setErreur(versErreur(x)))

  return (
    <EcranClm maquette="UI-23" titre={t('clm.rap.titre')} objectif={t('clm.rap.objectif')}
      actions={gestion && <button type="button" onClick={() => setEdition({ ...VIDE })}>{t('clm.rap.nouveau')}</button>}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        {edition && types && (
          <Bloc zone="01" titre={edition.id ? t('clm.rap.modifier', { nom: edition.nom }) : t('clm.rap.nouveau')} large>
            <FormulaireRapport types={types} rapport={edition} onEnregistrer={enregistrer} onAnnuler={() => setEdition(null)} />
          </Bloc>
        )}
        <Bloc zone="02" titre={t('clm.rap.enregistres')} large>
          {!rapports ? <p role="status">{t('clm.chargement')}</p> : rapports.length === 0 ? <p className="muted">{t('clm.rap.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.rap.enregistres')}</caption>
                <thead><tr><th scope="col">{t('clm.rap.nom')}</th><th scope="col">{t('clm.rap.type')}</th><th scope="col">{t('clm.rap.periode')}</th>
                  <th scope="col">{t('clm.rap.modifieLe')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {rapports.map((r) => (
                    <tr key={r.id}>
                      <td data-label={t('clm.rap.nom')}>{r.nom}</td>
                      <td data-label={t('clm.rap.type')}>{t(`clm.rap.type.${r.type}`)}</td>
                      <td data-label={t('clm.rap.periode')}>{t('clm.rap.jours', { n: r.periodeJours })}</td>
                      <td data-label={t('clm.rap.modifieLe')}>{dateHeure(r.modifieLe)} · {r.proprietaire}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        <button type="button" className="lien" aria-label={t('clm.rap.afficherNom', { nom: r.nom })} onClick={() => executer(r)}>{t('clm.rap.afficher')}</button>
                        <button type="button" className="lien" aria-label={t('clm.rap.csvNom', { nom: r.nom })} onClick={() => exporter(r, 'csv')}>CSV</button>
                        <button type="button" className="lien" aria-label={t('clm.rap.xlsxNom', { nom: r.nom })} onClick={() => exporter(r, 'xlsx')}>Excel</button>
                        {gestion && (
                          <>
                            <button type="button" className="lien" aria-label={t('clm.rap.modifier', { nom: r.nom })} onClick={() => setEdition({ ...VIDE, ...r, unite: r.unite || '', typeContrat: r.typeContrat || '', statut: r.statut || '' })}>{t('clm.modifier')}</button>
                            <button type="button" className="lien" aria-label={t('clm.rap.supprimerNom', { nom: r.nom })} onClick={() => supprimer(r)}>{t('clm.rap.supprimer')}</button>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        {choisi && (
          <Bloc zone="03" titre={resultat?.titre || choisi.nom} large>
            {!resultat ? <p role="status">{t('clm.chargement')}</p> : resultat.lignes.length === 0 ? <p className="muted">{t('clm.rap.vide')}</p> : (
              <div className="table-scroll">
                <table className="table clm-table">
                  <caption className="sr-only">{resultat.titre}</caption>
                  <thead><tr>{choisi.colonnes.map((c) => <th key={c} scope="col">{t(`clm.rap.col.${c}`)}</th>)}</tr></thead>
                  <tbody>
                    {resultat.lignes.map((l, i) => (
                      // eslint-disable-next-line react/no-array-index-key
                      <tr key={i}>{l.map((v, j) => <td key={choisi.colonnes[j]} data-label={t(`clm.rap.col.${choisi.colonnes[j]}`)}>{v}</td>)}</tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Bloc>
        )}
      </Blocs>
    </EcranClm>
  )
}

export default function RapportsClm() {
  return <GardeModule><Rapports /></GardeModule>
}
