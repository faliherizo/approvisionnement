import { test, expect, Page } from '@playwright/test'
import { resolve } from 'node:path'
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import {
  LoginPage, DashboardPage, DossiersPage, AssistantPage, DocumentEditorPage,
  ClausesPage, SearchPage, WorkflowsPage, AuditPage, DonneesIAPage, ChatIaPage,
  ProfilePage, RapportsPage, UsersPage, SuperadminPage, BasePage,
} from '../pages'
import { users, documents } from '../models/fixtures'

const MODELE_DOCX = resolve(__dirname, '../data/modele-demo.docx')

// PNG 1×1 valide, pour le téléversement de logo côté superadministrateur.
const LOGO_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==',
  'base64',
)

type Narrer = (cle: string) => Promise<void>

test.setTimeout(20 * 60 * 1000) // parcours long, filmé en continu, avec narration.

/**
 * Démonstration filmée, en continu et à vitesse normale, AVEC narration synchronisée et SANS texte
 * incrusté. Organisée par profil d'utilisateur : chaque profil se connecte et exécute les
 * fonctionnalités auxquelles son rôle donne réellement accès (RBAC respecté).
 *
 * Synchronisation narration : narrer(clé) enregistre l'horodatage réel (relatif au début du test)
 * et attend la durée du clip audio ; la timeline est écrite dans timeline-app.json, puis composer-audio.mjs
 * place chaque clip à son horodatage sur la piste finale.
 */
test('parcours complet par profil utilisateur (captation vidéo)', async ({ page }) => {
  const FACTEUR = 3.5
  const pause = (ms = 3500) => page.waitForTimeout(Math.round(ms * FACTEUR))

  const T0 = Date.now()
  const timeline: { t: number; key: string }[] = []
  const dpath = resolve(__dirname, 'narration-durations.json')
  const durees: Record<string, number> = existsSync(dpath) ? JSON.parse(readFileSync(dpath, 'utf8')) : {}
  const narrer: Narrer = async (cle) => {
    timeline.push({ t: Date.now() - T0, key: cle })
    await page.waitForTimeout(Math.round((durees[cle] ?? 3) * 1000) + 400)
  }

  await chapitreSuperadmin(page, pause, narrer)
  await chapitreRedacteur(page, pause, narrer)
  await chapitreJuriste(page, pause, narrer)
  await chapitreAdministrateur(page, pause, narrer)
  await chapitreVitrine(page, pause, narrer)

  writeFileSync(resolve(__dirname, 'timeline-app.json'), JSON.stringify(timeline, null, 2))
})

/** Connexion complète : saisit les identifiants puis attend l'état authentifié (shell chargé). */
async function connexion(page: Page, cle: keyof typeof users): Promise<void> {
  await new LoginPage(page).connecter(users[cle])
  await expect(page.getByRole('button', { name: 'Déconnexion' })).toBeVisible()
}

/** Bascule de profil : déconnexion propre puis reconnexion avec le nouvel utilisateur. */
async function changerDe(page: Page, cle: keyof typeof users): Promise<void> {
  const base = new (class extends BasePage {})(page)
  if (await page.getByRole('button', { name: 'Déconnexion' }).isVisible().catch(() => false)) {
    await base.seDeconnecter()
    await expect(page).toHaveURL(/\/login$/)
  }
  await connexion(page, cle)
}

