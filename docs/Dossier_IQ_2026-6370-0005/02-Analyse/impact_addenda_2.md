# Addenda 2 (14 novembre 2026) — impact sur la solution et ajustements

Source : [addenda-2.md](../09-Questions/reponses/addenda-2.md) (PDF : `09-Questions/reponses/ADDENDA 2_ 2026-6370-0005_FINAL.pdf`).
L'addenda 2 fait partie intégrante de l'appel d'offres et prévaut sur les points qu'il précise. Il répond aux questions
A1 à A6, B1 à B11, E1 et à la question sur la langue, posées dans [questions_a_clarifier_IQ.md](../09-Questions/questions_a_clarifier_IQ.md).

✅ déjà conforme · 🆕 réalisé suite à l'addenda (2026-11-16) · 📝 à refléter dans la soumission · ⚠ écart restant.

## 1. Synthèse par question

| Q | Réponse d'IQ (résumé) | Effet sur notre solution | État |
|---|---|---|---|
| Q1 | Base : données, modèles, génération, parcours, signature, retour des statuts ; CLM avancé et IA en options | Découpage base / options déjà arrêté avec l'addenda 1 | 📝 |
| Q2 | Le scénario 10 ne rend pas obligatoire le CLM avancé ; options présentées et tarifées à part | Idem ; le scénario 10 se démontre avec les options activées | 📝 |
| Q3 | Premier périmètre : conventions d'aide financière issues de Salesforce Grantmaking | Type « Convention d'aide financière » et préremplissage depuis `FundingRequest` | ✅ |
| Q4 | 55 000 à 65 000 documents par an, pics inconnus ; démontrer la volumétrie et des variations raisonnables | Plan de charge CLM ajouté au banc JMeter ([perf/clm-trousses-load.jmx](../../../perf/clm-trousses-load.jmx)) | 🆕 📝 |
| Q5 | Environ 1 300 employés ; répartition par profil en conception ; quantités du bordereau font foi | Aucune limite par profil dans la plateforme | ✅ 📝 |
| Q6 | Une organisation principale ; gérer unités, rôles, accès, modèles, données ; indiquer limites et coûts des groupes, unités, environnements | Groupes du module et unités sans limite ni coût (écran Groupes, liste Unités) | ✅ 📝 |
| Q7 | API admises (pas d'AppExchange imposé) ; objet `FundingRequest` lu par Id ou par `Name` ; correspondance des champs par modèle | Déjà le cas ; décrire méthode, prérequis, limites, responsabilités, erreurs et reprise | ✅ 📝 |
| Q8 | Champs de retour envisagés : `CLM_Statut__c`, `CLM_Reference__c`, `CLM_Correlation__c` | Valeurs par défaut de la plateforme, renommables par l'organisation | ✅ |
| Q9 | API Salesforce v60.0, ajustable ; rester compatible avec les versions supportées | `v60.0` par défaut, modifiable dans l'écran Intégrations API | ✅ |
| Q10 | REST : lecture à la demande, mise à jour des statuts, retour des références, erreurs et reprises ; pas d'événement entrant imposé | Déjà le cas (lecture, `PATCH` idempotent, boîte d'envoi avec reprises) | ✅ |
| Q11 | Compte d'exécution dédié possible ; **décrire une méthode de remplacement, notamment JWT Bearer** | **Authentification JWT Bearer réalisée** (RST-23) | 🆕 |
| Q12 | Salesforce maître du dossier ; la solution gère document signé, preuve, piste d'audit, métadonnées ; statuts et références retournés | Déjà le cas | ✅ |
| Q13 | Documents et preuves conservés dans la solution ; référence et statuts vers Salesforce ; **exportation ou transfert vers un référentiel désigné par IQ** | **Exportation en lot vers un référentiel** réalisée (RST-24) | 🆕 |
| Q14 | Retourner au moins : lancée, **retournée**, rejetée, annulée, terminée ; terminologie et correspondance fixées en conception | **Retour à une étape antérieure transmis comme « retournée »** (RST-20) ; **correspondance des statuts configurable** (RST-21) | 🆕 |
| Q15 | Seules les données nécessaires transférées ; nécessité et limitation de la collecte | **Lecture Salesforce limitée aux champs déclarés et aux sources du modèle** (RST-22) | 🆕 |
| Q16 | Environnements, comptes et jeux de données fixés au démarrage ; données fictives ou anonymisées | Bac à sable local disponible ; plan de recette | 📝 |
| Q17 | Respecter les limites d'API ; surveillance, journalisation, reprise ; 8 reprises au plus, intervalles croissants plafonnés à 1 h | Déjà le cas (`Relances.REPRISES_PAR_DEFAUT = 8`, recul 1, 2, 4… min plafonné à 1 h, alerte d'exploitation) | ✅ |
| Q18 | Hébergement au Québec ou ailleurs au Canada ; autre territoire seulement si protection équivalente et approbation d'IQ ; **préciser tous les lieux** (données, documents, preuves, journaux, sauvegardes, métadonnées, transit) | Tableau des lieux à joindre à la soumission (§ 4) | 📝 ⚠ |
| Q19 | Interfaces internes et externes **obligatoirement en français**, en anglais lorsqu'IQ le requiert ; livrables en français | Interfaces FR/EN déjà en place ; **langue de communication par signataire** réalisée (RST-25) | 🆕 📝 |
| Q20 | Un éditeur peut soumissionner seul | Aucun effet sur la solution | 📝 |

