import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth.jsx'
import AiAssistant from '../components/AiAssistant.jsx'
import Modal, { ConfirmModal } from '../components/Modal.jsx'
import RichText, { RichHtml } from '../components/RichText.jsx'
import useReference from '../hooks/useReference.js'
import { useI18n } from '../i18n/I18nContext'

// Attributs du moteur de règles définissables par l'admin (éligibilité des clauses).
const ATTRIBUTS = [
  { code: 'TYPE_CONTRAT', cleLibelle: 'clauses.attrTypeContrat' },
  { code: 'MODE_SOLLICITATION', cleLibelle: 'clauses.attrMode' },
  { code: 'VALEUR_MIN', cleLibelle: 'clauses.attrValeurMin' },
  { code: 'VALEUR_MAX', cleLibelle: 'clauses.attrValeurMax' },
  { code: 'CONTEXTE', cleLibelle: 'clauses.attrContexte' },
]

const VIDE = {
  code: '', titre: '', categorie: 'STANDARD', corps: '', numero: '', section: '',
  normative: false, dependancesTexte: '', exclusionsTexte: '', conditions: [],
}

const TAILLE = 6

export default function Clauses() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const CATS = useReference('CATEGORIE_CLAUSE')
  const typesContrat = useReference('TYPE_CONTRAT')
  const modes = useReference('MODE_SOLLICITATION')

  const [data, setData] = useState({ contenu: [], total: 0 })
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const [edit, setEdit] = useState(null)          // clause en édition (modale)
  const [aSupprimer, setASupprimer] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [params, setParams] = useSearchParams()

  const charger = useCallback(() => {
    api.get('/clauses/recherche', { params: { q, page, taille: TAILLE } })
      .then((r) => setData(r.data))
  }, [q, page])
  useEffect(() => { charger() }, [charger])

  // Ouverture directe en édition depuis un lien profond (ex. /clauses?edit=42).
  useEffect(() => {
    const id = params.get('edit')
    if (!id) return
    api.get(`/clauses/${id}`).then((r) => ouvrirEdition(r.data)).catch(() => {})
    params.delete('edit'); setParams(params, { replace: true })
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const peutEditer = hasRole('ADMIN') || hasRole('JURIDIQUE') || hasRole('REDACTEUR')
  const peutSupprimer = hasRole('ADMIN') || hasRole('JURIDIQUE')
  const pages = Math.max(1, Math.ceil(data.total / TAILLE))
  const estVide = (html) => !(html || '').replace(/<[^>]*>/g, '').trim()

  const ouvrirCreation = () => { setErreur(null); setEdit({ ...VIDE, conditions: [] }) }
  const ouvrirEdition = (c) => {
    setErreur(null)
    setEdit({
      ...VIDE, ...c,
      dependancesTexte: (c.dependances || []).join(', '),
      exclusionsTexte: (c.exclusions || []).join(', '),
      conditions: (c.conditions || []).map((x) => ({
        cle: crypto.randomUUID(), attribut: x.attribut, valeur: x.valeur,
      })),
    })
  }

  const codes = (s) => (s || '').split(',').map((x) => x.trim()).filter(Boolean)

  // --- Édition des conditions (règles d'éligibilité) ---
  const valeurDefaut = (attribut) => {
    if (attribut === 'TYPE_CONTRAT') return typesContrat[0]?.code || ''
    if (attribut === 'MODE_SOLLICITATION') return modes[0]?.code || ''
    return ''
  }
  const ajouterCondition = () => setEdit((f) => ({
    ...f, conditions: [...f.conditions,
      { cle: crypto.randomUUID(), attribut: 'TYPE_CONTRAT', valeur: valeurDefaut('TYPE_CONTRAT') }],
  }))
  const majCondition = (cle, champ, valeur) => setEdit((f) => ({
    ...f, conditions: f.conditions.map((c) => {
      if (c.cle !== cle) return c
      const modifie = { ...c, [champ]: valeur }
      if (champ === 'attribut') modifie.valeur = valeurDefaut(valeur)
      return modifie
    }),
  }))
  const retirerCondition = (cle) => setEdit((f) => ({
    ...f, conditions: f.conditions.filter((c) => c.cle !== cle),
  }))

  async function enregistrer(e) {
    e.preventDefault(); setErreur(null)
    if (estVide(edit.corps)) { setErreur(t('clauses.corpsRequis')); return }
    const payload = {
      code: edit.code, titre: edit.titre, categorie: edit.categorie, corps: edit.corps,
      numero: edit.numero || null, section: edit.section || null, normative: !!edit.normative,
      dependances: codes(edit.dependancesTexte), exclusions: codes(edit.exclusionsTexte),
      conditions: edit.conditions
        .filter((c) => String(c.valeur).trim())
        .map((c) => ({ attribut: c.attribut, valeur: String(c.valeur) })),
    }
    try {
      if (edit.id) await api.put(`/clauses/${edit.id}`, payload)
      else await api.post('/clauses', payload)
      setEdit(null); charger()
    } catch (err) {
      setErreur(err.response?.data?.erreur || t('clauses.saveRefused'))
    }
  }

  async function supprimer(id) {
    await api.delete(`/clauses/${id}`); setASupprimer(null); charger()
  }

  return (
    <section className="panel">
      <div className="page-head">
        <h1>{t('clauses.libraryTitle', { n: data.total })}</h1>
        {peutEditer && <button onClick={ouvrirCreation}>{t('clauses.newClause')}</button>}
      </div>
      <input className="search-input" placeholder={t('clauses.searchPlaceholder')} value={q}
             onChange={(e) => { setPage(0); setQ(e.target.value) }} />

      <div className="clause-list">
        {data.contenu.map((c) => (
          <article key={c.id} className="clause">
            <header>
              <span className="code">{c.code}</span>
              <span className={`tag t-${c.categorie}`}>{c.categorie}</span>
              {c.normative && <span className="tag">{t('clauses.tagNormative')}</span>}
              <span className="v">v{c.version}</span>
            </header>
            <h4>{c.numero ? `${c.numero} — ${c.titre}` : c.titre}</h4>
            <RichHtml html={c.corps} />
            {c.conditions?.length > 0 && (
              <div className="conds">{t('clauses.eligibility')} {c.conditions.map((x) => (
                <code key={x.id ?? `${x.attribut}${x.valeur}`}>{x.attribut}={x.valeur}</code>
              ))}</div>
            )}
            {c.dependances?.length > 0 && (
              <div className="conds">{t('clauses.requires')} {c.dependances.map((d) => <code key={d}>{d}</code>)}</div>
            )}
            {c.exclusions?.length > 0 && (
              <div className="conds">{t('clauses.excludes')} {c.exclusions.map((x) => <code key={x}>{x}</code>)}</div>
            )}
            {(peutEditer || peutSupprimer) && (
              <div className="row-actions">
                {peutEditer && <button className="secondary" onClick={() => ouvrirEdition(c)}>{t('clauses.edit')}</button>}
                {peutSupprimer && <button className="danger" onClick={() => setASupprimer(c)}>{t('clauses.delete')}</button>}
              </div>
            )}
          </article>
        ))}
        {data.contenu.length === 0 && <p className="muted">{t('clauses.empty')}</p>}
      </div>

      <div className="pagination">
        <button className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>{t('clauses.prev')}</button>
        <span className="muted">{t('clauses.pageOf', { page: page + 1, pages })}</span>
        <button className="secondary" disabled={page + 1 >= pages} onClick={() => setPage(page + 1)}>{t('clauses.next')}</button>
      </div>

      {edit && (
        <Modal titre={edit.id ? t('clauses.editTitle', { code: edit.code }) : t('clauses.newClause')}
               onClose={() => setEdit(null)} taille="lg">
          <form onSubmit={enregistrer} className="form">
            <label>{t('clauses.code')}
              <input value={edit.code} required disabled={!!edit.id}
                     onChange={(e) => setEdit({ ...edit, code: e.target.value })} />
            </label>
            <label>{t('clauses.titre')}
              <input value={edit.titre} required
                     onChange={(e) => setEdit({ ...edit, titre: e.target.value })} />
            </label>
            <div className="grid2">
              <label>{t('clauses.category')}
                <select value={edit.categorie}
                        onChange={(e) => setEdit({ ...edit, categorie: e.target.value })}>
                  {CATS.map((c) => <option key={c.code} value={c.code}>{c.libelle}</option>)}
                </select>
              </label>
              <label>{t('clauses.numero')}
                <input value={edit.numero || ''} onChange={(e) => setEdit({ ...edit, numero: e.target.value })} />
              </label>
            </div>
            <div className="grid2">
              <label>{t('clauses.section')}
                <input value={edit.section || ''} onChange={(e) => setEdit({ ...edit, section: e.target.value })} />
              </label>
              <label className="check-row">
                <input type="checkbox" checked={!!edit.normative}
                       onChange={(e) => setEdit({ ...edit, normative: e.target.checked })} /> {t('clauses.normativeLabel')}
              </label>
            </div>
            <label>{t('clauses.corps')}
              <RichText value={edit.corps} rows={5}
                        onChange={(corps) => setEdit((f) => ({ ...f, corps }))} />
            </label>

            <div>
              <div className="form-legend-row">
                <span className="form-legend">{t('clauses.conditionsLegend')}</span>
                <button type="button" className="secondary" onClick={ajouterCondition}>{t('clauses.addCondition')}</button>
              </div>
              {edit.conditions.length === 0 && (
                <p className="muted">{t('clauses.noCondition')}</p>
              )}
              {edit.conditions.map((c) => (
                <div key={c.cle} className="cond-row">
                  <select value={c.attribut} onChange={(e) => majCondition(c.cle, 'attribut', e.target.value)}>
                    {ATTRIBUTS.map((a) => <option key={a.code} value={a.code}>{t(a.cleLibelle)}</option>)}
                  </select>
                  {c.attribut === 'TYPE_CONTRAT' && (
                    <select value={c.valeur} onChange={(e) => majCondition(c.cle, 'valeur', e.target.value)}>
                      {typesContrat.map((t) => <option key={t.code} value={t.code}>{t.libelle}</option>)}
                    </select>
                  )}
                  {c.attribut === 'MODE_SOLLICITATION' && (
                    <select value={c.valeur} onChange={(e) => majCondition(c.cle, 'valeur', e.target.value)}>
                      {modes.map((m) => <option key={m.code} value={m.code}>{m.libelle}</option>)}
                    </select>
                  )}
                  {(c.attribut === 'VALEUR_MIN' || c.attribut === 'VALEUR_MAX') && (
                    <input type="number" step="0.01" placeholder={t('clauses.amountPlaceholder')} value={c.valeur}
                           onChange={(e) => majCondition(c.cle, 'valeur', e.target.value)} />
                  )}
                  {c.attribut === 'CONTEXTE' && (
                    <input placeholder={t('clauses.contextKeyword')} value={c.valeur}
                           onChange={(e) => majCondition(c.cle, 'valeur', e.target.value)} />
                  )}
                  <button type="button" className="danger" aria-label={t('clauses.removeCondition')}
                          onClick={() => retirerCondition(c.cle)}>×</button>
                </div>
              ))}
            </div>

            <label>{t('clauses.dependencies')}
              <input value={edit.dependancesTexte} placeholder={t('clauses.dependenciesPlaceholder')}
                     onChange={(e) => setEdit({ ...edit, dependancesTexte: e.target.value })} />
            </label>
            <label>{t('clauses.exclusions')}
              <input value={edit.exclusionsTexte} placeholder={t('clauses.exclusionsPlaceholder')}
                     onChange={(e) => setEdit({ ...edit, exclusionsTexte: e.target.value })} />
            </label>

            {erreur && <div className="erreur">{erreur}</div>}
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setEdit(null)}>{t('clauses.cancel')}</button>
              <button type="submit">{t('clauses.save')}</button>
            </div>

            <AiAssistant
              contexteParDefaut={t('clauses.aiContext')}
              onInsertion={(texte) => setEdit((f) => ({ ...f, corps: texte }))}
            />
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <ConfirmModal
          titre={t('clauses.deleteTitle')}
          message={t('clauses.deleteConfirm', { code: aSupprimer.code, titre: aSupprimer.titre })}
          onConfirm={() => supprimer(aSupprimer.id)}
          onClose={() => setASupprimer(null)}
        />
      )}
    </section>
  )
}
