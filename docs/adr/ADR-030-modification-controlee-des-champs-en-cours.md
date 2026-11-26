# ADR-030 — Modification contrôlée des champs en cours de traitement et journal avant/après

- **Statut** : Accepté
- **Date** : 2026-11-25
- **Portée** :
  - domaine `clm.domain.ModificationEnCours` (règle par champ, verdict, effet) ;
  - `ModeleContrat.modificationsJson` (changelog 077), `ModeleService` (enregistrement, nouvelle version, validation) ;
  - `TrousseService.saisirValeurs`, `ParcoursService.reprendreEtape` ;
  - `JournalClm.consignerConfidentiel`, `AuditService.logConfidentiel`.
- **Origine** : Annexe 10 d'IQ, AF-34 et SEC-15 (Essentielles) ; story RST-43.

## Contexte

- **Tout restait modifiable** : le gestionnaire pouvait corriger n'importe quelle valeur d'une trousse lancée, sans effet
  sur les approbations déjà obtenues (sauf régénération du document, qui ouvre un nouveau cycle).
- **Un journal muet** : la saisie était consignée « VALEURS_SAISIES n valeur(s) », sans le champ ni les valeurs.
- **Loi 25** : les valeurs d'une trousse sont des renseignements d'affaires, parfois personnels. Le journal technique
  (Logback) ne doit jamais les recevoir (docs/journalisation.md) ; le journal d'audit, lui, est la preuve.

## Options envisagées

1. **Règles portées par le modèle** : par champ, « modifiable en cours » (présence d'une règle), étapes permises, rôle
   requis, effet sur les approbations (aucun, reprise de l'étape, nouveau cycle). Versionnées avec le modèle.
2. **Règles portées par la trousse** : chaque initiateur décide ; contraire au contrôle demandé (l'autorité est fixée par
   l'organisation, pas par celui qui modifie).
3. **Un attribut ajouté à `Modele.Champ`** : touche le contrat d'un record utilisé partout (génération, Salesforce, MCP,
   démonstration) pour une préoccupation propre au parcours.

## Décision

Option 1, dans une liste distincte des champs (`modificationsEnCours` du modèle).

- **Refus par défaut** : une fois le parcours lancé, un champ sans règle n'est plus modifiable (409, motif nommant le
  champ). En brouillon, rien ne change.
- **Étapes et rôle** : la modification n'est permise que si une des étapes listées est active (liste vide : toutes) et
  si l'auteur porte le rôle requis (vide : quiconque gère la trousse).
- **Effet** : le plus lourd des champs modifiés s'applique. `REPRISE_ETAPE` invalide les décisions de la phase en cours et
  des suivantes ; `NOUVEAU_CYCLE` repart de la première phase (mécanisme existant). L'effet par défaut d'une règle sans
  effet précisé est `NOUVEAU_CYCLE`, le plus prudent.
- **Journal** : un événement `VALEUR_MODIFIEE` par champ effectivement changé, nommant le libellé, la clé, l'ancienne et la
  nouvelle valeur, l'auteur, les étapes actives et l'effet ; scellé dans la chaîne de la trousse et inscrit au journal
  d'audit de l'organisation par `consignerConfidentiel`, qui **n'écrit pas le détail** dans le journal technique.
- **Validation du modèle** : une règle visant un champ inconnu, définie deux fois ou citant une étape absente du parcours
  du modèle empêche la publication.

## Conséquences

- **Positives** : AF-34 et SEC-15 couverts ; l'organisation fixe qui modifie quoi, quand, et ce qu'il en coûte au
  parcours ; le journal devient une preuve lisible du changement.
- **Négatives** :
  - une trousse lancée depuis un modèle sans règle a désormais toutes ses valeurs figées : les organisations qui
    corrigeaient librement doivent déclarer leurs règles (changement de comportement assumé : refus par défaut) ;
  - les valeurs avant/après sont visibles de qui consulte l'historique de la trousse ou le journal d'audit.
- **Vérification** :
  - `ModificationEnCoursTest` : normalisation, validation, refus, effet retenu ;
  - `AuditServiceConfidentielTest` : le détail reste hors du journal technique ;
  - `AnnexeDixIntegrationTest` (RST-43) : refus d'un champ figé, d'une étape ou d'un rôle, reprise d'étape, nouveau
    cycle, historique avant/après ;
  - scénario e2e `E2E-AX-IQ-02`.
