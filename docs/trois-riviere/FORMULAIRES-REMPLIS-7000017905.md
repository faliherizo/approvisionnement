---
title: Formulaires de soumission remplis — AO 7000017905
---

# Formulaires de soumission remplis et signés — Appel d'offres n° 7000017905

> **Soumissionnaire : VINA IT Solutions inc.** — Acquisition et implantation d'une solution d'aide à la
> rédaction et à la gestion des documents d'approvisionnement · Ville de Trois-Rivières.
> Document produit à jour de l'**Addenda n° 1** (17 septembre 2026), dont la prise en compte est
> confirmée. En cas de disparité avec le cahier des charges, **le cahier des charges prédomine** ; en cas
> de contradiction entre exigences, **l'exigence obligatoire prévaut**.

---

## Section II — Bordereau de soumission

Forfait annuel « tout inclus » : **licence de base 68 000 $/an** (licences pour l'ensemble des
utilisateurs, hébergement Azure — régions canadiennes, **haute disponibilité SLA 99,99 %** —, mises à
jour, soutien et maintenance). Conformément au devis, **tous les services sont inclus dans les prix
présentés, sans frais supplémentaires** : l'**implantation (100 000 $)** est donc **intégrée au forfait
de l'année 1**. Prix **ferme** sur 3 ans, puis **indexé de 5 %/an** sur les 7 années optionnelles.
Taxes : TPS 5 %, TVQ 9,975 %.

Détail de l'implantation incluse à l'année 1 (services d'onboarding spécifiques à la Ville, frais
ponctuels, exigence 17) : analyse/cadrage 12 000 $ · configuration (règles, rôles, circuits) 20 000 $ ·
modèles et clauses 22 000 $ · migration des données 12 000 $ · M365/intégrations (dont SSO Entra) 14 000 $ ·
formation 10 000 $ · mise en production 5 000 $ · gestion de projet 5 000 $ = **100 000 $**.

### Période ferme (3 ans)

| Art. | Année | Description | Prix unitaire | Montant |
|-----:|-------|-------------|--------------:|--------:|
| 1 | 2027 | Forfait annuel — récurrent 68 000 $ **+ implantation 100 000 $ (An 1)** | 168 000,00 $ | 168 000,00 $ |
| 2 | 2028 | Forfait annuel tout inclus (récurrent) | 68 000,00 $ | 68 000,00 $ |
| 3 | 2029 | Forfait annuel tout inclus (récurrent) | 68 000,00 $ | 68 000,00 $ |
| | | **Somme partielle — période ferme** | | **304 000,00 $** |
| | | TPS (5 %) | | 15 200,00 $ |
| | | TVQ (9,975 %) | | 30 324,00 $ |
| | | **SOMME GLOBALE DE LA PÉRIODE FERME** | | **349 524,00 $** |

### Sept (7) années optionnelles de renouvellement (indexation +5 %/an)

| Art. | Année | Description | Prix unitaire | Montant |
|-----:|-------|-------------|--------------:|--------:|
| 4 | 2030 | Forfait annuel tout inclus (option 1) | 71 400,00 $ | 71 400,00 $ |
| 5 | 2031 | Forfait annuel tout inclus (option 2) | 74 970,00 $ | 74 970,00 $ |
| 6 | 2032 | Forfait annuel tout inclus (option 3) | 78 718,50 $ | 78 718,50 $ |
| 7 | 2033 | Forfait annuel tout inclus (option 4) | 82 654,43 $ | 82 654,43 $ |
| 8 | 2034 | Forfait annuel tout inclus (option 5) | 86 787,15 $ | 86 787,15 $ |
| 9 | 2035 | Forfait annuel tout inclus (option 6) | 91 126,50 $ | 91 126,50 $ |
| 10 | 2036 | Forfait annuel tout inclus (option 7) | 95 682,83 $ | 95 682,83 $ |
| | | **Somme partielle — 7 options** | | **581 339,41 $** |
| | | TPS (5 %) | | 29 066,97 $ |
| | | TVQ (9,975 %) | | 57 988,61 $ |
| | | **SOMME GLOBALE DES 7 OPTIONS** | | **668 394,99 $** |

