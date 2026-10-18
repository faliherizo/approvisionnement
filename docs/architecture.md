# Architecture de la solution — Logiciel de rédaction contractuelle

**Client :** Ville de Lévis — Avis # 2026-50-92
**Objet :** Solution clé en main de rédaction de clauses administratives et de contrats

---

## 1. Vision d'ensemble

La solution est un **atelier de rédaction contractuelle assistée** qui assemble des
documents (devis, contrats de gré à gré, addendas) à partir d'une **bibliothèque de
clauses paramétrables**. Les clauses s'adaptent automatiquement selon le type de
contrat, le mode de sollicitation, la valeur estimée et le contexte du projet. Un
circuit de **révision / validation hiérarchisée** (approvisionnement, juridique,
directions) encadre chaque document jusqu'à sa génération finale.

Principes directeurs :

- **Conformité québécoise native** : règles métier alignées LCOP/LCM/LCV, seuils de
  sollicitation, Loi 25 (protection des renseignements personnels).
- **Séparation contenu / logique** : la « connaissance juridique » est portée par des
  règles de données, pas par du code, pour permettre l'entretien par des juristes.
- **Traçabilité complète** : versionnage, journal d'audit, historique d'approbation.
- **Hébergement au Canada**, chiffrement au repos et en transit.

---

## 2. Vue logique (couches)

```
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND  — React (SPA)                                          │
│  Login/MDP · Bibliothèque de clauses · Éditeur de devis ·        │
│  Assemblage · Commentaires · Circuit d'approbation · Addendas    │
└───────────────────────────────┬─────────────────────────────────┘
                                 │ HTTPS / REST + JWT
┌───────────────────────────────▼─────────────────────────────────┐
│  BACKEND — Java (Spring Boot)                                     │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────────────┐  │
│  │ Auth & RBAC   │ │ Clause Library │ │ Document Assembly      │  │
│  │ (JWT, rôles)  │ │ Service        │ │ Engine (règles)        │  │
│  └───────────────┘ └────────────────┘ └───────────────────────┘  │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────────────┐  │
│  │ Approval /    │ │ Addenda        │ │ Rendu documentaire     │  │
│  │ Workflow      │ │ Service        │ │ (PDF/DOCX)             │  │
│  └───────────────┘ └────────────────┘ └───────────────────────┘  │
│  ┌───────────────┐ ┌────────────────┐ ┌───────────────────────┐  │
│  │ Comments      │ │ Audit / Journal│ │ Rule Engine (adaptation)│ │
│  └───────────────┘ └────────────────┘ └───────────────────────┘  │
└───────────────────────────────┬─────────────────────────────────┘
                                 │ JPA / JDBC
┌───────────────────────────────▼─────────────────────────────────┐
│  DONNÉES — PostgreSQL (Canada)   +   Stockage objets (documents) │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. Modules fonctionnels

| Module | Responsabilité | Exigence AO couverte |
|--------|----------------|----------------------|
| **Auth & RBAC** | Authentification (login/MDP + JWT), gestion des rôles et des directions | Loi 25, hiérarchisation d'approbation |
| **Clause Library** | CRUD des clauses standards / optionnelles / personnalisées, catégorisation, versionnage | Bibliothèque centralisée |
| **Rule Engine** | Adaptation automatique des clauses selon type de contrat, mode de sollicitation, valeur, contexte, clauses déjà sélectionnées | Adaptation automatique |
| **Document Assembly** | Composition d'un devis/contrat à partir des clauses retenues | Assemblage automatique |
| **Workflow / Approval** | Circuit de validation multi-niveaux, statuts, délégations | Hiérarchisation d'approbation |
| **Comments** | Annotations en ligne dans le devis, fils de discussion | Commenter dans le devis |
| **Addenda** | Rédaction interactive d'addendas rattachés au devis, réintégration | Addendas |
| **Rendu documentaire** | Génération PDF/DOCX finaux intégrant les addendas | Documents finaux |
| **Audit / Journal** | Traçabilité de toutes les actions | Conformité, sécurité |

---

## 4. Modèle de données (noyau)

```
User (id, email, mdp_hash, nom, direction, actif)
Role (id, code)            UserRole (user_id, role_id)

