import { Download, Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Éditeur d'un document : clauses, assemblage, approbation. */
export class DocumentEditorPage extends BasePage {
  codeClause(code: string): Locator {
    return this.page.getByText(code, { exact: true }).first()
  }

  async assembler(): Promise<void> {
    await this.page.getByRole('button', { name: 'Assembler le devis' }).click()
  }

  async demarrerApprobation(): Promise<void> {
    await this.page.getByRole('button', { name: "Démarrer l'approbation" }).click()
  }

  /** Approuve toutes les étapes en attente du circuit (jusqu'à validation complète du document). */
  async approuverTout(): Promise<void> {
    const boutons = this.page.getByRole('button', { name: 'Approuver' })
    for (let i = 0; i < 8 && (await boutons.count()) > 0; i++) {
      await boutons.first().click()
      await this.page.waitForTimeout(700) // le document se recharge après chaque décision
    }
  }

  /** Badge de statut du document dans l'en-tête (ex. « APPROUVE »). */
  badgeStatut(statut: string): Locator {
    return this.page.locator(`.badge.s-${statut}`).first()
  }

  /** Publie le document vers une cible externe (M365/SAP), avec répertoire optionnel, et attend la confirmation. */
  async publier(cible: string, format: string, destination?: string): Promise<void> {
    await this.page.getByRole('heading', { name: 'Publication (Microsoft 365 / SAP)' }).scrollIntoViewIfNeeded()
    await this.page.getByLabel('Cible').selectOption(cible)
    await this.page.getByLabel('Format').selectOption(format)
    if (destination !== undefined) {
      await this.page.getByLabel('Répertoire de destination').fill(destination)
    }
    await this.page.getByRole('button', { name: 'Publier' }).click()
  }

  historiquePublications(): Locator {
    return this.page.getByRole('table', { name: 'Historique des publications' })
  }

  /** Panneau du circuit d'approbation (pour vérifier les niveaux). */
  circuitApprobation(): Locator {
    return this.page.locator('.panel', {
      has: this.page.getByRole('heading', { name: "Circuit d'approbation" }),
    })
  }

  /** Panneau des annexes du contrat. */
  panneauAnnexes(): Locator {
    return this.page.locator('.panel', {
      has: this.page.getByRole('heading', { name: 'Annexes' }),
    })
  }

  async ajouterAnnexe(titre: string): Promise<void> {
    await this.panneauAnnexes().getByRole('button', { name: 'Ajouter une annexe' }).click()
    const modale = this.page.getByRole('dialog')
    await modale.getByLabel('Titre').fill(titre)
    await modale.getByRole('button', { name: 'Valider' }).click()
  }

  /** Panneau du bordereau de prix. */
  panneauBordereau(): Locator {
    return this.page.locator('.panel', {
      has: this.page.getByRole('heading', { name: 'Bordereau de prix' }),
    })
  }

  async ajouterLigneBordereau(description: string, quantite: number, taux: number): Promise<void> {
    const panneau = this.panneauBordereau()
    await panneau.getByRole('button', { name: 'Ajouter une ligne' }).click()
    const ligne = panneau.locator('tbody tr').last()
    await ligne.locator('input').nth(0).fill(description)
    await ligne.locator('input').nth(1).fill(String(quantite))
    await ligne.locator('input').nth(2).fill(String(taux))
  }

  async enregistrerBordereau(): Promise<void> {
    await this.panneauBordereau().getByRole('button', { name: 'Enregistrer' }).click()
  }

  totalBordereau(): Locator {
    return this.panneauBordereau().locator('tfoot')
  }

  descriptionLigneBordereau(index: number): Locator {
    return this.panneauBordereau().locator('tbody tr').nth(index).locator('input').first()
  }

  /** Recharge la page pour vérifier la persistance côté serveur. */
  async recharger(): Promise<void> {
    await this.page.reload()
  }

  async ajouterSignataire(nom: string, courriel: string, role: string): Promise<void> {
    await this.page.getByRole('button', { name: 'Ajouter un signataire' }).click()
    const m = this.page.getByRole('dialog')
    await m.getByLabel('Nom', { exact: true }).fill(nom)
    await m.getByLabel('Courriel').fill(courriel)
    await m.getByLabel(/Rôle/).fill(role)
    await m.getByRole('button', { name: 'Valider' }).click()
  }

  async demarrerSignature(): Promise<void> {
    await this.page.getByRole('button', { name: 'Démarrer la signature' }).click()
  }

  /** Ouvre le menu déroulant « Générer » s'il ne l'est pas déjà (idempotent). */
  async ouvrirMenuGenerer(): Promise<void> {
    const trigger = this.page.getByRole('button', { name: 'Générer' })
    if ((await trigger.getAttribute('aria-expanded')) !== 'true') await trigger.click()
  }

  async ouvrirApercuPdf(): Promise<void> {
    await this.ouvrirMenuGenerer()
    await this.page.getByRole('button', { name: 'Aperçu PDF', exact: true }).click()
  }

  /** Choisit le gabarit normalisé à générer (ex. « Bordereau de prix »). */
  async choisirGabarit(libelle: string): Promise<void> {
    await this.ouvrirMenuGenerer()
    await this.page.getByLabel('Gabarit du document à générer').selectOption({ label: libelle })
  }

  /** Déclenche l'export PDF et retourne le téléchargement (nom + contenu). */
  async telechargerPdf(): Promise<Download> {
    await this.ouvrirMenuGenerer()
    const [download] = await Promise.all([
      this.page.waitForEvent('download'),
      this.page.getByRole('button', { name: 'Export PDF', exact: true }).click(),
    ])
    return download
  }

  apercuPdf(): Locator {
    return this.page.locator('img.pdf-apercu')
  }

  /** Copie le lien public d'un signataire et retourne son URL (via le presse-papier). */
  async copierLienSignature(nom: string): Promise<string> {
    const ligne = this.page.locator('li', { hasText: nom })
    await ligne.getByRole('button', { name: 'Copier le lien' }).click()
    return this.page.evaluate(() => navigator.clipboard.readText())
  }
}
