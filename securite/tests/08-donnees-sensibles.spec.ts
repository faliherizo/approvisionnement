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

  test('aucune opération d\'écriture n\'accepte un numéro de carte de paiement (PCI DSS hors périmètre)', async () => {
    // IQ-D-14 (ADR-022) : le numéro est refusé à la frontière, avant toute logique métier, et n'est jamais renvoyé.
    const a = await organisation('intrusion-donnees')
    const sa = await session(SUPERADMIN.courriel, SUPERADMIN.motDePasse)
    const { operations } = await inventaire()
    const ecritures = operations.filter((o) => ['POST', 'PUT', 'PATCH'].includes(o.methode) && !estPublique(o))
    const ecarts: string[] = []
    for (const op of ecritures) {
      // Les opérations de la plateforme se refusent à l'administrateur d'organisation avant la garde : on les éprouve en superadministrateur.
      const qui = op.chemin.startsWith('/api/superadmin/') ? sa : a.admin
      const r = await appeler(qui, op.methode, remplir(op, { '*': 1 }), { nom: 'Carte 4111 1111 1111 1111', note: '5555-5555-5555-4444' })
      if (r.statut !== 422) ecarts.push(`${cle(op)} → ${r.statut}`)
      else if (r.texte.includes('4111')) ecarts.push(`${cle(op)} renvoie le numéro`)
    }
    consigner('08-cartes-paiement', { eprouvees: ecritures.length, ecarts })
    expect(ecarts).toEqual([])
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
