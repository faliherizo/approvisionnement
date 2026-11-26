// Score de base CVSS 3.1 (FIRST, spécification v3.1, section 7.1) calculé à partir du vecteur : le rapport ne recopie
// aucun score à la main. Usage : scoreCvss('CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L') → { score: 5.3, cote: 'Moyenne' }.
const POIDS = {
  AV: { N: 0.85, A: 0.62, L: 0.55, P: 0.2 },
  AC: { L: 0.77, H: 0.44 },
  UI: { N: 0.85, R: 0.62 },
  CIA: { H: 0.56, L: 0.22, N: 0 },
}
const PR = { U: { N: 0.85, L: 0.62, H: 0.27 }, C: { N: 0.85, L: 0.68, H: 0.5 } }

/** Arrondi supérieur à une décimale, tel que défini à l'annexe A de la spécification (évite les erreurs de virgule flottante). */
export function arrondiSuperieur(x) {
  const entier = Math.round(x * 100000)
  return entier % 10000 === 0 ? entier / 100000 : (Math.floor(entier / 10000) + 1) / 10
}

export function cote(score) {
  if (score === 0) return 'Aucune'
  if (score < 4) return 'Faible'
  if (score < 7) return 'Moyenne'
  if (score < 9) return 'Élevée'
  return 'Critique'
}

export function scoreCvss(vecteur) {
  const m = Object.fromEntries(vecteur.replace(/^CVSS:3\.1\//, '').split('/').map((p) => p.split(':')))
  for (const k of ['AV', 'AC', 'PR', 'UI', 'S', 'C', 'I', 'A']) if (!m[k]) throw new Error(`Vecteur CVSS incomplet (${k}) : ${vecteur}`)
  const portee = m.S === 'C'
  const iss = 1 - (1 - POIDS.CIA[m.C]) * (1 - POIDS.CIA[m.I]) * (1 - POIDS.CIA[m.A])
  const impact = portee ? 7.52 * (iss - 0.029) - 3.25 * Math.pow(iss - 0.02, 15) : 6.42 * iss
  const exploitabilite = 8.22 * POIDS.AV[m.AV] * POIDS.AC[m.AC] * PR[m.S][m.PR] * POIDS.UI[m.UI]
  const score = impact <= 0 ? 0 : arrondiSuperieur(Math.min(portee ? 1.08 * (impact + exploitabilite) : impact + exploitabilite, 10))
  return { score, cote: cote(score) }
}
