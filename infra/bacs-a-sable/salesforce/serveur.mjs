// Bac à sable Salesforce au niveau HTTP : imite le sous-ensemble de l'API REST que la plateforme emploie
// (OAuth 2.0 client credentials ou JWT Bearer, sobjects avec « fields= », SOQL « SELECT Id … WHERE Name = », PATCH,
// limits). Il exerce le vrai adaptateur SalesforceRest sans compte Salesforce ; il ne remplace pas la recette sur le bac
// à sable d'IQ. Aucune dépendance : node serveur.mjs. Variables : PORT, SF_CLIENT_ID, SF_CLIENT_SECRET, SF_DONNEES,
// SF_JWT_CERTIFICAT (clé publique ou certificat PEM qui vérifie les assertions JWT Bearer — addenda 2, Q11).
import { createServer } from 'node:http'
import { readFileSync } from 'node:fs'
import { createPublicKey, randomUUID, verify } from 'node:crypto'

const PORT = Number(process.env.PORT || 8099)
const CLIENT_ID = process.env.SF_CLIENT_ID || 'clm-bac-a-sable'
const CLIENT_SECRET = process.env.SF_CLIENT_SECRET || 'bac-a-sable-seulement'
const CERTIFICAT = process.env.SF_JWT_CERTIFICAT ? createPublicKey(readFileSync(process.env.SF_JWT_CERTIFICAT)) : null
const donnees = JSON.parse(readFileSync(process.env.SF_DONNEES || new URL('./donnees.json', import.meta.url), 'utf8'))

const jetons = new Set()
const journal = []
let panne = 0 // statut HTTP imposé aux appels d'API (ex. 503) pour éprouver les reprises ; 0 = aucun

const enregistrements = (objet) => donnees.objets[objet]?.enregistrements ?? null
const champsPermis = (objet) => new Set([...(donnees.objets[objet]?.champsModifiables ?? []), ...Object.keys(enregistrements(objet)?.[0] ?? {})])

function repondre(res, statut, corps) {
  res.writeHead(statut, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(corps === undefined ? '' : JSON.stringify(corps))
}

const erreur = (code, message) => [{ errorCode: code, message }]

async function lireCorps(req) {
  let s = ''
  for await (const morceau of req) s += morceau
  return s
}

function avecAttributs(objet, e) {
  return { attributes: { type: objet, url: `/services/data/${donnees.versionApi}/sobjects/${objet}/${e.Id}` }, ...e }
}

/** Comme Salesforce : « fields= » restreint la réponse ; un champ inconnu est refusé (INVALID_FIELD). */
function restreindre(objet, e, fields) {
  if (!fields) return { enregistrement: avecAttributs(objet, e) }
  const noms = fields.split(',').map((n) => n.trim()).filter(Boolean)
  const inconnu = noms.find((n) => !champsPermis(objet).has(n))
  if (inconnu) return { refus: erreur('INVALID_FIELD', `No such column '${inconnu}' on entity '${objet}'`) }
  const r = avecAttributs(objet, {})
  noms.forEach((n) => { r[n] = e[n] ?? null })
  return { enregistrement: r }
}

/** Assertion JWT Bearer (RFC 7523) : signature RS256 par la clé privée dont SF_JWT_CERTIFICAT est la contrepartie. */
function assertionValide(assertion) {
  if (!CERTIFICAT || !assertion) return false
  const [entete, charge, signature] = assertion.split('.')
  if (!signature || !verify('sha256', Buffer.from(`${entete}.${charge}`), CERTIFICAT, Buffer.from(signature, 'base64url'))) return false
  const c = JSON.parse(Buffer.from(charge, 'base64url').toString('utf8'))
  return c.iss === CLIENT_ID && Boolean(c.sub) && c.exp * 1000 > Date.now()
}

/** SOQL limité à la forme émise par l'adaptateur : SELECT Id FROM <objet> WHERE Name = '<numéro>' LIMIT 1. */
function interroger(soql) {
  const m = /^SELECT\s+Id\s+FROM\s+(\w+)\s+WHERE\s+Name\s*=\s*'((?:[^'\\]|\\.)*)'\s+LIMIT\s+1$/i.exec(soql.trim())
  if (!m) return null
  const nom = m[2].replace(/\\(.)/g, '$1')
  const trouves = (enregistrements(m[1]) ?? []).filter((e) => e.Name === nom).map((e) => ({ attributes: { type: m[1] }, Id: e.Id }))
  return { totalSize: trouves.length, done: true, records: trouves }
}

