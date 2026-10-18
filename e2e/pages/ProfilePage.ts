import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Profil personnel (self-service). */
export class ProfilePage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Mon profil' })
  }
  courriel(email: string): Locator {
    return this.page.getByText(email)
  }
}
