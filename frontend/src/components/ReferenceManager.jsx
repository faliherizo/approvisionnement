import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const CATEGORIES = [
  { code: 'TYPE_CONTRAT', cle: 'referencemanager.typeContrat' },
  { code: 'MODE_SOLLICITATION', cle: 'referencemanager.modeSollicitation' },
  { code: 'CATEGORIE_CLAUSE', cle: 'referencemanager.categorieClause' },
  { code: 'TYPE_OBLIGATION', cle: 'referencemanager.typeObligation' },
]

/**
 * Gestionnaire de valeurs de référence (sélecteurs dynamiques).
 * portee = "superadmin" (global) ou "admin" (tenant courant).
 */
export default function ReferenceManager({ portee }) {
  const { t } = useI18n()
  const base = portee === 'superadmin' ? '/superadmin/reference' : '/admin/reference'
  const [categorie, setCategorie] = useState('TYPE_CONTRAT')
  const [valeurs, setValeurs] = useState([])
  const [form, setForm] = useState({ code: '', libelle: '', ordre: 0 })
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get(base, { params: { categorie } }).then((r) => setValeurs(r.data)).catch(() => setValeurs([]))
  }, [base, categorie])
  useEffect(() => { charger() }, [charger])

  async function ajouter(e) {
    e.preventDefault(); setErr(null)
    try {
      await api.post(base, { categorie, code: form.code, libelle: form.libelle, ordre: Number(form.ordre) || 0 })
      setForm({ code: '', libelle: '', ordre: 0 }); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('referencemanager.erreur')) }
  }
  async function supprimer(id) {
    await api.delete(`${base}/${id}`); charger()
  }

  return (
    <div>
      <p className="muted">
        {portee === 'superadmin'
          ? t('referencemanager.valeursGlobales')
          : t('referencemanager.valeursTenant')}
      </p>
      <label>{t('referencemanager.categorie')}
        <select value={categorie} onChange={(e) => setCategorie(e.target.value)}>
          {CATEGORIES.map((c) => <option key={c.code} value={c.code}>{t(c.cle)}</option>)}
        </select>
      </label>

      <table className="table" style={{ marginTop: 12 }}>
        <thead><tr><th scope="col">{t('referencemanager.code')}</th><th scope="col">{t('referencemanager.libelle')}</th><th scope="col">{t('referencemanager.ordre')}</th><th scope="col">{t('referencemanager.actif')}</th><th scope="col"><span className="sr-only">{t('referencemanager.actions')}</span></th></tr></thead>
        <tbody>
          {valeurs.map((v) => (
            <tr key={v.id}>
              <td><code>{v.code}</code></td><td>{v.libelle}</td><td>{v.ordre}</td>
              <td><span className={`badge ${v.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{v.actif ? t('referencemanager.oui') : t('referencemanager.non')}</span></td>
              <td><button className="danger" onClick={() => supprimer(v.id)}>{t('referencemanager.supprimer')}</button></td>
            </tr>
          ))}
          {valeurs.length === 0 && <tr><td colSpan="5" className="muted">{t('referencemanager.aucuneValeur')}</td></tr>}
        </tbody>
      </table>

      <form onSubmit={ajouter} className="form" style={{ marginTop: 12 }}>
        <div className="cmp-row">
          <input placeholder={t('referencemanager.codePlaceholder')} value={form.code} required onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })} />
          <input placeholder={t('referencemanager.libelle')} value={form.libelle} required onChange={(e) => setForm({ ...form, libelle: e.target.value })} />
          <input type="number" placeholder={t('referencemanager.ordre')} value={form.ordre} style={{ width: 90 }} onChange={(e) => setForm({ ...form, ordre: e.target.value })} />
          <button type="submit">{t('referencemanager.ajouter')}</button>
        </div>
        {err && <div className="erreur">{err}</div>}
      </form>
    </div>
  )
}