const serveur = createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`)
  const corps = await lireCorps(req)
  journal.push({ le: new Date().toISOString(), methode: req.method, chemin: url.pathname, correlation: req.headers['x-correlation-id'] ?? null,
    corps: req.method === 'PATCH' ? corps : undefined })

  // Outils du bac à sable : journal des appels reçus et panne simulée.
  if (url.pathname === '/__bac-a-sable/journal') return repondre(res, 200, journal)
  if (url.pathname === '/__bac-a-sable/panne' && req.method === 'POST') {
    panne = Number(JSON.parse(corps || '{}').statut || 0)
    return repondre(res, 200, { panne })
  }
  if (url.pathname === '/__bac-a-sable/donnees') return repondre(res, 200, donnees.objets)

  if (url.pathname === '/services/oauth2/token' && req.method === 'POST') {
    const p = new URLSearchParams(corps)
    const jwt = p.get('grant_type') === 'urn:ietf:params:oauth:grant-type:jwt-bearer'
    if (jwt ? !assertionValide(p.get('assertion'))
      : p.get('grant_type') !== 'client_credentials' || p.get('client_id') !== CLIENT_ID || p.get('client_secret') !== CLIENT_SECRET) {
      return repondre(res, 400, jwt ? { error: 'invalid_grant', error_description: 'invalid assertion' }
        : { error: 'invalid_client', error_description: 'invalid client credentials' })
    }
    const jeton = `00D-bac-a-sable!${randomUUID()}`
    jetons.add(jeton)
    return repondre(res, 200, { access_token: jeton, instance_url: `http://${req.headers.host}`, token_type: 'Bearer', issued_at: String(Date.now()) })
  }

  const api = new RegExp(`^/services/data/${donnees.versionApi.replace('.', '\\.')}/(.+)$`).exec(url.pathname)
  if (!api) return repondre(res, 404, erreur('NOT_FOUND', 'The requested resource does not exist'))
  if (!jetons.has((req.headers.authorization ?? '').replace(/^Bearer /, ''))) {
    return repondre(res, 401, erreur('INVALID_SESSION_ID', 'Session expired or invalid'))
  }
  if (panne) return repondre(res, panne, erreur('SERVER_UNAVAILABLE', 'Simulated outage'))

  const chemin = api[1]
  if (chemin === 'limits') return repondre(res, 200, { DailyApiRequests: { Max: 15000, Remaining: 15000 - journal.length } })
  if (chemin === 'query') {
    const resultat = interroger(url.searchParams.get('q') ?? '')
    return resultat ? repondre(res, 200, resultat) : repondre(res, 400, erreur('MALFORMED_QUERY', 'Unsupported query in sandbox'))
  }
  const so = /^sobjects\/(\w+)\/([^/]+)$/.exec(chemin)
  if (!so) return repondre(res, 404, erreur('NOT_FOUND', 'The requested resource does not exist'))
  const [, objet, id] = so
  const liste = enregistrements(objet)
  if (!liste) return repondre(res, 404, erreur('NOT_FOUND', `sObject type '${objet}' is not supported`))
  const e = liste.find((x) => x.Id === decodeURIComponent(id))
  if (!e) return repondre(res, 404, erreur('NOT_FOUND', 'Provided external ID field does not exist or is not accessible'))
  if (req.method === 'GET') {
    const { enregistrement, refus } = restreindre(objet, e, url.searchParams.get('fields'))
    return refus ? repondre(res, 400, refus) : repondre(res, 200, enregistrement)
  }
  if (req.method === 'PATCH') {
    let maj
    try { maj = JSON.parse(corps) } catch { return repondre(res, 400, erreur('JSON_PARSER_ERROR', 'Invalid JSON')) }
    const inconnus = Object.keys(maj).filter((k) => !champsPermis(objet).has(k))
    if (inconnus.length) return repondre(res, 400, erreur('INVALID_FIELD', `No such column '${inconnus[0]}' on sobject of type ${objet}`))
    Object.assign(e, maj)
    return repondre(res, 204)
  }
  return repondre(res, 405, erreur('METHOD_NOT_ALLOWED', req.method))
})

serveur.listen(PORT, () => console.log(`Bac à sable Salesforce prêt sur http://localhost:${PORT} (API ${donnees.versionApi})`))
