package qc.levis.contrat.processus.application.dto;

/** Décision sur une étape d'approbation : {@code decision} = APPROUVE | REFUSE, + commentaire. */
public record DecisionApproRequest(String decision, String commentaire) {}
