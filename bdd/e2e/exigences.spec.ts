import { mkdirSync, readFileSync, statSync, writeFileSync } from 'fs'
import { phrase, narration } from '../outils/phrases.mjs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import { expect, Page, test } from '@playwright/test'
import { cas as casConnus } from '../outils/donnees'
import { Contexte, executer, substituer, totp } from '../outils/cas'
import { connecterUi, ecartsEcran, preparerOrganisation } from '../outils/ui'
import { Etape, jeux } from './scenarios'

/**
 * Exigences fonctionnelles de tous les appels d'offres, vérifiées depuis l'interface (docs/scenarios-e2e/). Un scénario par
 * fonctionnalité d'écran ; il porte les identifiants des exigences qu'il démontre, que le contrôle de couverture relit.
 */
// Un tournage narre chaque geste : il prend le délai de la configuration (playwright.e2e.config.ts).
test.describe.configure({ timeout: process.env.E2E_VIDEO ? 1_200_000 : 240_000 })

const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')

const PDF = Buffer.from('%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
  + '3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n', 'latin1')

/**
 * Bouton ou lien par son nom exact ; à défaut seulement, par le début de son nom (« Document PDF de la fiche » + numéro).
 * On attend qu'un candidat paraisse, puis le nom exact l'emporte toujours sur le début de nom.
 */
