import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, GardeModule, Message, Statut, dateHeure, telecharger, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

/** Lignes par page (SuiviService.TAILLE_DEFAUT) : le serveur pagine, l'écran ne charge jamais toute l'organisation. */
const TAILLE = 50

const STATUTS = ['BROUILLON', 'EN_TRAITEMENT', 'TERMINEE', 'REJETEE', 'ANNULEE']
const EXCEPTIONS = ['BLOCAGE', 'ERREUR_SALESFORCE', 'SIGNATURE_EXPIREE']
const VIDE = { q: '', statut: '', unite: '', responsable: '', source: '', du: '', au: '', exceptions: false }

/** UI-18 — Suivi centralisé : filtres, étapes et attentes, vue des exceptions, actions et export selon droits. */
function Contenu() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const versErreur = useErreur()
  const [form, setForm] = useState(VIDE)
  const [filtre, setFiltre] = useState(VIDE)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const [page, setPage] = useState(0)
  const qs = new URLSearchParams(Object.entries(filtre).filter(([, v]) => v !== '' && v !== false)).toString()
  const { donnees, erreur: errChargement, chargement, recharger } = useRessource(`/clm/suivi?${qs}${qs ? '&' : ''}page=${page}&taille=${TAILLE}`)
  const filtrer = (f) => { setPage(0); setFiltre(f) }
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })
  const admin = hasRole('ADMIN') || hasRole('SUPERADMIN')
  const unites = useListe('CLM_UNITE')
  const lignes = donnees?.lignes || []
  const resultats = donnees?.total != null ? t('clm.su.resultatsTotal', { n: lignes.length, total: donnees.total, page: page + 1 })
    : t('clm.su.resultatsPage', { n: lignes.length, page: page + 1 })
  const exporter = () => telecharger(`/clm/suivi/export?${qs}`, 'suivi-clm.csv').catch((e) => setErreur(e.response?.status === 403 ? t('clm.tr.exportRefuse') : versErreur(e)))

  async function relancerIntegration(l) {
    setErreur(null)
    try {
      const { data } = await api.post(`/clm/admin/integrations/salesforce/trousses/${l.id}/relancer`)
      setInfo(t('clm.su.integrationRelancee', { n: data.relances, ref: l.reference }))
      recharger()
    } catch (e) { setErreur(versErreur(e)) }
  }

  return (
    <EcranClm maquette="UI-18" titre={t('clm.su.titre')} objectif={t('clm.su.objectif')}
      actions={<button type="button" className="secondary" onClick={exporter}>{t('clm.exporter')}</button>}>
      <Blocs>
        <Bloc zone="02" titre={t('clm.su.blocFiltres')} large>
          <form className="form-grid form-grid--4" aria-label={t('clm.su.blocFiltres')} onSubmit={(e) => { e.preventDefault(); filtrer(form) }}>
            <label>{t('clm.rechercher')}<input type="search" value={form.q} onChange={maj('q')} /></label>
            <label>{t('clm.col.statut')}<select value={form.statut} onChange={maj('statut')}><option value="">{t('clm.tr.tousStatuts')}</option>{STATUTS.map((s) => <option key={s} value={s}>{t(`clm.statut.${s}`)}</option>)}</select></label>
            <label>{t('clm.champ.unite')}<ChoixListe liste={unites} value={form.unite} vide={t('clm.liste.toutes')}
              onChange={(v) => setForm({ ...form, unite: v })} /></label>
            <label>{t('clm.col.responsable')}<input value={form.responsable} onChange={maj('responsable')} /></label>
            <label>{t('clm.col.source')}<select value={form.source} onChange={maj('source')}><option value="">{t('clm.su.toutesSources')}</option>{SOURCES.map((x) => <option key={x} value={x}>{t(`clm.source.${x}`)}</option>)}</select></label>
            <label>{t('clm.su.du')}<input type="date" value={form.du} onChange={maj('du')} /></label>
            <label>{t('clm.su.au')}<input type="date" value={form.au} onChange={maj('au')} /></label>
            <label className="check-row"><input type="checkbox" checked={form.exceptions} onChange={maj('exceptions')} />{t('clm.su.exceptions')}</label>
            <div className="col-2 cmp-row"><button type="submit">{t('clm.rechercher')}</button><button type="button" className="secondary" onClick={() => { setForm(VIDE); filtrer(VIDE) }}>{t('clm.reinitialiser')}</button></div>
          </form>
        </Bloc>
      </Blocs>
      <Message erreur={erreur || errChargement} info={chargement ? t('clm.chargement') : info || resultats} onReessayer={recharger} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.su.blocTable')} large>
          {donnees && lignes.length === 0 ? <p className="muted">{t('clm.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table" data-zone="03">
                <caption className="sr-only">{t('clm.su.titre')}</caption>
                <thead><tr><th scope="col">{t('clm.col.reference')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.etapes')}</th>
                  <th scope="col">{t('clm.col.attente')}</th><th scope="col">{t('clm.col.echeance')}</th><th scope="col">{t('clm.col.dernierEvenement')}</th>
                  <th scope="col">{t('clm.col.exceptions')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {lignes.map((l) => (
                    <tr key={l.id}>
                      <td data-label={t('clm.col.reference')}>{l.reference}<br /><span className="muted">{l.titre} · {unites.libelle(l.unite) || '—'} · {l.proprietaire} · {t(`clm.source.${l.source}`)}</span></td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={l.statut} /></td>
                      <td data-label={t('clm.col.etapes')}>{l.etapesActives.join(', ') || '—'}</td>
                      <td data-label={t('clm.col.attente')}>{l.enAttenteDe.join(', ') || '—'}</td>
                      <td data-label={t('clm.col.echeance')}>{dateHeure(l.echeance)}</td>
                      <td data-label={t('clm.col.dernierEvenement')}>{l.dernierEvenement ? `${t(`clm.evt.${l.dernierEvenement}`)} · ${dateHeure(l.dernierEvenementLe)}` : '—'}</td>
                      <td data-label={t('clm.col.exceptions')}>{l.exceptions.length === 0 ? '—' : l.exceptions.map((x) => <span key={x} className="clm-statut clm-statut--ko"><span aria-hidden="true">⚠</span> {t(`clm.exception.${x}`)}</span>)}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        <Link to={`/clm/trousses/${l.id}`} aria-label={`${t('clm.su.ouvrir')} — ${l.reference}`}>{t('clm.su.ouvrir')}</Link>
                        {l.statut === 'EN_TRAITEMENT' && <Link to={`/clm/trousses/${l.id}?onglet=parcours`} aria-label={`${t('clm.su.reassigner')} — ${l.reference}`}>{t('clm.su.reassigner')}</Link>}
                        {admin && l.exceptions.includes('ERREUR_SALESFORCE') && <button type="button" className="lien" aria-label={`${t('clm.su.relancerIntegration')} — ${l.reference}`} onClick={() => relancerIntegration(l)}>{t('clm.su.relancerIntegration')}</button>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {(page > 0 || donnees?.suivant) && (
            <nav className="cmp-row" aria-label={t('clm.su.pagination')}>
              <button type="button" className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>{t('clm.su.precedente')}</button>
              <span aria-live="polite">{t('clm.su.page', { n: page + 1 })}</span>
              <button type="button" className="secondary" disabled={!donnees?.suivant} onClick={() => setPage(page + 1)}>{t('clm.su.suivante')}</button>
            </nav>
          )}
        </Bloc>
        <Bloc zone="04" titre={t('clm.su.blocExceptions')}>
          <ul className="clm-liste">
            {EXCEPTIONS.map((x) => <li key={x}><span aria-hidden="true">⚠</span> {t(`clm.exception.${x}`)} {t('clm.su.surLaPage')} : <strong>{lignes.filter((l) => l.exceptions.includes(x)).length}</strong></li>)}
          </ul>
          <button type="button" className="secondary" onClick={() => { const f = { ...form, exceptions: true }; setForm(f); filtrer(f) }}>{t('clm.su.voirExceptions')}</button>
        </Bloc>
        <Bloc zone="05" titre={t('clm.su.blocExport')}>
          <p>{t('clm.su.exportAide')}</p>
          <button type="button" className="secondary" onClick={exporter}>{t('clm.exporter')}</button>
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

/** Origines d'une trousse : écran, Salesforce, Workday ou autre système par l'API d'intégration (addenda 1). */
const SOURCES = ['MANUELLE', 'SALESFORCE', 'WORKDAY', 'INTEGRATION']

export default function SuiviClm() {
  return <GardeModule><Contenu /></GardeModule>
}
