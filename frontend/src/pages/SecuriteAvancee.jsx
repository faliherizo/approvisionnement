import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

const PORTEES = ['CHAMP', 'FICHE', 'BOUTON']
const ACTIONS = ['LIRE', 'ECRIRE', 'EXPORTER', 'VALIDER']
const SUJETS = ['ROLE', 'GROUPE', 'UTILISATEUR']

/** Sécurité fine : permissions granulaires (refus par défaut) et groupes. Module « securite_avancee ». */
export default function SecuriteAvancee() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [permissions, setPermissions] = useState([])
  const [groupes, setGroupes] = useState([])
  const [pForm, setPForm] = useState({ sujetType: 'ROLE', sujet: '', portee: 'CHAMP', cible: '', action: 'LIRE' })
  const [gForm, setGForm] = useState({ code: '', nom: '' })
  const [membre, setMembre] = useState({})
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get('/securite/permissions').then((r) => setPermissions(r.data || [])).catch(() => setPermissions([]))
    api.get('/securite/groupes').then((r) => setGroupes(r.data || [])).catch(() => setGroupes([]))
  }, [])
  useEffect(() => { if (!chargement && actif('securite_avancee')) charger() }, [charger, chargement, actif])

  if (chargement) return <div className="panel">{t('secf.chargement')}</div>
  if (!actif('securite_avancee')) return <div className="panel">{t('secf.moduleInactif')}</div>

  async function ajouterPermission(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/securite/permissions', pForm); setPForm({ sujetType: 'ROLE', sujet: '', portee: 'CHAMP', cible: '', action: 'LIRE' }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('secf.erreur')) }
  }
  async function supprimerPermission(id) { await api.delete(`/securite/permissions/${id}`); charger() }
  async function ajouterGroupe(e) {
    e.preventDefault(); setErr(null)
    try { await api.post('/securite/groupes', gForm); setGForm({ code: '', nom: '' }); charger() }
    catch (e2) { setErr(e2.response?.data?.message || t('secf.erreur')) }
  }
  async function ajouterMembre(code) {
    const u = (membre[code] || '').trim(); if (!u) return
    setErr(null)
    try { await api.post(`/securite/groupes/${code}/membres`, null, { params: { utilisateur: u } }); setMembre({ ...membre, [code]: '' }) }
    catch (e2) { setErr(e2.response?.data?.message || t('secf.erreur')) }
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('secf.titre')}</h1>
        <p className="muted">{t('secf.intro')}</p>
        {err && <div className="erreur" role="alert">{err}</div>}
      </div>

      <section className="panel">
        <h2>{t('secf.permissions')}</h2>
        <div className="table-wrap">
        <table className="table">
          <thead><tr><th scope="col">{t('secf.colSujet')}</th><th scope="col">{t('secf.colPortee')}</th><th scope="col">{t('secf.colCible')}</th><th scope="col">{t('secf.colAction')}</th><th scope="col"><span className="sr-only">{t('secf.colActions')}</span></th></tr></thead>
          <tbody>
            {permissions.map((p) => (
              <tr key={p.id}>
                <td><span className="badge">{p.sujetType}</span> {p.sujet}</td>
                <td>{p.portee}</td><td><code>{p.cible}</code></td><td>{p.action}</td>
                <td><button className="danger" onClick={() => supprimerPermission(p.id)}>{t('secf.supprimer')}</button></td>
              </tr>
            ))}
            {permissions.length === 0 && <tr><td colSpan="5" className="muted">{t('secf.aucunePermission')}</td></tr>}
          </tbody>
        </table>
        </div>
        <form onSubmit={ajouterPermission} className="form" style={{ marginTop: 12 }}>
          <div className="cmp-row">
            <select value={pForm.sujetType} onChange={(e) => setPForm({ ...pForm, sujetType: e.target.value })} aria-label={t('secf.colSujet')}>
              {SUJETS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <input placeholder={t('secf.phSujet')} value={pForm.sujet} required onChange={(e) => setPForm({ ...pForm, sujet: e.target.value })} />
            <select value={pForm.portee} onChange={(e) => setPForm({ ...pForm, portee: e.target.value })} aria-label={t('secf.colPortee')}>
              {PORTEES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <input placeholder={t('secf.phCible')} value={pForm.cible} required onChange={(e) => setPForm({ ...pForm, cible: e.target.value })} />
            <select value={pForm.action} onChange={(e) => setPForm({ ...pForm, action: e.target.value })} aria-label={t('secf.colAction')}>
              {ACTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
            <button type="submit">{t('secf.ajouterPermission')}</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>{t('secf.groupes')}</h2>
        <div className="table-wrap">
        <table className="table">
          <thead><tr><th scope="col">{t('secf.colCode')}</th><th scope="col">{t('secf.colNom')}</th><th scope="col">{t('secf.colMembre')}</th></tr></thead>
          <tbody>
            {groupes.map((g) => (
              <tr key={g.id}>
                <td><code>{g.code}</code></td><td>{g.nom}</td>
                <td>
                  <div className="cmp-row">
                    <input aria-label={`${t('secf.phUtilisateur')} — ${g.code}`} placeholder={t('secf.phUtilisateur')} value={membre[g.code] || ''} onChange={(e) => setMembre({ ...membre, [g.code]: e.target.value })} />
                    <button className="secondary" onClick={() => ajouterMembre(g.code)}>{t('secf.ajouterMembre')}</button>
                  </div>
                </td>
              </tr>
            ))}
            {groupes.length === 0 && <tr><td colSpan="3" className="muted">{t('secf.aucunGroupe')}</td></tr>}
          </tbody>
        </table>
        </div>
        <form onSubmit={ajouterGroupe} className="form" style={{ marginTop: 12 }}>
          <div className="cmp-row">
            <input placeholder={t('secf.phCodeGroupe')} value={gForm.code} required onChange={(e) => setGForm({ ...gForm, code: e.target.value })} />
            <input placeholder={t('secf.phNomGroupe')} value={gForm.nom} required onChange={(e) => setGForm({ ...gForm, nom: e.target.value })} />
            <button type="submit">{t('secf.ajouterGroupe')}</button>
          </div>
        </form>
      </section>
    </div>
  )
}
