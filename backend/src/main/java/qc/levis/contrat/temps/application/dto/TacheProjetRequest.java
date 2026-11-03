package qc.levis.contrat.temps.application.dto;

/** Création/mise à jour d'une tâche de projet (assignable à une personne). */
public record TacheProjetRequest(Long projetId, String nom, String description, String typeTache,
                                 String statut, Long assigneId, Double heuresEstimees, String echeance) {}
