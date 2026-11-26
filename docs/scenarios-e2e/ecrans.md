# Couverture e2e des fonctionnalités, écran par écran

> Généré par `node outils/couverture-ecrans.mjs` à partir de l'inventaire tiré du code du frontend
> (`bdd/e2e/fonctionnalites/inventaire.json`) et des traces de la dernière campagne e2e. Ne pas éditer.

**Ce que « couvert » veut dire.** Chaque écran (route de `App.jsx`) porte des fonctionnalités : lister, consulter,
ajouter, modifier, supprimer, télécharger, et les actions nommées (transférer, attribuer, approuver, signer, déléguer…).
Chacune se traduit par une opération d'API, relevée **dans le code** de l'écran et des composants qu'il rend — et non
déclarée à la main. Une fonctionnalité est couverte quand un scénario e2e l'a déclenchée **depuis l'interface**, sur un
écran qui la porte, avec une réponse en succès. Les préparations de données par l'API ne comptent pas.

| Mesure | Valeur |
|---|---|
| Écrans | 76, dont **76 ouverts** par au moins un scénario |
| Fonctionnalités (écran × opération) | 600 |
| Couvertes par un scénario | **593** |
| Sans objet (exclusion nominative et motivée) | 7 |
| **Taux** | **100.0 %** |

Les scénarios, leurs étapes et leurs données : [README.md](README.md) et `bdd/e2e/donnees/*.json`. Les exclusions et
les adresses calculées : `bdd/e2e/fonctionnalites/complements.json`.

## `/portail/:jeton` — Portail

- **Ouvert par** : E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17, E2E-CB-IQ-18, E2E-ECR-IQ-03, E2E-ECR-IQ-06, E2E-ECR-IQ-07
- **Fichiers** : `pages/clm/Portail.jsx`, `i18n/I18nContext.jsx`, `components/LangSwitcher.jsx`, `pages/clm/commun.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | consulter | `GET /clm/portail/{}` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+4) |
| `ouvrir` | consulter | `GET /clm/portail/{}/documents/{}` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+2) |
| `refuser` | refuser | `POST /clm/portail/{}/refuser` | ✅ E2E-ECR-IQ-03 |
| `signer` | signer | `POST /clm/portail/{}/signer` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+2) |
| `demarrer` | ajouter | `POST /clm/portail/{}/verification` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-18 (+1) |
| `verifier` | ajouter | `POST /clm/portail/{}/verification/reponse` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+2) |

## `/clm` — ClmTableauBord

- **Ouvert par** : E2E-AIDE-01, E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-09, E2E-AX-IQ-10, E2E-AX-IQ-11, E2E-AX-IQ-25 (+64)
- **Fichiers** : `pages/clm/ClmTableauBord.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Contenu` | lister | `GET /clm/tableau-de-bord` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-09 (+65) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-09 (+65) |

## `/clm/trousses` — Trousses

- **Ouvert par** : E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-09, E2E-CB-IQ-13, E2E-CB-IQ-16, E2E-CB-IQ-18, E2E-CB-IQ-19 (+6)
- **Fichiers** : `pages/clm/Trousses.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `exporter` | télécharger | `GET /clm/suivi/export` | ✅ E2E-ECR-IQ-06 |
| `Contenu` | lister | `GET /clm/trousses` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-09 (+10) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-09 (+10) |

## `/clm/trousses/nouvelle` — TrousseCreer

- **Ouvert par** : E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07, E2E-CB-IQ-08, E2E-CB-IQ-09, E2E-CB-IQ-10 (+12)
- **Fichiers** : `pages/clm/TrousseCreer.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `pages/clm/commun.jsx`, `pages/clm/gabarit.jsx`, `pages/clm/OngletDocuments.jsx`, `pages/clm/OngletParticipants.jsx`, `pages/clm/OngletParcours.jsx`, `pages/clm/ValeursTrousse.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /clm/trousses/{}/documents/{}` | ✅ E2E-ECR-IQ-01 |
| `retirer` | supprimer | `DELETE /clm/trousses/{}/participants/{}` | ✅ E2E-ECR-IQ-01 |
| `Contenu` | lister | `GET /clm/modeles` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `OngletParticipants` | consulter | `GET /clm/modeles/{}` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `Contenu` | lister | `GET /clm/parametres` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+17) |
| `rechercherSf` | consulter | `GET /clm/salesforce/dossiers/{}` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `Contenu` | consulter | `GET /clm/trousses/{}` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `OngletDocuments` | lister | `GET /clm/trousses/{}/analyses` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+12) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/apercu` | ✅ E2E-CB-IQ-16 |
| `charger` | télécharger | `GET /clm/trousses/{}/documents/{}/contenu` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/texte` | ✅ E2E-ECR-IQ-02 |
| `libelle` | consulter | `GET /clm/trousses/{}/suggestions/{}` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-22, E2E-ECR-IQ-02 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_CHAMP_SALESFORCE` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `useListe` | lister | `GET /reference/CLM_CLAUSE_ATTENDUE` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+12) |
| `useListe` | lister | `GET /reference/CLM_ROLE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+22) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-08, E2E-CB-IQ-04 (+34) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `reviser` | ajouter | `POST /clm/analyses/{}/revision` | ✅ E2E-ECR-IQ-06 |
| `enregistrer` | ajouter | `POST /clm/trousses` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `envoyer` | ajouter | `POST /clm/trousses/{}/documents` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `gestionnaire en ligne (onClick)` | ajouter | `POST /clm/trousses/{}/documents/{}/analyse` | ✅ E2E-ECR-IQ-06 |
| `envoyer` | ajouter | `POST /clm/trousses/{}/documents/{}/versions` | ✅ E2E-CB-IQ-16, E2E-ECR-IQ-01 |
| `gestionnaire en ligne (onClick)` | generer | `POST /clm/trousses/{}/generer` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `lancer` | lancer | `POST /clm/trousses/{}/lancer` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+8) |
| `tester` | ajouter | `POST /clm/trousses/{}/parcours/tester` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+6) |
| `ajouter` | ajouter | `POST /clm/trousses/{}/participants` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+4) |
| `importer` | ajouter | `POST /clm/trousses/{}/preremplissage` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `reassigner` | modifier | `PUT /clm/etapes/{}/affectations` | ✅ E2E-CB-IQ-07, E2E-CB-IQ-11, E2E-ECR-IQ-06 |
| `enregistrer` | modifier | `PUT /clm/trousses/{}` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+11) |
| `enregistrer` | modifier | `PUT /clm/trousses/{}/parcours` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+6) |
| `attribuer` | modifier | `PUT /clm/trousses/{}/participants/{}/role` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `enregistrer` | modifier | `PUT /clm/trousses/{}/valeurs` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-22, E2E-ECR-IQ-02 |

## `/clm/trousses/:id` — TrousseDetail

- **Ouvert par** : E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07, E2E-CB-IQ-08 (+26)
- **Fichiers** : `pages/clm/TrousseDetail.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `pages/clm/commun.jsx`, `pages/clm/gabarit.jsx`, `pages/clm/TrousseCreer.jsx`, `pages/clm/ValeursTrousse.jsx`, `pages/clm/OngletDocuments.jsx`, `pages/clm/OngletParticipants.jsx`, `pages/clm/OngletParcours.jsx`, `pages/clm/OngletPreuve.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /clm/trousses/{}/documents/{}` | ✅ E2E-ECR-IQ-01 |
| `retirer` | supprimer | `DELETE /clm/trousses/{}/participants/{}` | ✅ E2E-ECR-IQ-01 |
| `OngletParticipants` | consulter | `GET /clm/modeles/{}` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `OngletParticipants` | lister | `GET /clm/parametres` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+17) |
| `setOnglet` | consulter | `GET /clm/trousses/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-08, E2E-CB-IQ-04 (+30) |
| `OngletDocuments` | lister | `GET /clm/trousses/{}/analyses` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+12) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/apercu` | ✅ E2E-CB-IQ-16 |
| `charger` | télécharger | `GET /clm/trousses/{}/documents/{}/contenu` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/texte` | ✅ E2E-ECR-IQ-02 |
| `setOnglet` | lister | `GET /clm/trousses/{}/historique` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-CB-IQ-04, E2E-CB-IQ-06 (+11) |
| `OngletPreuve` | télécharger | `GET /clm/trousses/{}/preuve` | ✅ E2E-CB-IQ-18, E2E-ECR-IQ-01, E2E-ECR-IQ-06, E2E-IQ-11 (+2) |
| `charger` | télécharger | `GET /clm/trousses/{}/preuve/{}` | ✅ E2E-CB-IQ-18, E2E-ECR-IQ-06 |
| `libelle` | consulter | `GET /clm/trousses/{}/suggestions/{}` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-22, E2E-ECR-IQ-02 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_CHAMP_SALESFORCE` | ✅ E2E-AX-IQ-08, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+16) |
| `useListe` | lister | `GET /reference/CLM_CLAUSE_ATTENDUE` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+12) |
| `useListe` | lister | `GET /reference/CLM_ROLE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+22) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-08, E2E-CB-IQ-04 (+34) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-08, E2E-CB-IQ-04 (+30) |
| `reviser` | ajouter | `POST /clm/analyses/{}/revision` | ✅ E2E-ECR-IQ-06 |
| `relancerEtape` | relancer | `POST /clm/etapes/{}/relancer` | ✅ E2E-ECR-IQ-06 |
| `action` | ajouter | `POST /clm/trousses/{}/{}` | ✅ E2E-CB-IQ-11, E2E-ECR-IQ-06 |
| `envoyer` | ajouter | `POST /clm/trousses/{}/documents` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `gestionnaire en ligne (onClick)` | ajouter | `POST /clm/trousses/{}/documents/{}/analyse` | ✅ E2E-ECR-IQ-06 |
| `envoyer` | ajouter | `POST /clm/trousses/{}/documents/{}/versions` | ✅ E2E-CB-IQ-16, E2E-ECR-IQ-01 |
| `gestionnaire en ligne (onClick)` | generer | `POST /clm/trousses/{}/generer` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `tester` | ajouter | `POST /clm/trousses/{}/parcours/tester` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+6) |
| `ajouter` | ajouter | `POST /clm/trousses/{}/participants` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-17 (+4) |
| `verifier` | ajouter | `POST /clm/trousses/{}/preuve/integrite` | ✅ E2E-CB-IQ-18, E2E-ECR-IQ-01 |
| `reassigner` | modifier | `PUT /clm/etapes/{}/affectations` | ✅ E2E-CB-IQ-07, E2E-CB-IQ-11, E2E-ECR-IQ-06 |
| `enregistrer` | modifier | `PUT /clm/trousses/{}/parcours` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+6) |
| `attribuer` | modifier | `PUT /clm/trousses/{}/participants/{}/role` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-02 |
| `enregistrer` | modifier | `PUT /clm/trousses/{}/valeurs` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-22, E2E-ECR-IQ-02 |

## `/clm/lots` — Lots

- **Ouvert par** : E2E-CB-IQ-01, E2E-ECR-IQ-06, E2E-IQ-22
- **Fichiers** : `pages/clm/Lots.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `EcranLots` | lister | `GET /clm/lots` | ✅ E2E-CB-IQ-01, E2E-ECR-IQ-06, E2E-IQ-22 |
| `NouveauLot` | lister | `GET /clm/modeles` | ✅ E2E-CB-IQ-01, E2E-ECR-IQ-06, E2E-IQ-22 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `envoyer` | ajouter | `POST /clm/lots` | ✅ E2E-CB-IQ-01, E2E-ECR-IQ-06 |
| `approuver` | approuver | `POST /clm/lots/{}/approuver` | ✅ E2E-ECR-IQ-06 |

