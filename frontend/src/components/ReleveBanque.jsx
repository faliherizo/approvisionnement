import { useI18n } from '../i18n/I18nContext'

const CATEGORIES = ['ANALYSE', 'FORMATION', 'CONFIGURATION', 'PROGRAMMATION', 'RAPPORTS']

/** Solde d'une banque d'heures, répartition par catégorie (barre proportionnelle + valeur écrite) et relevé (CHU 9.05). */
export default function ReleveBanque({ banque }) {
  const { t, locale } = useI18n()
  const h = (n) => Number(n).toLocaleString(locale === 'en' ? 'en-CA' : 'fr-CA', { minimumFractionDigits: 2 })
  const s = banque.solde
  const max = Math.max(1, ...CATEGORIES.map((c) => Number(s.parCategorie[c] || 0)))
  return (
    <div>
      <div className="tableaux-bord-grille">
        {[['achetees', s.achetees], ['consommees', s.consommees], ['restantes', s.restantes]].map(([cle, v]) => (
          <figure key={cle} className="indicateur">
            <figcaption>{t(`banque.${cle}`)}</figcaption>
            <p className="indicateur-nombre">{h(v)} h</p>
          </figure>
        ))}
      </div>
      <h3>{t('banque.parCategorie')}</h3>
      <table className="table">
        <thead><tr><th scope="col">{t('banque.categorie')}</th><th scope="col">{t('banque.heures')}</th></tr></thead>
        <tbody>
          {CATEGORIES.map((c) => (
            <tr key={c}>
              <td>{t(`banque.categorie.${c}`)}</td>
              <td>
                <span className="barre-inline" style={{ width: `${(100 * Number(s.parCategorie[c] || 0)) / max}%` }} aria-hidden="true" />
                {' '}{h(s.parCategorie[c] || 0)} h
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {banque.mouvements?.length > 0 && (
        <>
          <h3>{t('banque.releve')}</h3>
          <div className="table-scroll">
            <table className="table">
              <thead><tr>
                <th scope="col">{t('banque.date')}</th><th scope="col">{t('banque.type')}</th><th scope="col">{t('banque.categorie')}</th>
                <th scope="col">{t('banque.heures')}</th><th scope="col">{t('banque.description')}</th><th scope="col">{t('banque.intervenant')}</th>
              </tr></thead>
              <tbody>{banque.mouvements.map((m) => (
                <tr key={m.id}>
                  <td>{m.date}</td><td>{t(`banque.type.${m.type}`)}</td><td>{m.categorie ? t(`banque.categorie.${m.categorie}`) : '—'}</td>
                  <td>{m.type === 'CONSOMMATION' ? '−' : '+'}{h(m.heures)}</td><td>{m.description}</td><td>{m.intervenant || '—'}</td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}

export { CATEGORIES }
