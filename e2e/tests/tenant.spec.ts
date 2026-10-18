import { test, expect } from '@playwright/test'
import { LoginPage, SuperadminPage, UsersPage, ProfilePage, ChatIaPage } from '../pages'
import { users, tenants } from '../models/fixtures'

test.describe('Multi-tenant, superadmin, utilisateurs, profil', () => {

  test('superadmin gère les tenants (création)', async ({ page }) => {
    await new LoginPage(page).connecter(users.superadmin)
    const superadmin = new SuperadminPage(page)
    await superadmin.naviguerVers('Espace superadmin')
    await expect(superadmin.titre()).toBeVisible()

    const slug = `t${Date.now()}`
    await superadmin.creerTenant(slug, tenants.nouveauTenant.titre)
    await expect(superadmin.celluleSlug(slug)).toBeVisible()
  })

  test('superadmin téléverse un logo de marque pour les documents générés', async ({ page }) => {
    await new LoginPage(page).connecter(users.superadmin)
    const superadmin = new SuperadminPage(page)
    await superadmin.naviguerVers('Espace superadmin')

    // PNG 1x1 valide (data minimal).
    const png = Buffer.from(
      'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==',
      'base64')
    await superadmin.televerserLogo(png)
    await expect(superadmin.logoApercu()).toBeVisible()
    await superadmin.enregistrerTheme()
    await expect(page.getByText(/Thème enregistré/)).toBeVisible()
  })

  test('superadmin définit le modèle de document d’un tenant', async ({ page }) => {
    await new LoginPage(page).connecter(users.superadmin)
    const superadmin = new SuperadminPage(page)
    await superadmin.naviguerVers('Espace superadmin')

    await superadmin.definirModeleTenant('COURIER', 'Ville de Lévis')
    await expect(page.getByText(/Modèle de document enregistré/)).toBeVisible()
  })

  test('admin définit le modèle de document de son entreprise', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    await page.getByRole('button', { name: 'Administration' }).click()
    await page.getByRole('link', { name: 'Paramètres' }).click()

    const modele = page.locator('.panel', {
      has: page.getByRole('heading', { name: /Modèle de document/ }),
    })
    await modele.getByLabel('Police').selectOption('TIMES')
    await modele.getByLabel(/Marque/).fill('Ville de Lévis')
    await modele.getByLabel(/Pied de page/).fill('Document confidentiel')
    await modele.getByRole('button', { name: 'Enregistrer le modèle' }).click()
    await expect(page.getByText(/Modèle de document enregistré/)).toBeVisible()
  })

  test('admin gère les utilisateurs (liste, recherche) sans voir le superadmin', async ({ page }) => {
    await new LoginPage(page).connecter(users.admin)
    const usersPage = new UsersPage(page)
    await usersPage.naviguerVers('Utilisateurs')
    await expect(usersPage.titre()).toBeVisible()

    // Le superadmin n'apparaît jamais dans la gestion d'un tenant.
    await expect(usersPage.ligneUtilisateur(users.superadmin.email)).toHaveCount(0)

    await usersPage.rechercher('juriste')
    await expect(usersPage.ligneUtilisateur(users.juriste.email)).toBeVisible()
  })

  test('un utilisateur consulte son profil', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const profil = new ProfilePage(page)
    await profil.naviguerVers('Profil')
    await expect(profil.titre()).toBeVisible()
    await expect(profil.courriel(users.redacteur.email)).toBeVisible()
  })

  test('assistant IA : ajout d\'une connaissance', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const chat = new ChatIaPage(page)
    await chat.naviguerVers('Assistant IA')
    await expect(chat.titre()).toBeVisible()

    const titre = `Note ${Date.now()}`
    await chat.ajouterConnaissance(titre, 'Extrait de référence juridique pour la rédaction.')
    await expect(chat.connaissance(titre)).toBeVisible()
  })

  test('assistant IA : le chat répond (résilient, mode hors-ligne)', async ({ page }) => {
    await new LoginPage(page).connecter(users.redacteur)
    const chat = new ChatIaPage(page)
    await chat.naviguerVers('Assistant IA')
    await chat.envoyerMessage('Quel est le seuil pour un appel d\'offres public ?')
    // Le chat répond toujours (réponse Ollama ou repli hors-ligne via la base RAG).
    await expect(chat.reponseAssistant()).toBeVisible()
  })
})
