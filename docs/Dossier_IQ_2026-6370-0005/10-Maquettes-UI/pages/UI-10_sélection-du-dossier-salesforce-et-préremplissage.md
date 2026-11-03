# UI-10 — Sélection du dossier Salesforce et préremplissage

## Objectif
Récupérer les données du système maître et contrôler le préremplissage.

## Utilisateurs concernés
Initiateur

## Exigences fonctionnelles couvertes
F-02, F-10

## Structure de la page
1. Recherche par identifiant de demande ou numéro de dossier
2. Résumé Salesforce : demandeur, programme, montant, entité
3. Table de correspondance champ source / champ cible
4. Valeurs préremplies avec provenance et date de récupération
5. Liste des champs manquants ou en conflit

## Actions utilisateur
- Rechercher
- Importer / actualiser
- Corriger valeur autorisée
- Continuer

## États et messages à prévoir
- Salesforce indisponible
- Dossier non trouvé
- Données obsolètes
- Mapping incomplet

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
Les champs et objets Salesforce précis ne sont pas tous détaillés dans les documents fournis.

## Maquette SVG
Voir `../svg/UI-10.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
