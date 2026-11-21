# Tests d'intrusion

Projet de tests d'intrusion automatisés de la plateforme. Il éprouve **chaque point d'accès** de l'API, lus dans
l'inventaire OpenAPI que publie le serveur. Il s'appuie aussi sur deux scanneurs de référence : OWASP ZAP (balayage
dynamique) et Trivy (dépendances, secrets, configuration).

- Scénarios détaillés : [`docs/securite/scenarios-tests-intrusion.md`](../docs/securite/scenarios-tests-intrusion.md)
- Rapport de la dernière campagne : [`docs/securite/rapport-tests-intrusion.md`](../docs/securite/rapport-tests-intrusion.md)

## Arborescence

```
securite/
├── tests/          01 à 10 : un fichier par famille OWASP (surface anonyme, rôles, organisations, personnes,
│                   authentification, injection, configuration, données sensibles, fichiers, SSRF)
├── donnees/
│   ├── surface-publique.json   opérations publiques par conception, chacune motivée (toute autre exige une session)
│   ├── roles.json              modifications légitimes d'un lecteur (toute autre lui est refusée)
│   ├── charges.json            charges d'attaque : injection SQL, XSS, traversée, SSRF, formules CSV, secrets
│   ├── constats.json           registre des constats : gravité, correction, test de non-régression, état
│   ├── acceptations.json       alertes d'outils acceptées, avec leur justification
│   └── conditions.json         conditions d'exploitation qui fondent le verdict
├── outils/
│   ├── api.ts                  sessions, inventaire OpenAPI, corps minimaux, organisations d'essai
│   ├── canari.ts               objets marqués de l'organisation A (étanchéité entre organisations)
│   ├── consigner.ts            bilans chiffrés de chaque test (resultats/*.json)
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

`lancer.sh` sort en erreur dans trois cas :
- un test d'intrusion échoue ;
- ZAP relève une alerte de risque élevé ;
- Trivy relève une vulnérabilité haute ou critique, un secret ou un défaut de configuration grave.

Une alerte ne cesse de bloquer que de deux façons. Soit elle est corrigée. Soit elle est acceptée dans
`donnees/acceptations.json`, avec une justification que le rapport reproduit.

## Ajouter un point d'accès

Rien à faire : l'inventaire est relu à chaque campagne.

- **S'il est public par conception**, l'ajouter à `surface-publique.json` avec sa raison d'être et sa protection.
  Sans cela, INT-01 échoue.
- **S'il manipule un nouvel objet par identifiant**, ajouter cet objet aux canaris (`outils/canari.ts`). Le
  balayage d'écriture d'INT-03 dresse la liste des opérations à identifiant qui n'ont pas de canari, et le rapport
  la reproduit.
