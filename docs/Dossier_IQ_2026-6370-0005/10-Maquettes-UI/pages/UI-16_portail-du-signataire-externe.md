# UI-16 — Portail du signataire externe

## Objectif
Permettre la consultation, le consentement et la signature sans compte interne.

## Utilisateurs concernés
Signataire externe

## Exigences fonctionnelles couvertes
F-06, F-09

## Structure de la page
1. Page d’invitation et identité de la trousse
2. Étape de vérification d’identité
3. Document à lire et télécharger si permis
4. Consentement explicite
5. Champs attribués et zones de signature
6. Confirmation et reçu

## Actions utilisateur
- Vérifier identité
- Accepter consentement
- Signer
- Refuser / signaler problème

## États et messages à prévoir
- Invitation expirée
- Lien déjà utilisé
- Vérification échouée
- Document retiré
- Signature confirmée

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
Le parcours d’identité et les mécanismes de preuve doivent être validés avec le fournisseur retenu.

## Maquette SVG
Voir `../svg/UI-16.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
