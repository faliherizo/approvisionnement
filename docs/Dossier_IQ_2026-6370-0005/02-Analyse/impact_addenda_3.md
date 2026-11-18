# Addenda 3 (17 novembre 2026) — impact sur la solution et ajustements

Source : [ADDENDA_3_2026-6370-0005_FINAL_REV1.md](../09-Questions/reponses/ADDENDA_3_2026-6370-0005_FINAL_REV1.md).
L'addenda 3 fait partie intégrante de l'appel d'offres. Il **reporte le dépôt au 4 décembre 2026, 11 h (heure de l'Est)**
et répond aux questions A, B, C, D, E, P et W de [questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md).
Les questions encore sans réponse sont listées dans [questions_sans_reponse_IQ.md](../09-Questions/questions_sans_reponse_IQ.md).

✅ déjà conforme · 🆕 réalisé suite à l'addenda (2026-11-17) · 📝 à refléter dans la soumission · ⚠ écart restant.

## 1. Ce qui change par rapport aux addenda 1 et 2

L'addenda 3 transforme plusieurs hypothèses en exigences fermes (« doit ») :

| Q | Exigence désormais ferme | Avant | Maintenant |
|---|---|---|---|
| Q51 | Provisionnement et déprovisionnement **SCIM 2.0** (Saviynt), configuration, mappage, essais, documentation | ⚠ non réalisé (A-08, criticité inconnue) | 🆕 point SCIM 2.0 complet (RST-26) |
| Q43 | **Mappage configurable** des groupes et attributs Entra ID vers les rôles, moindre privilège, traçabilité des changements d'accès | ⚠ un rôle par défaut à la connexion unique | 🆕 correspondances d'annuaire (RST-27) |
| Q44 | **Domaine d'affaires** alimenté par un groupe ou un attribut d'annuaire, séparation logique | ◐ groupes saisis à la main | 🆕 domaines tirés de l'annuaire (RST-27) |
| Q26 | Génération **Word modifiable** avant l'approbation finale ; modification substantielle → nouvelle version et reprise des validations ; document à signer figé | ⚠ PDF seulement | 🆕 génération Word (RST-28) ; nouveau cycle ✅ |
| Q48 | **Bordereau ou certificat de destruction** : éléments, date, autorité, méthode, résultat | ◐ trace au journal | 🆕 certificat scellé (RST-29) |
| Q39 | Unité de mesure : **l'enveloppe envoyée** ; annulée avant envoi non comptée ; pas de double facturation | — | 🆕 relevé d'utilisation (RST-30) |
| Q38 | Démontrer **500 utilisateurs concurrents** (essais documentés) | — | ⚠ essai réalisé, objectif non atteint (§ 4) |
| Q35 | RTO 8 h et RPO 4 h en **heures écoulées** | « valeurs à convenir » | 📝 plan de relève à ajuster |

## 2. Synthèse par question

