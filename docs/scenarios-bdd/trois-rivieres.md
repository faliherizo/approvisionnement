# Scénarios BDD — Ville de Trois-Rivières — appel d'offres 7000017905

*Solution de rédaction contractuelle — section III, tableau de conformité.* Source des exigences : `docs/trois-riviere/formulaires-separes/Tableau-de-conformite.md`.

> Document généré par `bdd/outils/documenter.mjs` depuis le catalogue, les fonctionnalités Gherkin et les cas JSON ;
> ne pas le modifier à la main. Régénérer : `cd bdd && npm run documenter`.

## Synthèse

| Exigences | Éprouvées par scénario | Dont réussies à la dernière exécution | Documentaires | Écarts déclarés | Sans objet |
|---|---|---|---|---|---|
| 37 | 20 | 20 | 17 | 0 | 0 |

## Matrice des exigences

| Exigence | Libellé | Caractère | Vérification | Scénarios |
|---|---|---|---|---|
| `TR-01` | Données hébergées au Canada | Obligatoire | Documentaire | Hébergement Azure au Canada : infrastructure (région paramétrée, validation Terraform). |
| `TR-02` | Générer des documents Word modifiables | Obligatoire | Scénario BDD exécuté | ✅ [Le document approuvé se télécharge en Word modifiable et en PDF](#le-document-approuve-se-telecharge-en-word-modifiable-et-en-pdf) |
| `TR-03` | Migrer/adapter les modèles Word existants | Obligatoire | Scénario BDD exécuté | ✅ [Les modèles Word existants de la Ville s'importent, d'abord à l'essai sans rien écrire](#les-modeles-word-existants-de-la-ville-simportent-dabord-a-lessai-sans-rien-ecrire) |
| `TR-04` | Compatible Microsoft 365 | Obligatoire | Scénario BDD exécuté | ✅ [Publication vers SharePoint, OneDrive, Teams et Outlook, seulement après validation](#publication-vers-sharepoint-onedrive-teams-et-outlook-seulement-apres-validation) |
| `TR-05` | Gestion sécurisée des accès + auth. corporative | Obligatoire | Scénario BDD exécuté | ✅ [Authentification corporative, second facteur et rôles distincts](#authentification-corporative-second-facteur-et-roles-distincts) |
| `TR-06` | Authentification multifacteur (MFA) | Obligatoire | Scénario BDD exécuté | ✅ [Authentification corporative, second facteur et rôles distincts](#authentification-corporative-second-facteur-et-roles-distincts) |
| `TR-07` | Rôles, permissions et profils d'accès | Obligatoire | Scénario BDD exécuté | ✅ [Authentification corporative, second facteur et rôles distincts](#authentification-corporative-second-facteur-et-roles-distincts) |
| `TR-08` | Chiffrement au repos et en transit | Obligatoire | Scénario BDD exécuté | ✅ [Les secrets d'intégration sont conservés chiffrés et jamais restitués](#les-secrets-dintegration-sont-conserves-chiffres-et-jamais-restitues) |
| `TR-09` | Journaux d'accès et d'activité | Obligatoire | Scénario BDD exécuté | ✅ [Journal d'accès et d'activité filtré puis exporté](#journal-dacces-et-dactivite-filtre-puis-exporte) |
| `TR-10` | Configurable aux règles internes + LCOM | Obligatoire | Scénario BDD exécuté | ✅ [Les étapes de validation se configurent par type de document](#les-etapes-de-validation-se-configurent-par-type-de-document) |
| `TR-11` | Traçabilité versions/validations/approbations | Obligatoire | Scénario BDD exécuté | ✅ [Versions, comparaison et historique des validations sont conservés](#versions-comparaison-et-historique-des-validations-sont-conserves) |
| `TR-12` | Conservation docs/versions/journaux | Obligatoire | Scénario BDD exécuté | ✅ [Journal d'accès et d'activité filtré puis exporté](#journal-dacces-et-dactivite-filtre-puis-exporte)<br>✅ [Versions, comparaison et historique des validations sont conservés](#versions-comparaison-et-historique-des-validations-sont-conserves) |
| `TR-13` | Séparation des responsabilités | Obligatoire | Scénario BDD exécuté | ✅ [La séparation des responsabilités empêche de décider à la place d'un autre rôle](#la-separation-des-responsabilites-empeche-de-decider-a-la-place-dun-autre-role) |
| `TR-14` | Pas d'entraînement d'IA externe sans autorisation | Obligatoire | Documentaire | Aucun entraînement de modèle externe : IA locale (Ollama), aucune donnée transmise ; vérifié par revue d'architecture. |
| `TR-15` | Validation humaine des contenus générés | Obligatoire | Scénario BDD exécuté | ✅ [L'IA ne modifie rien sans validation humaine et l'organisation peut la désactiver](#lia-ne-modifie-rien-sans-validation-humaine-et-lorganisation-peut-la-desactiver) |
| `TR-16` | Méthode de migration détaillée | Obligatoire | Documentaire | ✅ [Les modèles Word existants de la Ville s'importent, d'abord à l'essai sans rien écrire](#les-modeles-word-existants-de-la-ville-simportent-dabord-a-lessai-sans-rien-ecrire) |
| `TR-17` | Détailler coûts (ponctuels, récurrents, additionnels, sortie) | Obligatoire | Documentaire | Coûts : bordereau de prix. |
| `TR-18` | Soutien, formation, documentation en français | Obligatoire | Documentaire | Solution, soutien, formation et documentation en français : engagement de service (l'interface en français est éprouvée par tous les scénarios). |
| `TR-19` | Exportation complète en fin de contrat | Obligatoire | Scénario BDD exécuté | ✅ [Export complet des données en fin de contrat](#export-complet-des-donnees-en-fin-de-contrat) |
| `TR-20` | Intégrations SharePoint/Teams/OneDrive/Outlook | Souhaitable | Scénario BDD exécuté | ✅ [Publication vers SharePoint, OneDrive, Teams et Outlook, seulement après validation](#publication-vers-sharepoint-onedrive-teams-et-outlook-seulement-apres-validation) |
| `TR-21` | Dossier d'approvisionnement | Souhaitable | Scénario BDD exécuté | ✅ [Un dossier d'approvisionnement regroupe documents, versions et validations](#un-dossier-dapprovisionnement-regroupe-documents-versions-et-validations) |
| `TR-22` | Rapports de suivi | Souhaitable | Scénario BDD exécuté | ✅ [Rapports de suivi des documents, validations et dossiers](#rapports-de-suivi-des-documents-validations-et-dossiers) |
| `TR-23` | Étapes de validation par type de document | Souhaitable | Scénario BDD exécuté | ✅ [Les étapes de validation se configurent par type de document](#les-etapes-de-validation-se-configurent-par-type-de-document) |
| `TR-24` | Désactivation de l'IA | Souhaitable | Scénario BDD exécuté | ✅ [L'IA ne modifie rien sans validation humaine et l'organisation peut la désactiver](#lia-ne-modifie-rien-sans-validation-humaine-et-lorganisation-peut-la-desactiver) |
| `TR-25` | Références d'organisations similaires | Souhaitable | Documentaire | Références : formule de qualification. |
| `TR-26` | Environnement d'essai / pilote | Souhaitable | Documentaire | Environnement d'essai en ligne : service de démonstration. |
| `TR-27` | Évolutions prévues à court terme | Souhaitable | Documentaire | Évolutions prévues : feuille de route. |
| `TR-28` | Type de solution (infonuagique/locale/hybride) | Information requise | Documentaire | Information requise : solution infonuagique (SaaS). |
| `TR-29` | Connecteurs / interfaces / API + coûts | Information requise | Scénario BDD exécuté | ✅ [L'API REST est documentée par une spécification OpenAPI](#lapi-rest-est-documentee-par-une-specification-openapi) |
| `TR-30` | Notification et suivi des incidents de sécurité | Information requise | Documentaire | Information requise : procédure de notification des incidents. |
| `TR-31` | Sous-traitants (hébergement, traitement, soutien) | Information requise | Documentaire | Information requise : sous-traitants. |
| `TR-32` | Utilisation de l'IA — description | Information requise | Documentaire | Information requise : description de l'usage de l'IA (le comportement est éprouvé par TR-15 et TR-24). |
| `TR-33` | Volume inclus (modèles/clauses/contenus) | Information requise | Documentaire | Information requise : volume inclus. |
| `TR-34` | Délais de réponse du soutien par priorité | Information requise | Documentaire | Information requise : délais de soutien. |
| `TR-35` | Formations incluses (utilisateurs et admins) | Information requise | Documentaire | Information requise : formations incluses. |
| `TR-36` | Frais de sortie / restitution | Information requise | Documentaire | Information requise : aucuns frais de sortie (l'export complet est éprouvé par TR-19). |
| `TR-37` | Limites connues | Information requise | Documentaire | Information requise : limites connues. |

## Exigences non éprouvées par un scénario

- `TR-01` — Données hébergées au Canada : **Documentaire.** Hébergement Azure au Canada : infrastructure (région paramétrée, validation Terraform). Référence : `docs/adr/ADR-020-region-hebergement-et-niveaux-de-service.md`.
- `TR-14` — Pas d'entraînement d'IA externe sans autorisation : **Documentaire.** Aucun entraînement de modèle externe : IA locale (Ollama), aucune donnée transmise ; vérifié par revue d'architecture. Référence : `docs/architecture-technique.md`.
- `TR-16` — Méthode de migration détaillée : **Documentaire.** Méthode de migration décrite au mémoire ; l'import Word est éprouvé par le scénario TR-03. Référence : `docs/trois-riviere/`.
- `TR-17` — Détailler coûts (ponctuels, récurrents, additionnels, sortie) : **Documentaire.** Coûts : bordereau de prix. Référence : `docs/trois-riviere/`.
- `TR-18` — Soutien, formation, documentation en français : **Documentaire.** Solution, soutien, formation et documentation en français : engagement de service (l'interface en français est éprouvée par tous les scénarios).
- `TR-25` — Références d'organisations similaires : **Documentaire.** Références : formule de qualification. Référence : `docs/trois-riviere/`.
- `TR-26` — Environnement d'essai / pilote : **Documentaire.** Environnement d'essai en ligne : service de démonstration. Référence : `https://contrat.vinaitsolution.com/login`.
- `TR-27` — Évolutions prévues à court terme : **Documentaire.** Évolutions prévues : feuille de route. Référence : `docs/roadmap.md`.
- `TR-28` — Type de solution (infonuagique/locale/hybride) : **Documentaire.** Information requise : solution infonuagique (SaaS).
- `TR-30` — Notification et suivi des incidents de sécurité : **Documentaire.** Information requise : procédure de notification des incidents. Référence : `docs/journalisation.md`.
- `TR-31` — Sous-traitants (hébergement, traitement, soutien) : **Documentaire.** Information requise : sous-traitants.
- `TR-32` — Utilisation de l'IA — description : **Documentaire.** Information requise : description de l'usage de l'IA (le comportement est éprouvé par TR-15 et TR-24).
- `TR-33` — Volume inclus (modèles/clauses/contenus) : **Documentaire.** Information requise : volume inclus.
- `TR-34` — Délais de réponse du soutien par priorité : **Documentaire.** Information requise : délais de soutien.
- `TR-35` — Formations incluses (utilisateurs et admins) : **Documentaire.** Information requise : formations incluses.
- `TR-36` — Frais de sortie / restitution : **Documentaire.** Information requise : aucuns frais de sortie (l'export complet est éprouvé par TR-19).
- `TR-37` — Limites connues : **Documentaire.** Information requise : limites connues.

## Scénarios détaillés

### Documents Word et Microsoft 365 (AO 7000017905, section III n° 2, 3, 4, 20)


#### Le document approuvé se télécharge en Word modifiable et en PDF

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Documents Word et Microsoft 365 (AO 7000017905, section III n° 2, 3, 4, 20) (`bdd/features/trois-rivieres/01-documents-word-m365.feature`)
- **Exigences éprouvées :** `TR-02`

```gherkin
Et j'exécute le cas "tr.document.approuve"   ⟵ contexte
Quand j'exécute le cas "tr.word.modifiable"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.word.modifiable`** — Le document se télécharge en Word modifiable (.docx) et en PDF *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/documents/${document}/export/docx` |  | 2xx | contient « PK » ; contient « word/document.xml » |
| 2 | `GET /api/documents/${document}/export/pdf` |  | 2xx | contient « %PDF » |

#### Publication vers SharePoint, OneDrive, Teams et Outlook, seulement après validation

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Documents Word et Microsoft 365 (AO 7000017905, section III n° 2, 3, 4, 20) (`bdd/features/trois-rivieres/01-documents-word-m365.feature`)
- **Exigences éprouvées :** `TR-04`, `TR-20`

```gherkin
Et j'exécute le cas "tr.document.approuve"   ⟵ contexte
Quand j'exécute le cas "tr.m365.publication"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.m365.publication`** — Publication refusée avant validation, puis déposée vers SharePoint, OneDrive, Teams et Outlook, chaque dépôt tracé *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `brouillon` ← `$.id` |
| 2 | `POST /api/documents/${brouillon}/publications` |  | 409 | contient « doit d'abord être validé » |
| 3 | `POST /api/documents/${document}/publications` |  | 2xx | `$.succes` egal true ; `$.cible` egal "SHAREPOINT" |
| 4 | `POST /api/documents/${document}/publications` |  | 2xx | `$.succes` egal true |
| 5 | `POST /api/documents/${document}/publications` |  | 2xx | `$.succes` egal true |
| 6 | `POST /api/documents/${document}/publications` |  | 2xx | `$.succes` egal true |
| 7 | `GET /api/documents/${document}/publications` |  | 2xx | `$[*].cible` inclut "SHAREPOINT" ; `$[*].cible` inclut "ONEDRIVE" ; `$[*].cible` inclut "TEAMS" ; `$[*].cible` inclut "OUTLOOK" |
| 8 | `GET /api/simulateur/depots` | administrateur de Lévis | 2xx | `$[*].nomFichier` inclut "contrat-${document}.pdf" |

#### Les modèles Word existants de la Ville s'importent, d'abord à l'essai sans rien écrire

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Documents Word et Microsoft 365 (AO 7000017905, section III n° 2, 3, 4, 20) (`bdd/features/trois-rivieres/01-documents-word-m365.feature`)
- **Exigences éprouvées :** `TR-03`, `TR-16`

```gherkin
Et j'exécute le cas "tr.document.approuve"   ⟵ contexte
Quand j'exécute le cas "tr.word.migration"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — import et migration"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.word.migration`** — Un modèle Word existant s'importe : essai sans écriture, sections détectées, puis import réel *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/import/word?essai=true&nom=Modele%20TR%20${unique}&type=CONTRAT_SERVICES` |  | 2xx | `$.essai` egal true ; `$.sectionsDetectees` egal 2 ; `$.clausesCreees` egal 0 ; `$.erreurs` egal [] ; envoi multipart (fichier) |
| 2 | `POST /api/import/word?essai=false&nom=Modele%20TR%20${unique}&type=CONTRAT_SERVICES` |  | 2xx | `$.essai` egal false ; `$.templateId` existe true ; envoi multipart (fichier) |

### Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13)


#### Authentification corporative, second facteur et rôles distincts

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-05`, `TR-06`, `TR-07`

```gherkin
Quand j'exécute le cas "lev.securite.roles"
Et j'exécute le cas "lev.securite.mfa"
Et j'exécute le cas "lev.securite.sso"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.securite.roles`** — Rôles distincts : un rédacteur n'administre ni les utilisateurs ni le journal *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/admin/users` |  | 403 | — |
| 2 | `GET /api/audit` |  | 403 | — |
| 3 | `GET /api/admin/users` | administrateur de Lévis | 2xx | `$.contenu[*].email` inclut "redacteur@levis.qc.ca" |

**Cas `lev.securite.mfa`** — Second facteur : question secrète puis TOTP (RFC 6238) exigés et vérifiés *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true ; `$.ok` egal false |
| 2 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true |
| 3 | `POST /api/auth/login` | anonyme | 200, 401 | `$.ok` different true |
| 4 | `POST /api/auth/login` | anonyme | 2xx | `$.ok` egal true |

**Cas `lev.securite.sso`** — Authentification unique Entra ID : connecteurs OIDC et SAML configurables et activables par organisation *(données : `bdd/donnees/levis.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/parametres/integration-oidc` |  | 2xx | `$` existe true |
| 2 | `GET /api/parametres/integration-saml` |  | 2xx | `$` existe true |
| 3 | `GET /api/auth/config?slug=levis` |  | 2xx | `$` existe true |

#### Les secrets d'intégration sont conservés chiffrés et jamais restitués

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-08`

```gherkin
Quand j'exécute le cas "tr.secrets.chiffres"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — paramètres"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.secrets.chiffres`** — Un secret d'intégration n'est jamais restitué : seule sa présence est indiquée *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/parametres/integration-m365` |  | 2xx | capture `m365` ← `$` |
| 2 | `PUT /api/parametres/integration-m365` |  | 2xx | `$.secretDefini` egal true ; ne contient pas « SecretBdd-${unique} » |
| 3 | `GET /api/parametres/integration-m365` |  | 2xx | `$.secretDefini` egal true ; ne contient pas « SecretBdd-${unique} » ; `$.clientSecret` existe false |

#### Journal d'accès et d'activité filtré puis exporté

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-09`, `TR-12`

```gherkin
Et j'exécute le cas "tr.document.approuve"
Quand j'exécute le cas "tr.journal.export"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — journal d'audit"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.journal.export`** — Journal d'accès et d'activité filtrable et exportable (CSV) *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/audit?action=APPROUVER` |  | 2xx | `$` longueurMin 1 ; `$[*].action` exclut "COMMENTER" |
| 2 | `GET /api/audit/export` |  | 2xx | contient « horodatage,utilisateur,action,entite,entiteId,details » ; contient « APPROUVER » |

#### Versions, comparaison et historique des validations sont conservés

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-11`, `TR-12`

```gherkin
Et j'exécute le cas "tr.document.approuve"
Quand j'exécute le cas "tr.versions.tracabilite"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.versions.tracabilite`** — Versions du document conservées et comparables ; historique des validations *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents/${document}/versions` |  | 2xx | `$.numeroVersion` existe true ; capture `v1` ← `$.id` |
| 2 | `POST /api/documents/${document}/versions` |  | 2xx | capture `v2` ← `$.id` |
| 3 | `GET /api/documents/${document}/versions` |  | 2xx | `$` longueurMin 2 ; `$[*].note` inclut "Version approuvée ${unique}" |
| 4 | `GET /api/versions/comparer?a=${v1}&b=${v2}` |  | 2xx | `$[*].changement` inclut "INCHANGEE" |
| 5 | `GET /api/documents/${document}/approbation` |  | 2xx | `$[*].approbateur` inclut "juriste@levis.qc.ca" ; `$[*].note` inclut "Direction" |

#### La séparation des responsabilités empêche de décider à la place d'un autre rôle

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-13`

```gherkin
Quand j'exécute le cas "tr.separation.responsabilites"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.separation.responsabilites`** — Séparation des responsabilités : le rédacteur ne décide pas à la place du juridique *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `doc` ← `$.id` |
| 2 | `POST /api/documents/${doc}/approbation` |  | 2xx | capture `a1` ← `$[0].id` ; capture `a2` ← `$[1].id` |
| 3 | `POST /api/documents/approbation/${a2}/decision` |  | 403 | — |
| 4 | `POST /api/documents/approbation/${a1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${a1}/decision` |  | 409 | — |

#### Les étapes de validation se configurent par type de document

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13) (`bdd/features/trois-rivieres/02-securite-tracabilite.feature`)
- **Exigences éprouvées :** `TR-10`, `TR-23`

```gherkin
Quand j'exécute le cas "tr.workflow.par-type"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.workflow.par-type`** — Étapes de validation propres à un type de document : le bail ne passe que par l'approvisionnement et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/workflows` |  | 2xx | capture `avant` ← `$` |
| 2 | `POST /api/workflows` |  | 2xx | capture `workflow` ← `$.id` |
| 3 | `POST /api/documents` | rédacteur de Lévis | 2xx | capture `pret` ← `$.id` |
| 4 | `POST /api/documents/${pret}/approbation` | rédacteur de Lévis | 2xx | `$[*].roleRequis` egal ["APPRO","DIRECTION"] |
| 5 | `DELETE /api/workflows/${workflow}` |  | 2xx | — |

### IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29)


#### L'IA ne modifie rien sans validation humaine et l'organisation peut la désactiver

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29) (`bdd/features/trois-rivieres/03-ia-donnees-suivi.feature`)
- **Exigences éprouvées :** `TR-15`, `TR-24`

```gherkin
Et j'exécute le cas "tr.document.approuve"
Quand j'exécute le cas "tr.ia.encadree"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — assistant IA"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.ia.encadree`** — L'IA ne fait que proposer : le document reste inchangé tant qu'une personne n'a pas validé ; l'organisation peut la désactiver *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/documents/${document}` |  | 2xx | capture `texteAvant` ← `$.clauses[0].texteEffectif` |
| 2 | `POST /api/ia/rediger` |  | 200, 503 | — |
| 3 | `GET /api/documents/${document}` |  | 2xx | `$.clauses[0].texteEffectif` egal "${texteAvant}" |
| 4 | `PUT /api/ia/parametres` | administrateur de Lévis | 2xx | `$.activee` egal false |
| 5 | `POST /api/ia/rediger` |  | 409 | contient « désactivé » |
| 6 | `POST /api/ia/chat` |  | 409 | — |
| 7 | `PUT /api/ia/parametres` | administrateur de Lévis | 2xx | `$.activee` egal true |
| 8 | `GET /api/audit?action=IA_DESACTIVEE` | administrateur de Lévis | 2xx | `$` longueurMin 1 |

#### Export complet des données en fin de contrat

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29) (`bdd/features/trois-rivieres/03-ia-donnees-suivi.feature`)
- **Exigences éprouvées :** `TR-19`

```gherkin
Quand j'exécute le cas "tr.export.complet"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.export.complet`** — Export complet en fin de contrat : documents, modèles, configuration et manifeste, sans frais *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/reversibilite/export` |  | 2xx | contient « PK » ; contient « documents.json » ; contient « workflows.json » ; contient « manifeste.json » |

#### Un dossier d'approvisionnement regroupe documents, versions et validations

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29) (`bdd/features/trois-rivieres/03-ia-donnees-suivi.feature`)
- **Exigences éprouvées :** `TR-21`

```gherkin
Et j'exécute le cas "tr.document.approuve"
Quand j'exécute le cas "tr.dossier.approvisionnement"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — dossiers d'approvisionnement"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.document.approuve`** — Préalable : un contrat de services assemblé puis approuvé par l'approvisionnement, le juridique et la direction *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | — |
| 3 | `POST /api/documents/${document}/approbation` |  | 2xx | capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 4 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | — |
| 5 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | — |
| 6 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 7 | `GET /api/documents/${document}` |  | 2xx | `$.statut` egal "APPROUVE" |

**Cas `tr.dossier.approvisionnement`** — Dossier d'approvisionnement : regroupe documents, versions et validations ; rapport consolidé *(données : `bdd/donnees/trois-rivieres.json` ; profil : rédacteur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/dossiers` |  | 2xx | `$.statut` egal "OUVERT" ; capture `dossier` ← `$.id` |
| 2 | `POST /api/dossiers/${dossier}/documents/${document}` |  | 2xx | — |
| 3 | `GET /api/dossiers/${dossier}/documents` |  | 2xx | `$[*].id` inclut "${document}" |
| 4 | `POST /api/dossiers/${dossier}/statut` |  | 2xx | `$.statut` egal "EN_COURS" |
| 5 | `GET /api/dossiers/${dossier}/rapport` |  | 2xx | `$.titre` egal "Dossier TR ${unique}" ; `$.statut` egal "EN_COURS" ; `$.nombreDocuments` egal 1 ; `$.documentsApprouves` egal 1 |

#### Rapports de suivi des documents, validations et dossiers

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29) (`bdd/features/trois-rivieres/03-ia-donnees-suivi.feature`)
- **Exigences éprouvées :** `TR-22`

```gherkin
Quand j'exécute le cas "tr.rapports.suivi"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "TR — rapports de suivi"
Alors l'écran est accessible, sans débordement ni superposition
Quand j'ouvre l'écran "TR — tableau de bord"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `tr.rapports.suivi`** — Rapports de suivi : documents générés par statut et type, validations en attente, dossiers en cours *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/rapports/suivi` |  | 2xx | `$.totalDocuments` existe true ; `$.documentsParStatut` existe true ; `$.documentsParType` existe true ; `$.approbationsEnAttente` existe true ; `$.dossiersActifs` existe true |
| 2 | `GET /api/stats` |  | 2xx | `$` existe true |

#### L'API REST est documentée par une spécification OpenAPI

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29) (`bdd/features/trois-rivieres/03-ia-donnees-suivi.feature`)
- **Exigences éprouvées :** `TR-29`

```gherkin
Quand j'exécute le cas "tr.api.openapi"
Alors la plateforme se comporte comme attendu
```

**Cas `tr.api.openapi`** — API REST documentée : spécification OpenAPI publiée *(données : `bdd/donnees/trois-rivieres.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /v3/api-docs` |  | 2xx | `$.openapi` existe true ; `$.paths./api/documents` existe true |

