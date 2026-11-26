# ADR-038 — Négociation assistée avec un tiers, clause par clause, par le portail externe

- **Statut** : Accepté
- **Date** : 2026-11-26
- **Portée** :
  - domaine : `Negociation` (cycle, contrepartie, lien), `EtatNegociation`, `VersionNegociation` (clauses figées et
    empreinte), `PropositionNegociation` (proposition, arbitrage humain, suggestion), `ClauseNegociee`, `TourNegociation`,
    `DiffTexte`, `ComparaisonClauses`, `RecommandationNegociation` (repli déterministe), `PositionNegociation`
    (changelog 084) ;
  - application : `OuvertureNegociationService`, `NegociationService`, `AssistanceNegociationService`,
    `PortailNegociationService`, `RegistreNegociation`, `VuesNegociation`, `ClausesDuModele`, port
    `ConseillerNegociation` ; extensions `ConditionLancement` (`GardeLancementNegociation`) et `PieceDePreuve`
    (`PreuveNegociation`) ; `DocumentClmService.deposerVersionNegociee` ;
  - infrastructure : `ConseillerNegociationOllama`, `NegociationController`, `PortailNegociationController` ;
  - écrans : onglet « Négociation » de la trousse (`/clm/trousses/{id}?onglet=negociation`) et portail externe
    `/portail/negociation/{jeton}` ;
  - module activable `clm_negociation` (option tarifée à part, ligne de bordereau CLM-11).