/** Profil 1 — Superadministrateur (plateforme) : gestion complète des tenants, admins, thème, modèle et tarifs. */
async function chapitreSuperadmin(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await connexion(page, 'superadmin')
  const sa = new SuperadminPage(page)
  await page.goto('/superadmin')
  await expect(sa.titre()).toBeVisible()
  await narrer('sa_intro')

  // Vue d'ensemble : la liste des tenants (organisations) hébergés sur la plateforme multi-locataire.
  await page.getByRole('heading', { name: /Espace superadmin/ }).scrollIntoViewIfNeeded()
  await narrer('sa_liste')
  await pause(2500)

  // Création d'un nouveau tenant (nouvelle ville cliente) : slug = sous-domaine, titre de connexion.
  await narrer('sa_creer_tenant')
  const slug = `demo${Date.now().toString().slice(-6)}`
  await sa.creerTenant(slug, 'Ville de démonstration')
  await expect(sa.celluleSlug(slug)).toBeVisible()
  await pause(3500)

  // Provisionnement de l'administrateur du nouveau tenant (compte d'accès du client).
  await narrer('sa_admin')
  const sectionAdmin = page.locator('.panel', {
    has: page.getByRole('heading', { name: "Créer l'administrateur d'un tenant" }),
  })
  await sectionAdmin.scrollIntoViewIfNeeded()
  await sectionAdmin.getByLabel('Tenant').selectOption({ label: `Ville de démonstration (${slug})` })
  await sectionAdmin.getByLabel('Courriel').fill(`admin@${slug}.demo`)
  await sectionAdmin.getByLabel('Nom').fill('Administratrice de démonstration')
  await sectionAdmin.getByLabel('Mot de passe').fill('Passw0rd!')
  await pause(2500)
  await sectionAdmin.getByRole('button', { name: "Créer l'admin" }).click()
  await pause(3000)

  // Thème & design par tenant : logo et couleurs de marque appliqués à l'espace du client.
  await narrer('sa_theme')
  await sa.televerserLogo(LOGO_PNG)
  await expect(sa.logoApercu()).toBeVisible()
  const panneauTheme = page.locator('.panel', { has: page.getByRole('heading', { name: /Thème & design/ }) })
  await panneauTheme.getByLabel('Couleur primaire').fill('#0b4a8a')
  await panneauTheme.getByLabel("Couleur d'accent").fill('#c8102e')
  await pause(2500)
  await sa.enregistrerTheme()
  await pause(3000)

  // Modèle de document par tenant : police et marque des documents générés.
  await narrer('sa_modele')
  await sa.definirModeleTenant('TIMES', 'Ville de démonstration')
  await pause(3000)

  // Tarification d'abonnement par tenant (forfait annuel, indexation) — réservé au superadmin.
  const panneauTarif = page.locator('.panel', { has: page.getByRole('heading', { name: /Tarification par tenant/ }) })
  await panneauTarif.scrollIntoViewIfNeeded()
  await narrer('sa_tarif')
  // Le gestionnaire de tarifs charge sa liste au montage : on choisit un tenant existant (index 1).
  await panneauTarif.getByLabel('Tenant').selectOption({ index: 1 })
  await pause(2000)
  await panneauTarif.getByLabel(/Forfait annuel/).fill('58000')
  await panneauTarif.getByLabel(/Indexation/).fill('3')
  await pause(2500)
  await panneauTarif.getByRole('button', { name: /Enregistrer/ }).click()
  await pause(3500)
}

