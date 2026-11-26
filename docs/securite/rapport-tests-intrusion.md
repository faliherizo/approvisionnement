# Rapport des tests d'intrusion — plateforme contractuelle

> Document généré par `securite/outils/rapport.mjs` à partir des résultats de `securite/lancer.sh` ; ne pas le modifier à la main.

Version éprouvée : `e42650e` · produit le 2026-11-26 23:53 UTC · cible : pile éphémère de l'exécuteur GitHub Actions ([run](https://github.com/faliherizo/approvisionnement/actions/runs/45913022199)).

> **Nature de l'essai.** Test d'intrusion **interne et outillé**, mené par l'équipe de développement et rejoué à chaque campagne d'intégration continue. Il ne constitue pas le test d'intrusion **indépendant** exigé avant la mise en production : celui-ci est confié à une firme tierce (cahier des charges : [`cahier-des-charges-pentest-independant.md`](cahier-des-charges-pentest-independant.md)).

## 1. Verdict

**Aucune vulnérabilité exploitable n'est ouverte.** Les 29 tests d'intrusion réussissent, le balayage actif authentifié de ZAP ne relève aucune alerte de risque élevé et aucune vulnérabilité de dépendance haute ou critique ne reste sans correction ou sans acceptation motivée. Les 35 constats relevés au fil des campagnes sont corrigés et chacun est verrouillé par un test de non-régression (section 4).

## 2. Périmètre et méthode

### 2.1 Périmètre : tous les points d'accès

