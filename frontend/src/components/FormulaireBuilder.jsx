import { useEffect, useState, useCallback, useRef } from 'react'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import FormulaireRenderer from './FormulaireRenderer.jsx'

const OPERATEURS = ['EGAL', 'DIFFERENT', 'SUP', 'INF', 'SUPEGAL', 'INFEGAL', 'CONTIENT', 'VIDE', 'NON_VIDE']
const TYPES_REGLE = ['VISIBILITE', 'OBLIGATION', 'VALIDATION', 'CALCUL', 'ROUTAGE']

const STRUCTURE_VIDE = { pages: [], regles: [] }

function slug(s) {
  return (s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')
    .replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '').slice(0, 40) || 'champ'
}

/** Éditeur de structure (métadonnées) d'un formulaire : pages, champs, règles, cycle de vie, aperçu. */
/** Places par choix (CHU 3.06), saisies « Atelier=20, Formation=10 » : un choix sans nombre est illimité. */
export function lireCapacites(texte) {
  const capacites = {}
  for (const morceau of texte.split(',')) {
    const [choix, n] = morceau.split('=').map((x) => x.trim())
    if (choix && /^\d+$/.test(n ?? '')) capacites[choix] = Number(n)
  }
  return capacites
}

export function capacitesTexte(capacites) {
  return Object.entries(capacites || {}).map(([choix, n]) => `${choix}=${n}`).join(', ')
}

