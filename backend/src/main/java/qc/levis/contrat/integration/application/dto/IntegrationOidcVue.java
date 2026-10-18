package qc.levis.contrat.integration.application.dto;

/** Vue de la configuration OIDC renvoyée au frontend : SANS le secret (juste sa présence). */
public record IntegrationOidcVue(boolean actif, String issuer, String clientId, String scopes,
                                     String boutonLibelle, String claimCourriel, boolean autoProvision,
                                     String roleParDefaut, boolean secretDefini) {}
