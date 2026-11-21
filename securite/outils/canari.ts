import { APIRequestContext } from '@playwright/test'
import { Operation, Organisation, SUPERADMIN, session } from './api'

/**
 * Objets « canari » créés dans l'organisation A, tous marqués d'une même chaîne. L'organisation B ne doit jamais voir
 * cette chaîne, ni lire, modifier ou supprimer ces objets par leur identifiant.
 */
export type Canaris = { marque: string; valeurs: { motif: RegExp; parametres: Record<string, string | number> }[]; verifier: () => Promise<string[]> }

const STRUCTURE = (marque: string) => JSON.stringify({ pages: [{ titre: 'P', champs: [
  { cle: 'nom', libelle: `Nom ${marque}`, type: 'TEXTE' }, { cle: 'activite', libelle: 'Activité', type: 'LISTE', options: ['A', 'B'] }] }] })

async function json(ctx: APIRequestContext, methode: string, url: string, data?: unknown) {
  const r = await ctx.fetch(url, { method: methode, data, failOnStatusCode: false })
  if (!r.ok()) throw new Error(`Canari : ${methode} ${url} → ${r.status()} ${await r.text()}`)
  const t = await r.text()
  return t ? JSON.parse(t) : {}
}

export async function poserCanaris(a: Organisation): Promise<Canaris> {
  const marque = `CANARI-${Date.now().toString(36).toUpperCase()}`
  const ad = a.admin
  const clause = await json(ad, 'POST', '/api/clauses', { code: marque, titre: `Clause ${marque}`, categorie: 'STANDARD', corps: `Texte ${marque}` })
  const document = await json(ad, 'POST', '/api/documents', { titre: `Devis ${marque}`, type: 'APPEL_OFFRES_PUBLIC', modeSollicitation: 'APPEL_OFFRES_PUBLIC', numeroAO: marque })
  const formulaire = await json(ad, 'POST', '/api/formulaires', { code: marque, nom: `Formulaire ${marque}`, domaine: 'RH' })
  await json(ad, 'PUT', `/api/formulaires/${formulaire.id}/structure`, { structureJson: STRUCTURE(marque) })
  await json(ad, 'POST', `/api/formulaires/${formulaire.id}/soumettre`)
  await json(ad, 'POST', `/api/formulaires/${formulaire.id}/publier`)
  const fiche = await json(ad, 'POST', `/api/formulaires/${formulaire.id}/saisies`)
  await json(ad, 'POST', `/api/formulaires/${formulaire.id}/saisies/${fiche.id}/soumettre`, { donneesJson: JSON.stringify({ nom: marque, activite: 'A' }) })
  const conversation = await json(ad, 'POST', '/api/messagerie/conversations', { sujet: `Sujet ${marque}`, participants: [a.courrielLecteur], message: `Message ${marque}` })
  const tableau = await json(ad, 'POST', '/api/rapport/tableaux-de-bord', { nom: `Tableau ${marque}`, partage: true,
    indicateurs: [{ titre: `Indicateur ${marque}`, type: 'COMPTEUR', formulaireId: formulaire.id }] })
  const critere = await json(ad, 'POST', `/api/rapport/criteres?formulaireId=${formulaire.id}&nom=${marque}`, { criteres: marque })
  const page = await json(ad, 'GET', '/api/admin/users?taille=200')
  const utilisateurs = Array.isArray(page) ? page : (page.contenu ?? page.content ?? page.elements ?? [])
  const lecteur = utilisateurs.find((u: { email: string }) => u.email === a.courrielLecteur)
  const sa = await session(SUPERADMIN.courriel, SUPERADMIN.motDePasse)
  const banque = await json(sa, 'POST', `/api/superadmin/tenants/${a.id}/banques-heures`,
    { nom: `Banque ${marque}`, debut: '2026-01-01', fin: '2027-12-31', heures: 10 })

  const valeurs = [
    { motif: /^\/api\/formulaires\/\{[^}]+\}\/saisies\/\{[^}]+\}/, parametres: { fid: formulaire.id, id: fiche.id, '*': fiche.id } },
    { motif: /^\/api\/formulaires\//, parametres: { fid: formulaire.id, id: formulaire.id, '*': formulaire.id } },
    { motif: /^\/api\/rapport\/formulaires\//, parametres: { id: formulaire.id, '*': formulaire.id } },
    { motif: /^\/api\/clauses\//, parametres: { id: clause.id, '*': clause.id } },
    { motif: /^\/api\/documents\//, parametres: { id: document.id, '*': document.id } },
    { motif: /^\/api\/messagerie\//, parametres: { id: conversation.id, '*': conversation.id } },
    { motif: /^\/api\/rapport\/tableaux-de-bord\//, parametres: { id: tableau.id, '*': tableau.id } },
    { motif: /^\/api\/rapport\/criteres\//, parametres: { id: critere.id, '*': critere.id } },
    { motif: /^\/api\/admin\/users\//, parametres: { id: lecteur.id, '*': lecteur.id } },
    { motif: /^\/api\/soutien\/banque-heures\//, parametres: { id: banque.id, '*': banque.id } },
    { motif: /^\/api\/superadmin\/tenants\//, parametres: { id: a.id, tid: a.id, tenantId: a.id, '*': a.id } },
  ]

  /** Après les attaques : chaque canari existe encore chez A, inchangé. */
  const verifier = async () => {
    const pertes: string[] = []
    const lire = async (url: string, attendu: string) => {
      const r = await ad.get(url, { failOnStatusCode: false })
      if (!r.ok() || !(await r.text()).includes(attendu)) pertes.push(`${url} (${r.status()})`)
    }
    await lire(`/api/clauses/${clause.id}`, marque)
    await lire(`/api/documents/${document.id}`, marque)
    await lire(`/api/formulaires/${formulaire.id}/saisies/${fiche.id}`, marque)
    await lire(`/api/messagerie/conversations/${conversation.id}`, marque)
    await lire(`/api/rapport/tableaux-de-bord/${tableau.id}`, marque)
    await lire(`/api/soutien/banque-heures/${banque.id}`, marque)
    return pertes
  }
  return { marque, valeurs, verifier }
}

/** Valeurs des paramètres de chemin pour une opération : celles du canari dont le préfixe correspond. */
export function parametresPour(op: Operation, c: Canaris): Record<string, string | number> | undefined {
  return c.valeurs.find((v) => v.motif.test(op.chemin))?.parametres
}
