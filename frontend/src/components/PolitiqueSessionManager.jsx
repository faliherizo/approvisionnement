import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { Message, dateHeure, useErreur, useRessource } from '../pages/clm/commun.jsx'
import { Bloc, Blocs } from '../pages/clm/gabarit.jsx'

/**
 * Politique de session (ST-01) : l'administration technique fixe le délai d'inactivité et la durée maximale d'une
 * session de son organisation. Une session active se renouvelle d'elle-même, jamais au-delà de la durée maximale.
 */
export default function PolitiqueSessionManager() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: etat, recharger } = useRessource('/admin/session')
  const [valeurs, setValeurs] = useState({ inactivite: '', dureeMax: '' })
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  useEffect(() => {
    if (etat) setValeurs({ inactivite: String(etat.inactiviteMinutes), dureeMax: String(etat.dureeMaxMinutes) })
  }, [etat])

  if (!etat) return <p role="status">{t('clm.chargement')}</p>

  async function enregistrer(e) {
    e.preventDefault()
    setErreur(null)
    try {
      await api.put(`/admin/session?inactiviteMinutes=${encodeURIComponent(valeurs.inactivite)}&dureeMaxMinutes=${encodeURIComponent(valeurs.dureeMax)}`)
      setInfo(t('ps.enregistree'))
      recharger()
    } catch (x) { setErreur(versErreur(x)) }
  }

  return (
    <Blocs>
      <Bloc titre={t('ps.titre')} large>
        <Message erreur={erreur} info={info} />
        <p className="muted">{t('ps.aide')}</p>
        <p>{etat.propre ? t('ps.etatPropre', { par: etat.modifiePar, le: dateHeure(etat.modifieLe) }) : t('ps.etatDefaut')}</p>
        <form className="form" onSubmit={enregistrer}>
          <label>{t('ps.inactivite')}
            <input type="number" inputMode="numeric" min="5" max="240" required aria-describedby="ps-inactivite-aide"
              value={valeurs.inactivite} onChange={(e) => setValeurs({ ...valeurs, inactivite: e.target.value })} />
          </label>
          <p id="ps-inactivite-aide" className="muted">{t('ps.inactiviteAide')}</p>
          <label>{t('ps.dureeMax')}
            <input type="number" inputMode="numeric" min="15" max="1440" required aria-describedby="ps-duree-aide"
              value={valeurs.dureeMax} onChange={(e) => setValeurs({ ...valeurs, dureeMax: e.target.value })} />
          </label>
          <p id="ps-duree-aide" className="muted">{t('ps.dureeMaxAide')}</p>
          <div className="cmp-row"><button type="submit">{t('ps.enregistrer')}</button></div>
        </form>
      </Bloc>
    </Blocs>
  )
}
