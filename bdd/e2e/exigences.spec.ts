import { readFileSync, statSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import { expect, Page, test } from '@playwright/test'
import { cas as casConnus } from '../outils/donnees'
import { Contexte, executer, substituer } from '../outils/cas'
import { connecterUi, ecartsEcran, preparerOrganisation } from '../outils/ui'
import { Etape, jeux } from './scenarios'

/**
 * Exigences fonctionnelles de tous les appels d'offres, vérifiées depuis l'interface (docs/scenarios-e2e/). Un scénario par
 * fonctionnalité d'écran ; il porte les identifiants des exigences qu'il démontre, que le contrôle de couverture relit.
 */
test.describe.configure({ timeout: 240_000 })

const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')

const PDF = Buffer.from('%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
  + '3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n', 'latin1')

const bouton = (page: Page, nom: string) =>
  page.getByRole('button', { name: nom, exact: true }).or(page.getByRole('link', { name: nom, exact: true })).first()

/**
 * Champ par son libellé. Une liste enveloppée dans son <label> a pour nom accessible le libellé suivi de l'option
 * affichée (« Catégorie Standard ») : à défaut d'une correspondance exacte, on retient le libellé qui commence par le nom.
 */
async function champ(page: Page, nom: string) {
  const prefixe = new RegExp(`^\\s*${nom.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`)
  const candidats = [page.getByLabel(nom, { exact: true }), page.getByPlaceholder(nom, { exact: true }), page.getByLabel(prefixe)]
  // L'écran peut être encore en cours de rendu : on attend qu'un candidat paraisse, puis on retient le plus précis.
  await expect(candidats[0].or(candidats[1]).or(candidats[2]).first(), `champ « ${nom} »`).toBeVisible()
  for (const c of candidats) if (await c.count()) return c.first()
  return candidats[2].first()
}

const exact = (texte: string) => new RegExp(`^\\s*${texte.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*$`)

async function jouer(page: Page, e: Etape, ctx: Contexte, outils: { playwright: any; baseURL: string; profil: string }) {
  if ('aller' in e) return void await page.goto(e.aller)
  if ('connecter' in e) return void await connecterUi(page, e.connecter)
  if ('langue' in e) {
    await page.evaluate((l) => localStorage.setItem('locale', l), e.langue)
    return void await page.reload()
  }
  if ('cliquer' in e) {
    const cible = typeof e.cliquer === 'string' ? bouton(page, e.cliquer)
      : page.getByRole(e.cliquer.role, { name: e.cliquer.nom, exact: true }).first()
    return void await cible.click()
  }
  if ('dansLaLigne' in e) {
    const ligne = page.getByRole('row').filter({ hasText: e.dansLaLigne.texte }).first()
    return void await ligne.getByRole('button', { name: e.dansLaLigne.cliquer, exact: true })
      .or(ligne.getByRole('link', { name: e.dansLaLigne.cliquer, exact: true })).first().click()
  }
  if ('remplir' in e) {
    for (const [nom, valeur] of Object.entries(e.remplir)) await (await champ(page, nom)).fill(valeur)
    return
  }
  if ('choisir' in e) {
    for (const [nom, valeur] of Object.entries(e.choisir)) {
      const liste = await champ(page, nom)
      // Les listes se remplissent après le rendu (données de référence) : on attend l'option demandée.
      let options: { v: string; l: string }[] = []
      const trouver = async () => {
        options = await liste.locator('option').evaluateAll((os) => os.map((o) => ({ v: (o as HTMLOptionElement).value, l: o.textContent?.trim() ?? '' })))
        return options.find((o) => o.l === valeur) ?? options.find((o) => o.v === valeur)
      }
      await expect.poll(trouver, { message: `« ${nom} » : option « ${valeur} »`, timeout: 15_000 }).toBeTruthy()
      await liste.selectOption((await trouver())!.v)
    }
    return
  }
  if ('cocher' in e) return void await (await champ(page, e.cocher)).check()
  if ('decocher' in e) return void await (await champ(page, e.decocher)).uncheck()
  if ('televerser' in e) {
    const f = e.televerser.fichier
    const contenu = f.fichier === 'pdf' ? PDF
      : f.fichier.startsWith('chemin:') ? readFileSync(join(RACINE, f.fichier.slice(7)))
      : Buffer.from(f.fichier.replace(/^texte:/, ''), 'utf8')
    return void await (await champ(page, e.televerser.champ))
      .setInputFiles({ name: f.nom, mimeType: f.type ?? 'application/octet-stream', buffer: contenu })
  }
  if ('voir' in e) {
    for (const t of [e.voir].flat()) await expect(page.getByText(t, { exact: false }).filter({ visible: true }).first(), `« ${t} » visible`).toBeVisible()
    return
  }
  if ('nePasVoir' in e) {
    for (const t of [e.nePasVoir].flat()) await expect(page.getByText(t, { exact: false }), `« ${t} » absent`).toHaveCount(0)
    return
  }
  if ('valeur' in e) {
    for (const [nom, attendue] of Object.entries(e.valeur)) await expect(await champ(page, nom), `valeur de « ${nom} »`).toHaveValue(attendue)
    return
  }
  if ('voirTitre' in e) return void await expect(page.getByRole('heading', { name: exact(e.voirTitre) }).first()).toBeVisible()
  if ('url' in e) return void await expect(page).toHaveURL(new RegExp(e.url))
  if ('preparer' in e) {
    const nom = typeof e.preparer === 'string' ? e.preparer : 'préparation'
    const c = typeof e.preparer === 'string' ? casConnus[e.preparer] : e.preparer
    if (!c) throw new Error(`Cas de préparation inconnu : « ${e.preparer} »`)
    await executer(outils.playwright, outils.baseURL, outils.profil, nom, c, ctx)
    expect(ctx.ecarts, `préparation « ${nom} »`).toEqual([])
    return
  }
  if ('telecharger' in e) {
    const [fichier] = await Promise.all([page.waitForEvent('download'), bouton(page, e.telecharger).click()])
    const chemin = await fichier.path()
    expect(chemin && statSync(chemin).size, `fichier téléchargé par « ${e.telecharger} »`).toBeGreaterThan(0)
    return
  }
  if ('ecranConforme' in e) return void expect(await ecartsEcran(page), 'écran conforme aux quatre largeurs').toEqual([])
  throw new Error(`Étape inconnue : ${JSON.stringify(e)}`)
}

for (const jeu of jeux) {
  test.describe(jeu.appelOffres, () => {
    for (const s of jeu.scenarios) {
      test(`${s.id} — ${s.titre} ${s.exigences.map((x) => '@' + x).join(' ')}`, async ({ page, playwright, baseURL }) => {
        const unique = Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
        const jour = (n: number) => new Date(Date.now() + n * 86_400_000).toISOString().slice(0, 10)
        const ctx: Contexte = {
          variables: { unique, UNIQUE: unique.toUpperCase(), aujourdhui: jour(0), demain: jour(1), dansUnMois: jour(30) },
          ecarts: [],
        }
        // Une erreur JavaScript non rattrapée (écran blanc) fait échouer le scénario, avec sa cause.
        const erreurs: string[] = []
        page.on('pageerror', (err) => erreurs.push(`${err.message}\n${err.stack?.split('\n').slice(0, 4).join('\n')}`))
        if (s.organisation) await preparerOrganisation(playwright, baseURL!, s.organisation)
        await connecterUi(page, s.profil)
        for (const [i, brute] of s.etapes.entries()) {
          // Une préparation substitue elle-même ses étapes, au fil de ses captures.
          const e = ('preparer' in brute ? brute : substituer(brute, ctx.variables)) as Etape
          await test.step(`${i + 1}. ${JSON.stringify(e).slice(0, 120)}`, () => jouer(page, e, ctx, { playwright, baseURL: baseURL!, profil: s.profil }))
          expect(erreurs, `erreur JavaScript après l'étape ${i + 1}`).toEqual([])
        }
        if (s.id === 'DIAG') {
          await page.setViewportSize({ width: 390, height: 900 }); await page.waitForTimeout(500)
          console.log(await page.evaluate(() => [...document.querySelectorAll('body *')].map((e) => [e, e.scrollWidth, e.clientWidth, getComputedStyle(e).overflowX] as const)
            .filter(([e]) => e.getBoundingClientRect().right > window.innerWidth + 1).slice(-8).map(([e]) => `${e.tagName}.${e.className} r=${Math.round(e.getBoundingClientRect().right)} w=${Math.round(e.getBoundingClientRect().width)} «${(e.textContent||'').trim().slice(0,30)}»`).join('\n')))
        }
      })
    }
  })
}
