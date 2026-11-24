# Rapport des tests d'intrusion — plateforme contractuelle

> Document généré par `securite/outils/rapport.mjs` à partir des résultats de `securite/lancer.sh` ; ne pas le modifier à la main.

Version éprouvée : `54c96e3 + modifications locales non commitées` · produit le 2026-11-22 14:05 UTC · cible : serveur de recette branché sur les bacs à sable.

## 1. Verdict

**Aucune vulnérabilité exploitable n'est ouverte.** Les 26 tests d'intrusion réussissent, aucune alerte ZAP de risque élevé ni aucune vulnérabilité de dépendance haute ou critique ne reste sans correction ou sans acceptation motivée. Les constats relevés pendant la campagne ont été corrigés et chacun est verrouillé par un test de non-régression (section 4).

## 2. Périmètre et méthode

- **Inventaire** : 439 opérations d'API lues dans la spécification OpenAPI que publie le serveur ; 46 sont publiques par conception (tableau 2.1), toutes les autres exigent une session.
- **Tests automatisés** (`securite/tests/`) : balayages génériques de **chaque** opération (accès anonyme, moindre privilège, étanchéité entre organisations par la méthode du canari) et tests ciblés OWASP Top 10 2021 (authentification, injection, configuration, données sensibles, fichiers, SSRF).
- **Balayage dynamique** : OWASP ZAP 2.16 (règles passives et actives) sur toute l'API, avec une session authentifiée.
- **Analyse de composition et de secrets** : Trivy (dépendances Maven et npm, secrets, Dockerfile, Terraform, Kubernetes) et `npm audit`.
- **Scénarios détaillés** : [`scenarios-tests-intrusion.md`](scenarios-tests-intrusion.md).

### 2.1 Surface publique (sans session)

| Opérations | Pourquoi publiques | Protection |
|---|---|---|
| `^POST /api/auth/login$` | Connexion. | Mot de passe haché (BCrypt), second facteur, limitation des essais. |
| `^POST /api/auth/logout$` | Déconnexion (efface le cookie). | Aucune donnée exposée. |
| `^GET /api/auth/config$` | Options de connexion (SSO actif ou non). | Aucune donnée personnelle. |
| `^(GET|POST) /api/auth/(oidc|saml)/` | Flux d'authentification unique OIDC et SAML. | PKCE, état et nonce (OIDC), signature des assertions (SAML). |
| `^(GET|POST) /api/signature/` | Signature externe par lien. | Jeton à usage limité, imprévisible, expirant. |
| `^(GET|POST|PUT) /api/clm/portail/` | Portail du signataire externe (IQ). | Jeton d'invitation et vérification d'identité (code). |
| `^(GET|POST) /api/clm/integration/mcp$` | Connecteur MCP d'un assistant d'IA (IQ-A-09, ADR-023). | Jeton client credentials d'un client d'intégration ; outils en lecture seule bornés à ce client ; origine étrangère refusée. |
| ` /api/clm/integration/` | API d'intégration entrante (Workday, Salesforce). | Jeton OAuth client credentials vérifié par le contrôleur. |
| ` /scim/v2/` | Provisionnement SCIM 2.0. | Jeton porteur propre à l'organisation. |
| `^GET /api/tenants$` | Sélecteur d'organisation de la page de connexion. | Slug et titre seulement. |
| `^GET /api/tenants/[^/]+/public$` | Titre et thème de la page de connexion. | Données de présentation seulement. |
| `^GET /api/health$` | Sonde de disponibilité. | État seulement. |
| `^(GET|POST) /api/public/v1/echanges/` | Échanges hérités de la plateforme précédente (CHU-8.04, ADR-021). | Jeton d'API vérifié par le contrôleur, module interop requis, nom de champ inconnu refusé. |
| ` /api/public/` | API publique par jeton d'API. | Jeton d'API ou OAuth vérifié par le contrôleur. |

## 3. Résultats des tests d'intrusion automatisés

