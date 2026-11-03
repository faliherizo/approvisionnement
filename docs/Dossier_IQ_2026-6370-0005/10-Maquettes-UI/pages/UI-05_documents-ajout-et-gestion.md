# UI-05 — Documents – ajout et gestion

## Objectif
Téléverser et organiser un ou plusieurs documents PDF/Word.

## Utilisateurs concernés
Initiateur

## Exigences fonctionnelles couvertes
F-01, F-09

## Structure de la page
1. Zone de dépôt et sélecteur de fichiers
2. Rappel formats PDF/Word et taille minimale attendue de 25 Mo supportés
3. Liste : nom, type, taille, version, statut, propriétaire
4. Contrôle antivirus et état de traitement
5. Métadonnées et classement par document

## Actions utilisateur
- Téléverser
- Retirer avant lancement
- Prévisualiser
- Remplacer par nouvelle version

## États et messages à prévoir
- Téléversement en cours
- Format non pris en charge
- Taille dépassée par limite configurée
- Échec réseau
- Analyse en attente

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
La limite minimale de prise en charge est 25 Mo; la limite maximale réelle doit être confirmée.

## Maquette SVG
Voir `../svg/UI-05.svg`.

## Notes d’implémentation
- React : composants de page, composants métier réutilisables, formulaires contrôlés et gestion d’état serveur.
- API Spring : autorisation côté serveur, validation des transitions et journalisation des commandes.
- Ne jamais faire confiance à un rôle ou à un statut transmis uniquement par le navigateur.
