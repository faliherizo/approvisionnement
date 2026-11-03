# ADR-007 — Gabarit d'interface propre au module CLM (maquettes IQ UI-01 à UI-24)

- **Statut** : Accepté
- **Date** : 2026-11-02
- **Portée** : `frontend/src/pages/clm`, `frontend/src/App.jsx`, `e2e/views/clm.view.spec.ts`

## Contexte

Les maquettes du dossier IQ 2026-6370-0005 (`docs/Dossier_IQ_2026-6370-0005/10-Maquettes-UI/svg`) décrivent
un gabarit commun aux 24 écrans : bandeau « Contrats & signatures » avec recherche globale et profil, navigation
latérale (Tableau de bord, Mes trousses, Mes tâches, Modèles, Suivi centralisé, Administration), titre et objectif,
blocs numérotés en grille de deux colonnes, barre d'actions en pied. Le gabarit de la plateforme (barre supérieure à
menus déroulants, contenu limité à 1 200 px) ne permet ni la navigation latérale ni la recherche globale propres au
module. Le module reste activé ou désactivé par tenant depuis la console superadmin, comme les autres (`clm`, `clm_ia`).

## Options envisagées

1. **Conserver le gabarit de la plateforme** et y loger les écrans CLM : aucune duplication, mais écart structurel
   avec les maquettes (pas de navigation latérale, pas de recherche globale, largeur contrainte).
2. **Gabarit dédié pour les routes `/clm/*`** dans la même application (retenue).
3. **Application distincte** pour le module : fidélité maximale, mais authentification, i18n, thème et chaîne de
   construction dupliqués.

## Décision

Option 2. `App.jsx` rend les routes `/clm/*` dans `ClmShell` (bandeau + navigation latérale) au lieu du gabarit
de la plateforme, comme le portail du signataire (`/portail/*`) qui a déjà son propre gabarit. Chaque écran utilise
`EcranClm`, `Blocs`, `Bloc` et `BarreActions` (`pages/clm/gabarit.jsx`) ; les attributs `data-maquette` et
`data-zone` relient chaque écran et chaque bloc à sa maquette. L'administration du module est découpée en écrans
distincts (`/clm/admin/notifications`, `integrations`, `parametres`, `utilisateurs`, `journal`, `/clm/options`).

## Conséquences

- Deux gabarits coexistent ; le lien « Retour à la plateforme » du bandeau et le menu « Contrats & signature » de
  la plateforme assurent le passage de l'un à l'autre.
- L'activation reste gouvernée par le registre des modules : chaque écran est protégé par `GardeModule` côté
  interface et par `@modules.actif('clm')` côté API.
- Les contrôles automatisés (débordement, superposition, cibles ≥ 24 px, libellés, axe-core WCAG 2.2 AA) portent
  désormais sur toute la page, bandeau et navigation compris, aux quatre largeurs de référence.
- Les maquettes restent des fils de fer : l'ordre des blocs les suit, sauf lorsque l'accessibilité impose un autre
  ordre (filtres placés avant les résultats au suivi centralisé et au journal d'audit).
