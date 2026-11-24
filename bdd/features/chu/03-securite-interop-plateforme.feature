# language: fr
@AO-CHU-AVIS-INTENTION
Fonctionnalité: Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8)

  Contexte:
    Étant donné l'organisation "CHU" est prête
    Et je suis connecté en tant que "administrateur CHU"

  @CHU-7.03 @CHU-7.04 @CHU-7.06 @CHU-7.07 @CHU-7.09 @CHU-7.01
  Scénario: Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable
    Quand j'exécute le cas "chu.securite"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — sécurité avancée"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-7.02
  Scénario: Second facteur exigé et vérifié (question secrète, TOTP RFC 6238)
    Quand j'exécute le cas "lev.securite.mfa"
    Alors la plateforme se comporte comme attendu

  @CHU-1.01 @CHU-6.05 @CHU-7.02
  Scénario: Comptes sans limite de nombre, rôles distincts et politique de mot de passe
    Quand j'exécute le cas "chu.droits"
    Alors la plateforme se comporte comme attendu

  @CHU-8.01 @CHU-8.02 @CHU-8.03
  Scénario: Un système externe crée une fiche par l'API publique avec un jeton OAuth
    Quand j'exécute le cas "chu.interop"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — interopérabilité"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-8.04
  Scénario: Les échanges de la plateforme précédente sont maintenus sous leurs anciens noms, et l'historique est repris
    Quand j'exécute le cas "chu.echanges.herites"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — interopérabilité"
    Alors je vois "Échanges hérités"
    Et l'écran est accessible, sans débordement ni superposition
    Quand je choisis "ui-${unique}" dans la liste "Échange"
    Et je saisis '[{"NOM_EMPLOYE": "Zoé"}, {"MATRICULE": "7"}]' dans le champ "Export JSON"
    Et je clique sur "Essayer"
    Alors je vois "Rien n'a été écrit"
    Et l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "CHU — interopérabilité (téléphone)"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "CHU — interopérabilité (tablette)"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "CHU — interopérabilité (grand écran)"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-1.03
  Scénario: Les formulaires du CHU sont invisibles pour les autres clients
    Quand j'exécute le cas "chu.cloisonnement"
    Alors la plateforme se comporte comme attendu

  @CHU-1.04
  Scénario: Applications par domaine dans les environnements DEV, UAT et PROD
    Quand j'exécute le cas "chu.applications"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "CHU — applications"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-2.03
  Scénario: L'organisation porte son image institutionnelle
    Quand j'exécute le cas "chu.theme"
    Alors la plateforme se comporte comme attendu

  @CHU-1.08 @CHU-2.04
  Scénario: Affichage sur téléphone et tablette, accessible et sans débordement
    Quand j'ouvre l'écran "CHU — formulaires (téléphone)"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "CHU — formulaires (tablette)"
    Alors l'écran est accessible, sans débordement ni superposition

  @CHU-2.01
  Scénario: L'interface d'administration s'offre en français et en anglais
    Quand j'ouvre l'écran "CHU — formulaires"
    Et je choisis la langue "English"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand je choisis la langue "Français"

  @CHU-7.08
  Scénario: Un fichier joint porteur d'un maliciel est refusé par l'antivirus, un fichier sain est conservé
    Quand j'exécute le cas "chu.antivirus.pieces-jointes"
    Alors la plateforme se comporte comme attendu
