package qc.levis.contrat.integration.application.dto;

/** Saisie de la configuration SAML 2.0 (SP) d'un tenant (espace admin). */
public record IntegrationSamlRequest(Boolean actif, String idpEntityId, String idpSsoUrl,
                                         String idpCertificat, String spEntityId, String claimCourriel,
                                         String boutonLibelle, Boolean autoProvision,
                                         String roleParDefaut) {}
