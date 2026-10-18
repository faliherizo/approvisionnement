// Textes de narration (français du Québec), dits par la voix macOS « Amélie » (fr_CA).
// Chaque clé correspond à un point du parcours ; les tests appellent narrer(clé) qui enregistre
// l'horodatage réel et attend la durée du clip → l'audio composé est synchronisé avec la vidéo.

export const APP = {
  sa_intro: "Bienvenue dans la démonstration de la solution VINA de rédaction des documents d'approvisionnement. Commençons par l'espace superadministrateur de la plateforme.",
  sa_liste: "Voici la liste des organisations, ou tenants, hébergées sur la plateforme multilocataire.",
  sa_creer_tenant: "Le superadministrateur crée une nouvelle organisation cliente en quelques secondes.",
  sa_admin: "Il provisionne ensuite le compte administrateur de cette organisation.",
  sa_theme: "Chaque organisation possède son thème: logo et couleurs de marque appliqués à son espace.",
  sa_modele: "On définit aussi le modèle des documents générés: police et identité visuelle.",
  sa_tarif: "Enfin, la tarification d'abonnement par organisation: forfait annuel et indexation.",

  red_intro: "Passons au profil rédacteur, qui prépare les documents contractuels.",
  red_creer: "Le rédacteur crée un nouveau document à partir du type de contrat et du mode de sollicitation.",
  red_assembler: "Les clauses normalisées sont assemblées automatiquement selon le contexte.",
  red_apercu: "L'aperçu du document généré s'affiche fidèlement à l'écran.",
  red_bordereau: "Le bordereau de prix se remplit ligne par ligne, avec calcul automatique du total.",
  red_annexe: "On ajoute les annexes techniques au contrat.",
  red_approbation: "Le document suit un circuit d'approbation avant toute diffusion.",
  red_dossier: "Le rédacteur regroupe les documents dans un dossier d'approvisionnement.",
  red_sap: "La référence financière du dossier est transmise au système SAP.",
  red_assistant: "L'assistant guidé analyse le besoin et recommande le mode, les modèles et les clauses.",
  red_clauses: "La bibliothèque de clauses réutilisables est consultable et recherchable.",
  red_recherche: "La recherche enrichie retrouve rapidement les contenus.",
  red_rapports: "Les rapports de suivi consolident le portefeuille de documents.",
  red_ia: "L'assistant intelligent répond aux questions de rédaction.",

  jur_intro: "Le profil juriste gère les clauses juridiques et la conformité.",
  jur_clause: "Le juriste crée une clause juridique versionnée.",
  jur_recherche: "Il recherche dans les contenus juridiques.",
  jur_ia: "Et interroge l'assistant sur les obligations légales.",

  adm_intro: "L'administrateur de l'organisation pilote la configuration et la gouvernance.",
  adm_users: "Il gère les utilisateurs et leurs rôles.",
  adm_workflows: "Il configure les circuits d'approbation.",
  adm_audit: "Le journal d'audit trace toutes les actions.",
  adm_donnees: "L'espace Données et intelligence artificielle importe des données et des modèles Word.",
  adm_rapports: "Les rapports de suivi offrent une vue consolidée.",

  vitrine: "Enfin, le site vitrine présente la solution au public.",
}

export const O365 = {
  o365_intro: "Terminons par la publication réelle vers Microsoft 365, avec un compte Office 365 de test.",
  o365_creer: "L'administrateur crée et valide un document de démonstration dans l'application.",
  o365_publier: "Puis il le publie, une cible à la fois, vers SharePoint, OneDrive, Teams et Outlook ; chaque dépôt est tracé dans l'historique.",
  o365_pubSharepoint: "Première publication : le document est déposé dans la bibliothèque SharePoint du site, et la ligne d'historique confirme le dépôt réel.",
  o365_pubOnedrive: "Deuxième publication : le même document, au format Word modifiable, est déposé sur OneDrive.",
  o365_pubTeams: "Troisième publication : le fichier est déposé dans l'équipe Teams, dans l'onglet Fichiers du canal.",
  o365_pubOutlook: "Quatrième publication : le document est transmis par courriel Outlook, en pièce jointe.",
  o365_pont: "Les quatre dépôts sont désormais confirmés dans l'historique des publications de l'application.",
  o365_sharepoint: "Passons maintenant dans Microsoft 365 pour vérifier chaque dépôt. Voici d'abord la bibliothèque SharePoint, avec le document réellement présent.",
  o365_fichierSp: "En ouvrant le fichier, on retrouve le contrat généré, tel que déposé.",
  o365_onedrive: "Le document Word, lui, s'ouvre directement dans Microsoft 365, modifiable.",
  o365_teams: "Dans Microsoft Teams, au sein du canal Général de l'équipe, le document apparaît dans l'onglet des fichiers partagés du canal.",
  o365_fichierTeams: "C'est bien le même document, accessible aux membres de l'équipe.",
  o365_outlook: "Et le courriel de transmission figure dans les éléments envoyés d'Outlook, avec la pièce jointe.",
}

export const ART26 = {
  art26_intro: "Cette démonstration présente la solution VINA d'aide à la rédaction et à la gestion des documents d'approvisionnement, telle que proposée à la Ville de Trois-Rivières.",
  art26_version: "Chaque étape de validation peut être figée : une version du document est capturée et conservée.",
  art26_export: "Le document validé s'exporte en PDF et en Word modifiable.",
  art26_publication: "Le document se publie vers Microsoft 365 — SharePoint, OneDrive, Teams et Outlook — et chaque dépôt est tracé dans l'historique.",
  art26_multilingue: "L'interface est multilingue : français, anglais et malgache, au choix de l'utilisateur.",
}

export const TOUS = { ...APP, ...O365, ...ART26 }
