import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, GardeModule, Message, Statut, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const SORTS = ['DETRUIRE', 'VERSER', 'CONSERVER']

/**
 * Calendrier de conservation et destruction au moment opportun (Cahier § 1.4.11, § 1.4.12 ; Loi sur les archives) :
 * règles par type de contrat, trousses closes et leur échéance, gel légal, versement et destruction confirmés.
 */
function EcranConservation() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const types = useListe('CLM_TYPE_CONTRAT')
  const { donnees: regles, recharger: rechargerRegles } = useRessource('/clm/admin/conservation/regles')
  const [echues, setEchues] = useState(false)
  const { donnees: suivi, recharger } = useRessource(`/clm/admin/conservation/trousses?echues=${echues}`)
  const [f, setF] = useState({ typeContrat: '', dureeAns: 7, sortFinal: 'DETRUIRE' })
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { await fn(); setInfo(ok); recharger(); rechargerRegles(); return true } catch (x) { setErreur(versErreur(x)); return false }
  }
  const definir = (e) => {
    e.preventDefault()
    faire(() => api.put('/clm/admin/conservation/regles', { ...f, dureeAns: Number(f.dureeAns) }), t('clm.co.regleEnregistree'))
  }
  const geler = (l) => {
    const motif = window.prompt(t('clm.co.motifGel'))
    if (motif) faire(() => api.post(`/clm/admin/conservation/trousses/${l.trousseId}/gel`, { motif }), t('clm.co.gele', { ref: l.reference }))
  }
  const detruire = (l) => {
    if (window.confirm(t('clm.co.confirmerDestruction', { ref: l.reference }))) {
      faire(() => api.post(`/clm/admin/conservation/trousses/${l.trousseId}/detruire`), t('clm.co.detruite', { ref: l.reference }))
    }
  }
  const libelleType = (c) => (c ? types.libelle(c) : t('clm.co.defaut'))

  return (
    <EcranClm titre={t('clm.co.titre')} objectif={t('clm.co.objectif')}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc titre={t('clm.co.calendrier')} large>
          {regles && regles.length > 0 ? (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.co.calendrier')}</caption>
                <thead><tr><th scope="col">{t('clm.champ.typeContrat')}</th><th scope="col">{t('clm.co.duree')}</th><th scope="col">{t('clm.co.sort')}</th>
                  <th scope="col">{t('clm.co.modifie')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {regles.map((r) => (
                    <tr key={r.id}>
                      <td data-label={t('clm.champ.typeContrat')}>{libelleType(r.typeContrat)}</td>
                      <td data-label={t('clm.co.duree')}>{t('clm.co.ans', { n: r.dureeAns })}</td>
                      <td data-label={t('clm.co.sort')}>{t(`clm.co.sort.${r.sortFinal}`)}</td>
                      <td data-label={t('clm.co.modifie')}>{r.modifiePar} · {dateHeure(r.modifieLe)}</td>
                      <td data-label={t('clm.col.actions')}>
                        <button type="button" className="lien danger" aria-label={`${t('clm.retirer')} — ${libelleType(r.typeContrat)}`}
                          onClick={() => faire(() => api.delete(`/clm/admin/conservation/regles/${r.id}`), t('clm.co.regleRetiree'))}>{t('clm.retirer')}</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : <p className="muted">{t('clm.co.aucuneRegle')}</p>}
          <form className="form clm-sousform" onSubmit={definir} aria-label={t('clm.co.definir')}>
            <div className="form-grid">
              <label>{t('clm.champ.typeContrat')}
                <ChoixListe liste={types} value={f.typeContrat} onChange={(v) => setF({ ...f, typeContrat: v })} vide={t('clm.co.defaut')} /></label>
              <label>{t('clm.co.dureeAns')}<input type="number" min={1} max={99} value={f.dureeAns} onChange={(e) => setF({ ...f, dureeAns: e.target.value })} required /></label>
              <label>{t('clm.co.sort')}
                <select value={f.sortFinal} onChange={(e) => setF({ ...f, sortFinal: e.target.value })}>
                  {SORTS.map((s) => <option key={s} value={s}>{t(`clm.co.sort.${s}`)}</option>)}
                </select>
              </label>
            </div>
            <div className="cmp-row"><button type="submit">{t('clm.co.definir')}</button></div>
          </form>
        </Bloc>
        <Bloc titre={t('clm.co.closes')} large>
          <label className="check-row"><input type="checkbox" checked={echues} onChange={(e) => setEchues(e.target.checked)} />{t('clm.co.echuesSeulement')}</label>
          {!suivi ? <p role="status">{t('clm.chargement')}</p> : suivi.length === 0 ? <p className="muted">{t('clm.co.aucune')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.co.closes')}</caption>
                <thead><tr><th scope="col">{t('clm.col.reference')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.co.cloture')}</th>
                  <th scope="col">{t('clm.co.echeance')}</th><th scope="col">{t('clm.co.sort')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {suivi.map((l) => (
                    <tr key={l.trousseId}>
                      <td data-label={t('clm.col.reference')}><Link to={`/clm/trousses/${l.trousseId}?onglet=preuve`}>{l.reference}</Link><br /><span className="muted">{l.titre}</span></td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={l.statut} /></td>
                      <td data-label={t('clm.co.cloture')}>{dateHeure(l.clotureLe)}</td>
                      <td data-label={t('clm.co.echeance')}>{l.echeance || t('clm.co.sansEcheance')}{l.echue && <><br /><strong>{t('clm.co.echue')}</strong></>}
                        {l.gelLegal && <><br /><span>{t('clm.co.gelEnCours', { motif: l.gelMotif })}</span></>}</td>
                      <td data-label={t('clm.co.sort')}>{l.sortFinal ? t(`clm.co.sort.${l.sortFinal}`) : '—'}{l.verseeLe && <><br /><span className="muted">{t('clm.co.verseeLe', { d: dateHeure(l.verseeLe) })}</span></>}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        {l.gelLegal
                          ? <button type="button" className="lien" aria-label={`${t('clm.co.leverGel')} — ${l.reference}`}
                            onClick={() => faire(() => api.delete(`/clm/admin/conservation/trousses/${l.trousseId}/gel`), t('clm.co.gelLeve', { ref: l.reference }))}>{t('clm.co.leverGel')}</button>
                          : <button type="button" className="lien" aria-label={`${t('clm.co.geler')} — ${l.reference}`} onClick={() => geler(l)}>{t('clm.co.geler')}</button>}
                        {l.echue && l.sortFinal === 'VERSER' && !l.verseeLe && (
                          <button type="button" className="lien" aria-label={`${t('clm.co.marquerVersee')} — ${l.reference}`}
                            onClick={() => faire(() => api.post(`/clm/admin/conservation/trousses/${l.trousseId}/versee`), t('clm.co.versee', { ref: l.reference }))}>{t('clm.co.marquerVersee')}</button>
                        )}
                        {l.echue && (l.sortFinal === 'DETRUIRE' || l.verseeLe) && (
                          <button type="button" className="lien danger" aria-label={`${t('clm.co.detruire')} — ${l.reference}`} onClick={() => detruire(l)}>{t('clm.co.detruire')}</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function ConservationClm() {
  return <GardeModule><EcranConservation /></GardeModule>
}
