# Journalisation applicative (logs)

> **Public visé** : équipe de développement et d'exploitation.
> **Cadre** : SLF4J + Logback (fournis par Spring Boot). Niveaux pilotés par la configuration YAML,
> par profil. **Aucune donnée sensible n'est journalisée** (Loi 25).

Ce document décrit *quoi* journaliser, *à quel niveau*, *comment le configurer* et *ce qui ne doit
jamais apparaître dans les journaux*. Il est écrit **avant** l'implémentation et en fait foi.

---

## 1. Principes

- **API de log** : `org.slf4j.Logger` obtenu par `LoggerFactory.getLogger(MaClasse.class)`. Jamais
  `System.out`/`System.err`, jamais `printStackTrace()`.
- **Messages paramétrés** : `log.info("Document {} publié vers {}", documentId, cible)` — jamais de
  concaténation de chaînes (performance + sécurité).
- **Niveaux pilotés par la configuration** : on ne change pas le code pour ajuster la verbosité ; on
  ajuste `logging.level.*` dans le YAML (par profil). Le code émet à tous les niveaux ; le runtime
  filtre selon la configuration active.
- **Une journalisation ≠ un audit métier**. Le **journal d'audit** (`AuditService`, en base, exposé
  par l'API) reste la source de vérité fonctionnelle et réglementaire. Les logs techniques servent au
  diagnostic et à l'exploitation ; ils sont complémentaires, pas redondants.

---

## 2. Niveaux et usage attendu

| Niveau | Quand l'utiliser | Exemples dans cette application |
|--------|------------------|--------------------------------|
| **ERROR** | Échec non récupérable d'une opération ; incident nécessitant une intervention | Échec d'un dépôt Microsoft 365 réel, exception non prévue interceptée globalement, rasterisation d'aperçu impossible |
| **WARN** | Situation anormale mais gérée ; décision de repli ; requête refusée par une règle métier | Publication refusée car document non validé (409), configuration M365 incomplète, aucun adaptateur pour une cible, échec de connexion au test M365 |
| **INFO** | Événements métier significatifs, jalons du cycle de vie | Document créé / assemblé / approuvé / publié, tenant créé ou mis à jour, référence SAP transmise, import de contenus, connexion / déconnexion |
| **DEBUG** | Détail de flux utile au diagnostic, sans bruit en production | Adaptateur retenu pour une cible, format et gabarit de rendu, résolution de configuration par tenant, nombre d'étapes du circuit d'approbation |
| **TRACE** | Très fin, ponctuel (désactivé partout par défaut) | Non utilisé en routine |

**Règle** : la production tourne en **INFO** pour le code applicatif et **WARN** pour les cadriciels ;
le développement peut passer le paquet applicatif en **DEBUG**.

---

## 3. Données sensibles — interdits absolus

**Ne JAMAIS journaliser**, à aucun niveau :

- mots de passe, empreintes de mots de passe (hash) ;
- jetons (JWT `ACCESS_TOKEN`, jetons Microsoft Graph, jetons de signature) ;
- **secrets client Microsoft 365**, clés/sel de chiffrement (`app.crypto.*`) ;
- secrets MFA / codes TOTP / réponses aux questions secrètes ;
- **contenu** des documents, clauses, pièces jointes ou octets rendus ;
- renseignements personnels identifiables au-delà du strict nécessaire (courriels, noms) — Loi 25.

**Autorisé** (non sensible, utile au diagnostic) :

- identifiants techniques : `documentId`, `dossierId`, `tenantId`, `stepId`, id d'utilisateur ;
- libellés d'action et de statut : `PUBLIER`, `APPROUVE`, `SHAREPOINT`, `EN_APPROBATION` ;
- métadonnées : taille en octets, format, code d'erreur HTTP, durée, nombre d'éléments.

**Masquage** : si un identifiant à caractère personnel est indispensable au diagnostic (ex. courriel
lors d'un échec d'authentification), le **masquer** (`j***@domaine`) plutôt que l'inscrire en clair, et
préférer l'**id technique** quand il existe. Les secrets ne sont jamais masqués : ils sont **absents**.

---

## 3.1 Journalisation des actions utilisateurs

Chaque **action utilisateur** enregistrée dans le journal d'audit (`AuditService`) est aussi émise dans
les journaux techniques, au niveau **INFO**, sous la forme :

```
Action utilisateur : j***@ville.qc.ca · PUBLIER Document#42 (tenant 1) · SHAREPOINT -> sim://...
```

