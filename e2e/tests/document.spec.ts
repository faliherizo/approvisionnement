import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage, DocumentEditorPage, ClausesPage } from '../pages'
import { users, documents } from '../models/fixtures'

test.describe('Cycle de vie d\'un document contractuel', () => {

  test('créer un devis, assembler les clauses et démarrer l\'approbation', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await expect(dashboard.titre()).toBeVisible()

    const doc = { ...documents.appelOffres, titre: `${documents.appelOffres.titre} ${Date.now()}` }
    await dashboard.creerDocument(doc)

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await expect(page.getByRole('heading', { name: doc.titre })).toBeVisible()

    // Le moteur de règles propose la garantie de soumission (AOP-010).
    await expect(editeur.codeClause('AOP-010')).toBeVisible()

    await editeur.assembler()
    await expect(page.getByText('Objet du contrat')).toBeVisible()

    await editeur.demarrerApprobation()
    await expect(editeur.circuitApprobation()).toContainText('APPRO')
    await expect(editeur.circuitApprobation()).toContainText('JURIDIQUE')
  })

  test('ajouter une annexe et un bordereau de prix au contrat', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    const doc = { ...documents.appelOffres, titre: `Structure ${Date.now()}` }
    await dashboard.creerDocument(doc)

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()

    // Annexe (une notification de confirmation apparaît).
    await editeur.ajouterAnnexe('Plan des travaux')
    await expect(page.getByText('Annexe ajoutée')).toBeVisible()
    await expect(editeur.panneauAnnexes()).toContainText('Plan des travaux')

    // Bordereau de prix : deux lignes, total calculé.
    await editeur.ajouterLigneBordereau('Analyse', 10, 100)
    await editeur.ajouterLigneBordereau('Développement', 20, 50)
    await expect(editeur.totalBordereau()).toContainText('2000.00')
    await editeur.enregistrerBordereau()
    await expect(page.getByText('Bordereau enregistré')).toBeVisible()

    // Après enregistrement + rechargement, le bordereau persiste (valeurs des champs).
    await editeur.recharger()
    await expect(editeur.descriptionLigneBordereau(0)).toHaveValue('Analyse')
    await expect(editeur.totalBordereau()).toContainText('2000.00')
  })

  test('aperçu PDF du contrat avant téléchargement', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Aperçu ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()

    await editeur.ouvrirApercuPdf()
    await expect(editeur.apercuPdf()).toBeVisible()
    await expect(page.getByRole('button', { name: 'Télécharger le PDF' })).toBeVisible()
  })

  test('générer le PDF selon un gabarit normalisé (bordereau de prix)', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Gabarit ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()

    // On garnit le bordereau, puis on exporte avec le gabarit « Bordereau de prix ».
    await editeur.ajouterLigneBordereau('Technicien spécialisé', 1800, 95)
    await editeur.enregistrerBordereau()
    await expect(page.getByText('Bordereau enregistré')).toBeVisible()

    await editeur.choisirGabarit('Bordereau de prix')
    const pdf = await editeur.telechargerPdf()
    expect(pdf.suggestedFilename()).toMatch(/\.pdf$/)
    await expect(page.getByText('Export PDF généré')).toBeVisible()
  })

  test('la bibliothèque de clauses est accessible', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const clauses = new ClausesPage(page)
    await clauses.naviguerVers('Bibliothèque')
    await expect(clauses.titre()).toBeVisible()
    // La liste est paginée : on retrouve une clause de base par la recherche.
    await clauses.rechercher('GEN-001')
    await expect(clauses.clause('GEN-001')).toBeVisible()
  })
})