## `/clm/taches` — TachesClm

- **Ouvert par** : E2E-AX-IQ-10, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07, E2E-CB-IQ-08, E2E-CB-IQ-09, E2E-CB-IQ-10 (+8)
- **Fichiers** : `pages/clm/TachesClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`, `pages/clm/TachesAssignees.jsx`, `auth.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `TachesAssignees` | lister | `GET /clm/membres` | ✅ E2E-AX-IQ-10, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+12) |
| `Contenu` | lister | `GET /clm/taches` | ✅ E2E-AX-IQ-10, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+12) |
| `TachesAssignees` | lister | `GET /clm/taches-assignees` | ✅ E2E-AX-IQ-10, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+12) |
| `NouvelleTache` | lister | `GET /clm/trousses` | ✅ E2E-CB-IQ-24, E2E-ECR-IQ-06 |
| `Decision` | consulter | `GET /clm/trousses/{}` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/apercu` | ✅ E2E-CB-IQ-16 |
| `texteSeul` | télécharger | `GET /clm/trousses/{}/documents/{}/contenu` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `texteSeul` | lister | `GET /clm/trousses/{}/documents/{}/texte` | ✅ E2E-ECR-IQ-02 |
| `Decision` | lister | `GET /clm/trousses/{}/historique` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+7) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_PRIORITE_TACHE` | ✅ E2E-AX-IQ-10, E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06 (+12) |
| `commenter` | ajouter | `POST /clm/etapes/{}/commentaires` | ✅ E2E-ECR-IQ-02 |
| `decider` | ajouter | `POST /clm/etapes/{}/decider` | ✅ E2E-CB-IQ-04, E2E-CB-IQ-05, E2E-CB-IQ-06, E2E-CB-IQ-07 (+2) |
| `deleguer` | deleguer | `POST /clm/etapes/{}/deleguer` | ✅ E2E-ECR-IQ-06 |
| `lienSignature` | ajouter | `POST /clm/etapes/{}/lien-signature` | ✅ E2E-CB-IQ-09, E2E-CB-IQ-10, E2E-CB-IQ-11, E2E-CB-IQ-18 (+1) |
| `creer` | ajouter | `POST /clm/taches-assignees` | ✅ E2E-ECR-IQ-06 |
| `agir` | ajouter | `POST /clm/taches-assignees/{}/action` | ✅ E2E-ECR-IQ-06 |
| `envoyer` | reassigner | `POST /clm/taches-assignees/{}/reassigner` | ✅ E2E-ECR-IQ-06 |

## `/clm/suivi` — SuiviClm

- **Ouvert par** : E2E-AX-IQ-09, E2E-CB-IQ-04, E2E-CB-IQ-17, E2E-ECR-IQ-06, E2E-IQ-16
- **Fichiers** : `pages/clm/SuiviClm.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Contenu` | lister | `GET /clm/suivi` | ✅ E2E-AX-IQ-09, E2E-CB-IQ-04, E2E-CB-IQ-17, E2E-ECR-IQ-06 (+1) |
| `exporter` | télécharger | `GET /clm/suivi/export` | ✅ E2E-AX-IQ-09, E2E-ECR-IQ-06 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-09, E2E-CB-IQ-04, E2E-CB-IQ-17, E2E-ECR-IQ-06 (+1) |
| `relancerIntegration` | relancer | `POST /clm/admin/integrations/salesforce/trousses/{}/relancer` | ➖ n'apparaît qu'après l'échec réel d'un envoi vers un système externe (Salesforce, adresse de rappel d'un client) — couvert par ClmIntegrationTest (relance d'une trousse en erreur Salesforce) |

## `/clm/modeles` — Modeles

- **Ouvert par** : E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03, E2E-CB-IQ-14, E2E-CB-IQ-15, E2E-CB-IQ-17, E2E-CB-IQ-22 (+4)
- **Fichiers** : `pages/clm/Modeles.jsx`, `i18n/I18nContext.jsx`, `auth.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Contenu` | lister | `GET /clm/modeles` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+8) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+8) |
| `creer` | ajouter | `POST /clm/modeles` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+7) |
| `transition` | ajouter | `POST /clm/modeles/{}/{}` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-03, E2E-CB-IQ-14 (+4) |

## `/clm/modeles/:id` — ModeleEditeur

- **Ouvert par** : E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03, E2E-CB-IQ-14, E2E-CB-IQ-15, E2E-CB-IQ-17, E2E-CB-IQ-22 (+3)
- **Fichiers** : `pages/clm/ModeleEditeur.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`, `pages/clm/OngletParcours.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retirer` | supprimer | `DELETE /clm/modeles/{}/gabarit-word` | ✅ E2E-ECR-IQ-04 |
| `charger` | consulter | `GET /clm/modeles/{}` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+7) |
| `charger` | lister | `GET /clm/modeles/{}/gabarit-word` | ✅ E2E-ECR-IQ-04 |
| `valider` | lister | `GET /clm/modeles/{}/validation` | ✅ E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-15, E2E-CB-IQ-17 (+1) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_CHAMP_SALESFORCE` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+7) |
| `useListe` | lister | `GET /reference/CLM_ROLE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+22) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+7) |
| `previsualiser` | ajouter | `POST /clm/modeles/{}/apercu` | ✅ E2E-CB-IQ-01, E2E-CB-IQ-15, E2E-ECR-IQ-04 |
| `deposer` | ajouter | `POST /clm/modeles/{}/gabarit-word` | ✅ E2E-ECR-IQ-04 |
| `soumettre` | soumettre | `POST /clm/modeles/{}/soumettre` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-03, E2E-CB-IQ-14 (+4) |
| `enregistrer` | modifier | `PUT /clm/modeles/{}` | ✅ E2E-AX-IQ-02, E2E-CB-IQ-01, E2E-CB-IQ-02, E2E-CB-IQ-03 (+6) |

## `/clm/admin` — AccueilAdminClm

- **Ouvert par** : E2E-ECR-IQ-05
- **Fichiers** : `App.jsx`, `auth.jsx`
- Aucune opération d'API : écran d'affichage seul.

## `/clm/admin/notifications` — NotificationsClm

- **Ouvert par** : E2E-AIDE-01, E2E-CB-IQ-08, E2E-CB-IQ-12, E2E-ECR-IQ-05, E2E-IQ-15, E2E-IQ-35
- **Fichiers** : `pages/clm/AdminClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Notifications` | lister | `GET /clm/admin/messages` | ✅ E2E-AIDE-01, E2E-CB-IQ-08, E2E-CB-IQ-12, E2E-ECR-IQ-05 (+2) |
| `Notifications` | lister | `GET /clm/admin/parametres` | ✅ E2E-AIDE-01, E2E-CB-IQ-08, E2E-CB-IQ-12, E2E-ECR-IQ-05 (+2) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `tester` | ajouter | `POST /clm/admin/messages/{}/test` | ✅ E2E-ECR-IQ-05 |
| `enregistrer` | modifier | `PUT /clm/admin/messages` | ✅ E2E-CB-IQ-12, E2E-ECR-IQ-05 |

## `/clm/admin/integrations` — IntegrationsClm

- **Ouvert par** : E2E-AX-IQ-07, E2E-CB-IQ-17, E2E-CB-IQ-21, E2E-ECR-IQ-05, E2E-IQ-19, E2E-IQ-23, E2E-IQ-24
- **Fichiers** : `pages/clm/AdminClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retablir` | supprimer | `DELETE /clm/admin/integrations/salesforce/configuration` | ✅ E2E-ECR-IQ-05 |
| `ClientsIntegration` | lister | `GET /clm/admin/clients-integration` | ✅ E2E-AX-IQ-07, E2E-CB-IQ-17, E2E-CB-IQ-21, E2E-ECR-IQ-05 (+3) |
| `Integrations` | lister | `GET /clm/admin/integrations/salesforce` | ✅ E2E-AX-IQ-07, E2E-CB-IQ-17, E2E-CB-IQ-21, E2E-ECR-IQ-05 (+3) |
| `Integrations` | lister | `GET /clm/admin/parametres` | ✅ E2E-CB-IQ-17, E2E-CB-IQ-21, E2E-ECR-IQ-05 |
| `ClientsIntegration` | lister | `GET /clm/admin/rappels` | ✅ E2E-AX-IQ-07, E2E-CB-IQ-17, E2E-CB-IQ-21, E2E-ECR-IQ-05 (+3) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `creer` | ajouter | `POST /clm/admin/clients-integration` | ✅ E2E-AX-IQ-07, E2E-ECR-IQ-05 |
| `revoquer` | ajouter | `POST /clm/admin/clients-integration/{}/revoquer` | ✅ E2E-ECR-IQ-05 |
| `relancer` | relancer | `POST /clm/admin/integrations/salesforce/evenements/{}/relancer` | ➖ n'apparaît qu'après l'échec réel d'un envoi vers un système externe (Salesforce, adresse de rappel d'un client) — couvert par ClmIntegrationTest et EntitesClmTest.message_sortant_reprises_puis_echec_et_relance |
| `gestionnaire en ligne (onClick)` | ajouter | `POST /clm/admin/integrations/salesforce/relayer` | ✅ E2E-CB-IQ-17, E2E-ECR-IQ-05 |
| `gestionnaire en ligne (onClick)` | ajouter | `POST /clm/admin/integrations/salesforce/tester` | ✅ E2E-ECR-IQ-05 |
| `relancer` | relancer | `POST /clm/admin/rappels/{}/relancer` | ➖ n'apparaît qu'après l'échec réel d'un envoi vers un système externe (Salesforce, adresse de rappel d'un client) — couvert par IntegrationEntranteServicesTest.l_administrateur_consulte_et_relance_les_rappels et IntegrationEntranteControleursTest.l_administration_relance_un_rappel |
| `enregistrer` | modifier | `PUT /clm/admin/integrations/salesforce/configuration` | ✅ E2E-ECR-IQ-05 |

## `/clm/admin/microsoft-365` — Microsoft365Clm

- **Ouvert par** : E2E-AX-IQ-01
- **Fichiers** : `pages/clm/Microsoft365Clm.jsx`, `components/IntegrationM365Manager.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Microsoft365` | lister | `GET /clm/admin/integrations/m365` | ✅ E2E-AX-IQ-01 |
| `Microsoft365` | lister | `GET /clm/admin/integrations/m365/depots` | ✅ E2E-AX-IQ-01 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `IntegrationM365Manager` | lister | `GET /parametres/integration-m365` | ✅ E2E-AX-IQ-01, E2E-ECR-LEV-14, E2E-TR-04 |
| `relancer` | relancer | `POST /clm/admin/integrations/m365/depots/{}/relancer` | ➖ un dépôt en échec suppose un Microsoft Graph réel injoignable, qu'une campagne e2e n'appelle pas — couvert par AnnexeDixIntegrationTest.un_depot_en_panne_reprend_et_se_relance_apres_reparation |
| `relayer` | ajouter | `POST /clm/admin/integrations/m365/relayer` | ✅ E2E-AX-IQ-01 |
| `testerConnexion` | ajouter | `POST /parametres/integration-m365/test` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /clm/admin/integrations/m365` | ✅ E2E-AX-IQ-01 |
| `enregistrer` | modifier | `PUT /parametres/integration-m365` | ✅ E2E-ECR-LEV-14 |

