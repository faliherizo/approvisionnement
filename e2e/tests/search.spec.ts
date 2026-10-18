import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, SearchPage } from '../pages'
import { users, documents } from '../models/fixtures'

test.describe('Recherche : consulter, éditer, supprimer', () => {

  test('consulter une clause puis l’éditer dans la bibliothèque', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const recherche = new SearchPage(page)
    await recherche.naviguerVers('Recherche')
    await recherche.rechercher('garantie')

    await expect(recherche.carteClause('AOP-010')).toBeVisible()

    // Consulter : modale en lecture seule.
    await recherche.consulterClause('AOP-010')
    await expect(page.getByRole('dialog')).toContainText('AOP-010')
    await page.getByRole('dialog').locator('.modal-actions')
      .getByRole('button', { name: 'Fermer' }).click()

    // Éditer : lien profond vers la bibliothèque ; la modale d'édition s'ouvre
    // (le paramètre ?edit est consommé puis retiré de l'URL).
    await recherche.editerClause('AOP-010')
    await expect(page).toHaveURL(/\/clauses/)
    await expect(page.getByRole('dialog')).toContainText('Modifier AOP-010')
  })

  test('supprimer un document depuis les résultats', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    const titre = `ASupprimer${Date.now()}`
    await dashboard.creerDocument({ ...documents.appelOffres, titre })
    await expect(page).toHaveURL(/\/documents\/\d+$/)

    const recherche = new SearchPage(page)
    await recherche.naviguerVers('Recherche')
    await expect(page).toHaveURL(/\/recherche$/)
    await recherche.rechercher(titre)
    await expect(recherche.ligneDocument(titre)).toBeVisible()

    await recherche.supprimerDocument(titre)
    await recherche.rechercher(titre)
    await expect(recherche.ligneDocument(titre)).toHaveCount(0)
  })
})
