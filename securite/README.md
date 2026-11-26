# Tests d'intrusion

Projet de tests d'intrusion automatisés de la plateforme. Il éprouve **chaque point d'accès** de l'API, lus dans
l'inventaire OpenAPI que publie le serveur. Il s'appuie aussi sur deux scanneurs de référence : OWASP ZAP (balayage
dynamique) et Trivy (dépendances, secrets, configuration).

- Scénarios détaillés : [`docs/securite/scenarios-tests-intrusion.md`](../docs/securite/scenarios-tests-intrusion.md)
- Rapport de la dernière campagne : [`docs/securite/rapport-tests-intrusion.md`](../docs/securite/rapport-tests-intrusion.md)

## Arborescence

```
securite/
├── tests/          01 à 11 : un fichier par famille OWASP (surface anonyme, rôles, organisations, personnes,
│                   authentification, injection, configuration, données sensibles, fichiers, SSRF, matrice des rôles
│                   et jetons forgés et limitation de débit)
├── donnees/
│   ├── surface-publique.json   opérations publiques par conception, chacune motivée, « jeton » ou « ouverte »
│   ├── authentification-seule.json  opérations ouvertes à tout rôle, avec le contrôle fin du service et son test
│   ├── matrice-roles.json      matrice rôle × opération DÉRIVÉE des @PreAuthorize (générée, revue en PR)
│   ├── referentiels.json       correspondance OWASP WSTG v4.2 / ASVS 4.0.3 niveau 2
│   ├── roles.json              modifications légitimes d'un lecteur (toute autre lui est refusée)
│   ├── charges.json            charges d'attaque : injection SQL, XSS, traversée, SSRF, formules CSV, secrets
│   ├── constats.json           registre des constats : vecteur CVSS 3.1, correction, test de non-régression, état
│   ├── acceptations.json       alertes d'outils acceptées, avec leur justification
│   └── conditions.json         conditions d'exploitation qui fondent le verdict
├── outils/
│   ├── api.ts                  sessions, inventaire OpenAPI, corps minimaux, organisations d'essai
│   ├── canari.ts               objets marqués de l'organisation A (étanchéité entre organisations)
│   ├── consigner.ts            bilans chiffrés de chaque test (resultats/*.json)
│   ├── cvss.mjs                score de base CVSS 3.1 calculé à partir du vecteur
│   └── rapport.mjs             rapport Markdown, à partir des résultats réels
└── lancer.sh                   campagne complète : tests, ZAP, Trivy, npm audit, rapport
```

## Exécuter

Prérequis :
- le serveur sur le port 8100, avec les variables des bacs à sable (voir [`bdd/README.md`](../bdd/README.md),
  section 3) ;
- Docker, pour ZAP et Trivy.

```bash
cd securite
npm ci
./lancer.sh                  # campagne complète (25 à 40 min, ZAP compris)
npx playwright test          # tests d'intrusion seulement (≈ 3 min)
npm run rapport              # régénère le rapport à partir des derniers résultats
```

Variable : `SECURITE_API_URL` (défaut `http://localhost:8100`).

Les organisations d'essai (`intrusion-*`) sont créées au besoin, avec tous les modules hors CLM : CLM est exclusif
des autres modules (ADR-010), et son étanchéité est éprouvée par `IsolationTenantIqTest`. Les essais de force brute
visent un compte créé pour l'occasion : aucun compte réel n'est bloqué.

## Verdict

`lancer.sh` sort en erreur dans ces cas :
- un test d'intrusion échoue ;
- ZAP n'a pas produit de rapport, n'a pas pu ouvrir de session, ou la session n'a pas tenu jusqu'à la fin du balayage
  actif (un poste sans Docker pose `SECURITE_OUTILS=facultatifs`) ;
- ZAP relève une alerte de risque élevé ;
- Trivy relève une vulnérabilité haute ou critique, un secret ou un défaut de configuration grave.

Une alerte ne cesse de bloquer que de deux façons. Soit elle est corrigée. Soit elle est acceptée dans
`donnees/acceptations.json`, avec une justification que le rapport reproduit.

## Aucune route n'échappe

La construction du serveur (`mvn verify`) part de la liste RÉELLE des routes de Spring MVC, pas de l'OpenAPI
(`backend/src/test/java/qc/levis/contrat/securite/RoutesSecuriseesIntegrationTest.java`) : chaque route doit figurer
dans l'OpenAPI, déclarer sa méthode HTTP et une règle d'accès explicite, répondre 401 sans session, 403 à chaque rôle
que la matrice lui refuse et 405 à une méthode non prévue ; chaque route publique répond 429 au-delà du seuil
(`PareFeuSurfacePubliqueIntegrationTest`). La matrice dérivée doit égaler `donnees/matrice-roles.json` : une
autorisation modifiée régénère le fichier (`mvn test -Dtest=RoutesSecuriseesIntegrationTest
-Dsecurite.matrice.regenerer=true`) et passe en revue.

## Ajouter un point d'accès

L'inventaire est relu à chaque campagne ; la construction du serveur exige une règle explicite :
- **réservé à certains rôles** : `@PreAuthorize`, puis régénérer la matrice ;
- **ouvert à tout rôle authentifié** (contrôle fin dans le service) : l'ajouter à `authentification-seule.json`, avec le
  contrôle et le test qui le verrouille ;

- **S'il est public par conception**, l'ajouter à `surface-publique.json` avec sa raison d'être, sa protection et son
  contrôle (`jeton` ou `ouvert`), et à une famille de la limitation de débit (`app.securite.pare-feu.familles`). Sans
  cela, INT-01 et la construction du serveur échouent.
- **S'il manipule un nouvel objet par identifiant**, ajouter cet objet aux canaris (`outils/canari.ts`). Le
  balayage d'écriture d'INT-03 dresse la liste des opérations à identifiant qui n'ont pas de canari, et le rapport
  la reproduit.
