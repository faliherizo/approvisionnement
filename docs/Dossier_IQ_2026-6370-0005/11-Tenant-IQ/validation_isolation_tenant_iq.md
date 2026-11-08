# Validation — isolation du tenant Investissement Québec

**Critère validé** : pour tout compte du tenant `iq`, les menus, pages, fonctionnalités et API offerts sont ceux du module
souscrit (Contrats & signature électronique et son option IA) ; aucune fonctionnalité d'un autre module — socle de
rédaction contractuelle de la Ville de Lévis, formulaires, processus, RH, paie, etc. — n'est présente ni atteignable.

**Verdict** : validé le 2026-11-03 pour les sept profils du client (détail des profils :
[tenant_iq_profils_et_roles.md](tenant_iq_profils_et_roles.md)). Écarts hors critère au § 8.

## 1. Méthode : trois niveaux de défense, chacun prouvé par des tests automatisés

| Niveau | Mécanisme | Preuve |
|---|---|---|
| Interface | Une organisation dont les modules actifs se limitent au CLM n'a que l'espace du module : toute page de la plateforme renvoie à `/clm`, les pages transverses ont leur équivalent dans l'espace ; les menus et routes du socle exigent le module `redaction` | `e2e/views/tenant-iq.spec.ts` |
| API | `@ModuleRequis` + intercepteur `GardeModules` (403) sur les 28 contrôleurs du socle ; `@modules.actif` sur ceux des autres modules ; refus quel que soit le rôle, administrateur compris | `IsolationTenantIqTest`, `GardeModulesTest` |
| Construction | Toute API relève d'un module activable ou d'une des 17 fonctions transverses motivées ; une annotation de méthode répète la garde de module qu'elle remplace | `ArchitectureModulesTest` |
| Données | Cloisonnement multi-tenant existant (contexte de tenant, requêtes bornées au tenant) | `IsolationTenantIqTest`, `ClmIntegrationTest`, e2e |

## 2. Menus : Ville de Lévis comparée à Investissement Québec

