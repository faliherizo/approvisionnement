import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useI18n } from '../i18n/I18nContext'
import { useModules } from '../hooks/useModules.js'

/** Catalogue « Nouvelle demande » : formulaires publiés que le répondant peut remplir, filtrables par domaine. */
export default function Catalogue() {
  const { t } = useI18n()
  const { actif, chargement } = useModules()
  const [formulaires, setFormulaires] = useState([])
  const [domaines, setDomaines] = useState([])
  const [filtre, setFiltre] = useState('')

  const charger = useCallback(() => {
    const params = filtre ? { domaine: filtre } : {}
    api.get('/catalogue', { params }).then((r) => setFormulaires(r.data || [])).catch(() => setFormulaires([]))
  }, [filtre])
  useEffect(() => { if (!chargement && actif('formulaire')) charger() }, [charger, chargement, actif])
  useEffect(() => { api.get('/reference/DOMAINE').then((r) => setDomaines(r.data || [])).catch(() => setDomaines([])) }, [])

  const libelleDomaine = (code) => domaines.find((d) => d.code === code)?.libelle || code

  if (chargement) return <div className="panel">{t('catalogue.chargement')}</div>
  if (!actif('formulaire')) return <div className="panel">{t('catalogue.moduleInactif')}</div>

  return (
    <div className="panel">
      <h1>{t('catalogue.titre')}</h1>
      <p className="muted">{t('catalogue.intro')}</p>
      <label>{t('catalogue.filtreDomaine')}
        <select value={filtre} onChange={(e) => setFiltre(e.target.value)}>
          <option value="">{t('catalogue.tousDomaines')}</option>
          {domaines.map((d) => <option key={d.code} value={d.code}>{d.libelle}</option>)}
        </select>
      </label>

      <div className="cmp-row" style={{ flexWrap: 'wrap', gap: 12, marginTop: 12 }}>
        {formulaires.map((f) => (
          <div key={f.id} className="panel" style={{ flex: '1 1 260px', minWidth: 240 }}>
            <h2 style={{ marginTop: 0 }}>{f.nom}</h2>
            {f.domaine && <span className="badge">{libelleDomaine(f.domaine)}</span>}
            <p className="muted"><code>{f.code}</code> · v{f.versionCourante}</p>
            {actif('saisie')
              ? <Link className="btn-lien" to={`/formulaires/${f.id}/remplir`}>{t('catalogue.remplir')}</Link>
              : <span className="muted">{t('catalogue.saisieInactive')}</span>}
          </div>
        ))}
        {formulaires.length === 0 && <p className="muted">{t('catalogue.aucun')}</p>}
      </div>
    </div>
  )
}
