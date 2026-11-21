# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Trousses, documents et modèles (Cahier § 1.4.5, § 1.4.7 ; DAO § 3.1.1 ; scénario 1)

  @IQ-C-44 @IQ-C-45 @IQ-C-46 @IQ-C-50 @IQ-D-03 @IQ-D-09 @IQ-S-01 @IQ-C-42 @IQ-C-57
  Scénario: L'initiateur constitue une trousse : documents PDF et Word, versions, signataires internes et externes, copie
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.trousse.creation"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — détail de la trousse"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-47
  Scénario: Un document de 25 Mo est accepté ; au-delà du plafond configuré, il est refusé
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.documents.taille"
    Alors la plateforme se comporte comme attendu

  @IQ-C-32 @IQ-C-33 @IQ-C-34 @IQ-C-35 @IQ-C-36 @IQ-C-37 @IQ-C-38 @IQ-D-06 @IQ-C-17
  Scénario: Un modèle réutilisable porte des champs typés et positionnés, une clause conditionnelle et un cycle de vie
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.modele.cycle"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — modèles"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-09 @IQ-C-10 @IQ-C-11 @IQ-C-12 @IQ-C-14 @IQ-C-29 @IQ-D-01 @IQ-D-02 @IQ-A-06 @IQ-A-17 @IQ-A-18 @IQ-C-56
  Scénario: Le contrat se génère depuis le dossier Salesforce, prérempli, en PDF puis en Word modifiable, avec le parcours du modèle
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.generation.salesforce"
    Et j'exécute le cas "iq.suggestions"
    Alors la plateforme se comporte comme attendu

  @IQ-C-39
  Scénario: La saisie est assistée par les valeurs déjà retenues
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.generation.salesforce"
    Et j'exécute le cas "iq.suggestions"
    Alors la plateforme se comporte comme attendu

  @IQ-A-30 @IQ-A-18
  Scénario: Le gabarit Word d'IQ est fusionné en conservant sa mise en page, en Word comme en PDF
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.gabarit.word"
    Alors la plateforme se comporte comme attendu