## `/clm/admin/chiffrement` — ChiffrementClm

- **Ouvert par** : E2E-AX-IQ-25
- **Fichiers** : `pages/clm/ChiffrementClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Chiffrement` | lister | `GET /clm/admin/chiffrement` | ✅ E2E-AX-IQ-25 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `pivoter` | ajouter | `POST /clm/admin/chiffrement/pivoter` | ✅ E2E-AX-IQ-25 |
| `reenvelopper` | ajouter | `POST /clm/admin/chiffrement/reenvelopper` | ✅ E2E-AX-IQ-25 |

## `/clm/admin/purview` — PurviewClm

- **Ouvert par** : E2E-AX-IQ-21
- **Fichiers** : `pages/clm/PurviewClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retirer` | supprimer | `DELETE /clm/admin/purview/politiques/{}` | ✅ E2E-AX-IQ-21 |
| `Purview` | lister | `GET /clm/admin/purview/catalogue` | ✅ E2E-AX-IQ-21 |
| `Purview` | lister | `GET /clm/admin/purview/politiques` | ✅ E2E-AX-IQ-21 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-21 |
| `definir` | modifier | `PUT /clm/admin/purview/politiques` | ✅ E2E-AX-IQ-21 |

## `/clm/admin/soutien` — SoutienClm

- **Ouvert par** : E2E-AX-IQ-03
- **Fichiers** : `pages/clm/SoutienClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Soutien` | lister | `GET /clm/admin/soutien` | ✅ E2E-AX-IQ-03 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `decider` | ajouter | `POST /clm/admin/soutien/{}/{}` | ✅ E2E-AX-IQ-03 |

## `/clm/admin/rapports` — RapportsClm

- **Ouvert par** : E2E-AX-IQ-05
- **Fichiers** : `pages/clm/RapportsClm.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /clm/admin/rapports/{}` | ✅ E2E-AX-IQ-05 |
| `Rapports` | lister | `GET /clm/admin/rapports` | ✅ E2E-AX-IQ-05 |
| `exporter` | télécharger | `GET /clm/admin/rapports/{}/export` | ✅ E2E-AX-IQ-05 |
| `executer` | lister | `GET /clm/admin/rapports/{}/resultat` | ✅ E2E-AX-IQ-05 |
| `Rapports` | lister | `GET /clm/admin/rapports/types` | ✅ E2E-AX-IQ-05 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-AX-IQ-05 |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-05 |
| `enregistrer` | ajouter | `POST /clm/admin/rapports` | ✅ E2E-AX-IQ-05 |
| `enregistrer` | modifier | `PUT /clm/admin/rapports/{}` | ✅ E2E-AX-IQ-05 |

## `/clm/admin/parametres` — ParametresClm

- **Ouvert par** : E2E-CB-IQ-23, E2E-ECR-IQ-05, E2E-IQ-14
- **Fichiers** : `pages/clm/AdminClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retablir` | supprimer | `DELETE /clm/admin/parametres` | ✅ E2E-CB-IQ-23, E2E-ECR-IQ-05 |
| `Parametres` | lister | `GET /clm/admin/parametres` | ✅ E2E-CB-IQ-23, E2E-ECR-IQ-05, E2E-IQ-14 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `enregistrer` | modifier | `PUT /clm/admin/parametres` | ✅ E2E-CB-IQ-23, E2E-ECR-IQ-05 |

## `/clm/admin/listes` — ListesClm

- **Ouvert par** : E2E-CB-IQ-23, E2E-CB-IQ-24, E2E-ECR-IQ-05
- **Fichiers** : `pages/clm/AdminClm.jsx`, `auth.jsx`, `components/ReferenceManager.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-ECR-LEV-14 |
| `charger` | lister | `GET /admin/reference ou /superadmin/reference` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `ReferenceManager` | lister | `GET /admin/reference/categories ou /superadmin/reference/categories` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `enregistrer` | ajouter | `POST /admin/reference ou /superadmin/reference` | ✅ E2E-CB-IQ-23, E2E-CB-IQ-24, E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-CB-IQ-24, E2E-ECR-LEV-14 |

## `/clm/admin/utilisateurs` — UtilisateursClm

- **Ouvert par** : E2E-IQ-25, E2E-IQ-26
- **Fichiers** : `pages/clm/UtilisateursClm.jsx`, `i18n/I18nContext.jsx`, `pages/Users.jsx`, `auth.jsx`, `components/Modal.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /admin/users/{}` | ✅ E2E-ECR-LEV-01 |
| `modifier` | lister | `GET /admin/users` | ✅ E2E-ECR-LEV-01, E2E-IQ-25, E2E-IQ-26, E2E-TR-03 |
| `charger` | lister | `GET /admin/users/roles` | ✅ E2E-ECR-LEV-01, E2E-IQ-25, E2E-IQ-26, E2E-TR-03 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `enregistrer` | ajouter | `POST /admin/users` | ✅ E2E-ECR-LEV-01 |
| `modifier` | modifier | `PUT /admin/users/{}` | ✅ E2E-ECR-LEV-01 |

## `/clm/admin/groupes` — GroupesClm

- **Ouvert par** : E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05
- **Fichiers** : `pages/clm/UtilisateursClm.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `pages/clm/commun.jsx`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `choisirUnite` | supprimer | `DELETE /clm/admin/groupes/{}` | ✅ E2E-ECR-IQ-05 |
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /clm/admin/groupes/{}/membres/{}` | ✅ E2E-ECR-IQ-05 |
| `Groupes` | lister | `GET /clm/groupes` | ✅ E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05 |
| `Groupes` | lister | `GET /clm/membres` | ✅ E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05 |
| `creer` | ajouter | `POST /clm/admin/groupes` | ✅ E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05 |
| `choisirUnite` | ajouter | `POST /clm/admin/groupes/{}/membres` | ✅ E2E-CB-IQ-08, E2E-CB-IQ-14, E2E-ECR-IQ-05 |
| `choisirUnite` | modifier | `PUT /clm/admin/groupes/{}` | ✅ E2E-CB-IQ-08 |

## `/clm/admin/journal` — JournalClm

- **Ouvert par** : E2E-AX-IQ-06, E2E-AX-IQ-11, E2E-CB-IQ-18, E2E-ECR-IQ-05, E2E-ECR-IQ-07, E2E-IQ-17, E2E-IQ-25
- **Fichiers** : `pages/clm/JournalClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Contenu` | lister | `GET /clm/admin/journal` | ✅ E2E-AX-IQ-06, E2E-AX-IQ-11, E2E-CB-IQ-18, E2E-ECR-IQ-05 (+3) |
| `exporter` | télécharger | `GET /clm/admin/journal/export` | ✅ E2E-CB-IQ-18, E2E-ECR-IQ-07 |
| `Utilisation` | lister | `GET /clm/admin/utilisation` | ✅ E2E-AX-IQ-06, E2E-AX-IQ-11, E2E-CB-IQ-18, E2E-ECR-IQ-05 (+3) |
| `exporter` | télécharger | `GET /clm/admin/utilisation/export` | ✅ E2E-AX-IQ-06, E2E-AX-IQ-11 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_UNITE` | ✅ E2E-AX-IQ-11 |

## `/clm/admin/conservation` — ConservationClm

