import { test, expect, Page, APIRequestContext, Playwright } from '@playwright/test'
import { readFileSync } from 'fs'
import { join } from 'path'
import { users } from '../models/fixtures'
import { TestUser } from '../models/types'
import donnees from '../data/clm-exigences.json'
import { PDF, auditerAxe, connecter, controler } from './clm.outils'

/**
 * Exigences fonctionnelles de l'AO IQ 2026-6370-0005 (F-01 à F-13, `02-Analyse/analyse_besoins_exigences.md`) vérifiées
 * depuis l'interface, dans le tenant Investissement Québec, avec les comptes de chaque profil. Les données viennent de
 * `data/clm-exigences.json` ; seule la mise en place que l'exigence ne vise pas passe par l'API.
 * Chaque test porte l'identifiant de l'exigence : la matrice `02-Analyse/validation_exigences_fonctionnelles.md` y renvoie.
 */
test.describe.configure({ timeout: 300_000 })

const DOCX = readFileSync(join(__dirname, '../data/modele-demo.docx'))
const unique = () => Date.now().toString(36)

async function session(page: Page, compte: TestUser) {
  await page.context().clearCookies()
  await connecter(page, compte)
  await expect(page).toHaveURL(/\/clm$/)
}

async function api(playwright: Playwright, baseURL: string | undefined, compte: TestUser) {
  const c = await playwright.request.newContext({ baseURL })
  expect((await c.post('/api/auth/login', { data: { email: compte.email, motDePasse: compte.motDePasse } })).ok()).toBeTruthy()
  return c
}

async function ok<T = any>(r: Promise<import('@playwright/test').APIResponse>): Promise<T> {
  const reponse = await r
  expect(reponse.ok(), `${reponse.url()} → ${reponse.status()} ${await reponse.text()}`).toBeTruthy()
  const texte = await reponse.text()
  return (texte ? JSON.parse(texte) : {}) as T
}

/** Modèle du jeu de données rédigé par l'initiateur, soumis, puis publié par l'administratrice d'affaires. */
async function modelePublie(playwright: Playwright, baseURL: string | undefined, nom: string) {
  const redacteur = await api(playwright, baseURL, users.iqInitiateur)
  const admin = await api(playwright, baseURL, users.iqAdminAffaires)
  const m = await ok(redacteur.post('/api/clm/modeles', { data: { ...donnees.modele, nom } }))
  await ok(redacteur.post(`/api/clm/modeles/${m.id}/soumettre`, { data: {} }))
  await ok(admin.post(`/api/clm/modeles/${m.id}/publier`, { data: {} }))
  await Promise.all([redacteur.dispose(), admin.dispose()])
  return m.id as number
}

async function deposerPdf(c: APIRequestContext, id: number, nom = 'convention.pdf') {
  return ok(c.post(`/api/clm/trousses/${id}/documents`, { multipart: { fichier: { name: nom, mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } } }))
}

