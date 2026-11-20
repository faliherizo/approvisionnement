import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { Message, dateHeure, useErreur, useRessource } from '../pages/clm/commun.jsx'
import { Bloc, Blocs } from '../pages/clm/gabarit.jsx'

/**
 * Connexion locale et comptes de secours (addenda 4, Q12) : l'administration technique désigne les comptes de secours,
 * munis d'un second facteur, et peut réserver la connexion locale à eux seuls ; les autres passent par l'authentification unique.
 */
export default function ConnexionLocaleManager() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: etat, recharger } = useRessource('/admin/connexion-locale')
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function faire(fn, ok) {
    setErreur(null)
    try { await fn(); setInfo(ok); recharger() } catch (x) { setErreur(versErreur(x)) }
  }
  if (!etat) return <p role="status">{t('clm.chargement')}</p>
  const reserver = () => faire(() => api.put(`/admin/connexion-locale?reservee=${!etat.reservee}`),
    t(etat.reservee ? 'cl.ouverte' : 'cl.reservee'))

  return (
    <Blocs>
      <Bloc titre={t('cl.titre')} large>
        <Message erreur={erreur} info={info} />
        <p className="muted">{t('cl.aide')}</p>
        <p>{etat.reservee ? t('cl.etatReservee', { par: etat.modifiePar, le: dateHeure(etat.modifieLe) }) : t('cl.etatOuverte')}</p>
        <div className="cmp-row"><button type="button" className="secondary" onClick={reserver}>{t(etat.reservee ? 'cl.ouvrir' : 'cl.reserver')}</button></div>
        <div className="table-scroll">
          <table className="table clm-table">
            <caption className="sr-only">{t('cl.comptes')}</caption>
            <thead><tr><th scope="col">{t('clm.col.nom')}</th><th scope="col">{t('cl.secondFacteur')}</th><th scope="col">{t('cl.secours')}</th></tr></thead>
            <tbody>
              {etat.comptes.map((c) => (
                <tr key={c.id}>
                  <td data-label={t('clm.col.nom')}>{c.nom}<br /><span className="muted">{c.courriel}</span></td>
                  <td data-label={t('cl.secondFacteur')}>{c.secondFacteur ? t('cl.oui') : t('cl.non')}</td>
                  <td data-label={t('cl.secours')}>
                    <label className="check-row">
                      <input type="checkbox" checked={c.secours} aria-label={`${t('cl.secours')} — ${c.courriel}`}
                        onChange={(e) => faire(() => api.put(`/admin/connexion-locale/comptes/${c.id}?secours=${e.target.checked}`),
                          t(e.target.checked ? 'cl.designe' : 'cl.retire', { courriel: c.courriel }))} />
                      {c.secours ? t('cl.oui') : t('cl.non')}
                    </label>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Bloc>
    </Blocs>
  )
}
