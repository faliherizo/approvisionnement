# language: fr
@AO-TR-7000017905
Fonctionnalité: Sécurité, traçabilité et conservation (AO 7000017905, section III n° 5 à 13)

  @TR-05 @TR-06 @TR-07
  Scénario: Authentification corporative, second facteur et rôles distincts
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.securite.roles"
    Et j'exécute le cas "lev.securite.mfa"
    Et j'exécute le cas "lev.securite.sso"
    Alors la plateforme se comporte comme attendu

  @TR-08
  Scénario: Les secrets d'intégration sont conservés chiffrés et jamais restitués
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.secrets.chiffres"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "TR — paramètres"
    Alors l'écran est accessible, sans débordement ni superposition

  @TR-09 @TR-12
  Scénario: Journal d'accès et d'activité filtré puis exporté
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Et j'exécute le cas "tr.document.approuve"
    Quand j'exécute le cas "tr.journal.export"
    Alors la plateforme se comporte comme attendu
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'ouvre l'écran "TR — journal d'audit"
    Alors l'écran est accessible, sans débordement ni superposition

  @TR-11 @TR-12
  Scénario: Versions, comparaison et historique des validations sont conservés
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Et j'exécute le cas "tr.document.approuve"
    Quand j'exécute le cas "tr.versions.tracabilite"
    Alors la plateforme se comporte comme attendu

  @TR-13
  Scénario: La séparation des responsabilités empêche de décider à la place d'un autre rôle
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "tr.separation.responsabilites"
    Alors la plateforme se comporte comme attendu

  @TR-10 @TR-23
  Scénario: Les étapes de validation se configurent par type de document
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.workflow.par-type"
    Alors la plateforme se comporte comme attendu
