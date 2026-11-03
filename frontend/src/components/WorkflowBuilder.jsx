import { useEffect, useState, useCallback } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'

const OPERATEURS = ['EGAL', 'DIFFERENT', 'SUP', 'INF', 'SUPEGAL', 'INFEGAL', 'CONTIENT', 'VIDE', 'NON_VIDE']
const TYPES_ETAPE = ['VERIFICATION', 'APPROBATION', 'TRAITEMENT']
const ROLES = ['APPRO', 'DIRECTION', 'ADMIN', 'JURIDIQUE', 'REDACTEUR']

function slug(s) {
  return (s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '').slice(0, 40) || 'etape'
}

/**
 * Concepteur de workflow (écran 5) : séquence d'étapes, chacune assignée à un rôle, avec conditions de
 * routage (ex. montant > seuil) et délai. La définition est stockée en JSON — rien de figé.
 */
export default function WorkflowBuilder({ id }) {
  const { t } = useI18n()
  const [etapes, setEtapes] = useState([])
  const [champs, setChamps] = useState([])
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)

  const charger = useCallback(() => {
    api.get(`/formulaires/${id}/workflow`).then((r) => {
      try { const d = JSON.parse(r.data.definitionJson || '{}'); setEtapes(d.etapes || []) } catch { setEtapes([]) }
    }).catch(() => setErr(t('workflow.erreurChargement')))
    api.get(`/formulaires/${id}/version`).then((r) => {
      try {
        const s = r.data.structureJson ? JSON.parse(r.data.structureJson) : { pages: [] }
        setChamps((s.pages || []).flatMap((p) => (p.champs || []).map((c) => ({ code: c.code, libelle: c.libelle }))))
      } catch { setChamps([]) }
    }).catch(() => setChamps([]))
  }, [id, t])
  useEffect(() => { charger() }, [charger])

  const maj = (prod) => setEtapes((e) => prod(structuredClone(e)))
  const ajouterEtape = () => maj((e) => { e.push({ code: `etape_${e.length + 1}`, nom: t('workflow.etapeDefaut', { n: e.length + 1 }), type: 'APPROBATION', responsableRole: 'APPRO', responsableGroupe: '', delaiJours: 3, commentaireRefusObligatoire: true, conditions: [] }); return e })
  const majEtape = (i, patch) => maj((e) => { e[i] = { ...e[i], ...patch }; return e })
  const retirerEtape = (i) => maj((e) => { e.splice(i, 1); return e })
  const deplacer = (i, delta) => maj((e) => { const j = i + delta; if (j < 0 || j >= e.length) return e; [e[i], e[j]] = [e[j], e[i]]; return e })
  const ajouterCond = (i) => maj((e) => { e[i].conditions = [...(e[i].conditions || []), { champ: '', operateur: 'SUP', valeur: '' }]; return e })
  const majCond = (i, ci, patch) => maj((e) => { e[i].conditions[ci] = { ...e[i].conditions[ci], ...patch }; return e })
  const retirerCond = (i, ci) => maj((e) => { e[i].conditions.splice(ci, 1); return e })

  async function enregistrer() {
    setErr(null); setMsg(null)
    try {
      await api.put(`/formulaires/${id}/workflow`, { definitionJson: JSON.stringify({ etapes }) })
      setMsg(t('workflow.enregistre'))
    } catch (e) { setErr(e.response?.data?.message || t('workflow.erreurEnregistrement')) }
  }

  return (
    <div className="panel">
      <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
        <h3>{t('workflow.titre')}</h3>
        <div className="cmp-row">
          <button className="secondary" onClick={ajouterEtape}>+ {t('workflow.ajouterEtape')}</button>
          <button onClick={enregistrer}>{t('workflow.enregistrer')}</button>
        </div>
      </div>
      <p className="muted">{t('workflow.intro')}</p>
      {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
      {err && <div className="erreur">{err}</div>}
      {etapes.length === 0 && <p className="muted">{t('workflow.aucuneEtape')}</p>}

      {etapes.map((et, i) => (
        <fieldset key={i} className="panel" style={{ marginTop: 12 }}>
          <legend>{t('workflow.etapeN', { n: i + 1 })}</legend>
          <div className="cmp-row" style={{ flexWrap: 'wrap' }}>
            <label>{t('workflow.nom')}
              <input value={et.nom} onChange={(e) => majEtape(i, { nom: e.target.value, code: et.code || slug(e.target.value) })} />
            </label>
            <label>{t('workflow.type')}
              <select value={et.type} onChange={(e) => majEtape(i, { type: e.target.value })}>
                {TYPES_ETAPE.map((x) => <option key={x} value={x}>{t(`workflow.type.${x}`)}</option>)}
              </select>
            </label>
            <label>{t('workflow.role')}
              <select value={et.responsableRole} onChange={(e) => majEtape(i, { responsableRole: e.target.value })}>
                {ROLES.map((x) => <option key={x} value={x}>{x}</option>)}
              </select>
            </label>
            <label>{t('workflow.delai')}
              <input type="number" min="0" style={{ width: 90 }} value={et.delaiJours ?? ''} onChange={(e) => majEtape(i, { delaiJours: e.target.value ? Number(e.target.value) : null })} />
            </label>
            <label className="check-row">
              <input type="checkbox" checked={!!et.commentaireRefusObligatoire} onChange={(e) => majEtape(i, { commentaireRefusObligatoire: e.target.checked })} /> {t('workflow.commentaireRefus')}
            </label>
            <div className="cmp-row">
              <button className="secondary" aria-label={t('workflow.monter')} onClick={() => deplacer(i, -1)}>↑</button>
              <button className="secondary" aria-label={t('workflow.descendre')} onClick={() => deplacer(i, 1)}>↓</button>
              <button className="danger" onClick={() => retirerEtape(i)}>{t('workflow.retirer')}</button>
            </div>
          </div>
          <div style={{ marginTop: 8 }}>
            <strong>{t('workflow.conditions')}</strong> <span className="muted">{t('workflow.conditionsAide')}</span>
            {(et.conditions || []).map((cd, ci) => (
              <div className="cmp-row" key={ci} style={{ marginTop: 4 }}>
                <select value={cd.champ} onChange={(e) => majCond(i, ci, { champ: e.target.value })}>
                  <option value="">{t('workflow.champ')}</option>
                  {champs.map((c) => <option key={c.code} value={c.code}>{c.libelle} ({c.code})</option>)}
                </select>
                <select value={cd.operateur} onChange={(e) => majCond(i, ci, { operateur: e.target.value })}>
                  {OPERATEURS.map((o) => <option key={o} value={o}>{t(`operateur.${o}`)}</option>)}
                </select>
                <input value={cd.valeur || ''} placeholder={t('workflow.valeur')} onChange={(e) => majCond(i, ci, { valeur: e.target.value })} />
                <button className="danger" onClick={() => retirerCond(i, ci)}>{t('workflow.retirer')}</button>
              </div>
            ))}
            <button className="secondary" style={{ marginTop: 4 }} onClick={() => ajouterCond(i)}>+ {t('workflow.ajouterCondition')}</button>
          </div>
        </fieldset>
      ))}
    </div>
  )
}
