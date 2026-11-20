# ADR-018 — PDF/A-2b, PAdES-B-LTA et gabarits Word convertis par LibreOffice

- **Statut** : Accepté
- **Date** : 2026-11-20
- **Portée** :
  - `clm.infrastructure.ConformitePdfA`, `RenduDocumentsPdf` (génération et apposition) ;
  - `CachetX509`, `DonneesValidation`, `AutoriteHorodatage` (scellement) ;
  - `FusionDocx`, `ConversionLibreOffice`, ports `RenduDocuments.fusionnerDocx` et `ConversionPdf` ;
  - `clm.domain.GabaritWord`, migration 063, `backend/Dockerfile`.
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
3. **LibreOffice sans interface, dans l'image du serveur (retenue).**
   - Rendu le plus fidèle disponible en logiciel libre.
   - Export PDF/A-2 natif.
   - Chaque conversion s'exécute dans un répertoire et un profil jetables.

## Décision

- **Documents produits par la plateforme :** générés en PDF/A-2b.
  - Police Liberation Sans intégrée en sous-ensemble.
  - XMP déclarant `pdfaid:part=2` et `conformance=B`.
  - Titre et date concordants entre Info et XMP.
  - Intention de sortie sRGB.
  - L'apposition des signatures emploie la même police intégrée, ce qui préserve la conformité.
- **Scellement :**
  1. Signature CAdES détachée (`ETSI.CAdES.detached`), horodatée si une autorité est configurée.
  2. DSS : chaîne du cachet, certificats des autorités d'horodatage et listes de révocation.
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
  - En PDF, il est converti par LibreOffice. Si le convertisseur est absent, le PDF reprend le texte fusionné et le motif
    de version le dit expressément. Le repli n'est jamais silencieux.

## Conséquences

- L'image du serveur grossit de LibreOffice Writer et des polices Liberation et DejaVu (environ 300 Mo).
- Une conversion prend environ 2 à 5 s. Le délai maximal se règle avec `CLM_CONVERSION_DELAI_SECONDES` (120 s par
  défaut).
- **Révocation :** les listes (CRL) sont collectées. OCSP n'est pas interrogé, car les autorités québécoises et
  canadiennes publient des CRL HTTP. Un point de distribution injoignable est journalisé sans bloquer le scellement : le
  document reste B-LT incomplet plutôt que non signé.
- **Renouvellement de l'archive :** un nouvel horodatage de document avant l'expiration du certificat de l'autorité
  relève de la conservation (ADR-012). Il n'est pas automatisé dans cette version.
- **Vérifications :** la conformité est vérifiée de façon structurelle par les tests (`ConformitePdfATest`,
  `PadesLtaTest`). Une validation veraPDF et une ouverture dans Adobe Acrobat sont à faire sur l'environnement de
  recette, ces outils n'étant pas disponibles sur le poste de construction.
- **PDF déposés par les utilisateurs :** ils ne sont pas convertis. Leur conformité PDF/A dépend de leur source (limite
  déclarée).