| Test | OWASP | Scénario | Résultat |
|---|---|---|---|
| INT-01 | A01 · A07 | chaque opération non publique refuse l'accès anonyme par un 401 | ✅ réussi |
| INT-01 | A01 · A07 | les opérations publiques ne divulguent ni erreur interne ni secret | ✅ réussi |
| INT-02 | A01 | un lecteur ne peut exécuter aucune modification réservée | ✅ réussi |
| INT-02 | A01 | un administrateur d'organisation n'atteint aucune opération de la plateforme | ✅ réussi |
| INT-03 | A01 | aucune réponse de l'organisation B ne contient les données de A | ✅ réussi |
| INT-03 | A01 | aucun identifiant de A n'est modifiable ou supprimable depuis B | ✅ réussi |
| INT-04 | A01 | un demandeur ne lit ni la fiche, ni le document, ni les conversations, ni les critères d'un autre | ✅ réussi |
| INT-05 | A07 · A02 | la force brute est freinée, sans révéler si le compte existe | ✅ réussi |
| INT-05 | A07 · A02 | un jeton falsifié, sans signature ou expiré est refusé | ✅ réussi |
| INT-05 | A07 · A02 | le cookie de session est HttpOnly et SameSite ; la déconnexion invalide la session | ✅ réussi |
| INT-05 | A07 · A02 | un mot de passe faible est refusé | ✅ réussi |
| INT-06 | A03 | aucune charge d'injection SQL ne provoque d'erreur ni ne divulgue la base | ✅ réussi |
| INT-06 | A03 | une charge XSS est conservée comme texte et servie en JSON non interprétable | ✅ réussi |
| INT-06 | A03 | aucune traversée de répertoires n'atteint un fichier hors du dépôt de l'organisation | ✅ réussi |
| INT-07 | A05 | l'API pose les en-têtes de sécurité | ✅ réussi |
| INT-07 | A05 | le frontal de production pose CSP, HSTS et la protection contre l'encadrement | ✅ réussi |
| INT-07 | A05 | une origine étrangère n'obtient pas l'autorisation CORS | ✅ réussi |
| INT-07 | A05 | une requête d'une origine étrangère qui modifie est refusée (CSRF) | ✅ réussi |
| INT-07 | A05 | les erreurs ne divulguent aucune trace et les méthodes dangereuses sont fermées | ✅ réussi |
| INT-07 | A05 | le profil de production refuse les réglages de développement | ✅ réussi |
| INT-08 | A02 · A04 · A08 | aucune réponse ne contient d'empreinte de mot de passe, de secret TOTP ni de secret client | ✅ réussi |
| INT-08 | A02 · A04 · A08 | aucune opération d'écriture n'accepte un numéro de carte de paiement (PCI DSS hors périmètre) | ✅ réussi |
| INT-08 | A02 · A04 · A08 | un administrateur d'organisation ne se donne ni ne donne de privilège de plateforme | ✅ réussi |
| INT-09 | A04 · A05 | un fichier trop volumineux est refusé par un 413 | ✅ réussi |
| INT-09 | A04 · A05 | un faux document Word (HTML ou script) est refusé | ✅ réussi |
| INT-10 | A10 | le test de découverte OIDC refuse les adresses internes et les schémas non HTTP | ✅ réussi |

Volume éprouvé :

- 393 opérations éprouvées sans session
- 208 modifications tentées par un lecteur
- 22 opérations de plateforme tentées par un administrateur d'organisation
- 139 lectures de l'organisation B balayées à la recherche du canari de A
- 88 opérations de B dirigées sur les identifiants de A
- 380 charges d'injection SQL envoyées
- 278 réponses inspectées à la recherche de secrets
- 197 opérations d'écriture éprouvées avec un numéro de carte

