// Dictionnaires de traduction. Source unique de vérité : `messages.json` — une entrée par clé
// → { fr, en, mg }. `fr-CA` et `fr` utilisent le texte `fr`. Les traductions EN sont fiables ; les MG
// sont générées automatiquement et À RELIRE par un locuteur. Convention de clé : `namespace.sousCle`.
// Interpolation : « {n} » dans le texte, remplacé via t('clé', { n: 3 }).

import messages from './messages.json'

export const LOCALES = [
  { code: 'fr-CA', label: 'Français (Canada)', drapeau: '🇨🇦' },
  { code: 'fr', label: 'Français', drapeau: '🇫🇷' },
  { code: 'en', label: 'English', drapeau: '🇬🇧' },
  { code: 'mg', label: 'Malagasy', drapeau: '🇲🇬' },
]

export { messages }

const construire = (lang) => {
  const dict = {}
  for (const [cle, valeurs] of Object.entries(messages)) dict[cle] = valeurs[lang] ?? valeurs.fr
  return dict
}

export const translations = {
  'fr-CA': construire('fr'),
  fr: construire('fr'),
  en: construire('en'),
  mg: construire('mg'),
}