| Q | Réponse d'IQ (résumé) | Effet | État |
|---|---|---|---|
| Q1 | Base : génération, parcours, signature, preuve, traçabilité, intégrations obligatoires, administration ; IA, analyse de clauses et négociation avancée en options, jamais implicites dans le prix de base | Découpage base / options déjà arrêté | 📝 |
| Q2 | La démonstration valide les capacités déclarées ; options clairement identifiées ; rien ne s'ajoute après le dépôt | Déclarer dans la soumission tout ce qui sera démontré | 📝 |
| Q3 | Premier processus : conventions d'aide financière Grantmaking | Conforme | ✅ |
| Q4 | Volumétrie annuelle du bordereau ; pointes saisonnières absorbées ; seuils d'essais fixés en conception | Plan de charge CLM (addenda 2) | ✅ 📝 |
| Q5 | 1 300 signataires ou initiateurs, 20 administrateurs d'affaires, 18 administrateurs techniques ; expliquer l'incidence du modèle de licence | Aucune limite technique par profil | 📝 |
| Q6 | Une organisation ; séparation logique des rôles, groupes, modèles, données, unités, droits ; toute organisation additionnelle déclarée et tarifée | Une organisation `iq` ; groupes et domaines d'affaires | ✅ |
| Q7 | `FundingRequest` maître ; lecture par Id ou `Name` ; correspondance par modèle | Conforme | ✅ |
| Q8 | Trois champs de retour ou équivalents approuvés ; **aucun champ additionnel sans approbation d'IQ** | La solution n'en crée aucun | ✅ |
| Q9, Q24 | Grantmaking, API REST v60.0 | Conforme | ✅ |
| Q10 | REST + `PATCH` ; tout autre mécanisme décrit et approuvé | Conforme | ✅ |
| Q11 | Client credentials sous réserve ; sinon JWT Bearer ou autre OAuth 2.0, compte dédié, moindre privilège, secrets protégés | Les deux flux réalisés (RST-23) | ✅ |
| Q12 | Salesforce maître du dossier ; la plateforme maître du document signé, de la preuve et de la piste d'audit | Conforme | ✅ |
| Q13 | Fichiers et preuves dans la plateforme ; référence durable et statut vers Salesforce ; copie SharePoint = intégration distincte si requise | Conforme ; export en lot disponible (RST-24) | ✅ |
| Q14 | Statuts minimaux ; décrire la correspondance, le moment de chaque mise à jour, les erreurs et reprises | Correspondance configurable (RST-21) | ✅ 📝 |
| Q15 | Minimisation ; fournir l'information pour l'ÉFVP (catégories, finalités, flux, lieux, sous-traitants, durées) | Lecture limitée (RST-22) | ✅ 📝 |
| Q16 | Essais en bac à sable ou environnement contrôlé, données fictives | Bac à sable local | ✅ |
| Q17 | Limites Salesforce, 8 reprises plafonnées à 1 h, alertes approuvées en conception | Conforme | ✅ |
| Q18 | Pondérations confidentielles | — | — |
| Q19 | Colonnes « Applicabilité » et « Preuve attendue » de l'Annexe 10 réservées à IQ | Ne pas les remplir | 📝 |
| Q20 | Soumission en PDF + Annexe 2 et Annexe 10 en **Excel natif** | Dépôt | 📝 |
| Q21 | Prix fermes 3 ans ; années 4 et 5 inscrites au dépôt | Bordereau | 📝 |
| Q22 | Modèle commercial libre ; authentification standard incluse au prix de base | Bordereau | 📝 |
| Q23 | Renforcée = facteur ou preuve en plus ; SMS acceptable selon le risque ; IQ peut exiger plus robuste | Niveaux d'identité selon le risque ✅ | ✅ |
| Q25 | Workday : réception, signataires, déclenchement, suivi, statuts, document signé ou référence durable | API d'intégration (TI-22) | ✅ 📝 |
| Q26 | Word modifiable avant approbation finale ; nouvelle version et reprise des validations ; document à signer figé et traçable | **RST-28** | 🆕 |
| Q27 | Interfaces en français ; toute composante uniquement anglaise utilisée par IQ est un écart à déclarer | Interfaces FR/EN | ✅ |
| Q28, Q45 | Canada ; tout hors Canada (hébergement, traitement, sauvegarde, accès administratif, service partagé) déclaré et autorisé par écrit | Tableau des lieux à produire | 📝 ⚠ |
| Q29, Q30, Q32 | Références de l'éditeur ; intégrateur désigné ; assurances | Forme de la soumission | 📝 |
| Q31 | Démonstration : génération, validation, approbation, signature, modifications, notifications, preuves, audit, mobilité, accessibilité, exceptions, exportations | Toutes réalisées | ✅ 📝 |
| Q33 | Préavis de 90 jours ouvrables pour les changements majeurs | Politique de mise à jour | 📝 |
| Q34 | DEV, ACC, PRD distincts, migration contrôlée, retour arrière | Trois environnements prévus | 📝 |
| Q35 | RTO 8 h, RPO 4 h en heures écoulées | Plan de relève | 📝 |
| Q36 | Acheminement conditionnel via Salesforce Flow accepté s'il est de bout en bout | Règles conditionnelles dans la plateforme | ✅ |
| Q37 | Modalités de protection des données d'IQ prévalent (Loi 25) | Contrat | 📝 |
| Q38 | 500 utilisateurs concurrents démontrés de façon vérifiable | Essai réalisé ; 3 défauts corrigés ; suivi à paginer (§ 4) | ⚠ |
| Q39 | Facturation à l'enveloppe envoyée | **RST-30** | 🆕 📝 |
| Q40 | Aucun fournisseur imposé ; preuve, certificat, piste d'audit scellée ; tiers déclaré et inclus au prix | Cachet CAdES/PAdES (ADR-013) | ✅ 📝 |
| Q41 | Horodatage RFC 3161 reconnu ; autorité, lieux, coûts, validation à long terme décrits | RFC 3161 configurable | ✅ 📝 |
| Q42 | Microsoft Entra ID, OIDC ou SAML 2.0 ; le fournisseur configure, teste, met en service | Les deux réalisés | ✅ |
| Q43 | Mappage configurable groupes et attributs → rôles ; moindre privilège ; traçabilité | **RST-27** | 🆕 |
| Q44 | Domaine d'affaires = unité organisationnelle ; alimenté par groupe ou attribut d'annuaire | **RST-27** | 🆕 |
| Q46 | Périodes par catégorie documentaire, suspension par gel légal, preuve vérifiable de destruction | Calendrier (ADR-012) + **RST-29** | ✅ 🆕 |
| Q47 | Gel légal par un rôle autorisé ; aucune destruction pendant le gel ; opérations journalisées et exportables | Conforme (gel, journal, export CSV) | ✅ |
| Q48 | Détruire, transférer ou conserver ; bordereau ou certificat de destruction | Sorts finaux ✅ + **RST-29** | 🆕 |
| Q49 | Processus RH (offres, contrats, avenants, politiques) ; architecture évolutive | API d'intégration | ✅ |
| Q50 | Mécanisme Workday proposé et mis en œuvre par le fournisseur, inclus au prix | Côté Workday : travaux à chiffrer | 📝 |
| Q51 | SCIM 2.0 ou connecteur équivalent ; configuration, mappage, essais, désactivation, documentation | **RST-26** | 🆕 |

