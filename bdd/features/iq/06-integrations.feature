# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Intégrations Salesforce, Workday et signature en lot (Cahier § 1.4.3 à 1.4.5, § 1.7.1 ; DAO § 3.3.5-3.3.6 ; addenda 1 et 2)

  @IQ-A-01 @IQ-A-02 @IQ-C-06 @IQ-C-16 @IQ-C-69 @IQ-D-12 @IQ-D-26 @IQ-D-27 @IQ-D-28 @IQ-A-07 @IQ-C-03
  Scénario: Un système externe (Workday) obtient un jeton OAuth, crée une trousse par l'API, en lit le statut, puis est révoqué
    Étant donné que je suis connecté en tant que "administration technique IQ"
    Quand j'exécute le cas "iq.integration.api"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — intégrations"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-08 @IQ-C-13 @IQ-C-15 @IQ-A-05 @IQ-A-10 @IQ-A-12 @IQ-A-14
  Scénario: Les statuts reviennent vers Salesforce, résistent à une panne et se relancent ; le JWT Bearer exige sa clé
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.salesforce.statuts"
    Alors la plateforme se comporte comme attendu

  @IQ-A-13
  Scénario: Seuls les champs Salesforce déclarés sont lus
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.salesforce.minimisation"
    Alors la plateforme se comporte comme attendu

  @IQ-A-03 @IQ-A-28
  Scénario: Signature en lot depuis la plateforme et approbation globale du lot
    Étant donné que je suis connecté en tant que "initiateur IQ"
    Quand j'exécute le cas "iq.lots"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — signature en lot"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-D-29 @IQ-C-16
  Scénario: Les interfaces sont standards et documentées (OpenAPI, SCIM)
    Étant donné que je suis connecté en tant que "administration technique IQ"
    Quand j'exécute le cas "iq.api.documentee"
    Alors la plateforme se comporte comme attendu

  @IQ-A-09
  Scénario: Un assistant d'IA se connecte au connecteur MCP et lit, en lecture seule, le statut d'une trousse de son client
    Étant donné que je suis connecté en tant que "administration technique IQ"
    Quand j'exécute le cas "iq.mcp"
    Alors la plateforme se comporte comme attendu
    Quand un assistant MCP de référence se connecte avec le jeton capturé
    Alors l'assistant voit des outils en lecture seule et lit la trousse de son client
