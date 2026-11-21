import { useEffect, useState } from 'react'
import api from '../api'
import Modal from './Modal.jsx'
import { RichHtml } from './RichText.jsx'
import { useI18n } from '../i18n/I18nContext'

/**
 * Historique d'une clause (Lévis EF1, C-03) : chaque version avec son auteur, sa date et son motif ; le texte d'une version
 * se consulte et, pour qui peut modifier la clause, se restaure (la restauration crée une nouvelle version).
 */
export default function ClauseHistorique({ clause, peutRestaurer, onRestauree, onClose }) {
  const { t, locale } = useI18n()
  const [versions, setVersions] = useState([])
  const [choisie, setChoisie] = useState(null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    api.get(`/clauses/${clause.id}/versions`)
      .then((r) => { setVersions(r.data); setChoisie(r.data[0] ?? null) })
      .catch(() => setErreur(t('clauses.history.loadError')))
  }, [clause.id])

  async function restaurer(numero) {
    try {
      const r = await api.post(`/clauses/${clause.id}/versions/${numero}/restaurer`)
      onRestauree(r.data)
    } catch (e) {
      setErreur(e.response?.data?.message || t('clauses.history.restoreError'))
    }
  }

  const date = (iso) => new Date(iso).toLocaleString(locale === 'en' ? 'en-CA' : 'fr-CA')

  return (
    <Modal titre={t('clauses.history.title', { code: clause.code })} onClose={onClose} taille="lg">
      {erreur && <p className="error" role="alert">{erreur}</p>}
      <div className="table-scroll">
        <table>
          <caption className="sr-only">{t('clauses.history.caption')}</caption>
          <thead>
            <tr>
              <th scope="col">{t('clauses.history.version')}</th>
              <th scope="col">{t('clauses.history.date')}</th>
              <th scope="col">{t('clauses.history.author')}</th>
              <th scope="col">{t('clauses.history.reason')}</th>
              <th scope="col"><span className="sr-only">{t('clauses.history.actions')}</span></th>
            </tr>
          </thead>
          <tbody>
            {versions.map((v) => (
              <tr key={`${v.numero}-${v.id ?? 'origine'}`} aria-current={choisie === v ? 'true' : undefined}>
                <td>v{v.numero}</td>
                <td>{date(v.creeLe)}</td>
                <td>{v.auteur}</td>
                <td>{t(`clauses.history.motif.${v.motif}`, { n: v.versionRestauree })}</td>
                <td className="row-actions">
                  <button type="button" className="secondary" onClick={() => setChoisie(v)}>
                    {t('clauses.history.view', { n: v.numero })}
                  </button>
                  {peutRestaurer && v.numero !== clause.version && (
                    <button type="button" onClick={() => restaurer(v.numero)}>
                      {t('clauses.history.restore', { n: v.numero })}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {choisie && (
        <section aria-labelledby="version-choisie">
          <h3 id="version-choisie">{t('clauses.history.textOf', { n: choisie.numero, titre: choisie.titre })}</h3>
          <RichHtml html={choisie.corps} />
        </section>
      )}
    </Modal>
  )
}
