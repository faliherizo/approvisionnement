# UI-18 — Suivi centralisé et recherche

## Objectif
Suivre les dossiers et identifier les exceptions opérationnelles.

## Utilisateurs concernés
Gestionnaire, auditeur

## Exigences fonctionnelles couvertes
F-08, F-10, F-11

## Structure de la page
1. Tableau filtrable multi-dossiers
2. Filtres état, unité, date, responsable, source
3. Colonnes étapes, attente, échéance, dernier événement
4. Vue exceptions : blocage, erreur Salesforce, signature expirée
5. Export selon autorisations

## Actions utilisateur
- Ouvrir dossier
- Réassigner si autorisé
- Relancer intégration
- Exporter

## États et messages à prévoir
- Aucun résultat
- Données non actualisées
- Accès limité

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
Les fonctions de réassignation et export dépendent des droits à définir.

## Maquette SVG
Voir `../svg/UI-18.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