- **Ouvert par** : E2E-ECR-IQ-07, E2E-IQ-18
- **Fichiers** : `pages/clm/ConservationClm.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /clm/admin/conservation/regles/{}` | ✅ E2E-ECR-IQ-07 |
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /clm/admin/conservation/trousses/{}/gel` | ✅ E2E-ECR-IQ-07 |
| `EcranConservation` | lister | `GET /clm/admin/conservation/detruites` | ✅ E2E-ECR-IQ-07, E2E-IQ-18 |
| `exporter` | télécharger | `GET /clm/admin/conservation/exportation` | ✅ E2E-ECR-IQ-07 |
| `EcranConservation` | lister | `GET /clm/admin/conservation/regles` | ✅ E2E-ECR-IQ-07, E2E-IQ-18 |
| `EcranConservation` | lister | `GET /clm/admin/conservation/trousses` | ✅ E2E-ECR-IQ-07, E2E-IQ-18 |
| `certificat` | lister | `GET /clm/admin/conservation/trousses/{}/certificat-destruction` | ➖ n'existe qu'après une destruction (l'échéance de conservation (au moins un an après la clôture) ne s'atteint pas en e2e sans fausser l'horloge du serveur) — couvert par CertificatDestructionServiceTest et ClmIntegrationTest.relance_commentaire_delegation_certificat_et_journal |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-ECR-IQ-07, E2E-IQ-18 |
| `detruire` | ajouter | `POST /clm/admin/conservation/trousses/{}/detruire` | ➖ l'échéance de conservation (au moins un an après la clôture) ne s'atteint pas en e2e sans fausser l'horloge du serveur — couvert par ConservationServiceTest.echue_selon_la_regle_du_type_puis_detruite_avec_un_bordereau_sans_contenu |
| `geler` | ajouter | `POST /clm/admin/conservation/trousses/{}/gel` | ✅ E2E-ECR-IQ-07 |
| `gestionnaire en ligne (onClick)` | ajouter | `POST /clm/admin/conservation/trousses/{}/versee` | ➖ l'échéance de conservation (au moins un an après la clôture) ne s'atteint pas en e2e sans fausser l'horloge du serveur — couvert par ConservationServiceTest.sans_regle_rien_ne_se_detruit_et_un_versement_exige_le_sort_verser |
| `definir` | modifier | `PUT /clm/admin/conservation/regles` | ✅ E2E-ECR-IQ-07 |

## `/clm/admin/departs` — DepartEmploye

- **Ouvert par** : E2E-ECR-IQ-05
- **Fichiers** : `pages/clm/DepartEmploye.jsx`, `i18n/I18nContext.jsx`, `pages/clm/commun.jsx`, `hooks/useModules.js`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `consulter` | lister | `GET /clm/admin/departs` | ✅ E2E-ECR-IQ-05 |
| `EcranDepart` | lister | `GET /clm/membres` | ✅ E2E-ECR-IQ-05 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `transferer` | ajouter | `POST /clm/admin/departs` | ✅ E2E-ECR-IQ-05 |

## `/clm/options` — OptionsClm

- **Ouvert par** : E2E-ECR-IQ-05
- **Fichiers** : `pages/clm/OptionsClm.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `pages/clm/commun.jsx`, `pages/clm/gabarit.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Contenu` | lister | `GET /clm/analyses` | ✅ E2E-ECR-IQ-05 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useListe` | consulter | `GET /reference/{}` | ✅ E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-05, E2E-AX-IQ-08 (+70) |
| `useListe` | lister | `GET /reference/CLM_TYPE_CONTRAT` | ✅ E2E-ECR-IQ-05 |

## `/clm/profil` — Profil

- **Ouvert par** : E2E-ECR-IQ-05
- **Fichiers** : `pages/Profil.jsx`, `components/RepertoireDepotManager.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retirer` | supprimer | `DELETE /profil/repertoires/{}` | ✅ E2E-ECR-LEV-09 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `Profil` | lister | `GET /profil` | ✅ E2E-ECR-IQ-05, E2E-ECR-LEV-09 |
| `charger` | lister | `GET /profil/repertoires` | ✅ E2E-ECR-LEV-09 |
| `ajouter` | ajouter | `POST /profil/repertoires` | ✅ E2E-ECR-LEV-09 |
| `enregistrer` | modifier | `PUT /profil` | ✅ E2E-ECR-LEV-09 |

## `/clm/securite` — Securite

- **Ouvert par** : E2E-ECR-IQ-05, E2E-ECR-IQ-07
- **Fichiers** : `pages/Securite.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /auth/me` | ✅ E2E-CHU-17, E2E-ECR-IQ-05, E2E-ECR-IQ-07, E2E-ECR-LEV-10 (+1) |
| `desactiver` | ajouter | `POST /auth/mfa/disable` | ✅ E2E-ECR-LEV-10 |
| `activerQuestion` | ajouter | `POST /auth/mfa/question/setup` | ✅ E2E-ECR-LEV-10 |
| `activerTotp` | ajouter | `POST /auth/mfa/totp/enable` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-10 |
| `demarrerTotp` | ajouter | `POST /auth/mfa/totp/setup` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-10 |

## `/clm/admin/sso` — SsoClm

- **Ouvert par** : E2E-ECR-IQ-07, E2E-IQ-27, E2E-IQ-28, E2E-IQ-29
- **Fichiers** : `pages/clm/UtilisateursClm.jsx`, `i18n/I18nContext.jsx`, `components/IntegrationOidcManager.jsx`, `components/IntegrationSamlManager.jsx`, `components/AnnuaireManager.jsx`, `pages/clm/commun.jsx`, `pages/clm/gabarit.jsx`, `components/ConnexionLocaleManager.jsx`, `components/PolitiqueSessionManager.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `gestionnaire en ligne (onClick)` | supprimer | `DELETE /admin/annuaire/correspondances/{}` | ✅ E2E-ECR-IQ-07 |
| `AnnuaireManager` | lister | `GET /admin/annuaire/correspondances` | ✅ E2E-ECR-IQ-07, E2E-IQ-27, E2E-IQ-28, E2E-IQ-29 |
| `AnnuaireManager` | lister | `GET /admin/annuaire/scim` | ✅ E2E-ECR-IQ-07, E2E-IQ-27, E2E-IQ-28, E2E-IQ-29 |
| `ConnexionLocaleManager` | lister | `GET /admin/connexion-locale` | ✅ E2E-ECR-IQ-07, E2E-IQ-27, E2E-IQ-28, E2E-IQ-29 |
| `PolitiqueSessionManager` | lister | `GET /admin/session` | ✅ E2E-ECR-IQ-07, E2E-IQ-27, E2E-IQ-28, E2E-IQ-29 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `IntegrationOidcManager` | lister | `GET /parametres/integration-oidc` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-14, E2E-IQ-27, E2E-IQ-28 (+2) |
| `IntegrationSamlManager` | lister | `GET /parametres/integration-saml` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-14, E2E-IQ-27, E2E-IQ-28 (+2) |
| `ajouter` | ajouter | `POST /admin/annuaire/correspondances` | ✅ E2E-ECR-IQ-07 |
| `genererJeton` | ajouter | `POST /admin/annuaire/scim/jeton` | ✅ E2E-ECR-IQ-07 |
| `testerDecouverte` | ajouter | `POST /parametres/integration-oidc/test` | ✅ E2E-ECR-LEV-14 |
| `testerConfig` | ajouter | `POST /parametres/integration-saml/test` | ✅ E2E-ECR-LEV-14 |
| `reserver` | modifier | `PUT /admin/connexion-locale` | ✅ E2E-ECR-IQ-07 |
| `gestionnaire en ligne (onChange)` | modifier | `PUT /admin/connexion-locale/comptes/{}` | ✅ E2E-ECR-IQ-07 |
| `enregistrer` | modifier | `PUT /admin/session` | ✅ E2E-IQ-29 |
| `enregistrer` | modifier | `PUT /parametres/integration-oidc` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /parametres/integration-saml` | ✅ E2E-ECR-LEV-14 |

## `/clm/admin/audit` — Audit

- **Ouvert par** : E2E-ECR-IQ-05
- **Fichiers** : `pages/Audit.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /audit` | ✅ E2E-ECR-IQ-05, E2E-LEV-06, E2E-TR-05 |
| `exporter` | télécharger | `GET /audit/export` | ✅ E2E-TR-05 |

## `/accueil` — Vitrine

- **Ouvert par** : E2E-ECR-LEV-12, E2E-VIT-01
- **Fichiers** : `pages/Vitrine.jsx`, `i18n/I18nContext.jsx`, `components/ChoixOrganisation.jsx`, `components/Modal.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `ChoixOrganisation` | lister | `GET /tenants` | ✅ E2E-ECR-LEV-12 |
| `choisir` | consulter | `GET /vitrines/{}` | ✅ E2E-VIT-01 |

## `/login` — Login

- **Ouvert par** : E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-AX-IQ-05, E2E-AX-IQ-06 (+139)
- **Fichiers** : `pages/Login.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `components/LangSwitcher.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Login` | lister | `GET /auth/config` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `Login` | lister | `GET /tenants/{}/public` | ✅ E2E-ECR-LEV-01, E2E-ECR-LEV-02, E2E-ECR-LEV-03, E2E-ECR-LEV-04 (+17) |

## `/signature/:jeton` — SignaturePublique

- **Ouvert par** : E2E-ECR-LEV-17
- **Fichiers** : `pages/SignaturePublique.jsx`, `components/SignaturePad.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `SignaturePublique` | consulter | `GET /signature/{}` | ✅ E2E-ECR-LEV-17 |
| `signer` | signer | `POST /signature/{}/signer` | ✅ E2E-ECR-LEV-17 |

## `/` — RacineRoute

- **Ouvert par** : E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02, E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-AX-IQ-05, E2E-AX-IQ-06 (+139)
- **Fichiers** : `App.jsx`, `auth.jsx`, `hooks/useModules.js`, `pages/Dashboard.jsx`, `components/RichText.jsx`, `i18n/I18nContext.jsx`, `hooks/useReference.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /documents` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/MODE_SOLLICITATION` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `charger` | lister | `GET /templates` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `creer` | ajouter | `POST /documents` | ✅ E2E-ECR-LEV-08 |
| `instancier` | ajouter | `POST /templates/{}/instancier` | ✅ E2E-ECR-LEV-08 |

## `/clauses` — Clauses

- **Ouvert par** : E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01
- **Fichiers** : `pages/Clauses.jsx`, `auth.jsx`, `components/AiAssistant.jsx`, `i18n/I18nContext.jsx`, `components/ClauseHistorique.jsx`, `components/Modal.jsx`, `components/RichText.jsx`, `hooks/useReference.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /clauses/{}` | ✅ E2E-ECR-LEV-02 |
| `charger` | consulter | `GET /clauses/{}` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01 |
| `ClauseHistorique` | lister | `GET /clauses/{}/versions` | ✅ E2E-ECR-LEV-02, E2E-LEV-01 |
| `charger` | lister | `GET /clauses/recherche` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/CATEGORIE_CLAUSE` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01 |
| `useReference` | lister | `GET /reference/MODE_SOLLICITATION` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01 |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16, E2E-LEV-01 |
| `enregistrer` | ajouter | `POST /clauses` | ✅ E2E-LEV-01 |
| `restaurer` | restaurer | `POST /clauses/{}/versions/{}/restaurer` | ✅ E2E-ECR-LEV-02 |
| `generer` | ajouter | `POST /ia/rediger` | ✅ E2E-ECR-LEV-16 |
| `enregistrer` | modifier | `PUT /clauses/{}` | ✅ E2E-ECR-LEV-02, E2E-ECR-LEV-16 |

## `/dossiers` — Dossiers

