import { test as base, createBdd } from 'playwright-bdd'

/** État d'un scénario : valeurs capturées par les cas (identifiants, références…), erreurs de vérification. */
export type Monde = {
  variables: Record<string, unknown>
  ecarts: string[]
  derniere?: { statut: number; corps: unknown; texte: string }
}

export const test = base.extend<{ monde: Monde }>({
  // eslint-disable-next-line no-empty-pattern
  monde: async ({}, use) => {
    // Suffixe propre au scénario : codes et titres uniques, rejouables sans nettoyage de la base.
    const unique = Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
    // Date du jour dans le fuseau de l'organisation (CLM_FUSEAU), comme le serveur : pas la date UTC (voir e2e).
    const fuseau = process.env.E2E_FUSEAU ?? 'America/Toronto'
    const jour = (n: number) => new Intl.DateTimeFormat('en-CA', { timeZone: fuseau, year: 'numeric', month: '2-digit', day: '2-digit' })
      .format(new Date(Date.now() + n * 86_400_000))
    await use({ variables: { unique, UNIQUE: unique.toUpperCase(), aujourdhui: jour(0), demain: jour(1), dansUnMois: jour(30) }, ecarts: [] })
  },
})

export const { Given, When, Then } = createBdd(test)
