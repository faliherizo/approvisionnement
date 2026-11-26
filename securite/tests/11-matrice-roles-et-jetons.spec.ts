import { expect, test } from '@playwright/test'
import { MOT_DE_PASSE, Operation, SUPERADMIN, anonyme, appeler, cle, estPublique, inventaire, organisation, remplir, session } from '../outils/api'
import { consigner } from '../outils/consigner'
import matrice from '../donnees/matrice-roles.json' with { type: 'json' }
import publique from '../donnees/surface-publique.json' with { type: 'json' }

const ROLES = ['SUPERADMIN', 'ADMIN', 'REDACTEUR', 'JURIDIQUE', 'APPRO', 'DIRECTION', 'LECTEUR', 'AUDITEUR', 'ADMIN_AFFAIRES', 'ADMIN_TECHNIQUE']
const permis = matrice.matrice as Record<string, string[]>

/** Appel avec le type de corps que l'opération déclare (JSON, multipart, CSV…) : un refus doit être un 403, pas un 415. */
async function essayer(ctx: Awaited<ReturnType<typeof session>>, op: Operation) {
  const url = remplir(op)
  const sansCorps = op.methode === 'GET' || op.methode === 'DELETE'
  if (sansCorps || !op.typeCorps) return appeler(ctx, op.methode, url, sansCorps ? undefined : {})
  const options = op.typeCorps.startsWith('multipart/') ? { multipart: { fichier: { name: 'x.txt', mimeType: 'text/plain', buffer: Buffer.from('x') } } }
    : op.typeCorps === 'application/json' ? { data: {} } : { data: 'x', headers: { 'Content-Type': op.typeCorps } }
  const r = await ctx.fetch(url, { method: op.methode, failOnStatusCode: false, maxRedirects: 0, ...options })
  return { statut: r.status() }
}

/**
 * OWASP A01 / A07 — La matrice rôle × opération (DÉRIVÉE des @PreAuthorize par RoutesSecuriseesIntegrationTest, revue
 * dans donnees/matrice-roles.json) éprouvée sur le serveur réel : chaque rôle refusé reçoit 403 sur chaque opération ;
 * aucune route à jeton ne s'ouvre avec un jeton forgé ; la surface publique est limitée en débit.
 */
test.describe('INT-11 Matrice des rôles, jetons et limitation de débit', () => {
  test('chaque rôle non autorisé reçoit 403 sur chaque opération de la matrice', async () => {
    const a = await organisation('intrusion-matrice')
    const sessions: Record<string, Awaited<ReturnType<typeof session>>> = { SUPERADMIN: await session(SUPERADMIN.courriel, SUPERADMIN.motDePasse) }
    for (const role of ROLES.filter((r) => r !== 'SUPERADMIN')) {
      const courriel = `${role.toLowerCase().replace(/_/g, '-')}@intrusion-matrice.example`
      await a.admin.post('/api/admin/users', { failOnStatusCode: false, data: { email: courriel, nom: role, motDePasse: MOT_DE_PASSE, roles: [role], actif: true } })
      sessions[role] = await session(courriel, MOT_DE_PASSE)
    }
    const { operations } = await inventaire()
    const ecarts: string[] = []
    let essais = 0
    let sansMatrice = 0
    for (const op of operations.filter((o) => !estPublique(o))) {
      const roles = permis[cle(op)]
      if (!roles) { sansMatrice++; ecarts.push(`${cle(op)} absente de la matrice revue`); continue }
      if (roles.includes('*')) continue
      for (const role of ROLES.filter((r) => !roles.includes(r))) {
        const r = await essayer(sessions[role], op)
        essais++
        if (r.statut !== 403) ecarts.push(`${cle(op)} [${role}] → ${r.statut}`)
      }
    }
    consigner('11-matrice-roles', { operations: Object.keys(permis).length, essais, sansMatrice, ecarts })
    expect(ecarts, 'rôles refusés par la matrice mais non refusés (403) par le serveur').toEqual([])
  })

  test('aucune route publique à jeton ne s\'ouvre sans jeton ni avec un jeton forgé', async () => {
    const motifs = publique.operations.filter((p) => p.controle === 'jeton').map((p) => new RegExp(p.motif))
    const { operations } = await inventaire()
    const anon = await anonyme()
    const ecarts: string[] = []
    let essais = 0
    for (const op of operations.filter((o) => motifs.some((m) => m.test(cle(o))))) {
      for (const jeton of ['jeton-forge-0000000000000000', 'eyJhbGciOiJub25lIn0.e30.']) {
        const url = remplir(op, { '*': jeton })
        const r = await anon.fetch(url, { method: op.methode, failOnStatusCode: false, maxRedirects: 0,
          headers: { Authorization: `Bearer ${jeton}` }, data: op.methode === 'GET' || op.methode === 'DELETE' ? undefined : {} })
        essais++
        if (r.status() < 400 || r.status() >= 500) ecarts.push(`${cle(op)} [${jeton.slice(0, 12)}] → ${r.status()}`)
      }
    }
    consigner('11-jetons-forges', { essais, ecarts })
    expect(ecarts).toEqual([])
  })

  test('la surface publique est limitée en débit par client, sans contournement par X-Forwarded-For', async () => {
    const anon = await anonyme()
    const premiere = await anon.get('/api/health', { failOnStatusCode: false })
    const limite = Number(premiere.headers()['ratelimit-limit'] ?? 0)
    expect(limite, 'en-tête RateLimit-Limit annoncé').toBeGreaterThan(0)
    let refus = 0
    for (let i = 0; i < limite + 1 && refus === 0; i++) {
      // Le client forge la première entrée ; le frontal (ici, la boucle locale) a ajouté l'adresse réelle en dernier.
      const r = await anon.get('/api/health', { failOnStatusCode: false, headers: { 'X-Forwarded-For': `203.0.113.${i % 250}, 198.51.100.77` } })
      if (r.status() === 429) refus = i + 1
    }
    consigner('11-limitation-debit', { famille: '/api/health', limite, refuseApres: refus })
    expect(refus, 'la limite est atteinte malgré un X-Forwarded-For différent à chaque requête').toBeGreaterThan(0)
    expect(refus).toBeLessThanOrEqual(limite + 1)
  })
})
