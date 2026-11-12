import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { GardeModule, Message, Statut, dateHeure, useErreur, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

/**
 * Une ligne par dossier : « titre ou numéro de dossier Salesforce ; nom du signataire ; courriel ; téléphone ».
 * Le téléphone n'est requis que si le niveau de risque impose une vérification par SMS.
 */
export function lireElements(texte, colonne) {
  return texte.split('\n').map((l) => l.trim()).filter(Boolean).map((ligne) => {
    const [cle, nom, courriel, telephone] = ligne.split(';').map((x) => x.trim())
    const signataires = nom && courriel
      ? [{ nom, courriel, telephone: telephone || null, roleContrat: 'SIGNATAIRE_1', externe: true }] : []
    return colonne === 'SALESFORCE'
      ? { sfDossierId: cle, referenceExterne: cle, signataires }
      : { titre: cle, referenceExterne: cle, signataires }
  })
}

function NouveauLot({ onCree }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: modeles } = useRessource('/clm/modeles')
  const [f, setF] = useState({ libelle: '', modeleId: '', colonne: 'TITRE', lignes: '', lancer: true })
  const [erreur, setErreur] = useState(null)
  const [enCours, setEnCours] = useState(false)
  const publies = (modeles || []).filter((m) => m.statut === 'PUBLIE')
  const elements = lireElements(f.lignes, f.colonne)
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })

  async function envoyer(e) {
    e.preventDefault()
    setErreur(null)
    setEnCours(true)
    try {
      const { data } = await api.post('/clm/lots', { libelle: f.libelle, modeleId: Number(f.modeleId), lancer: f.lancer, elements })
      setF({ ...f, libelle: '', lignes: '' })
      onCree(data)
    } catch (x) { setErreur(versErreur(x)) } finally { setEnCours(false) }
  }

  return (
    <form className="form" onSubmit={envoyer} aria-label={t('clm.lot.nouveau')}>
      <Message erreur={erreur} />
      <div className="form-grid">
        <label>{t('clm.lot.libelle')}<input value={f.libelle} onChange={maj('libelle')} maxLength={200} /></label>
        <label>{t('clm.cr.modele')} <span aria-hidden="true">*</span>
          <select value={f.modeleId} onChange={maj('modeleId')} required>
            <option value="">{t('clm.lot.choisirModele')}</option>
            {publies.map((m) => <option key={m.id} value={m.id}>{m.nom} (v{m.version})</option>)}
          </select>
        </label>
        <label>{t('clm.lot.colonne')}
          <select value={f.colonne} onChange={maj('colonne')}>
            <option value="TITRE">{t('clm.lot.colonne.TITRE')}</option>
            <option value="SALESFORCE">{t('clm.lot.colonne.SALESFORCE')}</option>
          </select>
        </label>
        <label className="col-2">{t('clm.lot.lignes')} <span className="muted">{t('clm.lot.lignesAide')}</span>
          <textarea rows={6} value={f.lignes} onChange={maj('lignes')} required /></label>
        <label className="check-row"><input type="checkbox" checked={f.lancer} onChange={maj('lancer')} />{t('clm.lot.lancer')}</label>
      </div>
      <p className="muted" role="status">{t('clm.lot.compte', { n: elements.length })}</p>
      <div className="cmp-row"><button type="submit" disabled={enCours || elements.length === 0} aria-busy={enCours}>{t('clm.lot.creer')}</button></div>
    </form>
  )
}

function DetailLot({ lot }) {
  const { t } = useI18n()
  return (
    <div className="table-scroll">
      <table className="table clm-table">
        <caption>{t('clm.lot.elementsDe', { libelle: lot.libelle })}</caption>
        <thead><tr><th scope="col">#</th><th scope="col">{t('clm.lot.reference')}</th><th scope="col">{t('clm.col.statut')}</th>
          <th scope="col">{t('clm.lot.trousse')}</th><th scope="col">{t('clm.col.erreur')}</th></tr></thead>
        <tbody>
          {lot.elements.map((e) => (
            <tr key={e.rang}>
              <td data-label="#">{e.rang}</td>
              <td data-label={t('clm.lot.reference')}>{e.referenceExterne || '—'}</td>
              <td data-label={t('clm.col.statut')}><Statut valeur={e.statut} /></td>
              <td data-label={t('clm.lot.trousse')}>{e.trousseId ? <Link to={`/clm/trousses/${e.trousseId}`}>{e.reference}</Link> : '—'}</td>
              <td data-label={t('clm.col.erreur')}>{e.erreur || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/** Signature en lot (addenda 1, AF-47) : un modèle, un dossier par ligne, une trousse par dossier, chaque échec isolé. */
function EcranLots() {
  const { t } = useI18n()
  const { donnees: lots, recharger } = useRessource('/clm/lots')
  const [ouvert, setOuvert] = useState(null)
  const [info, setInfo] = useState('')
  const cree = (lot) => {
    setInfo(t('clm.lot.cree', { libelle: lot.libelle, r: lot.reussis, e: lot.echecs }))
    setOuvert(lot.id)
    recharger()
  }
  const detail = (lots || []).find((l) => l.id === ouvert)
  return (
    <EcranClm titre={t('clm.lot.titre')} objectif={t('clm.lot.objectif')}>
      <Message info={info} />
      <Blocs>
        <Bloc titre={t('clm.lot.nouveau')} large><NouveauLot onCree={cree} /></Bloc>
        <Bloc titre={t('clm.lot.recents')} large>
          {!lots ? <p role="status">{t('clm.chargement')}</p> : lots.length === 0 ? <p className="muted">{t('clm.lot.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.lot.recents')}</caption>
                <thead><tr><th scope="col">{t('clm.lot.libelle')}</th><th scope="col">{t('clm.lot.origine')}</th><th scope="col">{t('clm.col.statut')}</th>
                  <th scope="col">{t('clm.lot.resultat')}</th><th scope="col">{t('clm.col.date')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {lots.map((l) => (
                    <tr key={l.id}>
                      <td data-label={t('clm.lot.libelle')}>{l.libelle}</td>
                      <td data-label={t('clm.lot.origine')}>{l.origine}</td>
                      <td data-label={t('clm.col.statut')}><Statut valeur={l.statut} /></td>
                      <td data-label={t('clm.lot.resultat')}>{t('clm.lot.resultatValeur', { r: l.reussis, t: l.total, e: l.echecs })}</td>
                      <td data-label={t('clm.col.date')}>{dateHeure(l.creeLe)}</td>
                      <td data-label={t('clm.col.actions')}>
                        <button type="button" className="lien" aria-expanded={ouvert === l.id} aria-label={`${t('clm.lot.voir')} — ${l.libelle}`}
                          onClick={() => setOuvert(ouvert === l.id ? null : l.id)}>{t('clm.lot.voir')}</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {detail && <DetailLot lot={detail} />}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function Lots() {
  return <GardeModule><EcranLots /></GardeModule>
}
