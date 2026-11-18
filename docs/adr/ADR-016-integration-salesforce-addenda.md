# ADR-016 — Intégration Salesforce : authentification de remplacement, minimisation et correspondance des statuts

- **Statut** : Accepté
- **Date** : 2026-11-16 (confirmé par l'addenda 3 le 2026-11-17)
- **Portée** : `clm.application.port.PortSalesforce`, `clm.infrastructure.{SalesforceRest, AssertionJwt, SalesforceSimulation}`,
  `clm.application.SalesforceService`, `clm.domain.ConnexionSalesforce`, liste `CLM_STATUT_SALESFORCE`, changelog `055`
- **Origine** : addenda 2, Q11, Q14, Q15 ; addenda 3, Q11, Q14, Q15 ; complète ADR-004 du dossier IQ

## Contexte

L'adaptateur REST n'offrait que le flux *client credentials*, relisait l'enregistrement `FundingRequest` en entier et
écrivait des codes internes (`LANCEE`, `SIGNATURE_TERMINEE`) dans le champ de statut. IQ exige une méthode de
remplacement approuvable, la minimisation des données copiées et une correspondance décrite des statuts.

## Décision

- **Authentification** : méthode par connexion, *client credentials* (défaut) ou **JWT Bearer** (RFC 7523, RS256 ;
  assertion de 3 minutes ; émetteur = identifiant client, sujet = utilisateur d'exécution, audience = origine du serveur
  de jetons). La clé privée est chiffrée au repos, jamais réaffichée ni journalisée ; seul le certificat public est
  déposé chez Salesforce.
- **Minimisation** : le port reçoit la liste des champs nécessaires ; l'adaptateur demande `Id`, `Name`, les champs
  déclarés par l'organisation et les sources du modèle (`?fields=`), et écarte tout champ non demandé. Un champ refusé
  par Salesforce (`INVALID_FIELD`) devient un message qui nomme la configuration à corriger.
- **Statuts** : liste de référence `CLM_STATUT_SALESFORCE` (code = événement, libellé = valeur écrite), modifiable par
  l'organisation ; la valeur est figée dans le message au moment de l'événement, donc une relance republie exactement la
  même valeur. Le retour à une étape antérieure publie « retournée ».

## Conséquences

- En recette, la liste « Champs Salesforce » doit contenir les noms d'API réels de l'organisation d'IQ.
- Le bac à sable local (`infra/bacs-a-sable/salesforce`) gère `fields=` et le JWT Bearer (`SF_JWT_CERTIFICAT`).
- Tests : `SalesforceRestTest`, `ConfigurationDomaineClmTest`, `Addenda2IntegrationTest`, `Addenda2ServicesTest`, e2e « Addenda 2 ».
