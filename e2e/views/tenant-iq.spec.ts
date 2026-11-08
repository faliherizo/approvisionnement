import { test, expect, Page } from '@playwright/test'
import { users } from '../models/fixtures'
import { TestUser } from '../models/types'
import { activerModules, auditerAxe, connecter, controler } from './clm.outils'

/**
 * Tenant Investissement Québec (AO 2026-6370-0005) : chaque profil demandé par le client ne voit que l'espace du module CLM —
 * menus, pages et API. Aucune fonctionnalité d'un autre module (socle de rédaction de Lévis, formulaires, RH, paie…) n'est
 * atteignable : ni par la navigation, ni par une adresse saisie, ni par un appel direct de l'API.
 */
const PRINCIPAUX = ['Tableau de bord', 'Mes trousses', 'Mes tâches', 'Modèles', 'Suivi centralisé']
const ADMINISTRATION = ['Notifications et rappels', 'Intégrations API', 'Paramètres et environnements', 'Listes de référence', 'Utilisateurs et groupes',
  'Journal d’audit', 'Options CLM et IA']

type Profil = { profil: string; compte: TestUser; administration: string[]; initie: boolean }
const PROFILS: Profil[] = [
  { profil: 'Initiateur', compte: users.iqInitiateur, administration: [], initie: true },
  { profil: 'Validateur', compte: users.iqValidateur, administration: [], initie: true },
  { profil: 'Approbateur', compte: users.iqApprobateur, administration: [], initie: true },
  { profil: 'Signataire interne', compte: users.iqSignataire, administration: [], initie: false },
  { profil: 'Administrateur d’affaires', compte: users.iqAdminAffaires, administration: ADMINISTRATION, initie: true },
  { profil: 'Administrateur technique', compte: users.iqAdminTi, administration: ADMINISTRATION, initie: true },
  { profil: 'Auditeur / conformité', compte: users.iqAuditeur, administration: ['Journal d’audit'], initie: false },
]

/** Une lecture représentative par fonctionnalité hors module : socle de rédaction, puis autres modules de la plateforme. */
const API_HORS_MODULE = ['/api/dossiers', '/api/documents', '/api/clauses', '/api/templates', '/api/gabarits', '/api/stats',
  '/api/search?q=contrat', '/api/ia/knowledge', '/api/reference/TYPE_CONTRAT', '/api/formulaires', '/api/mes-demandes',
  '/api/rh/employes', '/api/paie/taux', '/api/applications']

/**
 * Pages de la plateforme hors module : saisies directement, elles ramènent à l'accueil du module. « /api-docs » n'y figure pas :
 * le mandataire de développement envoie tout chemin « /api… » au serveur (en production, nginx ne relaie que « /api/ »).
 */
const PAGES_HORS_MODULE = ['/', '/dossiers', '/clauses', '/assistant', '/recherche', '/tableau-de-bord', '/rapports', '/chat-ia',
  '/simulateur', '/templates', '/gabarits', '/workflows', '/donnees', '/documents/1', '/formulaires', '/mes-demandes',
  '/nouvelle-demande', '/taches', '/approbations', '/rh/personnel', '/rh/feuilles-temps', '/paie', '/impot', '/declarations',
  '/applications', '/notifications', '/interop', '/securite-avancee', '/superadmin']

/** Pages transverses de la plateforme et leur équivalent dans l'espace du module. */
const TRANSVERSES: Record<string, string> = {
  '/profil': '/clm/profil', '/securite': '/clm/securite', '/utilisateurs': '/clm/admin/utilisateurs',
  '/audit': '/clm/admin/audit', '/parametres': '/clm/admin/sso',
}

async function ouvrirSession(page: Page, compte: TestUser) {
  await page.context().clearCookies()
  await connecter(page, compte)
  await expect(page).toHaveURL(/\/clm$/)
}

const actifs = (modules: { code: string; actif: boolean }[]) => modules.filter((m) => m.actif).map((m) => m.code).sort()

test.describe.configure({ timeout: 300_000 })

