# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Parcours de validation et d'approbation (Cahier § 1.4.7 ; scénarios 2, 3, 5, 9)

  Contexte:
    Étant donné que je suis connecté en tant que "initiateur IQ"

  @IQ-C-01 @IQ-C-18 @IQ-C-22 @IQ-C-23 @IQ-C-25 @IQ-C-48 @IQ-D-04 @IQ-S-02 @IQ-S-03 @IQ-S-09 @IQ-C-52
  Scénario: Validation puis approbation, avec commentaire, retour à l'étape de validation et reprise
    Quand j'exécute le cas "iq.parcours.validation-approbation"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — parcours de la trousse"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-22 @IQ-S-09
  Scénario: Un rejet exige un motif et arrête le parcours
    Quand j'exécute le cas "iq.parcours.rejet"
    Alors la plateforme se comporte comme attendu

  @IQ-C-18 @IQ-C-20 @IQ-C-21 @IQ-C-55 @IQ-D-05 @IQ-S-05
  Scénario: Deux validations en parallèle, réassignation à chaud et délégation, sans relancer le parcours
    Quand j'exécute le cas "iq.parcours.parallele-reassignation"
    Alors la plateforme se comporte comme attendu

  @IQ-C-19 @IQ-A-25 @IQ-D-07
  Scénario: Attribution par rôle et par groupe, étape conditionnelle selon le risque, simulation avant lancement
    Quand j'exécute le cas "iq.parcours.regles"
    Alors la plateforme se comporte comme attendu

  @IQ-C-30 @IQ-C-31 @IQ-S-05
  Scénario: Un signataire est ajouté puis remplacé en cours de parcours ; une nouvelle version de document est tracée
    Quand j'exécute le cas "iq.signataires.en-cours"
    Alors la plateforme se comporte comme attendu
