package qc.levis.contrat.processus.application.dto;

/** Création d'une règle d'approbation. */
public record RegleApproRequest(String documentType, String champ, String operateur, String valeur,
                                int niveau, String approbateurRole, String approbateurId) {}
