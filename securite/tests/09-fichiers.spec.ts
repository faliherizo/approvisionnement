import { expect, test } from '@playwright/test'
import { organisation } from '../outils/api'
import { consigner } from '../outils/consigner'

/** OWASP A04 / A05 — Téléversements : taille plafonnée, contenu actif refusé ou servi en pièce jointe, nom de fichier neutralisé. */
test.describe('INT-09 Fichiers téléversés', () => {
  test('un fichier trop volumineux est refusé par un 413', async () => {
    const a = await organisation('intrusion-fichiers')
    const r = await a.admin.post('/api/import/word', { failOnStatusCode: false,
      multipart: { fichier: { name: 'gros.docx', mimeType: 'application/octet-stream', buffer: Buffer.alloc(60 * 1024 * 1024, 1) } } })
    consigner('09-taille', { statut: r.status() })
    expect(r.status()).toBe(413)
  })

  test('un faux document Word (HTML ou script) est refusé', async () => {
    const a = await organisation('intrusion-fichiers')
    const r = await a.admin.post('/api/import/word', { failOnStatusCode: false,
      multipart: { fichier: { name: '../../evil.docx', mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', buffer: Buffer.from('<script>alert(1)</script>') } } })
    consigner('09-contenu', { statut: r.status(), corps: (await r.text()).slice(0, 200) })
    expect(r.status()).toBeGreaterThanOrEqual(400)
    expect(r.status()).toBeLessThan(500)
  })
})
