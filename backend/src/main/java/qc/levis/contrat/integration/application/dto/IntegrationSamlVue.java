package qc.levis.contrat.integration.application.dto;

/** Vue de la configuration SAML renvoyée au frontend (le certificat de l'IdP est public). */
public record IntegrationSamlVue(boolean actif, String idpEntityId, String idpSsoUrl,
                                     String idpCertificat, String spEntityId, String claimCourriel,
                                     String boutonLibelle, boolean autoProvision, String roleParDefaut,
                                     boolean certificatDefini) {}
