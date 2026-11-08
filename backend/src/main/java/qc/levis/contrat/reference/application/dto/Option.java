package qc.levis.contrat.reference.application.dto;

/** Option servie aux sélecteurs du frontend ; {@code detail} : complément propre à la liste, s'il y en a un. */
public record Option(String code, String libelle, String detail) {}
