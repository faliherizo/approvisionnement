package qc.levis.contrat.processus.domain;

/** Cycle de vie d'une tâche (architecture §15.2). Une tâche terminée n'est pas rouverte (nouvelle tâche). */
public enum StatutTache {
    OUVERTE,      // OPEN
    EN_COURS,     // IN_PROGRESS
    TERMINEE,     // COMPLETED
    ANNULEE       // CANCELLED
}
