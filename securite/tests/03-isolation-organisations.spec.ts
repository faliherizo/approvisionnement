import { expect, test } from '@playwright/test'
import { appeler, cle, corpsMinimal, estPublique, inventaire, organisation, remplir } from '../outils/api'
import { parametresPour, poserCanaris } from '../outils/canari'
import { consigner } from '../outils/consigner'

/**
 * OWASP A01 (IDOR) — Étanchéité entre organisations. L'organisation A pose des canaris ; l'administrateur de
 * l'organisation B balaie toute l'API : aucune liste ne doit contenir la marque de A, aucun identifiant de A ne doit
 * être lu, modifié ou supprimé, et les canaris doivent être intacts à la fin.
 */
test.describe('INT-03 Isolation entre organisations', () => {
  test('aucune réponse de l\'organisation B ne contient les données de A', async () => {
    const a = await organisation('intrusion-a')
    const b = await organisation('intrusion-b')
    const canaris = await poserCanaris(a)
    const { operations } = await inventaire()
    const fuites: string[] = []
    let eprouvees = 0
    for (const op of operations.filter((o) => o.methode === 'GET' && !estPublique(o))) {
      const valeurs = op.parametres.length ? parametresPour(op, canaris) : {}
      if (valeurs === undefined) continue
      eprouvees++
      for (const ctx of [b.admin, b.lecteur]) {
        const r = await appeler(ctx, 'GET', remplir(op, valeurs))
        if (r.texte.includes(canaris.marque)) fuites.push(`${cle(op)} (${r.statut})`)
      }
    }
    consigner('03-isolation-lecture', { eprouvees, fuites })
    expect(fuites, 'réponses de B contenant des données de A').toEqual([])
  })

  test('aucun identifiant de A n\'est modifiable ou supprimable depuis B', async () => {
    const a = await organisation('intrusion-a')
    const b = await organisation('intrusion-b')
    const canaris = await poserCanaris(a)
    const { operations, schemas } = await inventaire()
    const lignes: { operation: string; statut: number }[] = []
    const nonCouvertes: string[] = []
    for (const op of operations.filter((o) => o.parametres.length > 0 && !estPublique(o) && !o.chemin.startsWith('/api/superadmin'))) {
      const valeurs = parametresPour(op, canaris)
      if (!valeurs) { nonCouvertes.push(cle(op)); continue }
      const r = await appeler(b.admin, op.methode, remplir(op, valeurs), op.corps ? corpsMinimal(op.corps, schemas) : undefined)
      lignes.push({ operation: cle(op), statut: r.statut })
    }
    const ecarts = lignes.filter((l) => l.statut >= 200 && l.statut < 300)
    const pertes = await canaris.verifier()
    consigner('03-isolation-ecriture', { eprouvees: lignes.length, ecarts, pertes, nonCouvertes })
    expect(ecarts, 'opérations de B acceptées sur les objets de A').toEqual([])
    expect(pertes, 'canaris de A altérés').toEqual([])
  })
})
