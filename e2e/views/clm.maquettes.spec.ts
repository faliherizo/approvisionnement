import { test, expect, Page, APIRequestContext, Playwright } from '@playwright/test'
import { readFileSync } from 'fs'
import { join } from 'path'
import { users } from '../models/fixtures'
import { TestUser } from '../models/types'
import { PDF, activerModules, connecter } from './clm.outils'

/**
 * Fidélité aux maquettes SVG du dossier IQ (UI-01 à UI-24), lues telles que livrées : chaque bloc numéroté d'une
 * maquette est rendu par un élément `data-zone` rattaché à cette maquette, et chaque action de sa barre est offerte par
 * un contrôle nommé. Un libellé retenu différent de la maquette est déclaré, avec son motif, dans LIBELLES_RETENUS.
 */
const DOSSIER_SVG = join(__dirname, '../../docs/Dossier_IQ_2026-6370-0005/10-Maquettes-UI/svg')

type Maquette = { titre: string; blocs: string[]; actions: string[] }

const ENTITES: Record<string, string> = { amp: '&', lt: '<', gt: '>', quot: '"', apos: "'" }
const decoder = (s: string) => s.replace(/&(#x[0-9a-f]+|#\d+|\w+);/gi, (m, e: string) =>
  e[0] === '#' ? String.fromCodePoint(parseInt(e.slice(e[1] === 'x' ? 2 : 1), e[1] === 'x' ? 16 : 10)) : ENTITES[e] ?? m)

/** Titre, blocs numérotés (« 01 — … ») et libellés de la barre d'actions, entre le dernier bloc et le pied « UI-xx ». */
function lireMaquette(ui: string): Maquette {
  const svg = readFileSync(join(DOSSIER_SVG, `${ui}.svg`), 'utf-8')
  const textes = [...svg.matchAll(/<text[^>]*>([\s\S]*?)<\/text>/g)].map((m) => decoder(m[1].replace(/<[^>]+>/g, '')).trim())
  const debut = textes.indexOf('Maquette fonctionnelle')
  const blocs: string[] = []
  let i = debut + 1
  while (i < textes.length && /^\d{2} — /.test(textes[i])) {
    blocs.push(textes[i].slice(0, 2))
    i += 2
  }
  const pied = textes.findIndex((t, k) => k >= i && t.startsWith('UI-'))
  return { titre: textes[debut - 2], blocs, actions: textes.slice(i, pied) }
}

/** Libellé de maquette → libellé retenu, quand l'écran nomme l'action autrement (motif en commentaire). */
const LIBELLES_RETENUS: Record<string, RegExp> = {
  // UI-09 : la maquette regroupe deux actions que l'écran offre en deux boutons distincts.
  'Ajouter / supprimer champ': /^(ajouter un|supprimer le) champ$/,
  // UI-10 : « autorisée » qualifie l'action ; le bouton n'est offert que sur une trousse modifiable, le serveur revérifie.
  'Corriger valeur autorisée': /^corriger la valeur — /,
  // UI-16 : le consentement s'exprime en cochant un énoncé explicite (valeur probante), pas par un bouton « Accepter ».
  'Accepter consentement': /je consens à les signer/,
  // UI-21 : l'anglicisme « rotater » est remplacé par « rotation ».
  'Rotater secret selon pr…': /^rotation du secret selon la procédure$/,
}

/** Actions qui n'existent que dans un état que ce parcours ne produit pas ; chacune est couverte par le test nommé. */
const ACTIONS_CONTEXTUELLES: Record<string, Record<string, string>> = {
  'UI-18': { 'Relancer intégration': 'dossier en file d’erreur Salesforce — ClmIntegrationTest.reprises_salesforce_et_retour_pour_correction' },
}

/** Blocs assurés par le gabarit commun plutôt que par l'écran. */
const ZONES_DU_GABARIT: Record<string, Record<string, string>> = {
  'UI-01': { '01': 'header [role=search]' },
}

const norme = (s: string) => s.replace(/[’']/g, "'").replace(/\s+/g, ' ').trim().toLowerCase()

const MOTS_VIDES = new Set(['le', 'la', 'les', 'l', 'un', 'une', 'des', 'du', 'de', 'd', 'au', 'aux', 'à', 'et', 'ou', 'en', 'par', 'pour', 'mon', 'ma', 'mes'])
const mots = (s: string) => norme(s).split(/[^\p{L}\p{N}]+/u).filter((m) => m && !MOTS_VIDES.has(m))

/** Mots porteurs d'une action : sans le mot coupé par « … » ni la condition d'affichage (« si autorisé », « selon … »). */
function motsAction(libelle: string) {
  let l = libelle.trim()
  if (l.endsWith('…')) l = /\s$/.test(l.slice(0, -1)) ? l.slice(0, -1) : l.slice(0, -1).replace(/\S+$/, '')
  return mots(l.replace(/\s+(si|selon)\b.*$/i, ''))
}

/** Zones rattachées à la maquette et noms des contrôles présents, quel que soit leur dépliage. */
async function releve(page: Page, ui: string) {
  return page.evaluate(([ui, gabarit]) => {
    const zones = [...document.querySelectorAll<HTMLElement>('[data-zone]')]
      .filter((e) => e.closest('[data-maquette]')?.getAttribute('data-maquette') === ui)
      .map((e) => e.getAttribute('data-zone') as string)
    Object.entries(gabarit).forEach(([zone, selecteur]) => { if (document.querySelector(selecteur)) zones.push(zone) })
    const noms = [...document.querySelectorAll<HTMLElement>('button, a[href], [role=tab], summary, input[type=submit], input[type=checkbox], input[type=file]')]
      .filter((e) => !e.closest('[aria-hidden=true], [hidden]'))
      .map((e) => e.getAttribute('aria-label') || (e.matches('input') ? e.closest('label')?.textContent : e.textContent) || '')
    return { zones, noms }
  }, [ui, ZONES_DU_GABARIT[ui] ?? {}] as const)
}

type Etape = (page: Page) => Promise<void>
type Ecran = { ui: string; utilisateur: TestUser | null; etapes: Etape[] }

async function session(playwright: Playwright, baseURL: string, u: TestUser) {
  const c = await playwright.request.newContext({ baseURL })
  expect((await c.post('/api/auth/login', { data: { email: u.email, motDePasse: u.motDePasse } })).ok()).toBeTruthy()
  return c
}

async function json(r: Promise<import('@playwright/test').APIResponse>) {
  const reponse = await r
  expect(reponse.ok(), `${reponse.url()} → ${reponse.status()} ${await reponse.text()}`).toBeTruthy()
  const texte = await reponse.text()
  return texte ? JSON.parse(texte) : {}
}

async function trousseAvecDocument(api: APIRequestContext, titre: string, extra: object = {}) {
  const t = await json(api.post('/api/clm/trousses', { data: { titre, risque: 'MOYEN', unite: 'Financement', ...extra } }))
  const document = await json(api.post(`/api/clm/trousses/${t.resume.id}/documents`, {
    multipart: { fichier: { name: 'convention.pdf', mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } },
  }))
  return { ...t.resume, document: document.id } as { id: number; reference: string; document: number }
}

test.describe('Fidélité aux maquettes SVG UI-01 à UI-24 : blocs et actions', () => {
  const ecrans: Ecran[] = []

  test.beforeAll(async ({ request, playwright }, info) => {
    await activerModules(request)
    const baseURL = info.project.use.baseURL as string
    const admin = await session(playwright, baseURL, users.admin)
    const redacteur = await session(playwright, baseURL, users.redacteur)
    const juriste = await session(playwright, baseURL, users.juriste)
    const suffixe = Date.now().toString(36)

    const modele = await json(redacteur.post('/api/clm/modeles', { data: {
      nom: `Maquettes ${suffixe}`, categorie: 'SUBVENTION', corps: 'Entre IQ et {{demandeur}} : {{montant}} $.\n{{clauses}}',
      champs: [
        { cle: 'demandeur', libelle: 'Demandeur', type: 'TEXTE', obligatoire: true, source: 'Demandeur' },
        { cle: 'montant', libelle: 'Montant', type: 'TEXTE', obligatoire: true, source: 'Montant',
          position: { page: 1, x: 10, y: 20, largeur: 30, hauteur: 6 } },
        { cle: 'titreSignataire', libelle: 'Titre', type: 'TEXTE', obligatoire: true, roleDestinataire: 'SIGNATAIRE_1' },
        { cle: 'signature1', libelle: 'Signature', type: 'SIGNATURE', roleDestinataire: 'SIGNATAIRE_1',
          position: { page: 1, x: 10, y: 70, largeur: 40, hauteur: 10 } },
      ],
      clauses: [{ cle: 'garantie', titre: 'Garantie', texte: 'Une garantie est exigée.', conditions: [{ champ: 'montant', operateur: 'SUP', valeur: '100000' }] }],
      parcours: [],
    } }))
    const modeleEnBrouillon = await json(admin.post('/api/clm/modeles', { data: {
      nom: `Brouillon ${suffixe}`, categorie: 'SUBVENTION', corps: 'Montant : {{montant}}\n{{clauses}}', clauses: [], parcours: [],
      champs: [{ cle: 'montant', libelle: 'Montant', type: 'TEXTE', obligatoire: true, source: 'Montant',
        position: { page: 1, x: 10, y: 20, largeur: 30, hauteur: 6 } }],
    } }))
    await json(redacteur.post(`/api/clm/modeles/${modele.id}/soumettre`, { data: {} }))
    await json(admin.post(`/api/clm/modeles/${modele.id}/publier`, { data: {} }))
    const enRevision = await json(redacteur.post('/api/clm/modeles', { data: {
      nom: `Révision ${suffixe}`, categorie: 'SUBVENTION', corps: 'Texte', champs: [], clauses: [], parcours: [],
    } }))
    await json(redacteur.post(`/api/clm/modeles/${enRevision.id}/soumettre`, { data: {} }))

    const sf = (await json(admin.post('/api/clm/trousses', { data: {
      titre: `Salesforce ${suffixe}`, risque: 'MOYEN', modeleId: modele.id, sfDossierId: 'DEM-2026-0001',
    } }))).resume
    await json(admin.post(`/api/clm/trousses/${sf.id}/participants`, { data: {
      nom: 'Marc Bénéficiaire', courriel: 'marc@exemple.ca', telephone: '+15145550102', roleContrat: 'SIGNATAIRE_2', externe: true,
    } }))
    const sfLancee = await trousseAvecDocument(admin, `Salesforce lancée ${suffixe}`, { modeleId: modele.id, sfDossierId: 'DEM-2026-0002' })
    await json(admin.put(`/api/clm/trousses/${sfLancee.id}/parcours`, { data: [
      { cle: 'v1', libelle: 'Validation juridique', phase: 1, type: 'VALIDATION', affectations: [{ type: 'UTILISATEUR', valeur: users.juriste.email }] },
    ] }))
    await json(admin.post(`/api/clm/trousses/${sfLancee.id}/lancer`, { data: {} }))

    const parcours = (participant: number) => [
      { cle: 'v1', libelle: 'Validation juridique', phase: 1, type: 'VALIDATION', affectations: [{ type: 'UTILISATEUR', valeur: users.juriste.email }] },
      { cle: 'a1', libelle: 'Approbation direction', phase: 2, type: 'APPROBATION', affectations: [{ type: 'UTILISATEUR', valeur: users.direction.email }] },
      { cle: 's1', libelle: 'Signature', phase: 3, type: 'SIGNATURE', affectations: [{ type: 'PARTICIPANT', valeur: String(participant) }] },
    ]
    const preparer = async (titre: string) => {
      const t = await trousseAvecDocument(admin, titre)
      const p = await json(admin.post(`/api/clm/trousses/${t.id}/participants`, { data: {
        nom: 'Jeanne Signataire', courriel: 'jeanne@exemple.ca', telephone: '+15145550101', roleContrat: 'SIGNATAIRE_1', externe: true,
      } }))
      await json(admin.put(`/api/clm/trousses/${t.id}/parcours`, { data: parcours(p.id) }))
      return t
    }
    const brouillon = await preparer(`Brouillon ${suffixe}`)
    await json(admin.post(`/api/clm/trousses/${brouillon.id}/documents/${brouillon.document}/analyse`, { data: {} }))
    const enValidation = await preparer(`Validation ${suffixe}`)
    await json(admin.post(`/api/clm/trousses/${enValidation.id}/lancer`, { data: {} }))
    const enApprobation = await preparer(`Approbation ${suffixe}`)
    await json(admin.post(`/api/clm/trousses/${enApprobation.id}/lancer`, { data: {} }))
    const etapeValidation = (await json(admin.get(`/api/clm/trousses/${enApprobation.id}`))).etapes[0].id
    await json(juriste.post(`/api/clm/etapes/${etapeValidation}/decider`, { data: { decision: 'APPROUVE' } }))

    const aSigner = await trousseAvecDocument(admin, `Signature ${suffixe}`, { risque: 'FAIBLE' })
    const interne = await json(admin.post(`/api/clm/trousses/${aSigner.id}/participants`, { data: {
      nom: 'Julie Juriste', courriel: users.juriste.email, roleContrat: 'SIGNATAIRE_1', externe: false,
      niveauIdentite: 'QUESTION', question: 'Ville du siège ?', reponse: 'Lévis',
    } }))
    await json(admin.put(`/api/clm/trousses/${aSigner.id}/parcours`, { data: [
      { cle: 's1', libelle: 'Signature', phase: 1, type: 'SIGNATURE', affectations: [{ type: 'PARTICIPANT', valeur: String(interne.id) }] },
    ] }))
    await json(admin.post(`/api/clm/trousses/${aSigner.id}/lancer`, { data: {} }))
    const etapeSignature = (await json(admin.get(`/api/clm/trousses/${aSigner.id}`))).etapes[0].id
    const { lien } = await json(juriste.post(`/api/clm/etapes/${etapeSignature}/lien-signature`, { data: {} }))
    const jeton = /\/portail\/([A-Za-z0-9_-]+)/.exec(lien)![1]

    const aller = (url: string): Etape => async (page) => {
      await page.goto(url)
      await expect(page.locator('main').first()).toBeVisible()
      await page.waitForLoadState('networkidle')
      expect.soft(await page.locator('main h1').count(), `${url} : un titre de niveau 1`).toBe(1)
    }
    const onglet = (id: number, nom: string): Etape => aller(`/clm/trousses/${id}?onglet=${nom}`)
    const tache = (reference: string): Etape => async (page) => {
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${reference}`) }).click()
      await expect(page.locator('[data-maquette^="UI-1"] h1').first()).toBeVisible()
      await page.waitForLoadState('networkidle')
    }
    const assistant = (id: number) => [0, 1, 2, 3, 4].map((n) => aller(`/clm/trousses/nouvelle?id=${id}&etape=${n}`))
    const portail: Etape[] = [
      aller(`/portail/${jeton}`),
      async (page) => {
        await page.getByLabel('Ville du siège ?').fill('Lévis')
        await page.getByRole('button', { name: 'Vérifier' }).click()
        await expect(page.getByRole('heading', { level: 2, name: /Lire, consentir et signer/ })).toBeVisible()
      },
      async (page) => {
        const telechargement = page.waitForEvent('download')
        await page.getByRole('button', { name: /Lire \/ télécharger/ }).click()
        await telechargement
        await page.getByLabel('Votre nom complet').fill('Julie Juriste')
        await page.getByLabel(/je consens à les signer/).check()
        await page.getByRole('button', { name: 'Signer', exact: true }).click()
        await expect(page.getByRole('heading', { level: 2, name: 'Reçu de signature' })).toBeVisible()
      },
    ]

    ecrans.push(
      { ui: 'UI-01', utilisateur: users.admin, etapes: [aller('/clm')] },
      { ui: 'UI-02', utilisateur: users.admin, etapes: [aller('/clm/trousses')] },
      { ui: 'UI-03', utilisateur: users.admin, etapes: assistant(brouillon.id) },
      { ui: 'UI-04', utilisateur: users.admin, etapes: [onglet(enValidation.id, 'resume'), onglet(brouillon.id, 'resume')] },
      { ui: 'UI-05', utilisateur: users.admin, etapes: [onglet(brouillon.id, 'documents')] },
      { ui: 'UI-06', utilisateur: users.admin, etapes: [onglet(brouillon.id, 'documents'), async (page) => {
        await page.getByRole('button', { name: /^Versions — convention\.pdf/ }).click()
        await page.getByRole('button', { name: /^Consulter les métadonnées — version 1/ }).click()
        await page.waitForLoadState('networkidle')
      }, onglet(brouillon.id, 'historique')] },
      { ui: 'UI-07', utilisateur: users.admin, etapes: [aller('/clm/modeles')] },
      { ui: 'UI-08', utilisateur: users.admin, etapes: [aller(`/clm/modeles/${modeleEnBrouillon.id}`)] },
      { ui: 'UI-09', utilisateur: users.admin, etapes: [aller(`/clm/modeles/${modeleEnBrouillon.id}`), async (page) => {
        await page.getByRole('button', { name: 'Montant', exact: true }).click()
      }] },
      { ui: 'UI-10', utilisateur: users.admin, etapes: [aller(`/clm/trousses/nouvelle?id=${sf.id}&etape=0`)] },
      { ui: 'UI-11', utilisateur: users.admin, etapes: [onglet(brouillon.id, 'parcours'), onglet(enValidation.id, 'parcours')] },
      { ui: 'UI-12', utilisateur: users.juriste, etapes: [aller('/clm/taches'), async (page) => {
        await page.getByRole('button', { name: 'Filtrer : Terminées' }).click()
        await expect(page.getByRole('button', { name: /^Ouvrir la tâche — / }).first()).toBeVisible()
      }] },
      { ui: 'UI-13', utilisateur: users.juriste, etapes: [tache(enValidation.reference)] },
      { ui: 'UI-14', utilisateur: users.direction, etapes: [tache(enApprobation.reference)] },
      { ui: 'UI-15', utilisateur: users.admin, etapes: [onglet(brouillon.id, 'participants'), onglet(sf.id, 'participants')] },
      { ui: 'UI-16', utilisateur: null, etapes: portail },
      { ui: 'UI-17', utilisateur: users.admin, etapes: [aller('/clm/admin/notifications')] },
      { ui: 'UI-18', utilisateur: users.admin, etapes: [aller('/clm/suivi')] },
      { ui: 'UI-19', utilisateur: users.admin, etapes: [onglet(enValidation.id, 'preuve')] },
      { ui: 'UI-20', utilisateur: users.admin, etapes: [aller('/clm/admin/utilisateurs'), aller('/clm/admin/groupes')] },
      { ui: 'UI-21', utilisateur: users.admin, etapes: [aller('/clm/admin/integrations')] },
      { ui: 'UI-22', utilisateur: users.admin, etapes: [aller('/clm/admin/parametres')] },
      { ui: 'UI-23', utilisateur: users.admin, etapes: [aller('/clm/admin/journal')] },
      { ui: 'UI-24', utilisateur: users.admin, etapes: [aller('/clm/options'), onglet(brouillon.id, 'documents')] },
    )
    await Promise.all([admin.dispose(), redacteur.dispose(), juriste.dispose()])
  })

  test('chaque bloc et chaque action des 24 maquettes sont présents à l’écran', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 900 })
    let connecte: TestUser | null = null
    for (const e of ecrans) {
      await test.step(e.ui, async () => {
        if (e.utilisateur !== connecte) {
          await page.context().clearCookies()
          if (e.utilisateur) await connecter(page, e.utilisateur)
          connecte = e.utilisateur
        }
        const attendu = lireMaquette(e.ui)
        const zones = new Set<string>()
        const noms = new Set<string>()
        for (const etape of e.etapes) {
          await etape(page)
          const r = await releve(page, e.ui)
          r.zones.forEach((z) => zones.add(z))
          r.noms.forEach((n) => noms.add(norme(n)))
        }
        expect.soft(attendu.blocs.filter((b) => !zones.has(b)), `${e.ui} « ${attendu.titre} » : blocs sans zone`).toEqual([])
        const absentes = attendu.actions.filter((a) => {
          if (ACTIONS_CONTEXTUELLES[e.ui]?.[a]) return false
          const retenu = LIBELLES_RETENUS[a]
          const cles = motsAction(a)
          return ![...noms].some((n) => (retenu ? retenu.test(n) : cles.every((m) => mots(n).includes(m))))
        })
        expect.soft(absentes, `${e.ui} « ${attendu.titre} » : actions introuvables parmi ${JSON.stringify([...noms].filter(Boolean))}`).toEqual([])
      })
    }
  })
})
