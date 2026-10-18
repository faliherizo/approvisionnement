package qc.levis.contrat.ia.application.dto;

/** Clause éligible pré-visualisée pour le contexte (sans persistance). */
public record ClauseApercu(Long id, String code, String titre, String categorie, boolean normative) {}
