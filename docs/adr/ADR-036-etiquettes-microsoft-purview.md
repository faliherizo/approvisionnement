# ADR-036 — Étiquettes Microsoft Purview (sensibilité et rétention) pour le module de signature

- **Statut** : Accepté
- **Date** : 2026-11-26
- **Portée** :
  - domaine : `EtiquettePurview`, `PolitiquePurview` (changelog 082), `DocumentTrousse` (étiquettes conservées),
    `DepotM365` (résultat de la propagation) ;
  - application : port `EtiquetagePurview`, `EtiquetagePurviewService` ; points d'appel dans `DocumentClmService`
    (document déposé ou produit), `ParcoursService` (version signée), `DepotsM365Service` (propagation au dépôt),
    `ConservationService` (échéance alignée) ;
  - infrastructure : `EtiquetagePurviewSimule` (défaut), `EtiquetagePurviewGraph` (profil `purview`), `ClientEntra`,
    `PurviewClmController` ;
  - écrans : `/clm/admin/purview`, métadonnées du document (onglet Documents), suivi des dépôts Microsoft 365.
- **Origine** : Annexe 10 d'IQ, SEC-27 (Souhaitable) ; story RST-53. Prolonge ADR-002 (ports et adaptateurs Microsoft 365)
  et ADR-029 (dépôt SharePoint du module, RST-45) ; s'articule avec ADR-012 (conservation et destruction).

## Contexte

- **SEC-27** : « intégration avec Microsoft Purview afin de soutenir la gestion automatisée du cycle de vie des données et
  l'application d'étiquettes ».
- **Existant** : les trousses terminées sont déposées dans SharePoint ou OneDrive par le connecteur Microsoft 365 de
  l'organisation (simulé tant qu'il n'est pas activé) ; la conservation applique des règles par type de contrat.
- **Microsoft Graph** offre : la liste des étiquettes de sensibilité (`/security/informationProtection/sensitivityLabels`,
  bêta en mode application), la liste des étiquettes de rétention (`/security/labels/retentionLabels`), l'apposition
  d'une étiquette de sensibilité à un fichier (`assignSensitivityLabel`, asynchrone, API facturée à l'usage) et d'une
  étiquette de rétention (`PATCH …/retentionLabel`).

## Options envisagées

1. **Étiquetage par la plateforme, propagé à Microsoft 365** : l'administration choisit les étiquettes dans le catalogue
   du locataire, par défaut et par type de contrat ; chaque document les reçoit et les garde ; le dépôt SharePoint les
   reçoit par Graph ; la conservation de la plateforme ne détruit jamais avant la rétention de l'étiquette.
2. **Étiquetage automatique par Purview seul** (stratégies d'auto-étiquetage sur la bibliothèque SharePoint) : aucun
   développement, mais rien n'est étiqueté dans la plateforme, et les documents non déposés échappent à Purview.
3. **Chiffrement MIP du contenu par la plateforme** (SDK MIP) : protège le fichier lui-même, mais SDK natif (C++/.NET),
   aucun client Java pris en charge, et un fichier chiffré MIP n'est plus lisible pour la signature et l'aperçu.

## Décision

Option 1, en ports et adaptateurs.

- **Port** `EtiquetagePurview` : `catalogue(organisation)` et `apposer(organisation, adresse du fichier déposé,
  sensibilité, rétention)`. Un échec se lit dans le résultat, jamais par exception : le dépôt reste fait et l'étiquette
  se réappose à la relance du dépôt.
- **Adaptateurs** :
  - simulé (défaut) : catalogue représentatif (Public, Interne, Confidentiel, Hautement confidentiel ; « Contrats - 7 ans »,
    « Dossiers RH - 10 ans », « Conservation permanente ») et apposition consignée ;
  - Microsoft Graph (profil `purview`) : mêmes identifiants d'application que la connexion Microsoft 365 de
    l'organisation (ADR-002) ; le fichier déposé est retrouvé par son adresse (`/shares/u!…/driveItem`) ; permissions
    `InformationProtectionPolicy.Read.All`, `RecordsManagement.Read.All`, `Files.ReadWrite.All`, `Sites.ReadWrite.All`.
- **Politique** (`clm_politique_purview`) : une par défaut et, au besoin, une par type de contrat qui la remplace ; au
  moins une étiquette ; les étiquettes doivent figurer au catalogue du locataire. Administration d'affaires ou technique ;
  chaque changement est inscrit au journal de configuration.
- **Apposition dans la plateforme** : à l'enregistrement d'un document (dépôt, nouvelle version, génération), la politique
  applicable lui donne ses étiquettes, conservées en colonnes du document (`clm_document.etiquette_*`) ; la version
  signée reprend celles de sa source. L'API et l'écran du document les affichent.
- **Propagation** : après le dépôt d'un document signé dans SharePoint/OneDrive (RST-45), ses étiquettes sont apposées au
  fichier déposé ; le résultat est noté sur le dépôt (écran Microsoft 365) et au journal scellé de la trousse
  (`ETIQUETTE_PURVIEW`).
- **Conservation alignée** : l'échéance d'une trousse n'est jamais antérieure à la fin de la rétention de l'étiquette
  conservée avec ses documents (clôture + durée) ; une conservation permanente le reste. La destruction suit donc la plus
  longue des deux obligations ; Purview reste maître de la rétention des copies déposées dans Microsoft 365.

## Conséquences

- **Positives** : SEC-27 démontrable de bout en bout (choix de l'étiquette, document étiqueté et conservé avec son
  étiquette, propagation au dépôt, conservation alignée) sans dépendre d'un locataire réel ; bascule vers Purview par un
  profil, sans changer le code métier.
- **Négatives** :
  - la plateforme n'applique pas la protection (chiffrement) MIP aux fichiers qu'elle conserve : la confidentialité y est
    assurée par le chiffrement au repos par organisation (ADR-037) ;
  - `assignSensitivityLabel` est une API Graph à l'usage (frais Microsoft) et asynchrone : le résultat noté dit que la
    demande est acceptée, non que l'étiquette est déjà visible ;
  - la liste des étiquettes de sensibilité en mode application dépend d'une API bêta de Graph ;
  - la rétention lue de Purview est la durée en jours ; les rétentions déclenchées par un événement ne sont pas traduites.
- **Vérification** :
  - `PurviewDomaineTest` (domaine à 100 %) ;
  - `EtiquetagePurviewContrat` exécuté contre `EtiquetagePurviewSimuleTest` et `EtiquetagePurviewGraphTest` (Graph
    simulé : catalogues, résolution du fichier, `assignSensitivityLabel`, `retentionLabel`, pannes) ;
  - `PurviewIntegrationTest` (vraie pile) : catalogue, politiques, document et version signée étiquetés, propagation au
    dépôt SharePoint simulé, échéance de conservation alignée sur la rétention ;
  - scénario e2e `E2E-AX-IQ-21`.
- **Reste à faire** : recette contre le locataire Microsoft 365 d'IQ (étiquettes publiées, consentement des permissions,
  facturation de `assignSensitivityLabel`) et vérification dans le centre de conformité Purview.
