package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import qc.levis.contrat.commun.domain.Enums;

public record ObligationRequest(
            @NotBlank String titre,
            String description,
            @NotNull Enums.ObligationType type,
            java.time.LocalDate dateEcheance,
            String responsable) {}
