# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Interface, mobilité, langues, accessibilité et IA (Cahier § 1.4.8, § 1.4.9 ; DAO § 3.3.3 ; scénario 8)

  @IQ-C-53 @IQ-S-08 @IQ-D-13
  Scénario: Consultation et décision sur téléphone, sans débordement ni violation d'accessibilité
    Étant donné que je suis connecté en tant que "validateur IQ"
    Quand j'ouvre l'écran "IQ — mes tâches (téléphone)"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "IQ — mes trousses (téléphone)"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-D-21 @IQ-D-23 @IQ-D-22 @IQ-A-15 @IQ-D-13
  Scénario: Les interfaces et la console d'administration sont en français, offertes en anglais
    Étant donné que je suis connecté en tant que "administration d'affaires IQ"
    Quand j'ouvre l'écran "IQ — notifications"
    Alors je vois "Notifications"
    Quand je choisis la langue "English"
    Alors je vois "Notifications"
    Et l'écran est accessible, sans débordement ni superposition
    Quand je choisis la langue "Français"

  @IQ-C-59 @IQ-C-04 @IQ-S-10b
  Scénario: L'analyse automatisée d'un document est soumise à une révision humaine obligatoire
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.ia.analyse"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — accueil CLM"
    Alors l'écran est accessible, sans débordement ni superposition
