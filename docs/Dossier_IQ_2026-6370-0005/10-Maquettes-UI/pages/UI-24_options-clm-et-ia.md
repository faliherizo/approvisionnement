# UI-24 — Options CLM et IA

## Objectif
Présenter séparément les options avancées de CLM et d’IA.

## Utilisateurs concernés
Administrateur / gestionnaire autorisé

## Exigences fonctionnelles couvertes
F-13

## Structure de la page
1. Catalogue des capacités optionnelles activées
2. Analyse de clauses et extraction de métadonnées
3. Classification et détection d’anomalies avec niveau de confiance
4. Validation humaine et justification
5. Indication explicite des coûts/licences si connus par contrat

## Actions utilisateur
- Lancer analyse
- Valider résultat
- Signaler erreur
- Désactiver capacité selon droits

## États et messages à prévoir
- Option non souscrite
- Résultat à réviser
- Données non admissibles

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
CLM avancé et CLM/IA sont des options tarifées séparément; ne pas les présenter comme incluses par défaut.

## Maquette SVG
Voir `../svg/UI-24.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
