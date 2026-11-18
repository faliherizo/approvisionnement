# ADR-014 — Annuaire d'entreprise : correspondances vers rôles et domaines d'affaires, provisionnement SCIM 2.0

- **Statut** : Accepté
- **Date** : 2026-11-17
- **Portée** : `auth.domain.{FaitAnnuaire, CorrespondanceAnnuaire, ReglesAnnuaire, AppartenanceAnnuaire, GroupeScim,
  MembreGroupeScim, JetonScim}`, `auth.application.{AnnuaireService, ScimService}`, `auth.infrastructure.web.{AnnuaireController,
  ScimController}`, `OidcClient`, `SamlClient`, `OidcSessionService`, `SamlSessionService`,
  `commun.application.AppartenancesAnnuaire`, `clm.infrastructure.GroupesAnnuaireEcouteur`, changelog `058`
- **Origine** : addenda 3, Q43 (mappage configurable groupes et attributs → rôles, moindre privilège, traçabilité),
  Q44 (domaine d'affaires tiré d'un groupe ou d'un attribut), Q51 (SCIM 2.0 obligatoire : provisionnement,
  déprovisionnement, mappage, essais, documentation)

## Contexte

La connexion unique (OIDC ou SAML) ne portait que le courriel et le nom ; un compte créé recevait un rôle par défaut et
les groupes de l'annuaire n'étaient pas repris. Aucun point SCIM n'existait. L'addenda 3 rend ces trois capacités
obligatoires, et le provisionnement est poussé par Saviynt.

## Options envisagées

1. **Rôles lus directement des claims à chaque requête** : aucune trace des changements d'accès, et aucun moyen de servir
   SCIM, qui n'a pas de jeton d'utilisateur.
2. **Une correspondance par module** (rôles dans `auth`, unités dans `clm`) : deux écrans, deux vérités, et `auth`
   devrait connaître les entités du module de contrats.
3. **Faits d'annuaire uniques, correspondances de l'organisation, domaines propagés par événement (retenue)**.

## Décision

- Les **faits d'annuaire** d'un compte (groupes, attributs) sont conservés par origine : `SSO` (remplacés à chaque
  connexion) et `SCIM` (appartenances aux groupes poussés). Seuls les attributs cités par une correspondance sont
  gardés (minimisation, Loi 25).
- Une **correspondance** : groupe (identifiant d'objet Entra ID ou nom) ou attribut = valeur → rôle, domaine d'affaires,
  ou les deux. Le rôle `SUPERADMIN` n'est jamais attribuable.
- **Moindre privilège** : si l'organisation a au moins une correspondance de rôle, les rôles d'un compte sont exactement
  ceux de ses correspondances, à défaut le rôle par défaut ; sans correspondance de rôle, les rôles attribués dans la
  plateforme sont conservés (transition sans rupture).
- **Traçabilité** : tout changement de rôle est consigné au journal d'audit (`ROLES_ANNUAIRE`, avant → après), comme les
  provisionnements et déprovisionnements SCIM.
- **Domaines d'affaires** : `auth` publie l'événement `AppartenancesAnnuaire` (primitives seulement) ; le module de
  contrats ajoute ou retire le compte des groupes **pilotés par l'annuaire**, sans toucher aux groupes manuels. Aucun
  module n'accède aux entités d'un autre (ADR-006, ADR-009).
- **SCIM 2.0** (RFC 7643, 7644) sous `/scim/v2` : `Users`, `Groups`, `ServiceProviderConfig`, `ResourceTypes` ; filtres
  `userName eq` et `displayName eq` ; PATCH tel qu'émis par Entra ID et Saviynt. Authentification par **jeton porteur
  propre à l'organisation**, dont seule l'empreinte SHA-256 est conservée ; en créer un nouveau révoque l'ancien.
- **Déprovisionnement = désactivation** : le compte est conservé (piste d'audit, preuves signées) et ne peut plus se
  connecter, ni localement ni par SSO.

## Conséquences

- Contrôleurs `ScimController` et `AnnuaireController` déclarés transverses dans `ArchitectureModulesTest`, avec motif.
- Porte de couverture `check-annuaire` (classes nommées une à une, ≥ 96 % par classe) dans `pom.xml`.
- `/scim/` est relayé par nginx (`default.conf.template`) et par Vite.
- À confirmer avec IQ : groupes Entra ID par identifiant ou par nom (N6), groupes SCIM ou attributs de rôle (N5).
- Tests : `AnnuaireDomaineTest`, `Addenda3IntegrationTest`, e2e « Addenda 3 — Q43 / Q51 ».
