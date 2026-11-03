import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { Message, Statut, Visionneuse, dateHeure, taille, telecharger, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs } from './gabarit.jsx'

const ACCEPT = '.pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document'

/** UI-05 / UI-06 — Documents : dépôt PDF/Word, liste, antivirus, métadonnées, versions ; option IA (UI-24). */
export default function OngletDocuments({ trousse, peutGerer, peutGenerer, iaActive, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [enCours, setEnCours] = useState(false)
  const [survol, setSurvol] = useState(false)
  const [apercu, setApercu] = useState(null)
  const [versionDe, setVersionDe] = useState(null)
  const [motif, setMotif] = useState('')
  const [historiqueDe, setHistoriqueDe] = useState(null)
  const [metaDe, setMetaDe] = useState(null)
  const fichierVersion = useRef(null)
  const analyses = useRessource(iaActive ? `/clm/trousses/${trousse.resume.id}/analyses` : null, iaActive)
  const id = trousse.resume.id
  const parCle = {}
  trousse.documents.forEach((d) => { (parCle[d.cleDocument] ||= []).push(d) })
  const courants = Object.values(parCle).map((v) => v[0])
  const cleHistorique = historiqueDe || courants[0]?.cleDocument
  const versions = parCle[cleHistorique] || []
  const finale = versions.find((v) => v.statut === 'SIGNE')
  const modifiable = (doc) => peutGerer && !['SIGNE', 'RETIRE'].includes(doc.statut)
  const categorie = (doc) => (analyses.donnees || []).find((a) => a.documentId === doc.id)?.resultat?.categorie
  const meta = versions.find((v) => v.id === metaDe)
  const suivi = versions[0]

  useEffect(() => { if (versionDe) fichierVersion.current?.focus() }, [versionDe])

  async function envoyer(fichier, url, extra) {
    if (!fichier) return
    setErreur(null); setEnCours(true); setInfo(t('clm.doc.enCours'))
    const fd = new FormData()
    fd.append('fichier', fichier)
    Object.entries(extra || {}).forEach(([k, v]) => fd.append(k, v))
    try {
      await api.post(url, fd)
      setInfo(t('clm.doc.depose', { nom: fichier.name }))
      setVersionDe(null); setMotif('')
      onChange()
    } catch (e) {
      setInfo('')
      setErreur(e.response ? versErreur(e) : t('clm.doc.echecReseau'))
    } finally {
      setEnCours(false)
    }
  }
  async function executer(fn, ok) {
    setErreur(null)
    try { await fn(); setInfo(ok); onChange(); analyses.recharger() } catch (e) { setErreur(versErreur(e)) }
  }
  const charger = (doc) => telecharger(`/clm/trousses/${id}/documents/${doc.id}/contenu`, doc.nom).catch((e) => setErreur(versErreur(e)))
  function deposer(e) {
    e.preventDefault()
    setSurvol(false)
    if (peutGerer && !enCours) envoyer(e.dataTransfer.files[0], `/clm/trousses/${id}/documents`)
  }

  return (
    <div data-maquette="UI-05">
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.doc.blocDepot')}>
          {peutGerer ? (
            <div className={`clm-depose ${survol ? 'is-survol' : ''}`} onDragOver={(e) => { e.preventDefault(); setSurvol(true) }}
              onDragLeave={() => setSurvol(false)} onDrop={deposer}>
              <p>{t('clm.doc.glisser')}</p>
              <label>{t('clm.doc.deposer')}
                <input type="file" accept={ACCEPT} disabled={enCours} onChange={(e) => { envoyer(e.target.files[0], `/clm/trousses/${id}/documents`); e.target.value = '' }} />
              </label>
              {peutGenerer && <button type="button" className="secondary" onClick={() => executer(() => api.post(`/clm/trousses/${id}/generer`), t('clm.doc.genere'))}>{t('clm.doc.generer')}</button>}
            </div>
          ) : <p className="muted">{t('clm.doc.depotFerme')}</p>}
        </Bloc>
        <Bloc zone="02" titre={t('clm.doc.blocFormats')}>
          <p>{t('clm.doc.formats', { n: 25 })}</p>
          <ul className="clm-liste">
            <li>{t('clm.doc.formatPdf')}</li>
            <li>{t('clm.doc.formatWord')}</li>
          </ul>
        </Bloc>
        <Bloc zone="03" titre={t('clm.doc.blocListe')} large>
          {courants.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.doc.blocListe')}</caption>
                <thead><tr>
                  <th scope="col">{t('clm.col.nom')}</th><th scope="col">{t('clm.col.type')}</th><th scope="col">{t('clm.col.taille')}</th>
                  <th scope="col">{t('clm.col.version')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.proprietaire')}</th>
                  <th scope="col">{t('clm.col.actions')}</th>
                </tr></thead>
                <tbody>
                  {courants.map((doc) => (
                    <tr key={doc.cleDocument}>
                      <td data-label={t('clm.col.nom')}>{doc.nom}</td>
                      <td data-label={t('clm.col.type')}>{doc.type}</td>
                      <td data-label={t('clm.col.taille')}>{taille(doc.taille)}</td>
                      <td data-label={t('clm.col.version')}>v{doc.version}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={doc.statut} /></td>
                      <td data-label={t('clm.col.proprietaire')}>{doc.auteur}<br /><span className="muted">{dateHeure(doc.creeLe)}</span></td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        <button type="button" className="lien" aria-label={`${t('clm.doc.apercu')} — ${doc.nom}`} onClick={() => setApercu(doc)}>{t('clm.doc.apercu')}</button>
                        <button type="button" className="lien" aria-label={`${t('clm.doc.telecharger')} — ${doc.nom}`} onClick={() => charger(doc)}>{t('clm.doc.telecharger')}</button>
                        <button type="button" className="lien" aria-label={`${t('clm.doc.voirVersions')} — ${doc.nom}`} onClick={() => setHistoriqueDe(doc.cleDocument)}>{t('clm.doc.voirVersions')}</button>
                        {modifiable(doc) && <button type="button" className="lien" aria-label={t('clm.doc.remplacerPar', { nom: doc.nom })} onClick={() => setVersionDe(doc.cleDocument)}>{t('clm.doc.remplacer')}</button>}
                        {modifiable(doc) && <button type="button" className="lien danger" aria-label={t('clm.doc.retirerAvant', { nom: doc.nom })}
                          onClick={() => window.confirm(t('clm.doc.confirmerRetrait')) && executer(() => api.delete(`/clm/trousses/${id}/documents/${doc.id}`), t('clm.doc.retire'))}>{t('clm.doc.retirer')}</button>}
                        {iaActive && doc.analyse === 'PROPRE' && doc.statut !== 'RETIRE' && <button type="button" className="lien" aria-label={`${t('clm.ia.lancer')} — ${doc.nom}`}
                          onClick={() => executer(() => api.post(`/clm/trousses/${id}/documents/${doc.id}/analyse`), t('clm.ia.lancee'))}>{t('clm.ia.lancer')}</button>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {versionDe && (
            <form className="form clm-sousform" onSubmit={(e) => { e.preventDefault(); envoyer(e.target.fichier.files[0], `/clm/trousses/${id}/documents/${versionDe}/versions`, { motif }) }}>
              <h3>{t('clm.doc.remplacerDe', { nom: parCle[versionDe][0].nom })}</h3>
              <label>{t('clm.doc.fichier')}<input ref={fichierVersion} name="fichier" type="file" accept={ACCEPT} required /></label>
              <label>{t('clm.col.motif')}<input value={motif} onChange={(e) => setMotif(e.target.value)} required /></label>
              <div className="cmp-row"><button type="button" className="secondary" onClick={() => setVersionDe(null)}>{t('clm.annuler')}</button><button type="submit" disabled={enCours}>{t('clm.doc.remplacer')}</button></div>
            </form>
          )}
        </Bloc>
        {apercu && (
          <Bloc titre={t('clm.doc.blocApercu')} large>
            <Visionneuse trousseId={id} document={apercu} onFermer={() => setApercu(null)} />
          </Bloc>
        )}
        <Bloc zone="04" titre={t('clm.doc.blocAntivirus')}>
          {courants.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : (
            <ul className="clm-liste">
              {courants.map((doc) => <li key={doc.cleDocument}>{doc.nom} : <Statut valeur={doc.analyse} /> <Statut valeur={doc.statut} /></li>)}
            </ul>
          )}
          {enCours && <p role="status">{t('clm.doc.enCours')}</p>}
        </Bloc>
        <Bloc zone="05" titre={t('clm.doc.blocMetadonnees')}>
          {courants.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : courants.map((doc) => (
            <dl key={doc.cleDocument} className="clm-dl clm-dl--compact" aria-label={doc.nom}>
              <div><dt>{t('clm.doc.cle')}</dt><dd>{doc.cleDocument}</dd></div>
              <div><dt>{t('clm.col.empreinte')}</dt><dd><code title={doc.empreinte}>{doc.empreinte.slice(0, 16)}…</code></dd></div>
              <div><dt>{t('clm.doc.classement')}</dt><dd>{categorie(doc) || '—'}</dd></div>
            </dl>
          ))}
        </Bloc>
        {courants.length > 0 && (
          <Bloc titre={t('clm.doc.blocVersions')} large>
            <label className="clm-champ-court">{t('clm.doc.documentSuivi')}
              <select value={cleHistorique} onChange={(e) => { setHistoriqueDe(e.target.value); setMetaDe(null) }}>
                {courants.map((doc) => <option key={doc.cleDocument} value={doc.cleDocument}>{doc.nom}</option>)}
              </select>
            </label>
            <div data-maquette="UI-06">
              <Blocs>
                <Bloc zone="01" niveau={3} titre={t('clm.doc.blocListeVersions')} large>
                  <div className="table-scroll">
                    <table className="table clm-table">
                      <caption className="sr-only">{t('clm.doc.versions', { n: versions.length })}</caption>
                      <thead><tr><th scope="col">{t('clm.col.version')}</th><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.col.auteur')}</th>
                        <th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                      <tbody>
                        {versions.map((v) => (
                          <tr key={v.id}>
                            <td data-label={t('clm.col.version')}>v{v.version}</td>
                            <td data-label={t('clm.col.date')}>{dateHeure(v.creeLe)}</td>
                            <td data-label={t('clm.col.auteur')}>{v.auteur}</td>
                            <td data-label={t('clm.col.statut')}><Statut valeur={v.statut} /></td>
                            <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                              <button type="button" className="lien" aria-label={`${t('clm.doc.telechargerVersion')} v${v.version}`} onClick={() => charger(v)}>{t('clm.doc.telechargerVersion')}</button>
                              <button type="button" className="lien" aria-pressed={metaDe === v.id} aria-label={t('clm.doc.consulterMeta', { v: v.version })} onClick={() => setMetaDe(v.id)}>{t('clm.doc.metadonnees')}</button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </Bloc>
                <Bloc zone="02" niveau={3} titre={t('clm.doc.blocMetaVersion')}>
                  {!meta ? <p className="muted">{t('clm.doc.choisirVersion')}</p> : (
                    <dl className="clm-dl" aria-label={t('clm.doc.consulterMeta', { v: meta.version })}>
                      <div><dt>{t('clm.col.version')}</dt><dd>v{meta.version} · {meta.type} · {taille(meta.taille)}</dd></div>
                      <div><dt>{t('clm.col.auteur')}</dt><dd>{meta.auteur}</dd></div>
                      <div><dt>{t('clm.col.date')}</dt><dd>{dateHeure(meta.creeLe)}</dd></div>
                      <div><dt>{t('clm.col.motif')}</dt><dd>{meta.motif || '—'}</dd></div>
                      <div><dt>{t('clm.col.empreinte')}</dt><dd><code className="clm-empreinte">{meta.empreinte}</code></dd></div>
                      <div><dt>{t('clm.col.statut')}</dt><dd><Statut valeur={meta.statut} /></dd></div>
                    </dl>
                  )}
                </Bloc>
                <Bloc zone="03" niveau={3} titre={t('clm.doc.blocEtatVersions')}>
                  <ul className="clm-liste">
                    {versions.map((v) => <li key={v.id}>v{v.version} : <Statut valeur={v.statut} /></li>)}
                  </ul>
                  <p className="muted">{t('clm.doc.etatsAide')}</p>
                </Bloc>
                <Bloc zone="04" niveau={3} titre={t('clm.doc.blocFinale')} large>
                  <p>{finale ? t('clm.doc.finale', { v: finale.version }) : t('clm.doc.pasDeFinale')}
                    {finale && <> <button type="button" className="lien" aria-label={`${t('clm.doc.telechargerVersion')} v${finale.version}`} onClick={() => charger(finale)}>{t('clm.doc.telecharger')}</button></>}</p>
                  <div className="cmp-row">
                    <Link className="clm-bouton secondaire" to={`/clm/trousses/${id}?onglet=historique`}>{t('clm.doc.historiqueModifs')}</Link>
                    {suivi && modifiable(suivi) && <button type="button" className="secondary" onClick={() => setVersionDe(suivi.cleDocument)}>{t('clm.doc.creerVersion')}</button>}
                  </div>
                </Bloc>
              </Blocs>
            </div>
          </Bloc>
        )}
      </Blocs>
      {iaActive && <AnalysesIa analyses={analyses} onChange={analyses.recharger} />}
    </div>
  )
}

/** UI-24 — Résultats d'analyse : niveau de confiance, validation humaine justifiée. */
function AnalysesIa({ analyses, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [justif, setJustif] = useState({})
  const [erreur, setErreur] = useState(null)
  if (!analyses.donnees || analyses.donnees.length === 0) return null
  async function reviser(a, valide) {
    setErreur(null)
    try { await api.post(`/clm/analyses/${a.id}/revision`, { valide, justification: justif[a.id] || '' }); onChange() } catch (e) { setErreur(versErreur(e)) }
  }
  return (
    <Blocs>
      <Bloc maquette="UI-24" titre={t('clm.ia.titre')} large>
        <p className="muted">{t('clm.ia.avertissement')}</p>
        <Message erreur={erreur} />
        {analyses.donnees.map((a) => (
          <article key={a.id} className="clm-carte">
            <p className="clm-meta"><Statut valeur={a.statut} /> <span>{dateHeure(a.creeLe)} · {a.lanceePar}</span></p>
            <p>{t('clm.ia.categorie', { c: a.resultat.categorie, p: Math.round((a.resultat.confianceCategorie || 0) * 100) })}</p>
            <ul className="clm-liste">
              {(a.resultat.metadonnees || []).map((m) => <li key={m.cle}>{m.cle} : <strong>{m.valeur}</strong> ({Math.round(m.confiance * 100)} %)</li>)}
              {(a.resultat.anomalies || []).map((m) => <li key={m.cle + m.valeur}><span aria-hidden="true">⚠</span> {t(`clm.ia.anomalie.${m.cle}`)} : {m.valeur} ({Math.round(m.confiance * 100)} %)</li>)}
            </ul>
            {a.statut === 'A_REVISER' ? (
              <div className="form">
                <label>{t('clm.ia.justification')}<textarea rows={2} value={justif[a.id] || ''} onChange={(e) => setJustif({ ...justif, [a.id]: e.target.value })} /></label>
                <div className="cmp-row"><button type="button" onClick={() => reviser(a, true)}>{t('clm.ia.valider')}</button>
                  <button type="button" className="danger" onClick={() => reviser(a, false)}>{t('clm.ia.signalerErreur')}</button></div>
              </div>
            ) : <p className="muted">{t('clm.ia.revisee', { par: a.reviseePar, j: a.justification })}</p>}
          </article>
        ))}
      </Bloc>
    </Blocs>
  )
}
