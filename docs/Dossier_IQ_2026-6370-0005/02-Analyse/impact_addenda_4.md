# Addenda 4 (19 novembre 2026) — impact sur la solution et ajustements

Source : [addenda-4.md](../09-Questions/reponses/addenda-4.md) (PDF : `09-Questions/reponses/ADDENDA 4_ 2026-6370-0005_FINAL.pdf`).
L'addenda 4 complète les addenda 1 à 3 sans reporter le dépôt (toujours le 4 décembre 2026, 11 h). Il répond aux
questions C, D, E, F, G, J, L, O, P, V et W restées ouvertes dans
[questions_sans_reponse_IQ.md](../09-Questions/questions_sans_reponse_IQ.md), et ajoute une liste de vérification
avant le dépôt.

✅ déjà conforme · 🆕 réalisé suite à l'addenda (2026-11-19) · 📝 à décrire dans la soumission · ⚠ limite à déclarer.

## 1. Synthèse par question

| Q | Réponse d'IQ (résumé) | Effet | État |
|---|---|---|---|
| Q1 | Niveaux d'authentification et de signature configurables selon le **type de document**, le processus et les règles d'autorisation | Niveau minimum par type de document, combiné à la politique par risque (RST-35) | 🆕 📝 |
| Q2 | Internes : Entra ID et SSO ; externes : lien par courriel, code à usage unique, SMS, méthode renforcée | Déjà le cas ; méthodes, prérequis, limites et coûts à décrire | ✅ 📝 |
| Q3 | Signature externe sans compte permanent, avec authentification, traçabilité et preuve suffisantes | Portail à jeton et vérification d'identité | ✅ |
| Q4 | Document signé et preuves : formats PDF, validation à long terme, formats d'export | PDF signé PAdES, CAdES détaché, RFC 3161, ZIP et JSON. Validation à long terme (LTV) à compléter | ✅ ⚠ 📝 |
| Q5 | Représentation visible de la signature, à un emplacement configurable | Zones de signature positionnées dans le modèle | ✅ |
| Q6 | Signature par un **représentant dûment autorisé** ; IQ exige ou conserve la preuve de son autorité ; traçabilité de l'identité et de la **qualité** | Qualité, entreprise représentée, attestation d'autorité (RST-32) | 🆕 |
| Q7 | Le papier n'est pas une exigence fonctionnelle | — | — |
| Q8, Q20 | Parcours conditionnels selon le type de processus, l'unité, le rôle, le montant ; délégations internes configurées | Règles sur les champs (montant) ✅ ; **sur le type, l'unité, le risque, l'origine** (RST-36) | 🆕 |
| Q9, Q30 | Environnements, comptes et données de test fixés au démarrage | Prérequis à déclarer | 📝 |
| Q10 | **Séparer l'administration d'affaires et l'administration technique**, moindre privilège | Rôles `ADMIN_AFFAIRES` et `ADMIN_TECHNIQUE` (RST-31, ADR-017) | 🆕 |
| Q11, Q31 | SCIM 2.0 ; désactivation rapide ; **mappage des attributs et groupes** | SCIM ✅ (RST-26) ; **attributs SCIM** dans les correspondances (RST-34) | ✅ 🆕 |
| Q12 | Compte local de secours justifié, limité, protégé, approuvé ; aucun contournement des contrôles | Comptes locaux avec second facteur. Restriction par organisation à réaliser | ⚠ 📝 |
| Q13 | Tout accès administratif ou de soutien déclaré avec son territoire | Tableau des lieux ([solutions proposées](../04-Architecture/solutions_proposees_reponses_IQ.md) § 3) | 📝 |
| Q14 | Export dans des formats pérennes, **PDF/PDF-A lorsque disponibles**, export massif | Export en lot ✅. PDF/A non produit | ✅ ⚠ 📝 |
| Q15 | Décrire les sauvegardes : fréquence, durée, redondance, restauration, contrôles ; respecter RTO 8 h et RPO 4 h | Plan de relève `infra/dr` (RPO ≤ 15 min, RTO ≤ 4 h) | 📝 |
| Q16 | Déclarer certifications, rapports d'audit, attestations | Attestations de l'hébergeur | 📝 |
| Q17 | Quorum configurable selon le processus | Quorum et politique de rejet par étape | ✅ |
| Q18 | Retour pour correction, reprise d'étapes, fin sur rejet ; validations requises après modification substantielle | Retour à une étape, nouveau cycle | ✅ |
| Q19 | Délais, rappels, expirations, annulations, délégations configurables | Paramètres du module et de la trousse | ✅ |
| Q21 | Nombre de modèles inclus et effort d'un modèle additionnel | Bordereau | 📝 |
| Q22 | Reprendre les modèles Word d'IQ en préservant, autant que possible, structure et présentation ; déclarer limites et conversions | Modèles à variables (texte). Mise en page Word non reprise | ⚠ 📝 |
| Q23 | Indiquer les tailles maximales et leur effet ; limites ajustables | 25 Mo par défaut, réglable jusqu'au plafond de la plateforme | ✅ 📝 |
| Q24, Q25 | Pas de réduction des niveaux de service ; recours du contrat | Engagements de service | 📝 |
| Q26 à Q28 | Tous les coûts déclarés ; tiers et sous-traitants déclarés | Bordereau, déclaration des tiers | 📝 |
| Q29 | Workday : architecture, échanges, statuts, document signé ou référence durable | API d'intégration ✅ | ✅ 📝 |
| Q32, Q33 | Intégrations Microsoft 365 décrites si proposées ; SharePoint fixé en conception | Publication M365 existante, à décrire ; option | 📝 |
| Q34 | Capacité maximale de traitement en lot | 500 dossiers par lot | ✅ 📝 |
| Q35 | Documents individualisés par dossier dans un lot | Une trousse générée par dossier | ✅ |
| Q36 | **Mécanismes d'approbation adaptés au lot**, traçabilité par dossier, préciser globale ou individuelle | **Approbation globale de lot** (RST-33) ; approbation individuelle existante | 🆕 |

