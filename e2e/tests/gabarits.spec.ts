import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage } from '../pages'
import { users, documents } from '../models/fixtures'

/**
 * Gestion dynamique des gabarits (CRUD) et disponibilité immédiate dans l'éditeur.
 * Un admin hérite du catalogue global (SCT), crée un gabarit d'entreprise, et le
 * retrouve aussitôt dans le sélecteur de génération d'un document.
 */
test.describe('Gabarits de documents — gestion dynamique', () => {

  test('un admin crée un gabarit et le retrouve dans l’éditeur', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)

    // Administration → Gabarits
    await page.getByRole('button', { name: 'Administration' }).click()
    await page.getByRole('link', { name: 'Gabarits' }).click()
    await expect(page.getByRole('heading', { name: 'Gabarits de documents', exact: true })).toBeVisible()

    // Le catalogue global (référentiel SCT) est hérité et listé.
    await expect(page.getByRole('cell', { name: 'BORDEREAU_PRIX' })).toBeVisible()

    // Créer un gabarit d'entreprise.
    const code = `TEST_${Date.now()}`
    await page.getByRole('button', { name: 'Nouveau gabarit' }).click()
    const modale = page.getByRole('dialog')
    await modale.getByLabel('Code', { exact: true }).fill(code)
    await modale.getByLabel('Nom (affiché)').fill(`Gabarit ${code}`)
    await modale.getByLabel('Libellé principal', { exact: true }).fill('DOCUMENT TEST')
    await modale.getByRole('button', { name: 'Créer le gabarit' }).click()

    await expect(page.getByText('Gabarit créé')).toBeVisible()
    await expect(page.getByRole('cell', { name: code, exact: true })).toBeVisible()

    // Le nouveau gabarit est immédiatement disponible dans l'éditeur d'un document.
    const dashboard = new DashboardPage(page)
    await dashboard.naviguerVers('Documents')
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Gab ${Date.now()}` })
    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.ouvrirMenuGenerer()
    await expect(page.getByLabel('Gabarit du document à générer')
      .locator('option', { hasText: code })).toHaveCount(1)
  })
})
