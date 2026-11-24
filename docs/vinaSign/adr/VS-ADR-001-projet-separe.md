# VS-ADR-001 — VinaSign dans un dépôt et un déploiement séparés

- **Statut** : Proposé
- **Date** : 2026-11-22
- **Décideurs** : direction technique Vina, responsables des applications clientes

## Contexte et problème

- **Plusieurs clients** : la signature électronique doit servir le contrat (CLM), le SAD et Télésoin
  (`soins-virtuel`), d'autres applications de `~/project`, puis des clients externes en SaaS.
- **Point de départ** : le moteur vit aujourd'hui dans le module CLM du dépôt `contractuelle_contrat`.

Faut-il le laisser dans ce dépôt, le distribuer comme bibliothèque, ou en faire un produit séparé ?

## Facteurs de décision

- **Une seule logique de sécurité** : clés, preuve, authentification des signataires.
- **Un périmètre de certification restreint** : SOC 2, ISO 27001, tests d'intrusion visent un système, pas tous les
  dépôts qui l'utilisent.
- **Des cycles de livraison indépendants** des applications clientes.
- **Un domaine de confiance distinct** : origine Web, secrets, HSM, journaux.
- **La vente en SaaS** sans livrer le code des applications internes.

## Options envisagées

1. **Garder le moteur dans le module CLM du contrat**, que les autres applications appellent.
   - Le contrat devient un passage obligé pour le SAD ou Télésoin, ce qui crée un couplage de déploiement et de
     disponibilité.
   - Le périmètre de certification englobe tout le contrat.
2. **Bibliothèque Java partagée**, embarquée dans chaque application.
   - Les versions divergent, les clés se dupliquent dans chaque application, la preuve se disperse.
   - Le dossier 2, § 2, l'écarte déjà.
3. **Produit séparé (retenue)** : dépôt `~/project/vinasign`, déploiement et domaine propres. Les applications
   l'appellent par l'API, les SDK et une interface intégrée.

## Décision

- **Option 3.** Le dépôt `vinasign` contient :

  ```
  vinasign/
  ├── serveur/            Spring Boot 3, Java 21 — monolithe modulaire, un paquet par contexte métier
  ├── portail/            Angular — administration de l'organisation, préparation des enveloppes
  ├── signataire/         Angular — expérience de signature, servie sur sa propre origine (intégrable en iframe)
  ├── sdk/java/           vinasign-client, vinasign-spring-boot-starter — compatibles Java 17
  ├── sdk/ts/             @vinasign/client (Node), @vinasign/embed (navigateur, sans framework)
  ├── sdk/dotnet/         (V1)
  ├── infra/              Terraform, chart Helm, politiques
  ├── securite/ e2e/ bdd/ perf/
  └── docs/               architecture, ADR (préfixe VS-ADR), guides d'intégration
  ```

- **Exposition sur trois origines distinctes** : API, signataire et portail. Les noms de domaine sont à réserver.
- **Distribution des SDK** par un registre privé (Azure Artifacts ou GitHub Packages) pour les applications
  internes, puis publique en V1.

## Conséquences

- **Positives** :
  - VinaSign se certifie et se teste seul ;
  - une faille d'une application cliente n'expose ni les clés ni les preuves ;
  - le produit se vend sans le reste.
- **Négatives** :
  - un service de plus à exploiter (supervision, PRA) ;
  - un appel réseau dans les parcours de signature des applications ;
  - une migration du CLM à planifier ([VS-ADR-002](VS-ADR-002-extraction-du-moteur-clm.md)).
- **Neutres** : les applications internes deviennent des clientes comme les autres, ce qui est un excellent banc
  d'essai du SaaS.
