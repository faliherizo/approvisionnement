import { test, expect, Page } from '@playwright/test'
import { resolve } from 'node:path'
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import {
  LoginPage, DashboardPage, DocumentEditorPage, DossiersPage, RapportsPage, ChatIaPage,
  SearchPage, UsersPage, WorkflowsPage, AuditPage, DonneesIAPage, SuperadminPage, BasePage,
} from '../pages'
import { users, documents } from '../models/fixtures'

const MODELE_DOCX = resolve(__dirname, '../data/modele-demo.docx')
const LOGO_PNG = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAC0lEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==', 'base64')

test.setTimeout(15 * 60 * 1000)

type Narrer = (cle: string) => Promise<void>

/**
 * Démonstration de l'article 26 (captation vidéo), enrichie et narrée : cycle de rédaction complet
 * (création/adaptation de modèle, clauses conditionnelles, génération, validation, versions,
 * exportation), publication Microsoft 365, dossier d'approvisionnement et référence SAP, rapports,
 * assistant IA, recherche, interface multilingue, puis administration et superadministration.
 * La solution montrée est celle réellement proposée.
 */
test('démonstration article 26 (captation vidéo)', async ({ page }) => {
  const F = 2.5
  const pause = (ms = 3000) => page.waitForTimeout(Math.round(ms * F))
  const T0 = Date.now()
  const timeline: { t: number; key: string }[] = []
  const dpath = resolve(__dirname, 'narration-durations.json')
  const durees: Record<string, number> = existsSync(dpath) ? JSON.parse(readFileSync(dpath, 'utf8')) : {}
  const narrer: Narrer = async (cle) => {
    timeline.push({ t: Date.now() - T0, key: cle })
    await page.waitForTimeout(Math.round((durees[cle] ?? 3) * 1000) + 400)
  }

  // Chaque chapitre est indépendant : un aléa d'un écran n'interrompt pas la démonstration.
  for (const [nom, chap] of [
    ['rédacteur', chapitreRedacteur], ['administration', chapitreAdministration], ['superadmin', chapitreSuperadmin],
  ] as const) {
    try { await chap(page, pause, narrer) } catch (e) { console.warn(`chapitre ${nom} interrompu : ${(e as Error).message}`) }
  }
  writeFileSync(resolve(__dirname, 'timeline-art26.json'), JSON.stringify(timeline, null, 2))
})

async function connexion(page: Page, cle: keyof typeof users): Promise<void> {
  await new LoginPage(page).connecter(users[cle])
  await expect(page.getByRole('button', { name: 'Déconnexion' })).toBeVisible()
}
async function changerDe(page: Page, cle: keyof typeof users): Promise<void> {
  const base = new (class extends BasePage {})(page)
  if (await page.getByRole('button', { name: 'Déconnexion' }).isVisible().catch(() => false)) {
    await base.seDeconnecter(); await expect(page).toHaveURL(/\/login$/)
  }
  await connexion(page, cle)
}

