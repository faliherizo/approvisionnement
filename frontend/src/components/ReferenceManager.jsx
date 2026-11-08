import { useEffect, useState, useCallback, useRef, useId } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const VIDE = { code: '', libelle: '', detail: '', ordre: 0, actif: true }

/**
 * Listes de référence (sélecteurs dynamiques), déclarées par chaque module. La plateforme (portee="superadmin") en fixe
 * les valeurs par défaut ; l'organisation (portee="admin") en ajoute, personnalise ou retire une valeur de la plateforme,
 * puis peut la rétablir. `modules` restreint les listes gérées à celles de ces modules (toutes sinon).
 */
export default function ReferenceManager({ portee, modules }) {
  const { t } = useI18n()
  const plateforme = portee === 'superadmin'
  const base = plateforme ? '/superadmin/reference' : '/admin/reference'
  const [categories, setCategories] = useState(null)
  const [categorie, setCategorie] = useState('')
  const [valeurs, setValeurs] = useState([])
  const [form, setForm] = useState(null)
  const [err, setErr] = useState(null)
  const [info, setInfo] = useState('')
  const premierChamp = useRef(null)
  const aideCode = useId()
  // Libellé associé par id plutôt qu'englobant : un <label> qui contient la liste ajoute l'option choisie au nom accessible.
  const champListe = useId()
  const aideListe = useId()
  const filtre = modules ? modules.join(',') : ''

  useEffect(() => {
    let actif = true
    api.get(`${base}/categories`).then((r) => {
      if (!actif) return
      const liste = r.data.filter((c) => !filtre || filtre.split(',').includes(c.module))
      setCategories(liste)
      setCategorie((c) => (liste.some((x) => x.code === c) ? c : liste[0]?.code || ''))
    }).catch(() => { if (actif) setCategories([]) })
    return () => { actif = false }
  }, [base, filtre])

  const charger = useCallback(() => {
    if (!categorie) { setValeurs([]); return }
    api.get(base, { params: { categorie } }).then((r) => setValeurs(r.data)).catch(() => setValeurs([]))
  }, [base, categorie])
  useEffect(() => { charger(); setForm(null) }, [charger])
  useEffect(() => { if (form) premierChamp.current?.focus() }, [form?.mode, form?.id, form?.code])

  const courante = (categories || []).find((c) => c.code === categorie)
  const avecDetail = Boolean(courante?.libelleDetail)
  const nomListe = (c) => {
    const cle = `referencemanager.cat.${c.code}`
    const traduit = t(cle)
    return traduit === cle ? c.libelle : traduit
  }

  async function executer(appel, message) {
    setErr(null); setInfo('')
    try { await appel(); setInfo(message); setForm(null); charger() } catch (x) { setErr(x.response?.data?.message || t('referencemanager.erreur')) }
  }
  const corps = (v, changements = {}) => ({ categorie, code: v.code, libelle: v.libelle, detail: avecDetail ? (v.detail || null) : null,
    ordre: Number(v.ordre) || 0, actif: v.actif, ...changements })
  /** Une valeur de la plateforme ne se modifie pas chez l'organisation : la reprendre sous son code la personnalise. */
  const parPersonnalisation = (v) => !plateforme && v.origine === 'PLATEFORME'
  const enregistrer = (e) => {
    e.preventDefault()
    const envoi = form.mode === 'modification' ? () => api.put(`${base}/${form.id}`, corps(form)) : () => api.post(base, corps(form))
    executer(envoi, t('referencemanager.enregistre'))
  }
  const basculer = (v) => executer(
    () => (parPersonnalisation(v) ? api.post(base, corps(v, { actif: false })) : api.put(`${base}/${v.id}`, corps(v, { actif: !v.actif }))),
    v.actif ? t('referencemanager.retiree') : t('referencemanager.reactivee'))
  const supprimer = (v) => executer(() => api.delete(`${base}/${v.id}`),
    v.origine === 'PERSONNALISEE' ? t('referencemanager.retablie') : t('referencemanager.supprimee'))
  const editer = (v) => setForm({ ...VIDE, ...v, detail: v.detail || '', mode: parPersonnalisation(v) ? 'personnalisation' : 'modification' })

  if (categories === null) return <p role="status">{t('referencemanager.chargement')}</p>
  if (categories.length === 0) return <p className="muted">{t('referencemanager.aucuneListe')}</p>

  return (
    <div className="reference-manager">
      <p className="muted">{plateforme ? t('referencemanager.valeursGlobales') : t('referencemanager.valeursTenant')}</p>
      <div className="champ-associe">
        <label htmlFor={champListe}>{t('referencemanager.categorie')}</label>
        <select id={champListe} value={categorie} onChange={(e) => setCategorie(e.target.value)} aria-describedby={courante?.aide ? aideListe : undefined}>
          {categories.map((c) => <option key={c.code} value={c.code}>{nomListe(c)}{plateforme ? ` (${c.module})` : ''}</option>)}
        </select>
      </div>
      {courante?.aide && <p id={aideListe} className="muted">{courante.aide}</p>}
      <div role="status" aria-live="polite">{info && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{info}</div>}</div>
      {err && <p className="erreur" role="alert">{err}</p>}

      <div className="table-scroll">
        <table className="table clm-table" style={{ marginTop: 12 }}>
          <caption className="sr-only">{courante ? nomListe(courante) : ''}</caption>
          <thead>
            <tr>
              <th scope="col">{t('referencemanager.code')}</th><th scope="col">{t('referencemanager.libelle')}</th>
              {avecDetail && <th scope="col">{courante.libelleDetail}</th>}
              <th scope="col">{t('referencemanager.ordre')}</th><th scope="col">{t('referencemanager.etat')}</th>
              {!plateforme && <th scope="col">{t('referencemanager.origine')}</th>}
              <th scope="col">{t('referencemanager.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {valeurs.map((v) => (
              <tr key={`${v.origine}-${v.code}`}>
                <td data-label={t('referencemanager.code')}><code>{v.code}</code></td>
                <td data-label={t('referencemanager.libelle')}>{v.libelle}</td>
                {avecDetail && <td data-label={courante.libelleDetail}>{v.detail || '—'}</td>}
                <td data-label={t('referencemanager.ordre')}>{v.ordre}</td>
                <td data-label={t('referencemanager.etat')}>{v.actif ? t('referencemanager.proposee') : t('referencemanager.retireeEtat')}</td>
                {!plateforme && <td data-label={t('referencemanager.origine')}>{t(`referencemanager.origine.${v.origine}`)}</td>}
                <td data-label={t('referencemanager.actions')}>
                  <div className="cmp-row">
                    <button type="button" className="secondary" aria-label={`${parPersonnalisation(v) ? t('referencemanager.personnaliser') : t('referencemanager.modifier')} — ${v.libelle}`}
                      onClick={() => editer(v)}>{parPersonnalisation(v) ? t('referencemanager.personnaliser') : t('referencemanager.modifier')}</button>
                    <button type="button" className="secondary" aria-label={`${v.actif ? t('referencemanager.retirer') : t('referencemanager.reactiver')} — ${v.libelle}`}
                      onClick={() => basculer(v)}>{v.actif ? t('referencemanager.retirer') : t('referencemanager.reactiver')}</button>
                    {v.origine !== 'PLATEFORME' || plateforme ? (
                      <button type="button" className={v.origine === 'PERSONNALISEE' ? 'secondary' : 'danger'}
                        aria-label={`${v.origine === 'PERSONNALISEE' ? t('referencemanager.retablir') : t('referencemanager.supprimer')} — ${v.libelle}`}
                        onClick={() => supprimer(v)}>{v.origine === 'PERSONNALISEE' ? t('referencemanager.retablir') : t('referencemanager.supprimer')}</button>
                    ) : null}
                  </div>
                </td>
              </tr>
            ))}
            {valeurs.length === 0 && <tr><td colSpan={7} className="muted">{t('referencemanager.listeVide')}</td></tr>}
          </tbody>
        </table>
      </div>

      {!form ? (
        <div className="cmp-row" style={{ marginTop: 12 }}>
          <button type="button" onClick={() => setForm({ ...VIDE, mode: 'ajout' })}>{t('referencemanager.ajouter')}</button>
        </div>
      ) : (
        <form onSubmit={enregistrer} className="form" style={{ marginTop: 12 }} aria-label={t(`referencemanager.form.${form.mode}`)}>
          <p><strong>{t(`referencemanager.form.${form.mode}`)}</strong></p>
          <label>{t('referencemanager.code')}
            <input ref={premierChamp} value={form.code} required maxLength={100} readOnly={form.mode === 'personnalisation' || form.origine === 'PERSONNALISEE'}
              aria-describedby={aideCode} onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })} />
          </label>
          <p id={aideCode} className="muted">{form.mode === 'personnalisation' || form.origine === 'PERSONNALISEE' ? t('referencemanager.codeFixe') : t('referencemanager.codeAide')}</p>
          <label>{t('referencemanager.libelle')}
            <input value={form.libelle} required maxLength={255} onChange={(e) => setForm({ ...form, libelle: e.target.value })} />
          </label>
          {avecDetail && (
            <label>{courante.libelleDetail}
              <textarea rows={3} maxLength={2000} value={form.detail} onChange={(e) => setForm({ ...form, detail: e.target.value })} />
            </label>
          )}
          <label>{t('referencemanager.ordre')}
            <input type="number" value={form.ordre} onChange={(e) => setForm({ ...form, ordre: e.target.value })} />
          </label>
          <label className="check-row"><input type="checkbox" checked={form.actif} onChange={(e) => setForm({ ...form, actif: e.target.checked })} />{t('referencemanager.proposee')}</label>
          <div className="cmp-row">
            <button type="button" className="secondary" onClick={() => setForm(null)}>{t('referencemanager.annuler')}</button>
            <button type="submit">{t('referencemanager.enregistrer')}</button>
          </div>
        </form>
      )}
    </div>
  )
}
