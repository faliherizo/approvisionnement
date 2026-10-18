/** Modèles de données de test (contrats de données entre les fixtures JSON et les specs). */

export interface TestUser {
  email: string
  motDePasse: string
  /** Réponse à la question secrète (comptes MFA « question »). */
  reponseSecrete?: string
  /** Secret base32 (comptes MFA « TOTP »). */
  totpSecret?: string
}

export interface TestDocument {
  titre: string
  type: string
  mode: string
  valeur: string
  contexte: string
}

export interface NouveauTenant {
  titre: string
}