## 2. Ajustements réalisés

### RST-20 — « Retournée » transmise à Salesforce lors d'un retour à une étape antérieure (Q14) 🆕

**Écart.** Le retour vers l'initiateur publiait bien `RETOURNEE` vers Salesforce, mais le retour à une étape antérieure
désignée (scénario 9, réalisé avec la matrice de conformité) n'en publiait aucun : Salesforce restait sur « lancée ».

**Réalisation.** `ParcoursService.retournerAEtape` dépose un événement `RETOURNEE` dans la boîte d'envoi, avec le motif et
la clé de l'étape visée. La clé d'idempotence (trousse, événement, séquence du journal) garantit un seul envoi.

**Vérification.** `Addenda2IntegrationTest.le_retour_a_une_etape_anterieure_est_transmis_a_salesforce…` ; e2e « Q14 ».
Depuis l'interface : trousse d'origine Salesforce, parcours validation puis approbation ; l'approbateur choisit
**Retourner à → Validation** ; Intégrations API → journal : événement `RETOURNEE` de la trousse.

### RST-21 — Correspondance configurable des statuts retournés à Salesforce (Q14) 🆕

**Écart.** La valeur écrite dans `CLM_Statut__c` était le code interne (`LANCEE`, `SIGNATURE_TERMINEE`…). IQ fixera la
terminologie pendant la conception.

**Réalisation.** Nouvelle liste de référence **Statuts retournés à Salesforce** (`CLM_STATUT_SALESFORCE`) : le code est
l'événement, le libellé la valeur transmise. Valeurs de la plateforme : Lancée, Retournée, Rejetée, Annulée, Terminée.
L'organisation les adapte dans **Administration → Listes de référence**, sans développement. La valeur est figée dans
le message au moment de l'événement (`valeurStatut`), donc une relance republie exactement ce qui était prévu.

**Vérification.** `Addenda2ServicesTest.la_valeur_de_statut_suit_la_liste…`, `SalesforceRestTest.la_valeur_de_statut…`.

### RST-22 — Lecture Salesforce limitée aux champs nécessaires (Q15) 🆕

**Écart.** L'adaptateur relisait l'enregistrement `FundingRequest` en entier ; la consultation d'une demande renvoyait à
l'interface tous ses champs, y compris ceux qu'aucun modèle n'emploie.

**Réalisation.** Le port `PortSalesforce.lireDossier(identifiant, champs)` reçoit la liste des champs nécessaires :
`Id`, `Name`, les champs déclarés par l'organisation (liste **Champs Salesforce**) et les sources des champs du modèle.
L'adaptateur REST les demande nommément (`GET …/sobjects/FundingRequest/{id}?fields=…`) et écarte tout champ rendu sans
avoir été demandé. Un nom qui n'a pas la forme d'un nom d'API n'est jamais transmis. Si Salesforce refuse un champ
(`INVALID_FIELD`), l'utilisateur voit le champ fautif et la liste ou le modèle à corriger, au lieu d'une erreur technique.

**Conséquence à connaître.** Un champ Salesforce n'est lu que s'il figure dans la liste **Champs Salesforce** ou comme
source d'un champ de modèle. En recette sur l'organisation d'IQ, la liste doit reprendre les noms d'API réels
(ex. `RequestedAmount`, `Demandeur__c`).

**Vérification.** `SalesforceRestTest.la_lecture_demande_nommement…`, `…un_champ_declare_inconnu…`,
`Addenda2IntegrationTest.la_lecture_salesforce_se_limite…` ; e2e « Q15 ».

### RST-23 — Authentification Salesforce par JWT Bearer (Q11) 🆕

**Écart.** Seul le flux *client credentials* était réalisé ; IQ demande de décrire une méthode de remplacement,
notamment JWT Bearer, si son analyse de sécurité l'exige.

