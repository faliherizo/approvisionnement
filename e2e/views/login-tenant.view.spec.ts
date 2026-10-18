import { test, expect } from '@playwright/test'

/**
 * Connexion multi-tenant par sous-domaine : la page de connexion n'affiche plus de sélecteur
 * d'établissement. Le tenant est déduit du sous-domaine (ex. levis.contrat.vinaitsolution.com) ;
 * en dev / domaine nu, le branding VINA par défaut s'applique.
 */
test.describe('Connexion (multi-tenant par sous-domaine)', () => {

  test('branding VINA, sans champ d’établissement à saisir', async ({ page }) => {
    await page.goto('/login')

    await expect(page.getByRole('heading', { name: 'VINA IT Solutions inc.' })).toBeVisible()

    // Plus aucun sélecteur d'établissement : le tenant provient du sous-domaine.
    await expect(page.getByLabel('Établissement (tenant)')).toHaveCount(0)

    // Seuls les identifiants sont demandés.
    await expect(page.getByLabel('Courriel')).toBeVisible()
    await expect(page.getByLabel('Mot de passe')).toBeVisible()
  })
})
