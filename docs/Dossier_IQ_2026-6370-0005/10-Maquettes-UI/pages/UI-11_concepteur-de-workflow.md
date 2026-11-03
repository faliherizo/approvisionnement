# UI-11 — Concepteur de workflow

## Objectif
Configurer les étapes de validation, approbation et signature.

## Utilisateurs concernés
Administrateur d’affaires

## Exigences fonctionnelles couvertes
F-05, F-06, F-07

## Structure de la page
1. Canvas ordonné des étapes
2. Types : validation, approbation, signature
3. Modes séquentiel, parallèle et mixte
4. Assignation par utilisateur, groupe ou règle
5. Échéance, quorum et politique de rejet configurables
6. Résumé lisible du parcours

## Actions utilisateur
- Ajouter étape
- Déplacer étape
- Affecter groupe
- Tester parcours
- Publier workflow

## États et messages à prévoir
- Étape sans responsable
- Cycle détecté
- Quorum incohérent
- Workflow publié verrouillé

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
Le quorum, les branches et les règles de retour sont des paramètres à faire confirmer.

## Maquette SVG
Voir `../svg/UI-11.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
