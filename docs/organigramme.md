# Organigramme organique du projet

**Avis # 2026-50-92 — Logiciel de rédaction contractuelle, Ville de Lévis**

Ce document décrit **l'organisation des acteurs** (côté Ville et côté prestataire) et
les **rôles applicatifs** (RBAC) qui en découlent dans la solution.

---

## 1. Structure de gouvernance du projet

```
                       ┌─────────────────────────────┐
                       │   COMITÉ DE PILOTAGE          │
                       │   (Direction générale Lévis)  │
                       └───────────────┬───────────────┘
                                       │
             ┌─────────────────────────┼─────────────────────────┐
             │                         │                          │
   ┌─────────▼─────────┐   ┌───────────▼──────────┐   ┌───────────▼──────────┐
   │ Direction de       │   │ Direction des        │   │ Directions           │
   │ l'approvisionnement│   │ services juridiques  │   │ requérantes          │
   │ (pilote métier)    │   │ (validation légale)  │   │ (besoins terrain)    │
   └─────────┬──────────┘   └───────────┬──────────┘   └───────────┬──────────┘
             │                          │                          │
             └──────────────┬───────────┴──────────────┬───────────┘
                            │                           │
                  ┌─────────▼─────────┐       ┌─────────▼──────────┐
                  │ Chef de projet     │       │ Sécurité de        │
                  │ Ville (produit)    │       │ l'information (TI) │
                  └─────────┬──────────┘       └────────────────────┘
                            │
        ═══════════════════ FRONTIÈRE VILLE / PRESTATAIRE ═══════════════════
                            │
                  ┌─────────▼─────────────────────────────┐
                  │   PRESTATAIRE (fournisseur solution)   │
                  ├────────────────────────────────────────┤
                  │  • Chef de projet prestataire          │
                  │  • Architecte / équipe développement   │
                  │  • Juriste(s) — assistance juridique   │
                  │  • Soutien technique (FR, 9h–16h HE)   │
                  │  • Formateur (formation FR documentée) │
                  └────────────────────────────────────────┘
```

---

## 2. Rôles applicatifs (RBAC) et responsabilités

| Rôle applicatif | Acteur organisationnel | Permissions clés |
|-----------------|------------------------|------------------|
| **ADMIN** | TI / prestataire | Gestion utilisateurs, rôles, paramètres, seuils |
| **REDACTEUR** | Direction de l'approvisionnement | Créer/éditer devis, sélectionner clauses, lancer l'assemblage |
| **JURIDIQUE** | Services juridiques | Valider les clauses, commenter, approuver le niveau « légal » |
| **APPRO** | Approvisionnement (validation) | Approuver le niveau « approvisionnement » |
| **DIRECTION** | Directions requérantes | Approuver le niveau « direction », commenter |
| **LECTEUR** | Intervenants consultés | Lecture seule, commentaires |

Un utilisateur appartient à une **direction** et porte un ou plusieurs rôles. Le
circuit d'approbation (voir BPMN) mobilise ces rôles par niveau hiérarchique.

---

## 3. Matrice RACI (macro)

| Activité | Rédacteur | Juridique | Appro | Direction | Prestataire |
|----------|:---------:|:---------:|:-----:|:---------:|:-----------:|
| Rédiger le devis | **R** | C | A | C | — |
| Adapter les clauses (auto) | R | C | A | I | (outil) |
| Valider juridiquement | I | **R/A** | C | I | C (juriste) |
| Approuver le document | I | C | **A** | A | — |
| Émettre un addenda | **R** | C | A | I | — |
| Générer le document final | R | I | A | I | — |
| Support / formation | I | I | I | I | **R/A** |

R = Réalise · A = Approuve · C = Consulté · I = Informé
