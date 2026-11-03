package qc.levis.contrat.temps.application.dto;

/** Vue d'un projet. */
public record ProjetVue(Long id, String code, String nom, String gestionnaire, Double budgetHeures, boolean actif) {}
