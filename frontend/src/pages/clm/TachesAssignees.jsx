import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, Message, Statut, date, useErreur, useListe, usePeutInitier, useRessource } from './commun.jsx'
import { Bloc } from './gabarit.jsx'

const VUES = ['assignees', 'creees', 'terminees']
const VIDE = { titre: '', description: '', assigneA: '', priorite: '', echeance: '', trousseId: '' }

/** Création d'une tâche confiée à une personne de l'organisation, choisie dans son annuaire. */
function NouvelleTache({ membres, onCreee, onFermer }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const priorites = useListe('CLM_PRIORITE_TACHE')
  const trousses = useRessource('/clm/trousses?taille=200')
  const [f, setF] = useState(VIDE)
  const [erreur, setErreur] = useState(null)
  const titre = useRef(null)
  useEffect(() => { titre.current?.focus() }, [])
  const maj = (cle) => (v) => setF({ ...f, [cle]: v })

  async function creer(e) {
    e.preventDefault()
    setErreur(null)
    try {
      await api.post('/clm/taches-assignees', {
        ...f, trousseId: f.trousseId ? Number(f.trousseId) : null, echeance: f.echeance || null, priorite: f.priorite || null,
      })
      onCreee(t('clm.tx.creee', { qui: f.assigneA }))
    } catch (x) { setErreur(versErreur(x)) }
  }

  const listeTrousses = trousses.donnees?.lignes || []
  return (
    <form className="form clm-sousform" onSubmit={creer} aria-labelledby="clm-tx-nouvelle">
      <h3 id="clm-tx-nouvelle">{t('clm.tx.nouvelle')}</h3>
      <Message erreur={erreur} />
      <div className="form-grid">
        <label>{t('clm.tx.titreTache')}<input ref={titre} value={f.titre} maxLength={200} required onChange={(e) => maj('titre')(e.target.value)} /></label>
        <label>{t('clm.tx.assigneA')}
          <select value={f.assigneA} required onChange={(e) => maj('assigneA')(e.target.value)}>
            <option value="">{t('clm.tx.choisirPersonne')}</option>
            {membres.map((m) => <option key={m.courriel} value={m.courriel}>{m.nom ? `${m.nom} · ${m.courriel}` : m.courriel}</option>)}
          </select>
        </label>
        <label>{t('clm.tx.priorite')}<ChoixListe liste={priorites} value={f.priorite} onChange={maj('priorite')} vide={t('clm.tx.sansPriorite')} /></label>
        <label>{t('clm.col.echeance')}<input type="date" value={f.echeance} onChange={(e) => maj('echeance')(e.target.value)} /></label>
        <label>{t('clm.tx.trousse')}
          <select value={f.trousseId} onChange={(e) => maj('trousseId')(e.target.value)}>
            <option value="">{t('clm.tx.sansTrousse')}</option>
            {listeTrousses.map((x) => <option key={x.id} value={x.id}>{x.reference} — {x.titre}</option>)}
          </select>
        </label>
      </div>
      <label>{t('clm.tx.description')}<textarea rows={3} maxLength={2000} value={f.description} onChange={(e) => maj('description')(e.target.value)} /></label>
      <div className="cmp-row">
        <button type="submit">{t('clm.tx.confier')}</button>
        <button type="button" className="secondary" onClick={onFermer}>{t('clm.annuler')}</button>
      </div>
    </form>
  )
}

/** Réassignation à une autre personne de l'organisation, motif facultatif. */
function Reassigner({ tache, membres, onFait, onFermer }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [vers, setVers] = useState('')
  const [motif, setMotif] = useState('')
  const [erreur, setErreur] = useState(null)
  const champ = useRef(null)
  useEffect(() => { champ.current?.focus() }, [])
  async function envoyer(e) {
    e.preventDefault()
    setErreur(null)
    try { await api.post(`/clm/taches-assignees/${tache.id}/reassigner`, { vers, motif }); onFait(t('clm.tx.reassignee', { qui: vers })) } catch (x) { setErreur(versErreur(x)) }
  }
  return (
    <form className="form clm-sousform" onSubmit={envoyer} aria-label={`${t('clm.tx.reassigner')} — ${tache.titre}`}>
      <Message erreur={erreur} />
      <div className="form-grid">
        <label>{t('clm.tx.nouvelAssigne')}
          <select ref={champ} value={vers} required onChange={(e) => setVers(e.target.value)}>
            <option value="">{t('clm.tx.choisirPersonne')}</option>
            {membres.filter((m) => m.courriel !== tache.assigneA).map((m) => <option key={m.courriel} value={m.courriel}>{m.nom ? `${m.nom} · ${m.courriel}` : m.courriel}</option>)}
          </select>
        </label>
        <label>{t('clm.col.motif')}<input value={motif} maxLength={300} onChange={(e) => setMotif(e.target.value)} /></label>
      </div>
      <div className="cmp-row">
        <button type="submit" className="secondary">{t('clm.tx.confirmerReassignation')}</button>
        <button type="button" className="secondary" onClick={onFermer}>{t('clm.annuler')}</button>
      </div>
    </form>
  )
}

