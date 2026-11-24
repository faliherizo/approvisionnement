import { useEffect, useState, useRef } from 'react'
import api from '../api'
import RichText from '../components/RichText.jsx'
import { useI18n } from '../i18n/I18nContext'

export default function ChatIA() {
  const { t } = useI18n()
  const [messages, setMessages] = useState([
    { role: 'ia', texte: t('chatia.bonjour') }
  ])
  const [question, setQuestion] = useState('')
  const [busy, setBusy] = useState(false)
  const [knowledge, setKnowledge] = useState([])
  const [doc, setDoc] = useState({ titre: '', source: '', contenu: '' })
  const bas = useRef(null)

  const chargerKnowledge = () => api.get('/ia/knowledge').then((r) => setKnowledge(r.data))
  useEffect(() => { chargerKnowledge() }, [])
  useEffect(() => { bas.current?.scrollIntoView({ behavior: 'smooth' }) }, [messages])

  async function envoyer(e) {
    e.preventDefault()
    if (!question.trim()) return
    const q = question
    setMessages((m) => [...m, { role: 'moi', texte: q }])
    setQuestion(''); setBusy(true)
    try {
      const { data } = await api.post('/ia/chat', { question: q })
      setMessages((m) => [...m, { role: 'ia', texte: data.reponse, sources: data.sources }])
    } catch (e2) {
      setMessages((m) => [...m, { role: 'ia', texte: '⚠️ ' + (e2.response?.data?.erreur || t('chatia.serviceIndisponible')), erreur: true }])
    } finally { setBusy(false) }
  }

  async function ajouterDoc(e) {
    e.preventDefault()
    if (!(doc.contenu || '').replace(/<[^>]*>/g, '').trim()) return // contenu requis
    await api.post('/ia/knowledge', doc)
    setDoc({ titre: '', source: '', contenu: '' }); chargerKnowledge()
  }
  async function supprimerDoc(id) {
    await api.delete(`/ia/knowledge/${id}`); chargerKnowledge()
  }

  return (
    <div className="grid2">
      <section className="panel chat">
        <h1><span className="vina-avatar" aria-hidden="true">V</span> {t('chatia.titre')}</h1>
        <p className="muted">{t('chatia.propulseA')} <strong>VINA IT Solutions inc.</strong> {t('chatia.propulseB')}</p>
        <div className="chat-log">
          {messages.map((m, i) => (
            <div key={i} className={`bulle ${m.role}`}>
              <div>{m.texte}</div>
              {m.sources?.length > 0 && <div className="chat-src">{t('chatia.sources', { sources: m.sources.join(', ') })}</div>}
            </div>
          ))}
          {busy && <div className="bulle ia muted">{t('chatia.assistantRedige')}</div>}
          <div ref={bas} />
        </div>
        <form onSubmit={envoyer} className="chat-input">
          <input value={question} placeholder={t('chatia.placeholderQuestion')}
                 onChange={(e) => setQuestion(e.target.value)} />
          <button type="submit" disabled={busy}>{t('chatia.envoyer')}</button>
        </form>
      </section>

      <section className="panel">
        <h2>{t('chatia.baseConnaissances', { n: knowledge.length })}</h2>
        <p className="muted">{t('chatia.ajoutezDocuments')}</p>
        <div className="clause-list">
          {knowledge.map((k) => (
            <article key={k.id} className="clause">
              <header>
                <strong>{k.titre}</strong>
                {k.source && <span className="tag">{k.source}</span>}
                <button className="danger" style={{ marginLeft: 'auto' }} onClick={() => supprimerDoc(k.id)} aria-label={t('chat.retirerDocument', { titre: k.titre })}>×</button>
              </header>
              <p>{k.contenu.slice(0, 180)}{k.contenu.length > 180 ? '…' : ''}</p>
            </article>
          ))}
          {knowledge.length === 0 && <p className="muted">{t('chatia.aucuneConnaissance')}</p>}
        </div>
        <form onSubmit={ajouterDoc} className="form" style={{ marginTop: 12 }}>
          <label>{t('chatia.labelTitre')}<input value={doc.titre} required onChange={(e) => setDoc({ ...doc, titre: e.target.value })} /></label>
          <label>{t('chatia.labelSource')}<input value={doc.source} onChange={(e) => setDoc({ ...doc, source: e.target.value })} /></label>
          <label>{t('chatia.labelContenu')}
            <RichText value={doc.contenu} rows={5} libelle={t('chatia.labelContenu')} onChange={(contenu) => setDoc((d) => ({ ...d, contenu }))} />
          </label>
          <button type="submit">{t('chatia.ajouter')}</button>
        </form>
      </section>
    </div>
  )
}