| Récapitulatif | Avant taxes | Toutes taxes comprises |
|---|---:|---:|
| Période ferme (3 ans, implantation incluse en An 1) | 304 000,00 $ | 349 524,00 $ |
| **SOMME GLOBALE (ferme + 7 options)** | **885 339,41 $** | **1 017 918,99 $** |

Je reconnais avoir lu toutes les clauses du présent appel d'offres et des documents auxquels il réfère,
**Addenda n° 1 compris**, et m'engage à fournir les biens et services conformément à ses exigences, aux
prix indiqués ci-dessus, lesquels comprennent l'ensemble des frais exigibles.

| Renseignement | Valeur |
|---|---|
| Nom du soumissionnaire (identique au NEQ) | **VINA IT Solutions inc.** |
| Numéro d'entreprise du Québec (NEQ) | 1181093866 |
| Adresse | 4028, rue de l'Étape, Québec (Québec) G1P 4H4 |
| Numéro de licence RBQ (si applicable) | s.o. |
| Téléphone | 418 264 8112 |
| Courriel | faliherizo@vinaitsolution.com |
| Responsable des commandes | Faliherizo RANDRIAMIRINDRA |
| Représentant autorisé | Faliherizo RANDRIAMIRINDRA |

**Signature du représentant autorisé :** ![Signature](signature-representant.jpeg)
**Date :** 22 septembre 2026

---

## Section III — Tableau de conformité (37 exigences)

Statuts : **O** = Obligatoire (respecté), **S** = Souhaitable (offert), **IR** = Information requise (décrite).

