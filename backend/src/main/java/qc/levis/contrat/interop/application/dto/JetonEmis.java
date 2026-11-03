package qc.levis.contrat.interop.application.dto;

import java.time.Instant;

/** Jeton d'accès émis (opaque) et sa date d'expiration. */
public record JetonEmis(String accessToken, String tokenType, Instant expireLe) {}
