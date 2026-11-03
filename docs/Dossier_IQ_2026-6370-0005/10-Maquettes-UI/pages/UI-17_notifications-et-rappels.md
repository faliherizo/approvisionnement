# UI-17 — Notifications et rappels

## Objectif
Paramétrer les communications et échéances automatiques.

## Utilisateurs concernés
Administrateur d’affaires

## Exigences fonctionnelles couvertes
F-07

## Structure de la page
1. Catalogue événements : invitation, tâche, retard, rejet, signature terminée
2. Canaux disponibles selon fournisseur
3. Modèles de messages et variables
4. Règles de rappel et destinataires en copie
5. Aperçu et test d’envoi

## Actions utilisateur
- Activer/désactiver
- Modifier modèle
- Configurer fréquence
- Envoyer test

## États et messages à prévoir
- Adresse invalide
- Variable non résolue
- Envoi en échec

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
Canaux, fréquence maximale et modèles sont à confirmer.

## Maquette SVG
Voir `../svg/UI-17.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
