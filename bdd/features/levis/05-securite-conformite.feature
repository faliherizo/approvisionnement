# language: fr
@AO-LEVIS-2026-50-92
Fonctionnalité: Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3)

  @LEV-C-16 @LEV-C-21
  Scénario: Accès par rôle, second facteur et authentification unique de l'annuaire de la Ville
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.securite.roles"
    Et j'exécute le cas "lev.securite.mfa"
    Et j'exécute le cas "lev.securite.sso"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — sécurité du compte"
    Alors l'écran est accessible, sans débordement ni superposition

  @LEV-C-17
  Scénario: Le journal d'audit trace les actions et se consulte par l'administration
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'ouvre l'écran "Lévis — journal d'audit"
    Alors l'écran est accessible, sans débordement ni superposition

  @LEV-C-22
  Scénario: Les données d'une organisation sont invisibles pour les autres
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.multitenant.isolation"
    Alors la plateforme se comporte comme attendu

  @LEV-C-15
  Scénario: Exercer les droits Loi 25 d'accès et d'effacement d'une personne concernée
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "lev.loi25.droits"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — vie privée (Loi 25)"
    Et je saisis "redacteur@levis.qc.ca" dans le champ "Courriel de la personne concernée"
    Et je clique sur "Rassembler les renseignements"
    Alors je vois "Télécharger le dossier"
    Et l'écran est accessible, sans débordement ni superposition

  @LEV-C-20
  Scénario: Exporter les métadonnées d'un devis au format SEAO
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.seao.export"
    Alors la plateforme se comporte comme attendu
