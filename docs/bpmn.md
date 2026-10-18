# Diagramme BPMN 2.0 — Processus de rédaction contractuelle

**Avis # 2026-50-92 — Ville de Lévis**

Ce processus modélise le cycle de vie d'un document contractuel, de l'expression du
besoin à la génération finale intégrant les addendas. Il précède et cadre le roadmap.

Un fichier **BPMN 2.0 XML** valide (importable dans Camunda Modeler, bpmn.io,
Signavio…) accompagne ce document : [`processus-redaction.bpmn`](./processus-redaction.bpmn).

---

## 1. Vue d'ensemble (pools & lanes)

- **Pool : Ville de Lévis** — lanes : *Direction requérante*, *Approvisionnement
  (Rédacteur)*, *Services juridiques*, *Approbateurs (Appro / Direction)*.
- **Pool : Système (logiciel de rédaction)** — tâches automatisées : adaptation des
  clauses, assemblage, génération documentaire.

---

## 2. Flux du processus (notation BPMN 2.0)

```
[⬤ Début: Besoin exprimé]
        │
        ▼
(Direction requérante) ─ Tâche: Définir le besoin (type contrat, valeur estimée, contexte)
        │
        ▼
(Rédacteur) ─ Tâche: Paramétrer le document
              (type · mode de sollicitation · valeur · contexte)
        │
        ▼
(Système) ⚙ Tâche auto: Adapter les clauses selon les critères  ──┐
        │                                                          │ (règles LCOP/LCM/LCV, seuils)
        ▼                                                          │
(Rédacteur) ─ Tâche: Sélectionner / ajuster les clauses  ◄────────┘
        │
        ▼
(Système) ⚙ Tâche auto: Assembler le devis
        │
        ▼
   ◇ Passerelle exclusive: Type de sollicitation ?
        ├─ [Gré à gré] ──────────────► (voir branche approbation)
        └─ [Appel d'offres public] ──► (voir branche approbation)
        │
        ▼
◆ Passerelle parallèle: Révision multi-intervenants
        ├──► (Juridique) Tâche: Valider juridiquement + commenter
        ├──► (Direction)  Tâche: Réviser + commenter
        └──► (Appro)      Tâche: Réviser + commenter
        │  (synchronisation)
        ▼
   ◇ Passerelle exclusive: Corrections requises ?
        ├─ [Oui] ──► retour à « Sélectionner / ajuster les clauses »
        └─ [Non] ──► suite
        │
        ▼
(Approbateurs) ─ Tâche: Approbation hiérarchisée (niveaux 1..n)
        │
        ▼
   ◇ Passerelle exclusive: Approuvé ?
        ├─ [Refusé] ──► retour à « Sélectionner / ajuster les clauses »
        └─ [Approuvé] ──► suite
        │
        ▼
◷ Événement intermédiaire: Publication / mise en concurrence
        │
        ▼
   ◇ Passerelle exclusive: Addenda nécessaire ?
        ├─ [Oui] ──► (Rédacteur) Tâche: Rédiger addenda interactif
        │                 │
        │                 ▼
        │            (Système) ⚙ Réintégrer l'addenda au document
        │                 │
        │                 └──► retour à « Approbation hiérarchisée »
        └─ [Non] ──► suite
        │
        ▼
(Système) ⚙ Tâche auto: Générer les documents finaux (PDF/DOCX)
              intégrant l'ensemble des addendas
        │
        ▼
[⬤ Fin: Document contractuel émis à la Ville et au cocontractant]
```

**Légende BPMN 2.0** — ⬤ événement début/fin · ◇ passerelle exclusive (XOR) ·
◆ passerelle parallèle (AND) · ◷ événement intermédiaire · ⚙ tâche de service
(automatisée) · les autres sont des tâches utilisateur.

---

## 3. Correspondance BPMN → modules de la solution

| Étape BPMN | Module applicatif |
|------------|-------------------|
| Paramétrer le document | Document Assembly (création) |
| Adapter les clauses (auto) | Rule Engine |
| Assembler le devis | Document Assembly |
| Valider / réviser / commenter | Comments + Workflow |
| Approbation hiérarchisée | Approval / Workflow |
| Rédiger addenda | Addenda Service |
| Générer documents finaux | Rendu documentaire |
| (transversal) | Audit / Journal |