async function chapitreRedacteur(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await connexion(page, 'redacteur')
  const dashboard = new DashboardPage(page)
  await expect(dashboard.titre()).toBeVisible()
  await narrer('art26_intro')

  // Création et adaptation d'un modèle ; clauses conditionnelles assemblées selon le contexte.
  await narrer('red_creer')
  const titre = `Démonstration — Réfection rue Guillaume-Couture ${Date.now()}`
  await dashboard.creerDocument({ ...documents.appelOffres, titre })
  const editeur = new DocumentEditorPage(page)
  await expect(page).toHaveURL(/\/documents\/\d+$/)
  await pause()
  await narrer('red_assembler')
  await editeur.assembler()
  await expect(page.getByText('Objet du contrat')).toBeVisible()
  await pause(3000)

  // Bordereau + annexe.
  await narrer('red_bordereau')
  await editeur.ajouterLigneBordereau('Réfection de chaussée', 1200, 85)
  await editeur.enregistrerBordereau()
  await expect(editeur.totalBordereau()).toBeVisible()
  await pause(2500)
  await editeur.ajouterAnnexe('Devis technique — plans et profils')
  await pause(2000)

  // Génération : aperçu fidèle.
  await narrer('red_apercu')
  await editeur.ouvrirApercuPdf()
  await expect(editeur.apercuPdf()).toBeVisible()
  await pause(4500)
  await page.keyboard.press('Escape')
  await pause(1000)

  // Validation + version.
  await narrer('red_approbation')
  await editeur.demarrerApprobation()
  await pause(1500)
  await editeur.approuverTout()
  await expect(editeur.badgeStatut('APPROUVE')).toBeVisible()
  await pause(2000)
  await narrer('art26_version')
  const capturer = page.getByRole('button', { name: /Capturer la version|Capturer version|version/i }).first()
  if (await capturer.isVisible().catch(() => false)) { await capturer.click(); await pause(2000) }

  // Exportation.
  await narrer('art26_export')
  await editeur.telechargerPdf().catch(() => null)
  await page.keyboard.press('Escape')
  await pause(1500)

  // La publication Microsoft 365 RÉELLE (SharePoint / OneDrive / Teams / Outlook) est démontrée avec
  // le vrai compte Office 365 dans le segment dédié, concaténé à la suite de cette vidéo.

  // Dossier d'approvisionnement + référence SAP.
  const dossiers = new DossiersPage(page)
  await narrer('red_dossier')
  await dossiers.naviguerVers('Dossiers')
  await expect(dossiers.titre()).toBeVisible()
  const titreDossier = `Acquisition solution ${Date.now()}`
  await dossiers.creerDossier({ titre: titreDossier, numeroAO: '7000017905', typeAcquisition: 'TECHNOLOGIES' })
  await expect(dossiers.detail()).toContainText(titreDossier)
  await pause(2500)
  await dossiers.rattacher(titre)
  await pause(2000)
  await narrer('red_sap')
  await dossiers.transmettreSap()
  await expect(dossiers.resultatSap()).toContainText('sap://')
  await pause(2500)

  // Rapports de suivi.
  await narrer('red_rapports')
  const rapports = new RapportsPage(page)
  await rapports.naviguerVers('Rapports de suivi')
  await expect(rapports.titre()).toBeVisible()
  await pause(2500)

  // Assistant IA.
  await narrer('red_ia')
  const chat = new ChatIaPage(page)
  await chat.naviguerVers('Assistant IA')
  await chat.envoyerMessage('Quelles clauses pour un appel d’offres public en TI ?')
  await pause(3000)

  // Recherche.
  await narrer('red_recherche')
  await page.goto('/recherche')
  await pause(1200)
  await new SearchPage(page).rechercher('Réfection')
  await pause(3000)

  // Interface multilingue (FR → EN → MGA → FR).
  await narrer('art26_multilingue')
  const sel = page.locator('.lang-switcher select')
  if (await sel.isVisible().catch(() => false)) {
    await sel.selectOption('en'); await pause(2200)
    await sel.selectOption('mg'); await pause(2200)
    await sel.selectOption('fr-CA'); await pause(1500)
  }
}

async function chapitreAdministration(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await changerDe(page, 'admin')
  const dashboard = new DashboardPage(page)
  await expect(dashboard.titre()).toBeVisible()
  await pause(1500)

  await narrer('adm_users')
  const utilisateurs = new UsersPage(page)
  await utilisateurs.naviguerVers('Utilisateurs')
  await expect(utilisateurs.titre()).toBeVisible()
  await utilisateurs.rechercher('redacteur')
  await pause(2500)

  await narrer('adm_workflows')
  const workflows = new WorkflowsPage(page)
  await workflows.naviguerVers('Circuits d’approbation')
  await expect(workflows.titre()).toBeVisible()
  await workflows.creer(`Circuit gré à gré ${Date.now()}`, 'GRE_A_GRE', ['APPRO', 'DIRECTION'])
  await pause(2500)

  await narrer('adm_audit')
  const audit = new AuditPage(page)
  await audit.naviguerVers('Journal d’audit')
  await expect(audit.titre()).toBeVisible()
  await audit.filtrer('CREER')
  await expect(audit.table()).toBeVisible()
  await pause(2500)

  await narrer('adm_donnees')
  const donnees = new DonneesIAPage(page)
  await donnees.naviguerVers('Données & IA')
  await expect(donnees.titre()).toBeVisible()
  await donnees.analyserWord(MODELE_DOCX)
  await expect(page.getByText(/section\(s\) détectée\(s\)/)).toBeVisible()
  await pause(2500)
}

async function chapitreSuperadmin(page: Page, pause: (ms?: number) => Promise<void>, narrer: Narrer): Promise<void> {
  await changerDe(page, 'superadmin')
  const sa = new SuperadminPage(page)
  await page.goto('/superadmin')
  await expect(sa.titre()).toBeVisible()
  await pause(1500)

  await narrer('sa_theme')
  await sa.televerserLogo(LOGO_PNG)
  await expect(sa.logoApercu()).toBeVisible()
  await sa.enregistrerTheme()
  await pause(2500)

  await narrer('sa_tarif')
  const panneauTarif = page.locator('.panel', { has: page.getByRole('heading', { name: /Tarification par tenant/ }) })
  await panneauTarif.scrollIntoViewIfNeeded()
  await panneauTarif.getByLabel('Tenant').selectOption({ index: 1 })
  await pause(1500)
  await panneauTarif.getByLabel(/Forfait annuel/).fill('58000')
  await panneauTarif.getByLabel(/Indexation/).fill('5')
  await pause(2500)
}
