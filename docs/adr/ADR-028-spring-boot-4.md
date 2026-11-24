# ADR-028 — Passage à Spring Boot 4 (Spring Framework 7)

- **Statut** : Accepté
- **Date** : 2026-11-24
- **Portée** : `backend/pom.xml`, `SecurityConfig`, `DonneesValidation`, `application.yml`, tests d'intégration (MockMvc).
- **Origine** : porte Trivy du déploiement continu (`deployer-contrat.yml`) : la mise en service de la démonstration
  était bloquée par des vulnérabilités élevées et critiques dont le correctif existe.

## Contexte

- **Une version ancienne en service** : Spring Boot 3.2.5, avec 50 vulnérabilités élevées ou critiques corrigées en amont
  (Spring, Tomcat, Spring Security, Jackson, Bouncy Castle).
- **Une branche 3 qui ne suffit plus** : Spring Boot 3.5.16 et des versions surchargées (Tomcat, Jackson) en retirent 48.
  Mais CVE-2026-47884 et CVE-2026-47890 (`spring-webmvc`) ne sont corrigées qu'en Spring Framework 7.0.9.
- Une exemption datée (`.trivyignore`, échéance 2027-01-31) a permis de livrer la 3.5.16 le 2026-11-24, en attendant.

## Options envisagées

1. **Rester sur Spring Boot 3.5 avec l'exemption** : livrable aussitôt, mais deux vulnérabilités critiques connues
   restent en service, et la branche 3 ne reçoit plus de correctifs publics.
2. **Spring Boot 4, Jackson 3 compris** : le plus propre à terme, mais Jackson 3 change de paquet
   (`tools.jackson`) et d'exceptions (non vérifiées) dans 88 fichiers. Trop de changements pour une même livraison.
3. **Spring Boot 4, Jackson 2 conservé** par le module de compatibilité `spring-boot-jackson2` : Spring 7 et tous les
   correctifs de sécurité, migration de Jackson faite à part.

## Décision

Option 3.

- **Version** : Spring Boot 4.2.0 (Spring Framework 7.1).
- **Démarreurs** :
  - `spring-boot-starter-webmvc` remplace `-web` ;
  - `spring-boot-starter-liquibase` (l'autoconfiguration est devenue un module) ;
  - `spring-boot-starter-webmvc-test` pour MockMvc.
- **JSON** :
  - `spring-boot-jackson2` ;
  - `spring.http.converters.preferred-json-mapper: jackson2`, sans quoi les contrôleurs qui échangent des types
    Jackson 2 (`JsonNode`, annotations) répondent 400.
- **Sécurité** (Spring Security 7) :
  - `RoleHierarchyImpl.fromHierarchy` ;
  - `PathPatternRequestMatcher` au lieu d'`AntPathRequestMatcher` ;
  - en-tête `Permissions-Policy` écrit directement, l'API `permissionsPolicy` étant retirée ;
  - même comportement (mêmes chemins, mêmes rôles, mêmes en-têtes).
- **springdoc 3.2.1**.
- **`jaxb-api` déclarée explicitement** : xmpbox 2.0 en a besoin pour lire les dates XMP. Sans elle, toute lecture des
  métadonnées PDF/A échoue ; un module de la branche 3 la fournissait implicitement.
- **Bouncy Castle 1.86** : la signature extraite d'un PDF ne garde que son premier objet DER, car l'emplacement
  `/Contents` est bourré de zéros, ce que Bouncy Castle ≥ 1.85 refuse.
- **Exemption Trivy retirée** : Trivy ne relève plus aucune vulnérabilité élevée ou critique dans le serveur.

## Conséquences

- **Positives** : aucune vulnérabilité élevée ou critique connue dans le serveur ; retour sur une branche maintenue.
- **Négatives** : Jackson 2 repose sur un module de compatibilité, qui disparaîtra dans une version future de Spring
  Boot. La migration vers Jackson 3 (88 fichiers) est à planifier.
- **Vérification** :
  - 1 048 tests et portes de couverture JaCoCo (globale et par classe) ;
  - campagnes BDD et e2e complètes contre un serveur Spring Boot 4 (bacs à sable réels) ;
  - recette en lecture seule de l'environnement déployé ;
  - Trivy sans exemption.
