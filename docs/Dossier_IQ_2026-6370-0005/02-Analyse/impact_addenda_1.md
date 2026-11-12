# Addenda 1 (9 novembre 2026) — impact sur la solution et ajustements

Source : [addenda-1.md](../09-Questions/reponses/addenda-1.md) (PDF : `09-Questions/reponses/ADDENDA 1_ 2026-6370-0005.pdf`).
L'addenda fait partie intégrante de l'appel d'offres et prévaut sur les points qu'il modifie. Une soumission qui l'ignore
peut être déclarée non conforme.

✅ déjà couvert · 🆕 réalisé suite à l'addenda (2026-11-11) · 📝 à refléter dans la soumission ou le bordereau ·
⚠ écart restant.

## 1. Synthèse

| Point de l'addenda | Effet sur notre solution | État |
|---|---|---|
| 4.3, Q1, Q2 — le CLM complet n'est pas exigé ; CLM avancé optionnel | Séparer clairement la base (signature, génération, orchestration) des options CLM avancé et CLM/IA | 📝 |
| 4.4, 4.5 — **TI-22 Workday, obligatoire** | API d'intégration entrante, rappels signés, récupération du document signé | 🆕 |
| Q10 — signature en lot depuis Salesforce **et** depuis la plateforme (AF-47) | Lots par l'API et écran « Signature en lot » | 🆕 |
| Q3 — intégrations Microsoft 365, Workday, Entra ID, Saviynt/SCIM ; pas de MCP | Workday 🆕 ; Entra ID ✅ (OIDC/SAML) ; Microsoft 365 ⚠ ; Saviynt/SCIM ⚠ | 🆕 / ⚠ |
| Q4 — authentification standard ou renforcée | Décrire nos méthodes, leur niveau d'assurance, leurs prérequis, leurs limites et leurs coûts | 📝 ⚠ |
| Q5 — 1 300 signataires, 20 administrateurs d'affaires, 3 + 15 administrateurs techniques (1 338) | Bordereau ; rôles d'administration distincts | 📝 ⚠ |
| Q6 — contenu de la mise en œuvre initiale, stabilisation de 30 jours ouvrables | Plan de mise en œuvre | 📝 |
| Q7, Q9 — nombre de modèles et de processus inclus, effort unitaire | Hypothèses de prix | 📝 |
| Q8 — environ 30 champs Salesforce par modèle | Correspondance champ par champ, sans limite | ✅ |
| Q11 — aucun éditeur visé ; équivalence admise | Démontrer l'équivalence fonctionnelle | 📝 |
| 4.1, 4.2 — définitions des criticités et de l'exigence obligatoire | Classer notre réponse quand l'Annexe 10 Excel sera disponible | 📝 |

## 2. Détail et ajustements

### 2.1 Portée : le CLM avancé devient optionnel (4.3, Q1, Q2)

IQ recherche « une Solution intégrée de signature électronique, de génération documentaire et d'orchestration » ; les
fonctions CLM avancées sont des options, tarifées à part. Les 70 à 100 utilisateurs CLM font partie des 1 300 et ne
s'additionnent pas.

Ajustement proposé pour la soumission (découpage de l'offre, sans changement de code) :

| Offre | Contenu | Lignes du bordereau |
|---|---|---|
| Base | Trousses, génération depuis modèles, parcours séquentiels, parallèles et mixtes, signature, notifications et rappels, suivi, dossier de preuve, intégrations Salesforce et Workday, SSO | Lignes obligatoires |
| CLM avancé (option) | Boîte de tâches assignées, catalogue de clauses et règles avancées, listes de référence administrables, rapports étendus | Ligne CLM avancé |
| CLM/IA (option) | Module `clm_ia` : classification, extraction, anomalies, révision humaine | Ligne CLM/IA |

Le contenu exact de « CLM avancé » est à arrêter avec l'équipe commerciale. L'activation par module (superadmin) permet
déjà de livrer l'option IA séparément ; un découpage plus fin de `clm` demanderait un module de plus.

### 2.2 Workday — TI-22, obligatoire (4.4, 4.5) 🆕

Exigence : intégration avec Workday pour les documents RH ; décrire le mécanisme (connecteurs ou API), les données
échangées, le déclenchement, le retour des statuts et, au besoin, le retour du document signé ou de sa référence.

Réalisé : API d'intégration entrante du module ([ADR-011](../../adr/ADR-011-api-integration-entrante.md), guide
[api_integration_entrante.md](../04-Architecture/api_integration_entrante.md)).

| Point de TI-22 | Réponse |
|---|---|
| Mécanisme | API REST documentée, OAuth 2.0 client credentials ; côté Workday : Studio, Orchestrate ou intergiciel d'IQ |
| Déclenchement | `POST /api/clm/integration/v1/trousses` depuis le processus métier Workday |
| Données ou documents échangés | Document PDF ou Word en base64, ou contrat généré par un modèle ; signataires ; référence Workday |
| Retour des statuts | Rappel HTTPS signé HMAC-SHA256 à chaque transition, avec reprises et clé d'idempotence |
| Retour du document signé | `GET …/documents/{id}/contenu` et `GET …/preuve` ; référence et empreinte dans la réponse |
| Licences et coûts | Aucun connecteur tiers côté plateforme ; l'effort Workday (Studio ou Orchestrate) est à chiffrer à la ligne Workday du bordereau |

Vérifié : `IntegrationEntranteIntegrationTest` (Workday crée et lance une trousse, reçoit un rappel signé vérifié,
télécharge le document et la preuve ; client révoqué refusé) et e2e `TI-22 — client Workday…`.

