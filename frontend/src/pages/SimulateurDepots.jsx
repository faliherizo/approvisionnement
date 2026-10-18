import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const ENVIRONNEMENTS = [
  { code: 'SHAREPOINT', libelle: 'SharePoint' },
  { code: 'ONEDRIVE', libelle: 'OneDrive' },
  { code: 'TEAMS', libelle: 'Teams' },
  { code: 'OUTLOOK', libelle: 'Outlook' },
  { code: 'SAP', libelle: 'SAP' },
]

/**
 * Visionneuse des dépôts simulés : liste, par environnement (SharePoint, OneDrive, Teams, Outlook,
 * SAP), les fichiers réellement écrits par la simulation, avec aperçu/téléchargement. Permet de
 * constater ce qui « part » avant de brancher un vrai environnement Microsoft 365 / SAP.
 */
export default function SimulateurDepots() {
  const { t } = useI18n()
  const [depots, setDepots] = useState([])
  const [err, setErr] = useState(null)
  const [charge, setCharge] = useState(false)

  const charger = () => {
    setCharge(true); setErr(null)
    api.get('/simulateur/depots')
      .then((r) => setDepots(r.data))
      .catch(() => setErr(t('simulateurdepots.chargementImpossible')))
      .finally(() => setCharge(false))
  }
  useEffect(() => { charger() }, [])

  const lienContenu = (chemin) => `/api/simulateur/depots/contenu?chemin=${encodeURIComponent(chemin)}`
  const taille = (o) => (o < 1024 ? t('simulateurdepots.octets', { n: o }) : t('simulateurdepots.kilooctets', { n: (o / 1024).toFixed(1) }))
  const dossier = (chemin) => {
    const parties = chemin.split('/')
    return parties.slice(1, -1).join('/') || t('simulateurdepots.racine')
  }

  return (
    <div>
      <section className="panel">
        <h1>{t('simulateurdepots.titre')}</h1>
        <p className="muted">
          {t('simulateurdepots.introA')} <strong>{t('simulateurdepots.simulees')}</strong> {t('simulateurdepots.introB')} <strong>{t('simulateurdepots.reellementEcrits')}</strong> {t('simulateurdepots.introC')}
        </p>
        <div className="vitrine-actions">
          <button type="button" onClick={charger}>{t('simulateurdepots.rafraichir')}</button>
        </div>
        {err && <div className="erreur" role="alert">{err}</div>}
        {!err && !charge && depots.length === 0 && (
          <p className="muted">{t('simulateurdepots.aucunDepot')}</p>
        )}
      </section>

      {ENVIRONNEMENTS.map((env) => {
        const fichiers = depots.filter((d) => d.cible === env.code)
        if (fichiers.length === 0) return null
        return (
          <section className="panel" key={env.code} aria-labelledby={`env-${env.code}`}>
            <h2 id={`env-${env.code}`}>{env.libelle} <span className="muted">({fichiers.length})</span></h2>
            <table className="table" aria-label={t('simulateurdepots.depotsAria', { env: env.libelle })}>
              <thead>
                <tr>
                  <th scope="col">{t('simulateurdepots.thFichier')}</th><th scope="col">{t('simulateurdepots.thDossier')}</th>
                  <th scope="col">{t('simulateurdepots.thTaille')}</th><th scope="col">{t('simulateurdepots.thDepose')}</th><th scope="col"><span className="sr-only">{t('simulateurdepots.thActions')}</span></th>
                </tr>
              </thead>
              <tbody>
                {fichiers.map((f) => (
                  <tr key={f.chemin}>
                    <td>{f.nomFichier}</td>
                    <td><code>{dossier(f.chemin)}</code></td>
                    <td>{taille(f.taille)}</td>
                    <td>{new Date(f.depuis).toLocaleString('fr-CA')}</td>
                    <td>
                      <a className="secondary" href={lienContenu(f.chemin)} target="_blank" rel="noreferrer">
                        {t('simulateurdepots.ouvrir')}
                      </a>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )
      })}
    </div>
  )
}
