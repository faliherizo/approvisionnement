import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'
import { TestUser } from '../models/types'

/** Page de connexion (mot de passe + MFA + SSO). */
export class LoginPage extends BasePage {
  async ouvrir(): Promise<void> {
    await this.page.goto('/login')
  }

  titre(): Locator {
    return this.page.getByRole('heading', { name: 'VINA IT Solutions inc.' })
  }

  boutonConnexion(): Locator {
    return this.page.getByRole('button', { name: 'Se connecter', exact: true })
  }

  erreurIdentifiants(): Locator {
    return this.page.getByText('Identifiants invalides')
  }

  boutonSecondFacteur(): Locator {
    return this.page.getByRole('button', { name: 'Valider le second facteur' })
  }

  async saisirIdentifiants(u: TestUser): Promise<void> {
    await this.page.getByLabel('Courriel').fill(u.email)
    await this.page.getByLabel('Mot de passe').fill(u.motDePasse)
  }

  /** Connexion complète (sans MFA). */
  async connecter(u: TestUser): Promise<void> {
    await this.ouvrir()
    await this.saisirIdentifiants(u)
    await this.boutonConnexion().click()
  }

  async saisirMotDePasse(motDePasse: string): Promise<void> {
    await this.page.getByLabel('Mot de passe').fill(motDePasse)
  }

  /** Saisit le code TOTP et valide le second facteur. */
  async saisirTotp(code: string): Promise<void> {
    await this.page.getByLabel(/Code d'authentification/).fill(code)
    await this.boutonSecondFacteur().click()
  }

  /** Répond à la question secrète dynamique affichée (champ hors courriel/mot de passe). */
  async repondreQuestionSecrete(reponse: string): Promise<void> {
    await this.page
      .locator('.login-card input:not([type=email]):not([type=password])')
      .last()
      .fill(reponse)
    await this.boutonSecondFacteur().click()
  }
}
