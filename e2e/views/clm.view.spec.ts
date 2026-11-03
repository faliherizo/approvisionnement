import { test, expect, Page, APIRequestContext } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { LoginPage } from '../pages'
import { users } from '../models/fixtures'

/**
 * Vues CLM (IQ 2026-6370-0005, UI-01 à UI-24) dans le gabarit des maquettes, aux quatre largeurs de référence :
 * aucun débordement horizontal, aucun chevauchement, cibles ≥ 24 px, contrôles nommés, un seul landmark principal,
 * zéro violation axe-core (WCAG 2.2 A/AA). Ajouter un écran = ajouter une entrée à ECRANS.
 */
const LARGEURS = [390, 768, 1280, 1680]
const ECRANS: { nom: string; chemin: string; titre: RegExp; maquette: string }[] = [
  { nom: 'UI-01 tableau de bord', chemin: '/clm', titre: /Tableau de bord des contrats/, maquette: 'UI-01' },
  { nom: 'UI-02 trousses', chemin: '/clm/trousses', titre: /Trousses/, maquette: 'UI-02' },
  { nom: 'UI-03 créer une trousse', chemin: '/clm/trousses/nouvelle', titre: /Créer une trousse/, maquette: 'UI-03' },
  { nom: 'UI-07 modèles', chemin: '/clm/modeles', titre: /Catalogue des modèles/, maquette: 'UI-07' },
  { nom: 'UI-12 tâches', chemin: '/clm/taches', titre: /Boîte de tâches/, maquette: 'UI-12' },
  { nom: 'UI-18 suivi', chemin: '/clm/suivi', titre: /Suivi centralisé/, maquette: 'UI-18' },
  { nom: 'UI-17 notifications', chemin: '/clm/admin/notifications', titre: /Notifications et rappels/, maquette: 'UI-17' },
  { nom: 'UI-21 intégrations', chemin: '/clm/admin/integrations', titre: /Intégrations API/, maquette: 'UI-21' },
  { nom: 'UI-22 paramètres', chemin: '/clm/admin/parametres', titre: /Paramètres et environnements/, maquette: 'UI-22' },
  { nom: 'UI-20 utilisateurs', chemin: '/clm/admin/utilisateurs', titre: /Utilisateurs/, maquette: 'UI-20' },
  { nom: 'UI-23 journal', chemin: '/clm/admin/journal', titre: /Journal d’audit et événements/, maquette: 'UI-23' },
  { nom: 'UI-24 options', chemin: '/clm/options', titre: /Options CLM et IA/, maquette: 'UI-24' },
]
const AXE_NORMES = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']
/** PDF minimal d'une page (en-tête %PDF reconnu par le contrôle de format). */
const PDF = '%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
  + '3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n'

async function activerModules(request: APIRequestContext) {
  await request.post('/api/auth/login', { data: { email: 'superadmin@plateforme.ca', motDePasse: 'SuperPassw0rd!' } })
  const tenants = await (await request.get('/api/superadmin/tenants')).json()
  for (const t of tenants) {
    for (const code of ['clm', 'clm_ia']) await request.put(`/api/superadmin/tenants/${t.id}/modules/${code}/activer`)
  }
  await request.post('/api/auth/logout')
}

