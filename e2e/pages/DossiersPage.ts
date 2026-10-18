import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Dossiers d'approvisionnement : liste, création, rattachement de documents, rapport (art. 15). */
export class DossiersPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: 'Dossiers d’approvisionnement' })
  }

  async creerDossier(d: { titre: string; numeroAO?: string; typeAcquisition?: string }): Promise<void> {
    await this.page.getByRole('heading', { name: 'Nouveau dossier' }).scrollIntoViewIfNeeded()
    await this.page.getByLabel('Titre', { exact: true }).fill(d.titre)
    if (d.numeroAO) await this.page.getByLabel('N° d’appel d’offres').fill(d.numeroAO)
    if (d.typeAcquisition) await this.page.getByLabel('Type d’acquisition').selectOption(d.typeAcquisition)
    await this.page.getByRole('button', { name: 'Créer le dossier' }).click()
  }

  ligneDossier(titre: string): Locator {
    return this.page.getByRole('row', { name: new RegExp(titre) })
  }

  async ouvrir(titre: string): Promise<void> {
    await this.ligneDossier(titre).first().click()
  }

  detail(): Locator {
    return this.page.getByRole('heading', { name: /^Dossier :/ })
  }

  rapport(): Locator {
    return this.page.getByLabel('Rapport de suivi du dossier')
  }

  async rattacher(titreDocument: string): Promise<void> {
    await this.page.getByLabel('Rattacher un document existant').selectOption({ label: titreDocument })
    await this.page.getByRole('button', { name: 'Rattacher au dossier' }).click()
  }

  documentsRattaches(): Locator {
    return this.page.getByRole('table', { name: 'Documents rattachés au dossier' })
  }

  async changerStatut(statut: string): Promise<void> {
    await this.page.getByLabel('Statut du dossier').selectOption(statut)
  }

  async transmettreSap(): Promise<void> {
    await this.page.getByRole('button', { name: 'Transmettre la référence à SAP' }).click()
  }

  async modifier(nouveauTitre: string): Promise<void> {
    await this.page.getByRole('button', { name: 'Modifier', exact: true }).click()
    await this.page.getByLabel('Titre', { exact: true }).fill(nouveauTitre)
    await this.page.getByRole('button', { name: 'Enregistrer les modifications' }).click()
  }

  async supprimer(): Promise<void> {
    await this.page.getByRole('button', { name: 'Supprimer le dossier' }).click()
    await this.page.getByRole('dialog').getByRole('button', { name: 'Confirmer' }).click()
  }

  resultatSap(): Locator {
    return this.page.locator('.sap-ref output')
  }
}
