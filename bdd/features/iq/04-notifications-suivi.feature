# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Notifications, rappels et suivi centralisé (Cahier § 1.4.7 ; scénario 6 ; addenda 3, Q38)

  @IQ-C-24 @IQ-C-40 @IQ-C-41 @IQ-S-06 @IQ-D-07 @IQ-C-58
  Scénario: Messages désactivés puis réactivés, invitation et rappel manuel reçus par courriel
    Étant donné que je suis connecté en tant que "administration d'affaires IQ"
    Quand j'exécute le cas "iq.notifications"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — notifications"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-43 @IQ-C-25 @IQ-D-08
  Scénario: Suivi centralisé paginé côté serveur, filtres, exceptions et export ; tableau de bord
    Étant donné que je suis connecté en tant que "administration d'affaires IQ"
    Quand j'exécute le cas "iq.suivi"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — suivi centralisé"
    Alors l'écran est accessible, sans débordement ni superposition
