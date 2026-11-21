# Scénarios BDD — Santé Québec — CHU, avis d'intention

*Plateforme de formulaires et processus configurables.* Source des exigences : `docs/sante-quebec-chu/08-CONFORMITE-AVIS-INTENTION.md`.

> Document généré par `bdd/outils/documenter.mjs` depuis le catalogue, les fonctionnalités Gherkin et les cas JSON ;
> ne pas le modifier à la main. Régénérer : `cd bdd && npm run documenter`.

## Synthèse

| Exigences | Éprouvées par scénario | Dont réussies à la dernière exécution | Documentaires | Écarts déclarés | Sans objet |
|---|---|---|---|---|---|
| 60 | 50 | 50 | 9 | 0 | 1 |

## Matrice des exigences

| Exigence | Libellé | Caractère | Vérification | Scénarios |
|---|---|---|---|---|
| `CHU-1.01` | Infonuagique, environnement dédié, accès en tout temps, sans limite d'utilisateurs | Besoin exprimé | Scénario BDD exécuté | ✅ [Comptes sans limite de nombre, rôles distincts et politique de mot de passe](#comptes-sans-limite-de-nombre-roles-distincts-et-politique-de-mot-de-passe) |
| `CHU-1.02` | Hébergement des RP au Québec | Besoin exprimé | Documentaire | Hébergement au Québec : région canadaeast (option B de l'ADR-020). |
| `CHU-1.03` | Cloisonnement données/formulaires entre environnements et clients | Besoin exprimé | Scénario BDD exécuté | ✅ [Les formulaires du CHU sont invisibles pour les autres clients](#les-formulaires-du-chu-sont-invisibles-pour-les-autres-clients) |
| `CHU-1.04` | Environnements PROD / DEV / acceptation (UAT) | Besoin exprimé | Scénario BDD exécuté | ✅ [Applications par domaine dans les environnements DEV, UAT et PROD](#applications-par-domaine-dans-les-environnements-dev-uat-et-prod) |
| `CHU-1.05` | Disponibilité 99,8 % en tout temps | Besoin exprimé | Documentaire | Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). |
| `CHU-1.06` | Sauvegardes redondantes locales+externes quotidiennes | Besoin exprimé | Documentaire | Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). |
| `CHU-1.07` | Hébergement, entretien, correctifs, MAJ, migrations | Besoin exprimé | Documentaire | Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). |
| `CHU-1.08` | Gestion durée des sessions + affichage ordi/tablette/mobile | Besoin exprimé | Scénario BDD exécuté | ✅ [Affichage sur téléphone et tablette, accessible et sans débordement](#affichage-sur-telephone-et-tablette-accessible-et-sans-debordement) |
| `CHU-2.01` | Interface d'administration FR/EN | Besoin exprimé | Scénario BDD exécuté | ✅ [L'interface d'administration s'offre en français et en anglais](#linterface-dadministration-soffre-en-francais-et-en-anglais) |
| `CHU-2.02` | Formulaires multilingues (contenu) | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-2.03` | Gabarits, images, feuilles de style institutionnels | Besoin exprimé | Scénario BDD exécuté | ✅ [L'organisation porte son image institutionnelle](#lorganisation-porte-son-image-institutionnelle) |
| `CHU-2.04` | Accessibilité sites Web gouv. QC (SGQRI 008 / WCAG 2.2 AA) | Besoin exprimé | Scénario BDD exécuté | ✅ [Affichage sur téléphone et tablette, accessible et sans débordement](#affichage-sur-telephone-et-tablette-accessible-et-sans-debordement) |
| `CHU-3.01` | Formulaires à une ou plusieurs pages | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-3.02` | Tous les types de champs (texte, zone, cases, radio, liste, date, hyperlien, courriel, nombre, décimale, montant, pourcentage, fichier, matrice, valeurs dynamiques, calculées) | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-3.03` | Aperçu des réponses avant soumission | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-3.04` | Sauvegarde automatique + reprise ultérieure | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-3.05` | Activation/désactivation selon date, nb max de répondants, plage réseau | Besoin exprimé | Scénario BDD exécuté | ✅ [L'ouverture dépend de la date, du nombre de répondants, du réseau et de la vérification anti-robot](#louverture-depend-de-la-date-du-nombre-de-repondants-du-reseau-et-de-la-verification-anti-robot) |
| `CHU-3.06` | Restriction de choix selon capacité maximale | Besoin exprimé | Scénario BDD exécuté | ✅ [Un choix dont la capacité est atteinte n'est plus proposé ni accepté](#un-choix-dont-la-capacite-est-atteinte-nest-plus-propose-ni-accepte) |
| `CHU-3.07` | Mécanisme anti-automatisation (formulaires publics) | Besoin exprimé | Scénario BDD exécuté | ✅ [L'ouverture dépend de la date, du nombre de répondants, du réseau et de la vérification anti-robot](#louverture-depend-de-la-date-du-nombre-de-repondants-du-reseau-et-de-la-verification-anti-robot) |
| `CHU-3.08` | Formulaires liés parent-enfant | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-3.09` | Versions, approbation des modifs, retour à une version | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-3.10` | Duplication / export / réimport de structure | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-4.01` | Afficher/masquer/modifier des champs selon les réponses | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-4.02` | Obligatoire/facultatif selon le contexte | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-4.03` | Validation croisée + format | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-4.04` | Calcul automatique (formules) | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-4.05` | Routage des demandes selon règles | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-4.06` | Déclenchement d'événements selon données / modif de fiche | Besoin exprimé | Scénario BDD exécuté | ✅ [La soumission d'une fiche déclenche une notification par courriel, tracée au journal des envois](#la-soumission-dune-fiche-declenche-une-notification-par-courriel-tracee-au-journal-des-envois) |
| `CHU-4.07` | Processus structurés : tâches, étapes d'approbation, responsabilités | Besoin exprimé | Scénario BDD exécuté | ✅ [Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel](#une-fiche-est-prise-en-charge-completee-commentee-documentee-validee-et-cloturee-synthese-et-export-excel)<br>✅ [Un circuit d'approbation multi-niveaux s'applique selon le montant](#un-circuit-dapprobation-multi-niveaux-sapplique-selon-le-montant) |
| `CHU-4.08` | Gestion d'inventaire / capacité (inscriptions à des activités) | Besoin exprimé | Scénario BDD exécuté | ✅ [L'inventaire des inscriptions par activité se consulte et s'exporte](#linventaire-des-inscriptions-par-activite-se-consulte-et-sexporte) |
| `CHU-5.01` | Notifications courriel / SMS / interne selon déclencheur | Besoin exprimé | Scénario BDD exécuté | ✅ [La soumission d'une fiche déclenche une notification par courriel, tracée au journal des envois](#la-soumission-dune-fiche-declenche-une-notification-par-courriel-tracee-au-journal-des-envois) |
| `CHU-5.02` | Confirmation à la soumission | Besoin exprimé | Scénario BDD exécuté | ✅ [Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet](#les-regles-masquent-rendent-obligatoire-valident-calculent-et-acheminent-la-fiche-se-reprend-puis-se-soumet) |
| `CHU-5.03` | Notification aux administrateurs à la création/modif d'une fiche | Besoin exprimé | Scénario BDD exécuté | ✅ [La soumission d'une fiche déclenche une notification par courriel, tracée au journal des envois](#la-soumission-dune-fiche-declenche-une-notification-par-courriel-tracee-au-journal-des-envois) |
| `CHU-5.04` | Production d'un document à partir des données + transmission | Besoin exprimé | Scénario BDD exécuté | ✅ [Un document PDF se produit depuis les données d'une fiche, puis se transmet](#un-document-pdf-se-produit-depuis-les-donnees-dune-fiche-puis-se-transmet) |
| `CHU-5.05` | Messagerie interne sécurisée | Besoin exprimé | Scénario BDD exécuté | ✅ [Les intervenants échangent par une messagerie interne sécurisée](#les-intervenants-echangent-par-une-messagerie-interne-securisee) |
| `CHU-6.01` | Recherche multicritère + critères sauvegardés | Besoin exprimé | Scénario BDD exécuté | ✅ [Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel](#une-fiche-est-prise-en-charge-completee-commentee-documentee-validee-et-cloturee-synthese-et-export-excel) |
| `CHU-6.02` | Export Excel (toutes/sélection) | Besoin exprimé | Scénario BDD exécuté | ✅ [Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel](#une-fiche-est-prise-en-charge-completee-commentee-documentee-validee-et-cloturee-synthese-et-export-excel) |
| `CHU-6.03` | Rapports PDF personnalisés | Besoin exprimé | Scénario BDD exécuté | ✅ [Un rapport PDF personnalisé se produit sur les fiches d'un formulaire](#un-rapport-pdf-personnalise-se-produit-sur-les-fiches-dun-formulaire) |
| `CHU-6.04` | Graphiques + tableaux de bord configurables | Besoin exprimé | Scénario BDD exécuté | ✅ [Des tableaux de bord et graphiques se configurent et se partagent](#des-tableaux-de-bord-et-graphiques-se-configurent-et-se-partagent) |
| `CHU-6.05` | Pages listes/dashboards/rapports selon les droits | Besoin exprimé | Scénario BDD exécuté | ✅ [Comptes sans limite de nombre, rôles distincts et politique de mot de passe](#comptes-sans-limite-de-nombre-roles-distincts-et-politique-de-mot-de-passe) |
| `CHU-6.06` | Export intégral structuré (réversibilité) | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-6.07` | Durée de conservation des données | Besoin exprimé | Scénario BDD exécuté | ✅ [Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté](#un-formulaire-multipage-bilingue-avec-tous-les-types-de-champs-versionne-duplique-exporte-et-reimporte) |
| `CHU-7.01` | SSO identité org + méthodes normalisées | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-7.02` | 2FA accès admin + politique de mots de passe | Besoin exprimé | Scénario BDD exécuté | ✅ [Second facteur exigé et vérifié (question secrète, TOTP RFC 6238)](#second-facteur-exige-et-verifie-question-secrete-totp-rfc-6238)<br>✅ [Comptes sans limite de nombre, rôles distincts et politique de mot de passe](#comptes-sans-limite-de-nombre-roles-distincts-et-politique-de-mot-de-passe) |
| `CHU-7.03` | Groupes avec droits lecture/modif/suppression | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-7.04` | Contrôle d'accès par utilisateur/groupe/rôle/formulaire/champ/page/fiche/bouton | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-7.05` | Classification public/interne/confidentiel/secret | Besoin exprimé | Scénario BDD exécuté | ✅ [Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel](#une-fiche-est-prise-en-charge-completee-commentee-documentee-validee-et-cloturee-synthese-et-export-excel) |
| `CHU-7.06` | Mesures renforcées (masquage, chiffrement, restriction affichage/export) | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-7.07` | Traçabilité accès et modifications | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-7.08` | Protection DDoS / brute-force / maliciels | Besoin exprimé | Documentaire | Limiteur de débit applicatif (PareFeuFilter) ; protection DDoS et pare-feu applicatif assurés par l'infrastructure (Front Door, WAF). |
| `CHU-7.09` | Config pour obligations Loi 25 | Besoin exprimé | Scénario BDD exécuté | ✅ [Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable](#groupes-et-droits-fins-nas-masque-loi-25-journal-des-acces-authentification-unique-configurable) |
| `CHU-8.01` | API consulter/récupérer/créer/compléter des fiches | Besoin exprimé | Scénario BDD exécuté | ✅ [Un système externe crée une fiche par l'API publique avec un jeton OAuth](#un-systeme-externe-cree-une-fiche-par-lapi-publique-avec-un-jeton-oauth) |
| `CHU-8.02` | Auth API par jeton / OAuth | Besoin exprimé | Scénario BDD exécuté | ✅ [Un système externe crée une fiche par l'API publique avec un jeton OAuth](#un-systeme-externe-cree-une-fiche-par-lapi-publique-avec-un-jeton-oauth) |
| `CHU-8.03` | Échanges formulaires/données/sites/systèmes externes | Besoin exprimé | Scénario BDD exécuté | ✅ [Un système externe crée une fiche par l'API publique avec un jeton OAuth](#un-systeme-externe-cree-une-fiche-par-lapi-publique-avec-un-jeton-oauth) |
| `CHU-8.04` | Maintien des échanges déjà en place | Besoin exprimé | Sans objet | n/a — nouvelle implémentation (pas de legacy chez nous) |
| `CHU-9.01` | Licences 2 secteurs (RH + Appro) + 4 apps Web | Besoin exprimé | Documentaire | Licences : modèle commercial (domaines RH et approvisionnement, applications). |
| `CHU-9.02` | Soutien 2e ligne L-V 8 h–17 h | Besoin exprimé | Documentaire | n/a (service) — offre de service |
| `CHU-9.03` | Tests applicatifs lors migrations/MAJ | Besoin exprimé | Documentaire | Démontré par cette suite BDD et les e2e Playwright, rejoués à chaque demande de fusion. |
| `CHU-9.04` | Test d'intrusion du portail RH (firme externe) | Besoin exprimé | Documentaire | n/a (service) — à planifier |
| `CHU-9.05` | Banque d'heures (analyse/formation/config/prog/rapports) | Besoin exprimé | Scénario BDD exécuté | ✅ [Une banque d'heures de services professionnels se tient et se suit](#une-banque-dheures-de-services-professionnels-se-tient-et-se-suit) |

## Exigences non éprouvées par un scénario

- `CHU-1.02` — Hébergement des RP au Québec : **Documentaire.** Hébergement au Québec : région canadaeast (option B de l'ADR-020). Référence : `docs/adr/ADR-020-region-hebergement-et-niveaux-de-service.md`.
- `CHU-1.05` — Disponibilité 99,8 % en tout temps : **Documentaire.** Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). Référence : `infra/dr/README.md`.
- `CHU-1.06` — Sauvegardes redondantes locales+externes quotidiennes : **Documentaire.** Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). Référence : `infra/dr/README.md`.
- `CHU-1.07` — Hébergement, entretien, correctifs, MAJ, migrations : **Documentaire.** Engagement d'infrastructure et de service (disponibilité, sauvegardes, exploitation). Référence : `infra/dr/README.md`.
- `CHU-7.08` — Protection DDoS / brute-force / maliciels : **Documentaire.** Limiteur de débit applicatif (PareFeuFilter) ; protection DDoS et pare-feu applicatif assurés par l'infrastructure (Front Door, WAF). Référence : `infra/terraform/azure`.
- `CHU-8.04` — Maintien des échanges déjà en place : **Sans objet.** n/a — nouvelle implémentation (pas de legacy chez nous)
- `CHU-9.01` — Licences 2 secteurs (RH + Appro) + 4 apps Web : **Documentaire.** Licences : modèle commercial (domaines RH et approvisionnement, applications).
- `CHU-9.02` — Soutien 2e ligne L-V 8 h–17 h : **Documentaire.** n/a (service) — offre de service
- `CHU-9.03` — Tests applicatifs lors migrations/MAJ : **Documentaire.** Démontré par cette suite BDD et les e2e Playwright, rejoués à chaque demande de fusion. Référence : `bdd/ ; e2e/ ; infra/cicd`.
- `CHU-9.04` — Test d'intrusion du portail RH (firme externe) : **Documentaire.** n/a (service) — à planifier

## Scénarios détaillés

### Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4)


#### Un formulaire multipage, bilingue, avec tous les types de champs, versionné, dupliqué, exporté et réimporté

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4) (`bdd/features/chu/01-formulaires.feature`)
- **Exigences éprouvées :** `CHU-3.01`, `CHU-3.02`, `CHU-3.08`, `CHU-3.09`, `CHU-3.10`, `CHU-2.02`, `CHU-6.07`, `CHU-6.06`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.formulaire.conception"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — formulaires"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.formulaire.conception`** — Formulaire multipage avec tous les types de champs et libellés bilingues ; versions approuvées, retour à une version ; duplication, export et réimport ; conservation ; formulaire enfant *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `GET /api/formulaires/${formulaire}/version` |  | 2xx | `$.structureJson` contient "MATRICE" ; `$.structureJson` contient "VALEURS_DYNAMIQUES" ; `$.structureJson` contient "FICHIER" ; `$.structureJson` contient "CALCULE" ; `$.structureJson` contient "Name" |
| 6 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | `$.numero` egal 2 |
| 7 | `POST /api/formulaires/${formulaire}/revenir/1` |  | 2xx | `$.structureJson` contient "Identité" |
| 8 | `GET /api/formulaires/${formulaire}/historique` |  | 2xx | `$` longueurMin 2 ; `$[*].statut` inclut "PUBLIE" |
| 9 | `POST /api/formulaires/${formulaire}/dupliquer?code=COPIE_${UNIQUE}&nom=Copie%20${unique}` |  | 2xx | `$.code` egal "COPIE_${UNIQUE}" |
| 10 | `GET /api/formulaires/${formulaire}/export` |  | 2xx | `$.pages` longueurMin 2 ; capture `structureExportee` ← `$` |
| 11 | `POST /api/formulaires/importer?code=IMPORT_${UNIQUE}&nom=Import%20${unique}` |  | 2xx | `$.code` egal "IMPORT_${UNIQUE}" |
| 12 | `POST /api/formulaires` |  | 2xx | `$.parentId` egalTexte "${formulaire}" |
| 13 | `GET /api/formulaires` |  | 2xx | `$[*].conservationJours` inclut 365 |

#### Les règles masquent, rendent obligatoire, valident, calculent et acheminent ; la fiche se reprend puis se soumet

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4) (`bdd/features/chu/01-formulaires.feature`)
- **Exigences éprouvées :** `CHU-3.03`, `CHU-3.04`, `CHU-4.01`, `CHU-4.02`, `CHU-4.03`, `CHU-4.04`, `CHU-4.05`, `CHU-5.02`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.saisie.regles"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — mes demandes"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.saisie.regles`** — Remplissage : sauvegarde automatique et reprise, règles d'affichage, d'obligation, de validation, de calcul et de routage ; refus motivé puis soumission confirmée *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `fiche` ← `$.id` ; capture `ficheNumero` ← `$.numero` |
| 6 | `PATCH /api/formulaires/${formulaire}/saisies/${fiche}` |  | 2xx | `$.donneesJson` contient "Alice Roy" |
| 7 | `GET /api/formulaires/${formulaire}/saisies/${fiche}` |  | 2xx | `$.donneesJson` contient "70" ; `$.statut` egal "BROUILLON" |
| 8 | `POST /api/formulaires/${formulaire}/regles/evaluer` |  | 2xx | `$.calculs.total` egal 1000 ; `$.routage` inclut "GESTIONNAIRE" ; `$.champsMasques` inclut "grille" ; `$.champsObligatoires` inclut "piece" |
| 9 | `POST /api/formulaires/${formulaire}/regles/evaluer` |  | 2xx | `$.erreurs` inclut "60 heures au plus" ; `$.champsMasques` exclut "grille" |
| 10 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 400 | contient « 60 heures au plus » |
| 11 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 2xx | `$.statut` egal "SOUMIS" ; `$.soumisLe` existe true ; `$.numero` contient "RH-" |
| 12 | `GET /api/mes-demandes` |  | 2xx | `$[*].numero` inclut "${ficheNumero}" |

#### L'ouverture dépend de la date, du nombre de répondants, du réseau et de la vérification anti-robot

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4) (`bdd/features/chu/01-formulaires.feature`)
- **Exigences éprouvées :** `CHU-3.05`, `CHU-3.07`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.activation"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.activation`** — Ouverture selon la date, le nombre maximal de répondants, la plage réseau et la vérification anti-robot *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `fMax` ← `$.id` |
| 2 | `PUT /api/formulaires/${fMax}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${fMax}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${fMax}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `POST /api/formulaires/${fMax}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `f1` ← `$.id` ; capture `f1Numero` ← `$.numero` |
| 6 | `POST /api/formulaires/${fMax}/saisies/${f1}/soumettre` |  | 2xx | — |
| 7 | `POST /api/formulaires/${fMax}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `f2` ← `$.id` ; capture `f2Numero` ← `$.numero` |
| 8 | `POST /api/formulaires/${fMax}/saisies/${f2}/soumettre` |  | 409 | contient « capacité maximale est atteinte » |
| 9 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `fFerme` ← `$.id` |
| 10 | `PUT /api/formulaires/${fFerme}/structure` |  | 2xx | — |
| 11 | `POST /api/formulaires/${fFerme}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 12 | `POST /api/formulaires/${fFerme}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 13 | `POST /api/formulaires/${fFerme}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `f3` ← `$.id` ; capture `f3Numero` ← `$.numero` |
| 14 | `POST /api/formulaires/${fFerme}/saisies/${f3}/soumettre` |  | 409 | contient « fermé » |
| 15 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `fReseau` ← `$.id` |
| 16 | `PUT /api/formulaires/${fReseau}/structure` |  | 2xx | — |
| 17 | `POST /api/formulaires/${fReseau}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 18 | `POST /api/formulaires/${fReseau}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 19 | `POST /api/formulaires/${fReseau}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `f4` ← `$.id` ; capture `f4Numero` ← `$.numero` |
| 20 | `POST /api/formulaires/${fReseau}/saisies/${f4}/soumettre` |  | 409 | contient « réseau non autorisé » |
| 21 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `fRobot` ← `$.id` |
| 22 | `PUT /api/formulaires/${fRobot}/structure` |  | 2xx | — |
| 23 | `POST /api/formulaires/${fRobot}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 24 | `POST /api/formulaires/${fRobot}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 25 | `POST /api/formulaires/${fRobot}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `f5` ← `$.id` ; capture `f5Numero` ← `$.numero` |
| 26 | `POST /api/formulaires/${fRobot}/saisies/${f5}/soumettre` |  | 400 | contient « anti-robot » |

#### Un choix dont la capacité est atteinte n'est plus proposé ni accepté

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4) (`bdd/features/chu/01-formulaires.feature`)
- **Exigences éprouvées :** `CHU-3.06`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.places.capacite"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.places.capacite`** — Capacité par choix : la dernière place prise, le choix est signalé complet et une nouvelle inscription y est refusée ; les autres choix restent ouverts *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | — |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | — |
| 5 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `fiche1` ← `$.id` |
| 6 | `POST /api/formulaires/${formulaire}/saisies/${fiche1}/soumettre` |  | 2xx | — |
| 7 | `GET /api/formulaires/${formulaire}/places` |  | 2xx | `$[0].choix` egal "Atelier" ; `$[0].complet` egal true ; `$[0].restantes` egal 0 ; `$[1].complet` egal false |
| 8 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `fiche2` ← `$.id` |
| 9 | `POST /api/formulaires/${formulaire}/saisies/${fiche2}/soumettre` |  | 409 | `$.message` contient "complet" |
| 10 | `POST /api/formulaires/${formulaire}/saisies/${fiche2}/soumettre` |  | 2xx | — |

#### L'inventaire des inscriptions par activité se consulte et s'exporte

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Conception et remplissage des formulaires (avis d'intention CHU, § 2 à 4) (`bdd/features/chu/01-formulaires.feature`)
- **Exigences éprouvées :** `CHU-4.08`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.places.inventaire"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — fiches du formulaire"
Alors je vois "Inventaire des inscriptions"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.places.inventaire`** — Inventaire : places et inscriptions par choix, export CSV ; le lecteur n'y a pas accès *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 200, 201, 400, 409 | — |
| 2 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 3 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 4 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | — |
| 5 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | — |
| 6 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `fiche1` ← `$.id` |
| 7 | `POST /api/formulaires/${formulaire}/saisies/${fiche1}/soumettre` |  | 2xx | — |
| 8 | `GET /api/formulaires/${formulaire}/inventaire` |  | 2xx | `$[0].place.inscrits` egal 1 ; `$[0].inscriptions[0].statut` egal "SOUMIS" ; `$[1].place.capacite` egal null |
| 9 | `GET /api/formulaires/${formulaire}/inventaire.csv` |  | 2xx | contient « champ;choix;capacite » ; contient « "Atelier";"1";"1";"0" » |
| 10 | `GET /api/formulaires/${formulaire}/inventaire` | lecteur CHU | 403 | — |

### Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6)


#### Une fiche est prise en charge, complétée, commentée, documentée, validée et clôturée ; synthèse et export Excel

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-4.07`, `CHU-7.05`, `CHU-6.01`, `CHU-6.02`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.traitement"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.traitement`** — Traitement d'une fiche : prise en charge, demande de complément, commentaire, pièce jointe classifiée, validation et clôture ; transitions interdites refusées ; tableau de synthèse et export Excel *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `fiche` ← `$.id` ; capture `ficheNumero` ← `$.numero` |
| 6 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 2xx | — |
| 7 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/cloturer` |  | 409 | contient « Transition interdite » |
| 8 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/prendre-en-charge` |  | 2xx | `$.statut` egal "EN_TRAITEMENT" |
| 9 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/commentaires` |  | 2xx | `$.visibilite` egal "INTERNE" |
| 10 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/documents?classification=CONFIDENTIEL` |  | 2xx | `$[0].classification` egal "CONFIDENTIEL" ; envoi multipart (fichier) |
| 11 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/complement` |  | 2xx | `$.statut` egal "A_COMPLETER" |
| 12 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 2xx | — |
| 13 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/prendre-en-charge` |  | 2xx | — |
| 14 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/valider` |  | 2xx | `$.statut` egal "VALIDE" |
| 15 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/cloturer` |  | 2xx | `$.statut` egal "CLOTURE" |
| 16 | `GET /api/rapport/formulaires/${formulaire}/resume` |  | 2xx | `$.CLOTURE` egal 1 |
| 17 | `GET /api/rapport/formulaires/${formulaire}/export.xlsx` |  | 2xx | contient « PK » |
| 18 | `POST /api/rapport/criteres?formulaireId=${formulaire}&nom=Cl%C3%B4tur%C3%A9es%20${unique}` |  | 2xx | `$.nom` egal "Clôturées ${unique}" |
| 19 | `GET /api/rapport/criteres` |  | 2xx | `$[*].nom` inclut "Clôturées ${unique}" |

#### Un circuit d'approbation multi-niveaux s'applique selon le montant

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-4.07`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.processus"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — approbations"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.processus`** — Processus structurés : circuit d'approbation multi-niveaux par règles (montant), décision par étape, demandes suivies *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/approbations/regles` |  | 2xx | — |
| 2 | `POST /api/approbations/demandes` |  | 2xx | `$.etapes` longueurMin 1 ; capture `demande` ← `$.id` |
| 3 | `GET /api/approbations/mes-etapes` |  | 2xx | `$` longueurMin 1 ; capture `etape` ← `$[0].id` |
| 4 | `POST /api/approbations/etapes/${etape}/decider` |  | 2xx | — |
| 5 | `GET /api/approbations/demandes/${demande}` |  | 2xx | `$.statut` egal "APPROVED" |

#### La soumission d'une fiche déclenche une notification par courriel, tracée au journal des envois

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-5.01`, `CHU-5.03`, `CHU-4.06`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.notifications"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — notifications"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.notifications`** — Notification par courriel déclenchée par la soumission d'une fiche (règle de notification), tracée au journal des envois *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `POST /api/notification/modeles` |  | 2xx | — |
| 6 | `POST /api/notification/regles` |  | 2xx | — |
| 7 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | `$.statut` egal "BROUILLON" ; capture `fiche` ← `$.id` ; capture `ficheNumero` ← `$.numero` |
| 8 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 2xx | — |
| 9 | `` |  | 2xx | — |
| 10 | `GET /api/notification/journal` |  | 2xx | `$[*].sujet` inclut "Fiche reçue ${unique} — ${ficheNumero}" ; `$[*].statut` inclut "ENVOYE" |

#### Un document PDF se produit depuis les données d'une fiche, puis se transmet

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-5.04`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.document.fiche"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — fiches du formulaire"
Alors je vois le bouton "Document PDF de la fiche"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.document.fiche`** — Document d'une fiche : PDF/A produit depuis ses données, transmis par courriel en pièce jointe (reçu par le relais SMTP), journalisé ; refusé au lecteur *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 200, 201, 400, 409 | — |
| 2 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 3 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 4 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | — |
| 5 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | — |
| 6 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `fiche` ← `$.id` |
| 7 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/soumettre` |  | 2xx | — |
| 8 | `GET /api/formulaires/${formulaire}/saisies/${fiche}/document.pdf` |  | 2xx | contient « %PDF » |
| 9 | `POST /api/formulaires/${formulaire}/saisies/${fiche}/document/transmettre` |  | 2xx | `$.statut` egal "ENVOYE" |
| 10 | `` |  | 2xx | — |
| 11 | `GET /api/notification/journal` |  | 2xx | contient « pièce jointe » |
| 12 | `GET /api/formulaires/${formulaire}/saisies/${fiche}/document.pdf` | lecteur CHU | 404 | — |

#### Les intervenants échangent par une messagerie interne sécurisée

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-5.05`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.messagerie"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — messagerie"
Et je clique sur "Dossier ${unique}"
Alors je vois "Pièce vérifiée."
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.messagerie`** — Messagerie sécurisée : l'administrateur ouvre une conversation avec un intervenant, qui la lit et répond ; une personne hors conversation n'y accède pas ; une adresse hors organisation est refusée *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 200, 201, 400, 409 | — |
| 2 | `POST /api/admin/users` |  | 2xx | — |
| 3 | `POST /api/messagerie/conversations` |  | 2xx | `$.messages[0].texte` egal "Merci de vérifier la pièce ${unique}." ; capture `conversation` ← `$.id` |
| 4 | `GET /api/messagerie/conversations` | lecteur CHU | 2xx | `$[?sujet=Dossier ${unique}].nonLus` egal 1 |
| 5 | `POST /api/messagerie/conversations/${conversation}/messages` | lecteur CHU | 2xx | `$.messages[1].auteur` egal "lecteur.bdd@chu.example" |
| 6 | `POST /api/messagerie/conversations` |  | 400 | `$.message` contient "membre actif" |
| 7 | `GET /api/messagerie/conversations/${conversation}` | rédacteur de Lévis | 403, 404 | — |

#### Un rapport PDF personnalisé se produit sur les fiches d'un formulaire

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-6.03`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.rapport.pdf"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — fiches du formulaire"
Alors je vois le bouton "Produire le rapport PDF"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.rapport.pdf`** — Rapport PDF personnalisé : questions et statuts choisis ; une question inconnue est refusée ; le lecteur n'y a pas accès *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 200, 201, 400, 409 | — |
| 2 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 3 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 4 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | — |
| 5 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | — |
| 6 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `f1` ← `$.id` |
| 7 | `POST /api/formulaires/${formulaire}/saisies/${f1}/soumettre` |  | 2xx | — |
| 8 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `f2` ← `$.id` |
| 9 | `POST /api/formulaires/${formulaire}/saisies/${f2}/soumettre` |  | 2xx | — |
| 10 | `GET /api/rapport/formulaires/${formulaire}/rapport.pdf?titre=Bilan-${unique}&champs=nom,activite&statuts=SOUMIS` |  | 2xx | contient « %PDF » |
| 11 | `GET /api/rapport/formulaires/${formulaire}/rapport.pdf?champs=inexistant` |  | 400 | `$.message` contient "Question inconnue" |
| 12 | `GET /api/rapport/formulaires/${formulaire}/rapport.pdf` | lecteur CHU | 403 | — |

#### Des tableaux de bord et graphiques se configurent et se partagent

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-6.04`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.tableaux-de-bord"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — tableaux de bord"
Et je choisis le tableau de bord "Pilotage ${unique}"
Alors je vois "Par activité"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.tableaux-de-bord`** — Tableau de bord : compteur, histogramme par réponse et évolution mensuelle calculés sur les fiches ; un formulaire d'une autre organisation est refusé *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | — |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | — |
| 5 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `f1` ← `$.id` |
| 6 | `POST /api/formulaires/${formulaire}/saisies/${f1}/soumettre` |  | 2xx | — |
| 7 | `POST /api/formulaires/${formulaire}/saisies` |  | 2xx | capture `f2` ← `$.id` |
| 8 | `POST /api/formulaires/${formulaire}/saisies/${f2}/soumettre` |  | 2xx | — |
| 9 | `POST /api/rapport/tableaux-de-bord` |  | 2xx | `$.indicateurs[0].points[0].valeur` egal 2 ; `$.indicateurs[1].points[0].etiquette` egal "Atelier" ; `$.indicateurs[1].points[0].valeur` egal 2 ; `$.indicateurs[2].points` longueurMin 1 ; capture `tableau` ← `$.id` |
| 10 | `GET /api/rapport/tableaux-de-bord` |  | 2xx | `$[*].nom` inclut "Pilotage ${unique}" |
| 11 | `POST /api/rapport/tableaux-de-bord` |  | 400 | — |

#### Une banque d'heures de services professionnels se tient et se suit

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Traitement, processus, notifications et rapports (avis d'intention CHU, § 4 à 6) (`bdd/features/chu/02-traitement-notifications.feature`)
- **Exigences éprouvées :** `CHU-9.05`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.banque-heures"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — banque d'heures"
Alors je vois "Heures restantes"
Et l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.banque-heures`** — Banque d'heures : le fournisseur l'ouvre et inscrit une consommation ; l'organisation voit son solde et son relevé ; un dépassement est refusé ; l'organisation ne peut pas s'ajouter d'heures *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/superadmin/tenants` | superadmin | 2xx | capture `chu` ← `$[?slug=chu-bdd].id` |
| 2 | `POST /api/superadmin/tenants/${chu}/banques-heures` | superadmin | 2xx | capture `banque` ← `$.id` |
| 3 | `POST /api/superadmin/tenants/${chu}/banques-heures/${banque}/mouvements` | superadmin | 2xx | `$.solde.restantes` egal 36.5 |
| 4 | `POST /api/superadmin/tenants/${chu}/banques-heures/${banque}/mouvements` | superadmin | 409 | `$.message` contient "Solde insuffisant" |
| 5 | `GET /api/soutien/banque-heures/${banque}` |  | 2xx | `$.solde.parCategorie.FORMATION` egal 3.5 ; `$.mouvements` longueurMin 2 |
| 6 | `GET /api/soutien/banque-heures/${banque}/releve.csv` |  | 2xx | contient « Formation ${unique} » |
| 7 | `POST /api/superadmin/tenants/${chu}/banques-heures/${banque}/mouvements` |  | 403 | — |

### Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8)


#### Groupes et droits fins, NAS masqué (Loi 25), journal des accès, authentification unique configurable

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-7.03`, `CHU-7.04`, `CHU-7.06`, `CHU-7.07`, `CHU-7.09`, `CHU-7.01`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.securite"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — sécurité avancée"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.securite`** — Groupes et droits fins (portée champ), classification ; masquage du NAS (Loi 25) ; journal des accès ; second facteur et authentification unique *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/securite/groupes` |  | 2xx | — |
| 2 | `POST /api/securite/groupes/RH_${UNIQUE}/membres?utilisateur=admin.bdd@chu.example` |  | 2xx | — |
| 3 | `POST /api/securite/permissions` |  | 2xx | `$.portee` egal "CHAMP" ; capture `permission` ← `$.id` |
| 4 | `GET /api/securite/permissions` |  | 2xx | `$[*].sujet` inclut "RH_${UNIQUE}" |
| 5 | `DELETE /api/securite/permissions/${permission}` |  | 2xx | — |
| 6 | `POST /api/rh/employes` |  | 2xx | `$.nasMasque` egal "***-***-286" ; ne contient pas « 046454286 » ; capture `employe` ← `$.id` |
| 7 | `GET /api/rh/employes/${employe}` |  | 2xx | ne contient pas « 046454286 » |
| 8 | `GET /api/audit?entite=Employe` |  | 2xx | `$` existe true |
| 9 | `GET /api/parametres/integration-oidc` |  | 2xx | `$` existe true |

#### Second facteur exigé et vérifié (question secrète, TOTP RFC 6238)

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-7.02`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "lev.securite.mfa"
Alors la plateforme se comporte comme attendu
```

**Cas `lev.securite.mfa`** — Second facteur : question secrète puis TOTP (RFC 6238) exigés et vérifiés *(données : `bdd/donnees/levis.json`)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true ; `$.ok` egal false |
| 2 | `POST /api/auth/login` | anonyme | 2xx | `$.mfaRequired` egal true |
| 3 | `POST /api/auth/login` | anonyme | 200, 401 | `$.ok` different true |
| 4 | `POST /api/auth/login` | anonyme | 2xx | `$.ok` egal true |

#### Comptes sans limite de nombre, rôles distincts et politique de mot de passe

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-1.01`, `CHU-6.05`, `CHU-7.02`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.droits"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.droits`** — Comptes sans limite de nombre, rôles distincts : le lecteur consulte les listes sans pouvoir concevoir ; politique de mot de passe appliquée *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/admin/users` |  | 400 | — |
| 2 | `POST /api/admin/users` |  | 200, 201, 400, 409 | — |
| 3 | `POST /api/admin/users` |  | 2xx | `$.roles` inclut "LECTEUR" |
| 4 | `GET /api/catalogue` | lecteur CHU | 2xx | `$` existe true |
| 5 | `GET /api/formulaires` | lecteur CHU | 403 | — |
| 6 | `POST /api/formulaires` | lecteur CHU | 403 | — |
| 7 | `GET /api/auth/me` | lecteur CHU | 2xx | `$.roles` inclut "LECTEUR" |

#### Un système externe crée une fiche par l'API publique avec un jeton OAuth

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-8.01`, `CHU-8.02`, `CHU-8.03`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.interop"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — interopérabilité"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.interop`** — API publique : client OAuth créé, jeton obtenu, fiche créée par un système externe ; jeton absent refusé *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | `$.statutCourant` egal "BROUILLON" ; capture `formulaire` ← `$.id` |
| 2 | `PUT /api/formulaires/${formulaire}/structure` |  | 2xx | — |
| 3 | `POST /api/formulaires/${formulaire}/soumettre` |  | 2xx | `$.statut` egal "EN_APPROBATION" |
| 4 | `POST /api/formulaires/${formulaire}/publier` |  | 2xx | `$.statut` egal "PUBLIE" |
| 5 | `POST /api/interop/clients?nom=Portail%20${unique}&scopes=fiches:ecrire` |  | 2xx | capture `clientId` ← `$.clientId` ; capture `secret` ← `$.secret` |
| 6 | `POST /api/public/oauth/token?client_id=${clientId}&client_secret=${secret}` | anonyme | 2xx | `$.tokenType` egal "Bearer" ; capture `jeton` ← `$.accessToken` |
| 7 | `POST /api/public/v1/formulaires/API_${UNIQUE}/fiches` | anonyme | 401 | — |
| 8 | `POST /api/public/v1/formulaires/API_${UNIQUE}/fiches` | anonyme | 200, 201 | contient « Carl Externe » |
| 9 | `GET /api/formulaires/${formulaire}/saisies` |  | 2xx | contient « Carl Externe » |

#### Les formulaires du CHU sont invisibles pour les autres clients

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-1.03`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.cloisonnement"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.cloisonnement`** — Cloisonnement entre clients : un formulaire du CHU est introuvable pour une autre organisation *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/formulaires` |  | 2xx | capture `formulaire` ← `$.id` |
| 2 | `GET /api/formulaires/${formulaire}/version` | administrateur de Lévis | 403, 404 | — |

#### Applications par domaine dans les environnements DEV, UAT et PROD

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-1.04`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.applications"
Alors la plateforme se comporte comme attendu
Quand j'ouvre l'écran "CHU — applications"
Alors l'écran est accessible, sans débordement ni superposition
```

**Cas `chu.applications`** — Applications et environnements DEV, UAT et PROD par domaine (RH, approvisionnement), dupliquées ; catalogue *(données : `bdd/donnees/chu.json` ; profil : administrateur CHU)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `POST /api/applications` |  | 2xx | `$.environnement` egal "DEV" ; capture `application` ← `$.id` |
| 2 | `POST /api/applications/${application}/dupliquer?code=PORTAIL_UAT_${UNIQUE}&nom=Portail%20RH%20UAT` |  | 2xx | `$.code` egal "PORTAIL_UAT_${UNIQUE}" |
| 3 | `POST /api/applications` |  | 2xx | `$.domaine` egal "APPROVISIONNEMENT" |
| 4 | `GET /api/applications?domaine=RH` |  | 2xx | `$[*].code` inclut "PORTAIL_${UNIQUE}" ; `$[*].domaine` exclut "APPROVISIONNEMENT" |
| 5 | `GET /api/catalogue` |  | 2xx | `$` existe true |

#### L'organisation porte son image institutionnelle

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-2.03`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'exécute le cas "chu.theme"
Alors la plateforme se comporte comme attendu
```

**Cas `chu.theme`** — Image institutionnelle : couleurs et logo propres à l'organisation, servis publiquement *(données : `bdd/donnees/chu.json` ; profil : superadmin)*

| # | Appel | Profil | Statut attendu | Vérifications et captures |
|---|---|---|---|---|
| 1 | `GET /api/superadmin/tenants` |  | 2xx | capture `organisation` ← `$[?slug=chu-bdd].id` |
| 2 | `PUT /api/superadmin/tenants/${organisation}/theme` |  | 2xx | — |
| 3 | `GET /api/tenants/chu-bdd/public` | anonyme | 2xx | `$.theme.couleurPrimaire` egal "#004B8D" ; `$.theme.logoTexte` egal "CHU ${unique}" |

#### Affichage sur téléphone et tablette, accessible et sans débordement

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-1.08`, `CHU-2.04`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'ouvre l'écran "CHU — formulaires (téléphone)"
Alors l'écran est accessible, sans débordement ni superposition
Quand j'ouvre l'écran "CHU — formulaires (tablette)"
Alors l'écran est accessible, sans débordement ni superposition
```

#### L'interface d'administration s'offre en français et en anglais

- **Résultat de la dernière exécution :** ✅ réussi
- **Fonctionnalité :** Sécurité, interopérabilité et plateforme (avis d'intention CHU, § 1, 2, 7 et 8) (`bdd/features/chu/03-securite-interop-plateforme.feature`)
- **Exigences éprouvées :** `CHU-2.01`

```gherkin
Et je suis connecté en tant que "administrateur CHU"   ⟵ contexte
Quand j'ouvre l'écran "CHU — formulaires"
Et je choisis la langue "English"
Alors l'écran est accessible, sans débordement ni superposition
Quand je choisis la langue "Français"
```

