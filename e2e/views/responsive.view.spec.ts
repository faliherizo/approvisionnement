import { test, expect, Page } from '@playwright/test'
import { LoginPage, DashboardPage, DossiersPage, AssistantPage, WorkflowsPage, AuditPage } from '../pages'
import { users } from '../models/fixtures'

/**
 * Tests de VUES (mise en page / responsive) — distincts des tests e2e fonctionnels.
 * Vérifient l'absence de débordement horizontal sur un viewport mobile.
 */
test.use({ viewport: { width: 390, height: 844 } })

// Compare au innerWidth (inclut la barre de défilement) pour éviter les faux positifs.
async function deborde(page: Page): Promise<boolean> {
  return page.evaluate(
    () => document.documentElement.scrollWidth > window.innerWidth + 1
  )
}

test.describe('Vues responsive (mobile)', () => {

  test('la page de connexion tient dans la largeur', async ({ page }) => {
    const login = new LoginPage(page)
    await login.ouvrir()
    await expect(login.boutonConnexion()).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })

  test('navigation et tableau de bord utilisables sur mobile', async ({ page }) => {
    const login = new LoginPage(page)
    await login.connecter(users.redacteur)

    const dashboard = new DashboardPage(page)
    await expect(dashboard.titre()).toBeVisible()
    // Le menu regroupé « Documents » s'ouvre et expose ses destinations sur mobile.
    await page.getByRole('button', { name: 'Documents' }).click()
    await expect(dashboard.lienNav('Bibliothèque')).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })

  test('la page des dossiers d’approvisionnement tient dans la largeur', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const dossiers = new DossiersPage(page)
    await dossiers.naviguerVers('Dossiers')
    await expect(dossiers.titre()).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })

  test('l’assistant guidé tient dans la largeur', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const assistant = new AssistantPage(page)
    await assistant.naviguerVers('Assistant guidé')
    await expect(assistant.titre()).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })

  test('les circuits d’approbation et le journal d’audit tiennent dans la largeur', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const workflows = new WorkflowsPage(page)
    await workflows.naviguerVers('Circuits d’approbation')
    await expect(workflows.titre()).toBeVisible()
    expect(await deborde(page)).toBeFalsy()

    const audit = new AuditPage(page)
    await audit.naviguerVers('Journal d’audit')
    await expect(audit.titre()).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })
})
