package qc.levis.contrat.soumission.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LigneRequest(@NotBlank String description,
                               java.math.BigDecimal quantite, java.math.BigDecimal taux) {}
