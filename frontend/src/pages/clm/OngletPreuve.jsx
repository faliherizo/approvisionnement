import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { Message, Statut, dateHeure, telecharger, useErreur, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'

const ACCES = ['DOCUMENT_TELECHARGE', 'DOCUMENT_CONSULTE', 'PREUVE_EXPORTEE', 'CERTIFICAT_TELECHARGE', 'INTEGRITE_VERIFIEE']

/** UI-19 — Dossier de preuve : document final et empreinte, certificat, identités, horodatages, accès, corrélation. */
export default function OngletPreuve({ trousseId, resume, admin, onHistorique }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: p, erreur: errChargement, recharger } = useRessource(`/clm/trousses/${trousseId}/preuve`)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function verifier() {
    setErreur(null)
    try {
      const { data } = await api.post(`/clm/trousses/${trousseId}/preuve/integrite`)
      setInfo(data.integre ? t('clm.pr.integre') : t('clm.pr.altere'))
      recharger()
    } catch (e) { setErreur(versErreur(e)) }
  }
  async function charger(chemin, nom, ok) {
    setErreur(null)
    try { await telecharger(`/clm/trousses/${trousseId}/preuve/${chemin}`, nom); setInfo(ok); recharger() } catch (e) {
      setErreur(e.response?.status === 403 ? t('clm.pr.exportRefuse') : versErreur(e))
    }
  }

  if (!p) return <Message erreur={errChargement} info={t('clm.pr.generation')} onReessayer={recharger} />
  const i = p.integrite
  const acces = p.evenements.filter((e) => ACCES.includes(e.type))
  const dernier = p.evenements[p.evenements.length - 1]
  return (
    <div data-maquette="UI-19">
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.pr.blocFinal')}>
          <p className={i.integre ? 'clm-ok' : 'erreur'}>
            <span aria-hidden="true">{i.integre ? '🔒' : '⚠'}</span> {i.integre ? t('clm.pr.integre') : t('clm.pr.altere')}
            {i.rupture ? ` ${t('clm.pr.rupture', { n: i.rupture })}` : ''} · {t('clm.pr.verifieeLe', { d: dateHeure(i.verifieeLe) })}
          </p>
          {i.documentsAlteres.length > 0 && <p className="erreur">{t('clm.pr.docsAlteres', { liste: i.documentsAlteres.join(', ') })}</p>}
          {i.attestationsInvalides.length > 0 && <p className="erreur">{t('clm.pr.attestationsInvalides', { liste: i.attestationsInvalides.join(', ') })}</p>}
          {p.documentsFinaux.length === 0 ? <p className="muted">{t('clm.pr.aucunFinal')}</p> : (
            <ul className="clm-liste">
              {p.documentsFinaux.map((d) => <li key={d.id}><Statut valeur={d.statut} /> {d.nom} v{d.version} · <code title={d.empreinte}>{d.empreinte.slice(0, 24)}…</code></li>)}
            </ul>
          )}
        </Bloc>
        <Bloc zone="02" titre={t('clm.pr.blocCertificat')}>
          <p>{t('clm.pr.certificatAide')}</p>
          <button type="button" className="secondary" onClick={() => charger('certificat', `certificat-${resume.reference}.pdf`, t('clm.pr.certificatTelecharge'))}>{t('clm.pr.telechargerCertificat')}</button>
        </Bloc>
        <Bloc zone="03" titre={t('clm.pr.signatures')} large>
          {p.signatures.length === 0 ? <p className="muted">{t('clm.pr.aucuneSignature')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.pr.signatures')}</caption>
                <thead><tr><th scope="col">{t('clm.pr.signataire')}</th><th scope="col">{t('clm.part.niveau')}</th><th scope="col">{t('clm.pr.verifiee')}</th>
                  <th scope="col">{t('clm.pr.consentement')}</th><th scope="col">{t('clm.pr.signee')}</th><th scope="col">{t('clm.pr.attestation')}</th></tr></thead>
                <tbody>
                  {p.signatures.map((s, k) => (
                    <tr key={k}>
                      <td data-label={t('clm.pr.signataire')}>{s.participant} <span className="muted">{s.courriel} · {s.roleContrat}</span><br /><Statut valeur={s.statut} /></td>
                      <td data-label={t('clm.part.niveau')}>{t(`clm.niveau.${s.niveau}`)}</td>
                      <td data-label={t('clm.pr.verifiee')}>{dateHeure(s.verifieeLe)}</td>
                      <td data-label={t('clm.pr.consentement')}>{dateHeure(s.consentementLe)}</td>
                      <td data-label={t('clm.pr.signee')}>{dateHeure(s.signeeLe)}{s.ip ? <><br /><span className="muted">IP {s.ip}</span></> : null}</td>
                      <td data-label={t('clm.pr.attestation')}>{s.attestation?.identifiant ? <><code>{s.attestation.identifiant}</code><br /><span className="muted">{s.attestation.fournisseur} · {s.attestation.algorithme}</span></> : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        <Bloc zone="04" titre={t('clm.pr.journal')}>
          <ol className="clm-chrono clm-chrono--compact">
            {p.evenements.map((e) => (
              <li key={e.sequence}><strong>{e.sequence}. {t(`clm.evt.${e.type}`)}</strong> · {e.acteur} · {dateHeure(e.horodatage)}
                <br /><code className="muted" title={e.empreinte}>{e.empreinte.slice(0, 20)}…</code>{e.detail ? ` · ${e.detail}` : ''}</li>
            ))}
          </ol>
        </Bloc>
        <Bloc zone="05" titre={t('clm.pr.blocAcces')}>
          {acces.length === 0 ? <p className="muted">{t('clm.pr.aucunAcces')}</p> : (
            <ul className="clm-liste">
              {acces.map((e) => <li key={e.sequence}>{t(`clm.evt.${e.type}`)} · {e.acteur} · {dateHeure(e.horodatage)}</li>)}
            </ul>
          )}
        </Bloc>
        <Bloc zone="06" titre={t('clm.pr.blocCorrelation')} large>
          <dl className="clm-dl">
            <div><dt>{t('clm.col.reference')}</dt><dd>{resume.reference}</dd></div>
            <div><dt>{t('clm.pr.dossierSf')}</dt><dd>{resume.sfDossierId || '—'}</dd></div>
            <div><dt>{t('clm.col.statut')}</dt><dd><Statut valeur={resume.statut} /></dd></div>
            <div><dt>{t('clm.pr.evenements')}</dt><dd>{p.evenements.length}</dd></div>
            <div><dt>{t('clm.pr.teteChaine')}</dt><dd>{dernier ? <code title={dernier.empreinte}>{dernier.empreinte.slice(0, 24)}…</code> : '—'}</dd></div>
          </dl>
          {admin && resume.sfDossierId && <p><Link to="/clm/admin/integrations">{t('clm.pr.voirCorrelation')}</Link></p>}
        </Bloc>
      </Blocs>
      <BarreActions>
        <button type="button" className="secondary" onClick={() => charger('certificat', `certificat-${resume.reference}.pdf`, t('clm.pr.certificatTelecharge'))}>{t('clm.pr.telechargerPreuve')}</button>
        <button type="button" onClick={() => charger('export', `preuve-${resume.reference}.zip`, t('clm.pr.exporte'))}>{t('clm.pr.exporter')}</button>
        <button type="button" className="secondary" onClick={verifier}>{t('clm.pr.verifier')}</button>
        {admin
          ? <Link className="clm-bouton secondaire" to={`/clm/admin/journal?trousse=${encodeURIComponent(resume.reference)}`}>{t('clm.pr.consulterJournal')}</Link>
          : <button type="button" className="secondary" onClick={onHistorique}>{t('clm.pr.consulterJournal')}</button>}
      </BarreActions>
    </div>
  )
}
