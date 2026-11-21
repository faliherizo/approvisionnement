# language: fr
@AO-TR-7000017905
Fonctionnalité: IA encadrée, réversibilité, dossiers et rapports (AO 7000017905, section III n° 14, 15, 19, 21, 22, 24, 29)

  @TR-15 @TR-24
  Scénario: L'IA ne modifie rien sans validation humaine et l'organisation peut la désactiver
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Et j'exécute le cas "tr.document.approuve"
    Quand j'exécute le cas "tr.ia.encadree"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "TR — assistant IA"
    Alors l'écran est accessible, sans débordement ni superposition

  @TR-19
  Scénario: Export complet des données en fin de contrat
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.export.complet"
    Alors la plateforme se comporte comme attendu

  @TR-21
  Scénario: Un dossier d'approvisionnement regroupe documents, versions et validations
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Et j'exécute le cas "tr.document.approuve"
    Quand j'exécute le cas "tr.dossier.approvisionnement"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "TR — dossiers d'approvisionnement"
    Alors l'écran est accessible, sans débordement ni superposition

  @TR-22
  Scénario: Rapports de suivi des documents, validations et dossiers
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.rapports.suivi"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "TR — rapports de suivi"
    Alors l'écran est accessible, sans débordement ni superposition
    Quand j'ouvre l'écran "TR — tableau de bord"
    Alors l'écran est accessible, sans débordement ni superposition

  @TR-29
  Scénario: L'API REST est documentée par une spécification OpenAPI
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.api.openapi"
    Alors la plateforme se comporte comme attendu
