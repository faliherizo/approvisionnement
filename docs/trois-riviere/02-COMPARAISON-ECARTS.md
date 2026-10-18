# Comparaison exigences ↔ solution VINA — écarts

> Confronte chaque exigence de l'appel d'offres n° 7000017905 (voir
> [01-ANALYSE-EXIGENCES.md](01-ANALYSE-EXIGENCES.md)) à la solution **VINA — rédaction contractuelle**
> (contractuelle_contrat) telle que déployée aujourd'hui. La conception pour combler les écarts,
> **sans duplication de module**, est dans [03-CONCEPTION-INTEGRATION.md](03-CONCEPTION-INTEGRATION.md).
>
> Légende : ✅ **Couvert** · 🟡 **Partiel** (existe mais à compléter) · ❌ **Manquant**.

## 1. Correspondance fonctionnelle (art. 7)

| Réf | Exigence | Module VINA existant | Statut | Écart / action |
|-----|----------|----------------------|:------:|----------------|
| 7.1 | Modèles documentaires personnalisés, classés par catégorie | `DocumentTemplate` + `Gabarit` (TemplateManager/GabaritManager) | ✅ | Ajouter un axe de **classement par « type d'acquisition »** (catégorie) sur le template |
| 7.2 | Génération auto (clauses/sections selon paramètres) + export Word/PDF | `DocumentService.assembler` + `RuleEngine` + `ContractComposer` + `Pdf/DocxContractRenderer` | ✅ | — (Word + PDF déjà générés) |
| 7.2 | **Parcours guidé / questionnaire / assistant** de génération | *(assistant IA existant, mais pas de wizard pas-à-pas)* | 🟡 | Ajouter un **assistant guidé** réutilisant `RuleEngine`/`TemplateService` |
| 7.3 | Bibliothèque centralisée de contenus réutilisables + droits | `Clause` (STANDARD/OPTIONNELLE/PERSONNALISÉE) + RBAC | ✅ | — |
| 7.3 | **Diffusion des nouvelles versions** de clauses aux documents | *(texte effectif copié à l'assemblage)* | 🟡 | Ajouter **notification/rafraîchissement** des clauses mises à jour dans les documents ouverts |
| 7.4 | Historique des versions (auteur, date, nature) | `DocumentVersion` + redlining (`DiffUtil`, VersionController) | ✅ | — |
| 7.4 | Recherche par titre, mots-clés, catégorie, type, **contenu** | `SearchController` (recherche plein texte) | 🟡 | Élargir la recherche aux **contenus réutilisables** et aux facettes (catégorie/type) |
| 7.5 | Révision collaborative + suivi des contributions | `Comment` + `DocumentVersion` | ✅ | — |
| 7.5 | Commentaires : ajout, consultation, **résolution** | `Comment` (sans statut de résolution) | 🟡 | Ajouter **statut « résolu »** + fil de discussion |
| 7.5 | Workflows de validation **configurables** (rôle/type doc/type dossier) | `ApprovalStep` (circuit hiérarchisé fixe) | 🟡 | Rendre le **circuit configurable** (gabarit de workflow par type) |

## 2. Technique, environnement, intégrations (art. 8, 9)

| Réf | Exigence | Existant | Statut | Écart / action |
|-----|----------|----------|:------:|----------------|
| 8.1 | Interface conviviale, **français**, gestion utilisateurs | React + i18n FR + `Users`/RBAC | ✅ | — |
| 9 | **Microsoft 365 / Word / SharePoint / Teams / OneDrive / Outlook** | Export **Word (.docx)** + PDF | 🟡→❌ | Word ✅ ; **dépôt SharePoint/OneDrive, envoi Outlook, notif Teams = manquants** |
| 9 | **SAP** (financier / approvisionnement) | *(aucun)* | ❌ | **Connecteur SAP** (référence dossier, n° AO, montants) |
| 9 | **Authentification corporative (SSO)** | OIDC **Entra ID** (profil `azure`) | ✅ | — (activer en prod) |

## 3. Sécurité, conformité, données, IA, migration (art. 14–22)

| Réf | Exigence | Existant | Statut | Écart / action |
|-----|----------|----------|:------:|----------------|
| 15 | **Regroupement par dossier d'approvisionnement** (docs + versions + validations + historiques) | *(centré Document, pas de dossier)* | ❌ | Nouveau **agrégat « Dossier d'approvisionnement »** regroupant plusieurs documents |
| 15 | **Rapports de suivi** (docs générés, validations, utilisateurs, dossiers) | `StatsController` (indicateurs globaux) | 🟡 | Ajouter des **rapports par dossier / par utilisateur** |
| 15 | Config aux règles internes, séparation des responsabilités, auditabilité | RBAC + `AuditLog` + versions | ✅ | — |
| 16 | Sauvegarde/DR/chiffrement, vuln, surveillance, incidents ; ISO 27001/SOC 2 ; RTO/RPO | Chiffrement en transit ; Azure HA (doc déploiement) | 🟡 | **Infra/ops** : formaliser DR/RTO/RPO, chiffrement au repos, certifications (hors code) |
| 17 | SSO, MFA, rôles/permissions | Entra ID + MFA (TOTP/questions) + RBAC | ✅ | — |
| 18 | Journalisation (connexions, modifs, validations, approbations, **exports, suppressions, droits**) **consultable + exportable** | `AuditLog`/`AuditService` (écriture) | 🟡 | **UI de consultation + export** des journaux ; compléter les types d'événements |
| 19 | Sous-traitants, notification incident, conservation/destruction, **pas d'entraînement IA** | IA **locale (Ollama)** → aucune donnée externe | ✅ | — (politique + IA locale conformes) |
| 20 | **Réversibilité** : export ouvert de *toutes* les données + restitution/destruction | Export **par document** (PDF/DOCX) | ❌ | **Service de réversibilité** : export complet tenant (JSON + Word) |
| 21 | IA identifiable, validation humaine, **traçabilité des suggestions**, **désactivation** | Assistant IA local + RAG | 🟡 | Ajouter **journal des suggestions IA** + **interrupteur d'activation** par tenant |
| 22 | **Migration des contenus existants** + **migration d'essai** | Seeders (démarrage) | ❌ | **Import administrable** (clauses/modèles/documents) + **migration d'essai** validée |

## 4. Implantation, accessibilité, langue (art. 10–12, 23–25)

| Réf | Exigence | Existant | Statut | Écart |
|-----|----------|----------|:------:|-------|
| 10–12, 23–24 | Implantation, formation, soutien, gouvernance, acceptation | Processus de service + déploiement Docker/Azure | ✅ | Volet **services** (offre), pas logiciel |
| 25 | Accessibilité + français + navigateurs + doc | **SGQRI 008 3.0 / WCAG 2.2 AA** (passe a11y réalisée) + i18n FR | ✅ | Maintenir la porte a11y sur les nouveaux écrans |

## 5. Soumissionnaire / cadre contractuel (art. 13, Annexe A, hébergement)

| Réf | Exigence | Existant | Statut | Écart |
|-----|----------|----------|:------:|-------|
| 13 | Expérience municipale/publique + références | Réponses / références (INSPQ, Revenu Québec, transport scolaire, Lévis) | ✅ | Volet **soumission** |
| Annexe A | Gestion des vulnérabilités (CVSS, correctifs, balayages, notification) | Trivy dans la CI/CD (doc OVH) | 🟡 | Formaliser **processus vuln + SLA de correctif** (ops, hors périmètre logiciel applicatif) |
| Hébergé | Disponibilité mensuelle, incidents, protection données | Azure HA (doc) | 🟡 | **Contractuel/ops** (SLA) |

---

## 6. Synthèse des écarts logiciels à combler (priorisés)

**Écarts structurants (❌)**
1. **Dossier d'approvisionnement** — agrégat regroupant plusieurs documents d'un même dossier (art. 15).
2. **Intégrations M365/SharePoint/OneDrive/Outlook/Teams et SAP** — dépôt/publication/envoi (art. 9).
3. **Réversibilité** — export complet et ouvert de toutes les données du tenant (art. 20).
4. **Import / migration** des contenus existants + migration d'essai (art. 22).

**Écarts d'complétion (🟡)**
5. Assistant **guidé** de génération (art. 7.2) · 6. **Diffusion** des clauses mises à jour (7.3) ·
7. **Résolution** des commentaires + **workflow configurable** (7.5) · 8. **Journal d'audit
consultable/exportable** (18) · 9. **Traçabilité IA + désactivation** (21) · 10. **Recherche
enrichie** (7.4) · 11. **Rapports par dossier** (15).

**Déjà couvert (✅)** : bibliothèque de clauses, moteur de règles, génération PDF/**Word**,
versions/redlining, commentaires, circuit d'approbation, gabarits, templates, addendas, RBAC,
MFA/SSO Entra, IA locale (Loi 25), accessibilité WCAG 2.2 / SGQRI, français, multi-tenant, audit.

> **Aucun de ces écarts n'exige un module en double** : chacun **étend un contexte existant** ou
> introduit un **nouveau contexte** clairement séparé — détaillé dans
> [03-CONCEPTION-INTEGRATION.md](03-CONCEPTION-INTEGRATION.md).
