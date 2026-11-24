# ADR-022 — Cartes de paiement refusées à la frontière : la plateforme reste hors du périmètre PCI DSS

- **Statut** : Accepté
- **Date** : 2026-11-21
- **Portée** :
  - `securite.domain.NumeroCarte` (détection) ;
  - `securite.infrastructure.GardeCartesPaiement` (filtre HTTP) ;
  - propriétés `app.securite.cartes.*`.
- **Origine** : IQ-D-14 (3.3.1 B, « PCI DSS si des cartes de paiement sont traitées »).
- **Étude** : [étude de conception, § 2](../etude-conception-ecarts-conditionnels.md#2-iq-d-14--pci-dss-si-des-cartes-de-paiement-sont-traitées).

## Contexte

- **Le périmètre PCI DSS** comprend tout système qui *stocke, traite ou transmet* un numéro de carte (PAN).
- **Aucun paiement par conception** : la plateforme n'a aucune fonction de paiement.
- **Mais des champs libres** : commentaires, valeurs de formulaires, messages ou clauses peuvent recevoir un numéro de
  carte par erreur. Une seule saisie suffirait à faire entrer la base, les sauvegardes et les journaux dans le
  périmètre.

## Options envisagées

1. **Certification PCI DSS.**
   - Disproportionnée pour une plateforme sans paiement.
2. **Déclaration contractuelle seule.**
   - Ne protège pas d'une saisie par erreur.
3. **Accepter la requête en masquant le numéro.**
   - Le numéro a déjà transité par l'application, et les journaux du relais l'ont vu.
   - Un masquage incomplet reste une donnée PCI.
4. **Refus à la frontière (retenue).**
   - Le numéro n'atteint aucune logique métier, aucune base, aucun journal applicatif.

## Décision

- **Détection**, dans le domaine, sans E/S. Un numéro de carte est :
  - une suite de 13 à 19 chiffres, séparés au plus par des espaces ou des tirets isolés, et non collée à d'autres
    chiffres ;
  - **et** préfixée par un réseau reconnu, avec sa longueur : Visa, Mastercard (51–55, 2221–2720), American Express,
    Discover, JCB, Diners, UnionPay ;
  - **et** valide selon la clé de Luhn.
- **Garde**, sous forme de filtre HTTP placé après l'authentification (un anonyme reçoit 401 sans inspection) :
  - il examine les écritures (`POST`, `PUT`, `PATCH`) vers `/api` et `/scim`, dont le corps est en JSON, en texte ou
    en formulaire, ainsi que la chaîne de requête de toute méthode ;
  - il rend `422`, avec `{"error":"carte_paiement_refusee"}` et un message qui dit quoi corriger ;
  - il journalise en WARN la méthode, le chemin et le réseau, **jamais** le numéro ni ses derniers chiffres.
- **Réglages** :
  - `app.securite.cartes.actif` (vrai par défaut) ;
  - `app.securite.cartes.chemins-exemptes`, une liste de préfixes vide par défaut, pour un faux positif avéré et
    motivé.

## Conséquences

- **Hors du périmètre PCI** : aucune donnée de carte n'est stockée, traitée ni transmise. La plateforme reste hors du
  périmètre PCI DSS, et l'exigence devient vérifiable par un test.
- **Faux positifs** : rares, puisqu'il faut à la fois un préfixe de réseau, la bonne longueur et la clé de Luhn. Un
  identifiant interne qui remplit les trois est refusé ; l'exemption se règle par chemin.
- **Limites** :
  - les fichiers téléversés (PDF, Word) ne sont pas inspectés. Ce sont des binaires, et le document d'un contrat ne
    contient pas de PAN par nature. Ce risque résiduel est accepté ;
  - un corps de plus de 2 Mio n'est pas inspecté au-delà de cette taille.
- **Vérification** : tests unitaires de la détection (numéros de test des réseaux, faux positifs), test d'intégration
  du refus sur l'API, scénario BDD `IQ-D-14`, test d'intrusion INT-08.
