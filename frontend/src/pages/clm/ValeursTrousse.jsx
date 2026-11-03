import { useEffect, useRef, useState } from 'react'
import api from '../../api'
import { useI18n } from '../../i18n/I18nContext'
import { Message, dateHeure, useErreur } from './commun.jsx'
import { Bloc, Blocs } from './gabarit.jsx'

/** Saisie selon le type du champ : liste et case par choix fermé, date au calendrier, texte libre au format du modèle. */
function SaisieValeur({ champ, valeur, onChange, champRef }) {
  const { t } = useI18n()
  const maj = (e) => onChange(e.target.value)
  if (champ.type === 'LISTE' || champ.type === 'CASE') {
    const options = champ.type === 'LISTE' ? champ.options.map((o) => [o, o]) : [['true', t('clm.oui')], ['false', t('clm.non')]]
    return (
      <select ref={champRef} value={valeur} onChange={maj}>
        <option value="">—</option>
        {options.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
      </select>
    )
  }
  return <input ref={champRef} type={champ.type === 'DATE' ? 'date' : 'text'} value={valeur} onChange={maj} autoComplete="off" />
}

/**
 * Données de la trousse (UI-10, zones 03 à 05) : correspondance des champs, valeurs avec provenance et date de
 * récupération, correction contrôlée par l'initiateur (F-04), champs manquants ou en conflit.
 */
export default function ValeursTrousse({ trousse, conflits, modifiable, onChange }) {
  const { t } = useI18n()
  const versErreur = useErreur()
  const [edition, setEdition] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [info, setInfo] = useState('')
  const champRef = useRef(null)
  const champs = trousse.champs || []
  const libelle = (cle) => champs.find((c) => c.cle === cle)?.libelle || cle
  const enCours = edition && champs.find((c) => c.cle === edition.cle)

  useEffect(() => { if (edition) champRef.current?.focus() }, [edition?.cle]) // eslint-disable-line react-hooks/exhaustive-deps

  async function enregistrer(e) {
    e.preventDefault()
    setErreur(null)
    try {
      const { data } = await api.put(`/clm/trousses/${trousse.resume.id}/valeurs`, { valeurs: { [edition.cle]: edition.valeur } })
      setInfo(t('clm.va.corrigee', { champ: libelle(edition.cle) }))
      setEdition(null)
      onChange(data)
    } catch (x) { setErreur(versErreur(x)) }
  }

  const sansModele = <p className="muted">{t('clm.va.sansModele')}</p>
  const ecarts = conflits || []
  return (
    <Blocs>
      {trousse.resume.source === 'SALESFORCE' && (
        <Bloc maquette="UI-10" zone="03" niveau={3} titre={t('clm.sf.blocCorrespondance')} large>
          {champs.length === 0 ? sansModele : (
            <div className="table-scroll">
              <table className="table clm-table">
                <thead><tr><th scope="col">{t('clm.sf.cible')}</th><th scope="col">{t('clm.sf.source')}</th></tr></thead>
                <tbody>
                  {champs.map((c) => (
                    <tr key={c.cle}>
                      <td data-label={t('clm.sf.cible')}>{c.libelle}</td>
                      <td data-label={t('clm.sf.source')}>{c.source || t('clm.sf.mappingAbsent')}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Bloc>
      )}
      <Bloc maquette="UI-10" zone="04" niveau={3} titre={t('clm.sf.blocValeurs')} large>
        <Message erreur={erreur} info={info} />
        {champs.length === 0 ? sansModele : (
          <>
            <div className="table-scroll">
              <table className="table clm-table">
                <thead><tr><th scope="col">{t('clm.sf.cible')}</th><th scope="col">{t('clm.col.valeur')}</th><th scope="col">{t('clm.sf.provenance')}</th>
                  <th scope="col">{t('clm.va.recupereLe')}</th>{modifiable && <th scope="col">{t('clm.col.actions')}</th>}</tr></thead>
                <tbody>
                  {champs.map((c) => {
                    const v = trousse.valeurs[c.cle]
                    return (
                      <tr key={c.cle}>
                        <td data-label={t('clm.sf.cible')}>{c.libelle}{c.obligatoire && <span aria-hidden="true"> *</span>}</td>
                        <td data-label={t('clm.col.valeur')}>{v ? String(v.valeur) : '—'}</td>
                        <td data-label={t('clm.sf.provenance')}>{v ? t(`clm.provenance.${v.provenance}`) : t('clm.sf.manquant')}</td>
                        <td data-label={t('clm.va.recupereLe')}>{v?.recupereLe ? dateHeure(v.recupereLe) : '—'}</td>
                        {modifiable && (
                          <td data-label={t('clm.col.actions')}>
                            <button type="button" className="secondary" onClick={() => setEdition({ cle: c.cle, valeur: v ? String(v.valeur) : '' })}>
                              {t('clm.va.corriger', { champ: c.libelle })}</button>
                          </td>
                        )}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
            {enCours && (
              <form className="form clm-sousform" onSubmit={enregistrer}>
                <fieldset className="form-fieldset">
                  <legend>{t('clm.va.corriger', { champ: enCours.libelle })}</legend>
                  <label>{t('clm.va.nouvelle', { champ: enCours.libelle })}
                    {enCours.format && <span className="muted"> {t('clm.va.formatAttendu', { format: enCours.format })}</span>}
                    <SaisieValeur champ={enCours} valeur={edition.valeur} champRef={champRef} onChange={(valeur) => setEdition({ ...edition, valeur })} />
                  </label>
                  <p className="muted">{t('clm.va.aide')}</p>
                  <div className="cmp-row">
                    <button type="submit">{t('clm.enregistrer')}</button>
                    <button type="button" className="secondary" onClick={() => setEdition(null)}>{t('clm.annuler')}</button>
                  </div>
                </fieldset>
              </form>
            )}
          </>
        )}
      </Bloc>
      <Bloc maquette="UI-10" zone="05" niveau={3} titre={t('clm.sf.blocManquants')} large>
        {trousse.manquants.length === 0 && ecarts.length === 0 ? <p>{t('clm.va.aucunManquant')}</p> : (
          <ul className="clm-liste">
            {trousse.manquants.length > 0 && <li><span aria-hidden="true">⚠</span> {t('clm.sf.manquants', { liste: trousse.manquants.map(libelle).join(', ') })}</li>}
            {ecarts.map((c) => (
              <li key={c.cle}><span aria-hidden="true">≠</span> {t('clm.sf.conflit', { cle: libelle(c.cle), source: c.valeurSource, manuelle: c.valeurManuelle })}</li>
            ))}
          </ul>
        )}
      </Bloc>
    </Blocs>
  )
}
