# UI-12 — Boîte de tâches

## Objectif
Afficher les tâches qui attendent une intervention de l’utilisateur.

## Utilisateurs concernés
Validateur, approbateur, signataire interne

## Exigences fonctionnelles couvertes
F-05, F-06, F-08, F-11

## Structure de la page
1. Filtres : à traiter, en retard, terminées, déléguées
2. Table : trousse, document, type d’action, demandeur, échéance
3. Priorisation par date et criticité
4. Indication des délégations et réassignations

## Actions utilisateur
- Ouvrir tâche
- Traiter
- Filtrer
- Voir historique

## États et messages à prévoir
- Aucune tâche
- Tâche déjà traitée
- Tâche réassignée

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
La boîte de tâches unifiée est une proposition UX.

## Maquette SVG
Voir `../svg/UI-12.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
