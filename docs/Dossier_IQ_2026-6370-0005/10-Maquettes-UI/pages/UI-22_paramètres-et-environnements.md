# UI-22 — Paramètres et environnements

## Objectif
Consulter les paramètres applicatifs et distinguer les environnements.

## Utilisateurs concernés
Administrateur technique

## Exigences fonctionnelles couvertes
NF-01, NF-05, NF-08, NF-09

## Structure de la page
1. Bannière environnement DEV/TEST/ACCEPTATION/PROD
2. Paramètres de session
3. Paramètres de conservation et export autorisés
4. État sauvegardes et maintenance (lecture seule si fournisseur)
5. Disponibilité et incidents

## Actions utilisateur
- Consulter configuration
- Modifier paramètre autorisé
- Consulter statut service

## États et messages à prévoir
- Modification interdite en production
- Paramètre invalide
- Maintenance planifiée

## Règles d’interaction et validation
- Les actions ne sont affichées que si l’utilisateur possède le rôle et les permissions nécessaires.
- Toute action métier modifiant un état doit être confirmée côté serveur et inscrite dans la piste d’audit.
- Les erreurs doivent être expliquées en français, associées à une action de récupération et annoncées aux technologies d’assistance.
- Les données sensibles sont masquées selon le rôle et le contexte d’accès.

## Responsive et accessibilité
- Mise en page utilisable à 320 px CSS, tablette et ordinateur; pas de défilement horizontal pour les contenus courants.
- Navigation clavier complète, focus visible et ordre de tabulation cohérent.
- Libellés persistants; ne pas utiliser un placeholder comme seul libellé.
- Contraste WCAG 2.2 AA, états non communiqués par la couleur seule.
- Cibles interactives conformes au minimum WCAG 2.2 AA (24 × 24 CSS px ou espacement conforme); viser 44 × 44 px.
- Tableaux avec en-têtes associés; messages d’état annoncés; erreurs liées aux champs.

## Hypothèses et points à confirmer
La séparation des environnements est une exigence; la visibilité de tous les paramètres dépend de l’offre.

## Maquette SVG
Voir `../svg/UI-22.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
