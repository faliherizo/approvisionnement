package qc.levis.contrat.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TotpEnableRequest(@NotBlank String code) {}
