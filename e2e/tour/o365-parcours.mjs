// Parcours de démonstration « publication Office 365 », piloté via CDP, AVEC narration synchronisée.
// À chaque point clé, narrer(clé) enregistre l'horodatage réel (relatif au début de l'enregistrement
// écran) et attend la durée du clip audio → l'audio composé ensuite tombe pile sur l'image.

import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const ICI = dirname(fileURLToPath(import.meta.url))
const CIBLES = ['SHAREPOINT', 'ONEDRIVE', 'TEAMS', 'OUTLOOK']
const DESTINATION = 'Demonstration/2027'

function chargerDurees() {
  const f = resolve(ICI, 'narration-durations.json')
  return existsSync(f) ? JSON.parse(readFileSync(f, 'utf8')) : {}
}

export async function executerParcours(page, opt) {
  const { appUrl, adminEmail, adminPass, slow, urls, o365, teams, recStartMs, timelineOut } = opt
  const pause = (ms = 4000) => page.waitForTimeout(Math.round(ms * slow))
  const durees = chargerDurees()
  const t0 = recStartMs || Date.now()
  const timeline = []
  const narrer = async (cle) => {
    timeline.push({ t: Date.now() - t0, key: cle })
    await page.waitForTimeout(Math.round((durees[cle] || 3) * 1000) + 400)
  }

  await connexionApp(page, pause, { appUrl, adminEmail, adminPass })
  await narrer('o365_intro')
  const docId = await creerEtValiderDocument(page, pause, narrer, { appUrl })
  const emplacements = await publierToutesCibles(page, pause, narrer)
  await narrer('o365_pont') // comble le temps de chargement avant la vérification dans Microsoft 365
  await montrerOffice365(page, pause, narrer, emplacements, urls, o365, teams)

  if (timelineOut) {
    writeFileSync(timelineOut, JSON.stringify(timeline, null, 2))
    console.log(`🕒 Timeline O365 (${timeline.length} points) → ${timelineOut}`)
  }
  return docId
}

async function assurerO365(page, pause, o365) {
  if (!o365?.user || !o365?.pass) return
  try {
    const email = page.locator('input[type=email], input[name=loginfmt]')
    if (!(await email.count())) return
    await email.first().fill(o365.user)
    await pause(1200)
    await page.getByRole('button', { name: /Next|Suivant/i }).first().click().catch(() => {})
    await page.waitForTimeout(2500)
    const pwd = page.locator('input[type=password]')
    if (await pwd.count()) {
      await pwd.first().fill(o365.pass)
      await pause(1200)
      await page.getByRole('button', { name: /Sign in|Se connecter|Next|Suivant/i }).first().click().catch(() => {})
      await page.waitForTimeout(3000)
      await page.getByRole('button', { name: /^(Yes|Oui)$/i }).first().click().catch(() => {})
      await page.waitForTimeout(2500)
    }
  } catch { /* connexion best-effort */ }
}

async function connexionApp(page, pause, { appUrl, adminEmail, adminPass }) {
  await page.goto(`${appUrl}/login`, { waitUntil: 'domcontentloaded' })
  await page.waitForLoadState('networkidle', { timeout: 20000 }).catch(() => {})
  await pause(2500)
  const courriel = page.getByLabel('Courriel')
  try {
    await courriel.waitFor({ state: 'visible', timeout: 15000 })
  } catch {
    // Formulaire absent : session déjà active (rare, cookies effacés) → on poursuit sans reconnexion.
    console.log(`ℹ Aucun champ « Courriel » sur ${page.url()} — on continue sans reconnexion.`)
    return
  }
  await courriel.fill(adminEmail)
  await page.getByLabel('Mot de passe').fill(adminPass)
  await pause(1500)
  await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
  await page.waitForTimeout(3500)
}

async function creerEtValiderDocument(page, pause, narrer, { appUrl }) {
  await page.goto(`${appUrl}/`, { waitUntil: 'domcontentloaded' })
  await narrer('o365_creer')
  const titre = `Démonstration publication ${new Date().toISOString().slice(0, 16).replace('T', ' ')}`
  await page.getByLabel('Titre', { exact: true }).fill(titre)
  await page.getByLabel('Type de contrat').selectOption('APPEL_OFFRES_PUBLIC')
  await page.getByLabel('Mode de sollicitation').selectOption('APPEL_OFFRES_PUBLIC')
  await page.getByLabel('Valeur estimée ($)').first().fill('1500000')
  await page.locator('.rte-zone').fill('Document de démonstration — publication Microsoft 365.')
  await pause(1500)
  await page.getByRole('button', { name: 'Créer le document' }).click()
  await page.waitForURL(/\/documents\/\d+$/, { timeout: 20000 })
  const docId = page.url().match(/\/documents\/(\d+)/)?.[1]
  await pause(2000)
  await page.getByRole('button', { name: 'Assembler le devis' }).click()
  await pause(2500)
  await page.getByRole('button', { name: "Démarrer l'approbation" }).click()
  await pause(1500)
  const approuver = page.getByRole('button', { name: 'Approuver' })
  for (let i = 0; i < 8 && (await approuver.count()) > 0; i++) {
    await approuver.first().click()
    await page.waitForTimeout(900)
  }
  await page.locator('.badge.s-APPROUVE').first().waitFor({ timeout: 15000 }).catch(() => {})
  await pause(2000)
  return docId
}

