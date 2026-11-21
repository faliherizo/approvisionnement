# language: fr
@AO-LEVIS-2026-50-92
Fonctionnalité: Addendas interactifs et documents finaux (avis 2026-50-92, art. 2.1 n° 7 et 8 ; cahier EF3)

  @LEV-2.1-7 @LEV-2.1-8 @LEV-C-10
  Scénario: Un addenda modifie une clause précise, est émis, puis intégré au document final en PDF et en Word
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Quand j'exécute le cas "lev.addendas.integration"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — éditeur du document"
    Alors je vois "Report de la date d'ouverture ${unique}"
    Et l'écran est accessible, sans débordement ni superposition