- **Ouvert par** : E2E-ECR-LEV-05, E2E-TR-08
- **Fichiers** : `pages/Dossiers.jsx`, `hooks/useReference.js`, `components/Modal.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimerDossier` | supprimer | `DELETE /dossiers/{}` | ✅ E2E-ECR-LEV-05 |
| `detacher` | supprimer | `DELETE /dossiers/{}/documents/{}` | ✅ E2E-ECR-LEV-05 |
| `charger` | lister | `GET /documents` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `charger` | lister | `GET /dossiers` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `ouvrir` | lister | `GET /dossiers/{}/documents` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `ouvrir` | lister | `GET /dossiers/{}/rapport` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/MODE_SOLLICITATION` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `soumettre` | ajouter | `POST /dossiers` | ✅ E2E-ECR-LEV-05, E2E-TR-08 |
| `rattacher` | ajouter | `POST /dossiers/{}/documents/{}` | ✅ E2E-ECR-LEV-05 |
| `transmettreSap` | ajouter | `POST /dossiers/{}/reference-sap` | ✅ E2E-ECR-LEV-05 |
| `changerStatut` | ajouter | `POST /dossiers/{}/statut` | ✅ E2E-ECR-LEV-05 |
| `soumettre` | modifier | `PUT /dossiers/{}` | ✅ E2E-ECR-LEV-05 |

## `/assistant` — Assistant

- **Ouvert par** : E2E-LEV-03, E2E-LEV-04
- **Fichiers** : `pages/Assistant.jsx`, `hooks/useReference.js`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Assistant` | lister | `GET /dossiers` | ✅ E2E-LEV-03, E2E-LEV-04 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-LEV-03, E2E-LEV-04 |
| `analyser` | ajouter | `POST /assistant/analyser` | ✅ E2E-LEV-03, E2E-LEV-04 |
| `generer` | generer | `POST /assistant/generer` | ✅ E2E-LEV-03, E2E-LEV-04 |

## `/workflows` — Workflows

- **Ouvert par** : E2E-ECR-LEV-04, E2E-LEV-05, E2E-TR-10
- **Fichiers** : `pages/Workflows.jsx`, `auth.jsx`, `components/Toast.jsx`, `hooks/useReference.js`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /workflows/{}` | ✅ E2E-ECR-LEV-04, E2E-LEV-05, E2E-TR-10 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-ECR-LEV-04, E2E-LEV-05, E2E-TR-10 |
| `charger` | lister | `GET /workflows` | ✅ E2E-ECR-LEV-04, E2E-LEV-05, E2E-TR-10 |
| `soumettre` | ajouter | `POST /workflows` | ✅ E2E-ECR-LEV-04, E2E-LEV-05, E2E-TR-10 |
| `soumettre` | modifier | `PUT /workflows/{}` | ✅ E2E-ECR-LEV-04 |

## `/audit` — Audit

- **Ouvert par** : E2E-LEV-06, E2E-TR-05
- **Fichiers** : `pages/Audit.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /audit` | ✅ E2E-ECR-IQ-05, E2E-LEV-06, E2E-TR-05 |
| `exporter` | télécharger | `GET /audit/export` | ✅ E2E-TR-05 |

## `/donnees` — DonneesIA

- **Ouvert par** : E2E-CHU-21, E2E-ECR-LEV-11, E2E-TR-01, E2E-TR-06, E2E-TR-07
- **Fichiers** : `pages/DonneesIA.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `components/Toast.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `chargerIa` | lister | `GET /ia/parametres` | ✅ E2E-CHU-21, E2E-ECR-LEV-11, E2E-TR-01, E2E-TR-06 (+1) |
| `chargerIa` | lister | `GET /ia/suggestions` | ✅ E2E-CHU-21, E2E-ECR-LEV-11, E2E-TR-01, E2E-TR-06 (+1) |
| `exporterReversibilite` | télécharger | `GET /reversibilite/export` | ✅ E2E-TR-07 |
| `importer` | ajouter | `POST /import/clauses` | ✅ E2E-ECR-LEV-11 |
| `importerWord` | ajouter | `POST /import/word` | ✅ E2E-TR-01 |
| `basculerIa` | modifier | `PUT /ia/parametres` | ✅ E2E-TR-06 |

## `/documents/:id` — DocumentEditor

- **Ouvert par** : E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03, E2E-LEV-04, E2E-LEV-06, E2E-LEV-07, E2E-TR-02 (+1)
- **Fichiers** : `pages/DocumentEditor.jsx`, `components/AiAssistant.jsx`, `i18n/I18nContext.jsx`, `components/DropdownMenu.jsx`, `components/Modal.jsx`, `components/RichText.jsx`, `components/SignaturePad.jsx`, `components/Toast.jsx`, `hooks/useReference.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimerAnnexe` | supprimer | `DELETE /documents/{}/annexes/{}` | ✅ E2E-ECR-LEV-13 |
| `supprimerSection` | supprimer | `DELETE /documents/{}/clauses/{}` | ✅ E2E-ECR-LEV-13 |
| `recharger` | consulter | `GET /documents/{}` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/addendas` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/annexes` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/approbation` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/bordereau` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/clauses-disponibles` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/commentaires` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `exporter` | télécharger | `GET /documents/{}/export/{}` | ✅ E2E-ECR-LEV-13, E2E-TR-02 |
| `ouvrirApercu` | télécharger | `GET /documents/{}/export/pdf/apercu-image` | ✅ E2E-ECR-LEV-13 |
| `exporterSeao` | télécharger | `GET /documents/{}/export/seao` | ✅ E2E-ECR-LEV-13 |
| `recharger` | lister | `GET /documents/{}/obligations` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/publications` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/signataires` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `recharger` | lister | `GET /documents/{}/versions` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `enregistrerChangement` | lister | `GET /gabarits` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `chargerRepertoires` | lister | `GET /profil/repertoires` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/TYPE_OBLIGATION` | ✅ E2E-ECR-LEV-08, E2E-ECR-LEV-13, E2E-ECR-LEV-16, E2E-LEV-03 (+5) |
| `comparer` | lister | `GET /versions/comparer` | ✅ E2E-ECR-LEV-13 |
| `enregistrerChangement` | ajouter | `POST /addendas/{}/changements` | ✅ E2E-ECR-LEV-13 |
| `reintegrerAddenda` | ajouter | `POST /addendas/{}/reintegrer` | ✅ E2E-ECR-LEV-13 |
| `soumettreModale` | ajouter | `POST /documents/{}/addendas` | ✅ E2E-ECR-LEV-13, E2E-LEV-07 |
| `soumettreModale` | ajouter | `POST /documents/{}/annexes` | ✅ E2E-ECR-LEV-13 |
| `demarrerApprobation` | ajouter | `POST /documents/{}/approbation` | ✅ E2E-ECR-LEV-13, E2E-LEV-06 |
| `assembler` | assembler | `POST /documents/{}/assembler` | ✅ E2E-ECR-LEV-13 |
| `enregistrerRedaction` | ajouter | `POST /documents/{}/clauses` | ✅ E2E-ECR-LEV-13 |
| `commenter` | ajouter | `POST /documents/{}/commentaires` | ✅ E2E-ECR-LEV-13, E2E-LEV-06 |
| `resoudreCommentaire` | ajouter | `POST /documents/{}/commentaires/{}/resolution` | ✅ E2E-ECR-LEV-13 |
| `soumettreModale` | ajouter | `POST /documents/{}/obligations` | ✅ E2E-ECR-LEV-13 |
| `publier` | ajouter | `POST /documents/{}/publications` | ✅ E2E-ECR-LEV-13 |
| `soumettreModale` | ajouter | `POST /documents/{}/signataires` | ✅ E2E-ECR-LEV-13 |
| `demarrerSignature` | demarrer | `POST /documents/{}/signature/demarrer` | ✅ E2E-ECR-LEV-13 |
| `soumettreModale` | ajouter | `POST /documents/{}/versions` | ✅ E2E-ECR-LEV-13, E2E-TR-05 |
| `decider` | ajouter | `POST /documents/approbation/{}/decision` | ✅ E2E-ECR-LEV-13 |
| `generer` | ajouter | `POST /ia/rediger` | ✅ E2E-ECR-LEV-16 |
| `completerObligation` | ajouter | `POST /obligations/{}/completer` | ✅ E2E-ECR-LEV-13 |
| `enregistrerRepertoire` | ajouter | `POST /profil/repertoires` | ✅ E2E-ECR-LEV-13 |
| `signerManuel` | signer | `POST /signature/{}/signer` | ✅ E2E-ECR-LEV-13 |
| `enregistrerBordereau` | modifier | `PUT /documents/{}/bordereau` | ✅ E2E-ECR-LEV-13 |
| `enregistrerRedaction` | modifier | `PUT /documents/{}/clauses/{}` | ✅ E2E-ECR-LEV-13 |

## `/recherche` — Search

- **Ouvert par** : E2E-ECR-LEV-03, E2E-LEV-02
- **Fichiers** : `pages/Search.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`, `hooks/useReference.js`, `components/Modal.jsx`, `components/RichText.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /clauses/{}` | ✅ E2E-ECR-LEV-03 |
| `supprimer` | supprimer | `DELETE /documents/{}` | ✅ E2E-ECR-LEV-03 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-ECR-LEV-03, E2E-LEV-02 |
| `rechercher` | lister | `GET /search` | ✅ E2E-ECR-LEV-03, E2E-LEV-02 |
| `Search` | lister | `GET /search/thematiques` | ✅ E2E-ECR-LEV-03, E2E-LEV-02 |

## `/tableau-de-bord` — Stats

- **Ouvert par** : E2E-TR-09
- **Fichiers** : `pages/Stats.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Stats` | lister | `GET /obligations/a-venir` | ✅ E2E-TR-09 |
| `Stats` | lister | `GET /stats` | ✅ E2E-TR-09 |

## `/rapports` — Rapports