async function publierToutesCibles(page, pause, narrer) {
  await page.getByRole('heading', { name: 'Publication (Microsoft 365 / SAP)' }).scrollIntoViewIfNeeded()
  await narrer('o365_publier')
  const narrCible = {
    SHAREPOINT: 'o365_pubSharepoint', ONEDRIVE: 'o365_pubOnedrive',
    TEAMS: 'o365_pubTeams', OUTLOOK: 'o365_pubOutlook',
  }
  const emplacements = {}
  for (const cible of CIBLES) {
    const format = cible === 'ONEDRIVE' ? 'DOCX' : 'PDF'
    await page.getByLabel('Cible').selectOption(cible)
    await page.getByLabel('Format').selectOption(format)
    await page.getByLabel('Répertoire de destination').fill(DESTINATION)
    await narrer(narrCible[cible]) // une phrase par publication → pas de silence pendant la boucle
    await page.getByRole('button', { name: 'Publier' }).click()
    await page.getByRole('table', { name: 'Historique des publications' })
      .getByText(cible).first().waitFor({ timeout: 25000 }).catch(() => {})
    await pause(1500)
    const emplacement = await derniereUrlHistorique(page)
    emplacements[cible] = emplacement
    console.log(emplacement.startsWith('sim://')
      ? `⚠ ${cible} : dépôt SIMULÉ (${emplacement})`
      : `✔ ${cible} : dépôt réel → ${emplacement}`)
  }
  await page.getByRole('table', { name: 'Historique des publications' }).scrollIntoViewIfNeeded()
  await pause(3000)
  return emplacements
}

async function derniereUrlHistorique(page) {
  const cellule = page.getByRole('table', { name: 'Historique des publications' })
    .locator('tbody tr').first().locator('td').last()
  return (await cellule.textContent().catch(() => '') || '').trim()
}

async function montrerOffice365(page, pause, narrer, emplacements, urls, o365, teams) {
  await visiter(page, pause, o365, 'SharePoint — dossier', dossierDe(emplacements.SHAREPOINT) || urls.sharepoint, narrer, 'o365_sharepoint')
  await visiter(page, pause, o365, 'SharePoint — fichier', urlFichier(emplacements.SHAREPOINT), narrer, 'o365_fichierSp')
  await visiter(page, pause, o365, 'OneDrive — fichier', urlFichier(emplacements.ONEDRIVE) || urls.onedrive, narrer, 'o365_onedrive')
  await montrerTeams(page, pause, o365, narrer, teams) // dans l'APP Teams (et non l'URL SharePoint) pour lever l'ambiguïté
  await visiter(page, pause, o365, 'Outlook — éléments envoyés', urls.outlook, narrer, 'o365_outlook')
}

/**
 * Ouvre le fichier déposé DANS l'application Microsoft Teams (deep link vers le canal), pour montrer
 * qu'il apparaît bien dans l'onglet des fichiers partagés du canal — et pas seulement dans SharePoint.
 * L'onglet fichiers s'appelle « Partagé » (Teams récent) ou « Fichiers »/« Files » (versions antérieures).
 */
async function montrerTeams(page, pause, o365, narrer, teams) {
  if (!teams?.lien) return
  console.log(`→ Teams (application) : ${teams.lien}`)
  try {
    await page.goto(teams.lien, { waitUntil: 'domcontentloaded', timeout: 60000 })
    await page.waitForTimeout(6000)
    await resterSurLeWeb(page)
    await assurerO365(page, pause, o365)
    await page.getByRole('heading', { name: /Général|General/ }).first().waitFor({ timeout: 20000 }).catch(() => {})
    await pause(4000)
    if (narrer) await narrer('o365_teams') // canal Général visible, chrome Teams autour
    const ongletFichiers = page.getByRole('tab', { name: /^Partagé$|^Fichiers$|^Files$|^Shared$/i })
      .or(page.getByRole('link', { name: /^Partagé$|^Fichiers$|^Files$|^Shared$/i }))
    if (await ongletFichiers.count().catch(() => 0)) await ongletFichiers.first().click().catch(() => {})
    await page.waitForTimeout(7000)
    for (const nom of ['Demonstration', '2027']) {
      const dossier = page.getByRole('button', { name: new RegExp(`^${nom}$`) })
        .or(page.getByRole('link', { name: new RegExp(`^${nom}$`) }))
        .or(page.getByText(new RegExp(`^${nom}$`)))
      if (await dossier.count().catch(() => 0)) {
        await dossier.first().dblclick().catch(() => dossier.first().click().catch(() => {}))
        await page.waitForTimeout(5000)
      }
    }
    await pause(5000)
    if (narrer) await narrer('o365_fichierTeams')
    await pause(4000)
  } catch (e) {
    console.warn(`⚠ Teams (application) injoignable : ${e.message}`)
  }
}

/** Écarte la bannière « ouvrir l'application de bureau » pour rester dans le navigateur. */
async function resterSurLeWeb(page) {
  for (const t of [/Utiliser l.application web/i, /Continuer dans ce navigateur/i, /Use the web app/i, /Continue on this browser/i]) {
    const el = page.getByRole('link', { name: t }).or(page.getByRole('button', { name: t }))
    if (await el.count().catch(() => 0)) { await el.first().click().catch(() => {}); break }
  }
}

async function visiter(page, pause, o365, libelle, url, narrer, cle) {
  if (!url) return
  console.log(`→ ${libelle} : ${url}`)
  try {
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 45000 })
    await page.waitForLoadState('networkidle', { timeout: 20000 }).catch(() => {})
    await assurerO365(page, pause, o365)
    if (narrer && cle) await narrer(cle)
    await pause(6000)
  } catch (e) {
    console.warn(`⚠ ${libelle} injoignable : ${e.message}`)
  }
}

function urlFichier(emplacement) {
  if (!emplacement || !emplacement.startsWith('http')) return null
  return emplacement
}

function dossierDe(emplacement) {
  if (!emplacement || !emplacement.startsWith('http') || emplacement.includes('Doc.aspx')) return null
  const i = emplacement.lastIndexOf('/')
  return i > 8 ? emplacement.slice(0, i) : null
}
