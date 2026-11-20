import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, EditeurCondition, Message, Statut, date, dateHeure, useErreur, useListe } from './commun.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'

const TYPES = ['VALIDATION', 'APPROBATION', 'SIGNATURE']
const POLITIQUES = ['UN_REFUS', 'QUORUM_INATTEIGNABLE']
const TYPES_AFFECTATION = ['UTILISATEUR', 'GROUPE', 'ROLE', 'PARTICIPANT']

const nouvelleEtape = (etapes) => ({ cle: `etape${etapes.length + 1}`, libelle: '', phase: Math.max(0, ...etapes.map((e) => Number(e.phase))) + 1,
  type: 'VALIDATION', affectations: [], conditions: [], quorum: 1, politique: 'UN_REFUS', delaiJours: '' })

const affectationsRemplies = (affs) => (affs || []).filter((a) => String(a.valeur ?? '').trim())
  .map((a) => ({ type: a.type, valeur: String(a.valeur).trim() }))

/** Mode d'enchaînement déduit des phases : une étape par phase = séquentiel, une seule phase = parallèle, sinon mixte. */
export function modeParcours(etapes) {
  const phases = [...new Set(etapes.map((e) => Number(e.phase)))]
  if (etapes.length <= 1 || phases.length === etapes.length) return 'SEQUENTIEL'
  return phases.length === 1 ? 'PARALLELE' : 'MIXTE'
}

/** Règle d'affaires lisible d'une étape : « si Montant est supérieur à 100000 ». */
export function libelleRegle(conditions, champs, t) {
  const c = (conditions || [])[0]
  if (!c?.champ) return null
  const champ = (champs || []).find((x) => x.cle === c.champ)
  return t('clm.pc.si', { champ: champ?.libelle || c.champ, op: t(`clm.op.${c.operateur}`), valeur: c.valeur ?? '' })
}

/** Libellé lisible d'une affectation : nom du participant (par identifiant ou rôle contractuel), sinon type et valeur. */
export function libelleAffectation(a, participants, t) {
  if (a.type !== 'PARTICIPANT') return `${t(`clm.affecte.${a.type}`)} ${a.valeur}`
  const p = (participants || []).find((x) => String(x.id) === a.valeur || x.roleContrat === a.valeur)
  return p ? p.nom : `${t('clm.affecte.PARTICIPANT')} ${a.valeur}`
}

/**
 * Canvas ordonné (UI-11, zone 01) : une colonne par phase ; les étapes d'une même phase s'exécutent en parallèle.
 * Avec `onChoisir`, chaque étape devient sélectionnable pour être modifiée dans les zones 02 à 05.
 */
export function CanvasParcours({ etapes, participants, champs, choisi, onChoisir }) {
  const { t } = useI18n()
  const phases = [...new Set(etapes.map((e) => Number(e.phase)))].sort((a, b) => a - b)
  if (phases.length === 0) return <p className="muted">{t('clm.dt.aucuneEtape')}</p>
  const indexees = etapes.map((e, i) => ({ e, i }))
  return (
    <ol className="clm-canvas" aria-label={t('clm.pc.canvas')}>
      {phases.map((p) => {
        const de = indexees.filter(({ e }) => Number(e.phase) === p)
        return (
          <li key={p} className="clm-canvas__phase">
            <h3>{t('clm.pc.phaseN', { n: p })} <span className="muted">· {de.length > 1 ? t('clm.pc.parallele') : t('clm.pc.sequentiel')}</span></h3>
            <ul>
              {de.map(({ e, i }) => {
                const nom = e.libelle || e.cle
                const regle = libelleRegle(e.conditions, champs, t)
                return (
                  <li key={e.id || `${e.cle}-${i}`} className={`clm-canvas__etape clm-canvas__etape--${e.type}${onChoisir && choisi === i ? ' is-choisie' : ''}`}>
                    {onChoisir ? (
                      <button type="button" className="lien" aria-pressed={choisi === i} aria-label={t('clm.pc.choisir', { n: i + 1, nom })}
                        onClick={() => onChoisir(i)}><strong>{nom}</strong></button>
                    ) : <strong>{nom}</strong>}
                    <span className="muted"> · {t(`clm.type.${e.type}`)}</span>
                    <br /><span className="muted">{affectationsRemplies(e.affectations).map((a) => libelleAffectation(a, participants, t)).join(', ') || '—'}</span>
                    {regle && <><br /><span className="muted">{regle}</span></>}
                    {e.statut && <><br /><Statut valeur={e.statut} /></>}
                  </li>
                )
              })}
            </ul>
          </li>
        )
      })}
    </ol>
  )
}

