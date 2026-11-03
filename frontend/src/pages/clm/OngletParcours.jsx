import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { Message, Statut, dateHeure, useErreur } from './commun.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'

const TYPES = ['VALIDATION', 'APPROBATION', 'SIGNATURE']
const POLITIQUES = ['UN_REFUS', 'QUORUM_INATTEIGNABLE']

export const versTexte = (affs) => (affs || []).map((a) => `${a.type}:${a.valeur}`).join(', ')
export const versAffectations = (texte) => texte.split(',').map((s) => s.trim()).filter(Boolean).map((s) => {
  const i = s.indexOf(':')
  return i < 0 ? { type: 'UTILISATEUR', valeur: s } : { type: s.slice(0, i).trim().toUpperCase(), valeur: s.slice(i + 1).trim() }
})
export const nouvelleEtape = (etapes) => ({ cle: `etape${etapes.length + 1}`, libelle: '', phase: Math.max(0, ...etapes.map((e) => Number(e.phase))) + 1,
  type: 'VALIDATION', affectationsTexte: '', quorum: 1, politique: 'UN_REFUS', delaiJours: '' })

/** Libellé lisible d'une affectation : nom du participant (par identifiant ou rôle contractuel), sinon type et valeur. */
export function libelleAffectation(a, participants, t) {
  if (a.type !== 'PARTICIPANT') return `${t(`clm.affecte.${a.type}`)} ${a.valeur}`
  const p = (participants || []).find((x) => String(x.id) === a.valeur || x.roleContrat === a.valeur)
  return p ? p.nom : `${t('clm.affecte.PARTICIPANT')} ${a.valeur}`
}

/** Canvas ordonné (UI-11, zone 01) : une colonne par phase ; les étapes d'une même phase s'exécutent en parallèle. */
export function CanvasParcours({ etapes, participants }) {
  const { t } = useI18n()
  const phases = [...new Set(etapes.map((e) => Number(e.phase)))].sort((a, b) => a - b)
  if (phases.length === 0) return <p className="muted">{t('clm.dt.aucuneEtape')}</p>
  return (
    <ol className="clm-canvas" aria-label={t('clm.pc.canvas')}>
      {phases.map((p) => {
        const de = etapes.filter((e) => Number(e.phase) === p)
        return (
          <li key={p} className="clm-canvas__phase">
            <h3>{t('clm.pc.phaseN', { n: p })} <span className="muted">· {de.length > 1 ? t('clm.pc.parallele') : t('clm.pc.sequentiel')}</span></h3>
            <ul>
              {de.map((e, j) => (
                <li key={e.id || `${e.cle}-${j}`} className={`clm-canvas__etape clm-canvas__etape--${e.type}`}>
                  <strong>{e.libelle || e.cle}</strong> <span className="muted">· {t(`clm.type.${e.type}`)}</span>
                  <br /><span className="muted">{e.affectations ? e.affectations.map((a) => libelleAffectation(a, participants, t)).join(', ') : e.affectationsTexte || '—'}</span>
                  {e.statut && <><br /><Statut valeur={e.statut} /></>}
                </li>
              ))}
            </ul>
          </li>
        )
      })}
    </ol>
  )
}

/** Résumé lisible du parcours (UI-11, zone 06). */
export function ResumeParcours({ etapes }) {
  const { t } = useI18n()
  const phases = [...new Set(etapes.map((e) => Number(e.phase)))].sort((a, b) => a - b)
  if (phases.length === 0) return <p className="muted">{t('clm.dt.aucuneEtape')}</p>
  return (
    <ol className="clm-chrono">
      {phases.map((p) => {
        const de = etapes.filter((e) => Number(e.phase) === p)
        return <li key={p}>{t('clm.pc.phaseN', { n: p })} — {de.length > 1 ? t('clm.pc.parallele') : t('clm.pc.sequentiel')} : {de.map((e) => `${e.libelle || e.cle} (${t(`clm.type.${e.type}`)})`).join(' + ')}</li>
      })}
    </ol>
  )
}