## 2. Ajustements réalisés

### RST-31 — Administration d'affaires et administration technique séparées (Q10) 🆕

Deux nouveaux rôles, `ADMIN_AFFAIRES` et `ADMIN_TECHNIQUE`, liés par une hiérarchie : `ADMIN` cumule les deux et reste
seul à attribuer les rôles ([ADR-017](../../adr/ADR-017-separation-des-administrations.md)).

| Administration d'affaires | Administration technique |
|---|---|
| Messages, paramètres du module, listes de référence, groupes, publication des modèles, conservation et destruction, journal d'audit du module, relevé d'utilisation, gestion de toutes les trousses | Intégrations Salesforce, clients d'intégration (Workday), authentification unique (OIDC, SAML), annuaire et SCIM, Microsoft 365 et SAP, sécurité avancée. **Aucun accès au contenu des trousses** |

- **Interface :** les menus suivent le rôle.
- **Comptes de démonstration :** `admin.affaires@iq.example` (affaires), `admin.technique@iq.example` (technique),
  `admin.ti@iq.example` (les deux).
- **Vérification :** `Addenda4IntegrationTest.l_administration_d_affaires_et_l_administration_technique_sont_separees`.

### RST-32 — Signataire représentant une entreprise (Q6) 🆕

- **Participant :** il porte sa **qualité** (président, mandataire…) et l'**entreprise représentée**. L'initiateur
  peut **exiger une attestation d'autorité**.
- **Portail :** il affiche « Vous signez pour X, à titre de Y » et, si l'attestation est exigée, refuse la signature
  sans la case « J'atteste être dûment autorisé ». Une référence (numéro de résolution, procuration) est facultative.
- **Preuve :** la qualité, l'entreprise et l'attestation sont versées à l'attestation de signature scellée, au journal
  (« pour X (Y) ») et au certificat de signature. Migration `060`.
- **Vérification :** `ClmIntegrationTest.un_representant_atteste_son_autorite…`, `Addenda4DomaineTest`.

