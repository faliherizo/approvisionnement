import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { useAuth } from '../../auth.jsx'
import { GardeModule, Message, Statut, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const STATUTS = ['BROUILLON', 'EN_REVISION', 'PUBLIE', 'RETIRE']
const groupesDe = (m) => (m.groupes || '').split(',').map((g) => g.trim()).filter(Boolean)

/** UI-07 — Catalogue des modèles : recherche et filtres (type, état, groupe, version), cycle de vie, usage par les trousses. */
function Contenu() {
  const { t } = useI18n()
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const versErreur = useErreur()
  const [filtre, setFiltre] = useState({ q: '', statut: '', categorie: '', groupe: '', version: '' })
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const { donnees, erreur: errChargement, recharger } = useRessource('/clm/modeles')
  const admin = hasRole('ADMIN_AFFAIRES') || hasRole('SUPERADMIN')
  const tous = donnees || []
  const types = useListe('CLM_TYPE_CONTRAT')
  // Catégories de la liste de l'organisation, plus celles encore portées par un modèle après leur retrait de la liste.
  const categories = [...new Set([...types.valeurs.map((v) => v.code), ...tous.map((m) => m.categorie).filter(Boolean)])]
  const groupes = [...new Set(tous.flatMap(groupesDe))].sort()
  const derniere = (m) => !tous.some((x) => x.cleModele === m.cleModele && x.numero > m.numero)
  const maj = (k) => (e) => setFiltre({ ...filtre, [k]: e.target.value })
  const lignes = tous.filter((m) => (!filtre.q || `${m.nom} ${m.categorie || ''} ${m.groupes || ''}`.toLowerCase().includes(filtre.q.toLowerCase()))
    && (!filtre.statut || m.statut === filtre.statut) && (!filtre.categorie || m.categorie === filtre.categorie)
    && (!filtre.groupe || groupesDe(m).includes(filtre.groupe)) && (filtre.version !== 'derniere' || derniere(m)))
  const utilises = tous.filter((m) => m.trousses > 0)

  async function creer() {
    setErreur(null)
    try {
      const { data } = await api.post('/clm/modeles', { nom: t('clm.mo.nouveauNom'), corps: '', champs: [], clauses: [], parcours: [] })
      navigate(`/clm/modeles/${data.id}`)
    } catch (e) { setErreur(versErreur(e)) }
  }
  async function transition(m, action) {
    setErreur(null)
    try { await api.post(`/clm/modeles/${m.id}/${action}`); setInfo(t(`clm.mo.ok.${action}`, { nom: m.nom })); recharger() } catch (e) { setErreur(versErreur(e)) }
  }

  return (
    <EcranClm maquette="UI-07" titre={t('clm.mo.titre')} objectif={t('clm.mo.objectif')}
      actions={<button type="button" onClick={creer}>{t('clm.mo.creer')}</button>}>
      <Message erreur={erreur || errChargement} info={info} onReessayer={errChargement ? recharger : undefined} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.mo.blocFiltres')} large>
          <div className="form-grid form-grid--4">
            <label className="col-2">{t('clm.rechercher')}<input type="search" value={filtre.q} onChange={maj('q')} /></label>
            <label>{t('clm.col.categorie')}
              <select value={filtre.categorie} onChange={maj('categorie')}>
                <option value="">{t('clm.mo.toutes')}</option>
                {categories.map((c) => <option key={c} value={c}>{types.libelle(c)}</option>)}
              </select>
            </label>
            <label>{t('clm.col.statut')}
              <select value={filtre.statut} onChange={maj('statut')}>
                <option value="">{t('clm.tr.tousStatuts')}</option>
                {STATUTS.map((s) => <option key={s} value={s}>{t(`clm.statut.${s}`)}</option>)}
              </select>
            </label>
            <label>{t('clm.mo.groupe')}
              <select value={filtre.groupe} onChange={maj('groupe')}>
                <option value="">{t('clm.mo.tousGroupes')}</option>
                {groupes.map((g) => <option key={g} value={g}>{g}</option>)}
              </select>
            </label>
            <label>{t('clm.col.version')}
              <select value={filtre.version} onChange={maj('version')}>
                <option value="">{t('clm.mo.toutesVersions')}</option>
                <option value="derniere">{t('clm.mo.derniereVersion')}</option>
              </select>
            </label>
          </div>
        </Bloc>
        <Bloc zone="02" titre={t('clm.mo.blocTable')} large>
          {donnees && lignes.length === 0 ? <p className="muted">{t('clm.mo.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.mo.titre')}</caption>
                <thead><tr><th scope="col">{t('clm.col.nom')}</th><th scope="col">{t('clm.col.categorie')}</th><th scope="col">{t('clm.col.proprietaire')}</th>
                  <th scope="col">{t('clm.mo.versionPubliee')}</th><th scope="col">{t('clm.col.statut')}</th><th scope="col">{t('clm.col.maj')}</th>
                  <th scope="col">{t('clm.mo.trousses')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {lignes.map((m) => (
                    <tr key={m.id}>
                      <td data-label={t('clm.col.nom')}><Link to={`/clm/modeles/${m.id}`}>{m.nom}</Link>{m.groupes ? <><br /><span className="muted">{t('clm.mo.partage', { g: m.groupes })}</span></> : null}</td>
                      <td data-label={t('clm.col.categorie')}>{m.categorie ? types.libelle(m.categorie) : '—'}</td>
                      <td data-label={t('clm.col.proprietaire')}>{m.proprietaire}</td>
                      <td data-label={t('clm.mo.versionPubliee')}>v{m.numero}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={m.statut} /></td>
                      <td data-label={t('clm.col.maj')}>{dateHeure(m.majLe)}</td>
                      <td data-label={t('clm.mo.trousses')}>{m.trousses}</td>
                      <td data-label={t('clm.col.actions')} className="clm-actions-ligne">
                        {m.statut === 'BROUILLON' && <Link to={`/clm/modeles/${m.id}`} aria-label={`${t('clm.mo.modifierBrouillon')} — ${m.nom}`}>{t('clm.mo.modifierBrouillon')}</Link>}
                        {m.statut === 'BROUILLON' && <button type="button" className="lien" onClick={() => transition(m, 'soumettre')}>{t('clm.mo.soumettre')}</button>}
                        {admin && m.statut === 'EN_REVISION' && <button type="button" className="lien" onClick={() => transition(m, 'publier')}>{t('clm.mo.publier')}</button>}
                        {admin && m.statut === 'EN_REVISION' && <button type="button" className="lien" onClick={() => transition(m, 'renvoyer')}>{t('clm.mo.renvoyer')}</button>}
                        {admin && m.statut === 'PUBLIE' && <button type="button" className="lien danger" onClick={() => transition(m, 'retirer')}>{t('clm.mo.retirer')}</button>}
                        {['PUBLIE', 'RETIRE'].includes(m.statut) && <button type="button" className="lien" onClick={() => transition(m, 'nouvelle-version')}>{t('clm.mo.nouvelleVersion')}</button>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
        <Bloc zone="03" titre={t('clm.mo.blocEtats')}>
          <ul className="clm-liste">
            {STATUTS.map((s) => <li key={s}><Statut valeur={s} /> {t(`clm.mo.etat.${s}`)} — <strong>{tous.filter((m) => m.statut === s).length}</strong></li>)}
          </ul>
        </Bloc>
        <Bloc zone="04" titre={t('clm.mo.blocUsage')}>
          {utilises.length === 0 ? <p className="muted">{t('clm.mo.aucunUsage')}</p> : (
            <ul className="clm-liste">
              {utilises.map((m) => <li key={m.id}><Link to={`/clm/modeles/${m.id}`}>{m.nom}</Link> v{m.numero} : {t('clm.mo.utilisePar', { n: m.trousses })}</li>)}
            </ul>
          )}
          <p className="muted">{t('clm.mo.usageAide')}</p>
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function Modeles() {
  return <GardeModule><Contenu /></GardeModule>
}
