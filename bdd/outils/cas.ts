import { APIRequestContext, Playwright, expect } from '@playwright/test'
import { createHmac } from 'crypto'
import { readFileSync } from 'fs'
import { dirname, join, resolve } from 'path'
import { fileURLToPath } from 'url'
import { comptes } from './donnees'

/**
 * Moteur des cas exécutables décrits en JSON (bdd/donnees/*.json). Un cas est une suite d'appels réels à la
 * plateforme, chacun avec son statut attendu, ses captures (réutilisées par les appels suivants sous la forme
 * ${variable}) et ses vérifications sur la réponse. Aucune logique d'exigence n'est codée ici : elle vit dans les
 * données, relues et tenues à jour avec les exigences.
 */

export type Verification =
  | { chemin: string; egal: unknown }
  | { chemin: string; contient: string }
  | { chemin: string; inclut: unknown }
  | { chemin: string; exclut: unknown }
  | { chemin: string; existe: boolean }
  | { chemin: string; longueurMin: number }
  | { chemin: string; different: unknown }
  | { chemin: string; egalTexte: string }
  | { texte: string }
  | { texteAbsent: string }

export type Fichier = { fichier: string; nom: string; type?: string }

/** Lecture d'un courriel reçu par SMTP (boîte Mailpit du bac à sable), comme le ferait le destinataire. */
/** `different` : ignore une valeur déjà vue (ex. l'ancien lien), pour attendre le courriel suivant. */
export type LectureCourriel = { destinataire: string; motif: string; capturer: string; different?: string }

export type Etape = {
  appel?: string
  courriel?: LectureCourriel
  profil?: string
  /** Appel sans session (connexion, portail public) : n'altère aucune session de profil. */
  anonyme?: boolean
  corps?: unknown
  multipart?: Record<string, string | Fichier>
  /** Corps application/x-www-form-urlencoded (ex. jeton OAuth 2.0). */
  formulaire?: Record<string, string>
  entetes?: Record<string, string>
  statut?: number | number[]
  capturer?: Record<string, string>
  verifier?: Verification[]
  note?: string
}

export type Cas = { description: string; profil?: string; etapes: Etape[] }

export type Contexte = {
  variables: Record<string, unknown>
  ecarts: string[]
  derniere?: { statut: number; corps: unknown; texte: string }
}

const RACINE = resolve(dirname(fileURLToPath(import.meta.url)), '..')

/** PDF minimal d'une page, reconnu par le contrôle de format de la plateforme. */
export const PDF_MINIMAL = Buffer.from('%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
  + '3 0 obj<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]>>endobj\ntrailer<</Root 1 0 R>>\n%%EOF\n', 'latin1')

const sessions = new Map<string, APIRequestContext>()

/** Contexte d'API authentifié par profil, réutilisé pendant toute l'exécution. */
// Node ne résout pas les sous-domaines de localhost (Chromium, si) : les appels d'API d'une organisation tournée sur
// <org>.e2e.localhost passent par le même serveur, sous localhost ; le compte connecté désigne l'organisation.
export const hoteApi = (baseURL: string) => baseURL.replace(/\/\/[^/:]+\.localhost(?=[:/]|$)/, '//localhost')

export async function session(playwright: Playwright, baseURL: string, profil: string): Promise<APIRequestContext> {
  baseURL = hoteApi(baseURL)
  const cle = `${baseURL}|${profil}`
  const existante = sessions.get(cle)
  if (existante) return existante
  const compte = comptes[profil]
  if (!compte) throw new Error(`Profil inconnu dans donnees/comptes.json : « ${profil} »`)
  const c = await playwright.request.newContext({ baseURL })
  const r = await c.post('/api/auth/login', { data: { email: compte.email, motDePasse: compte.motDePasse } })
  expect(r.ok(), `connexion de « ${profil} » (${compte.email}) → ${r.status()} ${await r.text()}`).toBeTruthy()
  sessions.set(cle, c)
  return c
}

export function oublierSession(baseURL: string, profil: string) {
  sessions.delete(`${hoteApi(baseURL)}|${profil}`)
}

/** Code TOTP (RFC 6238 : HMAC-SHA1, 30 s, 6 chiffres) du secret base32, comme une application d'authentification. */
export function totp(secret: string, instant = Date.now()): string {
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'
  let bits = ''
  for (const c of secret.replace(/=+$/, '').toUpperCase()) bits += alphabet.indexOf(c).toString(2).padStart(5, '0')
  const cle = Buffer.from(bits.match(/.{8}/g)!.map((b) => parseInt(b, 2)))
  const compteur = Buffer.alloc(8)
  compteur.writeBigUInt64BE(BigInt(Math.floor(instant / 30_000)))
  const h = createHmac('sha1', cle).update(compteur).digest()
  const o = h[h.length - 1] & 0xf
  return String((h.readUInt32BE(o) & 0x7fffffff) % 1_000_000).padStart(6, '0')
}

