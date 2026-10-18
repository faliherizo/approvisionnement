import { test, expect } from '@playwright/test'
import { LoginPage, DonneesIAPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Données & IA (P6) : import, réversibilité, encadrement IA', () => {

  test('migration d’essai d’un CSV de clauses', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const donnees = new DonneesIAPage(page)
    await donnees.naviguerVers('Données & IA')
    await expect(donnees.titre()).toBeVisible()

    await donnees.migrationEssai('code,titre,categorie,corps\nE2E-1,Objet,STANDARD,Corps du contrat\n,SansCode,STANDARD,x')
    await expect(donnees.rapport()).toContainText('1 valide')
    await expect(donnees.rapport()).toContainText('Ligne 3')
  })

  test('exporter la réversibilité et encadrer l’IA', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const donnees = new DonneesIAPage(page)
    await donnees.naviguerVers('Données & IA')

    // Réversibilité : le clic déclenche un téléchargement ZIP.
    const [telechargement] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter toutes les données (ZIP)' }).click(),
    ])
    expect(telechargement.suggestedFilename()).toMatch(/\.zip$/)

    // Encadrement IA : bascule l'état.
    await expect(donnees.etatIa()).toContainText('Activé')
    await donnees.basculerIa()
    await expect(donnees.etatIa()).toContainText('Désactivé')
    await donnees.basculerIa() // réactive
    await expect(donnees.etatIa()).toContainText('Activé')
  })
})
