package qc.levis.contrat.integration.application.dto;

/** Saisie de la configuration SAP d'un tenant (espace admin). Secrets write-only. */
public record IntegrationSapRequest(Boolean actif, String baseUrl, String servicePath, String auth,
                                        String username, String password, String apiKey) {}