export default function FormulaireBuilder({ id, onChange }) {
  const { t } = useI18n()
  const [version, setVersion] = useState(null)
  const [structure, setStructure] = useState(STRUCTURE_VIDE)
  const [typesChamp, setTypesChamp] = useState([])
  const [apercu, setApercu] = useState(false)
  const [valeursApercu, setValeursApercu] = useState({})
  const [etatApercu, setEtatApercu] = useState({})
  const [msg, setMsg] = useState(null)
  const [err, setErr] = useState(null)
  const evalTimer = useRef(null)

  const charger = useCallback(() => {
    api.get(`/formulaires/${id}/version`).then((r) => {
      setVersion(r.data)
      try {
        const s = r.data.structureJson ? JSON.parse(r.data.structureJson) : STRUCTURE_VIDE
        setStructure({ pages: s.pages || [], regles: s.regles || [] })
      } catch { setStructure(STRUCTURE_VIDE) }
    }).catch(() => setErr(t('builder.erreurChargement')))
  }, [id, t])

  useEffect(() => { charger() }, [charger])
  useEffect(() => { api.get('/reference/TYPE_CHAMP').then((r) => setTypesChamp(r.data || [])).catch(() => setTypesChamp([])) }, [])

  const publie = version?.statut === 'PUBLIE'
  const lectureSeule = publie // une version publiée est figée : la sauvegarde crée un nouveau brouillon

  function majStructure(prod) {
    setStructure((s) => { const n = prod(structuredClone(s)); return n })
  }

  // ---- Pages ----
  const ajouterPage = () => majStructure((s) => { s.pages.push({ code: `page_${s.pages.length + 1}`, titre: t('builder.pageDefaut', { n: s.pages.length + 1 }), champs: [] }); return s })
  const retirerPage = (i) => majStructure((s) => { s.pages.splice(i, 1); return s })
  const renommerPage = (i, titre) => majStructure((s) => { s.pages[i].titre = titre; return s })

  // ---- Champs ----
  function ajouterChamp(pi) {
    majStructure((s) => {
      const base = slug(t('builder.champDefaut'))
      let code = base; let n = 1
      const existants = new Set(s.pages.flatMap((p) => p.champs.map((c) => c.code)))
      while (existants.has(code)) code = `${base}_${++n}`
      s.pages[pi].champs.push({ code, type: 'texte', libelle: t('builder.champDefaut'), obligatoire: false, aide: '', options: [], classification: 'public' })
      return s
    })
  }
  const majChamp = (pi, ci, patch) => majStructure((s) => { s.pages[pi].champs[ci] = { ...s.pages[pi].champs[ci], ...patch }; return s })
  const retirerChamp = (pi, ci) => majStructure((s) => { s.pages[pi].champs.splice(ci, 1); return s })
  const deplacerChamp = (pi, ci, delta) => majStructure((s) => {
    const arr = s.pages[pi].champs; const j = ci + delta
    if (j < 0 || j >= arr.length) return s
    ;[arr[ci], arr[j]] = [arr[j], arr[ci]]; return s
  })

  // ---- Règles ----
  const ajouterRegle = () => majStructure((s) => { s.regles.push({ type: 'VISIBILITE', combinateur: 'ET', conditions: [{ champ: '', operateur: 'EGAL', valeur: '' }], cible: '', valeur: '' }); return s })
  const majRegle = (ri, patch) => majStructure((s) => { s.regles[ri] = { ...s.regles[ri], ...patch }; return s })
  const retirerRegle = (ri) => majStructure((s) => { s.regles.splice(ri, 1); return s })
  const majCondition = (ri, ci, patch) => majStructure((s) => { s.regles[ri].conditions[ci] = { ...s.regles[ri].conditions[ci], ...patch }; return s })
  const ajouterCondition = (ri) => majStructure((s) => { s.regles[ri].conditions.push({ champ: '', operateur: 'EGAL', valeur: '' }); return s })
  const retirerCondition = (ri, ci) => majStructure((s) => { s.regles[ri].conditions.splice(ci, 1); return s })

  const tousChamps = structure.pages.flatMap((p) => p.champs.map((c) => ({ code: c.code, libelle: c.libelle })))

  // ---- Persistance / cycle de vie ----
  async function enregistrer() {
    setErr(null); setMsg(null)
    try {
      const r = await api.put(`/formulaires/${id}/structure`, { structureJson: JSON.stringify(structure), note: '' })
      setVersion(r.data); setMsg(t('builder.enregistre')); onChange && onChange()
    } catch (e) { setErr(e.response?.data?.message || t('builder.erreurEnregistrement')) }
  }
  async function cycle(action, labelOk) {
    setErr(null); setMsg(null)
    try { const r = await api.post(`/formulaires/${id}/${action}`); setVersion(r.data); setMsg(labelOk); onChange && onChange() }
    catch (e) { setErr(e.response?.data?.message || t('builder.erreurAction')) }
  }

  // ---- Aperçu : évaluation live des règles ----
  function majValeurApercu(code, valeur) {
    setValeursApercu((v) => {
      const next = { ...v, [code]: valeur }
      clearTimeout(evalTimer.current)
      evalTimer.current = setTimeout(() => {
        api.post(`/formulaires/${id}/regles/evaluer`, next).then((r) => setEtatApercu(r.data || {})).catch(() => {})
      }, 300)
      return next
    })
  }

  if (!version) return <div className="panel">{err ? <span className="erreur">{err}</span> : t('builder.chargement')}</div>

  return (
    <div>
      <div className="panel">
        <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap' }}>
          <div>
            <span className="badge">{t('builder.version', { n: version.numero })}</span>{' '}
            <span className={`badge ${publie ? 's-APPROUVE' : ''}`}>{t(`statut.${version.statut}`)}</span>
          </div>
          <div className="cmp-row">
            <button className="secondary" onClick={() => setApercu((a) => !a)} aria-pressed={apercu}>
              {apercu ? t('builder.masquerApercu') : t('builder.apercu')}
            </button>
            <button onClick={enregistrer}>{t('builder.enregistrer')}</button>
            {!publie && <button className="secondary" onClick={() => cycle('soumettre', t('builder.soumis'))}>{t('builder.soumettre')}</button>}
            {!publie && <button onClick={() => cycle('publier', t('builder.publie'))}>{t('builder.publier')}</button>}
          </div>
        </div>
        {publie && <p className="muted">{t('builder.publieNote')}</p>}
        {msg && <div className="erreur" style={{ background: '#d9f5e3', color: '#1a7a44' }}>{msg}</div>}
        {err && <div className="erreur">{err}</div>}
      </div>

      {apercu ? (
        <div className="panel">
          <h3>{t('builder.apercuTitre')}</h3>
          <FormulaireRenderer structure={structure} valeurs={valeursApercu} etat={etatApercu} onChange={majValeurApercu} />
        </div>
      ) : (
        <>
          <div className="panel">
            <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>{t('builder.pages')}</h3>
              <button className="secondary" disabled={lectureSeule} onClick={ajouterPage}>+ {t('builder.ajouterPage')}</button>
            </div>
            {structure.pages.length === 0 && <p className="muted">{t('builder.aucunePage')}</p>}
            {structure.pages.map((p, pi) => (
              <fieldset key={pi} className="panel" style={{ marginTop: 12 }}>
                <legend>
                  <input aria-label={t('builder.titrePage')} value={p.titre} disabled={lectureSeule}
                         onChange={(e) => renommerPage(pi, e.target.value)} />
                  <input aria-label={t('builder.titrePageEn')} placeholder={t('builder.titrePageEn')} value={p.titreI18n?.en || ''}
                         disabled={lectureSeule} style={{ marginLeft: 8 }}
                         onChange={(e) => majStructure((s) => { s.pages[pi].titreI18n = { ...(s.pages[pi].titreI18n || {}), en: e.target.value }; return s })} />
                  <button className="danger" disabled={lectureSeule} style={{ marginLeft: 8 }} onClick={() => retirerPage(pi)}>{t('builder.retirerPage')}</button>
                </legend>
                <table className="table">
                  <thead><tr>
                    <th scope="col">{t('builder.colLibelle')}</th>
                    <th scope="col">{t('builder.colCode')}</th>
                    <th scope="col">{t('builder.colType')}</th>
                    <th scope="col">{t('builder.colOptions')}</th>
                    <th scope="col">{t('builder.colObligatoire')}</th>
                    <th scope="col"><span className="sr-only">{t('builder.colActions')}</span></th>
                  </tr></thead>
                  <tbody>
                    {p.champs.map((c, ci) => (
                      <tr key={ci}>
                        <td>
                          <input value={c.libelle} disabled={lectureSeule} aria-label={t('builder.colLibelle')} onChange={(e) => majChamp(pi, ci, { libelle: e.target.value })} />
                          <input value={c.libelleI18n?.en || ''} disabled={lectureSeule} placeholder={t('builder.libelleEn')} aria-label={t('builder.libelleEn')} style={{ marginTop: 4 }}
                                 onChange={(e) => majChamp(pi, ci, { libelleI18n: { ...(c.libelleI18n || {}), en: e.target.value } })} />
                        </td>
                        <td><input value={c.code} disabled={lectureSeule} style={{ width: 110 }} onChange={(e) => majChamp(pi, ci, { code: slug(e.target.value) })} /></td>
                        <td>
                          <select value={c.type} disabled={lectureSeule} onChange={(e) => majChamp(pi, ci, { type: e.target.value })}>
                            {typesChamp.map((tp) => <option key={tp.code} value={tp.code}>{tp.libelle}</option>)}
                          </select>
                        </td>
                        <td>
                          {(c.type === 'liste' || c.type === 'radio') ? (
                            <>
                              <input value={(c.options || []).join(', ')} disabled={lectureSeule}
                                     aria-label={t('builder.optionsLabel', { champ: c.libelle })}
                                     placeholder={t('builder.optionsPlaceholder')}
                                     onChange={(e) => majChamp(pi, ci, { options: e.target.value.split(',').map((x) => x.trim()).filter(Boolean) })} />
                              <input value={(c.optionsI18n?.en || []).join(', ')} disabled={lectureSeule}
                                     aria-label={t('builder.optionsEnLabel', { champ: c.libelle })}
                                     placeholder={t('builder.optionsEnPlaceholder')}
                                     onChange={(e) => majChamp(pi, ci, { optionsI18n: { ...(c.optionsI18n || {}), en: e.target.value.split(',').map((x) => x.trim()) } })} />
                              <input value={capacitesTexte(c.capacites)} disabled={lectureSeule}
                                     aria-label={t('builder.capacitesLabel', { champ: c.libelle })}
                                     placeholder={t('builder.capacitesPlaceholder')}
                                     onChange={(e) => majChamp(pi, ci, { capacites: lireCapacites(e.target.value) })} />
                            </>
                          ) : <span className="muted">—</span>}
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          <input type="checkbox" checked={!!c.obligatoire} disabled={lectureSeule}
                                 aria-label={t('builder.colObligatoire')}
                                 onChange={(e) => majChamp(pi, ci, { obligatoire: e.target.checked })} />
                        </td>
                        <td className="cmp-row">
                          <button className="secondary" disabled={lectureSeule} aria-label={t('builder.monter')} onClick={() => deplacerChamp(pi, ci, -1)}>↑</button>
                          <button className="secondary" disabled={lectureSeule} aria-label={t('builder.descendre')} onClick={() => deplacerChamp(pi, ci, 1)}>↓</button>
                          <button className="danger" disabled={lectureSeule} onClick={() => retirerChamp(pi, ci)}>{t('builder.retirer')}</button>
                        </td>
                      </tr>
                    ))}
                    {p.champs.length === 0 && <tr><td colSpan="6" className="muted">{t('builder.aucunChamp')}</td></tr>}
                  </tbody>
                </table>
                <button className="secondary" disabled={lectureSeule} onClick={() => ajouterChamp(pi)}>+ {t('builder.ajouterChamp')}</button>
              </fieldset>
            ))}
          </div>

          <div className="panel">
            <div className="cmp-row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>{t('builder.regles')}</h3>
              <button className="secondary" disabled={lectureSeule} onClick={ajouterRegle}>+ {t('builder.ajouterRegle')}</button>
            </div>
            <p className="muted">{t('builder.reglesAide')}</p>
            {structure.regles.length === 0 && <p className="muted">{t('builder.aucuneRegle')}</p>}
            {structure.regles.map((r, ri) => (
              <fieldset key={ri} className="panel" style={{ marginTop: 12 }}>
                <legend>{t('builder.regleN', { n: ri + 1 })}</legend>
                <div className="cmp-row" style={{ flexWrap: 'wrap' }}>
                  <label>{t('builder.regleType')}
                    <select value={r.type} disabled={lectureSeule} onChange={(e) => majRegle(ri, { type: e.target.value })}>
                      {TYPES_REGLE.map((x) => <option key={x} value={x}>{t(`regleType.${x}`)}</option>)}
                    </select>
                  </label>
                  <label>{t('builder.regleCombinateur')}
                    <select value={r.combinateur} disabled={lectureSeule} onChange={(e) => majRegle(ri, { combinateur: e.target.value })}>
                      <option value="ET">{t('builder.combinateurET')}</option>
                      <option value="OU">{t('builder.combinateurOU')}</option>
                    </select>
                  </label>
                  <label>{t('builder.regleCible')}
                    <select value={r.cible} disabled={lectureSeule} onChange={(e) => majRegle(ri, { cible: e.target.value })}>
                      <option value="">{t('builder.cibleChoisir')}</option>
                      {tousChamps.map((c) => <option key={c.code} value={c.code}>{c.libelle} ({c.code})</option>)}
                    </select>
                  </label>
                  <label>{t('builder.regleValeur')}
                    <input value={r.valeur || ''} disabled={lectureSeule} placeholder={t('builder.regleValeurAide')}
                           onChange={(e) => majRegle(ri, { valeur: e.target.value })} />
                  </label>
                  <button className="danger" disabled={lectureSeule} onClick={() => retirerRegle(ri)}>{t('builder.retirerRegle')}</button>
                </div>
                <div style={{ marginTop: 8 }}>
                  <strong>{t('builder.conditions')}</strong>
                  {(r.conditions || []).map((cd, ci) => (
                    <div className="cmp-row" key={ci} style={{ marginTop: 4 }}>
                      <select value={cd.champ} disabled={lectureSeule} onChange={(e) => majCondition(ri, ci, { champ: e.target.value })}>
                        <option value="">{t('builder.cibleChoisir')}</option>
                        {tousChamps.map((c) => <option key={c.code} value={c.code}>{c.libelle} ({c.code})</option>)}
                      </select>
                      <select value={cd.operateur} disabled={lectureSeule} onChange={(e) => majCondition(ri, ci, { operateur: e.target.value })}>
                        {OPERATEURS.map((o) => <option key={o} value={o}>{t(`operateur.${o}`)}</option>)}
                      </select>
                      <input value={cd.valeur || ''} disabled={lectureSeule} placeholder={t('builder.valeurCondition')}
                             onChange={(e) => majCondition(ri, ci, { valeur: e.target.value })} />
                      <button className="danger" disabled={lectureSeule} onClick={() => retirerCondition(ri, ci)}>{t('builder.retirer')}</button>
                    </div>
                  ))}
                  <button className="secondary" disabled={lectureSeule} style={{ marginTop: 4 }} onClick={() => ajouterCondition(ri)}>+ {t('builder.ajouterCondition')}</button>
                </div>
              </fieldset>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
