import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'
import { TestDocument } from '../models/types'

/** Tableau de bord : liste des documents, création, instanciation de template. */
export class DashboardPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Documents contractuels' })
  }

  async creerDocument(d: TestDocument): Promise<void> {
    await this.page.getByLabel('Titre', { exact: true }).fill(d.titre)
    await this.page.getByLabel('Type de contrat').selectOption(d.type)
    await this.page.getByLabel('Mode de sollicitation').selectOption(d.mode)
    await this.page.getByLabel('Valeur estimée ($)').first().fill(d.valeur)
    await this.page.locator('.rte-zone').fill(d.contexte) // contexte WYSIWYG
    await this.page.getByRole('button', { name: 'Créer le document' }).click()
  }

  async instancierTemplate(labelTemplate: string, titre: string): Promise<void> {
    await this.page.locator('select')
      .filter({ hasText: 'Choisir un template' })
      .selectOption({ label: labelTemplate })
    await this.page.getByLabel('Titre du document').fill(titre)
    await this.page.getByRole('button', { name: 'Instancier le template' }).click()
  }
}
