import { test, expect } from '@playwright/test'
import { LoginPage } from '../pages'
import { users } from '../models/fixtures'
import { PDF, activerModules, auditerAxe, ciblesGrandPublic, connecterAdmin, controler } from './clm.outils'

/**
 * Vues CLM (IQ 2026-6370-0005, UI-01 à UI-24) dans le gabarit des maquettes, aux quatre largeurs de référence :
 * aucun débordement horizontal, aucun chevauchement, cibles ≥ 24 px, contrôles nommés, un seul landmark principal,
 * zéro violation axe-core (WCAG 2.2 A/AA). Ajouter un écran = ajouter une entrée à ECRANS.
 */
const LARGEURS = [390, 768, 1280, 1680]
const ECRANS: { nom: string; chemin: string; titre: RegExp; maquette?: string }[] = [
  { nom: 'UI-01 tableau de bord', chemin: '/clm', titre: /Tableau de bord des contrats/, maquette: 'UI-01' },
  { nom: 'UI-02 trousses', chemin: '/clm/trousses', titre: /Trousses/, maquette: 'UI-02' },
  { nom: 'UI-03 créer une trousse', chemin: '/clm/trousses/nouvelle', titre: /Créer une trousse/, maquette: 'UI-03' },
  { nom: 'UI-07 modèles', chemin: '/clm/modeles', titre: /Catalogue des modèles/, maquette: 'UI-07' },
  { nom: 'UI-12 tâches', chemin: '/clm/taches', titre: /Boîte de tâches/, maquette: 'UI-12' },
  { nom: 'UI-18 suivi', chemin: '/clm/suivi', titre: /Suivi centralisé/, maquette: 'UI-18' },
  { nom: 'UI-17 notifications', chemin: '/clm/admin/notifications', titre: /Notifications et rappels/, maquette: 'UI-17' },
  { nom: 'UI-21 intégrations', chemin: '/clm/admin/integrations', titre: /Intégrations API/, maquette: 'UI-21' },
  { nom: 'UI-22 paramètres', chemin: '/clm/admin/parametres', titre: /Paramètres et environnements/, maquette: 'UI-22' },
  { nom: 'listes de référence', chemin: '/clm/admin/listes', titre: /Listes de référence/ },
  { nom: 'UI-20 utilisateurs', chemin: '/clm/admin/utilisateurs', titre: /Utilisateurs/, maquette: 'UI-20' },
  { nom: 'UI-23 journal', chemin: '/clm/admin/journal', titre: /Journal d’audit et événements/, maquette: 'UI-23' },
  { nom: 'UI-24 options', chemin: '/clm/options', titre: /Options CLM et IA/, maquette: 'UI-24' },
  // Fonctions transverses servies dans l'espace du module (un tenant CLM seul n'a pas d'autre espace).
  { nom: 'UI-20 authentification unique', chemin: '/clm/admin/sso', titre: /Authentification unique \(SSO\)/, maquette: 'UI-20' },
  { nom: 'journal de sécurité', chemin: '/clm/admin/audit', titre: /Journal d’audit/ },
  { nom: 'mon profil', chemin: '/clm/profil', titre: /Mon profil/ },
  { nom: 'sécurité du compte', chemin: '/clm/securite', titre: /Sécurité du compte/ },
]

test.describe.configure({ timeout: 300_000 })

