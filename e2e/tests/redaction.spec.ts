import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage } from '../pages'
import { users, documents, sections } from '../models/fixtures'

/**
 * Rédaction locale du contenu (approche hybride) : adapter le texte d'une section dans le
 * document sans toucher le catalogue, et ajouter une section libre.
 *
 * On saisit au clavier réel (pressSequentially) plutôt que fill() : cela vérifie que la
 * modale garde le focus pendant la frappe (régression d'accessibilité : le piège de focus
 * ne doit pas voler le focus à chaque caractère). Données de test issues de sections.json.
 */
test.describe('Rédaction du contenu des sections (hybride, accessible)', () => {

  test('adapter une section et ajouter une section libre en tapant au clavier', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Rédac ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()

    const devis = page.locator('.panel', {
      has: page.getByRole('heading', { name: /Devis assemblé/ }),
    })

    // ── Adapter localement le texte : on TAPE le contenu (focus doit rester dans le champ) ──
    await devis.getByRole('button', { name: 'Rédiger' }).first().click()
    const modale = page.getByRole('dialog')
    const zone = modale.locator('.rte-zone').first()
    await zone.click()
    await zone.pressSequentially(sections.clauseAdaptee.texte, { delay: 10 })
    await modale.getByRole('button', { name: 'Enregistrer' }).click()

    await expect(page.getByText('Section mise à jour')).toBeVisible()
    // Le texte COMPLET est présent → le focus n'a pas été volé pendant la frappe.
    await expect(devis.getByText(sections.clauseAdaptee.texte)).toBeVisible()
    await expect(devis.getByText('modifié localement').first()).toBeVisible()

    // ── Ajouter une section libre en tapant titre + contenu ──
    await devis.getByRole('button', { name: 'Ajouter une section libre' }).click()
    const modale2 = page.getByRole('dialog')
    const titre = modale2.getByLabel('Titre de la section')
    await titre.click()
    await titre.pressSequentially(sections.sectionLibre.titre, { delay: 10 })
    const zone2 = modale2.locator('.rte-zone').first()
    await zone2.click()
    await zone2.pressSequentially(sections.sectionLibre.contenu, { delay: 10 })
    await modale2.getByRole('button', { name: 'Ajouter la section' }).click()

    await expect(page.getByText('Section ajoutée')).toBeVisible()
    await expect(devis.getByText(sections.sectionLibre.titre)).toBeVisible()
    await expect(devis.getByText(sections.sectionLibre.contenu)).toBeVisible()
    await expect(devis.getByText('section libre').first()).toBeVisible()
  })
})
