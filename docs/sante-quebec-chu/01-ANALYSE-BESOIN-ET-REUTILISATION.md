# Analyse du besoin CHU → Réutiliser / Porter / Construire

Légende : **♻️ Réutiliser** (existe en Java chez nous) · **🔁 Porter** (concept présent dans
`freelance-invoicing`, à réimplémenter en Java/Spring) · **🔨 Construire** (neuf).

## Bloc 1 — Mode de prestation, hébergement, disponibilité

| Exigence | Statut | Base |
|---|---|---|
| Infonuagique, environnement **dédié**, accès en tout temps, sans limite d'utilisateurs | ♻️ | Azure/AWS HA, multi-tenant (un tenant dédié CHU) |
| Hébergement des renseignements personnels **au Québec** | ♻️ | AWS Montréal `ca-central-1` **ou** OVH Beauharnois (résidence Québec) |
| **Cloisonnement** données/formulaires entre environnements et clients | ♻️ | `TenantContext` + `tenant_id` systématique |
| Environnements distincts **prod / dév / acceptation** | ♻️🔨 | prod + essai existants ; **ajouter l'environnement d'acceptation** |
| Disponibilité **99,8 %** en tout temps | ♻️ | capacité 99,99 % déjà conçue (marge) |
| **Sauvegardes redondantes** locales + externes quotidiennes | ♻️ | plan DR (RPO ≤ 15 min, sauvegardes géo-redondantes) |
| Entretien, correctifs, mises à jour, migrations | ♻️ | maintenance incluse |
| **Sessions** + affichage responsive (desktop/tablette/mobile) | ♻️🔨 | SPA React responsive ; **gestion durée de session** à ajouter |

## Bloc 2 — Langues, accessibilité, présentation

| Exigence | Statut | Base |
|---|---|---|
| Interface d'administration **FR/EN** | ♻️ | i18n (FR/EN/MGA, `messages.json`, `useI18n`) |
| Formulaires et applications **multilingues** | 🔨 | i18n **au niveau du contenu des formulaires** (libellés par langue) |
| Personnalisation (gabarits, images, CSS, normes graphiques) | ♻️ | thèmes/branding par tenant + gabarits de rendu |
| Accessibilité (normes Web gouv. QC / SGQRI 008 / WCAG 2.2 AA) | ♻️ | déjà visée AA dans nos interfaces |

## Bloc 3 — Création et gestion des formulaires

| Exigence | Statut | Base |
|---|---|---|
| Formulaires **mono/multi-pages** | 🔁🔨 | modèle `Gabarit/Champ` (flux) à porter + notion de **page** |
| Types de champs (texte, zone texte, cases, radio, listes, dates, hyperliens, courriels, nombres, décimales, **montants, %**, **fichiers**, **matrices**, valeurs **dynamiques/calculées**) | 🔁🔨 | `montant/%/calculé/liste` existent (flux) ; **construire** case/radio/zone-texte/courriel/**fichier**/**matrice**/hyperlien |
| **Aperçu** des réponses avant soumission | 🔨 | mode prévisualisation |
| **Sauvegarde automatique** + reprise ultérieure | 🔨 | autosave des brouillons |
| **Activation/désactivation** par date, nb max de répondants, plage IP | 🔨 | fenêtres d'activation |
| Restriction de choix selon **capacité maximale** | 🔨 | compteur d'inventaire/capacité |
| **Anti-robot** (captcha) sur formulaires publics | 🔨 | captcha |
| Formulaires **liés parent-enfant** (multi-niveaux) | 🔨 | relations entre formulaires |
| **Versions**, approbation avant mise en ligne, **retour arrière** | ♻️🔁 | `DocumentVersion` + `ApprovalWorkflow` (ours) + chaînes datées (flux) |
| **Duplication, export/import** de structure | 🔨 | export/import JSON de la définition |

## Bloc 4 — Règles d'affaires, automatisation, processus

