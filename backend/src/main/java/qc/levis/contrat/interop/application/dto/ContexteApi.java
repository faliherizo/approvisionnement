package qc.levis.contrat.interop.application.dto;

/** Contexte résolu à partir d'un jeton d'API valide. */
public record ContexteApi(Long tenantId, String clientId) {}
