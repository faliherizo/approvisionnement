import { expect, Page, Playwright } from '@playwright/test'
import AxeBuilder from '@axe-core/playwright'
import { comptes, organisations } from './donnees'
import { session } from './cas'

/** Gestes d'interface partagés par les étapes BDD et les scénarios e2e : connexion, organisation d'essai, contrôle d'écran. */

const AXE_NORMES = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']

/**
 * Largeurs de référence (téléphone, tablette, portable, grand écran) : un défaut de mise en page n'apparaît souvent qu'à
 * une seule d'entre elles.
 */
export const LARGEURS = [390, 768, 1280, 1680]

/** Couches qui recouvrent le contenu par conception, au premier plan et momentanément. */
const COUCHES = '.dropdown__panel, .toast-zone, [role=dialog], [role=menu], [role=listbox]'

export async function connecterUi(page: Page, profil: string) {
  const compte = comptes[profil]
  if (!compte) throw new Error(`Profil inconnu : « ${profil} »`)
  // Quitter d'abord la page en cours : privée de session, elle renverrait d'elle-même vers /login (expiration),
  // en concurrence avec la navigation de cette étape.
  await page.goto('about:blank')
  await page.context().clearCookies()
  await page.goto('/login')
  await page.getByLabel('Courriel').fill(compte.email)
  await page.getByLabel('Mot de passe').fill(compte.motDePasse)
  await page.getByRole('button', { name: 'Se connecter', exact: true }).click()
  await page.waitForURL((url) => !url.pathname.startsWith('/login'))
}

export async function preparerOrganisation(playwright: Playwright, baseURL: string, nom: string) {
  const o = organisations[nom]
  if (!o) throw new Error(`Organisation inconnue : « ${nom} »`)
  const sa = await session(playwright, baseURL, 'superadmin')
  const existantes = await (await sa.get('/api/superadmin/tenants')).json() as { id: number; slug: string }[]
  let t = existantes.find((x) => x.slug === o.slug)
  if (!t) {
    const r = await sa.post('/api/superadmin/tenants', { data: { slug: o.slug, titre: o.titre, sousTitre: 'Essais BDD' } })
    expect(r.ok(), `création de l'organisation ${o.slug} : ${r.status()} ${await r.text()}`).toBeTruthy()
    t = await r.json()
    const a = await sa.post(`/api/superadmin/tenants/${t!.id}/admin`, { data: o.admin })
    expect(a.ok(), `administrateur de ${o.slug} : ${a.status()} ${await a.text()}`).toBeTruthy()
  }
  for (const code of o.modules) {
    const r = await sa.put(`/api/superadmin/tenants/${t!.id}/modules/${code}/activer`)
    expect(r.ok(), `module ${code} : ${r.status()} ${await r.text()}`).toBeTruthy()
  }
}

/**
 * SGQRI 008 3.0 / WCAG 2.2 AA : axe-core sans violation, cibles ≥ 24 px, aucun débordement, superposition ni champ désaligné, à chacune des
 * largeurs de référence. La largeur courante est rétablie à la fin. Rend la liste des écarts (vide : conforme).
 */
