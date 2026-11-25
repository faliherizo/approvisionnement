/**
 * Étapes des scénarios e2e en langage d'analyste (documentation et sous-titres des vidéos) et en langage parlé (narration).
 */

// --- Étapes en langage d'analyste -----------------------------------------------------------------------------------
export const liste = (v) => [v].flat().map((x) => `« ${x} »`).join(', ')
export const paires = (o) => Object.entries(o).map(([k, v]) => `« ${k} » : ${v === '' ? '(vide)' : `\`${v}\``}`).join(' ; ')
export function phrase(e) {
  if ('aller' in e) return `Ouvrir l'écran \`${e.aller}\``
  if ('connecter' in e) return typeof e.connecter === 'string' ? `Se connecter en tant que ${e.connecter}` : `Se connecter avec le compte \`${e.connecter.email}\``
  if ('lire' in e) return `Relever le texte affiché (\`${e.lire.selecteur}\`) sous le nom \`${e.lire.variable}\``
  if ('remplirTotp' in e) return `Saisir le code TOTP courant dans ${Object.keys(e.remplirTotp).map((k) => `« ${k} »`).join(', ')}`
  if ('largeur' in e) return `Afficher l'écran à ${e.largeur} px de large${e.largeur < 600 ? ' (téléphone)' : ''}`
  if ('langue' in e) return `Passer l'interface en ${{ fr: 'français', en: 'anglais', mg: 'malgache' }[e.langue]}`
  if ('cliquer' in e) return typeof e.cliquer === 'string' ? `Activer « ${e.cliquer} »` : `Activer « ${e.cliquer.nom} » (${e.cliquer.role})`
  if ('dansLaLigne' in e) return `Dans la ligne « ${e.dansLaLigne.texte} », activer « ${e.dansLaLigne.cliquer} »`
  if ('remplir' in e) return `Saisir ${paires(e.remplir)}`
  if ('choisir' in e) return `Choisir ${paires(e.choisir)}`
  if ('options' in e) return Object.entries(e.options).map(([k, v]) => `Constater que « ${k} »${v.contient ? ` propose ${liste(v.contient)}` : ''}${v.contient && v.exclut ? ' et' : ''}${v.exclut ? ` ne propose plus ${liste(v.exclut)}` : ''}`).join(' ; ')
  if ('cocher' in e) return `Cocher « ${e.cocher} »`
  if ('decocher' in e) return `Décocher « ${e.decocher} »`
  if ('televerser' in e) return `Joindre « ${e.televerser.fichier.nom} » au champ « ${e.televerser.champ} »`
  if ('voir' in e) return `Voir ${liste(e.voir)}`
  if ('nePasVoir' in e) return `Ne pas voir ${liste(e.nePasVoir)}`
  if ('valeur' in e) return `Constater la valeur ${paires(e.valeur)}`
  if ('voirTitre' in e) return `Voir le titre « ${e.voirTitre} »`
  if ('url' in e) return `Arriver sur une adresse de la forme \`${e.url}\``
  if ('preparer' in e) return typeof e.preparer === 'string' ? `Préparer par l'API : cas \`${e.preparer}\``
    : `Préparer par l'API : ${e.preparer.description}`
  if ('telecharger' in e) return `Télécharger par « ${e.telecharger} » et obtenir un fichier non vide`
  if ('dansLaFenetre' in e) return `Dans la fenêtre ouverte, activer « ${e.dansLaFenetre} »`
  if ('dansLaSection' in e) return `Dans la section « ${e.dansLaSection.titre} », activer « ${e.dansLaSection.cliquer} »`
  if ('cliquerTexte' in e) return `Activer l'élément « ${e.cliquerTexte} » (ligne ou carte cliquable)`
  if ('tracer' in e) return 'Tracer une signature manuscrite sur le pavé de signature'
  if ('repondre' in e) return `Répondre « ${e.repondre} » à la prochaine saisie demandée par le navigateur`
  if ('ecranConforme' in e) return "Contrôler l'écran : WCAG 2.2 AA (axe-core), cibles ≥ 24 px, sans débordement ni superposition, à 390, 768, 1280 et 1680 px"
  return JSON.stringify(e)
}


// --- Narration : la phrase dite à voix haute avant chaque geste des vidéos de démonstration -------------------------
// Sans les valeurs générées pour l'exécution (${unique}…) ni les détails techniques : la voix dit ce que fait
// l'utilisateur ; les sous-titres reprennent la phrase complète, valeurs comprises.
const propre = (t) => String(t).replace(/\$\{[^}]+\}/g, '').replace(/\s+([,.;:»])/g, '$1').replace(/\s{2,}/g, ' ').trim()
const champs = (o) => Object.keys(o).map((k) => `« ${propre(k.split(' » ').pop().replace(/ #\d+$/, ''))} »`).join(', ')
const nom = (c) => propre(typeof c === 'string' ? c : c.nom).replace(/ — .*$/, '')
export function narration(e) {
  if ('aller' in e) return "On ouvre l'écran suivant."
  if ('connecter' in e) return typeof e.connecter === 'string' ? `On se connecte en tant que ${propre(e.connecter)}.` : 'On se connecte avec le compte créé.'
  if ('cliquer' in e) return `On active « ${nom(e.cliquer)} ».`
  if ('dansLaLigne' in e) return `Dans la ligne visée, on active « ${propre(e.dansLaLigne.cliquer)} ».`
  if ('dansLaSection' in e) return `On active « ${propre(e.dansLaSection.cliquer)} ».`
  if ('dansLaFenetre' in e) return `On confirme par « ${propre(e.dansLaFenetre)} ».`
  if ('cliquerTexte' in e) return 'On ouvre l’élément visé.'
  if ('remplir' in e) return `On saisit ${champs(e.remplir)}.`
  if ('choisir' in e) return `On choisit ${champs(e.choisir)}.`
  if ('options' in e) return `La liste ${champs(e.options)} propose les valeurs à jour.`
  if ('cocher' in e) return `On coche « ${propre(e.cocher).slice(0, 80)} ».`
  if ('decocher' in e) return `On décoche « ${propre(e.decocher)} ».`
  if ('televerser' in e) return `On joint un fichier au champ « ${propre(e.televerser.champ)} ».`
  if ('telecharger' in e) return `On télécharge par « ${nom(e.telecharger)} ».`
  if ('voir' in e) return 'L’écran confirme le résultat.'
  if ('nePasVoir' in e) return 'L’élément n’apparaît pas, comme attendu.'
  if ('valeur' in e) return 'La valeur affichée est la bonne.'
  if ('tracer' in e) return 'On trace sa signature.'
  if ('largeur' in e) return e.largeur < 600 ? 'La suite se fait sur téléphone.' : 'Retour à l’écran d’ordinateur.'
  if ('langue' in e) return e.langue === 'en' ? 'On passe l’interface en anglais.' : 'On revient au français.'
  if ('preparer' in e) return null
  return null
}