test.describe('Exigences fonctionnelles IQ F-01 à F-13 — depuis l’interface', () => {
  test('F-01 — créer une trousse, y déposer un PDF et un Word, consulter état et historique', async ({ page }) => {
    await session(page, users.iqInitiateur)
    const titre = `${donnees.trousse.titre} ${unique()}`
    await page.getByRole('link', { name: /Créer une trousse|Nouvelle trousse/ }).first().click()
    await expect(page.getByRole('heading', { level: 1, name: 'Créer une trousse' })).toBeVisible()
    await page.getByRole('textbox', { name: 'Titre', exact: true }).fill(titre)
    await page.getByRole('combobox', { name: /^Unité responsable/ }).selectOption({ label: donnees.trousse.uniteLibelle })
    await page.getByLabel('Niveau de risque').selectOption(donnees.trousse.risque)
    await page.getByLabel('Échéance').fill(donnees.trousse.echeance)
    await page.getByRole('button', { name: 'Enregistrer le brouillon' }).click()
    await expect(page.getByText(/Brouillon .+ enregistré/).first()).toBeVisible()
    await expect(page).toHaveURL(/[?&]id=\d+/)
    const id = Number(new URL(page.url()).searchParams.get('id'))
    expect(id).toBeGreaterThan(0)

    await page.goto(`/clm/trousses/${id}?onglet=documents`)
    await expect(page.getByText(/jusqu’à 25 Mo/)).toBeVisible()
    const depot = page.getByLabel('Téléverser un document')
    await depot.setInputFiles({ name: 'convention.pdf', mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') })
    await expect(page.getByRole('button', { name: 'Prévisualiser — convention.pdf' })).toBeVisible()
    await depot.setInputFiles({ name: 'annexe.docx', mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', buffer: DOCX })
    await expect(page.getByRole('button', { name: 'Prévisualiser — annexe.docx' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)

    await page.getByRole('tab', { name: /^Résumé/ }).click()
    await expect(page.getByRole('main')).toContainText('Brouillon')
    await expect(page.getByRole('tabpanel')).toContainText(donnees.trousse.uniteLibelle)
    await page.getByRole('tab', { name: /^Historique/ }).click()
    await expect(page.getByRole('tabpanel')).toContainText('Trousse créée')
    await expect(page.getByRole('tabpanel')).toContainText(/annexe\.docx/)
  })

  test('F-02 — dossier Salesforce récupéré, contrat prérempli et généré depuis un modèle publié', async ({ page, playwright, baseURL }) => {
    const nomModele = `${donnees.modele.nom} F-02 ${unique()}`
    await modelePublie(playwright, baseURL, nomModele)
    await session(page, users.iqInitiateur)
    await page.goto('/clm/trousses/nouvelle')
    await page.getByLabel('Salesforce', { exact: true }).check()
    await page.getByLabel(/Identifiant de demande ou numéro de dossier Salesforce/).fill(donnees.salesforce.dossier)
    await page.getByRole('region', { name: /Recherche par identifiant/ }).getByRole('button', { name: 'Rechercher' }).click()
    await expect(page.getByText(`Dossier ${donnees.salesforce.dossier} trouvé.`)).toBeVisible()
    await expect(page.getByText(donnees.salesforce.demandeur).first()).toBeVisible()
    const choix = page.getByLabel('Modèle de contrat')
    await choix.selectOption(await choix.locator('option', { hasText: nomModele }).getAttribute('value'))
    await page.getByRole('textbox', { name: 'Titre', exact: true }).fill(`Convention ${donnees.salesforce.dossier} ${unique()}`)
    await page.getByRole('button', { name: 'Enregistrer le brouillon' }).click()
    await expect(page.getByText(/Brouillon .+ enregistré/).first()).toBeVisible()
    await expect(page).toHaveURL(/[?&]id=\d+/)
    const id = Number(new URL(page.url()).searchParams.get('id'))

    await expect(page.getByText(donnees.salesforce.montant).first()).toBeVisible()
    await page.goto(`/clm/trousses/${id}?onglet=documents`)
    await page.getByRole('button', { name: 'Générer depuis le modèle' }).click()
    await expect(page.getByText('Contrat généré.')).toBeVisible()
    await expect(page.getByRole('button', { name: /^Prévisualiser — / }).first()).toBeVisible()
    await page.getByRole('tab', { name: /^Preuve/ }).click()
    await expect(page.getByRole('tabpanel')).toContainText(donnees.salesforce.dossier)
    await controler(page)
    await auditerAxe(page)
  })

  test('F-03 — cycle de vie d’un modèle : soumis, publié, nouvelle version, retiré', async ({ page, playwright, baseURL }) => {
    const redacteur = await api(playwright, baseURL, users.iqInitiateur)
    const nom = `${donnees.modele.nom} F-03 ${unique()}`
    const m = await ok(redacteur.post('/api/clm/modeles', { data: { ...donnees.modele, nom, groupes: 'Gestion contractuelle' } }))
    await redacteur.dispose()

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/modeles/${m.id}`)
    await expect(page.getByRole('heading', { level: 1, name: new RegExp(nom.replace(/[()]/g, '.')) })).toBeVisible()
    await page.getByRole('button', { name: 'Signature du bénéficiaire', exact: true }).click()
    await expect(page.locator('.clm-zone--sig').first()).toBeVisible()
    await page.getByRole('button', { name: 'Soumettre à publication' }).click()
    await expect(page.getByText(/En révision/).first()).toBeVisible()

    await session(page, users.iqAdminAffaires)
    await page.goto('/clm/modeles')
    const ligne = page.getByRole('row').filter({ hasText: nom })
    await ligne.getByRole('button', { name: 'Publier' }).click()
    await expect(ligne).toContainText('Publié')
    await ligne.getByRole('button', { name: 'Nouvelle version' }).click()
    await expect(page.getByRole('row').filter({ hasText: nom }).filter({ hasText: 'Brouillon' })).toHaveCount(1)
    await page.getByRole('row').filter({ hasText: nom }).filter({ hasText: 'Publié' }).getByRole('button', { name: 'Retirer' }).click()
    await expect(page.getByRole('row').filter({ hasText: nom }).filter({ hasText: 'Retiré' })).toHaveCount(1)
    await controler(page)
    await auditerAxe(page)
  })

  test('F-04 — champs typés, format contrôlé, liste fermée et saisie assistée par les valeurs déjà retenues', async ({ page, playwright, baseURL }) => {
    const modele = await modelePublie(playwright, baseURL, `${donnees.modele.nom} F-04 ${unique()}`)
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    // Valeurs propres à l'exécution : les trousses des autres tests de l'initiateur proposent aussi les leurs.
    const marque = unique()
    const attendues = donnees.suggestions.attendues.map((v) => `${v} ${marque}`)
    for (const v of donnees.suggestions.valeursPrecedentes.map((x) => `${x} ${marque}`)) {
      const t = await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Antérieure ${v}`, modeleId: modele } }))
      await ok(initiateur.put(`/api/clm/trousses/${t.resume.id}/valeurs`, { data: { valeurs: { [donnees.suggestions.champ]: v } } }))
    }
    const t = await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Saisie assistée ${unique()}`, modeleId: modele } }))
    await initiateur.dispose()

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/nouvelle?id=${t.resume.id}&etape=0`)
    await page.getByRole('button', { name: 'Corriger la valeur — Demandeur' }).click()
    const saisie = page.getByLabel('Nouvelle valeur — Demandeur')
    await expect(saisie).toBeFocused()
    await expect(page.getByRole('status').filter({ hasText: /valeur\(s\) déjà utilisée\(s\) proposée\(s\)/ })).toBeVisible()
    const proposees = await page.locator(`datalist#${await saisie.getAttribute('list')} option`).evaluateAll((o) => o.map((x) => (x as HTMLOptionElement).value))
    expect(proposees.filter((v) => v.endsWith(marque)), 'la plus fréquente d’abord').toEqual(attendues)
    await controler(page)
    await auditerAxe(page)
    await saisie.fill(attendues[0])
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click()
    await expect(page.getByText(/Demandeur/).first()).toBeVisible()

    await page.getByRole('button', { name: 'Corriger la valeur — Montant' }).click()
    await expect(page.getByText(/format attendu/i).first()).toBeVisible()
    await page.getByLabel('Nouvelle valeur — Montant').fill('douze mille')
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click()
    await expect(page.getByRole('alert')).toContainText(/Montant/)

    await page.getByRole('button', { name: 'Corriger la valeur — Programme' }).click()
    const programme = page.getByLabel('Nouvelle valeur — Programme')
    await expect(programme.locator('option')).toHaveText(['—', 'ESSOR', 'IMPULSION'])
    await page.getByRole('button', { name: 'Corriger la valeur — Date de début' }).click()
    await expect(page.getByLabel('Nouvelle valeur — Date de début')).toHaveAttribute('type', 'date')
    await page.getByRole('button', { name: 'Corriger la valeur — Garantie exigée' }).click()
    await expect(page.getByLabel('Nouvelle valeur — Garantie exigée').locator('option')).toHaveText(['—', 'Oui', 'Non'])
  })

  test('F-05, F-06, F-08, F-09 — parcours mixte : commentaire, motif obligatoire, délégation, approbation, signature, preuve et suivi', async ({ page, playwright, baseURL }) => {
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Parcours mixte ${unique()}`, unite: donnees.trousse.unite, risque: 'MOYEN' } }))).resume
    await deposerPdf(initiateur, t.id)
    const signataire = await ok(initiateur.post(`/api/clm/trousses/${t.id}/participants`, { data: donnees.signataireInterne }))
    const parcours = donnees.parcoursMixte.map((e) => e.type === 'SIGNATURE'
      ? { ...e, affectations: [{ type: 'PARTICIPANT', valeur: String(signataire.id) }] } : e)
    await ok(initiateur.put(`/api/clm/trousses/${t.id}/parcours`, { data: parcours }))
    await initiateur.dispose()

    await test.step('F-05 : l’initiateur lance le parcours depuis la trousse', async () => {
      await session(page, users.iqInitiateur)
      await page.goto(`/clm/trousses/${t.id}`)
      page.once('dialog', (d) => d.accept())
      await page.getByRole('button', { name: 'Lancer le parcours' }).click()
      await expect(page.getByText('Parcours lancé.')).toBeVisible()
      await page.getByRole('tab', { name: /^Parcours/ }).click()
      await expect(page.getByRole('tabpanel')).toContainText('Validation juridique')
    })

    await test.step('F-05 : le validateur commente, ne peut retourner sans motif, puis valide', async () => {
      await session(page, users.iqValidateur)
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${t.reference} Validation juridique`) }).click()
      await page.getByLabel('Nouveau commentaire').fill(donnees.decision.commentaire)
      await page.getByRole('region', { name: 'Commentaires contextualisés' }).getByRole('button', { name: 'Ajouter un commentaire' }).click()
      await expect(page.getByText('Commentaire ajouté.')).toBeVisible()
      await expect(page.getByText(donnees.decision.commentaire)).toBeVisible()
      await page.getByRole('button', { name: 'Retourner pour correction' }).click()
      await expect(page.getByRole('alert')).toContainText('Un motif est obligatoire')
      await controler(page)
      await auditerAxe(page)
      await page.getByRole('button', { name: 'Valider', exact: true }).click()
      await expect(page.getByText('Décision enregistrée : approuvé.')).toBeVisible()
    })

    await test.step('F-05 : l’approbatrice délègue en cours de parcours ; la déléguée approuve', async () => {
      await session(page, users.iqApprobateur)
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${t.reference} Approbation financière`) }).click()
      await page.getByRole('button', { name: 'Déléguer si autorisé' }).click()
      await page.getByLabel('Courriel du délégataire').fill(donnees.decision.delegation)
      await page.getByRole('button', { name: 'Confirmer la délégation' }).click()
      await expect(page.getByText(`Tâche déléguée à ${donnees.decision.delegation}.`)).toBeVisible()

      await session(page, users.iqAdminAffaires)
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${t.reference} Approbation financière`) }).click()
      await page.getByRole('button', { name: 'Approuver', exact: true }).click()
      await expect(page.getByText('Décision enregistrée : approuvé.')).toBeVisible()
    })

    await test.step('F-06 : le signataire interne vérifie son identité, consent et signe', async () => {
      await session(page, users.iqSignataire)
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${t.reference} Signature de la direction`) }).click()
      // Le lien pointe vers l'adresse du portail configurée pour l'organisation ; le test le rejoue sur son propre serveur.
      await page.route('**/lien-signature', async (route) => {
        const reponse = await route.fetch()
        const corps = await reponse.json()
        const jeton = /\/portail\/([A-Za-z0-9_-]+)/.exec(corps.lien)![1]
        await route.fulfill({ response: reponse, json: { ...corps, lien: new URL(`/portail/${jeton}`, page.url()).toString() } })
      })
      await page.getByRole('button', { name: 'Signer maintenant' }).click()
      await expect(page).toHaveURL(/\/portail\//)
      await expect(page.getByText(donnees.organisation, { exact: true })).toBeVisible()
      await page.getByLabel(donnees.signataireInterne.question).fill(donnees.signataireInterne.reponse)
      await page.getByRole('button', { name: 'Vérifier' }).click()
      const signer = page.getByRole('button', { name: 'Signer', exact: true })
      await expect(signer).toBeDisabled()
      const lu = page.waitForEvent('download')
      await page.getByRole('button', { name: /Lire \/ télécharger/ }).click()
      await lu
      await page.getByLabel('Votre nom complet').fill(donnees.signataireInterne.nom)
      await page.getByLabel(/je consens à les signer/).check()
      await signer.click()
      await expect(page.getByRole('heading', { level: 2, name: 'Reçu de signature' })).toBeVisible()
    })

    await test.step('F-09 : dossier de preuve — intégrité, certificat et export complet', async () => {
      await session(page, users.iqInitiateur)
      await page.goto(`/clm/trousses/${t.id}?onglet=preuve`)
      await page.getByRole('button', { name: 'Vérifier l’intégrité' }).click()
      await expect(page.getByText(/Intégrité confirmée/)).toBeVisible()
      const certificat = page.waitForEvent('download')
      await page.getByRole('button', { name: 'Télécharger le certificat (PDF)' }).click()
      expect((await certificat).suggestedFilename()).toBe(`certificat-${t.reference}.pdf`)
      const archive = page.waitForEvent('download')
      await page.getByRole('button', { name: 'Exporter le dossier complet (ZIP)' }).click()
      expect((await archive).suggestedFilename()).toBe(`preuve-${t.reference}.zip`)
      await page.getByRole('tab', { name: /^Historique/ }).click()
      for (const evenement of ['Commentaire', 'Délégation', 'Identité vérifiée', 'Consentement exprimé', 'Signature apposée', 'Trousse terminée']) {
        await expect(page.getByRole('tabpanel')).toContainText(evenement)
      }
      await controler(page)
      await auditerAxe(page)
    })

    await test.step('F-08 : suivi centralisé filtré et exporté', async () => {
      await page.goto('/clm/suivi')
      const filtres = page.getByRole('main').getByRole('form')
      await filtres.getByLabel('Rechercher').fill(t.reference)
      await filtres.getByRole('button', { name: 'Rechercher', exact: true }).click()
      await expect(page.getByRole('row').filter({ hasText: t.reference })).toContainText('Terminée')
      const csv = page.waitForEvent('download')
      await page.getByRole('button', { name: 'Exporter (CSV)' }).first().click()
      expect((await csv).suggestedFilename()).toMatch(/\.csv$/)
      await filtres.getByLabel('Exceptions seulement').check()
      await filtres.getByRole('button', { name: 'Rechercher', exact: true }).click()
      await expect(page.getByRole('row').filter({ hasText: t.reference })).toHaveCount(0)
      await controler(page)
      await auditerAxe(page)
    })
  })

  test('F-07 — notifications : désactiver puis réactiver un message, préavis d’échéance réglé par l’organisation', async ({ page }) => {
    await session(page, users.iqAdminTi)
    await page.goto('/clm/admin/notifications')
    const nom = `${donnees.notification.evenement} ${donnees.notification.canal}`
    await page.getByRole('button', { name: `Désactiver — ${nom}` }).click()
    await expect(page.getByRole('button', { name: `Activer — ${nom}` })).toBeVisible()
    await page.getByRole('button', { name: `Activer — ${nom}` }).click()
    await expect(page.getByRole('button', { name: `Désactiver — ${nom}` })).toBeVisible()
    await page.getByRole('button', { name: `Modifier — ${nom}` }).click()
    await page.getByLabel('Sujet').fill(donnees.notification.sujet)
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click()
    await expect(page.getByRole('row').filter({ hasText: 'Échéance imminente' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)

    await page.goto('/clm/admin/parametres')
    await page.getByRole('button', { name: 'Modifier un paramètre autorisé' }).click()
    await page.getByLabel(/Préavis d’échéance/).fill(String(donnees.notification.preavisJours))
    await page.getByRole('form', { name: 'Paramètres du module de l’organisation' }).getByRole('button', { name: 'Enregistrer' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Paramètres enregistrés.' })).toBeVisible()
    await expect(page.getByText(`${donnees.notification.preavisJours} jour(s) avant l’échéance`)).toBeVisible()

    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: 'Modifier un paramètre autorisé' }).click()
    await page.getByRole('button', { name: 'Rétablir les paramètres de la plateforme' }).click()
    await expect(page.getByText('1 jour(s) avant l’échéance')).toBeVisible()
  })

  test('F-10 — connexion Salesforce testée et corrélation du dossier source', async ({ page }) => {
    await session(page, users.iqAdminTi)
    await page.goto('/clm/admin/integrations')
    await page.getByRole('button', { name: 'Tester la connexion' }).click()
    await expect(page.getByText('Connexion testée.')).toBeVisible()
    await controler(page)
    await auditerAxe(page)
  })

  test('F-11 — rôle attribué puis accès retiré, consignés au journal ; l’auditeur consulte sans intervenir', async ({ page, playwright, baseURL }) => {
    const admin = await api(playwright, baseURL, users.iqAdminTi)
    const email = `${unique()}.${donnees.compte.email}`
    const compte = await ok(admin.post('/api/admin/users', { data: { email, nom: donnees.compte.nom, roles: ['LECTEUR'], actif: true, motDePasse: 'Passw0rd!' } }))

    await session(page, users.iqAdminTi)
    await page.goto('/clm/admin/utilisateurs')
    await page.getByLabel('Courriel du compte').fill(email)
    await page.locator('label').filter({ hasText: /^Rôle/ }).locator('select').selectOption({ label: 'Auditeur / conformité' })
    await page.getByRole('button', { name: 'Attribuer le rôle' }).click()
    await expect(page.getByText(`Rôle « Auditeur / conformité » attribué à ${donnees.compte.nom}.`)).toBeVisible()
    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: 'Retirer l’accès' }).click()
    await expect(page.getByText(`Accès de ${donnees.compte.nom} retiré (compte désactivé).`)).toBeVisible()

    await page.goto('/clm/admin/audit')
    await expect(page.getByRole('cell', { name: 'COMPTE_ROLES_MODIFIES' }).first()).toBeVisible()
    await expect(page.getByRole('row').filter({ hasText: 'COMPTE_ROLES_MODIFIES' }).first()).toContainText('[LECTEUR] → [AUDITEUR, LECTEUR]')
    await expect(page.getByRole('cell', { name: 'COMPTE_DESACTIVE' }).first()).toBeVisible()
    await controler(page)
    await auditerAxe(page)
    await ok(admin.delete(`/api/admin/users/${compte.id}`))

    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Consultation auditeur ${unique()}` } }))).resume
    await Promise.all([initiateur.dispose(), admin.dispose()])
    await session(page, users.iqAuditeur)
    await page.goto(`/clm/trousses/${t.id}`)
    await expect(page.getByRole('heading', { level: 1, name: /Consultation auditeur/ })).toBeVisible()
    for (const action of ['Lancer le parcours', 'Annuler la trousse']) await expect(page.getByRole('button', { name: action })).toHaveCount(0)
    await page.goto('/clm/admin/journal')
    await expect(page.getByRole('heading', { level: 1, name: /Journal d’audit et événements/ })).toBeVisible()
    const export_ = page.waitForEvent('download')
    await page.getByRole('button', { name: 'Exporter le journal (CSV)' }).click()
    expect((await export_).suggestedFilename()).toMatch(/\.csv$/)
  })

  test('F-12 — interface bilingue, sans débordement ni violation (identité de l’organisation : portail, F-06)', async ({ page }) => {
    await session(page, users.iqInitiateur)
    await page.getByRole('combobox', { name: /Langue|Language/ }).selectOption('en')
    await expect(page.getByRole('heading', { level: 1, name: /Contracts dashboard|dashboard/i })).toBeVisible()
    await page.getByRole('combobox', { name: /Langue|Language/ }).selectOption('fr')
    await expect(page.getByRole('heading', { level: 1, name: 'Tableau de bord des contrats' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)
  })

  test('F-13 — analyse IA d’un document puis révision humaine obligatoire', async ({ page, playwright, baseURL }) => {
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Analyse IA ${unique()}` } }))).resume
    await deposerPdf(initiateur, t.id, 'garantie.pdf')
    await initiateur.dispose()

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=documents`)
    await page.getByRole('button', { name: 'Analyser (IA) — garantie.pdf' }).click()
    await expect(page.getByText('Analyse terminée : résultat à réviser.')).toBeVisible()
    await expect(page.getByText('À réviser').first()).toBeVisible()
    await page.getByLabel(/Justification/).first().fill('Classement vérifié par l’initiatrice.')
    await page.getByRole('button', { name: 'Valider le résultat' }).first().click()
    await expect(page.getByText(/Révisé par initiateur@iq\.example/)).toBeVisible()
    await controler(page)
    await auditerAxe(page)
  })
})

