# ADR-021 — Continuité des échanges hérités par correspondance déclarée

- **Statut** : Accepté
- **Date** : 2026-11-21
- **Portée** :
  - `interop.domain.CorrespondanceEchange`, `EchangeHerite`, `BilanReprise` ;
  - `interop.application.EchangesHeritesService` et ses ports `FormulairesEchange`, `FichesEchange` ;
  - `InteropPublicController` (`/api/public/v1/echanges/**`), `EchangesHeritesController` (`/api/interop/echanges/**`) ;
  - migration 071 ;
  - page *Intégrations (API)*.
- **Origine** : CHU-8.04 (avis d'intention CHU § 8, « maintien du fonctionnement des échanges de données, des
  automatisations et des configurations déjà en place »).
- **Étude** : [étude de conception, § 1](../etude-conception-ecarts-conditionnels.md#1-chu-804--maintien-des-échanges-déjà-en-place).

## Contexte

- **Les systèmes du CHU parlent leur propre langue.** Ils écrivent et lisent les fiches avec les noms de champs de
  la plateforme actuelle.
- **Notre API impose nos noms.** L'API publique exige les clés de champ de nos formulaires : tout système existant
  devrait être réécrit.
- **L'historique doit suivre.** Les données de la plateforme actuelle s'exportent « dans un format structuré » ; elles
  doivent être reprises et validées.

## Options envisagées

1. **Nommer nos champs comme les anciens.**
   - Lie nos formulaires à des noms techniques imposés.
   - Un renommage casse silencieusement l'échange.
2. **Script de migration hors plateforme.**
   - Ne traite que l'historique : les échanges courants doivent quand même être réécrits.
   - Le script n'est ni testé ni audité.
3. **Correspondance d'échange déclarée dans la plateforme (retenue).**
   - Elle traduit dans les deux sens, et sert aussi à la reprise.
   - Elle est validée contre le formulaire, et sa reprise s'essaie sans rien écrire.

## Décision

- **Correspondance** : une table `ancien nom → clé de champ`, rattachée à un formulaire et identifiée par un code. Elle
  est validée à l'enregistrement :
  - chaque cible existe dans le formulaire ;
  - aucune cible n'est en double ;
  - aucun nom n'est vide.
- **Entrant** : `POST /api/public/v1/echanges/{code}/fiches`, avec le jeton de l'API publique. La fiche traduite est
  soumise par `SoumissionFiche`, c'est-à-dire avec les mêmes règles, fenêtre et places que dans l'interface.
- **Sortant** : `GET /api/public/v1/echanges/{code}/fiches?depuis=…`. Les fiches soumises sont rendues sous les anciens
  noms, avec leur numéro, leur statut et leur date de soumission.
- **Reprise** : `POST /api/interop/echanges/{id}/reprise?essai=…`, à partir d'un export JSON (tableau d'objets).
  - L'essai valide tout et n'écrit rien.
  - La reprise soumet chaque enregistrement valide, chacun dans sa propre transaction : un rejet n'annule pas les
    autres.
  - Le bilan donne, ligne par ligne, le numéro attribué ou le motif du rejet.
- **Rigueur** : un nom absent de la correspondance est **refusé** (400, nom cité), jamais ignoré.
- **Désactivation** : une correspondance désactivée rend 404 sur l'API publique, comme un code inconnu.

## Conséquences

- **Pour les systèmes du CHU** : seuls l'URL et le jeton changent ; leurs noms de champs sont conservés.
- **Modules** : le module `interop` lit les formulaires et dépose les fiches par deux ports. Les adaptateurs vivent
  dans `formulaire` et `saisie` : aucune entité d'un autre module n'est manipulée.
- **Limites** :
  - les automatisations propres à l'ancienne plateforme (scripts, règles) ne se transfèrent pas. Elles se
    reconstruisent avec le moteur de règles et les workflows ; c'est la « reconstruction » que l'avis d'intention
    anticipe ;
  - les formats autres que JSON (CSV, XML) se convertissent en JSON avant la reprise.
- **Vérification** : tests unitaires du domaine, test d'intégration de l'aller-retour (entrant, sortant, reprise),
  scénario BDD `CHU-8.04` et test de bout en bout de la page.
