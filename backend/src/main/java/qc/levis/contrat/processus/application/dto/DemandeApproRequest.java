package qc.levis.contrat.processus.application.dto;

import java.util.Map;

/** Création d'une demande d'approbation : type de document, id, et contexte (ex. {montant: 25000}). */
public record DemandeApproRequest(String documentType, String documentId, Map<String, Object> contexte) {}
