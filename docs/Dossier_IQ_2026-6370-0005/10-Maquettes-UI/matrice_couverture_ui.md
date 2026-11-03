# Matrice de couverture UI / exigences

| Exigence | Pages UI | Commentaire |
|---|---|---|
| F-01 | UI-01 Tableau de bord, UI-02 Liste des trousses, UI-03 Créer une trousse, UI-04 Détail d’une trousse, UI-05 Documents – ajout et gestion | Couverture à confirmer lors des ateliers de validation |
| F-02 | UI-03 Créer une trousse, UI-08 Éditeur de modèle et clauses, UI-10 Sélection du dossier Salesforce et préremplissage | Couverture à confirmer lors des ateliers de validation |
| F-03 | UI-07 Catalogue des modèles, UI-08 Éditeur de modèle et clauses, UI-09 Configuration des champs | Couverture à confirmer lors des ateliers de validation |
| F-04 | UI-08 Éditeur de modèle et clauses, UI-09 Configuration des champs, UI-15 Préparation de la signature | Couverture à confirmer lors des ateliers de validation |
| F-05 | UI-01 Tableau de bord, UI-04 Détail d’une trousse, UI-11 Concepteur de workflow, UI-12 Boîte de tâches, UI-13 Validation documentaire, UI-14 Approbation | Couverture à confirmer lors des ateliers de validation |
| F-06 | UI-04 Détail d’une trousse, UI-11 Concepteur de workflow, UI-12 Boîte de tâches, UI-15 Préparation de la signature, UI-16 Portail du signataire externe, UI-19 Dossier de preuve et audit | Couverture à confirmer lors des ateliers de validation |
| F-07 | UI-01 Tableau de bord, UI-11 Concepteur de workflow, UI-17 Notifications et rappels | Couverture à confirmer lors des ateliers de validation |
| F-08 | UI-01 Tableau de bord, UI-02 Liste des trousses, UI-04 Détail d’une trousse, UI-12 Boîte de tâches, UI-18 Suivi centralisé et recherche | Couverture à confirmer lors des ateliers de validation |
| F-09 | UI-04 Détail d’une trousse, UI-05 Documents – ajout et gestion, UI-06 Historique et versions documentaires, UI-13 Validation documentaire, UI-14 Approbation, UI-16 Portail du signataire externe, UI-19 Dossier de preuve et audit, UI-23 Journal d’audit et événements | Couverture à confirmer lors des ateliers de validation |
| F-10 | UI-01 Tableau de bord, UI-03 Créer une trousse, UI-04 Détail d’une trousse, UI-09 Configuration des champs, UI-10 Sélection du dossier Salesforce et préremplissage, UI-18 Suivi centralisé et recherche, UI-21 Administration des intégrations API | Couverture à confirmer lors des ateliers de validation |
| F-11 | UI-02 Liste des trousses, UI-07 Catalogue des modèles, UI-12 Boîte de tâches, UI-18 Suivi centralisé et recherche, UI-19 Dossier de preuve et audit, UI-20 Administration des utilisateurs et groupes, UI-21 Administration des intégrations API, UI-23 Journal d’audit et événements | Couverture à confirmer lors des ateliers de validation |
| F-12 | — | Couverture à confirmer lors des ateliers de validation |
| F-13 | UI-24 Options CLM et IA | Couverture à confirmer lors des ateliers de validation |

## État d'intégration dans la solution (au 2026-11-03) — ne plus réanalyser

Les 24 maquettes sont intégrées dans le gabarit commun du module (`ClmShell`, ADR-007). La colonne « e2e » renvoie à
`e2e/views/clm.view.spec.ts` : géométrie (débordement, chevauchement, cibles), libellés et axe-core sans violation.
F-12 (mobile) est couverte par UI-16 et la story RST-19.

| Maquette | Route ou emplacement | e2e |
|---|---|---|
| UI-01 Tableau de bord | `/clm` | 4 largeurs |
| UI-02 Liste des trousses | `/clm/trousses` | 4 largeurs |
| UI-03 Créer une trousse | `/clm/trousses/nouvelle` (assistant en 5 étapes) | 4 largeurs + 5 étapes avec données |
| UI-04 Détail d'une trousse | `/clm/trousses/:id` (onglets) | 390 et 1280 px, 6 onglets |
| UI-05 Documents | onglet Documents, visionneuse PDF/DOCX | prévisualisation |
| UI-06 Historique et versions | onglets Documents (versions) et Historique | onglet Historique |
| UI-07 Catalogue des modèles | `/clm/modeles` | 4 largeurs |
| UI-08 / UI-09 Éditeur de modèle, champs | `/clm/modeles/:id` | champ sélectionné |
| UI-10 Dossier Salesforce et préremplissage | étape Source de l'assistant UI-03 | étapes de l'assistant |
| UI-11 Concepteur de workflow | onglet Parcours | brouillon et en traitement |
| UI-12 Boîte de tâches | `/clm/taches` | 4 largeurs |
| UI-13 / UI-14 Validation, approbation | décision depuis la boîte de tâches | validation (juriste) |
| UI-15 Préparation de la signature | onglet Participants | onglet Participants |
| UI-16 Portail du signataire | `/portail/:jeton` (public) | 390 et 768 px, 8 états, cibles de 44 px ; lien invalide |
| UI-17 Notifications et rappels | `/clm/admin/notifications` | 4 largeurs |
| UI-18 Suivi centralisé | `/clm/suivi` | 4 largeurs |
| UI-19 Dossier de preuve | onglet Preuve | onglet Preuve |
| UI-20 Utilisateurs et groupes | `/clm/admin/utilisateurs`, `/clm/admin/groupes` | utilisateurs, 4 largeurs |
| UI-21 Intégrations API | `/clm/admin/integrations` | 4 largeurs |
| UI-22 Paramètres | `/clm/admin/parametres` | 4 largeurs |
| UI-23 Journal d'audit | `/clm/admin/journal` | 4 largeurs |
| UI-24 Options CLM et IA | `/clm/options` | 4 largeurs |
