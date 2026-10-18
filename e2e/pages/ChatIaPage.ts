import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Assistant IA spécialisé + base de connaissances (RAG). */
export class ChatIaPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: /Assistant IA/ })
  }

  async ajouterConnaissance(titre: string, contenu: string): Promise<void> {
    await this.page.getByLabel('Titre').fill(titre)
    await this.page.locator('.rte-zone').fill(contenu) // contenu WYSIWYG
    await this.page.getByRole('button', { name: 'Ajouter à la base de connaissances' }).click()
  }

  connaissance(titre: string): Locator {
    return this.page.getByText(titre)
  }

  async envoyerMessage(question: string): Promise<void> {
    await this.page.getByPlaceholder('Votre question juridique / de rédaction…').fill(question)
    await this.page.getByRole('button', { name: 'Envoyer' }).click()
  }

  /** Bulle de réponse de l'assistant (dernière). */
  reponseAssistant(): Locator {
    return this.page.locator('.bulle.ia').last()
  }
}
