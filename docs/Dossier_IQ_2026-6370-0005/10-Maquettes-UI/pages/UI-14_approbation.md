# UI-14 — Approbation

## Objectif
Approuver ou refuser le document avant la signature.

## Utilisateurs concernés
Approbateur

## Exigences fonctionnelles couvertes
F-05, F-09

## Structure de la page
1. Résumé décisionnel et contexte dossier
2. Document et version soumis
3. Historique des validations
4. Commentaires et motif obligatoire en cas de rejet (proposition)
5. Décision et horodatage

## Actions utilisateur
- Approuver
- Rejeter
- Demander correction
- Déléguer si autorisé

## États et messages à prévoir
- Approbation déjà faite
- Délégation active
- Dossier suspendu

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
Le caractère obligatoire du motif de rejet est recommandé, à confirmer contractuellement.

## Maquette SVG
Voir `../svg/UI-14.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
