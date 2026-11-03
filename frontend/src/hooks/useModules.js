import { useEffect, useState, useCallback } from 'react'
import api from '../api'

/**
 * Modules actifs pour le tenant courant. Sert à afficher/masquer les vues selon l'activation
 * faite par le superadmin. `actif(code)` → booléen ; `chargement` pendant le chargement initial.
 */
export function useModules() {
  const [modules, setModules] = useState([])
  const [chargement, setChargement] = useState(true)

  const charger = useCallback(() => {
    setChargement(true)
    api.get('/modules')
      .then((r) => setModules(r.data || []))
      .catch(() => setModules([]))
      .finally(() => setChargement(false))
  }, [])
  useEffect(() => { charger() }, [charger])

  const actif = useCallback((code) => modules.some((m) => m.code === code && m.actif), [modules])
  return { modules, actif, chargement, recharger: charger }
}
