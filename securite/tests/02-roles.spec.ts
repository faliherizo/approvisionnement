import { expect, test } from '@playwright/test'
import { appeler, cle, corpsMinimal, estPublique, inventaire, organisation, remplir } from '../outils/api'
import { parametresPour, poserCanaris } from '../outils/canari'
import { consigner } from '../outils/consigner'
import roles from '../donnees/roles.json' with { type: 'json' }

const permis = (operation: string) => roles.permisAuLecteur.some((p) => new RegExp(p.motif).test(operation))

/** OWASP A01 — Moindre privilège : un lecteur ne modifie rien hors de son périmètre ; un administrateur d'organisation n'atteint pas l'administration de la plateforme. */
test.describe('INT-02 Rôles et moindre privilège', () => {
  test('un lecteur ne peut exécuter aucune modification réservée', async () => {
    const a = await organisation('intrusion-roles')
    const canaris = await poserCanaris(a)
    const { operations, schemas } = await inventaire()
    const lignes: { operation: string; statut: number }[] = []
    for (const op of operations.filter((o) => o.methode !== 'GET' && !estPublique(o) && !permis(cle(o)))) {
      const url = remplir(op, parametresPour(op, canaris) ?? {})
      const r = await appeler(a.lecteur, op.methode, url, op.corps ? corpsMinimal(op.corps, schemas) : undefined)
      lignes.push({ operation: cle(op), statut: r.statut })
    }
    const ecarts = lignes.filter((l) => l.statut >= 200 && l.statut < 300)
    consigner('02-roles-lecteur', { eprouvees: lignes.length, refusees403: lignes.filter((l) => l.statut === 403).length,
      autresRefus: lignes.filter((l) => l.statut >= 400 && l.statut !== 403).length, ecarts, erreurs500: lignes.filter((l) => l.statut >= 500) })
    expect(ecarts, 'modifications acceptées pour un lecteur').toEqual([])
    expect(lignes.filter((l) => l.statut >= 500), 'erreurs internes').toEqual([])
    expect(await canaris.verifier()).toEqual([])
  })

  test('un administrateur d\'organisation n\'atteint aucune opération de la plateforme', async () => {
    const a = await organisation('intrusion-roles')
    const { operations, schemas } = await inventaire()
    const lignes: { operation: string; statut: number }[] = []
    for (const op of operations.filter((o) => o.chemin.startsWith('/api/superadmin'))) {
      const r = await appeler(a.admin, op.methode, remplir(op, { '*': a.id }), op.corps ? corpsMinimal(op.corps, schemas) : undefined)
      lignes.push({ operation: cle(op), statut: r.statut })
    }
    const ecarts = lignes.filter((l) => l.statut !== 403)
    consigner('02-roles-plateforme', { eprouvees: lignes.length, ecarts })
    expect(ecarts).toEqual([])
  })
})