### 2.3 Signature en lot — AF-47 (Q10) 🆕

Exigence : lot initié depuis Salesforce lorsque le scénario y est piloté, et possiblement depuis la plateforme ; décrire
les deux modes, les limites, les contrôles, la traçabilité, les prérequis et les coûts.

| Mode | Réalisation |
|---|---|
| Depuis Salesforce | `POST /api/clm/integration/v1/lots` (client d'intégration Salesforce), un élément par dossier `sfDossierId`, préremplissage et parcours du modèle |
| Depuis la plateforme | Écran **Signature en lot** : modèle publié, un dossier par ligne, lancement immédiat ou brouillons |
| Limites | 500 éléments par lot, traitement synchrone ; au-delà, découper ou passer à un traitement asynchrone |
| Contrôles | Chaque élément suit les règles d'une trousse ordinaire (initiateur membre, modèle publié, identité selon le risque) ; un échec est isolé et motivé |
| Traçabilité | Lot et éléments conservés ; trousses rattachées au lot ; journal de configuration ; rappels au système appelant |

Vérifié : `IntegrationEntranteIntegrationTest` (lot Salesforce avec un échec isolé, lot depuis la plateforme) et e2e
`AF-47 — signature en lot depuis la plateforme`.

### 2.4 Intégrations listées (Q3)

| Système | État | Ajustement |
|---|---|---|
| Salesforce Grantmaking | ✅ ST-06, ST-12 ; lots 🆕 | Recette sur le bac à sable d'IQ |
| Workday | 🆕 | Ateliers pour les processus RH visés |
| Microsoft Entra ID | ✅ OIDC et SAML 2.0, essayés contre Keycloak | Groupes de l'annuaire → rôles et unités : à réaliser (questions D3, D4) |
| Saviynt ou SCIM | ⚠ absent | Proposer un point SCIM 2.0 (`/Users`, `/Groups`) pour le provisionnement et la désactivation ; à chiffrer |
| Microsoft 365 | ⚠ le connecteur de la plateforme (dépôt SharePoint, Teams) dépend du module « rédaction », non souscrit par IQ | Exposer le dépôt du document signé dans SharePoint au module CLM, par le même port ; à chiffrer |
| MCP | Non exigé | — |

### 2.5 Authentification standard ou renforcée (Q4)

| Méthode de la plateforme | Classe selon l'addenda | Prérequis | Limite |
|---|---|---|---|
| Lien sécurisé par courriel (`COURRIEL`) | Standard | Adresse courriel | Possession de la boîte seulement |
| Session de l'employé ou SSO (signataire interne) | Standard | Compte ou annuaire d'IQ | — |
| Code à usage unique par SMS (`CODE_SMS`) | Renforcée | Numéro de téléphone | Coût par SMS, fournisseur d'envoi |
| Question de vérification (`QUESTION`) | Renforcée | Réponse convenue hors bande | Connaissance partagée |
| Courriel et SMS combinés (`FORTE`) | Renforcée (multifacteur) | Courriel et téléphone | — |
| MFA de l'annuaire (Entra ID) | Renforcée | SSO | Signataires internes |
| Vérification d'identité sur pièce | Renforcée | Fournisseur spécialisé | ⚠ non offerte : à obtenir du fournisseur de signature retenu (C1) |

Le niveau minimal se règle par risque et par type de signataire (UI-22) ; il est à présenter tel quel dans la réponse.

### 2.6 Volumes et administrateurs (Q2, Q5)

Hypothèses de tarification : 1 300 signataires, 20 administrateurs d'affaires, 3 administrateurs techniques principaux,
15 administrateurs techniques utilisateurs (1 338 droits). La plateforme distingue mal les deux familles
d'administrateurs : un seul rôle `ADMIN`. L'addenda les compte à part, ce qui appuie la création des rôles
`ADMIN_AFFAIRES` et `ADMIN_TI` (question D5), à proposer dans la réponse.

### 2.7 Mise en œuvre initiale, modèles et processus (Q6, Q7, Q9)

À refléter dans le plan et le prix forfaitaire : configuration, modèles approuvés, processus, rôles et accès,
intégration Salesforce, environnements, essais, documentation, formation, transfert, mise en production et
**stabilisation de 30 jours ouvrables**. Le prix doit indiquer le nombre de modèles et de processus inclus et l'effort
unitaire d'un modèle ou d'un processus additionnel. Ces nombres relèvent d'une décision commerciale, non chiffrée ici.

### 2.8 Champs Salesforce (Q8)

Environ 30 champs par modèle : chaque champ du modèle désigne sa source Salesforce, sans plafond. Depuis le correctif du
2026-11-08, les champs personnalisés `…__c` sont lus même lors d'une recherche par numéro de dossier. À éprouver en
recette avec un modèle réel de 30 champs.

## 3. Écarts restants, par priorité

1. **Saviynt / SCIM** — provisionnement et désactivation automatiques des comptes ; point SCIM 2.0 à réaliser.
2. **Microsoft 365 pour le module CLM** — dépôt du document signé dans SharePoint, avis Teams.
3. **Groupes de l'annuaire → rôles et unités** (Entra ID).
4. **Rôles `ADMIN_AFFAIRES` / `ADMIN_TI`**.
5. **Vérification d'identité sur pièce** — par le fournisseur de signature retenu.
6. **Lots asynchrones** au-delà de 500 éléments.

Les questions correspondantes sont dans [questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md)
(réponses de l'addenda reportées ; nouvelles questions W, P, O et V).
