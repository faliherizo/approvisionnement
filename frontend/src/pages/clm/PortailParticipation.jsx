import { useState } from 'react'
import { useI18n } from '../../i18n/I18nContext'
import { dateHeure, taille } from './commun.jsx'

/** Documents à lire au portail ; chacun est marqué consulté une fois ouvert. */
export function DocumentsPortail({ documents, lus, onOuvrir }) {
  const { t } = useI18n()
  return (
    <ul className="clm-liste" data-zone="03" aria-label={t('clm.po.documents')}>
      {documents.map((d) => (
        <li key={d.id} className="clm-portail__doc">
          <span>{d.nom} <span className="muted">({d.type} · {taille(d.taille)})</span> {lus[d.id] && <span role="img" aria-label={t('clm.po.consulte')}>✔</span>}</span>
          <button type="button" className="secondary" onClick={() => onOuvrir(d)}>{t('clm.po.lire')}</button>
        </li>
      ))}
    </ul>
  )
}

/** Champs attribués au rôle du participant (« signer » ou « compléter »). */
export function ChampsPortail({ champs, valeurs, onChange, legende, aucun }) {
  const { t } = useI18n()
  const maj = (cle) => (e) => onChange({ ...valeurs, [cle]: e.target.value })
  return (
    <fieldset className="form-fieldset" data-zone="05">
      <legend>{legende}</legend>
      {champs.length === 0 && <p className="muted">{aucun}</p>}
      {champs.map((c) => (
        <label key={c.cle}>{c.libelle}{c.obligatoire && <span aria-hidden="true"> *</span>}
          {c.type === 'LISTE' ? (
            <select value={valeurs[c.cle] || ''} onChange={maj(c.cle)} required={c.obligatoire}>
              <option value="">—</option>{c.options.map((o) => <option key={o} value={o}>{o}</option>)}
            </select>
          ) : (
            <input type={c.type === 'DATE' ? 'date' : 'text'} value={valeurs[c.cle] || ''} required={c.obligatoire} aria-required={c.obligatoire}
              onChange={maj(c.cle)} />
          )}
        </label>
      ))}
    </fieldset>
  )
}

/** AF-48 (RST-50) — participant « consulter » : il lit chaque document puis atteste en avoir pris connaissance. */
export function FormulaireConsultation({ vue, lus, onOuvrir, onAttester, occupe }) {
  const { t } = useI18n()
  const [atteste, setAtteste] = useState(false)
  const tousLus = vue.documents.every((d) => lus[d.id])
  const soumettre = (e) => { e.preventDefault(); onAttester() }
  return (
    <form className="panel form" onSubmit={soumettre} aria-labelledby="clm-consulter">
      <h2 id="clm-consulter">{t('clm.po.etapeConsulter')}</h2>
      <p>{t('clm.po.natureConsulter')}</p>
      <DocumentsPortail documents={vue.documents} lus={lus} onOuvrir={onOuvrir} />
      <label className="check-row clm-consentement">
        <input type="checkbox" checked={atteste} onChange={(e) => setAtteste(e.target.checked)} required />
        {t('clm.po.attestationConsultation')}
      </label>
      {!tousLus && <p className="muted">{t('clm.po.lireAvantAttester')}</p>}
      <button type="submit" disabled={occupe || !atteste || !tousLus}>{t('clm.po.attesterConsultation')}</button>
    </form>
  )
}

/** AF-48 (RST-50) — participant « compléter » : il remplit les champs de son rôle et les soumet, sans signer. */
export function FormulaireCompletion({ vue, lus, onOuvrir, onSoumettre, occupe }) {
  const { t } = useI18n()
  const [valeurs, setValeurs] = useState({})
  const soumettre = (e) => { e.preventDefault(); onSoumettre(valeurs) }
  return (
    <form className="panel form" onSubmit={soumettre} aria-labelledby="clm-completer">
      <h2 id="clm-completer">{t('clm.po.etapeCompleter')}</h2>
      <p>{t('clm.po.natureCompleter')}</p>
      <DocumentsPortail documents={vue.documents} lus={lus} onOuvrir={onOuvrir} />
      <ChampsPortail champs={vue.champs} valeurs={valeurs} onChange={setValeurs} legende={t('clm.po.champsACompleter')} aucun={t('clm.po.aucunChampACompleter')} />
      <button type="submit" disabled={occupe}>{t('clm.po.soumettreChamps')}</button>
    </form>
  )
}

/** Reçu de la participation accomplie : signature, consultation attestée ou complétion. */
export function RecuPortail({ recu }) {
  const { t } = useI18n()
  const action = recu.action || 'SIGNATURE'
  return (
    <section className="panel" aria-labelledby="clm-recu" data-zone="06">
      <h2 id="clm-recu">{t(`clm.po.recuAction.${action}`)}</h2>
      <dl className="clm-dl">
        <div><dt>{t('clm.col.reference')}</dt><dd>{recu.reference}</dd></div>
        <div><dt>{action === 'SIGNATURE' ? t('clm.pr.signataire') : t('clm.po.participant')}</dt><dd>{recu.signataire}</dd></div>
        <div><dt>{action === 'SIGNATURE' ? t('clm.pr.signee') : t('clm.po.accomplieLe')}</dt><dd>{dateHeure(recu.signeeLe)}</dd></div>
        <div><dt>{t('clm.col.empreinte')}</dt><dd><code className="clm-casse">{recu.empreinteDocuments}</code></dd></div>
        {recu.attestation && <div><dt>{t('clm.pr.attestation')}</dt><dd><code>{recu.attestation}</code></dd></div>}
      </dl>
      <button type="button" className="secondary" onClick={() => window.print()}>{t('clm.po.imprimer')}</button>
    </section>
  )
}
