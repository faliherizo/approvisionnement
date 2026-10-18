// Captation vidéo de la PUBLICATION RÉELLE vers Microsoft 365.
//
// Réutilise votre session Office 365 en lançant Chrome avec VOTRE profil (persistent context),
// publie des documents de DÉMONSTRATION vers SharePoint / OneDrive / Teams / Outlook depuis
// l'application, puis ouvre chaque application Office 365 pour montrer le fichier réellement déposé.
// Tout est filmé en continu, lentement, dans un seul onglet (une seule vidéo .webm en sortie).
//
// PRÉ-REQUIS (obligatoires) :
//   1. Chrome COMPLÈTEMENT fermé (le profil est verrouillé s'il est ouvert).
//   2. Vous êtes déjà connecté à Office 365 dans le profil choisi.
//   3. L'application tourne (défaut http://localhost:8082) avec la config M365 réelle active.
//
// LANCEMENT :
//   cd e2e
//   CHROME_PROFILE="Default" node tour/o365-capture.mjs
//   (variables reconnues : CHROME_PROFILE, APP_URL, ADMIN_EMAIL, ADMIN_PASS, SLOW,
//    SP_URL, ONEDRIVE_URL, TEAMS_URL, OUTLOOK_URL, OUT_DIR)

import { chromium } from '@playwright/test'
import { homedir } from 'node:os'
import { resolve } from 'node:path'
import { mkdirSync, readdirSync, renameSync } from 'node:fs'

const ENV = process.env
const PROFILE = ENV.CHROME_PROFILE || 'Default'
const APP_URL = ENV.APP_URL || 'http://localhost:8082'
const ADMIN_EMAIL = ENV.ADMIN_EMAIL || 'admin@levis.qc.ca'
const ADMIN_PASS = ENV.ADMIN_PASS || 'Passw0rd!'
const SLOW = Number(ENV.SLOW || '1')
const OUT_DIR = resolve(ENV.OUT_DIR || './tour/o365-video')
const USER_DATA_DIR = resolve(homedir(), 'Library/Application Support/Google/Chrome')

// URLs Office 365 (défauts pour le tenant vinaitsolution ; surchargez au besoin).
const SP_URL = ENV.SP_URL || 'https://vinaitsolution.sharepoint.com/sites/Gestiondesapprovisionnements'
const ONEDRIVE_URL = ENV.ONEDRIVE_URL || 'https://vinaitsolution-my.sharepoint.com/'
const TEAMS_URL = ENV.TEAMS_URL || 'https://teams.microsoft.com/'
const OUTLOOK_URL = ENV.OUTLOOK_URL || 'https://outlook.office.com/mail/sentitems'

const DESTINATION = 'Demonstration/2027'
const CIBLES = ['SHAREPOINT', 'ONEDRIVE', 'TEAMS', 'OUTLOOK']

const pause = (page, ms = 4000) => page.waitForTimeout(Math.round(ms * SLOW))

async function main() {
  mkdirSync(OUT_DIR, { recursive: true })
  console.log(`▶ Profil Chrome : « ${PROFILE} »  |  app : ${APP_URL}  |  ralenti ×${SLOW}`)

  const ctx = await chromium.launchPersistentContext(USER_DATA_DIR, {
    channel: 'chrome',
    headless: false,
    viewport: { width: 1280, height: 800 },
    recordVideo: { dir: OUT_DIR, size: { width: 1280, height: 800 } },
    args: [`--profile-directory=${PROFILE}`],
  })

  const page = ctx.pages()[0] || (await ctx.newPage())
  try {
    await connexionApp(page)
    const docId = await creerEtValiderDocument(page)
    await publierToutesCibles(page)
    await montrerOffice365(page)
    console.log(`✔ Parcours terminé (document ${docId}).`)
  } catch (e) {
    console.error('✖ Erreur pendant la captation :', e.message)
  } finally {
    await pause(page, 2000)
    await ctx.close() // finalise la vidéo .webm
    renommerVideo()
  }
}

/** Connexion à l'application (formulaire local, indépendant de la session O365). */
async function connexionApp(page) {
  await page.goto(`${APP_URL}/login`, { waitUntil: 'domcontentloaded' })
  await pause(page, 2500)
  await page.getByLabel('Courriel').fill(ADMIN_EMAIL)
  await page.getByLabel('Mot de passe').fill(ADMIN_PASS)
  await pause(page, 1500)
  await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
  await page.waitForURL(/\/(accueil|tableau|documents|dashboard)?$|\/$/, { timeout: 20000 }).catch(() => {})
  await pause(page, 3000)
}

