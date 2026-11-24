# VinaSign — Sécurité et conformité

- **Date** : 22 novembre 2026
- **Objectif** : une plateforme de signature dont la sécurité se **démontre** par des tests automatisés, des
  vérifications tierces et des certifications, au lieu d'être déclarée.

## 1. Principes

1. **Les clés ne sortent jamais du HSM** : aucune clé privée de signature en mémoire applicative ni sur disque.
2. **Refus par défaut** : chaque route exige un scope, et chaque ressource est bornée à l'organisation, dans
   l'application comme dans la base (RLS).
3. **Minimisation** : le mode empreinte permet de signer sans recevoir le document. Les données personnelles sont
   chiffrées par organisation et absentes des journaux.
4. **Preuve vérifiable sans VinaSign** : PAdES-B-LTA, CAdES et RFC 3161 sont vérifiables par des outils tiers.
5. **Tout contrôle est un test** : une exigence de sécurité sans test automatisé n'est pas livrée.

## 2. Modèle de menace (STRIDE)

| Menace | Actif visé | Contre-mesures | Test qui le prouve |
|---|---|---|---|
| **Usurpation** d'une application cliente | API | `private_key_jwt`, mTLS en option, jetons de 5 min avec audience, révocation immédiate | Jeton d'une autre audience, expiré ou falsifié : refusé |
| **Usurpation** d'un signataire (lien volé) | Session | Jeton haché, usage unique, expiration, code ou clé d'accès selon le niveau, verrouillage après échecs | Rejeu du lien, force brute du code (429), lien révoqué |
| **Usurpation** d'un utilisateur attesté par une application | Session intégrée | Assertion signée par la clé de l'application, sujet lié au signataire, `amr` récent exigé au niveau renforcé | Assertion d'une autre application, `amr` périmé, sujet différent : refusés |
| **Altération** du document après consentement | Version préparée | Empreinte figée au consentement, revérifiée à la finalisation | Modifier l'objet stocké, puis finaliser : refus et alerte |
| **Altération** de la preuve | Journal | Chaîne d'empreintes, ancrage horodaté périodique, stockage immuable (WORM), aucune route de modification | Modifier ou supprimer un événement : la vérification de la chaîne échoue |
| **Répudiation** | Signature | Consentement versionné, niveau et `amr` attestés, horodatage externe, certificat de réalisation | Vérification du dossier de preuve par pyHanko et `openssl` |
| **Divulgation** entre organisations | Toutes les données | RLS, port `PorteeTenant`, stockage par organisation, clés de données par organisation | Canaris A et B sur tout l'inventaire OpenAPI (INT-03 porté) |
| **Divulgation** de documents | Stockage | Conteneurs privés, URL présignées de quelques minutes, chiffrement d'enveloppe | Accès direct au stockage refusé ; URL expirée refusée |
| **Divulgation** par l'iframe | Interface du signataire | Origine distincte, CSP stricte, `frame-ancestors` limité aux origines déclarées par l'application, `postMessage` à origine exacte | Intégrer depuis une origine non déclarée : refusé |
| **Déni de service** | API | Limitation de débit par application et par adresse IP, quotas, tailles bornées, traitements PDF asynchrones et bornés | Tests de charge ; fichiers géants ou malformés |
| **Élévation de privilège** | Portail | Rôles séparés (plateforme, organisation, préparateur, auditeur), accès support temporaire et journalisé | Balayage des rôles (INT-02 porté) ; affectation de masse (INT-08) |
| **SSRF** par les webhooks | Réseau interne | Liste d'adresses refusées (privées, métadonnées), résolution DNS contrôlée (règle `GardeSortante` du contrat) | INT-10 porté |
| **Fichier malveillant** | Serveur, signataires | Type réel, structure PDF, antivirus, rendu dans un conteneur sans réseau | Faux PDF, PDF avec JavaScript, bombe de décompression |
| **Chaîne d'approvisionnement** | Code | SBOM, Trivy et `npm audit` bloquants, images signées, dépendances épinglées, revue obligatoire | Construction en échec sur une vulnérabilité grave |

## 3. Contrôles par couche