/** Éditeur de parcours (UI-11) : canvas, étapes (type, assignation, échéance, quorum, politique de rejet), résumé. */
export function EditeurParcours({ etapes, onChange, aideParticipants }) {
  const { t } = useI18n()
  const majEtape = (i, k, v) => onChange(etapes.map((e, j) => (j === i ? { ...e, [k]: v } : e)))
  const deplacer = (i, d) => {
    const copie = [...etapes]
    const [x] = copie.splice(i, 1)
    copie.splice(i + d, 0, x)
    onChange(copie)
  }

  return (
    <Blocs>
      <Bloc zone="01" titre={t('clm.pc.blocCanvas')} large>
        <CanvasParcours etapes={etapes} />
      </Bloc>
      <Bloc zone="02-05" titre={t('clm.pc.blocEtapes')} large>
        <p className="muted">{t('clm.pc.aide')}</p>
        {aideParticipants && <p className="muted">{aideParticipants}</p>}
        {etapes.map((e, i) => (
          <fieldset key={i} className="form-fieldset clm-etape-edit">
            <legend>{t('clm.pc.etapeN', { n: i + 1 })} — {e.libelle || e.cle}</legend>
            <div className="form-grid form-grid--4">
              <label>{t('clm.pc.cle')}<input value={e.cle} onChange={(x) => majEtape(i, 'cle', x.target.value)} required /></label>
              <label>{t('clm.pc.libelle')}<input value={e.libelle} onChange={(x) => majEtape(i, 'libelle', x.target.value)} /></label>
              <label>{t('clm.pc.phase')}<input type="number" min={1} value={e.phase} onChange={(x) => majEtape(i, 'phase', Number(x.target.value))} /></label>
              <label>{t('clm.col.type')}
                <select value={e.type} onChange={(x) => majEtape(i, 'type', x.target.value)}>{TYPES.map((ty) => <option key={ty} value={ty}>{t(`clm.type.${ty}`)}</option>)}</select>
              </label>
              <label className="col-2">{t('clm.pc.affectations')} <span className="muted">{t('clm.pc.affectationsAide')}</span>
                <input value={e.affectationsTexte} onChange={(x) => majEtape(i, 'affectationsTexte', x.target.value)} /></label>
              <label>{t('clm.pc.quorum')}<input type="number" min={1} value={e.quorum} onChange={(x) => majEtape(i, 'quorum', Number(x.target.value))} /></label>
              <label>{t('clm.pc.delai')}<input type="number" min={1} value={e.delaiJours} onChange={(x) => majEtape(i, 'delaiJours', x.target.value)} /></label>
              <label className="col-2">{t('clm.pc.politique')}
                <select value={e.politique} onChange={(x) => majEtape(i, 'politique', x.target.value)}>{POLITIQUES.map((p) => <option key={p} value={p}>{t(`clm.politique.${p}`)}</option>)}</select>
              </label>
            </div>
            <div className="cmp-row">
              <button type="button" className="secondary" disabled={i === 0} onClick={() => deplacer(i, -1)} aria-label={t('clm.pc.monter', { n: i + 1 })}>↑</button>
              <button type="button" className="secondary" disabled={i === etapes.length - 1} onClick={() => deplacer(i, 1)} aria-label={t('clm.pc.descendre', { n: i + 1 })}>↓</button>
              <button type="button" className="danger" onClick={() => onChange(etapes.filter((_, j) => j !== i))}>{t('clm.retirer')}</button>
            </div>
          </fieldset>
        ))}
        {etapes.length === 0 && <p className="muted">{t('clm.dt.aucuneEtape')}</p>}
      </Bloc>
      <Bloc zone="06" titre={t('clm.pc.resume')} large>
        <ResumeParcours etapes={etapes} />
      </Bloc>
    </Blocs>
  )
}

export const versRequete = (etapes) => etapes.map((e) => ({ cle: e.cle, libelle: e.libelle, phase: Number(e.phase), type: e.type,
  affectations: versAffectations(e.affectationsTexte), quorum: e.quorum ? Number(e.quorum) : null, politique: e.politique,
  delaiJours: e.delaiJours === '' || e.delaiJours == null ? null : Number(e.delaiJours) }))

