# VS-ADR-005 — Mode « signature d'empreinte » : le document ne quitte pas l'application cliente

- **Statut** : Proposé
- **Date** : 2026-11-22

## Contexte et problème

- **Des renseignements de santé** : le SAD et Télésoin manipulent des notes cliniques et des consentements. Les
  confier à un tiers exige une évaluation des facteurs relatifs à la vie privée et un encadrement contractuel. La loi
  québécoise sur les renseignements de santé et de services sociaux est à confirmer avec le conseiller juridique.
- **Des notes non PDF** : les notes du SAD sont des instances de formulaire, pas des PDF à préparer.

## Options envisagées

1. **Toujours transmettre le document** à VinaSign. Simple, mais VinaSign devient dépositaire de dossiers de santé.
2. **Mode empreinte (retenue).**
   - L'application calcule l'empreinte (SHA-256) du contenu, ou de la plage `ByteRange` d'un PDF préparé.
   - VinaSign authentifie le signataire, signe l'empreinte (CMS/CAdES, horodatée) et consigne l'événement.
   - Il rend la signature, que l'application intègre (PDF) ou conserve à côté (note).

## Décision

- **Deux modes par enveloppe** : `DOCUMENT` (par défaut, avec préparation et PDF final) et `EMPREINTE`.
- **Contenu du mode `EMPREINTE`** :
  - VinaSign ne reçoit qu'une empreinte, un titre non nominatif et la référence de l'application ;
  - la preuve atteste l'empreinte, le signataire, le niveau d'authentification, le consentement et l'horodatage ;
  - le SDK fournit l'aide au calcul :
    - texte canonique d'une note (JSON trié, UTF-8) ;
    - préparation d'un PDF avec signature externe (Apache PDFBox), puis intégration du CMS rendu.
- **Vérification publique** : quiconque détient le document recalcule l'empreinte et vérifie la signature et
  l'horodatage, avec VinaSign ou sans lui.

## Conséquences

- **Aucune donnée de santé ne quitte l'application cliente** : VinaSign reste hors du périmètre des dossiers de santé.
- **Responsabilité de l'application** : elle doit conserver le document et la signature. La preuve VinaSign ne prouve
  rien sans eux. Les guides d'intégration le disent explicitement.
- **Ce mode sert aussi à la cosignature** : deux signataires signent la même empreinte, et l'ordre est contrôlé par
  VinaSign.
