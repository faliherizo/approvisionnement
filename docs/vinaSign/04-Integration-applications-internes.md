# VinaSign — Intégration des applications internes

- **Date** : 22 novembre 2026
- **Principe** : nos applications sont des clientes comme les autres. Elles utilisent l'API publique, les SDK et
  l'iframe, et aucun accès privilégié. Ce qui marche pour elles marchera pour un client SaaS.

## 1. Vue d'ensemble

| Application | Dépôt | Pile | Mode | Niveau | SDK | Ordre |
|---|---|---|---|---|---|---|
| SAD — notes et cosignatures (US-408) | `soins-virtuel` | Java 21, Angular | Empreinte, utilisateur attesté | Renforcé | `vinasign-spring-boot-starter` + `@vinasign/embed` | **Pilote 1** |
| Télésoin — consentements de l'usager | `soins-virtuel/telesoin` | Java 21, Angular | Session intégrée sur tablette ; empreinte ou document selon l'avis juridique | Simple ou renforcé | Idem | **Pilote 2** |
| Contrat — CLM (Lévis, Trois-Rivières) | `contractuelle_contrat` | Java 17, React | Document, invités, cachet | Simple ou renforcé | `vinasign-client` (Java 17) | Après le pilote, par organisation |
| Contrat — CLM (IQ) | `contractuelle_contrat` | Idem | Idem | Selon ST-09 | Idem | **Seulement avec l'accord d'IQ** |
| Autres applications | `~/project/*` | Java, Node, .NET | À qualifier | — | Java, TypeScript, .NET (V1) | Au fil de l'eau, après la V1 |

## 2. Ce que chaque application doit faire

1. **S'enregistrer** dans le portail VinaSign : une application cliente par environnement, avec sa clé publique
   (JWKS), ses scopes et les origines autorisées à intégrer l'iframe.
2. **Installer le SDK** depuis le registre privé et configurer :

   ```yaml
   vinasign:
     url: https://api.vinasign.<domaine>      # domaine à réserver
     client-id: ${VINASIGN_CLIENT_ID}
     cle-privee: ${VINASIGN_CLE_PRIVEE}       # coffre de l'application, jamais dans le dépôt
     webhook-secret: ${VINASIGN_WEBHOOK_SECRET}
   ```

3. **Exposer un point de réception des webhooks**, vérifier la signature (`VinaSignWebhooks.verifier(...)`), traiter
   de façon idempotente, puis **relire le statut par l'API** au lieu de se fier au seul webhook.
4. **Conserver la référence de l'enveloppe** avec l'objet métier, ainsi que les artefacts : PDF final, ou CMS et jeton
   d'horodatage en mode empreinte.
5. **Ajouter ses scénarios** à sa propre suite e2e et BDD, contre le bac à sable de VinaSign.

## 3. SAD — signature et cosignature des notes (US-408)

**Règles couvertes** : RG-408-2 (signature renforcée par TOTP), RG-408-3 et RG-408-4 (cosignature), RG-408-7 (empreinte
au journal chaîné).

