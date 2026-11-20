# ADR-017 — Séparation de l'administration d'affaires et de l'administration technique

- **Statut** : Accepté
- **Date** : 2026-11-19
- **Portée** : `commun.domain.Enums.Role` (`ADMIN_AFFAIRES`, `ADMIN_TECHNIQUE`), `auth.infrastructure.SecurityConfig`
  (hiérarchie de rôles), gardes `@PreAuthorize` des contrôleurs d'administration du module CLM, de l'intégration et de
  l'annuaire, `clm.domain.Habilitations.estAdmin`, `frontend/src/auth.jsx` (`hasRole`), menus du module
- **Origine** : addenda 4, Q10 (séparation des responsabilités et des privilèges entre administration d'affaires et
  administration technique, moindre privilège) ; Q12 (aucun mécanisme ne contourne les contrôles d'accès)

## Contexte

Un seul rôle `ADMIN` ouvrait tout : configuration des processus, intégrations, authentification unique, annuaire, et
contenu de toutes les trousses. Un administrateur technique chargé de Salesforce ou de Saviynt pouvait donc lire des
conventions d'aide financière, contrairement au moindre privilège.

## Options envisagées

1. **Remplacer `ADMIN` par deux rôles** : rompt les comptes et les intégrations existants, et oblige à attribuer deux
   rôles à qui fait les deux.
2. **Permissions fines par écran** (matrice droits × écrans) : souple mais lourde à administrer, et sans demande d'IQ.
3. **Deux rôles nouveaux, `ADMIN` les cumule par hiérarchie (retenue)** : aucun compte existant n'est touché ; chaque
   organisation peut attribuer l'un, l'autre ou les deux.

## Décision

- **Hiérarchie de rôles Spring Security :** `ROLE_ADMIN > ROLE_ADMIN_AFFAIRES` et `ROLE_ADMIN > ROLE_ADMIN_TECHNIQUE`.
  Elle s'applique aux expressions `@PreAuthorize`.
- **Administration d'affaires :**
  - messages, paramètres, listes de référence, groupes et conservation ;
  - journal d'audit du module et relevé d'utilisation (avec l'auditeur) ;
  - publication des modèles et gestion des trousses (`Habilitations.estAdmin`).
- **Administration technique :**
  - intégrations Salesforce, clients d'intégration, OIDC, SAML, Microsoft 365, SAP ;
  - annuaire et SCIM, sécurité avancée.

  `estAdmin` reste faux pour ce rôle : **aucun accès au contenu des trousses**.
- **Attribution des rôles** (`/api/admin/users`) : réservée à `ADMIN`. Un administrateur technique, qui gère pourtant
  l'annuaire, ne peut pas s'attribuer l'administration d'affaires.
- **Interface :** `hasRole` reproduit la hiérarchie côté client ; chaque entrée de menu d'administration déclare le ou
  les rôles qui l'ouvrent.

## Conséquences

- Les correspondances d'annuaire et SCIM peuvent attribuer `ADMIN_AFFAIRES` ou `ADMIN_TECHNIQUE` selon les groupes
  Entra ID d'IQ.
- Les modules hors CLM (approvisionnement, etc.) gardent `ADMIN` : ils ne relèvent pas de l'appel d'offres d'IQ.
- Comptes de démonstration : `admin.affaires@iq.example`, `admin.technique@iq.example`, `admin.ti@iq.example` (les deux).
- Tests : `Addenda4IntegrationTest.l_administration_d_affaires_et_l_administration_technique_sont_separees`.
