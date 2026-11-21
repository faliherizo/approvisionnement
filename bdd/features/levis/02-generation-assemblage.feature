# language: fr
@AO-LEVIS-2026-50-92
Fonctionnalité: Génération des documents par assemblage de clauses (avis 2026-50-92, art. 2.1 n° 1, 2, 4, 5 ; cahier EF2, EF3)

  Contexte:
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"

  @LEV-2.1-1 @LEV-2.1-2 @LEV-C-07 @LEV-C-08 @LEV-C-09
  Scénario: Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention
    Quand j'exécute le cas "lev.documents.types"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — documents contractuels"
    Alors je vois "Avis d'intention BDD ${unique}"
    Et l'écran est accessible, sans débordement ni superposition

  @LEV-2.1-4 @LEV-2.1-5a @LEV-2.1-5b @LEV-2.1-5c @LEV-2.1-5d @LEV-C-04 @LEV-C-05
  Scénario: Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent
    Quand j'exécute le cas "lev.assemblage.adaptation"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — éditeur du document"
    Alors l'écran est accessible, sans débordement ni superposition

  @LEV-2.1-5e @LEV-C-06
  Scénario: Une clause entraîne celles qu'elle requiert et écarte celles qui lui sont incompatibles
    Quand j'exécute le cas "lev.assemblage.dependances"
    Alors la plateforme se comporte comme attendu
