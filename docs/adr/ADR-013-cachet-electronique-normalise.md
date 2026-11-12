# ADR-013 — Cachet électronique normalisé des preuves (CAdES, PAdES, RFC 3161)

- **Statut** : Accepté
- **Date** : 2026-11-12
- **Portée** : port `clm.application.port.CachetElectronique`, adaptateur `clm.infrastructure.CachetX509`,
  `PreuveService` (certificat et archive), dépendance `org.bouncycastle:bcpkix-jdk18on`
- **Origine** : Cahier § 1.4.11 : « reposer sur des standards reconnus de l'industrie en matière de signature
  électronique » ; préserver la valeur probante pendant toute la conservation

## Contexte

La preuve était scellée par un HMAC propre à la plateforme : intègre, mais vérifiable seulement par la plateforme
(clé secrète) et sans format normalisé. Le choix du fournisseur de signature certifié et de l'autorité d'horodatage
reste à faire par IQ (questions C1, C3).

## Options envisagées

1. **Attendre le fournisseur certifié** : laisse l'exigence non satisfaite jusqu'à une décision hors de notre contrôle.
2. **Signature XML (XAdES)** : moins répandue pour des PDF et des fichiers Word.
3. **Cachet X.509 de l'organisation en CAdES et PAdES, horodatage RFC 3161 (retenue)** : normes ETSI et IETF reconnues,
   vérifiables par des outils indépendants (lecteur PDF, `openssl cms`), sans dépendre de la plateforme.

## Décision

- Chaque document final signé reçoit une signature CMS détachée (`.p7s`, CAdES-B : attribut ESS
  signing-certificate-v2) dans le dossier de preuve.
- Le certificat de signature (PDF) est signé en PAdES (sous-filtre `ETSI.CAdES.detached`, heure de signature dans le
  dictionnaire, signature couvrant tout le fichier).
- Un jeton RFC 3161 est ajouté à la signature quand `CLM_CACHET_TSA_URL` désigne une autorité.
- Certificat et clé viennent d'un magasin PKCS#12 désigné par `CLM_CACHET_MAGASIN` et `CLM_CACHET_MOT_DE_PASSE`,
  variables d'environnement jamais versionnées. Sans magasin, un certificat **de démonstration** autosigné est généré
  au démarrage, signalé au journal et dans la preuve.
- Le sceau HMAC du manifeste et le journal scellé sont conservés : ils servent toujours la vérification interne.

## Conséquences

- La valeur probante qualifiée dépend du certificat retenu : celui d'une autorité reconnue (par exemple un cachet
  d'organisation émis par un prestataire certifié) et d'une autorité d'horodatage qualifiée.
- Le port `FournisseurSignature` reste le point d'entrée d'un fournisseur de signature tiers.
- Tests : `CachetX509Test` (vérification CMS indépendante, PDF signé lu par PDFBox, autorité RFC 3161 de test, magasin
  PKCS#12), `ClmIntegrationTest` (certificat PAdES et `.p7s` dans l'archive).