export const depuisRequete = (etapes) => (etapes || []).map((e) => ({ ...e, affectationsTexte: versTexte(e.affectations), delaiJours: e.delaiJours ?? '' }))

/** Onglet parcours d'une trousse (UI-11) : conception en brouillon, réassignation à chaud en traitement (ST-08). */
export default function OngletParcours({ trousse, peutDefinir, peutReassigner, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [etapes, setEtapes] = useState(() => depuisRequete(trousse.etapes))
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [reassign, setReassign] = useState({})
  const id = trousse.resume.id
  const aide = trousse.participants.length ? t('clm.pc.participantsDispo', { liste: trousse.participants.map((p) => `PARTICIPANT:${p.id} (${p.nom})`).join(', ') }) : null

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
      await api.put(`/clm/etapes/${e.id}/affectations`, { affectations: versAffectations(r.texte ?? versTexte(e.affectations)), quorum: r.quorum ? Number(r.quorum) : null, motif: r.motif })
      setInfo(t('clm.pc.reassigne', { e: e.libelle }))
      onChange()
    } catch (x) { setErreur(versErreur(x)) }
  }

  if (peutDefinir) {
    return (
      <div data-maquette="UI-11">
        <Message erreur={erreur} info={info} />
        <EditeurParcours etapes={etapes} onChange={setEtapes} aideParticipants={aide} />
        <BarreActions>
          <button type="button" className="secondary" onClick={() => setEtapes([...etapes, nouvelleEtape(etapes)])}>{t('clm.pc.ajouterEtape')}</button>
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
          <CanvasParcours etapes={trousse.etapes} participants={trousse.participants} />
        </Bloc>
        <Bloc zone="02-05" titre={t('clm.pc.blocSuivi')} large>
          <ol className="clm-chrono">
            {trousse.etapes.map((e) => (
              <li key={e.id}>
                <strong>{t('clm.pc.phaseN', { n: e.phase })} · {e.libelle}</strong> ({t(`clm.type.${e.type}`)}) <Statut valeur={e.statut} />
                {e.enRetard && <> <Statut valeur="EXPIREE" /></>}
                <br /><span className="muted">{versTexte(e.affectations)} · {t('clm.pc.quorum')} {e.quorum}</span>
                {e.decisions.length > 0 && (
                  <ul className="clm-liste">{e.decisions.map((d, i) => <li key={i}><Statut valeur={d.nature === 'APPROUVE' ? 'APPROUVEE' : 'REJETEE'} /> {d.acteur} · {dateHeure(d.decideLe)}{d.commentaire ? ` — « ${d.commentaire} »` : ''}</li>)}</ul>
                )}
                {peutReassigner && ['EN_ATTENTE', 'ACTIVE'].includes(e.statut) && (
                  <details>
                    <summary>{t('clm.pc.reassigner')}</summary>
                    <div className="form clm-sousform">
                      <label>{t('clm.pc.affectations')}<input defaultValue={versTexte(e.affectations)} onChange={(x) => setReassign({ ...reassign, [e.id]: { ...reassign[e.id], texte: x.target.value } })} /></label>
                      <label>{t('clm.pc.quorum')}<input type="number" min={1} defaultValue={e.quorum} onChange={(x) => setReassign({ ...reassign, [e.id]: { ...reassign[e.id], quorum: x.target.value } })} /></label>
                      <label>{t('clm.col.motif')}<input onChange={(x) => setReassign({ ...reassign, [e.id]: { ...reassign[e.id], motif: x.target.value } })} /></label>
                      <div className="cmp-row"><button type="button" onClick={() => reassigner(e)}>{t('clm.enregistrer')}</button></div>
                    </div>
                  </details>
                )}
              </li>
            ))}
          </ol>
        </Bloc>
        <Bloc zone="06" titre={t('clm.pc.resume')} large>
          <ResumeParcours etapes={trousse.etapes} />
        </Bloc>
      </Blocs>
    </div>
  )
}
