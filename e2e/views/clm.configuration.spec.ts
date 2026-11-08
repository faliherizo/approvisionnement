import { test, expect, Page, APIRequestContext } from '@playwright/test'
import { users } from '../models/fixtures'
import { activerModules, auditerAxe, connecter, controler } from './clm.outils'

/**
 * « Rien n'est figé » : l'administration d'une organisation (ici Investissement Québec) adapte les listes de référence
 * du module, ses paramètres et sa connexion Salesforce. Chaque modification ne vaut que pour elle et se rétablit ; chaque
 * écran d'édition est contrôlé aux quatre largeurs (débordement, chevauchement, cibles, libellés, axe-core).
 */
const LARGEURS = [390, 768, 1280, 1680]

test.describe.configure({ timeout: 300_000 })

async function session(page: Page) {
  await page.context().clearCookies()
  await connecter(page, users.iqAdminTi)
  await expect(page).toHaveURL(/\/clm$/)
}

/** Remise à l'état de la plateforme par l'API, quel que soit le point d'arrêt du test. */
async function retablir(request: APIRequestContext) {
  await request.post('/api/auth/login', { data: users.iqAdminTi })
  await request.delete('/api/clm/admin/parametres')
  await request.delete('/api/clm/admin/integrations/salesforce/configuration')
  const types = await (await request.get('/api/admin/reference?categorie=CLM_TYPE_CONTRAT')).json()
  for (const v of types.filter((x: { origine: string }) => x.origine === 'PERSONNALISEE')) {
    await request.delete(`/api/admin/reference/${v.id}`)
  }
  await request.post('/api/auth/logout')
}