| N° | Exigence | Statut | Conformité VINA |
|---|---|---|---|
| 1 | Données hébergées au Canada | O | ✅ Hébergement **Microsoft Azure, régions canadiennes** (Canada Central/Est), résidence des données au Canada. |
| 2 | Générer des documents Word modifiables | O | ✅ Export **`.docx` modifiable** (moteur Apache POI, `DocxContractRenderer`). |
| 3 | Migrer/adapter les modèles Word existants | O | ✅ **Import de modèles `.docx`** existants, détection de sections, mise au format gabarit. |
| 4 | Compatible Microsoft 365 | O | ✅ Intégration **Microsoft Graph** réelle (SharePoint, OneDrive, Teams, Outlook). |
| 5 | Gestion sécurisée des accès + auth. corporative | O | ✅ Jeton `httpOnly`, RBAC ; **module de connecteur d'authentification — OIDC et SAML 2.0** (Microsoft Entra ID ou IdP gouvernemental) **configurable et activable par organisation** — les utilisateurs gouvernementaux du client s'authentifient via leur annuaire. |
| 6 | Authentification multifacteur (MFA) | O | ✅ **MFA** (TOTP et questions secrètes) — comptes de démo `totp@` et `question@levis.qc.ca` —, ou MFA corporatif via SSO. |
| 7 | Rôles, permissions et profils d'accès | O | ✅ **RBAC** (rédacteur, approbateur, juriste, admin, superadmin), isolation multi-tenant. |
| 8 | Chiffrement au repos et en transit | O | ✅ TLS en transit ; secrets **chiffrés (AES)** au repos ; PostgreSQL chiffré. |
| 9 | Journaux d'accès et d'activité | O | ✅ **Journal d'audit** complet (accès et actions), traçable et filtrable. |
| 10 | Configurable aux règles internes + LCOM | O | ✅ Circuits d'approbation, gabarits et règles **configurables** par la Ville. |
| 11 | Traçabilité versions/validations/approbations | O | ✅ **Versionnage**, historique des validations, approbations et modifications. |
| 12 | Conservation docs/versions/journaux | O | ✅ Conservation des documents, versions, journaux et historiques. |
| 13 | Séparation des responsabilités | O | ✅ Contrôle des accès/rôles soutenant la **séparation des responsabilités**. |
| 14 | Pas d'entraînement d'IA externe sans autorisation | O | ✅ **Aucun entraînement** de modèle externe ; IA locale (Ollama), données non transmises. |
| 15 | Validation humaine des contenus générés | O | ✅ Tout contenu IA est **proposé** et exige une **validation humaine** avant usage. |
| 16 | Méthode de migration détaillée | O | ✅ Méthode décrite (modèles, clauses, contenus) — voir mémoire §5 et import `.docx`. |
| 17 | Détailler coûts (ponctuels, récurrents, additionnels, sortie) | O | ✅ **Ponctuels** (implantation, An 1) et **récurrents** (licence annuelle) au bordereau + mémoire §10 ; **additionnels** : toute nouvelle demande **hors devis**, analysée et facturée à **120 $/h** (avant taxes) après accord écrit de la Ville, sans incidence sur les prix soumis ; **aucuns frais de sortie** (voir 36). |
| 18 | Soutien, formation, documentation en français | O | ✅ Solution, formation et documentation **entièrement en français**. |
| 19 | Exportation complète en fin de contrat | O | ✅ **Export complet** des données, documents, modèles et configuration. |
| 20 | Intégrations SharePoint/Teams/OneDrive/Outlook | S | ✅ **Offert** — publication réelle vers les quatre (Microsoft Graph). |
| 21 | Dossier d'approvisionnement | S | ✅ **Offert** — regroupement docs/versions/validations/historiques par dossier. |
| 22 | Rapports de suivi | S | ✅ **Offert** — documents générés, validations, utilisateurs, dossiers en cours. |
| 23 | Étapes de validation par type de document | S | ✅ **Offert** — circuits d'approbation paramétrables par type/processus. |
| 24 | Désactivation de l'IA | S | ✅ **Offert** — l'IA est activable/désactivable par tenant. |
| 25 | Références d'organisations similaires | S | ✅ Voir **Formule de qualification** (mandats publics : Ministère de la Sécurité publique via COFOMO, INSPQ via Randstad, Revenu Québec via COFOMO). |
| 26 | Environnement d'essai / pilote | S | ✅ **Offert** — démonstration en ligne <https://contrat.vinaitsolution.com/login> (comptes par rôle : `admin@`, `redacteur@`, `juriste@`, `direction@levis.qc.ca` — mot de passe `Passw0rd!`) + comptes de démo MFA (`totp@` TOTP, `question@levis.qc.ca` question secrète) ; vidéo <https://contrat.vinaitsolution.com/accueil#demonstration> ; projet pilote possible. |
| 27 | Évolutions prévues à court terme | S | ✅ Évolutions à court terme : **module SSO / OIDC gouvernemental** configurable et activable par organisation ; connecteurs vers les systèmes **financiers** et **SAP** ; **signature électronique** avancée ; tableaux de bord analytiques ; enrichissement du moteur de recherche. |
| 28 | Type de solution (infonuagique/locale/hybride) | IR | **Infonuagique** (SaaS), hébergée sur Microsoft Azure (régions canadiennes). |
| 29 | Connecteurs / interfaces / API + coûts | IR | **API REST documentée (OpenAPI / Swagger)** — interface Swagger : <https://contrat.vinaitsolution.com/swagger-ui/index.html> · spécification OpenAPI : <https://contrat.vinaitsolution.com/v3/api-docs> ; connecteurs Microsoft Graph (M365), SAP (OData) et **authentification SSO (OIDC / SAML 2.0)**, inclus au forfait ; connecteurs sur mesure additionnels cadrés avec la Ville, le cas échéant. |
| 30 | Notification et suivi des incidents de sécurité | IR | Procédure de notification, traitement et suivi ; journalisation et alertes. |
| 31 | Sous-traitants (hébergement, traitement, soutien) | IR | **Microsoft Azure** (hébergement) ; aucun autre sous-traitant de traitement des données. |
| 32 | Utilisation de l'IA — description | IR | IA **locale (Ollama)** d'assistance à la rédaction ; désactivable ; aucune donnée externe. |
| 33 | Volume inclus (modèles/clauses/contenus) | IR | Nombre illimité de modèles, clauses et documents dans le forfait de base. |
| 34 | Délais de réponse du soutien par priorité | IR | Soutien avec **engagements de niveau de service** (délais selon priorité — mémoire §8). |
| 35 | Formations incluses (utilisateurs et admins) | IR | Formation initiale par rôle (rédaction, juridique, approvisionnement) + admin, incluse An 1. |
| 36 | Frais de sortie / restitution | IR | **Aucuns frais de sortie** ; export complet fourni sans coût. |
| 37 | Limites connues | IR | Génération IA en français (locale) ; connecteurs sur mesure chiffrés après cadrage. |

