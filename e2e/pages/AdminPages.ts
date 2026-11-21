import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Journal d'audit (art. 18) : consultation filtrée et export CSV. */
export class AuditPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Journal d’audit' })
  }

  async filtrer(action: string): Promise<void> {
    await this.page.getByLabel('Action').fill(action)
    await this.page.getByRole('button', { name: 'Filtrer' }).click()
  }

  table(): Locator {
    return this.page.getByRole('table', { name: 'Événements du journal d’audit' })
  }
}

/** Circuits d'approbation configurables (art. 7.5, ADR-005). */
export class WorkflowsPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Circuits d’approbation' })
  }

  async creer(nom: string, type: string, roles: string[]): Promise<void> {
    await this.page.getByLabel('Nom', { exact: true }).fill(nom)
    await this.page.getByLabel('Type de document').selectOption(type)
    // Réinitialise la sélection par défaut puis coche les rôles demandés.
    for (const r of ['APPRO', 'JURIDIQUE', 'DIRECTION']) {
      const box = this.page.getByRole('checkbox', { name: new RegExp(`^${r}`) })
      if ((await box.isChecked()) !== roles.includes(r)) await box.click()
    }
    await this.page.getByRole('button', { name: 'Créer le circuit' }).click()
  }

  ligne(nom: string): Locator {
    return this.page.getByRole('row', { name: new RegExp(nom) })
  }

  async editer(nom: string): Promise<void> {
    await this.ligne(nom).first().getByRole('button', { name: /Éditer/ }).click()
  }

  async enregistrerModification(): Promise<void> {
    await this.page.getByRole('button', { name: 'Enregistrer les modifications' }).click()
  }
}

/** Données & IA (P6) : import/migration, réversibilité, encadrement IA. */
export class DonneesIAPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Import / migration de contenus' })
  }

  async migrationEssai(csv: string): Promise<void> {
    await this.page.getByLabel('Contenu CSV').fill(csv)
    await this.page.getByRole('button', { name: /Migration d['’]essai/ }).click()
  }

  rapport(): Locator {
    return this.page.locator('.ai-card[aria-live="polite"]')
  }

  etatIa(): Locator {
    return this.page.getByRole('heading', { name: /Encadrement de l['’]assistant IA/ })
      .locator('xpath=following-sibling::p[contains(., "État")]')
  }

  async basculerIa(): Promise<void> {
    await this.page.getByRole('button', { name: /assistant IA/ }).click()
  }

  async analyserWord(cheminFichier: string): Promise<void> {
    await this.page.getByLabel('Fichier Word').setInputFiles(cheminFichier)
    await this.page.getByRole('button', { name: 'Analyser (essai)' }).click()
  }

  async importerWord(nom: string, cheminFichier: string): Promise<void> {
    await this.page.getByLabel('Nom du modèle').fill(nom)
    await this.page.getByLabel('Fichier Word').setInputFiles(cheminFichier)
    await this.page.getByRole('button', { name: 'Importer le modèle' }).click()
  }
}

/** Rapports de suivi consolidés (Addenda 1). */
export class RapportsPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Rapports de suivi', level: 1 })
  }

  indicateurs(): Locator {
    return this.page.getByLabel('Indicateurs clés')
  }
}
