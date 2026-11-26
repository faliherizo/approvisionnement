import { useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { Message, Statut, dateHeure, useErreur, useRessource } from './commun.jsx'
import { BarreActions, Bloc, Blocs } from './gabarit.jsx'
import { ClausesVersion, ComparaisonVersions } from './NegociationCommun.jsx'

const FINAUX = ['CLOSE', 'ANNULEE']
const OUVERTURE = { nom: '', courriel: '', entreprise: '', langue: 'FR' }

/**
 * RST-55 (IA-05) — Négociation assistée avec un tiers, côté organisation : ouverture du cycle, propositions de la contrepartie
 * clause par clause, suggestion encadrée (IA locale ou règles), décision humaine, versions comparées et clôture.
 */
export default function OngletNegociation({ trousseId, peutOuvrir, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const { donnees, erreur: errChargement, recharger } = useRessource(`/clm/trousses/${trousseId}/negociations`)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')

  async function agir(appel, ok) {
    setErreur(null)
    try {
      await appel()
      setInfo(ok)
      recharger()
      onChange?.()
      return true
    } catch (e) {
      setErreur(versErreur(e))
      return false
    }
  }

  if (!donnees) return <Message erreur={errChargement} info={t('clm.chargement')} onReessayer={recharger} />
  const courante = donnees.find((n) => !FINAUX.includes(n.etat))
  const passees = donnees.filter((n) => n !== courante)
  return (
    <div data-maquette="RST-55">
      <Message erreur={erreur} info={info} />
      {courante && <Cycle n={courante} agir={agir} onErreur={(e) => setErreur(versErreur(e))} />}
      {!courante && peutOuvrir && <Ouverture trousseId={trousseId} agir={agir} />}
      {!courante && !peutOuvrir && <p className="muted">{t('clm.ng.aucuneOuverte')}</p>}
      {passees.length > 0 && <Passees cycles={passees} />}
    </div>
  )
}

function Ouverture({ trousseId, agir }) {
  const { t } = useI18n()
  const [form, setForm] = useState(OUVERTURE)
  const maj = (k) => (e) => setForm({ ...form, [k]: e.target.value })
  async function ouvrir(e) {
    e.preventDefault()
    if (await agir(() => api.post(`/clm/trousses/${trousseId}/negociations`, form), t('clm.ng.ouverte'))) setForm(OUVERTURE)
  }
  return (
    <Bloc titre={t('clm.ng.ouvrirTitre')} large>
      <p>{t('clm.ng.ouvrirAide')}</p>
      <form className="form clm-sousform" onSubmit={ouvrir} aria-label={t('clm.ng.ouvrirTitre')}>
        <div className="form-grid">
          <label>{t('clm.ng.contrepartie')} <span aria-hidden="true">*</span>
            <input value={form.nom} onChange={maj('nom')} required aria-required="true" autoComplete="organization" /></label>
          <label>{t('clm.ng.courriel')} <span aria-hidden="true">*</span>
            <input type="email" value={form.courriel} onChange={maj('courriel')} required aria-required="true" autoComplete="email" /></label>
          <label>{t('clm.ng.entreprise')}<input value={form.entreprise} onChange={maj('entreprise')} maxLength={200} /></label>
          <label>{t('clm.ng.langue')}
            <select value={form.langue} onChange={maj('langue')}>
              {['FR', 'EN'].map((l) => <option key={l} value={l}>{t(`clm.part.langue.${l}`)}</option>)}
            </select></label>
        </div>
        <button type="submit">{t('clm.ng.ouvrir')}</button>
      </form>
    </Bloc>
  )
}

function Cycle({ n, agir, onErreur }) {
  const { t } = useI18n()
  const peut = (a) => n.actions.includes(a)
  const enAttente = n.propositions.filter((p) => p.tour === n.tour && p.decision === 'EN_ATTENTE')
  const comparer = (de, a) => api.get(`/clm/negociations/${n.id}/comparaison`, { params: { de, a } }).then((r) => r.data)
  const suggerer = () => agir(() => api.post(`/clm/negociations/${n.id}/suggestions`), t('clm.ng.suggestionsObtenues'))
  const clore = () => window.confirm(t('clm.ng.confirmerCloture'))
    && agir(() => api.post(`/clm/negociations/${n.id}/cloture`), t('clm.ng.close'))
  const annuler = () => {
    const motif = window.prompt(t('clm.ng.motifAnnulation'))
    if (motif) agir(() => api.post(`/clm/negociations/${n.id}/annulation`, { motif }), t('clm.ng.annulee'))
  }
  return (
    <>
      <Blocs>
        <Bloc titre={t('clm.ng.cycleAvec', { nom: n.contrepartie })}>
          <dl className="clm-dl">
            <div><dt>{t('clm.col.statut')}</dt><dd><Statut valeur={n.etat} /></dd></div>
            <div><dt>{t('clm.ng.versionCourante')}</dt><dd>v{n.versionCourante} · {t('clm.ng.tourN', { n: n.tour })}</dd></div>
            <div><dt>{t('clm.ng.courriel')}</dt><dd>{n.courriel}{n.entreprise ? ` · ${n.entreprise}` : ''}</dd></div>
            <div><dt>{t('clm.ng.lienExpire')}</dt><dd>{dateHeure(n.expireLe)}</dd></div>
          </dl>
          <p className="muted">{t(`clm.ng.etatAide.${n.etat}`)}</p>
        </Bloc>
        <Bloc titre={t('clm.ng.clausesVersion', { v: n.versionCourante })}>
          <ClausesVersion clauses={n.clauses} />
        </Bloc>
      </Blocs>
      {enAttente.length > 0 && <DecisionsTour n={n} enAttente={enAttente} agir={agir} onSuggerer={peut('SUGGERER') ? suggerer : null} peutDecider={peut('DECIDER')} />}
      <Blocs>
        <Bloc titre={t('clm.ng.comparerTitre')}>
          <ComparaisonVersions versions={n.versions.map((v) => v.numero)} charger={comparer} onErreur={onErreur} />
        </Bloc>
        <Bloc titre={t('clm.ng.versions')}>
          <ul className="clm-liste">
            {n.versions.map((v) => (
              <li key={v.numero}><strong>v{v.numero}</strong> · {v.motif} · {v.auteur} · {dateHeure(v.creeLe)}
                <br /><code className="muted" title={v.empreinte}>{v.empreinte.slice(0, 20)}…</code> {v.integre ? t('clm.ng.integre') : t('clm.ng.altere')}</li>
            ))}
          </ul>
        </Bloc>
      </Blocs>
      <Echanges propositions={n.propositions} />
      {(peut('CLORE') || peut('ANNULER')) && (
        <BarreActions libelle={t('clm.ng.actions')}>
          {peut('CLORE') && <button type="button" onClick={clore}>{t('clm.ng.clore')}</button>}
          {peut('ANNULER') && <button type="button" className="danger" onClick={annuler}>{t('clm.ng.annuler')}</button>}
        </BarreActions>
      )}
    </>
  )
}

/** Décisions du tour : chaque proposition est acceptée, refusée (motif) ou contre-proposée (texte), par une personne. */
function DecisionsTour({ n, enAttente, agir, onSuggerer, peutDecider }) {
  const { t } = useI18n()
  const [choix, setChoix] = useState({})
  const maj = (id, k) => (e) => setChoix({ ...choix, [id]: { ...choix[id], [k]: e.target.value } })
  async function enregistrer(e) {
    e.preventDefault()
    const decisions = enAttente.map((p) => ({ propositionId: p.id, decision: choix[p.id]?.decision || '', texte: choix[p.id]?.texte,
      justification: choix[p.id]?.justification }))
    if (await agir(() => api.post(`/clm/negociations/${n.id}/decisions`, { decisions }), t('clm.ng.decisionsEnregistrees'))) setChoix({})
  }
  return (
    <Bloc titre={t('clm.ng.propositionsTour', { n: n.tour })} large>
      <p>{t('clm.ng.decisionHumaine')}</p>
      {onSuggerer && <p><button type="button" className="secondary" onClick={onSuggerer}>{t('clm.ng.suggerer')}</button></p>}
      <form className="form" onSubmit={enregistrer} aria-label={t('clm.ng.propositionsTour', { n: n.tour })}>
        {enAttente.map((p) => (
          <fieldset key={p.id} className="form-fieldset">
            <legend>{p.titre}</legend>
            <p className="muted">{t('clm.ng.texteEnVigueur')}</p><p className="clm-diff__texte">{p.texteBase}</p>
            <p className="muted">{t('clm.ng.texteProposeParTiers')}</p><p className="clm-diff__texte"><strong>{p.textePropose}</strong></p>
            {p.commentaire && <p>{t('clm.ng.commentaireTiers', { c: p.commentaire })}</p>}
            {p.suggestion && <Suggestion s={p.suggestion} />}
            {peutDecider && (
              <div className="form-grid">
                <label>{t('clm.ng.decisionPour', { clause: p.titre })}
                  <select value={choix[p.id]?.decision || ''} onChange={maj(p.id, 'decision')} required>
                    <option value="">—</option>
                    {['ACCEPTEE', 'REFUSEE', 'CONTRE_PROPOSEE'].map((d) => <option key={d} value={d}>{t(`clm.ng.decision.${d}`)}</option>)}
                  </select></label>
                {choix[p.id]?.decision === 'CONTRE_PROPOSEE' && (
                  <label>{t('clm.ng.contrePropositionPour', { clause: p.titre })}
                    <textarea rows={3} value={choix[p.id]?.texte || ''} onChange={maj(p.id, 'texte')} required /></label>
                )}
                <label>{t('clm.ng.motifPour', { clause: p.titre })} {choix[p.id]?.decision !== 'REFUSEE' && <span className="muted">{t('clm.ng.facultatif')}</span>}
                  <input value={choix[p.id]?.justification || ''} onChange={maj(p.id, 'justification')} required={choix[p.id]?.decision === 'REFUSEE'} maxLength={2000} /></label>
              </div>
            )}
          </fieldset>
        ))}
        {peutDecider && <button type="submit">{t('clm.ng.enregistrerDecisions')}</button>}
      </form>
    </Bloc>
  )
}

/** Suggestion d'aide à la décision : elle dit sa source et n'est jamais appliquée d'office. */
function Suggestion({ s }) {
  const { t } = useI18n()
  return (
    <div className="clm-carte clm-suggestion" role="note" aria-label={t('clm.ng.suggestion')}>
      <p><strong>{t('clm.ng.suggestion')} : {t(`clm.ng.position.${s.position}`)}</strong> <span className="muted">· {s.source}</span></p>
      <p className="clm-diff__texte">{s.resume}</p>
      <p className="muted">{t('clm.ng.suggestionAvertissement')}</p>
    </div>
  )
}

/** Tous les échanges de la négociation, tour par tour. */
function Echanges({ propositions }) {
  const { t } = useI18n()
  if (propositions.length === 0) return null
  return (
    <Bloc titre={t('clm.ng.echanges')} large>
      <div className="table-scroll">
        <table className="table clm-table">
          <caption className="sr-only">{t('clm.ng.echanges')}</caption>
          <thead><tr><th scope="col">{t('clm.ng.tour')}</th><th scope="col">{t('clm.ng.clause')}</th><th scope="col">{t('clm.ng.propose')}</th>
            <th scope="col">{t('clm.ng.decisionCol')}</th></tr></thead>
          <tbody>
            {propositions.map((p) => (
              <tr key={p.id}>
                <td data-label={t('clm.ng.tour')}>{p.tour}</td>
                <td data-label={t('clm.ng.clause')}>{p.titre}</td>
                <td data-label={t('clm.ng.propose')}>{p.textePropose}{p.commentaire ? <><br /><span className="muted">{p.commentaire}</span></> : null}</td>
                <td data-label={t('clm.ng.decisionCol')}><Statut valeur={p.decision} />
                  {p.texteContreProposition && <><br />{p.texteContreProposition}</>}
                  {p.justification && <><br /><span className="muted">{p.justification}</span></>}
                  {p.decidePar && <><br /><span className="muted">{p.decidePar} · {dateHeure(p.decideLe)}</span></>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Bloc>
  )
}

function Passees({ cycles }) {
  const { t } = useI18n()
  return (
    <Bloc titre={t('clm.ng.cyclesPasses')} large>
      <ul className="clm-liste">
        {cycles.map((n) => (
          <li key={n.id}><Statut valeur={n.etat} /> {n.contrepartie} · v{n.versionCourante} · {dateHeure(n.termineeLe)}{n.motifFin ? ` · ${n.motifFin}` : ''}</li>
        ))}
      </ul>
    </Bloc>
  )
}
