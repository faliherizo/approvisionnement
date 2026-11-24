import { useI18n } from '../i18n/I18nContext'

/** Normalise une option : accepte "code" ou { valeur|code, libelle }. */
function normOption(o) {
  if (o == null) return { valeur: '', libelle: '' }
  if (typeof o === 'string') return { valeur: o, libelle: o }
  return { valeur: o.valeur ?? o.code ?? '', libelle: o.libelle ?? o.valeur ?? o.code ?? '', libelleI18n: o.libelleI18n }
}

/**
 * Deux dialectes de structure coexistent : celui du concepteur (code, type en minuscules) et celui de l'API, de l'import
 * et de l'interopérabilité (cle, type en majuscules). Le serveur lit les deux (cle ?? code) ; le rendu aussi.
 */
const TYPES_API = {
  TEXTE: 'texte', ZONE_TEXTE: 'zone_texte', CASE: 'case_a_cocher', CASE_A_COCHER: 'case_a_cocher', RADIO: 'radio',
  LISTE: 'liste', DATE: 'date', COURRIEL: 'courriel', HYPERLIEN: 'hyperlien', NOMBRE: 'nombre', DECIMAL: 'decimale',
  DECIMALE: 'decimale', MONTANT: 'montant', POURCENTAGE: 'pourcentage', CALCULE: 'calcule', FICHIER: 'fichier',
  VALEURS_DYNAMIQUES: 'dynamique', DYNAMIQUE: 'dynamique',
}
export function normaliserChamp(c) {
  const type = String(c.type ?? 'texte')
  return { ...c, code: c.code ?? c.cle, type: TYPES_API[type.toUpperCase()] ?? type.toLowerCase() }
}

/**
 * Rendu dynamique d'un formulaire à partir de sa structure (métadonnées JSON) et de l'état d'évaluation
 * des règles (champs masqués / obligatoires / calculés / erreurs). Aucun champ n'est figé : le type est
 * une donnée de référence, la liste des champs vient de la structure.
 *
 * props : structure {pages:[{code,titre,champs:[...]}]}, valeurs (map code→valeur), etat (ResultatEvaluation),
 *         places (places des choix, CHU 3.06), onChange(code, valeur), lectureSeule.
 */
