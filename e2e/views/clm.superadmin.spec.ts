import { test, expect, Page } from '@playwright/test'
import { users } from '../models/fixtures'
import { activerModules, auditerAxe, connecter, controler } from './clm.outils'

/**
 * Le module Contrats & signature se gère depuis l'espace superadmin comme les autres modules : désactivé pour une
 * organisation, il quitte sa navigation, ses écrans se ferment et son API refuse ; clm_ia, qui en dépend, suit. Activer
 * clm_ia réactive son prérequis.
 */
const ORGANISATION = 'Ville de Lévis (levis)'

async function gestionModules(page: Page) {
  await page.context().clearCookies()
  await connecter(page, users.superadmin)
  await page.goto('/superadmin')
  const panneau = page.locator('section.panel', { has: page.getByRole('heading', { name: /Modules par tenant/ }) })
  await panneau.getByLabel('Organisation').selectOption({ label: ORGANISATION })
  return panneau
}

/** Ligne d'un module, repérée par son code dans la première colonne (les prérequis d'autres lignes le citent aussi). */
const ligne = (panneau: ReturnType<Page['locator']>, code: string) =>
  panneau.getByRole('row').filter({ has: panneau.page().locator('td:first-child code', { hasText: new RegExp(`^${code}$`) }) })

/** Connexion de l'administrateur de l'organisation, une fois ses modules actifs relus par la navigation. */
async function connecterAdministrateur(page: Page) {
  await page.context().clearCookies()
  const modules = page.waitForResponse((r) => r.url().endsWith('/api/modules') && r.status() === 200)
  await connecter(page, users.admin)
  await modules
}

test.describe('Module CLM géré depuis l’espace superadmin, comme les autres modules', () => {
  test.afterAll(async ({ request }) => { await activerModules(request) })

  test('désactivation puis réactivation pour une organisation, dépendance clm_ia comprise', async ({ page }) => {
    let panneau = await gestionModules(page)
    await expect(ligne(panneau, 'clm_ia').getByRole('cell').nth(2)).toContainText('clm')
    await ligne(panneau, 'clm').getByRole('button', { name: 'Désactiver' }).click()
    await expect(ligne(panneau, 'clm').getByText('Inactif', { exact: true })).toBeVisible()
    await expect(ligne(panneau, 'clm_ia').getByText('Inactif', { exact: true })).toBeVisible()

    await connecterAdministrateur(page)
    await expect(page.getByRole('button', { name: 'Documents' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Contrats & signature' })).toHaveCount(0)
    await page.goto('/clm/trousses')
    await expect(page.getByText('Le module Contrats & signature n’est pas activé pour votre organisation.')).toBeVisible()
    await controler(page)
    await auditerAxe(page)
    expect((await page.request.get('/api/clm/trousses')).status()).toBe(403)

    panneau = await gestionModules(page)
    await ligne(panneau, 'clm_ia').getByRole('button', { name: 'Activer' }).click()
    await expect(ligne(panneau, 'clm_ia').getByText('Actif', { exact: true })).toBeVisible()
    await expect(ligne(panneau, 'clm').getByText('Actif', { exact: true })).toBeVisible()

    await connecterAdministrateur(page)
    await expect(page.getByRole('button', { name: 'Contrats & signature' })).toBeVisible()
    await page.goto('/clm/trousses')
    await expect(page.getByRole('heading', { level: 1, name: /Trousses/ })).toBeVisible()
    expect((await page.request.get('/api/clm/trousses')).ok()).toBeTruthy()
  })
})
