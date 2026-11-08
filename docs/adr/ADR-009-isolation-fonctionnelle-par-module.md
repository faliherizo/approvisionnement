# ADR-009 — Isolation fonctionnelle par module et par tenant : socle désactivable, garde d'API et rôle Auditeur

- **Statut** : Accepté
- **Date** : 2026-11-03
- **Portée** : `commun.module` (catalogue, `@ModuleRequis`, `GardeModules`), contrôleurs du socle de rédaction,
  `Enums.Role`, `clm.domain.Habilitations`, `tenant.infrastructure.TenantsDemoSeeder`, `frontend/src/espaces.js`,
  `App.jsx`, `ClmShell.jsx`, `ArchitectureModulesTest`

## Contexte

Investissement Québec (AO 2026-6370-0005) souscrit le seul module CLM et son option IA. Pour un compte de ce tenant, aucun
menu, aucune page ni aucune API d'un autre module ne doit être présent. Deux constats :

1. Le socle de rédaction contractuelle de la Ville de Lévis (dossiers, documents, clauses, gabarits, circuits, assistant,
   pilotage) n'était pas un module du catalogue : ses menus s'affichaient pour toute organisation et ses API ne
   vérifiaient aucune souscription. Les autres modules (formulaires, processus, RH, paie…) étaient déjà gardés par
   `@PreAuthorize("@modules.actif('…')")`.
2. L'analyse des besoins (§ 3) et les maquettes UI-04, UI-06, UI-18, UI-19 et UI-23 demandent un profil
   « Auditeur / conformité » en lecture seule, absent du RBAC.

Contrainte technique : une annotation `@PreAuthorize` de méthode **remplace** celle de la classe. La plupart des
contrôleurs du socle portent des annotations de rôle par méthode : ajouter `@modules.actif` au niveau de la classe ne
garderait pas ces méthodes.

## Options envisagées

**Socle**

1. *Masquer menus et pages seulement* : rapide, mais l'API reste atteignable ; masquer n'est pas un contrôle d'accès
   (ST-02). Écartée.
2. *Répéter `@modules.actif('redaction') and …` sur chaque méthode du socle* : 108 méthodes exposées à annoter ou à
   modifier, un oubli passe inaperçu, et la souscription se mêle aux règles de rôle. Écartée.
3. *Module de catalogue `redaction`, actif par défaut, et annotation de classe `@ModuleRequis` appliquée par un
   intercepteur MVC unique* (retenue), complétée par un test d'architecture.

**Auditeur**

1. *Auditeur = LECTEUR* : ne voit que les trousses qui le désignent ; ne répond pas au besoin. Écartée.
2. *Auditeur = ADMIN* : privilèges excessifs (modifie, publie, administre). Écartée.
3. *Rôle `AUDITEUR` et règle de consultation distincte de la participation* (retenue).

## Décision

- **Catalogue** : nouveau module `redaction` (famille « Socle »), seul module `actifParDefaut = true`. Les organisations
  existantes le conservent sans écriture en base ; le superadmin le désactive par organisation comme tout module.
- **Garde d'API** : `@ModuleRequis("redaction")` sur les 26 contrôleurs du socle, `@ModuleRequis({"redaction",
  "formulaire"})` sur les 2 contrôleurs des listes de référence partagées. `GardeModules` (intercepteur `/api/**`)
  refuse en 403 `{"message": "Fonctionnalité non souscrite par votre organisation (module « … »)."}` avant toute
  exécution, quel que soit le rôle. Le superadmin et les appels publics à jeton (tenant nul) passent, comme avec
  `@modules.actif`.
- **Test d'architecture** : `ArchitectureModulesTest` exige que tout `@RestController` relève d'un module
  (`@ModuleRequis` ou `@modules.actif` de classe) ou figure, motivé, parmi 17 fonctions transverses ; et qu'une
  annotation de méthode répète la garde de module qu'elle remplace.
- **Espace exclusif côté interface** : une organisation dont les modules actifs se limitent à `clm`, `clm_ia` et
  `securite_avancee` n'a pas d'autre espace que celui du module. Le gabarit de la plateforme la redirige vers `/clm` ;
  les fonctions transverses (profil, sécurité du compte, comptes, authentification unique, journal de sécurité) sont
  servies dans l'espace CLM. Les menus et routes du socle sont conditionnés au module `redaction`.
- **Rôle `AUDITEUR`** : assignable par l'administrateur du tenant. `Habilitations.peutConsulter` = auditeur **ou**
  `peutVoir` ; `peutVoir` (participation) est inchangé. Commenter et analyser exigent `peutVoir`
  (`LectureTrousse.exigerContribution`) ; modifier exige `peutModifier`. L'auditeur exporte le dossier de preuve et
  consulte le journal UI-23 (`/api/clm/admin/journal`).
- **Amorçage** : `data/tenants-demo.json` décrit le tenant IQ, ses modules et un compte par profil ; inactif en
  production (`app.amorcage.tenants-demo=false`).

## Conséquences

- Ville de Lévis et tout tenant créé par le superadmin : comportement inchangé (socle actif par défaut).
- Toute nouvelle API doit relever d'un module ou être ajoutée, motivée, à la liste des transverses : sinon la
  construction échoue.
- L'interception s'ajoute aux contrôles existants (rôles, périmètre, tenant) sans les remplacer.
- La règle de visibilité des trousses (`LectureTrousse.visible`, impact GitNexus MEDIUM, 54 symboles) s'élargit pour
  le seul rôle `AUDITEUR` ; `Habilitations.peutVoir` (impact CRITICAL, 24 symboles) n'est pas modifié.
- Limites connues : le portail public du signataire n'est pas fermé par la désactivation du module CLM (le tenant
  n'est connu qu'à la lecture du jeton) ; administrateur d'affaires et administrateur technique partagent le rôle
  `ADMIN`.