/** Résumé lisible du parcours (UI-11, zone 06), règles d'affaires comprises. */
export function ResumeParcours({ etapes, champs }) {
  const { t } = useI18n()
  const phases = [...new Set(etapes.map((e) => Number(e.phase)))].sort((a, b) => a - b)
  if (phases.length === 0) return <p className="muted">{t('clm.dt.aucuneEtape')}</p>
  const decrire = (e) => {
    const regle = libelleRegle(e.conditions, champs, t)
    return `${e.libelle || e.cle} (${t(`clm.type.${e.type}`)}${regle ? `, ${regle}` : ''})`
  }
  return (
    <ol className="clm-chrono">
      {phases.map((p) => {
        const de = etapes.filter((e) => Number(e.phase) === p)
        return <li key={p}>{t('clm.pc.phaseN', { n: p })} — {de.length > 1 ? t('clm.pc.parallele') : t('clm.pc.sequentiel')} : {de.map(decrire).join(' + ')}</li>
      })}
    </ol>
  )
}

/** Responsables d'une étape (UI-11, zone 04) : utilisateur, groupe, rôle ou participant de la trousse. */
function EditeurAffectations({ affectations, onChange, participants, legende }) {
  const { t } = useI18n()
  const roles = useListe('CLM_ROLE_CONTRAT')
  const maj = (k, cle, v) => onChange(affectations.map((a, j) => (j === k ? { ...a, [cle]: v } : a)))
  /** Un participant se désigne parmi ceux de la trousse ; dans un modèle, par un rôle de la liste de l'organisation. */
  const champValeur = (a, k) => {
    if (a.type !== 'PARTICIPANT') return <input value={a.valeur} onChange={(x) => maj(k, 'valeur', x.target.value)} />
    if (!participants?.length) return <ChoixListe liste={roles} value={a.valeur} onChange={(v) => maj(k, 'valeur', v)} />
    return (
      <select value={a.valeur} onChange={(x) => maj(k, 'valeur', x.target.value)}>
        <option value="">—</option>
        {participants.map((p) => <option key={p.id} value={String(p.id)}>{p.nom} · {roles.libelle(p.roleContrat)}</option>)}
      </select>
    )
  }
  return (
    <fieldset className="form-fieldset">
      <legend>{legende}</legend>
      {affectations.length === 0 && <p className="muted">{t('clm.pc.aucunResponsable')}</p>}
      {affectations.map((a, k) => (
        <div key={k} className="form-grid clm-affectation">
          <label>{t('clm.pc.typeResponsable', { n: k + 1 })}
            <select value={a.type} onChange={(x) => maj(k, 'type', x.target.value)}>
              {TYPES_AFFECTATION.map((ty) => <option key={ty} value={ty}>{t(`clm.affecte.${ty}`)}</option>)}
            </select>
          </label>
          <label>{t('clm.pc.responsable', { n: k + 1 })}
            {champValeur(a, k)}
          </label>
          <button type="button" className="secondary" onClick={() => onChange(affectations.filter((_, j) => j !== k))}>
            {t('clm.pc.retirerResponsable', { n: k + 1 })}</button>
        </div>
      ))}
      <div className="cmp-row">
        <button type="button" className="secondary" onClick={() => onChange([...affectations, { type: 'UTILISATEUR', valeur: '' }])}>{t('clm.pc.ajouterResponsable')}</button>
        <button type="button" className="secondary" onClick={() => onChange([...affectations, { type: 'GROUPE', valeur: '' }])}>{t('clm.pc.affecterGroupe')}</button>
      </div>
    </fieldset>
  )
}

/**
 * Concepteur de parcours (UI-11) : l'étape choisie dans le canvas (zone 01) se règle dans les zones de la maquette —
 * type (02), phase et mode (03), responsables et règle d'affaires (04), échéance, quorum et politique de rejet (05).
 */