test.describe('Addenda 1 — intégrations et signature en lot', () => {
  const a = donnees.addenda1

  test('AF-47 — signature en lot depuis la plateforme : une trousse par dossier, chaque échec isolé', async ({ page, playwright, baseURL }) => {
    const redacteur = await api(playwright, baseURL, users.iqInitiateur)
    const admin = await api(playwright, baseURL, users.iqAdminAffaires)
    const nomModele = `${a.modeleLot.nom} ${unique()}`
    const m = await ok(redacteur.post('/api/clm/modeles', { data: { ...a.modeleLot, nom: nomModele } }))
    await ok(redacteur.post(`/api/clm/modeles/${m.id}/soumettre`, { data: {} }))
    await ok(admin.post(`/api/clm/modeles/${m.id}/publier`, { data: {} }))
    await Promise.all([redacteur.dispose(), admin.dispose()])

    await session(page, users.iqInitiateur)
    await page.getByRole('link', { name: 'Signature en lot' }).click()
    await expect(page.getByRole('heading', { level: 1, name: 'Signature en lot' })).toBeVisible()
    const libelle = `${a.lot.libelle} ${unique()}`
    const formulaire = page.getByRole('form', { name: 'Nouveau lot' })
    await formulaire.getByLabel('Libellé du lot').fill(libelle)
    const modele = formulaire.getByRole('combobox', { name: /^Modèle de contrat/ })
    await modele.selectOption(await modele.locator('option', { hasText: nomModele }).getAttribute('value'))
    await formulaire.getByLabel(/^Dossiers, un par ligne/).fill(a.lot.lignes.join('\n'))
    await expect(formulaire.getByRole('status').filter({ hasText: 'dossier(s) dans le lot.' })).toHaveText(`${a.lot.lignes.length} dossier(s) dans le lot.`)
    await controler(page)
    await auditerAxe(page)
    await formulaire.getByRole('button', { name: 'Créer le lot' }).click()

    await expect(page.getByText(`Lot « ${libelle} » : ${a.lot.reussis} trousse(s) créée(s), ${a.lot.echecs} échec(s).`)).toBeVisible()
    const detail = page.getByRole('table', { name: `Dossiers du lot « ${libelle} »` })
    await expect(detail.getByRole('row').filter({ hasText: 'Lancée' })).toHaveCount(a.lot.reussis)
    await expect(detail.getByRole('row').filter({ hasText: 'En échec' })).toContainText('Entente sans courriel valide')
    await controler(page)
    await auditerAxe(page)
    await detail.getByRole('link').first().click()
    await expect(page.getByRole('heading', { level: 1, name: 'Entente Agri-Nord' })).toBeVisible()
    await expect(page.getByRole('main')).toContainText('En traitement')
  })

  test('TI-22 — client Workday : secrets remis une fois, trousse créée par l’API, origine suivie, puis révocation', async ({ page, playwright, baseURL }) => {
    await session(page, users.iqAdminTi)
    await page.goto('/clm/admin/integrations')
    const formulaire = page.getByRole('form', { name: 'Nouveau client d’intégration' })
    const nom = `${a.workday.nom} ${unique()}`
    await formulaire.getByLabel('Nom', { exact: true }).fill(nom)
    await formulaire.getByLabel('Système').selectOption('WORKDAY')
    await formulaire.getByLabel(/^Adresse de rappel/).fill(a.workday.urlRappel)
    await controler(page)
    await auditerAxe(page)
    await formulaire.getByRole('button', { name: 'Créer le client' }).click()

    const remis = page.getByRole('status').filter({ hasText: 'ne seront plus jamais affichées' })
    await expect(remis).toBeVisible()
    const valeurs = await remis.locator('dd code').allTextContents()
    const [clientId, secret] = valeurs
    expect(clientId).toMatch(/^clm-/)
    await controler(page)
    await auditerAxe(page)
    await remis.getByRole('button', { name: 'J’ai copié les valeurs' }).click()
    await expect(page.getByText(secret)).toHaveCount(0)

    const systeme = await playwright.request.newContext({ baseURL })
    const jeton = await ok(systeme.post('/api/clm/integration/oauth/token', { form: { grant_type: 'client_credentials', client_id: clientId, client_secret: secret } }))
    const auth = { Authorization: `Bearer ${jeton.access_token}` }
    const t = await ok(systeme.post('/api/clm/integration/v1/trousses', { headers: auth, data: { ...a.workday.demande,
      documents: [{ nom: 'offre.pdf', contenuBase64: Buffer.from(PDF, 'latin1').toString('base64') }] } }))
    expect(t.source).toBe('WORKDAY')

    await page.goto('/clm/suivi')
    const filtres = page.getByRole('main').getByRole('form')
    await filtres.getByLabel('Source').selectOption('WORKDAY')
    await filtres.getByRole('button', { name: 'Rechercher', exact: true }).click()
    await expect(page.getByRole('row').filter({ hasText: t.reference })).toBeVisible()

    await page.goto('/clm/admin/integrations')
    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: `Révoquer — ${nom}` }).click()
    await expect(page.getByText(`Client « ${nom} » révoqué.`)).toBeVisible()
    expect((await systeme.get(`/api/clm/integration/v1/trousses/${t.id}`, { headers: auth })).status()).toBe(401)
    await systeme.dispose()
  })
})

