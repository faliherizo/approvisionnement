import { readFileSync, readdirSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import type { Cas, Fichier } from '../outils/cas'

/**
 * Scénarios e2e d'interface par exigence (bdd/e2e/donnees/*.json). Chaque étape est un geste d'utilisateur, décrit par
 * une seule clé ; seules les étapes « preparer » passent par l'API, pour mettre en place ce que l'exigence ne vise pas.
 */
export type Etape =
  | { aller: string }
  | { connecter: string | { email: string; motDePasse: string } }
  | { langue: 'fr' | 'en' | 'mg' }
  | { largeur: number }
  | { options: Record<string, { contient?: string[]; exclut?: string[] }> }
  | { cliquer: string | { role: 'button' | 'link' | 'tab' | 'menuitem' | 'checkbox' | 'radio'; nom: string } }
  | { dansLaLigne: { texte: string; cliquer: string } }
  | { remplir: Record<string, string> }
  | { choisir: Record<string, string> }
  | { cocher: string }
  | { decocher: string }
  | { televerser: { champ: string; fichier: Fichier } }
  | { voir: string | string[] }
  | { nePasVoir: string | string[] }
  | { valeur: Record<string, string> }
  | { voirTitre: string }
  | { url: string }
  | { preparer: string | Cas }
  | { telecharger: string }
  | { ecranConforme: true }
  | { cliquerTexte: string }
  | { dansLaFenetre: string }
  | { dansLaSection: { titre: string; cliquer: string } }
  | { repondre: string }
  | { tracer: true }
  | { lire: { selecteur: string; variable: string } }
  | { remplirTotp: Record<string, string> }

export type Scenario = {
  id: string
  titre: string
  exigences: string[]
  profil: string
  organisation?: string
  etapes: Etape[]
}

export type Jeu = { appelOffres: string; catalogue: string; scenarios: Scenario[] }

const DOSSIER = resolve(dirname(fileURLToPath(import.meta.url)), 'donnees')

export const jeux: Jeu[] = readdirSync(DOSSIER).filter((f) => f.endsWith('.json')).sort()
  .map((f) => JSON.parse(readFileSync(join(DOSSIER, f), 'utf8')) as Jeu)
