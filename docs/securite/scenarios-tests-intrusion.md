# Scénarios des tests d'intrusion

Ce document décrit ce que chaque test d'intrusion cherche à faire, comment il s'y prend et ce qui constitue un échec.
Les tests sont automatisés dans [`securite/tests/`](../../securite/tests/) et s'exécutent contre le serveur réel par
[`securite/lancer.sh`](../../securite/lancer.sh). Les résultats de la dernière campagne sont dans
[`rapport-tests-intrusion.md`](rapport-tests-intrusion.md).

Référentiel : OWASP Top 10 2021 et OWASP API Security Top 10 2023. Chaque scénario prend la position d'un attaquant
réaliste : une personne sans compte, un employé au rôle le plus faible, ou l'administrateur d'une autre organisation
cliente de la même plateforme.

## Principe : chaque point d'accès, sans liste à tenir à la main

Les balayages INT-01, INT-02, INT-03 et INT-08 ne reposent pas sur une liste d'URL écrite à la main. Ils lisent
l'inventaire complet des opérations dans la spécification OpenAPI que publie le serveur (`/v3/api-docs`). Une
opération ajoutée demain est donc éprouvée dès la campagne suivante. Le seul fichier tenu à la main est la liste des
opérations publiques par conception, [`surface-publique.json`](../../securite/donnees/surface-publique.json). Chaque
entrée y porte sa raison d'être publique et le mécanisme qui la protège.

## Méthode du canari (étanchéité entre organisations)

La plateforme est multi-organisation : la Ville de Lévis, Investissement Québec et un CHU partagent la même
application. La fuite la plus grave serait qu'une organisation voie ou modifie les données d'une autre. Le test
procède ainsi :

1. Deux organisations d'essai, A et B, sont créées avec tous les modules activés.
2. A crée un objet de chaque espèce : clause, devis, formulaire publié, fiche soumise, conversation, tableau de bord,
   critère de recherche, compte, banque d'heures. Tous portent une marque unique, du type `CANARI-…`.
3. L'administrateur et le lecteur de B appellent **toutes** les opérations de lecture. Aucune réponse ne doit
   contenir la marque de A.
4. L'administrateur de B appelle toutes les opérations qui prennent un identifiant, lecture, modification et
   suppression, avec les identifiants des objets de A. Aucune ne doit réussir.
5. On vérifie enfin que chaque canari de A existe encore, inchangé.

## Scénarios

