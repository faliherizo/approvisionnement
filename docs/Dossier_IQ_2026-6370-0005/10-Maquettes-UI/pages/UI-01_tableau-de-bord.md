# UI-01 — Tableau de bord

## Objectif
Donner une vue immédiate des trousses et actions prioritaires.

## Utilisateurs concernés
Initiateur, gestionnaire, administrateur

## Exigences fonctionnelles couvertes
F-01, F-05, F-07, F-08, F-10

## Structure de la page
1. En-tête avec recherche globale et profil
2. Indicateurs : brouillons, en validation, en signature, en retard
3. Liste « Mes actions requises » avec responsable et échéance
4. Activité récente et alertes d’intégration Salesforce
5. Filtres par période, unité et statut

## Actions utilisateur
- Ouvrir une trousse
- Reprendre une action
- Créer une trousse
- Accéder aux alertes

## États et messages à prévoir
- Chargement
- Aucun dossier
- Erreur de chargement
- Données partiellement synchronisées

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
Les indicateurs et filtres sont des choix d’ergonomie; les documents ne prescrivent pas les KPI exacts.

## Maquette SVG
Voir `../svg/UI-01.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
