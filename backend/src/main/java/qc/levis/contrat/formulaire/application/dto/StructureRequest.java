package qc.levis.contrat.formulaire.application.dto;

/** Mise à jour de la structure (métadonnées JSON) d'une version brouillon. */
public record StructureRequest(String structureJson, String note) {}