test.describe('Conformité — écarts comblés (matrice conformite_exigences_fonctionnelles.md)', () => {
  const c = donnees.conformite

  test('§ 1.4.12 — groupe créé depuis une unité, membre ajouté puis retiré', async ({ page }) => {
    await session(page, users.iqAdminTi)
    await page.goto('/clm/admin/utilisateurs')
    await page.getByRole('link', { name: 'Créer un groupe' }).click()
    const formulaire = page.getByRole('form', { name: 'Nouveau groupe' })
    await formulaire.getByRole('combobox', { name: /^À partir d’une unité/ }).selectOption({ label: c.groupe.unite })
    await expect(formulaire.getByLabel(/^Code/)).toHaveValue(c.groupe.code)
    await controler(page)
    await auditerAxe(page)
    await formulaire.getByRole('button', { name: 'Créer le groupe' }).click()
    await expect(page.getByText(`Groupe ${c.groupe.code} créé.`)).toBeVisible()
    const ajout = page.getByRole('form', { name: `Ajouter un membre au groupe ${c.groupe.code}` })
    await ajout.getByRole('combobox', { name: /^Membre/ }).selectOption({ value: c.groupe.membre })
    await ajout.getByRole('button', { name: 'Ajouter' }).click()
    await expect(page.getByRole('list', { name: `Membres du groupe ${c.groupe.code}` })).toContainText(c.groupe.membre)
    await controler(page)
    await auditerAxe(page)
    await page.getByRole('button', { name: `Retirer ${c.groupe.membre} du groupe ${c.groupe.code}` }).click()
    await expect(page.getByText(`${c.groupe.membre} retiré.`)).toBeVisible()
    page.once('dialog', (d) => d.accept())
    await page.getByRole('button', { name: `Supprimer le groupe ${c.groupe.code}` }).click()
    await expect(page.getByText(`Groupe ${c.groupe.code} supprimé.`)).toBeVisible()
  })

  test('Scénario 9 — l’approbatrice retourne le document à l’étape de validation, sans brouillon', async ({ page, playwright, baseURL }) => {
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Retour à l’étape ${unique()}`, risque: 'FAIBLE' } }))).resume
    await deposerPdf(initiateur, t.id)
    await ok(initiateur.put(`/api/clm/trousses/${t.id}/parcours`, { data: c.retour.parcours }))
    await ok(initiateur.post(`/api/clm/trousses/${t.id}/lancer`, { data: {} }))
    const v1 = (await ok(initiateur.get(`/api/clm/trousses/${t.id}`))).etapes[0].id
    const validateur = await api(playwright, baseURL, users.iqValidateur)
    await ok(validateur.post(`/api/clm/etapes/${v1}/decider`, { data: { decision: 'APPROUVE' } }))

    await session(page, users.iqApprobateur)
    await page.goto('/clm/taches')
    await page.getByRole('button', { name: new RegExp(`Traiter — ${t.reference} Approbation financière`) }).click()
    await page.getByRole('textbox', { name: /^Commentaire/ }).fill(c.retour.motif)
    await page.getByRole('combobox', { name: /^Retourner à/ }).selectOption({ label: 'Validation juridique' })
    await controler(page)
    await auditerAxe(page)
    await page.getByRole('button', { name: 'Demander une correction' }).click()
    await expect.poll(async () => (await ok(initiateur.get(`/api/clm/trousses/${t.id}`))).etapes.map((e: { statut: string }) => e.statut))
      .toEqual(['ACTIVE', 'EN_ATTENTE'])
    expect((await ok(initiateur.get(`/api/clm/trousses/${t.id}`))).resume.statut).toBe('EN_TRAITEMENT')
    await Promise.all([initiateur.dispose(), validateur.dispose()])
    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=historique`)
    await expect(page.getByRole('tabpanel')).toContainText('Retour à une étape antérieure')
  })

  test('§ 1.4.7 / scénario 5 — signataire ajouté en cours de parcours ; une modification de document annonce un nouveau cycle', async ({ page, playwright, baseURL }) => {
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Ajout en cours ${unique()}`, risque: 'MOYEN' } }))).resume
    await deposerPdf(initiateur, t.id)
    const premier = await ok(initiateur.post(`/api/clm/trousses/${t.id}/participants`, { data: c.ajoutEnCours.premier }))
    await ok(initiateur.put(`/api/clm/trousses/${t.id}/parcours`, { data: [{ cle: 's1', libelle: 'Signature', phase: 1, type: 'SIGNATURE',
      affectations: [{ type: 'PARTICIPANT', valeur: String(premier.id) }] }] }))
    await ok(initiateur.post(`/api/clm/trousses/${t.id}/lancer`, { data: {} }))
    await initiateur.dispose()

    await session(page, users.iqInitiateur)
    await page.goto(`/clm/trousses/${t.id}?onglet=participants`)
    const n = c.ajoutEnCours.nouveau
    await page.getByLabel(/^Nom/).fill(n.nom)
    await page.getByLabel(/^Courriel/).fill(n.courriel)
    await page.getByLabel(/^Téléphone/).fill(n.telephone)
    await page.getByRole('combobox', { name: /^Rôle dans le contrat/ }).selectOption({ label: n.role })
    await page.getByRole('checkbox', { name: 'Externe' }).check()
    await page.locator('form').filter({ has: page.getByRole('heading', { name: 'Ajouter un signataire' }) })
      .getByRole('button', { name: 'Ajouter un signataire' }).click()
    await expect(page.getByRole('row').filter({ hasText: n.nom })).toBeVisible()
    await controler(page)
    await auditerAxe(page)
    await page.getByRole('tab', { name: /^Documents/ }).click()
    await expect(page.getByRole('note').filter({ hasText: 'ouvre un nouveau cycle' })).toBeVisible()
    await page.getByRole('tab', { name: /^Historique/ }).click()
    await expect(page.getByRole('tabpanel')).toContainText('ajouté en cours de parcours')
  })

  test('§ 1.4.11 / § 1.4.12 — calendrier de conservation, échéance d’une trousse close, gel légal', async ({ page, playwright, baseURL }) => {
    const initiateur = await api(playwright, baseURL, users.iqInitiateur)
    const t = (await ok(initiateur.post('/api/clm/trousses', { data: { titre: `Conservation ${unique()}` } }))).resume
    await ok(initiateur.post(`/api/clm/trousses/${t.id}/annuler`, { data: { motif: 'Abandon' } }))
    await initiateur.dispose()

    await session(page, users.iqAdminTi)
    await page.getByRole('link', { name: 'Conservation et destruction' }).click()
    const regle = page.getByRole('form', { name: 'Enregistrer la règle' })
    await regle.getByLabel(/^Durée après la clôture/).fill(String(c.conservation.dureeAns))
    await regle.getByLabel('Sort final').selectOption({ label: c.conservation.sort })
    await regle.getByRole('button', { name: 'Enregistrer la règle' }).click()
    await expect(page.getByText('Règle enregistrée.')).toBeVisible()
    const ligne = page.getByRole('row').filter({ hasText: t.reference })
    await expect(ligne).toContainText(String(new Date().getFullYear() + c.conservation.dureeAns))
    await expect(ligne).toContainText(c.conservation.sort)
    await controler(page)
    await auditerAxe(page)
    page.once('dialog', (d) => d.accept(c.conservation.motifGel))
    await ligne.getByRole('button', { name: `Geler — ${t.reference}` }).click()
    await expect(ligne).toContainText(`Gel légal : ${c.conservation.motifGel}`)
    await ligne.getByRole('button', { name: `Lever le gel — ${t.reference}` }).click()
    await expect(page.getByText(`Gel levé sur ${t.reference}.`)).toBeVisible()
    await expect(ligne.getByRole('button', { name: `Détruire — ${t.reference}` })).toHaveCount(0)
  })
})

