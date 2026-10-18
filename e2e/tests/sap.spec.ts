import { test, expect } from '@playwright/test'
import { LoginPage, DossiersPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Connecteur SAP (référence financière du dossier — art. 9)', () => {

  test('transmettre la référence financière d’un dossier à SAP', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const dossiers = new DossiersPage(page)
    await dossiers.naviguerVers('Dossiers')
    await expect(dossiers.titre()).toBeVisible()

    const titre = `Dossier SAP ${Date.now()}`
    await dossiers.creerDossier({ titre, numeroAO: '7000018100', typeAcquisition: 'BIENS' })
    await expect(dossiers.detail()).toContainText(titre)

    await dossiers.transmettreSap()
    await expect(dossiers.resultatSap()).toContainText('sap://dossier/7000018100')
  })
})
