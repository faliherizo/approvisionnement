import { APIRequestContext, request as fabrique } from '@playwright/test'
import publique from '../donnees/surface-publique.json' with { type: 'json' }

/** Outils communs des tests d'intrusion : sessions, inventaire OpenAPI, organisations d'essai et objets « canari ». */

export const BASE = process.env.SECURITE_API_URL ?? 'http://localhost:8100'
// Compte de développement par défaut ; un environnement déployé fournit le sien (secrets de l'environnement GitHub).
export const SUPERADMIN = {
  courriel: process.env.SECURITE_SUPERADMIN_EMAIL || 'superadmin@plateforme.ca',
  motDePasse: process.env.SECURITE_SUPERADMIN_PASSWORD || 'SuperPassw0rd!',
}
export const MOT_DE_PASSE = 'Intrusion!2026x'

export type Operation = { methode: string; chemin: string; parametres: string[]; corps?: unknown; operationId?: string; typeCorps?: string }

/** Session authentifiée : le cookie de session est conservé par le contexte. */
export async function session(courriel: string, motDePasse: string): Promise<APIRequestContext> {
  const ctx = await fabrique.newContext({ baseURL: BASE })
  const r = await ctx.post('/api/auth/login', { data: { email: courriel, motDePasse } })
  if (!r.ok()) throw new Error(`Connexion refusée pour ${courriel} : ${r.status()} ${await r.text()}`)
  return ctx
}

export async function anonyme(): Promise<APIRequestContext> {
  return fabrique.newContext({ baseURL: BASE })
}

/** Inventaire de toutes les opérations de l'API, tiré de la spécification OpenAPI que publie le serveur. */
export async function inventaire(): Promise<{ operations: Operation[]; schemas: Record<string, any> }> {
  const ctx = await anonyme()
  const spec = await (await ctx.get('/v3/api-docs')).json()
  const operations: Operation[] = []
  for (const [chemin, item] of Object.entries<any>(spec.paths)) {
    for (const [methode, op] of Object.entries<any>(item)) {
      if (!['get', 'post', 'put', 'patch', 'delete'].includes(methode)) continue
      const parametres = (op.parameters ?? []).filter((p: any) => p.in === 'path').map((p: any) => p.name)
      const schema = op.requestBody?.content?.['application/json']?.schema
      const typeCorps = Object.keys(op.requestBody?.content ?? {})[0]
      operations.push({ methode: methode.toUpperCase(), chemin, parametres, corps: schema, operationId: op.operationId, typeCorps })
    }
  }
  return { operations, schemas: spec.components?.schemas ?? {} }
}

export const cle = (op: Operation) => `${op.methode} ${op.chemin}`

export function estPublique(op: Operation): boolean {
  return publique.operations.some((p) => new RegExp(p.motif).test(cle(op)))
}

/** Remplit les paramètres de chemin : identifiants fournis par nom, sinon une valeur neutre. */
export function remplir(op: Operation, valeurs: Record<string, string | number> = {}): string {
  return op.chemin.replace(/\{([^}]+)\}/g, (_, nom) => String(valeurs[nom] ?? valeurs['*'] ?? 1))
}

/** Corps minimal valide d'après le schéma : champs requis remplis de valeurs plausibles (courriel, date, énumération…). */
export function corpsMinimal(schema: any, schemas: Record<string, any>, profondeur = 0): unknown {
  if (!schema || profondeur > 4) return {}
  if (schema.$ref) return corpsMinimal(schemas[schema.$ref.split('/').pop()], schemas, profondeur + 1)
  if (schema.enum) return schema.enum[0]
  switch (schema.type) {
    case 'object': {
      const o: Record<string, unknown> = {}
      for (const [nom, prop] of Object.entries<any>(schema.properties ?? {})) o[nom] = valeur(nom, prop, schemas, profondeur)
      return o
    }
    case 'array': return []
    case 'integer': case 'number': return 1
    case 'boolean': return false
    default: return 'essai'
  }
}

function valeur(nom: string, prop: any, schemas: Record<string, any>, profondeur: number): unknown {
  if (/email|courriel|destinataire/i.test(nom) && (prop.type === 'string' || !prop.type)) return 'essai@intrusion.example'
  if (prop.format === 'date') return new Date().toISOString().slice(0, 10)
  if (prop.format === 'date-time') return new Date().toISOString()
  return corpsMinimal(prop, schemas, profondeur + 1)
}

/** Organisation d'essai complète : tous les modules activables, un administrateur, un lecteur. */
export type Organisation = { id: number; slug: string; admin: APIRequestContext; courrielAdmin: string; lecteur: APIRequestContext; courrielLecteur: string }

export async function organisation(slug: string): Promise<Organisation> {
  const sa = await session(SUPERADMIN.courriel, SUPERADMIN.motDePasse)
  const existantes = await (await sa.get('/api/superadmin/tenants')).json() as { id: number; slug: string }[]
  let t = existantes.find((x) => x.slug === slug)
  const courrielAdmin = `admin@${slug}.example`
  if (!t) {
    t = await (await sa.post('/api/superadmin/tenants', { data: { slug, titre: `Intrusion ${slug}`, sousTitre: 'Tests de sécurité' } })).json()
    await sa.post(`/api/superadmin/tenants/${t!.id}/admin`, { data: { email: courrielAdmin, motDePasse: MOT_DE_PASSE, nom: `Admin ${slug}` } })
  }
  // Tous les modules, sauf la famille CLM : elle exclut les autres (ADR-010). L'étanchéité de CLM est éprouvée par
  // IsolationTenantIqTest, entre deux organisations CLM.
  const modules = await (await sa.get('/api/superadmin/modules')).json() as { code: string; famille: string }[]
  for (const m of modules.filter((x) => x.famille !== 'CLM')) await sa.put(`/api/superadmin/tenants/${t!.id}/modules/${m.code}/activer`)
  const admin = await session(courrielAdmin, MOT_DE_PASSE)
  const courrielLecteur = `lecteur@${slug}.example`
  await admin.post('/api/admin/users', { data: { email: courrielLecteur, nom: 'Lecteur', motDePasse: MOT_DE_PASSE, roles: ['LECTEUR'], actif: true } })
  return { id: t!.id, slug, admin, courrielAdmin, lecteur: await session(courrielLecteur, MOT_DE_PASSE), courrielLecteur }
}

/** Charge d'un appel quelconque, pour les balayages génériques. */
export async function appeler(ctx: APIRequestContext, methode: string, url: string, corps?: unknown) {
  const r = await ctx.fetch(url, { method: methode, data: corps, failOnStatusCode: false, maxRedirects: 0 })
  return { statut: r.status(), texte: await r.text().catch(() => ''), entetes: r.headers() }
}
