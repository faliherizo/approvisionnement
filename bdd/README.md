# Tests BDD des exigences fonctionnelles — tous les appels d'offres

Ce répertoire décrit, en Gherkin français, le comportement que chaque client attend de la plateforme. Il l'éprouve
ensuite de bout en bout, sur la plateforme réelle (serveur, interface et applications externes des bacs à sable).

| Appel d'offres | Catalogue d'exigences | Fonctionnalités | Documentation générée |
|---|---|---|---|
| Ville de Lévis — avis d'appel d'intérêt 2026-50-92 | [`exigences/levis.json`](exigences/levis.json) | [`features/levis/`](features/levis/) | [`docs/scenarios-bdd/levis.md`](../docs/scenarios-bdd/levis.md) |
| Ville de Trois-Rivières — AO 7000017905 | [`exigences/trois-rivieres.json`](exigences/trois-rivieres.json) | [`features/trois-rivieres/`](features/trois-rivieres/) | [`docs/scenarios-bdd/trois-rivieres.md`](../docs/scenarios-bdd/trois-rivieres.md) |
| Investissement Québec — AO 2026-6370-0005 | [`exigences/iq.json`](exigences/iq.json) | [`features/iq/`](features/iq/) | [`docs/scenarios-bdd/iq.md`](../docs/scenarios-bdd/iq.md) |
| Santé Québec — CHU, avis d'intention | [`exigences/chu.json`](exigences/chu.json) | [`features/chu/`](features/chu/) | [`docs/scenarios-bdd/chu.md`](../docs/scenarios-bdd/chu.md) |

La page d'accueil de la documentation est [`docs/scenarios-bdd/README.md`](../docs/scenarios-bdd/README.md). Elle
contient, pour chaque appel d'offres, la matrice exigence → scénario, le texte Gherkin, les tableaux de cas et le
résultat de la dernière exécution.

## 1. Démarche

1. **Exigence.** Chaque exigence du client est recopiée dans un catalogue (`exigences/*.json`) avec sa source, son
   caractère (obligatoire, souhaitable…), la conformité que nous avons déclarée, et son mode de **vérification** :

   | `verification` | Sens | Ce que l'outil exige |
   |---|---|---|
   | `bdd` | éprouvée par au moins un scénario exécuté | un scénario étiqueté `@<id>` |
   | `documentaire` | ne relève pas d'un test : service, prix, référence, infrastructure | `justification` (et `reference` vers la pièce) |
   | `ecart` | écart connu et assumé | un scénario `@<id> @ecart @fixme`, conservé en attente |
   | `sans-objet` | ne s'applique pas à la solution proposée | `justification` |

2. **Scénario.** Une fonctionnalité (`features/<ao>/*.feature`) regroupe les scénarios d'un même domaine. Chaque
   scénario porte en étiquettes les identifiants des exigences qu'il éprouve (`@LEV-2.1-1`, `@TR-…`, `@IQ-…`, `@CHU-…`).
   Les étapes sont écrites dans la langue de l'analyste : qui agit, ce qu'il fait, ce que la plateforme doit rendre.

3. **Cas exécutable.** Les détails (appels, profils, corps, statuts, valeurs attendues) ne sont pas dans le Gherkin.
   Ils sont décrits en JSON dans `donnees/*.json` (section 4), sous un nom stable que le scénario cite :
   `Quand j'exécute le cas "chu.saisie.regles"`. Le même cas sert au test et à la documentation.

4. **Écran.** Quand l'exigence touche l'interface, le scénario ouvre l'écran et vérifie l'accessibilité selon
   SGQRI 008 3.0 / WCAG 2.2 AA (axe-core, aucune violation). Il contrôle aussi que les cibles mesurent au moins
   24 px, et qu'il n'y a ni débordement horizontal ni superposition.

5. **Contrôle de couverture.** `npm run couverture` échoue dans trois cas : une exigence `bdd` ou `ecart` n'a aucun
   scénario, une étiquette ne correspond à aucune exigence, ou une exigence `documentaire`/`sans-objet` n'a pas de
   justification.

## 2. Arborescence

