import { useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { Message, dateHeure, useErreur, useRessource } from '../pages/clm/commun.jsx'
import { Bloc, Blocs } from '../pages/clm/gabarit.jsx'

const VIDE = { source: 'GROUPE', attribut: '', valeur: '', role: '', groupe: '' }
const ROLES = ['', 'LECTEUR', 'REDACTEUR', 'JURIDIQUE', 'APPRO', 'DIRECTION', 'AUDITEUR', 'ADMIN']

/**
 * Annuaire d'entreprise (addenda 3, Q43, Q44, Q51) : correspondances des groupes et attributs de Microsoft Entra ID ou de
 * Saviynt vers les rôles et les domaines d'affaires, et jeton du point SCIM 2.0 de l'organisation.
 */
export default function AnnuaireManager() {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees: correspondances, recharger } = useRessource('/admin/annuaire/correspondances')
  const { donnees: scim, recharger: rechargerScim } = useRessource('/admin/annuaire/scim')
  const [f, setF] = useState(VIDE)
  const [jeton, setJeton] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const maj = (k) => (e) => setF({ ...f, [k]: e.target.value })

  async function faire(fn, ok) {
    setErreur(null)
    try { const r = await fn(); setInfo(ok); recharger(); rechargerScim(); return r } catch (x) { setErreur(versErreur(x)); return null }
  }
  const ajouter = async (e) => {
    e.preventDefault()
    if (await faire(() => api.post('/admin/annuaire/correspondances', f), t('an.ajoutee'))) setF(VIDE)
  }
  const genererJeton = async () => {
    if (scim?.configure && !window.confirm(t('an.confirmerJeton'))) return
    const r = await faire(() => api.post('/admin/annuaire/scim/jeton'), t('an.jetonCree'))
    if (r) setJeton(r.data.jeton)
  }
  const point = `${window.location.origin}${scim?.point || '/scim/v2'}`

  return (
    <Blocs>
      <Bloc titre={t('an.titre')} large>
        <Message erreur={erreur} info={info} />
        <p className="muted">{t('an.aide')}</p>
        {correspondances && correspondances.length > 0 ? (
          <div className="table-scroll">
            <table className="table clm-table">
              <caption className="sr-only">{t('an.titre')}</caption>
              <thead><tr><th scope="col">{t('an.source')}</th><th scope="col">{t('an.valeur')}</th><th scope="col">{t('an.role')}</th>
                <th scope="col">{t('an.domaine')}</th><th scope="col">{t('an.modifie')}</th><th scope="col">{t('clm.col.actions')}</th></tr></thead>
              <tbody>
                {correspondances.map((c) => (
                  <tr key={c.id}>
                    <td data-label={t('an.source')}>{t(`an.source.${c.source}`)}{c.attribut ? ` · ${c.attribut}` : ''}</td>
                    <td data-label={t('an.valeur')}><code>{c.valeur}</code></td>
                    <td data-label={t('an.role')}>{c.role || '—'}</td>
                    <td data-label={t('an.domaine')}>{c.groupe || '—'}</td>
                    <td data-label={t('an.modifie')}>{c.modifiePar} · {dateHeure(c.modifieLe)}</td>
                    <td data-label={t('clm.col.actions')}>
                      <button type="button" className="lien danger" aria-label={`${t('clm.retirer')} — ${c.valeur}`}
                        onClick={() => faire(() => api.delete(`/admin/annuaire/correspondances/${c.id}`), t('an.retiree'))}>{t('clm.retirer')}</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : <p className="muted">{t('an.aucune')}</p>}
        <form className="form clm-sousform" onSubmit={ajouter} aria-label={t('an.ajouter')}>
          <div className="form-grid">
            <label>{t('an.source')}
              <select value={f.source} onChange={maj('source')}>
                {['GROUPE', 'ATTRIBUT'].map((s) => <option key={s} value={s}>{t(`an.source.${s}`)}</option>)}
              </select>
            </label>
            {f.source === 'ATTRIBUT' && <label>{t('an.attribut')}<input value={f.attribut} onChange={maj('attribut')} placeholder="department" required /></label>}
            <label>{t(f.source === 'GROUPE' ? 'an.valeurGroupe' : 'an.valeurAttribut')}<input value={f.valeur} onChange={maj('valeur')} required /></label>
            <label>{t('an.role')}
              <select value={f.role} onChange={maj('role')}>
                {ROLES.map((r) => <option key={r} value={r}>{r || t('an.aucunRole')}</option>)}
              </select>
            </label>
            <label>{t('an.domaine')} <span className="muted">{t('an.domaineAide')}</span><input value={f.groupe} onChange={maj('groupe')} /></label>
          </div>
          <div className="cmp-row"><button type="submit">{t('an.ajouter')}</button></div>
        </form>
      </Bloc>
      <Bloc titre={t('an.scim')} large>
        <p className="muted">{t('an.scimAide')}</p>
        <dl className="clm-dl">
          <div><dt>{t('an.point')}</dt><dd><code className="clm-casse">{point}</code></dd></div>
          <div><dt>{t('an.jeton')}</dt><dd>{scim?.configure ? t('an.jetonConfigure', { par: scim.creePar, le: dateHeure(scim.creeLe) }) : t('an.jetonAbsent')}</dd></div>
        </dl>
        {jeton && (
          <div role="note" className="clm-info-statique">
            <p>{t('an.jetonUneFois')}</p>
            <code className="clm-casse">{jeton}</code>
          </div>
        )}
        <div className="cmp-row"><button type="button" className="secondary" onClick={genererJeton}>{t(scim?.configure ? 'an.remplacerJeton' : 'an.creerJeton')}</button></div>
      </Bloc>
    </Blocs>
  )
}
