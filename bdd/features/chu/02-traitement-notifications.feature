# language: fr
@AO-CHU-AVIS-INTENTION
Fonctionnalité: Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6)

  Contexte:
    Étant donné l'organisation "CHU" est prête
    Et je suis connecté en tant que "administrateur CHU"

  @CHU-4.07 @CHU-7.05 @CHU-6.01 @CHU-6.02
  Scénario: Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel
    Quand j'exécute le cas "chu.traitement"
    Alors la plateforme se comporte comme attendu

  @CHU-4.07
  Scénario: Un circuit d'approbation multi-niveaux s'applique selon le montant
    Quand j'exécute le cas "chu.processus"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — approbations"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-5.01 @CHU-5.03 @CHU-4.06
  Scénario: La soumission d'une fiche déclenche une notification par courriel, tracée au journal des envois
    Quand j'exécute le cas "chu.notifications"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — notifications"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-5.04
  Scénario: Un document PDF se produit depuis les données d'une fiche, puis se transmet
    Quand j'exécute le cas "chu.document.fiche"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — fiches du formulaire"
    Alors je vois le bouton "Document PDF de la fiche"
    Et l'écran est accessible, sans débordement ni superposition

  @CHU-5.05
  Scénario: Les intervenants échangent par une messagerie interne sécurisée
    Quand j'exécute le cas "chu.messagerie"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — messagerie"
    Et je clique sur "Dossier ${unique}"
    Alors je vois "Pièce vérifiée."
    Et l'écran est accessible, sans débordement ni superposition

  @CHU-6.03
  Scénario: Un rapport PDF personnalisé se produit sur les fiches d'un formulaire
    Quand j'exécute le cas "chu.rapport.pdf"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — fiches du formulaire"
    Alors je vois le bouton "Produire le rapport PDF"
    Et l'écran est accessible, sans débordement ni superposition

  @CHU-6.04
  Scénario: Des tableaux de bord et graphiques se configurent et se partagent
    Quand j'exécute le cas "chu.tableaux-de-bord"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — tableaux de bord"
    Et je choisis le tableau de bord "Pilotage ${unique}"
    Alors je vois "Par activité"
    Et l'écran est accessible, sans débordement ni superposition

  @CHU-9.05
  Scénario: Une banque d'heures de services professionnels se tient et se suit
    Quand j'exécute le cas "chu.banque-heures"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — banque d'heures"
    Alors je vois "Heures restantes"
    Et l'écran est accessible, sans débordement ni superposition
