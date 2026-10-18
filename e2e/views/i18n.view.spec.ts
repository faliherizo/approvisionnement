import { test, expect } from '@playwright/test'

/** Test de vue : le sélecteur de langue traduit l'interface. */
test.describe('Internationalisation (i18n)', () => {

  test('la page de connexion peut basculer en anglais', async ({ page }) => {
    await page.goto('/login')
    // Défaut : français (Canada).
    await expect(page.getByRole('button', { name: 'Se connecter', exact: true })).toBeVisible()

    // Bascule en anglais via le sélecteur de langue.
    const langue = page.locator('.lang-switcher select')
    await langue.selectOption('en')
    await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible()
    await expect(page.getByText('Powered by')).toBeVisible()

    // Retour au français.
    await langue.selectOption('fr-CA')
    await expect(page.getByRole('button', { name: 'Se connecter', exact: true })).toBeVisible()
  })

  test('la page de connexion peut basculer en malgache (MGA)', async ({ page }) => {
    await page.goto('/login')
    const langue = page.locator('.lang-switcher select')
    await langue.selectOption('mg')
    await expect(page.getByRole('button', { name: 'Hiditra', exact: true })).toBeVisible()
    await expect(page.getByText('Novokarin’i')).toBeVisible()
    await langue.selectOption('fr-CA')
    await expect(page.getByRole('button', { name: 'Se connecter', exact: true })).toBeVisible()
  })

  test('la langue choisie persiste après rechargement', async ({ page }) => {
    await page.goto('/login')
    await page.locator('.lang-switcher select').selectOption('en')
    await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible()
    await page.reload()
    await expect(page.getByRole('button', { name: 'Sign in', exact: true })).toBeVisible() // persistée (localStorage)
    await page.locator('.lang-switcher select').selectOption('fr-CA')
  })
})
