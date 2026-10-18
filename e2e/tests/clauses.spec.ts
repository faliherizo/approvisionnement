import { test, expect } from '@playwright/test'
import { LoginPage, ClausesPage } from '../pages'
import { users } from '../models/fixtures'

test.describe('Bibliothèque de clauses (EF1/EF2)', () => {

  test('EF2 : créer une clause avec exclusion (WYSIWYG) et l\'afficher', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const clauses = new ClausesPage(page)
    await clauses.naviguerVers('Bibliothèque')
    await expect(clauses.titre()).toBeVisible()

    const code = `E2E-${Date.now()}`
    await clauses.creerClause(code, 'Clause exclusive', 'Texte de la clause', {
      categorie: 'OPTIONNELLE',
      exclusions: 'GEN-002',
    })
    // La carte de la clause affiche sa relation d'exclusion.
    await expect(clauses.carte(code)).toContainText('Exclut')
    await expect(clauses.carte(code)).toContainText('GEN-002')
  })

  test('définir une clause éligible par condition (moteur de règles dynamique)', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const clauses = new ClausesPage(page)
    await clauses.naviguerVers('Bibliothèque')

    const code = `COND-${Date.now()}`
    await clauses.creerClause(code, 'Clause gré à gré', 'Applicable en gré à gré', {
      categorie: 'STANDARD',
      condition: { attribut: 'Type de contrat', valeur: 'GRE_A_GRE' },
    })
    // La carte affiche la règle d'éligibilité définie par l'admin.
    await expect(clauses.carte(code)).toContainText('Éligibilité')
    await expect(clauses.carte(code)).toContainText('TYPE_CONTRAT=GRE_A_GRE')
  })

  test('éditer puis rechercher une clause', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const clauses = new ClausesPage(page)
    await clauses.naviguerVers('Bibliothèque')

    const code = `EDIT-${Date.now()}`
    await clauses.creerClause(code, 'Titre initial', 'Corps')
    await clauses.ouvrirEdition(code)
    await clauses.modifierTitre('Titre modifié')
    await expect(clauses.carte(code)).toContainText('Titre modifié')

    // Recherche : filtre la liste sur le code.
    await clauses.rechercher(code)
    await expect(clauses.carte(code)).toBeVisible()
  })

  test('un administrateur supprime une clause de la bibliothèque', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const clauses = new ClausesPage(page)
    await clauses.naviguerVers('Bibliothèque')

    const code = `DEL-${Date.now()}`
    await clauses.creerClause(code, 'À supprimer', 'Corps')
    await expect(clauses.carte(code)).toBeVisible()
    await clauses.supprimer(code)
    await expect(clauses.carte(code)).toHaveCount(0)
  })
})
