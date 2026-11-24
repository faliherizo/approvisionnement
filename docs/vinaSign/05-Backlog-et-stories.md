# VinaSign — Backlog et stories

- **Date** : 22 novembre 2026
- **Estimation** : points relatifs (suite de Fibonacci). Une vélocité d'environ 36 points par sprint de deux semaines
  est supposée pour l'équipe décrite au [planning](06-Planning.md). Elle est à recalibrer après le sprint 2.
- **Priorité** :
  - **M** (Must) : requis pour le pilote interne ;
  - **S** (Should) : V1 commerciale ;
  - **C** (Could) : V2.
- **Origine** :
  - **CLM** : portée depuis le module CLM du contrat, donc estimée plus bas ;
  - **D1, D2** : dossiers d'origine (Backlog, Intégration réutilisable) ;
  - **N** : nouveau.

## Définition de « terminé » (toutes les stories)

- [ ] Critères d'acceptation couverts par des tests automatisés : unitaires, intégration (Testcontainers) et e2e ou BDD
  si la story touche un parcours.
- [ ] Couverture ≥ 96 % de lignes **par classe** et agrégée ; MSI non dégradé (≥ 80 %, ≥ 90 % sur le domaine).
- [ ] Test de contrat pour chaque implémentation d'un port touché.
- [ ] Analyse statique stricte, ArchUnit (règle de dépendance, isolation) : zéro violation.
- [ ] Interface : axe-core sans violation dans chaque état, quatre largeurs, clavier complet, aucune superposition,
  contraste calculé.
- [ ] OpenAPI et guide d'intégration à jour dans la même PR ; ADR si une décision structurante est prise.
- [ ] Aucune donnée personnelle ni secret journalisé ; entrées validées aux frontières.

## Épics

| Épic | Titre | Points | Priorité |
|---|---|---:|---|
| VS-E01 | Fondations | 34 | M |
| VS-E02 | Organisations et applications clientes | 29 | M |
| VS-E03 | Enveloppes et documents | 29 | M |
| VS-E04 | Moteur de signature (extraction du CLM) | 44 | M |
| VS-E05 | Session du signataire et authentification | 37 | M |
| VS-E06 | Expérience de signature tactile | 28 | M |
| VS-E07 | Finalisation et preuve | 23 | M |
| VS-E08 | Notifications | 13 | M |
| VS-E09 | Intégration et SDK | 34 | M |
| VS-E10 | Applications internes (pilotes) | 29 | M |
| VS-E11 | Sécurité et conformité | 27 | M |
| **Pilote interne** | | **327** | |
| VS-E12 | Préparation et éditeur tactile | 29 | S |
| VS-E13 | Exploitation SaaS | 34 | S |
| VS-E14 | Confiance avancée | 47 | C |

## VS-E01 — Fondations

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-001 | En tant qu'équipe, je veux le dépôt `vinasign` structuré par contexte (`domain/application/infrastructure`) afin de livrer des modules indépendants | Squelette des onze contextes ; ArchUnit : le domaine ne dépend de rien, aucun accès croisé d'entités | M | 5 | 1 | D1 US-001 |
| VS-002 | En tant qu'équipe, je veux une CI qui compile, teste, mesure et analyse | JaCoCo `check` par classe (96 %) et agrégé ; PIT ; Trivy ; SBOM ; construction en échec sur toute baisse | M | 8 | 1 | D1 US-002 |
| VS-003 | En tant qu'exploitant, je veux des environnements reproductibles et des secrets externalisés | Terraform pour l'intégration, la préproduction, la production et le bac à sable, au Canada ; Key Vault ; aucun secret dans le dépôt (Trivy) | M | 8 | 1 | D1 US-003 |
| VS-004 | En tant qu'exploitant, je veux des journaux structurés, des métriques et des traces | OpenTelemetry ; test d'architecture : aucun courriel, téléphone ou jeton journalisé | M | 5 | 1 | D1 US-004 |
| VS-005 | En tant que client, je veux que la base isole les organisations | Liquibase ; RLS sur chaque table métier ; une requête sans contexte rend zéro ligne (test) | M | 8 | 1 | N |

