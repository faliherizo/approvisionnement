import { expect, test } from '@playwright/test'
import { SUPERADMIN, appeler, cle, estPublique, inventaire, organisation, remplir, session } from '../outils/api'
import { parametresPour, poserCanaris } from '../outils/canari'
import { consigner } from '../outils/consigner'
import charges from '../donnees/charges.json' with { type: 'json' }

/** OWASP A02 / A04 / A08 — Aucun secret dans les réponses ; aucune affectation de masse ne donne un privilège. */
test.describe('INT-08 Données sensibles et affectation de masse', () => {
  test('aucune réponse ne contient d\'empreinte de mot de passe, de secret TOTP ni de secret client', async () => {
    const a = await organisation('intrusion-donnees')
    const canaris = await poserCanaris(a)
    const sa = await session(SUPERADMIN.courriel, SUPERADMIN.motDePasse)
    const { operations } = await inventaire()
    const ecarts: string[] = []
    let eprouvees = 0
    for (const op of operations.filter((o) => o.methode === 'GET' && !estPublique(o))) {
      const valeurs = op.parametres.length ? parametresPour(op, canaris) : {}
      if (valeurs === undefined) continue
      for (const ctx of [a.admin, sa]) {
        const r = await appeler(ctx, 'GET', remplir(op, valeurs))
        eprouvees++
        for (const s of charges.secretsInterdits) if (r.texte.includes(s)) ecarts.push(`${cle(op)} contient « ${s} »`)
      }
    }
    consigner('08-secrets', { eprouvees, ecarts: [...new Set(ecarts)] })
    expect([...new Set(ecarts)]).toEqual([])
  })

  test('un administrateur d\'organisation ne se donne ni ne donne de privilège de plateforme', async () => {
    const a = await organisation('intrusion-donnees')
    const courriel = `masse.${Date.now()}@intrusion-donnees.example`
    const r = await a.admin.post('/api/admin/users', { failOnStatusCode: false,
      data: { email: courriel, nom: 'M', motDePasse: 'Intrusion!2026x', roles: ['SUPERADMIN', 'LECTEUR'], actif: true, tenantId: 1 } })
    const ecarts: string[] = []
    if (r.ok()) {
      const u = await r.json()
      if ((u.roles ?? []).includes('SUPERADMIN')) ecarts.push('rôle SUPERADMIN accordé par un administrateur d\'organisation')
      if (u.tenantId !== undefined && u.tenantId !== a.id) ecarts.push(`compte créé dans une autre organisation (${u.tenantId})`)
    }
    consigner('08-affectation-masse', { statut: r.status(), ecarts })
    expect(ecarts).toEqual([])
  })
})
