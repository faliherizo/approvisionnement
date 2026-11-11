# Validation des exigences fonctionnelles F-01 à F-13 — et comment les tester depuis l'interface

État vérifié le 2026-11-08 dans le code, l'interface et les tests du module CLM, tenant **Investissement Québec**.
Source des exigences : [analyse_besoins_exigences.md](analyse_besoins_exigences.md), § 4. Synthèse par exigence et exigences
non fonctionnelles : [matrice_tracabilite.md](matrice_tracabilite.md).

✅ réalisée et testée · ✅🔌 réalisée ; recette sur le système réel d'IQ à faire · 🆕 écart comblé le 2026-11-08 ·
⏳ dépend d'une décision ou d'un fournisseur d'IQ.

## 1. Se préparer

| Élément | Valeur |
|---|---|
| Adresse | Docker : <http://localhost:8082/login> · développement : <http://localhost:5173/login> |
| Comptes (mot de passe `Passw0rd!`) | `initiateur@`, `validateur@`, `approbateur@`, `signataire@`, `admin.affaires@`, `admin.ti@`, `auditeur@iq.example` — rôles et connexion : [tenant_iq_profils_et_roles.md](../11-Tenant-IQ/tenant_iq_profils_et_roles.md) |
| Dossier Salesforce de démonstration | `DEM-2026-0001` (Coopérative Agri-Nord, 250 000,00 $), connecteur en mode simulation |
| Données des tests | [`e2e/data/clm-exigences.json`](../../../e2e/data/clm-exigences.json) : modèle, valeurs, parcours mixte, signataire, notification, compte |
| Tests de bout en bout | [`e2e/views/clm.exigences.spec.ts`](../../../e2e/views/clm.exigences.spec.ts), un test par exigence (`F-xx` dans le titre) |

Lancer les tests de bout en bout, sur une base neuve :

```bash
cd e2e
npx playwright test views/clm.exigences.spec.ts            # serveurs démarrés par le harnais (H2 neuve)
npx playwright test views/clm.exigences.spec.ts -g "F-07"  # une seule exigence
```

Si le port 5173 est occupé, viser d'autres ports : `E2E_FRONTEND_URL=http://localhost:5175 E2E_BACKEND_URL=http://localhost:8091`.

## 2. Résultat

Les 13 exigences sont réalisées. Trois manques relevés pendant la validation ont été comblés (🆕). Les points ⏳ ne
dépendent plus du code : ils attendent un choix d'IQ ou un contrat avec un fournisseur (§ 4).

| Exigence | État | Test de bout en bout | Tests serveur principaux |
|---|---|---|---|
| F-01 Trousses | ✅ | `F-01 — créer une trousse, y déposer un PDF et un Word…` | `ClmIntegrationTest` |
| F-02 Génération depuis Salesforce | ✅🔌 | `F-02 — dossier Salesforce récupéré…` | `ClmIntegrationTest`, `SalesforceRestTest` |
| F-03 Modèles | ✅ | `F-03 — cycle de vie d'un modèle…` | `ClmIntegrationTest`, `DomaineClmTest` |
| F-04 Champs et saisie | ✅ 🆕 saisie assistée | `F-04 — champs typés, format contrôlé…` | `SuggestionsTest`, `SuggestionsSaisieIntegrationTest` |
| F-05 Validation et approbation | ✅ | `F-05, F-06, F-08, F-09 — parcours mixte…` | `ClmIntegrationTest`, `DomaineClmTest` |
| F-06 Signature électronique | ✅ ⏳ fournisseur certifié | idem, étape F-06 ; portail : `clm.view.spec.ts` | `ClmIntegrationTest` |
| F-07 Notifications et rappels | ✅ 🆕 alerte avant échéance | `F-07 — notifications…` | `RappelServiceTest`, `DomaineClmTest` |
| F-08 Suivi centralisé | ✅ | idem, étape F-08 | `ClmIntegrationTest` |
| F-09 Versions, journal, preuve | ✅ ⏳ conservation | idem, étape F-09 | `ClmIntegrationTest`, `DomaineClmTest` |
| F-10 Salesforce et API | ✅🔌 | `F-10 — connexion Salesforce testée…` | `ClmIntegrationTest`, `SalesforceServiceAlerteTest` |
| F-11 Administration et accès | ✅ 🆕 journal des comptes ⏳ IdP | `F-11 — rôle attribué puis accès retiré…` | `AdminUserServiceTest`, `IsolationTenantIqTest` |
| F-12 Mobilité, expérience | ✅ | `F-12 — interface bilingue…` ; 4 largeurs : `clm.view.spec.ts` | — |
| F-13 IA (option) | ✅ | `F-13 — analyse IA d'un document…` | `ClmIntegrationTest`, `DomaineClmTest` |

