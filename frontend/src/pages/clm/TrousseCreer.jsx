import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import { GardeModule, Message, Statut, date, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'
import OngletDocuments from './OngletDocuments.jsx'
import OngletParticipants from './OngletParticipants.jsx'
import OngletParcours from './OngletParcours.jsx'

const ETAPES = ['source', 'documents', 'participants', 'processus', 'recapitulatif']
const RISQUES = ['FAIBLE', 'MOYEN', 'ELEVE']
const CHAMPS_SF = ['Demandeur', 'Programme', 'Montant', 'Entite']
const FORM_VIDE = { source: 'MANUELLE', sfDossierId: '', modeleId: '', titre: '', reference: '', typeContrat: '', unite: '',
  risque: 'MOYEN', echeance: '', copies: '', rappelFrequenceJours: 2, rappelsMax: 3 }
const ACTIFS = (docs) => docs.filter((x) => !['REMPLACE', 'RETIRE'].includes(x.statut))

const depuisDetail = (d) => ({ source: d.resume.source, sfDossierId: d.resume.sfDossierId || '', modeleId: d.modeleId ? String(d.modeleId) : '',
  titre: d.resume.titre, reference: d.resume.reference, typeContrat: d.resume.typeContrat || '', unite: d.resume.unite || '', risque: d.risque,
  echeance: d.resume.echeance || '', copies: d.copies || '', rappelFrequenceJours: d.rappelFrequenceJours, rappelsMax: d.rappelsMax })

/** Résumé d'import Salesforce (UI-10) : provenance, date de récupération, champs manquants ou en conflit. */
export function ResumePreremplissage({ vue }) {
  const { t } = useI18n()
  if (!vue) return null
  return (
    <div>
      <p>{t('clm.sf.dossier', { numero: vue.numero, id: vue.sfDossierId })} · {t('clm.sf.recupereLe', { d: dateHeure(vue.recupereLe) })}</p>
      <dl className="clm-dl">
        {['Demandeur', 'Programme', 'Montant', 'Entite'].map((k) => (
          <div key={k}><dt>{t(`clm.sf.champ.${k}`)}</dt><dd>{vue.dossier?.[k] ?? '—'}</dd></div>
        ))}
      </dl>
      {vue.correspondances.length > 0 && (
        <div className="table-scroll">
          <table className="table clm-table">
            <caption>{t('clm.sf.correspondance')}</caption>
            <thead><tr><th scope="col">{t('clm.sf.cible')}</th><th scope="col">{t('clm.sf.source')}</th>
              <th scope="col">{t('clm.col.valeur')}</th><th scope="col">{t('clm.sf.provenance')}</th></tr></thead>
            <tbody>
              {vue.correspondances.map((c) => (
                <tr key={c.champ}>
                  <td data-label={t('clm.sf.cible')}>{c.libelle}</td>
                  <td data-label={t('clm.sf.source')}>{c.source || t('clm.sf.mappingAbsent')}</td>
                  <td data-label={t('clm.col.valeur')}>{c.valeur ?? '—'}</td>
                  <td data-label={t('clm.sf.provenance')}>{c.provenance ? t(`clm.provenance.${c.provenance}`) : t('clm.sf.manquant')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {vue.manquants.length > 0 && <p className="erreur" role="alert">{t('clm.sf.manquants', { liste: vue.manquants.join(', ') })}</p>}
      {vue.conflits.length > 0 && (
        <ul className="clm-liste" aria-label={t('clm.sf.conflits')}>
          {vue.conflits.map((c) => <li key={c.cle}>{t('clm.sf.conflit', { cle: c.cle, source: c.valeurSource, manuelle: c.valeurManuelle })}</li>)}
        </ul>
      )}
    </div>
  )
}

/** Aperçu d'un dossier Salesforce avant import (UI-10, zone 02). */
function ApercuDossier({ dossier }) {
  const { t } = useI18n()
  if (!dossier) return null
  return (
    <div className="clm-apercu">
      <p>{t('clm.sf.dossier', { numero: dossier.numero, id: dossier.id })} · {t('clm.sf.recupereLe', { d: dateHeure(dossier.recupereLe) })}</p>
      <dl className="clm-dl">
        {CHAMPS_SF.map((k) => <div key={k}><dt>{t(`clm.sf.champ.${k}`)}</dt><dd>{String(dossier.champs?.[k] ?? '—')}</dd></div>)}
      </dl>
    </div>
  )
}

/** Étape 1 (UI-03 + UI-10) : origine, dossier Salesforce, informations du contrat, brouillon, données importées. */
function EtapeSource({ form, setForm, modeles, dossier, onRechercher, d, import_, enregistreLe, onImporter }) {
  const { t } = useI18n()
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'number' ? Number(e.target.value) : e.target.value })
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
          <div className="form clm-sousform">
            <label>{t('clm.cr.sfId')} <span className="muted">{t('clm.cr.sfAide')}</span>
              <input value={form.sfDossierId} onChange={maj('sfDossierId')} required aria-required="true" autoComplete="off" />
            </label>
            <div className="cmp-row"><button type="button" className="secondary" onClick={onRechercher}>{t('clm.rechercher')}</button></div>
            <ApercuDossier dossier={dossier} />
          </div>
        )}
      </Bloc>
      <Bloc zone="03" titre={t('clm.cr.blocInformations')}>
        <div className="form-grid">
          <label className="col-2">{t('clm.champ.titre')} <span aria-hidden="true">*</span>
            <input value={form.titre} onChange={maj('titre')} required aria-required="true" maxLength={250} /></label>
          <label>{t('clm.champ.reference')} <span className="muted">{t('clm.cr.referenceAide')}</span>
            <input value={form.reference} onChange={maj('reference')} maxLength={60} disabled={!!d} /></label>
          <label>{t('clm.champ.typeContrat')}<input value={form.typeContrat} onChange={maj('typeContrat')} maxLength={80} /></label>
          <label>{t('clm.champ.unite')}<input value={form.unite} onChange={maj('unite')} maxLength={120} /></label>
          <label>{t('clm.cr.modele')}
            <select value={form.modeleId} onChange={maj('modeleId')} disabled={!!d}>
              <option value="">{t('clm.cr.sansModele')}</option>
              {(modeles || []).map((m) => <option key={m.id} value={m.id}>{m.nom} · v{m.numero}</option>)}
            </select>
          </label>
          <label>{t('clm.champ.risque')}
            <select value={form.risque} onChange={maj('risque')}>{RISQUES.map((r) => <option key={r} value={r}>{t(`clm.risque.${r}`)}</option>)}</select>
          </label>
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
      <Bloc zone="05" titre={t('clm.cr.blocImport')}>
        {import_ ? <ResumePreremplissage vue={import_} /> : d && Object.keys(d.valeurs).length > 0 ? (
          <dl className="clm-dl">
            {Object.entries(d.valeurs).map(([k, v]) => (
              <div key={k}><dt>{k}</dt><dd>{String(v.valeur)} <span className="muted">({t(`clm.provenance.${v.provenance}`)}{v.recupereLe ? ` · ${dateHeure(v.recupereLe)}` : ''})</span></dd></div>
            ))}
          </dl>
        ) : <p className="muted">{form.source === 'SALESFORCE' ? t('clm.cr.importApres') : t('clm.cr.importManuel')}</p>}
        {d && form.source === 'SALESFORCE' && form.sfDossierId && (
          <button type="button" className="secondary" onClick={onImporter}>{t('clm.cr.importer')}</button>
        )}
      </Bloc>
    </Blocs>
  )
}

/** Étape 5 : récapitulatif et contrôles avant lancement. */
function Recapitulatif({ d, form, modeles }) {
  const { t } = useI18n()
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
          <div><dt>{t('clm.champ.unite')}</dt><dd>{d.resume.unite || '—'}</dd></div>
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
          <ul className="clm-liste">{d.participants.map((p) => <li key={p.id}>{p.nom} · {p.roleContrat} · {t(`clm.niveau.${p.niveauIdentite}`)}</li>)}</ul>
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
  const d = id ? detail.donnees : null
  const peut = (a) => !!d?.actions?.includes(a)

  useEffect(() => { if (d) setForm(depuisDetail(d)) }, [d?.resume?.id]) // eslint-disable-line react-hooks/exhaustive-deps
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
      if (!form.titre.trim()) setForm((f) => ({ ...f, titre: [data.champs?.Programme, data.champs?.Demandeur].filter(Boolean).join(' — ') }))
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
        reference: form.reference || null, sfDossierId: null }
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
        <EtapeSource form={form} setForm={setForm} modeles={modeles} dossier={dossier} onRechercher={rechercherSf}
          d={d} import_={import_} enregistreLe={enregistreLe} onImporter={actualiserImport} />
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
