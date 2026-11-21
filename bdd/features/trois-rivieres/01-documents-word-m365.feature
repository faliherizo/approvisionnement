# language: fr
@AO-TR-7000017905
Fonctionnalité: Documents Word et Microsoft 365 (AO 7000017905, section III n° 2, 3, 4, 20)

  Contexte:
    Étant donné que je suis connecté en tant que "rédacteur de Lévis"
    Et j'exécute le cas "tr.document.approuve"

  @TR-02
  Scénario: Le document approuvé se télécharge en Word modifiable et en PDF
    Quand j'exécute le cas "tr.word.modifiable"
    Alors la plateforme se comporte comme attendu

  @TR-04 @TR-20
  Scénario: Publication vers SharePoint, OneDrive, Teams et Outlook, seulement après validation
    Quand j'exécute le cas "tr.m365.publication"
    Alors la plateforme se comporte comme attendu

  @TR-03 @TR-16
  Scénario: Les modèles Word existants de la Ville s'importent, d'abord à l'essai sans rien écrire
    Étant donné que je suis connecté en tant que "administrateur de Lévis"
    Quand j'exécute le cas "tr.word.migration"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "TR — import et migration"
    Alors l'écran est accessible, sans débordement ni superposition