test.describe('Configuration du module par l’organisation — listes, paramètres, intégration', () => {
  test.beforeAll(async ({ request }) => {
    await activerModules(request)
    await retablir(request)
  })
  test.afterAll(async ({ request }) => { await retablir(request) })

  for (const largeur of LARGEURS) {
    test(`écrans d’édition conformes à ${largeur} px`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      await session(page)

      await test.step('UI-22 : paramètres du module en édition', async () => {
        await page.goto('/clm/admin/parametres')
        await page.getByRole('button', { name: 'Modifier un paramètre autorisé' }).click()
        await expect(page.getByRole('form', { name: 'Paramètres du module de l’organisation' })).toBeVisible()
        await expect(page.getByLabel(/Taille maximale par fichier/)).toBeFocused()
        await controler(page)
        await auditerAxe(page)
      })
      await test.step('UI-21 : connexion Salesforce en édition', async () => {
        await page.goto('/clm/admin/integrations')
        await page.getByRole('button', { name: 'Rotation du secret selon la procédure' }).click()
        await expect(page.getByRole('form', { name: 'Configurer la connexion' })).toBeVisible()
        await expect(page.getByLabel(/^Secret/)).toBeFocused()
        await expect(page.getByRole('note')).toContainText('jamais réaffiché')
        await controler(page)
        await auditerAxe(page)
      })
      await test.step('listes de référence : ajout d’une valeur', async () => {
        await page.goto('/clm/admin/listes')
        await page.getByLabel('Liste', { exact: true }).selectOption({ label: 'Unités administratives' })
        await expect(page.getByRole('cell').filter({ hasText: /^Financement des entreprises$/ })).toBeVisible()
        await page.getByRole('button', { name: 'Ajouter une valeur' }).click()
        await expect(page.getByRole('form', { name: 'Nouvelle valeur' })).toBeVisible()
        await controler(page)
        await auditerAxe(page)
      })
    })
  }

  test('une valeur de la plateforme personnalisée est proposée à la création de trousse, puis rétablie', async ({ page }) => {
    await session(page)
    await page.goto('/clm/admin/listes')
    await page.getByLabel('Liste', { exact: true }).selectOption({ label: 'Types de contrat et d\'entente' })
    await page.getByRole('button', { name: 'Personnaliser — Contrat de services' }).click()
    const formulaire = page.getByRole('form', { name: 'Personnaliser la valeur de la plateforme' })
    await expect(formulaire.getByLabel('Code')).toHaveAttribute('readonly', '')
    await formulaire.getByLabel('Libellé').fill('Services professionnels (IQ)')
    await formulaire.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Valeur enregistrée.' })).toBeVisible()
    const ligne = page.getByRole('row').filter({ hasText: 'Services professionnels (IQ)' })
    await expect(ligne).toContainText('Personnalisée')

    await page.goto('/clm/trousses/nouvelle')
    await expect(page.getByLabel('Type de contrat').locator('option', { hasText: 'Services professionnels (IQ)' })).toHaveCount(1)
    await expect(page.getByLabel('Unité responsable').locator('option', { hasText: 'Financement des entreprises' })).toHaveCount(1)

    await page.goto('/clm/admin/listes')
    await page.getByLabel('Liste', { exact: true }).selectOption({ label: 'Types de contrat et d\'entente' })
    await page.getByRole('button', { name: 'Rétablir — Services professionnels (IQ)' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Valeur de la plateforme rétablie.' })).toBeVisible()
    await expect(page.getByRole('row').filter({ hasText: 'Contrat de services' })).toContainText('Plateforme')
  })

  test('un paramètre du module modifié s’applique à la trousse suivante, puis la plateforme est rétablie', async ({ page }) => {
    await session(page)
    await page.goto('/clm/admin/parametres')
    await page.getByRole('button', { name: 'Modifier un paramètre autorisé' }).click()
    await page.getByLabel(/Préfixe des références/).fill('iqe')
    await page.getByRole('form', { name: 'Paramètres du module de l’organisation' }).getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Paramètres enregistrés.' })).toBeVisible()
    await expect(page.getByText(/Paramètres propres à l’organisation, modifiés par admin\.ti@iq\.example/)).toBeVisible()

    const trousse = await (await page.request.post('/api/clm/trousses', { data: { titre: 'Préfixe IQ e2e' } })).json()
    expect(trousse.resume.reference).toMatch(/^IQE-\d{4}-\d{5}$/)

    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: 'Modifier un paramètre autorisé' }).click()
    await page.getByRole('button', { name: 'Rétablir les paramètres de la plateforme' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Paramètres de la plateforme rétablis.' })).toBeVisible()
    await expect(page.getByText('Paramètres de la plateforme : l’organisation n’en a personnalisé aucun.')).toBeVisible()
  })

  test('la connexion Salesforce propre à IQ ne réaffiche jamais son secret et refuse un hôte non autorisé', async ({ page }) => {
    await session(page)
    await page.goto('/clm/admin/integrations')
    await page.getByRole('button', { name: 'Configurer la connexion' }).click()
    const formulaire = page.getByRole('form', { name: 'Configurer la connexion' })
    await formulaire.getByLabel('Mode').selectOption('REST')
    await formulaire.getByLabel(/^Instance/).fill('https://intranet.iq.example')
    await formulaire.getByLabel('Serveur de jetons OAuth').fill('https://login.salesforce.com/services/oauth2/token')
    await formulaire.getByLabel(/^Identifiant client/).fill('3MVG9-iq-e2e')
    await formulaire.getByLabel(/^Secret/).fill('s3cr3t-e2e')
    await formulaire.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('alert')).toContainText('domaine autorisé')

    await formulaire.getByLabel(/^Instance/).fill('https://iq.my.salesforce.com')
    await formulaire.getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Connexion enregistrée.' })).toBeVisible()
    await expect(page.getByText(/Connexion propre à l’organisation/)).toBeVisible()
    await expect(page.getByText('3MVG…2e')).toBeVisible()
    const contenu = await page.content()
    expect(contenu).not.toContain('s3cr3t-e2e')
    expect(contenu).not.toContain('3MVG9-iq-e2e')

    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: 'Configurer la connexion' }).click()
    await page.getByRole('button', { name: 'Rétablir la connexion de la plateforme' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Connexion de la plateforme rétablie.' })).toBeVisible()
  })
})