---

## Section IV — Formule de qualification

### Volet 1 — Expérience de la firme (30 pts) — trois contrats comparables (5 dernières années)

VINA intervient sur les grands programmes numériques publics **en sous-traitance de l'entreprise
titulaire** du contrat public, en y portant l'**architecture de solution et l'architecture logicielle**.

| # | Mandat | Organisme public | Titulaire | Période | Personne-ressource |
|---|--------|-----------|-----------|---------|--------------------|
| 1 | Site institutionnel `sante.quebec` | Santé Québec | Ateko / Bell inc. | 2025–2026 (en vigueur) | fournie sur demande |
| 2 | MIA — modernisation des interfaces + intégration de l'authentification gouvernementale (SAG) | Ministère de la Sécurité publique (Québec) | COFOMO | 2025–2026 (en vigueur) | Khalil Elloumi, 418 271-3736 |
| 3 | SI-PMI / PANORAMA et RRSS — gestion des patients et des vaccins | INSPQ (Québec) | Randstad | 2021–2025 | Jerry Grzadka, 418 558-9203 |
| 4 | Registre des événements (RE) et Registre des entreprises (projet Agro) | Revenu Québec | COFOMO | 2018–2020 | Kalidou Wane, 581 995-1970 ; Charles Soares, 418 559-3726 |
| 5 | Microservices AccèsD + protection des renseignements personnels | Desjardins (institution financière) | — (mandat direct) | 2020–2021 | Mahamady Ouedraogo, 418 255-2946 |

*Les mandats **1 à 3** (récents et publics) sont inscrits aux **trois emplacements de la Formule de
qualification** ; les mandats **4** (Revenu Québec, 2018-2020) et **5** (Desjardins, 2020-2021) sont
fournis **en références complémentaires**.
Tous sont des **organismes publics québécois**, en implantation/modernisation de solutions de gestion et
d'authentification. VINA (Faliherizo RANDRIAMIRINDRA) y a porté l'**architecture de solution et
logicielle**, la **sécurité applicative** (OAuth 2.0 / OpenID Connect, SAML) et le **DevSecOps** sur
Microsoft Azure. Personnes-ressources contactables ; attestations des titulaires (Bell inc., COFOMO,
Randstad) fournies sur demande.*

### Volet 2 — Démonstration (40 pts) — transmise par enregistrement vidéo

Conformément à l'article 26, une **captation vidéo** de la solution **réellement proposée** est jointe.
Elle couvre : création et adaptation d'un modèle · clauses conditionnelles · génération du document ·
validation, collaboration (commentaires, résolution) et versions · recherche et exportation.

### Volet 3 — Valeur ajoutée (10 pts) — article 27

Éléments au-delà des exigences (décrits ci-dessous et **démontrés dans la vidéo**) : publication réelle
Microsoft 365 (SharePoint/Teams/OneDrive/Outlook), dossier d'approvisionnement consolidé, assistant IA
**local** désactivable (aucune donnée externe), multilingue (FR/EN), hébergement **Azure haute
disponibilité (SLA 99,99 %)**, réversibilité complète sans frais de sortie.

### Volet 4 — Prix (30 pts)

Total soumis (bordereau, ferme + 7 options ; implantation incluse en An 1) : **885 339,41 $ avant taxes
/ 1 017 918,99 $ toutes taxes comprises**. Pointage établi par le comité selon la formule de l'article.

---

## Attestation d'intégrité du soumissionnaire (article 39)

Je, soussigné, **Faliherizo RANDRIAMIRINDRA**, *représentant autorisé*, en présentant à la Ville de
Trois-Rivières la soumission ci-jointe, au nom de **VINA IT Solutions inc.** (le « Soumissionnaire »),
atteste que les déclarations ci-après sont complètes et exactes :