```
bdd/
├── exigences/        catalogues d'exigences, un par appel d'offres (source de vérité de la matrice)
├── features/         scénarios Gherkin (# language: fr), un dossier par appel d'offres
├── donnees/          cas exécutables et données de test en JSON
│   ├── comptes.json      profils (courriel, mot de passe de démonstration, secret TOTP) et organisations d'essai
│   ├── levis.json, trois-rivieres.json, iq.json, chu.json
│   └── fichiers/         pièces jointes de test (gabarit Word IQ…)
├── etapes/           définitions d'étapes (fixtures.ts, communes.ts)
├── outils/
│   ├── cas.ts            moteur d'exécution des cas JSON
│   ├── donnees.ts        chargement et contrôle d'unicité des cas, écrans et profils
│   └── documenter.mjs    génération de docs/scenarios-bdd/ et contrôle de couverture
└── playwright.config.ts
```

## 3. Exécuter

### Prérequis

- Les bacs à sable démarrés : `docker compose -f infra/bacs-a-sable/docker-compose.yml up -d`. Ils fournissent l'ICP,
  l'horodatage, pyHanko, Gotenberg, veraPDF, Mailpit et ClamAV (voir
  [`guide_bacs_a_sable.md`](../docs/Dossier_IQ_2026-6370-0005/12-Bacs-a-sable/guide_bacs_a_sable.md)).
- Le serveur sur le port 8100, branché sur les bacs à sable avec les mêmes variables qu'en production :

  ```bash
  B=infra/bacs-a-sable
  export SERVER_PORT=8100 \
    CLM_CACHET_MAGASIN=$PWD/$B/icp/etat/cachet.p12 CLM_CACHET_MOT_DE_PASSE="$(cat $B/icp/etat/mot-de-passe.txt)" \
    CLM_CACHET_TSA_URL=http://localhost:18090/tsa CLM_CONVERSION_MODE=service CLM_CONVERSION_URL=http://localhost:18030 \
    CLM_SIGNATURE_FOURNISSEUR=cachet NOTIFICATION_COURRIEL_MODE=smtp MAIL_HOST=localhost MAIL_PORT=11025 \
    ANTIVIRUS_MODE=clamav ANTIVIRUS_CLAMAV_HOTE=localhost ANTIVIRUS_CLAMAV_PORT=13310
  (cd backend && mvn -q -DskipTests package && java -jar target/contrat-backend-*.jar)
  ```

- Le frontend de recette sur le port 5175 : `cd frontend && npm run dev -- --config vite.tour.config.js`.

La base H2 est en mémoire : un redémarrage du serveur réamorce les comptes et les organisations de démonstration. Les
cas ne dépendent pas de l'ordre d'exécution. Ils créent leurs propres objets (variable `${unique}`) et l'organisation
d'essai CHU (`chu-bdd`) est créée si elle n'existe pas. Aucune autre organisation n'est modifiée.

### Commandes

