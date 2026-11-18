package qc.levis.contrat.auth.application.dto;

import java.time.Instant;

public record CorrespondanceVue(Long id, String source, String attribut, String valeur, String role, String groupe,
                                String modifiePar, Instant modifieLe) {
}
