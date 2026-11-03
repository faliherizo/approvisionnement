import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
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
          <Link className="clm-bouton secondaire" to="/clm/trousses/nouvelle">{t('clm.ad.configurerFrequence')}</Link>
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

/** UI-21 — Intégrations API (Salesforce) : connexion, identifiants masqués, journal avec corrélation, erreurs et reprises, version. */
function Integrations() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: e, recharger } = useRessource('/clm/admin/integrations/salesforce')
  const { donnees: p } = useRessource('/clm/admin/parametres')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [detail, setDetail] = useState(null)
  const [procedure, setProcedure] = useState(false)
  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(typeof ok === 'function' ? ok(r.data) : ok); recharger() } catch (x) { setErreur(versErreur(x)) }
  }
  if (!e) return <p role="status">{t('clm.chargement')}</p>
  const echecs = e.journal.filter((m) => m.statut === 'ECHEC' || (m.statut === 'EN_ATTENTE' && m.tentatives > 0))
  const derniereSync = e.journal.map((m) => m.envoyeLe).filter(Boolean).sort().pop()
  const relancer = (m) => faire(() => api.post(`/clm/admin/integrations/salesforce/evenements/${m.id}/relancer`), t('clm.ad.relance'))

  return (
    <EcranClm maquette="UI-21" titre={t('clm.ad.integrations')} objectif={t('clm.ad.integrationsObjectif')}
      actions={(
        <>
          <button type="button" className="secondary" onClick={() => faire(() => api.post('/clm/admin/integrations/salesforce/tester'), t('clm.ad.testFait'))}>{t('clm.ad.tester')}</button>
          <button type="button" className="secondary" onClick={() => faire(() => api.post('/clm/admin/integrations/salesforce/relayer'), (d) => t('clm.ad.relaye', { n: d.envoyes }))}>{t('clm.ad.relayer')}</button>
          <button type="button" className="secondary" aria-expanded={procedure} aria-controls="clm-procedure-secret" onClick={() => setProcedure(!procedure)}>{t('clm.ad.rotation')}</button>
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
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.mode')}</dt><dd>{e.config.mode}</dd></div>
            <div><dt>{t('clm.ad.instance')}</dt><dd className="clm-casse">{e.config.instanceUrl || '—'}</dd></div>
            <div><dt>{t('clm.ad.clientId')}</dt><dd><code>{e.config.clientIdMasque || '—'}</code></dd></div>
            <div><dt>{t('clm.ad.secret')}</dt><dd>{e.config.secretConfigure ? t('clm.ad.secretConfigure') : t('clm.ad.secretAbsent')}</dd></div>
          </dl>
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
      </Blocs>
    </EcranClm>
  )
}

const duree = (depuis) => {
  const minutes = Math.max(0, Math.round((Date.now() - new Date(depuis).getTime()) / 60000))
  return { h: Math.floor(minutes / 60), m: minutes % 60 }
}

/** UI-22 — Paramètres et environnements : lecture seule, un paramètre autorisé se modifie par déploiement, selon la procédure. */
function Parametres() {
  const { t } = useI18n()
  const { donnees: p, erreur, recharger } = useRessource('/clm/admin/parametres')
  const [info, setInfo] = useState('')
  const [procedure, setProcedure] = useState(false)
  if (!p) return <Message erreur={erreur} info={t('clm.chargement')} onReessayer={recharger} />
  const enLigne = duree(p.demarreLe)
  function consulter() {
    const lien = document.createElement('a')
    lien.href = URL.createObjectURL(new Blob([JSON.stringify(p, null, 2)], { type: 'application/json' }))
    lien.download = 'parametres-clm.json'
    document.body.appendChild(lien)
    lien.click()
    lien.remove()
    setTimeout(() => URL.revokeObjectURL(lien.href), 1000)
  }

  return (
    <EcranClm maquette="UI-22" titre={t('clm.ad.parametres')} objectif={t('clm.ad.parametresObjectif')}
      actions={(
        <>
          <button type="button" className="secondary" onClick={consulter}>{t('clm.ad.consulterConfiguration')}</button>
          <button type="button" className="secondary" aria-expanded={procedure} aria-controls="clm-procedure-parametre" onClick={() => setProcedure(!procedure)}>{t('clm.ad.modifierParametre')}</button>
          <button type="button" className="secondary" onClick={() => { recharger(); setInfo(t('clm.ad.statutActualise')) }}>{t('clm.ad.consulterStatut')}</button>
        </>
      )}>
      <Message info={info} />
      {procedure && (
        <div id="clm-procedure-parametre" className="clm-info-statique" role="note">
          <p>{t('clm.ad.parametresModifiables')}</p>
          <p>{p.modificationPermise ? t('clm.ad.procedureParametre') : t('clm.ad.procedureParametreProd')}</p>
        </div>
      )}
      <Blocs>
        <Bloc zone="01" titre={t('clm.ad.blocEnvironnement')} large>
          <p className={`clm-environnement clm-environnement--${p.environnement.toLowerCase()}`}><strong>{t('clm.ad.environnement')} : {p.environnement}</strong></p>
          <p>{p.modificationPermise ? t('clm.ad.modificationParDeploiement') : t('clm.ad.prodInterdite')}</p>
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
            <div><dt>{t('clm.ad.tailleMax')}</dt><dd>{taille(p.tailleMaxOctets)}</dd></div>
            <div><dt>{t('clm.ad.formats')}</dt><dd>{p.formats.map((f) => (f.includes('pdf') ? t('clm.doc.formatPdf') : f.includes('wordprocessingml') ? t('clm.doc.formatWord') : f)).join(', ')}</dd></div>
            <div><dt>{t('clm.ad.validiteInvitation')}</dt><dd>{t('clm.ad.jours', { n: p.invitationValiditeJours })}</dd></div>
          </dl>
          <p>{t('clm.ad.exportAutorise')}</p>
          <p className="muted">{t('clm.ad.conservationAide')}</p>
        </Bloc>
        <Bloc zone="04" titre={t('clm.ad.blocSauvegardes')}>
          <p>{t('clm.ad.sauvegardesFournisseur')}</p>
          <dl className="clm-dl">
            <div><dt>{t('clm.ad.planificateur')}</dt><dd>{p.planificateurActif ? t('clm.ad.actifToutes', { s: Math.round(p.planificateurDelaiMs / 1000) }) : t('clm.ad.inactif')}</dd></div>
            <div><dt>{t('clm.ad.salesforceMode')}</dt><dd>{p.salesforceMode}</dd></div>
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

export function NotificationsClm() {
  return <GardeModule><Notifications /></GardeModule>
}

export function IntegrationsClm() {
  return <GardeModule><Integrations /></GardeModule>
}

export function ParametresClm() {
  return <GardeModule><Parametres /></GardeModule>
}
