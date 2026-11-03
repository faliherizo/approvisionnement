# Guide d'implémentation — conventions transversales

## Backend
- Java LTS compatible avec la version Spring Boot retenue et la plateforme de déploiement approuvée.
- Spring Boot, Spring Security, Spring Data JPA, Bean Validation, OpenAPI.
- Architecture hexagonale / ports-adaptateurs par module.
- DTO séparés du domaine; ne pas exposer directement les entités JPA.
- Migrations versionnées (Flyway ou Liquibase).
- Gestion centralisée des erreurs et identifiant de corrélation.
- Secrets via coffre de secrets; aucune clé dans le dépôt.
- Outbox transactionnelle pour les événements critiques.

## Frontend
- React avec TypeScript.
- Routage et composants par fonctionnalité.
- Formulaires validés côté client et serveur.
- Navigation clavier, libellés explicites, focus visible, annonces de statut et contrastes vérifiés.
- États chargement, vide, succès, erreur et accès interdit.
- Les contrôles d'accès sont appliqués côté backend; le frontend ne fait que refléter les autorisations.

## Données et fichiers
- UUID comme identifiants techniques; références externes conservées séparément.
- Horodatage UTC; affichage local selon paramètres.
- Hash SHA-256 pour chaque version de document et artefact de preuve.
- Analyse antivirus et validation type réel / taille.
- Stockage objet privé, chiffré, versionné; URL signées à durée courte.
- Pas de contenu documentaire sensible dans les logs applicatifs.

## Tests
- Unitaires domaine et règles de transition.
- Intégration repository / stockage / API externes.
- Tests de contrat Salesforce et fournisseur de signature.
- Tests E2E workflow complet.
- Tests d'autorisation horizontale (IDOR), CSRF, XSS, injection, SSRF, téléversement.
- Tests charge et endurance pour pics de documents.
- Tests de restauration et réconciliation.

## Pipeline
1. Build et analyse statique.
2. Tests unitaires.
3. Analyse dépendances / vulnérabilités.
4. Tests intégration et contrats.
5. Construction image signée.
6. Déploiement DEV/INT.
7. Validation ACC et approbation IQ.
8. Déploiement PRD autorisé et plan de retour arrière.
