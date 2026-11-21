import { useEffect, useState } from 'react'
import api from '../api'
import ReleveBanque from '../components/ReleveBanque.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Banque d'heures de services professionnels de l'organisation (CHU 9.05) : solde, répartition et relevé téléchargeable. */
export default function BanqueHeures() {
  const { t } = useI18n()
  const [banques, setBanques] = useState([])
  const [courante, setCourante] = useState(null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    api.get('/soutien/banque-heures').then((r) => {
      setBanques(r.data)
      if (r.data[0]) api.get(`/soutien/banque-heures/${r.data[0].id}`).then((x) => setCourante(x.data))
    }).catch(() => setErreur(t('banque.erreur')))
  }, [t])

  async function releve() {
    try {
      const r = await api.get(`/soutien/banque-heures/${courante.id}/releve.csv`, { responseType: 'blob' })
      const url = URL.createObjectURL(r.data)
      Object.assign(document.createElement('a'), { href: url, download: `releve-banque-${courante.id}.csv` }).click()
      URL.revokeObjectURL(url)
    } catch { setErreur(t('banque.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('banque.titre')}</h1>
        <p className="muted">{t('banque.intro')}</p>
        {erreur && <div className="erreur" role="alert">{erreur}</div>}
        {banques.length === 0 && <p className="muted">{t('banque.aucune')}</p>}
        {banques.length > 1 && (
          <label>{t('banque.choisir')}
            <select value={courante?.id ?? ''} onChange={(e) => api.get(`/soutien/banque-heures/${e.target.value}`).then((x) => setCourante(x.data))}>
              {banques.map((b) => <option key={b.id} value={b.id}>{b.nom} ({b.debut} – {b.fin})</option>)}
            </select>
          </label>
        )}
      </div>
      {courante && (
        <section className="panel" aria-labelledby="banque-courante">
          <h2 id="banque-courante">{courante.nom} <span className="muted">({courante.debut} – {courante.fin})</span></h2>
          <ReleveBanque banque={courante} />
          <button type="button" className="secondary" onClick={releve}>⬇ {t('banque.telechargerReleve')}</button>
        </section>
      )}
    </div>
  )
}
