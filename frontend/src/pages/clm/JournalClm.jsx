import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useI18n } from '../../i18n/I18nContext'
import { TYPES_EVENEMENTS } from '../../i18n/clm.messages.js'
import { GardeModule, Message, dateHeure, telecharger, useErreur, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs, EcranClm } from './gabarit.jsx'

const VIDE = { du: '', au: '', acteur: '', action: '', trousse: '', texte: '' }
const cle = (l) => `${l.trousseId}-${l.sequence}`

/** UI-23 — Journal d'audit : filtres, journal chronologique horodaté, détail et origine, export selon droits, système vs métier. */
function Contenu() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [params] = useSearchParams()
  const initial = { ...VIDE, trousse: params.get('trousse') || '' }
  const [form, setForm] = useState(initial)
  const [filtre, setFiltre] = useState(initial)
  const [choisi, setChoisi] = useState(null)
  const [erreur, setErreur] = useState(null)
  const qs = new URLSearchParams(Object.entries(filtre).filter(([, v]) => v)).toString()
  const { donnees, erreur: errChargement, chargement, recharger } = useRessource(`/clm/admin/journal${qs ? `?${qs}` : ''}`)
  const lignes = donnees || []
  const detail = lignes.find((l) => cle(l) === choisi)
  const systeme = lignes.filter((l) => l.systeme).length
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  const exporter = () => telecharger(`/clm/admin/journal/export${qs ? `?${qs}` : ''}`, 'journal-clm.csv')
    .catch((e) => setErreur(e.response?.status === 403 ? t('clm.tr.exportRefuse') : versErreur(e)))

  return (
    <EcranClm maquette="UI-23" titre={t('clm.jo.titre')} objectif={t('clm.jo.objectif')}>
      <Blocs>
        <Bloc zone="01" titre={t('clm.jo.blocFiltres')} large>
          <form className="form-grid form-grid--4" aria-label={t('clm.jo.blocFiltres')} onSubmit={(e) => { e.preventDefault(); setChoisi(null); setErreur(null); setFiltre(form) }}>
            <label>{t('clm.su.du')}<input type="date" value={form.du} onChange={maj('du')} /></label>
            <label>{t('clm.su.au')}<input type="date" value={form.au} onChange={maj('au')} /></label>
            <label>{t('clm.col.acteur')}<input value={form.acteur} onChange={maj('acteur')} /></label>
            <label>{t('clm.jo.action')}
              <select value={form.action} onChange={maj('action')}>
                <option value="">{t('clm.jo.toutesActions')}</option>
                {TYPES_EVENEMENTS.map((x) => <option key={x} value={x}>{t(`clm.evt.${x}`)}</option>)}
              </select>
            </label>
            <label>{t('clm.jo.trousse')}<input value={form.trousse} onChange={maj('trousse')} /></label>
            <label className="col-2">{t('clm.jo.texte')}<input type="search" value={form.texte} onChange={maj('texte')} /></label>
            <div className="cmp-row">
              <button type="submit">{t('clm.rechercher')}</button>
              <button type="button" className="secondary" onClick={() => { setForm(VIDE); setFiltre(VIDE); setChoisi(null) }}>{t('clm.reinitialiser')}</button>
            </div>
          </form>
        </Bloc>
      </Blocs>
      <Message erreur={erreur || errChargement} info={chargement ? t('clm.chargement') : t('clm.jo.resultats', { n: lignes.length })} onReessayer={recharger} />
      <Blocs>
        <Bloc zone="02" titre={t('clm.jo.blocJournal')} large>
          {donnees && lignes.length === 0 ? <p className="muted">{t('clm.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.jo.blocJournal')}</caption>
                <thead><tr><th scope="col">{t('clm.jo.horodatage')}</th><th scope="col">{t('clm.jo.trousse')}</th><th scope="col">{t('clm.jo.action')}</th>
                  <th scope="col">{t('clm.col.acteur')}</th><th scope="col">{t('clm.jo.origine')}</th><th scope="col">{t('clm.col.detail')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {lignes.map((l) => (
                    <tr key={cle(l)} className={choisi === cle(l) ? 'is-choisie' : ''}>
                      <td data-label={t('clm.jo.horodatage')}>{dateHeure(l.horodatage)}</td>
                      <td data-label={t('clm.jo.trousse')}><Link to={`/clm/trousses/${l.trousseId}`}>{l.reference}</Link> <span className="muted">#{l.sequence}</span></td>
                      <td data-label={t('clm.jo.action')}>{t(`clm.evt.${l.type}`)}</td>
                      <td data-label={t('clm.col.acteur')}>{l.acteur}</td>
                      <td data-label={t('clm.jo.origine')}><span className={`clm-statut ${l.systeme ? 'clm-statut--attente' : 'clm-statut--info'}`}>{l.systeme ? t('clm.jo.systeme') : t('clm.jo.metier')}</span></td>
                      <td data-label={t('clm.col.detail')} className="clm-casse">{l.detail ? (l.detail.length > 80 ? `${l.detail.slice(0, 80)}…` : l.detail) : '—'}</td>
                      <td data-label={t('clm.col.actions')}>
                        <button type="button" className="lien" aria-pressed={choisi === cle(l)} aria-label={`${t('clm.jo.ouvrir')} — ${l.reference} #${l.sequence}`} onClick={() => setChoisi(cle(l))}>{t('clm.jo.ouvrir')}</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        <Bloc zone="03" titre={t('clm.jo.blocDetail')}>
          {!detail ? <p className="muted">{t('clm.jo.choisir')}</p> : (
            <dl className="clm-dl">
              <div><dt>{t('clm.jo.trousse')}</dt><dd>{detail.reference} · #{detail.sequence}</dd></div>
              <div><dt>{t('clm.jo.action')}</dt><dd>{t(`clm.evt.${detail.type}`)} <code>{detail.type}</code></dd></div>
              <div><dt>{t('clm.col.acteur')}</dt><dd>{detail.acteur}</dd></div>
              <div><dt>{t('clm.jo.origine')}</dt><dd>{detail.systeme ? t('clm.jo.origineSysteme') : t('clm.jo.origineMetier')}</dd></div>
              <div><dt>{t('clm.jo.horodatage')}</dt><dd>{dateHeure(detail.horodatage)}</dd></div>
              <div><dt>{t('clm.col.detail')}</dt><dd className="clm-casse">{detail.detail || '—'}</dd></div>
              <div><dt>{t('clm.col.empreinte')}</dt><dd><code className="clm-casse">{detail.empreinte}</code></dd></div>
            </dl>
          )}
        </Bloc>
        <Bloc zone="05" titre={t('clm.jo.blocIndicateur')}>
          <ul className="clm-liste">
            <li><span className="clm-statut clm-statut--info">{t('clm.jo.metier')}</span> {lignes.length - systeme}</li>
            <li><span className="clm-statut clm-statut--attente">{t('clm.jo.systeme')}</span> {systeme}</li>
          </ul>
          <p className="muted">{t('clm.jo.indicateurAide')}</p>
        </Bloc>
        <Bloc zone="04" titre={t('clm.jo.blocExport')} large>
          <p>{t('clm.jo.exportAide')}</p>
        </Bloc>
      </Blocs>
      <BarreActions>
        <button type="button" className="secondary" disabled={lignes.length === 0} onClick={() => setChoisi(cle(detail || lignes[0]))}>{t('clm.jo.ouvrirEvenement')}</button>
        <button type="button" onClick={exporter}>{t('clm.jo.exporter')}</button>
      </BarreActions>
    </EcranClm>
  )
}

export default function JournalClm() {
  return <GardeModule><Contenu /></GardeModule>
}
