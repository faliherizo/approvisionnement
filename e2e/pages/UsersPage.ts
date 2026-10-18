import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Gestion des utilisateurs d'un tenant (admin) : liste, recherche, RBAC. */
export class UsersPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: /Utilisateurs/ })
  }

  async rechercher(terme: string): Promise<void> {
    await this.page.getByPlaceholder('Recherche (courriel, nom)…').fill(terme)
  }

  ligneUtilisateur(email: string): Locator {
    return this.page.getByText(email)
  }
}