## 3. Ajustements réalisés

### RST-26 — Provisionnement SCIM 2.0 (Q51) 🆕

Point `/scim/v2` conforme aux RFC 7643 et 7644, authentifié par un **jeton porteur propre à l'organisation**. Seule
l'empreinte SHA-256 du jeton est conservée ; il est remis une seule fois, et en créer un nouveau révoque l'ancien.

- **Users** : création, lecture, liste filtrée par `userName eq "…"`, remplacement (PUT), modification (PATCH, y compris
  `active` envoyé en texte par Entra ID), suppression.
- **Groups** : création, lecture, liste filtrée par `displayName eq "…"`, PUT, PATCH (ajout ou retrait de membres,
  renommage), suppression.
- **Découverte** : `ServiceProviderConfig` et `ResourceTypes`.
- Les erreurs suivent le format SCIM (`401`, `404`, `409 uniqueness`, `400 invalidValue`).

Un compte provisionné reçoit d'abord le rôle le plus restreint (`LECTEUR`), puis ses groupes lui donnent rôles et
domaines d'affaires par les correspondances (RST-27). Le **déprovisionnement** désactive le compte sans l'effacer : la
piste d'audit reste entière, et un compte désactivé ne peut plus se connecter par SSO. Chaque provisionnement,
déprovisionnement et changement de rôle est consigné au journal d'audit.

**Depuis l'interface :** Administration → Authentification unique → bloc **Provisionnement SCIM 2.0**, avec l'adresse
du point et le bouton **Créer un jeton**.

**Vérification :** `Addenda3IntegrationTest.saviynt_provisionne…`, `AnnuaireDomaineTest`, e2e « Q43 / Q51 ».

### RST-27 — Correspondances d'annuaire vers rôles et domaines d'affaires (Q43, Q44) 🆕

L'organisation définit des correspondances : un **groupe** (identifiant d'objet Entra ID ou nom) ou un **attribut**
(ex. `department = Financement`) donne un rôle, un domaine d'affaires (groupe du module, comme une unité
administrative), ou les deux.

- **Quand elles s'appliquent :** à chaque connexion unique (claims `groups` et `roles` en OIDC, attributs de groupe
  en SAML) et à chaque poussée SCIM.
- **Moindre privilège :** un compte sans correspondance reçoit le rôle par défaut. Le rôle de superadministrateur ne
  peut jamais être donné. Tant qu'aucune correspondance de rôle n'existe, les rôles attribués dans la plateforme sont
  conservés.
- **Traçabilité :** chaque changement de rôle est inscrit au journal d'audit, avec l'avant et l'après.
- **Minimisation :** seuls les attributs cités par une correspondance sont conservés.
- **Architecture :** les domaines d'affaires passent au module de contrats par un événement d'application. Le module
  d'authentification n'accède jamais aux groupes du module.

