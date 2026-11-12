# ADR-011 — API d'intégration entrante du module Contrats et signature (Workday, Salesforce)

- **Statut** : Accepté
- **Date** : 2026-11-11
- **Portée** : `clm.domain.{ClientIntegration, LotSignature, ElementLot, RappelSortant}`,
  `clm.application.{ClientsIntegrationService, DemandesSignatureService, RappelsExternesService, DiffusionStatut}`,
  `clm.infrastructure.{RappelHttp, web.IntegrationEntranteController, web.LotController, web.IntegrationsEntrantesAdminController}`,
  changelog `052`
- **Origine** : addenda 1 de l'AO 2026-6370-0005 — exigence TI-22 (Workday, obligatoire), réponse Q10 (AF-47, lots
  initiés depuis Salesforce ou depuis la plateforme), réponse Q3 (intégrations Microsoft 365, Workday, Entra ID,
  Saviynt/SCIM)

## Contexte

Le module ne recevait aucune demande d'un système externe : il lisait Salesforce et lui renvoyait des statuts, mais
seul un utilisateur connecté pouvait créer une trousse. L'API publique existante (`/api/public`, module « interop »)
appartient à la famille Approvisionnement, exclusive du produit IQ (ADR-010) : une organisation IQ n'y a pas accès.
L'addenda 1 exige qu'un processus RH de Workday déclenche une signature et récupère statut et document signé (TI-22),
et qu'un lot de signatures puisse partir de Salesforce comme de la plateforme (AF-47).

## Options envisagées

1. **Réutiliser l'API publique « interop »** : impose d'activer un module de la famille Approvisionnement, contraire
   à ADR-010 ; ses jetons ne portent pas de notion de système d'origine ni de rappel.
2. **Connecteur Workday dédié (SOAP Workday Web Services) dans la plateforme** : couplage fort à une version de Workday
   et aux processus métier d'IQ, encore inconnus ; chaque autre système demanderait son connecteur.
3. **API d'intégration propre au module, clients par organisation, rappels signés (retenue)** : un contrat REST unique
   pour Workday, Salesforce et tout autre système ; l'adaptation propre à chaque système reste de son côté (Workday
   Studio ou Orchestrate, flux Salesforce, intergiciel).

## Décision

- **Clients d'intégration par organisation**, créés et révoqués par l'administrateur (UI-21). Le secret n'est conservé
  que sous forme d'empreinte à clé (sceau HMAC du serveur : une base divulguée ne permet pas d'éprouver des secrets) ; la clé de signature des rappels est chiffrée (`SecretCipher`). Les deux sont remis une
  seule fois.
- **Jeton OAuth 2.0 client credentials** d'une heure, scellé par le `Scelleur` du module et sans état ; il est refusé
  dès que son client est révoqué. Le contrôleur vérifie ensuite le module pour le tenant du client (même motif que le
  portail du signataire, déclaré dans les tests d'architecture).
- **Mêmes cas d'utilisation qu'à l'écran** : `DemandesSignatureService` orchestre `TrousseService`,
  `DocumentClmService` et le parcours ; la trousse appartient à un initiateur membre de l'organisation, et toutes les
  règles (modèle publié, antivirus, identité selon le risque, séparation des responsabilités) s'appliquent.
- **Lots** : un élément par transaction (`REQUIRES_NEW`) ; un échec est consigné sur l'élément sans annuler les autres.
  500 éléments au plus par appel.
- **Rappels** : `DiffusionStatut` diffuse chaque transition au retour Salesforce existant et aux rappels du client
  créateur ; boîte d'envoi écrite dans la transaction (`clm_rappel_sortant`), relayée par le planificateur avec reprises,
  signée HMAC-SHA256 (`X-CLM-Signature`) avec clé d'idempotence (`Idempotency-Key`).
- **Isolation** : un client ne lit que les trousses et lots qu'il a créés.

## Conséquences

- TI-22 et AF-47 sont couverts par un contrat d'API documenté
  ([api_integration_entrante.md](../Dossier_IQ_2026-6370-0005/04-Architecture/api_integration_entrante.md)) ; la
  mise en œuvre côté Workday (Studio, Orchestrate ou intergiciel) reste à préciser en atelier avec IQ.
- Trois nouvelles origines de trousse (`WORKDAY`, `INTEGRATION`, en plus de `SALESFORCE`) apparaissent dans le suivi.
- Le traitement d'un lot est synchrone : au-delà de quelques centaines d'éléments, un traitement asynchrone sera
  nécessaire (file et consultation du lot par `GET /v1/lots/{id}`, déjà prévue).
- Le provisionnement des comptes (Saviynt / SCIM) n'est pas couvert par cette décision.
- Tests : `IntegrationEntranteIntegrationTest`, `IntegrationEntranteServicesTest`, `IntegrationEntranteDomaineTest`,
  `RappelHttpTest`, e2e `AF-47` et `TI-22` de `clm.exigences.spec.ts`.
