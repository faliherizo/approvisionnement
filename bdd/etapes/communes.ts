import { expect } from '@playwright/test'
import { Given, When, Then } from './fixtures'
import { cas, ecrans } from '../outils/donnees'
import { executer, substituer } from '../outils/cas'
import { connecterUi, ecartsEcran, preparerOrganisation } from '../outils/ui'

/** Étapes communes, en langage d'analyste : qui agit, ce qu'il fait, ce que la plateforme doit montrer ou rendre. */



Given('je suis connecté en tant que {string}', async ({ page, monde }, profil: string) => {
  await connecterUi(page, profil)
  monde.variables.profil = profil
})

Given('j\'agis en tant que {string}', async ({ monde }, profil: string) => {
  monde.variables.profil = profil
})

/** Organisation dédiée aux essais (créée une fois, modules activés) : aucune autre organisation n'est modifiée. */
Given('l\'organisation {string} est prête', async ({ playwright, baseURL }, nom: string) => {
  await preparerOrganisation(playwright, baseURL!, nom)
})

Given('j\'utilise un téléphone', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
})

When('j\'exécute le cas {string}', async ({ playwright, baseURL, monde }, nom: string) => {
  const c = cas[nom]
  if (!c) throw new Error(`Cas inconnu : « ${nom} » (bdd/donnees/*.json)`)
  // Le profil déclaré par le cas prime : il décrit qui agit ; à défaut, la personne connectée dans le scénario.
  const profil = c.profil ?? (monde.variables.profil as string | undefined)
  if (!profil && !c.profil) throw new Error(`Cas « ${nom} » : aucun profil (étape « je suis connecté en tant que … » ou champ profil)`)
  await executer(playwright, baseURL!, profil ?? c.profil!, nom, c, monde)
})

Then('la plateforme se comporte comme attendu', async ({ monde }) => {
  expect(monde.ecarts, 'écarts relevés par les vérifications des cas').toEqual([])
})

When('j\'ouvre l\'écran {string}', async ({ page, monde }, nom: string) => {
  const e = ecrans[nom]
  if (!e) throw new Error(`Écran inconnu : « ${nom} » (bdd/donnees/*.json)`)
  if (e.largeur) await page.setViewportSize({ width: e.largeur, height: 900 })
  await page.goto(substituer(e.chemin, monde.variables))
  await expect(page.locator('main')).toBeVisible()
  await expect(page.getByRole('heading', { name: new RegExp(substituer(e.titre, monde.variables), 'i') }).first()).toBeVisible()
})

When('je saisis {string} dans le champ {string}', async ({ page, monde }, valeur: string, champ: string) => {
  await page.getByRole('searchbox', { name: new RegExp(champ, 'i') }).or(page.getByLabel(new RegExp(champ, 'i'))).first()
    .fill(substituer(valeur, monde.variables))
})

When('je clique sur {string}', async ({ page, monde }, libelle: string) => {
  const nom = new RegExp(substituer(libelle, monde.variables), 'i')
  await page.getByRole('button', { name: nom }).or(page.getByRole('link', { name: nom })).first().click()
})

When('je choisis {string} dans la liste {string}', async ({ page, monde }, choix: string, liste: string) => {
  const select = page.getByRole('combobox', { name: new RegExp(`^${liste}$`, 'i') }).first()
  const option = await select.locator('option').filter({ hasText: substituer(choix, monde.variables) }).first().getAttribute('value')
  await select.selectOption(option!)
})

When('je choisis la langue {string}', async ({ page }, langue: string) => {
  const choix = page.getByRole('combobox', { name: /Langue|Language|Fiteny/ }).first()
  const option = await choix.locator('option').filter({ hasText: langue }).first().getAttribute('value')
  await choix.selectOption(option!)
})

When('je choisis le tableau de bord {string}', async ({ page, monde }, nom: string) => {
  const choix = page.getByRole('combobox', { name: /Tableau de bord|Dashboard/ }).first()
  const option = await choix.locator('option').filter({ hasText: substituer(nom, monde.variables) }).first().getAttribute('value')
  await choix.selectOption(option!)
})

Then('je vois {string}', async ({ page, monde }, texte: string) => {
  await expect(page.getByText(substituer(texte, monde.variables), { exact: false }).first()).toBeVisible()
})

Then('je vois le bouton {string}', async ({ page }, nom: string) => {
  await expect(page.getByRole('button', { name: new RegExp(nom, 'i') }).first()).toBeVisible()
})

Then('je vois le lien {string}', async ({ page }, nom: string) => {
  await expect(page.getByRole('link', { name: new RegExp(nom, 'i') }).first()).toBeVisible()
})

Then('je ne vois pas le lien {string}', async ({ page }, nom: string) => {
  await expect(page.getByRole('link', { name: new RegExp(`^${nom}$`, 'i') })).toHaveCount(0)
})

/** SGQRI 008 3.0 / WCAG 2.2 AA aux quatre largeurs de référence (outils/ui.ts). */
Then('l\'écran est accessible, sans débordement ni superposition', async ({ page }) => {
  expect(await ecartsEcran(page), 'mise en page et accessibilité aux largeurs de référence').toEqual([])
})
