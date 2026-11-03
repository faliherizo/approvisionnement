# UI-03 — Créer une trousse

## Objectif
Créer un dossier et définir son origine et ses paramètres initiaux.

## Utilisateurs concernés
Initiateur

## Exigences fonctionnelles couvertes
F-01, F-02, F-10

## Structure de la page
1. Assistant en étapes : source, documents, participants, processus, récapitulatif
2. Recherche dossier Salesforce et récupération des métadonnées
3. Titre, référence externe, type de contrat, unité responsable
4. Brouillon sauvegardable
5. Résumé des données importées et champs manquants

## Actions utilisateur
- Enregistrer brouillon
- Continuer
- Annuler

## États et messages à prévoir
- Dossier Salesforce introuvable
- Données incomplètes
- Doublon de référence
- Brouillon enregistré

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
Assistant multiétape proposé; le DAO exige la création et l’intégration mais ne prescrit pas le parcours UI.

## Maquette SVG
Voir `../svg/UI-03.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
