import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import { ConfirmModal } from '../components/Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

const VIDE_EFFACEMENT = { courriel: '', motif: '' }

/**
 * Droits Loi 25 de la personne concernée (Lévis ENF1, C-15) : l'administration de l'organisation communique à une personne
 * tout ce qui est détenu sur elle (format structuré, téléchargeable), l'efface sur demande, et tient le registre des demandes.
 */
export default function VieePrivee() {
  const { t, locale } = useI18n()
  const [courrielAcces, setCourrielAcces] = useState('')
  const [dossier, setDossier] = useState(null)
  const [effacement, setEffacement] = useState(VIDE_EFFACEMENT)
  const [aConfirmer, setAConfirmer] = useState(false)
  const [bilan, setBilan] = useState(null)
  const [registre, setRegistre] = useState([])
  const [erreur, setErreur] = useState(null)

  const date = (iso) => (iso ? new Date(iso).toLocaleString(locale === 'en' ? 'en-CA' : 'fr-CA') : '—')
  const chargerRegistre = useCallback(() => {
    api.get('/loi25/demandes').then((r) => setRegistre(r.data)).catch(() => setRegistre([]))
  }, [])
  useEffect(chargerRegistre, [chargerRegistre])

  async function consulter(e) {
    e.preventDefault(); setErreur(null); setDossier(null)
    try { setDossier((await api.post('/loi25/acces', { courriel: courrielAcces })).data); chargerRegistre() }
    catch (err) { setErreur(err.response?.data?.message || t('loi25.erreur')) }
  }

  function telecharger() {
    const url = URL.createObjectURL(new Blob([JSON.stringify(dossier, null, 2)], { type: 'application/json' }))
    const a = Object.assign(document.createElement('a'), { href: url, download: `renseignements-${dossier.produitLe.slice(0, 10)}.json` })
    a.click(); URL.revokeObjectURL(url)
  }

  async function effacer() {
    setErreur(null); setBilan(null)
    try { setBilan((await api.post('/loi25/effacement', effacement)).data); setEffacement(VIDE_EFFACEMENT); chargerRegistre() }
    catch (err) { setErreur(err.response?.data?.message || t('loi25.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('loi25.titre')}</h1>
        <p className="muted">{t('loi25.intro')}</p>
        {erreur && <div className="erreur" role="alert">{erreur}</div>}
      </div>

      <section className="panel" aria-labelledby="loi25-acces">
        <h2 id="loi25-acces">{t('loi25.acces')}</h2>
        <p className="muted">{t('loi25.accesAide')}</p>
        <form onSubmit={consulter} className="form">
          <label>{t('loi25.courriel')}
            <input type="email" required autoComplete="off" value={courrielAcces} onChange={(e) => setCourrielAcces(e.target.value)} />
          </label>
          <button type="submit">{t('loi25.consulter')}</button>
        </form>
        {dossier && (
          <div aria-live="polite">
            <p>{t('loi25.resume', { n: dossier.sources.reduce((s, x) => s + x.elements.length, 0), courriel: dossier.courriel })}</p>
            <button type="button" className="secondary" onClick={telecharger}>{t('loi25.telecharger')}</button>
            {dossier.sources.map((s) => (
              <section key={s.source} aria-label={s.source}>
                <h3>{s.source} <span className="badge">{s.elements.length}</span></h3>
                <p className="muted">{s.description} {s.effacement}</p>
                {s.elements.length > 0 && (
                  <div className="table-scroll">
                    <table className="table">
                      <thead><tr>
                        <th scope="col">{t('loi25.reference')}</th><th scope="col">{t('loi25.date')}</th><th scope="col">{t('loi25.valeurs')}</th>
                      </tr></thead>
                      <tbody>
                        {s.elements.map((r) => (
                          <tr key={r.reference}>
                            <td><code>{r.reference}</code></td>
                            <td>{date(r.date)}</td>
                            <td><dl className="dl-compact">{Object.entries(r.valeurs).map(([k, v]) => (
                              <div key={k}><dt>{k}</dt><dd>{v || '—'}</dd></div>
                            ))}</dl></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </section>
            ))}
          </div>
        )}
      </section>

      <section className="panel" aria-labelledby="loi25-effacement">
        <h2 id="loi25-effacement">{t('loi25.effacement')}</h2>
        <p className="muted">{t('loi25.effacementAide')}</p>
        <form onSubmit={(e) => { e.preventDefault(); setAConfirmer(true) }} className="form">
          <label>{t('loi25.courriel')}
            <input type="email" required autoComplete="off" value={effacement.courriel}
                   onChange={(e) => setEffacement({ ...effacement, courriel: e.target.value })} />
          </label>
          <label>{t('loi25.motif')}
            <input required maxLength={500} value={effacement.motif} placeholder={t('loi25.motifAide')}
                   onChange={(e) => setEffacement({ ...effacement, motif: e.target.value })} />
          </label>
          <button type="submit" className="danger">{t('loi25.effacer')}</button>
        </form>
        {bilan && (
          <div className="table-scroll" aria-live="polite">
            <table className="table">
              <caption>{t('loi25.bilan')}</caption>
              <thead><tr>
                <th scope="col">{t('loi25.source')}</th><th scope="col">{t('loi25.effaces')}</th>
                <th scope="col">{t('loi25.conserves')}</th><th scope="col">{t('loi25.motifConservation')}</th>
              </tr></thead>
              <tbody>{bilan.map((b) => (
                <tr key={b.source}><td>{b.source}</td><td>{b.effaces}</td><td>{b.conserves}</td><td>{b.motifConservation || '—'}</td></tr>
              ))}</tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel" aria-labelledby="loi25-registre">
        <h2 id="loi25-registre">{t('loi25.registre')}</h2>
        <div className="table-scroll">
          <table className="table">
            <thead><tr>
              <th scope="col">{t('loi25.date')}</th><th scope="col">{t('loi25.type')}</th><th scope="col">{t('loi25.traitePar')}</th>
              <th scope="col">{t('loi25.motif')}</th><th scope="col">{t('loi25.resultat')}</th>
            </tr></thead>
            <tbody>
              {registre.map((d) => (
                <tr key={d.id}><td>{date(d.traiteeLe)}</td><td>{t(`loi25.type.${d.type}`)}</td><td>{d.traitePar}</td><td>{d.motif || '—'}</td><td>{d.resultat}</td></tr>
              ))}
              {registre.length === 0 && <tr><td colSpan="5" className="muted">{t('loi25.registreVide')}</td></tr>}
            </tbody>
          </table>
        </div>
      </section>

      {aConfirmer && (
        <ConfirmModal titre={t('loi25.confirmerTitre')} message={t('loi25.confirmerMessage', { courriel: effacement.courriel })}
                      onConfirm={effacer} onClose={() => setAConfirmer(false)} />
      )}
    </div>
  )
}
