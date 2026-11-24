# Étude de conception — trois exigences déclarées « sans objet »

- **Date** : 2026-11-21
- **Exigences** : CHU-8.04 (maintien des échanges déjà en place), IQ-D-14 (PCI DSS si des cartes de paiement sont
  traitées), IQ-A-09 (connecteur MCP).
- **Décisions** : [ADR-021](adr/ADR-021-continuite-des-echanges-herites.md),
  [ADR-022](adr/ADR-022-cartes-de-paiement-hors-perimetre.md), [ADR-023](adr/ADR-023-connecteur-mcp-clm.md).

Les trois exigences étaient déclarées sans objet : pas d'historique chez nous, aucune carte traitée, MCP non exigé.
Chacune repose pourtant sur un risque réel du côté du client. La présente étude cherche, pour chacune, la réponse la
plus petite qui transforme une déclaration en garantie vérifiable.

## 1. CHU-8.04 — Maintien des échanges déjà en place

### Ce que demande le texte

L'appel d'intérêt du CHU (§ 8) demande le « maintien du fonctionnement des échanges de données, des automatisations et
des configurations déjà en place ». Le contexte du même document précise que les données de la plateforme actuelle
« peuvent être exportées intégralement dans un format structuré ». Les formulaires et les règles, en revanche, ne se
transfèrent pas : un changement de plateforme impose donc de reconstruire, de migrer, de valider les données et de
reprendre les échanges.

« Pas d'historique chez nous » est exact, mais le risque visé est **chez le CHU**. Les systèmes qui écrivent
aujourd'hui des fiches dans la plateforme actuelle, ou qui en lisent, continuent d'envoyer et d'attendre *leurs* noms
de champs. Chaque changement de nom les obligerait à être modifiés.

### Ce qui existe déjà

- API publique `POST /api/public/v1/formulaires/{code}/fiches` : jeton OAuth 2.0 client credentials, module `interop`.
  Elle impose cependant les clés de champ de *nos* formulaires.
- Connecteurs M365 et SAP (ADR-002), webhooks et notifications de fiches.

### Options

| Option | Effet sur les systèmes du CHU | Coût | Retenue |
|---|---|---|---|
| A. Nommer nos champs comme les anciens | Aucun changement, mais nos formulaires héritent de noms techniques imposés | faible, mais fragile (un renommage casse tout) | non |
| B. Script de migration ponctuel, hors plateforme | Les données sont reprises, mais les échanges *courants* doivent être réécrits | moyen ; le script n'est ni testé ni audité | non |
| C. **Correspondance d'échange déclarée dans la plateforme** | Le système externe ne change que l'URL et le jeton ; ses noms de champs sont traduits dans les deux sens | moyen | **oui** |

### Conception retenue (option C)

- **Correspondance d'échange** (objet de domaine) : code, formulaire cible, table `ancien nom → clé de champ`. Elle est
  validée à l'enregistrement : chaque clé cible existe dans le formulaire, aucune cible n'est en double, aucun nom
  n'est vide.
- **Trois usages d'une même correspondance :**
  1. **Entrant** : `POST /api/public/v1/echanges/{code}/fiches`. Le corps est au format de l'ancien système ; il est
     traduit, puis soumis par le même cas d'utilisation que l'interface (règles, fenêtre, places).
  2. **Sortant** : `GET /api/public/v1/echanges/{code}/fiches?depuis=…`. Les fiches soumises sont rendues avec les
     anciens noms, pour un système qui *lit* les fiches.
  3. **Reprise de l'historique** : `POST /api/interop/echanges/{id}/reprise?essai=true|false`, à partir de l'export
     structuré (JSON) de la plateforme actuelle. L'essai valide chaque enregistrement sans rien écrire et rend un bilan
     ligne par ligne. La reprise réelle soumet les enregistrements valides et rejette les autres, avec leur motif.
- **Rigueur** : un nom inconnu de la correspondance est refusé, jamais ignoré. Une perte silencieuse de donnée
  serait pire qu'un refus.
- **Interface** : la page *Intégrations (API)* gagne une section *Échanges hérités* (créer une correspondance, lancer
  un essai, lire le bilan).

## 2. IQ-D-14 — PCI DSS si des cartes de paiement sont traitées

### Ce que demande le texte

La norme PCI DSS s'applique dès qu'un système *stocke, traite ou transmet* un numéro de carte (PAN). La plateforme
n'en traite aucun par conception. Une déclaration ne suffit pourtant pas : un numéro de carte saisi dans un champ
libre (commentaire de fiche, valeur de formulaire, message) ferait entrer la plateforme dans le périmètre PCI.

### Options

