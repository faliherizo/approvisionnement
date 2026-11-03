import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, Statut, dateHeure, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const INDICATEURS = ['brouillons', 'enValidation', 'enSignature', 'enRetard', 'terminees']
const STATUTS = ['BROUILLON', 'EN_TRAITEMENT', 'TERMINEE', 'REJETEE', 'ANNULEE']
const VIDE = { du: '', au: '', unite: '', statut: '' }

/** UI-01 — Tableau de bord : indicateurs, actions requises, activité, alertes Salesforce, filtres période/unité/statut. */
function Contenu() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(VIDE)
  const [filtre, setFiltre] = useState(VIDE)
  const qs = new URLSearchParams(Object.entries(filtre).filter(([, v]) => v)).toString()
  const { donnees, erreur, chargement, recharger } = useRessource(`/clm/tableau-de-bord${qs ? `?${qs}` : ''}`)
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  const premiere = donnees?.mesActions?.[0]
  const admin = hasRole('ADMIN') || hasRole('SUPERADMIN')

  return (
    <EcranClm maquette="UI-01" titre={t('clm.tb.titre')} objectif={t('clm.tb.intro')}
      actions={(
        <>
          <Link className="clm-bouton secondaire" to="/clm/trousses">{t('clm.tb.ouvrir')}</Link>
          <button type="button" className="secondary" disabled={!premiere}
            onClick={() => navigate(`/clm/taches?etape=${premiere.etapeId}`)}>{t('clm.tb.reprendre')}</button>
          <Link className="clm-bouton" to="/clm/trousses/nouvelle">{t('clm.tb.creer')}</Link>
          {admin && <Link className="clm-bouton secondaire" to="/clm/admin/integrations">{t('clm.tb.accederAlertes')}</Link>}
        </>
      )}>
      <Message erreur={erreur} info={chargement ? t('clm.chargement') : (qs ? t('clm.tb.filtresAppliques') : '')} onReessayer={recharger} />
      {donnees && (
        <Blocs>
          <Bloc zone="02" titre={t('clm.tb.indicateurs')}>
            <ul className="clm-kpis">
              {INDICATEURS.map((k) => (
                <li key={k} className={k === 'enRetard' && donnees.indicateurs[k] > 0 ? 'clm-kpi clm-kpi--alerte' : 'clm-kpi'}>
                  <span className="clm-kpi__val">{donnees.indicateurs[k] ?? 0}</span>
                  <span className="clm-kpi__lib">{t(`clm.tb.${k}`)}</span>
                </li>
              ))}
            </ul>
          </Bloc>
          <Bloc zone="03" titre={t('clm.tb.mesActions')}>
            {donnees.mesActions.length === 0 ? <p className="muted">{t('clm.tb.aucuneAction')}</p> : (
              <div className="table-scroll">
                <table className="table clm-table">
                  <caption className="sr-only">{t('clm.tb.mesActions')}</caption>
                  <thead><tr>
                    <th scope="col">{t('clm.col.reference')}</th><th scope="col">{t('clm.col.etape')}</th>
                    <th scope="col">{t('clm.col.responsable')}</th><th scope="col">{t('clm.col.echeance')}</th>
                  </tr></thead>
                  <tbody>
                    {donnees.mesActions.map((a) => (
                      <tr key={a.etapeId}>
                        <td data-label={t('clm.col.reference')}><Link to={`/clm/taches?etape=${a.etapeId}`}>{a.reference}</Link> — {a.titre}</td>
                        <td data-label={t('clm.col.etape')}>{t(`clm.type.${a.type}`)} · {a.libelle}</td>
                        <td data-label={t('clm.col.responsable')}>{a.demandeur}</td>
                        <td data-label={t('clm.col.echeance')}>{dateHeure(a.echeance)} {a.enRetard && <Statut valeur="EXPIREE" />}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Bloc>
          <Bloc zone="04" titre={t('clm.tb.activite')}>
            {donnees.activite.length === 0 ? <p className="muted">{t('clm.tb.aucunDossier')}</p> : (
              <ol className="clm-chrono">
                {donnees.activite.map((e) => (
                  <li key={`${e.sequence}-${e.empreinte}`}><strong>{t(`clm.evt.${e.type}`)}</strong> · {e.acteur}<br />
                    <span className="muted">{dateHeure(e.horodatage)}{e.detail ? ` · ${e.detail}` : ''}</span></li>
                ))}
              </ol>
            )}
          </Bloc>
          <Bloc zone="04" titre={t('clm.tb.alertes')}>
            {donnees.alertesSalesforce.length === 0 ? <p className="muted">{t('clm.tb.aucuneAlerte')}</p> : (
              <ul className="clm-liste">
                {donnees.alertesSalesforce.map((s) => (
                  <li key={s.id}><Statut valeur={s.statut} /> {s.evenement} · {s.sfDossierId}
                    <br /><span className="muted">{s.derniereErreur}</span></li>
                ))}
              </ul>
            )}
          </Bloc>
          <Bloc zone="05" titre={t('clm.tb.filtres')} large>
            <form className="form-grid form-grid--4" onSubmit={(e) => { e.preventDefault(); setFiltre(form) }}>
              <label>{t('clm.su.du')}<input type="date" value={form.du} onChange={maj('du')} /></label>
              <label>{t('clm.su.au')}<input type="date" value={form.au} onChange={maj('au')} /></label>
              <label>{t('clm.champ.unite')}<input value={form.unite} onChange={maj('unite')} /></label>
              <label>{t('clm.col.statut')}
                <select value={form.statut} onChange={maj('statut')}>
                  <option value="">{t('clm.tr.tousStatuts')}</option>
                  {STATUTS.map((s) => <option key={s} value={s}>{t(`clm.statut.${s}`)}</option>)}
                </select>
              </label>
              <div className="col-2 cmp-row">
                <button type="submit">{t('clm.appliquer')}</button>
                <button type="button" className="secondary" onClick={() => { setForm(VIDE); setFiltre(VIDE) }}>{t('clm.reinitialiser')}</button>
              </div>
            </form>
          </Bloc>
        </Blocs>
      )}
    </EcranClm>
  )
}

export default function ClmTableauBord() {
  return <GardeModule><Contenu /></GardeModule>
}
