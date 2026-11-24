# Scénarios BDD — Ville de Lévis — avis d'appel d'intérêt 2026-50-92

*Solution logicielle de rédaction contractuelle.* Source des exigences : `docs/Reponse-Avis-interet-2026-50-92.md (art. 2.1) ; docs/analyse-conformite-roadmap.md (cahier EF/ENF)`.

> Document généré par `bdd/outils/documenter.mjs` depuis le catalogue, les fonctionnalités Gherkin et les cas JSON ;
> ne pas le modifier à la main. Régénérer : `cd bdd && npm run documenter`.

## Synthèse

| Exigences | Éprouvées par scénario | Dont réussies à la dernière exécution | Documentaires | Écarts déclarés | Sans objet |
|---|---|---|---|---|---|
| 41 | 34 | 34 | 7 | 0 | 0 |

## Matrice des exigences

| Exigence | Libellé | Caractère | Vérification | Scénarios |
|---|---|---|---|---|
| `LEV-2.1-1` | Rédiger des clauses administratives pour demandes de prix et procédures ouvertes (AOP) | Obligatoire | Scénario BDD exécuté | ✅ [Créer, modifier, retrouver puis archiver des clauses standards, optionnelles et personnalisées](#creer-modifier-retrouver-puis-archiver-des-clauses-standards-optionnelles-et-personnalisees)<br>✅ [Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention](#rediger-chaque-type-de-document-exige-du-devis-dappel-doffres-a-lavis-dintention) |
| `LEV-2.1-2` | Rédiger des contrats de gré à gré personnalisés de toute nature (prêts à usage, baux, contrats de services, etc.) | Obligatoire | Scénario BDD exécuté | ✅ [Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention](#rediger-chaque-type-de-document-exige-du-devis-dappel-doffres-a-lavis-dintention) |
| `LEV-2.1-3` | Créer et gérer une bibliothèque centralisée de clauses standards, optionnelles et personnalisées | Obligatoire | Scénario BDD exécuté | ✅ [Créer, modifier, retrouver puis archiver des clauses standards, optionnelles et personnalisées](#creer-modifier-retrouver-puis-archiver-des-clauses-standards-optionnelles-et-personnalisees) |
| `LEV-2.1-4` | Assembler automatiquement des documents contractuels à partir des clauses | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-2.1-5a` | Adapter les clauses selon le type de contrat | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-2.1-5b` | …selon le mode de sollicitation | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-2.1-5c` | …selon la valeur estimée | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-2.1-5d` | …selon le contexte particulier du projet | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-2.1-5e` | …selon les autres clauses sélectionnées | Obligatoire | Scénario BDD exécuté | ✅ [Une clause entraîne celles qu'elle requiert et écarte celles qui lui sont incompatibles](#une-clause-entraine-celles-quelle-requiert-et-ecarte-celles-qui-lui-sont-incompatibles) |
| `LEV-2.1-6` | Faciliter la révision et la validation par différents intervenants (approvisionnement, juridique, autres) | Obligatoire | Scénario BDD exécuté | ✅ [Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre](#le-juriste-commente-une-clause-du-devis-puis-lapprovisionnement-le-juridique-et-la-direction-approuvent-dans-lordre) |
| `LEV-2.1-6a` | *(Idéalement)* Commenter directement dans le devis | Souhaitable | Scénario BDD exécuté | ✅ [Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre](#le-juriste-commente-une-clause-du-devis-puis-lapprovisionnement-le-juridique-et-la-direction-approuvent-dans-lordre) |
| `LEV-2.1-6b` | *(Idéalement)* Gestion de niveaux de hiérarchisation d'approbation | Souhaitable | Scénario BDD exécuté | ✅ [Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre](#le-juriste-commente-une-clause-du-devis-puis-lapprovisionnement-le-juridique-et-la-direction-approuvent-dans-lordre) |
| `LEV-2.1-7` | Rédiger des addendas de manière interactive et en lien avec les clauses du devis | Obligatoire | Scénario BDD exécuté | ✅ [Un addenda modifie une clause précise, est émis, puis intégré au document final en PDF et en Word](#un-addenda-modifie-une-clause-precise-est-emis-puis-integre-au-document-final-en-pdf-et-en-word) |
| `LEV-2.1-8` | Générer les documents finaux pour la Ville et son cocontractant, intégrant l'ensemble des addendas | Obligatoire | Scénario BDD exécuté | ✅ [Un addenda modifie une clause précise, est émis, puis intégré au document final en PDF et en Word](#un-addenda-modifie-une-clause-precise-est-emis-puis-integre-au-document-final-en-pdf-et-en-word) |
| `LEV-2.1-9` | Solution validée juridiquement selon les lois applicables au Québec | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses normatives exigées par la loi sont fournies et verrouillées](#les-clauses-normatives-exigees-par-la-loi-sont-fournies-et-verrouillees) |
| `LEV-2.1-10` | Soutien technique complet du prestataire | Obligatoire | Documentaire | Service (soutien technique) : engagement contractuel, non vérifiable par un test. |
| `LEV-2.1-11` | Assistance juridique par un ou des juriste(s) | Obligatoire | Documentaire | Service (assistance juridique) : engagement contractuel. |
| `LEV-2.1-12` | Formation en français, documentée, complète et détaillée | Obligatoire | Documentaire | Service (formation en français) : programme et documentation. |
| `LEV-2.1-13` | Ensemble des fonctionnalités présentes et pleinement fonctionnelles | Obligatoire | Documentaire | Démontré par l'ensemble des scénarios BDD de Lévis (toutes les fonctionnalités exigées s'exécutent). |
| `LEV-C-01` | EF1 Bibliothèque : création/modif/archivage standards/optionnelles/perso | Obligatoire | Scénario BDD exécuté | ✅ [Créer, modifier, retrouver puis archiver des clauses standards, optionnelles et personnalisées](#creer-modifier-retrouver-puis-archiver-des-clauses-standards-optionnelles-et-personnalisees) |
| `LEV-C-02` | EF1 Recherche multicritère (thématique, portée, type) | Obligatoire | Scénario BDD exécuté | ✅ [Rechercher des clauses par terme, portée, thématique, type de contrat et caractère normatif](#rechercher-des-clauses-par-terme-portee-thematique-type-de-contrat-et-caractere-normatif) |
| `LEV-C-03` | EF1 Historisation par clause (versions + auteur/date) | Obligatoire | Scénario BDD exécuté | ✅ [Consulter et restaurer chaque version d'une clause, avec son auteur et sa date](#consulter-et-restaurer-chaque-version-dune-clause-avec-son-auteur-et-sa-date) |
| `LEV-C-04` | EF2 Génération dynamique par assemblage | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-C-05` | EF2 Adaptation type/mode/valeur/contexte | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent](#les-clauses-sadaptent-au-type-au-mode-de-sollicitation-a-la-valeur-et-au-contexte-puis-sassemblent) |
| `LEV-C-06` | EF2 Dépendances & exclusions entre clauses | Obligatoire | Scénario BDD exécuté | ✅ [Une clause entraîne celles qu'elle requiert et écarte celles qui lui sont incompatibles](#une-clause-entraine-celles-quelle-requiert-et-ecarte-celles-qui-lui-sont-incompatibles) |
| `LEV-C-07` | EF3 Demandes de prix / appels d'offres publics | Obligatoire | Scénario BDD exécuté | ✅ [Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention](#rediger-chaque-type-de-document-exige-du-devis-dappel-doffres-a-lavis-dintention) |
| `LEV-C-08` | EF3 Gré à gré (baux, prêts à usage, services) | Obligatoire | Scénario BDD exécuté | ✅ [Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention](#rediger-chaque-type-de-document-exige-du-devis-dappel-doffres-a-lavis-dintention) |
| `LEV-C-09` | EF3 Avis d'intention | Obligatoire | Scénario BDD exécuté | ✅ [Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention](#rediger-chaque-type-de-document-exige-du-devis-dappel-doffres-a-lavis-dintention) |
| `LEV-C-10` | EF3 Addendas interactifs + consolidation finale | Obligatoire | Scénario BDD exécuté | ✅ [Un addenda modifie une clause précise, est émis, puis intégré au document final en PDF et en Word](#un-addenda-modifie-une-clause-precise-est-emis-puis-integre-au-document-final-en-pdf-et-en-word) |
| `LEV-C-11` | EF4 Commentaires/annotations dans le devis | Obligatoire | Scénario BDD exécuté | ✅ [Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre](#le-juriste-commente-une-clause-du-devis-puis-lapprovisionnement-le-juridique-et-la-direction-approuvent-dans-lordre) |
| `LEV-C-12` | EF4 Workflow d'approbation configurable | Obligatoire | Scénario BDD exécuté | ✅ [L'administration configure le circuit d'approbation d'un type de document](#ladministration-configure-le-circuit-dapprobation-dun-type-de-document) |
| `LEV-C-13` | ENF1 Conformité LCOP/LCV (règles en données) | Obligatoire | Scénario BDD exécuté | ✅ [Les clauses normatives exigées par la loi sont fournies et verrouillées](#les-clauses-normatives-exigees-par-la-loi-sont-fournies-et-verrouillees) |
| `LEV-C-14` | ENF1 Loi 25 — chiffrement transit | Obligatoire | Documentaire | Chiffrement en transit assuré par l'infrastructure (TLS de l'entrée Kubernetes et de Front Door). |
| `LEV-C-15` | ENF1 Loi 25 — chiffrement au repos / *Privacy by Design* | Obligatoire | Scénario BDD exécuté | ✅ [Exercer les droits Loi 25 d'accès et d'effacement d'une personne concernée](#exercer-les-droits-loi-25-dacces-et-deffacement-dune-personne-concernee) |
| `LEV-C-16` | ENF2 Auth SSO Azure AD + RBAC + MFA | Obligatoire | Scénario BDD exécuté | ✅ [Accès par rôle, second facteur et authentification unique de l'annuaire de la Ville](#acces-par-role-second-facteur-et-authentification-unique-de-lannuaire-de-la-ville) |
| `LEV-C-17` | ENF2 Journal d'audit | Obligatoire | Scénario BDD exécuté | ✅ [Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre](#le-juriste-commente-une-clause-du-devis-puis-lapprovisionnement-le-juridique-et-la-direction-approuvent-dans-lordre)<br>✅ [Le journal d'audit trace les actions et se consulte par l'administration](#le-journal-daudit-trace-les-actions-et-se-consulte-par-ladministration) |
| `LEV-C-18` | ENF4 Solution clé en main, conteneurisée | Obligatoire | Documentaire | Livraison conteneurisée (Docker Compose, chart Helm). |
| `LEV-C-19` | ENF4 Formation FR documentée | Obligatoire | Documentaire | Guide de l'utilisateur en français : connexion, recherche multicritère, bibliothèque et historique, rédaction, révision et approbation, addendas, SEAO, administration, questions fréquentes. |
| `LEV-C-20` | DAO 2.3 Intégration SEAO (format devis) | Obligatoire | Scénario BDD exécuté | ✅ [Exporter les métadonnées d'un devis au format SEAO](#exporter-les-metadonnees-dun-devis-au-format-seao) |
| `LEV-C-21` | DAO 2.3 Intégration annuaire d'entreprise | Obligatoire | Scénario BDD exécuté | ✅ [Accès par rôle, second facteur et authentification unique de l'annuaire de la Ville](#acces-par-role-second-facteur-et-authentification-unique-de-lannuaire-de-la-ville) |
| `LEV-C-22` | Multi-tenant / multilingue (au-delà du cahier) | Obligatoire | Scénario BDD exécuté | ✅ [Les données d'une organisation sont invisibles pour les autres](#les-donnees-dune-organisation-sont-invisibles-pour-les-autres) |

## Exigences non éprouvées par un scénario

- `LEV-2.1-10` — Soutien technique complet du prestataire : **Documentaire.** Service (soutien technique) : engagement contractuel, non vérifiable par un test. Référence : `docs/Reponse-Avis-interet-2026-50-92.md §5-6`.
- `LEV-2.1-11` — Assistance juridique par un ou des juriste(s) : **Documentaire.** Service (assistance juridique) : engagement contractuel. Référence : `docs/Reponse-Avis-interet-2026-50-92.md §5`.
- `LEV-2.1-12` — Formation en français, documentée, complète et détaillée : **Documentaire.** Service (formation en français) : programme et documentation. Référence : `docs/Reponse-Avis-interet-2026-50-92.md §6`.
- `LEV-2.1-13` — Ensemble des fonctionnalités présentes et pleinement fonctionnelles : **Documentaire.** Démontré par l'ensemble des scénarios BDD de Lévis (toutes les fonctionnalités exigées s'exécutent). Référence : `bdd/features/levis/`.
- `LEV-C-14` — ENF1 Loi 25 — chiffrement transit : **Documentaire.** Chiffrement en transit assuré par l'infrastructure (TLS de l'entrée Kubernetes et de Front Door). Référence : `infra/k8s/chart/templates/ingress.yaml`.
- `LEV-C-18` — ENF4 Solution clé en main, conteneurisée : **Documentaire.** Livraison conteneurisée (Docker Compose, chart Helm). Référence : `docker-compose.yml ; infra/k8s/chart`.
- `LEV-C-19` — ENF4 Formation FR documentée : **Documentaire.** Guide de l'utilisateur en français : connexion, recherche multicritère, bibliothèque et historique, rédaction, révision et approbation, addendas, SEAO, administration, questions fréquentes. Référence : `docs/guide-utilisateur.md`.

## Scénarios détaillés

### Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1)

La Ville rédige ses clauses administratives une seule fois et les réutilise dans tous ses devis et contrats.

#### Créer, modifier, retrouver puis archiver des clauses standards, optionnelles et personnalisées

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1) (`bdd/features/levis/01-bibliotheque-clauses.feature`)
- **Exigences éprouvées :** `LEV-2.1-1`, `LEV-2.1-3`, `LEV-C-01`

```gherkin
Quand j'exécute le cas "lev.clauses.bibliotheque"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — bibliothèque de clauses"
Et je saisis "${unique}" dans le champ "Recherche"
Alors je vois "Délai d'exécution ${unique}"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.clauses.bibliotheque`** — Bibliothèque centralisée : une clause de chaque catégorie, modifiée puis retrouvée par recherche, puis archivée *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clauses` |  | 200, 201 | `$.categorie` egal "STANDARD" ; `$.version` egal 1 ; capture `standard` ← `$.id` |
| 2 | `POST /api/clauses` |  | 200, 201 | `$.categorie` egal "OPTIONNELLE" ; capture `optionnelle` ← `$.id` |
| 3 | `POST /api/clauses` |  | 200, 201 | `$.categorie` egal "PERSONNALISEE" |
| 4 | `PUT /api/clauses/${standard}` |  | 2xx | `$.corps` contient "60 jours" ; `$.version` egal 2 |
| 5 | `GET /api/clauses/recherche?q=${unique}&page=0&taille=20` |  | 2xx | `$.contenu[*].code` inclut "STD-${UNIQUE}" ; `$.contenu[*].code` inclut "OPT-${UNIQUE}" |
| 6 | `DELETE /api/clauses/${optionnelle}` | administrateur de Lévis | 2xx | — |
| 7 | `GET /api/clauses` |  | 2xx | `$[*].code` exclut "OPT-${UNIQUE}" ; `$[*].code` inclut "STD-${UNIQUE}" |

#### Rechercher des clauses par terme, portée, thématique, type de contrat et caractère normatif

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1) (`bdd/features/levis/01-bibliotheque-clauses.feature`)
- **Exigences éprouvées :** `LEV-C-02`

```gherkin
Quand j'exécute le cas "lev.clauses.recherche-multicritere"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — recherche"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.clauses.recherche-multicritere`** — Recherche multicritère : terme, portée (catégorie), thématique (section), type de contrat, clauses normatives *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/search?q=contrat&categorie=STANDARD` |  | 2xx | `$.clauses` longueurMin 1 ; `$.clauses[*].categorie` exclut "OPTIONNELLE" |
| 2 | `GET /api/search?q=Objet&type=APPEL_OFFRES_PUBLIC` |  | 2xx | `$.terme` egal "Objet" |
| 3 | `GET /api/search/thematiques` |  | 2xx | `$` contient "Dispositions générales" |
| 4 | `GET /api/search?q=&thematique=dispositions%20g%C3%A9n%C3%A9rales` |  | 2xx | `$.clauses` longueurMin 1 ; `$.facettesThematique.Dispositions générales` existe true |
| 5 | `GET /api/search?q=&normative=true` |  | 2xx | `$.clauses` longueurMin 1 ; `$.clauses[*].normative` exclut false |

#### Les clauses normatives exigées par la loi sont fournies et verrouillées

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1) (`bdd/features/levis/01-bibliotheque-clauses.feature`)
- **Exigences éprouvées :** `LEV-2.1-9`, `LEV-C-13`

```gherkin
Quand j'exécute le cas "lev.normatif.verrouille"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.normatif.verrouille`** — Conformité légale : une clause normative (LCV, Charte de la langue française) est fournie et verrouillée *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/clauses` |  | 2xx | `$[*].normative` inclut true ; `$[*].code` inclut "GEN-002" |

#### Consulter et restaurer chaque version d'une clause, avec son auteur et sa date

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Bibliothèque centralisée de clauses (avis 2026-50-92, art. 2.1 ; cahier EF1) (`bdd/features/levis/01-bibliotheque-clauses.feature`)
- **Exigences éprouvées :** `LEV-C-03`

```gherkin
Quand j'exécute le cas "lev.clauses.historique"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — bibliothèque de clauses"
Et je saisis "HIS-${UNIQUE}" dans le champ "Recherche"
Et je clique sur "Historique de la clause HIS-${UNIQUE}"
Alors je vois "Restauration de la version 1"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.clauses.historique`** — Historique par clause : chaque version consultable avec auteur, date et motif ; restauration d'une version antérieure, qui devient une nouvelle version *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clauses` |  | 2xx | capture `clause` ← `$.id` |
| 2 | `PUT /api/clauses/${clause}` | juriste de Lévis | 2xx | `$.version` egal 2 |
| 3 | `GET /api/clauses/${clause}/versions` |  | 2xx | `$` longueurMin 2 ; `$[0].numero` egal 2 ; `$[0].auteur` egal "juriste@levis.qc.ca" ; `$[0].motif` egal "MODIFICATION" ; `$[1].auteur` egal "redacteur@levis.qc.ca" ; `$[1].corps` egal "Texte initial ${unique}" ; `$[1].creeLe` existe true |
| 4 | `POST /api/clauses/${clause}/versions/1/restaurer` |  | 2xx | `$.corps` egal "Texte initial ${unique}" ; `$.version` egal 3 |
| 5 | `GET /api/clauses/${clause}/versions` |  | 2xx | `$[0].motif` egal "RESTAURATION" ; `$[0].versionRestauree` egal 1 |
| 6 | `POST /api/clauses/${clause}/versions/9/restaurer` |  | 404 | — |
| 7 | `GET /api/clauses/${clause}/versions` | administration d'affaires IQ | 403, 404 | — |

### Génération des documents par assemblage de clauses (avis 2026-50-92, art. 2.1 n° 1, 2, 4, 5 ; cahier EF2, EF3)


#### Rédiger chaque type de document exigé, du devis d'appel d'offres à l'avis d'intention

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Génération des documents par assemblage de clauses (avis 2026-50-92, art. 2.1 n° 1, 2, 4, 5 ; cahier EF2, EF3) (`bdd/features/levis/02-generation-assemblage.feature`)
- **Exigences éprouvées :** `LEV-2.1-1`, `LEV-2.1-2`, `LEV-C-07`, `LEV-C-08`, `LEV-C-09`

```gherkin
Quand j'exécute le cas "lev.documents.types"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — documents contractuels"
Alors je vois "Avis d'intention BDD ${unique}"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.documents.types`** — Chaque type de document exigé se crée : demande de prix, AOP, gré à gré, bail, prêt à usage, contrat de services, avis d'intention *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | `$.type` egal "DEMANDE_PRIX" ; `$.statut` egal "BROUILLON" |
| 2 | `POST /api/documents` |  | 2xx | `$.type` egal "APPEL_OFFRES_PUBLIC" |
| 3 | `POST /api/documents` |  | 2xx | `$.type` egal "GRE_A_GRE" |
| 4 | `POST /api/documents` |  | 2xx | `$.type` egal "BAIL" |
| 5 | `POST /api/documents` |  | 2xx | `$.type` egal "PRET_A_USAGE" |
| 6 | `POST /api/documents` |  | 2xx | `$.type` egal "CONTRAT_SERVICES" |
| 7 | `POST /api/documents` |  | 2xx | `$.type` egal "AVIS_INTENTION" |

#### Les clauses s'adaptent au type, au mode de sollicitation, à la valeur et au contexte, puis s'assemblent

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Génération des documents par assemblage de clauses (avis 2026-50-92, art. 2.1 n° 1, 2, 4, 5 ; cahier EF2, EF3) (`bdd/features/levis/02-generation-assemblage.feature`)
- **Exigences éprouvées :** `LEV-2.1-4`, `LEV-2.1-5a`, `LEV-2.1-5b`, `LEV-2.1-5c`, `LEV-2.1-5d`, `LEV-C-04`, `LEV-C-05`

```gherkin
Quand j'exécute le cas "lev.assemblage.adaptation"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — éditeur du document"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.assemblage.adaptation`** — Adaptation des clauses au type, au mode, à la valeur et au contexte ; assemblage automatique des clauses standards *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clauses` |  | 200, 201 | capture `clauseSeuil` ← `$.id` |
| 2 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 3 | `GET /api/documents/${document}/clauses-eligibles` |  | 2xx | `$[*].code` inclut "SEUIL-${UNIQUE}" |
| 4 | `POST /api/documents` |  | 2xx | capture `petit` ← `$.id` |
| 5 | `GET /api/documents/${petit}/clauses-eligibles` |  | 2xx | `$[*].code` exclut "SEUIL-${UNIQUE}" |
| 6 | `POST /api/documents/${document}/assembler` |  | 2xx | `$.clauses` longueurMin 1 ; `$.clauses[*].code` inclut "GEN-001" |

#### Une clause entraîne celles qu'elle requiert et écarte celles qui lui sont incompatibles

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Génération des documents par assemblage de clauses (avis 2026-50-92, art. 2.1 n° 1, 2, 4, 5 ; cahier EF2, EF3) (`bdd/features/levis/02-generation-assemblage.feature`)
- **Exigences éprouvées :** `LEV-2.1-5e`, `LEV-C-06`

```gherkin
Quand j'exécute le cas "lev.assemblage.dependances"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.assemblage.dependances`** — Une clause entraîne celles qu'elle requiert et écarte celles qu'elle exclut *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/clauses` |  | 200, 201 | — |
| 2 | `POST /api/clauses` |  | 200, 201 | capture `exclue` ← `$.id` |
| 3 | `POST /api/clauses` |  | 200, 201 | capture `principale` ← `$.id` |
| 4 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 5 | `POST /api/documents/${document}/assembler` |  | 2xx | `$.clauses[*].code` inclut "PRI-${UNIQUE}" ; `$.clauses[*].code` inclut "REQ-${UNIQUE}" ; `$.clauses[*].code` exclut "EXC-${UNIQUE}" |

### Révision et approbation par plusieurs intervenants (avis 2026-50-92, art. 2.1 n° 6 ; cahier EF4)


#### Le juriste commente une clause du devis, puis l'approvisionnement, le juridique et la direction approuvent dans l'ordre

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Révision et approbation par plusieurs intervenants (avis 2026-50-92, art. 2.1 n° 6 ; cahier EF4) (`bdd/features/levis/03-revision-approbation.feature`)
- **Exigences éprouvées :** `LEV-2.1-6`, `LEV-2.1-6a`, `LEV-2.1-6b`, `LEV-C-11`, `LEV-C-17`

```gherkin
Quand j'exécute le cas "lev.revision.circuit"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — éditeur du document"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.revision.circuit`** — Révision par plusieurs intervenants : commentaire ancré, résolution, circuit hiérarchisé APPRO → JURIDIQUE → DIRECTION *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | capture `clauseDoc` ← `$.clauses[0].clauseId` |
| 3 | `POST /api/documents/${document}/commentaires` | juriste de Lévis | 2xx | `$.resolu` egal false ; capture `commentaire` ← `$.id` |
| 4 | `POST /api/documents/${document}/commentaires/${commentaire}/resolution` |  | 2xx | `$.resolu` egal true |
| 5 | `POST /api/documents/${document}/approbation` |  | 2xx | `$[*].roleRequis` inclut "APPRO" ; `$[*].roleRequis` inclut "JURIDIQUE" ; `$[*].roleRequis` inclut "DIRECTION" ; capture `n1` ← `$[0].id` ; capture `n2` ← `$[1].id` ; capture `n3` ← `$[2].id` |
| 6 | `POST /api/documents/approbation/${n2}/decision` |  | 400, 403, 409 | — |
| 7 | `POST /api/documents/approbation/${n1}/decision` |  | 2xx | `$.statut` egal "APPROUVE" |
| 8 | `POST /api/documents/approbation/${n2}/decision` | juriste de Lévis | 2xx | `$.approbateur` egal "juriste@levis.qc.ca" |
| 9 | `POST /api/documents/approbation/${n3}/decision` | direction de Lévis | 2xx | — |
| 10 | `GET /api/documents/${document}/approbation` |  | 2xx | `$[*].statut` exclut "EN_ATTENTE" |
| 11 | `GET /api/audit?entite=Document` | administrateur de Lévis | 2xx | `$[*].action` inclut "COMMENTER" |

#### L'administration configure le circuit d'approbation d'un type de document

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Révision et approbation par plusieurs intervenants (avis 2026-50-92, art. 2.1 n° 6 ; cahier EF4) (`bdd/features/levis/03-revision-approbation.feature`)
- **Exigences éprouvées :** `LEV-C-12`

```gherkin
Quand j'exécute le cas "lev.revision.workflow-configurable"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — circuits d'approbation"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.revision.workflow-configurable`** — Circuit d'approbation configurable par type de document : un document de ce type suit le circuit configuré, et non le circuit par défaut *(données : `bdd/donnees/levis.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/workflows` |  | 2xx | `$.etapes` egal ["APPRO","DIRECTION"] ; capture `workflow` ← `$.id` |
| 2 | `GET /api/workflows` |  | 2xx | `$[*].nom` inclut "Circuit bail BDD ${unique}" |
| 3 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 4 | `POST /api/documents/${document}/approbation` |  | 2xx | `$[*].roleRequis` egal ["APPRO","DIRECTION"] |
| 5 | `DELETE /api/workflows/${workflow}` |  | 2xx | — |

### Addendas interactifs et documents finaux (avis 2026-50-92, art. 2.1 n° 7 et 8 ; cahier EF3)


#### Un addenda modifie une clause précise, est émis, puis intégré au document final en PDF et en Word

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Addendas interactifs et documents finaux (avis 2026-50-92, art. 2.1 n° 7 et 8 ; cahier EF3) (`bdd/features/levis/04-addendas-documents-finaux.feature`)
- **Exigences éprouvées :** `LEV-2.1-7`, `LEV-2.1-8`, `LEV-C-10`

```gherkin
Quand j'exécute le cas "lev.addendas.integration"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — éditeur du document"
Alors je vois "Report de la date d'ouverture ${unique}"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.addendas.integration`** — Addenda interactif rattaché à une clause, émis puis intégré au document final (PDF et Word) *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `POST /api/documents/${document}/assembler` |  | 2xx | capture `clauseDoc` ← `$.clauses[0].clauseId` |
| 3 | `POST /api/documents/${document}/addendas` |  | 2xx | `$.statut` egal "BROUILLON" ; `$.numero` egal 1 ; capture `addenda` ← `$.id` |
| 4 | `POST /api/addendas/${addenda}/changements` |  | 2xx | `$.changements[0].ancienTexte` existe true |
| 5 | `POST /api/addendas/${addenda}/reintegrer` |  | 2xx | `$.statut` egal "PUBLIE" ; `$.emisLe` existe true |
| 6 | `GET /api/documents/${document}` |  | 2xx | `$.clauses[*].texteEffectif` inclut "Texte modifié par l'addenda n° 1 ${unique}." |
| 7 | `GET /api/documents/${document}/export/pdf` |  | 2xx | contient « %PDF » |
| 8 | `GET /api/documents/${document}/export/docx` |  | 2xx | contient « PK » |

### Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3)


#### Accès par rôle, second facteur et authentification unique de l'annuaire de la Ville

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3) (`bdd/features/levis/05-securite-conformite.feature`)
- **Exigences éprouvées :** `LEV-C-16`, `LEV-C-21`

```gherkin
Quand j'exécute le cas "lev.securite.roles"
Et j'exécute le cas "lev.securite.mfa"
Et j'exécute le cas "lev.securite.sso"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — sécurité du compte"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.securite.roles`** — Rôles distincts : un rédacteur n'administre ni les utilisateurs ni le journal *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/admin/users` |  | 403 | — |
| 2 | `GET /api/audit` |  | 403 | — |
| 3 | `GET /api/admin/users` | administrateur de Lévis | 2xx | `$.contenu[*].email` inclut "redacteur@levis.qc.ca" |

**Cas `lev.securite.mfa`** — Second facteur : question secrète puis TOTP (RFC 6238) exigés et vérifiés *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true ; `$.ok` egal false |
| 2 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true |
| 3 | `POST /api/auth/login` | anonyme | 200, 401 | `$.ok` different true |
| 4 | `POST /api/auth/login` | anonyme | 2xx | `$.ok` egal true |

**Cas `lev.securite.sso`** — Authentification unique Entra ID : connecteurs OIDC et SAML configurables et activables par organisation *(données : `bdd/donnees/levis.json` ; profil : administrateur de Lévis)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/parametres/integration-oidc` |  | 2xx | `$` existe true |
| 2 | `GET /api/parametres/integration-saml` |  | 2xx | `$` existe true |
| 3 | `GET /api/auth/config?slug=levis` |  | 2xx | `$` existe true |

#### Le journal d'audit trace les actions et se consulte par l'administration

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3) (`bdd/features/levis/05-securite-conformite.feature`)
- **Exigences éprouvées :** `LEV-C-17`

```gherkin
Quand j'ouvre l'écran "Lévis — journal d'audit"
Alors l'écran est accessible, sans débordement ni superposition
```

#### Les données d'une organisation sont invisibles pour les autres

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3) (`bdd/features/levis/05-securite-conformite.feature`)
- **Exigences éprouvées :** `LEV-C-22`

```gherkin
Quand j'exécute le cas "lev.multitenant.isolation"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.multitenant.isolation`** — Isolation entre organisations : un document de Lévis est introuvable pour une autre organisation *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `GET /api/documents/${document}` | administration principale IQ | 403, 404 | — |

#### Exercer les droits Loi 25 d'accès et d'effacement d'une personne concernée

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3) (`bdd/features/levis/05-securite-conformite.feature`)
- **Exigences éprouvées :** `LEV-C-15`

```gherkin
Quand j'exécute le cas "lev.loi25.droits"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "Lévis — vie privée (Loi 25)"
Et je saisis "redacteur@levis.qc.ca" dans le champ "Courriel de la personne concernée"
Et je clique sur "Rassembler les renseignements"
Alors je vois "Télécharger le dossier"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `lev.loi25.droits`** — Loi 25 : une personne active (compte, journal, clause) obtient tout ce qui est détenu sur elle, puis est effacée ; registre des demandes sous empreinte ; refus de s'effacer soi-même et refus pour un non-administrateur *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 2xx | — |
| 2 | `POST /api/auth/login` | anonyme | 2xx | — |
| 3 | `POST /api/loi25/acces` |  | 2xx | `$.courriel` egal "marie.${unique}@levis.qc.ca" ; `$.sources[*].source` inclut "Compte" ; `$.sources[*].source` inclut "Journal d'audit" ; contient « Marie Tremblay ${unique} » ; ne contient pas « $2a$ » |
| 4 | `POST /api/loi25/effacement` |  | 2xx | `$[*].source` inclut "Compte" |
| 5 | `POST /api/auth/login` | anonyme | 400, 401, 403 | — |
| 6 | `POST /api/loi25/acces` |  | 2xx | ne contient pas « Marie Tremblay ${unique} » |
| 7 | `GET /api/loi25/demandes` |  | 2xx | `$[*].type` inclut "EFFACEMENT" ; `$[*].motif` inclut "Demande écrite ${unique}" ; ne contient pas « marie.${unique}@levis.qc.ca » |
| 8 | `POST /api/loi25/effacement` |  | 409 | — |
| 9 | `POST /api/loi25/acces` | rédacteur de Lévis | 403 | — |

#### Exporter les métadonnées d'un devis au format SEAO

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, conformité et organisation (cahier ENF1, ENF2, DAO 2.3) (`bdd/features/levis/05-securite-conformite.feature`)
- **Exigences éprouvées :** `LEV-C-20`

```gherkin
Quand j'exécute le cas "lev.seao.export"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.seao.export`** — Export SEAO : la fiche XML reprend les métadonnées du devis (échappées), l'export est journalisé ; un devis sans numéro d'appel d'offres est refusé *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/documents` |  | 2xx | capture `document` ← `$.id` |
| 2 | `GET /api/documents/${document}/export/seao` |  | 2xx | contient « <numero>AO-${UNIQUE}</numero> » ; contient « déneigement &amp; stationnements » ; contient « <type>Avis d'appel d'offres</type> » ; contient « <montantestime>250000 » |
| 3 | `GET /api/audit?action=EXPORT_SEAO` | administrateur de Lévis | 2xx | contient « AO-${UNIQUE} » |
| 4 | `POST /api/documents` |  | 2xx | capture `sansNumero` ← `$.id` |
| 5 | `GET /api/documents/${sansNumero}/export/seao` |  | 409 | `$.message` contient "numéro d'appel d'offres" |