a) je suis autorisé par le Soumissionnaire à signer cette attestation et à présenter la soumission en son nom ;
b) toutes les personnes dont le nom apparaît sur la soumission ont été autorisées à en fixer les modalités et à la signer ;
c) je reconnais que la soumission sera non conforme et rejetée si l'une des déclarations est incomplète ou inexacte ;
d) je reconnais qu'en cas de collusion ou de déclaration de culpabilité en vertu de la *Loi fédérale sur la concurrence*, le contrat sera résilié et des poursuites pourront être intentées ;
e) je déclare qu'aucune activité de lobbyisme contrevenant à la *Loi sur la transparence et l'éthique en matière de lobbyisme* n'a été exercée ;
f) j'ai pris connaissance des exigences d'intégrité (art. 21.26, 21.26.1 et 21.28 de la *Loi sur les contrats des organismes publics*, chapitre C-65.1) et m'engage à les respecter pendant toute la durée du contrat ;
g) je sais que le contrat peut être résilié si une des affirmations n'est pas vraie ou complète ;
h) j'ai lu et je comprends le contenu de la présente attestation.

**Signature :** ![Signature](signature-representant.jpeg) **Date :** 22 septembre 2026

---

## Formule de résolution

Le Soumissionnaire étant une société dont **Faliherizo RANDRIAMIRINDRA** est l'administrateur et le
dirigeant autorisé, la présente tient lieu de **résolution interne** conformément à la note de la formule
(« peut être substituée par une résolution interne pourvu qu'elle comprenne l'ensemble des informations
demandées »).

Il est résolu que **Faliherizo RANDRIAMIRINDRA** soit autorisé, pour et au nom de **VINA IT Solutions
inc.**, à présenter et à signer une soumission à la Ville de Trois-Rivières, et le contrat le cas échéant,
ainsi que toute modification nécessaire, pour l'**Acquisition et implantation d'une solution d'aide à la
rédaction et à la gestion des documents d'approvisionnement — projet n° 7000017905**, en conformité avec
le document d'appel d'offres.

Copie certifiée conforme ce 22e jour du mois de septembre de l'année 2026.

**Faliherizo RANDRIAMIRINDRA**, administrateur (enregistré au REQ) — **Signature :** ![Signature](signature-representant.jpeg)

---

## Engagement de confidentialité (article 19)

Considérant que, en vertu de la *Loi sur les cités et villes* (RLRQ, c. C-19) et du *Règlement sur la
gestion des contrats de la Ville* (2019, chapitre 110), la Ville doit garder certaines informations
confidentielles ; considérant que, dans le cadre du projet **n° 7000017905**, le soussigné est susceptible
d'avoir accès à des éléments d'information de nature confidentielle ;

je soussigné, **Faliherizo RANDRIAMIRINDRA**, représentant dûment autorisé de **VINA IT Solutions inc.**,
m'engage à : 1° traiter l'information confidentielle conformément au présent engagement ; 2° la garder
secrète et ne pas la divulguer ; 3° mettre en œuvre les mesures appropriées pour en conserver le caractère
secret ; 4° ne pas l'exploiter hors des fins prévues ; 5°-6° respecter cet engagement pendant toute la
durée du contrat et **sans limite de durée** ensuite pour l'information relative au contrat et au processus
d'appel d'offres ; 7° à la fin du contrat, remettre au greffier tous les éléments confidentiels et n'en
conserver aucune reproduction ; 8° attester l'absence de lien d'affaires ou d'intérêt pécuniaire avec
d'autres soumissionnaires ; 9°-12° ne favoriser que les intérêts de la Ville et la concurrence ; 13°
reconnaître les sanctions applicables en cas de manquement.

En foi de quoi, j'ai signé en **double exemplaire** à Trois-Rivières (Québec), ce 22 septembre 2026.

**Le mandataire autorisé :** ![Signature](signature-representant.jpeg) **Faliherizo RANDRIAMIRINDRA**

**Témoin :** `______________________________` (nom et signature)

---

## Document de conformité — Office québécois de la langue française (OQLF)

VINA IT Solutions inc. compte **moins de 25 employés** ; l'obligation d'inscription et d'obtention d'un
certificat de francisation (art. 139 de la *Charte de la langue française*) ne s'applique pas. La solution,
la formation et la documentation sont **entièrement en français**. *(À remplacer par l'attestation
d'inscription/certificat de l'OQLF si l'entreprise y est assujettie.)*
