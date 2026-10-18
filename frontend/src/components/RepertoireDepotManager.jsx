import { useEffect, useState } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

/**
 * Gestion des répertoires de dépôt personnels de l'utilisateur (son espace). Ces dossiers alimentent
 * les suggestions du champ « Répertoire de destination » lors de la publication vers Microsoft 365.
 */
export default function RepertoireDepotManager() {
  const { t } = useI18n()
  const [repertoires, setRepertoires] = useState([])
  const [saisie, setSaisie] = useState('')
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  const charger = () => api.get('/profil/repertoires').then((r) => setRepertoires(r.data)).catch(() => {})
  useEffect(() => { charger() }, [])

  async function ajouter(e) {
    e.preventDefault(); setErr(null); setMsg(null)
    if (!saisie.trim()) return
    try {
      await api.post('/profil/repertoires', { chemin: saisie })
      setSaisie(''); setMsg(t('repertoiredepotmanager.repertoireAjoute')); charger()
    } catch (e2) {
      setErr(e2.response?.data?.message || t('repertoiredepotmanager.ajoutImpossible'))
    }
  }

  async function retirer(id) {
    setErr(null); setMsg(null)
    try { await api.delete(`/profil/repertoires/${id}`); charger() }
    catch { setErr(t('repertoiredepotmanager.suppressionImpossible')) }
  }

  return (
    <section className="panel">
      <h2>📁 {t('repertoiredepotmanager.titre')}</h2>
      <p className="muted">
        {t('repertoiredepotmanager.description')}
      </p>
      {msg && <div className="erreur" role="status" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur" role="alert">{err}</div>}

      <form onSubmit={ajouter} className="form" style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
        <label style={{ flex: 1 }}>{t('repertoiredepotmanager.nouveauRepertoire')}
          <input value={saisie} placeholder={t('repertoiredepotmanager.exemplePlaceholder')}
                 onChange={(e) => setSaisie(e.target.value)} />
        </label>
        <button type="submit">{t('repertoiredepotmanager.ajouter')}</button>
      </form>

      {repertoires.length === 0
        ? <p className="muted">{t('repertoiredepotmanager.aucunRepertoire')}</p>
        : (
          <ul className="liste-repertoires">
            {repertoires.map((r) => (
              <li key={r.id}>
                <code>{r.chemin}</code>
                <button type="button" className="secondary" onClick={() => retirer(r.id)}>{t('repertoiredepotmanager.retirer')}</button>
              </li>
            ))}
          </ul>
        )}
    </section>
  )
}
