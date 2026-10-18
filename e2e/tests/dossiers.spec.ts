import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DossiersPage } from '../pages'
import { users, documents } from '../models/fixtures'

test.describe('Dossier d\'approvisionnement (regroupement de documents — art. 15)', () => {

  test('créer un dossier, y rattacher un document et consulter le rapport', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)

    // 1) Un document existant, non rattaché.
    const dashboard = new DashboardPage(page)
    const titreDoc = `Cahier des charges ${Date.now()}`
    await dashboard.creerDocument({ ...documents.appelOffres, titre: titreDoc })
    await expect(page).toHaveURL(/\/documents\/\d+$/)

    // 2) Création d'un dossier.
    const dossiers = new DossiersPage(page)
    await dossiers.naviguerVers('Dossiers')
    await expect(dossiers.titre()).toBeVisible()

    const titreDossier = `Acquisition solution ${Date.now()}`
    await dossiers.creerDossier({ titre: titreDossier, numeroAO: '7000017905', typeAcquisition: 'TECHNOLOGIES' })

    // 3) Le dossier s'ouvre et affiche son rapport de suivi.
    await expect(dossiers.detail()).toContainText(titreDossier)
    await expect(dossiers.rapport()).toContainText('0 document(s)')

    // 4) Rattachement du document existant → le rapport se met à jour.
    await dossiers.rattacher(titreDoc)
    await expect(dossiers.documentsRattaches()).toContainText(titreDoc)
    await expect(dossiers.rapport()).toContainText('1 document(s)')

    // 5) Changement de statut du dossier.
    await dossiers.changerStatut('EN_COURS')
    await expect(dossiers.ligneDossier(titreDossier).first()).toContainText('EN_COURS')
  })

  test('modifier puis supprimer un dossier', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const dossiers = new DossiersPage(page)
    await dossiers.naviguerVers('Dossiers')

    const titre = `Dossier a editer ${Date.now()}`
    await dossiers.creerDossier({ titre })
    await expect(dossiers.detail()).toContainText(titre)

    const titreModifie = `${titre} revise`
    await dossiers.modifier(titreModifie)
    await expect(dossiers.ligneDossier(titreModifie).first()).toBeVisible()

    await dossiers.ouvrir(titreModifie)
    await dossiers.supprimer()
    await expect(page.getByRole('row', { name: new RegExp(titreModifie) })).toHaveCount(0)
  })
})
