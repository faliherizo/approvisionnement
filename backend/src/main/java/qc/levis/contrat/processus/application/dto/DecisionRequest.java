package qc.levis.contrat.processus.application.dto;

/** Décision sur une tâche : {@code decision} = APPROUVE | REFUSE | RETOUR, + commentaire (motif). */
public record DecisionRequest(String decision, String commentaire) {}
