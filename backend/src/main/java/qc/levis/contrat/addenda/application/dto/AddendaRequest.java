package qc.levis.contrat.addenda.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AddendaRequest(@NotBlank String objet) {}