| Couche | Contrôles |
|---|---|
| Réseau | Front Door avec WAF (règles OWASP) ; serveur sans IP publique ; base et stockage en points de terminaison privés ; sortie filtrée |
| Identité | OIDC pour le portail, MFA obligatoire pour les rôles administratifs, clés d'accès proposées ; PIM pour l'exploitation |
| Application | Validation aux frontières, erreurs RFC 9457 sans détail interne, en-têtes de sécurité (CSP, HSTS, COOP, CORP), aucun secret dans le frontend |
| Données | Chiffrement TLS 1.2 et plus en transit ; au repos par la plateforme, plus une clé de données par organisation ; renseignements sensibles chiffrés champ par champ (téléphone, réponses) |
| Clés | HSM, rôles séparés (administrateur des clés ≠ opérateur), journal des usages, alertes sur usage anormal |
| Journalisation | Journaux structurés sans renseignement personnel ; journal d'audit de sécurité distinct de la preuve ; transmission au SIEM du client en V2 |
| Exploitation | Déploiements signés, environnements séparés, accès d'urgence journalisé et révisé |

## 4. Vérification continue

Reprise et extension du dispositif éprouvé dans le contrat (`securite/`, `bdd/`, `e2e/`) :

| Vérification | Fréquence | Bloquant |
|---|---|---|
| Tests unitaires, d'intégration (Testcontainers : PostgreSQL, Azurite, ClamAV) et de contrat par port | Chaque PR | Oui |
| Tests d'architecture : règle de dépendance, isolation, aucun journal de renseignement personnel | Chaque PR | Oui |
| Couverture ≥ 96 % par classe ; mutation (PIT) : MSI ≥ 80 %, ≥ 90 % sur le domaine | Chaque PR | Oui |
| Tests d'intrusion automatisés sur tout l'inventaire OpenAPI (INT-01 à INT-10 portés) | Chaque nuit, et avant chaque mise en production | Oui |
| OWASP ZAP authentifié, Trivy (code, images, IaC), `npm audit` | Chaque nuit | Oui (élevé et critique) |
| Validation externe des signatures : pyHanko, veraPDF, `openssl cms` | Chaque nuit | Oui |
| Accessibilité : axe-core dans chaque état, aux quatre largeurs | Chaque PR touchant l'interface | Oui |
| Test d'intrusion par un tiers indépendant | Avant le pilote, puis chaque année | Constats élevés corrigés avant la mise en production |
| Restauration de sauvegarde, bascule de région | Chaque trimestre | Oui |
| Programme de divulgation coordonnée (`security.txt`), puis prime aux bogues | Dès la V1 | — |

## 5. Conformité et cadre juridique

| Cadre | Portée pour VinaSign | Action | Échéance |
|---|---|---|---|
| LCCJTI (RLRQ c. C-1.1) et C.c.Q., art. 2827 | Valeur juridique, intégrité, lien entre la signature et le document | Avis d'un conseiller juridique québécois sur les trois niveaux et le mode empreinte | Phase 0 → pilote |
| Loi 25 (secteur privé) et loi sur l'accès (secteur public) | Renseignements personnels des signataires | EFVP, registre des incidents, responsable de la protection des renseignements, conservation documentée | Avant le pilote |
| Loi sur les renseignements de santé et de services sociaux (Québec) | SAD et Télésoin | Mode empreinte par défaut pour ces applications ; avis juridique sur le cas échéant | Avant l'intégration du SAD |
| Résidence des données | Clients publics québécois (IQ, Lévis, CHU) | Hébergement au Canada, sous-traitants listés, transferts évalués | Phase 0 |
| SOC 2 Type I, puis Type II | Ventes SaaS aux entreprises | Préparation (politiques, preuves de contrôles), puis période d'observation de 6 mois | Type I : V1 ; Type II : V2 |
| ISO/IEC 27001 | Ventes publiques et entreprises | Système de management de la sécurité de l'information, sur la base des contrôles de SOC 2 | V2 |
| ETSI EN 319 401, 319 411, 319 421 (prestataires de services de confiance) | Reconnaissance internationale, horodatage Vina | Analyse d'écart seulement | Après la V2 |
| PCI DSS | Paiement des abonnements | **Hors périmètre** : page de paiement hébergée par le prestataire, et refus de tout numéro de carte à la frontière (garde de l'ADR-022 du contrat, portée) | V1 |

## 6. Ce que VinaSign ne promettra pas

- Une « signature qualifiée » au sens d'un régime étranger.
- Une validité juridique universelle.
- Une identification biométrique par le tracé.

L'offre commerciale reprend la terminologie de [VS-ADR-003](adr/VS-ADR-003-plateforme-de-confiance-operee.md), et le
conseiller juridique la relit avant publication.
