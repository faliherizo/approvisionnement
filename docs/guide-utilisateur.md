# Guide de l'utilisateur — rédaction contractuelle

- **Pour qui** : le personnel de la Ville qui rédige, révise, approuve ou administre les documents contractuels
  (approvisionnement, services juridiques, directions requérantes, administration).
- **Exigence couverte** : cahier 2026-50-92, ENF4 « Formation en français documentée » (LEV-C-19).
- **Mise à jour** : 22 novembre 2026. Ce guide suit les écrans tels qu'ils apparaissent dans l'application ; une
  modification d'écran le met à jour dans le même changement.

## 1. Avant de commencer

### Se connecter

1. Ouvrez l'adresse de la plateforme fournie par la Ville.
2. Choisissez **Se connecter avec Microsoft** : l'authentification passe par l'annuaire de la Ville (Entra ID), avec
   la vérification en deux étapes déjà en place pour votre compte.
3. Après une période d'inactivité, la session se ferme d'elle-même : reconnectez-vous, le travail enregistré est
   conservé.

### Se repérer

Le menu du haut regroupe les écrans par usage. Vous ne voyez que ceux que votre rôle permet.

| Menu | Écrans | Pour |
|---|---|---|
| **Documents** | Documents, Dossiers, Assistant guidé, Bibliothèque, Recherche | rédiger et retrouver |
| **Pilotage** | Tableau de bord, Rapports de suivi, Assistant IA, Dépôts simulés | suivre l'activité |
| **Administration** | Utilisateurs, Vie privée (Loi 25), Templates, Gabarits, Circuits d'approbation, Journal d'audit, Paramètres | administrer |
| **Profil** | langue, sécurité du compte | soi-même |

La langue (français, anglais, malgache) se change en haut à droite. Le contenu des clauses reste dans la langue où il
a été rédigé.

### Les rôles

| Rôle | Peut |
|---|---|
| Rédacteur | créer et modifier des documents, proposer des clauses personnalisées |
| Approvisionnement | rédiger, assembler les devis, émettre les addendas, approuver l'étape « APPRO » |
| Juridique | gérer la bibliothèque de clauses, commenter, approuver l'étape « JURIDIQUE » |
| Direction | approuver l'étape « DIRECTION » |
| Lecteur | consulter |
| Administrateur | tout ce qui précède, plus l'administration |

## 2. Retrouver une clause — Recherche

Menu **Documents › Recherche**. La recherche porte à la fois sur les clauses, les documents et les modèles.

1. Saisissez un **terme** : il est cherché dans le code, le titre et le corps des clauses (sans tenir compte des
   majuscules). Laissez-le vide pour tout parcourir avec les seuls filtres.
2. Affinez avec les filtres, cumulables :

| Filtre | Effet |
|---|---|
| **Catégorie** (portée) | standard, optionnelle ou personnalisée |
| **Type de document** | les documents et modèles de ce type, et les clauses qui s'y appliquent : une clause sans condition de type vaut pour tous les types |
| **Thématique** | la section du cahier (ex. « Dispositions générales »), proposée à partir des clauses existantes |
| **Clauses normatives seulement** | les clauses exigées par la loi (Charte de la langue française, LCOP…) |

3. Choisissez **Rechercher**. Sous le nombre de résultats, deux séries de compteurs indiquent la répartition des
   clauses trouvées par catégorie et par thématique : elles aident à resserrer la recherche suivante.
4. Pour une clause : **Consulter** (lecture seule), **Éditer** (ouvre la bibliothèque) ou **Supprimer** (juridique et
   administration seulement, après confirmation).

## 3. Gérer la bibliothèque de clauses

Menu **Documents › Bibliothèque** (juridique et administration).

### Créer ou modifier une clause

1. **Nouvelle clause**, ou **Éditer** sur une clause existante.
2. Renseignez le **code** (unique), le **titre**, la **catégorie**, le **numéro** (ex. 10.06.01), la **section** (la
   thématique utilisée par la recherche) et le **corps**.
3. Cochez **Clause normative** si la loi l'exige : elle sera ajoutée d'office aux documents concernés et verrouillée.
4. Sous **Conditions d'éligibilité**, indiquez quand la clause s'applique : type de contrat, mode de sollicitation,
   valeur minimale ou maximale, mot-clé du contexte. Sans condition, la clause est proposée partout.
5. **Dépendances** : codes des clauses à inclure avec celle-ci. **Exclusions** : codes des clauses qui ne doivent
   jamais l'accompagner.
6. **Enregistrer**. Un refus signale soit un rôle insuffisant, soit un code déjà utilisé.

