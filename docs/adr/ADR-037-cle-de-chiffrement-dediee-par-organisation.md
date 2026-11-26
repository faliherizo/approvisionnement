# ADR-037 — Clé de chiffrement au repos dédiée à chaque organisation (chiffrement d'enveloppe)

- **Statut** : Accepté
- **Date** : 2026-11-26
- **Portée** :
  - domaine : `EnveloppeDocument` (forme stockée), `CleOrganisation` et `CleEnveloppee` (changelog 081),
    `DocumentTrousse` (forme stockée, version de clé, clair en mémoire) ;
  - application : port `GestionnaireCles` (KMS), port `AleaCryptographique`, `RegistreClesOrganisation`,
    `CoffreDocuments`, `ChiffrementClmService` ;
  - infrastructure : `ChiffrementAuRepos` (écouteurs Hibernate), `GestionnaireClesLocal` (défaut),
    `GestionnaireClesAzureKeyVault` (profil `azure-kms`), `ClientEntra`, `ChiffrementClmController` ;
  - écran `/clm/admin/chiffrement` (administration technique).
- **Origine** : Annexe 10 d'IQ, SEC-28 (Souhaitable) ; story RST-54. Complète ADR-020 (Key Vault, région canadienne).

## Contexte

- **SEC-28** : « chiffrer les données au repos avec une clé dédiée à IQ, non partagée avec d'autres clients ».
- **Existant** : le chiffrement au repos d'Azure (stockage, PostgreSQL) emploie des clés de plateforme, communes à toutes
  les organisations hébergées ; seuls les secrets d'intégration sont chiffrés par l'application (`SecretCipher`), avec une
  clé unique de plateforme.
- **Données visées** : le contenu des documents des trousses (`clm_document.contenu`, octets), soit les contrats et les
  documents signés. Le dossier de preuve (ZIP) n'est pas stocké : il est produit à la demande à partir de ces documents et
  du journal scellé, donc protégé par ricochet. Les gabarits Word (`clm_modele_gabarit`) ne portent pas de contenu de
  contrat et restent hors de cette décision.
- **Contraintes** : rotation sans interruption ; empreinte SHA-256 de preuve toujours calculée sur le clair ; reprise des
  documents existants en clair ; aucune clé ni secret dans le dépôt ; horloge et aléa injectés.

## Options envisagées

1. **Chiffrement d'enveloppe applicatif par organisation** : une clé de données AES-256 par organisation (versionnée),
   enveloppée par une clé maîtresse gardée par un service de gestion des clés (local ou Azure Key Vault).
2. **Clé gérée par le client (CMK) sur l'infrastructure** : une instance PostgreSQL et un compte de stockage dédiés à IQ,
   chiffrés par une clé Key Vault d'IQ. Isolation forte, mais une pile par organisation (coût, exploitation), et rien ne
   protège contre une lecture applicative d'une autre organisation.
3. **TDE / chiffrement de colonne PostgreSQL (pgcrypto)** : la clé circule dans les requêtes SQL et les journaux du
   serveur ; une seule clé par colonne en pratique ; non portable vers H2 pour les essais.
4. **Convertisseur JPA (`AttributeConverter`)** : simple, mais il ne connaît pas l'organisation de l'entité ; il faudrait
   lire l'organisation d'un contexte de fil d'exécution, fragile pour les traitements planifiés.

## Décision

Option 1, rendue transparente par des écouteurs Hibernate.

- **Forme stockée** : `00 'V' 'C' 'E' 01` ‖ organisation (8 octets) ‖ version de clé (4) ‖ IV (12) ‖ chiffré AES-GCM
  (étiquette 128 bits). L'en-tête est la donnée authentifiée : un document présenté sous une autre organisation ou une
  autre version échoue. La marque commence par un octet nul, qu'aucun format admis ne porte en tête ; un contenu sans
  marque est un ancien document en clair, lu tel quel.
- **Clés** : une `CleOrganisation` par version (`clm_cle_organisation`), stockée enveloppée seulement, avec l'identifiant
  de la clé maîtresse qui l'a enveloppée. La première est créée à la première écriture d'un document de l'organisation.
  Les clés ouvertes restent en mémoire du processus ; la version active se relit toutes les 5 minutes, pour qu'une
  rotation faite sur une autre instance s'applique sans redémarrage.
