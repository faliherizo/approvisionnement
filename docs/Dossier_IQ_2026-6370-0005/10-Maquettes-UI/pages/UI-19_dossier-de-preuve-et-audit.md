# UI-19 — Dossier de preuve et audit

## Objectif
Consulter et exporter les preuves associées aux documents signés.

## Utilisateurs concernés
Auditeur, conformité

## Exigences fonctionnelles couvertes
F-06, F-09, F-11

## Structure de la page
1. Résumé document final et empreinte
2. Certificat de signature
3. Identités et consentements disponibles
4. Horodatages et événements chronologiques
5. Historique de téléchargement et export
6. Métadonnées et liens de corrélation

## Actions utilisateur
- Télécharger preuve
- Exporter dossier complet
- Vérifier intégrité
- Consulter journal

## États et messages à prévoir
- Preuve en génération
- Élément indisponible
- Export refusé par droits

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
La composition exacte du dossier de preuve dépend du fournisseur et doit être démontrée.

## Maquette SVG
Voir `../svg/UI-19.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
