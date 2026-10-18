import { test, expect } from '@playwright/test'
import { LoginPage, AssistantPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Assistant guidé de génération (art. 7.2)', () => {

  test('analyser un besoin puis générer le document (sélection manuelle)', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)

    const assistant = new AssistantPage(page)
    await assistant.naviguerVers('Assistant guidé')
    await expect(assistant.titre()).toBeVisible()

    // 1) Analyse : mode suggéré + clauses éligibles apparaissent.
    await assistant.analyser({
      type: 'APPEL_OFFRES_PUBLIC',
      categorieAcquisition: 'TECHNOLOGIES',
      valeur: '300000',
      contexte: 'acquisition logicielle',
    })
    await expect(assistant.modeSuggere()).toContainText('APPEL_OFFRES_PUBLIC')
    await expect(assistant.clausesEligiblesTitre()).toBeVisible()

    // 2) Génération → redirection vers l'éditeur du document créé.
    await assistant.generer(`Document guidé ${Date.now()}`)
    await expect(page).toHaveURL(/\/documents\/\d+$/)
  })
})
