import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Accessibilité (WCAG)', () => {

  test('lien d’évitement, landmark principal et lien de navigation courant', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)

    // Lien d'évitement présent et focusable (WCAG 2.4.1).
    const skip = page.getByRole('link', { name: /Aller au contenu principal/ })
    await expect(skip).toBeAttached()
    await skip.focus()
    await expect(skip).toBeFocused()

    // Landmark principal identifiable et ciblable par le lien d'évitement.
    await expect(page.locator('main#contenu')).toBeVisible()

    // La navigation principale est étiquetée ; la page courante est exposée (aria-current)
    // une fois le groupe de menu déroulant ouvert.
    const nav = page.getByRole('navigation', { name: 'Navigation principale' })
    await expect(nav).toBeVisible()
    await nav.getByRole('button', { name: 'Documents' }).click()
    await expect(nav.locator('a[aria-current="page"]')).toHaveText('Documents')
  })
})
