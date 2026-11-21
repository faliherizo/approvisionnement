import { expect, test } from '@playwright/test'
import { BASE, MOT_DE_PASSE, anonyme, organisation, session } from '../outils/api'
import { consigner } from '../outils/consigner'

const b64url = (o: unknown) => Buffer.from(JSON.stringify(o)).toString('base64url')

/** OWASP A07 — Authentification : force brute, énumération des comptes, jetons falsifiés, cookie, déconnexion. */
test.describe('INT-05 Authentification et session', () => {
  test('la force brute est freinée, sans révéler si le compte existe', async () => {
    const a = await organisation('intrusion-auth')
    const cible = `brute.${Date.now()}@intrusion-auth.example`
    await a.admin.post('/api/admin/users', { data: { email: cible, nom: 'Cible', motDePasse: MOT_DE_PASSE, roles: ['LECTEUR'], actif: true } })
    const anon = await anonyme()
    const statuts: number[] = []
    for (let i = 0; i < 12; i++) statuts.push((await anon.post('/api/auth/login', { data: { email: cible, motDePasse: `Faux${i}!xx` }, failOnStatusCode: false })).status())
    const apres = await anon.post('/api/auth/login', { data: { email: cible, motDePasse: MOT_DE_PASSE }, failOnStatusCode: false })
    const inconnu = await anon.post('/api/auth/login', { data: { email: `inconnu.${Date.now()}@x.example`, motDePasse: 'Faux1!xx' }, failOnStatusCode: false })
    const faux = await anon.post('/api/auth/login', { data: { email: a.courrielAdmin, motDePasse: 'Faux1!xx' }, failOnStatusCode: false })
    const bilan = { statuts, apresBlocage: apres.status(), inconnu: [inconnu.status(), await inconnu.text()], mauvaisMotDePasse: [faux.status(), await faux.text()] }
    consigner('05-force-brute', bilan)
    expect(statuts.slice(-2), 'au-delà du seuil, les essais sont refusés (429)').toEqual([429, 429])
    expect(apres.status(), 'même le bon mot de passe attend la fin du blocage').toBe(429)
    expect(bilan.inconnu, 'un compte inconnu et un mauvais mot de passe répondent pareil').toEqual(bilan.mauvaisMotDePasse)
  })

  test('un jeton falsifié, sans signature ou expiré est refusé', async () => {
    const a = await organisation('intrusion-auth')
    const etat = await a.admin.storageState()
    const jeton = etat.cookies.find((c) => c.name === 'ACCESS_TOKEN')!.value
    const [entete, charge, signature] = jeton.split('.')
    const contenu = JSON.parse(Buffer.from(charge, 'base64url').toString())
    const essais: Record<string, string> = {
      'rôle élevé, signature d\'origine': `${entete}.${b64url({ ...contenu, roles: ['SUPERADMIN'], role: 'SUPERADMIN' })}.${signature}`,
      'algorithme none': `${b64url({ alg: 'none', typ: 'JWT' })}.${charge}.`,
      'expiré': `${entete}.${b64url({ ...contenu, exp: 1 })}.${signature}`,
      'aléatoire': 'abc.def.ghi',
    }
    const ecarts: string[] = []
    for (const [nom, valeur] of Object.entries(essais)) {
      const ctx = await anonyme()
      const r = await ctx.get('/api/superadmin/tenants', { headers: { Cookie: `ACCESS_TOKEN=${valeur}` }, failOnStatusCode: false })
      const moi = await ctx.get('/api/auth/me', { headers: { Cookie: `ACCESS_TOKEN=${valeur}` }, failOnStatusCode: false })
      if (r.ok() || moi.ok()) ecarts.push(`${nom} accepté`)
    }
    consigner('05-jetons', { essais: Object.keys(essais), ecarts })
    expect(ecarts).toEqual([])
  })

  test('le cookie de session est HttpOnly et SameSite ; la déconnexion invalide la session', async () => {
    const anon = await anonyme()
    const a = await organisation('intrusion-auth')
    const r = await anon.post('/api/auth/login', { data: { email: a.courrielAdmin, motDePasse: MOT_DE_PASSE } })
    const cookie = r.headers()['set-cookie'] ?? ''
    const jeton = /ACCESS_TOKEN=([^;]+)/.exec(cookie)?.[1] ?? ''
    await anon.post('/api/auth/logout')
    const rejoue = await (await anonyme()).get('/api/auth/me', { headers: { Cookie: `ACCESS_TOKEN=${jeton}` }, failOnStatusCode: false })
    const bilan = { httpOnly: /HttpOnly/i.test(cookie), sameSite: /SameSite=(Lax|Strict)/i.exec(cookie)?.[1] ?? null,
      secure: /Secure/i.test(cookie), jetonRejoueApresDeconnexion: rejoue.status(), base: BASE }
    consigner('05-cookie', bilan)
    expect(bilan.httpOnly).toBe(true)
    expect(bilan.sameSite).not.toBeNull()
    expect(rejoue.status(), 'un jeton rejoué après déconnexion est refusé').toBe(401)
  })

  test('un mot de passe faible est refusé', async () => {
    const a = await organisation('intrusion-auth')
    const r = await a.admin.post('/api/admin/users', { data: { email: `faible.${Date.now()}@x.example`, nom: 'F', motDePasse: 'abc', roles: ['LECTEUR'], actif: true }, failOnStatusCode: false })
    expect(r.status()).toBe(400)
  })
})
