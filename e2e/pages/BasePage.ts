import { Page, Locator } from '@playwright/test'

/** Groupe du menu déroulant contenant chaque destination (nav regroupée). */
const GROUPE_NAV: Record<string, string> = {
  'Documents': 'Documents', 'Dossiers': 'Documents', 'Assistant guidé': 'Documents',
  'Bibliothèque': 'Documents', 'Recherche': 'Documents',
  'Tableau de bord': 'Pilotage', 'Assistant IA': 'Pilotage', 'Rapports de suivi': 'Pilotage',
  'Dépôts simulés': 'Pilotage',
  'Utilisateurs': 'Administration', 'Paramètres': 'Administration', 'Gabarits': 'Administration',
  'Circuits d’approbation': 'Administration', 'Journal d’audit': 'Administration',
  'Données & IA': 'Administration',
}

/** Comportements communs à toutes les pages (navigation, session). */
export abstract class BasePage {
  constructor(protected readonly page: Page) {}

  lienNav(nom: string): Locator {
    return this.page.getByRole('link', { name: nom })
  }

  /** Ouvre le groupe de menu si nécessaire, puis suit le lien. */
  async naviguerVers(nom: string): Promise<void> {
    const groupe = GROUPE_NAV[nom]
    if (groupe) await this.page.getByRole('button', { name: groupe }).click()
    await this.lienNav(nom).click()
  }

  async seDeconnecter(): Promise<void> {
    await this.page.getByRole('button', { name: 'Déconnexion' }).click()
  }
}
