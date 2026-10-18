package qc.levis.contrat.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionItem(@NotBlank String question, @NotBlank String reponse) {}