- **Origine** : Annexe 10 d'IQ, IA-05 (Optionnelle) : « La négociation automatisée avec un tiers et les autres fonctions
  complètes de CLM sont optionnelles et doivent être décrites et tarifées séparément » ; story RST-55. S'appuie sur
  ADR-008 (règles d'architecture CLM) et ADR-037 (clé par organisation) ; prolonge l'IA encadrée du CLM (option `clm_ia`,
  ST-15 : une analyse n'a d'effet qu'après validation humaine justifiée).

## Contexte

- Le CLM couvrait déjà la rédaction depuis les modèles (clauses conditionnelles de la bibliothèque), le parcours de
  validation et de signature, le portail externe à jeton et l'analyse IA encadrée (option `clm_ia`). Il manquait l'échange
  structuré avec la contrepartie avant signature : propositions, réponses, versions et traces.
- IA-05 parle de négociation **automatisée**. Un organisme public ne peut pas laisser une machine accepter une clause
  contractuelle : la Loi 25 (décision fondée exclusivement sur un traitement automatisé), la directive sur l'IA du
  gouvernement du Québec et le principe de l'IA encadrée du CLM (ST-15) imposent une décision humaine, une explication et
  une trace.
- Contraintes : aucun compte pour la contrepartie ; jeton jamais conservé en clair ; aucune donnée ne quitte
  l'infrastructure pour l'IA ; repli si l'IA est en panne ; journal scellé et dossier de preuve ; domaine sans cadriciel.

## Options envisagées

1. **Négociation sur documents entiers** (la contrepartie téléverse une version Word annotée). Simple, proche des usages
   courriel ; mais aucune comparaison fiable entre formats, aucune décision traçable par clause, et l'IA devrait relire des
   documents arbitraires.
2. **Négociation clause par clause sur les clauses normatives du modèle** (retenue). Les clauses de la bibliothèque du
   modèle, fusionnées avec les valeurs de la trousse, forment la version 1 ; chaque proposition vise une clause ; chaque
   tour produit une version figée et comparable mot à mot.
3. **Nouvelle nature de participation `NEGOCIER` sur l'invitation de signature.** Réutilise le jeton, mais l'invitation
   est liée à une étape de parcours lancé, alors que la négociation précède le lancement ; elle mêlerait deux cycles de
   vie (signature et négociation) dans le même agrégat.
4. **Négociation réellement automatisée** (l'IA accepte sous un seuil de similarité). Écartée : incompatible avec la
   décision humaine exigée ; l'IA ne fait que suggérer.

## Décision

Option 2, objet dédié, exposé par le portail externe existant, IA en suggestion seulement.

- **Cycle** (`EtatNegociation`) : `ATTENTE_CONTREPARTIE` → (`ATTENTE_INTERNE` ↔ `ATTENTE_CONTREPARTIE`)* → `ENTENTE` →
  `CLOSE` ; `ANNULEE` depuis tout état non final. Une seule négociation ouverte par trousse (`Negociation.DejaEnCours`).
  Ouverture seulement sur une trousse au brouillon : le parcours de validation et de signature attend la clôture
  (`GardeLancementNegociation`, branché au lancement par la liste `ConditionLancement`, sans modifier ses règles).
- **Tours et versions** : la contrepartie propose un texte (et un commentaire) par clause, une proposition par clause et
  par tour ; une proposition qui ne change rien est refusée. L'organisation décide **toutes** les propositions du tour en
  une fois : acceptée, refusée (motif obligatoire) ou contre-proposée (texte obligatoire). Le tour produit la version
  n + 1 (`TourNegociation.appliquer`), figée en JSON avec son empreinte SHA-256, elle-même scellée au journal de la
  trousse. Tout accepté tel quel : entente ; sinon, la main revient à la contrepartie, qui propose encore ou accepte la
  version courante.
- **Comparaison** : `DiffTexte` (plus longue sous-suite commune mot à mot, bornée à 2 000 mots par texte, au-delà en bloc)
  et `ComparaisonClauses` (inchangée, modifiée, ajoutée, retirée), affichées avec `<ins>`/`<del>` et un libellé masqué :
  jamais par la seule couleur (WCAG 1.4.1).
- **Clôture** : sur entente seulement ; le contrat est réassemblé (corps du modèle + clauses négociées) et versé comme
  nouvelle version du document `contrat-genere` (`-negocie.pdf`), l'ancienne étant « remplacée » ; le parcours reprend
  donc sur la version négociée. Le lien de la contrepartie devient inopérant (`NegociationFermee`).
- **Assistance (IA encadrée)** — port `ConseillerNegociation`, adaptateur `ConseillerNegociationOllama` sur l'IA locale de
  la plateforme (`AssistantRedactionPort`, Ollama), soumise à l'interrupteur IA de l'organisation (art. 21). Pour chaque
  proposition en attente : résumé des écarts et position recommandée (accepter, contre-proposer, refuser) **au regard de
  la clause normative** (texte de la version 1). Repli déterministe `RecommandationNegociation` si l'IA est désactivée,
  injoignable ou muette : refuser si la proposition retire un élément attendu (liste « Clause attendue » de
  l'organisation, si l'option IA d'analyse est souscrite) ; accepter si ≥ 85 % de mots communs ; sinon contre-proposer.
  La source (« IA locale (modèle) » ou « Règles de la bibliothèque ») est conservée et affichée ; la suggestion n'est
  jamais appliquée — seule `PropositionNegociation.decider`, appelée par une personne, change la décision.
- **Accès externe** : `/api/clm/portail/negociation/{jeton}` (sous la règle `permitAll` existante du portail) ; jeton
  aléatoire (`SourceAleatoire`), seule son empreinte est conservée (`findByJetonHash`, exception motivée d'ADR-008) ;
  expiration selon la validité des invitations de l'organisation ; l'option `clm_negociation` est vérifiée pour
  l'organisation du jeton. La contrepartie ne voit ni les suggestions ni l'identité de qui a décidé.
- **Traçabilité** : chaque action est au journal scellé (`NEGOCIATION_OUVERTE`, `_INVITATION`, `_VERSION`,
  `_PROPOSITION`, `_SUGGESTION`, `_DECISION`, `_REPONSE`, `_ENTENTE`, `_ACCEPTEE`, `_CLOSE`, `_ANNULEE`) ; le dossier de
  preuve exporté reçoit `negociations/negociation-{id}.json` (versions, propositions, suggestions, décisions), couvert
  par le manifeste et le sceau de l'archive (extension `PieceDePreuve`).

## Conséquences

- **Positives** : IA-05 couverte et démontrable (intégration, e2e E2E-AX-IQ-30/31) ; option activable par organisation,
  donc tarifée à part (CLM-11) ; aucun changement de comportement pour une trousse sans négociation ; la décision reste
  humaine et motivée ; la preuve couvre la genèse du texte signé.
- **Négatives / limites** :
  - la contrepartie s'identifie par la seule possession du lien (niveau « courriel ») ; un second facteur (code par
    courriel, comme le portail de signature) est une extension directe si IQ l'exige ;
  - pas de relance automatique de la contrepartie ni d'avis courriel à l'initiateur à chaque tour (le journal et l'onglet
    font foi) ;
  - le texte des clauses négociées est stocké en clair, comme la bibliothèque de clauses du modèle dont il dérive ; le
    chiffrement par la clé de l'organisation (ADR-037) ne couvre que le contenu des documents, dont le contrat négocié
    versé à la clôture ;
  - la négociation porte sur les clauses de la bibliothèque du modèle : une trousse sans modèle n'est pas négociable par
    ce moyen ; ajouter ou retirer une clause se fait par une contre-proposition de texte, pas par la structure.