- Point unique : toutes les actions passent par `AuditService.journaliser(...)`, qui journalise en plus
  de persister — aucune duplication, couverture complète (création, assemblage, approbation,
  publication, référence SAP, connexion/déconnexion, gestion des utilisateurs, etc.).
- **Courriel masqué** (`LogSafe.courriel`) : `j***@domaine`. L'**identité complète** reste dans le
  **journal d'audit en base** (seul endroit autorisé, exigence 9 / Loi 25) ; les journaux techniques ne
  conservent pas de donnée personnelle en clair.
- Les **consultations** (GET) ne sont pas des « actions » et ne sont pas tracées à ce titre ; le détail
  de flux relève du niveau DEBUG.
- **Détail confidentiel** (Annexe 10 d'IQ, AF-34/SEC-15 ; ADR-030) : une modification de valeur d'une trousse
  (`CLM_VALEUR_MODIFIEE`, ancienne et nouvelle valeur) passe par `AuditService.logConfidentiel` : le détail complet est
  conservé au journal d'audit en base et au journal scellé de la trousse (la preuve), mais la ligne technique ne dit que
  `… · détail au journal d'audit`. Le journal transmis au SIEM d'IQ (ADR-033) omet aussi ce détail.
  Test : `AuditServiceConfidentielTest`.

## 4. Ce qui est journalisé, par domaine

| Domaine | INFO | WARN | ERROR | DEBUG |
|---------|------|------|-------|-------|
| Authentification (`AuthService`) | connexion réussie (id user), déconnexion | échec de connexion (courriel masqué, sans mot de passe) | — | méthode MFA évaluée |
| Documents (`DocumentService`) | création, assemblage, démarrage d'approbation, décision, statut final | — | — | nb clauses, circuit résolu |
| Publication (`PublicationService`) | dépôt effectué (documentId, cible, dossier) | document non validé → refus | — | adaptateur retenu, format |
| Adaptateurs M365 (`Graph…`, `Simulation…`) | — | échec Graph par tenant | exception réseau Graph (sans secret) | cible, dossier de dépôt |
| Config M365 (`IntegrationM365Service`) | configuration enregistrée (tenantId, actif) — **jamais le secret** | — | — | présence du secret (booléen) |
| SAP (`SapReferenceService`) | référence transmise (dossierId, n° AO) | — | — | montant agrégé |
| Import (`ImportService`) | import réalisé (nb valides / rejetés) | lignes invalides (compte) | — | — |
| Tenants (`TenantAdminService`) | tenant créé / mis à jour (slug, actif) | slug déjà utilisé | — | — |
| Exceptions (`GlobalExceptionHandler`) | — | 400 / 404 / 409 (message métier) | 500 inattendu (avec pile) | — |

---

## 5. Configuration YAML (par profil)

La verbosité se règle **uniquement** par configuration. Racine du paquet applicatif :
`qc.levis.contrat`.

`application.yml` (défaut / développement) :

```yaml
logging:
  level:
    root: INFO
    qc.levis.contrat: ${LOG_APP_LEVEL:DEBUG}   # applicatif en DEBUG en dev
    org.springframework: WARN
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n"
```

`application-prod.yml` (production) :

```yaml
logging:
  level:
    root: WARN
    qc.levis.contrat: ${LOG_APP_LEVEL:INFO}    # applicatif en INFO en prod
    org.springframework: WARN
  file:
    name: ${LOG_FILE:/var/log/contrat/app.log}
```

- **`LOG_APP_LEVEL`** permet d'ajuster le niveau applicatif sans redéploiement (variable
  d'environnement), utile pour un diagnostic ponctuel en production.
- En production, la sortie fichier (`logging.file.name`) alimente la collecte (Log Analytics /
  agrégateur). Aucune donnée sensible n'y transite, par construction (section 3).

---

## 6. Corrélation (option)

Le `tenantId` courant (déjà porté par `TenantContext`) peut être ajouté au **MDC** en début de requête
pour tracer les journaux par organisation (`%X{tenantId}` dans le motif). Optionnel ; à activer si le
volume multi-tenant le justifie. Le MDC ne doit contenir que des identifiants techniques.

---

## 7. Vérification

- **Revue** : aucune concaténation dans les messages, aucun secret/jeton/mot de passe en argument de
  log, `printStackTrace()`/`System.out` absents (vérifiable par recherche).
- **Tests** : les tests existants continuent de passer ; la journalisation n'altère pas le comportement
  observable. Un test de non-régression peut vérifier qu'un échec d'authentification ne fait pas
  apparaître le mot de passe dans les journaux (assertion sur un `ListAppender` Logback).
