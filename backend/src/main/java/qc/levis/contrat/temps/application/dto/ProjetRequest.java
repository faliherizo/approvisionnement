package qc.levis.contrat.temps.application.dto;

/** Création/mise à jour d'un projet. */
public record ProjetRequest(String code, String nom, String gestionnaire, Double budgetHeures) {}
