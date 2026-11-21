import { expect, test } from '@playwright/test'
import { MOT_DE_PASSE, appeler, organisation, session } from '../outils/api'
import { consigner } from '../outils/consigner'

/** OWASP A01 — Dans une même organisation, une personne n'atteint pas les objets privés d'une autre (IDOR horizontal). */
test.describe('INT-04 Isolation entre personnes', () => {
  test('un demandeur ne lit ni la fiche, ni le document, ni les conversations, ni les critères d\'un autre', async () => {
    const a = await organisation('intrusion-personnes')
    const autre = `autre@intrusion-personnes.example`
    await a.admin.post('/api/admin/users', { data: { email: autre, nom: 'Autre', motDePasse: MOT_DE_PASSE, roles: ['LECTEUR'], actif: true } })
    const intrus = await session(autre, MOT_DE_PASSE)
    const f = await (await a.admin.post('/api/formulaires', { data: { code: `P${Date.now()}`, nom: 'Demande', domaine: 'RH' } })).json()
    await a.admin.put(`/api/formulaires/${f.id}/structure`, { data: { structureJson: '{"pages":[{"champs":[{"cle":"nom","libelle":"Nom"}]}]}' } })
    await a.admin.post(`/api/formulaires/${f.id}/soumettre`)
    await a.admin.post(`/api/formulaires/${f.id}/publier`)
    const fiche = await (await a.lecteur.post(`/api/formulaires/${f.id}/saisies`)).json()
    await a.lecteur.post(`/api/formulaires/${f.id}/saisies/${fiche.id}/soumettre`, { data: { donneesJson: '{"nom":"Diagnostic privé"}' } })
    const critere = await (await a.lecteur.post(`/api/rapport/criteres?nom=prive`, { data: 'x' })).json()
    const conv = await (await a.admin.post('/api/messagerie/conversations', { data: { sujet: 'Privé', participants: [a.courrielLecteur], message: 'confidentiel' } })).json()
    const essais = [
      ['GET', `/api/formulaires/${f.id}/saisies/${fiche.id}`],
      ['GET', `/api/formulaires/${f.id}/saisies/${fiche.id}/document.pdf`],
      ['PATCH', `/api/formulaires/${f.id}/saisies/${fiche.id}`],
      ['POST', `/api/formulaires/${f.id}/saisies/${fiche.id}/annuler`],
      ['GET', `/api/messagerie/conversations/${conv.id}`],
      ['DELETE', `/api/rapport/criteres/${critere.id}`],
    ]
    const ecarts: string[] = []
    for (const [m, url] of essais) {
      const r = await appeler(intrus, m, url, m === 'PATCH' ? { nom: 'modifié par un autre' } : undefined)
      if (r.statut >= 200 && r.statut < 300) ecarts.push(`${m} ${url} → ${r.statut}`)
    }
    const apres = await (await a.lecteur.get(`/api/formulaires/${f.id}/saisies/${fiche.id}`)).text()
    if (!apres.includes('Diagnostic privé')) ecarts.push('la fiche du demandeur a été altérée')
    consigner('04-isolation-personnes', { eprouvees: essais.length, ecarts })
    expect(ecarts).toEqual([])
  })
})
