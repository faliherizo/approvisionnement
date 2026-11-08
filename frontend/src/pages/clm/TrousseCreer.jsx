import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import { ChoixListe, GardeModule, Message, Statut, date, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'
import OngletDocuments from './OngletDocuments.jsx'
import OngletParticipants from './OngletParticipants.jsx'
import OngletParcours from './OngletParcours.jsx'
import ValeursTrousse from './ValeursTrousse.jsx'

const ETAPES = ['source', 'documents', 'participants', 'processus', 'recapitulatif']
const RISQUES = ['FAIBLE', 'MOYEN', 'ELEVE']
/** Rappels laissés vides : l'organisation applique ses valeurs par défaut (UI-22) tant que l'initiateur n'en fixe pas. */
const FORM_VIDE = { source: 'MANUELLE', sfDossierId: '', modeleId: '', titre: '', reference: '', typeContrat: '', unite: '',
  risque: 'MOYEN', echeance: '', copies: '', rappelFrequenceJours: '', rappelsMax: '' }
const ACTIFS = (docs) => docs.filter((x) => !['REMPLACE', 'RETIRE'].includes(x.statut))
const nombreOuNul = (v) => (v === '' || v === null || v === undefined ? null : Number(v))

const depuisDetail = (d) => ({ source: d.resume.source, sfDossierId: d.resume.sfDossierId || '', modeleId: d.modeleId ? String(d.modeleId) : '',
  titre: d.resume.titre, reference: d.resume.reference, typeContrat: d.resume.typeContrat || '', unite: d.resume.unite || '', risque: d.risque,
  echeance: d.resume.echeance || '', copies: d.copies || '', rappelFrequenceJours: d.rappelFrequenceJours, rappelsMax: d.rappelsMax })

/** Champs Salesforce à résumer : ceux de la liste de l'organisation, sinon tous ceux du dossier. */
const champsSalesforce = (liste, dossier) => (liste.valeurs.length > 0
  ? liste.valeurs.map((v) => [v.code, v.libelle])
  : Object.keys(dossier?.champs || {}).map((k) => [k, k]))

/** Résumé d'un dossier Salesforce (UI-10, zone 02) : dossier consulté par la recherche ou importé dans la trousse. */
export function ApercuDossier({ dossier }) {
  const { t } = useI18n()
  const champs = useListe('CLM_CHAMP_SALESFORCE')
  if (!dossier) return <p className="muted">{t('clm.sf.aucunDossier')}</p>
  return (
    <div className="clm-apercu">
      <p>{t('clm.sf.dossier', { numero: dossier.numero, id: dossier.id })} · {t('clm.sf.recupereLe', { d: dateHeure(dossier.recupereLe) })}</p>
      <dl className="clm-dl">
        {champsSalesforce(champs, dossier).map(([k, libelle]) => <div key={k}><dt>{libelle}</dt><dd>{String(dossier.champs?.[k] ?? '—')}</dd></div>)}
      </dl>
    </div>
  )
}

/** Dossier à résumer : celui de la dernière recherche, sinon celui du dernier import. */
export const dossierImporte = (vue) => (vue?.sfDossierId ? { id: vue.sfDossierId, numero: vue.numero, champs: vue.dossier, recupereLe: vue.recupereLe } : null)

/** Étape 1 (UI-03 + UI-10) : origine, dossier Salesforce, informations du contrat, brouillon, données importées et corrigées. */
function EtapeSource({ form, setForm, modeles, dossier, onRechercher, d, import_, enregistreLe, onImporter, onValeurs, publics }) {
  const { t } = useI18n()
  const types = useListe('CLM_TYPE_CONTRAT')
  const unites = useListe('CLM_UNITE')
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'number' ? (e.target.value === '' ? '' : Number(e.target.value)) : e.target.value })
  const choisir = (k) => (v) => setForm({ ...form, [k]: v })
  const niveau = (cote) => publics?.[cote]?.[form.risque.toLowerCase()]
  return (
    <Blocs>
      <Bloc zone="02" titre={t('clm.cr.blocSalesforce')}>
        <fieldset className="form-fieldset">
          <legend>{t('clm.cr.origine')}</legend>
          {['MANUELLE', 'SALESFORCE'].map((s) => (
            <label key={s} className="check-row"><input type="radio" name="source" value={s} checked={form.source === s} onChange={maj('source')} disabled={!!d} />{t(`clm.source.${s}`)}</label>
          ))}
        </fieldset>
        {form.source === 'SALESFORCE' && (
          <>
            <Bloc maquette="UI-10" zone="01" niveau={3} titre={t('clm.sf.blocRecherche')}>
              <div className="form clm-sousform">
                <label>{t('clm.cr.sfId')} <span className="muted">{t('clm.cr.sfAide')}</span>
                  <input value={form.sfDossierId} onChange={maj('sfDossierId')} required aria-required="true" autoComplete="off" />
                </label>
                <div className="cmp-row">
                  <button type="button" className="secondary" onClick={onRechercher}>{t('clm.rechercher')}</button>
                  {d && form.sfDossierId && <button type="button" className="secondary" onClick={onImporter}>{t('clm.cr.importer')}</button>}
                </div>
              </div>
            </Bloc>
            <Bloc maquette="UI-10" zone="02" niveau={3} titre={t('clm.sf.blocResume')}>
              <ApercuDossier dossier={dossier || dossierImporte(import_)} />
            </Bloc>
          </>
        )}
      </Bloc>
      <Bloc zone="03" titre={t('clm.cr.blocInformations')}>
        <div className="form-grid">
          <label className="col-2">{t('clm.champ.titre')} <span aria-hidden="true">*</span>
            <input value={form.titre} onChange={maj('titre')} required aria-required="true" maxLength={250} /></label>
          <label>{t('clm.champ.reference')} <span className="muted">{t('clm.cr.referenceAide')}</span>
            <input value={form.reference} onChange={maj('reference')} maxLength={60} disabled={!!d} /></label>
          <label>{t('clm.champ.typeContrat')}<ChoixListe liste={types} value={form.typeContrat} onChange={choisir('typeContrat')} maxLength={80} /></label>
          <label>{t('clm.champ.unite')}<ChoixListe liste={unites} value={form.unite} onChange={choisir('unite')} maxLength={120} /></label>
          <label>{t('clm.cr.modele')}
            <select value={form.modeleId} onChange={maj('modeleId')} disabled={!!d}>
              <option value="">{t('clm.cr.sansModele')}</option>
              {(modeles || []).map((m) => <option key={m.id} value={m.id}>{m.nom} · v{m.numero}</option>)}
            </select>
          </label>
          <label>{t('clm.champ.risque')}
            <select value={form.risque} onChange={maj('risque')} aria-describedby="clm-risque-aide">{RISQUES.map((r) => <option key={r} value={r}>{t(`clm.risque.${r}`)}</option>)}</select>
          </label>
          <p id="clm-risque-aide" className="muted">{publics ? t('clm.cr.identiteExigee', { externe: t(`clm.niveau.${niveau('identiteExterne')}`),
            interne: t(`clm.niveau.${niveau('identiteInterne')}`) }) : ''}</p>
          <label>{t('clm.champ.echeance')}<input type="date" value={form.echeance} onChange={maj('echeance')} /></label>
          <label className="col-2">{t('clm.champ.copies')} <span className="muted">{t('clm.cr.copiesAide')}</span><input value={form.copies} onChange={maj('copies')} /></label>
          <label>{t('clm.champ.frequence')}<input type="number" min={1} max={30} value={form.rappelFrequenceJours} onChange={maj('rappelFrequenceJours')} /></label>
          <label>{t('clm.champ.rappelsMax')}<input type="number" min={0} max={10} value={form.rappelsMax} onChange={maj('rappelsMax')} /></label>
        </div>
      </Bloc>
      <Bloc zone="04" titre={t('clm.cr.blocBrouillon')}>
        {d ? (
          <p><Statut valeur={d.resume.statut} /> {t('clm.cr.brouillonEtat', { ref: d.resume.reference, d: dateHeure(enregistreLe || d.resume.majLe) })}</p>
        ) : <p>{t('clm.cr.nonEnregistre')}</p>}
        <p className="muted">{t('clm.cr.brouillonAide')}</p>
      </Bloc>
      <Bloc zone="05" titre={t('clm.cr.blocImport')} large>
        {d ? <ValeursTrousse trousse={d} conflits={import_?.conflits} modifiable={d.actions.includes('PREREMPLIR')} onChange={onValeurs} />
          : <p className="muted">{form.source === 'SALESFORCE' ? t('clm.cr.importApres') : t('clm.cr.importManuel')}</p>}
      </Bloc>
    </Blocs>
  )
}