**Réalisation.** La connexion de l'organisation porte une **méthode d'authentification** : *client credentials* (défaut)
ou **JWT Bearer** (RFC 7523). En JWT Bearer, la plateforme signe une assertion RS256 (émetteur : identifiant client de
l'application connectée ; sujet : utilisateur d'exécution ; audience : origine du serveur de jetons ; validité 3 minutes)
avec la clé privée de l'organisation. Seul le certificat public est déposé chez Salesforce : aucun secret partagé ne
circule. La clé privée est chiffrée au repos, n'est jamais réaffichée, et n'apparaît dans aucun message d'erreur ni
journal. Migration `055-clm-salesforce-jwt`.

**Depuis l'interface.** Intégrations API → **Configurer la connexion** → Mode REST, Authentification **JWT Bearer** →
utilisateur d'exécution et clé privée (PEM PKCS#8).

**Prérequis chez IQ.** Application connectée avec certificat numérique, utilisateur d'exécution préautorisé
(*Admin approved users are pre-authorized*), profil limité à la lecture de `FundingRequest` et à l'écriture des trois
champs de retour.

**Vérification.** `SalesforceRestTest.le_jwt_bearer_presente_une_assertion_signee…` (signature vérifiée avec la clé
publique), `ConfigurationDomaineClmTest.le_jwt_bearer_exige…`, `Addenda2IntegrationTest.la_connexion_jwt_bearer…` ;
e2e « Q11 » ; bac à sable local avec `SF_JWT_CERTIFICAT` ([guide](../12-Bacs-a-sable/guide_bacs_a_sable.md) § 1.1).

### RST-24 — Exportation en lot vers un référentiel désigné par IQ (Q13) 🆕

**Écart.** Le dossier de preuve s'exportait trousse par trousse ; aucun moyen de transférer un ensemble de documents et
de métadonnées vers un référentiel d'IQ (SharePoint, GED, archives).

**Réalisation.** `GET /api/clm/admin/conservation/exportation?du=…&au=…` (administration seulement) produit une archive
ZIP des trousses closes sur la période, non détruites : le dossier de preuve autoportant de chacune (documents, `.p7s`,
certificat PAdES, journal, manifeste scellé) et un index `index.json` et `index.csv` (référence, titre, type, statut,
date de clôture, identifiant Salesforce, fichier, empreinte SHA-256) prêt à charger. 200 trousses au plus par lot.
Chaque trousse exportée reçoit `PREUVE_EXPORTEE` à son journal scellé ; l'exportation est tracée au journal d'audit.

**Depuis l'interface.** Administration → **Conservation et destruction** → bloc **Exportation vers un référentiel** →
période → **Télécharger le lot**.

**Vérification.** `Addenda2IntegrationTest.un_lot_de_trousses_closes_s_exporte…` ; e2e « Q13 ».

### RST-25 — Langue de communication du signataire (Q19) 🆕

**Écart.** Les interfaces étaient bilingues (sélecteur FR/EN, portail compris), mais l'invitation, les rappels et le
code de vérification envoyés aux signataires externes n'existaient qu'en français, et le lien ouvrait le portail dans
la langue du navigateur.

**Réalisation.** Chaque participant a une **langue de communication** (français par défaut, anglais sur demande).
En anglais : invitation, rappels, alertes d'échéance et code de vérification utilisent les textes anglais de la
plateforme, et le lien ouvre le portail en anglais (`?lang=en`), le signataire gardant le sélecteur. Un message
désactivé par l'organisation reste désactivé dans les deux langues. Migration `056-clm-participant-langue`.

**Limite.** Les messages personnalisés par l'organisation (écran Messages) sont rédigés en français ; leur version
anglaise personnalisée n'est pas encore éditable : la plateforme emploie son texte anglais.

**Vérification.** `LangueTest`, `Addenda2ServicesTest.un_signataire_anglophone…`, `…le_lien_du_portail…`,
`Addenda2IntegrationTest.un_signataire_externe_peut_communiquer_en_anglais…` ; e2e « Q19 ».

### Bac à sable Salesforce

