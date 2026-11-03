# UI-13 — Validation documentaire

## Objectif
Examiner le document, commenter et décider.

## Utilisateurs concernés
Validateur

## Exigences fonctionnelles couvertes
F-05, F-09

## Structure de la page
1. Visionneuse PDF/document
2. Panneau des champs et métadonnées
3. Commentaires contextualisés
4. Historique des versions
5. Résumé des validations précédentes

## Actions utilisateur
- Valider
- Rejeter
- Retourner pour correction
- Ajouter commentaire

## États et messages à prévoir
- Document non accessible
- Version remplacée
- Conflit de traitement

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
Les annotations en ligne et le niveau de granularité des commentaires sont à confirmer.

## Maquette SVG
Voir `../svg/UI-13.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
