import { mkdirSync, writeFileSync } from 'node:fs'

/** Enregistre le bilan d'un test (opérations éprouvées, statuts, écarts) pour le rapport. */
export function consigner(nom: string, bilan: unknown) {
  mkdirSync(new URL('../resultats/', import.meta.url), { recursive: true })
  writeFileSync(new URL(`../resultats/${nom}.json`, import.meta.url), JSON.stringify(bilan, null, 2))
}