export async function ecartsEcran(page: Page): Promise<string[]> {
  const initiale = page.viewportSize()
  const ecarts: string[] = []
  for (const largeur of LARGEURS) {
    await page.setViewportSize({ width: largeur, height: 900 })
    await page.waitForTimeout(150)
    const bilan = await page.evaluate((COUCHES) => {
      const visibles = (sel: string) => [...document.querySelectorAll<HTMLElement>(sel)].filter((e) => {
        const r = e.getBoundingClientRect()
        // Sous une modale ouverte, la page est inerte (ni focus ni clic) : seule la modale compte.
        return r.width > 0 && r.height > 0 && r.bottom > 0 && !e.closest('[inert]')
          && e.checkVisibility({ visibilityProperty: true, contentVisibilityAuto: true })
      })
      const interactifs = visibles('a[href], button, input:not([type=hidden]), select, textarea')
      const petits = interactifs.filter((e) => {
        const r = e.getBoundingClientRect()
        const enLigne = e.tagName === 'A' && getComputedStyle(e).display === 'inline'
        return !enLigne && (r.width < 24 || r.height < 24) && !(e as HTMLInputElement).type?.match(/checkbox|radio/)
      }).map((e) => e.outerHTML.slice(0, 80))
      const chevauchements: string[] = []
      // Partie réellement visible d'un élément : sa boîte rognée par chaque ancêtre qui masque son débordement (liste
      // défilante). Une ligne défilée hors d'une liste n'est pas « sous » le bouton qui suit la liste : elle est cachée.
      const boiteVisible = (e: HTMLElement) => {
        const r = e.getBoundingClientRect()
        let { left, top, right, bottom } = r
        for (let p = e.parentElement; p && p !== document.body; p = p.parentElement) {
          const st = getComputedStyle(p)
          if (st.overflowX === 'visible' && st.overflowY === 'visible') continue
          const c = p.getBoundingClientRect()
          left = Math.max(left, c.left); top = Math.max(top, c.top)
          right = Math.min(right, c.right); bottom = Math.min(bottom, c.bottom)
        }
        return { left, top, right, bottom }
      }
      // Balayage par ordonnée : seules les paires qui se recouvrent verticalement sont comparées (une page à des
      // centaines de lignes restait sinon quadratique, aux quatre largeurs).
      const boites = interactifs.map((e) => ({ e, r: boiteVisible(e) }))
        .filter(({ r }) => r.right - r.left > 0 && r.bottom - r.top > 0).sort((x, y) => x.r.top - y.r.top)
      for (let i = 0; i < boites.length; i++) {
        const { e: a, r: ra } = boites[i]
        for (let j = i + 1; j < boites.length && boites[j].r.top < ra.bottom - 2; j++) {
          const { e: b, r: rb } = boites[j]
          const l = Math.max(0, Math.min(ra.right, rb.right) - Math.max(ra.left, rb.left))
          const h = Math.max(0, Math.min(ra.bottom, rb.bottom) - Math.max(ra.top, rb.top))
          if (l <= 2 || h <= 2) continue
          if (a.contains(b) || b.contains(a) || a.closest('label')?.contains(b) || b.closest('label')?.contains(a)) continue
          // Couches superposées par conception (menu ouvert, zone des notifications, boîte de dialogue) : elles passent
          // au-dessus du contenu, qu'elles ne figent pas ; seuls leurs éléments entre eux doivent rester sans collision.
          if (a.closest(COUCHES) !== b.closest(COUCHES)) continue
          chevauchements.push(`${a.textContent?.trim().slice(0, 30)} / ${b.textContent?.trim().slice(0, 30)}`)
        }
      }
      // Alignement : deux champs voisins (même conteneur) posés sur la même rangée partagent leur bord inférieur. Un libellé
      // dont l'astérisque ou l'aide prenait sa propre ligne faisait descendre son champ sous celui du voisin.
      const SIMPLE = ':scope > input:not([type=checkbox]):not([type=radio]):not([type=file]):not([type=color]), :scope > select'
      const champs = visibles('label').map((l) => ({ l, c: l.querySelector<HTMLElement>(SIMPLE) })).filter((x) => x.c)
      const desalignes: string[] = []
      for (let i = 0; i < champs.length; i++) {
        for (let j = i + 1; j < champs.length; j++) {
          const a = champs[i], b = champs[j]
          if (a.l.parentElement !== b.l.parentElement) continue
          const ra = a.l.getBoundingClientRect(), rb = b.l.getBoundingClientRect()
          if (ra.top >= rb.bottom || rb.top >= ra.bottom) continue
          const ca = a.c!.getBoundingClientRect(), cb = b.c!.getBoundingClientRect()
          if (Math.abs(ca.height - cb.height) > 3 || Math.abs(ca.bottom - cb.bottom) <= 3) continue
          desalignes.push(`${a.l.textContent?.trim().slice(0, 25)} / ${b.l.textContent?.trim().slice(0, 25)}`)
        }
      }
      return { deborde: document.documentElement.scrollWidth > window.innerWidth + 1, petits, chevauchements, desalignes }
    }, COUCHES)
    if (bilan.deborde) ecarts.push(`${largeur} px : débordement horizontal`)
    if (bilan.petits.length) ecarts.push(`${largeur} px : cibles < 24 px ${JSON.stringify(bilan.petits)}`)
    if (bilan.chevauchements.length) ecarts.push(`${largeur} px : éléments superposés ${JSON.stringify(bilan.chevauchements)}`)
    if (bilan.desalignes.length) ecarts.push(`${largeur} px : champs désalignés ${JSON.stringify(bilan.desalignes)}`)
    const { violations } = await new AxeBuilder({ page }).withTags(AXE_NORMES).analyze()
    for (const v of violations) ecarts.push(`${largeur} px : axe ${v.id} (${v.nodes.length}) : ${v.nodes[0]?.target.join(' ')}`)
  }
  if (initiale) await page.setViewportSize(initiale)
  return ecarts
}