/** Attributs de la trousse offerts aux règles d'acheminement (addenda 4, Q8 et Q20), en plus des champs du modèle. */
export const attributsTrousse = (t) => ['typeContrat', 'unite', 'risque', 'source']
  .map((a) => ({ cle: `trousse.${a}`, libelle: t(`clm.pc.attribut.${a}`) }))

export function EditeurParcours({ etapes, onChange, champs: champsModele = [], participants, aideParticipants, modifiable = true }) {
  const { t } = useI18n()
  const champs = [...champsModele, ...attributsTrousse(t)]
  const [choisi, setChoisi] = useState(0)
  const i = Math.min(choisi, Math.max(0, etapes.length - 1))
  const e = etapes[i]
  const nom = e ? `${t('clm.pc.etapeN', { n: i + 1 })} — ${e.libelle || e.cle}` : ''
  const majEtape = (k, v) => onChange(etapes.map((x, j) => (j === i ? { ...x, [k]: v } : x)))
  const deplacer = (d) => {
    const copie = [...etapes]
    const [x] = copie.splice(i, 1)
    copie.splice(i + d, 0, x)
    onChange(copie)
    setChoisi(i + d)
  }
  const ajouter = () => { onChange([...etapes, nouvelleEtape(etapes)]); setChoisi(etapes.length) }
  const retirer = () => { onChange(etapes.filter((_, j) => j !== i)); setChoisi(Math.max(0, i - 1)) }
  const sansEtape = <p className="muted">{t('clm.dt.aucuneEtape')}</p>

  return (
    <Blocs>
      <Bloc zone="01" titre={t('clm.pc.blocCanvas')} large>
        <CanvasParcours etapes={etapes} participants={participants} champs={champs} choisi={i} onChoisir={setChoisi} />
        {modifiable && (
          <div className="cmp-row">
            <button type="button" className="secondary" onClick={ajouter}>{t('clm.pc.ajouterEtape')}</button>
            {e && (
              <>
                <button type="button" className="secondary" disabled={i === 0} onClick={() => deplacer(-1)}>{t('clm.pc.deplacerAvant', { n: i + 1 })}</button>
                <button type="button" className="secondary" disabled={i === etapes.length - 1} onClick={() => deplacer(1)}>{t('clm.pc.deplacerApres', { n: i + 1 })}</button>
                <button type="button" className="danger" onClick={retirer}>{t('clm.pc.retirerEtape', { n: i + 1 })}</button>
              </>
            )}
          </div>
        )}
      </Bloc>
      <Bloc zone="02" titre={t('clm.pc.blocTypes')}>
        {!e ? sansEtape : (
          <fieldset className="form-fieldset">
            <legend>{nom}</legend>
            <div className="form-grid">
              <label>{t('clm.pc.cle')}<input value={e.cle} onChange={(x) => majEtape('cle', x.target.value)} required /></label>
              <label>{t('clm.pc.libelle')}<input value={e.libelle} onChange={(x) => majEtape('libelle', x.target.value)} /></label>
              <label className="col-2">{t('clm.col.type')}
                <select value={e.type} onChange={(x) => majEtape('type', x.target.value)}>{TYPES.map((ty) => <option key={ty} value={ty}>{t(`clm.type.${ty}`)}</option>)}</select>
              </label>
            </div>
          </fieldset>
        )}
      </Bloc>
      <Bloc zone="03" titre={t('clm.pc.blocModes')}>
        {etapes.length > 0 && <p><strong>{t(`clm.part.mode.${modeParcours(etapes)}`)}</strong></p>}
        <p className="muted">{t('clm.pc.aide')}</p>
        {e && <label>{t('clm.pc.phase')} · {nom}<input type="number" min={1} value={e.phase} onChange={(x) => majEtape('phase', Number(x.target.value))} /></label>}
      </Bloc>
      <Bloc zone="04" titre={t('clm.pc.blocAssignation')}>
        {!e ? sansEtape : (
          <>
            {aideParticipants && <p className="muted">{aideParticipants}</p>}
            <EditeurAffectations legende={nom} affectations={e.affectations || []} participants={participants} onChange={(v) => majEtape('affectations', v)} />
            <EditeurCondition legende={t('clm.pc.regle')} aucune={t('clm.pc.toujoursRequise')} conditions={e.conditions || []} champs={champs}
              onChange={(v) => majEtape('conditions', v)} />
            <p className="muted">{champs.length ? t('clm.pc.regleAide') : t('clm.pc.regleSansModele')}</p>
          </>
        )}
      </Bloc>
      <Bloc zone="05" titre={t('clm.pc.blocEcheance')}>
        {!e ? sansEtape : (
          <fieldset className="form-fieldset">
            <legend>{nom}</legend>
            <div className="form-grid">
              <label>{t('clm.pc.delai')}<input type="number" min={1} value={e.delaiJours} onChange={(x) => majEtape('delaiJours', x.target.value)} /></label>
              <label>{t('clm.pc.quorum')}<input type="number" min={1} value={e.quorum} onChange={(x) => majEtape('quorum', Number(x.target.value))} /></label>
              <label className="col-2">{t('clm.pc.politique')}
                <select value={e.politique} onChange={(x) => majEtape('politique', x.target.value)}>{POLITIQUES.map((p) => <option key={p} value={p}>{t(`clm.politique.${p}`)}</option>)}</select>
              </label>
            </div>
          </fieldset>
        )}
      </Bloc>
      <Bloc zone="06" titre={t('clm.pc.resume')} large>
        <ResumeParcours etapes={etapes} champs={champs} />
      </Bloc>
    </Blocs>
  )
}