test.describe('Vues CLM — gabarit des maquettes, responsive, superposition, cibles, libellés et axe-core', () => {
  test.beforeAll(async ({ request }) => { await activerModules(request) })

  for (const largeur of LARGEURS) {
    test(`écrans CLM à ${largeur} px`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      await connecterAdmin(page)
      for (const e of ECRANS) {
        await test.step(e.nom, async () => {
          await page.goto(e.chemin)
          await expect(page.getByRole('heading', { level: 1, name: e.titre })).toBeVisible()
          if (e.maquette) await expect(page.locator(`[data-maquette="${e.maquette}"]`).first()).toBeAttached()
          await controler(page)
          await auditerAxe(page)
        })
      }
    })
  }

  test('navigation latérale repliée sur téléphone, dépliable au clavier', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await connecterAdmin(page)
    await page.goto('/clm')
    const menu = page.getByRole('button', { name: /Menu/ })
    const navigation = page.getByRole('navigation', { name: /Navigation Contrats & signatures/ })
    await expect(navigation).toBeHidden()
    await menu.focus()
    await page.keyboard.press('Enter')
    await expect(menu).toHaveAttribute('aria-expanded', 'true')
    await expect(navigation.getByRole('link', { name: 'Mes trousses' })).toBeVisible()
    await controler(page)
    await auditerAxe(page)
  })

  test('recherche globale du bandeau : renvoie à la liste filtrée des trousses', async ({ page }) => {
    await connecterAdmin(page)
    await page.goto('/clm')
    await page.getByRole('searchbox', { name: /Recherche globale des trousses/ }).fill('TR-')
    await page.getByRole('search', { name: /Recherche globale des trousses/ }).getByRole('button', { name: 'Rechercher' }).click()
    await expect(page).toHaveURL(/\/clm\/trousses\?q=TR-/)
    await expect(page.getByRole('heading', { level: 1, name: /Trousses/ })).toBeVisible()
  })

  for (const largeur of [390, 1280]) {
    test(`écrans avec données à ${largeur} px : assistant, détail et onglets, éditeur de modèle, décision`, async ({ page, playwright, baseURL }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      await connecterAdmin(page)
      const api = page.request
      const modele = await (await api.post('/api/clm/modeles', { data: {
        nom: `Modèle e2e ${largeur}`, corps: 'Montant : {{montant}}\n{{clauses}}', clauses: [], parcours: [],
        champs: [{ cle: 'montant', libelle: 'Montant', type: 'TEXTE', obligatoire: true, options: [], conditions: [],
          position: { page: 1, x: 10, y: 20, largeur: 30, hauteur: 6 } }],
      } })).json()
      const trousse = await (await api.post('/api/clm/trousses', { data: { titre: `Trousse e2e ${largeur}`, risque: 'MOYEN', unite: 'Financement' } })).json()
      const id = trousse.resume.id
      expect((await api.post(`/api/clm/trousses/${id}/documents`, { multipart: { fichier: { name: 'contrat.pdf', mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } } })).ok()).toBeTruthy()
      const participant = await (await api.post(`/api/clm/trousses/${id}/participants`, { data: { nom: 'Jeanne Signataire', courriel: 'jeanne@exemple.ca', telephone: '+15145550101', roleContrat: 'SIGNATAIRE_1', externe: true } })).json()
      expect((await api.put(`/api/clm/trousses/${id}/parcours`, { data: [
        { cle: 'v1', libelle: 'Validation juridique', phase: 1, type: 'VALIDATION', affectations: [{ type: 'UTILISATEUR', valeur: users.juriste.email }] },
        { cle: 's1', libelle: 'Signature', phase: 2, type: 'SIGNATURE', affectations: [{ type: 'PARTICIPANT', valeur: String(participant.id) }] },
      ] })).ok()).toBeTruthy()

      const verifier = async (nom: string) => test.step(nom, async () => { await controler(page); await auditerAxe(page) })

      for (let etape = 0; etape < 5; etape++) {
        await page.goto(`/clm/trousses/nouvelle?id=${id}&etape=${etape}`)
        await expect(page.getByRole('heading', { level: 1, name: /Modifier le brouillon/ })).toBeVisible()
        await expect(page.getByText(new RegExp(`Étape ${etape + 1} sur 5`))).toBeVisible()
        await verifier(`UI-03 étape ${etape + 1}`)
      }

      await page.goto(`/clm/trousses/${id}`)
      await expect(page.getByRole('heading', { level: 1, name: `Trousse e2e ${largeur}` })).toBeVisible()
      for (const onglet of [/^Résumé/, /^Documents/, /^Participants/, /^Parcours/, /^Historique/, /^Preuve/]) {
        await page.getByRole('tab', { name: onglet }).click()
        await expect(page.getByRole('tabpanel')).toBeVisible()
        await verifier(`UI-04 brouillon, onglet ${onglet}`)
      }
      await page.getByRole('tab', { name: /^Documents/ }).click()
      await page.getByRole('button', { name: /Prévisualiser — contrat.pdf/ }).click()
      await expect(page.locator('.clm-visionneuse')).toBeVisible()
      await verifier('UI-05 prévisualisation')
      await page.getByRole('button', { name: /^Versions — contrat\.pdf/ }).click()
      await page.getByRole('button', { name: /^Consulter les métadonnées — version 1/ }).click()
      await expect(page.getByRole('definition').filter({ hasText: /^[0-9a-f]{64}$/ })).toBeVisible()
      await verifier('UI-06 métadonnées d’une version')

      // Trousse issue d'un modèle publié et d'un dossier Salesforce : valeurs corrigées, zone de signature, règle d'étape.
      const redacteur = await playwright.request.newContext({ baseURL })
      expect((await redacteur.post('/api/auth/login', { data: { email: users.redacteur.email, motDePasse: users.redacteur.motDePasse } })).ok()).toBeTruthy()
      const publie = await (await redacteur.post('/api/clm/modeles', { data: {
        nom: `Modèle publié e2e ${largeur}`, corps: 'Entre IQ et {{demandeur}} : {{montant}} $.\n{{clauses}}', clauses: [], parcours: [],
        champs: [
          { cle: 'demandeur', libelle: 'Demandeur', type: 'TEXTE', obligatoire: true, source: 'Demandeur' },
          { cle: 'montant', libelle: 'Montant', type: 'TEXTE', obligatoire: true, source: 'Montant' },
          { cle: 'signature1', libelle: 'Signature', type: 'SIGNATURE', roleDestinataire: 'SIGNATAIRE_1', position: { page: 1, x: 10, y: 70, largeur: 40, hauteur: 10 } },
        ],
      } })).json()
      expect((await redacteur.post(`/api/clm/modeles/${publie.id}/soumettre`, { data: {} })).ok()).toBeTruthy()
      await redacteur.dispose()
      expect((await api.post(`/api/clm/modeles/${publie.id}/publier`, { data: {} })).ok()).toBeTruthy()
      const sf = (await (await api.post('/api/clm/trousses', { data: {
        titre: `Salesforce e2e ${largeur}`, risque: 'MOYEN', modeleId: publie.id, sfDossierId: 'DEM-2026-0001' } })).json()).resume
      expect((await api.post(`/api/clm/trousses/${sf.id}/participants`, { data: {
        nom: 'Marc Bénéficiaire', courriel: 'marc@exemple.ca', telephone: '+15145550102', roleContrat: 'SIGNATAIRE_2', externe: true } })).ok()).toBeTruthy()

      await page.goto(`/clm/trousses/nouvelle?id=${sf.id}&etape=0`)
      await page.getByRole('button', { name: 'Corriger la valeur — Montant' }).click()
      await expect(page.getByLabel('Nouvelle valeur — Montant')).toBeFocused()
      await verifier('UI-03/UI-10 correction d’une valeur ouverte')
      await page.getByLabel('Nouvelle valeur — Montant').fill('90000')
      await page.getByRole('button', { name: 'Enregistrer', exact: true }).click()
      await expect(page.getByText(/« Montant » : Salesforce indique 250000\.00, saisie manuelle 90000/)).toBeVisible()
      await verifier('UI-10 écart avec Salesforce tracé en conflit')

      await page.goto(`/clm/trousses/${sf.id}?onglet=participants`)
      await page.getByLabel('Signataire de la zone « Signature »').selectOption({ label: 'Marc Bénéficiaire · Deuxième signataire' })
      await page.getByRole('button', { name: 'Attribuer la zone — Signature' }).click()
      await expect(page.getByText('Zone « Signature » attribuée.')).toBeVisible()
      await verifier('UI-15 zone de signature attribuée')

      await page.getByRole('tab', { name: /^Parcours/ }).click()
      await page.getByRole('button', { name: 'Ajouter une étape' }).click()
      await page.getByLabel('Si le champ').selectOption('montant')
      await page.getByLabel('Opérateur').selectOption('SUP')
      await expect(page.getByText(/si Montant est supérieur à/).first()).toBeVisible()
      await verifier('UI-11 règle d’affaires d’une étape')

      // UI-22 : le bouton ouvre l'édition des paramètres de l'organisation ; UI-21 : il déplie la procédure de rotation.
      for (const [chemin, bouton, ouvert] of [
        ['/clm/admin/parametres', 'Modifier un paramètre autorisé', page.getByRole('form', { name: 'Paramètres du module de l’organisation' })],
        ['/clm/admin/integrations', 'Rotation du secret selon la procédure', page.getByRole('note')],
      ] as const) {
        await page.goto(chemin)
        await page.getByRole('button', { name: bouton }).click()
        await expect(ouvert).toBeVisible()
        await verifier(`${chemin} : ${bouton} — état ouvert`)
      }

      expect((await api.post(`/api/clm/trousses/${id}/lancer`, { data: {} })).ok()).toBeTruthy()
      await page.goto(`/clm/trousses/${id}?onglet=parcours`)
      await expect(page.locator('.clm-canvas')).toBeVisible()
      await verifier('UI-04/UI-11 en traitement')

      await page.goto(`/clm/modeles/${modele.id}`)
      await expect(page.getByRole('heading', { level: 1, name: new RegExp(`Modèle e2e ${largeur}`) })).toBeVisible()
      await page.getByRole('button', { name: 'Montant', exact: true }).click()
      await expect(page.locator('[data-maquette="UI-09"]').first()).toBeVisible()
      await verifier('UI-08/UI-09 éditeur, champ choisi')

      await page.context().clearCookies()
      await new LoginPage(page).connecter(users.juriste)
      await page.waitForURL((url) => !url.pathname.startsWith('/login'))
      await page.goto('/clm/taches')
      await page.getByRole('button', { name: new RegExp(`Traiter — ${trousse.resume.reference}`) }).click()
      await expect(page.getByRole('heading', { level: 1, name: /Validation — Validation juridique/ })).toBeVisible()
      await verifier('UI-13 validation documentaire')
    })
  }

  /**
   * Boîte de tâches, zone « Tâches assignées » : l'administrateur confie une tâche à la juriste (personne choisie dans
   * l'annuaire, priorité tirée de la liste de l'organisation) ; la juriste la démarre puis la termine. Chaque état —
   * formulaire ouvert, tâche confiée, réassignation ouverte, tâche terminée — est contrôlé au téléphone et au portable.
   */
  for (const largeur of [390, 1280]) {
    test(`tâches assignées à ${largeur} px : confier, réassigner, démarrer, terminer`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 900 })
      const verifier = async (nom: string) => test.step(nom, async () => { await controler(page); await auditerAxe(page) })
      const titre = `Relire l’annexe financière ${largeur}-${Date.now() % 100000}`
      await connecterAdmin(page)
      await page.goto('/clm/taches')
      const bloc = page.getByRole('region', { name: 'Tâches assignées' })
      await bloc.getByRole('button', { name: 'Nouvelle tâche' }).click()
      await expect(bloc.getByLabel('Tâche', { exact: true })).toBeFocused()
      await verifier('formulaire de création ouvert')

      await bloc.getByRole('button', { name: 'Confier la tâche' }).click()
      await expect(bloc.getByLabel('Tâche', { exact: true })).toBeFocused()
      await bloc.getByLabel('Tâche', { exact: true }).fill(titre)
      await bloc.getByLabel('Assignée à').selectOption(users.redacteur.email)
      await bloc.getByLabel('Priorité').selectOption('HAUTE')
      await bloc.getByLabel('Description').fill('Vérifier le montant de la garantie.')
      await bloc.getByRole('button', { name: 'Confier la tâche' }).click()
      await expect(bloc.getByText(`Tâche confiée à ${users.redacteur.email}.`)).toBeVisible()

      await bloc.getByRole('button', { name: 'Que j’ai confiées' }).click()
      const ligne = () => bloc.getByRole('row').filter({ hasText: titre })
      await expect(ligne()).toContainText('Haute')
      await verifier('tâche confiée, vue « que j’ai confiées »')

      await bloc.getByRole('button', { name: `Réassigner — ${titre}` }).click()
      await expect(bloc.getByLabel('Nouvelle personne assignée')).toBeFocused()
      await verifier('réassignation ouverte')
      await bloc.getByLabel('Nouvelle personne assignée').selectOption(users.juriste.email)
      await bloc.getByLabel('Motif').fill('Expertise juridique requise')
      await bloc.getByRole('button', { name: 'Confirmer la réassignation' }).click()
      await expect(bloc.getByText(`Tâche réassignée à ${users.juriste.email}.`)).toBeVisible()

      await page.context().clearCookies()
      await new LoginPage(page).connecter(users.juriste)
      await page.waitForURL((url) => !url.pathname.startsWith('/login'))
      await page.goto('/clm/taches')
      await bloc.getByRole('button', { name: `Démarrer — ${titre}` }).click()
      await expect(bloc.getByText(`« ${titre} » démarrée.`)).toBeVisible()
      await expect(ligne()).toContainText('En cours')
      await verifier('tâche démarrée par la personne assignée')
      await bloc.getByRole('button', { name: `Terminer — ${titre}` }).click()
      await expect(bloc.getByText(`« ${titre} » terminée.`)).toBeVisible()
      await bloc.getByRole('button', { name: 'Que j’ai terminées' }).click()
      await expect(ligne()).toContainText('Terminée')
      await verifier('tâche terminée')
    })
  }

  /**
   * RST-19 — parcours mobile du signataire, de l'invitation au reçu, sans interruption : chaque état du portail est
   * contrôlé (géométrie, libellés, cibles grand public de 44 px, axe-core) au téléphone puis à la tablette.
   */
  for (const largeur of [390, 768]) {
    test(`portail du signataire à ${largeur} px : vérification, refus, lecture, consentement, signature et reçu`, async ({ page }) => {
      await page.setViewportSize({ width: largeur, height: 844 })
      await connecterAdmin(page)
      const api = page.request
      const titre = `Signature mobile ${largeur}`
      const trousse = await (await api.post('/api/clm/trousses', { data: { titre, risque: 'FAIBLE' } })).json()
      const id = trousse.resume.id
      expect((await api.post(`/api/clm/trousses/${id}/documents`, { multipart: { fichier: { name: 'convention.pdf', mimeType: 'application/pdf', buffer: Buffer.from(PDF, 'latin1') } } })).ok()).toBeTruthy()
      const signataire = await (await api.post(`/api/clm/trousses/${id}/participants`, { data: {
        nom: 'Julie Juriste', courriel: users.juriste.email, roleContrat: 'SIGNATAIRE_1', externe: false,
        niveauIdentite: 'QUESTION', question: 'Ville du siège ?', reponse: 'Lévis',
      } })).json()
      expect((await api.put(`/api/clm/trousses/${id}/parcours`, { data: [
        { cle: 's1', libelle: 'Signature', phase: 1, type: 'SIGNATURE', affectations: [{ type: 'PARTICIPANT', valeur: String(signataire.id) }] },
      ] })).ok()).toBeTruthy()
      expect((await api.post(`/api/clm/trousses/${id}/lancer`, { data: {} })).ok()).toBeTruthy()
      const etape = (await (await api.get(`/api/clm/trousses/${id}`)).json()).etapes[0].id

      // Le signataire interne obtient son lien depuis son compte ; le portail lui-même ne dépend d'aucune session.
      expect((await api.post('/api/auth/login', { data: { email: users.juriste.email, motDePasse: users.juriste.motDePasse } })).ok()).toBeTruthy()
      const { lien } = await (await api.post(`/api/clm/etapes/${etape}/lien-signature`, { data: {} })).json()
      const jeton = /\/portail\/([A-Za-z0-9_-]+)/.exec(lien)![1]
      await page.context().clearCookies()

      const etat = (nom: string) => test.step(nom, async () => { await controler(page); await ciblesGrandPublic(page); await auditerAxe(page) })

      await page.goto(`/portail/${jeton}`)
      await expect(page.getByRole('heading', { level: 1, name: titre })).toBeVisible()
      const reponse = page.getByLabel('Ville du siège ?')
      await expect(reponse).toBeVisible()
      await etat('invitation et question de vérification')

      await page.getByRole('button', { name: /Refuser de signer/ }).click()
      await expect(page.getByLabel('Motif')).toBeVisible()
      await etat('formulaire de refus ouvert')
      await page.getByRole('button', { name: 'Annuler' }).click()

      await reponse.fill('Québec')
      await page.getByRole('button', { name: 'Vérifier' }).click()
      await expect(page.getByRole('alert')).toContainText(/\S/)
      await etat('réponse refusée annoncée')

      await reponse.fill('Lévis')
      await page.getByRole('button', { name: 'Vérifier' }).click()
      await expect(page.getByRole('heading', { level: 2, name: /Lire, consentir et signer/ })).toBeVisible()
      const signer = page.getByRole('button', { name: 'Signer', exact: true })
      await expect(signer).toBeDisabled()
      await etat('identité vérifiée, documents à lire')

      const telechargement = page.waitForEvent('download')
      await page.getByRole('button', { name: /Lire \/ télécharger/ }).click()
      expect((await telechargement).suggestedFilename()).toBe('convention.pdf')
      await expect(page.getByLabel('consulté')).toBeVisible()
      await etat('document consulté')

      await page.getByLabel('Votre nom complet').fill('Julie Juriste')
      await page.getByLabel(/je consens à les signer/).check()
      await expect(signer).toBeEnabled()
      await etat('consentement donné, prêt à signer')

      await signer.click()
      await expect(page.getByRole('heading', { level: 1, name: /Merci, votre signature est enregistrée/ })).toBeVisible()
      await expect(page.getByRole('heading', { level: 2, name: 'Reçu de signature' })).toBeVisible()
      await expect(page.getByText('Julie Juriste', { exact: true })).toBeVisible()
      await etat('reçu de signature')

      await page.reload()
      await expect(page.getByRole('heading', { level: 2, name: 'Reçu de signature' })).toBeVisible()
      await etat('reçu relu après rechargement')
    })
  }

  test('portail du signataire : lien invalide annoncé, mobile sans débordement', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/portail/jeton-inexistant-0123456789')
    await expect(page.getByRole('alert')).toContainText(/Invitation introuvable/)
    expect(await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth + 1)).toBeFalsy()
    expect(await page.locator('main').count()).toBe(1)
    await auditerAxe(page)
  })
})
