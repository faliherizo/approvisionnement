# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Accès, rôles, annuaire et protection des données (Cahier § 1.4.10, § 1.4.12 ; DAO § 3.3.2 ; addenda 3 et 4)

  @IQ-D-15 @IQ-D-16 @IQ-D-17 @IQ-A-26 @IQ-C-67
  Scénario: Moindre privilège : administrations séparées, auditeur en lecture seule, module cloisonné
    Étant donné que je suis connecté en tant que "administration principale IQ"
    Quand j'exécute le cas "iq.acces.administrations"
    Alors la plateforme se comporte comme attendu

  @IQ-D-05 @IQ-D-18 @IQ-C-67
  Scénario: Comptes créés avec un rôle, rattachés à un groupe, puis désactivés ; départ d'un employé consulté
    Étant donné que je suis connecté en tant que "administration principale IQ"
    Quand j'exécute le cas "iq.acces.comptes"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — utilisateurs"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-A-08 @IQ-A-19 @IQ-A-20 @IQ-A-27
  Scénario: Correspondance d'annuaire et provisionnement SCIM 2.0, puis désactivation immédiate
    Étant donné que je suis connecté en tant que "administration technique IQ"
    Quand j'exécute le cas "iq.annuaire.scim"
    Alors la plateforme se comporte comme attendu

  @IQ-C-60
  Scénario: Authentification unique configurable et connexion locale réservable aux comptes de secours
    Étant donné que je suis connecté en tant que "administration technique IQ"
    Quand j'exécute le cas "iq.sso.connexion"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — authentification unique"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-07 @IQ-D-20
  Scénario: Les données d'IQ sont introuvables pour une autre organisation
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.isolation"
    Alors la plateforme se comporte comme attendu
