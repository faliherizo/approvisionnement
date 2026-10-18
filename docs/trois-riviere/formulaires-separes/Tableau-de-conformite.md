# Appel d'offres n° 7000017905 — VINA IT Solutions inc.

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
| 25 | Références d'organisations similaires | S | ✅ Voir **Formule de qualification** (mandats publics : Santé Québec, Revenu Québec, Sécurité publique via Cofomo, INSPQ via Randstad). |
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
