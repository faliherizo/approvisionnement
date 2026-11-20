import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

/**
 * Départ d'un employé (P3) : l'administration d'affaires voit ce que la personne détient encore — trousses ouvertes,
 * étapes qui la désignent, tâches ouvertes —, puis le confie en une opération à une personne active de l'organisation.
 */
function EcranDepart() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: membres } = useRessource('/clm/membres')
  const [de, setDe] = useState('')
  const [inventaire, setInventaire] = useState(null)
  const [vers, setVers] = useState('')
  const [motif, setMotif] = useState('')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function consulter(e) {
    e.preventDefault()
    setErreur(null); setInfo('')
    try { setInventaire((await api.get('/clm/admin/departs', { params: { courriel: de } })).data) } catch (x) { setInventaire(null); setErreur(versErreur(x)) }
  }
  async function transferer(e) {
    e.preventDefault()
    if (!window.confirm(t('clm.dp.confirmer', { de: inventaire.courriel, vers }))) return
    setErreur(null)
    try {
      const { data } = await api.post('/clm/admin/departs', { de: inventaire.courriel, vers, motif })
      setInfo(t('clm.dp.bilan', data))
      setInventaire((await api.get('/clm/admin/departs', { params: { courriel: data.de } })).data)
    } catch (x) { setErreur(versErreur(x)) }
  }
  const rien = inventaire && inventaire.trousses.length === 0 && inventaire.etapes === 0 && inventaire.taches === 0
  const candidats = (membres || []).filter((m) => !inventaire || m.courriel.toLowerCase() !== inventaire.courriel)

  return (
    <EcranClm titre={t('clm.dp.titre')} objectif={t('clm.dp.objectif')}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc titre={t('clm.dp.blocInventaire')}>
          <form className="clm-gabarit" onSubmit={consulter}>
            <label>{t('clm.dp.de')}<input type="email" required value={de} onChange={(e) => setDe(e.target.value)} autoComplete="off" /></label>
            <button type="submit">{t('clm.dp.consulter')}</button>
          </form>
          {inventaire && (
            <dl className="clm-dl">
              <div><dt>{t('clm.dp.trousses')}</dt><dd>{inventaire.trousses.length === 0 ? t('clm.dp.aucune') : inventaire.trousses.join(', ')}</dd></div>
              <div><dt>{t('clm.dp.etapes')}</dt><dd>{inventaire.etapes}</dd></div>
              <div><dt>{t('clm.dp.taches')}</dt><dd>{inventaire.taches}</dd></div>
            </dl>
          )}
        </Bloc>
        {inventaire && (
          <Bloc titre={t('clm.dp.blocTransfert')}>
            {rien ? <p role="status">{t('clm.dp.rien', { courriel: inventaire.courriel })}</p> : (
              <form className="form-grid" onSubmit={transferer}>
                <label>{t('clm.dp.vers')}
                  <select required value={vers} onChange={(e) => setVers(e.target.value)}>
                    <option value="">{t('clm.dp.choisir')}</option>
                    {candidats.map((m) => <option key={m.courriel} value={m.courriel}>{m.nom} ({m.courriel})</option>)}
                  </select>
                </label>
                <label>{t('clm.col.motif')}<input value={motif} onChange={(e) => setMotif(e.target.value)} placeholder={t('clm.dp.motifAide')} /></label>
                <div className="cmp-row col-2"><button type="submit" disabled={!vers}>{t('clm.dp.transferer')}</button></div>
              </form>
            )}
          </Bloc>
        )}
      </Blocs>
    </EcranClm>
  )
}

export default function DepartEmploye() {
  return <GardeModule><EcranDepart /></GardeModule>
}