/** Contrôles géométriques sur toute la page : bandeau, navigation latérale et contenu principal. */
async function controler(page: Page) {
  const bilan = await page.evaluate(() => {
    // checkVisibility écarte aussi le contenu d'un <details> fermé, non rendu mais doté de boîtes résiduelles.
    const visibles = (sel: string) => [...document.querySelectorAll<HTMLElement>(sel)].filter((e) => {
      const r = e.getBoundingClientRect()
      return r.width > 0 && r.height > 0 && r.bottom > 0 && e.checkVisibility({ visibilityProperty: true, contentVisibilityAuto: true })
    })
    const interactifs = visibles('a[href], button, input:not([type=hidden]), select, textarea')
    const petits = interactifs.filter((e) => {
      const r = e.getBoundingClientRect()
      const enLigne = e.tagName === 'A' && getComputedStyle(e).display === 'inline'
      return !enLigne && (r.width < 24 || r.height < 24) && !(e as HTMLInputElement).type?.match(/checkbox|radio/)
    }).map((e) => e.outerHTML.slice(0, 80))
    const chevauchements: string[] = []
    for (let i = 0; i < interactifs.length; i++) {
      for (let j = i + 1; j < interactifs.length; j++) {
        const a = interactifs[i], b = interactifs[j]
        if (a.contains(b) || b.contains(a) || a.closest('label')?.contains(b) || b.closest('label')?.contains(a)) continue
        const ra = a.getBoundingClientRect(), rb = b.getBoundingClientRect()
        const l = Math.max(0, Math.min(ra.right, rb.right) - Math.max(ra.left, rb.left))
        const h = Math.max(0, Math.min(ra.bottom, rb.bottom) - Math.max(ra.top, rb.top))
        if (l > 2 && h > 2) chevauchements.push(`${a.textContent?.trim().slice(0, 30)} / ${b.textContent?.trim().slice(0, 30)}`)
      }
    }
    const sansNom = visibles('input:not([type=hidden]), select, textarea').filter((e) => {
      const id = e.id
      return !e.closest('label') && !e.getAttribute('aria-label') && !(id && document.querySelector(`label[for="${id}"]`))
    }).map((e) => e.outerHTML.slice(0, 80))
    const largeur = window.innerWidth
    const debordants = [...document.querySelectorAll<HTMLElement>('body *')]
      .filter((e) => e.getBoundingClientRect().right > largeur + 1 && ![...e.children].some((c) => c.getBoundingClientRect().right > largeur + 1))
      .slice(0, 5).map((e) => `${e.tagName.toLowerCase()}.${[...e.classList].join('.')} → ${Math.round(e.getBoundingClientRect().right)} px`)
    return {
      deborde: document.documentElement.scrollWidth > largeur + 1 ? (debordants.length ? debordants : ['document']) : [],
      mains: document.querySelectorAll('main').length,
      petits, chevauchements, sansNom,
    }
  })
  expect(bilan.deborde, 'débordement horizontal').toEqual([])
  expect(bilan.mains, 'un seul landmark principal').toBe(1)
  expect(bilan.petits, 'cibles < 24 px').toEqual([])
  expect(bilan.chevauchements, 'éléments superposés').toEqual([])
  expect(bilan.sansNom, 'contrôles sans libellé').toEqual([])
}

async function auditerAxe(page: Page) {
  const { violations } = await new AxeBuilder({ page }).withTags(AXE_NORMES).analyze()
  expect(violations.map((v) => `${v.id} (${v.nodes.length}) : ${v.nodes[0]?.target.join(' ')}`), 'violations axe-core').toEqual([])
}

/** Connexion terminée (cookie de session posé) avant toute navigation directe. */
async function connecterAdmin(page: Page) {
  await new LoginPage(page).connecter(users.admin)
  await page.waitForURL((url) => !url.pathname.startsWith('/login'))
}

