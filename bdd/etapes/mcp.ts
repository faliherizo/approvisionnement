import { expect } from '@playwright/test'
import { Client } from '@modelcontextprotocol/sdk/client/index.js'
import { StreamableHTTPClientTransport } from '@modelcontextprotocol/sdk/client/streamableHttp.js'
import { When, Then } from './fixtures'

/**
 * IQ-A-09 (ADR-023) : le client MCP de référence (@modelcontextprotocol/sdk), une implémentation tierce, se connecte
 * au connecteur avec le jeton capturé par le cas. Il valide le protocole de bout en bout, comme le ferait un assistant.
 */
When('un assistant MCP de référence se connecte avec le jeton capturé', async ({ baseURL, monde }) => {
  const jeton = monde.variables.jeton as string
  expect(jeton, 'jeton capturé par le cas').toBeTruthy()
  const transport = new StreamableHTTPClientTransport(new URL('/api/clm/integration/mcp', baseURL), {
    requestInit: { headers: { Authorization: `Bearer ${jeton}` } },
  })
  const client = new Client({ name: 'bdd-assistant', version: '1.0.0' })
  try {
    await client.connect(transport)
    const outils = await client.listTools()
    const trousse = await client.callTool({ name: 'lire_trousse', arguments: { trousseId: monde.variables.trousse } })
    const inconnue = await client.callTool({ name: 'lire_trousse', arguments: { trousseId: 999999999 } })
    monde.variables.mcp = { serveur: client.getServerVersion(), outils: outils.tools, trousse, inconnue }
  } finally {
    await client.close()
  }
})

Then('l\'assistant voit des outils en lecture seule et lit la trousse de son client', async ({ monde }) => {
  const r = monde.variables.mcp as {
    serveur: { name: string }
    outils: { name: string; annotations?: { readOnlyHint?: boolean } }[]
    trousse: { isError?: boolean; structuredContent?: { referenceExterne?: string } }
    inconnue: { isError?: boolean }
  }
  expect(r.serveur.name).toBe('vina-clm')
  expect(r.outils.map((o) => o.name).sort()).toEqual(['lire_lot', 'lire_trousse', 'lister_modeles'])
  expect(r.outils.every((o) => o.annotations?.readOnlyHint === true), 'aucun outil d\'écriture').toBeTruthy()
  expect(r.trousse.isError).toBeFalsy()
  expect(r.trousse.structuredContent?.referenceExterne).toBe(`MCP-${monde.variables.UNIQUE}`)
  expect(r.inconnue.isError, 'une trousse qui n\'est pas à ce client reste introuvable').toBe(true)
})
