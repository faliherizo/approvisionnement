import { expect, test } from '@playwright/test'
import { readFileSync } from 'node:fs'
import { anonyme, organisation } from '../outils/api'
import { consigner } from '../outils/consigner'

const RACINE = new URL('../../', import.meta.url)
const lire = (chemin: string) => readFileSync(new URL(chemin, RACINE), 'utf8')

/** OWASP A05 — Configuration : en-têtes de sécurité, CORS, erreurs, méthodes, points d'administration, profil de production. */
test.describe('INT-07 Configuration de sécurité', () => {
  test('l\'API pose les en-têtes de sécurité', async () => {
    const a = await organisation('intrusion-config')
    const h = (await a.admin.get('/api/auth/me')).headers()
    const attendus: Record<string, RegExp> = {
      'x-content-type-options': /^nosniff$/,
      'x-frame-options': /^DENY$/i,
      'content-security-policy': /default-src 'none'|frame-ancestors 'none'/,
      'referrer-policy': /no-referrer|strict-origin/,
      'cache-control': /no-store|no-cache/,
      'cross-origin-resource-policy': /^same-origin$/,
      'cross-origin-opener-policy': /^same-origin$/,
    }
    const ecarts = Object.entries(attendus).filter(([n, v]) => !v.test(h[n] ?? '')).map(([n]) => `${n} : ${h[n] ?? 'absent'}`)
    consigner('07-entetes-api', { entetes: h, ecarts })
    expect(ecarts).toEqual([])
  })

  test('le frontal de production pose CSP, HSTS et la protection contre l\'encadrement', () => {
    const conf = lire('frontend/default.conf.template')
    const attendus = ['Content-Security-Policy', 'X-Content-Type-Options', 'X-Frame-Options', 'Referrer-Policy', 'Strict-Transport-Security', 'Permissions-Policy', 'Cross-Origin-Opener-Policy', 'Cross-Origin-Resource-Policy', 'server_tokens off']
    const ecarts = attendus.filter((x) => !conf.includes(x))
    consigner('07-entetes-frontal', { ecarts })
    expect(ecarts).toEqual([])
  })

  test('une origine étrangère n\'obtient pas l\'autorisation CORS', async () => {
    const anon = await anonyme()
    const r = await anon.fetch('/api/auth/me', { method: 'OPTIONS', failOnStatusCode: false,
      headers: { Origin: 'https://pirate.example', 'Access-Control-Request-Method': 'GET' } })
    const permis = r.headers()['access-control-allow-origin']
    consigner('07-cors', { statut: r.status(), allowOrigin: permis ?? null })
    expect(permis === 'https://pirate.example' || permis === '*').toBe(false)
  })

  test('une requête d\'une origine étrangère qui modifie est refusée (CSRF)', async () => {
    const a = await organisation('intrusion-config')
    const r = await a.admin.post('/api/clauses', { failOnStatusCode: false, headers: { Origin: 'https://pirate.example' },
      data: { code: `CSRF${Date.now()}`, titre: 'csrf', categorie: 'STANDARD', corps: 'csrf' } })
    consigner('07-csrf', { statut: r.status() })
    expect(r.status()).toBe(403)
  })

  test('les erreurs ne divulguent aucune trace et les méthodes dangereuses sont fermées', async () => {
    const a = await organisation('intrusion-config')
    const ecarts: string[] = []
    const malforme = await a.admin.post('/api/clauses', { headers: { 'Content-Type': 'application/json' }, data: '{"code":', failOnStatusCode: false })
    if (malforme.status() >= 500 || /Exception|at [a-z]+\./.test(await malforme.text())) ecarts.push(`JSON malformé → ${malforme.status()}`)
    for (const m of ['TRACE', 'TRACK']) {
      const r = await a.admin.fetch('/api/auth/me', { method: m, failOnStatusCode: false })
      if (r.status() < 400) ecarts.push(`${m} accepté`)
    }
    for (const url of ['/actuator', '/actuator/env', '/actuator/heapdump', '/h2-console/', '/.git/config', '/WEB-INF/web.xml']) {
      const r = await (await anonyme()).get(url, { failOnStatusCode: false, maxRedirects: 0 })
      if (r.ok() && !/<!doctype html>/i.test(await r.text())) ecarts.push(`${url} exposé (${r.status()})`)
    }
    consigner('07-erreurs-methodes', { ecarts })
    expect(ecarts).toEqual([])
  })

  test('le profil de production refuse les réglages de développement', () => {
    const prod = lire('backend/src/main/resources/application-prod.yml')
    const attendus = ['console:\n      enabled: false', 'api-docs:\n    enabled: ${APP_API_DOCS:false}', 'secure: ${COOKIE_SECURE:true}']
    const ecarts = attendus.filter((x) => !prod.includes(x))
    consigner('07-profil-production', { ecarts })
    expect(ecarts).toEqual([])
  })
})
