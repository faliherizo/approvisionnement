import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useI18n } from '../../i18n/I18nContext'
import { useModules } from '../../hooks/useModules.js'
import { GardeModule, Message, Statut, dateHeure, useListe, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs, EcranClm } from './gabarit.jsx'

const CAPACITES = ['clauses', 'metadonnees', 'classification', 'anomalies']
const pourcent = (x) => Math.round((x || 0) * 100)

/** UI-24 — Options CLM et IA : capacités activées, extraction, classification avec confiance, validation humaine, coûts. */
function Contenu() {
  const { t } = useI18n()
  const { actif } = useModules()
  const ia = actif('clm_ia')
  const { donnees, erreur, recharger } = useRessource(ia ? '/clm/analyses' : null, ia)
  const types = useListe('CLM_TYPE_CONTRAT', ia)
  const nomCategorie = (c) => (c === 'INDETERMINEE' ? t('clm.ia.indeterminee') : types.libelle(c))
  const [note, setNote] = useState(false)
  const analyses = donnees || []
  const aReviser = analyses.filter((a) => a.statut === 'A_REVISER')
  const revisees = analyses.length - aReviser.length
  const versDocuments = (a) => `/clm/trousses/${a.trousseId}?onglet=documents`

  return (
    <EcranClm maquette="UI-24" titre={t('clm.opt.titre')} objectif={t('clm.opt.objectif')}>
      <Message erreur={erreur} onReessayer={recharger} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.opt.blocCatalogue')}>
          <ul className="clm-liste">
            {CAPACITES.map((c) => <li key={c}>{t(`clm.opt.capacite.${c}`)} — <strong>{ia ? t('clm.opt.activee') : t('clm.ia.nonSouscrite')}</strong></li>)}
          </ul>
          <p className="muted">{t('clm.ia.avertissement')}</p>
        </Bloc>
        <Bloc zone="02" titre={t('clm.opt.blocExtraction')}>
          {!ia ? <p className="muted">{t('clm.ia.nonSouscrite')}</p> : analyses.length === 0 ? <p className="muted">{t('clm.opt.aucuneAnalyse')}</p> : (
            <ul className="clm-liste">
              {analyses.slice(0, 5).map((a) => (
                <li key={a.id}><Link to={versDocuments(a)}>#{a.trousseId}</Link> · {dateHeure(a.creeLe)}
                  <br /><span className="muted">{(a.resultat.metadonnees || []).map((m) => `${m.cle} : ${m.valeur} (${pourcent(m.confiance)} %)`).join(' · ') || '—'}</span></li>
              ))}
            </ul>
          )}
        </Bloc>
        <Bloc zone="03" titre={t('clm.opt.blocClassification')} large>
          {!ia ? <p className="muted">{t('clm.ia.nonSouscrite')}</p> : analyses.length === 0 ? <p className="muted">{t('clm.opt.aucuneAnalyse')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.opt.blocClassification')}</caption>
                <thead><tr><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.opt.trousse')}</th><th scope="col">{t('clm.col.categorie')}</th>
                  <th scope="col">{t('clm.opt.anomalies')}</th><th scope="col">{t('clm.col.statut')}</th></tr></thead>
                <tbody>
                  {analyses.map((a) => (
                    <tr key={a.id}>
                      <td data-label={t('clm.col.date')}>{dateHeure(a.creeLe)}</td>
                      <td data-label={t('clm.opt.trousse')}><Link to={versDocuments(a)}>#{a.trousseId}</Link> <span className="muted">· {t('clm.opt.document', { id: a.documentId })}</span></td>
                      <td data-label={t('clm.col.categorie')}>{t('clm.ia.categorie', { c: nomCategorie(a.resultat.categorie), p: pourcent(a.resultat.confianceCategorie) })}</td>
                      <td data-label={t('clm.opt.anomalies')}>{(a.resultat.anomalies || []).length === 0 ? '—' : (a.resultat.anomalies || []).map((m) => (
                        <span key={m.cle + m.valeur} className="clm-bloc-ligne"><span aria-hidden="true">⚠</span> {t(`clm.ia.anomalie.${m.cle}`)} ({pourcent(m.confiance)} %)</span>
                      ))}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={a.statut} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        <Bloc zone="04" titre={t('clm.opt.blocValidation')}>
          <p>{t('clm.opt.validationAide', { n: aReviser.length, r: revisees })}</p>
          {aReviser.length > 0 && (
            <ul className="clm-liste">
              {aReviser.map((a) => <li key={a.id}><Link to={versDocuments(a)}>{t('clm.opt.reviser', { id: a.trousseId })}</Link> · {dateHeure(a.creeLe)}</li>)}
            </ul>
          )}
        </Bloc>
        <Bloc zone="05" titre={t('clm.opt.blocCouts')}>
          <p>{t('clm.opt.coutsAide')}</p>
        </Bloc>
      </Blocs>
      {note && <p className="clm-info-statique" role="note">{t('clm.opt.desactivationAide')}</p>}
      <BarreActions>
        <Link className="clm-bouton secondaire" to="/clm/trousses">{t('clm.opt.lancer')}</Link>
        {aReviser.length > 0 && <Link className="clm-bouton" to={versDocuments(aReviser[0])}>{t('clm.opt.validerResultat')}</Link>}
        {aReviser.length > 0 && <Link className="clm-bouton secondaire" to={versDocuments(aReviser[0])}>{t('clm.ia.signalerErreur')}</Link>}
        <button type="button" className="secondary" aria-expanded={note} onClick={() => setNote(!note)}>{t('clm.opt.desactiver')}</button>
      </BarreActions>
    </EcranClm>
  )
}

export default function OptionsClm() {
  return <GardeModule><Contenu /></GardeModule>
}
