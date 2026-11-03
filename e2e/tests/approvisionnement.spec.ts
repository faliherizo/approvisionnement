import { test, expect, APIRequestContext } from '@playwright/test'
import { LoginPage } from '../pages'
import { users } from '../models/fixtures'
import fixture from '../data/appro-demande-achat.json'

/**
 * Parcours Approvisionnement de bout en bout, piloté par la fixture JSON `appro-demande-achat.json` :
 * conception (API) → publication → portail « Nouvelle demande » (UI) → remplissage/soumission (UI) →
 * « Mes demandes » (UI, section Soumises) → cycle de vie gestionnaire (API) → section Terminées.
 *
 * S'exécute contre une pile déjà démarrée (voir playwright.appro.config.ts, baseURL :8082) ou contre
 * le harnais par défaut (H2 :5173). Le code du formulaire est rendu unique par exécution pour éviter
 * les collisions dans une base persistante.
 */

const suffixe = Date.now().toString().slice(-6)
const code = `${fixture.form.code}_${suffixe}`
const nom = `${fixture.form.nom} ${suffixe}`

async function activerModules(api: APIRequestContext) {
  // Idempotent : active les modules requis pour le tenant 1 via le superadmin (tolère déjà actifs).
  const sa = await api.post('/api/auth/login', { data: users.superadmin })
  if (!sa.ok()) return
  for (const mod of ['formulaire', 'regle', 'saisie', 'rapport']) {
    await api.put(`/api/superadmin/tenants/1/modules/${mod}/activer`).catch(() => {})
  }
  await api.post('/api/auth/logout').catch(() => {})
}

test.describe('Approvisionnement — demande d\'achat (fixture JSON)', () => {
  let formulaireId: number
  let numero: string | null = null

  test('conception → publication → portail → soumission → cycle de vie', async ({ page, request }) => {
    await activerModules(request)

    // --- 1. Connexion concepteur/admin (UI) ; le contexte partage ensuite le cookie avec page.request.
    const login = new LoginPage(page)
    await login.connecter(users.admin)
    await expect(page.getByRole('button', { name: 'Déconnexion' })).toBeVisible()

    // --- 2. Conception depuis la fixture JSON (API) : créer, structurer, publier.
    const creation = await page.request.post('/api/formulaires', {
      data: { code, nom, domaine: fixture.form.domaine, parentId: null },
    })
    expect(creation.ok()).toBeTruthy()
    formulaireId = (await creation.json()).id

    const maj = await page.request.put(`/api/formulaires/${formulaireId}/structure`, {
      data: { structureJson: JSON.stringify(fixture.form.structure), note: 'e2e' },
    })
    expect(maj.ok()).toBeTruthy()
    expect((await page.request.post(`/api/formulaires/${formulaireId}/soumettre`)).ok()).toBeTruthy()
    expect((await page.request.post(`/api/formulaires/${formulaireId}/publier`)).ok()).toBeTruthy()

    // --- 3. Portail « Nouvelle demande » (UI) : le formulaire publié apparaît, filtrable par domaine.
    await page.goto('/nouvelle-demande')
    await expect(page.getByRole('heading', { name: 'Nouvelle demande' })).toBeVisible()
    await expect(page.getByRole('heading', { name: nom })).toBeVisible()

    // Accessibilité structurelle : chaque <select> du filtre porte un libellé.
    await expect(page.getByLabel('Domaine')).toBeVisible()

    await page.locator(`a.btn-lien[href$="/formulaires/${formulaireId}/remplir"]`).click()
    await expect(page).toHaveURL(new RegExp(`/formulaires/${formulaireId}/remplir`))

    // --- 4. Remplissage + soumission (UI), données de la fixture (jeu « sous seuil »).
    const r = fixture.reponsesSousSeuil
    await page.getByLabel(/Objet de la demande/).fill(r.objet)
    await page.getByLabel(/Montant estimé/).fill(r.montant)
    await page.getByLabel(/Justification/).fill(r.justification)
    await page.getByRole('button', { name: 'Soumettre' }).click()
    await expect(page.getByText('soumise avec succès')).toBeVisible()

    // --- 5. « Mes demandes » (UI) : la demande apparaît dans Soumises avec un numéro AP-….
    await page.goto('/mes-demandes')
    await expect(page.getByRole('heading', { name: 'Mes demandes' })).toBeVisible()
    const ligne = page.getByRole('row').filter({ hasText: nom }).first()
    await expect(ligne).toBeVisible()
    const texte = await ligne.innerText()
    const m = texte.match(/AP-\d{4}-\d{6}/)
    expect(m, 'un numéro AP-AAAA-NNNNNN doit être affiché').not.toBeNull()
    numero = m![0]

    // --- 6. Cycle de vie gestionnaire (API) : prise en charge → validation → clôture.
    const saisies = await (await page.request.get(`/api/formulaires/${formulaireId}/saisies`)).json()
    const saisieId = saisies[0].id
    expect((await page.request.post(`/api/formulaires/${formulaireId}/saisies/${saisieId}/prendre-en-charge`)).ok()).toBeTruthy()
    expect((await page.request.post(`/api/formulaires/${formulaireId}/saisies/${saisieId}/valider`)).ok()).toBeTruthy()
    expect((await page.request.post(`/api/formulaires/${formulaireId}/saisies/${saisieId}/cloturer`)).ok()).toBeTruthy()

    // --- 7. « Mes demandes » : la demande est maintenant Terminée (clôturée).
    await page.goto('/mes-demandes')
    const ligneFinale = page.getByRole('row').filter({ hasText: numero! }).first()
    await expect(ligneFinale).toBeVisible()
    await expect(ligneFinale.getByText('Clôturée')).toBeVisible()
  })
})