/** Étape 5 : récapitulatif et contrôles avant lancement. */
function Recapitulatif({ d, form, modeles }) {
  const { t } = useI18n()
  const unites = useListe('CLM_UNITE')
  const roles = useListe('CLM_ROLE_CONTRAT')
  const docs = ACTIFS(d.documents)
  const phases = [...new Set(d.etapes.map((e) => e.phase))].sort((a, b) => a - b)
  const controles = [
    [docs.length > 0 && docs.every((x) => x.analyse !== 'INFECTE'), 'clm.cr.ctrlDocuments'],
    [d.etapes.length > 0, 'clm.cr.ctrlParcours'],
    [!d.etapes.some((e) => e.type === 'SIGNATURE') || d.participants.length > 0, 'clm.cr.ctrlSignataires'],
  ]
  return (
    <Blocs>
      <Bloc zone="R1" titre={t('clm.cr.blocInformations')}>
        <dl className="clm-dl">
          <div><dt>{t('clm.cr.origine')}</dt><dd>{t(`clm.source.${d.resume.source}`)}{d.resume.sfDossierId ? ` · ${d.resume.sfDossierId}` : ''}</dd></div>
          <div><dt>{t('clm.champ.titre')}</dt><dd>{d.resume.titre}</dd></div>
          <div><dt>{t('clm.champ.reference')}</dt><dd>{d.resume.reference}</dd></div>
          <div><dt>{t('clm.champ.unite')}</dt><dd>{unites.libelle(d.resume.unite) || '—'}</dd></div>
          <div><dt>{t('clm.champ.risque')}</dt><dd>{t(`clm.risque.${d.risque}`)}</dd></div>
          <div><dt>{t('clm.champ.echeance')}</dt><dd>{date(d.resume.echeance)}</dd></div>
          <div><dt>{t('clm.cr.modele')}</dt><dd>{(modeles || []).find((m) => String(m.id) === String(form.modeleId))?.nom || t('clm.cr.sansModele')}</dd></div>
        </dl>
      </Bloc>
      <Bloc zone="R2" titre={t('clm.cr.blocControles')}>
        <ul className="clm-liste">
          {controles.map(([ok, cle]) => <li key={cle}><span aria-hidden="true">{ok ? '✔' : '⚠'}</span> {t(cle)} — <strong>{ok ? t('clm.cr.ok') : t('clm.cr.aCompleter')}</strong></li>)}
          {d.alertes.map((a) => <li key={a}><span aria-hidden="true">⚠</span> {a}</li>)}
        </ul>
      </Bloc>
      <Bloc zone="R3" titre={t('clm.cr.etape.documents')}>
        {docs.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : (
          <ul className="clm-liste">{docs.map((x) => <li key={x.id}>{x.nom} v{x.version} <Statut valeur={x.analyse} /></li>)}</ul>
        )}
      </Bloc>
      <Bloc zone="R4" titre={t('clm.cr.etape.participants')}>
        {d.participants.length === 0 ? <p className="muted">{t('clm.part.aucun')}</p> : (
          <ul className="clm-liste">{d.participants.map((p) => <li key={p.id}>{p.nom} · {roles.libelle(p.roleContrat)} · {t(`clm.niveau.${p.niveauIdentite}`)}</li>)}</ul>
        )}
      </Bloc>
      <Bloc zone="R5" titre={t('clm.pc.resume')} large>
        {phases.length === 0 ? <p className="muted">{t('clm.dt.aucuneEtape')}</p> : (
          <ol className="clm-chrono">
            {phases.map((p) => {
              const de = d.etapes.filter((e) => e.phase === p)
              return <li key={p}>{t('clm.pc.phaseN', { n: p })} — {de.length > 1 ? t('clm.pc.parallele') : t('clm.pc.sequentiel')} : {de.map((e) => `${e.libelle} (${t(`clm.type.${e.type}`)})`).join(' + ')}</li>
            })}
          </ol>
        )}
      </Bloc>
    </Blocs>
  )
}

