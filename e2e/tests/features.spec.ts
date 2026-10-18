import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage, SearchPage, StatsPage } from '../pages'
import { users, tenants } from '../models/fixtures'

test.describe('Fonctionnalités CLM (vague 2 + tableaux de bord)', () => {

  test('E-6 : instancier un document depuis un template', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await expect(dashboard.titre()).toBeVisible()

    await dashboard.instancierTemplate(tenants.modeleTemplateAop, `AO depuis template ${Date.now()}`)

    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await expect(new DocumentEditorPage(page).codeClause('AOP-010')).toBeVisible()
  })

  test('E-3 : le tableau de bord affiche les indicateurs', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const stats = new StatsPage(page)
    await stats.naviguerVers('Tableau de bord')
    await expect(stats.kpiDocuments()).toBeVisible()
    await expect(stats.titreRepartitionStatut()).toBeVisible()
  })

  test('E-2 : recherche plein texte', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const recherche = new SearchPage(page)
    await recherche.naviguerVers('Recherche')
    await recherche.rechercher('garantie')
    await expect(recherche.resultat('AOP-010')).toBeVisible()
  })

  test('le lien API (Swagger) n\'est plus exposé dans le menu', async ({ page }) => {
    const login = new LoginPage(page)
    const dashboard = new DashboardPage(page)

    // Choix produit : la documentation Swagger n'est plus dans la navigation,
    // ni pour le rédacteur ni pour l'administrateur (accessible seulement via /api-docs).
    await login.connecter(users.redacteur)
    await expect(dashboard.lienNav('API (Swagger)')).toHaveCount(0)

    await dashboard.seDeconnecter()
    await login.connecter(users.admin)
    await expect(dashboard.lienNav('API (Swagger)')).toHaveCount(0)
  })
})
