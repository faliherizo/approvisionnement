# VinaSign — Analyse des deux dossiers et de l'existant

- **Date** : 22 novembre 2026
- **Sources analysées** :
  - [VinaSign_Dossier_Architecture_Backlog.md](VinaSign_Dossier_Architecture_Backlog.md), dit « dossier 1 » ;
  - [VinaSign_Architecture_Integration_Reutilisable.md](VinaSign_Architecture_Integration_Reutilisable.md), dit « dossier 2 » ;
  - le module CLM du projet `contractuelle_contrat` ;
  - les besoins de signature des applications de `~/project`.

## 1. Le changement de cap

Les deux dossiers supposent que les services de confiance avancés viennent d'un fournisseur (dossier 1, ADR-005) et
que VinaSign se contente du parcours et de la preuve. La nouvelle orientation est différente :

> VinaSign devient **une plateforme tierce de confiance opérée par Vina** : elle gère et exécute la signature
> électronique de bout en bout, sert d'abord nos applications internes, puis se vend en SaaS.

Cette orientation tient, à une condition qu'il faut énoncer clairement : **opérer la plateforme ne fait pas de Vina
une autorité de certification**. Les certificats qui donnent une valeur probante reconnue hors de Vina (cachet de
l'organisation, certificats individuels pour la signature avancée, horodatage d'archive) restent émis par une
autorité reconnue. Vina, en revanche, détient et protège les clés dans un HSM, signe, horodate, conserve la preuve et
la rend vérifiable. C'est le modèle des plateformes de signature du marché. Devenir soi-même autorité de
certification (audits, politique de certification, infrastructure hors ligne) reste hors portée au moins jusqu'à la V2,
et n'est pas nécessaire pour vendre le service. Voir [VS-ADR-003](adr/VS-ADR-003-plateforme-de-confiance-operee.md).

## 2. Ce que les deux dossiers font bien

| Sujet | Appréciation |
|---|---|
| Distinction entre tracé manuscrit et preuve de signature | Juste et essentielle ; elle doit être reprise telle quelle dans l'offre commerciale |
| Coordonnées normalisées des champs (dossier 1, § 5.2) | Bonne décision, à versionner |
| Service centralisé exposé par API plutôt qu'une bibliothèque embarquée (dossier 2, § 2) | Bonne décision : une seule logique de sécurité, une seule surface à certifier |
| OAuth 2.0 client credentials, scopes, séparation tenant / application / acteur / signataire (dossier 2, § 7) | Bonne base ; à renforcer (§ 4) |
| Webhooks signés, anti-rejeu, anti-SSRF (dossier 2, § 9) | Conforme à ce que le CLM fait déjà |
| GED maître, VinaSign opérateur du workflow (dossier 2, § 4.3) | Bonne décision, à étendre au mode « empreinte » (§ 4) |
| Monolithe modulaire plutôt que microservices | Bonne décision |

## 3. Incohérences et lacunes relevées

