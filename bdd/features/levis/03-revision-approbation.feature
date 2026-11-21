# language: fr
@AO-LEVIS-2026-50-92
Fonctionnalité: Révision et approbation par plusieurs intervenants (avis 2026-50-92, art. 2.1 n° 6 ; cahier EF4)

  @LEV-2.1-6 @LEV-2.1-6a @LEV-2.1-6b @LEV-C-11 @LEV-C-17
  Scénario: Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.revision.circuit"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — éditeur du document"
    Alors l'écran est accessible, sans débordement ni superposition

  @LEV-C-12
  Scénario: L'administration configure le circuit d'approbation d'un type de document
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "lev.revision.workflow-configurable"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — circuits d'approbation"
    Alors l'écran est accessible, sans débordement ni superposition
