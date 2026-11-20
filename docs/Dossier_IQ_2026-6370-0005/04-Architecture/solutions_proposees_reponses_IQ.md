# Solutions proposées en réponse aux addenda 1 à 3 — AO 2026-6370-0005

Ce document répond, point par point, aux réponses d'IQ qui demandent au soumissionnaire de **décrire** sa solution
(méthode, correspondance, flux, lieux, mécanismes). Chaque solution est réalisée dans la plateforme, sauf mention
contraire ; la colonne « Preuve » renvoie au code, aux tests ou au guide qui la démontrent. Les textes sont prêts à
reprendre dans la soumission.

Analyses : [addenda 1](../02-Analyse/impact_addenda_1.md) · [addenda 2](../02-Analyse/impact_addenda_2.md) ·
[addenda 3](../02-Analyse/impact_addenda_3.md) · [matrice de conformité](../02-Analyse/conformite_exigences_fonctionnelles.md) ·
[questions ouvertes](../09-Questions/questions_sans_reponse_IQ.md).

## 1. Salesforce Grantmaking (add. 2 Q7 à Q17 ; add. 3 Q7 à Q17, Q24)

### 1.1 Méthode, prérequis, limites, responsabilités (Q7)

| Élément | Solution |
|---|---|
| Méthode | API REST Salesforce v60.0 (modifiable par l'organisation), sans application AppExchange. Lecture à la demande de `FundingRequest` par identifiant ou par numéro (`Name`, requête SOQL), puis relecture limitée aux champs nécessaires (`?fields=`). |
| Correspondance des champs | Par modèle : chaque champ documentaire désigne sa source Salesforce (nom d'API, champs `…__c` compris). Une saisie manuelle prévaut, l'écart avec la source est signalé comme conflit. Environ 30 champs par modèle, sans plafond. |
| Prérequis chez IQ | Application connectée ; utilisateur d'exécution dédié au profil minimal (lecture de `FundingRequest`, écriture des trois champs de retour) ; trois champs de retour approuvés ; liste des champs lus validée par l'ÉFVP. |
| Limites | Lecture à la demande (pas d'abonnement aux événements entrants, conformément à Q10) ; aucun champ créé dans Salesforce sans approbation d'IQ (Q8). |
| Responsabilités | IQ : organisation Salesforce, droits, champs de retour, environnements. Fournisseur : adaptateur, configuration, correspondance des champs, surveillance, reprise, documentation. |
| Preuve | [ADR-016](../../adr/ADR-016-integration-salesforce-addenda.md), `SalesforceRest`, `SalesforceRestTest`, bac à sable `infra/bacs-a-sable/salesforce` |

### 1.2 Authentification (Q11)

Deux méthodes OAuth 2.0, au choix de la sécurité d'IQ, réglées par l'administrateur dans **Intégrations API** :

- ***Client credentials*** avec un utilisateur d'exécution dédié (méthode par défaut) ;
- **JWT Bearer** (RFC 7523) : assertion RS256 de 3 minutes, signée par la clé privée de l'organisation ; seul le
  certificat public est déposé chez Salesforce, aucun secret partagé ne circule.

Les secrets et clés sont chiffrés au repos, jamais réaffichés ni journalisés ; la rotation se fait par simple
remplacement, sans interruption. Les domaines joignables sont limités par liste blanche (protection contre la
falsification de requêtes côté serveur).

### 1.3 Statuts retournés : correspondance, moment, erreurs et reprises (Q14, Q17)

| Événement de la plateforme | Moment | Valeur écrite par défaut dans `CLM_Statut__c` |
|---|---|---|
| `LANCEE` | Lancement du parcours | Lancée |
| `RETOURNEE` | Retour pour correction, vers l'initiateur ou vers une étape antérieure | Retournée |
| `REJETEE` | Rejet d'une étape | Rejetée |
| `ANNULEE` | Annulation de la trousse | Annulée |
| `SIGNATURE_TERMINEE` | Dernière signature recueillie, documents verrouillés | Terminée |

- **Correspondance** : la valeur écrite se règle dans la liste de référence « Statuts retournés à Salesforce », sans
  développement ; elle est figée au moment de l'événement.
- **Contenu** : `CLM_Statut__c` (valeur), `CLM_Reference__c` (référence durable de la trousse), `CLM_Correlation__c`
  (identifiant de corrélation, aussi transmis en en-tête `X-Correlation-Id`).
- **Fiabilité** : boîte d'envoi transactionnelle écrite dans la même transaction que le changement d'état (aucun
  statut perdu si Salesforce est indisponible) ; clé d'idempotence (aucun statut publié deux fois).
- **Reprises** : 8 au plus, intervalles croissants (1, 2, 4… minutes) plafonnés à une heure, nombre réglable ; au-delà,
  l'événement passe en échec, une alerte d'exploitation est émise, et l'administrateur peut relancer depuis l'écran
  Intégrations API ou depuis la trousse.

### 1.4 Données personnelles et ÉFVP (Q15)

| Élément pour l'ÉFVP | Réponse |
|---|---|
| Catégories | Celles des champs déclarés par l'organisation et des sources des modèles (ex. demandeur, montant, coordonnées de contact) ; rien d'autre n'est lu. |
| Finalités | Génération, validation, approbation et signature du document de la demande. |
| Flux | Salesforce → plateforme (lecture à la demande) ; plateforme → Salesforce (statut, référence, corrélation). |
| Lieux | Voir § 3. |
| Conservation | Calendrier de l'organisation par type de contrat ; destruction avec certificat (§ 5). |
| Minimisation | Lecture `?fields=` limitée ; tout champ rendu sans avoir été demandé est écarté ; attributs d'annuaire conservés seulement s'ils servent une correspondance. |

## 2. Annuaire, accès et provisionnement (add. 3 Q42 à Q44, Q51)

| Exigence | Solution | Preuve |
|---|---|---|
| Entra ID, OIDC ou SAML 2.0 (Q42) | Connecteurs OIDC (code d'autorisation + PKCE) et SAML 2.0 par organisation, configurables et activables par l'administrateur. | Écran Authentification unique |
| Mappage groupes et attributs → rôles (Q43) | Correspondances configurables : groupe (identifiant d'objet ou nom) ou attribut = valeur → rôle et/ou domaine d'affaires. Moindre privilège : rôle par défaut sans correspondance ; superadministrateur jamais attribuable. Chaque changement de rôle est au journal d'audit (avant → après). | [ADR-014](../../adr/ADR-014-annuaire-entreprise-et-scim.md), `AnnuaireDomaineTest` |
| Domaine d'affaires (Q44) | Groupe ou attribut d'annuaire → groupe du module (unité administrative), qui ouvre les trousses de l'unité ; les groupes saisis à la main restent intacts. | `Addenda3IntegrationTest` |
| SCIM 2.0 (Q51) | Point `/scim/v2` (Users, Groups, ServiceProviderConfig, ResourceTypes), jeton porteur par organisation ; création au rôle le plus restreint, rôles et domaines selon les groupes ; déprovisionnement = désactivation (le compte ne peut plus se connecter, la piste d'audit reste). | [Guide d'exploitation](guide_annuaire_scim.md) |

## 3. Lieux d'hébergement, de traitement et de transit (add. 2 Q18 ; add. 3 Q28, Q45)

| Élément | Lieu | Remarque |
|---|---|---|
| Base de données (dossiers, journaux scellés, métadonnées) | Azure `canadacentral` (Toronto) | `canadaeast` (Québec) possible |
| Documents, preuves, certificats | Azure `canadacentral` | Chiffrés au repos |
| Sauvegardes et relève | Azure `canadaeast` | Région de relève canadienne ([infra/dr](../../../infra/dr/README.md)) |
| Journaux applicatifs et métriques | Azure `canadacentral` | Sans donnée personnelle (courriels masqués) |
| Option IA | Dans l'environnement de la plateforme, au Canada | Aucune donnée envoyée à un tiers |
| Courriel, SMS | **Fournisseurs à désigner** | À choisir au Canada ou à déclarer avec catégories, finalités et mesures (question H1) |
| Horodatage RFC 3161 | **Autorité à désigner** | Seule l'empreinte du document est transmise |
| Accès d'administration et de soutien | Depuis le Canada | Tout accès hors Canada serait déclaré et soumis à autorisation écrite |

Tout hébergement, traitement, sauvegarde, accès administratif ou service partagé hors Canada sera déclaré dans la
soumission ; une déclaration ne vaut pas approbation (Q28).

## 4. Continuité et relève (add. 3 Q35)

Objectifs exigés : **RTO 8 h, RPO 4 h, en heures écoulées et continues**. Solution proposée : reprise inter-région
« pilot light » avec réplication continue de la base et des documents vers `canadaeast`, conçue pour un **RPO ≤ 15 min et
un RTO ≤ 4 h** ([infra/dr](../../../infra/dr/README.md)), donc en deçà des exigences. Essai de relève annuel documenté,
rapport remis à IQ.

## 5. Conservation, gel légal et destruction (add. 3 Q46 à Q48)

| Exigence | Solution |
|---|---|
| Périodes par catégorie documentaire | Règle par type de contrat (et règle par défaut) : durée après clôture et sort final ([ADR-012](../../adr/ADR-012-conservation-et-destruction.md)). |
| Gel légal | Appliqué et levé par l'administration ; pendant le gel, aucune destruction possible ; opérations journalisées et exportables (CSV). |
| Sorts finaux | Détruire ; transférer vers un dépôt désigné (exportation en lot ZIP + index, puis versement confirmé) ; conserver. |
| Preuve de destruction | Bordereau consigné avec la trousse et **certificat de destruction** PDF scellé (PAdES) : éléments détruits et empreintes, date, autorité, méthode, résultat. |

## 6. Document Word et modifications (add. 3 Q26)

- Génération depuis le modèle **en Word (.docx) modifiable** ou en PDF.
- Avant l'approbation finale, le Word se corrige puis se redépose comme nouvelle version.
- Pendant le parcours, toute nouvelle version **remplace** la précédente, **invalide** les signatures recueillies et
  **reprend** les validations depuis la première étape (nouveau cycle au journal).
- Le document soumis à la signature est figé : il est verrouillé à la signature et son empreinte est scellée au journal.

## 7. Mesure de l'utilisation et facturation (add. 3 Q22, Q39)

Unité : **l'enveloppe envoyée** ([ADR-015](../../adr/ADR-015-mesure-par-enveloppe.md)), tirée du journal scellé, donc
vérifiable par IQ dans le bloc « Utilisation par enveloppe » du journal d'audit.

| Cas | Traitement |
|---|---|
| Trousse annulée avant envoi | Non comptée |
| Plusieurs documents ou signataires | Une enveloppe |
| Rappel, renvoi manuel, signataire remplacé | Même enveloppe, non recomptée |
| Correction après envoi (nouveau cycle, retour à une étape) | Même enveloppe, non recomptée |
| Annulée, rejetée ou échue après envoi | Comptée une fois (l'invitation est partie) |
| Invitation renvoyée après expiration | Non recomptée ; à confirmer avec IQ (question N1) |

L'authentification standard (lien sécurisé par courriel, SSO) est incluse au prix de base ; l'authentification
renforcée par SMS suit le niveau de risque (Q23).

## 8. Signature, preuve et horodatage (add. 3 Q40, Q41)

- **Preuve** : certificat de signature PDF scellé (PAdES), signature CMS détachée (CAdES, `.p7s`) de chaque document
  signé, journal chaîné et scellé, dossier de preuve ZIP autoportant, vérifiable sans la plateforme
  ([ADR-013](../../adr/ADR-013-cachet-electronique-normalise.md)).
- **Compatibilité** : le cachet est un port ; un fournisseur de signature reconnu se branche comme adaptateur. Tout
  tiers retenu sera déclaré et inclus dans la portée et le prix.
- **Horodatage** : RFC 3161, autorité configurable (`CLM_CACHET_TSA_URL`). L'autorité proposée, ses lieux de traitement,
  ses coûts et la validation à long terme (LTV) seront décrits dans la soumission.

## 9. Workday (add. 3 Q25, Q49, Q50)

- **Côté plateforme (réalisé)** : API d'intégration (OAuth *client credentials*) : création de trousse avec documents ou
  génération par modèle, signataires, déclenchement, suivi, rappels signés HMAC des statuts, récupération du document
  signé et du dossier de preuve, ou d'une référence durable ([api_integration_entrante.md](api_integration_entrante.md)).
- **Côté Workday (à réaliser et chiffrer)** : intégration Workday Studio (ou Orchestrate selon l'environnement d'IQ)
  qui appelle l'API sur les événements RH visés (offre, contrat, avenant, politique à attester) et reçoit les rappels.
  Documentée, testée de bout en bout, incluse au prix.

## 10. Langue (add. 2 Q19 ; add. 3 Q27)

Toutes les interfaces destinées à IQ et aux signataires sont en français, avec l'anglais sur demande : sélecteur dans
l'interface et le portail ; langue de communication par signataire (invitation, rappels, code, ouverture du portail).
Aucune composante utilisée par IQ n'est uniquement en anglais.

## 11. Capacité : 500 utilisateurs concurrents (add. 3 Q38)

Méthode, configuration et résultats dans [perf/README.md](../../../perf/README.md), section « Q38 ». 

Solution :
- numérotation verrouillée ;
- pool de connexions dimensionné ;
- suivi centralisé paginé côté serveur, filtres en base, requêtes groupées par page.

Essai sur poste de développement : 500 utilisateurs concurrents, 1 500 trousses, **0 erreur**, suivi P95 130 ms.
L'essai qui engage la soumission sera refait sur l'infrastructure cible (PostgreSQL, Azure Canada), avec méthode,
dates, configuration et limites documentées comme l'exige Q38.

## 12. Addenda 4 (19 novembre 2026)

Analyse et vérifications : [impact_addenda_4.md](../02-Analyse/impact_addenda_4.md).

### 12.1 Niveaux de signature et d'authentification (Q1, Q2, Q3)

| Niveau | Signataire externe | Signataire interne |
|---|---|---|
| Courriel | Lien sécurisé à usage limité, valide 14 jours (réglable), code à 6 chiffres par courriel | Microsoft Entra ID (OIDC ou SAML 2.0) |
| Code SMS | Code à usage unique par SMS | Entra ID, second facteur de l'annuaire |
| Question | Question de vérification convenue d'avance | — |
| Forte | Deux moitiés du code par deux canaux distincts (courriel et SMS) | — |

- **Choix du niveau :** le plus exigeant entre la politique de l'organisation par niveau de risque, le minimum du type
  de document (liste « Niveau d'authentification minimum par type de document ») et le niveau demandé pour le
  signataire.
- **Comptes :** aucun compte permanent pour un externe.
- **Protection :** blocage après des essais infructueux (3 à 10, réglable).
- **Coûts :**
  - courriel et SMS selon le fournisseur canadien retenu (§ 3) ;
  - une vérification d'identité par un tiers, si IQ l'exige pour certains processus, s'ajoute par le port prévu à cet
    effet, déclarée et chiffrée.

### 12.2 Formats de preuve et d'archivage (Q4, Q14)

**Formats produits :**
- PDF signé et scellé (PAdES), signature CMS détachée de chaque document signé (CAdES, `.p7s`) ;
- certificat de signature PDF scellé et sa version JSON ;
- journal des événements chaîné (JSON, CSV) ;
- dossier de preuve ZIP avec manifeste SHA-256 ;
- export en lot avec index JSON et CSV ;
- certificat de destruction PDF scellé.

**Horodatage :** RFC 3161 par l'autorité retenue.

**Attestation de chaque signature (ST-09, [ADR-019](../../adr/ADR-019-attestation-cades-et-courriel-smtp.md)) :** signée
en CAdES par le cachet X.509 et horodatée. Elle porte l'empreinte des documents, l'identité, le niveau
d'authentification et le consentement, et se vérifie hors de la plateforme (`openssl cms -verify`).

**Validation par des applications externes ([guide](../12-Bacs-a-sable/guide_bacs_a_sable.md), § 2.3) :** ICP OpenSSL,
Gotenberg, veraPDF REST, pyHanko et Mailpit, joints par le réseau aux protocoles réels.

**PDF/A et validation à long terme (RST-37, [ADR-018](../../adr/ADR-018-pdfa-pades-lta-et-gabarits-word.md)) :**

- documents produits (contrats, certificats de signature et de destruction) en PDF/A-2b : police intégrée,
  métadonnées XMP, profil sRGB ; l'apposition des signatures préserve la conformité ;
- scellement PAdES-B-LTA avec l'autorité d'horodatage retenue : signature horodatée, DSS (chaîne de certificats,
  certificats d'horodatage, listes de révocation), puis horodatage de document couvrant le tout ;
- PDF déposés par les utilisateurs : conservés tels quels, leur conformité PDF/A dépend de leur source.

### 12.3 Représentants d'entreprise (Q6)

Le signataire porte sa qualité et l'entreprise représentée. IQ peut exiger une attestation d'autorité, assortie d'une
référence (résolution, procuration). Identité, qualité et attestation sont versées à la preuve scellée et au
certificat.

### 12.4 Administration et accès (Q10, Q11, Q12, Q31)

- **Rôles d'administration :** administration d'affaires et administration technique distinctes, l'administration
  principale cumulant les deux ([ADR-017](../../adr/ADR-017-separation-des-administrations.md)).
- **Provisionnement :** SCIM 2.0 avec mappage des groupes et attributs (département, titre, division…), désactivation
  immédiate ([guide](guide_annuaire_scim.md)).
- **Comptes locaux (RST-39) :** l'administration technique désigne les comptes de secours (second facteur exigé) et
  peut réserver la connexion locale à eux seuls ; toute autre personne passe par l'authentification unique. Chaque
  connexion de secours et chaque refus sont journalisés. L'approbation des comptes reste une décision d'IQ.

### 12.5 Parcours (Q8, Q17 à Q20)

- **Configurations :** séquentiel, parallèle, conditionnel et mixte, avec quorum et politique de rejet par étape.
- **Règles d'acheminement :** sur un champ (ex. montant) ou sur le type de document, l'unité, le niveau de risque ou
  l'origine.
- **Retour et rejet :** retour à une étape choisie ou fin sur rejet ; toute nouvelle version reprend les validations.
- **Délais :** rappels, expirations, annulations et délégations configurables.

### 12.6 Modèles et documents (Q21 à Q23)

- **Modèles :**
  - corps de texte avec variables, clauses conditionnelles, champs positionnés ;
  - génération en PDF ou en Word modifiable ;
  - gabarit Word d'IQ (RST-38) : le `.docx` d'origine se dépose sur le modèle ; ses variables `{{cle}}` et le
    marqueur `{{clauses}}` sont remplacés en conservant styles, tableaux, en-têtes, pieds de page et logos ; le PDF
    est produit par LibreOffice côté serveur (PDF/A-2). Limite : les fonctions Word que LibreOffice ne rend pas à
    l'identique (certaines macros, champs dynamiques avancés).
- **Taille des documents :** 25 Mo par défaut, réglable par l'organisation jusqu'au plafond de la plateforme (30 Mo
  par défaut, configurable au déploiement), sans effet sur la tarification.

### 12.7 Signature en lot (Q34 à Q36)

- **Capacité :** jusqu'à 500 dossiers par lot.
- **Documents :** chaque dossier génère son propre document à partir du même modèle.
- **Approbation :** individuelle (dossier par dossier) ou **globale** (« Approuver le lot »). Dans les deux cas,
  chaque décision est tracée au journal de son dossier.

