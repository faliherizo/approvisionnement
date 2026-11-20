# ADR-018 — PDF/A-2b, PAdES-B-LTA et gabarits Word convertis par un service LibreOffice

- **Statut** : Accepté
- **Date** : 2026-11-20
- **Portée** :
  - `clm.infrastructure.ConformitePdfA`, `RenduDocumentsPdf` (génération et apposition) ;
  - `CachetX509`, `DonneesValidation`, `AutoriteHorodatage` (scellement) ;
  - `FusionDocx`, `ConversionServiceHttp`, `ConversionLibreOffice`, ports `RenduDocuments.fusionnerDocx` et `ConversionPdf` ;
  - `clm.domain.GabaritWord`, migration 063, `docker-compose.yml` et chart Helm (service `conversion`) ;
  - bacs à sable `infra/bacs-a-sable` (ICP, conversion, veraPDF, pyHanko).
- **Origine** : addenda 4, Q4 (validation à long terme), Q14 (PDF/A) et Q22 (modèles Word d'IQ, présentation
  préservée dans la mesure du possible).

## Contexte

Avant cette décision :

- **PDF générés :** OpenPDF avec Helvetica non intégrée, sans métadonnées ni profil de couleur. Ils n'étaient donc pas
  PDF/A.
- **Scellement :** PAdES-B, avec une seule horodatation, celle de la signature, et sans données de validation. Une fois
  le certificat expiré ou révoqué, rien dans le fichier ne permettait de prouver qu'il était valide au moment de la
  signature.
- **Modèles Word d'IQ :** ressaisis en texte à variables. Leur mise en page (styles, tableaux, en-têtes, logos) était
  perdue.

## Options envisagées

### Validation à long terme

1. **EU DSS (Digital Signature Service).**
   - Avantages : bibliothèque de référence, qui couvre B-LTA et OCSP.
   - Inconvénients : plusieurs dizaines de modules, non disponible dans l'environnement de construction hors ligne,
     et redondante avec PDFBox et BouncyCastle déjà en place.
2. **PDFBox et BouncyCastle (retenue).**
   - Le DSS est écrit par mise à jour incrémentale.
   - L'horodatage de document suit le format ETSI.RFC3161.
   - La révocation passe par les listes publiées aux points de distribution HTTP du certificat.

### Conversion Word → PDF

1. **docx4j, ou POI avec un moteur FO.** Ces solutions sont fidèles seulement en partie : rendu approximatif des
   tableaux, des sections et des polices.
2. **Service tiers en ligne.** Des documents confidentiels sortiraient du périmètre hébergé au Québec. Exclu.
3. **LibreOffice sans interface, dans l'image du serveur.**
   - Rendu fidèle, export PDF/A-2 natif.
   - Mais LibreOffice analyse des fichiers fournis par des tiers dans le processus même du serveur, et l'image grossit
     d'environ 300 Mo. Conservé comme adaptateur de repli (`CLM_CONVERSION_MODE=soffice`).
4. **Service LibreOffice externe à l'API Gotenberg (retenue).**
   - Même moteur, isolé dans son conteneur, sans compte de service ni secret.
   - Mis à l'échelle à part.
   - La même image sert la production et le bac à sable : le comportement éprouvé est celui qui sera déployé.

## Décision

- **Documents produits par la plateforme :** générés en PDF/A-2b.
  - Police Liberation Sans intégrée en sous-ensemble.
  - XMP déclarant `pdfaid:part=2` et `conformance=B`.
  - Titre et date concordants entre Info et XMP.
  - Intention de sortie sRGB.
  - L'apposition des signatures emploie la même police intégrée, ce qui préserve la conformité.
- **Scellement :**
  1. Signature CAdES détachée (`ETSI.CAdES.detached`), horodatée si une autorité est configurée.
  2. DSS : chaîne du cachet, certificats des autorités d'horodatage et des répondeurs, listes de révocation (points de
     distribution) et réponses OCSP (accès aux informations de l'autorité).
  3. Avec une autorité configurée, horodatage de document `/DocTimeStamp` couvrant le tout.
  - Niveau atteint : **PAdES-B-LTA** avec autorité, PAdES-B-B (avec DSS) sans autorité. `niveauPades()` l'expose, et le
    dossier de preuve le cite.
- **Gabarit Word :** un `.docx` facultatif par version de modèle.
  - Déposé sur un brouillon. Il est refusé s'il emploie une variable inconnue du modèle.
  - Il suit le modèle à la nouvelle version.
  - Les variables sont fusionnées sur le texte reconstitué de chaque paragraphe, y compris quand Word les a découpées en
    plusieurs runs. Cela couvre le corps, les tableaux (imbriqués compris), les en-têtes et les pieds de page.
  - Les clauses retenues sont insérées au marqueur `{{clauses}}`.
  - En Word, le document fusionné est remis tel quel.
  - En PDF, il est converti par le service `conversion` (Gotenberg, `pdfa=PDF/A-2b`). Si le service est indisponible, le
    PDF reprend le texte fusionné et le motif de version le dit expressément. Le repli n'est jamais silencieux.

## Conséquences

- **Déploiement :** un service `conversion` de plus (Gotenberg, environ 1,5 Go d'image, 512 Mio à 2 Gio de mémoire),
  interne au réseau. L'image du serveur ne contient plus LibreOffice. Une conversion prend de 2 à 5 s ; le délai maximal
  se règle avec `CLM_CONVERSION_DELAI_SECONDES` (120 s par défaut).
- **Révocation :** les listes (CRL) et les réponses OCSP sont collectées. Un point injoignable est journalisé sans
  bloquer le scellement : le document reste signé, avec moins de données de validation.
- **Renouvellement d'archive :** le certificat de signature et les signatures détachées sont produits **à chaque
  export** avec les certificats et l'horodatage du moment. Aucun document scellé n'est donc conservé au-delà de la
  validité de l'autorité d'horodatage, et il n'y a pas d'horodatage d'archive à renouveler.
- **Vérification par des applications externes :** le 2026-11-20, dans les bacs à sable (`infra/bacs-a-sable`) :
  - veraPDF REST a jugé conformes PDF/A-2b le contrat issu du gabarit Word converti par Gotenberg et les certificats
    scellés (144 règles, 0 échec) ;
  - pyHanko a validé les certificats au profil PAdES-B-LTA, révocation intégrée exigée ;
  - pyHanko a refusé un document scellé après révocation du cachet.
  La recette avec IQ refait ces contrôles avec l'autorité et le certificat retenus.
- **PDF déposés par les utilisateurs :** ils ne sont pas convertis. Leur conformité PDF/A dépend de leur source (limite
  déclarée).