### Historique d'une clause

Le bouton **Historique** liste chaque version (numéro, auteur, date, motif). Choisissez une version pour la
**restaurer** : la restauration crée une nouvelle version, l'historique n'est jamais réécrit.

## 4. Rédiger un document

### Avec l'assistant guidé (recommandé)

Menu **Documents › Assistant guidé**. L'assistant pose les questions qui déterminent les clauses :

1. type de document (appel d'offres public, demande de prix, gré à gré, bail, prêt à usage, avis d'intention…) ;
2. valeur estimée, qui détermine le mode de sollicitation ;
3. contexte du projet (mots-clés).

Il propose ensuite les clauses éligibles : les normatives sont incluses d'office, les dépendances s'ajoutent seules
(badge « auto »), les exclusions sont appliquées. Vérifiez la sélection, puis générez le document.

### Dans l'éditeur

Menu **Documents**, puis ouvrez un document.

- **Assembler le devis** : assemble les clauses du catalogue retenues par le moteur de règles.
- **Ajouter une section libre** : pour un texte propre au projet.
- Une **clause normative verrouillée** ne se modifie pas dans le document ; demandez au juridique de faire évoluer la
  clause dans la bibliothèque.
- **Bordereau de prix**, **Annexes** : ajoutez les lignes de prix et les pièces jointes.
- **Capturer une version** fige l'état du document ; **Comparer** montre les écarts entre deux versions.
- **Aperçu PDF** produit le document tel qu'il sera publié.

### Regrouper par dossier

Menu **Documents › Dossiers** : un dossier réunit les documents d'un même projet d'acquisition et en suit
l'avancement.

## 5. Réviser et approuver

### Commenter

Dans l'éditeur, section **Commentaires** : **Commenter** une clause ou le document, répondre dans le fil, puis marquer
le commentaire **résolu** (ou le **rouvrir**). Les commentaires ne figurent pas dans le document publié.

### Faire approuver

1. **Démarrer l'approbation** : le document suit le **circuit configuré pour son type** (voir § 7). Sans circuit
   configuré, le circuit par défaut s'applique : approvisionnement, puis juridique, puis direction.
2. Chaque approbateur voit l'étape qui l'attend et choisit **Approuver** ou **Refuser**. Une étape refusée est
   signalée comme telle dans le circuit : le document repasse alors par la rédaction avant un nouveau circuit.
3. Le circuit se lit dans la section **Circuit d'approbation** : étape en cours, décisions, auteurs et dates.

## 6. Addendas et version finale

1. Pendant l'appel d'offres, section **Addendas** : **Ajouter un changement**, choisissez la **clause du devis à
   modifier**, rédigez le nouveau texte.
2. Émettez l'addenda : il est **réintégré** dans le document.
3. La version finale consolidée (PDF ou Word) intègre tous les addendas émis.
4. **Fiche SEAO (XML)** : produit les métadonnées du devis au format XML attendu pour la publication au SEAO.

## 7. Administration

Menu **Administration** (administrateurs).

| Écran | Usage |
|---|---|
| **Utilisateurs** | créer un compte, attribuer les rôles, désactiver un compte |
| **Circuits d'approbation** | définir, par type de document, les étapes et leur ordre (ex. bail : approvisionnement puis direction). Le circuit s'applique aux approbations démarrées ensuite |
| **Templates** et **Gabarits** | modèles de documents et mise en page des PDF |
| **Journal d'audit** | qui a fait quoi et quand : création, modification, approbation, suppression, connexion |
| **Vie privée (Loi 25)** | traiter une demande d'accès ou d'effacement des renseignements d'une personne ; l'effacement pseudonymise sans rompre l'historique. Chaque demande est consignée au registre |
| **Paramètres** | paramètres de l'organisation |

## 8. Questions fréquentes

| Situation | Que faire |
|---|---|
| Une clause attendue n'apparaît pas dans l'assistant | Vérifiez ses conditions d'éligibilité (type, valeur, contexte) dans la bibliothèque |
| Impossible de modifier une clause dans le document | Elle est normative : elle évolue dans la bibliothèque, par le juridique |
| Le circuit d'approbation n'est pas celui attendu | Vérifiez le circuit du type de document dans **Circuits d'approbation** ; un changement ne touche pas une approbation déjà démarrée |
| Déconnecté sans action de votre part | La session expire après inactivité : reconnectez-vous |
| Un bouton n'apparaît pas | Votre rôle ne le permet pas : adressez-vous à l'administrateur |

Le soutien est joignable par l'adresse indiquée dans votre entente de service.
