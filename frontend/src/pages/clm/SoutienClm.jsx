import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

/**
 * Accès de soutien de l'exploitant (Annexe 10, SEC-17) : l'administration d'affaires voit chaque demande, l'accorde ou la
 * refuse, révoque un accès en cours et suit les consultations. Hors accès accordé, l'exploitant ne lit aucune donnée.
 */
function Soutien() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: acces, recharger } = useRessource('/clm/admin/soutien')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function decider(a, action, ok) {
    setErreur(null)
    try { await api.post(`/clm/admin/soutien/${a.id}/${action}`); setInfo(t(ok, { qui: a.demandeur })); recharger() } catch (x) { setErreur(versErreur(x)) }
  }

  return (
    <EcranClm maquette="UI-23" titre={t('clm.so.titre')} objectif={t('clm.so.objectif')}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.so.blocAcces')} large>
          <p className="muted">{t('clm.so.aide')}</p>
          {!acces ? <p role="status">{t('clm.chargement')}</p> : acces.length === 0 ? <p className="muted">{t('clm.so.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.so.blocAcces')}</caption>
                <thead><tr><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.so.demandeur')}</th><th scope="col">{t('clm.so.motif')}</th>
                  <th scope="col">{t('clm.so.duree')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.so.expire')}</th>
                  <th scope="col">{t('clm.so.consultations')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {acces.map((a) => (
                    <tr key={a.id}>
                      <td data-label={t('clm.col.date')}>{dateHeure(a.demandeLe)}</td>
                      <td data-label={t('clm.so.demandeur')}>{a.demandeur}</td>
                      <td data-label={t('clm.so.motif')}>{a.motif}</td>
                      <td data-label={t('clm.so.duree')}>{t('clm.so.heures', { n: a.dureeHeures })}</td>
                      <td data-label={t('clm.col.statut')}>{t(`clm.so.statut.${a.statut}`)}{a.decidePar ? ` · ${a.decidePar}` : ''}</td>
                      <td data-label={t('clm.so.expire')}>{dateHeure(a.expireLe)}</td>
                      <td data-label={t('clm.so.consultations')}>{a.consultations}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        {a.statut === 'DEMANDE' && (
                          <>
                            <button type="button" className="lien" aria-label={t('clm.so.accorderA', { qui: a.demandeur })}
                              onClick={() => decider(a, 'accorder', 'clm.so.accorde')}>{t('clm.so.accorder')}</button>
                            <button type="button" className="lien" aria-label={t('clm.so.refuserA', { qui: a.demandeur })}
                              onClick={() => decider(a, 'refuser', 'clm.so.refuse')}>{t('clm.so.refuser')}</button>
                          </>
                        )}
                        {a.statut === 'ACCORDE' && (
                          <button type="button" className="lien" aria-label={t('clm.so.revoquerA', { qui: a.demandeur })}
                            onClick={() => decider(a, 'revoquer', 'clm.so.revoque')}>{t('clm.so.revoquer')}</button>
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

export default function SoutienClm() {
  return <GardeModule><Soutien /></GardeModule>
}