Chaque test de bout en bout contrôle aussi l'écran parcouru : aucun débordement ni chevauchement, cibles ≥ 24 px, contrôles
nommés, zéro violation axe-core (WCAG 2.2 AA).

## 3. Détail par exigence — comment le tester depuis l'interface

### F-01 — Création et gestion des trousses

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Créer une trousse | ✅ | *Initiateur* : **Créer une trousse** → Titre, Unité responsable, Niveau de risque, Échéance → **Enregistrer le brouillon** |
| Ajouter des documents PDF ou Word | ✅ | Trousse → onglet **Documents** → **Téléverser un document** (`.pdf`, `.docx`) |
| Téléversement d'au moins 25 Mo | ✅ | Même onglet : « jusqu'à 25 Mo » affiché ; plafond modifiable dans **Paramètres et environnements** |
| Intervenants, métadonnées, paramètres | ✅ | Onglets **Participants** (**Ajouter un signataire**) et **Parcours** ; échéance, copies, rappels à la création |
| Plusieurs documents par trousse | ✅ | Déposer un 2ᵉ fichier : chaque document a ses versions (**Versions — {document}**) |
| État et historique | ✅ | Onglets **Résumé** (statut) et **Historique** (journal scellé) |

### F-02 — Génération automatisée de contrats

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Récupérer données et métadonnées Salesforce | ✅🔌 | *Initiateur* : Créer une trousse → Origine **Salesforce** → `DEM-2026-0001` → **Rechercher** ; résumé du dossier affiché |
| Générer depuis un modèle approuvé | ✅ | Choisir un **Modèle de contrat** publié → **Enregistrer le brouillon** → onglet Documents → **Générer depuis le modèle** |
| Préremplir les champs | ✅ | Assistant, étape 1 : valeurs avec provenance « Salesforce » et date de récupération |
| Clauses et règles conditionnelles | ✅ | Éditeur de modèle : **Ajouter une clause** → **Définir une condition** ; **Tester la règle** |
| Version préliminaire, correction contrôlée | ✅ | **Corriger la valeur — {champ}** ; un écart avec Salesforce reste tracé comme conflit |
| Traçabilité source → document → preuve | ✅ | Onglet **Preuve** : identifiant du dossier Salesforce, empreintes, corrélation |
| Acheminer vers le parcours | ✅ | **Lancer le parcours** sur la trousse |

