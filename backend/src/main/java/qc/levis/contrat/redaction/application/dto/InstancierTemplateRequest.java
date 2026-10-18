package qc.levis.contrat.redaction.application.dto;

import jakarta.validation.constraints.NotBlank;

/** Création d'un document à partir d'un template. */
public record InstancierTemplateRequest(
            @NotBlank String titre,
            java.math.BigDecimal valeurEstimee,
            String contexte) {}
