import { test, expect } from '@playwright/test'
import { LoginPage, DashboardPage } from '../pages'
import { users } from '../models/fixtures'
import { genererTotp } from '../models/totp'

test.describe('Authentification (login / mot de passe)', () => {

  test('connexion réussie avec un compte rédacteur', async ({ page }) => {
    const login = new LoginPage(page)
    await login.ouvrir()
    await expect(login.titre()).toBeVisible()
    // SSO Microsoft masqué tant qu'aucun client OIDC « azure » n'est configuré (évite un 403).
    await expect(page.getByRole('button', { name: /Microsoft/ })).toHaveCount(0)
    await login.connecter(users.redacteur)

    const dashboard = new DashboardPage(page)
    // La navigation regroupée expose le menu « Documents » (bouton déroulant).
    await expect(page.getByRole('button', { name: 'Documents' })).toBeVisible()
    await expect(dashboard.titre()).toBeVisible()
  })

  test('rejet des identifiants invalides', async ({ page }) => {
    const login = new LoginPage(page)
    await login.ouvrir()
    await login.saisirMotDePasse('mauvais-mdp')
    await login.boutonConnexion().click()
    await expect(login.erreurIdentifiants()).toBeVisible()
  })

  test('accès protégé : redirection vers /login sans session', async ({ page }) => {
    await page.goto('/clauses')
    await expect(page).toHaveURL(/\/login$/)
  })

  test('connexion administrateur puis déconnexion (session cookie)', async ({ page }) => {
    const login = new LoginPage(page)
    await login.connecter(users.admin)
    const dashboard = new DashboardPage(page)
    await expect(dashboard.titre()).toBeVisible()

    // La session cookie survit au rechargement (restaurée via /auth/me).
    await page.reload()
    await expect(dashboard.titre()).toBeVisible()

    // Déconnexion → cookie effacé, accès protégé redirige vers /login.
    await dashboard.seDeconnecter()
    await expect(page).toHaveURL(/\/login$/)
    await page.goto('/clauses')
    await expect(page).toHaveURL(/\/login$/)
  })

  test('MFA question secrète dynamique : second facteur exigé puis validé', async ({ page }) => {
    const login = new LoginPage(page)
    await login.connecter(users.question)

    await expect(login.boutonSecondFacteur()).toBeVisible()
    await login.repondreQuestionSecrete(users.question.reponseSecrete!)

    const dashboard = new DashboardPage(page)
    await expect(dashboard.titre()).toBeVisible()
  })

  test('MFA TOTP : code calculé (RFC 6238) accepté', async ({ page }) => {
    const login = new LoginPage(page)
    await login.connecter(users.totp)
    await expect(login.boutonSecondFacteur()).toBeVisible()
    await login.saisirTotp(genererTotp(users.totp.totpSecret!))
    await expect(new DashboardPage(page).titre()).toBeVisible()
  })
})
