# ADR-023 — Connecteur MCP du module CLM, en lecture seule

- **Statut** : Accepté
- **Date** : 2026-11-21
- **Portée** :
  - `clm.application.mcp` (protocole `ServeurMcp`, registre d'outils `OutilMcp`) ;
  - `clm.infrastructure.web.McpController` (`/api/clm/integration/mcp`) ;
  - outils `lister_modeles`, `lire_trousse`, `lire_lot`.
- **Origine** : IQ-A-09 (addenda 1, Q3 : « Any connectors to MCP? », réponse : non exigé).
- **Étude** : [étude de conception, § 3](../etude-conception-ecarts-conditionnels.md#3-iq-a-09--connecteur-mcp).

## Contexte

- **MCP** (Model Context Protocol) est le protocole ouvert par lequel un assistant d'IA appelle les outils d'un
  système.
- **Intérêt d'IQ** : IQ s'en est enquis, sans l'exiger.
- **Ce qui existe** : l'API d'intégration entrante (ADR-011) possède déjà les clients, les jetons, la révocation et le
  cloisonnement par client.

## Options envisagées

1. **Ne rien faire.**
   - Conforme, puisque MCP n'est pas exigé, mais laisse sans réponse un intérêt exprimé, alors que le coût est faible.
2. **Serveur MCP séparé, qui appelle l'API d'intégration.**
   - Un composant de plus à déployer, durcir, surveiller et mettre à jour.
3. **Point d'accès MCP intégré au module CLM (retenue).**
   - Il réutilise les clients d'intégration, sans nouveau secret ni nouveau composant.

## Décision

- **Transport** : *Streamable HTTP*, en mode réponse JSON, sur `POST /api/clm/integration/mcp`.
  - `GET` rend 405 : le serveur n'ouvre pas de flux.
  - Une notification rend 202, sans corps.
- **Protocole** : JSON-RPC 2.0, avec `initialize`, `notifications/initialized`, `ping`, `tools/list` et `tools/call`.
  - Versions acceptées : `2025-11-25`, `2025-06-18` et `2025-03-26`. Une autre version proposée à `initialize` est
    ramenée à la plus récente.
  - Un en-tête `MCP-Protocol-Version` inconnu rend 400.
- **Authentification** : jeton porteur d'un client d'intégration CLM.
  - Un jeton absent, invalide ou révoqué rend 401, avec `WWW-Authenticate: Bearer`.
  - Un module CLM non souscrit rend 403.
- **Outils**, tous en lecture seule (`readOnlyHint: true`), dans l'organisation du client :
  - `lister_modeles` : modèles publiés et leurs champs ;
  - `lire_trousse(trousseId)` : statut, signataires et documents d'une trousse **créée par ce client** ;
  - `lire_lot(lotId)` : statut d'un lot créé par ce client.
- **Erreurs d'outil** : une trousse introuvable ou un argument invalide rendent `isError: true`, avec un message, et
  non une erreur de protocole (comme le prévoit la spécification).
- **Sécurité propre à MCP** :
  - un en-tête `Origin` étranger aux origines autorisées rend 403 (attaque par *DNS rebinding*), refusé par la politique CORS de la plateforme avant le contrôleur ;
  - chaque `tools/call` est consigné au journal CLM, avec le client et l'outil ;
  - un nouvel outil s'ajoute par un bean `OutilMcp`, sans modifier le protocole.
- **Aucune écriture** : un assistant d'IA peut être détourné par le contenu qu'il lit (injection d'invite).
  Déclencher une signature reste réservé à l'API d'intégration, appelée par un système maître.

## Conséquences

- **Usage** : un assistant d'IA (Claude, Copilot, etc.), configuré avec l'URL et un jeton de client, répond sur l'état
  des signatures sans accès à l'interface.
- **Sécurité** : la surface d'attaque est celle de l'API d'intégration, déjà éprouvée. Les tests d'intrusion
  inventorient le nouveau point d'accès.
- **Offre** : la réponse d'IQ à Q3 demande que tout connecteur additionnel soit « clairement décrit et tarifé
  séparément ». Le connecteur figure donc dans l'offre comme option, avec sa description et son prix.
- **Limites** : pas de flux SSE, ni de ressources ou d'invites MCP ; le mode réponse JSON suffit à des outils
  synchrones.
- **Vérification** : tests unitaires du protocole, test d'intégration sur la vraie pile, et scénario BDD `IQ-A-09`. Ce
  dernier fait appel au client de référence `@modelcontextprotocol/sdk`, une implémentation tierce qui valide le
  protocole de bout en bout.
