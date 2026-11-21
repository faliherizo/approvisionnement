import { useCallback, useEffect, useState } from 'react'
import api from '../api'
import { useAuth } from '../auth.jsx'
import { useI18n } from '../i18n/I18nContext'

const VIDE = { sujet: '', reference: '', participants: '', message: '' }

/**
 * Messagerie interne sécurisée entre intervenants (CHU 5.05) : conversations visibles des seuls participants, messages en
 * texte brut (jamais interprétés comme HTML), indicateur de messages non lus.
 */
export default function Messagerie() {
  const { t, locale } = useI18n()
  const { user } = useAuth()
  const [liste, setListe] = useState([])
  const [ouverte, setOuverte] = useState(null)
  const [nouvelle, setNouvelle] = useState(null)
  const [reponse, setReponse] = useState('')
  const [ajout, setAjout] = useState('')
  const [erreur, setErreur] = useState(null)

  const date = (iso) => new Date(iso).toLocaleString(locale === 'en' ? 'en-CA' : 'fr-CA')
  const charger = useCallback(() => {
    api.get('/messagerie/conversations').then((r) => setListe(r.data)).catch(() => setListe([]))
  }, [])
  useEffect(charger, [charger])

  async function faire(action) {
    setErreur(null)
    try { const r = await action(); setOuverte(r.data); charger(); return true }
    catch (e) { setErreur(e.response?.data?.message || t('messagerie.erreur')); return false }
  }

  const ouvrir = (id) => faire(() => api.get(`/messagerie/conversations/${id}`))
  async function creer(e) {
    e.preventDefault()
    const participants = nouvelle.participants.split(/[,;\s]+/).map((x) => x.trim()).filter(Boolean)
    if (await faire(() => api.post('/messagerie/conversations', { ...nouvelle, participants }))) setNouvelle(null)
  }
  async function repondre(e) {
    e.preventDefault()
    if (await faire(() => api.post(`/messagerie/conversations/${ouverte.id}/messages`, { texte: reponse }))) setReponse('')
  }
  async function ajouter(e) {
    e.preventDefault()
    if (await faire(() => api.post(`/messagerie/conversations/${ouverte.id}/participants`, { courriel: ajout }))) setAjout('')
  }

  return (
    <div>
      <div className="panel">
        <h1>{t('messagerie.titre')}</h1>
        <p className="muted">{t('messagerie.intro')}</p>
        {erreur && <div className="erreur" role="alert">{erreur}</div>}
        <button type="button" onClick={() => { setNouvelle(VIDE); setOuverte(null) }}>{t('messagerie.nouvelle')}</button>
      </div>
      <div className="messagerie">
        <nav className="panel messagerie-liste" aria-label={t('messagerie.conversations')}>
          <h2>{t('messagerie.conversations')}</h2>
          {liste.length === 0 && <p className="muted">{t('messagerie.aucune')}</p>}
          <ul>
            {liste.map((c) => (
              <li key={c.id}>
                <button type="button" className={`secondary messagerie-item${ouverte?.id === c.id ? ' actif' : ''}`}
                        aria-current={ouverte?.id === c.id ? 'true' : undefined} onClick={() => ouvrir(c.id)}>
                  <strong>{c.sujet}</strong>
                  {c.nonLus > 0 && <span className="badge">{t('messagerie.nonLus', { n: c.nonLus })}</span>}
                  <small className="muted">{date(c.dernierMessageLe)}</small>
                </button>
              </li>
            ))}
          </ul>
        </nav>

        <section className="panel messagerie-fil" aria-live="polite">
          {nouvelle && (
            <form className="form" onSubmit={creer}>
              <h2>{t('messagerie.nouvelle')}</h2>
              <label>{t('messagerie.sujet')}<input required maxLength={200} value={nouvelle.sujet} onChange={(e) => setNouvelle({ ...nouvelle, sujet: e.target.value })} /></label>
              <label>{t('messagerie.reference')}<input maxLength={100} value={nouvelle.reference} placeholder={t('messagerie.referenceAide')} onChange={(e) => setNouvelle({ ...nouvelle, reference: e.target.value })} /></label>
              <label>{t('messagerie.participants')}<input required value={nouvelle.participants} placeholder={t('messagerie.participantsAide')} onChange={(e) => setNouvelle({ ...nouvelle, participants: e.target.value })} /></label>
              <label>{t('messagerie.message')}<textarea required rows={4} maxLength={5000} value={nouvelle.message} onChange={(e) => setNouvelle({ ...nouvelle, message: e.target.value })} /></label>
              <div className="modal-actions">
                <button type="button" className="secondary" onClick={() => setNouvelle(null)}>{t('modal.annuler')}</button>
                <button type="submit">{t('messagerie.envoyer')}</button>
              </div>
            </form>
          )}
          {!nouvelle && !ouverte && <p className="muted">{t('messagerie.choisir')}</p>}
          {!nouvelle && ouverte && (
            <>
              <h2>{ouverte.sujet} {ouverte.reference && <code>{ouverte.reference}</code>}</h2>
              <p className="muted">{t('messagerie.avec', { participants: ouverte.participants.join(', ') })}</p>
              <ol className="messagerie-messages">
                {ouverte.messages.map((m) => (
                  <li key={m.id} className={m.auteur === user?.email?.toLowerCase() ? 'moi' : ''}>
                    <div className="muted"><strong>{m.auteur}</strong> · <time dateTime={m.envoyeLe}>{date(m.envoyeLe)}</time></div>
                    <p className="messagerie-texte">{m.texte}</p>
                  </li>
                ))}
              </ol>
              <form className="form" onSubmit={repondre}>
                <label>{t('messagerie.repondre')}<textarea required rows={3} maxLength={5000} value={reponse} onChange={(e) => setReponse(e.target.value)} /></label>
                <button type="submit">{t('messagerie.envoyer')}</button>
              </form>
              <form className="form cmp-row" onSubmit={ajouter}>
                <label>{t('messagerie.ajouterParticipant')}<input type="email" required value={ajout} onChange={(e) => setAjout(e.target.value)} /></label>
                <button type="submit" className="secondary">{t('messagerie.ajouter')}</button>
              </form>
            </>
          )}
        </section>
      </div>
    </div>
  )
}