**Depuis l'interface :** Administration → Authentification unique → bloc **Annuaire : rôles et domaines d'affaires**.

**Vérification :** `Addenda3IntegrationTest.la_connexion_unique_applique_les_attributs…`, `AnnuaireDomaineTest` ;
e2e « Q43 / Q51 ».

### RST-28 — Génération Word modifiable (Q26) 🆕

Le contrat se génère depuis le modèle en **Word (.docx)** ou en PDF. Le Word se corrige, puis se redépose comme nouvelle
version avant l'approbation finale. Pendant le parcours, toute nouvelle version ouvre un nouveau cycle : la version
précédente est remplacée, les signatures sont invalidées et les validations reprennent. Le document signé est figé et
son empreinte scellée.

**Depuis l'interface :** onglet Documents → **Générer en Word modifiable**.

**Vérification :** `Addenda3IntegrationTest.le_contrat_se_genere_en_word…` ; e2e « Q26 ».

### RST-29 — Certificat de destruction (Q48) 🆕

La destruction consigne un **bordereau** avec la trousse. Il contient :

- les éléments détruits (documents, versions, empreintes SHA-256) ;
- la date et l'autorité ;
- la méthode : effacement des contenus et anonymisation, empreintes conservées ;
- le résultat ;
- le décompte des données anonymisées.

Le **certificat de destruction** est ce bordereau rendu en PDF et scellé par le cachet électronique (PAdES).

**Depuis l'interface :** Conservation et destruction → bloc **Destructions effectuées** → **Certificat de destruction**.

**Vérification :** `ConservationServiceTest`, `CertificatDestructionServiceTest`, `Addenda3DomaineTest` ; e2e « Q39 / Q48 ».

### RST-30 — Relevé d'utilisation par enveloppe envoyée (Q39) 🆕

Une enveloppe est une trousse dont l'invitation à signer est partie. Le relevé est tiré du journal scellé, donc
vérifiable événement par événement. Les règles de comptage :

- l'enveloppe compte une fois, au mois du premier envoi ;
- une trousse annulée avant envoi ne compte pas ;
- un renvoi, un signataire remplacé ou une correction (nouveau cycle) ne compte pas de nouveau ;
- une annulation après envoi reste comptée.

Le relevé distingue aussi les enveloppes terminées, rejetées et échues.

**Depuis l'interface :** Journal d'audit → bloc **Utilisation par enveloppe** (administration et audit).

**Vérification :** `Addenda3DomaineTest.une_enveloppe_se_compte_une_fois…`, `Addenda3IntegrationTest` ; e2e « Q39 / Q48 ».

## 4. Q38 — 500 utilisateurs concurrents ⚠

Essai réalisé le 2026-11-17 ([perf/README.md](../../../perf/README.md), section « Q38 ») : **objectif non atteint à ce
jour**. L'essai a révélé trois défauts de concurrence, corrigés :

- références de trousse en double sous concurrence ;
- erreurs serveur masquées en 403 ;
- pool de connexions trop petit.

Il a aussi révélé une limite de capacité, seulement en partie corrigée : le suivi centralisé relit trop de trousses.
Reste à faire : paginer l'API et l'écran du suivi, et grouper ses requêtes ; puis refaire l'essai sur l'infrastructure
cible.

## 5. Points restants

| # | Point | Suite |
|---|---|---|
| ⚠ Q28, Q45 | Tableau des lieux : courriel, SMS et horodatage pas encore désignés | Choisir des fournisseurs au Canada ou les déclarer |
| 📝 Q35 | RTO 8 h et RPO 4 h en heures écoulées | Ajuster le plan de relève (sauvegarde au moins toutes les 4 h) |
| 📝 Q20 | Annexes 2 et 10 en Excel natif | Dépôt |
| 📝 Q50 | Mécanisme côté Workday (Studio, Orchestrate, Extend) à proposer et chiffrer | Soumission |
| — | Questions sans réponse | [questions_sans_reponse_IQ.md](../09-Questions/questions_sans_reponse_IQ.md) |

## 6. Comment vérifier

```bash
cd backend && mvn -o verify                     # inclut Addenda3IntegrationTest et la porte de couverture « annuaire »
cd e2e && npx playwright test views/clm.exigences.spec.ts -g "Addenda 3"
# SCIM à la main (jeton créé dans Administration → Authentification unique)
curl -H "Authorization: Bearer $JETON" http://localhost:8080/scim/v2/Users
```