test.describe('Tenant IQ — chaque profil n’a que l’espace du module CLM', () => {
  let reference = ''

  test.beforeAll(async ({ playwright, baseURL, request }) => {
    await activerModules(request)
    // Une trousse de l'initiatrice IQ : l'auditeur la consulte, le signataire interne non désigné ne la voit pas.
    const api = await playwright.request.newContext({ baseURL })
    expect((await api.post('/api/auth/login', { data: users.iqInitiateur })).ok()).toBeTruthy()
    const trousse = await (await api.post('/api/clm/trousses', {
      data: { titre: 'Entente Essor — démonstration IQ', typeContrat: 'SERVICES', unite: 'FINANCEMENT_ENTREPRISES' },
    })).json()
    reference = trousse.resume.reference
    await api.dispose()
  })

  for (const p of PROFILS) {
    test(`${p.profil} : menus, pages et API du seul module CLM`, async ({ page }) => {
      await ouvrirSession(page, p.compte)
      await expect(page.locator('#clm-navigation').getByRole('link')).toHaveText([...PRINCIPAUX, ...p.administration])
      await expect(page.locator('header.topbar'), 'aucun gabarit de la plateforme').toHaveCount(0)
      await expect(page.getByRole('link', { name: 'Retour à la plateforme' })).toHaveCount(0)
      await expect(page.getByRole('link', { name: 'Mon profil' })).toBeVisible()
      await expect(page.getByRole('link', { name: 'Créer une trousse' })).toHaveCount(p.initie ? 1 : 0)

      expect(actifs(await (await page.request.get('/api/modules')).json())).toEqual(['clm', 'clm_ia'])
      for (const url of API_HORS_MODULE) expect((await page.request.get(url)).status(), url).toBe(403)
      expect((await page.request.get('/api/clm/trousses')).status()).toBe(200)
      await controler(page)
      await auditerAxe(page)
    })
  }

  test('adresses saisies hors module : retour à l’espace CLM ; pages transverses servies dans l’espace', async ({ page }) => {
    await ouvrirSession(page, users.iqAdminTi)
    for (const chemin of PAGES_HORS_MODULE) {
      await page.goto(chemin)
      await expect(page, chemin).toHaveURL(/\/clm$/)
      await expect(page.locator('#clm-navigation')).toBeAttached()
    }
    for (const [chemin, equivalent] of Object.entries(TRANSVERSES)) {
      await page.goto(chemin)
      await expect(page, chemin).toHaveURL(new RegExp(`${equivalent}$`))
      await expect(page.locator('#clm-navigation')).toBeAttached()
      await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
    }
  })

  test('l’auditeur consulte toute l’organisation sans intervenir ; le signataire interne ne voit que ce qui le désigne', async ({ page }) => {
    await ouvrirSession(page, users.iqAuditeur)
    const toutes = await (await page.request.get('/api/clm/trousses')).json()
    expect(toutes.map((t: { reference: string }) => t.reference)).toContain(reference)
    expect(toutes.every((t: { proprietaire: string }) => t.proprietaire.endsWith('@iq.example')), 'aucune trousse d’une autre organisation')
      .toBeTruthy()
    await page.goto('/clm/trousses')
    await expect(page.getByRole('link', { name: `Ouvrir le détail de ${reference}` })).toBeVisible()
    await expect(page.getByRole('link', { name: `Modifier le brouillon ${reference}` })).toHaveCount(0)
    await page.goto('/clm/admin/journal')
    await expect(page.getByRole('heading', { level: 1, name: 'Journal d’audit et événements' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)

    await ouvrirSession(page, users.iqSignataire)
    const siennes = await (await page.request.get('/api/clm/trousses')).json()
    expect(siennes.map((t: { reference: string }) => t.reference)).not.toContain(reference)
    expect((await page.request.get('/api/clm/admin/journal')).status()).toBe(403)
  })

  test('Ville de Lévis inchangée : socle de rédaction et module CLM, sans aucune donnée IQ', async ({ page }) => {
    await page.context().clearCookies()
    await connecter(page, users.admin)
    await expect(page.getByRole('button', { name: 'Documents' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Pilotage' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Contrats & signature' })).toBeVisible()
    expect((await page.request.get('/api/dossiers')).status()).toBe(200)
    const trousses = await (await page.request.get('/api/clm/trousses')).json()
    expect(trousses.some((t: { proprietaire: string }) => t.proprietaire.endsWith('@iq.example')), 'aucune trousse IQ').toBeFalsy()
    await page.goto('/clm')
    await expect(page.getByRole('link', { name: 'Retour à la plateforme' })).toBeVisible()
  })
})