export default function FormulaireRenderer({ structure, valeurs = {}, etat = {}, places = [], onChange, lectureSeule = false }) {
  const { t, locale } = useI18n()
  // Libellé multilingue : le concepteur peut fournir champ.libelleI18n = {fr, en, mg} ; fallback sur libelle.
  const lang = (locale || 'fr').startsWith('en') ? 'en' : (locale || 'fr').startsWith('mg') ? 'mg' : 'fr'
  // Tout texte de la structure se traduit de même : titre de page (titreI18n), aide (aideI18n), options (optionsI18n,
  // liste parallèle aux valeurs) ; la valeur enregistrée reste celle de la langue de conception.
  const traduit = (objet, cle) => objet?.[`${cle}I18n`]?.[lang] || objet?.[cle]
  const libelle = (c) => traduit(c, 'libelle') || c.code
  const pages = Array.isArray(structure?.pages) ? structure.pages : []
  const masques = new Set(etat.champsMasques || [])
  const requisDynamiques = new Set(etat.champsObligatoires || [])
  const calculs = etat.calculs || {}

  // Un choix complet reste affiché (on comprend pourquoi il manque) mais n'est plus sélectionnable, sauf s'il est déjà retenu.
  const place = (c, valeur) => places.find((p) => p.champ === (c.cle ?? c.code) && p.choix === valeur)
  const indisponible = (c, o, actuelle) => !!place(c, o.valeur)?.complet && String(actuelle) !== String(o.valeur)
  const libelleOption = (c, o) => {
    const p = place(c, o.valeur)
    if (p?.complet) return `${o.libelle} — ${t('rendu.complet')}`
    if (p?.restantes != null) return `${o.libelle} — ${t('rendu.placesRestantes', { n: p.restantes })}`
    return o.libelle
  }

  if (pages.length === 0) return <p className="muted">{t('rendu.aucuneStructure')}</p>

  function valeurChamp(c) {
    if (c.type === 'calcule' && calculs[c.code] != null) return calculs[c.code]
    return valeurs[c.code]
  }

  function champInput(c) {
    const requis = c.obligatoire || requisDynamiques.has(c.code)
    const id = `champ-${c.code}`
    const commun = { id, 'aria-required': requis || undefined, disabled: lectureSeule }
    const v = valeurChamp(c)
    const maj = (val) => onChange && onChange(c.code, val)
    const traductions = c.optionsI18n?.[lang] || []
    const options = Array.isArray(c.options)
      ? c.options.map(normOption).map((o, i) => ({ ...o, libelle: traductions[i] || traduit(o, 'libelle') }))
      : []

    switch (c.type) {
      case 'zone_texte':
        return <textarea {...commun} rows={4} value={v ?? ''} onChange={(e) => maj(e.target.value)} />
      case 'case_a_cocher':
        return <input {...commun} type="checkbox" checked={!!v} onChange={(e) => maj(e.target.checked)} />
      case 'radio':
        return (
          <div role="radiogroup" aria-labelledby={`lbl-${c.code}`}>
            {options.map((o) => (
              <label key={o.valeur} className="radio-inline">
                <input type="radio" name={id} value={o.valeur} checked={String(v) === String(o.valeur)}
                       disabled={lectureSeule || indisponible(c, o, v)} onChange={() => maj(o.valeur)} /> {libelleOption(c, o)}
              </label>
            ))}
          </div>
        )
      case 'liste':
        return (
          <select {...commun} value={v ?? ''} onChange={(e) => maj(e.target.value)}>
            <option value="">{t('rendu.choisir')}</option>
            {options.map((o) => (
              <option key={o.valeur} value={o.valeur} disabled={indisponible(c, o, v)}>{libelleOption(c, o)}</option>
            ))}
          </select>
        )
      case 'date':
        return <input {...commun} type="date" value={v ?? ''} onChange={(e) => maj(e.target.value)} />
      case 'courriel':
        return <input {...commun} type="email" value={v ?? ''} onChange={(e) => maj(e.target.value)} />
      case 'hyperlien':
        return <input {...commun} type="url" value={v ?? ''} onChange={(e) => maj(e.target.value)} />
      case 'nombre': case 'decimale': case 'montant': case 'pourcentage':
        return <input {...commun} type="number" inputMode="decimal" value={v ?? ''} onChange={(e) => maj(e.target.value)} />
      case 'fichier':
        return <input {...commun} type="file" onChange={(e) => maj(e.target.files?.[0]?.name || '')} />
      case 'calcule': case 'dynamique':
        return <input {...commun} type="text" readOnly value={v ?? ''} className="muted" />
      default: // texte + tout type de référence non spécialisé
        return <input {...commun} type="text" value={v ?? ''} onChange={(e) => maj(e.target.value)} />
    }
  }

  return (
    <div className="formulaire-rendu">
      {pages.map((p, i) => (
        <fieldset key={p.code || i} className="panel" style={{ marginBottom: 16 }}>
          <legend><strong>{traduit(p, 'titre') || t('rendu.pageN', { n: i + 1 })}</strong></legend>
          {(p.champs || []).map(normaliserChamp).filter((c) => !masques.has(c.code)).map((c) => {
            const requis = c.obligatoire || requisDynamiques.has(c.code)
            const estCase = c.type === 'case_a_cocher'
            return (
              <div className="form" key={c.code} style={{ marginBottom: 10 }}>
                <label id={`lbl-${c.code}`} htmlFor={`champ-${c.code}`} style={estCase ? { display: 'flex', gap: 8, alignItems: 'center' } : undefined}>
                  {estCase && champInput(c)}
                  <span>{libelle(c)}{requis && <span className="danger" aria-hidden="true"> *</span>}</span>
                </label>
                {!estCase && champInput(c)}
                {traduit(c, 'aide') && <small className="muted">{traduit(c, 'aide')}</small>}
              </div>
            )
          })}
          {(p.champs || []).map(normaliserChamp).filter((c) => !masques.has(c.code)).length === 0 && (
            <p className="muted">{t('rendu.pageVide')}</p>
          )}
        </fieldset>
      ))}
      {(etat.erreurs || []).length > 0 && (
        <div className="erreur" role="alert">
          <strong>{t('rendu.erreursTitre')}</strong>
          <ul>{etat.erreurs.map((m, i) => <li key={i}>{m}</li>)}</ul>
        </div>
      )}
    </div>
  )
}
