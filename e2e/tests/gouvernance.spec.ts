import { test, expect } from '@playwright/test'
import { LoginPage, WorkflowsPage, AuditPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Gouvernance (P3) : workflow configurable + journal d’audit', () => {

  test('configurer un circuit d’approbation par type de document', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const workflows = new WorkflowsPage(page)
    await workflows.naviguerVers('Circuits d’approbation')
    await expect(workflows.titre()).toBeVisible()

    const nom = `Circuit gre a gre ${Date.now()}`
    await workflows.creer(nom, 'GRE_A_GRE', ['APPRO', 'DIRECTION'])
    await expect(workflows.ligne(nom).first()).toContainText('APPRO → DIRECTION')

    // Édition : on ajoute l'étape Juridique.
    await workflows.editer(nom)
    await page.getByRole('checkbox', { name: /^JURIDIQUE/ }).check()
    await workflows.enregistrerModification()
    await expect(workflows.ligne(nom).first()).toContainText('APPRO → DIRECTION → JURIDIQUE')
  })

  test('consulter le journal d’audit filtré', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const audit = new AuditPage(page)
    await audit.naviguerVers('Journal d’audit')
    await expect(audit.titre()).toBeVisible()
    await audit.filtrer('LOGIN') // la connexion qui précède est toujours journalisée
    await expect(audit.table()).toBeVisible()
    await expect(page.getByRole('button', { name: 'Exporter en CSV' })).toBeVisible()
  })
})