- **Transparence** : `ChiffrementAuRepos` s'inscrit auprès d'Hibernate (`PRE_INSERT` : la forme stockée devient
  l'enveloppe ; `POST_LOAD` : le clair est rétabli dans un champ transitoire, sans modifier l'état suivi, donc sans
  écriture parasite). Les cas d'utilisation et les contrôleurs lisent `getContenu()` comme avant. L'empreinte et la
  taille sont calculées sur le clair à la construction du document et ne changent pas.
- **Étanchéité** : `CoffreDocuments.dechiffrer(organisation, stockage)` refuse une enveloppe qui désigne une autre
  organisation avant tout calcul, et la clé d'une organisation n'ouvre pas le chiffré d'une autre (étiquette GCM).
- **Service de clés (port `GestionnaireCles`)** :
  - local (défaut) : clé maîtresse AES-256 issue du secret `CLM_CLE_MAITRESSE` (Base64), enveloppe AES-GCM avec
    l'identifiant de la clé maîtresse en données authentifiées ; anciennes clés maîtresses déclarées par
    `app.clm.chiffrement.anciennes-cles.<id>` le temps du réenveloppement. Sans secret : clé de développement publique,
    signalée au démarrage et **refusée** si `CLM_ENVIRONNEMENT=PROD` ;
  - Azure Key Vault (profil `azure-kms`) : clé RSA du coffre (logicielle ou HSM), `wrapkey`/`unwrapkey` RSA-OAEP-256,
    jeton Entra ID (client credentials, portée `https://vault.azure.net/.default`). La clé enveloppée retient
    l'identifiant versionné de la clé du coffre ; un identifiant qui désigne un autre hôte que le coffre configuré est
    refusé sans appel (pas de falsification de requête depuis la base).
- **Rotation sans interruption** (écran Chiffrement, administration technique) :
  - « Pivoter la clé » : la version active est retirée (elle déchiffre encore), une nouvelle version chiffre les
    nouveaux documents ;
  - « Réenvelopper les documents », par lots de 200 : les clés de données enveloppées par une autre clé maîtresse le sont
    par l'active (rotation de la clé maîtresse), puis les documents d'une autre version, et les anciens contenus en clair,
    sont chiffrés par la version active. Chaque geste est inscrit au journal de configuration.
- **Base** : changelog 081 (table `clm_cle_organisation`, colonne `clm_document.cle_version`). Aucune `@Lob` : la clé
  enveloppée est une chaîne bornée (`VARCHAR(2048)`).

## Conséquences

- **Positives** :
  - SEC-28 démontrable : les documents d'IQ sont chiffrés par une clé propre à IQ, illisibles pour toute autre
    organisation et pour qui lit la base sans le service de clés ;
  - avec `azure-kms`, la clé maîtresse peut être une clé Key Vault d'IQ (CMK) : IQ peut la révoquer, ce qui rend ses
    documents illisibles pour l'exploitant ;
  - aucune modification des cas d'utilisation : 13 lectures de contenu continuent de lire du clair.
- **Négatives** :
  - le clair des documents transite en mémoire du processus applicatif (inévitable pour la signature et l'aperçu) ;
  - les sauvegardes de la base contiennent des enveloppes ; leur restauration exige aussi le service de clés (et les
    anciennes clés maîtresses tant que des clés de données en dépendent) — à inscrire au plan de reprise ;
  - les clés ouvertes vivent en mémoire jusqu'au redémarrage ou à la rotation ;
  - le chiffrement de la base et du stockage par une CMK d'IQ (option 2) reste un complément d'infrastructure, non
    réalisé ici (Terraform) ;
  - les gabarits Word et les secrets d'intégration restent sous les clés de plateforme.
- **Vérification** :
  - `ChiffrementDomaineTest` : forme stockée, clé d'organisation, clair en mémoire (domaine à 100 %) ;
  - `CoffreDocumentsTest`, `RegistreClesOrganisationTest` : étanchéité, cache, rotation relue, création concurrente ;
  - `GestionnaireClesContrat` exécuté contre `GestionnaireClesLocalTest` et `GestionnaireClesAzureKeyVaultTest` (coffre
    simulé appliquant RSA-OAEP-256) ;
  - `ChiffrementOrganisationIntegrationTest` (vraie pile) : aucun clair en base, API en clair, empreinte du clair, refus
    pour une autre organisation, rotation, réenveloppement des anciens contenus en clair ;
  - scénario e2e `E2E-AX-IQ-25`.
- **Reste à faire** : recette contre le Key Vault d'IQ (clé RSA-HSM, identité d'application, journal d'accès du coffre) ;
  secret `CLM_CLE_MAITRESSE` à provisionner sur chaque environnement avant `CLM_ENVIRONNEMENT=PROD`.
