/**
 * Espaces de travail selon les modules souscrits par l'organisation. Un tenant qui ne souscrit que le module CLM (et ce qui
 * s'y intègre) vit entièrement dans l'espace CLM : aucun menu ni aucune page d'un autre module ne lui est présenté.
 */

/** Modules dont les écrans vivent dans l'espace CLM (sécurité avancée : groupes et unités de UI-20). */
const MODULES_ESPACE_CLM = new Set(['clm', 'clm_ia', 'securite_avancee'])

export function espaceClmSeul(modules) {
  const actifs = modules.filter((m) => m.actif).map((m) => m.code)
  return actifs.includes('clm') && actifs.every((code) => MODULES_ESPACE_CLM.has(code))
}

/** Pages transversales de la plateforme et leur équivalent dans l'espace CLM ; toute autre page mène à l'accueil CLM. */
const EQUIVALENTS_CLM = {
  '/profil': '/clm/profil',
  '/securite': '/clm/securite',
  '/utilisateurs': '/clm/admin/utilisateurs',
  '/audit': '/clm/admin/audit',
  '/parametres': '/clm/admin/sso',
}

export const equivalentClm = (chemin) => EQUIVALENTS_CLM[chemin] || '/clm'

/** Accueil d'une organisation qui ne souscrit pas le socle de rédaction : son premier espace, sinon son profil. */
const ACCUEILS = [['clm', '/clm'], ['saisie', '/mes-demandes'], ['formulaire', '/formulaires'], ['rh', '/rh/personnel'], ['paie', '/paie']]

export const accueilSansSocle = (actif) => ACCUEILS.find(([code]) => actif(code))?.[1] || '/profil'
