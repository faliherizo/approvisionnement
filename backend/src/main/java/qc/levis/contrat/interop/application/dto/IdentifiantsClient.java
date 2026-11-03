package qc.levis.contrat.interop.application.dto;

/** Identifiants d'un client d'API retournés UNE SEULE FOIS à la création (le secret n'est plus récupérable). */
public record IdentifiantsClient(String clientId, String secret) {}
