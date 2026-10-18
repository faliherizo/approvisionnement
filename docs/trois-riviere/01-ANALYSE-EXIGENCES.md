# Analyse de l'appel d'offres n° 7000017905 — Ville de Trois-Rivières

> **Objet** : *Acquisition et implantation d'une solution d'aide à la rédaction et à la gestion
> des documents d'approvisionnement.*
> **Type** : demande de prix / appel d'offres public (SEAO), évaluation qualité + prix.
> **Durée** : 3 ans fermes + 7 renouvellements optionnels de 1 an (max 10 ans).
> **Dépôt** : avant 11 h le **28 septembre 2026** (validité 90 jours). Contact : Sarra Boulahmi,
> `SBoulahmi@v3r.net`.
> **Source** : [cahier-des-charges.md](cahier-des-charges.md) · [formulaires-de-soumission.md](formulaires-de-soumission.md) ·
> [annexe-A-gestion-vulnerabilites.md](annexe-A-gestion-vulnerabilites.md) ·
> [reglement-gestion-contrats.md](reglement-gestion-contrats.md).

Ce document restitue **les besoins, les exigences fonctionnelles et techniques, et les exigences
imposées aux sociétés soumissionnaires**. La comparaison avec notre solution est dans
[02-COMPARAISON-ECARTS.md](02-COMPARAISON-ECARTS.md) ; la conception d'intégration dans
[03-CONCEPTION-INTEGRATION.md](03-CONCEPTION-INTEGRATION.md).

---

## 1. Besoin d'affaires (art. 1, 4, 5)

La Direction de l'approvisionnement produit un fort volume de documents (appels d'offres, demandes
de prix, devis administratifs et techniques, addendas, contrats, lettres d'invitation, analyses de
conformité, avis, formulaires). Elle veut une **solution logicielle moderne** pour :

- **automatiser** la production de ces documents à partir de modèles et de contenus réutilisables ;
- **centraliser** la gestion des modèles et des clauses (bibliothèque commune) ;
- **standardiser** pour la conformité aux lois (LCOM), règlements et politiques de la Ville ;
- **collaborer** (révision, validation, suivi des modifications) ;
- **s'intégrer** à l'écosystème Ville : **Microsoft 365, Word, SharePoint, Teams, OneDrive,
  Outlook, SAP**, GED et authentification corporative ;
- assurer **sécurité, protection des renseignements, gouvernance documentaire, traçabilité,
  réversibilité**, évolutivité et facilité d'usage.

**Portée** (art. 5) : rédaction des documents d'approvisionnement **+** révision, validation,
approbation, publication, archivage et **recherche documentaire**.
**Profils utilisateurs** (art. 6) : conseillers/professionnels de l'approvisionnement,
gestionnaires de dossiers d'acquisition, personnes autorisées à préparer/réviser.

---

## 2. Exigences fonctionnelles (art. 7)