- **Ouvert par** : E2E-TR-09
- **Fichiers** : `pages/Rapports.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `Rapports` | lister | `GET /rapports/suivi` | ✅ E2E-TR-09 |

## `/chat-ia` — ChatIA

- **Ouvert par** : E2E-ECR-LEV-16
- **Fichiers** : `pages/ChatIA.jsx`, `components/RichText.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimerDoc` | supprimer | `DELETE /ia/knowledge/{}` | ✅ E2E-ECR-LEV-16 |
| `chargerKnowledge` | lister | `GET /ia/knowledge` | ✅ E2E-ECR-LEV-16 |
| `envoyer` | ajouter | `POST /ia/chat` | ✅ E2E-ECR-LEV-16 |
| `ajouterDoc` | ajouter | `POST /ia/knowledge` | ✅ E2E-ECR-LEV-16 |

## `/simulateur` — SimulateurDepots

- **Ouvert par** : E2E-ECR-LEV-12
- **Fichiers** : `pages/SimulateurDepots.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /simulateur/depots` | ✅ E2E-ECR-LEV-12 |

## `/utilisateurs` — Users

- **Ouvert par** : E2E-ECR-LEV-01, E2E-TR-03
- **Fichiers** : `pages/Users.jsx`, `auth.jsx`, `components/Modal.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /admin/users/{}` | ✅ E2E-ECR-LEV-01 |
| `charger` | lister | `GET /admin/users` | ✅ E2E-ECR-LEV-01, E2E-IQ-25, E2E-IQ-26, E2E-TR-03 |
| `charger` | lister | `GET /admin/users/roles` | ✅ E2E-ECR-LEV-01, E2E-IQ-25, E2E-IQ-26, E2E-TR-03 |
| `enregistrer` | ajouter | `POST /admin/users` | ✅ E2E-ECR-LEV-01 |
| `enregistrer` | modifier | `PUT /admin/users/{}` | ✅ E2E-ECR-LEV-01 |

## `/vie-privee` — VieePrivee

- **Ouvert par** : E2E-ECR-LEV-11, E2E-LEV-08
- **Fichiers** : `pages/VieePrivee.jsx`, `components/Modal.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `chargerRegistre` | lister | `GET /loi25/demandes` | ✅ E2E-ECR-LEV-11, E2E-LEV-08 |
| `consulter` | ajouter | `POST /loi25/acces` | ✅ E2E-LEV-08 |
| `effacer` | ajouter | `POST /loi25/effacement` | ✅ E2E-ECR-LEV-11 |

## `/messagerie` — Messagerie

- **Ouvert par** : E2E-CHU-09, E2E-ECR-CHU-07
- **Fichiers** : `pages/Messagerie.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /messagerie/conversations` | ✅ E2E-CHU-09, E2E-ECR-CHU-07 |
| `ouvrir` | consulter | `GET /messagerie/conversations/{}` | ✅ E2E-ECR-CHU-07 |
| `creer` | ajouter | `POST /messagerie/conversations` | ✅ E2E-ECR-CHU-07 |
| `repondre` | ajouter | `POST /messagerie/conversations/{}/messages` | ✅ E2E-ECR-CHU-07 |
| `ajouter` | ajouter | `POST /messagerie/conversations/{}/participants` | ✅ E2E-ECR-CHU-07 |

## `/tableaux-de-bord` — TableauxBord

- **Ouvert par** : E2E-CHU-11, E2E-ECR-CHU-07
- **Fichiers** : `pages/TableauxBord.jsx`, `components/GraphiqueIndicateur.jsx`, `i18n/I18nContext.jsx`, `components/Modal.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /rapport/tableaux-de-bord/{}` | ✅ E2E-ECR-CHU-07 |
| `charger` | lister | `GET /catalogue` | ✅ E2E-CHU-11, E2E-ECR-CHU-07 |
| `charger` | lister | `GET /rapport/tableaux-de-bord` | ✅ E2E-CHU-11, E2E-ECR-CHU-07 |
| `ouvrir` | consulter | `GET /rapport/tableaux-de-bord/{}` | ✅ E2E-ECR-CHU-07 |
| `enregistrer` | ajouter | `POST /rapport/tableaux-de-bord` | ✅ E2E-ECR-CHU-07 |
| `enregistrer` | modifier | `PUT /rapport/tableaux-de-bord/{}` | ✅ E2E-ECR-CHU-07 |

## `/banque-heures` — BanqueHeures

- **Ouvert par** : E2E-CHU-12, E2E-ECR-LEV-15
- **Fichiers** : `pages/BanqueHeures.jsx`, `components/ReleveBanque.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `BanqueHeures` | lister | `GET /soutien/banque-heures` | ✅ E2E-CHU-12, E2E-ECR-LEV-15 |
| `BanqueHeures` | consulter | `GET /soutien/banque-heures/{}` | ✅ E2E-CHU-12, E2E-ECR-LEV-15 |
| `releve` | télécharger | `GET /soutien/banque-heures/{}/releve.csv` | ✅ E2E-ECR-LEV-15 |

## `/gabarits` — Gabarits

- **Ouvert par** : E2E-ECR-LEV-07
- **Fichiers** : `pages/Gabarits.jsx`, `auth.jsx`, `components/GabaritManager.jsx`, `components/Modal.jsx`, `i18n/I18nContext.jsx`, `components/Toast.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `confirmerSuppression` | supprimer | `DELETE /gabarits/{}` | ✅ E2E-ECR-LEV-07 |
| `charger` | lister | `GET /gabarits` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-07, E2E-ECR-LEV-15 (+1) |
| `enregistrer` | ajouter | `POST /gabarits` | ✅ E2E-ECR-LEV-07 |
| `cloner` | ajouter | `POST /gabarits/{}/cloner` | ✅ E2E-ECR-LEV-07 |
| `enregistrer` | modifier | `PUT /gabarits/{}` | ✅ E2E-ECR-LEV-07 |

## `/templates` — Templates

- **Ouvert par** : E2E-ECR-LEV-06
- **Fichiers** : `pages/Templates.jsx`, `auth.jsx`, `components/TemplateManager.jsx`, `components/Modal.jsx`, `i18n/I18nContext.jsx`, `components/RichText.jsx`, `components/Toast.jsx`, `hooks/useReference.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `confirmerSuppression` | supprimer | `DELETE /templates/{}` | ✅ E2E-ECR-LEV-06 |
| `charger` | lister | `GET /gabarits` | ✅ E2E-ECR-LEV-06 |
| `useReference` | consulter | `GET /reference/{}` | ✅ E2E-AIDE-02, E2E-AX-IQ-03, E2E-CHU-02, E2E-CHU-03 (+57) |
| `useReference` | lister | `GET /reference/MODE_SOLLICITATION` | ✅ E2E-ECR-LEV-06 |
| `useReference` | lister | `GET /reference/TYPE_CONTRAT` | ✅ E2E-ECR-LEV-06 |
| `charger` | lister | `GET /templates` | ✅ E2E-ECR-LEV-06 |
| `enregistrer` | ajouter | `POST /templates` | ✅ E2E-ECR-LEV-06 |
| `enregistrer` | modifier | `PUT /templates/{}` | ✅ E2E-ECR-LEV-06 |

## `/parametres` — Parametres

- **Ouvert par** : E2E-ECR-LEV-14, E2E-TR-04
- **Fichiers** : `pages/Parametres.jsx`, `auth.jsx`, `hooks/useModules.js`, `i18n/I18nContext.jsx`, `components/ReferenceManager.jsx`, `components/ModeleDocumentManager.jsx`, `components/IntegrationM365Manager.jsx`, `components/IntegrationSapManager.jsx`, `components/IntegrationOidcManager.jsx`, `components/IntegrationSamlManager.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-ECR-LEV-14 |
| `charger` | lister | `GET /admin/reference ou /superadmin/reference` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `ReferenceManager` | lister | `GET /admin/reference/categories ou /superadmin/reference/categories` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `baseUrl` | lister | `GET /modele-document ou /superadmin/tenants/{}/modele-document` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-14, E2E-ECR-LEV-15 (+2) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `IntegrationM365Manager` | lister | `GET /parametres/integration-m365` | ✅ E2E-AX-IQ-01, E2E-ECR-LEV-14, E2E-TR-04 |
| `IntegrationOidcManager` | lister | `GET /parametres/integration-oidc` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-14, E2E-IQ-27, E2E-IQ-28 (+2) |
| `IntegrationSamlManager` | lister | `GET /parametres/integration-saml` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-14, E2E-IQ-27, E2E-IQ-28 (+2) |
| `IntegrationSapManager` | lister | `GET /parametres/integration-sap` | ✅ E2E-ECR-LEV-14, E2E-TR-04 |
| `baseUrl` | lister | `GET /superadmin/tenants` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-15, E2E-VIT-01 |
| `enregistrer` | ajouter | `POST /admin/reference ou /superadmin/reference` | ✅ E2E-CB-IQ-23, E2E-CB-IQ-24, E2E-ECR-LEV-14 |
| `testerConnexion` | ajouter | `POST /parametres/integration-m365/test` | ✅ E2E-ECR-LEV-14 |
| `testerDecouverte` | ajouter | `POST /parametres/integration-oidc/test` | ✅ E2E-ECR-LEV-14 |
| `testerConfig` | ajouter | `POST /parametres/integration-saml/test` | ✅ E2E-ECR-LEV-14 |
| `testerConnexion` | ajouter | `POST /parametres/integration-sap/test` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-CB-IQ-24, E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /modele-document ou /superadmin/tenants/{}/modele-document` | ✅ E2E-ECR-LEV-14, E2E-ECR-LEV-15 |
| `enregistrer` | modifier | `PUT /parametres/integration-m365` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /parametres/integration-oidc` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /parametres/integration-saml` | ✅ E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /parametres/integration-sap` | ✅ E2E-ECR-LEV-14 |

## `/securite` — Securite

- **Ouvert par** : E2E-CHU-17, E2E-ECR-LEV-10, E2E-TR-03
- **Fichiers** : `pages/Securite.jsx`, `i18n/I18nContext.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /auth/me` | ✅ E2E-CHU-17, E2E-ECR-IQ-05, E2E-ECR-IQ-07, E2E-ECR-LEV-10 (+1) |
| `desactiver` | ajouter | `POST /auth/mfa/disable` | ✅ E2E-ECR-LEV-10 |
| `activerQuestion` | ajouter | `POST /auth/mfa/question/setup` | ✅ E2E-ECR-LEV-10 |
| `activerTotp` | ajouter | `POST /auth/mfa/totp/enable` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-10 |
| `demarrerTotp` | ajouter | `POST /auth/mfa/totp/setup` | ✅ E2E-ECR-IQ-07, E2E-ECR-LEV-10 |

## `/profil` — Profil