| Commande | Effet |
|---|---|
| `npm ci && npx playwright install chromium` | installation (une fois) |
| `npm test` | génère les specs, exécute tous les scénarios, régénère `docs/scenarios-bdd/` |
| `npx bddgen && npx playwright test --grep @AO-CHU-AVIS-INTENTION` | un seul appel d'offres (étiquette de la fonctionnalité) |
| `npx bddgen && npx playwright test --grep @IQ-C-63` | les scénarios d'une exigence |
| `npm run documenter` | régénère la documentation à partir des catalogues et du dernier résultat |
| `npm run couverture` | contrôle de couverture (sortie non nulle si une exigence n'a pas de scénario) |
| `npm run rapport` | ouvre le rapport HTML Playwright |

Variables : `BDD_FRONTEND_URL` (défaut `http://localhost:5175`, qui relaie `/api` vers le serveur) et
`BDD_COURRIEL_URL` (API de Mailpit, défaut `http://localhost:18025/api/v1`).

### Intégration continue

`infra/bacs-a-sable/ci.sh` exécute cette suite après les scénarios e2e BS-01 à BS-08 et les scripts de validation, sur
le même serveur et les mêmes bacs à sable. Le contrôle de couverture passe en premier. La documentation est régénérée
même en cas d'échec, puis le verdict est rendu. GitHub Actions (`reusable-bacs-a-sable.yml`) et Azure DevOps
(`templates/bacs-a-sable.yml`) publient le rapport Playwright et `docs/scenarios-bdd/` en artefacts.

## 4. Cas exécutables (JSON)

Un fichier de données contient deux sections : `cas` et `ecrans`.

```json
{
  "cas": {
    "chu.processus": {
      "description": "Circuit d'approbation multi-niveaux par règles (montant), décision par étape",
      "profil": "administrateur CHU",
      "etapes": [
        { "appel": "POST /api/approbations/demandes",
          "corps": { "documentType": "ACHAT_${UNIQUE}", "contexte": { "montant": "5000" } },
          "capturer": { "demande": "$.id" },
          "verifier": [ { "chemin": "$.etapes", "longueurMin": 1 } ] },
        { "appel": "GET /api/approbations/demandes/${demande}",
          "verifier": [ { "chemin": "$.statut", "egal": "APPROVED" } ] }
      ]
    }
  },
  "ecrans": {
    "CHU — formulaires": { "chemin": "/formulaires", "titre": "Formulaires", "largeur": 390 }
  }
}
```

### Champs d'une étape

| Champ | Rôle |
|---|---|
| `appel` | `"MÉTHODE /chemin"` ; les variables `${…}` sont substituées |
| `profil` | profil qui agit pour cette étape (prime sur celui du cas, qui prime sur la personne connectée) |
| `anonyme` | `true` : appel sans session, pour ne pas écraser la session du profil (connexion MFA, portail public) |
| `corps` | corps JSON |
| `formulaire` | corps `application/x-www-form-urlencoded` (jeton OAuth…) |
| `multipart` | champs de formulaire multipart ; un fichier s'écrit `pdf`, `pdf:N` (PDF de N Mo), `texte:…` ou `chemin:fichiers/…` |
| `entetes` | en-têtes supplémentaires |
| `statut` | statut attendu, ou liste de statuts admis (défaut : 200, 201 ou 204) |
| `capturer` | `{ "variable": "$.chemin" }` : valeurs réutilisées dans les étapes suivantes |
| `verifier` | liste de vérifications (ci-dessous) |
| `courriel` | lit le dernier courriel reçu par Mailpit : `{ destinataire, motif, capturer, different }` |
| `note` | commentaire repris dans la documentation |

### Vérifications

`chemin` désigne une valeur de la réponse : `$.a.b`, `$.liste[0].x`, `$[*].x` (toutes les valeurs), ou
`$[?slug=chu-bdd].id` (premier élément dont le champ vaut la valeur). Les opérateurs sont les suivants :
`egal`, `egalTexte`, `different`, `contient` (sous-chaîne), `inclut` / `exclut` (élément d'une liste), `existe`,
`longueurMin`, `texte` / `texteAbsent` (sur le corps brut, PDF ou CSV compris).

### Variables

| Variable | Valeur |
|---|---|
| `${unique}` / `${UNIQUE}` | suffixe propre à l'exécution, en minuscules / majuscules |
| `${aujourdhui}`, `${demain}`, `${dansUnMois}` | dates ISO |
| `${totp:SECRET}` | code TOTP RFC 6238 courant pour ce secret |
| `${nom}` | toute valeur capturée par une étape précédente du scénario |

Un écart relevé par une vérification n'interrompt pas le cas : tous les écarts sont rassemblés et rapportés ensemble
par l'étape `Alors la plateforme se comporte comme attendu`. Un statut inattendu interrompt le cas, avec le corps de la
réponse dans le message.

## 5. Étapes disponibles

| Étape | Effet |
|---|---|
| `Étant donné que je suis connecté en tant que "<profil>"` | connexion par l'interface |
| `Étant donné que j'agis en tant que "<profil>"` | profil des cas, sans connexion à l'interface |
| `Étant donné que l'organisation "<nom>" est prête` | crée l'organisation d'essai et active ses modules (idempotent) |
| `Étant donné que j'utilise un téléphone` | fenêtre de 390 px |
| `Quand j'exécute le cas "<nom>"` | exécute un cas de `donnees/*.json` |
| `Alors la plateforme se comporte comme attendu` | aucun écart relevé par les vérifications |
| `Quand j'ouvre l'écran "<nom>"` | ouvre un écran déclaré et attend son titre |
| `Quand je saisis "<valeur>" dans le champ "<libellé>"`, `Quand je clique sur "<nom>"`, `Quand je choisis la langue "<langue>"` | interactions |
| `Alors je vois "<texte>"`, `… le bouton "<nom>"`, `… le lien "<nom>"`, `Alors je ne vois pas le lien "<nom>"` | assertions d'interface |
| `Alors l'écran est accessible, sans débordement ni superposition` | axe-core WCAG 2.2 AA, cibles ≥ 24 px, aucun débordement ni chevauchement |

## 6. Ajouter une exigence

1. Ajouter l'exigence au catalogue de l'appel d'offres, avec `verification`.
2. Écrire ou compléter un scénario étiqueté `@<id>` dans la fonctionnalité du domaine.
3. Décrire le cas dans `donnees/<ao>.json`. Le nom doit être unique : le chargement refuse un doublon.
4. `npm run couverture`, puis `npm test`. La documentation est régénérée ; on la livre dans le même changement.

Un écart se déclare avec `"verification": "ecart"` et un scénario `@ecart @fixme`, accompagné d'un commentaire qui
dit ce qui manque. Le scénario reste en attente jusqu'à ce que la fonction existe : on retire alors `@fixme`, on passe
l'exigence à `bdd`, et le scénario devient bloquant.

## 7. Écarts déclarés

Aucun. Les dix écarts déclarés jusqu'ici sont comblés et éprouvés par leurs scénarios :

| Appel d'offres | Exigence | Fonction livrée |
|---|---|---|
| Lévis | C-03 | historique consultable de chaque clause (version, auteur, date, motif) et restauration |
| Lévis | C-15 | droits Loi 25 : accès structuré, effacement par pseudonymisation, registre des demandes |
| Lévis | C-20 | fiche des métadonnées SEAO d'un devis (XML), export journalisé |
| CHU | 3.06 | places par choix ; un choix complet n'est plus proposé ni accepté |
| CHU | 4.08 | inventaire des inscriptions par choix, export CSV |
| CHU | 5.04 | document PDF/A d'une fiche, transmis par courriel en pièce jointe |
| CHU | 5.05 | messagerie interne sécurisée (participants seuls, chiffrée au repos) |
| CHU | 6.03 | rapport PDF/A personnalisé (questions et statuts choisis) |
| CHU | 6.04 | tableaux de bord configurables (compteurs, histogrammes, évolutions), partageables |
| CHU | 9.05 | banque d'heures de services professionnels (achats, consommations, solde, relevé) |

## 8. Scénarios e2e d'interface par exigence

`bdd/e2e/` démontre chaque exigence fonctionnelle depuis l'interface : connexion du profil, gestes à l'écran, résultat
constaté, contrôle d'accessibilité et de mise en page aux quatre largeurs. Les scénarios et leurs données sont en JSON
(`bdd/e2e/donnees/<appel d'offres>.json`) ; le moteur `bdd/e2e/exigences.spec.ts` ne contient aucune donnée.
`npm run e2e` les exécute et régénère [`docs/scenarios-e2e/`](../docs/scenarios-e2e/README.md) ;
`npm run e2e:couverture` échoue si une exigence fonctionnelle n'a aucun scénario e2e.

**Couverture des fonctionnalités, écran par écran.** Au-delà des exigences, chaque écran (route de `App.jsx`) et chacune
de ses fonctionnalités — lister, consulter, ajouter, modifier, supprimer, télécharger, transférer, attribuer, approuver,
signer, déléguer… — doivent être exercés depuis l'interface. La liste n'est pas tenue à la main : elle est **tirée du
code** du frontend.

| Outil | Rôle |
|---|---|
| `outils/inventaire-ecrans.mjs` | pour chaque route, le composant rendu, les composants qu'il importe et chaque opération d'API qu'ils déclenchent → `e2e/fonctionnalites/inventaire.json` (généré) |
| `e2e/fonctionnalites/complements.json` | adresses calculées (`api.get(base)`) déclarées, et **exclusions nominatives et motivées** — l'inventaire échoue sur un appel non déclaré |
| moteur `e2e/exigences.spec.ts` | consigne, pour chaque scénario, les écrans ouverts et les appels d'API faits depuis chacun, avec leur statut ; attend après chaque geste qu'aucun appel ne soit en cours |
| `outils/couverture-ecrans.mjs` | croise inventaire et traces → [`docs/scenarios-e2e/ecrans.md`](../docs/scenarios-e2e/ecrans.md) ; `--verifier` échoue sur un écran jamais ouvert ou une fonctionnalité non couverte |

Une fonctionnalité est couverte quand un scénario **réussi** l'a déclenchée depuis un écran qui la porte, avec une
réponse en succès ; les préparations par l'API ne comptent pas. Bilan : 71 écrans ouverts ; 544 fonctionnalités
couvertes et 6 sans objet en e2e (échéance de conservation d'un an, panne d'un système externe), chacune renvoyant au
test d'intégration qui la couvre. Les scénarios d'écran sont dans `e2e/donnees/ecrans-*.json`.

```bash
npm run e2e            # inventaire, scénarios, documentation, couverture des écrans
npm run e2e:ecrans     # porte : tous les écrans ouverts, toutes les fonctionnalités couvertes
```

## 9. Défauts révélés par ces scénarios et corrigés dans la plateforme

| Défaut | Correction |
|---|---|
| Une étape d'approbation pouvait être décidée par n'importe quel rôle, ou hors de l'ordre des niveaux | `DocumentService.decider` exige le rôle de l'étape (403) et l'approbation des niveaux précédents (409) |
| L'exclusion entre clauses dépendait de l'ordre de création | les clauses qui excluent sont traitées en premier |
| Les clauses légales amorcées n'étaient pas normatives | `normative` dans `clauses.json` (GEN-002, AOP-020) |
| Mot de passe faible (« abc ») accepté | `PolitiqueMotDePasse` : 8 caractères au moins et 3 des 4 classes, à la création comme à la modification |
| Fichier trop volumineux : erreur 500 | réponse 413 avec un message clair |
| OpenAPI : 7 DTO de même nom simple fusionnés | `springdoc.use-fqn: true` |
| Horodatage : « header parser received no bytes » | client RFC 3161 en HTTP/1.1, avec une nouvelle tentative sur coupure |
| Accessibilité : zone d'édition sans nom, cibles trop petites, contraste insuffisant (statut approuvé, commentaire résolu), champ de recherche sans libellé | `RichText` `aria-label`, cibles de 24 à 28 px, couleurs recalculées, `type=search` libellé |
| Une fenêtre modale laissait la page dessous atteignable (focus, clic) | modale rendue au niveau du document, page dessous `inert` le temps de son ouverture |
| `{{numero}}` restait vide dans les notifications | le numéro de la fiche est fourni aux gabarits |
| Débordement horizontal à 390 px : éditeur du document (cases des clauses), sécurité fine (tableaux), remplissage d'un formulaire (`<fieldset>`) | `.check-row` passe à la ligne, tableaux dans `table-wrap`, `fieldset { min-inline-size: 0 }` |
| Une structure de formulaire de l'API (`cle`, types en majuscules) s'affichait mal : tous les libellés visaient le premier champ, types ignorés | le rendu lit les deux dialectes, comme le serveur (`normaliserChamp`) |
| Recherche multicritère des fiches et critères sauvegardés offerts par l'API seulement | écran « Rechercher dans les fiches » (`RechercheFiches`) |
| `/api-docs` partait vers le serveur en développement (page d'erreur) | proxy Vite sur « /api/ » et non « /api » |
| Un formulaire supprimé restait affiché dans la liste (suppression logique non filtrée) | `FormulaireService.lister` écarte les formulaires désactivés ; l'export de réversibilité les garde |
| L'assistant IA était inutilisable depuis l'édition d'une clause : son formulaire, imbriqué, soumettait la clause | `AiAssistant` sans `<form>` (groupe nommé, bouton `type="button"`, Entrée lance la génération) |
| Génération IA sans borne : sur processeur, une réponse dépassait le délai et bloquait la file d'Ollama | `num_predict` borné (`app.ai.ollama.longueur-max`, 512 par défaut) |
| Copie dans le presse-papier refusée par le navigateur : erreur non traitée, aucun retour | réussite et échec annoncés par un message |
| Accessibilité : champs sans nom (feuille de temps, versions à comparer), boutons « × » sans nom, zones de texte enrichi nommées « Zone de texte enrichi », pavé de signature sans nom | `aria-label` tirés des libellés visibles, en trois langues |

Les failles de sécurité relevées par les tests d'intrusion sont décrites dans
[`docs/securite/rapport-tests-intrusion.md`](../docs/securite/rapport-tests-intrusion.md).