/** Boîte de tâches, zone « Tâches assignées » : ce qu'on m'a confié, ce que j'ai confié, ce que j'ai terminé. */
export default function TachesAssignees() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const peutCreer = usePeutInitier()
  const priorites = useListe('CLM_PRIORITE_TACHE')
  const [vue, setVue] = useState('assignees')
  const [creation, setCreation] = useState(false)
  const [aReassigner, setAReassigner] = useState(null)
  const [info, setInfo] = useState('')
  const [erreur, setErreur] = useState(null)
  const liste = useRessource(`/clm/taches-assignees?filtre=${vue}`)
  const membres = useRessource('/clm/membres').donnees || []
  const taches = liste.donnees || []

  const fini = (msg) => { setInfo(msg); setCreation(false); setAReassigner(null); liste.recharger() }
  async function agir(x, action) {
    setErreur(null)
    try { await api.post(`/clm/taches-assignees/${x.id}/action`, { action }); fini(t(`clm.tx.ok.${action}`, { titre: x.titre })) } catch (e) { setErreur(versErreur(e)) }
  }

  return (
    <Bloc zone="05" titre={t('clm.tx.bloc')} large>
      <p className="muted">{t('clm.tx.aide')}</p>
      <Message erreur={erreur || liste.erreur} info={liste.chargement ? t('clm.chargement') : info} onReessayer={liste.recharger} />
      <div className="cmp-row clm-tx-barre">
        <div className="clm-onglets" role="group" aria-label={t('clm.tx.vues')}>
          {VUES.map((v) => (
            <button key={v} type="button" aria-pressed={vue === v} className={vue === v ? 'is-actif' : ''} onClick={() => { setVue(v); setInfo('') }}>
              {t(`clm.tx.vue.${v}`)}
            </button>
          ))}
        </div>
        {peutCreer && !creation && <button type="button" onClick={() => { setCreation(true); setInfo('') }}>{t('clm.tx.nouvelle')}</button>}
      </div>
      {creation && <NouvelleTache membres={membres} onCreee={fini} onFermer={() => setCreation(false)} />}
      {liste.donnees && taches.length === 0 ? <p className="muted">{t(`clm.tx.aucune.${vue}`)}</p> : (
        <div className="table-scroll">
          <table className="table clm-table">
            <caption className="sr-only">{t('clm.tx.bloc')} — {t(`clm.tx.vue.${vue}`)}</caption>
            <thead><tr><th scope="col">{t('clm.tx.titreTache')}</th><th scope="col">{vue === 'creees' ? t('clm.tx.assigneA') : t('clm.tx.creePar')}</th>
              <th scope="col">{t('clm.tx.priorite')}</th><th scope="col">{t('clm.col.echeance')}</th><th scope="col">{t('clm.col.statut')}</th>
              <th scope="col">{t('clm.col.actions')}</th></tr></thead>
            <tbody>
              {taches.map((x) => (
                <tr key={x.id}>
                  <td data-label={t('clm.tx.titreTache')}>{x.titre}
                    {x.trousseId && <><br /><Link to={`/clm/trousses/${x.trousseId}`}>{x.reference}</Link></>}
                    {x.description && <><br /><span className="muted">{x.description}</span></>}</td>
                  <td data-label={vue === 'creees' ? t('clm.tx.assigneA') : t('clm.tx.creePar')}>{vue === 'creees' ? x.assigneA : x.creePar}</td>
                  <td data-label={t('clm.tx.priorite')}>{x.priorite ? priorites.libelle(x.priorite) : '—'}</td>
                  <td data-label={t('clm.col.echeance')}>{date(x.echeance && `${x.echeance}T12:00:00`)} {x.enRetard && <Statut valeur="EXPIREE" />}</td>
                  <td data-label={t('clm.col.statut')}><Statut valeur={x.statut} /></td>
                  <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                    {x.peutDemarrer && <button type="button" className="lien" aria-label={`${t('clm.tx.demarrer')} — ${x.titre}`} onClick={() => agir(x, 'DEMARRER')}>{t('clm.tx.demarrer')}</button>}
                    {['A_FAIRE', 'EN_COURS'].includes(x.statut) && <>
                      <button type="button" className="lien" aria-label={`${t('clm.tx.terminer')} — ${x.titre}`} onClick={() => agir(x, 'TERMINER')}>{t('clm.tx.terminer')}</button>
                      <button type="button" className="lien" aria-label={`${t('clm.tx.reassigner')} — ${x.titre}`} onClick={() => setAReassigner(x)}>{t('clm.tx.reassigner')}</button>
                    </>}
                    {x.statut === 'TERMINEE' && <button type="button" className="lien" aria-label={`${t('clm.tx.rouvrir')} — ${x.titre}`} onClick={() => agir(x, 'ROUVRIR')}>{t('clm.tx.rouvrir')}</button>}
                    {x.peutAnnuler && <button type="button" className="lien" aria-label={`${t('clm.tx.annulerTache')} — ${x.titre}`} onClick={() => agir(x, 'ANNULER')}>{t('clm.tx.annulerTache')}</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {aReassigner && <Reassigner tache={aReassigner} membres={membres} onFait={fini} onFermer={() => setAReassigner(null)} />}
    </Bloc>
  )
}
