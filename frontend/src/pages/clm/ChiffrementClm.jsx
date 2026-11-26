import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

/**
 * Clé de chiffrement au repos dédiée à l'organisation (Annexe 10, SEC-28 ; ADR-037) : versions de la clé, documents
 * chiffrés par chacune, rotation sans interruption et réenveloppement par lots. Aucune clé n'est jamais affichée.
 */
function Chiffrement() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: etat, recharger } = useRessource('/clm/admin/chiffrement')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(ok(r.data)); recharger() } catch (x) { setErreur(versErreur(x)) }
  }
  const pivoter = () => {
    if (!window.confirm(t('clm.chiffre.confirmerRotation'))) return
    faire(() => api.post('/clm/admin/chiffrement/pivoter'), (d) => t('clm.chiffre.pivotee', { v: d.versions[0]?.version }))
  }
  const reenvelopper = () => faire(() => api.post('/clm/admin/chiffrement/reenvelopper'),
    (d) => t('clm.chiffre.reenveloppe', { cles: d.cles, docs: d.documents, restants: d.restants }))

  return (
    <EcranClm maquette="UI-21" titre={t('clm.chiffre.titre')} objectif={t('clm.chiffre.objectif')}
      actions={<>
        <button type="button" className="secondary" onClick={reenvelopper}>{t('clm.chiffre.reenvelopper')}</button>
        <button type="button" onClick={pivoter}>{t('clm.chiffre.pivoter')}</button>
      </>}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.chiffre.blocCle')} large>
          <p className="muted">{t('clm.chiffre.aide')}</p>
          {!etat ? <p role="status">{t('clm.chargement')}</p> : (
            <>
              <dl className="clm-dl">
                <div><dt>{t('clm.chiffre.cleMaitresse')}</dt><dd><code className="clm-casse">{etat.cleMaitresseActive}</code></dd></div>
                <div><dt>{t('clm.chiffre.nonChiffres')}</dt><dd>{etat.nonChiffres}</dd></div>
              </dl>
              {etat.versions.length === 0 ? <p className="muted">{t('clm.chiffre.aucuneVersion')}</p> : (
                <div className="table-scroll">
                  <table className="table clm-table">
                    <caption className="sr-only">{t('clm.chiffre.blocCle')}</caption>
                    <thead><tr><th scope="col">{t('clm.chiffre.version')}</th><th scope="col">{t('clm.col.statut')}</th>
                      <th scope="col">{t('clm.chiffre.documents')}</th><th scope="col">{t('clm.chiffre.creeLe')}</th>
                      <th scope="col">{t('clm.chiffre.enveloppe')}</th></tr></thead>
                    <tbody>
                      {etat.versions.map((v) => (
                        <tr key={v.version}>
                          <td data-label={t('clm.chiffre.version')}>v{v.version}</td>
                          <td data-label={t('clm.col.statut')}>{t(`clm.chiffre.statut.${v.statut}`)}</td>
                          <td data-label={t('clm.chiffre.documents')}>{v.documents}</td>
                          <td data-label={t('clm.chiffre.creeLe')}>{dateHeure(v.creeLe)}</td>
                          <td data-label={t('clm.chiffre.enveloppe')} className="clm-casse">
                            {v.cleMaitresse}{v.aReenvelopper ? ` — ${t('clm.chiffre.aReenvelopper')}` : ''}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function ChiffrementClm() {
  return <GardeModule><Chiffrement /></GardeModule>
}
