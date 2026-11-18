# ADR-012 — Calendrier de conservation, gel légal et destruction des trousses

- **Statut** : Accepté
- **Date** : 2026-11-12
- **Portée** : `clm.domain.{RegleConservation, Trousse}` (clôture, gel légal, versement, destruction),
  `clm.application.ConservationService`, `clm.infrastructure.web.ConservationController`, écran
  `/clm/admin/conservation`, changelog `054`
- **Origine** : Cahier § 1.4.11 (conservation pendant la période requise par IQ) et § 1.4.12 (destruction au moment
  opportun, Loi sur les archives) ; matrice [conformite_exigences_fonctionnelles.md](../Dossier_IQ_2026-6370-0005/02-Analyse/conformite_exigences_fonctionnelles.md)

## Contexte

Aucune durée de conservation n'existait : les trousses, leurs documents et les renseignements personnels des
signataires étaient gardés indéfiniment. Le calendrier de conservation d'IQ n'est pas encore connu (question E3).

## Options envisagées

1. **Purge automatique à l'échéance** : conforme sur le papier, mais irréversible sans contrôle humain ; une erreur de
   règle détruirait des preuves.
2. **Durée unique de la plateforme** : ne correspond pas à un calendrier qui distingue les types de documents.
3. **Calendrier par type de contrat, destruction confirmée par un administrateur (retenue)** : la règle est une donnée
   de l'organisation ; la plateforme calcule l'échéance et présente les trousses échues ; l'humain décide.

## Décision

- Règle par type de contrat, ou règle par défaut : durée après la clôture (1 à 99 ans) et sort final : `DETRUIRE`,
  `VERSER` (versement aux archives confirmé, puis destruction permise) ou `CONSERVER` (permanent).
- **Sans règle applicable, rien n'est détruit.**
- Clôture datée à la fin de la trousse (terminée ou annulée) ; échéance calculée dans le fuseau de l'organisation.
- **Gel légal** (litige, enquête, demande d'accès) : suspend toute destruction tant qu'il n'est pas levé.
- **Destruction** : contenu des documents, coordonnées des participants, données de signature, commentaires, analyses
  et valeurs saisies sont effacés ; les empreintes et le **journal scellé** demeurent : le bordereau de destruction
  (règle, échéance, empreintes) y est consigné. La chaîne de journal reste vérifiable.

## Révision du 2026-11-17 — certificat de destruction (addenda 3, Q46 à Q48)

- La destruction consigne aussi, avec la trousse, un **bordereau structuré** (changelog `057`) : éléments détruits
  (documents, versions, empreintes SHA-256), date, autorité (acteur, rôle), méthode, résultat, décompte des données
  anonymisées. Il ne se consigne qu'une fois.
- Le **certificat de destruction** est ce bordereau rendu en PDF et scellé par le cachet électronique (PAdES, ADR-013),
  téléchargeable depuis le bloc « Destructions effectuées » (`CertificatDestructionService`).
- Les trois sorts finaux exigés (détruire, transférer vers un dépôt désigné, conserver) sont couverts : le transfert
  s'appuie sur l'exportation en lot (addenda 2, Q13) puis la confirmation du versement.

## Conséquences

- Les durées et sorts finaux restent à saisir selon le calendrier d'IQ (approuvé par BAnQ si applicable).
- Le journal conserve des libellés d'événements (noms au moment des faits) : sa durée relève de la politique de
  conservation des journaux d'audit, à préciser avec IQ.
- Tests : `ConformiteDomaineTest`, `ConservationServiceTest`, `ConformiteExigencesIntegrationTest`, e2e
  « calendrier de conservation ».
