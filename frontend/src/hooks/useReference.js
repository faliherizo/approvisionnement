import { useEffect, useState } from 'react'
import api from '../api'

/**
 * Charge les options d'un sélecteur depuis les valeurs de référence dynamiques
 * (globales + spécifiques au tenant courant). Retourne [{ code, libelle }].
 */
export default function useReference(categorie) {
  const [options, setOptions] = useState([])
  useEffect(() => {
    let actif = true
    api.get(`/reference/${categorie}`)
      .then((r) => { if (actif) setOptions(r.data) })
      .catch(() => { if (actif) setOptions([]) })
    return () => { actif = false }
  }, [categorie])
  return options
}
