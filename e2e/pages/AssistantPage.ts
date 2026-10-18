import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Assistant guidé de génération (art. 7.2) : analyse du contexte puis génération. */
export class AssistantPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Assistant guidé de génération' })
  }

  async analyser(ctx: { type?: string; categorieAcquisition?: string; valeur?: string; contexte?: string }): Promise<void> {
    if (ctx.type) await this.page.getByLabel('Type de document').selectOption(ctx.type)
    if (ctx.categorieAcquisition) await this.page.getByLabel('Type d’acquisition').selectOption(ctx.categorieAcquisition)
    if (ctx.valeur) await this.page.getByLabel('Valeur estimée ($)').fill(ctx.valeur)
    if (ctx.contexte) await this.page.getByLabel('Contexte du projet').fill(ctx.contexte)
    await this.page.getByRole('button', { name: 'Analyser le besoin' }).click()
  }

  modeSuggere(): Locator {
    return this.page.getByText('Mode de sollicitation suggéré :')
  }

  clausesEligiblesTitre(): Locator {
    return this.page.getByRole('heading', { name: /Clauses éligibles/ })
  }

  async generer(titre: string): Promise<void> {
    await this.page.getByLabel('Titre du document').fill(titre)
    await this.page.getByRole('button', { name: 'Générer le document' }).click()
  }
}
