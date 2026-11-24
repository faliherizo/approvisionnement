# Scénarios e2e d'interface, par exigence

> Document généré par `bdd/outils/documenter-e2e.mjs` depuis `bdd/exigences/*.json` (exigences),
> `bdd/e2e/donnees/*.json` (scénarios et données de test) et le dernier résultat d'exécution. Ne pas modifier à la main.

## Principe

- **Ce qui est vérifié** : chaque exigence fonctionnelle est démontrée depuis l'interface, comme un utilisateur du
  profil concerné la vivrait. Il se connecte, ouvre l'écran, saisit, choisit, active, joint un fichier, puis constate
  le résultat à l'écran.
- **Préparation par l'API** : seule la mise en place que l'exigence ne vise pas passe par l'API (ex. un devis à
  commenter). Elle est marquée « Préparer » dans les étapes.
- **Contrôle d'écran** : chaque écran visité est contrôlé selon WCAG 2.2 AA et SGQRI 008 (axe-core), avec des cibles
  ≥ 24 px, sans débordement ni superposition, aux quatre largeurs de référence.
- **Données** : les données de test sont dans les fichiers JSON, jamais dans le code du moteur
  (`bdd/e2e/exigences.spec.ts`). `${unique}` rend chaque exécution indépendante des précédentes.
- **Complément de la suite BDD** : la suite BDD (`docs/scenarios-bdd/`) vérifie les règles par l'API sur tous les
  cas limites ; les scénarios e2e vérifient le parcours d'interface.

## Vocabulaire des étapes (JSON)

| Clé | Geste | Exemple |
|---|---|---|
| `aller` | ouvrir un écran | `{ "aller": "/recherche" }` |
| `connecter` | changer d'utilisateur | `{ "connecter": "juriste de Lévis" }` |
| `langue` | changer la langue de l'interface | `{ "langue": "en" }` |
| `largeur` | afficher l'écran à une largeur donnée (téléphone : 390) | `{ "largeur": 390 }` |
| `cliquer` | activer un bouton ou un lien, par son nom accessible | `{ "cliquer": "Rechercher" }` |
| `dansLaLigne` | agir sur une ligne de tableau | `{ "dansLaLigne": { "texte": "Inscription", "cliquer": "Concevoir" } }` |
| `remplir` | saisir des champs, par leur libellé ; « Section » Libellé » limite à une section, « Libellé #2 » vise la 2e occurrence | `{ "remplir": { "Terme": "garantie" } }` |
| `choisir` | choisir dans une liste, par libellé ou par code | `{ "choisir": { "Catégorie": "OPTIONNELLE" } }` |
| `options` | constater les options d'une liste (présentes, absentes) | `{ "options": { "Unité responsable": { "contient": ["Innovation"], "exclut": ["Ancienne"] } } }` |
| `cocher` / `decocher` | case à cocher, par son libellé | `{ "cocher": "Clauses normatives seulement" }` |
| `televerser` | joindre un fichier | `{ "televerser": { "champ": "Pièce", "fichier": { "fichier": "pdf", "nom": "a.pdf" } } }` |
| `voir` / `nePasVoir` | constater un texte à l'écran | `{ "voir": ["Circuit créé"] }` |
| `valeur` | constater la valeur d'un champ (ex. calculé) | `{ "valeur": { "Total": "600" } }` |
| `voirTitre` / `url` | constater le titre ou l'adresse | `{ "url": "/documents/\\d+" }` |
| `telecharger` | télécharger un fichier et vérifier qu'il n'est pas vide | `{ "telecharger": "Exporter en CSV" }` |
| `preparer` | mise en place par l'API (cas BDD nommé ou étapes en ligne) | `{ "preparer": "lev.clauses.recherche-multicritere" }` |
| `ecranConforme` | contrôle d'accessibilité et de mise en page | `{ "ecranConforme": true }` |
| `dansLaFenetre` | activer un bouton de la fenêtre de dialogue ouverte (confirmation) | `{ "dansLaFenetre": "Supprimer" }` |
| `dansLaSection` | activer un bouton d'une section, repérée par le début de son titre | `{ "dansLaSection": { "titre": "Annexes", "cliquer": "Supprimer" } }` |
| `cliquerTexte` | activer un élément cliquable par son texte (ligne de tableau, carte) | `{ "cliquerTexte": "Dossier E2E" }` |
| `lire` / `remplirTotp` | relever un texte affiché (ex. secret TOTP), puis saisir le code calculé à partir de lui | `{ "lire": { "selecteur": "code", "variable": "secret" } }` |
| `tracer` | tracer une signature manuscrite à la souris sur le pavé de signature | `{ "tracer": true }` |
| `repondre` | réponse à une saisie native du navigateur (motif demandé par `window.prompt`) ; les confirmations sont acceptées | `{ "repondre": "Départ à la retraite" }` |

Chaque scénario consigne aussi les écrans ouverts et les appels d'API faits depuis chacun : la couverture des fonctionnalités écran par écran en découle ([ecrans.md](ecrans.md)).

## Exécution

Même plateforme que la suite BDD : serveur :8100 branché sur les bacs à sable, frontend de recette :5175 (`bdd/README.md`).

```bash
cd bdd
npm run e2e              # exécute les scénarios, puis régénère ce dossier
npm run e2e:couverture   # échoue si une exigence fonctionnelle n'a aucun scénario e2e
npx playwright test -c playwright.e2e.config.ts --grep @LEV-C-02   # les scénarios d'une exigence
```

## Couverture

| Appel d'offres | Document | Exigences fonctionnelles | Couvertes en e2e | Scénarios | Réussis |
|---|---|---:|---:|---:|---:|
| Ville de Lévis — avis d'appel d'intérêt 2026-50-92 | [levis.md](levis.md) | 34 | 34 | 25 | 25 |
| Ville de Trois-Rivières — appel d'offres 7000017905 | [trois-rivieres.md](trois-rivieres.md) | 20 | 20 | 11 | 11 |
| Investissement Québec — AO 2026-6370-0005 | [iq.md](iq.md) | 132 | 132 | 66 | 66 |
| Santé Québec — CHU, avis d'intention | [chu.md](chu.md) | 52 | 52 | 28 | 28 |
