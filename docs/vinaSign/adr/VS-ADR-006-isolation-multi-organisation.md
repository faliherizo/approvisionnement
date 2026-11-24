# VS-ADR-006 — Isolation multi-organisation en profondeur

- **Statut** : Proposé
- **Date** : 2026-11-22

## Contexte et problème

Une fuite entre organisations est le risque critique d'un SaaS de signature (dossier 1, § 17). Le contrat l'éprouve
par des canaris (INT-03) ; VinaSign doit faire au moins aussi bien, dès la conception.

## Décision

- **Application** :
  - chaque ressource porte `tenant_id` ;
  - l'organisation se déduit du jeton (application cliente) ou de la session, jamais d'un paramètre ;
  - les dépôts sont filtrés par un port `PorteeTenant`, ce qu'un test d'architecture vérifie.
- **Base de données** : sécurité au niveau des lignes de PostgreSQL (RLS), activée sur toutes les tables métier. Le
  rôle applicatif ne peut pas la contourner, et `app.tenant_id` est posé par transaction.
- **Stockage** : un conteneur ou un préfixe par organisation, et seulement des URL présignées de quelques minutes.
- **Clés** : une clé de chiffrement des données par organisation (chiffrement d'enveloppe) et un cachet par
  organisation. En V2, une option permet à l'organisation d'apporter sa propre clé.
- **Preuve** : l'isolation est éprouvée à chaque construction.
  - Les canaris de l'organisation A doivent rester invisibles et inaltérables depuis B, sur tout l'inventaire OpenAPI.
  - Ce sont les tests d'intrusion du contrat, portés.

## Conséquences

- **Contrainte d'implémentation** : la RLS impose de poser le contexte à chaque transaction. Un test vérifie qu'une
  requête sans contexte ne rend aucune ligne.
- **Instance dédiée** : pour un client entreprise, c'est le même code avec un tenant unique. Aucune bifurcation.
