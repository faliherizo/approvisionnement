import { test, expect, Download } from '@playwright/test'
import { readFile } from 'node:fs/promises'
import { extractText, getDocumentProxy } from 'unpdf'
import { LoginPage, DashboardPage, DocumentEditorPage } from '../pages'
import { users, documents } from '../models/fixtures'

/**
 * Validation e2e « contenu » : on génère les PDF via le frontend (menu Générer →
 * choix du gabarit → Export PDF), puis on extrait le texte du PDF téléchargé et on
 * assère les marqueurs propres à chaque gabarit normalisé (mêmes contenus/format que
 * les pièces jointes SCT). Complète les tests backend PDFBox par une preuve bout-en-bout.
 */
test.describe('Génération PDF par gabarit (contenu conforme aux pièces jointes)', () => {

  /** Extrait le texte de toutes les pages du PDF téléchargé. */
  async function texteDuPdf(download: Download): Promise<string> {
    const chemin = await download.path()
    const buffer = await readFile(chemin)
    const pdf = await getDocumentProxy(new Uint8Array(buffer))
    const { text } = await extractText(pdf, { mergePages: true })
    return text
  }

  test('un même document produit le bon contenu selon le gabarit choisi', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const dashboard = new DashboardPage(page)
    await dashboard.creerDocument({ ...documents.appelOffres, titre: `Contenu ${Date.now()}` })

    const editeur = new DocumentEditorPage(page)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
    await editeur.assembler()
    await editeur.ajouterLigneBordereau('Technicien spécialisé', 1800, 95)
    await editeur.enregistrerBordereau()
    await expect(page.getByText('Bordereau enregistré')).toBeVisible()

    // Gabarit « Contrat de services » : couverture APPEL D'OFFRES - SERVICES / CONTRAT.
    await editeur.choisirGabarit('Contrat de services')
    const contrat = await texteDuPdf(await editeur.telechargerPdf())
    expect(contrat).toContain("APPEL D'OFFRES - SERVICES")
    expect(contrat).toContain('CONTRAT')
    expect(contrat).toContain('Services de nature technique')

    // Gabarit « Régie » : même sur-label, libellé RÉGIE.
    await editeur.choisirGabarit('Régie')
    const regie = await texteDuPdf(await editeur.telechargerPdf())
    expect(regie).toContain("APPEL D'OFFRES - SERVICES")
    expect(regie).toContain('RÉGIE')

    // Gabarit « Bordereau de prix » : grille SCT + bloc signature, sans préambule contractuel.
    await editeur.choisirGabarit('Bordereau de prix')
    const bordereau = await texteDuPdf(await editeur.telechargerPdf())
    expect(bordereau).toContain('BORDEREAU DE PRIX')
    expect(bordereau).toContain('Taux')
    expect(bordereau).toContain('Technicien spécialisé')
    expect(bordereau).toContain('GRAND TOTAL DE LA SOUMISSION')
    expect(bordereau).toContain('SIGNATURE DU SOUMISSIONNAIRE')
  })
})