| # | Constat | Conséquence | Traitement proposé |
|---|---|---|---|
| L1 | Les dossiers partent de zéro, alors que le CLM du contrat possède déjà le moteur : PAdES-B-LTA, CAdES, horodatage RFC 3161, journal chaîné, portail signataire avec code, rappels HMAC, mesure par enveloppe | 8 à 12 semaines-personnes refaites, et des validations (veraPDF, pyHanko) à rejouer | Extraire le moteur du CLM, sous tests de caractérisation ([VS-ADR-002](adr/VS-ADR-002-extraction-du-moteur-clm.md)) |
| L2 | ADR-005 (fournisseur pour la confiance avancée) contredit la nouvelle orientation | Architecture des clés absente | HSM, cycle de vie des clés, autorité partenaire ([VS-ADR-003](adr/VS-ADR-003-plateforme-de-confiance-operee.md)) |
| L3 | Aucune signature par un utilisateur **déjà authentifié** dans l'application cliente | Le SAD (US-408, notes et cosignatures) ne serait pas servi : le professionnel n'est pas un « invité » | Assertion d'identité de l'application cliente, renforcée par une seconde preuve (VS-044) |
| L4 | Les documents transitent toujours par VinaSign | Inacceptable pour des renseignements de santé (SAD, télésoin) sans encadrement lourd | Mode **signature d'empreinte** : le document ne quitte pas l'application cliente ([VS-ADR-005](adr/VS-ADR-005-signature-d-empreinte.md)) |
| L5 | Java 21 imposé aux clients | Le contrat est en Java 17 ; Télésoin est en Java 21 | Serveur en Java 21, SDK Java compatible 17 |
| L6 | Dossier 1, § 3.4 exclut tout paiement, mais l'épic E14 prévoit « Plans, compteurs, paiement » | Risque d'entrer dans le périmètre PCI DSS | Paiement confié à une page hébergée par le prestataire ; aucun numéro de carte chez VinaSign (même garde que l'ADR-022 du contrat) |
| L7 | Accessibilité « cible de conception » WCAG 2.2 AA | Plus faible que notre standard (SGQRI 008 3.0, axe-core sans violation dans chaque état) | Porte bloquante de la définition de « terminé » |
| L8 | Couverture et tests non chiffrés | Écart avec nos standards (96 % par classe, mutation, tests de contrat par port) | Repris dans la définition de « terminé » ([05-Backlog](05-Backlog-et-stories.md)) |
| L9 | Les ADR du dossier 1 sont numérotés 001 à 006, comme ceux du contrat | Confusion entre dépôts | Préfixe `VS-ADR-` |
| L10 | Redis imposé au MVP | Une dépendance de plus à exploiter | Boîte d'envoi transactionnelle et verrous PostgreSQL au MVP ; Redis si la mesure le justifie |
| L11 | Dossier 1, § 14 : 12 à 18 semaines | Vraisemblable seulement avec la réutilisation du CLM, et hors plateforme de confiance | Pilote interne en production le 4 juin 2027, soit 26 semaines calendaires après le début de la phase 0, congés compris ([06-Planning](06-Planning.md)) |
| L12 | Aucun plan de migration des clients existants (CLM pour IQ, Lévis, Trois-Rivières) | Risque de casser un engagement contractuel | Bascule par organisation, derrière un drapeau, jamais pour IQ sans son accord ([04-Integration](04-Integration-applications-internes.md)) |

## 4. Inventaire réutilisable du module CLM

Mesuré le 22 novembre 2026 : 146 classes Java, environ 15 000 lignes, plus les tests du module.

| Capacité | Classe(s) dans `contractuelle_contrat` | Maturité | Devenir dans VinaSign |
|---|---|---|---|
| Cachet PAdES-B-LTA d'un PDF | `CachetElectronique`, `CachetX509`, `DonneesValidation` | Validée par pyHanko et veraPDF (bacs à sable BS-04, BS-06) | Porté ; clé déplacée en HSM |
| Attestation CAdES détachée | `SignatureProbanteCachet`, `FournisseurSignature` | Validée par `openssl cms -verify` (ADR-019) | Porté |
| Horodatage RFC 3161 | `AutoriteHorodatage` | Validé contre l'autorité du bac à sable | Porté, avec plusieurs autorités et bascule |
| Journal scellé chaîné | `ChaineScellee`, `EvenementTrousse` | Testé | Porté, ancré par horodatage périodique |
| Dossier de preuve ZIP, vérification d'intégrité | `PreuveService` | Validé (BS-06) | Porté |
| Niveaux d'identité du signataire | `Identite` (COURRIEL, CODE_SMS, QUESTION, FORTE) | Testé | Porté, étendu (clé d'accès, assertion de l'application) |
| Invitation et portail du signataire | `Invitation`, `PortailService` | Éprouvé en e2e | Réécrit en Angular ; logique serveur portée |
| Clients d'intégration OAuth 2.0 | `ClientsIntegrationService`, `ClientIntegration` | Éprouvé | Remplacé par un serveur d'autorisation à JWT (aud, scopes) |
| Rappels sortants signés HMAC, idempotents | `RappelsExternesService`, `RappelHttp`, `GardeSortante` | Éprouvé | Devient les webhooks |
| Mesure par enveloppe | `Enveloppes` | Testé (addenda 3, Q39) | Base de la facturation SaaS |
| Signature en lot | `LotSignature`, `ElementLot` | Éprouvé | Porté (V1) |
| PDF/A, conversion Word | `ConversionServiceHttp` (Gotenberg), `ConformitePdfA` | Validé (BS-01, BS-03) | Porté |
| Connecteur MCP | `ServeurMcp`, `McpController` | Éprouvé (client de référence) | Porté |
| Antivirus | port `AnalyseAntivirus` | Port seulement | Adaptateur ClamAV à écrire |

**Ce qui reste propre au CLM** et ne va pas dans VinaSign : modèles de contrats, gabarits Word, groupes CLM,
Salesforce, parcours d'approbation métier, analyse IA, conservation propre aux contrats d'IQ. Ces fonctions relèvent du
métier « gestion de contrats ». VinaSign n'en connaît que l'enveloppe à signer.

## 5. Besoins des applications internes

| Application (dépôt) | Besoin relevé | Mode d'intégration pressenti | État |
|---|---|---|---|
| Contrat — CLM (IQ, Lévis, Trois-Rivières) | Signature de trousses multi-signataires, cachet, preuve | API + webhooks ; adaptateur derrière les ports `FournisseurSignature` et `CachetElectronique` | Moteur en place ; bascule à planifier |
| SAD (`soins-virtuel`, US-408) | Signature et cosignature des notes évolutives, empreinte au journal chaîné, TOTP à la signature | Signature d'empreinte, par un professionnel déjà authentifié | Story rédigée ; question QM-16 ouverte (avancée ou authentification forte) |
| Télésoin (`soins-virtuel/telesoin`) | Consentements par finalité (ADR-0028), consentement aux soins, représentant légal | Session intégrée (iframe) sur tablette, ou signature d'empreinte | Consentements livrés sans signature ; à confirmer |
| Autres dépôts (`transport-rcl`, `bus-ecole`, `lms`, `dsn-vina`, `refonte-assnat`, `urgences-sante`, `gestion-loisirs`, `billeterie-scolaire`) | Mentions de consentement ou de signature dans leurs documents | À qualifier avec chaque équipe | Non analysés en détail |

## 6. Conclusion

Les deux dossiers donnent une bonne base produit et intégration. Il leur manque quatre éléments, tous comblés par les
documents qui suivent :

1. la réutilisation du moteur existant ;
2. l'architecture des clés d'une plateforme de confiance opérée ;
3. la signature par un utilisateur déjà authentifié ;
4. le mode « empreinte », indispensable aux applications de santé.
