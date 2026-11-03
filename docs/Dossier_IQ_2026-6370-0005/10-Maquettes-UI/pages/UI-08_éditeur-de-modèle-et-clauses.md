# UI-08 — Éditeur de modèle et clauses

## Objectif
Configurer le document type, les champs et les clauses réutilisables.

## Utilisateurs concernés
Administrateur d’affaires

## Exigences fonctionnelles couvertes
F-02, F-03, F-04

## Structure de la page
1. Zone de prévisualisation document
2. Panneau champs : texte, liste, date, case à cocher
3. Panneau clauses et règles conditionnelles
4. Propriétés : obligatoire, format, source, rôle destinataire
5. Version et état du modèle

## Actions utilisateur
- Ajouter champ
- Positionner champ
- Définir condition
- Enregistrer brouillon
- Soumettre à publication

## États et messages à prévoir
- Champ invalide
- Variable source inconnue
- Conflit de version
- Aperçu non disponible

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
L’édition visuelle est un choix de conception; les types de champs sont explicitement cités.

## Maquette SVG
Voir `../svg/UI-08.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
