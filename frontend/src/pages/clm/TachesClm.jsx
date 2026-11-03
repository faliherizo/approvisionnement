import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, Statut, Visionneuse, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const FILTRES = ['a_traiter', 'en_retard', 'terminees', 'deleguees']
const JOUR_MS = 86_400_000

/** UI-13 (validation) / UI-14 (approbation) : document, contexte, commentaires, versions, décisions précédentes, décision motivée. */
function Decision({ tache, onDecide, onRetour }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const titre = useRef(null)
  const { donnees: d, erreur: errTrousse } = useRessource(`/clm/trousses/${tache.trousseId}`)
  const historique = useRessource(`/clm/trousses/${tache.trousseId}/historique`)
  const [motif, setMotif] = useState('')
  const [note, setNote] = useState('')
  const [delegation, setDelegation] = useState({ vers: '', motif: '' })
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [vu, setVu] = useState(null)
  useEffect(() => { titre.current?.focus() }, [tache.etapeId])

  const docs = (d?.documents || []).filter((x) => ['ENVOYE', 'SIGNE'].includes(x.statut))
  const docVu = vu || docs[0]
  const etape = d?.etapes?.find((e) => e.id === tache.etapeId)
  const precedentes = (d?.etapes || []).filter((e) => e.decisions.length > 0)
  const commentaires = (historique.donnees || []).filter((e) => e.type === 'COMMENTAIRE')
  const versions = docVu ? (d?.documents || []).filter((x) => x.cleDocument === docVu.cleDocument) : []
  const approbation = tache.type === 'APPROBATION'

  async function decider(decision) {
    setErreur(null)
    if (decision !== 'APPROUVE' && !motif.trim()) { setErreur(t('clm.ta.motifObligatoire')); focaliser('clm-motif'); return }
    try { await api.post(`/clm/etapes/${tache.etapeId}/decider`, { decision, commentaire: motif.trim() || null }); onDecide(t(`clm.ta.ok.${decision}`)) } catch (e) { setErreur(versErreur(e)) }
  }
  async function commenter() {
    setErreur(null)
    if (!note.trim()) { setErreur(t('clm.ta.commentaireVide')); return }
    try { await api.post(`/clm/etapes/${tache.etapeId}/commentaires`, { texte: note.trim() }); setNote(''); setInfo(t('clm.ta.commentaireAjoute')); historique.recharger() } catch (e) { setErreur(versErreur(e)) }
  }
  async function deleguer(e) {
    e.preventDefault()
    setErreur(null)
    try { await api.post(`/clm/etapes/${tache.etapeId}/deleguer`, delegation); onDecide(t('clm.ta.delegue', { vers: delegation.vers })) } catch (x) { setErreur(versErreur(x)) }
  }
  async function lienSignature() {
    setErreur(null)
    try { const { data } = await api.post(`/clm/etapes/${tache.etapeId}/lien-signature`); window.location.assign(data.lien) } catch (e) { setErreur(versErreur(e)) }
  }

  const actions = tache.decidee ? <button type="button" className="secondary" onClick={onRetour}>{t('clm.ta.retourListe')}</button>
    : tache.type === 'SIGNATURE' ? <button type="button" onClick={lienSignature}>{t('clm.ta.signer')}</button> : (
      <>
        <button type="button" onClick={() => decider('APPROUVE')}>{approbation ? t('clm.ta.approuver') : t('clm.ta.valider')}</button>
        <button type="button" className="danger" onClick={() => decider('REJETE')}>{t('clm.ta.rejeter')}</button>
        <button type="button" className="secondary" onClick={() => decider('RETOUR')}>{approbation ? t('clm.ta.demanderCorrection') : t('clm.ta.retour')}</button>
        <button type="button" className="secondary" onClick={() => focaliser('clm-note')}>{t('clm.ta.ajouterCommentaire')}</button>
        <button type="button" className="secondary" onClick={() => focaliser('clm-delegation')}>{t('clm.ta.deleguer')}</button>
      </>
    )

  const blocDocument = (
    <Bloc key="doc" zone={approbation ? '02' : '01'} titre={approbation ? t('clm.ta.blocDocumentSoumis') : t('clm.ta.blocVisionneuse')} large>
      {docs.length === 0 ? <p className="muted">{t('clm.ta.documentInaccessible')}</p> : (
        <>
          {docs.length > 1 && (
            <div className="cmp-row" role="group" aria-label={t('clm.ta.documents')}>
              {docs.map((x) => <button key={x.id} type="button" className="secondary" aria-pressed={docVu?.id === x.id} onClick={() => setVu(x)}>{x.nom} v{x.version}</button>)}
            </div>
          )}
          <Visionneuse trousseId={tache.trousseId} document={docVu} />
        </>
      )}
    </Bloc>
  )
  const blocContexte = (
    <Bloc key="ctx" zone={approbation ? '01' : '02'} titre={approbation ? t('clm.ta.blocContexte') : t('clm.ta.blocChamps')}>
      <dl className="clm-dl">
        <div><dt>{t('clm.col.reference')}</dt><dd><Link to={`/clm/trousses/${tache.trousseId}`}>{tache.reference}</Link></dd></div>
        <div><dt>{t('clm.col.objet')}</dt><dd>{tache.titre}</dd></div>
        <div><dt>{t('clm.col.demandeur')}</dt><dd>{tache.demandeur}</dd></div>
        <div><dt>{t('clm.col.echeance')}</dt><dd>{dateHeure(tache.echeance)} {tache.enRetard && <Statut valeur="EXPIREE" />}</dd></div>
        {d && Object.entries(d.valeurs).map(([k, v]) => <div key={k}><dt>{k}</dt><dd>{String(v.valeur)}</dd></div>)}
      </dl>
    </Bloc>
  )
  const blocCommentaires = (
    <Bloc key="com" zone={approbation ? '04' : '03'} titre={approbation ? t('clm.ta.blocMotif') : t('clm.ta.blocCommentaires')}>
      {commentaires.length === 0 ? <p className="muted">{t('clm.ta.aucunCommentaire')}</p> : (
        <ul className="clm-liste">{commentaires.map((c) => <li key={c.sequence}>{c.detail}<br /><span className="muted">{c.acteur} · {dateHeure(c.horodatage)}</span></li>)}</ul>
      )}
      {!tache.decidee && (
        <div className="form">
          <label>{t('clm.ta.nouveauCommentaire')}<textarea id="clm-note" rows={2} maxLength={1500} value={note} onChange={(e) => setNote(e.target.value)} /></label>
          <div className="cmp-row"><button type="button" className="secondary" onClick={commenter}>{t('clm.ta.ajouterCommentaire')}</button></div>
        </div>
      )}
    </Bloc>
  )
  const blocVersions = (
    <Bloc key="ver" zone="04" titre={t('clm.ta.blocVersions')}>
      {versions.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : (
        <ul className="clm-liste">{versions.map((v) => <li key={v.id}>v{v.version} · {v.auteur} · {dateHeure(v.creeLe)}{v.motif ? ` — ${v.motif}` : ''} <Statut valeur={v.statut} /></li>)}</ul>
      )}
    </Bloc>
  )
  const blocPrecedentes = (
    <Bloc key="prec" zone={approbation ? '03' : '05'} titre={approbation ? t('clm.ta.blocHistoriqueValidations') : t('clm.ta.precedentes')}>
      {precedentes.length === 0 ? <p className="muted">{t('clm.ta.aucunePrecedente')}</p> : (
        <ul className="clm-liste">
          {precedentes.flatMap((e) => e.decisions.map((x, i) => (
            <li key={`${e.id}-${i}`}>{e.libelle} : <Statut valeur={x.nature === 'APPROUVE' ? 'APPROUVEE' : 'REJETEE'} /> {x.acteur} · {dateHeure(x.decideLe)}{x.commentaire ? ` — « ${x.commentaire} »` : ''}</li>
          )))}
        </ul>
      )}
    </Bloc>
  )
  const blocDecision = (
    <Bloc key="dec" zone={approbation ? '05' : 'D'} titre={t('clm.ta.blocDecision')} large>
      <dl className="clm-dl">
        <div><dt>{t('clm.ta.activeeLe')}</dt><dd>{dateHeure(etape?.activeeLe)}</dd></div>
        <div><dt>{t('clm.col.statut')}</dt><dd>{tache.decidee ? <Statut valeur={tache.maDecision === 'APPROUVE' ? 'APPROUVEE' : 'REJETEE'} /> : <Statut valeur={tache.statut} />}</dd></div>
        {tache.reassignee && <div><dt>{t('clm.ta.origine')}</dt><dd>{t('clm.ta.reassignee')}</dd></div>}
      </dl>
      {!tache.decidee && tache.type !== 'SIGNATURE' && (
        <>
          <label>{t('clm.ta.commentaire')} <span className="muted">{t('clm.ta.commentaireAide')}</span>
            <textarea id="clm-motif" rows={3} value={motif} onChange={(e) => setMotif(e.target.value)} /></label>
          <form className="form clm-sousform" onSubmit={deleguer}>
            <h3>{t('clm.ta.deleguer')}</h3>
            <p className="muted">{t('clm.ta.delegationAide')}</p>
            <div className="form-grid">
              <label>{t('clm.ta.delegataire')}<input id="clm-delegation" type="email" value={delegation.vers} onChange={(e) => setDelegation({ ...delegation, vers: e.target.value })} required autoComplete="email" /></label>
              <label>{t('clm.col.motif')}<input value={delegation.motif} onChange={(e) => setDelegation({ ...delegation, motif: e.target.value })} /></label>
            </div>
            <div className="cmp-row"><button type="submit" className="secondary">{t('clm.ta.confirmerDelegation')}</button></div>
          </form>
        </>
      )}
    </Bloc>
  )
  const blocs = approbation ? [blocContexte, blocDocument, blocPrecedentes, blocCommentaires, blocDecision]
    : [blocDocument, blocContexte, blocCommentaires, blocVersions, blocPrecedentes, blocDecision]

  return (
    <EcranClm maquette={approbation ? 'UI-14' : 'UI-13'} titreRef={titre} titre={`${t(`clm.type.${tache.type}`)} — ${tache.libelle}`}
      objectif={approbation ? t('clm.ta.objectifApprobation') : t('clm.ta.objectifValidation')}
      ariane={<><button type="button" className="lien" onClick={onRetour}>{t('clm.ta.titre')}</button> / {tache.reference}</>}
      actions={actions}>
      <Message erreur={erreur || errTrousse} info={info} />
      <Blocs>{blocs}</Blocs>
    </EcranClm>
  )
}

