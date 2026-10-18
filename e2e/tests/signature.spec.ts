import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage } from '../pages'
import { users, documents } from '../models/fixtures'

test.describe('Signature électronique externe (lien public par jeton)', () => {
  test.use({ permissions: ['clipboard-read', 'clipboard-write'] })

  test('un signataire externe signe via le lien public', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Signature ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.ajouterSignataire('Jean Externe', 'jean@externe.ca', 'Cocontractant')
    await editeur.demarrerSignature()

    const lien = await editeur.copierLienSignature('Jean Externe')
    expect(lien).toContain('/signature/')

    // Page publique (non authentifiée) : le signataire appose sa signature.
    await page.goto(lien)
    await expect(page.getByRole('heading', { name: 'Signature électronique' })).toBeVisible()

    const canvas = page.locator('.sign-canvas')
    const box = await canvas.boundingBox()
    if (!box) throw new Error('Canvas de signature introuvable')
    await page.mouse.move(box.x + 30, box.y + 30)
    await page.mouse.down()
    await page.mouse.move(box.x + 140, box.y + 90)
    await page.mouse.move(box.x + 240, box.y + 40)
    await page.mouse.up()

    await page.getByRole('button', { name: 'Apposer ma signature' }).click()
    await expect(page.getByText(/signature a bien été enregistrée/)).toBeVisible()
  })
})
