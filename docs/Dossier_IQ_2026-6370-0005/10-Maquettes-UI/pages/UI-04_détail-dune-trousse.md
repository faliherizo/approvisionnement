# UI-04 — Détail d’une trousse

## Objectif
Présenter l’état complet d’une trousse et ses éléments.

## Utilisateurs concernés
Initiateur, validateur, approbateur, auditeur

## Exigences fonctionnelles couvertes
F-01, F-05, F-06, F-08, F-09, F-10

## Structure de la page
1. Bandeau référence, état, propriétaire et échéance
2. Onglets : résumé, documents, participants, workflow, historique, preuves
3. Chronologie des étapes et responsables
4. Alertes et anomalies
5. Actions permises selon état et rôle

## Actions utilisateur
- Modifier brouillon
- Ajouter document
- Relancer une étape selon autorisation
- Consulter preuve

## États et messages à prévoir
- Brouillon
- En traitement
- Terminé
- Rejeté
- Annulé
- Erreur de synchronisation

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
Onglets et chronologie constituent une proposition de présentation.

## Maquette SVG
Voir `../svg/UI-04.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