| N° | OWASP | Attaquant | Ce qu'il tente | Échec si |
|---|---|---|---|---|
| INT-01-a | A01, A07 | anonyme | appeler chaque opération non publique de l'inventaire | une réponse autre que 401 |
| INT-01-b | A05 | anonyme | appeler chaque opération publique avec des valeurs absurdes | erreur 500, trace d'exécution ou secret dans la réponse |
| INT-02-a | A01 | lecteur | exécuter chaque modification (POST, PUT, PATCH, DELETE) hors de son périmètre, sur les vrais identifiants de son organisation | une réponse 2xx, une erreur 500, ou un canari altéré |
| INT-02-b | A01 | administrateur d'organisation | appeler chaque opération de l'administration de la plateforme (`/api/superadmin/**`) | une réponse autre que 403 |
| INT-03-a | A01 | administrateur et lecteur de B | lire toute l'API à la recherche des données de A | la marque de A apparaît dans une réponse |
| INT-03-b | A01 (IDOR) | administrateur de B | lire, modifier et supprimer les objets de A par leur identifiant | une réponse 2xx, ou un canari de A altéré |
| INT-04 | A01 (IDOR horizontal) | employé de la même organisation | lire, modifier ou annuler la fiche d'un collègue, obtenir son document PDF, lire sa conversation, supprimer son critère | une réponse 2xx, ou la fiche altérée |
| INT-05-a | A07 | anonyme | deviner un mot de passe par 12 essais rapides ; comparer les réponses pour un compte inconnu et un compte existant | pas de blocage (429) au-delà du seuil, ou des réponses différentes qui révèlent l'existence du compte |
| INT-05-b | A02, A07 | anonyme muni d'un jeton volé | élever son rôle en modifiant le jeton, retirer la signature (`alg: none`), rejouer un jeton expiré ou inventé | un jeton falsifié accepté |
| INT-05-c | A07 | voleur de jeton | rejouer le jeton d'une session après la déconnexion ; lire le cookie depuis un script | jeton rejoué accepté, cookie sans `HttpOnly` ou sans `SameSite` |
| INT-05-d | A07 | administrateur | créer un compte au mot de passe faible (« abc ») | le compte est créé |
| INT-06-a | A03 | administrateur | injecter du SQL dans les paramètres de recherche de toutes les listes | erreur 500, message de base de données, empreinte de mot de passe dans la réponse |
| INT-06-b | A03 | rédacteur | stocker du HTML ou du JavaScript (clause) | contenu servi autrement qu'en JSON, sans `X-Content-Type-Options: nosniff` |
| INT-06-c | A01, A03 | rédacteur | lire `/etc/passwd` par traversée de répertoires dans le dépôt de documents | un fichier hors du dépôt de l'organisation est lu |
| INT-07-a | A05 | tous | — | l'API ne pose pas `X-Content-Type-Options`, `X-Frame-Options: DENY`, une CSP, `Referrer-Policy`, `Cache-Control: no-store` |
| INT-07-b | A05 | tous | — | le frontal de production ne pose pas CSP, HSTS, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`, ou affiche sa version |
| INT-07-c | A05 | site tiers | obtenir une autorisation CORS pour une origine étrangère | l'origine étrangère est autorisée |
| INT-07-d | A01 (CSRF) | site tiers | faire exécuter une modification par le navigateur d'une personne connectée | la requête d'origine étrangère est acceptée |
| INT-07-e | A05 | anonyme | provoquer une trace d'exécution (JSON malformé) ; utiliser `TRACE` ; atteindre `/actuator`, `/h2-console`, `/.git` | une trace, une méthode acceptée ou un point d'administration exposé |
| INT-07-f | A05 | — | revue du profil de production | console H2 active, documentation d'API publique par défaut, cookie non `Secure` par défaut |
| INT-08-a | A02, A04 | administrateur, superadministrateur | lire toutes les réponses à la recherche d'empreintes de mot de passe, de secrets TOTP, de secrets clients, de clés privées | un secret dans une réponse |
| INT-08-b | A08 (affectation de masse) | administrateur d'organisation | se créer un compte SUPERADMIN, ou créer un compte dans une autre organisation, en ajoutant des champs à la requête | le privilège ou l'organisation demandés sont accordés |
| INT-09-a | A04 | rédacteur | téléverser un fichier de 60 Mo | une réponse autre que 413 |
| INT-09-b | A03, A04 | rédacteur | téléverser un faux document Word (script, nom `../../evil.docx`) | une réponse 2xx ou une erreur 500 |
| INT-10 | A10 (SSRF) | administrateur | faire interroger par le serveur les métadonnées infonuagiques (169.254.169.254), le serveur lui-même, `file://` | la requête sortante est émise |

## Outils externes

| Outil | Ce qu'il apporte | Échec si |
|---|---|---|
| OWASP ZAP 2.16 (`zap-api-scan`) | balayage passif et actif de toute l'API, à partir de l'OpenAPI, avec une session : injections, en-têtes, divulgations, erreurs | une alerte de risque élevé non corrigée, ou non acceptée avec justification dans [`acceptations.json`](../../securite/donnees/acceptations.json) |
| Trivy 0.58 | vulnérabilités connues (CVE) des dépendances Maven et npm, secrets oubliés dans le dépôt, configuration des Dockerfile, du Terraform et du Kubernetes | une vulnérabilité haute ou critique, un secret ou un défaut de configuration grave non traité |
| `npm audit` | vulnérabilités des dépendances de production du frontend | consigné au rapport |

## Exécution

```bash
# serveur sur 8100 (voir bdd/README.md, section 3), puis :
cd securite && npm ci && ./lancer.sh
```

Les essais de force brute visent un compte créé pour l'occasion dans une organisation d'essai : aucun compte réel
n'est bloqué. Les organisations d'essai (`intrusion-*`) ne sont créées que si elles n'existent pas encore.
