import { test, expect, Page, APIRequestContext, Playwright, Download } from '@playwright/test'
import { createHash, randomBytes } from 'crypto'
import { execFileSync } from 'child_process'
import { mkdtempSync, readFileSync, writeFileSync } from 'fs'
import { tmpdir } from 'os'
import { join } from 'path'
import { users } from '../models/fixtures'
import { TestUser } from '../models/types'
import d from '../data/bacs-a-sable.json'
import { PDF, auditerAxe, connecter, controler } from './clm.outils'

/**
 * Scénarios e2e sur les bacs à sable (docs/Dossier_IQ_2026-6370-0005/12-Bacs-a-sable/scenarios_e2e_bacs_a_sable.md) :
 * chaque verdict est rendu par l'application externe — veraPDF (PDF/A), pyHanko (PAdES), OpenSSL (CAdES, RFC 3161),
 * l'ICP (OCSP, liste de révocation) —, jamais par la plateforme qui a produit le document.
 */
test.describe.configure({ mode: 'serial', timeout: 300_000 })

const S = d.services
const travail = mkdtempSync(join(tmpdir(), 'clm-bacs-'))
const unique = () => Date.now().toString(36)

async function servicesDisponibles(request: APIRequestContext) {
  const sondes = [`${S.icp}/sante`, `${S.validation}/sante`, `${S.verapdf}/api/info`, `${S.conversion}/health`, `${S.courriel}/info`]
  for (const url of sondes) {
    const r = await request.get(url, { timeout: 5_000 }).catch(() => null)
    if (!r || !r.ok()) return `${url} injoignable`
  }
  return null
}

async function api(playwright: Playwright, baseURL: string | undefined, compte: TestUser) {
  const c = await playwright.request.newContext({ baseURL })
  expect((await c.post('/api/auth/login', { data: { email: compte.email, motDePasse: compte.motDePasse } })).ok()).toBeTruthy()
  return c
}

async function ok<T = any>(r: Promise<import('@playwright/test').APIResponse>): Promise<T> {
  const reponse = await r
  expect(reponse.ok(), `${reponse.url()} → ${reponse.status()} ${await reponse.text()}`).toBeTruthy()
  const texte = await reponse.text()
  return (texte ? JSON.parse(texte) : {}) as T
}

async function session(page: Page, compte: TestUser) {
  await page.context().clearCookies()
  await connecter(page, compte)
  await expect(page).toHaveURL(/\/clm$/)
}

async function telecharger(page: Page, action: () => Promise<unknown>): Promise<{ nom: string; octets: Buffer }> {
  const [dl]: [Download, unknown] = await Promise.all([page.waitForEvent('download'), action()])
  return { nom: dl.suggestedFilename(), octets: readFileSync((await dl.path())!) }
}

/** veraPDF REST : rapport du profil demandé. */
async function verapdf(request: APIRequestContext, nom: string, octets: Buffer) {
  const r = await ok(request.post(`${S.verapdf}/api/validate/${d.verapdf.profil}`, {
    headers: { Accept: 'application/json' }, multipart: { file: { name: nom, mimeType: 'application/pdf', buffer: octets } } }))
  return r.report.jobs[0].validationResult[0] as { compliant: boolean; details: { passedRules: number; failedRules: number } }
}

/** pyHanko : profil PAdES-B-LTA, données de révocation intégrées exigées, racine de l'ICP pour seule ancre. */
async function pyhanko(request: APIRequestContext, octets: Buffer) {
  return ok<{ valide: boolean; rapport: string }>(request.post(`${S.validation}/valider?profil=${d.pades.profil}`, {
    headers: { 'Content-Type': 'application/pdf' }, data: octets }))
}

/** Opération de l'autorité sur le certificat de cachet (suspension certificateHold, levée de suspension). */
async function operationIcp(request: APIRequestContext, operation: 'suspendre' | 'retablir') {
  const r = await request.post(`${S.icp}/admin/cachet/${operation}`)
  expect(r.ok(), `ICP ${operation} → ${r.status()} ${await r.text()}`).toBeTruthy()
}

