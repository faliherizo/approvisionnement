package qc.levis.contrat.integration.application.dto;

/** Saisie de la configuration OIDC d'un tenant (espace admin). Secret client write-only. */
public record IntegrationOidcRequest(Boolean actif, String issuer, String clientId,
                                         String clientSecret, String scopes, String boutonLibelle,
                                         String claimCourriel, Boolean autoProvision,
                                         String roleParDefaut) {}