### F-03 — Modèles documentaires

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Créer et réutiliser | ✅ | *Initiateur ou administrateur d'affaires* : **Modèles** → **Créer un modèle** |
| Partager par groupes | ✅ | Éditeur : **Partage par groupes** (vide = toute l'organisation) |
| Positionner précisément les champs | ✅ | **Positionner sur le document** : zone choisie puis déplacée aux flèches du clavier |
| Champs autorisés par rôle | ✅ | Propriété **Rôle autorisé à compléter** de chaque champ |
| Cycle de vie et versions | ✅ | **Soumettre à publication** → *administrateur* **Publier** / **Renvoyer en brouillon** → **Nouvelle version** → **Retirer** |

### F-04 — Champs et saisie

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Texte avec format, liste, date, case à cocher | ✅ | Éditeur : type du champ et **Format (expression régulière)** ; à la saisie, une valeur hors format est refusée |
| Distinguer champs de formulaire, saisie et signature | ✅ | Page schématique : les zones de signature ont leur propre style ; tableau « Champs » par type |
| Attribuer un champ à un signataire | ✅ | Onglet **Participants** → **Signataire de la zone « … »** → **Attribuer la zone — …** |
| Règles conditionnelles d'affichage | ✅ | Éditeur : **Définir une condition** sur un champ ou une clause |
| Saisie assistée, suggestions | ✅ 🆕 | **Corriger la valeur — Demandeur** : flèche bas pour parcourir les valeurs déjà retenues par l'initiateur, de la plus fréquente à la moins fréquente |

### F-05 — Validation et approbation

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Séquentiel, parallèle, mixte | ✅ | Onglet **Parcours** : étapes d'une même phase en parallèle, phases successives ; **Tester le parcours** |
| Affectation par rôle, groupe ou règle | ✅ | **Ajouter un responsable** (utilisateur, rôle, groupe, participant) ; **Si le champ** / **Opérateur** pour une règle d'affaires |
| Ajouter, retirer, remplacer, réassigner en cours | ✅ | Trousse lancée → Parcours → **Réassigner, ajouter ou retirer des intervenants** ; ou, depuis la tâche, **Déléguer si autorisé** |
| Approuver, rejeter, retourner pour correction | ✅ | *Validateur* : **Mes tâches** → **Traiter** → **Valider** / **Rejeter** / **Retourner pour correction** (motif obligatoire) |
| Commentaires et historique | ✅ | **Nouveau commentaire** → **Ajouter un commentaire** ; bloc « Historique des validations » |
| Suivi des étapes, rappels configurables | ✅ | Onglet Parcours (étape active, échéance) ; échéance, quorum et rappels par étape et par trousse |

### F-06 — Signature électronique

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Séquentielle, parallèle, mixte ; groupes | ✅ | Étapes de type Signature, même phase = simultanées ; quorum par étape |
| Associer étapes et signataires | ✅ | Responsable de type « Participant » ou rôle du contrat (modèle) |
| Modifier ou réassigner en cours | ✅ | Comme F-05 ; un participant se retire tant qu'il n'a pas signé |
| Authentification selon profil et risque | ✅ | Risque de la trousse → niveau minimal (courriel, SMS, question, forte) ; politique dans **Paramètres et environnements** |
| Consentement, horodatage, preuve | ✅ | *Signataire* : **Signer maintenant** → vérification → **Lire / télécharger** → consentement → **Signer** → reçu |
| Certificat et document final | ✅ | Onglet **Preuve** → **Télécharger le certificat (PDF)** |
| Valeur probante qualifiée | ⏳ | Fournisseur de signature certifié et horodatage qualifié : choix d'IQ (ST-09) |

### F-07 — Notifications et rappels

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Activer, désactiver | ✅ | *Administrateur* : **Notifications et rappels** → **Désactiver — {événement} {canal}** / **Activer — …** |
| Rappels automatiques | ✅ | Fréquence et nombre de rappels par trousse ; valeurs par défaut dans **Paramètres et environnements** |
| Destinataires en copie | ✅ | Créer une trousse → **Destinataires en copie** |
| Messages et échéances | ✅ | **Modifier — {événement} {canal}** : sujet et message avec variables ; **Envoyer un test** |
| Alerte d'échéance imminente | ✅ 🆕 | Paramètres → **Préavis d'échéance (jours)** (0 = aucune alerte) ; message « Échéance proche » envoyé une fois aux intervenants qui n'ont pas répondu, inscrit au journal |

### F-08 — Suivi centralisé

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Vue des trousses, documents, signatures | ✅ | **Suivi centralisé** ; **Tableau de bord** |
| Étapes terminées, en attente, exceptions | ✅ | Colonnes d'avancement ; **Exceptions seulement** ; **Afficher seulement les exceptions** |
| Filtrer selon les droits | ✅ | Recherche, statut, unité, responsable, source, période ; chaque profil ne voit que son périmètre |
| Export | ✅ | **Exporter (CSV)** |

### F-09 — Gestion documentaire, versions et preuve

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| PDF et Word | ✅ | Comme F-01 |
| Versions, étapes, transactions | ✅ | Documents → **Versions — {document}** → **Consulter les métadonnées — version n** ; onglet Historique |
| Journaliser chaque événement | ✅ | Historique de la trousse ; **Journal d'audit et événements** pour toute l'organisation |
| Consulter, exporter signé, certificat, journal, métadonnées | ✅ | Onglet Preuve → **Exporter le dossier complet (ZIP)** ; journal → **Exporter le journal (CSV)** |
| Intégrité | ✅ | Onglet Preuve → **Vérifier l'intégrité** |
| Conservation et destruction | ⏳ | Durées et règles de destruction à fixer par IQ (calendrier de conservation) |

### F-10 — Intégration Salesforce et systèmes IQ

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| API et connecteurs documentés | ✅ | API REST `/api/clm` (OpenAPI) |
| Récupérer, retourner statuts et références | ✅🔌 | F-02 ; retours vers Salesforce dans **Intégrations API** (file d'événements) |
| Erreurs, reprises, transactions incomplètes | ✅ | *Administrateur* : **Intégrations API** → **Relancer l'événement — {code}** ; Suivi → **Relancer l'intégration — {référence}** |
| Corrélation dossier ↔ trousse ↔ preuve | ✅ | Onglet Preuve, « Métadonnées et liens de corrélation » |
| Connexion de l'organisation | ✅ | **Configurer la connexion** ; **Tester la connexion** ; secret jamais réaffiché |

### F-11 — Administration et contrôle d'accès

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| SSO des utilisateurs internes | ✅ ⏳ | *Administrateur* : **Authentification unique (SSO)**, OIDC ou SAML 2.0 ; branchement sur l'IdP d'IQ à faire (ST-01) |
| Authentification adaptée, renforcée | ✅ | Second facteur dans **Sécurité du compte** ; signataires : F-06 |
| Rôles, groupes, unités | ✅ | **Utilisateurs et groupes** → **Attribuer le rôle** ; unité responsable par trousse |
| Séparation des responsabilités | ✅ | L'initiateur ne peut ni valider ni approuver sa trousse ; l'auditeur consulte sans intervenir |
| Administration des modèles, parcours, paramètres | ✅ | Menus d'administration du module |
| Journal des changements de comptes et de rôles | ✅ 🆕 | Attribuer un rôle ou **Retirer l'accès** → **Journal d'audit** : `COMPTE_ROLES_MODIFIES` (avant → après), `COMPTE_DESACTIVE`, `COMPTE_CREE`, `COMPTE_SUPPRIME` |
| Domaines d'affaires, rôles d'administration distincts | ⏳ | À préciser par IQ (voir [tenant_iq_profils_et_roles.md](../11-Tenant-IQ/tenant_iq_profils_et_roles.md), § 7) |

### F-12 — Mobilité et expérience utilisateur

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Web, mobile, tablette | ✅ | Tout écran à 390, 768, 1280 et 1680 px ; portail du signataire à cibles de 44 px |
| Navigation simple et cohérente | ✅ | Gabarit commun des maquettes UI-01 à UI-24 ; recherche globale dans le bandeau |
| Image de marque, messages, parcours | ✅ | Portail du signataire au nom et au logo de l'organisation ; messages personnalisables (F-07) |
| Langue | ✅ | Sélecteur **Langue** du bandeau (français, anglais ; malgache à relire) |

### F-13 — Fonctionnalités optionnelles IA

| Sous-exigence | État | Depuis l'interface (profil) |
|---|---|---|
| Extraction, classification, anomalies | ✅ | Documents → **Analyser (IA) — {document}** : catégorie, confiance, anomalies |
| Contrôle humain | ✅ | **Valider le résultat** / **Signaler une erreur**, justification exigée ; rien n'est appliqué sans révision |
| Option facturée à part | ✅ | **Options CLM et IA** ; activation par le superadmin (module `clm_ia`) |

## 4. Écarts

### Comblés pendant cette validation

| Exigence | Écart constaté | Réalisation |
|---|---|---|
| F-04 | Aucune suggestion pendant la saisie | `Suggestions` (domaine) et `GET /api/clm/trousses/{id}/suggestions/{champ}` : valeurs retenues par l'initiateur dans ses propres trousses, jamais celles d'un collègue ; liste proposée dans le champ |
| F-07 | Alerte seulement après l'échéance dépassée | Alerte « Échéance proche » avant l'échéance, préavis réglé par l'organisation (0 à 30 jours, 1 par défaut), message modifiable et désactivable ; changelog `051` |
| F-11 | Les changements de comptes et de rôles n'étaient pas journalisés | Création, rôles (avant → après), désactivation, réactivation, réinitialisation du mot de passe et suppression consignées au journal d'audit ; le mot de passe n'y figure jamais |

### Restant, hors du code

Comment éprouver chacun de ces points dès maintenant (bacs à sable locaux et externes) : [guide des bacs à sable](../12-Bacs-a-sable/guide_bacs_a_sable.md). Questions correspondantes : [questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md).

| Exigence | Point | Décision attendue |
|---|---|---|
| F-02, F-10 | Recette sur le bac à sable Salesforce Grantmaking d'IQ | Accès au bac à sable et correspondance des champs |
| F-06 | Valeur probante qualifiée | Choix du fournisseur de signature et d'horodatage (ST-09) |
| F-09 | Conservation et destruction | Calendrier de conservation d'IQ |
| F-11 | IdP d'IQ, domaines d'affaires, rôles `ADMIN_AFFAIRES` / `ADMIN_TI` | Paramètres de l'IdP ; découpage voulu par IQ |
