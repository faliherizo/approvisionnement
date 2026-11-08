# ADR-010 — Exclusivité des produits Approvisionnement et IQ (contrats et signature)

- **Statut** : Accepté
- **Date** : 2026-11-08
- **Portée** : `commun.module.application.ModuleService`, `components/ModuleManager.jsx`, `frontend/src/espaces.js`
  (inchangé), `e2e/views/clm.outils.ts`, `e2e/tests/approvisionnement.spec.ts`
- **Complète** : ADR-009 (isolation fonctionnelle par module et espace exclusif CLM)

## Contexte

La plateforme porte deux produits : l'**approvisionnement** (moteur de formulaires, demandes, processus, rapports —
famille de catalogue « Plateforme ») et **IQ** (trousses, signature probante, preuve, Salesforce — famille « CLM »).
Le métier demande que, depuis l'espace superadmin, une organisation exploite **l'un ou l'autre** : quand IQ est actif,
l'accueil est le tableau de bord IQ et aucun écran d'approvisionnement n'est proposé.

ADR-009 a déjà donné à une organisation « CLM seul » un espace exclusif (`espaceClmSeul` redirige vers `/clm`). Mais
rien n'empêchait d'activer les deux familles ensemble : l'organisation retombait alors dans le gabarit général, avec
les deux menus, et l'accueil IQ n'était plus garanti.

## Options envisagées

1. *Sous-menu « Approvisionnement » dans le menu IQ* : garde les deux produits actifs, l'approvisionnement devenant une
   section d'IQ. Écartée par le métier (choix explicite du 2026-11-08) : les produits sont vendus séparément.
2. *Avertir seulement dans l'écran superadmin* : l'état incohérent reste possible par l'API ou par mégarde. Écartée.
3. *Exclusivité appliquée par le registre de modules* (retenue) : activer un module d'une famille exclusive désactive
   tous les modules actifs de l'autre famille exclusive, dans la même transaction.

## Décision

- `ModuleService` déclare les familles exclusives `{"Plateforme", "CLM"}`. À l'activation, après les prérequis, il
  désactive chaque module actif d'une famille exclusive autre que celle(s) activée(s). Le résultat liste ces modules
  dans `codesAffectes` et le message le dit (« Produit concurrent désactivé (exclusivité) : … »).
- L'exclusivité est **par famille du catalogue**, pas par liste de codes : un futur module « Plateforme » ou « CLM »
  y est soumis sans changement de code.
- Les familles hors de l'ensemble (Socle `redaction`, Sécurité, RH) cohabitent avec n'importe quel produit.
- L'écran superadmin des modules énonce la règle avant toute bascule.
- Le routage n'est pas modifié : une organisation dont seul le produit IQ est actif satisfait déjà `espaceClmSeul`
  (ADR-009) et son accueil est `/clm`.

## Conséquences

- Une organisation ne peut plus être dans l'état « approvisionnement + IQ ». Les données d'un produit désactivé sont
  conservées ; le réactiver les retrouve (seul l'état `tenant_module` change).
- Les suites e2e qui activent un produit pour un tenant désactivent l'autre ; chaque suite rétablit son propre
  prérequis avant de s'exécuter (`activerModules`), et Playwright s'exécute en série (`workers: 1`).
- Les tests d'intégration qui activent `clm` sur le tenant de Lévis y désactivent l'approvisionnement ; chaque test
  qui dépend de l'approvisionnement active lui-même ses modules.
- Tests : `ModuleServiceTest` (IQ retire l'approvisionnement, l'approvisionnement retire IQ, un module neutre ne retire
  rien).
