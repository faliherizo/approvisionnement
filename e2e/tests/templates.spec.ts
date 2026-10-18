import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages'
import { users, documentTemplates } from '../models/fixtures'

/**
 * Gestion dynamique des templates de documents : créer, modifier, supprimer (CRUD complet),
 * avec type, gabarit (design), titre/contexte par défaut, clauses et annexes.
 * Données de test issues de templates.json.
 */
test.describe('Templates de documents — gestion dynamique', () => {

  test('un admin crée, modifie puis supprime un template', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)

    // Administration → Templates
    await page.getByRole('button', { name: 'Administration' }).click()
    await page.getByRole('link', { name: 'Templates', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Templates de documents', exact: true })).toBeVisible()

    // ── Créer ──
    const code = `TPL_${Date.now()}`
    const { nouveau, modifie } = documentTemplates
    await page.getByRole('button', { name: 'Nouveau template' }).click()
    const modale = page.getByRole('dialog')
    await modale.getByLabel('Code', { exact: true }).fill(code)
    await modale.getByLabel('Nom (affiché)').fill(nouveau.nom)
    await modale.getByLabel('Type de contrat').selectOption({ index: 1 })
    await modale.getByLabel(/Gabarit/).selectOption({ label: 'Bordereau de prix' })
    await modale.getByLabel('Titre par défaut').fill(nouveau.titreParDefaut)
    await modale.getByLabel(/Codes de clauses/).fill(nouveau.clauseCodes)
    await modale.getByLabel(/Annexes par défaut/).fill(nouveau.annexes)
    await modale.getByRole('button', { name: 'Créer le template' }).click()

    await expect(page.getByText('Template créé')).toBeVisible()
    await expect(page.getByRole('cell', { name: code, exact: true })).toBeVisible()

    // ── Modifier ──
    const ligne = page.locator('tr', { has: page.getByRole('cell', { name: code, exact: true }) })
    await ligne.getByRole('button', { name: 'Modifier' }).click()
    const modale2 = page.getByRole('dialog')
    await modale2.getByLabel('Nom (affiché)').fill(modifie.nom)
    await modale2.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByText('Template modifié')).toBeVisible()
    await expect(page.getByRole('cell', { name: modifie.nom })).toBeVisible()

    // ── Supprimer ──
    await ligne.getByRole('button', { name: 'Supprimer' }).click()
    await page.getByRole('dialog').getByRole('button', { name: 'Supprimer' }).click()
    await expect(page.getByText('Template supprimé')).toBeVisible()
    await expect(page.getByRole('cell', { name: code, exact: true })).toHaveCount(0)
  })
})