/** Crée un document de démonstration, l'assemble et le fait approuver (publication ensuite offerte). */
async function creerEtValiderDocument(page) {
  await page.goto(`${APP_URL}/`, { waitUntil: 'domcontentloaded' })
  await pause(page, 2500)
  const titre = `Démonstration publication ${new Date().toISOString().slice(0, 16).replace('T', ' ')}`
  await page.getByLabel('Titre', { exact: true }).fill(titre)
  await page.getByLabel('Type de contrat').selectOption('APPEL_OFFRES_PUBLIC')
  await page.getByLabel('Mode de sollicitation').selectOption('APPEL_OFFRES_PUBLIC')
  await page.getByLabel('Valeur estimée ($)').first().fill('1500000')
  await page.locator('.rte-zone').fill('Document de démonstration — publication Microsoft 365.')
  await pause(page, 1500)
  await page.getByRole('button', { name: 'Créer le document' }).click()
  await page.waitForURL(/\/documents\/\d+$/, { timeout: 20000 })
  const docId = page.url().match(/\/documents\/(\d+)/)?.[1]
  await pause(page, 2500)

  await page.getByRole('button', { name: 'Assembler le devis' }).click()
  await pause(page, 3000)

  await page.getByRole('button', { name: "Démarrer l'approbation" }).click()
  await pause(page, 1500)
  const approuver = page.getByRole('button', { name: 'Approuver' })
  for (let i = 0; i < 8 && (await approuver.count()) > 0; i++) {
    await approuver.first().click()
    await page.waitForTimeout(900)
  }
  await page.locator('.badge.s-APPROUVE').first().waitFor({ timeout: 15000 }).catch(() => {})
  await pause(page, 2500)
  return docId
}

/** Publie vers chaque cible, lentement, et vérifie que le dépôt est RÉEL (URL https, pas sim://). */
async function publierToutesCibles(page) {
  await page.getByRole('heading', { name: 'Publication (Microsoft 365 / SAP)' }).scrollIntoViewIfNeeded()
  await pause(page, 2000)
  for (const cible of CIBLES) {
    const format = cible === 'ONEDRIVE' ? 'DOCX' : 'PDF'
    await page.getByLabel('Cible').selectOption(cible)
    await page.getByLabel('Format').selectOption(format)
    await page.getByLabel('Répertoire de destination').fill(DESTINATION)
    await pause(page, 1500)
    await page.getByRole('button', { name: 'Publier' }).click()
    // Attend une ligne d'historique mentionnant la cible.
    await page.getByRole('table', { name: 'Historique des publications' })
      .getByText(cible).first().waitFor({ timeout: 25000 }).catch(() => {})
    await pause(page, 3500)

    const emplacement = await derniereUrlHistorique(page)
    if (emplacement.startsWith('sim://')) {
      console.warn(`⚠ ${cible} : dépôt SIMULÉ (${emplacement}) — la config M365 réelle n'est pas active pour ce tenant.`)
    } else {
      console.log(`✔ ${cible} : dépôt réel → ${emplacement}`)
    }
  }
  await page.getByRole('table', { name: 'Historique des publications' }).scrollIntoViewIfNeeded()
  await pause(page, 4000) // on laisse voir l'historique complet
}

/** Lit la colonne « Emplacement » de la première ligne d'historique (la plus récente). */
async function derniereUrlHistorique(page) {
  const cellule = page.getByRole('table', { name: 'Historique des publications' })
    .locator('tbody tr').first().locator('td').last()
  return (await cellule.textContent().catch(() => '') || '').trim()
}

/** Ouvre chaque application Office 365 (session réelle) et s'attarde pour montrer le fichier déposé. */
async function montrerOffice365(page) {
  const etapes = [
    ['SharePoint — bibliothèque du site', SP_URL],
    ['OneDrive', ONEDRIVE_URL],
    ['Teams — onglet Fichiers du canal', TEAMS_URL],
    ['Outlook — éléments envoyés', OUTLOOK_URL],
  ]
  for (const [libelle, url] of etapes) {
    console.log(`→ ${libelle} : ${url}`)
    try {
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 45000 })
      await page.waitForLoadState('networkidle', { timeout: 20000 }).catch(() => {})
      await pause(page, 9000) // temps de bien voir le fichier à l'écran
    } catch (e) {
      console.warn(`⚠ ${libelle} injoignable : ${e.message}`)
    }
  }
}

/** Renomme la vidéo produite en un nom stable. */
function renommerVideo() {
  const webm = readdirSync(OUT_DIR).filter((f) => f.endsWith('.webm')).sort()
  if (webm.length === 0) return console.warn('⚠ Aucune vidéo produite.')
  const src = resolve(OUT_DIR, webm[webm.length - 1])
  const dst = resolve(OUT_DIR, 'publication-o365.webm')
  renameSync(src, dst)
  console.log(`🎬 Vidéo : ${dst}`)
}

main()
