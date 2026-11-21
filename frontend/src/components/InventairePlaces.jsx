import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/**
 * Inventaire des inscriptions par choix (CHU 4.08) : places, inscrits et restantes pour chaque choix d'un formulaire,
 * avec les fiches qui occupent les places ; export CSV. N'apparaît que si le formulaire comporte des champs à choix.
 */
export default function InventairePlaces({ formulaireId, rafraichir }) {
  const { t, locale } = useI18n()
  const [inventaire, setInventaire] = useState([])
  const [ouvert, setOuvert] = useState(null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    api.get(`/formulaires/${formulaireId}/inventaire`).then((r) => setInventaire(r.data || [])).catch(() => setInventaire([]))
  }, [formulaireId, rafraichir])

  async function exporter() {
    try {
      const r = await api.get(`/formulaires/${formulaireId}/inventaire.csv`, { responseType: 'blob' })
      const url = URL.createObjectURL(r.data)
      Object.assign(document.createElement('a'), { href: url, download: `inventaire-${formulaireId}.csv` }).click()
      URL.revokeObjectURL(url)
    } catch { setErreur(t('inventaire.erreurExport')) }
  }

  if (inventaire.length === 0) return null
  const date = (iso) => (iso ? new Date(iso).toLocaleString(locale === 'en' ? 'en-CA' : 'fr-CA') : '—')
  const cle = (i) => `${i.place.champ}::${i.place.choix}`

  return (
    <section className="panel" aria-labelledby="inventaire-titre">
      <h2 id="inventaire-titre">{t('inventaire.titre')}</h2>
      {erreur && <div className="erreur" role="alert">{erreur}</div>}
      <div className="table-scroll">
        <table className="table">
          <thead><tr>
            <th scope="col">{t('inventaire.champ')}</th><th scope="col">{t('inventaire.choix')}</th>
            <th scope="col">{t('inventaire.capacite')}</th><th scope="col">{t('inventaire.inscrits')}</th>
            <th scope="col">{t('inventaire.restantes')}</th><th scope="col"><span className="sr-only">{t('inventaire.actions')}</span></th>
          </tr></thead>
          <tbody>
            {inventaire.map((i) => (
              <tr key={cle(i)}>
                <td>{i.place.libelleChamp}</td>
                <td>{i.place.choix} {i.place.complet && <span className="badge s-REFUSE">{t('rendu.complet')}</span>}</td>
                <td>{i.place.capacite ?? t('inventaire.illimite')}</td>
                <td>{i.place.inscrits}</td>
                <td>{i.place.restantes ?? '—'}</td>
                <td>
                  {i.inscriptions.length > 0 && (
                    <button type="button" className="secondary" aria-expanded={ouvert === cle(i)}
                            onClick={() => setOuvert(ouvert === cle(i) ? null : cle(i))}>
                      {t('inventaire.voirInscriptions', { choix: i.place.choix })}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {inventaire.filter((i) => cle(i) === ouvert).map((i) => (
        <div key={cle(i)} className="table-scroll">
          <table className="table">
            <caption>{t('inventaire.inscriptionsDe', { choix: i.place.choix })}</caption>
            <thead><tr>
              <th scope="col">{t('fiches.colNumero')}</th><th scope="col">{t('fiches.colStatut')}</th>
              <th scope="col">{t('fiches.colSoumis')}</th><th scope="col">{t('inventaire.demandeur')}</th>
            </tr></thead>
            <tbody>{i.inscriptions.map((f) => (
              <tr key={f.numero}><td><code>{f.numero}</code></td><td>{t(`statutSaisie.${f.statut}`)}</td><td>{date(f.soumisLe)}</td><td>{f.demandeur}</td></tr>
            ))}</tbody>
          </table>
        </div>
      ))}
      <button type="button" className="secondary" onClick={exporter}>⬇ {t('inventaire.exportCsv')}</button>
    </section>
  )
}
