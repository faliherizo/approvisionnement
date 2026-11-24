import { readFileSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import { expect, test } from '@playwright/test'
import { ecartsEcran } from '../outils/ui'

/**
 * Recette d'un environnement DÉPLOYÉ (E2E_BASE_URL), en lecture seule : aucun geste n'écrit. Pour chaque compte publié
 * de la démonstration (backend/src/main/resources/data/tenants-demo.json), connexion par l'écran puis ouverture de chaque
 * écran de son espace, tiré de l'inventaire du code (e2e/fonctionnalites/inventaire.json) — un écran ajouté au frontend
 * entre dans la recette sans modifier ce fichier.
 *
 * Chaque écran : aucune erreur JavaScript, aucune réponse 5xx de l'API. Pour l'administrateur, en plus : le contrôle
 * d'écran complet (axe-core WCAG 2.2 AA, débordement, chevauchement, cibles ≥ 24 px) aux quatre largeurs.
 * Le mot de passe publié peut être remplacé par E2E_DEMO_PASSWORD (secret de l'environnement GitHub).
 */
const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const demos = JSON.parse(readFileSync(resolve(RACINE, '../backend/src/main/resources/data/tenants-demo.json'), 'utf8')) as {
  slug: string; motDePasse: string; modules: Record<string, boolean>; comptes: { email: string; profil: string; roles: string[] }[]
}[]
const inventaire = JSON.parse(readFileSync(join(RACINE, 'e2e/fonctionnalites/inventaire.json'), 'utf8')) as {
  ecrans: { route: string; espace: string }[]
}

const actif = !!process.env.E2E_BASE_URL
test.skip(!actif, 'E2E_BASE_URL absente : la recette ne vise qu\'un environnement déployé')
test.describe.configure({ mode: 'serial', timeout: 600_000 })

// Configuration de l'hébergement, sans rien écrire : les relais nginx laissent passer une pièce de 26 Mo (cahier IQ 1.4.7,
// téléversement d'au moins 25 Mo) : le serveur la refuse ensuite faute de session, jamais nginx par 413.
test('hébergement : pièce de 26 Mo admise par les relais nginx', async ({ request }) => {
  const r = await request.post('/api/clm/trousses/0/documents', {
    multipart: { fichier: { name: 'gros.pdf', mimeType: 'application/pdf', buffer: Buffer.alloc(26 * 1024 * 1024, 0x20) } },
  })
  expect(r.status(), 'refus par nginx (413) : client_max_body_size trop bas').not.toBe(413)
  expect([401, 403]).toContain(r.status())
})

for (const demo of demos) {
  // Un tenant CLM seul (ADR-010) n'a que l'espace /clm ; les autres, l'espace du socle.
  const espace = demo.modules.clm && Object.entries(demo.modules).filter(([k, v]) => v && !k.startsWith('clm')).length === 0 ? 'clm' : 'socle'
  const ecrans = inventaire.ecrans.map((e) => e).filter((e) => e.espace === espace && !e.route.includes(':')).map((e) => e.route)

  for (const compte of demo.comptes) {
    const controleComplet = compte.roles.includes('ADMIN')
    test(`${demo.slug} — ${compte.profil} (${compte.email}) : ${ecrans.length} écrans${controleComplet ? ', contrôle d\'écran complet' : ''}`, async ({ page }) => {
      const erreurs: string[] = []
      page.on('pageerror', (e) => erreurs.push(`JS ${page.url()} : ${e.message}`))
      page.on('response', (r) => { if (r.url().includes('/api/') && r.status() >= 500) erreurs.push(`${r.status()} ${r.request().method()} ${new URL(r.url()).pathname} (depuis ${page.url()})`) })

      await page.goto('/login')
      await page.getByLabel('Courriel').fill(compte.email)
      await page.getByLabel('Mot de passe').fill(process.env.E2E_DEMO_PASSWORD || demo.motDePasse)
      await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
      await page.waitForURL((u) => !u.pathname.startsWith('/login'))

      const ecarts: string[] = []
      for (const route of ecrans) {
        await page.goto(route)
        await page.waitForLoadState('networkidle')
        await expect(page.locator('body')).not.toBeEmpty()
        if (controleComplet) ecarts.push(...(await ecartsEcran(page)).map((x) => `${route} : ${x}`))
        // La limitation de débit du frontal répond 503 à des rechargements enchaînés : rythme d'une personne.
        await page.waitForTimeout(Number(process.env.E2E_PAUSE_MS ?? 400))
      }
      expect(erreurs, 'erreurs JavaScript ou réponses 5xx').toEqual([])
      expect(ecarts, 'écrans conformes (WCAG 2.2 AA, mise en page) aux quatre largeurs').toEqual([])
    })
  }
}
