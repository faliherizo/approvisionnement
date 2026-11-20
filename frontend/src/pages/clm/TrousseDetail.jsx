import { useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import api from '../../api'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import { GardeModule, Message, Onglets, Panneau, Statut, date, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'
import { ApercuDossier, dossierImporte } from './TrousseCreer.jsx'
import ValeursTrousse from './ValeursTrousse.jsx'
import OngletDocuments from './OngletDocuments.jsx'
import OngletParticipants from './OngletParticipants.jsx'
import OngletParcours, { libelleAffectation } from './OngletParcours.jsx'
import OngletPreuve from './OngletPreuve.jsx'

const ONGLETS = ['resume', 'documents', 'participants', 'parcours', 'historique', 'preuve']

/** UI-04 — Détail d'une trousse : bandeau, chronologie, alertes, onglets et actions permises selon état et rôle. */
function Contenu() {
  const { id } = useParams()
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const { actif: moduleActif } = useModules()
  const versErreur = useErreur()
  const [params, setParams] = useSearchParams()
  const onglet = ONGLETS.includes(params.get('onglet')) ? params.get('onglet') : 'resume'
  const setOnglet = (o) => setParams({ onglet: o }, { replace: true })
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [import_, setImport] = useState(null)
  const [aRelancer, setARelancer] = useState('')
  const { donnees: d, erreur: errChargement, chargement, recharger } = useRessource(`/clm/trousses/${id}`)
  const historique = useRessource(onglet === 'historique' ? `/clm/trousses/${id}/historique` : null)
  const types = useListe('CLM_TYPE_CONTRAT')
  const unites = useListe('CLM_UNITE')

  async function action(chemin, corps, messageOk, confirmation) {
    if (confirmation && !window.confirm(confirmation)) return
    setErreur(null)
    try {
      const r = await api.post(`/clm/trousses/${id}/${chemin}`, corps || {})
      setInfo(messageOk)
      if (chemin === 'preremplissage') setImport(r.data)
      recharger()
    } catch (e) {
      setErreur(versErreur(e))
    }
  }
  async function relancerEtape() {
    setErreur(null)
    try {
      const { data } = await api.post(`/clm/etapes/${aRelancer}/relancer`)
      setInfo(t('clm.dt.etapeRelancee', { e: data.libelle }))
      recharger()
    } catch (e) {
      setErreur(versErreur(e))
    }
  }

  if (chargement && !d) return <div className="panel" role="status">{t('clm.chargement')}</div>
  if (!d) return <Message erreur={errChargement} onReessayer={recharger} />
  const r = d.resume
  const peut = (a) => d.actions.includes(a)
  const actives = d.etapes.filter((e) => e.statut === 'ACTIVE')
  const lancer = () => action('lancer', null, t('clm.dt.lancee'), t('clm.dt.confirmerLancer'))
  const onglets = [
    { id: 'resume', libelle: t('clm.dt.resume') },
    { id: 'documents', libelle: t('clm.dt.documents', { n: d.documents.filter((x) => !['REMPLACE', 'RETIRE'].includes(x.statut)).length }) },
    { id: 'participants', libelle: t('clm.dt.participants', { n: d.participants.length }) },
    { id: 'parcours', libelle: t('clm.dt.parcours') },
    { id: 'historique', libelle: t('clm.dt.historique') },
    { id: 'preuve', libelle: t('clm.dt.preuve') },
  ]

  return (
    <EcranClm maquette="UI-04" titre={r.titre} objectif={t('clm.dt.objectif')} zoneActions="05"
      ariane={<><Link to="/clm/trousses">{t('clm.tr.titre')}</Link> / {r.reference}</>}
      statut={(
        <dl className="clm-bandeau-meta" data-zone="01">
          <div><dt>{t('clm.col.reference')}</dt><dd>{r.reference}</dd></div>
          <div><dt>{t('clm.col.statut')}</dt><dd><Statut valeur={r.statut} />{r.enRetard && <> <Statut valeur="EXPIREE" /></>}</dd></div>
          <div><dt>{t('clm.col.proprietaire')}</dt><dd>{r.proprietaire}</dd></div>
          <div><dt>{t('clm.col.echeance')}</dt><dd>{date(r.echeance)}</dd></div>
          <div><dt>{t('clm.col.source')}</dt><dd>{t(`clm.source.${r.source}`)}{r.sfDossierId ? ` · ${r.sfDossierId}` : ''}</dd></div>
        </dl>
      )}
      actions={(
        <>
          {peut('MODIFIER') && <Link className="clm-bouton secondaire" to={`/clm/trousses/nouvelle?id=${r.id}`}>{t('clm.dt.modifierBrouillon')}</Link>}
          {peut('AJOUTER_DOCUMENT') && <button type="button" className="secondary" onClick={() => setOnglet('documents')}>{t('clm.dt.ajouterDocument')}</button>}
          {peut('REASSIGNER') && actives.length > 0 && (
            <span className="clm-barre__groupe">
              <label>{t('clm.dt.etapeARelancer')}
                <select value={aRelancer} onChange={(e) => setARelancer(e.target.value)}>
                  <option value="">—</option>
                  {actives.map((e) => <option key={e.id} value={e.id}>{e.libelle}</option>)}
                </select>
              </label>
              <button type="button" className="secondary" disabled={!aRelancer} onClick={relancerEtape}>{t('clm.dt.relancerEtape')}</button>
            </span>
          )}
          <button type="button" className="secondary" onClick={() => setOnglet('preuve')}>{t('clm.dt.consulterPreuve')}</button>
          {peut('LANCER') && <button type="button" onClick={lancer}>{t('clm.dt.lancer')}</button>}
          {peut('REPRENDRE') && <button type="button" className="secondary" onClick={() => action('reprendre', null, t('clm.dt.reprise'))}>{t('clm.dt.reprendre')}</button>}
          {peut('ANNULER') && <button type="button" className="danger" onClick={() => {
            const motif = window.prompt(t('clm.dt.motifAnnulation'))
            if (motif) action('annuler', { motif }, t('clm.dt.annulee'))
          }}>{t('clm.dt.annuler')}</button>}
        </>
      )}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="03" titre={t('clm.dt.chronologie')}>
          {d.etapes.length === 0 ? <p className="muted">{t('clm.dt.aucuneEtape')}</p> : (
            <ol className="clm-chrono">
              {d.etapes.map((e) => (
                <li key={e.id}>
                  <strong>{t('clm.pc.phaseN', { n: e.phase })} · {e.libelle}</strong> ({t(`clm.type.${e.type}`)}) <Statut valeur={e.statut} />
                  <br /><span className="muted">{e.affectations.map((a) => libelleAffectation(a, d.participants, t)).join(', ')}
                    {e.echeance ? ` · ${t('clm.col.echeance')} ${dateHeure(e.echeance)}` : ''}</span>
                </li>
              ))}
            </ol>
          )}
        </Bloc>
        <Bloc zone="04" titre={t('clm.dt.alertes')} className={d.alertes.length > 0 ? 'clm-alertes' : ''}>
          {d.alertes.length === 0 ? <p className="muted">{t('clm.dt.aucuneAlerte')}</p> : (
            <ul className="clm-liste">{d.alertes.map((a) => <li key={a}><span aria-hidden="true">⚠</span> {a}</li>)}</ul>
          )}
        </Bloc>
        <div className="clm-bloc clm-bloc--large" data-zone="02">
          <Onglets onglets={onglets} actif={onglet} onChange={setOnglet} label={t('clm.dt.sections')} />
          <Panneau id="resume" actif={onglet}>
            <Blocs>
              {r.sfDossierId && (
                <Bloc titre={t('clm.dt.donnees')} niveau={3}>
                  {import_ ? <ApercuDossier dossier={dossierImporte(import_)} />
                    : <p>{t('clm.sf.dossierCourt', { id: r.sfDossierId, d: dateHeure(d.sfRecupereLe) })}</p>}
                  {peut('PREREMPLIR') && (
                    <button type="button" className="secondary" onClick={() => action('preremplissage', { sfDossierId: r.sfDossierId }, t('clm.dt.actualise'))}>{t('clm.dt.actualiserSf')}</button>
                  )}
                </Bloc>
              )}
              <Bloc titre={t('clm.dt.parametres')} niveau={3}>
                <dl className="clm-dl">
                  <div><dt>{t('clm.champ.typeContrat')}</dt><dd>{types.libelle(r.typeContrat) || '—'}</dd></div>
                  <div><dt>{t('clm.champ.unite')}</dt><dd>{unites.libelle(r.unite) || '—'}</dd></div>
                  <div><dt>{t('clm.champ.risque')}</dt><dd>{t(`clm.risque.${d.risque}`)}</dd></div>
                  <div><dt>{t('clm.dt.rappels')}</dt><dd>{t('clm.dt.rappelsValeur', { f: d.rappelFrequenceJours, n: d.rappelsMax })}</dd></div>
                  <div><dt>{t('clm.champ.copies')}</dt><dd>{d.copies || '—'}</dd></div>
                  <div><dt>{t('clm.cr.modele')}</dt><dd>{d.modeleId ? <Link to={`/clm/modeles/${d.modeleId}`}>#{d.modeleId}</Link> : t('clm.cr.sansModele')}</dd></div>
                </dl>
              </Bloc>
            </Blocs>
            <ValeursTrousse trousse={d} conflits={import_?.conflits} modifiable={peut('PREREMPLIR')}
              onChange={(vue) => { if (vue.sfDossierId) setImport(vue); recharger() }} />
          </Panneau>
          <Panneau id="documents" actif={onglet}>
            <OngletDocuments trousse={d} peutGerer={peut('AJOUTER_DOCUMENT')} peutGenerer={peut('GENERER') && !!d.modeleId}
              iaActive={moduleActif('clm_ia')} onChange={recharger} />
          </Panneau>
          <Panneau id="participants" actif={onglet}>
            <OngletParticipants trousse={d} peutGerer={peut('PARTICIPANTS')} onChange={recharger}
              onOrdre={() => setOnglet('parcours')} onLancer={peut('LANCER') ? lancer : null} />
          </Panneau>
          <Panneau id="parcours" actif={onglet}>
            <OngletParcours trousse={d} peutDefinir={peut('PARCOURS')} peutReassigner={peut('REASSIGNER') || peut('PARCOURS')} onChange={recharger} />
          </Panneau>
          <Panneau id="historique" actif={onglet}>
            <Message erreur={historique.erreur} onReessayer={historique.recharger} />
            <div className="table-scroll">
              <table className="table clm-table">
                <caption>{t('clm.dt.historiqueLegende')}</caption>
                <thead><tr><th scope="col">#</th><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.col.evenement')}</th>
                  <th scope="col">{t('clm.col.acteur')}</th><th scope="col">{t('clm.col.detail')}</th></tr></thead>
                <tbody>
                  {(historique.donnees || []).map((e) => (
                    <tr key={e.sequence}>
                      <td data-label="#">{e.sequence}</td>
                      <td data-label={t('clm.col.date')}>{dateHeure(e.horodatage)}</td>
                      <td data-label={t('clm.col.evenement')}>{t(`clm.evt.${e.type}`)}</td>
                      <td data-label={t('clm.col.acteur')}>{e.acteur}</td>
                      <td data-label={t('clm.col.detail')}>{e.detail}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Panneau>
          <Panneau id="preuve" actif={onglet}>
            <OngletPreuve trousseId={id} resume={r} admin={hasRole('ADMIN_AFFAIRES') || hasRole('SUPERADMIN')} onHistorique={() => setOnglet('historique')} />
          </Panneau>
        </div>
      </Blocs>
    </EcranClm>
  )
}

export default function TrousseDetail() {
  return <GardeModule><Contenu /></GardeModule>
}
