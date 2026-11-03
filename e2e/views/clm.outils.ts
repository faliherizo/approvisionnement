import { expect, Page, APIRequestContext } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { LoginPage } from '../pages'
import { users } from '../models/fixtures'
import { TestUser } from '../models/types'

/** Outils partagés des vues CLM : données minimales, connexion, contrôles géométriques et d'accessibilité. */

const AXE_NORMES = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']

/** PDF minimal d'une page (en-tête %PDF reconnu par le contrôle de format). */
export const PDF = '%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
  + '3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n'

export async function activerModules(request: APIRequestContext) {
  await request.post('/api/auth/login', { data: { email: 'superadmin@plateforme.ca', motDePasse: 'SuperPassw0rd!' } })
  const tenants = await (await request.get('/api/superadmin/tenants')).json()
  for (const t of tenants) {
    for (const code of ['clm', 'clm_ia']) await request.put(`/api/superadmin/tenants/${t.id}/modules/${code}/activer`)
  }
  await request.post('/api/auth/logout')
}

/** Contrôles géométriques sur toute la page : bandeau, navigation latérale et contenu principal. */
export async function controler(page: Page) {
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

export async function auditerAxe(page: Page) {
  const { violations } = await new AxeBuilder({ page }).withTags(AXE_NORMES).analyze()
  expect(violations.map((v) => `${v.id} (${v.nodes.length}) : ${v.nodes[0]?.target.join(' ')}`), 'violations axe-core').toEqual([])
}

/**
 * Interface grand public (signataires externes) : cibles portées à 44 px. Une case à cocher compte avec son libellé,
 * qui fait partie de la cible cliquable ; le lien d'évitement, hors écran tant qu'il n'a pas le focus, est ignoré.
 */
export async function ciblesGrandPublic(page: Page) {
  const petites = await page.evaluate(() => [...document.querySelectorAll<HTMLElement>('a[href], button, input:not([type=hidden]), select, textarea')]
    .filter((e) => e.checkVisibility() && !e.classList.contains('skip-link'))
    .map((e) => ({ e, r: ((e as HTMLInputElement).type === 'checkbox' ? e.closest('label') ?? e : e).getBoundingClientRect() }))
    .filter(({ r }) => r.width > 0 && (r.width < 44 || r.height < 44))
    .map(({ e, r }) => `${e.outerHTML.slice(0, 70)} (${Math.round(r.width)}×${Math.round(r.height)})`))
  expect(petites, 'cibles < 44 px sur le portail grand public').toEqual([])
}

/** Connexion terminée (cookie de session posé) avant toute navigation directe. */
export async function connecter(page: Page, utilisateur: TestUser) {
  await new LoginPage(page).connecter(utilisateur)
  await page.waitForURL((url) => !url.pathname.startsWith('/login'))
}

export async function connecterAdmin(page: Page) {
  await connecter(page, users.admin)
}