| Réf | Exigence | Nature |
|-----|----------|--------|
| 7.1 | **Gestion des modèles documentaires** : créer/modifier/gérer des modèles personnalisés, **classés par catégorie** (type de document, type d'acquisition) | Obligatoire |
| 7.2 | **Génération automatisée** : générer depuis un modèle, **intégration auto** des sections/clauses/contenus selon des **paramètres préétablis** ; **parcours guidé / questionnaire / assistant** (souhaitable) ; export **Word modifiable + PDF** | Obligatoire (assistant = souhaitable) |
| 7.3 | **Contenus réutilisables** : bibliothèque centralisée (clauses, textes, paragraphes, sections, formulaires) ; **mise à jour centralisée + diffusion des nouvelles versions** ; **droits** sur ajout/modif/approbation/utilisation | Obligatoire |
| 7.4 | **Gestion documentaire** : **historique des versions** (auteur, date, nature du changement) ; **recherche** par titre, mots-clés, catégorie, type, contenu | Obligatoire |
| 7.5 | **Collaboration et validation** : révision collaborative + suivi des contributions ; **commentaires/annotations** (ajout, consultation, **résolution**) ; **workflows de validation/approbation configurables** par rôle, type de document ou type de dossier | Obligatoire |

---

## 3. Exigences techniques, environnement et intégrations (art. 8, 9)

| Réf | Exigence |
|-----|----------|
| 8.1 | Interface conviviale, **en français** ; gestion simple des utilisateurs (ajout/modif/désactivation/accès) |
| 8.2 | Mécanismes de sécurité (détaillés art. 14–19) |
| 9 | **Compatibilité M365** (Word, SharePoint, Teams, OneDrive, Outlook), **GED**, **SAP** (systèmes financiers/approvisionnement), **authentification corporative** ; intégrations par **connecteurs / API / développements**, avec limitations et coûts identifiés |

---

## 4. Sécurité, conformité, données, IA, migration (art. 14–22)

| Réf | Exigence |
|-----|----------|
| 14 | Sécurité de l'information, confidentialité, protection des RP, conservation, traçabilité — **preuves** (doc technique, politiques, **certifications**) |
| 15 | **Conformité municipale et gestion contractuelle** : configurable aux règles internes ; **traçabilité** versions/validations/approbations/modifications ; conservation (docs, versions, journaux, historiques) ; **séparation des responsabilités** (rôles/permissions) ; **regroupement par dossier d'approvisionnement** ; **rapports de suivi** ; **étapes de validation selon type de document/processus** |
| 16 | Disponibilité/intégrité/confidentialité ; **sauvegarde, DR, chiffrement au repos et en transit**, gestion des vulnérabilités, surveillance, gestion des incidents ; **ISO 27001 / SOC 2 Type II**, **RTO/RPO** |
| 17 | **SSO, MFA**, gestion des rôles/permissions, standards d'authentification |
| 18 | **Journalisation** : connexions, modifications, validations, approbations, **exportations, suppressions, changements de droits** ; journaux **consultables et exportables** |
| 19 | Accès restreint aux autorisés ; gestion des sous-traitants ; **notification d'incident** ; conservation/destruction sécuritaire ; **données non utilisées pour entraîner une IA sans autorisation écrite** |
| 20 | **Propriété exclusive à la Ville** ; **réversibilité** : export en formats **ouverts, documentés, exploitables** de *toutes* les données (modèles, clauses, contenus, docs, métadonnées, journaux, config, profils, rôles) ; restitution + destruction en fin de contrat |
| 21 | **IA / automatisation intelligente** : clairement identifiable ; fonctionnement, données, modèles, limites, **contrôles**, **validation humaine**, **traçabilité des suggestions**, **option de désactivation** documentés ; pas d'entraînement externe sans autorisation |
| 22 | **Migration des contenus existants** : méthode structurée (formats, volume inclus, responsabilités, nettoyage/normalisation, validations, **migration d'essai** avant production) |

---

## 5. Implantation, formation, soutien, gouvernance, accessibilité (art. 10–12, 23–25)

| Réf | Exigence |
|-----|----------|
| 10 | Démarche : **analyse → paramétrage → migration → formation → mise en production** |
| 11 | Formation utilisateurs principaux + administrateurs, **documentation FR**, soutien au démarrage |
| 12 | **Soutien et maintenance** (heures, délais de prise en charge/résolution, priorités, escalade, maintenance planifiée, MàJ, communication d'incident) |
| 23 | **Gouvernance** : chargé de projet, calendrier, ressources, responsabilités, livrables, suivi (avancement, risques, décisions) |
| 24 | **Acceptation des livrables** conditionnelle (config, modèles, contenus, migration d'essai, formations, doc, mise en production) |
| 25 | **Accessibilité** (limitations fonctionnelles), **français**, navigateurs courants, documentation claire |

> **Accessibilité** : aligne avec notre exigence permanente **SGQRI 008 3.0 / WCAG 2.2 AA**.

---

## 6. Exigences pour les sociétés soumissionnaires

### 6.1 Qualification et expérience (art. 13)
- Expérience dans des **organisations similaires** ; **références clients** comparables, idéalement
  **secteur municipal/public** ; expérience d'**implantation** de solutions comparables.

### 6.2 Contenu de la soumission (formulaires)
- **Bordereau de soumission** : forfait annuel **Année 1–3 (fermes)** + **7 années optionnelles**
  (prix unitaire + montant, TPS 5 %, TVQ 9,975 %) ;
- **Formule de qualification** ;
- **Attestation d'intégrité du soumissionnaire** (art. 39 ; LCOP c. C-65.1 — 21.26/21.28) ;
- **Formule de résolution** (autorisation du signataire) ;
- **Engagement de confidentialité** ;
- **Conformité OQLF** (Charte de la langue française) ;
- Dépôt **via SEAO** (chiffré, horodaté).

### 6.3 Évaluation (art. 7 avis, art. 26)
- **Comité de sélection** : pointage par critère sur les seuls renseignements de la soumission ;
- **Scénario de démonstration obligatoire** (art. 26), transmis en **vidéo**, couvrant :
  création/adaptation d'un modèle → ajout de clauses réutilisables → **règles conditionnelles** →
  génération d'un document → modification d'une version → collaboration → **validation** →
  **exportation** → **recherche** d'un contenu. Les fonctionnalités futures ne comptent pas.
- **Valeur ajoutée** (art. 27) : connecteurs M365/SAP, automatisation avancée, assistants guidés,
  règles conditionnelles avancées, contrôles qualité, alertes, IA encadrée, environnement d'essai,
  feuille de route, **agents IA**, etc.

### 6.4 Obligations de sécurité — gestion des vulnérabilités (Annexe A)
- **CVSS v3.1/v4.0** pour la priorisation ; correctifs de sécurité maintenus à jour ;
- **balayage de vulnérabilités** avant mise en service (aucune critique/élevée non corrigée) et
  **régulier** en cours de contrat ; **coopération** aux balayages de la Ville ;
- mesures compensatoires temporaires (segmentation, WAF, IPS/EDR, MFA/rotation de secrets…) ;
- notification et suivi des vulnérabilités/incidents, y compris sur la **chaîne d'approvisionnement**
  (sous-traitants).

### 6.5 Cadre contractuel
- **Services hébergés** (clauses techniques générales) : modèle infonuagique, **disponibilité du
  service** mesurée mensuellement, incidents, protection des données.
- **Règlement sur la gestion des contrats** de la Ville : transparence, rotation, intégrité,
  gestion des contrats — la solution doit **soutenir** ces règles (art. 15).
- **Loi applicable** : Québec ; **français** (Charte) sur tous les documents et communications.