- **Ouvert par** : E2E-ECR-LEV-09
- **Fichiers** : `pages/Profil.jsx`, `components/RepertoireDepotManager.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `retirer` | supprimer | `DELETE /profil/repertoires/{}` | ✅ E2E-ECR-LEV-09 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `Profil` | lister | `GET /profil` | ✅ E2E-ECR-IQ-05, E2E-ECR-LEV-09 |
| `charger` | lister | `GET /profil/repertoires` | ✅ E2E-ECR-LEV-09 |
| `ajouter` | ajouter | `POST /profil/repertoires` | ✅ E2E-ECR-LEV-09 |
| `enregistrer` | modifier | `PUT /profil` | ✅ E2E-ECR-LEV-09 |

## `/api-docs` — ApiDocs

- **Ouvert par** : E2E-TR-11
- **Fichiers** : `pages/ApiDocs.jsx`, `auth.jsx`, `i18n/I18nContext.jsx`
- Aucune opération d'API : écran d'affichage seul.

## `/superadmin` — Superadmin

- **Ouvert par** : E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-12, E2E-ECR-LEV-15, E2E-VIT-01
- **Fichiers** : `pages/Superadmin.jsx`, `auth.jsx`, `components/ReferenceManager.jsx`, `i18n/I18nContext.jsx`, `components/ModeleDocumentManager.jsx`, `components/TarifTenantManager.jsx`, `components/GabaritManager.jsx`, `components/Modal.jsx`, `components/Toast.jsx`, `components/ModuleManager.jsx`, `components/BanqueHeuresManager.jsx`, `components/ReleveBanque.jsx`, `components/SoutienClmManager.jsx`, `components/EditeurVitrine.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-ECR-LEV-14 |
| `confirmerSuppression` | supprimer | `DELETE /gabarits/{}` | ✅ E2E-ECR-LEV-07 |
| `charger` | lister | `GET /admin/reference ou /superadmin/reference` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `ReferenceManager` | lister | `GET /admin/reference/categories ou /superadmin/reference/categories` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-CB-IQ-23, E2E-CB-IQ-24 (+5) |
| `charger` | lister | `GET /gabarits` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-07, E2E-ECR-LEV-15 (+1) |
| `baseUrl` | lister | `GET /modele-document ou /superadmin/tenants/{}/modele-document` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-14, E2E-ECR-LEV-15 (+2) |
| `chargerMediatheque` | lister | `GET /superadmin/mediatheque` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-15, E2E-VIT-01 |
| `charger` | lister | `GET /superadmin/tenants` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04, E2E-ECR-LEV-15, E2E-VIT-01 |
| `choisirTenant` | lister | `GET /superadmin/tenants/{}/banques-heures` | ✅ E2E-ECR-LEV-15 |
| `gestionnaire en ligne (onClick)` | consulter | `GET /superadmin/tenants/{}/banques-heures/{}` | ✅ E2E-ECR-LEV-15 |
| `charger` | lister | `GET /superadmin/tenants/{}/modules` | ✅ E2E-ECR-LEV-15 |
| `recharger` | lister | `GET /superadmin/tenants/{}/soutien-clm/demandes` | ✅ E2E-AX-IQ-03, E2E-AX-IQ-04 |
| `consulter` | consulter | `GET /superadmin/tenants/{}/soutien-clm/trousses/{}` | ✅ E2E-AX-IQ-03 |
| `TarifTenantManager` | lister | `GET /superadmin/tenants/{}/tarif` | ✅ E2E-ECR-LEV-15 |
| `chargerMediatheque` | lister | `GET /superadmin/tenants/{}/vitrine` | ✅ E2E-VIT-01 |
| `enregistrer` | ajouter | `POST /admin/reference ou /superadmin/reference` | ✅ E2E-CB-IQ-23, E2E-CB-IQ-24, E2E-ECR-LEV-14 |
| `enregistrer` | ajouter | `POST /gabarits` | ✅ E2E-ECR-LEV-07 |
| `cloner` | ajouter | `POST /gabarits/{}/cloner` | ✅ E2E-ECR-LEV-07 |
| `creerTenant` | ajouter | `POST /superadmin/tenants` | ✅ E2E-ECR-LEV-15 |
| `creerAdmin` | ajouter | `POST /superadmin/tenants/{}/admin` | ✅ E2E-ECR-LEV-15 |
| `gestionnaire en ligne (onSubmit)` | ajouter | `POST /superadmin/tenants/{}/banques-heures` | ✅ E2E-ECR-LEV-15 |
| `gestionnaire en ligne (onSubmit)` | ajouter | `POST /superadmin/tenants/{}/banques-heures/{}/mouvements` | ✅ E2E-ECR-LEV-15 |
| `demander` | ajouter | `POST /superadmin/tenants/{}/soutien-clm/demandes` | ✅ E2E-AX-IQ-04 |
| `televerser` | ajouter | `POST /superadmin/tenants/{}/vitrine/video` | ✅ E2E-VIT-01 |
| `enregistrer` | modifier | `PUT /admin/reference/{} ou /superadmin/reference/{}` | ✅ E2E-CB-IQ-24, E2E-ECR-LEV-14 |
| `enregistrer` | modifier | `PUT /gabarits/{}` | ✅ E2E-ECR-LEV-07 |
| `enregistrer` | modifier | `PUT /modele-document ou /superadmin/tenants/{}/modele-document` | ✅ E2E-ECR-LEV-14, E2E-ECR-LEV-15 |
| `basculerActif` | modifier | `PUT /superadmin/tenants/{}` | ✅ E2E-ECR-LEV-15 |
| `basculer` | modifier | `PUT /superadmin/tenants/{}/modules/{}/{}` | ✅ E2E-ECR-LEV-15 |
| `enregistrer` | modifier | `PUT /superadmin/tenants/{}/tarif` | ✅ E2E-ECR-LEV-15 |
| `enregistrerTheme` | modifier | `PUT /superadmin/tenants/{}/theme` | ✅ E2E-ECR-LEV-15 |
| `enregistrer` | modifier | `PUT /superadmin/tenants/{}/vitrine` | ✅ E2E-VIT-01 |

## `/formulaires` — Formulaires

- **Ouvert par** : E2E-CHU-01, E2E-CHU-18, E2E-CHU-19, E2E-CHU-21, E2E-ECR-CHU-01, E2E-ECR-CHU-03
- **Fichiers** : `pages/Formulaires.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `components/FormulaireBuilder.jsx`, `components/FormulaireRenderer.jsx`, `components/WorkflowBuilder.jsx`, `components/VersionsPanel.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /formulaires/{}` | ✅ E2E-ECR-CHU-01 |
| `charger` | lister | `GET /formulaires` | ✅ E2E-CHU-01, E2E-CHU-18, E2E-CHU-19, E2E-CHU-21 (+2) |
| `charger` | lister | `GET /formulaires/{}/historique` | ✅ E2E-CHU-01, E2E-ECR-CHU-01 |
| `charger` | lister | `GET /formulaires/{}/version` | ✅ E2E-CHU-01, E2E-ECR-CHU-01, E2E-ECR-CHU-03 |
| `charger` | lister | `GET /formulaires/{}/workflow` | ✅ E2E-ECR-CHU-03 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /reference/DOMAINE` | ✅ E2E-CHU-01, E2E-CHU-18, E2E-CHU-19, E2E-CHU-21 (+2) |
| `charger` | lister | `GET /reference/TYPE_CHAMP` | ✅ E2E-CHU-01, E2E-ECR-CHU-01, E2E-ECR-CHU-03 |
| `creer` | ajouter | `POST /formulaires` | ✅ E2E-CHU-01 |
| `cycle` | ajouter | `POST /formulaires/{}/{}` | ✅ E2E-CHU-01 |
| `majValeurApercu` | ajouter | `POST /formulaires/{}/regles/evaluer` | ✅ E2E-ECR-CHU-01 |
| `revenir` | ajouter | `POST /formulaires/{}/revenir/{}` | ✅ E2E-ECR-CHU-01 |
| `enregistrer` | modifier | `PUT /formulaires/{}/structure` | ✅ E2E-CHU-01 |
| `enregistrer` | modifier | `PUT /formulaires/{}/workflow` | ✅ E2E-ECR-CHU-03 |

## `/formulaires/:id/remplir` — FormulaireRemplir

- **Ouvert par** : E2E-CHU-02, E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02, E2E-ECR-CHU-03
- **Fichiers** : `pages/FormulaireRemplir.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `components/FormulaireRenderer.jsx`, `components/DefiAntiRobot.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `FormulaireRemplir` | lister | `GET /formulaires/{}/places` | ✅ E2E-CHU-02, E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02 (+1) |
| `FormulaireRemplir` | consulter | `GET /formulaires/{}/saisies/{}` | ✅ E2E-CHU-02, E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02 (+1) |
| `FormulaireRemplir` | lister | `GET /formulaires/{}/saisies/structure` | ✅ E2E-CHU-02, E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02 (+1) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `DefiAntiRobot` | lister | `GET /saisie/captcha` | ✅ E2E-CHU-04 |
| `changer` | modifier | `PATCH /formulaires/{}/saisies/{}` | ✅ E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02, E2E-ECR-CHU-03 |
| `changer` | ajouter | `POST /formulaires/{}/regles/evaluer` | ✅ E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02, E2E-ECR-CHU-03 |
| `FormulaireRemplir` | ajouter | `POST /formulaires/{}/saisies` | ✅ E2E-CHU-02, E2E-CHU-03, E2E-CHU-04, E2E-ECR-CHU-02 (+1) |
| `soumettre` | soumettre | `POST /formulaires/{}/saisies/{}/soumettre` | ✅ E2E-CHU-04, E2E-ECR-CHU-02, E2E-ECR-CHU-03 |

## `/formulaires/:id/fiches` — Saisies

- **Ouvert par** : E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10, E2E-CHU-20, E2E-ECR-CHU-02
- **Fichiers** : `pages/Saisies.jsx`, `components/InventairePlaces.jsx`, `i18n/I18nContext.jsx`, `components/RapportPdfFiches.jsx`, `components/DocumentFiche.jsx`, `components/Modal.jsx`, `auth.jsx`, `hooks/useModules.js`, `components/DemandeAnnexes.jsx`, `components/RechercheFiches.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /rapport/criteres/{}` | ✅ E2E-ECR-CHU-02 |
| `lien` | télécharger | `GET /documents/{}/download` | ✅ E2E-ECR-CHU-02 |
| `InventairePlaces` | lister | `GET /formulaires/{}/inventaire` | ✅ E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10 (+2) |
| `exporter` | télécharger | `GET /formulaires/{}/inventaire.csv` | ✅ E2E-CHU-20 |
| `charger` | lister | `GET /formulaires/{}/saisies` | ✅ E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10 (+2) |
| `charger` | lister | `GET /formulaires/{}/saisies/{}/commentaires` | ✅ E2E-CHU-06, E2E-ECR-CHU-02 |
| `ouvrir` | télécharger | `GET /formulaires/{}/saisies/{}/document.pdf` | ✅ E2E-ECR-CHU-02 |
| `charger` | lister | `GET /formulaires/{}/saisies/{}/documents` | ✅ E2E-CHU-06, E2E-ECR-CHU-02 |
| `RapportPdfFiches` | lister | `GET /formulaires/{}/saisies/structure` | ✅ E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10 (+2) |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /rapport/criteres` | ✅ E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10 (+2) |
| `exporter` | télécharger | `GET /rapport/formulaires/{}/export.xlsx` | ✅ E2E-CHU-05 |
| `produire` | télécharger | `GET /rapport/formulaires/{}/rapport.pdf` | ✅ E2E-CHU-10 |
| `charger` | lister | `GET /rapport/formulaires/{}/resume` | ✅ E2E-CHU-05, E2E-CHU-06, E2E-CHU-08, E2E-CHU-10 (+2) |
| `statuer` | ajouter | `POST /formulaires/{}/saisies/{}/{}` | ✅ E2E-ECR-CHU-02 |
| `ajouterCommentaire` | ajouter | `POST /formulaires/{}/saisies/{}/commentaires` | ✅ E2E-ECR-CHU-02 |
| `envoyer` | ajouter | `POST /formulaires/{}/saisies/{}/document/transmettre` | ✅ E2E-ECR-CHU-02 |
| `televerser` | ajouter | `POST /formulaires/{}/saisies/{}/documents` | ✅ E2E-ECR-CHU-02 |
| `sauvegarder` | ajouter | `POST /rapport/criteres` | ✅ E2E-CHU-05, E2E-ECR-CHU-02 |

## `/mes-demandes` — MesDemandes

- **Ouvert par** : E2E-CHU-22, E2E-ECR-CHU-02
- **Fichiers** : `pages/MesDemandes.jsx`, `components/DocumentFiche.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `ouvrir` | télécharger | `GET /formulaires/{}/saisies/{}/document.pdf` | ✅ E2E-ECR-CHU-02 |
| `charger` | lister | `GET /mes-demandes` | ✅ E2E-CHU-22, E2E-ECR-CHU-02 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `annuler` | annuler | `POST /formulaires/{}/saisies/{}/annuler` | ✅ E2E-ECR-CHU-02 |