/** OpenSSL cms -verify : signature CAdES détachée sur le contenu donné. */
async function cades(request: APIRequestContext, signature: Buffer, contenu: Buffer) {
  return ok<{ valide: boolean; rapport: string }>(request.post(`${S.validation}/cms`, {
    data: { signature: signature.toString('base64'), contenu: contenu.toString('base64') } }))
}

/** Valeur extraite du plus récent courriel reçu par le destinataire dans Mailpit. */
async function courriel(request: APIRequestContext, destinataire: string, motif: RegExp, apres = 0): Promise<string> {
  for (let i = 0; i < 30; i++) {
    const boite = await ok(request.get(`${S.courriel}/search?query=${encodeURIComponent(`to:${destinataire}`)}`))
    if (boite.messages.length > apres) {
      const m = await ok(request.get(`${S.courriel}/message/${boite.messages[0].ID}`))
      const trouve = (m.Text as string).match(motif)
      if (trouve) return trouve[1]
    }
    await new Promise((r) => setTimeout(r, 1_000))
  }
  throw new Error(`Aucun courriel pour ${destinataire} correspondant à ${motif}`)
}

/** Texte du Word tel qu'il s'affiche (en-tête puis corps, runs réunis) et XML du corps, pour la mise en forme. */
function lireWord(octets: Buffer): { texte: string; corps: string } {
  const fichier = join(travail, `w-${unique()}.docx`)
  writeFileSync(fichier, octets)
  const partie = (nom: string) => execFileSync('unzip', ['-p', fichier, nom]).toString('utf8')
  const texte = ['word/header1.xml', 'word/document.xml'].map((nom) => (partie(nom).match(/<w:p[ >][\s\S]*?<\/w:p>/g) || [])
    .map((p) => (p.match(/<w:t[^>]*>([^<]*)<\/w:t>/g) || []).map((t) => t.replace(/<[^>]+>/g, '')).join('')).join('\n')).join('\n')
  return { texte, corps: partie('word/document.xml') }
}

/** Requête RFC 3161 (TimeStampReq, DER) telle que la plateforme l'émet : SHA-256, nonce, certificat demandé. */
function requeteHorodatage(donnees: Buffer): Buffer {
  const empreinte = createHash('sha256').update(donnees).digest()
  const algo = Buffer.from('300d06096086480165030402010500', 'hex')
  const imprint = Buffer.concat([Buffer.from([0x30, algo.length + 34]), algo, Buffer.from([0x04, 0x20]), empreinte])
  const nonce = randomBytes(8)
  nonce[0] &= 0x7f
  const corps = Buffer.concat([Buffer.from([0x02, 0x01, 0x01]), imprint, Buffer.from([0x02, 0x08]), nonce, Buffer.from([0x01, 0x01, 0xff])])
  return Buffer.concat([Buffer.from([0x30, corps.length]), corps])
}

const etat: { trousseGabarit?: { id: number; reference: string }; trousseSignee?: { id: number; reference: string } } = {}