function focaliser(id) {
  document.getElementById(id)?.focus()
}

/** UI-12 — Boîte de tâches : filtres, priorisation par retard et échéance, délégations et réassignations. */
function Contenu() {
  const { t } = useI18n()
  const [params, setParams] = useSearchParams()
  const filtre = FILTRES.includes(params.get('filtre')) ? params.get('filtre') : 'a_traiter'
  const [info, setInfo] = useState('')
  const { donnees, erreur, chargement, recharger } = useRessource(`/clm/taches?filtre=${filtre}`)
  const taches = donnees || []
  const choisie = taches.find((x) => String(x.etapeId) === params.get('etape'))
  const ouvrir = (x) => setParams({ filtre, etape: String(x.etapeId) })
  const retour = () => setParams({ filtre })

  if (choisie) {
    return <Decision tache={choisie} onRetour={retour} onDecide={(msg) => { setInfo(msg); retour(); recharger() }} />
  }
  const enRetard = taches.filter((x) => x.enRetard).length
  const bientot = taches.filter((x) => !x.enRetard && x.echeance && new Date(x.echeance).getTime() - Date.now() < 2 * JOUR_MS).length
  const reassignees = taches.filter((x) => x.reassignee)

  return (
    <EcranClm maquette="UI-12" titre={t('clm.ta.titre')} objectif={t('clm.ta.objectif')}
      actions={(
        <>
          <button type="button" disabled={taches.length === 0 || filtre === 'terminees'} onClick={() => ouvrir(taches[0])}>{t('clm.ta.traiterPrioritaire')}</button>
          <button type="button" className="secondary" onClick={() => setParams({ filtre: 'en_retard' })}>{t('clm.ta.voirRetard')}</button>
        </>
      )}>
      <Message erreur={erreur} info={chargement ? t('clm.chargement') : info} onReessayer={recharger} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.ta.filtres')} large>
          <div className="clm-onglets" role="group" aria-label={t('clm.ta.filtres')}>
            {FILTRES.map((f) => (
              <button key={f} type="button" aria-pressed={filtre === f} className={filtre === f ? 'is-actif' : ''} onClick={() => setParams({ filtre: f })}
                aria-label={t('clm.ta.filtrerPar', { f: t(`clm.ta.filtre.${f}`) })}>{t(`clm.ta.filtre.${f}`)}</button>
            ))}
          </div>
        </Bloc>
        <Bloc zone="02" titre={t(`clm.ta.filtre.${filtre}`)} large>
          {donnees && taches.length === 0 ? <p className="muted">{t('clm.ta.aucune')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t(`clm.ta.filtre.${filtre}`)}</caption>
                <thead><tr><th scope="col">{t('clm.ta.trousse')}</th><th scope="col">{t('clm.ta.typeAction')}</th><th scope="col">{t('clm.col.demandeur')}</th>
                  <th scope="col">{t('clm.col.echeance')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {taches.map((x) => (
                    <tr key={`${x.etapeId}-${x.maDecision || ''}`}>
                      <td data-label={t('clm.ta.trousse')}>{x.reference} — {x.titre}</td>
                      <td data-label={t('clm.ta.typeAction')}>{t(`clm.type.${x.type}`)} · {x.libelle}
                        {x.reassignee && <><br /><span className="clm-statut clm-statut--attente"><span aria-hidden="true">⇄</span> {t('clm.ta.reassignee')}</span></>}</td>
                      <td data-label={t('clm.col.demandeur')}>{x.demandeur}</td>
                      <td data-label={t('clm.col.echeance')}>{dateHeure(x.echeance)} {x.enRetard && <Statut valeur="EXPIREE" />}</td>
                      <td data-label={t('clm.col.statut')}>{x.decidee ? <Statut valeur={x.maDecision === 'APPROUVE' ? 'APPROUVEE' : 'REJETEE'} /> : <Statut valeur={x.statut} />}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        <button type="button" className="lien" aria-label={`${x.decidee ? t('clm.ta.ouvrirTache') : t('clm.ta.traiter')} — ${x.reference} ${x.libelle}`} onClick={() => ouvrir(x)}>{x.decidee ? t('clm.ta.ouvrir') : t('clm.ta.traiter')}</button>
                        <Link to={`/clm/trousses/${x.trousseId}?onglet=historique`} aria-label={`${t('clm.ta.voirHistorique')} — ${x.reference}`}>{t('clm.ta.voirHistorique')}</Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        <Bloc zone="03" titre={t('clm.ta.blocPriorisation')}>
          <p>{t('clm.ta.priorisationAide')}</p>
          <ul className="clm-liste">
            <li><Statut valeur="EXPIREE" /> {t('clm.ta.nbRetard', { n: enRetard })}</li>
            <li>{t('clm.ta.nbBientot', { n: bientot })}</li>
          </ul>
        </Bloc>
        <Bloc zone="04" titre={t('clm.ta.blocDelegations')}>
          {reassignees.length === 0 ? <p className="muted">{t('clm.ta.aucuneDelegation')}</p> : (
            <ul className="clm-liste">{reassignees.map((x) => <li key={x.etapeId}>{x.reference} · {x.libelle}</li>)}</ul>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function TachesClm() {
  return <GardeModule><Contenu /></GardeModule>
}
