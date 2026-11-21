import { expect, test } from '@playwright/test'
import { organisation } from '../outils/api'
import { consigner } from '../outils/consigner'
import charges from '../donnees/charges.json' with { type: 'json' }

/** OWASP A10 — SSRF : les tests de connexion aux fournisseurs n'atteignent ni le réseau interne ni les métadonnées infonuagiques. */
test.describe('INT-10 Falsification de requêtes côté serveur', () => {
  test('le test de découverte OIDC refuse les adresses internes et les schémas non HTTP', async () => {
    const a = await organisation('intrusion-ssrf')
    const ecarts: string[] = []
    const reponses: Record<string, unknown> = {}
    for (const cible of charges.ssrf) {
      const r = await a.admin.post('/api/parametres/integration-oidc/test', { data: { issuer: cible }, failOnStatusCode: false })
      const texte = await r.text()
      reponses[cible] = [r.status(), texte.slice(0, 160)]
      if (!/refus|interdit|non autoris/i.test(texte) && r.status() !== 400) ecarts.push(`${cible} → ${r.status()} ${texte.slice(0, 80)}`)
    }
    consigner('10-ssrf', { reponses, ecarts })
    expect(ecarts).toEqual([])
  })
})