test.describe('Vues CLM — gabarit des maquettes, responsive, superposition, cibles, libellés et axe-core', () => {
  test.beforeAll(async ({ request }) => { await activerModules(request) })

  for (const largeur of LARGEURS) {
    test(`écrans CLM à ${largeur} px`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      await connecterAdmin(page)
      for (const e of ECRANS) {
        await test.step(e.nom, async () => {
          await page.goto(e.chemin)
          await expect(page.getByRole('heading', { level: 1, name: e.titre })).toBeVisible()
          await expect(page.locator(`[data-maquette="${e.maquette}"]`).first()).toBeAttached()
          await controler(page)
          await auditerAxe(page)
        })
      }
    })
  }

  test('navigation latérale repliée sur téléphone, dépliable au clavier', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await connecterAdmin(page)
    await page.goto('/clm')
    const menu = page.getByRole('button', { name: /Menu/ })
    const navigation = page.getByRole('navigation', { name: /Navigation Contrats & signatures/ })
    await expect(navigation).toBeHidden()
    await menu.focus()
    await page.keyboard.press('Enter')
    await expect(menu).toHaveAttribute('aria-expanded', 'true')
    await expect(navigation.getByRole('link', { name: 'Mes trousses' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)
  })

  test('recherche globale du bandeau : renvoie à la liste filtrée des trousses', async ({ page }) => {
    await connecterAdmin(page)
    await page.goto('/clm')
    await page.getByRole('searchbox', { name: /Recherche globale des trousses/ }).fill('TR-')
    await page.getByRole('search', { name: /Recherche globale des trousses/ }).getByRole('button', { name: 'Rechercher' }).click()
    await expect(page).toHaveURL(/\/clm\/trousses\?q=TR-/)
    await expect(page.getByRole('heading', { level: 1, name: /Trousses/ })).toBeVisible()
  })

  for (const largeur of [390, 1280]) {
    test(`écrans avec données à ${largeur} px : assistant, détail et onglets, éditeur de modèle, décision`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      await connecterAdmin(page)
      const api = page.request
      const modele = await (await api.post('/api/clm/modeles', { data: {
        nom: `Modèle e2e ${largeur}`, corps: 'Montant : {{montant}}\n{{clauses}}', clauses: [], parcours: [],
        champs: [{ cle: 'montant', libelle: 'Montant', type: 'TEXTE', obligatoire: true, options: [], conditions: [],
          position: { page: 1, x: 10, y: 20, largeur: 30, hauteur: 6 } }],
      } })).json()
      const trousse = await (await api.post('/api/clm/trousses', { data: { titre: `Trousse e2e ${largeur}`, risque: 'MOYEN', unite: 'Financement' } })).json()
      const id = trousse.resume.id
      expect((await api.post(`/api/clm/trousses/${id}/documents`, { multipart: { fichier: { name: 'contrat.pdf', mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } } })).ok()).toBeTruthy()
      const participant = await (await api.post(`/api/clm/trousses/${id}/participants`, { data: { nom: 'Jeanne Signataire', courriel: 'jeanne@exemple.ca', telephone: '+15145550101', roleContrat: 'SIGNATAIRE_1', externe: true } })).json()
      expect((await api.put(`/api/clm/trousses/${id}/parcours`, { data: [
        { cle: 'v1', libelle: 'Validation juridique', phase: 1, type: 'VALIDATION', affectations: [{ type: 'UTILISATEUR', valeur: users.juriste.email }] },
        { cle: 's1', libelle: 'Signature', phase: 2, type: 'SIGNATURE', affectations: [{ type: 'PARTICIPANT', valeur: String(participant.id) }] },
      ] })).ok()).toBeTruthy()

      const verifier = async (nom: string) => test.step(nom, async () => { await controler(page); await auditerAxe(page) })

      for (let etape = 0; etape < 5; etape++) {
        await page.goto(`/clm/trousses/nouvelle?id=${id}&etape=${etape}`)
        await expect(page.getByRole('heading', { level: 1, name: /Modifier le brouillon/ })).toBeVisible()
        await expect(page.getByText(new RegExp(`Étape ${etape + 1} sur 5`))).toBeVisible()
        await verifier(`UI-03 étape ${etape + 1}`)
      }

      await page.goto(`/clm/trousses/${id}`)
      await expect(page.getByRole('heading', { level: 1, name: `Trousse e2e ${largeur}` })).toBeVisible()
      for (const onglet of [/^Résumé/, /^Documents/, /^Participants/, /^Parcours/, /^Historique/, /^Preuve/]) {
        await page.getByRole('tab', { name: onglet }).click()
        await expect(page.getByRole('tabpanel')).toBeVisible()
        await verifier(`UI-04 brouillon, onglet ${onglet}`)
      }
      await page.getByRole('tab', { name: /^Documents/ }).click()
      await page.getByRole('button', { name: /Prévisualiser — contrat.pdf/ }).click()
      await expect(page.locator('.clm-visionneuse')).toBeVisible()
      await verifier('UI-05 prévisualisation')

      expect((await api.post(`/api/clm/trousses/${id}/lancer`, { data: {} })).ok()).toBeTruthy()
      await page.goto(`/clm/trousses/${id}?onglet=parcours`)
      await expect(page.locator('.clm-canvas')).toBeVisible()
      await verifier('UI-04/UI-11 en traitement')

      await page.goto(`/clm/modeles/${modele.id}`)
      await expect(page.getByRole('heading', { level: 1, name: new RegExp(`Modèle e2e ${largeur}`) })).toBeVisible()
      await page.getByRole('button', { name: 'Montant', exact: true }).click()
      await expect(page.locator('[data-maquette="UI-09"]').first()).toBeVisible()
      await verifier('UI-08/UI-09 éditeur, champ choisi')

      await page.context().clearCookies()
      await new LoginPage(page).connecter(users.juriste)
      await page.waitForURL((url) => !url.pathname.startsWith('/login'))
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${trousse.resume.reference}`) }).click()
      await expect(page.getByRole('heading', { level: 1, name: /Validation — Validation juridique/ })).toBeVisible()
      await verifier('UI-13 validation documentaire')
    })
  }

  test('portail du signataire : lien invalide annoncé, mobile sans débordement', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portail/jeton-inexistant-0123456789')
    await expect(page.getByRole('alert')).toContainText(/Invitation introuvable/)
    expect(await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth + 1)).toBeFalsy()
    expect(await page.locator('main').count()).toBe(1)
    await auditerAxe(page)
  })
})
