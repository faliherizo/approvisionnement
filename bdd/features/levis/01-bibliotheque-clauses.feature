# language: fr
@AO-LEVIS-2026-50-92
Fonctionnalité: Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1)
  La Ville rédige ses clauses administratives une seule fois et les réutilise dans tous ses devis et contrats.

  Contexte:
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"

  @LEV-2.1-1 @LEV-2.1-3 @LEV-C-01
  Scénario: Créer, modifier, retrouver puis archiver des clauses standards, optionnelles et personnalisées
    Quand j'exécute le cas "lev.clauses.bibliotheque"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — bibliothèque de clauses"
    Et je saisis "${unique}" dans le champ "Recherche"
    Alors je vois "Délai d'exécution ${unique}"
    Et l'écran est accessible, sans débordement ni superposition

  @LEV-C-02
  Scénario: Rechercher des clauses par terme, catégorie et type de contrat
    Quand j'exécute le cas "lev.clauses.recherche-multicritere"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — recherche"
    Alors l'écran est accessible, sans débordement ni superposition

  @LEV-2.1-9 @LEV-C-13
  Scénario: Les clauses normatives exigées par la loi sont fournies et verrouillées
    Quand j'exécute le cas "lev.normatif.verrouille"
    Alors la plateforme se comporte comme attendu

  @LEV-C-03
  Scénario: Consulter et restaurer chaque version d'une clause, avec son auteur et sa date
    Quand j'exécute le cas "lev.clauses.historique"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "Lévis — bibliothèque de clauses"
    Et je saisis "HIS-${UNIQUE}" dans le champ "Recherche"
    Et je clique sur "Historique de la clause HIS-${UNIQUE}"
    Alors je vois "Restauration de la version 1"
    Et l'écran est accessible, sans débordement ni superposition