async function bouton(page: Page, nomBrut: string) {
  // « Nom #2 » : la deuxième occurrence visible d'un bouton (deux boutons de même nom sur l'écran).
  const rang = nomBrut.match(/^(.*) #(\d+)$/)
  if (rang) {
    const c = page.getByRole('button', { name: rang[1], exact: true }).filter({ visible: true }).nth(Number(rang[2]) - 1)
    await expect(c, `bouton « ${nomBrut} »`).toBeVisible()
    return c
  }
  // « Créer un jeton | Remplacer le jeton » : l'un ou l'autre selon l'état (le libellé suit ce qui existe déjà).
  if (nomBrut.includes(' | ')) {
    const noms = nomBrut.split(' | ')
    const c = noms.map((n) => page.getByRole('button', { name: n, exact: true })).reduce((a, b) => a.or(b)).filter({ visible: true }).first()
    await expect(c, `bouton « ${nomBrut} »`).toBeVisible()
    return c
  }
  const nom = nomBrut
  const debut = new RegExp(`^\\s*${nom.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`)
  const exacts = page.getByRole('button', { name: nom, exact: true }).or(page.getByRole('link', { name: nom, exact: true })).filter({ visible: true })
  const debuts = page.getByRole('button', { name: debut }).or(page.getByRole('link', { name: debut })).filter({ visible: true })
  await expect(exacts.or(debuts).first(), `bouton « ${nom} »`).toBeVisible()
  return (await exacts.count()) ? exacts.first() : debuts.first()
}

/**
 * Champ par son libellé. Une liste enveloppée dans son <label> a pour nom accessible le libellé suivi de l'option
 * affichée (« Catégorie Standard ») : à défaut d'une correspondance exacte, on retient le libellé qui commence par le nom.
 */
/**
 * Section repérée par son titre (sous-chaîne : les signes en tête, emoji ou pictogramme, sont ignorés). Candidats essayés dans
 * l'ordre — formulaire, région ou groupe nommés, conteneur titré, groupe de champs légendé — chacun en localisateur simple :
 * les enchaîner en « or » produit un sélecteur malformé dès qu'un titre contient une apostrophe.
 */
async function section(page: Page, titre: string) {
  // Nom par sous-chaîne (comportement par défaut) : ignore l'emoji de tête, et aucune expression régulière à échapper.
  const motif = titre
  const candidats = [
    page.getByRole('form', { name: motif }), page.getByRole('region', { name: motif }), page.getByRole('group', { name: motif }),
    page.locator('section, form, fieldset').filter({ has: page.getByRole('heading', { name: motif }) }),
    page.locator('fieldset').filter({ has: page.locator('legend').filter({ hasText: titre }) }),
  ]
  let trouve: ReturnType<Page['locator']> | undefined
  await expect.poll(async () => {
    for (const c of candidats) if (await c.count()) { trouve = c.last(); return true }
    return false
  }, { message: `section « ${titre} »`, timeout: 15_000 }).toBe(true)
  return trouve!
}

/** Contrôles de saisie seulement : une région ou un groupe nommés par un libellé ne sont pas des champs. */
const SAISIE = 'input, select, textarea, [contenteditable="true"], [role="textbox"], [role="combobox"]'

async function champ(page: Page, nomComplet: string) {
  // « Titre de section » Libellé » : le champ de cette section (même libellé dans plusieurs formulaires d'un écran).
  const [titreSection, nomBrut] = nomComplet.includes(' » ') ? nomComplet.split(' » ') : [null, nomComplet]
  if (titreSection) {
    const portee = await section(page, titreSection)
    const c = portee.getByLabel(nomBrut, { exact: true }).or(portee.getByPlaceholder(nomBrut, { exact: true }))
      .or(portee.getByLabel(new RegExp(`^\\s*${nomBrut.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`)))
      .and(page.locator(SAISIE)).filter({ visible: true }).first()
    await expect(c, `champ « ${nomComplet} »`).toBeVisible()
    return c
  }
  // « Libellé #2 » : la deuxième occurrence visible du libellé (deux formulaires d'un même écran).
  const rang = nomBrut.match(/^(.*) #(\d+)$/)
  const nom = rang ? rang[1] : nomBrut
  if (rang) {
    const c = page.getByLabel(nom, { exact: true }).and(page.locator(SAISIE)).filter({ visible: true }).nth(Number(rang[2]) - 1)
    await expect(c, `champ « ${nomBrut} »`).toBeVisible()
    return c
  }
  const prefixe = new RegExp(`^\\s*${nom.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}`)
  // Seuls les champs visibles comptent : un panneau replié peut porter un champ de même libellé, plus haut dans le DOM.
  const candidats = [page.getByLabel(nom, { exact: true }), page.getByPlaceholder(nom, { exact: true }), page.getByLabel(prefixe)]
    .map((c) => c.and(page.locator(SAISIE)).filter({ visible: true }))
  // L'écran peut être encore en cours de rendu : on attend qu'un candidat paraisse, puis on retient le plus précis.
  await expect(candidats[0].or(candidats[1]).or(candidats[2]).first(), `champ « ${nom} »`).toBeVisible()
  for (const c of candidats) if (await c.count()) return c.first()
  return candidats[2].first()
}

const exact = (texte: string) => new RegExp(`^\\s*${texte.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\s*$`)

async function jouer(page: Page, e: Etape, ctx: Contexte, outils: { playwright: any; baseURL: string; profil: string }) {
  if ('aller' in e) return void await page.goto(e.aller)
  if ('connecter' in e) {
    if (typeof e.connecter === 'string') return void await connecterUi(page, e.connecter)
    // Compte créé pendant le scénario (préparation ou écran d'administration).
    await page.goto('about:blank'); await page.context().clearCookies(); await page.goto('/login')
    await page.getByLabel('Courriel').fill(e.connecter.email)
    await page.getByLabel('Mot de passe').fill(e.connecter.motDePasse)
    await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
    return void await page.waitForURL((u) => !u.pathname.startsWith('/login'))
  }
  // Largeur de l'écran (ex. 390 : téléphone) pour la suite du scénario.
  if ('largeur' in e) return void await page.setViewportSize({ width: e.largeur, height: e.largeur < 600 ? 844 : 900 })
  if ('langue' in e) {
    await page.evaluate((l) => localStorage.setItem('locale', l), e.langue)
    return void await page.reload()
  }
  if ('cliquer' in e) {
    const cible = typeof e.cliquer === 'string' ? await bouton(page, e.cliquer)
      : page.getByRole(e.cliquer.role, { name: e.cliquer.nom, exact: true }).first()
    return void await cible.click()
  }
  if ('dansLaLigne' in e) {
    // Ligne de tableau, sinon élément de liste, sinon carte (article) : le premier type qui contient le texte l'emporte.
    // Un conteneur plus large (une carte qui englobe tout un tableau) ne doit jamais être pris pour la ligne visée.
    const texte = e.dansLaLigne.texte
    let ligne = page.getByRole('row').filter({ hasText: texte })
    await expect(ligne.or(page.getByRole('listitem').filter({ hasText: texte })).or(page.getByRole('article').filter({ hasText: texte })).first(),
      `ligne « ${texte} »`).toBeVisible()
    if (!(await ligne.count())) ligne = page.getByRole('listitem').filter({ hasText: texte })
    if (!(await ligne.count())) ligne = page.getByRole('article').filter({ hasText: texte })
    const cible = ligne.last()
    return void await cible.getByRole('button', { name: e.dansLaLigne.cliquer, exact: true })
      .or(cible.getByRole('link', { name: e.dansLaLigne.cliquer, exact: true })).first().click()
  }
  if ('remplir' in e) {
    for (const [nom, valeur] of Object.entries(e.remplir)) await (await champ(page, nom)).fill(String(valeur))
    return
  }
  // Options proposées par une liste : présence et absence (une valeur retirée par l'administration n'est plus offerte).
  if ('options' in e) {
    for (const [nom, attendu] of Object.entries(e.options)) {
      const liste = await champ(page, nom)
      const lire = () => liste.locator('option').evaluateAll((os) => os.map((o) => o.textContent?.trim() ?? ''))
      for (const t of attendu.contient ?? []) await expect.poll(async () => (await lire()).some((l) => l.includes(t)), { message: `« ${nom} » propose « ${t} »` }).toBe(true)
      for (const t of attendu.exclut ?? []) expect((await lire()).some((l) => l.includes(t)), `« ${nom} » ne propose plus « ${t} »`).toBe(false)
    }
    return
  }
  if ('choisir' in e) {
    for (const [nom, valeur] of Object.entries(e.choisir)) {
      const liste = await champ(page, nom)
      // Les listes se remplissent après le rendu (données de référence) : on attend l'option demandée.
      let options: { v: string; l: string }[] = []
      const trouver = async () => {
        options = await liste.locator('option').evaluateAll((os) => os.map((o) => ({ v: (o as HTMLOptionElement).value, l: o.textContent?.trim() ?? '' })))
        // Libellé exact, puis code, puis début du libellé (« Bail — gré à gré (TPL_BAIL) »).
        // « (premier) » : la première option non vide (libellé non connu d'avance, ex. une clause du devis).
        if (valeur === '(premier)') return options.find((o) => o.v !== '')
        return options.find((o) => o.l === valeur) ?? options.find((o) => o.v === valeur) ?? options.find((o) => o.l.startsWith(valeur))
      }
      await expect.poll(trouver, { message: `« ${nom} » : option « ${valeur} »`, timeout: 15_000 }).toBeTruthy()
        .catch((e) => { throw new Error(`${e.message}\noptions lues : ${options.map((o) => `${o.l} [${o.v}]`).join(' | ')}`) })
      await liste.selectOption((await trouver())!.v)
    }
    return
  }
  if ('cocher' in e) return void await (await champ(page, e.cocher)).check()
  if ('decocher' in e) return void await (await champ(page, e.decocher)).uncheck()
  if ('televerser' in e) {
    const f = e.televerser.fichier
    // « pdf:25Mo » : PDF valide complété d'espaces jusqu'à la taille voulue (plafonds de téléversement).
    const taille = f.fichier.match(/^pdf:(\d+)Mo$/)
    const contenu = f.fichier === 'pdf' ? PDF
      : taille ? Buffer.concat([PDF, Buffer.alloc(Number(taille[1]) * 1024 * 1024 - PDF.length, 0x20)])
      : f.fichier.startsWith('chemin:') ? readFileSync(join(RACINE, f.fichier.slice(7)))
      : Buffer.from(f.fichier.replace(/^texte:/, ''), 'utf8')
    return void await (await champ(page, e.televerser.champ))
      .setInputFiles({ name: f.nom, mimeType: f.type ?? 'application/octet-stream', buffer: contenu }, { timeout: 60_000 })
  }
  if ('voir' in e) {
    for (const t of [e.voir].flat()) await expect(page.getByText(t, { exact: false }).filter({ visible: true }).first(), `« ${t} » visible`).toBeVisible()
    return
  }
  if ('nePasVoir' in e) {
    // Absent de l'écran : un texte resté dans un élément caché (option de liste, panneau replié) ne compte pas.
    for (const t of [e.nePasVoir].flat()) await expect(page.getByText(t, { exact: false }).filter({ visible: true }), `« ${t} » absent`).toHaveCount(0)
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
    const [fichier] = await Promise.all([page.waitForEvent('download', { timeout: 60_000 }), (await bouton(page, e.telecharger)).click()])
    const chemin = await fichier.path()
    expect(chemin && statSync(chemin).size, `fichier téléchargé par « ${e.telecharger} »`).toBeGreaterThan(0)
    return
  }
  if ('dansLaFenetre' in e) {
    const fenetre = page.getByRole('dialog').filter({ visible: true }).last()
    return void await fenetre.getByRole('button', { name: e.dansLaFenetre, exact: true }).first().click()
  }
  if ('dansLaSection' in e) {
    // Section repérée par le début de son titre : « Devis assemblé (3 clauses) » se cherche par « Devis assemblé ».
    const portee = await section(page, e.dansLaSection.titre)
    return void await portee.getByRole('button', { name: e.dansLaSection.cliquer, exact: true })
      .or(portee.getByRole('link', { name: e.dansLaSection.cliquer, exact: true })).first().click()
  }
  if ('cliquerTexte' in e) return void await page.getByText(e.cliquerTexte, { exact: true }).filter({ visible: true }).first().click()
  if ('lire' in e) {
    const texte = (await page.locator(e.lire.selecteur).filter({ visible: true }).first().textContent())?.trim() ?? ''
    expect(texte, `texte lu par « ${e.lire.selecteur} »`).not.toBe('')
    return void (ctx.variables[e.lire.variable] = texte)
  }
  if ('remplirTotp' in e) {
    for (const [nom, variable] of Object.entries(e.remplirTotp)) await (await champ(page, nom)).fill(totp(ctx.variables[variable]))
    return
  }
  if ('tracer' in e) {
    // Signature manuscrite : un trait tracé à la souris sur le pavé de signature, comme le ferait la personne.
    const zone = page.locator('canvas').filter({ visible: true }).first()
    const b = (await zone.boundingBox())!
    await page.mouse.move(b.x + b.width * 0.2, b.y + b.height * 0.6)
    await page.mouse.down()
    for (const [x, y] of [[0.35, 0.3], [0.5, 0.7], [0.65, 0.35], [0.8, 0.6]]) await page.mouse.move(b.x + b.width * x, b.y + b.height * y, { steps: 5 })
    return void await page.mouse.up()
  }
  if ('repondre' in e) return void (ctx.variables.reponseDialogue = e.repondre)
  if ('ecranConforme' in e) return void expect(await ecartsEcran(page), 'écran conforme aux quatre largeurs').toEqual([])
  throw new Error(`Étape inconnue : ${JSON.stringify(e)}`)
}

const TRACES = join(RACINE, 'test-results-e2e/appels')

/**
 * Écrans ouverts et appels d'API du navigateur, avec l'écran d'où chacun part. Les préparations passent par un autre
 * client HTTP et ne sont donc pas comptées : seule l'interface couvre une fonctionnalité.
 */
function journaliser(page: Page) {
  const ecrans = new Set<string>()
  const appels: { methode: string; chemin: string; ecran: string; statut?: number }[] = []
  const chemin = (u: string) => new URL(u).pathname
  const origine = new WeakMap<object, string>()
  const enCours = new Set<object>()
  const estApi = (u: string) => { try { return new URL(u).pathname.startsWith('/api/') } catch { return false } }
  page.on('request', (r) => { if (estApi(r.url())) enCours.add(r) })
  // Un fichier servi en pièce jointe interrompt la navigation (« requestfailed ») alors que le serveur a bien répondu :
  // l'appel est consigné avec le statut réellement reçu.
  page.on('requestfailed', async (r) => {
    enCours.delete(r)
    const reponse = await r.response().catch(() => null)
    if (reponse && estApi(r.url())) appels.push({ methode: r.method(), chemin: chemin(r.url()).slice(4), ecran: origine.get(r) ?? chemin(page.url()), statut: reponse.status() })
  })
  page.on('framenavigated', (f) => { if (f === page.mainFrame()) ecrans.add(chemin(f.url())) })
  // L'écran est celui d'où part la requête : relevé à son départ, avant toute navigation qu'elle déclencherait. Les
  // ouvertures de route par le routeur React ne naviguent pas : ce relevé les ajoute aussi aux écrans ouverts.
  page.on('request', (r) => {
    try { const e = chemin(page.url()); origine.set(r, e); ecrans.add(e) } catch { /* page fermée */ }
  })
  page.on('requestfinished', async (r) => {
    enCours.delete(r)
    const p = chemin(r.url())
    if (!p.startsWith('/api/')) return
    const reponse = await r.response().catch(() => null)
    appels.push({ methode: r.method(), chemin: p.slice(4), ecran: origine.get(r) ?? chemin(page.url()), statut: reponse?.status() })
  })
  return {
    /**
     * Attend qu'aucun appel d'API ne soit en cours (300 ms de calme, 120 s au plus : une génération par l'IA hébergée
     * sur processeur peut durer) : un geste dont l'appel serait
     * interrompu par la navigation suivante, ou par la fin du scénario, passerait sinon pour réussi.
     */
    async calme() {
      const fin = Date.now() + 120_000
      let calmeDepuis = Date.now()
      while (Date.now() < fin) {
        if (enCours.size) calmeDepuis = Date.now()
        else if (Date.now() - calmeDepuis >= 300) return
        await page.waitForTimeout(50)
      }
    },
    ecrire(id: string, reussi: boolean) {
      mkdirSync(TRACES, { recursive: true })
      writeFileSync(join(TRACES, `${id}.json`), JSON.stringify({ scenario: id, reussi, ecrans: [...ecrans], appels }, null, 1))
    },
  }
}

// Mode « tournage » (E2E_VIDEO=1, bdd/outils/videos.mjs) : chaque geste est précédé de sa narration (le temps du clip
// vocal, durées préparées dans E2E_VIDEO_DUREES) et suivi d'une pause ; la chronologie sert à poser la voix et les
// sous-titres sur la vidéo. Le contrôle d'écran, qui change la taille de la fenêtre, n'est pas filmé.
const VIDEO = !!process.env.E2E_VIDEO
const DUREES: Record<string, number> = VIDEO && process.env.E2E_VIDEO_DUREES ? JSON.parse(readFileSync(process.env.E2E_VIDEO_DUREES, 'utf8')) : {}
const VIDEOS = join(dirname(fileURLToPath(import.meta.url)), '../test-results-e2e/videos')
type Moment = { t: number; voix: string; texte: string }

for (const jeu of jeux) {
  test.describe(jeu.appelOffres, () => {
    for (const s of jeu.scenarios) {
      test(`${s.id} — ${s.titre} ${s.exigences.map((x) => '@' + x).join(' ')}`, async ({ page, playwright, baseURL }) => {
        // Tournage : un suffixe court, lisible à l'écran et dans la narration ; base neuve à chaque tournage.
        const unique = VIDEO ? 'v' + (Date.now() % 46_656).toString(36) : Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
        const jour = (n: number) => new Date(Date.now() + n * 86_400_000).toISOString().slice(0, 10)
        const ctx: Contexte = {
          variables: { unique, UNIQUE: unique.toUpperCase(), aujourdhui: jour(0), demain: jour(1), dansUnMois: jour(30) },
          ecarts: [],
        }
        // Une erreur JavaScript non rattrapée (écran blanc) fait échouer le scénario, avec sa cause.
        const erreurs: string[] = []
        page.on('pageerror', (err) => erreurs.push(`${err.message}\n${err.stack?.split('\n').slice(0, 4).join('\n')}`))
        // Trace des fonctionnalités exercées : écrans ouverts et appels d'API faits depuis chacun (couverture-ecrans.mjs).
        const trace = journaliser(page)
        // Confirmations et saisies natives (window.confirm, window.prompt) : acceptées, avec la réponse fixée par « repondre ».
        page.on('dialog', (d) => void d.accept(d.type() === 'prompt' ? (ctx.variables.reponseDialogue ?? 'Motif E2E') : undefined).catch(() => {}))
        const t0 = Date.now()
        const chronologie: Moment[] = []
        const dire = async (voix: string | null, texte: string) => {
          if (!VIDEO || !voix) return
          chronologie.push({ t: Date.now() - t0, voix, texte })
          await page.waitForTimeout(Math.round((DUREES[voix] ?? 2.5) * 1000) + 250)
        }
        if (s.organisation) await preparerOrganisation(playwright, baseURL!, s.organisation)
        await connecterUi(page, s.profil)
        await dire(s.titre, s.titre)
        let reussi = false
        try {
          for (const [i, brute] of s.etapes.entries()) {
            if (VIDEO && 'ecranConforme' in brute) continue
            // Une préparation substitue elle-même ses étapes, au fil de ses captures.
            const e = ('preparer' in brute ? brute : substituer(brute, ctx.variables)) as Etape
            await dire(narration(brute), phrase(e))
            await test.step(`${i + 1}. ${JSON.stringify(e).slice(0, 120)}`, async () => {
              await jouer(page, e, ctx, { playwright, baseURL: baseURL!, profil: s.profil })
              await trace.calme()
            })
            if (VIDEO) await page.waitForTimeout(700)
            expect(erreurs, `erreur JavaScript après l'étape ${i + 1}`).toEqual([])
          }
          reussi = true
        } finally {
          // Écrite aussi en cas d'échec (diagnostic) ; seule une trace réussie compte pour la couverture.
          trace.ecrire(s.id, reussi)
          if (VIDEO) {
            // Playwright range la vidéo dans le dossier du test, sous ce nom, à la fermeture de la page.
            mkdirSync(VIDEOS, { recursive: true })
            writeFileSync(join(VIDEOS, `${s.id}${process.env.E2E_COMPTES ? '-' + process.env.E2E_COMPTES : ''}.json`), JSON.stringify({ id: s.id, titre: s.titre, exigences: s.exigences, profil: s.profil,
              appelOffres: jeu.appelOffres, organisation: process.env.E2E_COMPTES ?? null, miseEnRoute: s.miseEnRoute ?? null, reussi, video: join(test.info().outputDir, 'video.webm'), chronologie }, null, 1))
          }
        }
      })
    }
  })
}
