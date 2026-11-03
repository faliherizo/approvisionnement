# ADR local — ST-08 — Exécuter validation et approbation

- **Statut** : proposé, à confirmer en revue d'architecture.
- **Contexte** : En tant que validateur ou approbateur, je veux décider et commenter afin de faire avancer ou corriger le document. Cette story doit respecter les exigences F-05.
- **Décision** : implémenter la capacité comme cas d'utilisation dédié dans le module métier correspondant, exposé par une API versionnée; appliquer les contrôles d'accès côté serveur et émettre un événement d'audit à chaque transition significative.
- **Options considérées** :
  1. Logique métier dans les composants React — rejetée, car les règles seraient contournables et dupliquées.
  2. Logique métier dans contrôleurs REST — rejetée, car elle mélange transport et domaine.
  3. Service applicatif + domaine + adaptateurs — retenue pour testabilité et séparation des responsabilités.
- **Conséquences positives** : règles testables, contrats stables, audit transversal, évolution contrôlée.
- **Conséquences négatives / risques** : davantage de structure initiale; nécessité de maintenir les contrats API et événements.
- **Sécurité** : autorisation au niveau de la ressource, minimisation des données, journalisation sans contenu sensible.
- **Tests obligatoires** : nominal, erreurs, répétition/idempotence, droits positifs/négatifs, audit, intégration dépendances.
- **Réexamen** : si les exigences de performance, d'isolation ou de disponibilité démontrent le besoin d'extraire un service.