Clause (id, code, titre, categorie[STANDARD|OPTIONNELLE|PERSONNALISEE],
        corps, version, statut, cree_par, maj_le)
ClauseCondition (id, clause_id, attribut[TYPE_CONTRAT|MODE_SOLLICITATION|
        VALEUR|CONTEXTE], operateur, valeur)   -- pilote l'adaptation auto

Document (id, titre, type[DEVIS|CONTRAT_GRE_A_GRE|ADDENDA],
        mode_sollicitation, valeur_estimee, contexte, statut, cree_par)
DocumentClause (document_id, clause_id, ordre, texte_effectif)

Comment (id, document_id, clause_id?, auteur_id, corps, cree_le, resolu)

ApprovalStep (id, document_id, niveau, role_requis, statut, approbateur_id,
        decide_le, note)

Addenda (id, document_parent_id, numero, objet, statut, emis_le)
AddendaChange (id, addenda_id, clause_id, ancien_texte, nouveau_texte)

AuditLog (id, user_id, action, entite, entite_id, horodatage, details)
```

---

## 5. Moteur d'adaptation (règles)

Chaque clause porte un ensemble de **conditions d'inclusion**. À la construction d'un
document, le moteur évalue le *contexte du document* (type, mode, valeur, contexte
projet, clauses déjà retenues) contre les conditions de chaque clause :

```
inclure(clause) =  toutes les ClauseCondition de la clause sont satisfaites
                   par le contexte courant du document
```

Exemples de règles québécoises encodées comme données :

- `MODE_SOLLICITATION = APPEL_OFFRES_PUBLIC` → inclure clauses de garantie de
  soumission, d'AMP (autorisation à contracter), de langue française.
- `VALEUR >= seuil_appel_offres_public` → forcer la procédure ouverte.
- `TYPE_CONTRAT = BAIL` → inclure clauses de bail, exclure clauses de garantie
  d'exécution de travaux.

> Les seuils et références légales sont **des données** maintenues par l'équipe
> juridique, jamais codées en dur — cf. exigence d'entretien par juristes.

---

## 6. Sécurité & conformité

- **Authentification** : login + mot de passe, hachage BCrypt, jeton JWT signé,
  expiration courte + refresh. Verrouillage après tentatives échouées.
- **Autorisation** : RBAC par rôle et par direction (RÉDACTEUR, APPRO, JURIDIQUE,
  DIRECTION, ADMIN).
- **Loi 25** : minimisation des données, journal d'audit, chiffrement AES-256 au
  repos, TLS 1.2+ en transit, hébergement au Canada, politique de conservation.
- **Traçabilité** : chaque décision d'approbation et modification de clause est
  horodatée et attribuée.

---

## 7. Stack technique

| Couche | Technologie |
|--------|-------------|
| Frontend | React 18 (Vite), React Router, Axios |
| Backend | Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA |
| Base de données | PostgreSQL 15 (dév : H2 en mémoire) |
| Génération docs | Apache POI (DOCX) / OpenPDF (PDF) |
| Déploiement | Conteneurs Docker, hébergement infonuagique canadien |
| Observabilité | Actuator, journalisation centralisée |

---

## 8. Portée du prototype livré

Le code fourni (dossiers `backend/` et `frontend/`) implémente le **socle** :
authentification login/MDP avec JWT, RBAC, bibliothèque de clauses, moteur
d'assemblage/adaptation, commentaires et circuit d'approbation. Le rendu DOCX/PDF,
les addendas et l'intégration PostgreSQL de production sont décrits ici et amorcés
dans le code (points d'extension identifiés).