- **Inventaire réel** : la référence est la liste des routes que sert Spring MVC (`RequestMappingHandlerMapping`), et non la documentation. `RoutesSecuriseesIntegrationTest` (construction du serveur) échoue si une route manque à la spécification OpenAPI ou si une opération documentée n'est servie par aucune route ; la suite d'intrusion relit l'OpenAPI à chaque campagne : **494 opérations**.
- **Surface publique** : 57 opérations accessibles sans session, chacune motivée (tableau 2.3), toutes limitées en débit par client ; toutes les autres (437) exigent une session et répondent 401 sans elle.
- **Autorisation** : 437 opérations authentifiées dans la matrice rôle × opération (`securite/donnees/matrice-roles.json`), DÉRIVÉE des annotations `@PreAuthorize` par le moteur d'autorisation de l'application ; 283 sont réservées à certains rôles, 154 sont ouvertes à tout rôle authentifié avec un contrôle fin motivé dans le service (`authentification-seule.json`, 18 motifs, chacun avec son test).
- **Hors périmètre** : interface web (couverte par les scénarios e2e et l'accessibilité), infrastructure d'hébergement réelle (OVH, Azure), ingénierie sociale, déni de service volumétrique.

### 2.2 Méthodologie et correspondance OWASP WSTG v4.2 / ASVS 4.0.3 niveau 2

| Essai | Objet | WSTG v4.2 | ASVS 4.0.3 (N2) |
|---|---|---|---|
| RoutesSecuriseesIntegrationTest | Inventaire réel des routes (Spring MVC) = OpenAPI ; règle d'accès explicite ; 401 sans session ; matrice rôle × opération dérivée et éprouvée (403) ; méthodes non prévues (405) | WSTG-INFO-06, WSTG-ATHZ-02, WSTG-ATHZ-03, WSTG-CONF-06 | V1.4.4, V4.1.1, V4.1.3, V4.1.5, V13.2.1, V14.5.1 |
| PareFeuSurfacePubliqueIntegrationTest | Limitation de débit de chaque opération publique ; X-Forwarded-For forgé | WSTG-BUSL-05, WSTG-BUSL-07 | V2.2.1, V11.1.4 |
| INT-01 | Accès anonyme à chaque opération ; surface publique sans fuite | WSTG-ATHN-04, WSTG-ATHZ-02, WSTG-ERRH-01 | V4.1.1, V4.1.5, V7.4.1 |
| INT-02 | Moindre privilège (lecteur, administrateur d'organisation) | WSTG-ATHZ-03 | V4.1.2, V4.1.3 |
| INT-03 | Étanchéité entre organisations (canaris, IDOR en lecture et écriture) | WSTG-ATHZ-04 | V4.2.1 |
| INT-04 | Étanchéité entre personnes d'une organisation (IDOR horizontal) | WSTG-ATHZ-04 | V4.2.1 |
| INT-05 | Force brute, énumération, jetons falsifiés / sans signature / expirés / rejoués, cookie, politique de mot de passe | WSTG-ATHN-03, WSTG-ATHN-07, WSTG-IDNT-04, WSTG-SESS-02, WSTG-SESS-06, WSTG-SESS-09 | V2.1.1, V2.2.1, V3.3.1, V3.4.1, V3.4.2, V3.4.3, V3.5.3 |
| INT-06 | Injection SQL, XSS stockée, traversée de chemins | WSTG-INPV-05, WSTG-INPV-02, WSTG-ATHZ-01 | V5.3.3, V5.3.4, V12.3.1 |
| INT-07 | En-têtes de sécurité, CORS, CSRF, erreurs, méthodes, interfaces d'administration, profil de production | WSTG-CONF-05, WSTG-CONF-06, WSTG-CONF-07, WSTG-CLNT-07, WSTG-CLNT-09, WSTG-SESS-05, WSTG-ERRH-01 | V4.2.2, V7.4.1, V14.3.2, V14.4.3, V14.4.4, V14.4.5, V14.4.6, V14.4.7, V14.5.3 |
| INT-08 | Secrets dans les réponses, numéro de carte refusé, affectation de masse | WSTG-INFO-05, WSTG-ATHZ-03 | V2.4.1, V5.1.2 |
| INT-09 | Téléversements : taille, type, nom de fichier (antivirus : ADR-027, bacs à sable ClamAV) | WSTG-BUSL-08, WSTG-BUSL-09 | V12.1.1, V12.2.1 |
| INT-10 | SSRF : adresses internes, métadonnées infonuagiques, schémas non HTTP | WSTG-INPV-19 | V12.6.1 |
| INT-11 | Matrice des rôles sur le serveur réel ; routes à jeton avec jeton forgé ; limitation de débit | WSTG-ATHZ-02, WSTG-ATHZ-03, WSTG-ATHN-04, WSTG-BUSL-05 | V4.1.1, V4.1.3, V11.1.4 |
| OWASP ZAP (actif, authentifié) | Règles passives et actives sur chaque opération de l'OpenAPI | WSTG-INPV-01, WSTG-INPV-02, WSTG-INPV-05, WSTG-INPV-11, WSTG-INPV-12, WSTG-INPV-07 | V5.2.x, V5.3.x, V14.4.x |
| Trivy, npm audit, gitleaks, SBOM CycloneDX | Composants vulnérables, secrets, configuration (Dockerfile, Terraform, Kubernetes) | WSTG-CONF-01, WSTG-CONF-02 | V14.2.1, V14.1.x, V2.10.4 |

Outils : Playwright (tests d'intrusion `securite/tests/`), JUnit + MockMvc (`backend/src/test/java/qc/levis/contrat/securite/`), OWASP ZAP 2.16.1 (`zap-api-scan.py`, règles passives ET actives, session authentifiée), Trivy 0.58.1, npm audit. Scores : CVSS 3.1, score de base calculé à partir du vecteur (`securite/outils/cvss.mjs`). Scénarios détaillés : [`scenarios-tests-intrusion.md`](scenarios-tests-intrusion.md).

### 2.3 Surface publique (sans session)

| Opérations | Contrôle | Pourquoi publiques | Protection |
|---|---|---|---|
| `^POST /api/auth/login$` | ouverte | Connexion. | Mot de passe haché (BCrypt), second facteur, limitation des essais. |
| `^POST /api/auth/logout$` | ouverte | Déconnexion (efface le cookie). | Aucune donnée exposée. |
| `^GET /api/auth/config$` | ouverte | Options de connexion (SSO actif ou non). | Aucune donnée personnelle. |
| `^(GET|POST) /api/auth/(oidc|saml)/` | ouverte | Flux d'authentification unique OIDC et SAML. | PKCE, état et nonce (OIDC), signature des assertions (SAML). |
| `^(GET|POST) /api/signature/` | jeton exigé | Signature externe par lien. | Jeton à usage limité, imprévisible, expirant. |
| `^(GET|POST) /api/clm/portail/negociation/` | jeton exigé | Portail de la contrepartie externe pour la négociation clause par clause (IQ, IA-05, RST-55, ADR-038). | Jeton aléatoire du lien (empreinte seule conservée), expirant, inopérant une fois la négociation close ou annulée ; option clm_negociation exigée pour l'organisation du jeton ; ni suggestion interne ni identité des décideurs exposées. |
| `^(GET|POST|PUT) /api/clm/portail/` | jeton exigé | Portail du signataire externe (IQ). | Jeton d'invitation et vérification d'identité (code). |
| `^(GET|POST) /api/clm/integration/mcp$` | jeton exigé | Connecteur MCP d'un assistant d'IA (IQ-A-09, ADR-023). | Jeton client credentials d'un client d'intégration ; outils en lecture seule bornés à ce client ; origine étrangère refusée. |
| ` /api/clm/integration/` | jeton exigé | API d'intégration entrante (Workday, Salesforce). | Jeton OAuth client credentials vérifié par le contrôleur. |
| ` /scim/v2/` | jeton exigé | Provisionnement SCIM 2.0. | Jeton porteur propre à l'organisation. |
| `^GET /api/tenants$` | ouverte | Sélecteur d'organisation de la page de connexion. | Slug et titre seulement. |
| `^GET /api/tenants/[^/]+/public$` | ouverte | Titre et thème de la page de connexion. | Données de présentation seulement. |
| `^GET /api/vitrines/[^/]+$` | ouverte | Site vitrine public d'une organisation. | Lecture seule de contenu de présentation ; l'édition est réservée au superadministrateur. |
| `^GET /api/media/` | ouverte | Médiathèque publique (vidéos de vitrine et d'aide, sous-titres, affiches). | Lecture seule, types de fichiers énumérés, chemin normalisé et borné à la racine de la médiathèque (pas de traversée). |
| `^GET /api/health$` | ouverte | Sonde de disponibilité. | État seulement. |
| `^(GET|POST) /api/public/v1/echanges/` | jeton exigé | Échanges hérités de la plateforme précédente (CHU-8.04, ADR-021). | Jeton d'API vérifié par le contrôleur, module interop requis, nom de champ inconnu refusé. |
| ` /api/public/` | jeton exigé | API publique par jeton d'API. | Jeton d'API ou OAuth vérifié par le contrôleur. |

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
| INT-11 | A01 · A04 · A07 | chaque rôle non autorisé reçoit 403 sur chaque opération de la matrice | ✅ réussi |
| INT-11 | A01 · A04 · A07 | aucune route publique à jeton ne s'ouvre sans jeton ni avec un jeton forgé | ✅ réussi |
| INT-11 | A01 · A04 · A07 | la surface publique est limitée en débit par client, sans contournement par X-Forwarded-For | ✅ réussi |

Volume éprouvé :

- 437 opérations éprouvées sans session
- 231 modifications tentées par un lecteur
- 29 opérations de plateforme tentées par un administrateur d'organisation
- 155 lectures de l'organisation B balayées à la recherche du canari de A
- 88 opérations de B dirigées sur les identifiants de A
- 432 charges d'injection SQL envoyées
- 310 réponses inspectées à la recherche de secrets
- 218 opérations d'écriture éprouvées avec un numéro de carte
- 2130 couples rôle refusé × opération éprouvés sur le serveur réel (403 attendu)
- 90 appels aux routes à jeton avec un jeton forgé
- 301 requêtes publiques avant le refus 429 (seuil annoncé + 1)

Opérations à paramètre de chemin sans canari dans l'organisation A (136) : leur étanchéité repose sur les tests d'intégration de leur module (organisation d'essai distincte) et sur le balayage de lecture ci-dessus.

## 4. Constats et corrections (CVSS 3.1)

| N° | CVSS 3.1 | OWASP | Constat | Correction | Verrou de non-régression | État |
|---|---|---|---|---|---|---|
| SEC-02 | **8.1 Élevée**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:H` | A02 · A05 | Le profil de production démarrait avec le secret JWT, la clé de chiffrement et le mot de passe de base de données par défaut écrits dans la configuration : un secret JWT connu permet de forger une session de superadministrateur. | ExigencesProduction refuse le démarrage en production avec une valeur par défaut, un secret JWT de moins de 256 bits ou un cookie non Secure ; docker-compose exige des secrets propres au poste, générés par scripts/generer-secrets.sh. | ExigencesProductionTest ; INT-07-f | corrigé |
| SEC-26 | **7.5 Élevée**<br>`AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:H` | A06 | Dépendances vulnérables : pilote PostgreSQL 42.6.2 (CVE-2026-42198), axios 1.18.1 (7 CVE hautes), follow-redirects 1.16.0 (3 CVE hautes). | PostgreSQL 42.7.13, axios 1.20.0, follow-redirects 1.16.3. | Trivy, npm audit | corrigé |
| SEC-01 | **7.4 Élevée**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N` | A07 | Aucun frein à la force brute : 12 essais de mot de passe d'affilée, puis le bon est accepté. | Au-delà de 10 échecs en 15 minutes pour un même courriel, la connexion répond 429, même avec le bon mot de passe ; un compte inconnu est freiné pareil (aucune énumération). Compte tenu en base (journal d'audit) : commun à toutes les instances. | INT-05-a ; DurcissementIntegrationTest ; LimiteurEtRevocationTest | corrigé |
| SEC-23 | **7.4 Élevée**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:H/I:H/A:N` | A02 | Les variantes de déploiement AWS (ECS et relève) servaient l'application en HTTP seulement : sessions et données en clair, et cookie « Secure » inopérant. | Écouteur HTTPS (TLS 1.2 minimum, TLS 1.3 de préférence, certificat ACM), HTTP redirigé en 301, en-têtes invalides rejetés. | Trivy AVD-AWS-0054, AVD-AWS-0052 ; terraform validate | corrigé |
| SEC-03 | **7.1 Élevée**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:H/A:N` | A01 | Une clause d'une autre organisation se lisait, se modifiait et se supprimait par son identifiant (lecture sans contrôle d'organisation). | Toute clause hors de l'organisation courante est introuvable (404), en lecture comme en écriture. | INT-03-b ; ClauseVersionIntegrationTest | corrigé |
| SEC-04 | **7.1 Élevée**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:L/A:N` | A01 | Fiches : tout membre de l'organisation lisait, modifiait ou annulait la fiche d'un autre demandeur et voyait toutes les fiches d'un formulaire ; la structure et la création acceptaient un formulaire d'une autre organisation ; la fiche n'était pas rapprochée du formulaire de l'adresse. | AccesFiches : formulaire de l'organisation, fiche de ce formulaire, demandeur ou personnel qui traite ; sinon 404. Un demandeur ne liste que ses fiches. | INT-04 ; INT-03 ; DocumentFicheIntegrationTest | corrigé |
| SEC-05 | **7.1 Élevée**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:L/A:N` | A01 | Pièces jointes et commentaires d'une fiche accessibles à tout membre de l'organisation ; les commentaires internes du personnel étaient lus par le demandeur. | Même contrôle (AccesFiches) sur les pièces jointes, leur téléchargement et les commentaires ; le demandeur ne lit ni n'écrit de commentaire interne. | INT-04 ; PiecesJointesAccesIntegrationTest | corrigé |
| SEC-17 | **7.1 Élevée**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:L/A:N` | A01 | Sous-ressources d'un devis (versions, signataires, obligations, addendas, y compris par l'identifiant de l'addenda ou de l'obligation) lisibles et créables par une autre organisation : le devis était lu sans contrôle d'organisation. | DocumentsDuTenant : tout devis hors de l'organisation est introuvable, en lecture comme en écriture. | INT-03-b ; SousRessourcesIntegrationTest | corrigé |
| SEC-09 | **6.8 Moyenne**<br>`AV:N/AC:L/PR:H/UI:N/S:C/C:H/I:N/A:N` | A10 | Le test de découverte OIDC (et les connecteurs SAP, Salesforce, rappels) émettait des requêtes vers toute adresse saisie : 169.254.169.254 (métadonnées infonuagiques), le serveur lui-même, le réseau interne. | GardeSortante : HTTP(S) seulement, aucune adresse interne (boucle locale, privée, lien local, ULA, CGNAT, multidiffusion), sauf hôte:port autorisé par configuration (vide en production). | INT-10 ; GardeSortanteTest ; DurcissementIntegrationTest | corrigé |
| SEC-06 | **6.5 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N` | A01 | Le décompte et l'export Excel de toutes les fiches (réponses des demandeurs) étaient accessibles au rôle lecteur. | Réservés au personnel qui traite (ADMIN, APPRO, DIRECTION, REDACTEUR). | INT-02-a | corrigé |
| SEC-28 | **6.1 Moyenne**<br>`AV:N/AC:L/PR:N/UI:R/S:C/C:L/I:L/A:N` | A06 | react-router 6.30 : redirection ouverte par barre oblique inverse dans <Link>/navigate et défaut d'hydratation côté serveur (avis modérés). | react-router-dom 7.18.6 : avis corrigés (npm audit : 0 vulnérabilité). L'application n'utilisait que des API stables (Routes, Route, Link, NavLink, Navigate, useNavigate, useParams, useSearchParams, useLocation), inchangées en version 7. | npm audit ; e2e (navigation complète) | corrigé |
| SEC-18 | **5.4 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:L/A:N` | A01 | Le circuit d'approbation d'un formulaire d'une autre organisation se lisait et se remplaçait ; son résumé et son export répondaient (vides). | Formulaire de l'organisation exigé (404 sinon). | INT-03-b ; SousRessourcesIntegrationTest | corrigé |
| SEC-19 | **5.4 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:L/A:N` | A01 | Une feuille de temps se lisait, se modifiait et se soumettait par son identifiant, quel qu'en soit le propriétaire. | Lecture par le propriétaire et les approbateurs ; modification et soumission par le seul propriétaire. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-32 | **5.4 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:L` | A01 · A04 | L'autorisation (@PreAuthorize) n'était décidée qu'à l'appel du contrôleur, APRÈS la lecture et la validation du corps et la réception des fichiers : un rôle non autorisé recevait 400 ou 415 (format attendu décrit ; 441 couples rôle × opération) et faisait recevoir au serveur un téléversement complet (jusqu'à 32 Mo) avant d'être refusé. | AutorisationAnticipee : la même règle @PreAuthorize est évaluée par le moteur d'autorisation de l'application avant toute lecture de la requête (intercepteur en tête) ; fichiers reçus à la demande du contrôleur (spring.servlet.multipart.resolve-lazily). La sécurité de méthode reste la seconde barrière. | RoutesSecuriseesIntegrationTest (chaque rôle refusé → 403 sur chaque opération) ; AutorisationAnticipeeTest ; INT-11-a | corrigé |
| SEC-33 | **5.3 Moyenne**<br>`AV:N/AC:L/PR:N/UI:N/S:U/C:N/I:N/A:L` | A04 · A07 | La limitation de débit par adresse ne couvrait que /api/public/** : portails à jeton (négociation, signataire et son code de vérification), signature par lien, intégration entrante, SCIM, OIDC/SAML, connexion et pages publiques n'avaient aucun frein par client. Et elle retenait la PREMIÈRE adresse de X-Forwarded-For, que le client forge : il suffisait d'en changer à chaque requête pour ne jamais être limité. | PareFeuFilter : chaque route sans session appartient à une famille limitée par client (300/min par défaut, APP_PARE_FEU_MAX_PAR_MINUTE), 429 avec Retry-After et RateLimit-Limit ; adresse du client résolue par AdresseClient (X-Forwarded-For lu de droite à gauche, au-delà des seuls mandataires privés). | PareFeuSurfacePubliqueIntegrationTest (chaque opération publique → 429) ; PareFeuFilterTest ; AdresseClientTest ; INT-11-c | corrigé |
| SEC-24 | **5.0 Moyenne**<br>`AV:L/AC:H/PR:H/UI:N/S:C/C:L/I:L/A:L` | A05 | Les images du serveur et du frontal s'exécutaient en root ; les pods Kubernetes n'avaient aucun contexte de sécurité. | Comptes non privilégiés (10001, nginx 101 sur 8080), racine en lecture seule, aucune élévation de privilège, capacités retirées, seccomp RuntimeDefault ; image du frontal éprouvée ainsi (page, proxy, CSP sans violation). | Trivy DS002, KSV014, KSV118 ; essai de l'image durcie | corrigé |
| SEC-10 | **4.8 Moyenne**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N` | A07 | Un jeton de session volé restait valide après la déconnexion, jusqu'à son expiration (1 h). | La déconnexion révoque le jeton (empreinte SHA-256 conservée jusqu'à son expiration) ; le filtre refuse un jeton révoqué. | INT-05-c ; DurcissementIntegrationTest | corrigé |
| SEC-12 | **4.8 Moyenne**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N` | A05 | En production, la documentation OpenAPI et Swagger UI étaient publiques et le cookie de session n'était pas Secure par défaut. | Documentation fermée par défaut en production (APP_API_DOCS) ; cookie Secure par défaut (dérogation explicite pour une démonstration en HTTP local). | INT-07-f | corrigé |
| SEC-25 | **4.8 Moyenne**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:L/A:N` | A05 | Infrastructure : coffres de clés Azure ouverts à tout réseau, API AKS et EKS ouvertes à tout Internet (AKS sans RBAC explicite), compartiment S3 des sondes sans blocage d'accès public ni chiffrement, rubrique d'alertes SNS non chiffrée. | Coffres en refus réseau par défaut ; API AKS et EKS restreintes aux adresses d'administration (variable obligatoire, 0.0.0.0/0 refusé), RBAC AKS, accès privé EKS ; S3 privé et chiffré (KMS) ; SNS chiffré. | Trivy AVD-AZU-0013, AVD-AZU-0041, AVD-AZU-0042, AVD-AWS-0086 à 0095 ; terraform validate ; AVD-AWS-0041 | corrigé |
| SEC-07 | **4.3 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:L/A:N` | A01 | Le critère de recherche sauvegardé d'une autre personne, voire d'une autre organisation, se supprimait par son identifiant. | Suppression par le seul propriétaire, dans son organisation ; sinon 404. | INT-04 ; INT-03-b | corrigé |
| SEC-08 | **4.3 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N` | A01 | Les règles d'un formulaire d'une autre organisation s'évaluaient par son identifiant. | Formulaire hors de l'organisation introuvable (404). | INT-03-b | corrigé |
| SEC-11 | **4.3 Moyenne**<br>`AV:N/AC:L/PR:N/UI:R/S:U/C:N/I:L/A:N` | A05 | Protection contre l'encadrement désactivée sur toute l'API (clic piégé), aucune politique de contenu, aucune politique de référent ; le frontal de production ne posait aucun en-tête de sécurité et affichait sa version. | API : X-Frame-Options DENY, CSP « default-src 'none' », Referrer-Policy no-referrer, Permissions-Policy, Cache-Control no-store. Frontal : CSP sans script tiers ni en ligne, HSTS, nosniff, SAMEORIGIN, server_tokens off. | INT-07-a ; INT-07-b ; DurcissementIntegrationTest | corrigé |
| SEC-20 | **4.3 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:L/A:N` | A01 | Un rôle de simple consultation créait et listait les demandes d'approbation et commentait les devis en révision. | Demandes d'approbation et commentaires de révision réservés aux rôles qui traitent et révisent. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-35 | **4.3 Moyenne**<br>`AV:N/AC:L/PR:L/UI:N/S:U/C:N/I:N/A:L` | A04 · A05 | Une valeur plus longue que sa colonne, ou un doublon, provoquait une erreur interne 500 (DataIntegrityViolationException non traitée) sur huit opérations d'écriture (applications, dossiers, import de formulaire, base de connaissances, clients d'interopérabilité, modèles de notification, répertoires du profil). Relevé par le balayage actif de ZAP une fois sa session tenue jusqu'au bout (règles 30001, 30002, 100000). | Toute contrainte de la base violée répond 400 (valeur refusée) ou 409 (doublon), sans reprendre ni la valeur ni la contrainte ; le journal ne garde que le code SQLSTATE. | ContraintesDonneesTest ; ZAP 30001, 30002 et 100000 (balayage actif authentifié) | corrigé |
| SEC-30 | **3.9 Faible**<br>`AV:L/AC:H/PR:H/UI:N/S:U/C:L/I:L/A:L` | A05 | Le service de conversion (LibreOffice) gardait une racine inscriptible ; l'image d'outils des bacs à sable s'exécutait en root. | Gotenberg en racine en lecture seule (profil et fichiers de travail en volumes temporaires), sans capacité ; image des bacs à sable sous un compte sans privilège. Conversion et validations (PAdES-LTA, CAdES, révocation) éprouvées ainsi. | Trivy KSV014, DS002 ; essai de conversion ; valider.sh, valider-signature.sh, valider-revocation.sh | corrigé |
| SEC-16 | **3.7 Faible**<br>`AV:N/AC:H/PR:L/UI:R/S:U/C:L/I:L/A:N` | A03 · A05 | Une pièce jointe était servie avec le type déclaré par le déposant (text/html, image/svg+xml) et un nom de fichier non échappé ; un type mal formé provoquait une erreur 500. | Types sûrs servis tels quels, tout autre en flux binaire ; en-tête Content-Disposition encodé (RFC 6266). | DocumentDownloadControllerTest | corrigé |
| SEC-34 | **3.7 Faible**<br>`AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:N/A:L` | A05 | La racine de la médiathèque publique (GET /api/media, sans chemin) provoquait une erreur interne 500 (index de chaîne hors limites), relevée par le balayage actif de ZAP. Elle était masquée par l'acceptation de la règle ZAP 100000, accordée pour toute l'API au lieu du seul assistant de rédaction. | Racine sans chemin introuvable (404). Acceptation ZAP limitée par adresse (motifUrl) : une erreur serveur hors de /api/ia/ redevient bloquante. | VitrineIntegrationTest ; ZAP 100000 (acceptation limitée à /api/ia/) | corrigé |
| SEC-13 | **3.6 Faible**<br>`AV:L/AC:H/PR:L/UI:N/S:U/C:L/I:L/A:N` | A05 | Console H2 ouverte par défaut hors production. | Fermée par défaut (H2_CONSOLE pour l'ouvrir sur un poste). | INT-07-e | corrigé |
| SEC-14 | **3.1 Faible**<br>`AV:N/AC:H/PR:N/UI:R/S:U/C:L/I:N/A:N` | A07 | Une requête sans session (ou une session expirée) recevait 403 : l'interface ne reconnaissait pas l'expiration et ne renvoyait pas à la connexion. | 401 sans session ; l'interface renvoie à la connexion, sauf pour la sonde de session des pages publiques. | INT-01-a ; DurcissementIntegrationTest | corrigé |
| SEC-15 | **3.1 Faible**<br>`AV:N/AC:H/PR:L/UI:N/S:U/C:L/I:N/A:N` | A01 | L'administration de la plateforme (/api/superadmin/**) n'était contrôlée qu'à l'appel de la méthode : un paramètre manquant répondait 400 avant le refus. | Règle d'URL : /api/superadmin/** réservé au superadministrateur, avant toute lecture de la requête. | INT-02-b ; DurcissementIntegrationTest | corrigé |
| SEC-21 | **3.1 Faible**<br>`AV:N/AC:H/PR:L/UI:N/S:U/C:N/I:N/A:L` | A05 | Une requête de fichier qui n'était pas en multipart provoquait une erreur 500. | Réponse 400 explicite. | INT-02-a ; SousRessourcesIntegrationTest | corrigé |
| SEC-22 | **3.1 Faible**<br>`AV:N/AC:H/PR:L/UI:N/S:U/C:L/I:N/A:N` | A05 | L'indisponibilité de l'assistant de rédaction renvoyait au client le message technique de l'exception (adresse et port internes du moteur d'IA). | Message générique au client, détail technique au journal du serveur. | ZAP 100000 (réponse examinée) | corrigé |
| SEC-27 | **3.1 Faible**<br>`AV:N/AC:H/PR:N/UI:R/S:U/C:L/I:N/A:N` | A05 | Aucune isolation inter-origines (en-têtes COOP et CORP) contre les attaques par canal auxiliaire de type Spectre. | Cross-Origin-Opener-Policy et Cross-Origin-Resource-Policy « same-origin » sur l'API et le frontal. | ZAP 90004 ; INT-07-a ; INT-07-b | corrigé |
| SEC-29 | **2.2 Faible**<br>`AV:N/AC:H/PR:H/UI:N/S:U/C:L/I:N/A:N` | A02 | Données au repos (documents, exports, artefacts des sondes, alertes) chiffrées par la clé KMS gérée par AWS : rotation et révocation hors du contrôle de l'exploitant. | Clé KMS gérée par l'organisation (rotation annuelle, politique d'accès propre) pour S3 et SNS ; usage accordé aux seuls rôles du serveur, de Glue et des sondes. | Trivy AVD-AWS-0132, AVD-AWS-0136 ; terraform validate | corrigé |
| SEC-31 | **2.2 Faible**<br>`AV:N/AC:H/PR:H/UI:N/S:U/C:L/I:N/A:N` | A05 | La base de données, les répartiteurs et le lien VPC d'API Gateway pouvaient émettre vers tout Internet, sans en avoir besoin. | Sortie limitée au VPC pour ces composants (trois variantes AWS) ; seules les tâches applicatives gardent une sortie vers Internet. | Trivy aws-vpc-no-public-egress-sgr ; terraform validate | corrigé |

Répartition : 0 critique(s), 8 élevée(s), 16 moyenne(s), 11 faible(s) ; 0 ouvert(s).

## 5. Balayage dynamique OWASP ZAP (actif, authentifié)

Règles exécutées : 117, dont 53 règles du balayeur actif. Session du compte de balayage encore valide à la fin : oui. Rapport complet : artefact `zap.html`.

| Alerte | Risque | Instances | Décision |
|---|---|---|---|
| SQL Injection (40018) | High | 13 | acceptée : Faux positif : les seules instances visent une adresse dont un paramètre de chemin est vide (« //signer », « tenants//vitrine »). Le pare-feu HTTP de Spring Security (StrictHttpFirewall) rejette toute URL contenant « // » par un 400, avant le routage, l'authentification et tout accès à la base ; ZAP interprète la variation de ces 400 selon la charge comme une injection booléenne. Les requêtes sont paramétrées (JPA) et INT-06 éprouve l'injection sur chaque liste. Acceptation limitée aux URL contenant « // » : une alerte d'injection SQL sur toute autre adresse reste bloquante. |
| A Client Error response code was returned by the server (100000) | Informational | 15145 | information |
| Authentication Request Identified (10111) | Informational | 4 | information |
| Non-Storable Content (10049) | Informational | 12 | information |
| A Server Error response code was returned by the server (100000) | Low | 1 | acceptée : 503 volontaire de l'assistant de rédaction (/api/ia/) quand le moteur d'IA local (Ollama) est arrêté dans l'environnement d'essai ; la réponse ne divulgue aucun détail technique (constat SEC-22). Acceptation limitée à /api/ia/ : toute autre erreur serveur reste bloquante (c'est ainsi que SEC-34 a été relevé). |
| Unexpected Content-Type was returned (100001) | Low | 341 | information |
| ZAP is Out of Date (10116) | Low | 1 | acceptée : Avertissement sur la version de l'outil ZAP lui-même, sans rapport avec l'application ; l'image est mise à jour à chaque révision de la campagne. |

## 6. Dépendances, secrets et configuration (Trivy, npm audit)

Vulnérabilités de dépendances : 0 critique(s), 0 haute(s), 1 moyenne(s), 0 faible(s). Secrets : 0. Défauts de configuration : 110.


| Configuration | Fichier | Gravité | Décision |
|---|---|---|---|
| AVD-AWS-0053 Load balancer is exposed to the internet. | infra/dr/aws/failover/compute.tf | HIGH | Répartiteur public par conception : il sert l'application aux usagers. Il n'écoute qu'en HTTPS (HTTP redirigé, SEC-23) et rejette les en-têtes invalides. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | infra/dr/aws/failover/network.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |
| AVD-AWS-0053 Load balancer is exposed to the internet. | infra/serverless/terraform/aws-ecs/ecs.tf | HIGH | Répartiteur public par conception : il sert l'application aux usagers. Il n'écoute qu'en HTTPS (HTTP redirigé, SEC-23) et rejette les en-têtes invalides. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | infra/serverless/terraform/aws-ecs/ecs.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |
| AVD-AWS-0040 EKS Clusters should have the public access disabled | terraform-aws-modules/eks/aws/main.tf | CRITICAL | Point d'accès public de l'API EKS conservé pour les exécutants d'intégration continue hors du VPC, mais restreint aux seules adresses d'administration déclarées (variable obligatoire, 0.0.0.0/0 refusé) ; l'accès privé depuis le VPC est activé. |
| aws-vpc-no-public-egress-sgr A security group rule should not allow unrestricted egress to any IP address. | terraform-aws-modules/eks/aws/node_groups.tf | CRITICAL | Seules les tâches applicatives (service ECS, nœuds EKS) sortent vers Internet : elles doivent joindre Microsoft Graph, SAP, Salesforce et les fournisseurs d'identité des organisations, dont les adresses changent. Chaque appel vers une adresse fournie par une organisation passe la garde anti-SSRF (SEC-09). Base de données, répartiteurs et lien d'API Gateway sont limités au VPC (SEC-31). Un mandataire de sortie à liste blanche reste recommandé en production. |

npm audit (frontend, production) : 0 critique(s), 0 haute(s), 0 moyenne(s), 0 faible(s).

## 7. Risques résiduels, conditions d'exploitation et limites

Aucun constat ouvert.
- Le niveau de sécurité attesté suppose le déploiement de production prévu : HTTPS de bout en bout (répartiteur Azure Front Door ou ingress), profil « prod », secrets fournis par le coffre (Key Vault, Secrets Manager) et jamais par les valeurs par défaut — le serveur refuse d'ailleurs de démarrer sans eux.
- Le frein à la force brute est double : par courriel (10 échecs en 15 minutes) et, sur toute la surface sans session, par client (300 requêtes par minute et par famille de routes, SEC-33). Derrière un NAT d'entreprise, tous les usagers partagent une adresse : le seuil se règle (APP_PARE_FEU_MAX_PAR_MINUTE). Un bourrage d'identifiants réparti sur de nombreuses adresses se contient au répartiteur (pare-feu applicatif), recommandé en production.
- La limitation de débit identifie le client par la dernière adresse publique de X-Forwarded-For, ajoutée par le frontal : elle suppose que le serveur n'est joignable QUE par le frontal et le répartiteur (réseau privé), comme dans les déploiements fournis (Docker, Kubernetes, Container Apps).
- La garde anti-SSRF résout le nom avant l'appel : une attaque par rebond DNS (changement de résolution entre le contrôle et la connexion) reste théoriquement possible ; elle ne vise que des fonctions réservées aux administrateurs, et l'hébergement doit bloquer en sortie l'accès aux métadonnées infonuagiques (pratique standard).
- L'étanchéité des modules CLM (Investissement Québec), exclusifs des autres modules, est éprouvée par IsolationTenantIqTest (deux organisations CLM) et par la matrice des rôles (RoutesSecuriseesIntegrationTest, INT-11) plutôt que par le balayage canari.
- Le balayage actif de ZAP est limité à 25 minutes et partage la limitation de débit des routes publiques : il complète les tests ciblés, il ne les remplace pas.
- Ce rapport est celui d'un test INTERNE et outillé, exécuté par l'équipe qui développe la plateforme. Un test d'intrusion par une firme indépendante, avant la mise en production puis chaque année (cahier des charges : cahier-des-charges-pentest-independant.md), complète cette campagne : il apporte le regard d'un auditeur sur la logique métier et les chaînes d'attaque, qu'aucun outil ne remplace.