- **Mode** : empreinte. La note ne quitte pas le SAD ([VS-ADR-005](adr/VS-ADR-005-signature-d-empreinte.md)).
- **Identité** : le SAD atteste son professionnel par une assertion signée (`sub` = identifiant du professionnel,
  `amr` = `["pwd","otp"]` lorsque le TOTP vient d'être saisi). VinaSign n'exige pas de second code, puisque le niveau
  renforcé est atteint ([VS-ADR-007](adr/VS-ADR-007-authentification-des-signataires.md)).
- **Cosignature** : une enveloppe à deux signataires ordonnés sur la même empreinte. L'auteur signe, puis le
  cosignataire. Le délai et l'alerte de RG-408-4 restent dans le SAD, qui interroge l'état ou reçoit
  `signer.signed`.
- **Correction** : une note de correction est une nouvelle empreinte, donc une nouvelle enveloppe, liée par
  `externalReference` (`NOTE-4471-CORR-1`).
- **Sans connexion** (QM-15) : la signature exige la connexion au moment de signer. La saisie hors ligne reste
  possible ; la signature se fait à la reconnexion. Point à confirmer dans le devis.

**Changements dans `soins-virtuel`** :

| Élément | Changement |
|---|---|
| Module `sad-notes` | Port `SignatureNotes` ; adaptateur `SignatureNotesVinaSign` (infrastructure) ; double de test pour les tests unitaires ; test de contrat exécuté contre les deux |
| Table `signature` | Ajout de `envelope_id`, `cms`, `jeton_horodatage`, `niveau`, `amr` |
| Interface SAD-07 | Bouton « Signer » : ouvre l'iframe VinaSign (`@vinasign/embed`) dans une fenêtre modale |
| Webhook | `POST /api/integrations/vinasign/evenements`, vérifié et idempotent |

## 4. Télésoin — consentement de l'usager

- **Parcours** : l'intervenant ouvre le consentement sur la tablette de l'usager. Le serveur Télésoin crée une
  session intégrée pour l'usager comme signataire invité, sur place : niveau simple, ou code par SMS si la politique
  l'exige. L'usager lit, coche, signe au doigt.
- **Représentant légal** (QM-10) : un second signataire, de type « représentant », avec le motif de sa qualité,
  consigné dans la preuve.
- **Consentements par finalité** (ADR-0028 de Télésoin) : la preuve VinaSign devient la pièce jointe de l'historique en
  ajout seul. La révocation reste dans Télésoin : une révocation n'est pas une signature.
- **Mode** : empreinte par défaut. Le mode document n'est retenu que sur avis juridique favorable, si l'usager doit
  recevoir le PDF.

## 5. Contrat — CLM

- **Adaptateurs** : `FournisseurSignatureVinaSign` et `CachetElectroniqueVinaSign` sont ajoutés dans
  `clm/infrastructure`, derrière les ports existants.
  - Le choix se fait par organisation : un paramètre `signature.fournisseur` vaut `local` ou `vinasign`.
  - Le choix se fait par configuration, jamais par une condition dans le domaine (DIP).
- **Test de contrat** : la suite qui caractérise aujourd'hui `SignatureProbanteCachet` et `CachetX509` s'exécute
  contre les deux implémentations.
- **Séquence de bascule** :
  1. Lévis en préproduction ;
  2. Trois-Rivières ;
  3. IQ, seulement après acceptation écrite d'IQ. Les prérequis sont l'hébergement conforme à l'ADR-020, la validation
     ST-09 et le transfert de preuve.
- **Preuves existantes** : elles restent vérifiables par le CLM. VinaSign reçoit la reconnaissance du certificat de
  cachet historique, de sorte que la vérification publique les accepte aussi.
- **Retrait** du moteur local du CLM : au plus tard un trimestre après la dernière bascule
  ([VS-ADR-002](adr/VS-ADR-002-extraction-du-moteur-clm.md)).

## 6. Applications suivantes

Pour chaque application de `~/project` qui exprime un besoin, une fiche d'intégration d'une page :

- cas d'usage ;
- signataires ;
- niveau ;
- mode ;
- volume mensuel ;
- données sensibles ;
- propriétaire du document.

Les mentions relevées dans `transport-rcl`, `bus-ecole`, `lms`, `dsn-vina`, `refonte-assnat`, `urgences-sante`,
`gestion-loisirs` et `billeterie-scolaire` sont à qualifier avec leurs équipes. Aucune n'est engagée dans le présent
planning.

## 7. Garanties offertes aux applications

| Garantie | Mécanisme |
|---|---|
| Rétrocompatibilité de l'API v1 | Contrat OpenAPI versionné ; tests de contrat de chaque SDK ; dépréciation annoncée 6 mois à l'avance |
| Bac à sable | Environnement isolé, certificats de test, courriels capturés (Mailpit) |
| Disponibilité | SLO publié ; page d'état ; webhooks rejoués jusqu'à 72 h |
| Réversibilité | Export complet des preuves d'une application ; documents signés vérifiables sans VinaSign |