/** Remplace ${variable} par sa valeur ; une chaîne réduite à « ${x} » garde le type de la valeur (nombre, objet…). */
export function substituer<T>(valeur: T, variables: Record<string, unknown>): T {
  if (typeof valeur === 'string') {
    const seule = valeur.match(/^\$\{([\w.-]+)\}$/)
    if (seule && seule[1] in variables) return variables[seule[1]] as T
    return valeur.replace(/\$\{totp:([A-Z2-7]+)\}/g, (_, secret) => totp(secret)).replace(/\$\{([\w.-]+)\}/g, (_, nom) => {
      if (!(nom in variables)) throw new Error(`Variable non capturée : \${${nom}}`)
      return String(variables[nom])
    }) as T
  }
  if (Array.isArray(valeur)) return valeur.map((v) => substituer(v, variables)) as T
  if (valeur && typeof valeur === 'object') {
    // Une clé peut porter une variable (libellé d'un champ qui contient un code unique) : elle est substituée aussi.
    return Object.fromEntries(Object.entries(valeur).map(([k, v]) => [k.includes('${') ? String(substituer(k, variables)) : k, substituer(v, variables)])) as T
  }
  return valeur
}

/** Chemin JSON simple : $.a.b[0].c, $[1], $.a[*].b (projection sur un tableau), $[?slug=x].id (premier élément filtré). */
export function lire(objet: unknown, chemin: string): unknown {
  const parties = chemin.replace(/^\$\.?/, '').match(/\[\?[^\]]+\]|[^.[\]]+|\[\d+\]|\[\*\]/g) ?? []
  let courant: unknown = objet
  for (let i = 0; i < parties.length; i++) {
    const p = parties[i]
    if (courant === undefined || courant === null) return undefined
    if (p === '[*]') {
      const reste = '$' + parties.slice(i + 1).map((x) => (x.startsWith('[') ? x : '.' + x)).join('')
      return (courant as unknown[]).map((e) => (reste === '$' ? e : lire(e, reste)))
    }
    const filtre = p.match(/^\[\?([\w.-]+)=(.+)\]$/)
    if (filtre) {
      courant = (courant as Record<string, unknown>[]).find((e) => String(e?.[filtre[1]]) === filtre[2])
      continue
    }
    const index = p.match(/^\[(\d+)\]$/)
    courant = index ? (courant as unknown[])[Number(index[1])] : (courant as Record<string, unknown>)[p]
  }
  return courant
}

function verifier(v: Verification, corps: unknown, texte: string): string | null {
  if ('texte' in v) return texte.includes(v.texte) ? null : `la réponse devrait contenir « ${v.texte} »`
  if ('texteAbsent' in v) return texte.includes(v.texteAbsent) ? `la réponse ne devrait pas contenir « ${v.texteAbsent} »` : null
  const valeur = lire(corps, v.chemin)
  const vue = JSON.stringify(valeur)?.slice(0, 200)
  if ('egal' in v) return JSON.stringify(valeur) === JSON.stringify(v.egal) ? null : `${v.chemin} = ${vue}, attendu ${JSON.stringify(v.egal)}`
  if ('egalTexte' in v) return String(valeur) === String(v.egalTexte) ? null : `${v.chemin} = ${vue}, attendu « ${v.egalTexte} »`
  if ('different' in v) return JSON.stringify(valeur) !== JSON.stringify(v.different) ? null : `${v.chemin} ne devrait pas valoir ${vue}`
  if ('contient' in v) return JSON.stringify(valeur ?? '').includes(v.contient) ? null : `${v.chemin} = ${vue}, devrait contenir « ${v.contient} »`
  if ('inclut' in v) {
    const liste = Array.isArray(valeur) ? valeur.flat(Infinity) : []
    return liste.some((e) => JSON.stringify(e) === JSON.stringify(v.inclut)) ? null : `${v.chemin} = ${vue}, devrait inclure ${JSON.stringify(v.inclut)}`
  }
  if ('exclut' in v) {
    const liste = Array.isArray(valeur) ? valeur.flat(Infinity) : []
    return liste.some((e) => JSON.stringify(e) === JSON.stringify(v.exclut)) ? `${v.chemin} = ${vue}, ne devrait pas inclure ${JSON.stringify(v.exclut)}` : null
  }
  if ('existe' in v) return (valeur !== undefined && valeur !== null) === v.existe ? null : `${v.chemin} ${v.existe ? 'absent' : 'présent'} (${vue})`
  if ('longueurMin' in v) {
    const n = Array.isArray(valeur) || typeof valeur === 'string' ? valeur.length : -1
    return n >= v.longueurMin ? null : `${v.chemin} : longueur ${n}, au moins ${v.longueurMin} attendu`
  }
  return `vérification inconnue : ${JSON.stringify(v)}`
}

