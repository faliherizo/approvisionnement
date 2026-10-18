import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Espace superadmin : gestion des tenants. */
export class SuperadminPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: /Espace superadmin/ })
  }

  async creerTenant(slug: string, titre: string): Promise<void> {
    await this.page.getByLabel('Slug (sous-domaine)').fill(slug)
    await this.page.getByLabel('Titre de connexion').fill(titre)
    await this.page.getByRole('button', { name: 'Créer le tenant' }).click()
  }

  celluleSlug(slug: string): Locator {
    return this.page.locator('code', { hasText: slug })
  }

  /** Panneau « Thème & design par tenant ». */
  private panneauTheme(): Locator {
    return this.page.locator('.panel', {
      has: this.page.getByRole('heading', { name: /Thème & design/ }),
    })
  }

  /** Choisit le premier tenant réel puis téléverse un logo image (documents). */
  async televerserLogo(png: Buffer): Promise<void> {
    const panneau = this.panneauTheme()
    await panneau.getByLabel('Tenant').selectOption({ index: 1 })
    await panneau.getByLabel(/Logo image/).setInputFiles({
      name: 'logo.png', mimeType: 'image/png', buffer: png,
    })
  }

  logoApercu(): Locator {
    return this.panneauTheme().locator('.logo-apercu img')
  }

  async enregistrerTheme(): Promise<void> {
    await this.panneauTheme().getByRole('button', { name: 'Enregistrer le thème' }).click()
  }

  /** Panneau « Modèle de document par tenant ». */
  private panneauModele(): Locator {
    return this.page.locator('.panel', {
      has: this.page.getByRole('heading', { name: /Modèle de document/ }),
    })
  }

  /** Définit le modèle de document d'un tenant précis (le premier réel) puis enregistre. */
  async definirModeleTenant(police: string, marque: string): Promise<void> {
    const panneau = this.panneauModele()
    await panneau.getByLabel('Tenant').selectOption({ index: 1 })
    await panneau.getByLabel('Police').selectOption(police)
    await panneau.getByLabel(/Marque/).fill(marque)
    await panneau.getByRole('button', { name: 'Enregistrer le modèle' }).click()
  }
}
