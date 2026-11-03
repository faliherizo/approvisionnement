# UI-06 — Historique et versions documentaires

## Objectif
Comparer et retrouver les versions successives d’un document.

## Utilisateurs concernés
Initiateur, auditeur

## Exigences fonctionnelles couvertes
F-09

## Structure de la page
1. Liste chronologique des versions
2. Auteur, date, motif, empreinte et statut
3. Indication version brouillon / envoyée / signée
4. Lien vers document final et historique des modifications

## Actions utilisateur
- Télécharger version autorisée
- Consulter métadonnées
- Créer une nouvelle version si le processus le permet

## États et messages à prévoir
- Aucune version antérieure
- Version verrouillée après signature

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
La règle de modification après signature est à confirmer; la maquette applique le principe prudent d’immutabilité.

## Maquette SVG
Voir `../svg/UI-06.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
