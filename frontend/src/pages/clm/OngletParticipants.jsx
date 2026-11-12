import { useEffect, useRef, useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { ChoixListe, Message, Statut, dateHeure, useErreur, useListe, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'
import { libelleAffectation, modeParcours } from './OngletParcours.jsx'

const NIVEAUX = ['', 'COURRIEL', 'CODE_SMS', 'QUESTION', 'FORTE']
const VIDE = { nom: '', courriel: '', telephone: '', roleContrat: '', externe: true, niveauIdentite: '', question: '', reponse: '' }

/** Premier rôle de la liste de l'organisation qu'aucun participant ne tient encore. */
const roleLibre = (roles, participants) => roles.valeurs.map((r) => r.code).find((c) => !participants.some((p) => p.roleContrat === c)) || ''

/** UI-15 — Préparation de la signature : documents, participants, ordre, zones, vérification d'identité, aperçu du parcours. */
export default function OngletParticipants({ trousse, peutGerer, onChange, onOrdre, onLancer }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const premierChamp = useRef(null)
  const premiereZone = useRef(null)
  const roles = useListe('CLM_ROLE_CONTRAT')
  const { donnees: publics } = useRessource('/clm/parametres')
  const [form, setForm] = useState(VIDE)
  const [choixZone, setChoixZone] = useState({})
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const id = trousse.resume.id
  const modele = useRessource(trousse.modeleId ? `/clm/modeles/${trousse.modeleId}` : null, !!trousse.modeleId)
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value })
  const documents = trousse.documents.filter((d) => !['REMPLACE', 'RETIRE'].includes(d.statut))
  const zones = (modele.donnees?.champs || []).filter((c) => c.type === 'SIGNATURE' || (c.roleDestinataire && c.roleDestinataire !== 'INITIATEUR'))
  const parRole = (role) => trousse.participants.find((p) => p.roleContrat === role)
  const phases = [...new Set(trousse.etapes.map((e) => e.phase))].sort((a, b) => a - b)
  const minimum = (cote) => publics?.[cote]?.[trousse.risque.toLowerCase()]

  useEffect(() => {
    setForm((f) => (f.roleContrat ? f : { ...f, roleContrat: roleLibre(roles, trousse.participants) }))
  }, [roles, trousse.participants])

  async function ajouter(e) {
    e.preventDefault()
    setErreur(null)
    try {
      const { data } = await api.post(`/clm/trousses/${id}/participants`, { ...form, niveauIdentite: form.niveauIdentite || null })
      setInfo(t('clm.part.ajoute', { nom: data.nom, niveau: t(`clm.niveau.${data.niveauIdentite}`) }))
      setForm({ ...VIDE, roleContrat: roleLibre(roles, [...trousse.participants, data]) })
      onChange()
    } catch (e2) { setErreur(versErreur(e2)) }
  }
  async function retirer(p) {
    if (!window.confirm(t('clm.part.confirmerRetrait', { nom: p.nom }))) return
    setErreur(null)
    try { await api.delete(`/clm/trousses/${id}/participants/${p.id}`); onChange() } catch (e) { setErreur(versErreur(e)) }
  }
  async function attribuer(zone) {
    setErreur(null)
    try {
      await api.put(`/clm/trousses/${id}/participants/${choixZone[zone.cle]}/role`, { roleContrat: zone.roleDestinataire })
      setInfo(t('clm.part.zoneAttribuee', { zone: zone.libelle || zone.cle }))
      setChoixZone({ ...choixZone, [zone.cle]: undefined })
      onChange()
    } catch (e) { setErreur(versErreur(e)) }
  }
  // Une zone ne se réattribue qu'au brouillon : en cours de parcours, on remplace le signataire de l'étape (Parcours).
  const zonesAttribuables = peutGerer && trousse.resume.statut === 'BROUILLON' && trousse.participants.length > 0

  return (
    <div data-maquette="UI-15">
      <Message erreur={erreur} info={info} />
      <Blocs>
        <Bloc zone="01" titre={t('clm.part.blocDocuments')}>
          {documents.length === 0 ? <p className="muted">{t('clm.doc.aucun')}</p> : (
            <ul className="clm-liste">{documents.map((d) => <li key={d.id}>{d.nom} v{d.version} <Statut valeur={d.statut} /></li>)}</ul>
          )}
        </Bloc>
        <Bloc zone="03" titre={t('clm.part.blocOrdre')}>
          <p><strong>{t(`clm.part.mode.${modeParcours(trousse.etapes)}`)}</strong></p>
          {phases.length === 0 ? <p className="muted">{t('clm.dt.aucuneEtape')}</p> : (
            <ol className="clm-liste">
              {phases.map((p) => <li key={p}>{t('clm.pc.phaseN', { n: p })} : {trousse.etapes.filter((e) => e.phase === p).map((e) => e.libelle).join(' + ')}</li>)}
            </ol>
          )}
        </Bloc>
        <Bloc zone="02" titre={t('clm.part.titre')} large>
          {trousse.participants.length === 0 ? <p className="muted">{t('clm.part.aucun')}</p> : (
            <div className="table-scroll">
              <table className="table clm-table">
                <caption className="sr-only">{t('clm.part.titre')}</caption>
                <thead><tr><th scope="col">{t('clm.col.nom')}</th><th scope="col">{t('clm.part.role')}</th><th scope="col">{t('clm.part.coordonnees')}</th>
                  <th scope="col">{t('clm.part.invitation')}</th>{peutGerer && <th scope="col">{t('clm.col.actions')}</th>}</tr></thead>
                <tbody>
                  {trousse.participants.map((p) => {
                    const inv = [...trousse.invitations].reverse().find((i) => i.participantId === p.id)
                    return (
                      <tr key={p.id}>
                        <td data-label={t('clm.col.nom')}>{p.nom} <span className="muted">#{p.id} · {p.externe ? t('clm.part.externe') : t('clm.part.interne')}</span></td>
                        <td data-label={t('clm.part.role')}>{roles.libelle(p.roleContrat)}</td>
                        <td data-label={t('clm.part.coordonnees')}>{p.courriel}{p.telephone ? <><br />{p.telephone}</> : null}</td>
                        <td data-label={t('clm.part.invitation')}>{inv ? <><Statut valeur={inv.statut} /><br /><span className="muted">{inv.signeeLe ? dateHeure(inv.signeeLe) : t('clm.part.expire', { d: dateHeure(inv.expireLe) })}</span></> : '—'}</td>
                        {peutGerer && <td data-label={t('clm.col.actions')}><button type="button" className="lien danger" aria-label={`${t('clm.retirer')} — ${p.nom}`} onClick={() => retirer(p)}>{t('clm.retirer')}</button></td>}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
          {peutGerer && (
            <form className="form clm-sousform" onSubmit={ajouter} noValidate>
              <h3>{t('clm.part.ajouter')}</h3>
              <div className="form-grid">
                <label>{t('clm.col.nom')} <span aria-hidden="true">*</span><input ref={premierChamp} value={form.nom} onChange={maj('nom')} required aria-required="true" autoComplete="name" /></label>
                <label>{t('clm.part.courriel')} <span aria-hidden="true">*</span><input type="email" value={form.courriel} onChange={maj('courriel')} required aria-required="true" autoComplete="email" /></label>
                <label>{t('clm.part.telephone')} <span className="muted">{t('clm.part.telephoneAide')}</span><input type="tel" value={form.telephone} onChange={maj('telephone')} autoComplete="tel" /></label>
                <label>{t('clm.part.role')} <span className="muted">{t('clm.part.roleAide')}</span>
                  <ChoixListe liste={roles} value={form.roleContrat} onChange={(v) => setForm({ ...form, roleContrat: v })} required aria-required="true" /></label>
                <label className="check-row"><input type="checkbox" checked={form.externe} onChange={maj('externe')} />{t('clm.part.externe')}</label>
                <label>{t('clm.part.niveau')} <span className="muted">{t('clm.part.niveauAide', { risque: t(`clm.risque.${trousse.risque}`) })}</span>
                  <select value={form.niveauIdentite} onChange={maj('niveauIdentite')}>
                    {NIVEAUX.map((n) => <option key={n} value={n}>{n ? t(`clm.niveau.${n}`) : t('clm.part.niveauAuto')}</option>)}
                  </select>
                </label>
                {form.niveauIdentite === 'QUESTION' && (
                  <>
                    <label>{t('clm.part.question')}<input value={form.question} onChange={maj('question')} required /></label>
                    <label>{t('clm.part.reponse')}<input value={form.reponse} onChange={maj('reponse')} required autoComplete="off" /></label>
                  </>
                )}
              </div>
              <div className="cmp-row"><button type="submit">{t('clm.part.ajouter')}</button></div>
            </form>
          )}
        </Bloc>
        <Bloc zone="04" titre={t('clm.part.blocZones')}>
          {!trousse.modeleId ? <p className="muted">{t('clm.part.sansZones')}</p> : zones.length === 0 ? <p className="muted">{t('clm.part.aucuneZone')}</p> : (
            <ul className="clm-liste">
              {zones.map((c, i) => {
                const p = parRole(c.roleDestinataire)
                const nomZone = c.libelle || c.cle
                return (
                  <li key={c.cle}>{nomZone} ({t(`clm.mo.type.${c.type}`)}) → {c.roleDestinataire ? roles.libelle(c.roleDestinataire) : '—'} :{' '}
                    {p ? p.nom : <strong><span aria-hidden="true">⚠</span> {t('clm.part.roleSansParticipant')}</strong>}
                    {zonesAttribuables && c.roleDestinataire && (
                      <div className="cmp-row clm-zone-attribution">
                        <label>{t('clm.part.signataireZone', { zone: nomZone })}
                          <select ref={i === 0 ? premiereZone : undefined} value={choixZone[c.cle] ?? ''}
                            onChange={(e) => setChoixZone({ ...choixZone, [c.cle]: e.target.value })}>
                            <option value="">—</option>
                            {trousse.participants.map((x) => <option key={x.id} value={x.id}>{x.nom} · {roles.libelle(x.roleContrat)}</option>)}
                          </select>
                        </label>
                        <button type="button" className="secondary" disabled={!choixZone[c.cle]} onClick={() => attribuer(c)}>
                          {t('clm.part.attribuerZoneDe', { zone: nomZone })}</button>
                      </div>
                    )}
                  </li>
                )
              })}
            </ul>
          )}
        </Bloc>
        <Bloc zone="05" titre={t('clm.part.blocIdentite')}>
          <p className="muted">{t('clm.part.minimumRisque', { risque: t(`clm.risque.${trousse.risque}`) })}</p>
          {publics && (
            <p>{t('clm.part.minimumPolitique', { externe: t(`clm.niveau.${minimum('identiteExterne')}`), interne: t(`clm.niveau.${minimum('identiteInterne')}`) })}</p>
          )}
          {trousse.participants.length === 0 ? <p className="muted">{t('clm.part.aucun')}</p> : (
            <ul className="clm-liste">{trousse.participants.map((p) => <li key={p.id}>{p.nom} : {t(`clm.niveau.${p.niveauIdentite}`)}</li>)}</ul>
          )}
        </Bloc>
        <Bloc zone="06" titre={t('clm.part.blocApercu')} large>
          {trousse.etapes.length === 0 ? <p className="muted">{t('clm.dt.aucuneEtape')}</p> : (
            <ol className="clm-chrono">
              {trousse.etapes.map((e) => <li key={e.id}>{t('clm.pc.phaseN', { n: e.phase })} · {e.libelle} ({t(`clm.type.${e.type}`)}) — {e.affectations.map((a) => libelleAffectation(a, trousse.participants, t)).join(', ')} <Statut valeur={e.statut} /></li>)}
            </ol>
          )}
        </Bloc>
      </Blocs>
      <BarreActions>
        {peutGerer && <button type="button" className="secondary" onClick={() => premierChamp.current?.focus()}>{t('clm.part.ajouter')}</button>}
        {onOrdre && <button type="button" className="secondary" onClick={onOrdre}>{t('clm.part.reordonner')}</button>}
        {zonesAttribuables && zones.some((c) => c.roleDestinataire) && (
          <button type="button" className="secondary" onClick={() => premiereZone.current?.focus()}>{t('clm.part.attribuerZone')}</button>
        )}
        {onLancer && <button type="button" onClick={onLancer}>{t('clm.part.envoyer')}</button>}
      </BarreActions>
    </div>
  )
}