| Exigence | Statut | Base |
|---|---|---|
| **Afficher/masquer/modifier** des champs selon les réponses | 🔨 | **logique conditionnelle de visibilité** (nouveau) |
| Caractère **obligatoire conditionnel** | 🔁🔨 | généraliser `RuleEngine` + obligation conditionnelle |
| **Validation croisée** + validation de format | 🔁 | `validate.ts` (flux) → porter |
| **Calcul automatique** (formules/règles) | 🔁 | champs `calculé` avec détection de cycle (flux) → porter |
| **Routage** des demandes selon règles CHU | 🔁 | `approvals/engine.ts` (field/operator/value/level/role) → porter |
| **Déclenchement d'événements** selon données/modif | 🔨 | moteur d'événements/déclencheurs |
| **Processus structurés** (tâches, étapes d'approbation, responsabilités) | ♻️🔁 | `ApprovalWorkflow` (ours) + `flux/domaine` (datées, historique) |
| Gestion **inventaire/capacité** (ex. inscriptions) | 🔨 | compteurs de capacité |

## Bloc 5 — Notifications et communications

| Exigence | Statut | Base |
|---|---|---|
| Notifications **courriel / SMS / messagerie interne** par déclencheur | 🔁 | `notifications/` (ACS courriel, **Twilio/Telnyx SMS**, avis internes) → porter |
| **Confirmation** à la soumission | 🔁🔨 | règle de notification |
| Notification **aux administrateurs** (création/modif de fiche) | 🔁🔨 | règle de notification |
| **Production d'un document** à partir des données + envoi | ♻️ | rendu PDF/DOCX (ours) + notification |
| **Messagerie interne** sécurisée | 🔁🔨 | `avis.ts` (flux) → porter |

## Bloc 6 — Gestion, recherche, rapports, données

| Exigence | Statut | Base |
|---|---|---|
| Recherche multi-critères + **critères sauvegardés** | ♻️🔨 | recherche (ours) ; **sauvegarde des critères** à construire |
| **Export Excel** (toutes fiches / sélection) | 🔨 | XLSX (on a le CSV ; ajouter Excel) |
| **Rapports PDF** personnalisés | ♻️ | rendu PDF |
| **Graphiques / tableaux de bord** configurables | 🔁🔨 | `bi/` cubes + gabarits de rapports (flux) → porter |
| Pages listes/tableaux de bord **selon droits** | 🔁🔨 | RBAC + vues |
| **Export intégral** structuré (réversibilité) | ♻️ | export JSON (réversibilité, art. 20 déjà traité) |
| **Durée de conservation** des données | 🔁 | `auth/retention.ts` (flux, Loi 25/RGPD) → porter |

## Bloc 7 — Sécurité, confidentialité, contrôle des accès

| Exigence | Statut | Base |
|---|---|---|
| **SSO** + méthodes normalisées | ♻️ | OIDC + SAML per-tenant |
| **2FA** admin + politique de mots de passe | ♻️🔁 | TOTP (ours) ; politique MDP + WebAuthn à porter |
| **Groupes** avec droits lecture/modif/suppression | 🔨 | modèle de groupes/ACL |
| Contrôle d'accès **très fin** (utilisateur/groupe/rôle/formulaire/champ/page/fiche/bouton) | 🔁🔨 | `accessPolicy`+`tenantRoles`+`scope` (flux) → porter + étendre |
| **Classification** (public/interne/confidentiel/secret) | 🔨 | niveaux de classification sur champs |
| **Masquage/chiffrement/restriction** d'affichage/export par rôle/contexte | 🔁🔨 | `SecretCipher` (ours) + masquage par classification (nouveau) |
| **Traçabilité** accès + modifications | ♻️🔁 | `AuditLog` (ours) ; option **chaîné SHA-256** (flux) |
| Protection **DDoS / brute-force / malwares** | 🔁🔨 | WAF cloud + `securite/pareFeu.ts` (flux) → porter + **antivirus fichiers** |
| Config pour obligations **RP (Loi 25)** | ♻️ | déjà au cœur (résidence, journal, masquage) |

## Bloc 8 — Interopérabilité et échanges

| Exigence | Statut | Base |
|---|---|---|
| **API** pour consulter/récupérer/créer/compléter des fiches | 🔨 | **API REST publique** (nouveau) |
| Auth API par **jeton / OAuth** (ou équivalent) | 🔨 | OAuth2 *client_credentials* / clés d'API / PAT |
| Échanges formulaires ↔ sites ↔ systèmes externes CHU | ♻️🔨 | module `integration` (ours) + connecteurs |
| **Maintien** des échanges/automatisations/configs en place | 🔨 | compatibilité ascendante (reprise de l'existant CHU) |

## Bloc 9 — Licences, soutien, services professionnels

| Exigence | Statut | Base |
|---|---|---|
| Licences **2 secteurs (RH, approv.) + 4 applications Web** | ♻️🔁 | multi-tenant + **registre de modules** (par secteur/app) |
| Soutien **L2** lun–ven 8 h–17 h | ♻️ | support (niveaux existants) |
| Tests applicatifs lors des migrations/MàJ | ♻️ | environnement d'acceptation + migration d'essai |
| **Test d'intrusion** du portail RH (firme externe) | 🔨 | prestation pentest (sous-traitée) |
| **Banque d'heures** (analyse/formation/config/programmation/rapports) | ♻️ | taux horaire hors-devis (déjà prévu, 120–150 $/h) |

## Verdict

- **~55 % réutilisable/portable immédiatement** (socle Java + conceptions `freelance-invoicing`).
- **Pièce maîtresse à construire** : le **moteur de formulaires piloté par métadonnées** (champs
  dynamiques, logique conditionnelle, aperçu, autosave, fenêtres d'activation, parent-enfant) et le
  **registre de modules superadmin**.
- **À porter en priorité** depuis `freelance-invoicing** : schéma de formulaire + calcul, catalogue de
  modules, notifications, pare-feu applicatif, RBAC granulaire, BI/exports, rétention.