import { expect, test } from '@playwright/test'
import { anonyme, appeler, cle, estPublique, inventaire, remplir } from '../outils/api'
import { consigner } from '../outils/consigner'
import charges from '../donnees/charges.json' with { type: 'json' }

/** OWASP A01 / A07 — Toute opération non publique de l'inventaire refuse une requête sans session. */
test.describe('INT-01 Surface anonyme', () => {
  test('chaque opération non publique refuse l\'accès anonyme par un 401', async () => {
    const { operations } = await inventaire()
    const anon = await anonyme()
    const lignes: { operation: string; statut: number }[] = []
    for (const op of operations.filter((o) => !estPublique(o))) {
      const r = await appeler(anon, op.methode, remplir(op), op.corps ? {} : undefined)
      lignes.push({ operation: cle(op), statut: r.statut })
    }
    const ecarts = lignes.filter((l) => l.statut !== 401)
    consigner('01-surface-anonyme', { total: operations.length, eprouvees: lignes.length, publiques: operations.length - lignes.length, ecarts })
    expect(ecarts, 'opérations qui ne répondent pas 401 sans session').toEqual([])
  })

  test('les opérations publiques ne divulguent ni erreur interne ni secret', async () => {
    const { operations } = await inventaire()
    const anon = await anonyme()
    const ecarts: string[] = []
    for (const op of operations.filter(estPublique)) {
      const r = await appeler(anon, op.methode, remplir(op, { '*': 'inexistant' }), op.corps ? {} : undefined)
      if (r.statut >= 500) ecarts.push(`${cle(op)} → ${r.statut}`)
      for (const s of charges.secretsInterdits) if (r.texte.includes(s)) ecarts.push(`${cle(op)} divulgue « ${s} »`)
      if (/at [a-z]+\.[a-z]+\.[A-Za-z]+\(|Exception:|SQLState/.test(r.texte)) ecarts.push(`${cle(op)} divulgue une trace d'exécution`)
    }
    consigner('01-surface-publique', { publiques: operations.filter(estPublique).map(cle), ecarts })
    expect(ecarts).toEqual([])
  })
})
