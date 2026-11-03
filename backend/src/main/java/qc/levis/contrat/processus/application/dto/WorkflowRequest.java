package qc.levis.contrat.processus.application.dto;

/** Enregistrement de la définition d'un workflow (étapes/conditions en JSON) pour un formulaire. */
public record WorkflowRequest(String definitionJson) {}
