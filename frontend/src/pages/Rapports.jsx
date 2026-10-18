import { useEffect, useState } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'

/** Rapports de suivi consolidés (Addenda 1, exig. 22) — pilotage. Accessible WCAG 2.2. */
export default function Rapports() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const [r, setR] = useState(null)

  useEffect(() => { api.get('/rapports/suivi').then((res) => setR(res.data)).catch(() => setR(null)) }, [])

  if (!hasRole('APPRO') && !hasRole('DIRECTION') && !hasRole('ADMIN')) {
    return <div className="panel">{t('rapports.accesReserve')}</div>
  }

  return (
    <div className="panel" aria-labelledby="titre-rapports">
      <h1 id="titre-rapports">{t('rapports.titre')}</h1>
      <p className="muted">{t('rapports.description')}</p>

      {!r && <p className="muted">{t('rapports.chargement')}</p>}
      {r && (
        <>
          <ul className="stats-inline" aria-label={t('rapports.indicateursCles')}>
            <li><strong>{r.totalDocuments}</strong> {t('rapports.documents')}</li>
            <li><strong>{r.approbationsEnAttente}</strong> {t('rapports.enAttenteApprobation')}</li>
            <li><strong>{r.dossiersActifs}</strong> {t('rapports.dossiersActifs')}</li>
            <li><strong>{r.obligationsAVenir}</strong> {t('rapports.obligationsAVenir')}</li>
          </ul>

          <div className="grid2" style={{ marginTop: '1rem' }}>
            <section aria-labelledby="titre-statut">
              <h2 id="titre-statut">{t('rapports.docsParStatut')}</h2>
              <table className="table">
                <caption className="sr-only">{t('rapports.captionStatut')}</caption>
                <thead><tr><th scope="col">{t('rapports.statut')}</th><th scope="col">{t('rapports.nombre')}</th></tr></thead>
                <tbody>
                  {Object.entries(r.documentsParStatut).map(([s, n]) => (
                    <tr key={s}><td><span className={`badge s-${s}`}>{s}</span></td><td>{n}</td></tr>
                  ))}
                  {Object.keys(r.documentsParStatut).length === 0 && (
                    <tr><td colSpan="2" className="muted">{t('rapports.aucunDocument')}</td></tr>
                  )}
                </tbody>
              </table>
            </section>

            <section aria-labelledby="titre-type">
              <h2 id="titre-type">{t('rapports.docsParType')}</h2>
              <table className="table">
                <caption className="sr-only">{t('rapports.captionType')}</caption>
                <thead><tr><th scope="col">{t('rapports.type')}</th><th scope="col">{t('rapports.nombre')}</th></tr></thead>
                <tbody>
                  {Object.entries(r.documentsParType).map(([type, n]) => (
                    <tr key={type}><td>{type}</td><td>{n}</td></tr>
                  ))}
                  {Object.keys(r.documentsParType).length === 0 && (
                    <tr><td colSpan="2" className="muted">{t('rapports.aucunDocument')}</td></tr>
                  )}
                </tbody>
              </table>
            </section>
          </div>

          <h2 style={{ marginTop: '1rem' }}>{t('rapports.activiteParUtilisateur')}</h2>
          <table className="table">
            <caption className="sr-only">{t('rapports.captionActivite')}</caption>
            <thead><tr><th scope="col">{t('rapports.utilisateur')}</th><th scope="col">{t('rapports.actions')}</th></tr></thead>
            <tbody>
              {r.activiteParUtilisateur.map((a) => (
                <tr key={a.utilisateur}><td>{a.utilisateur}</td><td>{a.actions}</td></tr>
              ))}
              {r.activiteParUtilisateur.length === 0 && (
                <tr><td colSpan="2" className="muted">{t('rapports.aucuneActivite')}</td></tr>
              )}
            </tbody>
          </table>
        </>
      )}
    </div>
  )
}