| Élément | Ville de Lévis (socle + CLM) | Investissement Québec (CLM seul) |
|---|---|---|
| Gabarit | Plateforme : bandeau, menus déroulants, pied de page | Espace CLM seul (gabarit des maquettes UI-01 à UI-24) |
| Documents (documents, dossiers, assistant guidé, bibliothèque, recherche) | ✔ | absent |
| Pilotage (tableau de bord, rapports, chat IA, dépôts simulés) | ✔ | absent |
| Administration du socle (templates, gabarits, circuits d'approbation, données et IA) | ✔ | absent |
| Formulaires, portail des demandes, espace RH, paie | si souscrits | absents |
| Contrats & signature | ✔ (menu déroulant puis espace CLM) | espace unique |
| Lien « Retour à la plateforme » dans l'espace CLM | ✔ | absent |
| Profil, sécurité du compte, comptes, authentification unique, journal de sécurité | pages de la plateforme | pages servies dans l'espace CLM |

## 3. Résultats par profil (e2e, `tenant-iq.spec.ts`)

Pour chaque profil : connexion, arrivée sur `/clm`, navigation latérale exacte, absence du gabarit de la plateforme et du
lien de retour, présence de « Mon profil », action « Créer une trousse » selon le rôle, modules actifs relus par l'API,
14 API hors module refusées, API du module accessible, contrôles géométriques et axe-core sur l'écran d'accueil.

| Profil | Navigation latérale observée | « Créer une trousse » | Modules actifs | API hors module | Accessibilité |
|---|---|---|---|---|---|
| Initiateur | 5 entrées principales | offerte | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Validateur | 5 entrées principales | offerte | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Approbateur | 5 entrées principales | offerte | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Signataire interne | 5 entrées principales | absente | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Administrateur d'affaires | 5 entrées + 6 d'administration | offerte | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Administrateur technique | 5 entrées + 6 d'administration | offerte | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |
| Auditeur / conformité | 5 entrées + « Journal d'audit » | absente | `clm`, `clm_ia` | 14/14 refusées (403) | 0 violation |

Entrées principales : Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé. Administration : Notifications
et rappels, Intégrations API, Paramètres et environnements, Utilisateurs et groupes, Journal d'audit, Options CLM et IA.

API hors module interrogées : `/api/dossiers`, `/api/documents`, `/api/clauses`, `/api/templates`, `/api/gabarits`,
`/api/stats`, `/api/search`, `/api/ia/knowledge`, `/api/reference/TYPE_CONTRAT` (socle) ; `/api/formulaires`,
`/api/mes-demandes`, `/api/rh/employes`, `/api/paie/taux`, `/api/applications` (autres modules).

## 4. Pages saisies directement (compte administrateur technique, le plus privilégié)

| Pages de la plateforme | Résultat |
|---|---|
| `/`, `/dossiers`, `/clauses`, `/assistant`, `/recherche`, `/tableau-de-bord`, `/rapports`, `/chat-ia`, `/simulateur`, `/templates`, `/gabarits`, `/workflows`, `/donnees`, `/documents/1`, `/formulaires`, `/mes-demandes`, `/nouvelle-demande`, `/taches`, `/approbations`, `/rh/personnel`, `/rh/feuilles-temps`, `/paie`, `/impot`, `/declarations`, `/applications`, `/notifications`, `/interop`, `/securite-avancee`, `/superadmin` (29) | renvoi à `/clm`, espace CLM affiché |
| `/profil` → `/clm/profil` ; `/securite` → `/clm/securite` ; `/utilisateurs` → `/clm/admin/utilisateurs` ; `/audit` → `/clm/admin/audit` ; `/parametres` → `/clm/admin/sso` | équivalent servi dans l'espace CLM |

Les quatre pages transverses de l'espace (profil, sécurité du compte, authentification unique, journal de sécurité) sont
contrôlées aux quatre largeurs de référence (390, 768, 1280, 1680 px) par `clm.view.spec.ts` : aucun débordement,
aucun chevauchement, cibles ≥ 24 px, contrôles nommés, un seul landmark principal, zéro violation axe-core.

## 5. API (serveur, `IsolationTenantIqTest`)

| Vérification | Compte | Résultat attendu et obtenu |
|---|---|---|
| Organisation `iq` amorcée, 7 comptes, un rôle par profil, aucun compte d'une autre organisation listé | administrateur technique | ✔ |
| `AUDITEUR` parmi les rôles assignables | administrateur technique | ✔ |
| Modules actifs = `clm`, `clm_ia` | chacun des 7 comptes | ✔ |
| 19 lectures du socle (dossiers, documents, clauses, templates, gabarits, modèle de document, circuits, statistiques, rapports, recherche, connaissances IA, paramètres IA, dépôts simulés, répertoires, listes de référence, administration des listes, intégrations M365 et SAP, réversibilité) | administrateur technique | 403 « Fonctionnalité non souscrite par votre organisation » |
| Assistant guidé du socle (`POST /api/assistant/analyser`) | administrateur technique | 403 |
| 14 lectures d'autres modules (formulaires, catalogue, demandes, tâches, approbations, notifications, rapports, interopérabilité, applications, groupes, employés, feuilles de temps, projets, paie) | administrateur technique | 403 |
| 8 fonctions transverses (session, profil, modules, comptes, rôles, journal d'audit, SSO OIDC et SAML) | administrateur technique | 200 |
| Module souscrit (`/api/clm/trousses`) | administrateur technique | 200 |
| Les 19 lectures du socle | administrateur de Lévis | 200 (Lévis inchangée) |
| Le superadmin active puis retire le socle pour IQ | superadmin, puis administrateur IQ | 200 puis 403 ; l'administrateur IQ ne peut pas l'activer (403) |
| Modifier, supprimer ou recréer un compte de Lévis | administrateur technique IQ | 404, 404, 400 ; compte de Lévis intact |
| Organisation `iq` dans la liste publique de connexion | anonyme | ✔ |

## 6. Auditeur et signataire interne

| Vérification | Preuve | Résultat |
|---|---|---|
| L'auditeur voit toute l'organisation (trousse d'une initiatrice où il n'intervient pas), jamais une trousse d'une autre organisation | e2e, `ClmIntegrationTest` | ✔ |
| L'auditeur consulte le détail (action unique « consulter la preuve »), l'historique, le dossier de preuve, et exporte l'archive de preuve | `ClmIntegrationTest` | ✔ |
| L'auditeur consulte et exporte le journal d'audit UI-23 ; l'écran passe axe-core et les contrôles géométriques | `ClmIntegrationTest`, e2e | ✔ |
| L'auditeur ne crée, ne modifie, ne commente, ne décide, ne lance ni ne révise d'analyse IA, n'administre rien | `ClmIntegrationTest` | 403 à chaque tentative |
| Le lien « Modifier le brouillon » n'est pas offert à l'auditeur | e2e | ✔ |
| Le signataire interne ne voit pas une trousse qui ne le désigne pas (détail 404, absente de la liste) et n'a pas accès au journal | `ClmIntegrationTest`, e2e | ✔ |
| Règle du domaine : la consultation d'audit ne fait pas de l'auditeur un intervenant | `CasLimitesDomaineClmTest` | ✔ |

## 7. Non-régression et qualité

| Suite | Résultat |
|---|---|
| Backend complet (`mvn test`) | 579 tests : 578 verts, 1 échec préexistant (§ 8) ; `IsolationTenantIqTest` relancé après l'ajout de son 7e test : 7/7 |
| Porte de couverture CLM (tests CLM seuls, une invocation) | 130 tests verts ; lignes 99,36 %, branches 87,88 %, classe la plus basse 96,5 % ; domaine 100 % |
| Classes ajoutées hors CLM | `GardeModules`, `TenantsDemoSeeder`, `ModuleSeeder` : 100 % lignes et branches ; `AdminUserService` (modifiée) : 97,6 % lignes |
| e2e `tenant-iq.spec.ts` | 10/10 |
| e2e `clm.view.spec.ts` (dont les 4 pages transverses × 4 largeurs) | 11/11 |
| e2e `clm.maquettes.spec.ts` (fidélité aux 24 maquettes) | 1/1 |
| e2e `clm.superadmin.spec.ts` | 1/1 |

## 8. Écarts relevés (hors critère)

| Écart | Portée | Suite proposée |
|---|---|---|
| `HttpMicrosoft365ClientTest.deposer_teamsAvecCanal_deposeDansLeDossierDuCanal` échoue | Préexistant : échoue de même sur le commit `36c0336` dans une copie propre ; client M365 du socle, non modifié | Corriger le client ou le serveur simulé Teams |
| Le portail public du signataire reste ouvert si le module CLM est retiré à une organisation | Le tenant n'est connu qu'à la lecture du jeton d'invitation | Vérifier la souscription dans le service du portail |
| Changements de comptes et de rôles non consignés au journal d'audit | Administration des comptes (UI-20, zone 05) | Journaliser création, changement de rôles, désactivation et suppression |
| Administrateurs d'affaires et technique sur un même rôle `ADMIN` | RBAC | Rôles distincts si IQ l'exige |
| `/api-docs` saisi en développement est relayé au serveur | Mandataire Vite (`/api` en préfixe) ; nginx en production ne relaie que `/api/` | Aucun impact en production |

## 9. Reproduire

```bash
# Backend : tests d'isolation, d'architecture et de la garde
cd backend && mvn -o test -Dtest='IsolationTenantIqTest,ArchitectureModulesTest,GardeModulesTest,TenantsDemoSeederTest'

# Porte de couverture CLM (une seule invocation)
rm -f target/jacoco.exec && mvn -o test -Dtest='qc.levis.contrat.clm.**.*Test' -Dsurefire.failIfNoSpecifiedTests=false \
  jacoco:report jacoco:check@check-clm

# e2e : backend sur 8081, Vite sur 5174, puis
cd e2e && npx playwright test --config playwright.clm-8081.tmp.config.ts views/tenant-iq.spec.ts
```

Connexion manuelle : `http://localhost:5173/login`, un compte du tableau du § 2 de
[tenant_iq_profils_et_roles.md](tenant_iq_profils_et_roles.md), mot de passe `Passw0rd!`.
