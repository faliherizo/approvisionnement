# language: fr
@AO-CHU-AVIS-INTENTION
Fonctionnalité: Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4)

  Contexte:
    Étant donné l'organisation "CHU" est prête
    Et je suis connecté en tant que "administrateur CHU"

  @CHU-3.01 @CHU-3.02 @CHU-3.08 @CHU-3.09 @CHU-3.10 @CHU-2.02 @CHU-6.07 @CHU-6.06
  Scénario: Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté
    Quand j'exécute le cas "chu.formulaire.conception"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — formulaires"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-3.03 @CHU-3.04 @CHU-4.01 @CHU-4.02 @CHU-4.03 @CHU-4.04 @CHU-4.05 @CHU-5.02
  Scénario: Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet
    Quand j'exécute le cas "chu.saisie.regles"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — mes demandes"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-6.06
  Scénario: L'organisation récupère en une archive toutes ses données, module par module, avec un manifeste
    Quand j'exécute le cas "chu.reversibilite.export-integral"
    Alors la plateforme se comporte comme attendu

  @CHU-3.05 @CHU-3.07
  Scénario: L'ouverture dépend de la date, du nombre de répondants, du réseau et de la vérification anti-robot
    Quand j'exécute le cas "chu.activation"
    Alors la plateforme se comporte comme attendu

  @CHU-3.06
  Scénario: Un choix dont la capacité est atteinte n'est plus proposé ni accepté
    Quand j'exécute le cas "chu.places.capacite"
    Alors la plateforme se comporte comme attendu

  @CHU-4.08
  Scénario: L'inventaire des inscriptions par activité se consulte et s'exporte
    Quand j'exécute le cas "chu.places.inventaire"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — fiches du formulaire"
    Alors je vois "Inventaire des inscriptions"
    Et l'écran est accessible, sans débordement ni superposition