export const versRequete = (etapes) => etapes.map((e) => ({ cle: e.cle, libelle: e.libelle, phase: Number(e.phase), type: e.type,
  affectations: affectationsRemplies(e.affectations), quorum: e.quorum ? Number(e.quorum) : null, politique: e.politique,
  delaiJours: e.delaiJours === '' || e.delaiJours == null ? null : Number(e.delaiJours), conditions: (e.conditions || []).filter((c) => c.champ) }))

export const depuisRequete = (etapes) => (etapes || []).map((e) => ({ ...e, affectations: e.affectations || [], conditions: e.conditions || [],
  delaiJours: e.delaiJours ?? '' }))

/** Onglet parcours d'une trousse (UI-11) : conception en brouillon, réassignation à chaud en traitement (ST-08). */
export default function OngletParcours({ trousse, peutDefinir, peutReassigner, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [etapes, setEtapes] = useState(() => depuisRequete(trousse.etapes))
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [reassign, setReassign] = useState({})
  const id = trousse.resume.id
  const champs = trousse.champs || []
  const majReassign = (e, k, v) => setReassign({ ...reassign, [e.id]: { ...reassign[e.id], [k]: v } })

  async function tester() {
    setErreur(null)
    try {
      const { data } = await api.post(`/clm/trousses/${id}/parcours/tester`, versRequete(etapes))
      if (data.valide) setInfo(t('clm.pc.valide'))
      else setErreur(data.erreurs.join(' '))
    } catch (e) { setErreur(versErreur(e)) }
  }
  async function enregistrer() {
    setErreur(null)
    try { await api.put(`/clm/trousses/${id}/parcours`, versRequete(etapes)); setInfo(t('clm.pc.enregistre')); onChange() } catch (e) { setErreur(versErreur(e)) }
  }
  async function reassigner(e) {
    const r = reassign[e.id] || {}
    setErreur(null)
    try {
      await api.put(`/clm/etapes/${e.id}/affectations`, { affectations: affectationsRemplies(r.affectations ?? e.affectations),
        quorum: r.quorum ? Number(r.quorum) : null, motif: r.motif })
      setInfo(t('clm.pc.reassigne', { e: e.libelle }))
      onChange()
    } catch (x) { setErreur(versErreur(x)) }
  }

  if (peutDefinir) {
    return (
      <div data-maquette="UI-11">
        <Message erreur={erreur} info={info} />
        <EditeurParcours etapes={etapes} onChange={setEtapes} champs={champs} participants={trousse.participants} />
        <BarreActions>
          <button type="button" className="secondary" onClick={tester}>{t('clm.pc.tester')}</button>
          <button type="button" onClick={enregistrer}>{t('clm.enregistrer')}</button>
        </BarreActions>
      </div>
    )
  }
  return (
    <div data-maquette="UI-11">
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.pc.blocCanvas')} large>
          <CanvasParcours etapes={trousse.etapes} participants={trousse.participants} champs={champs} />
        </Bloc>
        <Bloc zone="02" titre={t('clm.pc.blocTypes')}>
          <ol className="clm-chrono">
            {trousse.etapes.map((e) => (
              <li key={e.id}>
                <strong>{e.libelle}</strong> ({t(`clm.type.${e.type}`)}) <Statut valeur={e.statut} />
                {e.enRetard && <> <Statut valeur="EXPIREE" /></>}
                {e.decisions.length > 0 && (
                  <ul className="clm-liste">{e.decisions.map((d, i) => <li key={i}><Statut valeur={d.nature === 'APPROUVE' ? 'APPROUVEE' : 'REJETEE'} /> {d.acteur} · {dateHeure(d.decideLe)}{d.commentaire ? ` — « ${d.commentaire} »` : ''}</li>)}</ul>
                )}
              </li>
            ))}
          </ol>
        </Bloc>
        <Bloc zone="03" titre={t('clm.pc.blocModes')}>
          {trousse.etapes.length > 0 && <p><strong>{t(`clm.part.mode.${modeParcours(trousse.etapes)}`)}</strong></p>}
          <p className="muted">{t('clm.pc.aide')}</p>
        </Bloc>
        <Bloc zone="04" titre={t('clm.pc.blocAssignation')}>
          <ul className="clm-liste">
            {trousse.etapes.map((e) => {
              const regle = libelleRegle(e.conditions, champs, t)
              return (
                <li key={e.id}>
                  <strong>{e.libelle}</strong> : {e.affectations.map((a) => libelleAffectation(a, trousse.participants, t)).join(', ')}
                  {regle && <><br /><span className="muted">{regle}</span></>}
                  {peutReassigner && ['EN_ATTENTE', 'ACTIVE'].includes(e.statut) && (
                    <details>
                      <summary>{t('clm.pc.reassigner')} — {e.libelle}</summary>
                      <div className="form clm-sousform">
                        <EditeurAffectations legende={e.libelle} affectations={reassign[e.id]?.affectations ?? e.affectations}
                          participants={trousse.participants} onChange={(v) => majReassign(e, 'affectations', v)} />
                        <label>{t('clm.pc.quorum')}<input type="number" min={1} defaultValue={e.quorum} onChange={(x) => majReassign(e, 'quorum', x.target.value)} /></label>
                        <label>{t('clm.col.motif')}<input onChange={(x) => majReassign(e, 'motif', x.target.value)} /></label>
                        <div className="cmp-row"><button type="button" onClick={() => reassigner(e)}>{t('clm.enregistrer')}</button></div>
                      </div>
                    </details>
                  )}
                </li>
              )
            })}
          </ul>
        </Bloc>
        <Bloc zone="05" titre={t('clm.pc.blocEcheance')}>
          <div className="table-scroll">
            <table className="table clm-table">
              <thead><tr><th scope="col">{t('clm.pc.libelle')}</th><th scope="col">{t('clm.pc.echeance')}</th>
                <th scope="col">{t('clm.pc.quorum')}</th><th scope="col">{t('clm.pc.politique')}</th></tr></thead>
              <tbody>
                {trousse.etapes.map((e) => (
                  <tr key={e.id}>
                    <td data-label={t('clm.pc.libelle')}>{e.libelle}</td>
                    <td data-label={t('clm.pc.echeance')}>{e.echeance ? date(e.echeance) : '—'}{e.enRetard && <> <Statut valeur="EXPIREE" /></>}</td>
                    <td data-label={t('clm.pc.quorum')}>{e.quorum}</td>
                    <td data-label={t('clm.pc.politique')}>{t(`clm.politique.${e.politique}`)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Bloc>
        <Bloc zone="06" titre={t('clm.pc.resume')} large>
          <ResumeParcours etapes={trousse.etapes} champs={champs} />
        </Bloc>
      </Blocs>
    </div>
  )
}
