# ADR-006 — Découpage du backend en contextes métier (DDD / Clean Architecture)

- **Statut** : Accepté
- **Date** : 2026-09-27
- **Portée** : `backend/src/main/java/qc/levis/contrat`

## Contexte

Le backend était organisé **par type technique** (`api`, `service`, `domain`, `repo`, `render`,
`config`) avec, en plus, quelques paquets « fonctionnels » incohérents (`auth`, `tenant`, `reference`,
`admin`) mélangeant les couches, et un fichier `Dtos.java` unique regroupant 54 records. Cela
contrevenait à la règle de dépendance et au découpage par contexte métier.

## Décision

Réorganisation **par contexte métier**, chaque contexte exposant les trois couches de la Clean
Architecture (**dépendances vers l'intérieur** : `infrastructure → application → domain`).

### Contextes

`auth`, `redaction`, `dossier`, `soumission`, `addenda`, `approbation`, `audit`, `tenant`,
`integration`, `ia`, `reference`, et le noyau partagé `commun`.

### Couches (par contexte)

| Sous-paquet | Contenu |
|---|---|
| `domain` | Entités, objets valeur, énumérations. Aucune dépendance à un framework applicatif. |
| `application` | Services (cas d'utilisation). |
| `application.repository` | Interfaces de dépôt (ports de persistance). |
| `application.port` | Autres ports (assistant IA, client M365, cible de publication, référence financière). |
| `application.dto` | DTO d'API — **un record par fichier**. |
| `infrastructure` | Adaptateurs sortants (clients HTTP, Graph, SAP, rendu PDF/DOCX, amorces de données). |
| `infrastructure.web` | Contrôleurs REST (adaptateurs entrants). |

`commun` porte le noyau transverse : `commun.domain.Enums`, utilitaires (`SecretCipher`, `LogSafe`,
`DiffUtil`), multilocation (`TenantContext`), `OpenApiConfig` et le `GlobalExceptionHandler`.

## Conséquences

- Chaque contrôleur est en `…infrastructure.web`, chaque entité en `…domain`, chaque dépôt en
  `…application.repository`, chaque DTO dans son propre fichier — conforme à la demande.
- **Comportement inchangé** : `mvn clean test-compile` vert ; **279 tests, 0 erreur** (l'unique échec
  résiduel, `HttpMicrosoft365ClientTest`, est **préexistant** — logique identique au code d'origine, lié
  aux travaux Teams/SharePoint en cours, sans rapport avec le repackaging).

## Concessions pragmatiques (dette assumée)

1. **JPA dans le domaine** : les entités restent annotées `@Entity` (pas de séparation modèle de
   domaine / modèle de persistance). Une purification hexagonale (modèles + mappeurs) est un chantier
   ultérieur si requis.
2. **Dépôts Spring Data comme ports d'application** : les interfaces `…Repository` (qui étendent
   `JpaRepository`) sont placées en `application.repository` et traitées comme ports ; l'adaptateur est
   généré par Spring au démarrage.
3. **Dépendances inter-contextes de service** : certains services d'application dépendent encore
   directement de services/entités d'un autre contexte (ex. `redaction` → `audit`). La règle « un module
   n'accède jamais aux entités d'un autre module » (ports de lecture / événements de domaine) reste à
   introduire contexte par contexte.
4. **Tests** : conservés dans leurs paquets d'origine (ils passent), sauf `PublicationServiceTest`
   déplacé dans le paquet de son sujet (`integration.application`) pour l'accès aux membres
   *package-private*. Le miroir complet de l'arborescence de test est un suivi recommandé.

## Options écartées

- **Conserver les couches à plat** (`api`/`service`/`domain`/`repo`) : rejeté — c'est le découpage par
  type technique que la norme proscrit.
- **Purification hexagonale complète immédiate** (domaine sans JPA, ports de lecture inter-contextes) :
  reporté — coût et risque disproportionnés pour un même livrable, à mener incrémentalement.
