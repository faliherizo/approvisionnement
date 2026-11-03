import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import Modal, { ConfirmModal } from '../components/Modal.jsx'
import { useI18n } from '../i18n/I18nContext'

const VIDE = { email: '', nom: '', direction: '', motDePasse: '', roles: [], actif: true }

export default function Users() {
  const { hasRole } = useAuth()
  const { t } = useI18n()
  const [data, setData] = useState({ contenu: [], total: 0, page: 0, taille: 10 })
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const [rolesDispo, setRolesDispo] = useState([])
  const [edit, setEdit] = useState(null)         // user en édition (modale)
  const [aSupprimer, setASupprimer] = useState(null)
  const [err, setErr] = useState(null)

  const taille = 8
  const charger = useCallback(() => {
    api.get('/admin/users', { params: { q, page, taille } }).then((r) => setData(r.data))
  }, [q, page])

  useEffect(() => { charger() }, [charger])
  useEffect(() => { api.get('/admin/users/roles').then((r) => setRolesDispo(r.data)) }, [])

  if (!hasRole('ADMIN')) return <div className="panel">{t('users.adminOnly')}</div>

  const pages = Math.max(1, Math.ceil(data.total / taille))

  async function enregistrer(e) {
    e.preventDefault(); setErr(null)
    try {
      if (edit.id) await api.put(`/admin/users/${edit.id}`, edit)
      else await api.post('/admin/users', edit)
      setEdit(null); charger()
    } catch (e2) { setErr(e2.response?.data?.message || t('users.errorSave')) }
  }
  async function supprimer(id) {
    await api.delete(`/admin/users/${id}`); charger()
  }
  const toggleRole = (r) => setEdit((u) => ({
    ...u, roles: u.roles.includes(r) ? u.roles.filter((x) => x !== r) : [...u.roles, r]
  }))

  return (
    <section className="panel">
      <div className="page-head">
        <h1>{t('users.title', { n: data.total })}</h1>
        <button onClick={() => { setEdit({ ...VIDE }); setErr(null) }}>{t('users.newUser')}</button>
      </div>
      <input className="search-input" aria-label={t('users.searchPlaceholder')} placeholder={t('users.searchPlaceholder')} value={q}
             onChange={(e) => { setPage(0); setQ(e.target.value) }} />
      <div className="table-wrap">
        <table className="table">
          <thead><tr><th scope="col">{t('users.colEmail')}</th><th scope="col">{t('users.colName')}</th><th scope="col">{t('users.colRoles')}</th><th scope="col">{t('users.colActive')}</th><th scope="col" className="col-actions">{t('users.colActions')}</th></tr></thead>
          <tbody>
            {data.contenu.map((u) => (
              <tr key={u.id}>
                <td>{u.email}</td><td>{u.nom}</td>
                <td>{u.roles?.join(', ')}</td>
                <td><span className={`badge ${u.actif ? 's-APPROUVE' : 's-REFUSE'}`}>{u.actif ? t('users.yes') : t('users.no')}</span></td>
                <td>
                  <div className="row-actions">
                    <button className="secondary" onClick={() => { setEdit({ ...u, motDePasse: '' }); setErr(null) }}>{t('users.edit')}</button>
                    <button className="danger" onClick={() => setASupprimer(u)}>{t('users.delete')}</button>
                  </div>
                </td>
              </tr>
            ))}
            {data.contenu.length === 0 && <tr><td colSpan="5" className="muted">{t('users.empty')}</td></tr>}
          </tbody>
        </table>
      </div>
      <div className="pagination">
        <button className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>{t('users.previous')}</button>
        <span className="muted">{t('users.pageOf', { current: page + 1, total: pages })}</span>
        <button className="secondary" disabled={page + 1 >= pages} onClick={() => setPage(page + 1)}>{t('users.next')}</button>
      </div>

      {edit && (
        <Modal titre={edit.id ? t('users.editUser') : t('users.createUser')} onClose={() => setEdit(null)}>
          <form onSubmit={enregistrer} className="form">
            <label>{t('users.colEmail')}
              <input type="email" value={edit.email} required disabled={!!edit.id}
                     onChange={(e) => setEdit({ ...edit, email: e.target.value })} />
            </label>
            <label>{t('users.colName')}<input value={edit.nom} required onChange={(e) => setEdit({ ...edit, nom: e.target.value })} /></label>
            <label>{t('users.direction')}<input value={edit.direction || ''} onChange={(e) => setEdit({ ...edit, direction: e.target.value })} /></label>
            <label>{edit.id ? t('users.newPassword') : t('users.password')}
              <input type="password" value={edit.motDePasse || ''} onChange={(e) => setEdit({ ...edit, motDePasse: e.target.value })} />
            </label>
            <div>
              <div className="form-legend">{t('users.rolesRbac')}</div>
              <div className="roles-grid">
                {rolesDispo.map((r) => (
                  <label key={r} className="check-row">
                    <input type="checkbox" checked={edit.roles?.includes(r)} onChange={() => toggleRole(r)} /> {r}
                  </label>
                ))}
              </div>
            </div>
            <label className="check-row">
              <input type="checkbox" checked={edit.actif} onChange={(e) => setEdit({ ...edit, actif: e.target.checked })} /> {t('users.colActive')}
            </label>
            {err && <div className="erreur">{err}</div>}
            <div className="modal-actions">
              <button type="button" className="secondary" onClick={() => setEdit(null)}>{t('users.cancel')}</button>
              <button type="submit">{t('users.save')}</button>
            </div>
          </form>
        </Modal>
      )}

      {aSupprimer && (
        <ConfirmModal
          titre={t('users.deleteUserTitle')}
          message={t('users.deleteConfirm', { email: aSupprimer.email })}
          onConfirm={() => supprimer(aSupprimer.id)}
          onClose={() => setASupprimer(null)}
        />
      )}
    </section>
  )
}