Opérations à paramètre de chemin sans canari dans l'organisation A (118) : leur étanchéité repose sur les tests d'intégration de leur module (organisation d'essai distincte) et sur le balayage de lecture ci-dessus.

## 4. Constats et corrections

| N° | Gravité | OWASP | Constat | Correction | Verrou de non-régression | État |
|---|---|---|---|---|---|---|
| SEC-01 | Élevée | A07 | Aucun frein à la force brute : 12 essais de mot de passe d'affilée, puis le bon est accepté. | Au-delà de 10 échecs en 15 minutes pour un même courriel, la connexion répond 429, même avec le bon mot de passe ; un compte inconnu est freiné pareil (aucune énumération). Compte tenu en base (journal d'audit) : commun à toutes les instances. | INT-05-a ; DurcissementIntegrationTest ; LimiteurEtRevocationTest | corrigé |
| SEC-02 | Élevée | A02 · A05 | Le profil de production démarrait avec le secret JWT, la clé de chiffrement et le mot de passe de base de données par défaut écrits dans la configuration : un secret JWT connu permet de forger une session de superadministrateur. | ExigencesProduction refuse le démarrage en production avec une valeur par défaut, un secret JWT de moins de 256 bits ou un cookie non Secure ; docker-compose exige des secrets propres au poste, générés par scripts/generer-secrets.sh. | ExigencesProductionTest ; INT-07-f | corrigé |
| SEC-03 | Élevée | A01 | Une clause d'une autre organisation se lisait, se modifiait et se supprimait par son identifiant (lecture sans contrôle d'organisation). | Toute clause hors de l'organisation courante est introuvable (404), en lecture comme en écriture. | INT-03-b ; ClauseVersionIntegrationTest | corrigé |
| SEC-04 | Élevée | A01 | Fiches : tout membre de l'organisation lisait, modifiait ou annulait la fiche d'un autre demandeur et voyait toutes les fiches d'un formulaire ; la structure et la création acceptaient un formulaire d'une autre organisation ; la fiche n'était pas rapprochée du formulaire de l'adresse. | AccesFiches : formulaire de l'organisation, fiche de ce formulaire, demandeur ou personnel qui traite ; sinon 404. Un demandeur ne liste que ses fiches. | INT-04 ; INT-03 ; DocumentFicheIntegrationTest | corrigé |
| SEC-05 | Élevée | A01 | Pièces jointes et commentaires d'une fiche accessibles à tout membre de l'organisation ; les commentaires internes du personnel étaient lus par le demandeur. | Même contrôle (AccesFiches) sur les pièces jointes, leur téléchargement et les commentaires ; le demandeur ne lit ni n'écrit de commentaire interne. | INT-04 ; PiecesJointesAccesIntegrationTest | corrigé |
| SEC-06 | Moyenne | A01 | Le décompte et l'export Excel de toutes les fiches (réponses des demandeurs) étaient accessibles au rôle lecteur. | Réservés au personnel qui traite (ADMIN, APPRO, DIRECTION, REDACTEUR). | INT-02-a | corrigé |
| SEC-07 | Moyenne | A01 | Le critère de recherche sauvegardé d'une autre personne, voire d'une autre organisation, se supprimait par son identifiant. | Suppression par le seul propriétaire, dans son organisation ; sinon 404. | INT-04 ; INT-03-b | corrigé |
| SEC-08 | Moyenne | A01 | Les règles d'un formulaire d'une autre organisation s'évaluaient par son identifiant. | Formulaire hors de l'organisation introuvable (404). | INT-03-b | corrigé |
| SEC-09 | Moyenne | A10 | Le test de découverte OIDC (et les connecteurs SAP, Salesforce, rappels) émettait des requêtes vers toute adresse saisie : 169.254.169.254 (métadonnées infonuagiques), le serveur lui-même, le réseau interne. | GardeSortante : HTTP(S) seulement, aucune adresse interne (boucle locale, privée, lien local, ULA, CGNAT, multidiffusion), sauf hôte:port autorisé par configuration (vide en production). | INT-10 ; GardeSortanteTest ; DurcissementIntegrationTest | corrigé |
| SEC-10 | Moyenne | A07 | Un jeton de session volé restait valide après la déconnexion, jusqu'à son expiration (1 h). | La déconnexion révoque le jeton (empreinte SHA-256 conservée jusqu'à son expiration) ; le filtre refuse un jeton révoqué. | INT-05-c ; DurcissementIntegrationTest | corrigé |
| SEC-11 | Moyenne | A05 | Protection contre l'encadrement désactivée sur toute l'API (clic piégé), aucune politique de contenu, aucune politique de référent ; le frontal de production ne posait aucun en-tête de sécurité et affichait sa version. | API : X-Frame-Options DENY, CSP « default-src 'none' », Referrer-Policy no-referrer, Permissions-Policy, Cache-Control no-store. Frontal : CSP sans script tiers ni en ligne, HSTS, nosniff, SAMEORIGIN, server_tokens off. | INT-07-a ; INT-07-b ; DurcissementIntegrationTest | corrigé |
| SEC-12 | Moyenne | A05 | En production, la documentation OpenAPI et Swagger UI étaient publiques et le cookie de session n'était pas Secure par défaut. | Documentation fermée par défaut en production (APP_API_DOCS) ; cookie Secure par défaut (dérogation explicite pour une démonstration en HTTP local). | INT-07-f | corrigé |
| SEC-13 | Faible | A05 | Console H2 ouverte par défaut hors production. | Fermée par défaut (H2_CONSOLE pour l'ouvrir sur un poste). | INT-07-e | corrigé |
| SEC-14 | Faible | A07 | Une requête sans session (ou une session expirée) recevait 403 : l'interface ne reconnaissait pas l'expiration et ne renvoyait pas à la connexion. | 401 sans session ; l'interface renvoie à la connexion, sauf pour la sonde de session des pages publiques. | INT-01-a ; DurcissementIntegrationTest | corrigé |
| SEC-15 | Faible | A01 | L'administration de la plateforme (/api/superadmin/**) n'était contrôlée qu'à l'appel de la méthode : un paramètre manquant répondait 400 avant le refus. | Règle d'URL : /api/superadmin/** réservé au superadministrateur, avant toute lecture de la requête. | INT-02-b ; DurcissementIntegrationTest | corrigé |
| SEC-16 | Faible | A03 · A05 | Une pièce jointe était servie avec le type déclaré par le déposant (text/html, image/svg+xml) et un nom de fichier non échappé ; un type mal formé provoquait une erreur 500. | Types sûrs servis tels quels, tout autre en flux binaire ; en-tête Content-Disposition encodé (RFC 6266). | DocumentDownloadControllerTest | corrigé |
| SEC-17 | Élevée | A01 | Sous-ressources d'un devis (versions, signataires, obligations, addendas, y compris par l'identifiant de l'addenda ou de l'obligation) lisibles et créables par une autre organisation : le devis était lu sans contrôle d'organisation. | DocumentsDuTenant : tout devis hors de l'organisation est introuvable, en lecture comme en écriture. | INT-03-b ; SousRessourcesIntegrationTest | corrigé |
| SEC-18 | Moyenne | A01 | Le circuit d'approbation d'un formulaire d'une autre organisation se lisait et se remplaçait ; son résumé et son export répondaient (vides). | Formulaire de l'organisation exigé (404 sinon). | INT-03-b ; SousRessourcesIntegrationTest | corrigé |
| SEC-19 | Moyenne | A01 | Une feuille de temps se lisait, se modifiait et se soumettait par son identifiant, quel qu'en soit le propriétaire. | Lecture par le propriétaire et les approbateurs ; modification et soumission par le seul propriétaire. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-20 | Moyenne | A01 | Un rôle de simple consultation créait et listait les demandes d'approbation et commentait les devis en révision. | Demandes d'approbation et commentaires de révision réservés aux rôles qui traitent et révisent. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-21 | Faible | A05 | Une requête de fichier qui n'était pas en multipart provoquait une erreur 500. | Réponse 400 explicite. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-22 | Faible | A05 | L'indisponibilité de l'assistant de rédaction renvoyait au client le message technique de l'exception (adresse et port internes du moteur d'IA). | Message générique au client, détail technique au journal du serveur. | ZAP 100000 (réponse examinée) | corrigé |
| SEC-23 | Élevée | A02 | Les variantes de déploiement AWS (ECS et relève) servaient l'application en HTTP seulement : sessions et données en clair, et cookie « Secure » inopérant. | Écouteur HTTPS (TLS 1.2 minimum, TLS 1.3 de préférence, certificat ACM), HTTP redirigé en 301, en-têtes invalides rejetés. | Trivy AVD-AWS-0054, AVD-AWS-0052 ; terraform validate | corrigé |
| SEC-24 | Moyenne | A05 | Les images du serveur et du frontal s'exécutaient en root ; les pods Kubernetes n'avaient aucun contexte de sécurité. | Comptes non privilégiés (10001, nginx 101 sur 8080), racine en lecture seule, aucune élévation de privilège, capacités retirées, seccomp RuntimeDefault ; image du frontal éprouvée ainsi (page, proxy, CSP sans violation). | Trivy DS002, KSV014, KSV118 ; essai de l'image durcie | corrigé |
| SEC-25 | Moyenne | A05 | Infrastructure : coffres de clés Azure ouverts à tout réseau, API AKS et EKS ouvertes à tout Internet (AKS sans RBAC explicite), compartiment S3 des sondes sans blocage d'accès public ni chiffrement, rubrique d'alertes SNS non chiffrée. | Coffres en refus réseau par défaut ; API AKS et EKS restreintes aux adresses d'administration (variable obligatoire, 0.0.0.0/0 refusé), RBAC AKS, accès privé EKS ; S3 privé et chiffré (KMS) ; SNS chiffré. | Trivy AVD-AZU-0013, AVD-AZU-0041, AVD-AZU-0042, AVD-AWS-0086 à 0095 ; terraform validate ; AVD-AWS-0041 | corrigé |
| SEC-26 | Élevée | A06 | Dépendances vulnérables : pilote PostgreSQL 42.6.2 (CVE-2026-42198), axios 1.18.1 (7 CVE hautes), follow-redirects 1.16.0 (3 CVE hautes). | PostgreSQL 42.7.13, axios 1.20.0, follow-redirects 1.16.3. | Trivy, npm audit | corrigé |
| SEC-27 | Faible | A05 | Aucune isolation inter-origines (en-têtes COOP et CORP) contre les attaques par canal auxiliaire de type Spectre. | Cross-Origin-Opener-Policy et Cross-Origin-Resource-Policy « same-origin » sur l'API et le frontal. | ZAP 90004 ; INT-07-a ; INT-07-b | corrigé |
| SEC-28 | Moyenne | A06 | react-router 6.30 : redirection ouverte par barre oblique inverse dans <Link>/navigate et défaut d'hydratation côté serveur (avis modérés). | react-router-dom 7.18.6 : avis corrigés (npm audit : 0 vulnérabilité). L'application n'utilisait que des API stables (Routes, Route, Link, NavLink, Navigate, useNavigate, useParams, useSearchParams, useLocation), inchangées en version 7. | npm audit ; e2e (navigation complète) | corrigé |
| SEC-29 | Faible | A02 | Données au repos (documents, exports, artefacts des sondes, alertes) chiffrées par la clé KMS gérée par AWS : rotation et révocation hors du contrôle de l'exploitant. | Clé KMS gérée par l'organisation (rotation annuelle, politique d'accès propre) pour S3 et SNS ; usage accordé aux seuls rôles du serveur, de Glue et des sondes. | Trivy AVD-AWS-0132, AVD-AWS-0136 ; terraform validate | corrigé |
| SEC-30 | Faible | A05 | Le service de conversion (LibreOffice) gardait une racine inscriptible ; l'image d'outils des bacs à sable s'exécutait en root. | Gotenberg en racine en lecture seule (profil et fichiers de travail en volumes temporaires), sans capacité ; image des bacs à sable sous un compte sans privilège. Conversion et validations (PAdES-LTA, CAdES, révocation) éprouvées ainsi. | Trivy KSV014, DS002 ; essai de conversion ; valider.sh, valider-signature.sh, valider-revocation.sh | corrigé |
| SEC-31 | Faible | A05 | La base de données, les répartiteurs et le lien VPC d'API Gateway pouvaient émettre vers tout Internet, sans en avoir besoin. | Sortie limitée au VPC pour ces composants (trois variantes AWS) ; seules les tâches applicatives gardent une sortie vers Internet. | Trivy aws-vpc-no-public-egress-sgr ; terraform validate | corrigé |

## 5. Balayage dynamique OWASP ZAP

| Alerte | Risque | Instances | Décision |
|---|---|---|---|
| A Client Error response code was returned by the server (100000) | Informational | 15631 | acceptée : 503 volontaire de l'assistant de rédaction quand le moteur d'IA local (Ollama) est arrêté dans l'environnement d'essai ; la réponse ne divulgue plus aucun détail technique (constat SEC-22). |
| Authentication Request Identified (10111) | Informational | 4 | information |
| Content-Type Header Missing (10019) | Informational | 1 | information |
| Non-Storable Content (10049) | Informational | 12 | information |
| Session Management Response Identified (10112) | Informational | 1 | information |
| A Server Error response code was returned by the server (100000) | Low | 1 | acceptée : 503 volontaire de l'assistant de rédaction quand le moteur d'IA local (Ollama) est arrêté dans l'environnement d'essai ; la réponse ne divulgue plus aucun détail technique (constat SEC-22). |
| ZAP is Out of Date (10116) | Low | 1 | acceptée : Avertissement sur la version de l'outil ZAP lui-même, sans rapport avec l'application ; l'image est mise à jour à chaque révision de la campagne. |

## 6. Dépendances, secrets et configuration (Trivy, npm audit)

Vulnérabilités de dépendances : 0 critique(s), 0 haute(s), 3 moyenne(s), 0 faible(s). Secrets : 0. Défauts de configuration : 104.


| Configuration | Fichier | Gravité | Décision |
|---|---|---|---|
| AVD-AWS-0053 Load balancer is exposed to the internet. | infra/dr/aws/failover/compute.tf | HIGH | Répartiteur public par conception : il sert l'application aux usagers. Il n'écoute qu'en HTTPS (HTTP redirigé, SEC-23) et rejette les en-têtes invalides. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | infra/dr/aws/failover/network.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |
| AVD-AWS-0053 Load balancer is exposed to the internet. | infra/serverless/terraform/aws-ecs/ecs.tf | HIGH | Répartiteur public par conception : il sert l'application aux usagers. Il n'écoute qu'en HTTPS (HTTP redirigé, SEC-23) et rejette les en-têtes invalides. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | infra/serverless/terraform/aws-ecs/ecs.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |
| AVD-AWS-0040 EKS Clusters should have the public access disabled | terraform-aws-modules/eks/aws/main.tf | CRITICAL | Point d'accès public de l'API EKS conservé pour les exécutants d'intégration continue hors du VPC, mais restreint aux seules adresses d'administration déclarées (variable obligatoire, 0.0.0.0/0 refusé) ; l'accès privé depuis le VPC est activé. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | terraform-aws-modules/eks/aws/node_groups.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |

npm audit (frontend, production) : 0 critique(s), 0 haute(s), 0 moyenne(s), 0 faible(s).

## 7. Risques résiduels et conditions d'exploitation

Aucun constat ouvert.
- Le niveau de sécurité attesté suppose le déploiement de production prévu : HTTPS de bout en bout (répartiteur Azure Front Door ou ingress), profil « prod », secrets fournis par le coffre (Key Vault, Secrets Manager) et jamais par les valeurs par défaut — le serveur refuse d'ailleurs de démarrer sans eux.
- Le frein à la force brute est par courriel. Une attaque par bourrage d'identifiants répartie sur de nombreux comptes se contient au niveau du répartiteur (pare-feu applicatif et limitation de débit par adresse IP), recommandé en production.
- La garde anti-SSRF résout le nom avant l'appel : une attaque par rebond DNS (changement de résolution entre le contrôle et la connexion) reste théoriquement possible ; elle ne vise que des fonctions réservées aux administrateurs, et l'hébergement doit bloquer en sortie l'accès aux métadonnées infonuagiques (pratique standard).
- L'étanchéité des modules CLM (Investissement Québec), exclusifs des autres modules, est éprouvée par IsolationTenantIqTest (deux organisations CLM) plutôt que par le balayage canari.
- Un test d'intrusion manuel par un tiers indépendant, avant la mise en production, complète cette campagne automatisée : elle ne remplace pas le regard d'un auditeur sur la logique métier.
