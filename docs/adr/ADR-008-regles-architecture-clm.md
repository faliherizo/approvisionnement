# ADR-008 — Règles d'architecture et porte de couverture du module CLM vérifiées à la construction

- **Statut** : Accepté
- **Date** : 2026-11-03
- **Portée** : `backend/src/main/java/qc/levis/contrat/clm`, `backend/pom.xml`, `ArchitectureClmTest`

## Contexte

La story ST-02 demande une séparation des responsabilités documentée et des tests d'architecture ; ADR-006 fixe la
règle de dépendance `infrastructure → application → domaine` mais la laissait vérifier en revue. Une vérification du
module a révélé deux écarts que la revue n'avait pas relevés : la couche application lisait le tenant dans
`commun.infrastructure.TenantContext` (état statique d'infrastructure), et le domaine dépendait d'un objet valeur du
module `regle`. La couverture de tests, elle, n'était mesurée qu'à la main, sans seuil bloquant.

## Options envisagées

1. **Revue de code seule** : aucun outillage, mais les écarts ci-dessus montrent qu'elle ne suffit pas.
2. **Script d'analyse des `import`** : simple, mais aveugle aux dépendances sans `import` (même paquet, noms
   qualifiés, annotations) et sans vocabulaire pour les couches.
3. **ArchUnit sur le bytecode, en test JUnit** (retenue) : dépendances réelles, règles lisibles, déjà disponible
   dans le dépôt Maven local, aucun impact à l'exécution (portée `test`).

## Décision

- `ArchitectureClmTest` vérifie, à chaque construction : dépendances vers le domaine ; domaine limité au JDK et à JPA
  (concession n° 1 d'ADR-006) ; couche application sans transport ni infrastructure ; aucune lecture d'entité ou de
  dépôt d'un autre module, et aucun autre module dépendant du CLM ; `@PreAuthorize` sur chaque API sauf le portail
  public à jeton ; ports réalisés en infrastructure ; chaque requête de dépôt bornée au tenant, sauf exceptions
  nommées et motivées dans le test.
- Le tenant passe par le port `PorteeTenant` (adaptateur `AdaptateursClm.PorteeTenantFil`), couvert par un test de
  contrat exécuté contre l'adaptateur et contre le double de test. Le domaine porte sa propre `Modele.Condition`,
  traduite vers le moteur de règles à la frontière applicative.
- La règle JaCoCo `check-clm` (phase `verify`) fait échouer la construction sous 96 % de lignes (agrégé et chaque
  classe), sous 86 % de branches (cliquet, cible 92 %) et sous 100 % pour le domaine.

## Conséquences

- Une dépendance interdite ou une API sans autorisation serveur casse la construction au lieu d'attendre la revue.
- Toute nouvelle requête non bornée au tenant oblige à écrire sa justification dans le test.
- Les seuils de couverture ne s'appliquent qu'au module CLM : les autres modules ne les atteignent pas encore.
  Les étendre exige d'abord de mesurer chaque module.
- Le seuil de branches est un cliquet : on le relève quand la couverture progresse, on ne l'abaisse jamais.
