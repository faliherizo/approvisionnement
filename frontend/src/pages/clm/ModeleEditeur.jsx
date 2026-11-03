import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { EditeurCondition, GardeModule, Message, Statut, dateHeure, useErreur } from './commun.jsx'
import { Bloc, Blocs, EcranClm } from './gabarit.jsx'
import { EditeurParcours, depuisRequete, versRequete } from './OngletParcours.jsx'

const TYPES = ['TEXTE', 'LISTE', 'DATE', 'CASE', 'SIGNATURE']
const CHAMP_VIDE = { cle: '', libelle: '', type: 'TEXTE', obligatoire: false, format: '', options: [], source: '', valeurDefaut: '', conditions: [], roleDestinataire: '', position: null }
const PAS = 1

const nettoyerChamp = (c) => ({ ...c, format: c.format || null, source: c.source || null, valeurDefaut: c.valeurDefaut || null,
  roleDestinataire: c.roleDestinataire || null, options: (c.options || []).filter(Boolean), conditions: (c.conditions || []).filter((x) => x.champ) })

/** Positionnement visuel (ST-05) : page schématique ; la zone sélectionnée se déplace aux flèches (alternative au glisser, WCAG 2.5.7). */
function PagePositions({ champs, choisi, onChoisir, onDeplacer }) {
  const { t } = useI18n()
  const places = champs.map((c, i) => ({ c, i })).filter((x) => x.c.position)
  const clavier = (e, i) => {
    const d = { ArrowLeft: [-PAS, 0], ArrowRight: [PAS, 0], ArrowUp: [0, -PAS], ArrowDown: [0, PAS] }[e.key]
    if (!d) return
    e.preventDefault()
    onDeplacer(i, d[0], d[1])
  }
  return (
    <div>
      <p className="muted" id="clm-page-aide">{t('clm.mo.positionAide')}</p>
      <div className="clm-page" role="group" aria-label={t('clm.mo.pageApercu')} aria-describedby="clm-page-aide">
        {places.map(({ c, i }) => (
          <button key={i} type="button" className={`clm-zone ${choisi === i ? 'is-actif' : ''} ${c.type === 'SIGNATURE' ? 'clm-zone--sig' : ''}`}
            style={{ left: `${c.position.x}%`, top: `${c.position.y}%`, width: `${c.position.largeur}%`, height: `${c.position.hauteur}%` }}
            aria-label={t('clm.mo.zone', { libelle: c.libelle || c.cle, x: c.position.x, y: c.position.y, page: c.position.page })}
            onClick={() => onChoisir(i)} onKeyDown={(e) => clavier(e, i)}>
            <span>{c.libelle || c.cle}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

/** UI-08 / UI-09 — Éditeur de modèle : prévisualisation, champs, clauses et règles, propriétés, version ; parcours par défaut. */
function Contenu() {
  const { id } = useParams()
  const { t } = useI18n()
  const versErreur = useErreur()
  const [m, setM] = useState(null)
  const [choisi, setChoisi] = useState(null)
  const [parcours, setParcours] = useState([])
  const [essai, setEssai] = useState({})
  const [apercu, setApercu] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  const charger = () => api.get(`/clm/modeles/${id}`).then((r) => { setM(r.data); setParcours(depuisRequete(r.data.parcours)) }).catch((e) => setErreur(versErreur(e)))
  useEffect(() => { charger() }, [id]) // eslint-disable-line react-hooks/exhaustive-deps

  if (!m) return <Message erreur={erreur} info={t('clm.chargement')} />
  const modifiable = m.statut === 'BROUILLON'
  const champs = m.champs || []
  const majChamp = (i, k, v) => setM({ ...m, champs: champs.map((c, j) => (j === i ? { ...c, [k]: v } : c)) })
  const majClause = (i, k, v) => setM({ ...m, clauses: m.clauses.map((c, j) => (j === i ? { ...c, [k]: v } : c)) })
  const deplacer = (i, dx, dy) => {
    const p = champs[i].position
    const x = Math.min(100 - p.largeur, Math.max(0, p.x + dx))
    const y = Math.min(100 - p.hauteur, Math.max(0, p.y + dy))
    majChamp(i, 'position', { ...p, x, y })
    setInfo(t('clm.mo.deplace', { x, y }))
  }
  const ajouterChamp = () => { setM({ ...m, champs: [...champs, { ...CHAMP_VIDE, cle: `champ${champs.length + 1}` }] }); setChoisi(champs.length) }
  const positionner = () => {
    if (choisi == null) { setErreur(t('clm.mo.choisirChamp')); return }
    if (!champs[choisi].position) majChamp(choisi, 'position', { page: 1, x: 10, y: 80, largeur: 30, hauteur: 8 })
    setInfo(t('clm.mo.positionAide'))
  }
  const definirCondition = () => {
    if (choisi == null) { setErreur(t('clm.mo.choisirChamp')); return }
    document.getElementById('clm-condition-champ')?.focus()
  }

  async function enregistrer() {
    setErreur(null)
    try {
      const corps = { nom: m.nom, categorie: m.categorie, groupes: m.groupes, corps: m.corps, champs: champs.map(nettoyerChamp),
        clauses: m.clauses.map((c) => ({ ...c, conditions: (c.conditions || []).filter((x) => x.champ) })), parcours: versRequete(parcours), verrou: m.verrou }
      const { data } = await api.put(`/clm/modeles/${id}`, corps)
      setM(data); setParcours(depuisRequete(data.parcours)); setInfo(t('clm.mo.enregistre'))
    } catch (e) { setErreur(versErreur(e)) }
  }
  async function valider() {
    setErreur(null)
    try {
      const { data } = await api.get(`/clm/modeles/${id}/validation`)
      if (data.valide) setInfo(t('clm.mo.valide'))
      else setErreur(data.erreurs.join(' '))
    } catch (e) { setErreur(versErreur(e)) }
  }
  async function previsualiser() {
    setErreur(null)
    try { const { data } = await api.post(`/clm/modeles/${id}/apercu`, essai); setApercu(data) } catch (e) { setErreur(t('clm.mo.apercuIndispo') + ' ' + versErreur(e)) }
  }
  async function soumettre() {
    setErreur(null)
    try { const { data } = await api.post(`/clm/modeles/${id}/soumettre`); setM(data); setInfo(t('clm.mo.ok.soumettre', { nom: data.nom })) } catch (e) { setErreur(versErreur(e)) }
  }

  const c = choisi != null ? champs[choisi] : null

  return (
    <EcranClm maquette="UI-08" titre={<>{m.nom} <span className="muted">v{m.numero}</span></>} objectif={t('clm.mo.objectifEditeur')}
      ariane={<><Link to="/clm/modeles">{t('clm.mo.titre')}</Link> / {m.nom}</>}
      statut={<p className="clm-meta"><Statut valeur={m.statut} /> <span>{m.proprietaire}</span></p>}
      actions={(
        <>
          {modifiable && <button type="button" className="secondary" onClick={ajouterChamp}>{t('clm.mo.ajouterChamp')}</button>}
          {modifiable && <button type="button" className="secondary" onClick={positionner}>{t('clm.mo.positionnerChamp')}</button>}
          {modifiable && <button type="button" className="secondary" onClick={definirCondition}>{t('clm.mo.definirCondition')}</button>}
          <button type="button" className="secondary" onClick={valider}>{t('clm.mo.valider')}</button>
          {modifiable && <button type="button" onClick={enregistrer}>{t('clm.mo.enregistrerBrouillon')}</button>}
          {modifiable && <button type="button" className="secondary" onClick={soumettre}>{t('clm.mo.soumettre')}</button>}
        </>
      )}>
      {!modifiable && <p className="clm-info-statique" role="note">{t('clm.mo.verrouille')}</p>}
      <Message erreur={erreur} info={info} />
      <fieldset disabled={!modifiable} className="clm-sans-bordure clm-editeur">
        <legend className="sr-only">{t('clm.mo.sections')}</legend>
        <div className="clm-editeur__col">
          <Bloc zone="01" titre={t('clm.mo.blocApercu')}>
            <PagePositions champs={champs} choisi={choisi} onChoisir={setChoisi} onDeplacer={deplacer} />
            <label>{t('clm.mo.corps')} <span className="muted">{t('clm.mo.corpsAide')}</span>
              <textarea rows={12} value={m.corps || ''} onChange={(e) => setM({ ...m, corps: e.target.value })} /></label>
          </Bloc>
          <Bloc zone="05" titre={t('clm.mo.blocVersion')}>
            <dl className="clm-dl">
              <div><dt>{t('clm.col.version')}</dt><dd>v{m.numero}</dd></div>
              <div><dt>{t('clm.col.statut')}</dt><dd><Statut valeur={m.statut} /></dd></div>
              <div><dt>{t('clm.col.maj')}</dt><dd>{dateHeure(m.majLe)}</dd></div>
              <div><dt>{t('clm.mo.trousses')}</dt><dd>{m.trousses}</dd></div>
            </dl>
            <div className="form-grid">
              <label>{t('clm.col.nom')}<input value={m.nom} onChange={(e) => setM({ ...m, nom: e.target.value })} required /></label>
              <label>{t('clm.col.categorie')}<input value={m.categorie || ''} onChange={(e) => setM({ ...m, categorie: e.target.value })} /></label>
              <label className="col-2">{t('clm.mo.groupes')} <span className="muted">{t('clm.mo.groupesAide')}</span><input value={m.groupes || ''} onChange={(e) => setM({ ...m, groupes: e.target.value })} /></label>
            </div>
          </Bloc>
        </div>
        <div className="clm-editeur__col">
          <Bloc zone="02" titre={t('clm.mo.blocChamps')}>
            {champs.length === 0 ? <p className="muted">{t('clm.mo.aucunChamp')}</p> : (
              <ul className="clm-liste">
                {champs.map((x, i) => (
                  <li key={i}><button type="button" className={`lien ${choisi === i ? 'is-actif' : ''}`} aria-pressed={choisi === i} onClick={() => setChoisi(i)}>
                    {x.libelle || x.cle || t('clm.mo.sansNom')}</button> <span className="muted">{t(`clm.mo.type.${x.type}`)}{x.obligatoire ? ` · ${t('clm.mo.obligatoire')}` : ''}{x.roleDestinataire ? ` · ${x.roleDestinataire}` : ''}</span></li>
                ))}
              </ul>
            )}
          </Bloc>
          <Bloc zone="04" titre={t('clm.mo.blocProprietes')}>
            {c ? (
              <div className="form">
                <fieldset className="form-fieldset" data-maquette="UI-09" data-zone="01">
                  <legend>{t('clm.mo.zoneNom')}</legend>
                  <div className="form-grid">
                    <label>{t('clm.mo.cle')}<input value={c.cle} onChange={(e) => majChamp(choisi, 'cle', e.target.value)} required /></label>
                    <label>{t('clm.mo.libelle')}<input value={c.libelle} onChange={(e) => majChamp(choisi, 'libelle', e.target.value)} required /></label>
                  </div>
                </fieldset>
                <fieldset className="form-fieldset" data-maquette="UI-09" data-zone="02">
                  <legend>{t('clm.mo.zoneType')}</legend>
                  <div className="form-grid">
                    <label>{t('clm.col.type')}<select value={c.type} onChange={(e) => majChamp(choisi, 'type', e.target.value)}>{TYPES.map((ty) => <option key={ty} value={ty}>{t(`clm.mo.type.${ty}`)}</option>)}</select></label>
                    <label className="check-row"><input type="checkbox" checked={c.obligatoire} onChange={(e) => majChamp(choisi, 'obligatoire', e.target.checked)} />{t('clm.mo.obligatoire')}</label>
                    <label className="col-2">{t('clm.mo.format')} <span className="muted">{t('clm.mo.formatAide')}</span><input value={c.format || ''} onChange={(e) => majChamp(choisi, 'format', e.target.value)} /></label>
                    {c.type === 'LISTE' && <label className="col-2">{t('clm.mo.options')}<input value={(c.options || []).join(', ')} onChange={(e) => majChamp(choisi, 'options', e.target.value.split(',').map((s) => s.trim()))} /></label>}
                  </div>
                </fieldset>
                <fieldset className="form-fieldset" data-maquette="UI-09" data-zone="03">
                  <legend>{t('clm.mo.zoneSource')}</legend>
                  <div className="form-grid">
                    <label>{t('clm.mo.source')} <span className="muted">{t('clm.mo.sourceAide')}</span><input value={c.source || ''} onChange={(e) => majChamp(choisi, 'source', e.target.value)} /></label>
                    <label>{t('clm.mo.defaut')}<input value={c.valeurDefaut || ''} onChange={(e) => majChamp(choisi, 'valeurDefaut', e.target.value)} /></label>
                  </div>
                </fieldset>
                <div data-maquette="UI-09" data-zone="04">
                  <EditeurCondition idChamp="clm-condition-champ" conditions={c.conditions || []} champs={champs.filter((x) => x.cle && x.cle !== c.cle)} onChange={(v) => majChamp(choisi, 'conditions', v)} />
                </div>
                <fieldset className="form-fieldset" data-maquette="UI-09" data-zone="05">
                  <legend>{t('clm.mo.zoneRole')}</legend>
                  <label>{t('clm.mo.role')} <span className="muted">{t('clm.mo.roleAide')}</span><input value={c.roleDestinataire || ''} onChange={(e) => majChamp(choisi, 'roleDestinataire', e.target.value)} /></label>
                  <fieldset className="form-fieldset">
                    <legend>{t('clm.mo.position')}</legend>
                    {c.position ? (
                      <div className="form-grid form-grid--4">
                        {['page', 'x', 'y', 'largeur', 'hauteur'].map((k) => (
                          <label key={k}>{t(`clm.mo.pos.${k}`)}<input type="number" min={k === 'page' ? 1 : 0} max={k === 'page' ? undefined : 100} value={c.position[k]}
                            onChange={(e) => majChamp(choisi, 'position', { ...c.position, [k]: Number(e.target.value) })} /></label>
                        ))}
                        <button type="button" className="secondary" onClick={() => majChamp(choisi, 'position', null)}>{t('clm.mo.sansPosition')}</button>
                      </div>
                    ) : <button type="button" className="secondary" onClick={() => majChamp(choisi, 'position', { page: 1, x: 10, y: 80, largeur: 30, hauteur: 8 })}>{t('clm.mo.positionner')}</button>}
                  </fieldset>
                </fieldset>
                <div className="clm-apercu" aria-label={t('clm.mo.apercuChamp')} role="group" data-maquette="UI-09" data-zone="06">
                  <p className="muted">{t('clm.mo.apercuChamp')}</p>
                  <p><strong>{c.libelle || c.cle}</strong>{c.obligatoire ? ' *' : ''} · {t(`clm.mo.type.${c.type}`)}{c.type === 'LISTE' && c.options?.length ? ` (${c.options.join(' / ')})` : ''}</p>
                </div>
                <button type="button" className="danger" onClick={() => { setM({ ...m, champs: champs.filter((_, j) => j !== choisi) }); setChoisi(null) }}>{t('clm.mo.supprimerChamp')}</button>
              </div>
            ) : <p className="muted">{t('clm.mo.choisirChamp')}</p>}
          </Bloc>
          <Bloc zone="03" titre={t('clm.mo.blocClauses')}>
            {m.clauses.map((cl, i) => (
              <fieldset key={i} className="form-fieldset clm-etape-edit">
                <legend>{cl.titre || cl.cle}</legend>
                <div className="form-grid">
                  <label>{t('clm.mo.cle')}<input value={cl.cle} onChange={(e) => majClause(i, 'cle', e.target.value)} /></label>
                  <label>{t('clm.mo.titreClause')}<input value={cl.titre || ''} onChange={(e) => majClause(i, 'titre', e.target.value)} /></label>
                  <label className="col-2">{t('clm.mo.texte')}<textarea rows={3} value={cl.texte || ''} onChange={(e) => majClause(i, 'texte', e.target.value)} /></label>
                </div>
                <EditeurCondition conditions={cl.conditions || []} champs={champs.filter((x) => x.cle)} onChange={(v) => majClause(i, 'conditions', v)} />
                <button type="button" className="danger" onClick={() => setM({ ...m, clauses: m.clauses.filter((_, j) => j !== i) })}>{t('clm.retirer')}</button>
              </fieldset>
            ))}
            <button type="button" className="secondary" onClick={() => setM({ ...m, clauses: [...m.clauses, { cle: `clause${m.clauses.length + 1}`, titre: '', texte: '', combinateur: 'ET', conditions: [] }] })}>{t('clm.mo.ajouterClause')}</button>
          </Bloc>
        </div>
        <div className="clm-editeur__plein" data-maquette="UI-11">
          <EditeurParcours etapes={parcours} onChange={setParcours} modifiable={modifiable} aideParticipants={t('clm.mo.parcoursAide')}
            champs={champs.filter((x) => x.cle && x.type !== 'SIGNATURE' && (!x.roleDestinataire?.trim() || x.roleDestinataire === 'INITIATEUR'))} />
        </div>
      </fieldset>
      <Blocs>
        <Bloc maquette="UI-09" titre={t('clm.mo.blocTest')} large>
          <p className="muted">{t('clm.mo.essaiAide')}</p>
          <div className="form-grid">
            {champs.filter((x) => x.type !== 'SIGNATURE').map((x) => (
              <label key={x.cle}>{x.libelle || x.cle}<input value={essai[x.cle] || ''} onChange={(e) => setEssai({ ...essai, [x.cle]: e.target.value })} /></label>
            ))}
          </div>
          <div className="cmp-row"><button type="button" onClick={previsualiser}>{t('clm.mo.tester')}</button></div>
          {apercu && (
            <div className="clm-apercu">
              {apercu.erreurs.length > 0 && <ul className="erreur">{apercu.erreurs.map((e) => <li key={e}>{e}</li>)}</ul>}
              <p className="muted">{t('clm.mo.clausesRetenues', { liste: apercu.clausesRetenues.join(', ') || '—' })}</p>
              <pre tabIndex={0}>{apercu.texte}</pre>
            </div>
          )}
        </Bloc>
      </Blocs>
    </EcranClm>
  )
}

export default function ModeleEditeur() {
  return <GardeModule><Contenu /></GardeModule>
}
