import api from './api'

/**
 * Slug du tenant déduit du sous-domaine (ex. « levis » pour levis.contrat.vinaitsolution.com).
 * Retourne null sur un domaine nu ou un sous-domaine réservé (www, app, contrat, accueil) :
 * l'application reste utilisable (le tenant réel provient du compte), avec le thème VINA par défaut.
 */
export function slugTenant() {
  const parts = window.location.hostname.split('.')
  const reserves = ['www', 'app', 'contrat', 'accueil', 'localhost']
  const sub = parts.length >= 3 && !reserves.includes(parts[0]) ? parts[0] : null
  return sub
}

/** Applique un thème (couleurs) via les variables CSS globales. */
export function appliquerTheme(theme) {
  if (!theme) return
  const s = document.documentElement.style
  if (theme.couleurPrimaire) { s.setProperty('--bleu', theme.couleurPrimaire); s.setProperty('--bleu-fonce', theme.couleurPrimaire) }
  if (theme.couleurAccent) s.setProperty('--accent', theme.couleurAccent)
  if (theme.couleurFond) s.setProperty('--bg', theme.couleurFond)
  if (theme.couleurTexte) s.setProperty('--texte', theme.couleurTexte)
}

/** Charge la vue publique du tenant (titre + thème) et applique le thème. */
export async function chargerTheme() {
  const slug = slugTenant()
  if (!slug) return null // domaine nu → thème VINA par défaut
  try {
    const { data } = await api.get(`/tenants/${slug}/public`)
    appliquerTheme(data.theme)
    return data
  } catch {
    return null
  }
}

/**
 * Adresse de connexion d'une organisation : son sous-domaine sous le domaine de la plateforme
 * (contrat.vinaitsolution.com → levis.contrat.vinaitsolution.com/login). Depuis un sous-domaine d'organisation, on
 * remonte d'abord au domaine de la plateforme. Le port est conservé (recette locale).
 */
export function adresseOrganisation(slug, lieu = window.location) {
  const hote = slugTenant() ? lieu.hostname.split('.').slice(1).join('.') : lieu.hostname
  const port = lieu.port ? `:${lieu.port}` : ''
  return `${lieu.protocol}//${encodeURIComponent(slug)}.${hote}${port}/login`
}
