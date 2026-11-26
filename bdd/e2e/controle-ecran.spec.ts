import { test, expect } from '@playwright/test'
import { ecartsEcran } from '../outils/ui'

/** Le contrôle d'écran lui-même : il relève une vraie superposition, et ignore une ligne défilée hors de sa liste. */
const page = (corps: string) => `<!doctype html><html lang="fr"><head><title>Contrôle</title></head><body><main>${corps}</main></body></html>`

test.describe("Contrôle d'écran (outils/ui.ts)", () => {
  test('relève deux boutons posés l’un sur l’autre', async ({ page: p }) => {
    await p.setContent(page(`<button style="position:absolute;top:10px;left:10px;width:120px;height:44px">Éditer</button>
      <button style="position:absolute;top:20px;left:20px;width:120px;height:44px">Supprimer</button>`))
    expect((await ecartsEcran(p)).join()).toContain('éléments superposés ["Éditer / Supprimer"]')
  })

  test('ignore une ligne masquée par le défilement de sa liste', async ({ page: p }) => {
    await p.setContent(page(`<div style="height:60px;overflow:auto"><button style="display:block;height:44px">Ligne 1</button><button style="display:block;height:44px">Ligne cachée</button></div>
      <button style="display:block;height:44px">Suivant</button>`))
    expect((await ecartsEcran(p)).filter((e) => e.includes('superposés'))).toEqual([])
  })

  test('relève deux champs d’une même rangée dont les bords ne s’alignent pas, accepte ceux qui s’alignent', async ({ page: p }) => {
    // Sans « appearance: none », Safari dessine la liste à sa hauteur native et ignore la sienne (styles.css fait de même).
    const grille = (style: string) => page(`<div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;width:600px">
      <label style="${style}">Libellé du lot<input style="height:40px;box-sizing:border-box"></label>
      <label style="${style}">Modèle de contrat <span>*</span><select style="height:40px;box-sizing:border-box;appearance:none"><option>—</option></select></label></div>`)
    await p.setContent(grille('display:flex;flex-direction:column'))
    expect((await ecartsEcran(p)).join()).toContain('champs désalignés')
    await p.setContent(grille('display:block'))
    expect((await ecartsEcran(p)).filter((e) => e.includes('désalignés'))).toEqual([])
  })
})