/** UI-03 — Créer une trousse : assistant en cinq étapes (source, documents, participants, processus, récapitulatif). */
function Contenu() {
  const { t } = useI18n()
  const navigate = useNavigate()
  const versErreur = useErreur()
  const { actif: moduleActif } = useModules()
  const [params, setParams] = useSearchParams()
  const id = params.get('id')
  const etape = Math.min(ETAPES.length - 1, Math.max(0, Number(params.get('etape')) || 0))
  const repere = useRef(null)
  const etapePrecedente = useRef(etape)
  const [form, setForm] = useState(FORM_VIDE)
  const [dossier, setDossier] = useState(null)
  const [import_, setImport] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [enCours, setEnCours] = useState(false)
  const [enregistreLe, setEnregistreLe] = useState(null)
  const detail = useRessource(id ? `/clm/trousses/${id}` : null, !!id)
  const { donnees: modeles } = useRessource('/clm/modeles?statut=PUBLIE')
  const { donnees: publics } = useRessource('/clm/parametres')
  const champsSf = useListe('CLM_CHAMP_SALESFORCE')
  const d = id ? detail.donnees : null
  const peut = (a) => !!d?.actions?.includes(a)

  useEffect(() => { if (d) setForm(depuisDetail(d)) }, [d?.resume?.id]) // eslint-disable-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (id || !publics) return
    setForm((f) => ({ ...f, rappelFrequenceJours: f.rappelFrequenceJours === '' ? publics.rappelFrequenceJours : f.rappelFrequenceJours,
      rappelsMax: f.rappelsMax === '' ? publics.rappelsMax : f.rappelsMax }))
  }, [publics, id])
  useEffect(() => {
    if (etapePrecedente.current === etape) return
    etapePrecedente.current = etape
    repere.current?.focus()
  }, [etape])

  const aller = (i) => setParams(id ? { id, etape: String(i) } : { etape: String(i) })

  async function rechercherSf() {
    setErreur(null)
    setDossier(null)
    if (!form.sfDossierId.trim()) { setErreur(t('clm.cr.sfRequis')); return }
    try {
      const { data } = await api.get(`/clm/salesforce/dossiers/${encodeURIComponent(form.sfDossierId.trim())}`)
      setDossier(data)
      // Titre suggéré : les deux premiers champs renseignés de la liste de l'organisation, hors numéro de demande.
      const suggestion = champsSalesforce(champsSf, data).map(([k]) => k).filter((k) => k !== 'Name' && data.champs?.[k])
        .slice(0, 2).map((k) => data.champs[k]).join(' — ')
      if (!form.titre.trim() && suggestion) setForm((f) => ({ ...f, titre: suggestion }))
      setInfo(t('clm.cr.sfTrouve', { numero: data.numero }))
    } catch (e) { setErreur(versErreur(e)) }
  }

  async function importer(trousseId) {
    const p = await api.post(`/clm/trousses/${trousseId}/preremplissage`, { sfDossierId: form.sfDossierId.trim() })
    setImport(p.data)
  }

  async function enregistrer() {
    if (form.source === 'SALESFORCE' && !form.sfDossierId.trim()) { setErreur(t('clm.cr.sfRequis')); return null }
    if (!form.titre.trim()) { setErreur(t('clm.cr.titreRequis')); return null }
    setEnCours(true)
    setErreur(null)
    try {
      const corps = { ...form, modeleId: form.modeleId ? Number(form.modeleId) : null, echeance: form.echeance || null,
        reference: form.reference || null, sfDossierId: null, rappelFrequenceJours: nombreOuNul(form.rappelFrequenceJours),
        rappelsMax: nombreOuNul(form.rappelsMax) }
      const r = id ? await api.put(`/clm/trousses/${id}`, corps) : await api.post('/clm/trousses', corps)
      const nouvelId = String(r.data.resume.id)
      setEnregistreLe(new Date().toISOString())
      setInfo(t('clm.cr.brouillonEnregistre', { ref: r.data.resume.reference }))
      if (form.source === 'SALESFORCE' && !r.data.resume.sfDossierId) {
        try { await importer(nouvelId) } catch (e) { setErreur(versErreur(e)) }
      }
      return nouvelId
    } catch (e) {
      setErreur(versErreur(e))
      return null
    } finally {
      setEnCours(false)
    }
  }

  async function enregistrerBrouillon() {
    const n = await enregistrer()
    if (!n) return
    if (n === id) detail.recharger()
    else setParams({ id: n, etape: '0' })
  }
  async function continuer() {
    if (etape > 0) { aller(etape + 1); return }
    const n = await enregistrer()
    if (!n) return
    if (n === id) detail.recharger()
    setParams({ id: n, etape: '1' })
  }
  async function actualiserImport() {
    setErreur(null)
    try { await importer(id); setInfo(t('clm.dt.actualise')); detail.recharger() } catch (e) { setErreur(versErreur(e)) }
  }
  async function lancer() {
    if (!window.confirm(t('clm.dt.confirmerLancer'))) return
    setErreur(null)
    try { await api.post(`/clm/trousses/${id}/lancer`); navigate(`/clm/trousses/${id}`) } catch (e) { setErreur(versErreur(e)) }
  }

  const actions = etape === 0 ? (
    <>
      <button type="button" className="secondary" onClick={enregistrerBrouillon} disabled={enCours} aria-busy={enCours}>{t('clm.cr.enregistrerBrouillon')}</button>
      <button type="button" onClick={continuer} disabled={enCours}>{t('clm.continuer')}</button>
      <Link className="clm-bouton secondaire" to={id ? `/clm/trousses/${id}` : '/clm/trousses'}>{t('clm.annuler')}</Link>
    </>
  ) : (
    <>
      <button type="button" className="secondary" onClick={() => aller(etape - 1)}>{t('clm.precedent')}</button>
      {etape < ETAPES.length - 1 && <button type="button" onClick={continuer}>{t('clm.continuer')}</button>}
      {etape === ETAPES.length - 1 && peut('LANCER') && <button type="button" onClick={lancer}>{t('clm.dt.lancer')}</button>}
      <Link className="clm-bouton secondaire" to={`/clm/trousses/${id}`}>{t('clm.cr.terminerPlusTard')}</Link>
    </>
  )

  return (
    <EcranClm maquette="UI-03" titre={id ? t('clm.cr.titreModifier') : t('clm.cr.titre')} objectif={t('clm.cr.objectif')}
      ariane={<><Link to="/clm/trousses">{t('clm.tr.titre')}</Link> / {d ? d.resume.reference : t('clm.cr.nouvelle')}</>}
      actions={actions}>
      <Blocs>
        <Bloc zone="01" titre={t('clm.cr.progression')} large>
          <ol className="clm-etapes">
            {ETAPES.map((x, i) => {
              const accessible = i === 0 || !!d
              const libelle = <><span aria-hidden="true">{i < etape ? '✔' : i + 1}</span> {t(`clm.cr.etape.${x}`)}</>
              return (
                <li key={x} aria-current={i === etape ? 'step' : undefined} className={i === etape ? 'is-actif' : i < etape ? 'is-fait' : ''}>
                  {accessible && i !== etape ? <button type="button" className="lien" onClick={() => aller(i)}>{libelle}</button> : libelle}
                </li>
              )
            })}
          </ol>
          <p ref={repere} tabIndex={-1} className="clm-etape-courante">{t('clm.cr.etapeSur', { n: etape + 1, total: ETAPES.length, nom: t(`clm.cr.etape.${ETAPES[etape]}`) })}</p>
        </Bloc>
      </Blocs>
      <Message erreur={erreur || (id ? detail.erreur : null)} info={info} />
      {etape === 0 && (
        <EtapeSource form={form} setForm={setForm} modeles={modeles} dossier={dossier} onRechercher={rechercherSf} publics={publics}
          d={d} import_={import_} enregistreLe={enregistreLe} onImporter={actualiserImport}
          onValeurs={(vue) => { if (vue.sfDossierId) setImport(vue); detail.recharger() }} />
      )}
      {etape > 0 && !d && <p role="status">{t('clm.chargement')}</p>}
      {etape === 1 && d && (
        <OngletDocuments trousse={d} peutGerer={peut('AJOUTER_DOCUMENT')} peutGenerer={peut('GENERER') && !!d.modeleId}
          iaActive={moduleActif('clm_ia')} onChange={detail.recharger} />
      )}
      {etape === 2 && d && <OngletParticipants trousse={d} peutGerer={peut('PARTICIPANTS')} onChange={detail.recharger} onOrdre={() => aller(3)} />}
      {etape === 3 && d && <OngletParcours trousse={d} peutDefinir={peut('PARCOURS')} peutReassigner={false} onChange={detail.recharger} />}
      {etape === 4 && d && <Recapitulatif d={d} form={form} modeles={modeles} />}
    </EcranClm>
  )
}

export default function TrousseCreer() {
  return <GardeModule><Contenu /></GardeModule>
}
