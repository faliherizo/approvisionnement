import { expect, test } from '@playwright/test'
import { appeler, cle, estPublique, inventaire, organisation, remplir } from '../outils/api'
import { consigner } from '../outils/consigner'
import charges from '../donnees/charges.json' with { type: 'json' }

const TRACE = /SQLSyntax|org\.h2\.|PSQLException|SQLState|org\.hibernate|java\.lang\.|at qc\.levis/

/** OWASP A03 — Injection : SQL, XSS stockée, traversée de répertoires, formules CSV. */
test.describe('INT-06 Injection', () => {
  test('aucune charge d\'injection SQL ne provoque d\'erreur ni ne divulgue la base', async () => {
    const a = await organisation('intrusion-injection')
    const { operations } = await inventaire()
    const ecarts: string[] = []
    let essais = 0
    for (const op of operations.filter((o) => o.methode === 'GET' && o.parametres.length === 0 && !estPublique(o))) {
      for (const charge of charges.injectionSql) {
        const q = encodeURIComponent(charge)
        const r = await appeler(a.admin, 'GET', `${remplir(op)}?q=${q}&utilisateur=${q}&action=${q}&domaine=${q}&nom=${q}`)
        essais++
        if (r.statut >= 500 || TRACE.test(r.texte)) ecarts.push(`${cle(op)} [${charge}] → ${r.statut}`)
        if (/\$2[ab]\$/.test(r.texte)) ecarts.push(`${cle(op)} [${charge}] divulgue une empreinte de mot de passe`)
      }
    }
    for (const charge of charges.injectionSql) {
      const r = await appeler(a.admin, 'GET', `/api/clauses/recherche?q=${encodeURIComponent(charge)}`)
      essais++
      if (r.statut !== 200) ecarts.push(`/api/clauses/recherche [${charge}] → ${r.statut}`)
    }
    consigner('06-injection-sql', { essais, ecarts })
    expect(ecarts).toEqual([])
  })

  test('une charge XSS est conservée comme texte et servie en JSON non interprétable', async () => {
    const a = await organisation('intrusion-injection')
    const ecarts: string[] = []
    for (const charge of charges.xss) {
      const r = await a.admin.post('/api/clauses', { data: { code: `XSS${Date.now()}${Math.random().toString(36).slice(2, 6)}`, titre: charge, categorie: 'STANDARD', corps: charge }, failOnStatusCode: false })
      const h = r.headers()
      if (!String(h['content-type']).startsWith('application/json')) ecarts.push(`type servi : ${h['content-type']}`)
      if (h['x-content-type-options'] !== 'nosniff') ecarts.push('X-Content-Type-Options absent')
    }
    consigner('06-xss', { charges: charges.xss.length, ecarts })
    expect(ecarts).toEqual([])
  })

  test('aucune traversée de répertoires n\'atteint un fichier hors du dépôt de l\'organisation', async () => {
    const a = await organisation('intrusion-injection')
    const ecarts: string[] = []
    for (const charge of charges.traversee) {
      const r = await appeler(a.admin, 'GET', `/api/simulateur/depots/contenu?chemin=${encodeURIComponent(charge)}`)
      if (r.statut < 400 || /root:x:0:0/.test(r.texte)) ecarts.push(`${charge} → ${r.statut}`)
    }
    consigner('06-traversee', { charges: charges.traversee.length, ecarts })
    expect(ecarts).toEqual([])
  })
})
