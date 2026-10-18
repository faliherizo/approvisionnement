# Communications inter-applications sécurisées

**Contexte.** Deux applications complémentaires de la plateforme :

- **freelance-invoicing / Nexu VINA** — gestion de projets, du temps, des contrats
  (facturation/paie), du budget (Node/Express + React, multi-tenant).
- **contractuelle_contrat** (ce dépôt) — rédaction contractuelle (devis, clauses,
  addendas, signature) — Java/Spring Boot, multi-tenant.

Objectif : les faire **communiquer via des services sécurisés**, chaque application
restant maître de son domaine (bounded context distinct, bases séparées).

---

## 1. Principes

- **Pas de base partagée.** Échanges uniquement par **API REST** (synchrone) et/ou
  **événements** (asynchrone). Chaque app possède ses données.
- **Identité de service.** Communication *service-to-service* via **OAuth2 Client
  Credentials** (comptes de service), jetons **JWT** à portée (*scopes*) limitée.
- **Confiance mutuelle.** **mTLS** entre services (certificats), ou passerelle/mesh
  (ex. mTLS via un service mesh) en production.
- **Propagation du tenant.** Le `tenant` (claim JWT) est propagé de bout en bout pour
  préserver l'**isolation multi-tenant** des deux côtés.
- **Idempotence & traçabilité.** Clés d'idempotence sur les commandes ; journal
  d'audit des appels entrants/sortants.

---

## 2. Architecture

```
┌──────────────────────────┐        OAuth2 (client credentials) + mTLS        ┌──────────────────────────┐
│  Nexu VINA (projets,      │  ───────────────────────────────────────────►  │  Rédaction contractuelle  │
│  temps, budget, factures) │   POST /api/documents (créer un devis lié)      │  (ce dépôt)               │
│                           │  ◄───────────────────────────────────────────  │                           │
│  Consomme : statut du     │      Webhook / événement « contrat.signé »      │  Émet : contrat.signé,    │
│  contrat, PDF final       │                                                 │  contrat.publié           │
└──────────────────────────┘                                                 └──────────────────────────┘
                 │                          ┌───────────────────────┐                     │
                 └─────────────────────────►│  IdP OIDC (Keycloak)   │◄────────────────────┘
                                            │  émet les jetons de     │
                                            │  service + tenant claim │
                                            └───────────────────────┘
```

---

## 3. Flux typiques

### A. VINA demande la rédaction d'un contrat (synchrone)
1. Un projet VINA atteint l'étape « contractualisation ».
2. VINA obtient un jeton de service (client credentials, scope `contrat:write`,
   claim `tenant`).
3. VINA appelle `POST /api/documents` (+ `/instancier` d'un template) sur l'app de
   rédaction, en propageant le tenant.
4. L'app de rédaction crée le devis **dans le bon tenant** et renvoie son `id`.

### B. Contrat signé → facturation/budget (asynchrone)
1. À la signature (`document → SIGNE`), l'app de rédaction publie un événement
   **`contrat.signé`** (webhook signé HMAC ou message bus).
2. VINA reçoit l'événement (vérifie la signature + idempotence), met à jour le budget
   et déclenche la facturation du projet.

### C. Récupération du document final
- VINA récupère le PDF final via `GET /api/documents/{id}/export/pdf` avec son jeton
  de service (scope `contrat:read`).

---

## 4. Sécurité — points de contrôle

| Contrôle | Mise en œuvre |
|----------|---------------|
| Authentification de service | OAuth2 **client credentials**, JWT signé |
| Autorisation | **Scopes** (`contrat:read`, `contrat:write`) + rôle service |
| Confidentialité/intégrité transport | **TLS 1.2+**, idéalement **mTLS** |
| Isolation multi-tenant | Claim `tenant` propagé et vérifié des deux côtés |
| Webhooks | Signature **HMAC** + horodatage + rejeu interdit (idempotence) |
| Moindre privilège | Un compte de service par intégration, scopes minimaux |
| Journalisation | Audit des appels entrants/sortants (corrélation par `traceId`) |
| Résidence des données | Hébergement Canada ; IA locale (Loi 25) |

---

## 5. Compatibilité avec l'existant

- Les deux apps utilisent déjà **JWT** et un modèle **multi-tenant** → alignement
  naturel des claims (`sub`, `roles`, `tenant`).
- **IdP commun recommandé** : **Keycloak/Entra ID** (OIDC) émet à la fois les jetons
  utilisateurs (SSO) et les jetons de service — voir
  [securite-authentification.md](securite-authentification.md).
- Passerelle unique (nginx / Spring Cloud Gateway) devant les deux backends, routage
  par préfixe — voir [integration-freelance-invoicing.md](integration-freelance-invoicing.md).

## 6. Roadmap d'intégration
1. Déclarer les **comptes de service** (VINA↔Rédaction) dans l'IdP + scopes.
2. Exposer les **webhooks** `contrat.signé` / `contrat.publié` (HMAC signé).
3. mTLS entre services (ou service mesh).
4. Corrélation & observabilité (traceId partagé).
