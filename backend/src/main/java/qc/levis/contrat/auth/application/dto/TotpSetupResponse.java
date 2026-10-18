package qc.levis.contrat.auth.application.dto;

// Configuration du second facteur
public record TotpSetupResponse(String secret, String uriOtpauth) {}
