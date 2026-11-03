# UI-15 — Préparation de la signature

## Objectif
Définir les signataires, leur ordre et les zones de signature.

## Utilisateurs concernés
Initiateur

## Exigences fonctionnelles couvertes
F-04, F-06

## Structure de la page
1. Liste des documents à signer
2. Participants internes/externes et coordonnées
3. Ordre séquentiel, parallèle ou mixte
4. Zones de signature et champs associés
5. Niveau de vérification d’identité configuré
6. Aperçu du parcours

## Actions utilisateur
- Ajouter signataire
- Réordonner
- Attribuer zone
- Envoyer pour signature

## États et messages à prévoir
- Coordonnée invalide
- Zone sans signataire
- Signataire en doublon
- Identité à vérifier

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
Les méthodes d’authentification sont dépendantes du fournisseur et du niveau de risque; à confirmer.

## Maquette SVG
Voir `../svg/UI-15.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
