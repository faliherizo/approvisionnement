import { readFileSync, readdirSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import type { Cas } from './cas'

/** Données de test (bdd/donnees/*.json) : comptes, écrans et cas, réunis en un seul registre sans doublon. */

export type Compte = { email: string; motDePasse: string; totpSecret?: string; reponseSecrete?: string }
export type Ecran = { chemin: string; titre: string; largeur?: number }
export type Organisation = { slug: string; titre: string; admin: Compte & { nom: string }; modules: string[] }

const DOSSIER = resolve(dirname(fileURLToPath(import.meta.url)), '../donnees')
const lireJson = (f: string) => JSON.parse(readFileSync(join(DOSSIER, f), 'utf8'))

const base = lireJson('comptes.json')
// E2E_COMPTES=v3r : les mêmes parcours joués par les comptes d'une autre organisation (vidéos de sa vitrine) ; chaque
// profil de la table correspondante remplace le sien, les autres restent.
const substitution = process.env.E2E_COMPTES ? lireJson(`comptes-${process.env.E2E_COMPTES}.json`).comptes : {}
export const comptes: Record<string, Compte> = { ...base.comptes, ...substitution }
export const organisations: Record<string, Organisation> = base.organisations ?? {}
export const cas: Record<string, Cas> = {}
export const ecrans: Record<string, Ecran> = {}

for (const f of readdirSync(DOSSIER).filter((x) => x.endsWith('.json') && !x.startsWith('comptes')).sort()) {
  const d = lireJson(f)
  for (const [k, v] of Object.entries<Cas>(d.cas ?? {})) {
    if (k in cas) throw new Error(`Cas « ${k} » défini deux fois (${f})`)
    cas[k] = v
  }
  for (const [k, v] of Object.entries<Ecran>(d.ecrans ?? {})) {
    if (k in ecrans) throw new Error(`Écran « ${k} » défini deux fois (${f})`)
    ecrans[k] = v
  }
  Object.assign(comptes, d.comptes ?? {})
}
