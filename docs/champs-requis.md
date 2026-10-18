# Analyse & conception — champs requis (validation front + back)

Détermine, par formulaire, les champs **obligatoires** et le lieu de validation
(frontend `required` / garde, backend Bean Validation `@NotBlank/@NotNull/@Email`).

## Méthode
Un champ est requis s'il est **indispensable à l'intégrité métier** (le traitement
échoue ou est ambigu sans lui). Les champs contextuels/facultatifs restent optionnels.

## Matrice

| Formulaire | Champ | Requis | Front | Back |
|-----------|-------|:-----:|:-----:|:----:|
| Connexion | courriel, mot de passe | ✅ | `required` | `@Email @NotBlank` |
| Document | **titre**, **type** | ✅ | `required` | `@NotBlank`/`@NotNull` |
| Document | mode de sollicitation | ✅ (front) | `required` (select) | ➖ recommandé |
| Document | valeur estimée, contexte | ➖ | — | — |
| Clause | **code, titre, catégorie, corps** | ✅ | `required` + garde WYSIWYG | `@NotBlank`/`@NotNull` |
| Clause | dépendances, exclusions | ➖ | — | — |
| Commentaire | **corps** | ✅ | garde WYSIWYG | `@NotBlank` |
| Addenda | **objet** | ✅ | `required` | `@NotBlank` |
| Obligation | **titre, type** | ✅ | `required` | `@NotBlank`/`@NotNull` |
| Obligation | échéance, responsable, description | ➖ | — | — |
| Signataire | **nom, courriel** | ✅ | `required` | `@Email @NotBlank` |
| Connaissance IA | **titre, contenu** | ✅ | `required` + garde WYSIWYG | `@NotBlank` |
| Utilisateur (admin) | **courriel, nom** | ✅ | `required` | `@Email @NotBlank` |
| Tenant (superadmin) | **slug, titre** | ✅ | `required` | `@NotBlank` |
| Admin de tenant | **courriel, nom, mot de passe** | ✅ | `required` | `@Email @NotBlank` |
| Valeur de référence | **catégorie, code, libellé** | ✅ | `required` | `@NotBlank` |

## Décisions appliquées dans cette itération
- **Document → mode de sollicitation** requis à la saisie (select frontend, toujours
  renseigné) ; backend tolérant (compatibilité des flux existants / imports).
- **Champs WYSIWYG** (corps de clause, contenu de connaissance) : le `contenteditable`
  ne supporte pas `required` → **garde applicative** (blocage si vide, balises retirées)
  en plus du `@NotBlank` backend.
- Les autres champs déjà validés restent inchangés.

> Les listes de valeurs des sélecteurs (types, modes, catégories…) sont désormais
> **dynamiques** (gérées par superadmin en global, par admin au niveau tenant) — voir
> le sous-système `ReferenceValue`.
