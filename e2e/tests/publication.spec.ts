import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage } from '../pages'
import { users, documents } from '../models/fixtures'

test.describe('Publication vers une cible externe (M365/SAP — art. 9)', () => {

  test('publier un document vers SharePoint et tracer l’historique', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Publication ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()
    // Publication réservée aux documents validés : chaque niveau est approuvé par son rôle.
    await editeur.demarrerApprobation()
    await editeur.approuverParLeCircuit([users.redacteur, users.juriste, users.direction], users.redacteur)

    await editeur.publier('SHAREPOINT', 'PDF')
    await expect(editeur.historiquePublications()).toContainText('SHAREPOINT')
    await expect(editeur.historiquePublications()).toContainText('sim://sharepoint/')
  })

  test('publier vers les quatre cibles Microsoft 365 et tracer chaque dépôt', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Publication 4 cibles ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()

    // La publication n'est offerte qu'après validation du document (statut APPROUVE).
    await editeur.demarrerApprobation()
    await editeur.approuverParLeCircuit([users.redacteur, users.juriste, users.direction], users.redacteur)
    await expect(editeur.badgeStatut('APPROUVE')).toBeVisible()

    // Chaque cible externe reçoit un dépôt distinct, tracé dans l'historique.
    for (const cible of ['SHAREPOINT', 'ONEDRIVE', 'TEAMS', 'OUTLOOK'] as const) {
      await editeur.publier(cible, 'PDF', 'Contrats/2027')
      await expect(editeur.historiquePublications()).toContainText(cible)
    }
  })
})
