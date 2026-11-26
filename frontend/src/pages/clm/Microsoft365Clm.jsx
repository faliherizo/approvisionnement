import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import IntegrationM365Manager from '../../components/IntegrationM365Manager.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, Statut, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const BIBLIOTHEQUES = ['SHAREPOINT', 'ONEDRIVE']

/** Réglage de la diffusion : bibliothèque, dossier, dossier de preuve et avis Teams (Annexe 10, TI-01). */
function FormulaireDiffusion({ reglage, onEnregistrer }) {
  const { t } = useI18n()
  const [f, setF] = useState(reglage)
  useEffect(() => setF(reglage), [reglage])
  const coche = (champ) => (e) => setF({ ...f, [champ]: e.target.checked })
  return (
    <form className="form" aria-label={t('clm.m365.blocDiffusion')} onSubmit={(e) => { e.preventDefault(); onEnregistrer(f) }}>
      <label className="check-row"><input type="checkbox" checked={f.actif} onChange={coche('actif')} />{t('clm.m365.actif')}</label>
      <label>{t('clm.m365.bibliotheque')}
        <select value={f.bibliotheque} onChange={(e) => setF({ ...f, bibliotheque: e.target.value })}>
          {BIBLIOTHEQUES.map((b) => <option key={b} value={b}>{t(`clm.m365.bibliotheque.${b}`)}</option>)}
        </select>
      </label>
      <label>{t('clm.m365.dossier')} <span className="muted">{t('clm.m365.dossierAide')}</span>
        <input value={f.dossier} onChange={(e) => setF({ ...f, dossier: e.target.value })} required maxLength={300} />
      </label>
      <label className="check-row"><input type="checkbox" checked={f.deposerPreuve} onChange={coche('deposerPreuve')} />{t('clm.m365.preuve')}</label>
      <label className="check-row"><input type="checkbox" checked={f.avisTeams} onChange={coche('avisTeams')} />{t('clm.m365.avisTeams')}</label>
      <div className="cmp-row"><button type="submit">{t('clm.m365.enregistrer')}</button></div>
    </form>
  )
}

/**
 * Microsoft 365 pour le module Contrats et signature (Annexe 10, TI-01) : connexion de l'organisation (la même que celle du
 * module de rédaction, simulée tant qu'elle n'est pas activée), dépôt des trousses terminées dans SharePoint ou OneDrive,
 * avis Teams, suivi et relance des dépôts.
 */
function Microsoft365() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: reglage, recharger: rechargerReglage } = useRessource('/clm/admin/integrations/m365')
  const { donnees: depots, recharger } = useRessource('/clm/admin/integrations/m365/depots')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(typeof ok === 'function' ? ok(r.data) : ok); recharger(); rechargerReglage() } catch (x) { setErreur(versErreur(x)) }
  }
  const enregistrer = (f) => faire(() => api.put('/clm/admin/integrations/m365', f), t('clm.m365.enregistre'))
  const relayer = () => faire(() => api.post('/clm/admin/integrations/m365/relayer'), (d) => t('clm.m365.relaye', { n: d.deposes }))
  const relancer = (d) => faire(() => api.post(`/clm/admin/integrations/m365/depots/${d.id}/relancer`), t('clm.m365.relance', { ref: d.reference }))

  return (
    <EcranClm maquette="UI-21" titre={t('clm.m365.titre')} objectif={t('clm.m365.objectif')}
      ariane={<Link to="/clm/admin/integrations">{t('clm.nav.integrations')}</Link>}
      actions={<button type="button" className="secondary" onClick={relayer}>{t('clm.m365.relayer')}</button>}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.m365.blocDiffusion')}>
          <p className="muted">{t('clm.m365.diffusionAide')}</p>
          {!reglage ? <p role="status">{t('clm.chargement')}</p> : (
            <>
              <p>{reglage.modifiePar ? t('clm.m365.modifie', { par: reglage.modifiePar, le: dateHeure(reglage.modifieLe) }) : t('clm.m365.jamaisRegle')}</p>
              <FormulaireDiffusion reglage={reglage} onEnregistrer={enregistrer} />
            </>
          )}
        </Bloc>
        <Bloc zone="02" titre={t('clm.m365.blocConnexion')} large>
          <p className="muted">{t('clm.m365.connexionAide')}</p>
          <IntegrationM365Manager />
        </Bloc>
        <Bloc zone="03" titre={t('clm.m365.blocDepots')} large>
          {!depots ? <p role="status">{t('clm.chargement')}</p> : depots.length === 0 ? <p className="muted">{t('clm.m365.aucunDepot')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.m365.blocDepots')}</caption>
                <thead><tr><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.m365.trousse')}</th><th scope="col">{t('clm.m365.nature')}</th>
                  <th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.tentatives')}</th><th scope="col">{t('clm.m365.emplacement')}</th>
                  <th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {depots.map((d) => (
                    <tr key={d.id}>
                      <td data-label={t('clm.col.date')}>{dateHeure(d.creeLe)}</td>
                      <td data-label={t('clm.m365.trousse')}><Link to={`/clm/trousses/${d.trousseId}`}>{d.reference}</Link></td>
                      <td data-label={t('clm.m365.nature')}>{t(`clm.m365.nature.${d.nature}`)}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={d.statut} /></td>
                      <td data-label={t('clm.col.tentatives')}>{d.tentatives}</td>
                      <td data-label={t('clm.m365.emplacement')} className="clm-casse">{d.url || d.derniereErreur || '—'}</td>
                      <td data-label={t('clm.col.actions')}>
                        {d.statut !== 'DEPOSE' && (
                          <button type="button" className="lien" aria-label={t('clm.m365.relancerDepot', { ref: d.reference, nature: t(`clm.m365.nature.${d.nature}`) })}
                            onClick={() => relancer(d)}>{t('clm.ad.relancer')}</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function Microsoft365Clm() {
  return <GardeModule><Microsoft365 /></GardeModule>
}