### RST-33 — Approbation globale d'un lot (Q36) 🆕

**Signature en lot → Approuver le lot** approuve d'un coup les étapes actives du lot qui reviennent à l'utilisateur.
Chaque décision passe par le parcours normal : droits, séparation des responsabilités, journal scellé de chaque
dossier, avec le motif « Approbation globale du lot ». Les étapes écartées sont rapportées avec leur motif.
L'approbation individuelle, dossier par dossier, reste disponible.

**Vérification :** `Addenda4IntegrationTest.l_approbateur_approuve_un_lot…`.

### RST-34 — Attributs SCIM dans les correspondances (Q31) 🆕

`title`, `userType`, `preferredLanguage` et, de l'extension entreprise, `department`, `division`, `organization`,
`costCenter`, `employeeNumber` deviennent des faits d'annuaire. Seuls ceux qu'une correspondance cite sont conservés.
Ils donnent rôles et domaines d'affaires comme les attributs de la connexion unique. Un changement de département
déplace le compte ; un attribut retiré le retire. Migration `061`.

**Vérification :** `Addenda4IntegrationTest.les_attributs_scim_alimentent…`.

### RST-35 — Niveau d'authentification par type de document (Q1) 🆕

Liste de référence « Niveau d'authentification minimum par type de document » : pour un type, `COURRIEL`, `CODE_SMS`,
`QUESTION` ou `FORTE`. Le niveau d'un signataire est le plus exigeant entre :

- la politique par niveau de risque ;
- le minimum du type de document ;
- le niveau demandé à l'ajout du signataire.

**Vérification :** `Addenda4DomaineTest`.

### RST-36 — Acheminement selon les attributs de la trousse (Q8, Q20) 🆕

Une règle d'étape porte sur un champ du modèle (ex. montant), ou sur le **type de document**, l'**unité
administrative**, le **niveau de risque** ou l'**origine** de la trousse (`trousse.typeContrat`, `trousse.unite`,
`trousse.risque`, `trousse.source`). Ces attributs sont proposés dans l'éditeur de parcours.

**Vérification :** `Addenda4IntegrationTest.une_etape_s_achemine_selon_le_risque…`.

## 3. Limites à déclarer et suite donnée (réalisée le 2026-11-20)

| # | Sujet | État | Proposition |
|---|---|---|---|
| RST-37 | PDF/A (Q14) et validation à long terme (Q4) | PDF signé PAdES-B, horodatage RFC 3161 si configuré ; ni PDF/A, ni données de validation (LTV) intégrées | ✅ PDF/A-2b à la génération et PAdES-B-LTA avec l'autorité retenue (ADR-018) |
| RST-38 | Modèles Word d'IQ (Q22) | Modèles à variables en texte ; la mise en page Word n'est pas reprise | ✅ Gabarit `.docx` fusionné, mise en page préservée ; PDF/A par LibreOffice côté serveur (ADR-018) |
| RST-39 | Comptes locaux de secours (Q12) | Comptes locaux possibles, second facteur disponible | ✅ Par organisation : connexion locale réservée aux comptes de secours désignés, second facteur obligatoire, journalisée |
| RST-40 | Tableau de bord et « Mes trousses » à la volumétrie d'IQ | Lisent encore toutes les trousses visibles | ✅ Pagination serveur et décomptes groupés (`LecturePaginee`) |

## 4. Liste de vérification avant le dépôt

L'addenda rappelle les contrôles d'admissibilité et de conformité :

- formulaires signés ;
- annexes jointes, l'Annexe 2 et l'Annexe 10 en Excel natif ;
- bordereau complet ;
- sous-traitants déclarés ;
- sécurité, hébergement, intégration, authentification et renseignements personnels traités ;
- addenda 1 à 4 intégrés ;
- **aucune question laissée sans réponse**.

Les questions encore ouvertes sont dans [questions_sans_reponse_IQ.md](../09-Questions/questions_sans_reponse_IQ.md),
§ 0, à transmettre à IQ sans délai.