/** Profil 2 — Rédacteur : cycle de rédaction complet (création → dossier → assistant). */
async function chapitreRedacteur(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await changerDe(page, 'redacteur')
  const dashboard = new DashboardPage(page)
  await expect(dashboard.titre()).toBeVisible()
  await narrer('red_intro')

  // Création d'un document et assemblage automatique des clauses.
  await narrer('red_creer')
  const titreDoc = `Réfection rue Guillaume-Couture ${Date.now()}`
  await dashboard.creerDocument({ ...documents.appelOffres, titre: titreDoc })
  const editeur = new DocumentEditorPage(page)
  await expect(page).toHaveURL(/\/documents\/\d+$/)
  await pause()
  await narrer('red_assembler')
  await editeur.assembler()
  await expect(page.getByText('Objet du contrat')).toBeVisible()
  await pause(2500)

  // Aperçu du document généré : la première page du PDF s'affiche réellement à l'écran (image fidèle).
  await narrer('red_apercu')
  await editeur.ouvrirApercuPdf()
  await expect(editeur.apercuPdf()).toBeVisible()
  await pause(5000)                     // on laisse bien voir le contenu du document
  await page.keyboard.press('Escape')
  await pause(1000)

  // Bordereau de prix : ajout d'une ligne, enregistrement, total calculé.
  await narrer('red_bordereau')
  await editeur.ajouterLigneBordereau('Réfection de chaussée', 1200, 85)
  await editeur.enregistrerBordereau()
  await expect(editeur.totalBordereau()).toBeVisible()
  await pause(3000)

  // Annexe au contrat.
  await narrer('red_annexe')
  await editeur.ajouterAnnexe('Devis technique — plans et profils')
  await pause(2500)

  // Validation avant diffusion : le circuit d'approbation est complété, le document passe à « APPROUVE ».
  await narrer('red_approbation')
  await editeur.demarrerApprobation()
  await pause(1500)
  await editeur.approuverTout()
  await expect(editeur.badgeStatut('APPROUVE')).toBeVisible()
  await pause(2500)

  // La publication Microsoft 365 réelle (SharePoint / OneDrive / Teams / Outlook) est démontrée avec
  // le compte Office 365 de test dans le chapitre dédié, en fin de vidéo (dépôts et fichiers réels).

  // Dossier d'approvisionnement : création, rattachement, rapport, référence SAP.
  await narrer('red_dossier')
  const dossiers = new DossiersPage(page)
  await dossiers.naviguerVers('Dossiers')
  await expect(dossiers.titre()).toBeVisible()
  const titreDossier = `Acquisition solution rédaction ${Date.now()}`
  await dossiers.creerDossier({ titre: titreDossier, numeroAO: '7000017905', typeAcquisition: 'TECHNOLOGIES' })
  await expect(dossiers.detail()).toContainText(titreDossier)
  await pause(2500)
  await dossiers.rattacher(titreDoc)
  await expect(dossiers.rapport()).toContainText('1 document(s)')
  await pause(2000)
  await narrer('red_sap')
  await dossiers.transmettreSap()
  await expect(dossiers.resultatSap()).toContainText('sap://dossier/7000017905')
  await pause(3000)

  // Assistant guidé : analyse du besoin → recommandations (mode, modèles, clauses éligibles).
  await narrer('red_assistant')
  const assistant = new AssistantPage(page)
  await assistant.naviguerVers('Assistant guidé')
  await expect(assistant.titre()).toBeVisible()
  await assistant.analyser({ type: 'APPEL_OFFRES_PUBLIC', categorieAcquisition: 'TECHNOLOGIES', valeur: '300000', contexte: 'acquisition logicielle' })
  await expect(assistant.modeSuggere()).toContainText('APPEL_OFFRES_PUBLIC')
  await expect(assistant.clausesEligiblesTitre()).toBeVisible()
  await pause(3500)

  // Bibliothèque de clauses réutilisables.
  await narrer('red_clauses')
  const clauses = new ClausesPage(page)
  await clauses.naviguerVers('Bibliothèque')
  await expect(clauses.titre()).toBeVisible()
  await clauses.rechercher('GEN-001')
  await expect(clauses.clause('GEN-001')).toBeVisible()
  await pause(2800)

  // Recherche enrichie à facettes.
  await narrer('red_recherche')
  const recherche = new SearchPage(page)
  await recherche.naviguerVers('Recherche')
  await recherche.rechercher('contrat')
  await pause(3000)

  // Rapports de suivi consolidés (le rédacteur possède aussi le rôle d'approbateur).
  await narrer('red_rapports')
  const rapports = new RapportsPage(page)
  await rapports.naviguerVers('Rapports de suivi')
  await expect(rapports.titre()).toBeVisible()
  await expect(rapports.indicateurs()).toContainText('document(s)')
  await pause(3000)

  // Assistant IA (chat) — réponse en mode résilient.
  await narrer('red_ia')
  const chat = new ChatIaPage(page)
  await chat.naviguerVers('Assistant IA')
  await chat.envoyerMessage('Quelles clauses pour un appel d’offres public en TI ?')
  await pause(3500)

  // Profil personnel.
  const profil = new ProfilePage(page)
  await profil.naviguerVers('Profil')
  await expect(profil.titre()).toBeVisible()
  await pause(2200)
}

