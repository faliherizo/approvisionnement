import { expect, Page } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { Given, When, Then } from './fixtures'
import { cas, comptes, ecrans, organisations } from '../outils/donnees'
import { executer, session, substituer } from '../outils/cas'

/** Étapes communes, en langage d'analyste : qui agit, ce qu'il fait, ce que la plateforme doit montrer ou rendre. */

const AXE_NORMES = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']

async function connecterUi(page: Page, profil: string) {
  const compte = comptes[profil]
  if (!compte) throw new Error(`Profil inconnu : « ${profil} »`)
  // Quitter d'abord la page en cours : privée de session, elle renverrait d'elle-même vers /login (expiration),
  // en concurrence avec la navigation de cette étape.
  await page.goto('about:blank')
  await page.context().clearCookies()
  await page.goto('/login')
  await page.getByLabel('Courriel').fill(compte.email)
  await page.getByLabel('Mot de passe').fill(compte.motDePasse)
  await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
  await page.waitForURL((url) => !url.pathname.startsWith('/login'))
}

Given('je suis connecté en tant que {string}', async ({ page, monde }, profil: string) => {
  await connecterUi(page, profil)
  monde.variables.profil = profil
})

Given('j\'agis en tant que {string}', async ({ monde }, profil: string) => {
  monde.variables.profil = profil
})

/** Organisation dédiée aux essais (créée une fois, modules activés) : aucune autre organisation n'est modifiée. */
Given('l\'organisation {string} est prête', async ({ playwright, baseURL }, nom: string) => {
  const o = organisations[nom]
  if (!o) throw new Error(`Organisation inconnue : « ${nom} »`)
  const sa = await session(playwright, baseURL!, 'superadmin')
  const existantes = await (await sa.get('/api/superadmin/tenants')).json() as { id: number; slug: string }[]
  let t = existantes.find((x) => x.slug === o.slug)
  if (!t) {
    const r = await sa.post('/api/superadmin/tenants', { data: { slug: o.slug, titre: o.titre, sousTitre: 'Essais BDD' } })
    expect(r.ok(), `création de l'organisation ${o.slug} : ${r.status()} ${await r.text()}`).toBeTruthy()
    t = await r.json()
    const a = await sa.post(`/api/superadmin/tenants/${t!.id}/admin`, { data: o.admin })
    expect(a.ok(), `administrateur de ${o.slug} : ${a.status()} ${await a.text()}`).toBeTruthy()
  }
  for (const code of o.modules) {
    const r = await sa.put(`/api/superadmin/tenants/${t!.id}/modules/${code}/activer`)
    expect(r.ok(), `module ${code} : ${r.status()} ${await r.text()}`).toBeTruthy()
  }
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

/** SGQRI 008 3.0 / WCAG 2.2 AA : axe-core sans violation, cibles ≥ 24 px, aucun débordement ni superposition. */
Then('l\'écran est accessible, sans débordement ni superposition', async ({ page }) => {
  const bilan = await page.evaluate(() => {
    const visibles = (sel: string) => [...document.querySelectorAll<HTMLElement>(sel)].filter((e) => {
      const r = e.getBoundingClientRect()
      // Sous une modale ouverte, la page est inerte (ni focus ni clic) : seule la modale compte.
      return r.width > 0 && r.height > 0 && r.bottom > 0 && !e.closest('[inert]')
        && e.checkVisibility({ visibilityProperty: true, contentVisibilityAuto: true })
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
    return { deborde: document.documentElement.scrollWidth > window.innerWidth + 1, petits, chevauchements }
  })
  expect(bilan.deborde, 'débordement horizontal').toBe(false)
  expect(bilan.petits, 'cibles < 24 px').toEqual([])
  expect(bilan.chevauchements, 'éléments superposés').toEqual([])
  const { violations } = await new AxeBuilder({ page }).withTags(AXE_NORMES).analyze()
  expect(violations.map((v) => `${v.id} (${v.nodes.length}) : ${v.nodes[0]?.target.join(' ')}`), 'violations axe-core').toEqual([])
})
