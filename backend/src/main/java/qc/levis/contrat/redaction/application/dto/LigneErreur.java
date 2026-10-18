package qc.levis.contrat.redaction.application.dto;

/** Erreur de validation sur une ligne du fichier importé. */
public record LigneErreur(int ligne, String message) {}
