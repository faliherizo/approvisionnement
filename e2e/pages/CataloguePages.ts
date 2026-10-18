import { Locator } from '@playwright/test'
import { BasePage } from './BasePage'

/** Bibliothèque de clauses. */
export class ClausesPage extends BasePage {
  titre(): Locator {
    return this.page.getByRole('heading', { name: /Bibliothèque de clauses/ })
  }
  clause(code: string): Locator {
    return this.page.getByText(code, { exact: true }).first()
  }

  /** La modale d'édition/création de clause. */
  private modale(): Locator {
    return this.page.getByRole('dialog')
  }

  /** Crée une clause (WYSIWYG pour le corps), avec options (catégorie, exclusions, condition d'éligibilité). */
  async creerClause(code: string, titre: string, corps: string,
                    opts: { categorie?: string; exclusions?: string;
                            condition?: { attribut: string; valeur: string } } = {}): Promise<void> {
    await this.page.getByRole('button', { name: 'Nouvelle clause' }).click()
    const m = this.modale()
    await m.getByLabel('Code', { exact: true }).fill(code)
    await m.getByLabel('Titre', { exact: true }).fill(titre)
    if (opts.categorie) await m.getByLabel('Catégorie').selectOption(opts.categorie)
    await m.locator('.rte-zone').first().fill(corps)
    if (opts.exclusions) await m.getByLabel(/Exclusions/).fill(opts.exclusions)
    if (opts.condition) await this.ajouterCondition(opts.condition.attribut, opts.condition.valeur)
    await m.getByRole('button', { name: 'Enregistrer' }).click()
  }

  /** Ajoute une condition d'éligibilité dans la modale ouverte (attribut = libellé, valeur = code/texte). */
  async ajouterCondition(attributLibelle: string, valeur: string): Promise<void> {
    const m = this.modale()
    await m.getByRole('button', { name: 'Ajouter une condition' }).click()
    const ligne = m.locator('.cond-row').last()
    await ligne.locator('select').first().selectOption({ label: attributLibelle })
    const champ = ligne.locator('select, input').nth(1)
    // Sélecteur (type/mode) ou saisie libre (contexte/valeur) selon l'attribut.
    if (await ligne.locator('select').count() > 1) await champ.selectOption(valeur)
    else await champ.fill(valeur)
  }

  async rechercher(terme: string): Promise<void> {
    await this.page.locator('.search-input').fill(terme)
  }

  async ouvrirEdition(code: string): Promise<void> {
    await this.carte(code).getByRole('button', { name: 'Éditer' }).click()
  }

  async supprimer(code: string): Promise<void> {
    await this.carte(code).getByRole('button', { name: 'Supprimer' }).click()
    await this.page.getByRole('dialog').getByRole('button', { name: 'Confirmer' }).click()
  }

  /** Renomme le titre dans la modale d'édition ouverte puis enregistre. */
  async modifierTitre(nouveauTitre: string): Promise<void> {
    const m = this.modale()
    await m.getByLabel('Titre', { exact: true }).fill(nouveauTitre)
    await m.getByRole('button', { name: 'Enregistrer' }).click()
  }

  /** Carte d'une clause (pour vérifier ses relations affichées). */
  carte(code: string): Locator {
    return this.page.locator('.clause', { has: this.page.getByText(code, { exact: true }) })
  }
}

/** Recherche plein texte (avec actions consulter / éditer / supprimer). */
export class SearchPage extends BasePage {
  async rechercher(terme: string): Promise<void> {
    await this.page.getByLabel('Terme').fill(terme)
    await this.page.getByRole('button', { name: 'Rechercher' }).click()
  }
  resultat(texte: string): Locator {
    return this.page.getByText(texte, { exact: true }).first()
  }

  /** Carte de clause d'un résultat. */
  carteClause(code: string): Locator {
    return this.page.locator('.clause', { has: this.page.getByText(code, { exact: true }) })
  }

  async consulterClause(code: string): Promise<void> {
    await this.carteClause(code).getByRole('button', { name: 'Consulter' }).click()
  }

  async editerClause(code: string): Promise<void> {
    await this.carteClause(code).getByRole('button', { name: 'Éditer' }).click()
  }

  /** Ligne document d'un résultat (par titre). */
  ligneDocument(titre: string): Locator {
    return this.page.locator('tr', { has: this.page.getByText(titre, { exact: true }) })
  }

  async supprimerDocument(titre: string): Promise<void> {
    await this.ligneDocument(titre).getByRole('button', { name: 'Supprimer' }).click()
    await this.page.getByRole('dialog').getByRole('button', { name: 'Confirmer' }).click()
  }
}

/** Tableau de bord analytique. */
export class StatsPage extends BasePage {
  kpiDocuments(): Locator {
    return this.page.getByText('Documents', { exact: true })
  }
  titreRepartitionStatut(): Locator {
    return this.page.getByRole('heading', { name: 'Documents par statut' })
  }
}
