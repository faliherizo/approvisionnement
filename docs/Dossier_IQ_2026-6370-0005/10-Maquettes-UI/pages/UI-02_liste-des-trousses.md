# UI-02 — Liste des trousses

## Objectif
Rechercher, filtrer et accéder aux dossiers de génération et signature.

## Utilisateurs concernés
Initiateur, gestionnaire

## Exigences fonctionnelles couvertes
F-01, F-08, F-11

## Structure de la page
1. Recherche par numéro, titre, dossier Salesforce ou participant
2. Filtres statut, initiateur, période, type de document
3. Table : référence, objet, source, état, prochaine action, échéance, dernière mise à jour
4. Pagination et tri accessibles
5. Actions contextuelles selon rôle

## Actions utilisateur
- Créer une trousse
- Ouvrir le détail
- Exporter la liste si autorisé

## États et messages à prévoir
- Aucun résultat
- Accès refusé
- Chargement
- Résultat paginé

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
Export de liste et filtres précis proposés, à valider avec IQ.

## Maquette SVG
Voir `../svg/UI-02.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
