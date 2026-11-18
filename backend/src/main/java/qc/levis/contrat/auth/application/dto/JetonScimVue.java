package qc.levis.contrat.auth.application.dto;

import java.time.Instant;

/** État du jeton SCIM ; {@code jeton} n'est rempli qu'à la création, seule fois où il est remis en clair. */
public record JetonScimVue(boolean configure, String jeton, String creePar, Instant creeLe, String point) {
}
