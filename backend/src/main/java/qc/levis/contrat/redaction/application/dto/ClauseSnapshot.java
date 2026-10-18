package qc.levis.contrat.redaction.application.dto;

/** Instantané d'une clause dans une version (sérialisé en JSON). */
public record ClauseSnapshot(String code, String titre, String texteEffectif, int ordre) {}
