// Pilote un Chrome DÉDIÉ déjà lancé (avec --remote-debugging-port), via CDP, pour exécuter le
// parcours de publication Office 365. Ce Chrome utilise un profil COPIÉ de votre profil réel : la
// session O365 est donc réutilisée (cookies déchiffrés par le vrai Trousseau, car ce n'est pas
// Playwright qui lance Chrome). La VIDÉO est produite par capture d'écran (voir capturer-cdp.sh),
// pas par Playwright (non supporté en mode CDP).
//
// Usage (normalement appelé par capturer-cdp.sh) :
//   CDP_URL=http://localhost:9222 node tour/o365-capture-cdp.mjs

import { chromium } from '@playwright/test'
import { executerParcours } from './o365-parcours.mjs'

const ENV = process.env
const CDP_URL = ENV.CDP_URL || 'http://localhost:9222'
const opt = {
  appUrl: ENV.APP_URL || 'http://localhost:8082',
  adminEmail: ENV.ADMIN_EMAIL || 'admin@levis.qc.ca',
  adminPass: ENV.ADMIN_PASS || 'Passw0rd!',
  slow: Number(ENV.SLOW || '1.2'),
  o365: { user: ENV.O365_USER || '', pass: ENV.O365_PASS || '' },
  recStartMs: Number(ENV.REC_START_MS || '0') || Date.now(),
  timelineOut: ENV.TIMELINE_OUT || '',
  urls: {
    sharepoint: ENV.SP_URL || 'https://vinaitsolution.sharepoint.com/sites/Gestiondesapprovisionnements',
    onedrive: ENV.ONEDRIVE_URL || 'https://vinaitsolution-my.sharepoint.com/',
    teams: ENV.TEAMS_URL || 'https://teams.microsoft.com/',
    outlook: ENV.OUTLOOK_URL || 'https://outlook.office.com/mail/sentitems',
  },
  // Deep link vers le canal Teams : montre le dépôt DANS l'application Teams (onglet Partagé), pas SharePoint.
  teams: teamsLien(
    ENV.TEAMS_TEAM_ID || '13a0c0ec-2b04-42ae-8207-e7ccc251bf90',
    ENV.TEAMS_CHANNEL_RAW || '19:3x94ONGCnmUPy5tiLgWFT9QZLDLYeNeys-jL_2tVQ0s1@thread.tacv2',
    ENV.TEAMS_CHANNEL_NAME || 'General',
    ENV.MS_TENANT_ID || 'da960e23-cf10-4399-9002-f877a254ebc1',
  ),
}

function teamsLien(teamId, canalRaw, canalNom, tenantId) {
  return {
    lien: `https://teams.microsoft.com/l/channel/${encodeURIComponent(canalRaw)}/`
      + `${encodeURIComponent(canalNom)}?groupId=${teamId}&tenantId=${tenantId}`,
  }
}

const browser = await chromium.connectOverCDP(CDP_URL)
try {
  const ctx = browser.contexts()[0] || (await browser.newContext())
  // Page NEUVE : le profil copié peut restaurer d'anciens onglets ; pages()[0] ne serait pas la nôtre.
  const page = await ctx.newPage()
  // Efface UNIQUEMENT les cookies du domaine de l'app → login admin propre et déterministe.
  // La session O365 (SharePoint/Teams/Outlook, autres domaines) n'est pas touchée.
  try {
    const host = new URL(opt.appUrl).hostname
    await ctx.clearCookies({ domain: host })
    console.log(`ℹ Cookies de ${host} effacés (session O365 préservée).`)
  } catch (e) {
    console.warn(`⚠ Effacement cookies app ignoré : ${e.message}`)
  }
  // Force la langue FR : le profil copié peut avoir une locale sauvegardée (en/mg) qui changerait
  // tous les libellés et casserait les sélecteurs. L'init script s'exécute avant le rendu React.
  await page.addInitScript(() => { try { localStorage.setItem('locale', 'fr') } catch { /* privé */ } })
  await page.bringToFront()
  const docId = await executerParcours(page, opt)
  console.log(`✔ Parcours terminé (document ${docId}).`)
} catch (e) {
  console.error('✖ Erreur pendant le parcours :', e.message)
  process.exitCode = 1
} finally {
  await browser.close() // détache le CDP (ne ferme pas le Chrome dédié)
}
