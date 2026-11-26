# ADR-032 — Rapports personnalisables du module de signature, exportés en CSV et en Excel

- **Statut** : Accepté
- **Date** : 2026-11-25
- **Portée** :
  - entité `clm.domain.RapportClm` (définition, calcul des délais), changelog 079 ;
  - `clm.application.RapportsClmService`, `TableauExport` ; port `ClasseurTableur`, adaptateur `ClasseurXlsx` (Apache POI) ;
  - `RapportsClmController` (`/api/clm/admin/rapports`), export du relevé (`/api/clm/admin/utilisation/export`).
- **Origine** : Annexe 10 d'IQ, AF-37 (Essentielle) ; story RST-42.

## Contexte

- **Existant** : suivi filtrable exporté en CSV, tableau de bord, relevé d'utilisation en JSON. Ni délais moyens, ni
  incidents, ni rapport que l'utilisateur crée et garde, ni export Excel.
- **Une plateforme qui a déjà un module « rapport »** (CHU) : il porte sur les formulaires et leurs saisies ; le module CLM
  ne lit pas ses tables (ADR-006) et n'en dépend pas.

## Options envisagées

1. **Rapports du module CLM, définitions enregistrées, calcul à la demande** depuis le journal scellé et les files
   d'intégration ; neuf types de rapport aux colonnes connues, filtrés par période glissante, unité, type de contrat et
   statut.
2. **Entrepôt de données et outil de BI** (Power BI branché sur la base) : puissant, mais hors de l'application, sans
   contrôle d'accès par rôle du module, et dépendant d'un accès direct à la base que SEC-17 restreint.
3. **Requêtes libres** (générateur de requêtes) : flexible, mais ouvre la lecture de toute table et la saisie d'expressions.

## Décision

Option 1.

- **Types** : signatures en attente, délais de traitement (avec moyenne), délais moyens par étape, processus complétés,
  rejets, échéances dépassées, utilisation (relevé par enveloppe), volumétrie mensuelle, incidents (antivirus, intégrité,
  Microsoft 365, Salesforce, rappels d'intégration).
- **Délais** : calculés par le domaine à partir du journal scellé — premier lancement → achèvement ; activation d'une
  étape → décision qui la clôt — et exprimés en heures à une décimale.
- **Droits** : l'administration d'affaires crée, modifie et supprime ; elle et l'auditeur consultent et exportent.
  Chaque création, modification, suppression et export est inscrit au journal d'audit.
- **Exports** : CSV (séparateur « ; », BOM UTF-8, cellules de formule neutralisées) et XLSX par un port ; chaque cellule
  XLSX est écrite comme texte, jamais comme formule. Le relevé d'utilisation s'exporte dans les mêmes formats.

## Conséquences

- **Positives** : AF-37 couvert dans l'application, sous les mêmes droits que le reste du module ; un nouveau type de
  rapport s'ajoute au domaine et au service sans toucher aux autres.
- **Négatives** :
  - le calcul se fait à la demande sur la période (366 jours au plus) : à surveiller sur de gros volumes (essai de charge
    RST-48) ;
  - les en-têtes d'export sont en français, langue de travail de l'organisation ; l'écran les traduit.
- **Vérification** :
  - `RapportClmTest` : définition, colonnes, délais, moyenne ;
  - `RapportsClmServiceTest` : incidents de toutes sources, signatures en attente, formules neutralisées ;
  - `AnnexeDixIntegrationTest` (RST-42) : création, modification, exécution, exports CSV et XLSX relus, droits ;
  - scénarios e2e `E2E-AX-IQ-05` et `E2E-AX-IQ-06`.
