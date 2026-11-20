# ADR-019 — Attestation de signature CAdES par le cachet X.509 et canal courriel SMTP

- **Statut** : Accepté
- **Date** : 2026-11-20
- **Portée** :
  - `clm.infrastructure.SignatureProbanteCachet` et `SignatureProbanteLocale` ;
  - `CachetElectronique.reconnait` ;
  - `notification.infrastructure.CourrielSmtp` et `CourrielSimulation` ;
  - migration 064, `docker-compose.yml`, chart Helm ;
  - bacs à sable `infra/bacs-a-sable` (Mailpit, `valider-signature.sh`).
- **Origine** : ST-09 (signature à valeur probante), Cahier § 1.4.11 (standards reconnus), question H2 (expédition
  depuis le domaine d'IQ).

## Contexte

- **Attestation de signature :** l'attestation remise à chaque signataire était scellée par un HMAC de la plateforme.
  Elle était intègre, mais vérifiable seulement par la plateforme, qui détient la clé.
- **Courriel :** le canal était simulé. Aucun adaptateur n'expédiait réellement, si bien que l'invitation et le code
  de vérification ne pouvaient pas être éprouvés comme en exploitation.

## Options envisagées

1. **Attendre le fournisseur de signature retenu par IQ.**
   - L'adaptateur certifié reste prévu par le port `FournisseurSignature`.
   - Inconvénient : d'ici là, aucune preuve n'est vérifiable hors plateforme.
2. **Attestation CAdES par le cachet X.509 de l'organisation, horodatée RFC 3161 (retenue).**
   - Normes ETSI et IETF, vérifiables par `openssl cms` et les validateurs courants.
   - La valeur probante tient au certificat et à l'autorité choisis par IQ.
3. **Courriel par une API propriétaire (ACS, Graph).**
   - Lie la plateforme à un fournisseur.
   - SMTP est accepté par tous les relais, y compris ACS et Microsoft 365 : c'est l'option retenue.

## Décision

- **Ce qui est signé :** l'empreinte des documents, le nom signé, le courriel, le niveau d'identité et l'instant du
  consentement.
- **Comment :** signature CAdES détachée par le cachet (`CLM_SIGNATURE_FOURNISSEUR=cachet`), avec le jeton RFC 3161
  de l'autorité configurée.
- **Vérification :** une attestation n'est acceptée que si elle est signée par le certificat du cachet, ou par un
  certificat renouvelé au même titulaire *et* émis par la même autorité (signature de l'émetteur vérifiée). Un homonyme
  est refusé. Les attestations HMAC antérieures restent vérifiables.
- **Courriel :** canal SMTP (`NOTIFICATION_COURRIEL_MODE=smtp`, `spring.mail.*`), expéditeur du domaine configuré. Un
  échec rend `ECHEC`, que la messagerie consigne et relance. Le destinataire n'est jamais journalisé en clair.
- **Choix par configuration :** les adaptateurs se choisissent par propriété (`@ConditionalOnProperty`). Les valeurs
  par défaut restent `local` et `simulation` pour les postes de développement, tandis que la production (compose,
  Helm) retient `cachet` et `smtp`.

## Conséquences

- **Taille :** `clm_invitation.attestation_json` passe à 40 000 caractères (migration 064). Une attestation horodatée
  avec chaîne de certificats occupe environ 9 000 caractères.
- **Vérification de bout en bout, le 2026-11-20** (`infra/bacs-a-sable/valider-signature.sh`) :
  - invitation et code reçus par SMTP dans Mailpit ;
  - signature au portail ;
  - attestation vérifiée hors plateforme par `openssl cms -verify`, avec la racine de l'ICP pour seule ancre ;
  - attestation altérée refusée ;
  - intégrité de la preuve confirmée par la plateforme.
- **Ce qui reste à IQ :**
  - désigner le certificat du cachet et l'autorité d'horodatage qualifiée ;
  - fournir le relais SMTP de son domaine, avec SPF et DKIM ;
  - le cas échéant, retenir un prestataire certifié, qui s'ajoute comme un adaptateur de plus.