function contenuFichier(f: Fichier): Buffer {
  if (f.fichier === 'pdf') return PDF_MINIMAL
  if (f.fichier.startsWith('pdf:')) {
    // PDF de la taille demandée : en-tête valide, puis remplissage en commentaire (fichier volumineux réaliste).
    const taille = Number(f.fichier.slice(4))
    return Buffer.concat([PDF_MINIMAL, Buffer.alloc(Math.max(0, taille - PDF_MINIMAL.length), '%')])
  }
  if (f.fichier.startsWith('texte:')) return Buffer.from(f.fichier.slice(6), 'utf8')
  if (f.fichier.startsWith('chemin:')) return readFileSync(join(RACINE, f.fichier.slice(7)))
  throw new Error(`Fichier inconnu : ${f.fichier} (pdf, texte:…, chemin:…)`)
}

const BOITE = process.env.BDD_COURRIEL_URL ?? 'http://localhost:18025/api/v1'

/** Attend le plus récent courriel du destinataire qui contient le motif (expression régulière à un groupe). */
async function lireCourriel(playwright: Playwright, l: LectureCourriel, ou: string): Promise<string> {
  const api = await playwright.request.newContext()
  const motif = new RegExp(l.motif)
  for (let essai = 0; essai < 30; essai++) {
    const r = await api.get(`${BOITE}/search?query=${encodeURIComponent(`to:${l.destinataire}`)}`).catch(() => null)
    expect(r?.ok(), `${ou} : boîte courriel du bac à sable injoignable (${BOITE})`).toBeTruthy()
    for (const m of ((await r!.json()).messages ?? []).slice(0, 5)) {
      const texte = (await (await api.get(`${BOITE}/message/${m.ID}`)).json()).Text as string
      const trouve = texte.match(motif)
      if (trouve && trouve[1] !== l.different) return trouve[1]
    }
    await new Promise((ok) => setTimeout(ok, 1_000))
  }
  throw new Error(`${ou} : aucun courriel pour ${l.destinataire} correspondant à /${l.motif}/`)
}

/** Exécute un cas ; un statut inattendu arrête le cas, les vérifications de contenu sont toutes relevées. */
export async function executer(playwright: Playwright, baseURL: string, profilParDefaut: string, nom: string, cas: Cas, ctx: Contexte) {
  for (const [i, brute] of cas.etapes.entries()) {
    const e = substituer(brute, ctx.variables)
    if (e.courriel) {
      ctx.variables[e.courriel.capturer] = await lireCourriel(playwright, e.courriel, `cas « ${nom} », étape ${i + 1}`)
      continue
    }
    const [methode, chemin] = e.appel!.split(' ')
    const api = e.anonyme ? await playwright.request.newContext({ baseURL: hoteApi(baseURL) }) : await session(playwright, baseURL, e.profil ?? cas.profil ?? profilParDefaut)
    const options: Parameters<APIRequestContext['fetch']>[1] = { method: methode, headers: e.entetes, failOnStatusCode: false, timeout: 60_000 }
    if (e.multipart) {
      options.multipart = Object.fromEntries(Object.entries(e.multipart).map(([k, v]) => [k, typeof v === 'string' ? v
        : { name: v.nom, mimeType: v.type ?? 'application/octet-stream', buffer: contenuFichier(v) }]))
    } else if (e.formulaire) {
      options.form = e.formulaire
    } else if (e.corps !== undefined) {
      options.data = e.corps
    }
    const r = await api.fetch(chemin, options)
    const texte = await r.text()
    let corps: unknown = texte
    try { corps = texte ? JSON.parse(texte) : null } catch { /* réponse non JSON : texte brut */ }
    ctx.derniere = { statut: r.status(), corps, texte }
    const attendus = e.statut === undefined ? [200, 201, 204] : Array.isArray(e.statut) ? e.statut : [e.statut]
    expect(attendus, `cas « ${nom} », étape ${i + 1} (${e.appel}) : statut ${r.status()} — ${texte.slice(0, 300)}`).toContain(r.status())
    for (const [variable, cheminJson] of Object.entries(e.capturer ?? {})) {
      const valeur = lire(corps, cheminJson)
      if (valeur === undefined) ctx.ecarts.push(`cas « ${nom} », étape ${i + 1} : capture ${variable} ← ${cheminJson} introuvable`)
      ctx.variables[variable] = valeur
    }
    for (const v of e.verifier ?? []) {
      const ecart = verifier(v, corps, texte)
      if (ecart) ctx.ecarts.push(`cas « ${nom} », étape ${i + 1} (${e.appel}) : ${ecart}`)
    }
  }
}