Le serveur `infra/bacs-a-sable/salesforce/serveur.mjs` accepte désormais `fields=` (et refuse un champ inconnu par
`INVALID_FIELD`, comme Salesforce) et le flux JWT Bearer (`SF_JWT_CERTIFICAT` : clé publique PEM qui vérifie
l'assertion). Essai réalisé : assertion valide acceptée, assertion falsifiée refusée.

## 3. Déjà conforme, confirmé par l'addenda

| Q | Point | Où |
|---|---|---|
| Q7 | Lecture de `FundingRequest` par identifiant ou par `Name` ; correspondance par modèle | `SalesforceRest.lireDossier`, champ « source » des modèles |
| Q8 | Trois champs de retour, renommables | Intégrations API → Champs de retour |
| Q9 | API v60.0, modifiable | Intégrations API → Version de l'API |
| Q10 | REST à la demande ; `PATCH` idempotent avec `X-Correlation-Id` | Boîte d'envoi `clm_sortie` |
| Q12 | Document signé, preuve, piste d'audit et métadonnées dans la solution | Dossier de preuve, journal scellé |
| Q17 | 8 reprises au plus, intervalles croissants plafonnés à 1 h, surveillance et alerte | `Relances`, `AlertesExploitation.SALESFORCE_FILE_ERREUR` |

## 4. À refléter dans la soumission

| Q | Contenu à rédiger |
|---|---|
| Q1, Q2 | Ventilation base / options déjà arrêtée ([impact_addenda_1.md](impact_addenda_1.md) § 2.1) ; le scénario 10 démontré avec les options activées, en le signalant |
| Q4 | ~65 000 documents par an ≈ 260 par jour ouvrable ; hypothèse de pointe ×5 sur une heure ≈ 260 trousses par heure. Premier essai (2026-11-16, poste de développement, base H2, 20 initiateurs × 5 boucles) : 100 trousses créées, documentées et lancées en 62 s (≈ 5 800 par heure), 1 600 requêtes, 0 erreur, lecture P95 523 ms, écriture P95 70 ms : SLO respectés. À refaire sur l'infrastructure cible (PostgreSQL, Azure Canada) pour la soumission, avec le rapport `perf/results/rapport` et [SLI-SLO-SLA.md](../../../perf/SLI-SLO-SLA.md) |
| Q5 | Aucune limite technique par profil ; licences selon le bordereau |
| Q6 | Groupes, unités et rôles sans limite ni coût additionnel ; environnements distincts (DEV, préproduction, production) prévus ; coût d'un environnement supplémentaire à chiffrer |
| Q7 | Méthode (API REST), prérequis (application connectée, utilisateur d'exécution, trois champs de retour, liste des champs lus), limites (lecture à la demande, pas d'événement entrant), responsabilités (IQ : organisation Salesforce et droits ; fournisseur : adaptateur, correspondance, surveillance), erreurs et reprise (boîte d'envoi, 8 reprises, relance manuelle, alertes) |
| Q11 | Deux méthodes : *client credentials* et JWT Bearer, au choix de la sécurité d'IQ |
| Q13 | Conservation dans la solution, référence vers Salesforce, exportation en lot (ZIP + index) ; un connecteur SharePoint réel s'ajoute par le port de publication (ADR-002) si IQ le requiert |
| Q16 | Essais sur données fictives : bac à sable local, puis bac à sable d'IQ |
| Q18 | Tableau exhaustif des lieux : base de données, fichiers et sauvegardes (Azure Canada Centre et Canada Est), journaux applicatifs, horodatage, courriel et SMS (fournisseurs à confirmer, transit hors Canada possible), soutien (accès depuis le Canada) |
| Q19 | Interfaces FR obligatoires et EN sur demande, y compris le portail de signature et les messages aux signataires ; formation, guides et livrables en français |
| Q20 | Forme de la soumission (éditeur seul ou avec intégrateur) |

## 5. Points restants

| # | Point | Suite |
|---|---|---|
| ⚠ Q18 | Les fournisseurs de courriel, de SMS et d'horodatage ne sont pas encore désignés : un transit hors Canada reste possible | Choisir des fournisseurs hébergés au Canada ou le déclarer dans le tableau des lieux |
| ⚠ Q19 | Messages personnalisés par l'organisation : version anglaise non éditable | Ajouter la langue aux messages de l'écran Messages si IQ personnalise les textes anglais |
| Q14 | Terminologie et déclencheurs définitifs | Ajuster la liste « Statuts retournés à Salesforce » en conception |
| Q4 | Pics réels inconnus | Recalibrer le plan de charge dès que IQ fournit les volumes mensuels |
| A-08 | Saviynt ou SCIM : criticité toujours inconnue (Annexe 10) | Inchangé |

## 6. Comment vérifier

```bash
# Backend
cd backend && mvn -o test -Dtest='Addenda2IntegrationTest,Addenda2ServicesTest,LangueTest,SalesforceRestTest,ConfigurationDomaineClmTest'
# Interface (données : e2e/data/clm-exigences.json, section addenda2)
cd e2e && npx playwright test views/clm.exigences.spec.ts -g "Addenda 2"
# Charge CLM (backend lancé sur :8100)
cd perf && PLAN=clm-trousses-load.jmx BASE_PORT=8100 ./run.sh
```
