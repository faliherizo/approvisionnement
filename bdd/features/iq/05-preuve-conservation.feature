# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Preuve, journal, conservation et exportation (Cahier § 1.4.11, § 1.4.12 ; scénarios 7 et 10 ; addenda 2, 3, 4)

  @IQ-C-49 @IQ-C-51 @IQ-C-64 @IQ-C-65 @IQ-D-10 @IQ-D-11 @IQ-D-19 @IQ-S-07 @IQ-S-10 @IQ-A-29
  Scénario: Le dossier de preuve d'une trousse signée se consulte, se vérifie, s'exporte ; le journal se consulte et s'exporte
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Et j'exécute le cas "iq.signature.externe"
    Quand j'exécute le cas "iq.preuve.export"
    Alors la plateforme se comporte comme attendu
    Étant donné que je suis connecté en tant que "auditeur IQ"
    Quand j'ouvre l'écran "IQ — journal"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-66 @IQ-A-21 @IQ-A-11 @IQ-A-22
  Scénario: Conservation par type de contrat, gel légal, exportation en lot et relevé d'utilisation par enveloppe
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Et j'exécute le cas "iq.signature.externe"
    Quand j'exécute le cas "iq.conservation"
    Alors la plateforme se comporte comme attendu
    Étant donné que je suis connecté en tant que "administration d'affaires IQ"
    Quand j'ouvre l'écran "IQ — conservation"
    Alors l'écran est accessible, sans débordement ni superposition
