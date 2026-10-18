package qc.levis.contrat.soumission.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SignerRequest(@NotBlank String nomComplet, String signatureImage) {}
