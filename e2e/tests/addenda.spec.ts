import { test, expect } from '@playwright/test'
import { resolve } from 'node:path'
import { LoginPage, DonneesIAPage, RapportsPage } from '../pages'
import { users } from '../models/fixtures'

const MODELE_DOCX = resolve(__dirname, '../data/modele-demo.docx')

test.describe('Addenda 1 : import Word + rapports de suivi', () => {

  test('importer un modèle Word (.docx) : essai puis réel', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const donnees = new DonneesIAPage(page)
    await donnees.naviguerVers('Données & IA')
    await expect(donnees.titre()).toBeVisible()

    // Migration d'essai : les sections du .docx sont détectées.
    await donnees.analyserWord(MODELE_DOCX)
    await expect(page.getByText(/section\(s\) détectée\(s\)/)).toBeVisible()

    // Import réel : un modèle + ses clauses sont créés.
    await donnees.importerWord('Modele importe e2e', MODELE_DOCX)
    await expect(page.getByText(/clause\(s\) créée\(s\)/)).toBeVisible()
    await expect(page.getByText(/Modele importe e2e/)).toBeVisible()
  })

  test('consulter les rapports de suivi consolidés', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const rapports = new RapportsPage(page)
    await rapports.naviguerVers('Rapports de suivi')
    await expect(rapports.titre()).toBeVisible()
    await expect(rapports.indicateurs()).toContainText('document(s)')
    await expect(page.getByRole('heading', { name: 'Documents par statut' })).toBeVisible()
  })
})