| Option | Effet | Retenue |
|---|---|---|
| A. Se certifier PCI DSS | disproportionné : la plateforme n'a aucun besoin de paiement | non |
| B. Déclaration contractuelle seule | ne protège pas d'une saisie par erreur | non |
| C. Masquer le numéro et l'accepter | le numéro tronqué reste une donnée PCI (avec le reste) ; la requête d'origine est déjà journalisée par le relais | non |
| D. **Refuser à la frontière toute requête qui contient un numéro de carte** | aucun PAN n'est stocké, traité ni transmis ; la plateforme reste hors périmètre | **oui** |

### Conception retenue (option D)

- **Détection** (domaine pur, sans E/S) : une suite de 13 à 19 chiffres, séparés au plus par des espaces ou des tirets
  isolés, et qui n'est pas collée à d'autres chiffres. Elle doit en outre remplir **les deux** conditions suivantes :
  - préfixe d'un réseau émetteur reconnu (Visa, Mastercard, American Express, Discover, JCB, Diners, UnionPay), avec la
    longueur propre à ce réseau ;
  - clé de Luhn valide.
  Les faux positifs sur des numéros internes de 16 chiffres en sont fortement réduits.
- **Garde** (filtre HTTP) : toute requête d'écriture (`POST`, `PUT`, `PATCH`) vers `/api` ou `/scim` est examinée. Le
  filtre lit le corps JSON, texte ou formulaire, et la chaîne de requête de toute méthode. Une détection rend
  `422 carte_paiement_refusee`, avant toute logique métier. Le refus est journalisé sans le numéro (méthode, chemin,
  réseau).
- **Fichiers téléversés** : non inspectés. Ce sont des binaires (PDF, Word) ; la limite est documentée dans l'ADR.
- **Réglages** : `app.securite.cartes.actif` (vrai par défaut) et `app.securite.cartes.chemins-exemptes`, vide par
  défaut, pour un faux positif avéré.

## 3. IQ-A-09 — Connecteur MCP

### Ce que demande le texte

L'addenda 1 (Q3) demande s'il existe des connecteurs MCP ; IQ répond qu'ils ne sont pas exigés. MCP (Model Context
Protocol) est le protocole ouvert par lequel un assistant d'IA (Claude, Copilot, etc.) appelle les outils d'un
système. L'offrir permet à un employé d'IQ de demander à son assistant « où en est la signature du contrat X ? », sans
ouvrir de brèche.

### Options

| Option | Retenue |
|---|---|
| A. Ne rien faire (non exigé) | non : la demande d'IQ montre un intérêt, et le coût est faible |
| B. Serveur MCP séparé (Node), qui appelle l'API d'intégration | non : un composant de plus à déployer, durcir et surveiller |
| C. **Point d'accès MCP intégré au module CLM, transport *Streamable HTTP*, mêmes jetons que l'API d'intégration** | **oui** |

### Conception retenue (option C)

- **Protocole** : JSON-RPC 2.0, transport *Streamable HTTP* en mode réponse JSON (sans flux SSE). Méthodes :
  `initialize`, `notifications/initialized`, `ping`, `tools/list`, `tools/call`. Versions acceptées : `2025-06-18`,
  `2025-03-26`, `2025-11-25`.
- **Point d'accès** : `POST /api/clm/integration/mcp`. `GET` rend 405 (aucun flux ouvert par le serveur).
- **Authentification** : jeton porteur d'un client d'intégration CLM (ADR-011). Le client désigne l'organisation, et
  il est révoqué comme pour l'API. Le module CLM doit être souscrit.
- **Outils, en lecture seule** (annotation `readOnlyHint`) :
  - `lister_modeles` : modèles publiés et leurs champs ;
  - `lire_trousse` : statut d'une trousse **créée par ce client** ;
  - `lire_lot` : statut d'un lot créé par ce client.
  Aucune écriture : un assistant d'IA peut être manipulé par le contenu qu'il lit (injection d'invite). Déclencher une
  signature reste réservé à l'API d'intégration, sous le contrôle du système appelant.
- **Sécurité propre à MCP** :
  - l'en-tête `Origin` est validé (attaque par *DNS rebinding*), une origine étrangère rend 403 ;
  - une version de protocole non prise en charge rend 400 ;
  - chaque `tools/call` est consigné au journal CLM.
- **Validation réelle** : le client MCP de référence (`@modelcontextprotocol/sdk`, TypeScript) se connecte au serveur,
  liste les outils et en appelle un, dans la suite BDD.

## 4. Vérification commune

- **Tests unitaires** du domaine : correspondance, détecteur de numéro de carte, protocole MCP.
- **Tests d'intégration** sur la vraie pile, avec les tests d'architecture déjà en place.
- **Scénarios BDD** : `CHU-8.04`, `IQ-D-14` et `IQ-A-09` passent de `sans-objet` à `bdd`.
- **Tests de bout en bout** : la page *Intégrations (API)* et sa section *Échanges hérités*, aux quatre largeurs de
  référence et avec axe-core.
- **Tests d'intrusion** : les nouveaux points publics figurent dans `surface-publique.json`, avec leur protection.
