import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useAuth } from '../../auth.jsx'
import ReferenceManager from '../../components/ReferenceManager.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, Statut, dateHeure, taille, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const CANAUX = ['COURRIEL', 'SMS', 'INTERNE']
const exemple = (t) => ({ reference: 'TR-2026-00001', titre: t('clm.ad.exemple.titre'), etape: t('clm.ad.exemple.etape'),
  destinataire: t('clm.ad.exemple.destinataire'), lien: 'https://exemple.ca/portail/jeton', echeance: '2026-07-01', code: '123456', motif: t('clm.ad.exemple.motif') })
const rendre = (texte, valeurs) => (texte || '').replace(/\{\{(\w+)\}\}/g, (_, k) => valeurs[k] ?? `{{${k}}}`)

/** UI-17 — Notifications et rappels : catalogue des événements, canaux, modèles et variables, règles de rappel, aperçu et test. */
function Notifications() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees, recharger } = useRessource('/clm/admin/messages')
  const { donnees: p } = useRessource('/clm/admin/parametres')
  const [edite, setEdite] = useState(null)
  const [test, setTest] = useState('')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const messages = donnees || []

  async function enregistrer(m, ok) {
    setErreur(null)
    try { await api.put('/clm/admin/messages', m); setInfo(ok); recharger() } catch (x) { setErreur(versErreur(x)) }
  }
  async function tester() {
    setErreur(null)
    try { const { data } = await api.post(`/clm/admin/messages/${edite.evenement}/test`, { canal: edite.canal, destinataire: test }); setInfo(t('clm.ad.testStatut', { s: data.statut })) } catch (x) { setErreur(versErreur(x)) }
  }
  const basculer = (m) => enregistrer({ ...m, actif: !m.actif }, m.actif ? t('clm.ad.desactive') : t('clm.ad.active'))

  return (
    <EcranClm maquette="UI-17" titre={t('clm.ad.notifications')} objectif={t('clm.ad.notifObjectif')}
      actions={(
        <>
          <button type="button" className="secondary" disabled={!edite} onClick={() => { basculer(edite); setEdite({ ...edite, actif: !edite.actif }) }}>{t('clm.ad.activerDesactiver')}</button>
          <button type="button" className="secondary" disabled={!edite} onClick={() => document.getElementById('clm-msg-sujet')?.focus()}>{t('clm.ad.modifierModele')}</button>
          <Link className="clm-bouton secondaire" to="/clm/admin/parametres">{t('clm.ad.configurerFrequence')}</Link>
          <button type="button" disabled={!edite || !test} onClick={tester}>{t('clm.ad.envoyerTest')}</button>
        </>
      )}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.ad.blocCatalogue')} large>
          <div className="table-scroll">
            <table className="table clm-table">
              <caption className="sr-only">{t('clm.ad.blocCatalogue')}</caption>
              <thead><tr><th scope="col">{t('clm.col.evenement')}</th><th scope="col">{t('clm.col.canal')}</th><th scope="col">{t('clm.col.sujet')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
              <tbody>
                {messages.map((m) => (
                  <tr key={`${m.evenement}-${m.canal}`} className={edite?.evenement === m.evenement && edite?.canal === m.canal ? 'is-choisie' : ''}>
                    <td data-label={t('clm.col.evenement')}>{t(`clm.ad.evt.${m.evenement}`)}</td>
                    <td data-label={t('clm.col.canal')}>{t(`clm.canal.${m.canal}`)}</td>
                    <td data-label={t('clm.col.sujet')}>{m.sujet}</td>
                    <td data-label={t('clm.col.statut')}>{m.actif ? t('clm.ad.actif') : t('clm.ad.inactif')}{m.id ? '' : ` · ${t('clm.ad.parDefaut')}`}</td>
                    <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                      <button type="button" className="lien" aria-label={`${t('clm.modifier')} — ${t(`clm.ad.evt.${m.evenement}`)} ${t(`clm.canal.${m.canal}`)}`} onClick={() => setEdite({ ...m })}>{t('clm.modifier')}</button>
                      <button type="button" className="lien" aria-label={`${m.actif ? t('clm.ad.desactiver') : t('clm.ad.activer')} — ${t(`clm.ad.evt.${m.evenement}`)} ${t(`clm.canal.${m.canal}`)}`} onClick={() => basculer(m)}>{m.actif ? t('clm.ad.desactiver') : t('clm.ad.activer')}</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Bloc>
        <Bloc zone="02" titre={t('clm.ad.blocCanaux')}>
          <ul className="clm-liste">
            {CANAUX.map((c) => <li key={c}><strong>{t(`clm.canal.${c}`)}</strong> — {t(`clm.ad.fournisseur.${c}`)} ({t('clm.ad.nbMessages', { n: messages.filter((m) => m.canal === c && m.actif).length })})</li>)}
          </ul>
        </Bloc>
        <Bloc zone="04" titre={t('clm.ad.blocRappels')}>
          <p>{t('clm.ad.notifAide')}</p>
          {p && <p>{t('clm.ad.planificateur')} : {p.planificateurActif ? t('clm.ad.actifToutes', { s: Math.round(p.planificateurDelaiMs / 1000) }) : t('clm.ad.inactif')}</p>}
        </Bloc>
        <Bloc zone="03" titre={t('clm.ad.blocModeles')}>
          <p className="muted">{t('clm.ad.variables')}</p>
          {!edite ? <p>{t('clm.ad.choisirMessage')}</p> : (
            <form className="form" onSubmit={(e) => { e.preventDefault(); enregistrer(edite, t('clm.ad.msgEnregistre')) }}>
              <p><strong>{t(`clm.ad.evt.${edite.evenement}`)} · {t(`clm.canal.${edite.canal}`)}</strong></p>
              <label>{t('clm.col.sujet')}<input id="clm-msg-sujet" value={edite.sujet} onChange={(e) => setEdite({ ...edite, sujet: e.target.value })} required /></label>
              <label>{t('clm.ad.corps')}<textarea rows={5} value={edite.corps} onChange={(e) => setEdite({ ...edite, corps: e.target.value })} required /></label>
              <label className="check-row"><input type="checkbox" checked={edite.actif} onChange={(e) => setEdite({ ...edite, actif: e.target.checked })} />{t('clm.ad.actif')}</label>
              <div className="cmp-row"><button type="button" className="secondary" onClick={() => setEdite(null)}>{t('clm.annuler')}</button><button type="submit">{t('clm.enregistrer')}</button></div>
            </form>
          )}
        </Bloc>
        <Bloc zone="05" titre={t('clm.ad.blocApercu')}>
          {!edite ? <p className="muted">{t('clm.ad.choisirMessage')}</p> : (
            <>
              <div className="clm-apercu">
                <p><strong>{rendre(edite.sujet, exemple(t))}</strong></p>
                <pre tabIndex={0}>{rendre(edite.corps, exemple(t))}</pre>
              </div>
              <label>{t('clm.ad.destinataireTest')}<input type={edite.canal === 'SMS' ? 'tel' : 'email'} value={test} onChange={(e) => setTest(e.target.value)} /></label>
              <div className="cmp-row"><button type="button" className="secondary" disabled={!test} onClick={tester}>{t('clm.ad.envoyerTest')}</button></div>
            </>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

/** Connexion propre à l'organisation (UI-21, zone 02) ; un secret ou un identifiant client laissé vide conserve celui en place. */
function FormulaireConnexion({ config, secretRef, onEnregistrer, onAnnuler, onRetablir }) {
  const { t } = useI18n()
  const [f, setF] = useState(() => ({ mode: config.mode, instanceUrl: config.instanceUrl || '', tokenUrl: config.tokenUrl || '',
    clientId: '', clientSecret: '', objet: config.objet, versionApi: config.versionApi, champStatut: config.champStatut,
    champReference: config.champReference, champCorrelation: config.champCorrelation, reprisesMax: config.reprisesMax,
    authentification: config.authentification || 'CLIENT_CREDENTIALS', utilisateur: config.utilisateur || '' }))
  const jwt = f.authentification === 'JWT_BEARER'
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.type === 'number' ? Number(e.target.value) : e.target.value })
  const domaines = config.hotesAutorises.join(', ')
  return (
    <form id="clm-connexion-sf" className="form clm-sousform" onSubmit={(e) => { e.preventDefault(); onEnregistrer(f) }} aria-label={t('clm.ad.configurerConnexion')}>
      <p className="muted">{t('clm.ad.connexionAide')}</p>
      <div className="form-grid">
        <label>{t('clm.ad.mode')}
          <select value={f.mode} onChange={maj('mode')}>
            {['SIMULATION', 'REST'].map((m) => <option key={m} value={m}>{t(`clm.ad.modeSf.${m}`)}</option>)}
          </select>
        </label>
        <label>{t('clm.ad.reprisesMax')}<input type="number" min={1} max={20} value={f.reprisesMax} onChange={maj('reprisesMax')} required /></label>
        <label className="col-2">{t('clm.ad.instance')} <span className="muted">{t('clm.ad.domainesAutorises', { d: domaines })}</span>
          <input type="url" value={f.instanceUrl} onChange={maj('instanceUrl')} placeholder="https://" /></label>
        <label className="col-2">{t('clm.ad.serveurJetons')}<input type="url" value={f.tokenUrl} onChange={maj('tokenUrl')} placeholder="https://" /></label>
        <label>{t('clm.ad.clientId')} <span className="muted">{config.clientIdMasque ? t('clm.ad.conserverSiVide', { v: config.clientIdMasque }) : ''}</span>
          <input value={f.clientId} onChange={maj('clientId')} autoComplete="off" /></label>
        <label>{t('clm.ad.authentification')}
          <select value={f.authentification} onChange={maj('authentification')}>
            {['CLIENT_CREDENTIALS', 'JWT_BEARER'].map((m) => <option key={m} value={m}>{t(`clm.ad.auth.${m}`)}</option>)}
          </select>
        </label>
        {jwt && <label>{t('clm.ad.utilisateur')}<input value={f.utilisateur} onChange={maj('utilisateur')} autoComplete="off" required /></label>}
        <label className={jwt ? 'col-2' : undefined}>{t(jwt ? 'clm.ad.clePrivee' : 'clm.ad.secret')} <span className="muted">{config.secretConfigure ? t('clm.ad.secretConserve') : ''}</span>
          {jwt
            ? <textarea ref={secretRef} rows={4} value={f.clientSecret} onChange={maj('clientSecret')} autoComplete="off" spellCheck={false}
                placeholder="-----BEGIN PRIVATE KEY-----" />
            : <input ref={secretRef} type="password" value={f.clientSecret} onChange={maj('clientSecret')} autoComplete="new-password" />}</label>
        <label>{t('clm.ad.objet')}<input value={f.objet} onChange={maj('objet')} required /></label>
        <label>{t('clm.ad.versionApi')}<input value={f.versionApi} onChange={maj('versionApi')} required /></label>
        <label>{t('clm.ad.champStatut')}<input value={f.champStatut} onChange={maj('champStatut')} required /></label>
        <label>{t('clm.ad.champReference')}<input value={f.champReference} onChange={maj('champReference')} required /></label>
        <label>{t('clm.ad.champCorrelation')}<input value={f.champCorrelation} onChange={maj('champCorrelation')} required /></label>
      </div>
      <div className="cmp-row">
        <button type="button" className="secondary" onClick={onAnnuler}>{t('clm.annuler')}</button>
        {config.personnalisee && <button type="button" className="secondary" onClick={onRetablir}>{t('clm.ad.retablirConnexion')}</button>}
        <button type="submit">{t('clm.enregistrer')}</button>
      </div>
    </form>
  )
}

/** UI-21 — Intégrations API (Salesforce) : connexion, identifiants masqués, journal avec corrélation, erreurs et reprises, version. */
const SYSTEMES = ['WORKDAY', 'SALESFORCE', 'AUTRE']

/**
 * Clients d'intégration entrants (addenda 1 : Workday TI-22, lots Salesforce AF-47) : identifiant et secret remis une
 * seule fois, clé de signature des rappels, révocation ; puis les rappels de statut envoyés à ces systèmes.
 */
function ClientsIntegration() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: clients, recharger } = useRessource('/clm/admin/clients-integration')
  const { donnees: rappels, recharger: rechargerRappels } = useRessource('/clm/admin/rappels')
  const [f, setF] = useState({ nom: '', systeme: 'WORKDAY', urlRappel: '' })
  const [remis, setRemis] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.value })

  async function creer(e) {
    e.preventDefault()
    setErreur(null)
    try {
      const { data } = await api.post('/clm/admin/clients-integration', { ...f, urlRappel: f.urlRappel.trim() || null })
      setRemis(data)
      setF({ nom: '', systeme: 'WORKDAY', urlRappel: '' })
      recharger()
    } catch (x) { setErreur(versErreur(x)) }
  }
  async function revoquer(c) {
    if (!window.confirm(t('clm.ci.confirmerRevocation', { nom: c.nom }))) return
    setErreur(null)
    try { await api.post(`/clm/admin/clients-integration/${c.id}/revoquer`); setInfo(t('clm.ci.revoque', { nom: c.nom })); recharger() } catch (x) { setErreur(versErreur(x)) }
  }
  async function relancer(r) {
    setErreur(null)
    try { await api.post(`/clm/admin/rappels/${r.id}/relancer`); setInfo(t('clm.ad.relance')); rechargerRappels() } catch (x) { setErreur(versErreur(x)) }
  }

  return (
    <>
      <Bloc zone="06" titre={t('clm.ci.titre')} large>
        <Message erreur={erreur} info={info} />
        <p className="muted">{t('clm.ci.aide')}</p>
        {remis && (
          <div className="clm-info-statique" role="status">
            <p><strong>{t('clm.ci.remis', { nom: remis.client.nom })}</strong></p>
            <dl className="clm-dl">
              <div><dt>{t('clm.ci.clientId')}</dt><dd><code className="clm-casse">{remis.client.clientId}</code></dd></div>
              <div><dt>{t('clm.ci.secret')}</dt><dd><code className="clm-casse">{remis.secret}</code></dd></div>
              <div><dt>{t('clm.ci.cleRappel')}</dt><dd><code className="clm-casse">{remis.cleRappel}</code></dd></div>
            </dl>
            <button type="button" className="secondary" onClick={() => setRemis(null)}>{t('clm.ci.copies')}</button>
          </div>
        )}
        {clients && clients.length > 0 && (
          <div className="table-scroll">
            <table className="table clm-table">
              <caption className="sr-only">{t('clm.ci.titre')}</caption>
              <thead><tr><th scope="col">{t('clm.col.nom')}</th><th scope="col">{t('clm.ci.systeme')}</th><th scope="col">{t('clm.ci.clientId')}</th>
                <th scope="col">{t('clm.ci.urlRappel')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
              <tbody>
                {clients.map((c) => (
                  <tr key={c.id}>
                    <td data-label={t('clm.col.nom')}>{c.nom}</td>
                    <td data-label={t('clm.ci.systeme')}>{t(`clm.ci.systeme.${c.systeme}`)}</td>
                    <td data-label={t('clm.ci.clientId')}><code className="clm-casse">{c.clientId}</code></td>
                    <td data-label={t('clm.ci.urlRappel')} className="clm-casse">{c.urlRappel || '—'}</td>
                    <td data-label={t('clm.col.statut')}>{c.actif ? t('clm.ci.actif') : t('clm.ci.revoqueLe', { d: dateHeure(c.revoqueLe) })}</td>
                    <td data-label={t('clm.col.actions')}>
                      {c.actif && <button type="button" className="lien danger" aria-label={`${t('clm.ci.revoquer')} — ${c.nom}`} onClick={() => revoquer(c)}>{t('clm.ci.revoquer')}</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <form className="form clm-sousform" onSubmit={creer} aria-label={t('clm.ci.nouveau')}>
          <div className="form-grid">
            <label>{t('clm.col.nom')}<input value={f.nom} onChange={maj('nom')} maxLength={120} required /></label>
            <label>{t('clm.ci.systeme')}
              <select value={f.systeme} onChange={maj('systeme')}>{SYSTEMES.map((x) => <option key={x} value={x}>{t(`clm.ci.systeme.${x}`)}</option>)}</select>
            </label>
            <label className="col-2">{t('clm.ci.urlRappel')} <span className="muted">{t('clm.ci.urlRappelAide')}</span>
              <input type="url" value={f.urlRappel} onChange={maj('urlRappel')} /></label>
          </div>
          <div className="cmp-row"><button type="submit">{t('clm.ci.creer')}</button></div>
        </form>
      </Bloc>
      <Bloc zone="07" titre={t('clm.ci.rappels')} large>
        {!rappels || rappels.length === 0 ? <p className="muted">{t('clm.ci.aucunRappel')}</p> : (
          <div className="table-scroll">
            <table className="table clm-table">
              <caption className="sr-only">{t('clm.ci.rappels')}</caption>
              <thead><tr><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.ci.client')}</th><th scope="col">{t('clm.col.evenement')}</th>
                <th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.tentatives')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
              <tbody>
                {rappels.map((r) => (
                  <tr key={r.id}>
                    <td data-label={t('clm.col.date')}>{dateHeure(r.creeLe)}</td>
                    <td data-label={t('clm.ci.client')}>{r.client}</td>
                    <td data-label={t('clm.col.evenement')}>{r.evenement} · <Link to={`/clm/trousses/${r.trousseId}`}>#{r.trousseId}</Link>{r.derniereErreur && <><br /><span className="muted">{r.derniereErreur}</span></>}</td>
                    <td data-label={t('clm.col.statut')}><Statut valeur={r.statut} /></td>
                    <td data-label={t('clm.col.tentatives')}>{r.tentatives}</td>
                    <td data-label={t('clm.col.actions')}>
                      {r.statut !== 'ENVOYE' && <button type="button" className="lien" aria-label={`${t('clm.ad.relancer')} — ${t('clm.ci.rappel')} #${r.id}`} onClick={() => relancer(r)}>{t('clm.ad.relancer')}</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Bloc>
    </>
  )
}

function Integrations() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: e, recharger } = useRessource('/clm/admin/integrations/salesforce')
  const { donnees: p } = useRessource('/clm/admin/parametres')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [detail, setDetail] = useState(null)
  const [procedure, setProcedure] = useState(false)
  const [edition, setEdition] = useState(false)
  const secretRef = useRef(null)
  useEffect(() => { if (edition && procedure) secretRef.current?.focus() }, [edition, procedure])
  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(typeof ok === 'function' ? ok(r.data) : ok); recharger(); return true } catch (x) { setErreur(versErreur(x)); return false }
  }
  if (!e) return <p role="status">{t('clm.chargement')}</p>
  const echecs = e.journal.filter((m) => m.statut === 'ECHEC' || (m.statut === 'EN_ATTENTE' && m.tentatives > 0))
  const derniereSync = e.journal.map((m) => m.envoyeLe).filter(Boolean).sort().pop()
  const relancer = (m) => faire(() => api.post(`/clm/admin/integrations/salesforce/evenements/${m.id}/relancer`), t('clm.ad.relance'))
  const enregistrer = async (f) => {
    if (await faire(() => api.put('/clm/admin/integrations/salesforce/configuration', f), t('clm.ad.connexionEnregistree'))) setEdition(false)
  }
  const retablir = async () => {
    if (!window.confirm(t('clm.ad.confirmerRetablirConnexion'))) return
    if (await faire(() => api.delete('/clm/admin/integrations/salesforce/configuration'), t('clm.ad.connexionRetablie'))) setEdition(false)
  }

  return (
    <EcranClm maquette="UI-21" titre={t('clm.ad.integrations')} objectif={t('clm.ad.integrationsObjectif')}
      actions={(
        <>
          <button type="button" className="secondary" onClick={() => faire(() => api.post('/clm/admin/integrations/salesforce/tester'), t('clm.ad.testFait'))}>{t('clm.ad.tester')}</button>
          <button type="button" className="secondary" onClick={() => faire(() => api.post('/clm/admin/integrations/salesforce/relayer'), (d) => t('clm.ad.relaye', { n: d.envoyes }))}>{t('clm.ad.relayer')}</button>
          <button type="button" className="secondary" aria-expanded={edition} aria-controls="clm-connexion-sf" onClick={() => { setProcedure(false); setEdition(!edition) }}>{t('clm.ad.configurerConnexion')}</button>
          <button type="button" className="secondary" aria-expanded={procedure} aria-controls="clm-procedure-secret" onClick={() => { setProcedure(!procedure); setEdition(true) }}>{t('clm.ad.rotation')}</button>
        </>
      )}>
      <Message erreur={erreur} info={info} />
      {procedure && <div id="clm-procedure-secret" className="clm-info-statique" role="note">{t('clm.ad.rotationProcedure')}</div>}
      <Blocs>
        <Bloc zone="01" titre={t('clm.ad.blocConnexion')}>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.connexion')}</dt><dd>{e.connexion ? <>{e.connexion.disponible ? '✔ ' : '⚠ '}{e.connexion.detail} · {dateHeure(e.connexion.testeLe)}</> : t('clm.ad.nonTeste')}</dd></div>
            <div><dt>{t('clm.ad.derniereSync')}</dt><dd>{dateHeure(derniereSync)}</dd></div>
            <div><dt>{t('clm.ad.enAttente')}</dt><dd>{e.enAttente}</dd></div>
            <div><dt>{t('clm.ad.enEchec')}</dt><dd>{e.enEchec}</dd></div>
            <div><dt>{t('clm.ad.envoyes')}</dt><dd>{e.envoyes}</dd></div>
          </dl>
        </Bloc>
        <Bloc zone="02" titre={t('clm.ad.blocIdentifiants')}>
          <p>{e.config.personnalisee ? t('clm.ad.connexionPropre', { par: e.config.modifiePar, le: dateHeure(e.config.modifieLe) }) : t('clm.ad.connexionPlateforme')}</p>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.mode')}</dt><dd>{t(`clm.ad.modeSf.${e.config.mode}`)}</dd></div>
            <div><dt>{t('clm.ad.instance')}</dt><dd className="clm-casse">{e.config.instanceUrl || '—'}</dd></div>
            <div><dt>{t('clm.ad.serveurJetons')}</dt><dd className="clm-casse">{e.config.tokenUrl || '—'}</dd></div>
            <div><dt>{t('clm.ad.clientId')}</dt><dd><code>{e.config.clientIdMasque || '—'}</code></dd></div>
            <div><dt>{t('clm.ad.authentification')}</dt><dd>{t(`clm.ad.auth.${e.config.authentification || 'CLIENT_CREDENTIALS'}`)}{e.config.utilisateur ? ` · ${e.config.utilisateur}` : ''}</dd></div>
            <div><dt>{t('clm.ad.secret')}</dt><dd>{e.config.secretConfigure ? t('clm.ad.secretConfigure') : t('clm.ad.secretAbsent')}</dd></div>
            <div><dt>{t('clm.ad.champsRetour')}</dt><dd><code>{e.config.champStatut}</code>, <code>{e.config.champReference}</code>, <code>{e.config.champCorrelation}</code></dd></div>
            <div><dt>{t('clm.ad.reprisesMax')}</dt><dd>{e.config.reprisesMax}</dd></div>
          </dl>
          {edition && <FormulaireConnexion config={e.config} secretRef={secretRef} onEnregistrer={enregistrer} onAnnuler={() => setEdition(false)} onRetablir={retablir} />}
        </Bloc>
        <Bloc zone="03" titre={t('clm.ad.journal')} large>
          <div className="table-scroll">
            <table className="table clm-table">
              <caption className="sr-only">{t('clm.ad.journal')}</caption>
              <thead><tr><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.col.evenement')}</th><th scope="col">{t('clm.ad.correlation')}</th>
                <th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.tentatives')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
              <tbody>
                {e.journal.map((m) => (
                  <tr key={m.id}>
                    <td data-label={t('clm.col.date')}>{dateHeure(m.creeLe)}</td>
                    <td data-label={t('clm.col.evenement')}>{m.evenement} · <Link to={`/clm/trousses/${m.trousseId}`}>#{m.trousseId}</Link> · {m.sfDossierId}</td>
                    <td data-label={t('clm.ad.correlation')}><code>{m.correlationId.slice(0, 8)}</code></td>
                    <td data-label={t('clm.col.statut')}><Statut valeur={m.statut} /></td>
                    <td data-label={t('clm.col.tentatives')}>{m.tentatives}</td>
                    <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                      <button type="button" className="lien" aria-expanded={detail === m.id} aria-label={`${t('clm.ad.details')} — ${m.correlationId.slice(0, 8)}`} onClick={() => setDetail(detail === m.id ? null : m.id)}>{t('clm.ad.details')}</button>
                      {m.statut !== 'ENVOYE' && <button type="button" className="lien" aria-label={t('clm.ad.relancerEvenement', { c: m.correlationId.slice(0, 8) })} onClick={() => relancer(m)}>{t('clm.ad.relancer')}</button>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {detail && (() => {
            const m = e.journal.find((x) => x.id === detail)
            return m && (
              <dl className="clm-dl clm-apercu" aria-label={t('clm.ad.details')}>
                <div><dt>{t('clm.ad.correlation')}</dt><dd><code className="clm-casse">{m.correlationId}</code></dd></div>
                <div><dt>{t('clm.ad.envoyeLe')}</dt><dd>{dateHeure(m.envoyeLe)}</dd></div>
                <div><dt>{t('clm.ad.prochainEssai')}</dt><dd>{dateHeure(m.prochainEssai)}</dd></div>
                <div><dt>{t('clm.col.erreur')}</dt><dd>{m.derniereErreur || '—'}</dd></div>
              </dl>
            )
          })()}
        </Bloc>
        <Bloc zone="04" titre={t('clm.ad.blocErreurs')}>
          {echecs.length === 0 ? <p className="muted">{t('clm.ad.aucunEchec')}</p> : (
            <ul className="clm-liste">
              {echecs.map((m) => (
                <li key={m.id}><Statut valeur={m.statut} /> {m.evenement} · {m.sfDossierId} · {t('clm.col.tentatives')} {m.tentatives}
                  {m.statut === 'EN_ATTENTE' && <><br /><span className="muted">{t('clm.ad.reprise', { d: dateHeure(m.prochainEssai) })}</span></>}
                  <br /><span className="muted">{m.derniereErreur}</span>{' '}
                  <button type="button" className="lien" aria-label={t('clm.ad.relancerEvenement', { c: m.correlationId.slice(0, 8) })} onClick={() => relancer(m)}>{t('clm.ad.relancer')}</button></li>
              ))}
            </ul>
          )}
        </Bloc>
        <Bloc zone="05" titre={t('clm.ad.blocVersion')}>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.versionApi')}</dt><dd>{e.config.versionApi}</dd></div>
            <div><dt>{t('clm.ad.objet')}</dt><dd>{e.config.objet}</dd></div>
            <div><dt>{t('clm.ad.environnement')}</dt><dd>{p?.environnement || '—'}</dd></div>
          </dl>
        </Bloc>
        <ClientsIntegration />
      </Blocs>
    </EcranClm>
  )
}

const duree = (depuis) => {
  const minutes = Math.max(0, Math.round((Date.now() - new Date(depuis).getTime()) / 60000))
  return { h: Math.floor(minutes / 60), m: minutes % 60 }
}

const MO = 1_048_576
const NIVEAUX_IDENTITE = ['COURRIEL', 'CODE_SMS', 'QUESTION', 'FORTE']
const RISQUES_IDENTITE = ['faible', 'moyen', 'eleve']
const enMo = (octets) => Math.round((octets / MO) * 10) / 10

/** Paramètres du module propres à l'organisation (UI-22) ; chaque borne est vérifiée par le serveur. */
function FormulaireParametres({ module, onEnregistrer, onAnnuler, onRetablir }) {
  const { t } = useI18n()
  const premier = useRef(null)
  const initial = enMo(module.tailleMaxOctets)
  const [f, setF] = useState(() => ({ tailleMo: initial, invitationValiditeJours: module.invitationValiditeJours, portailUrl: module.portailUrl,
    prefixeReference: module.prefixeReference, rappelFrequenceJours: module.rappelFrequenceJours, rappelsMax: module.rappelsMax,
    preavisEcheanceJours: module.preavisEcheanceJours,
    identiteExterne: { ...module.identiteExterne }, identiteInterne: { ...module.identiteInterne }, tentativesIdentite: module.tentativesIdentite,
    codeValiditeMinutes: module.codeValiditeMinutes, champMontant: module.champMontant }))
  useEffect(() => { premier.current?.focus() }, [])
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.type === 'number' ? Number(e.target.value) : e.target.value })
  const niveau = (cote, r) => (e) => setF({ ...f, [cote]: { ...f[cote], [r]: e.target.value } })
  const envoyer = (e) => {
    e.preventDefault()
    const { tailleMo, ...reste } = f
    // Une taille inchangée n'est pas renvoyée : l'arrondi en Mo ne modifie pas la valeur en place.
    onEnregistrer({ ...reste, tailleMaxOctets: Number(tailleMo) === initial ? null : Math.round(Number(tailleMo) * MO) })
  }
  return (
    <form id="clm-parametres-module" className="form clm-sousform" onSubmit={envoyer} aria-label={t('clm.ad.pm.titre')}>
      <p className="muted">{t('clm.ad.pm.aide')}</p>
      <fieldset className="form-fieldset">
        <legend>{t('clm.ad.pm.documents')}</legend>
        <div className="form-grid">
          <label>{t('clm.ad.pm.tailleMo')} <span className="muted">{t('clm.ad.pm.plafond', { n: enMo(module.plafondOctets) })}</span>
            <input ref={premier} type="number" min={0.1} max={enMo(module.plafondOctets)} step={0.1} value={f.tailleMo} onChange={maj('tailleMo')} required /></label>
          <label>{t('clm.ad.pm.validiteJours')}<input type="number" min={1} max={90} value={f.invitationValiditeJours} onChange={maj('invitationValiditeJours')} required /></label>
          <label className="col-2">{t('clm.ad.pm.portail')} <span className="muted">{t('clm.ad.pm.portailAide')}</span>
            <input type="url" value={f.portailUrl} onChange={maj('portailUrl')} required /></label>
        </div>
      </fieldset>
      <fieldset className="form-fieldset">
        <legend>{t('clm.ad.pm.trousses')}</legend>
        <div className="form-grid">
          <label>{t('clm.ad.pm.prefixe')} <span className="muted">{t('clm.ad.pm.prefixeAide')}</span>
            <input value={f.prefixeReference} onChange={(e) => setF({ ...f, prefixeReference: e.target.value.toUpperCase() })} maxLength={10} required /></label>
          <label>{t('clm.champ.frequence')}<input type="number" min={1} max={30} value={f.rappelFrequenceJours} onChange={maj('rappelFrequenceJours')} required /></label>
          <label>{t('clm.champ.rappelsMax')}<input type="number" min={0} max={10} value={f.rappelsMax} onChange={maj('rappelsMax')} required /></label>
          <label>{t('clm.ad.pm.preavis')} <span className="muted">{t('clm.ad.pm.preavisAide')}</span>
            <input type="number" min={0} max={30} value={f.preavisEcheanceJours} onChange={maj('preavisEcheanceJours')} required /></label>
        </div>
      </fieldset>
      <fieldset className="form-fieldset">
        <legend>{t('clm.ad.pm.identite')}</legend>
        <p className="muted">{t('clm.ad.pm.identiteAide')}</p>
        <div className="table-scroll">
          <table className="table clm-table">
            <caption className="sr-only">{t('clm.ad.pm.identite')}</caption>
            <thead><tr><th scope="col">{t('clm.champ.risque')}</th><th scope="col">{t('clm.ad.pm.externes')}</th><th scope="col">{t('clm.ad.pm.internes')}</th></tr></thead>
            <tbody>
              {RISQUES_IDENTITE.map((r) => (
                <tr key={r}>
                  <th scope="row">{t(`clm.risque.${r.toUpperCase()}`)}</th>
                  {['identiteExterne', 'identiteInterne'].map((cote) => (
                    <td key={cote} data-label={t(cote === 'identiteExterne' ? 'clm.ad.pm.externes' : 'clm.ad.pm.internes')}>
                      <select aria-label={`${t(cote === 'identiteExterne' ? 'clm.ad.pm.externes' : 'clm.ad.pm.internes')} — ${t(`clm.risque.${r.toUpperCase()}`)}`}
                        value={f[cote][r]} onChange={niveau(cote, r)} style={{ width: '100%' }}>
                        {NIVEAUX_IDENTITE.map((n) => <option key={n} value={n}>{t(`clm.niveau.${n}`)}</option>)}
                      </select>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="form-grid">
          <label>{t('clm.ad.pm.tentatives')}<input type="number" min={3} max={10} value={f.tentativesIdentite} onChange={maj('tentativesIdentite')} required /></label>
          <label>{t('clm.ad.pm.codeMinutes')}<input type="number" min={5} max={60} value={f.codeValiditeMinutes} onChange={maj('codeValiditeMinutes')} required /></label>
        </div>
      </fieldset>
      <fieldset className="form-fieldset">
        <legend>{t('clm.ad.pm.ia')}</legend>
        <label>{t('clm.ad.pm.champMontant')} <span className="muted">{t('clm.ad.pm.champMontantAide')}</span>
          <input value={f.champMontant} onChange={maj('champMontant')} required /></label>
      </fieldset>
      <div className="cmp-row">
        <button type="button" className="secondary" onClick={onAnnuler}>{t('clm.annuler')}</button>
        {module.personnalise && <button type="button" className="secondary" onClick={onRetablir}>{t('clm.ad.pm.retablir')}</button>}
        <button type="submit">{t('clm.enregistrer')}</button>
      </div>
    </form>
  )
}

/** UI-22 — Paramètres et environnements : réglages de la plateforme en lecture seule, paramètres du module modifiables par l'organisation. */
function Parametres() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: p, erreur, recharger } = useRessource('/clm/admin/parametres')
  const [info, setInfo] = useState('')
  const [erreurEdition, setErreurEdition] = useState(null)
  const [edition, setEdition] = useState(false)
  if (!p) return <Message erreur={erreur} info={t('clm.chargement')} onReessayer={recharger} />
  const m = p.module
  const enLigne = duree(p.demarreLe)
  const niveaux = (cote) => RISQUES_IDENTITE.map((r) => `${t(`clm.risque.${r.toUpperCase()}`)} : ${t(`clm.niveau.${m[cote][r]}`)}`).join(' · ')
  function consulter() {
    const lien = document.createElement('a')
    lien.href = URL.createObjectURL(new Blob([JSON.stringify(p, null, 2)], { type: 'application/json' }))
    lien.download = 'parametres-clm.json'
    document.body.appendChild(lien)
    lien.click()
    lien.remove()
    setTimeout(() => URL.revokeObjectURL(lien.href), 1000)
  }
  async function faire(appel, message) {
    setErreurEdition(null)
    try { await appel(); setInfo(message); setEdition(false); recharger() } catch (x) { setErreurEdition(versErreur(x)) }
  }
  const enregistrer = (corps) => faire(() => api.put('/clm/admin/parametres', corps), t('clm.ad.pm.enregistres'))
  const retablir = () => { if (window.confirm(t('clm.ad.pm.confirmerRetablir'))) faire(() => api.delete('/clm/admin/parametres'), t('clm.ad.pm.retablis')) }

  return (
    <EcranClm maquette="UI-22" titre={t('clm.ad.parametres')} objectif={t('clm.ad.parametresObjectif')}
      actions={(
        <>
          <button type="button" className="secondary" onClick={consulter}>{t('clm.ad.consulterConfiguration')}</button>
          <button type="button" className="secondary" aria-expanded={edition} aria-controls="clm-parametres-module" onClick={() => setEdition(!edition)}>{t('clm.ad.modifierParametre')}</button>
          <button type="button" className="secondary" onClick={() => { recharger(); setInfo(t('clm.ad.statutActualise')) }}>{t('clm.ad.consulterStatut')}</button>
        </>
      )}>
      <Message erreur={erreurEdition} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.ad.blocEnvironnement')} large>
          <p className={`clm-environnement clm-environnement--${p.environnement.toLowerCase()}`}><strong>{t('clm.ad.environnement')} : {p.environnement}</strong></p>
          <p>{t('clm.ad.environnementAide')}</p>
        </Bloc>
        <Bloc titre={t('clm.ad.pm.titre')} large>
          <p>{m.personnalise ? t('clm.ad.pm.propres', { par: m.modifiePar, le: dateHeure(m.modifieLe) }) : t('clm.ad.pm.plateforme')}</p>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.pm.portail')}</dt><dd className="clm-casse">{m.portailUrl}</dd></div>
            <div><dt>{t('clm.ad.pm.prefixe')}</dt><dd><code>{m.prefixeReference}</code></dd></div>
            <div><dt>{t('clm.ad.pm.rappels')}</dt><dd>{t('clm.ad.pm.rappelsValeur', { j: m.rappelFrequenceJours, n: m.rappelsMax })}</dd></div>
            <div><dt>{t('clm.ad.pm.preavis')}</dt><dd>{m.preavisEcheanceJours > 0 ? t('clm.ad.pm.preavisValeur', { n: m.preavisEcheanceJours }) : t('clm.ad.pm.preavisAucun')}</dd></div>
            <div><dt>{t('clm.ad.pm.externes')}</dt><dd>{niveaux('identiteExterne')}</dd></div>
            <div><dt>{t('clm.ad.pm.internes')}</dt><dd>{niveaux('identiteInterne')}</dd></div>
            <div><dt>{t('clm.ad.pm.tentatives')}</dt><dd>{m.tentativesIdentite}</dd></div>
            <div><dt>{t('clm.ad.pm.codeMinutes')}</dt><dd>{t('clm.ad.minutes', { n: m.codeValiditeMinutes })}</dd></div>
            <div><dt>{t('clm.ad.pm.champMontant')}</dt><dd><code>{m.champMontant}</code></dd></div>
          </dl>
          {edition && <FormulaireParametres module={m} onEnregistrer={enregistrer} onAnnuler={() => setEdition(false)} onRetablir={retablir} />}
        </Bloc>
        <Bloc zone="02" titre={t('clm.ad.blocSession')}>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.dureeSession')}</dt><dd>{t('clm.ad.minutes', { n: p.sessionMinutes })}</dd></div>
            <div><dt>{t('clm.ad.cookieSecurise')}</dt><dd>{p.cookieSecurise ? t('clm.oui') : t('clm.non')}</dd></div>
          </dl>
          <p className="muted">{t('clm.ad.sessionAide')} <Link to="/clm/admin/sso">{t('clm.ut.titreSso')}</Link></p>
        </Bloc>
        <Bloc zone="03" titre={t('clm.ad.blocConservation')}>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.tailleMax')}</dt><dd>{taille(m.tailleMaxOctets)}</dd></div>
            <div><dt>{t('clm.ad.formats')}</dt><dd>{p.formats.map((f) => (f.includes('pdf') ? t('clm.doc.formatPdf') : f.includes('wordprocessingml') ? t('clm.doc.formatWord') : f)).join(', ')}</dd></div>
            <div><dt>{t('clm.ad.validiteInvitation')}</dt><dd>{t('clm.ad.jours', { n: m.invitationValiditeJours })}</dd></div>
          </dl>
          <p>{t('clm.ad.exportAutorise')}</p>
          <p className="muted">{t('clm.ad.conservationAide')}</p>
        </Bloc>
        <Bloc zone="04" titre={t('clm.ad.blocSauvegardes')}>
          <p>{t('clm.ad.sauvegardesFournisseur')}</p>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.planificateur')}</dt><dd>{p.planificateurActif ? t('clm.ad.actifToutes', { s: Math.round(p.planificateurDelaiMs / 1000) }) : t('clm.ad.inactif')}</dd></div>
            <div><dt>{t('clm.ad.salesforceMode')}</dt><dd>{t(`clm.ad.modeSf.${p.salesforceMode}`)}</dd></div>
          </dl>
        </Bloc>
        <Bloc zone="05" titre={t('clm.ad.blocDisponibilite')}>
          <p><span aria-hidden="true">✔</span> <strong>{t('clm.ad.operationnel')}</strong></p>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.demarreLe')}</dt><dd>{dateHeure(p.demarreLe)}</dd></div>
            <div><dt>{t('clm.ad.enLigneDepuis')}</dt><dd>{t('clm.ad.dureeHm', enLigne)}</dd></div>
          </dl>
          <p className="muted">{t('clm.ad.incidentsAide')} <Link to="/clm/admin/journal">{t('clm.nav.journal')}</Link></p>
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

/** Listes de référence du module : l'organisation adapte celles de la plateforme ; le superadmin en fixe les valeurs par défaut. */
function Listes() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const plateforme = hasRole('SUPERADMIN') && !hasRole('ADMIN')
  return (
    <EcranClm titre={t('clm.ad.listes')} objectif={t('clm.ad.listesObjectif')}>
      <Blocs>
        <Bloc titre={t('clm.ad.blocListes')} large>
          <ReferenceManager portee={plateforme ? 'superadmin' : 'admin'} modules={['clm', 'clm_ia']} />
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export function NotificationsClm() {
  return <GardeModule><Notifications /></GardeModule>
}

export function IntegrationsClm() {
  return <GardeModule><Integrations /></GardeModule>
}

export function ParametresClm() {
  return <GardeModule><Parametres /></GardeModule>
}

export function ListesClm() {
  return <GardeModule><Listes /></GardeModule>
}
