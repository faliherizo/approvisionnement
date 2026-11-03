# Conformité à l'avis d'intention CHU (00-Appel-interet.md)

> Validation **fonctionnalité par fonctionnalité** des besoins de l'avis d'intention (gré à gré Dexero,
> clôture manifestation d'intérêt **2026-10-26**) au regard de la solution `contractuelle_contrat`.
> Légende : ✅ conforme · 🟡 partiel · ❌ écart. Les écarts sont **traités** (voir §Traitement) ou
> **documentés avec justification/question client** (voir `09-QUESTIONS-CLIENT.md`).

## Portée — lecture importante

L'avis décrit une **plateforme de formulaires électroniques / applications Web + gestion de données**,
utilisée **par** le portail RH et le portail Approvisionnements. Ce n'est **pas** une demande de SIRH
(paie, absences, recrutement…) : « RH » et « Approvisionnements » sont des **domaines/portails** configurés
sur la plateforme. Notre moteur générique (P0→P6) + la dimension domaine (AP-1→AP-5) répond au besoin.
*(Le volet paie/impôt/déclarations/dossiers employés porté de Nexu VINA — RH-1→RH-3 — est un **bonus**
au-delà de l'avis ; utile en démonstration, non exigé ici.)*

---

## 1. Mode de prestation, hébergement, disponibilité

| Besoin | Statut | Où |
|---|---|---|
| Infonuagique, environnement dédié, accès en tout temps, **sans limite d'utilisateurs** | ✅ | multi-tenant ; infra Azure/AWS (docs trois-rivière, DR) |
| Hébergement des RP **au Québec** | ✅ | Azure Canada / AWS Montréal (docs infra) |
| Cloisonnement données/formulaires entre **environnements et clients** | ✅ | `TenantContext` (multi-tenant) + `environnement` (Application) |
| Environnements **PROD / DEV / acceptation (UAT)** | ✅ | `Application.environnement` (DEV/UAT/PROD) |
| Disponibilité **99,8 %** en tout temps | ✅ (contractuel/infra) | plans de reprise RPO/RTO |
| Sauvegardes redondantes locales+externes **quotidiennes** | ✅ (infra) | docs DR |
| Hébergement, entretien, correctifs, MAJ, migrations | ✅ (service) | offre de service |
| Gestion durée des sessions + **affichage ordi/tablette/mobile** | 🟡 | sessions JWT ok ; responsive présent, **à valider par tests visuels aux largeurs de référence** |

## 2. Langues, accessibilité, présentation

| Besoin | Statut | Où |
|---|---|---|
| Interface d'administration **FR/EN** | ✅ | i18n (FR/EN/MGA, 1500+ clés) |
| **Formulaires multilingues** (contenu) | ❌ → 🟡 traité | libellés de champ multilingues **ajoutés** (voir Traitement G1) |
| Gabarits, images, feuilles de style institutionnels | ✅ | thème par tenant (couleurs, logo) |
| **Accessibilité** sites Web gouv. QC (SGQRI 008 / WCAG 2.2 AA) | 🟡 | règles respectées à la conception ; **axe-core non câblé en CI** (voir questions) |

## 3. Création et gestion des formulaires

| Besoin | Statut | Où |
|---|---|---|
| Formulaires à une ou plusieurs pages | ✅ | `FormulaireBuilder` (pages) |
| Tous les types de champs (texte, zone, cases, radio, liste, date, hyperlien, courriel, nombre, décimale, montant, pourcentage, **fichier**, **matrice**, **valeurs dynamiques**, **calculées**) | ✅ | 16 types en données de référence `TYPE_CHAMP` |
| Aperçu des réponses avant soumission | ✅ | `FormulaireRenderer` / aperçu builder |
| **Sauvegarde automatique** + reprise ultérieure | ✅ | autosave + reprise `?saisie=` |
| Activation/désactivation selon **date**, **nb max de répondants**, **plage réseau** | 🟡 → ✅ traité | date + maxRépondants déjà ; **plage réseau (IP) ajoutée** (G2) |
| **Restriction de choix selon capacité maximale** | ❌ → ✅ traité | capacité par choix **ajoutée** (G3) |
| Mécanisme **anti-automatisation** (formulaires publics) | 🟡 | port `CaptchaVerifier` + simulation ; adaptateur réel (hCaptcha/reCAPTCHA) à activer |
| Formulaires liés **parent-enfant** | ✅ | `Formulaire.parentId`, `Saisie.parentSaisieId` |
| Versions, **approbation des modifs**, retour à une version | ✅ | `StatutFormulaire` + historique + `revenirA` + onglet Versions |
| Duplication / export / réimport de structure | ✅ | `dupliquer` / `exporter` / `importer` |

## 4. Règles d'affaires, automatisation, processus

| Besoin | Statut | Où |
|---|---|---|
| Afficher/masquer/modifier des champs selon les réponses | ✅ | `MoteurRegles` VISIBILITE |
| Obligatoire/facultatif selon le contexte | ✅ | OBLIGATION |
| Validation croisée + format | ✅ | VALIDATION |
| Calcul automatique (formules) | ✅ | CALCUL (somme/diff/produit/quotient, détection de cycle) |
| **Routage des demandes** selon règles | ✅ | ROUTAGE + workflow (routage par montant/condition) |
| Déclenchement d'événements selon données / modif de fiche | ✅ | `FicheEvent` |
| **Processus structurés : tâches, étapes d'approbation, responsabilités** | ✅ | contexte `processus` : `Workflow`/`Tache`/`Decision`, écran **Tâches et approbations** (`/taches`) |
| **Gestion d'inventaire / capacité** (inscriptions à des activités) | ❌ | documenté (G3 couvre la capacité par choix ; l'inventaire d'inscriptions complet → question client) |

> **Note sur « la gestion de tâches »** : l'écran **/taches** EST livré (AP-3). Pour des circuits
> d'approbation **multi-niveaux par règle** plus riches, le moteur pur **`Approbations`** de Nexu VINA
> est portable (voir Traitement G9).

## 5. Notifications et communications

| Besoin | Statut | Où |
|---|---|---|
| Notifications **courriel / SMS / interne** selon déclencheur | ✅ | contexte `notification` (3 canaux, simulation ; adaptateurs réels Twilio/ACS à activer) |
| Confirmation à la soumission | ✅ | écran de remplissage |
| Notification aux administrateurs à la création/modif d'une fiche | ✅ | règles de notification sur événements |
| **Production d'un document à partir des données + transmission** | 🟡 | infra PDF existante (contrats) ; **génération PDF depuis une fiche à câbler** (voir questions) |
| Messagerie interne sécurisée | 🟡 | canal INTERNE + commentaires de demande ; messagerie complète → question |

## 6. Gestion, recherche, rapports, données

| Besoin | Statut | Où |
|---|---|---|
| Recherche multicritère + **critères sauvegardés** | ✅ | contexte `rapport` (`CritereRechercheSauve`) |
| Export **Excel** (toutes/sélection) | ✅ | export POI |
| **Rapports PDF personnalisés** | 🟡 | à câbler sur le moteur de fiches (voir questions) |
| **Graphiques + tableaux de bord configurables** | ❌ | écart documenté (voir questions / plan) |
| Pages listes/dashboards/rapports **selon les droits** | 🟡 | listes/rapports ok ; dashboards à construire |
| **Export intégral structuré** (réversibilité) | 🟡 | export structure (JSON) + données (Excel) ; export intégral unifié à formaliser |
| **Durée de conservation** des données | ❌ → ✅ traité | politique de rétention par formulaire **ajoutée** (G7) |

## 7. Sécurité, confidentialité, contrôle des accès

| Besoin | Statut | Où |
|---|---|---|
| **SSO** identité org + méthodes normalisées | ✅ | connecteurs OIDC/SAML per-tenant |
| **2FA** accès admin + politique de mots de passe | ✅ | MFA TOTP / question |
| Groupes avec droits lecture/modif/suppression | ✅ | `securite` (Groupe/GroupeMembre/PermissionAcces) |
| Contrôle d'accès par utilisateur/groupe/rôle/formulaire/**champ/page/fiche/bouton** | ✅ | `MoteurAcces` (portée champ/fiche/bouton, refus par défaut) |
| Classification **public/interne/confidentiel/secret** | ✅ | `CLASSIFICATION` + `Masquage` |
| Mesures renforcées (masquage, chiffrement, restriction affichage/export) | 🟡 | masquage + classification ok ; chiffrement au repos = infra |
| **Traçabilité** accès et modifications | ✅ | journal d'audit (Loi 25) |
| Protection **DDoS / brute-force / maliciels** | 🟡 | `PareFeuFilter` (limiteur de débit) + WAF/anti-DDoS = infra |
| Config pour obligations **Loi 25** | ✅ | masquage NAS, courriel masqué en journal |

## 8. Interopérabilité

| Besoin | Statut | Où |
|---|---|---|
| **API** consulter/récupérer/créer/compléter des fiches | ✅ | `interop` (`/api/public/v1/formulaires/{code}/fiches`) |
| Auth API par **jeton / OAuth** | ✅ | OAuth2 client_credentials (jetons SHA-256) |
| Échanges formulaires/données/sites/systèmes externes | 🟡 | API publique ok ; connecteurs M365/SAP existants ; intégrations spécifiques sur demande |
| Maintien des échanges déjà en place | n/a | nouvelle implémentation (pas de legacy chez nous) |

## 9. Licences, soutien, services professionnels

| Besoin | Statut | Où |
|---|---|---|
| Licences **2 secteurs (RH + Appro) + 4 apps Web** | ✅ | domaines RH/APPROVISIONNEMENT + catalogue d'`Application` |
| Soutien 2e ligne **L-V 8 h–17 h** | n/a (service) | offre de service |
| Tests applicatifs lors migrations/MAJ | 🟡 | e2e Playwright ; à étendre par application |
| **Test d'intrusion** du portail RH (firme externe) | n/a (service) | à planifier |
| **Banque d'heures** (analyse/formation/config/prog/rapports) | ❌ | modèle `Soutien`/banque d'heures portable de Nexu VINA (voir questions) |

---

## Traitement des écarts — FAIT + testé

- **G1 — Formulaires multilingues** ✅ : libellé de champ `libelleI18n = {fr, en, mg}` dans la structure ;
  concepteur « Libellé (EN) », rendu selon la locale (`FormulaireRenderer`). Sans migration.
- **G2 — Activation par plage réseau** ✅ : `Activation.Config.plagesReseau` (préfixes IP) +
  `Activation.ipAutorisee(cfg, ip)` (pur, testé) ; l'IP du client (X-Forwarded-For/remoteAddr) est
  vérifiée à la soumission (`SaisieController`→`SaisieService.soumettre`). Format exact à confirmer (Q12).
- **G7 — Durée de conservation** ✅ : `Formulaire.conservationJours` (migration 046) + champ au builder.
  La **valeur** (durées) viendra du calendrier de conservation du CHU (Q19).
- **G9 — Approbations multi-niveaux** ✅ : moteur pur `Approbations` **porté** + **CÂBLÉ** en feature
  complète : `RegleApprobation`/`DemandeApprobation`/`EtapeApprobation` (migration 044), `ApprobationService`
  (sélection d'étapes par règles, décision, recalcul du statut), API `/api/approbations`, écran **Approbations**
  (`/approbations`, menu « Mes services »). Tests : `ApprobationsTest` (moteur) + `ApprobationServiceTest`.
- **Gestion de tâches (remarque utilisateur)** ✅ : ajout de tâche/approbation repris — l'écran `/taches`
  (AP-3) **plus** le gestionnaire d'approbations multi-niveaux `/approbations` ci-dessus.
- **Feuille de temps** ✅ (demande utilisateur) : contexte `temps` (migration 045) — `FeuilleTemps`
  (lignes JSON date/catégorie/heures, total, cycle BROUILLON→SOUMISE→APPROUVEE/REFUSEE), API
  `/api/rh/feuilles-temps`, écran **Feuille de temps** dans l'**Espace RH**. Test `FeuilleTempsServiceTest`.

## Écarts restants — à cadrer avec le client avant implémentation (voir 09-QUESTIONS-CLIENT.md)

Ces écarts dépendent d'une décision du CHU : les coder sans la connaître produirait l'inverse du besoin.
- **G3** capacité maximale **par choix** → sémantique à confirmer (Q13 : places par option ? liste
  d'attente ?). Conception : capacité par option dans les métadonnées du champ + comptage des soumissions.
- **G4** inventaire/inscriptions complet (Q13, englobe G3).
- **G5** génération **PDF depuis une fiche** → quels gabarits (Q16) ; l'infra PDF existe (contrats), à câbler.
- **G6** tableaux de bord & graphiques configurables → quels indicateurs (Q18) ; données `rapport/resume`
  déjà disponibles, dashboard générique à monter.
- **G8** banque d'heures (Q24) → modèle `Soutien` de Nexu VINA portable (tickets + heures consommées).
- Adaptateurs réels captcha/SMS/courriel (fournisseurs, Q17) ; **axe-core** en CI (Q10).

> **Sur la remarque « gestionnaire de tâches / fonctionnalités RH de freelance-invoicing »** : l'écran
> **Tâches et approbations** (`/taches`, contexte `processus`) est **livré et vérifié** (AP-3) et couvre
> le besoin §4 de l'avis. Le moteur **`Approbations`** (G9) est désormais porté pour les circuits
> multi-niveaux. Les modules RH étendus de Nexu VINA (absences, horaires, performance, recrutement,
> formations) ne sont **pas exigés par l'avis** : ce sont des **applications métier** que le CHU
> construirait *sur* la plateforme (comme des formulaires/workflows), pas des fonctions de la plateforme.
> Ils restent **portables à la demande** (voir Q23/Q24).
