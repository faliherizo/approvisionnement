import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, GardeModule, Message, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'

const VIDE = { typeContrat: '', sensibiliteId: '', retention: '' }

/**
 * Étiquettes Microsoft Purview (Annexe 10, SEC-27 ; ADR-036) : l'administration choisit, dans le catalogue du locataire
 * Microsoft 365, l'étiquette de sensibilité et l'étiquette de rétention apposées aux documents, par défaut et par type de
 * contrat. Les étiquettes sont conservées avec chaque document et propagées au dépôt SharePoint ou OneDrive.
 */
function Purview() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const types = useListe('CLM_TYPE_CONTRAT')
  const { donnees: catalogue } = useRessource('/clm/admin/purview/catalogue')
  const { donnees: politiques, recharger } = useRessource('/clm/admin/purview/politiques')
  const [f, setF] = useState(VIDE)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  const sensibilites = (catalogue || []).filter((e) => e.type === 'SENSIBILITE')
  const retentions = (catalogue || []).filter((e) => e.type === 'RETENTION')
  const libelleType = (c) => (c ? types.libelle(c) : t('clm.purview.defaut'))
  const duree = (j) => (j == null ? '' : ` (${t('clm.purview.jours', { n: j })})`)

  async function faire(fn, ok) {
    setErreur(null)
    try { await fn(); setInfo(ok); recharger(); return true } catch (x) { setErreur(versErreur(x)); return false }
  }
  const definir = async (e) => {
    e.preventDefault()
    if (await faire(() => api.put('/clm/admin/purview/politiques', f), t('clm.purview.enregistree', { type: libelleType(f.typeContrat) }))) setF(VIDE)
  }
  const retirer = (p) => faire(() => api.delete(`/clm/admin/purview/politiques/${p.id}`), t('clm.purview.retiree', { type: libelleType(p.typeContrat) }))

  return (
    <EcranClm titre={t('clm.purview.titre')} objectif={t('clm.purview.objectif')}>
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.purview.blocPolitiques')} large>
          <p className="muted">{t('clm.purview.aide')}</p>
          {!politiques ? <p role="status">{t('clm.chargement')}</p> : politiques.length === 0 ? <p className="muted">{t('clm.purview.aucune')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.purview.blocPolitiques')}</caption>
                <thead><tr><th scope="col">{t('clm.champ.typeContrat')}</th><th scope="col">{t('clm.purview.sensibilite')}</th>
                  <th scope="col">{t('clm.purview.retention')}</th><th scope="col">{t('clm.co.modifie')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
                <tbody>
                  {politiques.map((p) => (
                    <tr key={p.id}>
                      <td data-label={t('clm.champ.typeContrat')}>{libelleType(p.typeContrat)}</td>
                      <td data-label={t('clm.purview.sensibilite')}>{p.sensibilite || '—'}</td>
                      <td data-label={t('clm.purview.retention')}>{p.retention ? `${p.retention}${duree(p.retentionJours)}` : '—'}</td>
                      <td data-label={t('clm.co.modifie')}>{p.modifiePar} · {dateHeure(p.modifieLe)}</td>
                      <td data-label={t('clm.col.actions')}>
                        <button type="button" className="lien danger" aria-label={`${t('clm.retirer')} — ${libelleType(p.typeContrat)}`}
                          onClick={() => retirer(p)}>{t('clm.retirer')}</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <form className="form clm-sousform" onSubmit={definir} aria-label={t('clm.purview.definir')}>
            <div className="form-grid">
              <label>{t('clm.champ.typeContrat')}
                <ChoixListe liste={types} value={f.typeContrat} onChange={(v) => setF({ ...f, typeContrat: v })} vide={t('clm.purview.defaut')} /></label>
              <label>{t('clm.purview.sensibilite')}
                <select value={f.sensibiliteId} onChange={(e) => setF({ ...f, sensibiliteId: e.target.value })}>
                  <option value="">{t('clm.purview.aucuneEtiquette')}</option>
                  {sensibilites.map((s) => <option key={s.id} value={s.id}>{s.nom}</option>)}
                </select>
              </label>
              <label>{t('clm.purview.retention')}
                <select value={f.retention} onChange={(e) => setF({ ...f, retention: e.target.value })}>
                  <option value="">{t('clm.purview.aucuneEtiquette')}</option>
                  {retentions.map((r) => <option key={r.id} value={r.id}>{r.nom}{duree(r.jours)}</option>)}
                </select>
              </label>
            </div>
            <p className="muted">{t('clm.purview.conservationAide')}</p>
            <div className="cmp-row"><button type="submit">{t('clm.purview.definir')}</button></div>
          </form>
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function PurviewClm() {
  return <GardeModule><Purview /></GardeModule>
}
