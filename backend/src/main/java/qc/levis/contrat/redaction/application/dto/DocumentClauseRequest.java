package qc.levis.contrat.redaction.application.dto;

/** Rédaction locale d'une section du document : texte adapté au dossier ou section libre. */
public record DocumentClauseRequest(String titre, String section, String numero,
                                        String texteEffectif, Integer ordre) {}