/** Profil 3 — Juriste : révision et gestion des clauses juridiques, recherche et assistant IA. */
async function chapitreJuriste(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await changerDe(page, 'juriste')
  await narrer('jur_intro')

  // Bibliothèque de clauses : création d'une clause juridique (droit du juriste).
  await narrer('jur_clause')
  const clauses = new ClausesPage(page)
  await clauses.naviguerVers('Bibliothèque')
  await expect(clauses.titre()).toBeVisible()
  await pause(2200)
  const code = `JUR-${Date.now().toString().slice(-6)}`
  await clauses.creerClause(code, 'Responsabilité et garanties', 'Le fournisseur garantit la conformité et assume sa responsabilité contractuelle.')
  await expect(clauses.clause(code)).toBeVisible()
  await pause(3000)

  // Recherche enrichie sur les contenus juridiques.
  await narrer('jur_recherche')
  const recherche = new SearchPage(page)
  await recherche.naviguerVers('Recherche')
  await recherche.rechercher('garantie')
  await pause(3000)

  // Assistant IA — question juridique.
  await narrer('jur_ia')
  const chat = new ChatIaPage(page)
  await chat.naviguerVers('Assistant IA')
  await chat.envoyerMessage('Quelles obligations légales pour la garantie dans un contrat public ?')
  await pause(3500)

  // Profil personnel.
  const profil = new ProfilePage(page)
  await profil.naviguerVers('Profil')
  await expect(profil.titre()).toBeVisible()
  await pause(2200)
}

/** Profil 4 — Administrateur (tenant) : administration, gouvernance et pilotage. */
async function chapitreAdministrateur(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await changerDe(page, 'admin')
  const dashboard = new DashboardPage(page)
  await expect(dashboard.titre()).toBeVisible()
  await narrer('adm_intro')

  // Gestion des utilisateurs (RBAC).
  await narrer('adm_users')
  const utilisateurs = new UsersPage(page)
  await utilisateurs.naviguerVers('Utilisateurs')
  await expect(utilisateurs.titre()).toBeVisible()
  await utilisateurs.rechercher('redacteur')
  await expect(utilisateurs.ligneUtilisateur('redacteur@levis.qc.ca')).toBeVisible()
  await pause(3000)

  // Circuits d'approbation configurables.
  await narrer('adm_workflows')
  const workflows = new WorkflowsPage(page)
  await workflows.naviguerVers('Circuits d’approbation')
  await expect(workflows.titre()).toBeVisible()
  await workflows.creer(`Circuit gré à gré ${Date.now()}`, 'GRE_A_GRE', ['APPRO', 'DIRECTION'])
  await pause(3000)

  // Journal d'audit consultable et filtrable.
  await narrer('adm_audit')
  const audit = new AuditPage(page)
  await audit.naviguerVers('Journal d’audit')
  await expect(audit.titre()).toBeVisible()
  await audit.filtrer('CREER')
  await expect(audit.table()).toBeVisible()
  await pause(3000)

  // Données & IA : migration d'essai CSV puis import d'un modèle Word (.docx). Réservé à l'admin.
  await narrer('adm_donnees')
  const donnees = new DonneesIAPage(page)
  await donnees.naviguerVers('Données & IA')
  await expect(donnees.titre()).toBeVisible()
  await donnees.migrationEssai('code,titre,categorie,corps\nDEMO-1,Objet du mandat,STANDARD,Le présent mandat...')
  await expect(donnees.rapport()).toContainText('1 valide')
  await pause(3000)
  await donnees.analyserWord(MODELE_DOCX)
  await expect(page.getByText(/section\(s\) détectée\(s\)/)).toBeVisible()
  await pause(3000)

  // Rapports de suivi consolidés (portefeuille + éléments d'action).
  await narrer('adm_rapports')
  const rapports = new RapportsPage(page)
  await rapports.naviguerVers('Rapports de suivi')
  await expect(rapports.titre()).toBeVisible()
  await expect(rapports.indicateurs()).toContainText('document(s)')
  await pause(3000)
}

/** Profil 5 — Visiteur public : le site vitrine, hors authentification. */
async function chapitreVitrine(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  const base = new (class extends BasePage {})(page)
  if (await page.getByRole('button', { name: 'Déconnexion' }).isVisible().catch(() => false)) {
    await base.seDeconnecter()
  }
  await page.goto('/accueil')
  await expect(page.getByRole('heading', { name: /rédaction des documents d’approvisionnement/i })).toBeVisible()
  await narrer('vitrine')
  await pause(4000)
}
