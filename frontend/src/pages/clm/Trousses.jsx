import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../auth.jsx'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, GardeModule, Message, Statut, date, dateHeure, telecharger, useErreur, useListe, usePeutInitier, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const STATUTS = ['BROUILLON', 'EN_TRAITEMENT', 'TERMINEE', 'REJETEE', 'ANNULEE']
const PAR_PAGE = 20
const VIDE = { q: '', statut: '', initiateur: '', type: '', du: '', au: '' }

/** UI-02 — Liste des trousses : recherche, filtres (statut, initiateur, période, type), tri, pagination, export. */
function Contenu() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [params] = useSearchParams()
  const qUrl = params.get('q') || ''
  const [form, setForm] = useState({ ...VIDE, q: qUrl })
  const [filtre, setFiltre] = useState({ ...VIDE, q: qUrl })
  const [tri, setTri] = useState({ col: 'majLe', asc: false })
  const [page, setPage] = useState(0)
  const [erreur, setErreur] = useState(null)
  const qs = new URLSearchParams(Object.entries(filtre).filter(([, v]) => v)).toString()
  // Pagination côté serveur (RST-40) : la liste ne charge jamais toutes les trousses de l'organisation.
  const { donnees, erreur: errChargement, chargement, recharger } = useRessource(`/clm/trousses?${qs ? `${qs}&` : ''}page=${page}&taille=${PAR_PAGE}`)
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  const types = useListe('CLM_TYPE_CONTRAT')
  const peutInitier = usePeutInitier()
  const { user, hasRole } = useAuth()
  // Même règle que le serveur : l'initiateur et l'administration gèrent une trousse, l'auditeur la consulte.
  const gere = (tr) => hasRole('ADMIN_AFFAIRES') || hasRole('SUPERADMIN') || tr.proprietaire?.toLowerCase() === user?.email?.toLowerCase()

  useEffect(() => {
    setForm((f) => ({ ...f, q: qUrl }))
    setFiltre((f) => ({ ...f, q: qUrl }))
    setPage(0)
  }, [qUrl])

  const lignes = [...(donnees?.lignes || [])].sort((a, b) => {
    const va = a[tri.col] ?? ''
    const vb = b[tri.col] ?? ''
    return (va < vb ? -1 : va > vb ? 1 : 0) * (tri.asc ? 1 : -1)
  })
  const total = donnees?.total
  const pages = total != null ? Math.max(1, Math.ceil(total / PAR_PAGE)) : null
  const visibles = lignes
  const trier = (col) => setTri((x) => ({ col, asc: x.col === col ? !x.asc : true }))
  const colonnesTriables = { reference: t('clm.col.reference'), titre: t('clm.col.objet'), statut: t('clm.col.statut'),
    echeance: t('clm.col.echeance'), majLe: t('clm.col.maj') }
  const ariaTri = (col) => (tri.col === col ? (tri.asc ? 'ascending' : 'descending') : 'none')
  const entete = (col, libelle) => (
    <th scope="col" aria-sort={ariaTri(col)}>
      <button type="button" className="lien" onClick={() => trier(col)}>{libelle}</button>
    </th>
  )
  const appliquer = (e) => { e.preventDefault(); setPage(0); setFiltre(form) }
  async function exporter() {
    setErreur(null)
    const export_ = new URLSearchParams(Object.entries({ q: filtre.q, statut: filtre.statut }).filter(([, v]) => v)).toString()
    try { await telecharger(`/clm/suivi/export?${export_}`, 'trousses.csv') } catch (e) {
      setErreur(e.response?.status === 403 ? t('clm.tr.exportRefuse') : versErreur(e))
    }
  }

  return (
    <EcranClm maquette="UI-02" titre={t('clm.tr.titre')} objectif={t('clm.tr.objectif')} zoneActions="05"
      actions={(
        <>
          {peutInitier && <Link className="clm-bouton" to="/clm/trousses/nouvelle">{t('clm.tb.creer')}</Link>}
          <button type="button" className="secondary" onClick={exporter}>{t('clm.tr.exporter')}</button>
        </>
      )}>
      <form onSubmit={appliquer} aria-label={t('clm.tr.formulaire')}>
        <Blocs>
          <Bloc zone="01" titre={t('clm.tr.blocRecherche')}>
            <div className="form">
              <label>{t('clm.tr.recherche')}<input type="search" value={form.q} onChange={maj('q')} /></label>
              <div className="cmp-row">
                <button type="submit">{t('clm.rechercher')}</button>
                <button type="button" className="secondary" onClick={() => { setForm(VIDE); setFiltre(VIDE); setPage(0) }}>{t('clm.reinitialiser')}</button>
              </div>
            </div>
          </Bloc>
          <Bloc zone="02" titre={t('clm.tr.blocFiltres')}>
            <div className="form-grid">
              <label>{t('clm.col.statut')}
                <select value={form.statut} onChange={maj('statut')}>
                  <option value="">{t('clm.tr.tousStatuts')}</option>
                  {STATUTS.map((s) => <option key={s} value={s}>{t(`clm.statut.${s}`)}</option>)}
                </select>
              </label>
              <label>{t('clm.tr.initiateur')}<input value={form.initiateur} onChange={maj('initiateur')} /></label>
              <label>{t('clm.tr.creeDu')}<input type="date" value={form.du} onChange={maj('du')} /></label>
              <label>{t('clm.su.au')}<input type="date" value={form.au} onChange={maj('au')} /></label>
              <label className="col-2">{t('clm.champ.typeContrat')}<ChoixListe liste={types} value={form.type} vide={t('clm.liste.tous')}
                onChange={(v) => setForm({ ...form, type: v })} /></label>
            </div>
          </Bloc>
        </Blocs>
      </form>
      <Message erreur={erreur || errChargement} info={chargement ? t('clm.chargement') : t('clm.tr.resultats', { n: total ?? lignes.length })} onReessayer={recharger} />
      <Blocs>
        <Bloc zone="03" titre={t('clm.tr.blocTable')} large>
          {donnees && lignes.length === 0 ? <p className="muted">{t('clm.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table clm-table--triable">
                <caption className="sr-only">{t('clm.tr.titre')}</caption>
                <thead><tr>
                  {entete('reference', t('clm.col.reference'))}
                  {entete('titre', t('clm.col.objet'))}
                  <th scope="col">{t('clm.col.source')}</th>
                  {entete('statut', t('clm.col.statut'))}
                  <th scope="col">{t('clm.col.prochaineAction')}</th>
                  {entete('echeance', t('clm.col.echeance'))}
                  {entete('majLe', t('clm.col.maj'))}
                  <th scope="col">{t('clm.col.actions')}</th>
                </tr></thead>
                <tbody>
                  {visibles.map((tr) => (
                    <tr key={tr.id}>
                      <td data-label={t('clm.col.reference')}>{tr.reference}</td>
                      <td data-label={t('clm.col.objet')}>{tr.titre}<br /><span className="muted">{tr.proprietaire}{tr.typeContrat ? ` · ${types.libelle(tr.typeContrat)}` : ''}</span></td>
                      <td data-label={t('clm.col.source')}>{t(`clm.source.${tr.source}`)}{tr.sfDossierId ? ` · ${tr.sfDossierId}` : ''}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={tr.statut} /></td>
                      <td data-label={t('clm.col.prochaineAction')}>{tr.prochaineAction}{tr.enRetard && <> <Statut valeur="EXPIREE" /></>}</td>
                      <td data-label={t('clm.col.echeance')}>{date(tr.echeance)}</td>
                      <td data-label={t('clm.col.maj')}>{dateHeure(tr.majLe)}</td>
                      <td data-label={t('clm.col.actions')}>
                        <Link to={`/clm/trousses/${tr.id}`} aria-label={t('clm.tr.ouvrirDe', { ref: tr.reference })}>{t('clm.tr.ouvrir')}</Link>
                        {tr.statut === 'BROUILLON' && gere(tr) && <> · <Link to={`/clm/trousses/nouvelle?id=${tr.id}`} aria-label={t('clm.tr.modifierDe', { ref: tr.reference })}>{t('clm.dt.modifierBrouillon')}</Link></>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <nav className="pagination cmp-row" aria-label={t('clm.pagination')} data-zone="04">
            <span>{t('clm.tr.triePar', { col: colonnesTriables[tri.col], sens: t(tri.asc ? 'clm.tri.croissant' : 'clm.tri.decroissant') })}</span>
            <button type="button" className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>{t('clm.precedent')}</button>
            <span aria-live="polite">{pages ? t('clm.pageSur', { p: page + 1, n: pages }) : t('clm.su.page', { n: page + 1 })}</span>
            <button type="button" className="secondary" disabled={!donnees?.suivant} onClick={() => setPage(page + 1)}>{t('clm.suivant')}</button>
          </nav>
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function Trousses() {
  return <GardeModule><Contenu /></GardeModule>
}
