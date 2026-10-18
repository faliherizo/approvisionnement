package qc.levis.contrat.integration.application.dto;

/** Vue de la configuration SAP renvoyée au frontend : SANS les secrets (juste leur présence). */
public record IntegrationSapVue(boolean actif, String baseUrl, String servicePath, String auth,
                                    String username, boolean passwordDefini, boolean apiKeyDefini) {}
