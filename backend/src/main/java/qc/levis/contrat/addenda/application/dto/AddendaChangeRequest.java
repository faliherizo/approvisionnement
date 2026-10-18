package qc.levis.contrat.addenda.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddendaChangeRequest(
            @NotNull Long clauseId,
            @NotBlank String nouveauTexte) {}