## VS-E02 — Organisations et applications clientes

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-010 | En tant qu'administrateur de la plateforme, je veux créer, suspendre et réactiver une organisation | Une organisation suspendue : toute API rend 403 ; journal d'audit | M | 3 | 2 | D1 US-010 |
| VS-011 | En tant qu'administrateur d'organisation, je veux enregistrer une application cliente (environnement, JWKS, scopes, origines d'intégration) | Validation de la JWKS ; origines en https ; le bac à sable et la production ne partagent rien | M | 5 | 2 | D2 E15-02 |
| VS-012 | En tant qu'application cliente, je veux obtenir un jeton par client credentials (`private_key_jwt`) | JWT de 5 min avec `aud`, `iss` et scopes ; refus d'un jeton d'une autre audience, expiré ou rejoué (`jti`) | M | 8 | 2 | D2 E15-03 |
| VS-013 | En tant qu'administrateur d'organisation, je veux révoquer ou faire tourner les clés d'une application | Révocation effective en moins d'une minute ; deux clés actives pendant une rotation | M | 3 | 2 | D2 |
| VS-014 | En tant qu'administrateur d'organisation, je veux gérer les utilisateurs du portail (OIDC, rôles) | MFA obligatoire pour les rôles d'administration ; rôles : administrateur, préparateur, auditeur | M | 5 | 2 | D1 US-011/012 |
| VS-015 | En tant qu'exploitant, je veux limiter le débit et appliquer des quotas par application | 429 avec `Retry-After` ; quotas par plan ; métriques par application | M | 5 | 2 | D2 E15-13 |

## VS-E03 — Enveloppes et documents

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-020 | En tant qu'application cliente, je veux créer une enveloppe avec une référence externe et une clé d'idempotence | Même clé et même corps : même réponse ; même clé et autre corps : 422 ; `externalReference` unique par application | M | 5 | 3 | D2 E15-05 |
| VS-021 | En tant qu'application cliente, je veux déposer un PDF par une URL présignée | URL de 5 min, un seul objet ; à la confirmation : type réel, taille, structure, antivirus ; refus explicite | M | 8 | 3 | D1 US-020 |
| VS-022 | En tant que système, je veux des versions immuables (original, préparé, final), chacune avec son SHA-256 | Toute réécriture est refusée ; l'empreinte est recalculée à la lecture critique | M | 3 | 3 | D1 US-023 |
| VS-023 | En tant que préparateur, je veux définir les signataires, leur ordre (séquentiel ou parallèle), leur langue et leur type (invité ou utilisateur de l'application) | Un signataire ne peut signer avant son tour ; le type « utilisateur » exige `sujet_externe` | M | 5 | 3 | D1 US-040/041 |
| VS-024 | En tant que système, je veux contrôler les transitions de l'enveloppe et des signataires | Machine d'états du domaine ; aucune route ne pose un statut ; tests de chaque transition interdite | M | 5 | 3 | D1 § 8.3 |
| VS-025 | En tant que préparateur, je veux annuler une enveloppe ; en tant que système, l'expirer à échéance | Liens invalidés ; événements consignés ; webhooks `envelope.cancelled` et `envelope.expired` | M | 3 | 3 | D1 US-042/043 |

## VS-E04 — Moteur de signature (extraction du CLM)

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-030 | En tant qu'équipe, je veux des tests de caractérisation du moteur du CLM avant de le porter | PAdES-B-LTA vérifié par pyHanko, PDF/A vérifié par veraPDF, CAdES vérifié par `openssl` ; la même suite s'exécute contre le CLM et contre VinaSign | M | 5 | 2 | CLM |
| VS-031 | En tant que système, je veux signer avec une clé du HSM (port `CleDeSignature`) | Adaptateurs Key Vault et PKCS#12 (développement seulement, refusé en production) ; test de contrat commun ; la clé n'est pas exportable | M | 8 | 4 | N |
| VS-032 | En tant qu'organisation, je veux que le PDF final porte mon cachet PAdES-B-LTA | Porté de `CachetX509` ; pyHanko : « VALID », niveau LTA ; un certificat renouvelé reste reconnu | M | 8 | 4 | CLM |
| VS-033 | En tant que système, je veux horodater par RFC 3161 avec une autorité de repli | Porté d'`AutoriteHorodatage` ; bascule automatique ; un jeton d'un autre document est refusé | M | 5 | 4 | CLM |
| VS-034 | En tant qu'application cliente, je veux faire signer une empreinte sans transmettre le document | CMS sur l'empreinte, avec horodatage ; vérifiable par `openssl cms -verify` ; aucun contenu stocké | M | 8 | 5 | N |
| VS-035 | En tant que système, je veux produire un PDF/A et convertir les formats bureautiques | Service de conversion isolé (sans réseau) ; veraPDF conforme | M | 5 | 5 | CLM |
| VS-036 | En tant que tiers, je veux vérifier un document signé ou une empreinte | `POST /verifications` limité en débit ; rapport (signataires, horodatage, intégrité, niveau) ; aucun stockage du fichier soumis | M | 5 | 5 | CLM |

## VS-E05 — Session du signataire et authentification

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-040 | En tant qu'invité, je veux ouvrir un lien sécurisé sans compte | Jeton de 256 bits, haché, expirant, révocable, usage borné ; un lien rejoué après la signature rend 410 | M | 5 | 4 | CLM |
| VS-041 | En tant que signataire, je veux accepter un consentement explicite | Texte versionné ; empreinte du texte ; horodatage ; refusé sans consentement | M | 3 | 4 | D1 US-052 |
| VS-042 | En tant que signataire au niveau renforcé, je veux recevoir un code par courriel ou SMS | Porté du portail CLM ; 5 essais puis verrouillage ; code à usage unique ; aucune énumération possible | M | 5 | 5 | CLM |
| VS-043 | En tant que signataire récurrent, je veux signer avec une clé d'accès (WebAuthn) | Enregistrement et usage ; `amr=hwk` attesté ; repli sur le code | M | 8 | 8 | N |
| VS-044 | En tant qu'application cliente, je veux attester mon utilisateur déjà authentifié | Assertion signée par la clé de l'application ; `sub` lié au signataire ; `amr` récent exigé au niveau renforcé ; refus sinon | M | 8 | 6 | N |
| VS-045 | En tant que signataire, je veux refuser de signer, avec un motif facultatif | Refus distinct d'une signature ; enveloppe `DECLINED` ; webhook `signer.declined` | M | 3 | 8 | D1 US-055 |
| VS-046 | En tant qu'application cliente, je veux une session intégrée éphémère pour un signataire | 5 min, usage unique, liée à l'enveloppe, au signataire et aux actions ; une session réutilisée ou expirée est refusée | M | 5 | 5 | D2 E15-07 |

## VS-E06 — Expérience de signature tactile

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-050 | En tant que signataire, je veux lire le document sur téléphone ou tablette | PDF.js ; ajustement à la largeur ; zoom par pincement ; affichage progressif | M | 5 | 6 | D1 US-051 |
| VS-051 | En tant que signataire, je veux dessiner ma signature au doigt ou au stylet, l'effacer et recommencer | Pointer Events ; alternative clavier (nom tapé) ; aucune biométrie conservée au-delà de l'image | M | 5 | 6 | D1 US-036 |
| VS-052 | En tant que signataire, je veux remplir les champs qui me sont assignés | Texte, date, initiales, case ; champs obligatoires bloquants, avec un message qui dit quoi corriger | M | 5 | 6 | D1 US-053 |
| VS-053 | En tant que signataire, je veux une interface accessible | SGQRI 008 3.0 et WCAG 2.2 AA ; axe-core dans chaque état ; quatre largeurs ; cibles ≥ 44 px ; aucune superposition | M | 5 | 6 | N |
| VS-054 | En tant que signataire, je veux l'interface en français ou en anglais | Langue de l'invitation ; bascule ; aucune chaîne en dur | M | 3 | 6 | D1 Q2 |
| VS-055 | En tant qu'application cliente, je veux intégrer la signature dans ma page (`@vinasign/embed`) | Iframe ; `frame-ancestors` limité aux origines déclarées ; `postMessage` typé avec origine exacte ; événements `signed`, `declined`, `closed` | M | 5 | 6 | D2 E15-08 |

## VS-E07 — Finalisation et preuve

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-070 | En tant que système, je veux finaliser le document de façon idempotente | Empreinte préparée revérifiée ; PDF final immuable ; `COMPLETED` posé seulement après succès ; une reprise ne produit pas de double signature | M | 5 | 7 | D1 US-060..062 |
| VS-071 | En tant qu'auditeur, je veux un journal de preuve inaltérable | Chaîne d'empreintes (portée de `ChaineScellee`), ancrage horodaté toutes les heures, stockage immuable ; une altération est détectée | M | 8 | 7 | CLM |
| VS-072 | En tant qu'expéditeur, je veux un certificat de réalisation | PDF/A, scellé ; chronologie, niveaux, empreintes | M | 5 | 7 | CLM |
| VS-073 | En tant qu'auditeur, je veux exporter le dossier de preuve | ZIP signé ; vérifiable hors VinaSign ; accès journalisé | M | 5 | 7 | CLM |

## VS-E08 — Notifications

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-080 | En tant que signataire, je veux recevoir l'invitation et la confirmation par courriel | Boîte d'envoi ; reprise ; modèles français et anglais ; aucun document joint par défaut ; destinataire jamais journalisé en clair | M | 5 | 4 | CLM |
| VS-081 | En tant que signataire, je veux recevoir mon code par SMS | Port `Sms` ; adaptateur fournisseur (à choisir en phase 0) ; simulateur en bac à sable | M | 5 | 5 | N |
| VS-082 | En tant qu'expéditeur, je veux des relances et une échéance | Porté de `Relances` ; plafond de relances ; aucun envoi après une annulation | M | 3 | 5 | CLM |

## VS-E09 — Intégration et SDK

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-090 | En tant qu'intégrateur, je veux un contrat OpenAPI v1 et des erreurs RFC 9457 | Publié ; tests de contrat dans la CI ; erreurs typées | M | 5 | 7 | D2 E15-01 |
| VS-091 | En tant qu'application cliente, je veux des webhooks signés et fiables | HMAC avec horodatage ; reprise avec délai croissant pendant 72 h ; journal des livraisons ; anti-SSRF ; idempotence côté client documentée | M | 8 | 7 | CLM, D2 E15-06 |
| VS-092 | En tant que développeur Java, je veux un SDK et un module d'autoconfiguration Spring Boot | Java 17 et 21 ; client généré ; vérification des webhooks ; aide à l'empreinte (canonicalisation, PDF externe avec PDFBox) | M | 8 | 8 | D2 E15-10 |
| VS-093 | En tant que développeur TypeScript, je veux `@vinasign/client` et `@vinasign/embed` | Node 20 et plus ; navigateur sans dépendance ; exemples React et Angular | M | 5 | 8 | D2 E15-09 |
| VS-094 | En tant qu'utilisateur d'un assistant d'IA, je veux consulter l'état de mes enveloppes par MCP | Porté de `ServeurMcp` ; lecture seule ; validé par le client de référence | M | 3 | 8 | CLM |
| VS-095 | En tant qu'intégrateur, je veux un bac à sable | Certificats de test ; courriels capturés ; données réinitialisables | M | 5 | 8 | D2 E15-12 |

## VS-E10 — Applications internes (pilotes)

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-100 | En tant que professionnel du SAD, je veux signer et faire cosigner ma note par VinaSign | US-408 de bout en bout : empreinte, assertion avec TOTP, cosignature ordonnée, CMS conservé avec la note, BDD du SAD au vert | M | 8 | 9 | N |
| VS-101 | En tant qu'usager de Télésoin, je veux signer mon consentement sur la tablette | Session intégrée ; représentant légal ; preuve rattachée à l'historique du consentement | M | 8 | 9 | N |
| VS-102 | En tant qu'administrateur du contrat, je veux basculer une organisation du CLM sur VinaSign | Adaptateurs derrière les ports existants ; paramètre par organisation ; suite de caractérisation au vert pour les deux implémentations ; IQ exclu par défaut | M | 8 | 10 | CLM |
| VS-103 | En tant qu'auditeur, je veux que les preuves CLM antérieures restent vérifiables | Le certificat historique est reconnu ; vérification publique positive ; aucun document réémis | M | 5 | 10 | CLM |

## VS-E11 — Sécurité et conformité

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-110 | En tant qu'équipe, je veux un modèle de menace revu | STRIDE ([03](03-Securite-et-conformite.md)) relu par un pair ; chaque menace associée à un test | M | 3 | 8 | N |
| VS-111 | En tant qu'équipe, je veux les tests d'intrusion automatisés sur tout l'inventaire | `securite/` du contrat porté (INT-01 à INT-10), plus le rejeu de lien, d'assertion et de webhook ; ZAP et Trivy chaque nuit, bloquants | M | 8 | 9 | CLM |
| VS-112 | En tant que responsable de la protection des renseignements, je veux l'EFVP et le registre | EFVP signée ; résidence au Canada ; sous-traitants listés ; durées de conservation | M | 5 | 9 | N |
| VS-113 | En tant qu'exploitant, je veux un PRA éprouvé | Restauration à partir d'une sauvegarde et bascule vers Canada East mesurées (RPO 15 min, RTO 4 h) | M | 5 | 10 | D1 § 7.3 |
| VS-114 | En tant que direction, je veux un test d'intrusion indépendant avant le pilote | Mandat attribué ; constats élevés corrigés ou faisant l'objet d'une décision de risque formelle | M | 3 | 10 | D1 § 15.4 |
| VS-115 | En tant que direction, je veux l'avis juridique sur les niveaux et le mode empreinte | Avis écrit (LCCJTI, C.c.Q., renseignements de santé) ; terminologie de l'offre validée | M | 3 | 10 | D1 § 10.4 |

## VS-E12 — Préparation et éditeur tactile (V1)

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-060 | En tant que préparateur, je veux placer, déplacer et redimensionner les champs au doigt | Coordonnées normalisées et versionnées ; aucune dérive au zoom ni à la rotation ; gestes de page et de champ distincts | S | 8 | 11 | D1 US-030..035 |
| VS-061 | En tant que préparateur, je veux annuler et rétablir | Historique de 50 actions | S | 3 | 11 | D1 US-034 |
| VS-062 | En tant que préparateur, je veux des modèles d'enveloppe | Champs et rôles réutilisables ; versionnés | S | 5 | 12 | D1 US-103 |
| VS-063 | En tant qu'intégrateur, je veux placer les champs par ancres textuelles | `{{signature:1}}` dans le PDF ; position calculée ; erreur claire si l'ancre est absente | S | 5 | 12 | N |
| VS-064 | En tant que préparateur, je veux envoyer depuis le portail, sans application cliente | Parcours complet sur téléphone, tablette et ordinateur ; e2e aux quatre largeurs | S | 8 | 12 | D1 § 4.1 |

## VS-E13 — Exploitation SaaS (V1)

| ID | Story | Critères d'acceptation clés | P | Pts | Sprint | Origine |
|---|---|---|---|---:|---|---|
| VS-120 | En tant qu'organisation, je veux suivre ma consommation | Règle d'enveloppe portée du CLM (comptée une fois, au premier envoi) ; rapport mensuel ; export | S | 5 | 13 | CLM |
| VS-121 | En tant qu'organisation, je veux souscrire et payer un plan | Page de paiement hébergée par le prestataire ; aucun numéro de carte chez VinaSign (garde portée de l'ADR-022) ; factures | S | 8 | 14 | D1 E14 |
| VS-122 | En tant que prospect, je veux créer mon organisation en libre-service | Vérification du domaine ; bac à sable immédiat ; production après vérification de l'organisation | S | 8 | 14 | N |
| VS-123 | En tant qu'organisation, je veux personnaliser la marque | Logo, couleurs, sous-domaine ; contraste vérifié par calcul | S | 5 | 13 | D1 V1 |
| VS-124 | En tant que client, je veux une page d'état et des SLO publiés | Sondes synthétiques ; historique des incidents | S | 3 | 13 | N |
| VS-125 | En tant que développeur .NET, je veux un SDK | .NET 8 ; tests de contrat | S | 5 | 15 | D2 E15-11 |

## VS-E14 — Confiance avancée (V2)

| ID | Story | Critères d'acceptation clés | P | Pts | Origine |
|---|---|---|---|---:|---|
| VS-130 | En tant que signataire, je veux une signature avancée avec un certificat personnel | Certificat émis à la volée par l'autorité partenaire après vérification d'identité ; PAdES personnelle | C | 13 | N |
| VS-131 | En tant qu'organisation, je veux exiger une vérification d'identité | Port `VerificationIdentite` ; fournisseur à choisir ; résultat attesté dans la preuve | C | 8 | D1 US-112 |
| VS-132 | En tant que système, je veux renouveler la validité à long terme | Horodatage d'archive avant l'expiration de la chaîne ; tâche planifiée ; alerte | C | 5 | D1 US-111 |
| VS-133 | En tant que client entreprise, je veux apporter ma propre clé | Clé de données de l'organisation dans son Key Vault ; révocation = données illisibles | C | 8 | N |
| VS-134 | En tant que client entreprise, je veux une instance dédiée | Même code ; déploiement isolé par Terraform ; procédure de mise à jour | C | 8 | D2 E15-14 |
| VS-135 | En tant que direction, je veux SOC 2 Type II puis ISO 27001 | Période d'observation ; audit externe | C | 5 | N |
