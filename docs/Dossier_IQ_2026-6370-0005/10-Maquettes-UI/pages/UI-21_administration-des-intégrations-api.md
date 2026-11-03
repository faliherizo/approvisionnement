# UI-21 — Administration des intégrations API

## Objectif
Surveiller et configurer les échanges avec Salesforce et systèmes IQ.

## Utilisateurs concernés
Administrateur technique

## Exigences fonctionnelles couvertes
F-10, F-11, NF-11

## Structure de la page
1. État connexion et dernière synchronisation
2. API / webhooks / identifiants techniques masqués
3. Journal requêtes avec corrélation
4. Erreurs, reprises et files en échec
5. Version contrat API et environnement

## Actions utilisateur
- Tester connexion
- Relancer événement
- Consulter détails
- Rotater secret selon procédure

## États et messages à prévoir
- Indisponible
- Authentification expirée
- Événement dupliqué
- Reprise en cours

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
La gestion des secrets doit être déléguée à un coffre sécurisé, jamais affichée dans l’interface.

## Maquette SVG
Voir `../svg/UI-21.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