test.describe('Bacs à sable — ICP, LibreOffice (Gotenberg), veraPDF, pyHanko', () => {
  test.beforeAll(async ({ request }) => {
    const absent = await servicesDisponibles(request)
    test.skip(absent !== null, `Bacs à sable non démarrés (${absent}) : docker compose -f infra/bacs-a-sable/docker-compose.yml up -d --wait`)
  })

  test('BS-01 — contrat sans gabarit généré depuis l’interface : PDF/A-2b jugé conforme par veraPDF', async ({ page, playwright, baseURL }) => {
    const redacteur = await api(playwright, baseURL, users.iqInitiateur)
    const admin = await api(playwright, baseURL, users.iqAdminAffaires)
    const m = await ok(redacteur.post('/api/clm/modeles', { data: { ...d.modeleSimple, nom: `${d.modeleSimple.nom} ${unique()}` } }))
    await ok(redacteur.post(`/api/clm/modeles/${m.id}/soumettre`, { data: {} }))
    await ok(admin.post(`/api/clm/modeles/${m.id}/publier`, { data: {} }))
    const t = (await ok(redacteur.post('/api/clm/trousses', { data: { titre: `BS-01 ${unique()}`, modeleId: m.id, risque: 'FAIBLE' } }))).resume
    await ok(redacteur.put(`/api/clm/trousses/${t.id}/valeurs`, { data: { valeurs: d.valeurs } }))
    await Promise.all([redacteur.dispose(), admin.dispose()])

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=documents`)
    await page.getByRole('button', { name: 'Générer depuis le modèle' }).click()
    const pdf = await telecharger(page, () => page.getByRole('button', { name: `Télécharger — ${t.reference}.pdf` }).click())
    await controler(page)
    await auditerAxe(page)
    const v = await verapdf(page.request, pdf.nom, pdf.octets)
    expect(v.compliant, `veraPDF : ${v.details.failedRules} règle(s) en échec`).toBe(true)
    expect(v.details.failedRules).toBe(0)
  })

  test('BS-02 — gabarit Word d’IQ déposé dans l’éditeur : le Word généré conserve la mise en page et fusionne tout', async ({ page, playwright, baseURL }) => {
    const gabarit = join(travail, 'gabarit.docx')
    execFileSync('python3', [join(__dirname, '../../infra/bacs-a-sable/donnees/fabriquer_gabarit.py'), gabarit])
    const g = d.modeleGabarit
    const redacteur = await api(playwright, baseURL, users.iqInitiateur)
    const m = await ok(redacteur.post('/api/clm/modeles', { data: { nom: `${g.nom} ${unique()}`, corps: g.corps, champs: g.champs, clauses: g.clauses, parcours: g.parcours } }))

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/modeles/${m.id}`)
    await page.getByLabel('Document Word (.docx)').setInputFiles({ name: g.fichierGabarit,
      mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', buffer: readFileSync(gabarit) })
    await page.getByRole('button', { name: 'Déposer le gabarit' }).click()
    await expect(page.getByText(`Gabarit en service : ${g.fichierGabarit}`)).toBeVisible()
    await controler(page)
    await auditerAxe(page)

    const admin = await api(playwright, baseURL, users.iqAdminAffaires)
    await ok(redacteur.post(`/api/clm/modeles/${m.id}/soumettre`, { data: {} }))
    await ok(admin.post(`/api/clm/modeles/${m.id}/publier`, { data: {} }))
    const t = (await ok(redacteur.post('/api/clm/trousses', { data: { titre: `BS-02 ${unique()}`, modeleId: m.id, risque: 'FAIBLE' } }))).resume
    await ok(redacteur.put(`/api/clm/trousses/${t.id}/valeurs`, { data: { valeurs: d.valeurs } }))
    await Promise.all([redacteur.dispose(), admin.dispose()])
    etat.trousseGabarit = { id: t.id, reference: t.reference }

    await page.goto(`/clm/trousses/${t.id}?onglet=documents`)
    await page.getByRole('button', { name: 'Générer en Word modifiable' }).click()
    const word = await telecharger(page, () => page.getByRole('button', { name: `Télécharger — ${t.reference}.docx` }).click())
    const { texte, corps } = lireWord(word.octets)
    for (const attendu of d.attendus.word) expect(texte, texte).toContain(attendu)
    expect(texte).not.toContain('{{')
    expect(corps.slice(0, corps.indexOf('Convention de financement')), 'gras du titre d’origine').toContain('<w:b/>')
  })

  test('BS-03 — même gabarit en PDF : converti par Gotenberg, texte fusionné, PDF/A-2b conforme selon veraPDF', async ({ page }) => {
    const t = etat.trousseGabarit!
    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=documents`)
    await page.getByRole('button', { name: 'Générer depuis le modèle' }).click()
    const pdf = await telecharger(page, () => page.getByRole('button', { name: `Télécharger — ${t.reference}.pdf` }).click())
    const versions = await ok(page.request.get(`/api/clm/trousses/${t.id}/documents/contrat-genere/versions`))
    expect(versions[0].motif, 'converti par le service LibreOffice').toContain(d.attendus.motifConversion)
    const texte = await (await page.request.get(`/api/clm/trousses/${t.id}/documents/${versions[0].id}/texte`)).text()
    for (const attendu of d.attendus.pdf) expect(texte).toContain(attendu)
    const v = await verapdf(page.request, pdf.nom, pdf.octets)
    expect(v.compliant, `veraPDF : ${v.details.failedRules} règle(s) en échec`).toBe(true)
  })

  test('BS-04 — certificat de signature (onglet Preuve) : PDF/A-2b pour veraPDF, PAdES-B-LTA valide pour pyHanko', async ({ page }) => {
    const t = etat.trousseGabarit!
    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=preuve`)
    const certificat = await telecharger(page, () => page.getByRole('button', { name: 'Télécharger le certificat (PDF)' }).click())
    await controler(page)
    await auditerAxe(page)
    expect((await verapdf(page.request, certificat.nom, certificat.octets)).compliant).toBe(true)
    const p = await pyhanko(page.request, certificat.octets)
    for (const attendu of d.attendus.pyhanko) expect(p.rapport, p.rapport).toContain(attendu)
    expect(p.valide).toBe(true)
  })

  test('BS-05 — signature externe au portail par courriel SMTP : attestation CAdES vérifiée par OpenSSL', async ({ page, playwright, baseURL }) => {
    const s = d.signature
    const courrielSignataire = `signataire.${unique()}@${s.signataire.domaine}`
    const redacteur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(redacteur.post('/api/clm/trousses', { data: { titre: `${s.trousse} ${unique()}`, risque: 'FAIBLE' } }))).resume
    await ok(redacteur.post(`/api/clm/trousses/${t.id}/documents`, { multipart: { fichier: { name: s.document, mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } } }))
    const p = await ok(redacteur.post(`/api/clm/trousses/${t.id}/participants`, { data: { nom: s.signataire.nom, courriel: courrielSignataire,
      telephone: s.signataire.telephone, roleContrat: s.signataire.roleContrat, externe: true } }))
    await ok(redacteur.put(`/api/clm/trousses/${t.id}/parcours`, { data: [{ cle: 's1', libelle: 'Signature', phase: 1, type: 'SIGNATURE',
      affectations: [{ type: 'PARTICIPANT', valeur: String(p.id) }] }] }))
    await ok(redacteur.post(`/api/clm/trousses/${t.id}/lancer`, { data: {} }))
    etat.trousseSignee = { id: t.id, reference: t.reference }

    const jeton = await courriel(page.request, courrielSignataire, /\/portail\/([A-Za-z0-9_-]+)/)
    await page.context().clearCookies()
    await page.goto(`/portail/${jeton}`)
    await page.getByRole('button', { name: 'Recevoir mon code' }).click()
    const code = await courriel(page.request, courrielSignataire, /\b(\d{6})\b/, 1)
    await page.getByLabel('Code de vérification').fill(code)
    await page.getByRole('button', { name: 'Vérifier mon identité' }).click()
    await telecharger(page, () => page.getByRole('button', { name: 'Lire / télécharger' }).first().click())
    await page.getByLabel('Votre nom complet').fill(s.signataire.nom)
    await page.getByRole('checkbox', { name: /J’ai lu les documents/ }).check()
    await controler(page)
    await auditerAxe(page)
    await page.getByRole('button', { name: 'Signer', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Merci, votre signature est enregistrée' })).toBeVisible()
    await expect(page.getByText(/CAC-[0-9A-F]{20}/)).toBeVisible()

    const preuve = await ok(redacteur.get(`/api/clm/trousses/${t.id}/preuve`))
    await redacteur.dispose()
    const sig = preuve.signatures[0]
    const a = sig.attestation
    expect(a.fournisseur).toBe(s.fournisseur)
    expect(a.algorithme).toBe(s.algorithme)
    const canon = (niveau: string) => Buffer.from([a.empreinteDocuments, sig.participant, sig.courriel, niveau, sig.consentementLe].join('|'), 'utf8')
    const signature = Buffer.from(a.jetonHorodatage, 'base64')
    const verdict = await cades(page.request, signature, canon(sig.niveau))
    expect(verdict.valide, verdict.rapport).toBe(true)
    const altere = await cades(page.request, signature, canon(s.niveauAltere))
    expect(altere.valide, 'une donnée attestée modifiée invalide l’attestation').toBe(false)
  })

  test('BS-06 — intégrité confirmée puis dossier exporté : .p7s vérifié par OpenSSL, certificat de l’archive valide pour pyHanko', async ({ page }) => {
    const t = etat.trousseSignee!
    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=preuve`)
    await page.getByRole('button', { name: 'Vérifier l’intégrité' }).click()
    await expect(page.getByText(/Intégrité confirmée/).first()).toBeVisible()
    const zip = await telecharger(page, () => page.getByRole('button', { name: 'Exporter le dossier complet (ZIP)' }).click())
    const archive = join(travail, `preuve-${unique()}.zip`)
    writeFileSync(archive, zip.octets)
    const fichiers = execFileSync('unzip', ['-Z1', archive]).toString('utf8').trim().split('\n')
    const p7s = fichiers.find((f) => f.endsWith('.p7s'))
    expect(p7s, fichiers.join(', ')).toBeTruthy()
    const document = execFileSync('unzip', ['-p', archive, p7s!.replace(/\.p7s$/, '')])
    const v = await cades(page.request, execFileSync('unzip', ['-p', archive, p7s!]), document)
    expect(v.valide, v.rapport).toBe(true)
    const manifeste = JSON.parse(execFileSync('unzip', ['-p', archive, 'manifeste.json']).toString('utf8'))
    for (const f of fichiers.filter((x) => !['manifeste.json', 'sceau.json'].includes(x))) expect(Object.keys(manifeste)).toContain(f)
    const p = await pyhanko(page.request, execFileSync('unzip', ['-p', archive, 'certificat.pdf']))
    expect(p.valide, p.rapport).toBe(true)
  })

  test('BS-07 — l’autorité suspend le cachet : pyHanko refuse le certificat scellé ensuite ; rétabli, il redevient valide', async ({ page }) => {
    const t = etat.trousseSignee!
    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=preuve`)
    try {
      await operationIcp(page.request, 'suspendre')
      const pendant = await telecharger(page, () => page.getByRole('button', { name: 'Télécharger le certificat (PDF)' }).click())
      const refus = await pyhanko(page.request, pendant.octets)
      expect(refus.valide, 'certificat suspendu').toBe(false)
      expect(refus.rapport).toContain(d.attendus.revoque)
    } finally {
      await operationIcp(page.request, 'retablir')
    }
    const apres = await telecharger(page, () => page.getByRole('button', { name: 'Télécharger le certificat (PDF)' }).click())
    const valide = await pyhanko(page.request, apres.octets)
    expect(valide.valide, valide.rapport).toBe(true)
  })

  test('BS-08 — horodatage RFC 3161 de l’ICP : jeton accordé et vérifié par OpenSSL contre la racine', async ({ request }) => {
    const donnees = Buffer.from(d.horodatage.donnees, 'utf8')
    const r = await request.post(`${S.icp}/tsa`, { headers: { 'Content-Type': 'application/timestamp-query' }, data: requeteHorodatage(donnees) })
    expect(r.status()).toBe(200)
    expect(r.headers()['content-type']).toBe('application/timestamp-reply')
    const v = await ok(request.post(`${S.validation}/horodatage`, { data: { reponse: (await r.body()).toString('base64'), donnees: donnees.toString('base64') } }))
    expect(v.accorde, v.rapport).toBe(true)
    expect(v.valide, v.rapport).toBe(true)
    const autres = await ok(request.post(`${S.validation}/horodatage`, { data: { reponse: (await r.body()).toString('base64'),
      donnees: Buffer.from('document différent').toString('base64') } }))
    expect(autres.valide, 'un autre document ne correspond pas au jeton').toBe(false)
  })
})

