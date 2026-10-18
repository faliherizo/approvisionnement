/**
 * Chargeur de fixtures : expose les données de test JSON de façon typée.
 * Source unique de vérité pour les jeux de données (principe DRY / SRP).
 */
import usersJson from '../data/users.json'
import documentsJson from '../data/documents.json'
import tenantsJson from '../data/tenants.json'
import sectionsJson from '../data/sections.json'
import documentTemplatesJson from '../data/templates.json'
import { TestUser, TestDocument, NouveauTenant } from './types'

export const users: Record<string, TestUser> = usersJson
export const documents: Record<string, TestDocument> = documentsJson
export const sections = sectionsJson
export const documentTemplates = documentTemplatesJson

export const tenants = {
  modeleTemplateAop: tenantsJson.modeleTemplateAop as string,
  nouveauTenant: tenantsJson.nouveauTenant as NouveauTenant,
}