## `/nouvelle-demande` — Catalogue

- **Ouvert par** : E2E-ECR-CHU-02
- **Fichiers** : `pages/Catalogue.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /catalogue` | ✅ E2E-ECR-CHU-02 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /reference/DOMAINE` | ✅ E2E-ECR-CHU-02 |

## `/taches` — Taches

- **Ouvert par** : E2E-ECR-CHU-03
- **Fichiers** : `pages/Taches.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `components/Modal.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /taches/mine` | ✅ E2E-ECR-CHU-03 |
| `decider` | ajouter | `POST /taches/{}/decisions` | ✅ E2E-ECR-CHU-03 |

## `/applications` — Applications

- **Ouvert par** : E2E-CHU-15, E2E-ECR-CHU-06
- **Fichiers** : `pages/Applications.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /applications` | ✅ E2E-CHU-15, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /reference/DOMAINE` | ✅ E2E-CHU-15, E2E-ECR-CHU-06 |
| `soumettre` | ajouter | `POST /applications` | ✅ E2E-ECR-CHU-06 |
| `soumettre` | modifier | `PUT /applications/{}` | ✅ E2E-ECR-CHU-06 |
| `archiver` | modifier | `PUT /applications/{}/archiver` | ✅ E2E-ECR-CHU-06 |

## `/paie` — Paie

- **Ouvert par** : E2E-ECR-CHU-05
- **Fichiers** : `pages/Paie.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `Paie` | lister | `GET /paie/frequences` | ✅ E2E-ECR-CHU-05 |
| `calculer` | ajouter | `POST /paie/apercu` | ✅ E2E-ECR-CHU-05 |

## `/impot` — Impot

- **Ouvert par** : E2E-ECR-CHU-05
- **Fichiers** : `pages/Impot.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `calculer` | ajouter | `POST /paie/impot/apercu` | ✅ E2E-ECR-CHU-05 |

## `/declarations` — Declarations

- **Ouvert par** : E2E-ECR-CHU-05
- **Fichiers** : `pages/Declarations.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `appeler` | ajouter | `POST /paie/declarations/fin-annee` | ✅ E2E-ECR-CHU-05 |
| `appeler` | ajouter | `POST /paie/declarations/retenues` | ✅ E2E-ECR-CHU-05 |
| `appeler` | ajouter | `POST /paie/declarations/taxes` | ✅ E2E-ECR-CHU-05 |

## `/rh/personnel` — Personnel

- **Ouvert par** : E2E-ECR-CHU-04
- **Fichiers** : `pages/Personnel.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /rh/employes` | ✅ E2E-ECR-CHU-04 |
| `soumettre` | ajouter | `POST /rh/employes` | ✅ E2E-ECR-CHU-04 |
| `soumettre` | modifier | `PUT /rh/employes/{}` | ✅ E2E-ECR-CHU-04 |
| `archiver` | modifier | `PUT /rh/employes/{}/archiver` | ✅ E2E-ECR-CHU-04 |

## `/rh/employes/:id` — DossierEmploye

- **Ouvert par** : E2E-ECR-CHU-04
- **Fichiers** : `pages/DossierEmploye.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | consulter | `GET /rh/employes/{}` | ✅ E2E-ECR-CHU-04 |
| `calculerPaie` | ajouter | `POST /paie/apercu` | ✅ E2E-ECR-CHU-04 |

## `/approbations` — Approbations

- **Ouvert par** : E2E-ECR-CHU-06
- **Fichiers** : `pages/Approbations.jsx`, `i18n/I18nContext.jsx`, `auth.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /approbations/demandes` | ✅ E2E-ECR-CHU-06 |
| `charger` | lister | `GET /approbations/mes-etapes` | ✅ E2E-ECR-CHU-06 |
| `charger` | lister | `GET /approbations/regles` | ✅ E2E-ECR-CHU-06 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `creerDemande` | ajouter | `POST /approbations/demandes` | ✅ E2E-ECR-CHU-06 |
| `decider` | ajouter | `POST /approbations/etapes/{}/decider` | ✅ E2E-ECR-CHU-06 |
| `creerRegle` | ajouter | `POST /approbations/regles` | ✅ E2E-ECR-CHU-06 |

## `/rh/feuilles-temps` — FeuilleTemps

- **Ouvert par** : E2E-ECR-CHU-04
- **Fichiers** : `pages/FeuilleTemps.jsx`, `i18n/I18nContext.jsx`, `auth.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /rh/feuilles-temps/a-approuver` | ✅ E2E-ECR-CHU-04 |
| `charger` | lister | `GET /rh/feuilles-temps/mine` | ✅ E2E-ECR-CHU-04 |
| `enregistrer` | ajouter | `POST /rh/feuilles-temps` | ✅ E2E-ECR-CHU-04 |
| `action` | ajouter | `POST /rh/feuilles-temps/{}/{}` | ✅ E2E-ECR-CHU-04 |
| `enregistrer` | modifier | `PUT /rh/feuilles-temps/{}` | ✅ E2E-ECR-CHU-04 |

## `/rh/projets-taches` — ProjetsTaches

- **Ouvert par** : E2E-ECR-CHU-04
- **Fichiers** : `pages/ProjetsTaches.jsx`, `i18n/I18nContext.jsx`, `auth.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimer` | supprimer | `DELETE /rh/taches/{}` | ✅ E2E-ECR-CHU-04 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /rh/employes` | ✅ E2E-ECR-CHU-04 |
| `charger` | lister | `GET /rh/projets` | ✅ E2E-ECR-CHU-04 |
| `charger` | lister | `GET /rh/taches` | ✅ E2E-ECR-CHU-04 |
| `charger` | lister | `GET /rh/taches/mine` | ✅ E2E-ECR-CHU-04 |
| `creerProjet` | ajouter | `POST /rh/projets` | ✅ E2E-ECR-CHU-04 |
| `soumettreTache` | ajouter | `POST /rh/taches` | ✅ E2E-ECR-CHU-04 |
| `soumettreTache` | modifier | `PUT /rh/taches/{}` | ✅ E2E-ECR-CHU-04 |

## `/notifications` — NotificationsAdmin

- **Ouvert par** : E2E-CHU-07, E2E-ECR-CHU-06
- **Fichiers** : `pages/NotificationsAdmin.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /notification/journal` | ✅ E2E-CHU-07, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /notification/modeles` | ✅ E2E-CHU-07, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /notification/regles` | ✅ E2E-CHU-07, E2E-ECR-CHU-06 |
| `creerModele` | ajouter | `POST /notification/modeles` | ✅ E2E-ECR-CHU-06 |
| `creerRegle` | ajouter | `POST /notification/regles` | ✅ E2E-ECR-CHU-06 |

## `/securite-avancee` — SecuriteAvancee

- **Ouvert par** : E2E-CHU-16, E2E-ECR-CHU-06
- **Fichiers** : `pages/SecuriteAvancee.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `supprimerPermission` | supprimer | `DELETE /securite/permissions/{}` | ✅ E2E-ECR-CHU-06 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `charger` | lister | `GET /securite/groupes` | ✅ E2E-CHU-16, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /securite/permissions` | ✅ E2E-CHU-16, E2E-ECR-CHU-06 |
| `ajouterGroupe` | ajouter | `POST /securite/groupes` | ✅ E2E-CHU-16, E2E-ECR-CHU-06 |
| `u` | ajouter | `POST /securite/groupes/{}/membres` | ✅ E2E-ECR-CHU-06 |
| `ajouterPermission` | ajouter | `POST /securite/permissions` | ✅ E2E-ECR-CHU-06 |

## `/interop` — InteropAdmin

- **Ouvert par** : E2E-CHU-13, E2E-CHU-14, E2E-ECR-CHU-06
- **Fichiers** : `pages/InteropAdmin.jsx`, `i18n/I18nContext.jsx`, `hooks/useModules.js`, `components/EchangesHerites.jsx`

| Fonctionnalité | Type | Opération | Couverte par |
|---|---|---|---|
| `charger` | lister | `GET /formulaires` | ✅ E2E-CHU-13, E2E-CHU-14, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /interop/clients` | ✅ E2E-CHU-13, E2E-CHU-14, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /interop/echanges` | ✅ E2E-CHU-13, E2E-CHU-14, E2E-ECR-CHU-06 |
| `charger` | lister | `GET /modules` | ✅ E2E-AIDE-01, E2E-AIDE-02, E2E-AX-IQ-01, E2E-AX-IQ-02 (+143) |
| `creer` | ajouter | `POST /interop/clients` | ✅ E2E-CHU-13 |
| `declarer` | ajouter | `POST /interop/echanges` | ✅ E2E-ECR-CHU-06 |
| `desactiver` | ajouter | `POST /interop/echanges/{}/desactivation` | ✅ E2E-ECR-CHU-06 |
| `reprendre` | ajouter | `POST /interop/echanges/{}/reprise` | ✅ E2E-ECR-CHU-06 |
