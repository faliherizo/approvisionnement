# Roadmap de mise en œuvre

**Avis # 2026-50-92 — Logiciel de rédaction contractuelle, Ville de Lévis**

Roadmap élaboré **après** la modélisation BPMN 2.0 (cf. [`bpmn.md`](./bpmn.md)),
l'architecture ([`architecture.md`](./architecture.md)) et l'organigramme
([`organigramme.md`](./organigramme.md)).

---

## Phase 0 — Cadrage & conformité (semaines 1-3)

- Atelier de cadrage avec Approvisionnement, Juridique et TI.
- Validation du processus BPMN 2.0 et des rôles (RACI).
- Recensement légal : seuils LCOP/LCM/LCV, exigences Loi 25, hébergement Canada.
- Questionnaire sécurité de l'information (exigence AO) : plan de réponse.
- **Livrables :** cahier de conformité, backlog priorisé, environnements dév.

## Phase 1 — Socle technique & authentification (semaines 3-6) ✅ *prototype livré*

- Backend Spring Boot : sécurité JWT, RBAC, modèle de données noyau.
- Frontend React : écran de connexion (login/MDP), routage protégé, session.
- Journal d'audit transversal.
- **Livrables :** `backend/` + `frontend/` opérationnels, connexion fonctionnelle.

## Phase 2 — Bibliothèque de clauses (semaines 6-10)

- CRUD clauses standards / optionnelles / personnalisées, catégories, versionnage.
- Import initial du corpus de clauses de la Ville.
- Interface d'entretien par les juristes (sans code).
- **Livrables :** bibliothèque centralisée alimentée et gérée.

## Phase 3 — Moteur d'adaptation & assemblage (semaines 10-15)

- Rule Engine : conditions d'inclusion (type, mode, valeur, contexte, clauses liées).
- Encodage des règles québécoises comme **données** maintenables.
- Assemblage automatique du devis à partir des clauses retenues.
- **Livrables :** génération d'un devis adapté aux critères objectifs.

## Phase 4 — Révision, commentaires & approbation (semaines 15-20)

- Commentaires en ligne dans le devis (fils, résolution).
- Circuit d'approbation hiérarchisé multi-niveaux (Appro, Juridique, Directions).
- Notifications, tableaux de bord d'état.
- **Livrables :** collaboration + validation conformes au BPMN.

## Phase 5 — Addendas & rendu documentaire (semaines 20-25)

- Rédaction interactive d'addendas rattachés au devis.
- Réintégration des addendas et re-approbation.
- Génération PDF/DOCX finaux intégrant tous les addendas.
- **Livrables :** documents contractuels finaux pour la Ville et le cocontractant.

## Phase 6 — Contrats de gré à gré & modèles étendus (semaines 25-29)

- Modèles : prêts à usage, baux, contrats de services, etc.
- Personnalisation avancée des contrats de gré à gré.
- **Livrables :** couverture complète des types de contrats visés.

## Phase 7 — Durcissement, sécurité & Loi 25 (semaines 29-33)

- Chiffrement au repos/transit, tests d'intrusion, réponse au questionnaire sécurité.
- Politique de conservation, gestion des accès, revue Loi 25.
- **Livrables :** dossier de sécurité, conformité validée.

## Phase 8 — Formation, support & mise en production (semaines 33-37)

- Formation FR documentée, complète et détaillée.
- Mise en place du soutien technique et de l'assistance juridique (9h-16h HE).
- Reprise de données, bascule production, garantie.
- **Livrables :** solution en production, équipe formée, support actif.

---

## Jalons clés

| Jalon | Fin de phase | Critère de succès |
|-------|--------------|-------------------|
| J1 — Connexion & socle | Phase 1 | Authentification JWT + RBAC opérationnels |
| J2 — Bibliothèque | Phase 2 | Corpus de clauses géré par les juristes |
| J3 — Devis adapté | Phase 3 | Assemblage automatique selon les 5 critères |
| J4 — Validation | Phase 4 | Circuit d'approbation + commentaires |
| J5 — Documents finaux | Phase 5 | PDF/DOCX avec addendas |
| J6 — Mise en production | Phase 8 | Formation + support conformes à l'AO |

## Risques & mitigations

| Risque | Mitigation |
|--------|------------|
| Évolution du cadre légal (LCOP/LCM/LCV) | Règles en données, entretien juriste |
| Adoption utilisateurs | Formation FR, ergonomie, pilote |
| Conformité Loi 25 | Hébergement Canada, audit, chiffrement |
| Qualité du corpus de clauses | Import contrôlé, versionnage, validation juridique |
