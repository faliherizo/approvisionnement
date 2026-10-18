# Rapport — intégration frontend des fonctionnalités (couverture des endpoints)

> Vérifie que chaque capacité exposée par le backend (contrôleurs REST) possède une interface
> correspondante côté frontend React. Méthode : recensement de tous les `@*Mapping` des contrôleurs,
> confronté à tous les appels `api.*` du frontend (`frontend/src`). Daté du 2026-09-17.

## 1. Synthèse

La couverture est désormais **complète (100 %)**. L'audit avait révélé **3 capacités** backend sans
interface ; elles sont maintenant implémentées :

| # | Capacité backend | Endpoint | État | Écran |
|---|------------------|----------|:----:|-------|
| 1 | Modifier un dossier d'approvisionnement | `PUT /api/dossiers/{id}` | ✅ implémenté | Dossiers (bouton « Modifier ») |
| 2 | Supprimer un dossier d'approvisionnement | `DELETE /api/dossiers/{id}` | ✅ implémenté | Dossiers (bouton « Supprimer » + confirmation) |
| 3 | Modifier un circuit d'approbation | `PUT /api/workflows/{id}` | ✅ implémenté | Workflows (bouton « Éditer ») |

Tout le reste était déjà intégré (voir §2). Les endpoints « GET par id » unitaires
(`/dossiers/{id}`, `/workflows/{id}`, `/gabarits/{id}`) ne sont pas appelés séparément car les
listes fournissent déjà les données ; ce ne sont pas des écarts fonctionnels.

## 2. Couverture par contexte (intégré ✅)

| Contexte | Endpoints | Écran(s) frontend |
|----------|-----------|-------------------|
| Authentification / MFA | `/api/auth/*` (config, login, logout, me, mfa/*) | Login, Securite, auth.jsx |
| Documents | CRUD, clauses éligibles/disponibles, assembler, sections, commentaires (+résolution), approbation (+décision) | Dashboard, DocumentEditor |
| Dossiers d'approvisionnement | liste, créer, statut, rattacher/détacher, documents, rapport, référence SAP | Dossiers |
| Assistant guidé | analyser, générer | Assistant |
| Clauses | liste, recherche, CRUD | Clauses |
| Bordereau / Annexes / Addendas | get/put, CRUD, changements, réintégration | DocumentEditor |
| Versions / Obligations | versions, comparer, obligations (+à venir, compléter) | DocumentEditor, Stats |
| Signature électronique | signataires, démarrer, jeton public, signer | DocumentEditor, SignaturePublique |
| Templates / Gabarits | CRUD, instancier, cloner | Templates, Gabarits, Dashboard |
| Publication M365 / SAP | publications (get/post), reference-sap | DocumentEditor, Dossiers |
| Recherche / Stats | search (facettes), stats | Search, Stats |
| Circuits d'approbation | liste, créer, supprimer | Workflows |
| Journal d'audit | consulter, exporter CSV | Audit |
| Import / Réversibilité | import clauses (essai/réel), export ZIP | DonneesIA |
| Assistant IA | rédiger, chat, paramètres (activation), suggestions, base de connaissances | ChatIA, DonneesIA, DocumentEditor |
| Administration | utilisateurs (CRUD, rôles), valeurs de référence (admin/superadmin), modèle de document, tenants, thèmes | Users, Parametres, Superadmin |
| Référence (sélecteurs) | `/api/reference/{categorie}` | useReference (transversal) |

## 3. Conception des fonctionnalités manquantes

### 3.1 Modifier un dossier (`PUT /api/dossiers/{id}`)
- **Où** : page `Dossiers`, dans le panneau de détail du dossier sélectionné.
- **UI** : un bouton « Modifier » bascule le panneau de création à droite en **mode édition**,
  pré-rempli avec les champs du dossier (titre, n° AO, objet, type d'acquisition, mode de
  sollicitation, responsable). Le bouton d'envoi devient « Enregistrer les modifications ».
  Un bouton « Annuler » revient au mode création.
- **Accessibilité** : titre de section dynamique (`aria`), champs étiquetés, focus géré.

### 3.2 Supprimer un dossier (`DELETE /api/dossiers/{id}`)
- **Où** : panneau de détail du dossier.
- **UI** : bouton « Supprimer le dossier » (style danger) avec **confirmation** avant l'appel.
  À la suppression, le dossier disparaît de la liste et les documents sont détachés (comportement
  backend), la sélection est réinitialisée.
- **Rôles** : action réservée `APPRO`/`ADMIN` (comme au backend).

### 3.3 Modifier un circuit d'approbation (`PUT /api/workflows/{id}`)
- **Où** : page `Workflows` (Circuits d'approbation).
- **UI** : bouton « Éditer » sur chaque ligne du tableau → charge le circuit dans le formulaire
  (nom, type, étapes cochées dans l'ordre). Le formulaire passe en mode édition (« Enregistrer les
  modifications » + « Annuler »).

## 4. Résultat

Les trois écarts ont été implémentés (frontend + tests e2e) :

- **Dossiers** (`Dossiers.jsx`) : mode édition pré-rempli (PUT) et suppression avec confirmation
  (DELETE, via `ConfirmModal`) dans le panneau de détail.
- **Circuits d'approbation** (`Workflows.jsx`) : bouton « Éditer » par ligne, formulaire bascule en
  mode modification (PUT) avec « Annuler ».
- **Tests e2e** : `dossiers.spec.ts` (« modifier puis supprimer un dossier ») et `gouvernance.spec.ts`
  (« éditer un circuit ») — verts.

**Conclusion : toutes les capacités du backend sont désormais exposées côté frontend — intégration
à 100 %.**
