import { test, expect, Page } from '@playwright/test'

/** Site vitrine public : accessibilité (WCAG 2.2) et responsivité, sans authentification. */

async function deborde(page: Page): Promise<boolean> {
  return page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth + 1)
}

test.describe('Site vitrine public', () => {

  test('la racine redirige un visiteur non connecté vers l’accueil', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveURL(/\/accueil$/)
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  })

  test('accessible sans connexion, structure et vidéo présentes', async ({ page }) => {
    await page.goto('/accueil')

    // Un seul titre principal, landmarks présents.
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    await expect(page.getByRole('banner')).toBeVisible()
    await expect(page.getByRole('main')).toBeVisible()
    await expect(page.getByRole('contentinfo')).toBeVisible()

    // Lien d'évitement focusable (WCAG 2.4.1).
    const skip = page.getByRole('link', { name: /Aller au contenu principal/ })
    await expect(skip).toBeAttached()

    // Vidéo de démonstration présente, sans texte (silencieuse) et avec son alternative textuelle.
    await expect(page.locator('video')).toBeVisible()
    await expect(page.locator('video track[kind="captions"]')).toHaveCount(0)
    await expect(page.locator('figcaption')).toContainText('Parcours filmé')

    // Accès à l'application depuis la vitrine : sur le domaine de la plateforme, il ouvre le choix de l'organisation.
    await expect(page.getByRole('button', { name: 'Accéder à l’application' })).toBeVisible()
  })

  test('« Accéder à l’application » propose les organisations actives et mène au sous-domaine choisi', async ({ page }) => {
    await page.route('**/api/tenants', (r) => r.fulfill({ json: [
      { slug: 'levis', titre: 'Ville de Lévis' }, { slug: 'v3r', titre: 'Ville de Trois-Rivières' },
    ] }))
    await page.route(/\/\/v3r\./, (r) => r.fulfill({ contentType: 'text/html', body: '<title>v3r</title>' }))
    await page.goto('/accueil')
    await page.getByRole('button', { name: 'Accéder à l’application' }).first().click()

    const fenetre = page.getByRole('dialog', { name: 'Choisir votre organisation' })
    await expect(fenetre).toBeVisible()
    const liste = fenetre.getByLabel('Organisation')
    await expect(liste.locator('option')).toHaveText(['Ville de Lévis', 'Ville de Trois-Rivières'])
    await liste.selectOption('v3r')
    await fenetre.getByRole('button', { name: 'Continuer vers la connexion' }).click()
    await expect(page).toHaveURL(/\/\/v3r\.localhost(:\d+)?\/login$/)
  })

  test('le choix de l’organisation se ferme par Échap et rend le focus', async ({ page }) => {
    await page.route('**/api/tenants', (r) => r.fulfill({ status: 503 }))
    await page.goto('/accueil')
    const acces = page.getByRole('button', { name: 'Accéder à l’application' }).first()
    await acces.click()
    const fenetre = page.getByRole('dialog', { name: 'Choisir votre organisation' })
    await expect(fenetre.getByRole('alert')).toContainText('indisponible')
    await expect(fenetre.getByRole('button', { name: 'Continuer vers la connexion' })).toBeDisabled()
    await page.keyboard.press('Escape')
    await expect(fenetre).toBeHidden()
    await expect(acces).toBeFocused()
  })

  test('tient dans la largeur sur mobile (390 px) et tablette 10 pouces (800 px)', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/accueil')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    expect(await deborde(page)).toBeFalsy()

    await page.setViewportSize({ width: 800, height: 1280 })
    await page.goto('/accueil')
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    expect(await deborde(page)).toBeFalsy()
  })
})
